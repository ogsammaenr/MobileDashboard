package media

import (
	"bytes"
	"fmt"
	"io"
	"net/http"
	"net/url"
	"os/exec"
	"regexp"
	"strconv"
	"strings"
	"sync"
	"time"

	"github.com/mobiledashboard/backend-go/internal/apps"
	"github.com/mobiledashboard/backend-go/internal/models"
)

var (
	ytRegex       = regexp.MustCompile(`(?:youtu\.be/|v/|u/\w/|embed/|shorts/|watch\?v=|&v=)([a-zA-Z0-9_-]{11})`)
	ytSearchRegex = regexp.MustCompile(`"videoId":"([a-zA-Z0-9_-]{11})"`)
)

type Controller struct {
	ytCacheMu sync.RWMutex
	ytCache   map[string]string
	fetching  map[string]bool
	fetchMu   sync.Mutex
	client    *http.Client
}

func NewController() *Controller {
	return &Controller{
		ytCache:  make(map[string]string),
		fetching: make(map[string]bool),
		client: &http.Client{
			Timeout: 4 * time.Second,
		},
	}
}

type playerCandidate struct {
	status    string
	title     string
	artist    string
	album     string
	artURL    string
	position  int64
	length    int64
	player    string
	xesamURL  string
	trackID   string
	score     int
}

// GetMedia queries all active MPRIS media players and selects the best active stream.
func (c *Controller) GetMedia() models.MediaData {
	media := models.MediaData{
		Title:  "Çalan Medya Yok",
		Artist: "--",
		Album:  "--",
		Status: "Stopped",
	}

	// Read playerctl metadata with custom delimiter for ALL active players (-a)
	// Format: {{status}}||{{title}}||{{artist}}||{{album}}||{{mpris:artUrl}}||{{position}}||{{mpris:length}}||{{playerName}}||{{xesam:url}}||{{mpris:trackid}}
	formatStr := "{{status}}||{{title}}||{{artist}}||{{album}}||{{mpris:artUrl}}||{{position}}||{{mpris:length}}||{{playerName}}||{{xesam:url}}||{{mpris:trackid}}"
	cmd := exec.Command("playerctl", "-a", "metadata", "--format", formatStr)
	var out bytes.Buffer
	cmd.Stdout = &out
	if err := cmd.Run(); err != nil {
		// Fallback to default single playerctl call if -a is unsupported
		cmdSingle := exec.Command("playerctl", "metadata", "--format", formatStr)
		cmdSingle.Stdout = &out
		if err := cmdSingle.Run(); err != nil {
			return media
		}
	}

	raw := strings.TrimSpace(out.String())
	if raw == "" {
		return media
	}

	lines := strings.Split(raw, "\n")
	var candidates []playerCandidate

	for _, line := range lines {
		line = strings.TrimSpace(line)
		if line == "" {
			continue
		}

		parts := strings.Split(line, "||")
		if len(parts) < 4 {
			continue
		}

		cand := playerCandidate{}
		if len(parts) >= 1 {
			cand.status = strings.TrimSpace(parts[0])
		}
		if len(parts) >= 2 {
			cand.title = strings.TrimSpace(parts[1])
		}
		if len(parts) >= 3 {
			cand.artist = strings.TrimSpace(parts[2])
		}
		if len(parts) >= 4 {
			cand.album = strings.TrimSpace(parts[3])
		}
		if len(parts) >= 5 {
			cand.artURL = strings.TrimSpace(parts[4])
		}
		if len(parts) >= 6 && parts[5] != "" {
			if posMicro, err := strconv.ParseInt(strings.TrimSpace(parts[5]), 10, 64); err == nil {
				cand.position = posMicro / 1000000
			}
		}
		if len(parts) >= 7 && parts[6] != "" {
			if lenMicro, err := strconv.ParseInt(strings.TrimSpace(parts[6]), 10, 64); err == nil {
				cand.length = lenMicro / 1000000
			}
		}
		if len(parts) >= 8 {
			cand.player = strings.TrimSpace(parts[7])
		}
		if len(parts) >= 9 {
			cand.xesamURL = strings.TrimSpace(parts[8])
		}
		if len(parts) >= 10 {
			cand.trackID = strings.TrimSpace(parts[9])
		}

		// Calculate priority score:
		// Playing is highest priority. Active metadata receives bonus points.
		score := 0
		switch strings.ToLower(cand.status) {
		case "playing":
			score += 1000
		case "paused":
			score += 200
		case "stopped":
			score += 10
		}

		if cand.title != "" && !strings.EqualFold(cand.title, "medya oynatıyor") {
			score += 50
		}
		if cand.artist != "" && cand.artist != "--" {
			score += 25
		}
		if cand.artURL != "" || cand.xesamURL != "" {
			score += 30
		}

		cand.score = score
		candidates = append(candidates, cand)
	}

	if len(candidates) == 0 {
		return media
	}

	// Pick candidate with highest score
	best := candidates[0]
	for _, c := range candidates[1:] {
		if c.score > best.score {
			best = c
		}
	}

	// If the best candidate has no meaningful title and status is stopped/paused, return default
	if best.title == "" && best.status == "" {
		return media
	}

	if best.status != "" {
		media.Status = best.status
	}
	if best.title != "" {
		media.Title = best.title
	}
	if best.artist != "" {
		media.Artist = best.artist
	}
	if best.album != "" {
		media.Album = best.album
	}
	media.Position = best.position
	media.Length = best.length

	// Resolve the album artwork URL cleanly across Spotify, YouTube, Local Files, etc.
	media.ArtURL = c.ResolveArtURL(best.artURL, best.xesamURL, best.trackID, best.title, best.artist, best.player)

	return media
}

// ExtractYouTubeVideoID extracts 11-char YouTube video ID from various URL patterns.
func ExtractYouTubeVideoID(rawURL string) string {
	if rawURL == "" {
		return ""
	}
	matches := ytRegex.FindStringSubmatch(rawURL)
	if len(matches) >= 2 && len(matches[1]) == 11 {
		return matches[1]
	}
	return ""
}

// SearchYouTubeVideoID looks up the YouTube video ID via title & artist when browser SPA navigation omits the URL parameter.
func (c *Controller) SearchYouTubeVideoID(title, artist string) string {
	cleanTitle := strings.TrimSpace(title)
	cleanArtist := strings.TrimSpace(artist)
	if cleanTitle == "" || cleanTitle == "Çalan Medya Yok" || strings.EqualFold(cleanTitle, "medya oynatıyor") || strings.EqualFold(cleanTitle, "youtube") {
		return ""
	}

	key := strings.ToLower(cleanTitle + "||" + cleanArtist)

	// Check cache
	c.ytCacheMu.RLock()
	if val, ok := c.ytCache[key]; ok {
		c.ytCacheMu.RUnlock()
		return val
	}
	c.ytCacheMu.RUnlock()

	// Prevent duplicate concurrent searches
	c.fetchMu.Lock()
	if c.fetching[key] {
		c.fetchMu.Unlock()
		return ""
	}
	c.fetching[key] = true
	c.fetchMu.Unlock()

	// Channel to receive fast synchronous response (within 350ms)
	resChan := make(chan string, 1)

	go func() {
		defer func() {
			c.fetchMu.Lock()
			delete(c.fetching, key)
			c.fetchMu.Unlock()
		}()

		searchQuery := cleanTitle
		if cleanArtist != "" && cleanArtist != "--" {
			searchQuery += " " + cleanArtist
		}

		reqURL := "https://www.youtube.com/results?search_query=" + url.QueryEscape(searchQuery)
		req, err := http.NewRequest("GET", reqURL, nil)
		if err != nil {
			resChan <- ""
			return
		}
		req.Header.Set("User-Agent", "Mozilla/5.0 (X11; Linux x86_64; rv:128.0) Gecko/20100101 Firefox/128.0")
		req.Header.Set("Accept-Language", "en-US,en;q=0.5")

		resp, err := c.client.Do(req)
		if err != nil {
			resChan <- ""
			return
		}
		defer resp.Body.Close()

		if resp.StatusCode != http.StatusOK {
			resChan <- ""
			return
		}

		lr := io.LimitReader(resp.Body, 128*1024)
		buf, err := io.ReadAll(lr)
		if err != nil {
			resChan <- ""
			return
		}

		matches := ytSearchRegex.FindStringSubmatch(string(buf))
		if len(matches) >= 2 && len(matches[1]) == 11 {
			videoID := matches[1]
			c.ytCacheMu.Lock()
			c.ytCache[key] = videoID
			c.ytCacheMu.Unlock()
			resChan <- videoID
			return
		}

		resChan <- ""
	}()

	// Wait up to 350ms for instant resolution, otherwise let background goroutine populate the cache
	select {
	case id := <-resChan:
		return id
	case <-time.After(350 * time.Millisecond):
		return ""
	}
}

// ResolveArtURL standardizes art URLs from various players and online sources.
func (c *Controller) ResolveArtURL(artURL, xesamURL, trackID, title, artist, player string) string {
	rawArt := strings.TrimSpace(artURL)

	// 1. Spotify Linux MPRIS Cover Resolution
	// Spotify on Linux returns `https://open.spotify.com/image/...` or `spotify:image:...`
	// which fails to load or 404s. The direct CDN format is `https://i.scdn.co/image/...`.
	if strings.HasPrefix(rawArt, "https://open.spotify.com/image/") {
		imgID := strings.TrimPrefix(rawArt, "https://open.spotify.com/image/")
		return "https://i.scdn.co/image/" + imgID
	}
	if strings.HasPrefix(rawArt, "http://open.spotify.com/image/") {
		imgID := strings.TrimPrefix(rawArt, "http://open.spotify.com/image/")
		return "https://i.scdn.co/image/" + imgID
	}
	if strings.HasPrefix(rawArt, "spotify:image:") {
		imgID := strings.TrimPrefix(rawArt, "spotify:image:")
		return "https://i.scdn.co/image/" + imgID
	}

	// 2. Local File URL (file://...)
	if strings.HasPrefix(rawArt, "file://") {
		cleanPath := strings.TrimPrefix(rawArt, "file://")
		if unescaped, err := url.PathUnescape(cleanPath); err == nil {
			cleanPath = unescaped
		}
		return fmt.Sprintf("/api/media/cover?path=%s", url.QueryEscape(cleanPath))
	}

	// 3. Direct HTTP / HTTPS / Data URI
	if strings.HasPrefix(rawArt, "http://") || strings.HasPrefix(rawArt, "https://") || strings.HasPrefix(rawArt, "data:") {
		return rawArt
	}

	// 4. If artURL is empty or not provided, inspect xesam:url and trackid for YouTube or Local Media
	rawXesam := strings.TrimSpace(xesamURL)
	if ytID := ExtractYouTubeVideoID(rawXesam); ytID != "" {
		return fmt.Sprintf("https://i.ytimg.com/vi/%s/hqdefault.jpg", ytID)
	}
	if ytID := ExtractYouTubeVideoID(trackID); ytID != "" {
		return fmt.Sprintf("https://i.ytimg.com/vi/%s/hqdefault.jpg", ytID)
	}

	// Check if xesam:url is a local file
	if strings.HasPrefix(rawXesam, "file://") {
		cleanPath := strings.TrimPrefix(rawXesam, "file://")
		if unescaped, err := url.PathUnescape(cleanPath); err == nil {
			cleanPath = unescaped
		}
		return fmt.Sprintf("/api/media/cover?path=%s", url.QueryEscape(cleanPath))
	}

	// Check if trackID has spotify image
	if strings.HasPrefix(trackID, "spotify:image:") {
		return "https://i.scdn.co/image/" + strings.TrimPrefix(trackID, "spotify:image:")
	}

	// 5. Browser / YouTube SPA Navigation Fallback (Firefox, Zen, Chrome, etc. omit video URL on SPA change)
	pLower := strings.ToLower(player)
	isBrowser := strings.Contains(pLower, "firefox") || strings.Contains(pLower, "zen") ||
		strings.Contains(pLower, "chrome") || strings.Contains(pLower, "chromium") ||
		strings.Contains(pLower, "brave") || strings.Contains(pLower, "edge") ||
		strings.Contains(pLower, "opera") || strings.Contains(pLower, "vivaldi") ||
		strings.Contains(strings.ToLower(xesamURL), "youtube") || strings.Contains(strings.ToLower(title), "youtube")

	if isBrowser && title != "" && title != "Çalan Medya Yok" {
		if ytID := c.SearchYouTubeVideoID(title, artist); ytID != "" {
			return fmt.Sprintf("https://i.ytimg.com/vi/%s/hqdefault.jpg", ytID)
		}
	}

	return ""
}

func (c *Controller) ControlMedia(action string) error {
	var cmd *exec.Cmd
	switch action {
	case "play-pause", "play_pause", "toggle":
		cmd = exec.Command("playerctl", "play-pause")
	case "next":
		cmd = exec.Command("playerctl", "next")
	case "previous", "prev":
		cmd = exec.Command("playerctl", "previous")
	case "stop":
		cmd = exec.Command("playerctl", "stop")
	case "play":
		cmd = exec.Command("playerctl", "play")
	case "pause":
		cmd = exec.Command("playerctl", "pause")
	case "seek-forward", "forward", "ff":
		cmd = exec.Command("playerctl", "position", "10+")
	case "seek-backward", "rewind", "rw":
		cmd = exec.Command("playerctl", "position", "10-")
	case "shuffle":
		cmd = exec.Command("playerctl", "shuffle", "toggle")
	case "loop", "repeat":
		cmd = exec.Command("playerctl", "loop", "track")
	case "vol-up":
		cmd = exec.Command("pactl", "set-sink-volume", "@DEFAULT_SINK@", "+5%")
	case "vol-down":
		cmd = exec.Command("pactl", "set-sink-volume", "@DEFAULT_SINK@", "-5%")
	default:
		return fmt.Errorf("unsupported media action: %s", action)
	}

	return cmd.Run()
}

func (c *Controller) GetAudio() models.AudioData {
	audio := models.AudioData{
		VolumePercent: 50,
		IsMuted:       false,
	}

	// 1. Get Volume
	outVol, err := exec.Command("pactl", "get-sink-volume", "@DEFAULT_SINK@").Output()
	if err == nil {
		str := string(outVol)
		if idx := strings.Index(str, "%"); idx != -1 {
			start := idx - 1
			for start >= 0 && str[start] >= '0' && str[start] <= '9' {
				start--
			}
			if num, err := strconv.Atoi(str[start+1 : idx]); err == nil {
				audio.VolumePercent = num
			}
		}
	}

	// 2. Get Mute
	outMute, err := exec.Command("pactl", "get-sink-mute", "@DEFAULT_SINK@").Output()
	if err == nil {
		str := strings.ToLower(string(outMute))
		if strings.Contains(str, "yes") {
			audio.IsMuted = true
		}
	}

	return audio
}

func (c *Controller) ExecuteSystemControl(req models.SystemControlRequest) error {
	switch req.Action {
	case "vol-up":
		return exec.Command("pactl", "set-sink-volume", "@DEFAULT_SINK@", "+5%").Run()
	case "vol-down":
		return exec.Command("pactl", "set-sink-volume", "@DEFAULT_SINK@", "-5%").Run()
	case "vol-mute":
		return exec.Command("pactl", "set-sink-mute", "@DEFAULT_SINK@", "toggle").Run()
	case "set-volume":
		target := req.Value
		if target < 0 {
			target = 0
		}
		if target > 100 {
			target = 100
		}
		return exec.Command("pactl", "set-sink-volume", "@DEFAULT_SINK@", fmt.Sprintf("%d%%", target)).Run()
	case "lock":
		return exec.Command("loginctl", "lock-session").Run()
	case "sleep", "suspend":
		return exec.Command("systemctl", "suspend").Run()
	case "launch-app", "launch", "open-app":
		return apps.LaunchTarget(req.Target)
	default:
		return fmt.Errorf("unsupported system action: %s", req.Action)
	}
}

func (c *Controller) LaunchApplication(target string) error {
	return apps.LaunchTarget(target)
}

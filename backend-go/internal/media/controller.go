package media

import (
	"bytes"
	"fmt"
	"net/url"
	"os/exec"
	"strconv"
	"strings"

	"github.com/mobiledashboard/backend-go/internal/apps"
	"github.com/mobiledashboard/backend-go/internal/models"
)

type Controller struct{}

func NewController() *Controller {
	return &Controller{}
}

func (c *Controller) GetMedia() models.MediaData {
	media := models.MediaData{
		Title:  "Çalan Medya Yok",
		Artist: "--",
		Album:  "--",
		Status: "Stopped",
	}

	// Read playerctl metadata with custom delimiter
	// Format: {{status}}|{{title}}|{{artist}}|{{album}}|{{mpris:artUrl}}|{{position}}|{{mpris:length}}
	formatStr := "{{status}}||{{title}}||{{artist}}||{{album}}||{{mpris:artUrl}}||{{position}}||{{mpris:length}}"
	cmd := exec.Command("playerctl", "metadata", "--format", formatStr)
	var out bytes.Buffer
	cmd.Stdout = &out
	if err := cmd.Run(); err != nil {
		return media
	}

	raw := strings.TrimSpace(out.String())
	if raw == "" {
		return media
	}

	parts := strings.Split(raw, "||")
	if len(parts) >= 1 && parts[0] != "" {
		media.Status = parts[0]
	}
	if len(parts) >= 2 && parts[1] != "" {
		media.Title = parts[1]
	}
	if len(parts) >= 3 && parts[2] != "" {
		media.Artist = parts[2]
	}
	if len(parts) >= 4 && parts[3] != "" {
		media.Album = parts[3]
	}
	if len(parts) >= 5 && parts[4] != "" {
		artUrl := parts[4]
		if strings.HasPrefix(artUrl, "file://") {
			// Convert local file:// url to backend api endpoint /api/media/cover?path=...
			cleanPath := strings.TrimPrefix(artUrl, "file://")
			if decoded, err := url.QueryUnescape(cleanPath); err == nil {
				cleanPath = decoded
			}
			media.ArtURL = fmt.Sprintf("/api/media/cover?path=%s", url.QueryEscape(cleanPath))
		} else {
			media.ArtURL = artUrl
		}
	}
	if len(parts) >= 6 && parts[5] != "" {
		if posMicro, err := strconv.ParseInt(parts[5], 10, 64); err == nil {
			media.Position = posMicro / 1000000
		}
	}
	if len(parts) >= 7 && parts[6] != "" {
		if lenMicro, err := strconv.ParseInt(parts[6], 10, 64); err == nil {
			media.Length = lenMicro / 1000000
		}
	}

	return media
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

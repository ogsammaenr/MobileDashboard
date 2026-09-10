package media

import (
	"testing"
)

func TestExtractYouTubeVideoID(t *testing.T) {
	tests := []struct {
		url      string
		expected string
	}{
		{"https://www.youtube.com/watch?v=dQw4w9WgXcQ", "dQw4w9WgXcQ"},
		{"https://music.youtube.com/watch?v=dQw4w9WgXcQ&list=RDAMVM", "dQw4w9WgXcQ"},
		{"https://youtu.be/dQw4w9WgXcQ", "dQw4w9WgXcQ"},
		{"https://www.youtube.com/embed/dQw4w9WgXcQ?autoplay=1", "dQw4w9WgXcQ"},
		{"https://www.youtube.com/shorts/dQw4w9WgXcQ", "dQw4w9WgXcQ"},
		{"https://example.com/not-a-yt-url", ""},
		{"", ""},
	}

	for _, tt := range tests {
		got := ExtractYouTubeVideoID(tt.url)
		if got != tt.expected {
			t.Errorf("ExtractYouTubeVideoID(%q) = %q; want %q", tt.url, got, tt.expected)
		}
	}
}

func TestResolveArtURL(t *testing.T) {
	ctrl := NewController()

	tests := []struct {
		name     string
		artURL   string
		xesamURL string
		trackID  string
		title    string
		artist   string
		player   string
		want     string
	}{
		{
			name:   "Spotify open.spotify.com URL",
			artURL: "https://open.spotify.com/image/ab67616d0000b2738734293",
			want:   "https://i.scdn.co/image/ab67616d0000b2738734293",
		},
		{
			name:   "Spotify spotify:image URI",
			artURL: "spotify:image:ab67616d0000b2738734293",
			want:   "https://i.scdn.co/image/ab67616d0000b2738734293",
		},
		{
			name:   "Spotify direct CDN URL",
			artURL: "https://i.scdn.co/image/ab67616d0000b2738734293",
			want:   "https://i.scdn.co/image/ab67616d0000b2738734293",
		},
		{
			name:     "YouTube video in xesamURL when artURL is empty",
			artURL:   "",
			xesamURL: "https://www.youtube.com/watch?v=K1Jc_W3O70M",
			want:     "https://i.ytimg.com/vi/K1Jc_W3O70M/hqdefault.jpg",
		},
		{
			name:     "YouTube music in xesamURL",
			artURL:   "",
			xesamURL: "https://music.youtube.com/watch?v=K1Jc_W3O70M",
			want:     "https://i.ytimg.com/vi/K1Jc_W3O70M/hqdefault.jpg",
		},
		{
			name:   "Local file artURL",
			artURL: "file:///home/user/Music/Album/cover.jpg",
			want:   "/api/media/cover?path=%2Fhome%2Fuser%2FMusic%2FAlbum%2Fcover.jpg",
		},
		{
			name:     "Local file in xesamURL when artURL is empty",
			artURL:   "",
			xesamURL: "file:///home/user/Music/Song.mp3",
			want:     "/api/media/cover?path=%2Fhome%2Fuser%2FMusic%2FSong.mp3",
		},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			got := ctrl.ResolveArtURL(tt.artURL, tt.xesamURL, tt.trackID, tt.title, tt.artist, tt.player)
			if got != tt.want {
				t.Errorf("ResolveArtURL() = %q; want %q", got, tt.want)
			}
		})
	}
}

func TestSearchYouTubeVideoID(t *testing.T) {
	ctrl := NewController()
	// Test live search for Daft Punk - Get Lucky
	videoID := ctrl.SearchYouTubeVideoID("Daft Punk - Get Lucky", "Daft Punk")
	if videoID == "" {
		t.Log("Note: Live search returned empty or timed out (network dependent)")
	} else {
		t.Logf("Found video ID: %s", videoID)
	}
}

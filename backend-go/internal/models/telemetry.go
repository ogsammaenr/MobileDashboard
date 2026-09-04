package models

type CPUData struct {
	Percent float64 `json:"percent"`
	Temp    float64 `json:"temp"`
}

type GPUData struct {
	Percent     float64 `json:"percent"`
	Temp        float64 `json:"temp"`
	Name        string  `json:"name"`
	MemoryUsed  float64 `json:"memory_used_mb"`
	MemoryTotal float64 `json:"memory_total_mb"`
}

type RAMData struct {
	Percent float64 `json:"percent"`
	UsedGB  float64 `json:"used_gb"`
	TotalGB float64 `json:"total_gb"`
	FreeGB  float64 `json:"free_gb"`
}

type DiskData struct {
	Percent float64 `json:"percent"`
	UsedGB  float64 `json:"used_gb"`
	TotalGB float64 `json:"total_gb"`
	FreeGB  float64 `json:"free_gb"`
}

type NetworkData struct {
	DownKBps float64 `json:"down_kbps"`
	UpKBps   float64 `json:"up_kbps"`
}

type MediaData struct {
	Title    string `json:"title"`
	Artist   string `json:"artist"`
	Album    string `json:"album"`
	Status   string `json:"status"` // "Playing", "Paused", "Stopped"
	ArtURL   string `json:"art_url"`
	Position int64  `json:"position_sec"`
	Length   int64  `json:"length_sec"`
}

type AudioData struct {
	VolumePercent int  `json:"volume_percent"`
	IsMuted       bool `json:"is_muted"`
}

type TelemetryPayload struct {
	Timestamp int64       `json:"timestamp"`
	CPU       CPUData     `json:"cpu"`
	GPU       GPUData     `json:"gpu"`
	RAM       RAMData     `json:"ram"`
	Disk      DiskData    `json:"disk"`
	Network   NetworkData `json:"network"`
	Media     MediaData   `json:"media"`
	Audio     AudioData   `json:"audio"`
}

type MediaControlRequest struct {
	Action string `json:"action"` // "play-pause", "next", "previous", "stop"
}

type SystemControlRequest struct {
	Action string `json:"action"` // "vol-up", "vol-down", "vol-mute", "set-volume", "lock", "sleep", "launch-app"
	Value  int    `json:"value,omitempty"`
	Target string `json:"target,omitempty"` // For app launch: "spotify", "steam", "code", or custom command/URL
}

type StatusResponse struct {
	Status   string `json:"status"`
	Version  string `json:"version"`
	Hostname string `json:"hostname"`
	Port     int    `json:"port"`
}

package models

type WidgetConfig struct {
	CustomTitle    string `json:"custom_title,omitempty"`
	FontScale      string `json:"font_scale,omitempty"`      // "small", "medium", "large", "xlarge"
	AccentColor    string `json:"accent_color,omitempty"`    // "cyan", "green", "purple", "red", "amber", "white"
	ShapeStyle     string `json:"shape_style,omitempty"`     // "rounded", "segmented_pills", "pill", "scalloped", "clover", "asymmetric"
	AppID          string `json:"app_id,omitempty"`          // e.g. "spotify", "steam", "discord", "code", "browser", "terminal", "files", "calculator", "screenshot", "custom"
	AppPath        string `json:"app_path,omitempty"`        // Path to .desktop or binary / .exe
	AppCommand     string `json:"app_command,omitempty"`     // Custom shell command / URL / binary
	AppIcon        string `json:"app_icon,omitempty"`        // Emoji or icon identifier
	AppIconURL     string `json:"app_icon_url,omitempty"`    // URL path to icon, e.g. "/api/apps/icon?name=spotify-client"
	ShowSeconds    *bool  `json:"show_seconds,omitempty"`    // pointer to allow true/false distinction
	ShowDate       *bool  `json:"show_date,omitempty"`
	ShowTemp       *bool  `json:"show_temp,omitempty"`
	ShowBar        *bool  `json:"show_bar,omitempty"`
	ShowBadge      *bool  `json:"show_badge,omitempty"`
	Is12Hour       *bool  `json:"is_12hour,omitempty"`
	BlurBackground *bool  `json:"blur_background,omitempty"`
}

type WidgetInstance struct {
	WidgetID string       `json:"widget_id"`
	X        int          `json:"x"`        // 0 to 3 column index
	Y        int          `json:"y"`        // 0, 1, 2... row index
	W        int          `json:"w"`        // 1 to 4 column width
	H        int          `json:"h"`        // 1 to 3 row height
	Span     int          `json:"span"`     // Backward compatibility fallback
	RowSpan  int          `json:"row_span"` // Backward compatibility fallback
	Config   WidgetConfig `json:"config"`
}

type PageLayout struct {
	ID      string           `json:"id"`
	Title   string           `json:"title"`
	Icon    string           `json:"icon"`
	Theme   string           `json:"theme"`
	Widgets []WidgetInstance `json:"widgets"`
}

type WidgetCatalogItem struct {
	ID          string `json:"id"`
	Name        string `json:"name"`
	Category    string `json:"category"`
	DefaultSpan int    `json:"default_span"`
	DefaultRow  int    `json:"default_row_span"`
	Icon        string `json:"icon"`
	Description string `json:"description"`
}

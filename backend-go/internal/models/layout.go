package models

import (
	"encoding/json"
)

// WidgetConfig defines universal visual/styling metadata alongside a dynamic parameters map.
// This prevents "God-Object" bloat and allows arbitrary widget-specific options.
type WidgetConfig struct {
	CustomTitle string         `json:"custom_title,omitempty"`
	FontScale   string         `json:"font_scale,omitempty"`   // "small", "medium", "large", "xlarge"
	AccentColor string         `json:"accent_color,omitempty"` // "nord", "catppuccin", "everforest", etc.
	ShapeStyle  string         `json:"shape_style,omitempty"`  // "rounded", "segmented_pills", "pill", "scalloped", "clover", "asymmetric"
	Params      map[string]any `json:"params,omitempty"`       // Dynamic widget-specific options (e.g. show_seconds, show_temp, app_id)
}

// UnmarshalJSON provides seamless backward-compatibility by capturing any legacy top-level keys into Params.
func (w *WidgetConfig) UnmarshalJSON(data []byte) error {
	type Alias WidgetConfig
	aux := &struct {
		*Alias
	}{
		Alias: (*Alias)(w),
	}
	if err := json.Unmarshal(data, aux); err != nil {
		return err
	}

	var raw map[string]any
	if err := json.Unmarshal(data, &raw); err != nil {
		return err
	}

	if w.Params == nil {
		w.Params = make(map[string]any)
	}

	knownMetadata := map[string]bool{
		"custom_title": true,
		"font_scale":   true,
		"accent_color": true,
		"shape_style":  true,
		"params":       true,
	}

	for k, v := range raw {
		if !knownMetadata[k] {
			if _, exists := w.Params[k]; !exists {
				w.Params[k] = v
			}
		}
	}

	return nil
}

// WidgetInstance represents a placed widget in the 2D coordinate grid (0-3 columns, N rows).
type WidgetInstance struct {
	WidgetID string       `json:"widget_id"`
	X        int          `json:"x"`                  // 0 to 3 column index
	Y        int          `json:"y"`                  // 0, 1, 2... row index
	W        int          `json:"w"`                  // 1 to 4 column width
	H        int          `json:"h"`                  // 1 to 3 row height
	Span     int          `json:"span,omitempty"`     // Fallback for v1.0 compatibility
	RowSpan  int          `json:"row_span,omitempty"` // Fallback for v1.0 compatibility
	Config   WidgetConfig `json:"config"`
}

// PageLayout represents a swipeable dashboard screen containing widgets.
type PageLayout struct {
	ID      string           `json:"id"`
	Title   string           `json:"title"`
	Icon    string           `json:"icon"`
	Theme   string           `json:"theme"`
	Widgets []WidgetInstance `json:"widgets"`
}

// WidgetCatalogItem provides catalog metadata for the Web Admin widget drawer.
type WidgetCatalogItem struct {
	ID          string `json:"id"`
	Name        string `json:"name"`
	Category    string `json:"category"`
	DefaultSpan int    `json:"default_span"`
	DefaultRow  int    `json:"default_row_span"`
	Icon        string `json:"icon"`
	Description string `json:"description"`
}

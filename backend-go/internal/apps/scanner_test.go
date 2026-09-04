package apps

import (
	"testing"
)

func TestResolveSpecificAppIcons(t *testing.T) {
	testApps := []string{
		"org.prismlauncher.PrismLauncher",
		"prismlauncher",
		"org.gnome.Calculator",
		"gnome-calculator",
		"accessories-calculator",
		"org.kde.kcalc",
		"spotify",
		"vesktop",
		"zen-browser",
		"vlc",
	}

	for _, name := range testApps {
		path, mime := ResolveIconPath(name, "")
		t.Logf("App %-32s => Found: %t | Path: %s (%s)", name, path != "", path, mime)
		if path == "" {
			t.Errorf("Failed to resolve icon for: %s", name)
		}
	}
}

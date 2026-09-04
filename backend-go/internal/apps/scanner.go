package apps

import (
	"bufio"
	"fmt"
	"net/url"
	"os"
	"os/exec"
	"os/user"
	"path/filepath"
	"sort"
	"strings"
)

type DesktopApp struct {
	ID          string `json:"id"`
	Name        string `json:"name"`
	Exec        string `json:"exec"`
	Icon        string `json:"icon"`
	Path        string `json:"path"`
	IconURL     string `json:"icon_url"`
	Comment     string `json:"comment,omitempty"`
	GenericName string `json:"generic_name,omitempty"`
}

// Standard Linux .desktop and icon search directories
func getDesktopSearchDirs() []string {
	var dirs []string
	dirs = append(dirs, "/usr/share/applications", "/usr/local/share/applications")

	if usr, err := user.Current(); err == nil && usr.HomeDir != "" {
		dirs = append(dirs,
			filepath.Join(usr.HomeDir, ".local/share/applications"),
			filepath.Join(usr.HomeDir, ".local/share/flatpak/exports/share/applications"),
		)
	}

	dirs = append(dirs,
		"/var/lib/flatpak/exports/share/applications",
		"/var/lib/snapd/desktop/applications",
	)

	return dirs
}

func trimImageExtension(name string) string {
	ext := strings.ToLower(filepath.Ext(name))
	switch ext {
	case ".png", ".svg", ".xpm", ".ico", ".jpg", ".jpeg", ".webp":
		return name[:len(name)-len(ext)]
	default:
		return name
	}
}

func getIconSearchDirs() []string {
	var dirs []string
	dirs = append(dirs, "/usr/share/pixmaps", "/usr/share/icons/hicolor")

	if usr, err := user.Current(); err == nil && usr.HomeDir != "" {
		dirs = append(dirs,
			filepath.Join(usr.HomeDir, ".local/share/icons"),
			filepath.Join(usr.HomeDir, ".icons"),
			filepath.Join(usr.HomeDir, ".local/share/pixmaps"),
			filepath.Join(usr.HomeDir, ".local/share/flatpak/exports/share/icons"),
			filepath.Join(usr.HomeDir, ".local/share/flatpak/appstream"),
		)
	}

	dirs = append(dirs,
		"/usr/share/icons",
		"/var/lib/flatpak/exports/share/icons",
		"/var/lib/flatpak/appstream",
		"/var/lib/snapd/desktop/icons",
	)

	return dirs
}

// Clean desktop Exec field (remove %u, %F, %U, %f etc.)
func cleanExecField(execStr string) string {
	tokens := strings.Fields(execStr)
	var cleanTokens []string
	for _, tok := range tokens {
		if strings.HasPrefix(tok, "%") && len(tok) == 2 {
			continue
		}
		cleanTokens = append(cleanTokens, tok)
	}
	return strings.Join(cleanTokens, " ")
}

// Parse a single .desktop file
func ParseDesktopFile(filePath string) (*DesktopApp, error) {
	file, err := os.Open(filePath)
	if err != nil {
		return nil, err
	}
	defer file.Close()

	scanner := bufio.NewScanner(file)
	inDesktopEntry := false

	app := &DesktopApp{
		Path: filePath,
		ID:   strings.TrimSuffix(filepath.Base(filePath), ".desktop"),
	}

	noDisplay := false
	isApplication := true

	for scanner.Scan() {
		line := strings.TrimSpace(scanner.Text())
		if line == "" || strings.HasPrefix(line, "#") {
			continue
		}

		if strings.HasPrefix(line, "[") && strings.HasSuffix(line, "]") {
			inDesktopEntry = (line == "[Desktop Entry]")
			continue
		}

		if !inDesktopEntry {
			continue
		}

		parts := strings.SplitN(line, "=", 2)
		if len(parts) != 2 {
			continue
		}

		key := strings.TrimSpace(parts[0])
		val := strings.TrimSpace(parts[1])

		switch key {
		case "Name":
			if app.Name == "" {
				app.Name = val
			}
		case "GenericName":
			if app.GenericName == "" {
				app.GenericName = val
			}
		case "Comment":
			if app.Comment == "" {
				app.Comment = val
			}
		case "Exec":
			if app.Exec == "" {
				app.Exec = cleanExecField(val)
			}
		case "Icon":
			if app.Icon == "" {
				app.Icon = val
			}
		case "Type":
			if val != "Application" {
				isApplication = false
			}
		case "NoDisplay", "Hidden":
			if strings.ToLower(val) == "true" {
				noDisplay = true
			}
		}
	}

	if err := scanner.Err(); err != nil {
		return nil, err
	}

	if noDisplay || !isApplication || app.Name == "" || app.Exec == "" {
		return nil, fmt.Errorf("not a launchable application")
	}

	if app.Icon != "" {
		app.IconURL = fmt.Sprintf("/api/apps/icon?name=%s&path=%s", url.QueryEscape(app.Icon), url.QueryEscape(filePath))
	} else {
		app.IconURL = "/api/apps/icon?name=application-x-executable"
	}

	return app, nil
}

// ScanInstalledApps returns all launchable desktop apps installed on the system
func ScanInstalledApps() []DesktopApp {
	searchDirs := getDesktopSearchDirs()
	seenIDs := make(map[string]bool)
	var apps []DesktopApp

	for _, dir := range searchDirs {
		entries, err := os.ReadDir(dir)
		if err != nil {
			continue
		}

		for _, entry := range entries {
			if entry.IsDir() || !strings.HasSuffix(entry.Name(), ".desktop") {
				continue
			}

			fullPath := filepath.Join(dir, entry.Name())
			app, err := ParseDesktopFile(fullPath)
			if err != nil || app == nil {
				continue
			}

			if seenIDs[app.ID] || seenIDs[app.Name] {
				continue
			}
			seenIDs[app.ID] = true
			seenIDs[app.Name] = true

			apps = append(apps, *app)
		}
	}

	sort.Slice(apps, func(i, j int) bool {
		return strings.ToLower(apps[i].Name) < strings.ToLower(apps[j].Name)
	})

	return apps
}

// ResolveIconPath finds the real absolute file path of an icon on Linux
func ResolveIconPath(iconNameOrPath string, desktopPath string) (string, string) {
	iconNameOrPath = strings.TrimSpace(iconNameOrPath)
	if iconNameOrPath == "" {
		return "", ""
	}

	// 1. Direct file path
	if filepath.IsAbs(iconNameOrPath) {
		if _, err := os.Stat(iconNameOrPath); err == nil {
			return iconNameOrPath, getMimeType(iconNameOrPath)
		}
		for _, ext := range []string{".png", ".svg", ".xpm"} {
			withExt := iconNameOrPath + ext
			if _, err := os.Stat(withExt); err == nil {
				return withExt, getMimeType(withExt)
			}
		}
	}

	// 2. Build candidate icon names
	rawClean := trimImageExtension(iconNameOrPath)
	var candidates []string
	addCandidate := func(c string) {
		c = strings.TrimSpace(c)
		if c == "" {
			return
		}
		for _, existing := range candidates {
			if existing == c {
				return
			}
		}
		candidates = append(candidates, c)
	}

	addCandidate(rawClean)
	addCandidate(strings.ToLower(rawClean))

	// If reverse DNS like "org.prismlauncher.PrismLauncher", also try "PrismLauncher" and "prismlauncher"
	if strings.Contains(rawClean, ".") {
		parts := strings.Split(rawClean, ".")
		lastPart := parts[len(parts)-1]
		addCandidate(lastPart)
		addCandidate(strings.ToLower(lastPart))
	}

	// Special fallbacks for calculator and common utilities
	lowerRaw := strings.ToLower(rawClean)
	if strings.Contains(lowerRaw, "calculator") || strings.Contains(lowerRaw, "calc") {
		addCandidate("org.gnome.Calculator")
		addCandidate("gnome-calculator")
		addCandidate("accessories-calculator")
		addCandidate("calc")
		addCandidate("org.kde.kcalc")
		addCandidate("kcalc")
	}
	if strings.Contains(lowerRaw, "prism") {
		addCandidate("org.prismlauncher.PrismLauncher")
		addCandidate("prismlauncher")
		addCandidate("PrismLauncher")
	}

	iconDirs := getIconSearchDirs()
	sizes := []string{"512x512", "256x256", "128x128", "scalable", "96x96", "64x64", "48x48", "32x32"}
	extensions := []string{".png", ".svg", ".xpm"}

	for _, name := range candidates {
		for _, baseDir := range iconDirs {
			// Direct check
			for _, ext := range extensions {
				directCandidate := filepath.Join(baseDir, name+ext)
				if _, err := os.Stat(directCandidate); err == nil {
					return directCandidate, getMimeType(directCandidate)
				}
			}

			// Size & Category combinations
			for _, size := range sizes {
				for _, category := range []string{"apps", "categories", "devices", "mimetypes", "places", "status"} {
					for _, ext := range extensions {
						c1 := filepath.Join(baseDir, size, category, name+ext)
						if _, err := os.Stat(c1); err == nil {
							return c1, getMimeType(c1)
						}
						c2 := filepath.Join(baseDir, category, size, name+ext)
						if _, err := os.Stat(c2); err == nil {
							return c2, getMimeType(c2)
						}
					}
				}
			}

			// Sub-theme directories (kora, Papirus, Adwaita, Tela, etc.)
			themeEntries, err := os.ReadDir(baseDir)
			if err == nil {
				for _, themeEntry := range themeEntries {
					if !themeEntry.IsDir() {
						continue
					}
					themeDir := filepath.Join(baseDir, themeEntry.Name())
					for _, size := range sizes {
						for _, ext := range extensions {
							c1 := filepath.Join(themeDir, size, "apps", name+ext)
							if _, err := os.Stat(c1); err == nil {
								return c1, getMimeType(c1)
							}
							c2 := filepath.Join(themeDir, "apps", size, name+ext)
							if _, err := os.Stat(c2); err == nil {
								return c2, getMimeType(c2)
							}
							c3 := filepath.Join(themeDir, "apps", "scalable", name+ext)
							if _, err := os.Stat(c3); err == nil {
								return c3, getMimeType(c3)
							}
						}
					}
				}
			}
		}
	}

	// 3. Fallback: Fast recursive search in Flatpak and user icons if still not found
	for _, baseDir := range iconDirs {
		for _, name := range candidates {
			for _, ext := range extensions {
				targetFile := name + ext
				var foundPath string
				_ = filepath.Walk(baseDir, func(p string, info os.FileInfo, err error) error {
					if err == nil && !info.IsDir() && strings.EqualFold(info.Name(), targetFile) {
						foundPath = p
						return filepath.SkipAll
					}
					return nil
				})
				if foundPath != "" {
					return foundPath, getMimeType(foundPath)
				}
			}
		}
	}

	return "", ""
}

func getMimeType(path string) string {
	ext := strings.ToLower(filepath.Ext(path))
	switch ext {
	case ".png":
		return "image/png"
	case ".svg":
		return "image/svg+xml"
	case ".jpg", ".jpeg":
		return "image/jpeg"
	case ".xpm":
		return "image/x-xpixmap"
	case ".ico":
		return "image/x-icon"
	default:
		return "image/png"
	}
}

// LaunchTarget launches a .desktop file, executable, script, or wine exe
func LaunchTarget(target string) error {
	target = strings.TrimSpace(target)
	if target == "" {
		return fmt.Errorf("target path or command is empty")
	}

	fmt.Printf("\033[32m\033[1m[🚀 UYGULAMA BAŞLATILIYOR]\033[0m Hedef: \033[36m%s\033[0m\n", target)

	// 1. If it's a .desktop file
	if strings.HasSuffix(strings.ToLower(target), ".desktop") {
		// Try gio launch or gtk-launch
		if path, err := exec.LookPath("gio"); err == nil {
			cmd := exec.Command(path, "launch", target)
			if err := cmd.Start(); err == nil {
				return nil
			}
		}
		if path, err := exec.LookPath("gtk-launch"); err == nil {
			base := strings.TrimSuffix(filepath.Base(target), ".desktop")
			cmd := exec.Command(path, base)
			if err := cmd.Start(); err == nil {
				return nil
			}
		}

		// Fallback: Parse Exec from the .desktop file and run it
		app, err := ParseDesktopFile(target)
		if err == nil && app.Exec != "" {
			cmd := exec.Command("sh", "-c", app.Exec)
			return cmd.Start()
		}
	}

	// 2. If it's an exe (Windows executable on Linux via Wine or Windows native)
	if strings.HasSuffix(strings.ToLower(target), ".exe") {
		if path, err := exec.LookPath("wine"); err == nil {
			cmd := exec.Command(path, target)
			return cmd.Start()
		}
	}

	// 3. If it's an executable file on disk
	if filepath.IsAbs(target) {
		if info, err := os.Stat(target); err == nil && !info.IsDir() {
			cmd := exec.Command(target)
			return cmd.Start()
		}
	}

	// 4. Default shell execution / web URL
	if strings.HasPrefix(target, "http://") || strings.HasPrefix(target, "https://") {
		cmd := exec.Command("xdg-open", target)
		return cmd.Start()
	}

	cmd := exec.Command("sh", "-c", target)
	return cmd.Start()
}

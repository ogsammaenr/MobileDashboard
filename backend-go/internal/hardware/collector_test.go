package hardware

import (
	"strings"
	"testing"

	"github.com/shirou/gopsutil/v3/disk"
)

func TestAggregatedDiskUsage(t *testing.T) {
	parts, err := disk.Partitions(false)
	if err != nil {
		t.Fatalf("Partitions err: %v", err)
	}

	var totalBytes uint64
	var usedBytes uint64
	var freeBytes uint64
	seenDevices := make(map[string]bool)

	for _, p := range parts {
		if p.Mountpoint == "" || isIgnoredFsType(p.Fstype) {
			continue
		}
		if strings.HasPrefix(p.Device, "/dev/loop") {
			continue
		}
		// Avoid duplicate partition counting (bind mounts / btrfs subvolumes on same device)
		if p.Device != "" && seenDevices[p.Device] {
			continue
		}
		if p.Device != "" {
			seenDevices[p.Device] = true
		}

		u, err := disk.Usage(p.Mountpoint)
		if err != nil || u.Total == 0 {
			continue
		}

		totalBytes += u.Total
		usedBytes += u.Used
		freeBytes += u.Free

		t.Logf("Added partition: %s (%s) [%s] -> Total: %.1f GB, Used: %.1f GB, Free: %.1f GB",
			p.Mountpoint, p.Device, p.Fstype,
			float64(u.Total)/1024/1024/1024,
			float64(u.Used)/1024/1024/1024,
			float64(u.Free)/1024/1024/1024,
		)
	}

	if totalBytes > 0 {
		totalGB := float64(totalBytes) / 1024 / 1024 / 1024
		usedGB := float64(usedBytes) / 1024 / 1024 / 1024
		freeGB := float64(freeBytes) / 1024 / 1024 / 1024
		usedPercent := (float64(usedBytes) / float64(totalBytes)) * 100

		t.Logf("AGGREGATED DISK: Total: %.1f GB | Used: %.1f GB (%.1f%%) | Free: %.1f GB",
			totalGB, usedGB, usedPercent, freeGB)
	}
}

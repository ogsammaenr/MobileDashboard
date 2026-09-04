package hardware

import (
	"bytes"
	"math"
	"os"
	"os/exec"
	"strconv"
	"strings"
	"sync"
	"time"

	"github.com/mobiledashboard/backend-go/internal/models"
	"github.com/shirou/gopsutil/v3/cpu"
	"github.com/shirou/gopsutil/v3/disk"
	"github.com/shirou/gopsutil/v3/host"
	"github.com/shirou/gopsutil/v3/mem"
	gopsnet "github.com/shirou/gopsutil/v3/net"
)

type Collector struct {
	mu           sync.Mutex
	lastNetTime  time.Time
	lastBytesRec uint64
	lastBytesSen uint64
}

func NewCollector() *Collector {
	c := &Collector{
		lastNetTime: time.Now(),
	}
	if ioCounters, err := gopsnet.IOCounters(false); err == nil && len(ioCounters) > 0 {
		c.lastBytesRec = ioCounters[0].BytesRecv
		c.lastBytesSen = ioCounters[0].BytesSent
	}
	return c
}

func (c *Collector) CollectAll() models.TelemetryPayload {
	return models.TelemetryPayload{
		Timestamp: time.Now().Unix(),
		CPU:       c.GetCPU(),
		GPU:       c.GetGPU(),
		RAM:       c.GetRAM(),
		Disk:      c.GetDisk(),
		Network:   c.GetNetwork(),
	}
}

func (c *Collector) GetCPU() models.CPUData {
	var cpuPercent float64 = 0
	if p, err := cpu.Percent(0, false); err == nil && len(p) > 0 {
		cpuPercent = round(p[0], 1)
	}

	var cpuTemp float64 = 0
	if temps, err := host.SensorsTemperatures(); err == nil {
		for _, t := range temps {
			sensorKey := strings.ToLower(t.SensorKey)
			if strings.Contains(sensorKey, "core") || strings.Contains(sensorKey, "k10temp") || strings.Contains(sensorKey, "cpu") || strings.Contains(sensorKey, "tctl") || strings.Contains(sensorKey, "package") {
				if t.Temperature > 0 {
					cpuTemp = round(t.Temperature, 0)
					break
				}
			}
		}
	}

	// Fallback Linux thermal zone
	if cpuTemp == 0 {
		if data, err := os.ReadFile("/sys/class/thermal/thermal_zone0/temp"); err == nil {
			if tVal, err := strconv.ParseFloat(strings.TrimSpace(string(data)), 64); err == nil {
				cpuTemp = round(tVal/1000.0, 0)
			}
		}
	}

	return models.CPUData{
		Percent: cpuPercent,
		Temp:    cpuTemp,
	}
}

func (c *Collector) GetGPU() models.GPUData {
	gpu := models.GPUData{
		Name: "GPU",
	}

	// 1. Try NVIDIA SMI
	if cmd := exec.Command("nvidia-smi", "--query-gpu=utilization.gpu,temperature.gpu,name,memory.used,memory.total", "--format=csv,noheader,nounits"); cmd != nil {
		var out bytes.Buffer
		cmd.Stdout = &out
		if err := cmd.Run(); err == nil {
			parts := strings.Split(strings.TrimSpace(out.String()), ",")
			if len(parts) >= 5 {
				if val, err := strconv.ParseFloat(strings.TrimSpace(parts[0]), 64); err == nil {
					gpu.Percent = round(val, 1)
				}
				if val, err := strconv.ParseFloat(strings.TrimSpace(parts[1]), 64); err == nil {
					gpu.Temp = round(val, 0)
				}
				gpu.Name = strings.TrimSpace(parts[2])
				if val, err := strconv.ParseFloat(strings.TrimSpace(parts[3]), 64); err == nil {
					gpu.MemoryUsed = round(val, 0)
				}
				if val, err := strconv.ParseFloat(strings.TrimSpace(parts[4]), 64); err == nil {
					gpu.MemoryTotal = round(val, 0)
				}
				return gpu
			}
		}
	}

	// 2. Fallback AMD/Intel Sysfs DRM
	if data, err := os.ReadFile("/sys/class/drm/card0/device/gpu_busy_percent"); err == nil {
		if val, err := strconv.ParseFloat(strings.TrimSpace(string(data)), 64); err == nil {
			gpu.Percent = round(val, 1)
		}
	}

	return gpu
}

func (c *Collector) GetRAM() models.RAMData {
	if vm, err := mem.VirtualMemory(); err == nil {
		usedGB := float64(vm.Used) / 1024 / 1024 / 1024
		totalGB := float64(vm.Total) / 1024 / 1024 / 1024
		freeGB := float64(vm.Available) / 1024 / 1024 / 1024
		return models.RAMData{
			Percent: round(vm.UsedPercent, 1),
			UsedGB:  round(usedGB, 1),
			TotalGB: round(totalGB, 1),
			FreeGB:  round(freeGB, 1),
		}
	}
	return models.RAMData{}
}

func isIgnoredFsType(fstype string) bool {
	ignored := []string{
		"tmpfs", "devtmpfs", "squashfs", "iso9660", "overlay", "efivarfs",
		"proc", "sysfs", "devpts", "cgroup", "cgroup2", "autofs", "pstore",
		"bpf", "ramfs", "hugetlbfs", "mqueue", "configfs", "securityfs",
		"fusectl", "binfmt_misc", "nsfs", "tracefs", "debugfs",
	}
	fstype = strings.ToLower(strings.TrimSpace(fstype))
	for _, ign := range ignored {
		if fstype == ign || strings.HasPrefix(fstype, "fuse.") {
			return true
		}
	}
	return false
}

func (c *Collector) GetDisk() models.DiskData {
	parts, err := disk.Partitions(false)
	if err == nil && len(parts) > 0 {
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
			// Avoid duplicate counting for bind mounts or multi-mounted btrfs subvolumes
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
		}

		if totalBytes > 0 {
			totalGB := float64(totalBytes) / 1024 / 1024 / 1024
			usedGB := float64(usedBytes) / 1024 / 1024 / 1024
			freeGB := float64(freeBytes) / 1024 / 1024 / 1024
			usedPercent := (float64(usedBytes) / float64(totalBytes)) * 100

			return models.DiskData{
				Percent: round(usedPercent, 1),
				UsedGB:  round(usedGB, 1),
				TotalGB: round(totalGB, 1),
				FreeGB:  round(freeGB, 1),
			}
		}
	}

	// Fallback to single root mount
	if d, err := disk.Usage("/"); err == nil {
		usedGB := float64(d.Used) / 1024 / 1024 / 1024
		totalGB := float64(d.Total) / 1024 / 1024 / 1024
		freeGB := float64(d.Free) / 1024 / 1024 / 1024
		return models.DiskData{
			Percent: round(d.UsedPercent, 1),
			UsedGB:  round(usedGB, 1),
			TotalGB: round(totalGB, 1),
			FreeGB:  round(freeGB, 1),
		}
	}
	return models.DiskData{}
}

func (c *Collector) GetNetwork() models.NetworkData {
	c.mu.Lock()
	defer c.mu.Unlock()

	now := time.Now()
	deltaSec := now.Sub(c.lastNetTime).Seconds()
	if deltaSec <= 0 {
		deltaSec = 1.0
	}

	var downKBps float64 = 0
	var upKBps float64 = 0

	if ioCounters, err := gopsnet.IOCounters(false); err == nil && len(ioCounters) > 0 {
		currentRec := ioCounters[0].BytesRecv
		currentSen := ioCounters[0].BytesSent

		if c.lastBytesRec > 0 && currentRec >= c.lastBytesRec {
			downKBps = float64(currentRec-c.lastBytesRec) / 1024.0 / deltaSec
		}
		if c.lastBytesSen > 0 && currentSen >= c.lastBytesSen {
			upKBps = float64(currentSen-c.lastBytesSen) / 1024.0 / deltaSec
		}

		c.lastBytesRec = currentRec
		c.lastBytesSen = currentSen
		c.lastNetTime = now
	}

	return models.NetworkData{
		DownKBps: round(downKBps, 1),
		UpKBps:   round(upKBps, 1),
	}
}

func round(val float64, precision int) float64 {
	p := math.Pow(10, float64(precision))
	return math.Round(val*p) / p
}

# ChestScanner

A Minecraft Spigot plugin that scans nearby chunks for chests containing items. Perfect for finding storage locations, locating specific materials, and tracking chest ownership and access.

## Features

- **Chunk Scanning**: Scan multiple chunks around your location for chests with items
- **Material Filtering**: Search for chests containing specific materials (e.g., diamonds, iron, etc.)
- **Ownership Tracking**: Automatically tracks who placed each chest
- **Access Logging**: Logs all interactions with chests (items added/removed)
- **Teleportation**: Quick teleport to discovered chest locations
- **Export Results**: Export scan results to CSV or YAML format
- **Async Processing**: Configurable chunks-per-tick to prevent server lag
- **Optional Chunk Loading**: Can load unloaded chunks during scan (requires permission)
- **Interactive Results**: Clickable teleport links in scan results

## Installation

1. Download the latest release JAR file
2. Place the JAR file in your server's `plugins` folder
3. Restart your server
4. Configure the plugin in `plugins/ChestScanner/config.yml` (optional)

## Requirements

- Spigot 1.8.8 or compatible server (Spigot, Paper, etc.)
- Java 8 or higher

## Commands

All commands use the `/scan` prefix:

### Basic Scanning
- `/scan start [count] [load]` - Start scanning nearby chunks
  - `count`: Number of chunks to scan (1-5000, default: 100)
  - `load`: Add "load" to load unloaded chunks (requires `chestscanner.load` permission)

### Material Search
- `/scan find <MATERIAL> [count] [load]` - Search for chests containing a specific material
  - `MATERIAL`: Material name (e.g., DIAMOND, IRON_INGOT)
  - `count`: Number of chunks to scan (default: 100)
  - `load`: Add "load" to load unloaded chunks

### Results Management
- `/scan list [page]` - View scan results with pagination (10 per page)
- `/scan tp <index>` - Teleport to a chest from scan results
- `/scan export <csv|yml>` - Export scan results to file

### Chest Information
- `/scan owner <index>` - Show who placed the chest at the given index
- `/scan who <index> [lines]` - View access log for a chest
  - `lines`: Number of log entries to show (1-50, default: 10)

### Control
- `/scan stop` - Stop an active scan

## Permissions

- `chestscanner.scan` (default: op) - Use basic scan commands
- `chestscanner.load` (default: op) - Allow loading unloaded chunks during scan
- `chestscanner.tp` (default: op) - Use teleport command
- `chestscanner.export` (default: op) - Export scan results

## Configuration

Edit `plugins/ChestScanner/config.yml` to customize behavior:

```yaml
# Number of chunks to process per server tick (higher = faster but more lag)
chunks-per-tick: 2

# Show progress message every N chunks processed
progress-every: 20

# Maximum number of clickable results to show after scan completes
max-list-show: 30

# Whether to attempt loading chunks by default (can be overridden per command)
attempt-load-default: false
```

## Building from Source

Requirements:
- Java Development Kit (JDK) 8 or higher
- Maven

Build steps:
```bash
git clone https://github.com/Puk3p/ChestScanner.git
cd ChestScanner
mvn clean package
```

The compiled JAR will be in the `target` folder.

## Examples

### Scan 50 chunks around you
```
/scan start 50
```

### Find chests with diamonds in 200 chunks, loading unloaded chunks
```
/scan find DIAMOND 200 load
```

### View scan results
```
/scan list
/scan list 2
```

### Teleport to the 5th chest in results
```
/scan tp 5
```

### Check who owns the 3rd chest
```
/scan owner 3
```

### View last 20 access logs for the 3rd chest
```
/scan who 3 20
```

### Export results to CSV
```
/scan export csv
```

## How It Works

1. **Scanning**: The plugin scans chunks in a spiral pattern starting from your location
2. **Detection**: Checks all tile entities for chests (ignores Ender Chests as they're personal)
3. **Filtering**: Optionally filters by material or shows all non-empty chests
4. **Storage**: Results are stored per-player and can be accessed via list, tp, export commands
5. **Tracking**: Ownership is recorded when chests are placed, access logs track inventory changes

## License

This project is maintained by [Puk3p](https://github.com/Puk3p).

## Support

For issues, questions, or feature requests, please visit the [GitHub repository](https://github.com/Puk3p/ChestScanner).

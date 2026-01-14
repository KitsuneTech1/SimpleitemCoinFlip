# SimpleItemCoinFlip

A Minecraft 1.21.1 plugin for GUI-based coinflip gambling with multiple currency support.

## Features

- 🎲 **GUI-based betting** - Easy to use inventory menus
- 💎 **Multiple currencies** - Diamonds, Iron Ingots, Emeralds
- 👤 **Player head animation** - Animated coinflip with player heads
- ⚖️ **Fair 50/50 odds** - Random winner selection
- 🔄 **Auto-refund** - Items returned on cancel or server shutdown

## Commands

| Command | Description |
|---------|-------------|
| `/coinflip` | Opens the coinflip menu |
| `/cf` | Alias for /coinflip |

## Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `coinflip.use` | Use the coinflip system | true |
| `coinflip.admin` | Admin commands | op |

## Installation

1. Download the latest release JAR
2. Place in your server's `plugins/` folder
3. Restart the server
4. Use `/coinflip` to start!

## Configuration

Edit `plugins/CoinflipPlugin/config.yml` to customize:
- Messages and prefixes
- Animation speed and duration
- Min/max bet limits
- Enable/disable currencies

## Compatibility

- **Minecraft:** 1.21.1
- **Server:** Spigot, Paper, Purpur

## Building

```bash
mvn clean package
```

The compiled JAR will be in `target/CoinflipPlugin.jar`

## License

MIT License - See [LICENSE](LICENSE) for details.

# BedWars Practice Plugin
Abandoned Project. use at ur own risk

Current world-per-player system may cause massive server lag.

Enhance your Bedwars skills with this advanced practice plugin! Perfect for players who want to master bed-breaking strategies while evading enemy attacks.

## Features

### 1. Multi-Map System
- Each player gets a private world when the game starts.
- Automatically deletes the world when the game ends to save resources.

### 2. Automatic Wool Destruction
- Wool blocks placed by the player during the game are automatically removed.
- Fully customizable via game options:
    - Enable/disable automatic wool breaking.
    - Adjust the delay and interval for wool destruction.

### 3. Damage Over Time (Open Space Penalty)
- Players take continuous damage if they do not fully surround themselves with blocks.
- Adjustable damage intensity through game options.

## Installation

1. Download the latest release from [Releases](https://github.com/dogsbean/BedwarsPractice/releases/tag/Release).
2. Place the plugin `.jar` file into the `plugins` folder of your Spigot/Paper server (1.8.8+).
3. Restart or reload your server.

## Commands

- `/setup setlobby` - Sets spawn
- `/setup createMap <map name>` - Creates new map with specific name.
- `/setup setspawn <map name>` - Sets spawn of map
- `/setup saveBlocks <map name>` - Save the cuboid area for map with selected pos1, 2.
- `/setup wand` - Get a wand tool for map area creation.

## Contributing

Feel free to open issues and pull requests for improvements or bug fixes.

## Support

For support or inquiries, open an ticket in my discord [Support Discord](https://discord.gg/xydjE7ym5W).


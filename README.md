# InventoryRestore

### Introduction

**Description**

InventoryRestore is a plugin which backs up player inventories for various events. Useful if players lose items due to lag, griefing and more!

**When does the plugin backup player inventories?**

When a player: Joins, Leaves, Dies, Changes world, or when requested by staff. Also auto-saves every 30 seconds when enabled.

**What does the plugin save?**

The plugin saves the player's: Inventory, Enderchest, Location, Hunger, XP. (Health restore has been removed.)

**How do I use the plugin?**

When a backup is created, it is added to a list of available backups to view and restore.

Players with the required permission can open a rollback menu by running the command `/ir restore [player]`. You will be presented with all the recent backups. To view a backup just click on the corresponding icon. You can choose to restore what you want or go back to the list of backups.

The plugin saves 50 deaths and 10 joins, leaves and world changes by default. Crash recoveries from the 30-second autosave are stored separately. You can change these values in the configuration file.

### Documentation

**Commands**

- `/ir restore [player]` - Open a menu to view all player backups
- `/ir forcebackup <all/player> [player]` - Create a backup manually
- `/ir enable` - Enable the plugin
- `/ir disable` - Disable the plugin
- `/ir reload` - Reload the configuration file

**Permissions**

- `inventoryrestore.viewbackups` - (Default: OP) Allow /ir restore (without ability to restore)
- `inventoryrestore.restore` - (Default: OP) Allow restore operations
- `inventoryrestore.restore.teleport` - (Default: OP) Allow teleport to backup location
- `inventoryrestore.forcebackup` - (Default: OP) Allow /ir forcebackup
- `inventoryrestore.enable` - (Default: OP) Allow /ir enable
- `inventoryrestore.disable` - (Default: OP) Allow /ir disable
- `inventoryrestore.reload` - (Default: OP) Allow /ir reload
- `inventoryrestore.adminalerts` - (Default: OP) Admin notifications on join
- `inventoryrestore.deathsave` - (Default: All) Backup on death
- `inventoryrestore.joinsave` - (Default: All) Backup on join
- `inventoryrestore.leavesave` - (Default: All) Backup on leave
- `inventoryrestore.worldchangesave` - (Default: All) Backup on world change
- `inventoryrestore.help` - (Default: All) View help
- `inventoryrestore.version` - (Default: All) View version

### Author

notauthorised

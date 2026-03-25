# InventoryRestore

[![Discord](https://img.shields.io/discord/1480618281189773314?label=Discord&logo=discord&logoColor=white&color=5865F2)](https://discord.gg/qdmSv7usbJ)
[![GitHub](https://img.shields.io/badge/GitHub-inventoryrestore-181717?logo=github)](https://github.com/vanillaxtra/inventoryrestore)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.13--1.21.x-62B47A)](https://github.com/vanillaxtra/Dinventoryrestore)
[![Folia](https://img.shields.io/badge/Folia-Supported-blue)](https://github.com/vanillaxtra/inventoryrestore)

**InventoryRestore** backs up player inventories and lets staff inspect, restore, and document refunds—with optional MySQL, crash recovery, autosave, and a Discord webhook for refund confirmations.

---

## Features

### Backups & storage

- **Automatic saves** when players **join**, **quit**, **die**, **change worlds**, **crashes**, and on a configurable **autosave** interval

- **Force backup** for one online player or **everyone online** (`/ir forcebackup`).
- **YAML** (default) or **MySQL** for centralized storage.
- Per-player limits per save type (`max-saves`: join, quit, death, world-change, force, crash).
```
## Maximum saves a backup will hold per type per user.
max-saves:
  join: 10
  quit: 10
  death: 50
  world-change: 10
  force: 10
  crash: 20
```
- **Material ignore list** so chosen items are never stored (`backup-ignore-materials`) this is so that item is not taking up lots of storage for example string if you have a `/string` plugin.
```
## Materials (Bukkit names) excluded from backups and not stored. Example: [DIRT, STONE]
## Empty list = nothing ignored.
backup-ignore-materials: []
```
- **Offline restores & inventory viewing**

- **Export restores to shulkers, chests, bundles & drop** Incase you dont want to restore someones current inventory you can put chests at their base, put a bundle in their inventory etc.

### Staff GUIs



- **Main menu** — browse players (restore mode shows names; refund mode can hide names for privacy).
![Main menu](https://raw.githubusercontent.com/vanillaxtra/inventoryrestore/refs/heads/main/assets/mainmenu.png)


- **Player menu** — choose backup category (join, quit, death, world change, force, crash).
![Main inventory backup menu](https://raw.githubusercontent.com/vanillaxtra/inventoryrestore/refs/heads/main/assets/backupmenu.png)

| Joins | Quits | Deaths |
|-------|-------|--------|
| ![Joins backups](https://raw.githubusercontent.com/vanillaxtra/inventoryrestore/refs/heads/main/assets/joins.png) | ![Quits backups](https://raw.githubusercontent.com/vanillaxtra/inventoryrestore/refs/heads/main/assets/quits.png) | ![Death backups](https://raw.githubusercontent.com/vanillaxtra/inventoryrestore/refs/heads/main/assets/deaths.png) |

| World changes | Force saves | Crashes |
|---------------|-------------|---------|
| ![World change backups](https://raw.githubusercontent.com/vanillaxtra/inventoryrestore/refs/heads/main/assets/worldchanges.png) | ![Force saves](https://raw.githubusercontent.com/vanillaxtra/inventoryrestore/refs/heads/main/assets/forcesaves.png) | ![Crash backups](https://raw.githubusercontent.com/vanillaxtra/inventoryrestore/refs/heads/main/assets/crashes.png) |


- **Rollback list** — paginated list of snapshots with timestamps (timezone/format configurable).

![Rollback list](https://raw.githubusercontent.com/vanillaxtra/inventoryrestore/refs/heads/main/assets/rollbacklist.png)

- **Main inventory backup** — full **36-slot preview**, **armor & off-hand** in the same layout as vanilla, plus actions:
![Player / restore menu](https://raw.githubusercontent.com/vanillaxtra/inventoryrestore/refs/heads/main/assets/restoremenu.png)
  - Restore main inventory + armor + off-hand (with **overwrite warning** when the target still has items; skipped if their inventory is **completely empty** when detectable).
  - **Ender chest** backup view and restore.
  - **Hunger** and **XP** restore (when target is online).
  - **Teleport** to saved backup location (permission `inventoryrestore.restore.teleport`).
  - **Export to storage** (extract items for manual handling).
  - **Backup activity** — who opened or restored a snapshot (audit trail).
- **Overwrite warning** — shows the target’s **current** inventory (or last saved layout offline) including **armor & off-hand** before confirming a full restore.
![Overwrite warning](https://raw.githubusercontent.com/vanillaxtra/inventoryrestore/refs/heads/main/assets/overwritewarning.png)

#### Screenshots — restore actions & tools

| Teleport to backup coords | Ender chest | Ender chest |
|---------------------------|-----------------|-----------------|
| ![Restore coords](https://raw.githubusercontent.com/vanillaxtra/inventoryrestore/refs/heads/main/assets/restorecoords.png) | ![Ender chest restore 1](https://raw.githubusercontent.com/vanillaxtra/inventoryrestore/refs/heads/main/assets/restoreenderchest1.png) | ![Ender chest restore 2](https://raw.githubusercontent.com/vanillaxtra/inventoryrestore/refs/heads/main/assets/restoreenderchest2.png) |

| Restore food | Restore XP | Restore offline |
|--------------|------------|-----------------|
| ![Restore food](https://raw.githubusercontent.com/vanillaxtra/inventoryrestore/refs/heads/main/assets/restorefood.png) | ![Restore XP](https://raw.githubusercontent.com/vanillaxtra/inventoryrestore/refs/heads/main/assets/restorexp.png) | ![Restore offline](https://raw.githubusercontent.com/vanillaxtra/inventoryrestore/refs/heads/main/assets/restoreoffline.png) |

| Export (chest) | Export (bundle) |
|----------------|-----------------|
| ![Export chest](https://raw.githubusercontent.com/vanillaxtra/inventoryrestore/refs/heads/main/assets/exportchest.png) | ![Export bundle](https://raw.githubusercontent.com/vanillaxtra/inventoryrestore/refs/heads/main/assets/exportbundle.png) |

![Backup activity](https://raw.githubusercontent.com/vanillaxtra/inventoryrestore/refs/heads/main/assets/backupactivity.png)



| Refund / restore history (1) | Refund / restore history (2) |
|------------------------------|------------------------------|
| ![Restore history 1](https://raw.githubusercontent.com/vanillaxtra/inventoryrestore/refs/heads/main/assets/restorehistory1.png) | ![Restore history 2](https://raw.githubusercontent.com/vanillaxtra/inventoryrestore/refs/heads/main/assets/restorehistory2.png) |

### Commands & safety

- **`/restore`** / **`/ir restore [player|uuid]`** — open restore GUI; supports **UUID** or name tab-complete.
- **`/refund`** / **`/ir refund [player|uuid]`** — refund GUI + ledger context.
- **`/ir enable`**, **`/ir disable`**, **`/ir reload`**, **`/ir version`**, **`/ir help`**.
- **`/ir import confirm`** — one-time migration from legacy backup data (requires **`inventoryrestore.import`**; grant via your permissions plugin if needed).
- Configurable **death event ordering** vs other plugins (`allow-other-plugins-edit-death-inventory`).
- **`loadbefore`** several death-chest style plugins to reduce ordering conflicts.

### Integrations & ops

- Optional **Discord refund webhook** when a full restore is confirmed from the refund GUI (`refund-webhook` in `config.yml`).
- **Update checker** on startup (configurable).
- **Sounds** for restore, teleport, food, hunger, and XP (each togglable).

---

## Commands

| Command | Permission | Description |
|--------|------------|-------------|
| `/restore [player]` | `inventoryrestore.viewbackups` | Open restore GUI (optional target). |
| `/refund [player]` | `inventoryrestore.refund` | Open refund GUI. |
| `/ir restore [player]` | `inventoryrestore.viewbackups` | Same as `/restore`. |
| `/ir refund [player]` | `inventoryrestore.refund` | Same as `/refund`. |
| `/ir forcebackup all` | `inventoryrestore.forcebackup` | Force-save all online players. |
| `/ir forcebackup player <name>` | `inventoryrestore.forcebackup` | Force-save one online player. |
| `/ir enable` | `inventoryrestore.enable` | Turn plugin on. |
| `/ir disable` | `inventoryrestore.disable` | Turn plugin off. |
| `/ir reload` | `inventoryrestore.reload` | Reload config. |
| `/ir version` | `inventoryrestore.version` | Version info. |
| `/ir help` | `inventoryrestore.help` | In-game command list. |
| `/ir import confirm` | `inventoryrestore.import` | Import legacy backups (destructive; confirm only). |

Child permission **`inventoryrestore.restore.teleport`** — allows teleporting to the backup’s saved world/coords from the GUI.

Full permission tree is in **`plugin.yml`**.

---

## Configuration (`config.yml`)

| Option | What it does |
|--------|----------------|
| `enabled` | Master switch. |
| `max-saves` | Caps per type per player. |
| `backup-lines-visible` | Rows visible on rollback list (max 5). |
| `folder-location` | Data path or `DEFAULT`. |
| `mysql` | Enable DB + pool settings. |
| `sounds.*.enabled` | Toggle feedback sounds. |
| `time-zone` / `time-format` | Backup timestamp display. |
| `allow-other-plugins-edit-death-inventory` | Death save timing vs other plugins. |
| `restore-to-player-button` | Show full-restore button. |
| `save-empty-inventories` | Whether empty snapshots are kept. |
| `backup-ignore-materials` | Bukkit material names to skip. |
| `refund-webhook` | Discord POST on refund-path full restore. |
| `autosave-*` | Interval autosave + crash recovery behavior. |
| `update-checker` | GitHub Releases update notice (`vanillaxtra/inventoryrestore`). |
| `bStats` | Anonymous metrics (see below). |
| `debug` | Extra console logging. |

---

## 📊 bStats

[![bStats](https://img.shields.io/badge/bStats-inventory--restore-6366f1?style=flat-square)](https://bstats.org/plugin/bukkit/inventory-restore/30407)

Anonymous usage metrics help gauge adoption and environments. Disable in `config.yml` with `bStats: false`.
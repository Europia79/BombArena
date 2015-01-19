BattleArena Demolition
======
Bukkit plugin that adds the Call of Duty game modes (Sabotage & SND) 
to Minecraft servers.


- **Demolition**
  * this word describes a group of construction workers that demolish a building.
  * SOCOM game mode similar to Sabotage in Call of Duty.
  * This plugin was inspired by SOCOM: US Navy Seals.
- **Sabotage**
  * There is one bomb in the middle of the map.
  * There are two teams each with their own base.
  * There are three ways to win:
    - Eliminate the other team.
    - Pickup the bomb and destroy the other teams base.
    - Protect your own base by defusing the bomb before it detonates.
  * use the `/bomb` command to join/create arenas.
- **Search N Destroy (SND)**
  * There are two teams: attackers & defenders.
  * The team closest the bomb is designated as the attacking team.
  * The defenders cannot pickup the bomb.
  * There are one of more objectives (bases) to destroy/defend.
  * There are four ways to win:
    - Eliminate the other team.
    - Attackers can win by picking up the bomb and destroying an objective.
    - Defenders can win by defusing the bomb.
    - Defenders can win by letting time expire.
  * use the `/snd` command to join/create arenas.
  
  
Downloads:
---

**Official builds**

You can find the official builds at dev.bukkit.org .The source code for these builds 
have been checked to make sure that they do NOT contain any malicious code. 

[http://dev.bukkit.org/bukkit-plugins/bombarena/] (http://dev.bukkit.org/bukkit-plugins/bombarena/ "Official builds")


**Development builds**

```python
"Development builds of this project can be acquired at the provided continuous integration server."
"These builds have not been approved by the BukkitDev staff. Use them at your own risk."
```

[http://ci.battleplugins.com/job/BombArena/](http://ci.battleplugins.com/job/BombArena/ "dev builds")

The dev builds are primarily for testing purposes.

Dependencies:
---

- **Bukkit API**
  * https://github.com/Bukkit/Bukkit
  * Requires Spigot, Craftbukkit, or any other server software that implements the Bukkit API.
- **BattleArena**
  * http://dev.bukkit.org/bukkit-plugins/battlearena/
  * The Demolition plugin is an addon for BattleArena.
  * Required dependency
- **BattleTracker**
  * http://dev.bukkit.org/bukkit-plugins/battletracker/
  * Used to track player stats like "Bombs planted" and "Bombs defused"
  * Optional dependency
- **Holographic Displays**
  * http://dev.bukkit.org/bukkit-plugins/holographic-displays/
  * Adds icons on target bases and bombs that are dropped on the ground.
  * Optional dependency
- **HoloAPI**
  * http://dev.bukkit.org/bukkit-plugins/holoapi/
  * Adds icons on target bases and bombs that are dropped on the ground.
  * Optional dependency
- **EnjinMinecraftPlugin**
  * http://dev.bukkit.org/bukkit-plugins/emp/
  * Used to track player stats like "Bombs planted" and "Bombs defused"
  * Future optional dependency


Arena Setup:
---

These commands will create a Sabotage arena.  
If you want to create an SND arena, just use the `/snd` command instead.

`/bomb create ArenaName`

`/bomb alter ArenaName wr 1` (Waiting rooms)

`/bomb alter ArenaName wr 2` (Waiting rooms)

`/bomb alter <arena> 1` (spawn point for team1)

`/bomb spawnbomb <arena>` (sets the bomb spawn location)

If the `/bomb spawnbomb <arena>` command ever breaks, then you can 
use these two commands instead:

`/aa select <arena>`

`/aa addspawn 46 fs=1 rs=300 index=1`

"aa stands for `/arenaAlter`. `fs` stands for First Spawn (1 second after the match begins). 
`rs=300` stands for ReSpawn after 300 seconds. `46` is TNT (substitute it with whatever 
you've defined the BombBlock to be inside config.yml). 

The `addspawn` command can be used to spawn mobs or other items, 
but please leave index one for the bomb.


Base Setup
---


The plugin needs some kind of way to identify bases 
and assign players to a base that they must defend... 
and to make sure players cannot arbitrarily destroy ANY base they find 
(like their own), but rather, force them to find and destroy the other teams base.

This is how you make bases:

- place two BaseBlocks (1 at each base. The default BaseBlock is BREWING_STAND, but this can be changed in the config.yml)
- do `/bomb addbase <arena>`
- or `/snd addbase <arena>`

You can now join a BombArena or SndArena (or even get a VirtualPlayer 
to test it out). Note however, that having 1 base for SND gives a 
slight advantage to the defenders (since they only have to let time expire to win). 
Putting a 2nd (or more) base makes it more challenging for defenders and 
evens the playing field.

Other commands:
---

| Command | Permission | Description |
|:----------|:-------------------------:|:------------------:|
|`/bomb join` | arena.join.bombarena | Join the Sabotage game-mode |
|`/snd join`  | arena.join.sndarena  | Join the Search-N-Destroy game-mode |
|`/bomb leave` | arena.leave.bombarena | Exit the arena |
|`/bomb forcestart` | arena.admin | Force start the arena |
|`/bomb stats` | bomb.stats | Display your own personal stats |
|`/bomb stats <player>` | bomb.stats.other | View another player's stats |
|`/bomb stats top <X>` | bomb.stats.top | Display the leaderboard |
|`/bomb spawnbomb <arena>` | bombarena.spawnbomb | Set the spawn location for the bomb |
|`/bomb addbase <arena>` | bombarena.addbase | Each arena must have 2 bases |
|`/bomb removebase <arena>` | bombarena.addbase | Alias: delete, clear |
|`/bomb removeallbases <arena>` | bombarena.addbase | Alias: delete, clear |
|`/bomb listconfig` | bombarena.setconfig | View all config.yml options |
|`/bomb setconfig <option> <value>` | bombarena.setconfig | Used to set config.yml options |
|`/bomb debug` | bombarena.debug | Toggles debugging mode on/off |


(Optional) You can add a Worldguard region to BattleArena 
so that block changes reset after each match. (Be careful 
when using LARGE areas, it might lag your server).

`/region select RegionName`

`/bomb alter <arena> addregion`

FYI: You do NOT need to use this last command.
The Demolition plugin will automatically reset bases after each match.
The last command should be only used if you want players to break blocks 
(or access chests) in the arena and have WorldGuard reset all the broken blocks 
to their original state after each match.


How to access Player Stats Database:
---
```sql
sqlite3 tracker.sqlite
.tables
.schema bt_Demolition_overall
```
output:
```sql
CREATE TABLE bt_Demolition 
  (ID VARCHAR(32) NOT NULL,
  Name VARCHAR(48),
  Wins INTEGER UNSIGNED,
  Losses INTEGER UNSIGNED,
  Ties INTEGER UNSIGNED,
  Streak, INTEGER UNSIGNED,
  maxStreak INTEGER UNSIGNED,
  Elo INTEGER UNSIGNED DEFAULT 1250,
  maxElo INTEGER UNSIGNED DEFAULT 1250,
  Count INTEGER UNSIGNED DEFAULT 1,
  Flags INTEGER UNSIGNED DEFAULT 0,
  PIMRARY KEY (ID));
```
sql command:
```sql
SELECT ID, Wins, Losses, Ties FROM bt_Demolition_overall order by Wins desc limit 100;
```
sample output:

| Player | No. of Bombs Planted Successfully | No. of Bombs Failed   | No. of Bombs Defused |
|:-------|:-------------------------------:|:-------------------:|:------------------:|
|`Autumn07`     | 25  | 75 | 3  |
|`SmileyBrooke` | 99  | 1  | 25 |
|`Europia79`    | 9   | 1  | 25 |
|`Ralkia`       | 4   | 1  | 25 |
|**Totals**   | 137 | 78 | 78 |


As you can see, the `No. of Bombs Defused` in the last column 
will ALWAYS equal the `No of Bombs that Failed to detonate`.


Also notice what the SQL columns mean for the Bomb Game Type:
```sql
  Wins   = # of Bombs Planted Successfully
  Losses = # of Bombs that Failed to Detonate
  Ties   = # of Bombs Defused
```


Notice that each time Autumn planted the bomb, she successfully 
destroyed the other teams based 25% of the time, and that she caused 
her own team to lose 75% of the time. Whereas Brooke was successful 
99% of the time. Europia was 90% successful, and Ralkia was 80% successful. 


So... you can use BattleTracker to store player stats into either 
an sqlite or MySQL database... You can then have your website access 
the database and print the player stats. Just use the above SQL SELECT 
command: it's the same for both sqlite and MySQL. Then, you can even 
calculate the percentages and display those too if you want.

Also, there's a fake player in the SQL table called 
`Bombs Planted Defused` that contains the totals.


To-Do List
---
- Add custom events.
- Add Timer + Event Sounds per player.
- Add Timer bars & timer holograms.
- Allow for more customized Holograms via config.yml
- Maybe change plant/defuse mechanics ?
- ~~Finish implementing backwards-compatibility with BattleArena.~~ done.
- ~~Add & implement other commands:~~
- ~~/bomb clearbase <arena>~~ done.
- ~~/bomb clearallbases <arena>~~ done.
- write PHP script to access the database & display player stats on a website.
- ~~Finish Search N Destroy.~~ done.
- ~~Implement config options.~~ done.
- ~~Update arenas.yml to match any changes in config.yml option BombBlock.~~ done.
- ~~Update bases at the start of the game to match config option BaseBlock.~~ done.
- ~~Restore missing (destroyed) brewing stands (bases) at the end of the match.~~ done.
- ~~Close plant+defuse exploit~~ done.
- ~~Kill off players that are too close to the bomb when it detonates.~~ done.
- ~~Add compass direction to let players know the location of a dropped bomb.~~ done.
- ~~Add HAT to the bomb carrier onBombPickup()~~ done.
- ~~Take away the HAT+BombBlock after the player plants the bomb (instead of at the end of the match).~~ done.
- ~~update to UUID~~ Not necessary because BattleTracker handles persistent data.
- ~~/bomb setconfig <option> <value>~~ done.
- ~~have onBombPlace() trigger onBombPlant() event (if the player is close enough).~~ done.


Bugs to fix:
---
- ~~onBombSpawn(ItemSpawnEvent e) breaks ALL other events.~~ fixed
- ~~onBombDespawn(ItemDespawnEvent e) breaks ALL other events.~~ fixed
  

Known caveats:
---
- Requires Craftbukkit v1.5 or newer.
- Requires BattleArena v3.9.6 or newer.
- bases.yml is deprecated.
  

Contact:
======

Nick at Nikolai.Kalashnikov@hotmail.com

Nicodemis79 on Skype


[http://rainbowcraft.net/](http://Rainbowcraft.net/ "Rainbowcraft")


Javadocs & Wiki
---

[http://wiki.battleplugins.org/Main_Page](BattleArena wiki "BattleArena wiki")
[http://javadocs.rainbowcraft.net/BattleArena](BattleArena javadocs "BattleArena javadocs")
[http://javadocs.rainbowcraft.net/BombArena](BombArena javadocs "BombArena javadocs")

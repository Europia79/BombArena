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
  * There are two teams each wtih their own base.
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
  * Demolition plugin is just a game-type addition to BattleArena.
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

`/aa addspawn 172 fs=1 rs=300 1`

"aa stands for `/arenaAlter`. `fs` stands for First Spawn (1 second after the match begins). 
`rs=300` stands for ReSpawn after 300 seconds. `172` is HARD_CLAY (substitute it with whatever 
you've defined the BombBlock to be inside config.yml). 

Other commands:

**/bomb join**

**/bomb leave**

**/bomb forcestart**

**/bomb delete ArenaName**


Base Setup
---


The plugin needs some kind of way to identify bases 
and assign players to a base that they must defend... 
and to make sure players cannot arbitrarily destroy ANY base they find 
(like their own), but rather, force them to find and destroy the other teams base.

This is how you make bases for Sabotage:

- place two BaseBlocks (1 at each base. The default BaseBlock is BREWING_STAND, but this can be changed in the config.yml)
- do `/bomb setbase <arena> <teamID>` (value for team can be 1 or 2)

This is how you make bases for Search N Destroy:

- place a BaseBlock
- do `/snd addbase <arenaName>`

You can now join a BombArena or SndArena (or even get a VirtualPlayer 
to test it out). Note however, that having 1 base for SND gives a 
slight advantage to the defenders (since they only have to let time expire to win). 
Putting a 2nd (or more) base makes it more challenging for defenders and 
evens the playing field.

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


Listener Methods:
---
- onBombSpawn(ItemSpawnEvent e)
   * Set the compass so players know where the bomb is located.
- onBombPickup(PlayerPickupEvent e)
   * put a HAT on the bomb carrier so that players know WHO has the bomb.
   * Map the Arena to a value of PlayerName (so that the plugin itself knows who has the bomb for each arena).
   * Set the compass to the opponents base.
- onBombCarrierLeave(ArenaPlayerLeaveEvent e)
   * Does this person have the bomb ?
   * if so, drop it on the ground.
- onCarrierDeath(PlayerDeathEvent e)
   * remove them from the map listing.
   * drop the bomb on the ground.
- onBombDrop(PlayerDropItemEvent e)
   * make sure they didn't throw it outside the map.
   * point the compass to the direction of the bomb and
   * give a visual aid so that players know the location of the bomb.
- onBombDespawn(ItemDespawnEvent e)
   * This event breaks ALL other events.
   * The despawn time for the bomb is now set to the entire duration of the match.
   * cmd (/bomb spawnbomb) is the cmd that sets the duration on arena setup.
- onBombPlace (BlockPlaceEvent e)
   * Going to help new players out by calling onBombPlant if they're close enough.
   * If they're not close enough, then tell the player to follow their compass.
   * client-side bug (invisible bomb) was fixed with deprecated updateInventory().
- onBombPlantDefuse(InventoryOpenEvent e)
   * check defuse conditions.
   * check plant conditions.
   * start a 8 sec PlantTimer or 8 sec DefuseTimer.
   * Successful PlantTimer will start a 30 sec DetonateTimer.
   * Successful DetonationTimer or DefuseTimer will declare the winners.
- onBombPlantFailure(InventoryCloseEvent e)
   * cancel() the PlantTimer or DefuseTimer.
   * Notice that there's two ways for Plant or Defusal to fail: 
      1. Player prematurely closes the inventory, or 
	  2. the player dies. If the player dies, then we'll let onCarrierDeath handle everything.
- onBaseExploit(BlockBreakEvent e)
   * Prevents players from destroying base blocks.
- onBaseInteraction(PlayerInteractionEvent e)
   * Allows players to plant+defuse inside protected regions.


To-Do List
---
- test against the lastest versions of BattleTracker.
- Finish implementing backwards-compatibility with BattleArena.
- Add & implement other commands:
- /bomb checkbase
- /bomb clearbases <arena> (listen for /bomb delete <arena>)
- write PHP script to access the database & display player stats on a website.
- Fix Timer Sounds to broadcast per player.
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
- onBombSpawn(ItemSpawnEvent e) breaks ALL other events.
- ~~onBombDespawn(ItemDespawnEvent e)~~ breaks ALL other events.
- onBombDespawn() is not necessary since the despawn time is now set to the max duration of the match.
  

Known Issues:
---
- Requires BattleArena v3.9.7.3 or newer.
- Killing a player who is planting or defusing the bomb should stop their progress but this was never tested since it requires a 2nd player to test.
  

Contact:
======

Nick at Nikolai.Kalashnikov@hotmail.com

Nicodemis79 on Skype


[http://rainbowcraft.net/](http://Rainbowcraft.net/ "Rainbowcraft")


Javadocs & Wiki
---

[http://ci.battleplugins.com/job/BombArena/javadoc/](http://ci.battleplugins.com/job/BombArena/javadoc/ "javadocs")

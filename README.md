BattleArena Demolition
======
Bukkit plugin that adds the Demolition game type to
Minecraft servers running BattleArena (dependency).


Demolition - There is one bomb in the middle of the map.
There are two teams each with their own base. 
There are three ways to win: 1. Eliminate the other team. 
2. Pickup the bomb and destroy the other teams base. 
3. Protect your own base by defusing the bomb before it 
detonates.


Concept to Implementation
---
So the initial question is 
"How do we implement this in Minecraft ?" and 
What mechanics & system will we use ?


First, I observed that breaking a block naturally 
takes a few seconds to perform... So this is a very good 
mechanism to use for defusing the bomb because we don't 
have to use system resources for a timer here. Notable blocks 
that are good to use: Brick + Nether Brick which both take 10 seconds 
to destroy, Hardened Clay at 7 seconds, and Logs at about 4 to 5 seconds. 
Altho, I might change this in the future (so the timing is less restrictive 
and more customizable).


However, placing a block is instaneous... and most Shooter games 
that I've played require about 5 to 8 seconds in order to plant 
the bomb. So what I'll probably do... is have two brewing stands... 
Each brewing stand will symbolize the position of a base... and 
it'll be the exact spot where you plant the bomb. When a player attempts 
to plant the bomb, the brew inventory will stay open until the bomb 
is planted (5 to 8 sec) then close. At which point, it'll turn into a bomb block that opposing 
players can break to defuse. (Please let me know if you have other ideas 
for how this can work).


Like most shooter games, if the bomb carrier dies... or throws the bomb on 
the ground, then we want some kind of way to let all the players know where the 
bomb is located. Easiest option will be via a compass. But I also want to 
add some kind of visual aid.


Any other ideas on how this can be implemented into Minecraft, 
just lemme know! (contact info below)


Arena Setup:
---
Create the arena:
**/bomb create ArenaName**

This creates one waiting room "wr" shared by both teams: 

**/bomb alter ArenaName wr**

This creates a waiting room "wr" for each team: 

**/bomb alter ArenaName wr 1**

**/bomb alter ArenaName wr 2**

Set spawn point for Team 1:
**/bomb alter ArenaName 1**

"aa" stands for /arenaAlter:
**/aa select ArenaName**

"fs" stands for First Spawn (1 second after the match begins).
172 is the bomb block (which is HARD_CLAY). The last one is the index 
for addspawn (which means that you can spawn other items and/or mobs in other indexes).

**/aa addspawn 172 fs=1 1**

This will ask you to click a block: That block will be saved:
**/aa addblock**

Alternatively, you can define a WorldGuard region. 
Use one of these two options on the two Brewing Stands. 
The Brewing Stands define where a base is located. 
If the Brewing Stand is destroyed by the bomb, then you'll 
need one of these options to reset it back after each match.

Also,

**/aa showSpawns**

**/aa hideSpawns**

**/aa listSpawns**

Finally,

**/bomb join**

**/bomb leave**

**/bomb forcestart**

**/bomb delete ArenaName**


Base & Region Setup
---
The Demolition plugin needs some kind of way to identify bases 
and assign players to a base that they must defend... 
and to make sure players cannot arbitrarily destroy ANY base they find 
(like their own), but rather, force them to find and destroy the other teams base.

This is how you make bases that this plugin can read:

First, make a WorldGuard region with //wand and/or //pos1 //pos2 //hpos1 //hpos2. 

Then, fly next to one of the Brewing Stands and type:

**/region flag RegionName teleport here**

Goto the other base and do

**/region flag RegionName spawn here**

Optional, add the Worldguard region to BattleArena 
so that block changes reset after each match. (Be careful 
when using LARGE areas, it might lag your server).

**/region select RegionName**

**/bomb alter ArenaName addregion**

FYI: You do NOT need to use this last command.
The Demolition plugin will automatically reset bases after each match.
The last command should be only used if you want players to break blocks 
in the arena and have WorldGuard reset all the broken blocks to their original 
state after each match.

Some of these steps might change in the future to make to easier to setup Bomb Arenas. 


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
   * There are two different ways to handle this:  
     1. cancel the event OR
	 2. respawn a new bomb
- onBombPlace (BlockPlaceEvent e)
   * Going to help new players out by calling onBombPlant if they're close enough.
   * If they're not close enough, then tell the player to follow their compass.
   * client-side bug (invisible bomb) was fixed with deprecated updateInventory().
- onBombPlant(InventoryOpenEvent e)
   * start a 7 sec PlantTimer 
   * if successful, will start a 30 sec DetonateTimer.
   * if successful, declare the winners.
- onPlantFailure(InventoryCloseEvent e)
   * cancel() the PlantTimer. 
   * Notice that there's two ways for the BombPlant to fail: 
      1. Carrier prematurely closes the inventory, or 
	  2. the Carrier dies. If the carrier dies, then we'll let onCarrierDeath handle everything.
- onBombDefuse(BlockBreakEvent e)
   * Get the player, get his team, and set the winners.


Dependencies:
---

- **BattleArena**
  * http://dev.bukkit.org/bukkit-plugins/battlearena/
  * Demolition plugin is just a game-type addition to BattleArena.
- **BattleTracker**
  * http://dev.bukkit.org/bukkit-plugins/battletracker/
  * Used to track player stats like "Bombs planted" and "Bombs defused"
  * Optional dependency
- **WorldGuard**
  * http://dev.bukkit.org/bukkit-plugins/worldguard/
  * Used to define bases and reset bases that are destroyed by the bomb.
- **WorldEdit**
  * http://dev.bukkit.org/bukkit-plugins/worldedit/
  * Needed for WorldGuard.
- **Holographic Displays**
  * http://dev.bukkit.org/bukkit-plugins/holographic-displays/
  * Adds visual aid for when the bomb is dropped on the ground.
  * Future dependency (not currently implemented).
  
  
To-Do List
---
- test against the lastest BattleArena version.
- fix the fake TNT block appearance.
- have onBombPlace() trigger onBombPlant() event (if the player is close enough).
- Add config options.
- ~~Restore missing (destroyed) brewing stands (bases) at the end of the match.~~ done.
- ~~Close plant+defuse exploit~~ done.
- ~~Kill off players that are too close to the bomb when it detonates.~~ done.
- ~~Add compass+visual aids to let players know the location of a dropped bomb.~~ done.
- ~~Add HAT to the bomb carrier onBombPickup()~~ done.


Bugs to fix:
---
- Remove HATs and bombs from the player inventory after the bomb has been planted.
- fix the fake TNT block appearance in PlantTimer
- onBombSpawn(ItemSpawnEvent e) breaks ALL other events.
- onBombDespawn(ItemDespawnEvent e) breaks ALL other events.
  

Known Issues:
---
- Obviously, there is NO handling for when the bomb despawns after 5 minutes because this event breaks all other events.
  

Contact:
======

Nick at Nikolai.Kalashnikov@hotmail.com

Nicodemis79 on Skype


[http://www.Battlecraft.co/](http://www.Battlecraft.co/ "Battlecraft")


Javadocs:
---

[http://javadocs.rainbowcraft.net/](http://javadocs.rainbowcraft.net/ "javadocs")

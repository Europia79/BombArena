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
it'll the exact spot where you plant the bomb. When a player attempts 
to plant the bomb, the brew inventory will stay open until the bomb 
is planted (5 to 8 sec) then close. At which point, it'll turn into a bomb block that opposing 
players can break to defuse. (Please let me know if you have other ideas 
for how this can work).


Like most shooter games, if the bomb carrier dies... or throws the bomb on 
the ground, then we some kind of way to let all the players know where the 
bomb is located. Easiest option will be via a compass. But I also want to 
add some kind of visual aid.


Any other ideas on how this can be implemented into Minecraft, 
just lemme know! (contact info below)


To-Do List
---
- Close plant+defuse exploit.
- Kill off players that are too close to the bomb when it detonates.
- Add compass+visual aids to let players know the location of a dropped bomb.
- Add config options.
- Add commands.


Timeline:
---
<dl>

<dt>3/15/2014 - BombListener.java</dt>
<dd> Listen for </dd>
<dd> 1. bomb pickup - and put a HAT on the bomb carrier 
so that other players know WHO has the bomb.</dd>
<dd> 2. bomb disappear - after 5 minutes (respawn it or cancel event)</dd>
<dd> 3. bomb carrier dies - (drop it on the ground)</dd>
<dd> 4. bomb plant - 30 seconds to detonate</dd>
<dd> 5. bomb defusal - BlockBreakEvent = takes 7 seconds</dd>
<dd> 6. bomb drop - make sure they didn't throw it outside the map. 
Also, let the players know the drop location (compass + visual aid).</dd>

<dt>3/15/2014 - Start</dt>
<dd>Laid the initial ground work for the 
internal structure and layout of the plugin.</dd>
</dl>


Contact:
======

Nick at Nikolai.Kalashnikov@hotmail.com

Nicodemis79 on Skype

[http://www.Battlecraft.co/](http://www.Battlecraft.co/ "Battlecraft")
[http://Rainbowcraft.net/](http://Rainbowcraft.net/ "Rainbowcraft")

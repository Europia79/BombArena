package com.github.Europia79.Demolition;

import com.github.Europia79.Demolition.commands.Demo;
import com.github.Europia79.Demolition.debug.*;
import com.github.Europia79.Demolition.tracker.TrackerOff;
import com.github.Europia79.Demolition.util.DetonateTimer;
import com.github.Europia79.Demolition.util.PlantTimer;
import java.util.HashMap;
import java.util.Map;
import mc.alk.arena.BattleArena;
import mc.alk.tracker.Tracker;
import mc.alk.tracker.TrackerInterface;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Bukkit plugin that adds the Demolition game type to Minecraft servers running BattleArena.
 * @author Nikolai
 */
public class Main extends JavaPlugin {
    
    /**
     * debug = new DebugOn(); <br/>
     * debug = new DebugOff(); <br/>
     * debug.log("x = " + x); <br/>
     * debug.messagePlayer(p, "debug msg"); <br/>
     * debug.msgArenaPlayers(match.getPlayers(), "info"); <br/><br/>
     * 
     * https://github.com/Europia79/debug for more info.
     */
    public DebugInterface debug;
    /**
     * This Map<matchID, playerName> contains the carrier for each match.
     */
    public Map<Integer, String> carriers;
    /**
     * <matchID, <teamID, Base Location>> contains the base location for each team & match.
     */
    public Map<Integer, Map<Integer, Location>> bases;
    /**
     * <matchID, new PlantTimer(InventoryOpenEvent e, getMatch())> contains the Plant Timer for each match.
     */
    public Map<Integer, PlantTimer> pTimers;
    /**
     * Possible future use to transfer responsibility of the Detonation Timer from PlantTimer to this class.
     */
    public Map<Integer, DetonateTimer> dTimers;
    /**
     * Adds Bombs Planted and Bombs Defused to the database. <br/>
     * WLT.WIN = Bomb Planted Successfully (opponents base was destroyed). <br/>
     * WLT.LOSS = Plant Failure caused by enemy defusal of the bomb. <br/>
     * WLT.TIE = Bomb Defused by the player. <br/>
     * Notice that in the databse, Ties = Losses.
     */
    public TrackerInterface ti;
    
    @Override  
    public void onEnable() {
        
        debug = new DebugOn(this);
        carriers = new HashMap<Integer, String>();
        bases = new HashMap<Integer, Map<Integer, Location>>();
        pTimers = new HashMap<Integer, PlantTimer>();
        dTimers = new HashMap<Integer, DetonateTimer>();
          
        // Commands are not yet implemented.
        getCommand("demolition").setExecutor(new Demo());
        
        BattleArena.registerCompetition(this, "Demolition", "demolition", BombArenaListener.class, new Demo());
        loadTracker("Demolition");
        getServer().dispatchCommand(Bukkit.getConsoleSender(), "bomb stats");
        
        
    }
    
    @Override
    public void onDisable() {

    }

    /**
     * 
     * @param i The tracker needs some way to identify all the different plugins that are using it to keep track of stats. This name "i" ("Demolition") will show up in the database tables too.
     */
    private void loadTracker(String i) {
        Tracker tracker = (Tracker) Bukkit.getPluginManager().getPlugin("BattleTracker");
        if (tracker != null){
            ti = Tracker.getInterface(i);
        } else {
            ti = new TrackerOff(this);
            getLogger().warning("BattleTracker turned off or not found.");
        }
    }
    
    

    
}

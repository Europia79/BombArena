package com.github.Europia79.Demolition;

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
    
    public DebugInterface debug;
    // This will contain the carrier for each arena match.
    // <matchID, PlayerName>
    Map<Integer, String> carriers;
    //  <matchID,   <teamID, Base Location>>
    Map<Integer, Map<Integer, Location>> bases;
    public Map<Integer, PlantTimer> pTimers;
    public Map<Integer, DetonateTimer> dTimers;
    /**
     *
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
        // getCommand("demo").setExecutor(new Demo());
        
        BattleArena.registerCompetition(this, "Demolition", "demolition", BombArenaListener.class);
        loadTracker("Demolition");
        
        
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

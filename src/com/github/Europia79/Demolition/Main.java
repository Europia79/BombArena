package com.github.Europia79.Demolition;

import com.github.Europia79.Demolition.debug.*;
import com.github.Europia79.Demolition.tracker.TrackerOff;
import com.github.Europia79.Demolition.util.DetonateTimer;
import com.github.Europia79.Demolition.util.PlantTimer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import mc.alk.arena.BattleArena;
import mc.alk.tracker.Tracker;
import mc.alk.tracker.TrackerInterface;
import mc.alk.tracker.objects.WLT;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author Nikolai
 */
public class Main extends JavaPlugin {
    
    public DebugInterface debug;
    // Integer = match.getID(), String = player.getName()
    // This will contain the carrier for each arena match.
    Map<Integer, String> carriers;
    Map<Integer, Map<String, Location>> pbases;
    Map<Integer, Map<Integer, Location>> bases;
    Map<Integer, Map<Integer, Location>> tbases;
    // The carriers Map will replace plugin.carrier
    public String carrier;
    public Map<Integer, PlantTimer> pTimers;
    public Map<Integer, DetonateTimer> dTimers;
    public PlantTimer ptimer;
    public DetonateTimer dtimer;
    public TrackerInterface ti;
    
    @Override  
    public void onEnable() {
        
        debug = new DebugOn(this);
        carriers = new HashMap<Integer, String>();
        bases = new HashMap<Integer, Map<Integer, Location>>();
        pbases = new HashMap<Integer, Map<String, Location>>();
        tbases = new HashMap<Integer, Map<Integer, Location>>();
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

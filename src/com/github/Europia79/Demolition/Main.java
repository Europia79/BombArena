package com.github.Europia79.Demolition;

import com.github.Europia79.Demolition.debug.*;
import com.github.Europia79.Demolition.util.DetonateTimer;
import com.github.Europia79.Demolition.util.PlantTimer;
import java.util.HashMap;
import java.util.Map;
import mc.alk.arena.BattleArena;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author Nikolai
 */
public class Main extends JavaPlugin {
    
    public DebugInterface debug;
    // Integer = match.getID(), String = player.getName()
    Map<Integer, String> carriers;
    // This will contain the carrier for each arena match in progress.
    public String carrier;
    public Map<Integer, PlantTimer> pTimers;
    public Map<Integer, DetonateTimer> dTimers;
    public PlantTimer ptimer;
    public DetonateTimer dtimer;
    
    @Override  
    public void onEnable() {
        
        debug = new DebugOn();
        carriers = new HashMap<Integer, String>();
        pTimers = new HashMap<Integer, PlantTimer>();
        dTimers = new HashMap<Integer, DetonateTimer>();
        
        // Commands are not yet implemented.
        // getCommand("demo").setExecutor(new Demo());
        
        BattleArena.registerCompetition(this, "Demolition", "demolition", BombArenaListener.class);
        
        
    }
    
    @Override
    public void onDisable() {

    }
    
    

    
}

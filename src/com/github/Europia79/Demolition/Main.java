package com.github.Europia79.Demolition;

import com.github.Europia79.Demolition.debug.*;
import mc.alk.arena.BattleArena;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author Nikolai
 */
public class Main extends JavaPlugin {
    
    public DebugInterface debug;
    public String carrier;
    
    @Override  
    public void onEnable() {
        
        debug = new DebugOn();
        
        // Commands are not yet implemented.
        // getCommand("demo").setExecutor(new Demo());
        
        BattleArena.registerCompetition(this, "Demolition", "demolition", BombTestListener.class);
        
        
    }
    
    @Override
    public void onDisable() {

    }

    
}

package mc.euro.demolition;

import mc.euro.demolition.debug.DebugOn;
import mc.euro.demolition.debug.DebugInterface;
import mc.euro.demolition.commands.Demo;
import mc.euro.demolition.util.DetonateTimer;
import mc.euro.demolition.util.PlantTimer;
import java.util.HashMap;
import java.util.Map;
import mc.alk.arena.BattleArena;
import mc.euro.demolition.debug.DebugOff;
import mc.euro.demolition.tracker.PlayerStats;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Bukkit plugin that adds the Demolition game type to Minecraft servers running BattleArena.
 * @author Nikolai
 */
public class BombPlugin extends JavaPlugin {
    
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
     * Notice that in the database, Ties = Losses.
     */
    public PlayerStats ti;
    // public TrackerInterface ti;
    
    @Override  
    public void onEnable() {
        
        debug = new DebugOff(this);
        carriers = new HashMap<Integer, String>();
        bases = new HashMap<Integer, Map<Integer, Location>>();
        pTimers = new HashMap<Integer, PlantTimer>();
        dTimers = new HashMap<Integer, DetonateTimer>();
          
        // Implemented "/bomb stats" command.
        getCommand("demolition").setExecutor(new Demo());
        
        BattleArena.registerCompetition(this, "Demolition", "demolition", BombArena.class, new Demo());
        ti = new PlayerStats("Demolition");
        getServer().dispatchCommand(Bukkit.getConsoleSender(), "bomb stats");
        
        
    }
    
    @Override
    public void onDisable() {

    }

    
    

    
}

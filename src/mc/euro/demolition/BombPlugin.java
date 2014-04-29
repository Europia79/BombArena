package mc.euro.demolition;

import java.util.ArrayList;
import mc.euro.demolition.debug.DebugOn;
import mc.euro.demolition.debug.DebugInterface;
import mc.euro.demolition.commands.BombExecutor;
import mc.euro.demolition.util.DetonateTimer;
import mc.euro.demolition.util.PlantTimer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import mc.alk.arena.BattleArena;
import mc.alk.arena.util.SerializerUtil;
import mc.euro.demolition.appljuze.ConfigManager;
import mc.euro.demolition.appljuze.CustomConfig;
import mc.euro.demolition.debug.DebugOff;
import mc.euro.demolition.tracker.PlayerStats;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
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
     */
    public DebugInterface debug;
    public Map<Integer, String> carriers; // <matchID, playerName>
    public Map<Integer, Map<Integer, Location>> bases; // <matchID, <teamID, BaseLocation>>
    public Map<String, ArrayList<Location>> allbases; // <ArenaName, Set<BaseLocations>>
    public Map<Integer, PlantTimer> pTimers; // <matchID, new PlantTimer(event, getMatch())>
    public Map<Integer, DetonateTimer> dTimers; // <matchID, new DetonateTimer(event, getMatch())>
    /**
     * Adds Bombs Planted and Bombs Defused to the database. <br/>
     * WLT.WIN = Bomb Planted Successfully (opponents base was destroyed). <br/>
     * WLT.LOSS = Plant Failure caused by enemy defusal of the bomb. <br/>
     * WLT.TIE = Bomb Defused by the player. <br/>
     * Notice that in the database, Ties = Losses.
     */
    public PlayerStats ti;
    /**
     * Configuration variables
     */
    public int PlantTime;
    public int DetonationTime;
    public int DefuseTime;
    public Material BombBlock;
    public Material BaseBlock;
    public String FakeName;
    public String ChangeFakeName;
    public int MaxDamage;
    public int DeltaDamage;
    public int DamageRadius;
    public int StartupDisplay;
    public String DatabaseTable;
    
    public ConfigManager manager;
    public CustomConfig arenasYml;
    
    @Override  
    public void onEnable() {

        debug = new DebugOn(this);
        carriers = new HashMap<Integer, String>();
        bases = new HashMap<Integer, Map<Integer, Location>>();
        allbases = new HashMap<String, ArrayList<Location>>();
        pTimers = new HashMap<Integer, PlantTimer>();
        dTimers = new HashMap<Integer, DetonateTimer>();
        
        // Database Tables: bt_Demolition_*
        ti = new PlayerStats("Demolition");
        
        manager = new ConfigManager(this);
        arenasYml = manager.getNewConfig("arenas.yml");
        
        saveDefaultConfig();
        loadDefaultConfig();
        
        BattleArena.registerCompetition(this, "BombArena", "bomb", BombArena.class, new BombExecutor());
        getServer().dispatchCommand(Bukkit.getConsoleSender(), "bomb stats top " + StartupDisplay);
        
    }
    
    @Override
    public void onDisable() {
        // saveConfig();
    }
    
    private void loadDefaultConfig() {
        
        getLogger().info("Loading config.yml");
        PlantTime = getConfig().getInt("PlantTime");
        DetonationTime = getConfig().getInt("DetonationTime");
        DefuseTime = getConfig().getInt("DefuseTime");
        BombBlock = Material.getMaterial(
                getConfig().getString("BombBlock").toUpperCase());
        BaseBlock = Material.valueOf(
                getConfig().getString("BaseBlock").toUpperCase());
        FakeName = getConfig().getString("FakeName");
        ChangeFakeName = getConfig().getString("ChangeFakeName");
        MaxDamage = getConfig().getInt("MaxDamage");
        DeltaDamage = getConfig().getInt("DeltaDamage");
        DamageRadius = getConfig().getInt("DamageRadius");
        StartupDisplay = getConfig().getInt("StartupDisplay");
        DatabaseTable = getConfig().getString("DatabaseTable");
        
        debug.log("PlantTime = " + PlantTime);
        debug.log("DetonationTime = " + DetonationTime);
        debug.log("DefuseTime = " + DefuseTime);
        debug.log("BombBlock = " + BombBlock.toString());
        debug.log("BaseBlock = " + BaseBlock.toString());
        
        /* Set<String> keys = arenasYml.getConfigurationSection("arenas.bomb1.bases").getKeys(false);
        debug.log("keys.size() = " + keys.size());
        for (String k : keys) {
            debug.log("key." + k + ".value = "
                    + arenasYml.getString("arenas.bomb1.bases." + k));
            
        } */
    }
    
    public ArrayList<Location> getBases(String a) {
        String path = "arenas." + a + ".bases";
        if (arenasYml.getConfigurationSection(path) != null
                && arenasYml.getConfigurationSection(path).getKeys(false) != null
                && arenasYml.getConfigurationSection(path).getKeys(false).size() >= 2) {
            ConfigurationSection cs = arenasYml.getConfigurationSection(path);
            Map<Integer, Location> locs = SerializerUtil.parseLocations(cs);
            if (locs != null) {
                ArrayList<Location> temp = new ArrayList<Location>();
                for (Integer i : locs.keySet()) {
                    // Map<String, ArrayList<Location>> allbases; // <ArenaName, Set<BaseLocations>>
                    temp.add(locs.get(i));
                }
                // this.allbases.put(a, temp);
                return temp;
            }
        }
        getLogger().severe("BombPlugin:getBases(String ArenaName) has failed to return a List of Locations.");
        return null;
    }
    
}

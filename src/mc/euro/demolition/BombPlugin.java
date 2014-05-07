package mc.euro.demolition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import mc.alk.arena.BattleArena;
import mc.alk.arena.controllers.BattleArenaController;
import mc.alk.arena.objects.spawns.SpawnInstance;
import mc.alk.arena.serializers.SpawnSerializer;
import mc.alk.arena.util.SerializerUtil;
import mc.euro.demolition.appljuze.ConfigManager;
import mc.euro.demolition.appljuze.CustomConfig;
import mc.euro.demolition.commands.BombExecutor;
import mc.euro.demolition.debug.*;
import mc.euro.demolition.objects.BaseType;
import mc.euro.demolition.tracker.PlayerStats;
import mc.euro.demolition.util.DefuseCounter;
import mc.euro.demolition.util.DetonateTimer;
import mc.euro.demolition.util.PlantTimer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.InventoryType;
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
    public int Tolerance;
    public Material BombBlock;
    public Material BaseBlock;
    public InventoryType Baseinv;
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
        
        saveDefaultConfig();

        debug = new DebugOn(this);
        carriers = new HashMap<Integer, String>();
        bases = new HashMap<Integer, Map<Integer, Location>>();
        allbases = new HashMap<String, ArrayList<Location>>();
        pTimers = new HashMap<Integer, PlantTimer>();
        dTimers = new HashMap<Integer, DetonateTimer>();
        
        manager = new ConfigManager(this);
        arenasYml = manager.getNewConfig("arenas.yml");

        loadDefaultConfig();
        
        // Database Tables: bt_Demolition_*
        ti = new PlayerStats(this.DatabaseTable);
        
        BattleArena.registerCompetition(this, "BombArena", "bomb", BombArena.class, new BombExecutor());
        getServer().dispatchCommand(Bukkit.getConsoleSender(), "bomb stats top " + StartupDisplay);
        // getServer().dispatchCommand(Bukkit.getConsoleSender(), "bomb update arenas");
        // updateArenasYml(this.BombBlock.name());
        
    }
    
    @Override
    public void onDisable() {
        // saveConfig();
    }
    
    public void loadDefaultConfig() {
        
        getLogger().info("Loading config.yml");
        PlantTime = getConfig().getInt("PlantTime", 8);
        DetonationTime = getConfig().getInt("DetonationTime", 35);
        DefuseTime = getConfig().getInt("DefuseTime", 1);
        Tolerance = getConfig().getInt("Tolerance", 500);
        if (Tolerance < 1) Tolerance = 1;
        BombBlock = Material.getMaterial(
                getConfig().getString("BombBlock", "HARD_CLAY").toUpperCase());
        // setBombSpawn(BombBlock.name());
        BaseBlock = Material.valueOf(
                getConfig().getString("BaseBlock", "BREWING_STAND").toUpperCase());
        try {
            this.Baseinv = BaseType.convert(BaseBlock);
        } catch (IllegalArgumentException ex) {
            getLogger().warning("loadDefaultConfig() has thrown an IllegalArgumentException");
            getLogger().warning("InventoryType has been set to default, BREWING");
            this.Baseinv = InventoryType.BREWING;
        }
        FakeName = getConfig().getString("FakeName", "Bombs Planted Defused");
        ChangeFakeName = getConfig().getString("ChangeFakeName");
        MaxDamage = getConfig().getInt("MaxDamage", 50);
        DeltaDamage = getConfig().getInt("DeltaDamage", 5);
        DamageRadius = getConfig().getInt("DamageRadius", 9);
        StartupDisplay = getConfig().getInt("StartupDisplay", 5);
        DatabaseTable = getConfig().getString("DatabaseTable", "bombarena");
        
        debug.log("PlantTime = " + PlantTime + " seconds");
        debug.log("DetonationTime = " + DetonationTime + " seconds");
        debug.log("DefuseTime = " + DefuseTime + " times");
        debug.log("Tolerance = " + Tolerance + " milliseconds");
        debug.log("BombBlock = " + BombBlock.toString());
        debug.log("BaseBlock = " + BaseBlock.toString());
        debug.log("Baseinv = " + Baseinv.toString());
        
        /* Set<String> keys = arenasYml.getConfigurationSection("arenas.bomb1.bases").getKeys(false);
        debug.log("keys.size() = " + keys.size());
        for (String k : keys) {
            debug.log("key." + k + ".value = "
                    + arenasYml.getString("arenas.bomb1.bases." + k));
            
        } */
    }
    
    public ArrayList<Location> getBases(String a) {
        // PATH = "arenas.{arena}.spawns.{n}.spawn"
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
    
    public Location getBaseLocation(int matchID, int teamID) {
        Location temp = this.bases.get(matchID).get(teamID);
        return this.bases.get(matchID).get(teamID);
    }
    
    public DefuseCounter getCounter(int matchID) {
        return this.dTimers.get(matchID).getCounter();
    }

    public int getPlantTime() {
        return PlantTime;
    }

    public void setPlantTime(int PlantTime) {
        this.PlantTime = PlantTime;
    }

    public int getDetonationTime() {
        return DetonationTime;
    }

    public void setDetonationTime(int DetonationTime) {
        this.DetonationTime = DetonationTime;
    }

    public int getDefuseTime() {
        return DefuseTime;
    }

    public void setDefuseTime(int DefuseTime) {
        this.DefuseTime = DefuseTime;
    }

    public Material getBombBlock() {
        return BombBlock;
    }

    public void setBombBlock(Material type) {
        this.BombBlock = type;                      // IN MEMORY
        this.getConfig().set("BombBlock", type.name()); // update config.yml
        this.saveConfig();                              // save to disk (config.yml)
        // this.updateArenasYml(this.BombBlock.name()); // update arenas.yml
    }
    
    private void updateArenasYml(String x) {
        // PATH = "arenas.arena.basesN.spawn"
        // Map<Long, TimedSpawn> timedSpawns = BattleArena.getBAController().getArena("b2").getTimedSpawns();
        BattleArenaController ac;
        // ac.
        String value = x + " 1";
        String BombBlockString = this.BombBlock.name();
        this.debug.log("updating arenas.yml with " + x);
        ConfigurationSection arenas = this.arenasYml.getConfigurationSection("arenas");
        this.debug.log("" + arenas.getKeys(false).toString());
        for (String arena : arenas.getKeys(false)) {
            this.debug.log("" + arena.toString());
            ConfigurationSection spawns = this.arenasYml.getConfigurationSection("arenas." + arena);
            this.debug.log("" + spawns.getKeys(false).toString());
            for (String n : spawns.getKeys(false)) {
                String path = "arenas." + arena + ".spawns." + n + ".spawn";
                List<String> KEYS = new ArrayList(spawns.getKeys(false));
                List<SpawnInstance> spawnables = SpawnSerializer.parseSpawnable(KEYS);
                this.debug.log("spawnables = " + spawnables.toString());
                for (SpawnInstance i : spawnables) {
                    this.debug.log("string = " + i.toString());
                    this.debug.log("Class = " + i.getClass());
                }
                /* String old = this.arenasYml.getItemStack(path).toString();
                if (!old.startsWith(BombBlockString)) {
                    this.debug.log("BombArena arenas.yml has an item/mob spawn that is NOT a Bomb! " + old);
                    continue;
                } */
                getLogger().info("" + path + " has been changed from to " + value);
                this.arenasYml.set(path, value);
            }
        }
        this.arenasYml.saveConfig();
    }

    public Material getBaseBlock() {
        return BaseBlock;
    }

    public void setBaseBlock(Material type) {
        this.BaseBlock = type;
        this.getConfig().set("BaseBlock", type.name());
        this.saveConfig();
    }

    public InventoryType getBaseinv() {
        return Baseinv;
    }

    public void setBaseinv(InventoryType type) {
        this.Baseinv = type;
    }

    public String getFakeName() {
        return FakeName;
    }

    public void setFakeName(String fakeName) {
        this.FakeName = fakeName;
        this.getConfig().set("FakeName", fakeName);
        this.saveConfig();
    }

    public String getChangeFakeName() {
        return ChangeFakeName;
    }

    public void setChangeFakeName(String fakeName) {
        this.ChangeFakeName = fakeName;
    }

    public int getMaxDamage() {
        return MaxDamage;
    }

    public void setMaxDamage(int max) {
        this.MaxDamage = max;
        this.getConfig().set("MaxDamage", max);
        this.saveConfig();
    }

    public int getDeltaDamage() {
        return DeltaDamage;
    }

    public void setDeltaDamage(int delta) {
        this.DeltaDamage = delta;
        this.getConfig().set("DeltaDamage", delta);
        this.saveConfig();
    }

    public int getDamageRadius() {
        return DamageRadius;
    }

    public void setDamageRadius(int radius) {
        this.DamageRadius = radius;
        this.getConfig().set("DamageRadius", radius);
        this.saveConfig();
    }

    public int getStartupDisplay() {
        return StartupDisplay;
    }

    public void setStartupDisplay(int num) {
        this.StartupDisplay = num;
        this.getConfig().set("StartupDisplay", num);
        this.saveConfig();
    }

    public String getDatabaseTable() {
        return DatabaseTable;
    }

    public void setDatabaseTable(String table) {
        this.DatabaseTable = table;
        this.getConfig().set("DatabaseTable", table);
        this.saveConfig();
    }
    
    
    
}

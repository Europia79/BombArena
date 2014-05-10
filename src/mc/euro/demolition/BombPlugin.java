package mc.euro.demolition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import mc.alk.arena.BattleArena;
import mc.alk.arena.controllers.BattleArenaController;
import mc.alk.arena.objects.arenas.Arena;
import mc.alk.arena.objects.spawns.ItemSpawn;
import mc.alk.arena.objects.spawns.SpawnInstance;
import mc.alk.arena.objects.spawns.TimedSpawn;
import mc.alk.arena.serializers.ArenaSerializer;
import mc.alk.arena.serializers.SpawnSerializer;
import mc.alk.arena.util.SerializerUtil;
import mc.euro.demolition.appljuze.ConfigManager;
import mc.euro.demolition.appljuze.CustomConfig;
import mc.euro.demolition.commands.BombExecutor;
import mc.euro.demolition.debug.*;
import mc.euro.demolition.objects.BaseType;
import mc.euro.demolition.tracker.PlayerStats;
import mc.euro.demolition.util.DefuseTimer;
import mc.euro.demolition.util.DetonationTimer;
import mc.euro.demolition.util.PlantTimer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

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
    public Map<Integer, DetonationTimer> detTimers; // <matchID, new DetonationTimer(event, getMatch())>
    public Map<Integer, Map<String, DefuseTimer>> defTimers; // <matchID, <PlayerName, new DefuseTimer()>>
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
    
    @Override  
    public void onEnable() {
        
        saveDefaultConfig();

        debug = new DebugOn(this);
        carriers = new HashMap<Integer, String>();
        bases = new HashMap<Integer, Map<Integer, Location>>();
        allbases = new HashMap<String, ArrayList<Location>>();
        pTimers = new HashMap<Integer, PlantTimer>();
        detTimers = new HashMap<Integer, DetonationTimer>();
        defTimers = new HashMap<Integer, Map<String, DefuseTimer>>();
        
        

        loadDefaultConfig();
        
        // Database Tables: bt_Demolition_*
        ti = new PlayerStats(this.DatabaseTable);
        
        BattleArena.registerCompetition(this, "BombArena", "bomb", BombArena.class, new BombExecutor());
        getServer().dispatchCommand(Bukkit.getConsoleSender(), "bomb stats top " + StartupDisplay);
        // getServer().dispatchCommand(Bukkit.getConsoleSender(), "bomb update arenas");
        manager = new ConfigManager(this);
        updateArenasYml(this.BombBlock);
        
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
            this.setBaseBlock(Material.BREWING_STAND);
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
        CustomConfig arenasYml = getConfig("arenas.yml");
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
    
    /**
     * Used by assignBases() and setbase command. <br/><br/>
     * 
     * @param loc This is the location of their own base. (NOT the enemy base).
     */
    public Location getExactLocation(Location loc) {
        int length = 10;
        Location base_loc = null;
        this.debug.log("Location loc = " + loc.toString());

        int x1 = loc.getBlockX() - length;
        int y1 = loc.getBlockY() - length;
        int z1 = loc.getBlockZ() - length;

        int x2 = loc.getBlockX() + length;
        int y2 = loc.getBlockY() + length;
        int z2 = loc.getBlockZ() + length;

        World world = loc.getWorld();
        this.debug.log("World world = " + world.getName());

        // Loop over the cube in the x dimension.
        for (int xPoint = x1; xPoint <= x2; xPoint++) {
            // Loop over the cube in the y dimension.
            for (int yPoint = y1; yPoint <= y2; yPoint++) {
                // Loop over the cube in the z dimension.
                for (int zPoint = z1; zPoint <= z2; zPoint++) {
                    // Get the block that we are currently looping over.
                    Block currentBlock = world.getBlockAt(xPoint, yPoint, zPoint);
                    // Set the block to type 57 (Diamond block!)
                    if (currentBlock.getType() == this.BaseBlock) {
                        base_loc = new Location(world, xPoint, yPoint, zPoint);
                        this.debug.log("base_loc = " + base_loc.toString());
                        return base_loc;
                    }
                }
            }
        }
        return base_loc;
    } // END OF getExactLocation()
    
    public DefuseTimer getDefuseTimer(int matchID, String p) {
        return this.defTimers.get(matchID).get(p);
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
        return this.BombBlock;
    }

    public void setBombBlock(Material type) {
        this.BombBlock = type;                      // IN MEMORY
        this.getConfig().set("BombBlock", type.name()); // update config.yml
        this.saveConfig();                              // save to disk (config.yml)
        this.updateArenasYml(this.BombBlock);           // update arenas.yml
    }
    
    private void updateArenasYml(Material x) {
        // PATH = "arenas.{arenaName}.spawns.{index}.spawn"
        this.debug.log("updating arenas.yml with " + x.name());
        BattleArenaController bc = BattleArena.getBAController();
        Map<String, Arena> amap = bc.getArenas();
        for (String key : amap.keySet()) {
            if (amap.get(key).getArenaType().toString().equalsIgnoreCase("BombArena")
                    && amap.get(key).getTimedSpawns().containsKey(1L)) {
                long fs = 1L;
                long rs = amap.get(key).getParams().getMatchTime();
                long ds = amap.get(key).getParams().getMatchTime();
                ItemSpawn item = new ItemSpawn(new ItemStack(this.BombBlock, 1));
                TimedSpawn timedSpawn = new TimedSpawn(fs, rs, ds, item);
                Map<Long, TimedSpawn> temp2 = amap.get(key).getTimedSpawns();
                for (Long index : temp2.keySet()) {
                    temp2.get(index).getSpawn().toString();
                }
                temp2.put(1L, timedSpawn);
                bc.updateArena(amap.get(key));
            }
        }
        ArenaSerializer.saveAllArenas(true);
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
    
    public CustomConfig getConfig(String x) {
        return this.manager.getNewConfig(x);
    }
}

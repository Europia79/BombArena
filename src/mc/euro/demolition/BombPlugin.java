package mc.euro.demolition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import mc.alk.arena.BattleArena;
import mc.alk.arena.controllers.BattleArenaController;
import mc.alk.arena.objects.arenas.Arena;
import mc.alk.arena.objects.spawns.ItemSpawn;
import mc.alk.arena.objects.spawns.SpawnLocation;
import mc.alk.arena.objects.spawns.TimedSpawn;
import mc.alk.arena.serializers.ArenaSerializer;
import mc.alk.arena.util.SerializerUtil;
import mc.euro.demolition.appljuze.ConfigManager;
import mc.euro.demolition.appljuze.CustomConfig;
import mc.euro.demolition.commands.BombExecutor;
import mc.euro.demolition.debug.*;
import mc.euro.demolition.util.BaseType;
import mc.euro.demolition.tracker.PlayerStats;
import mc.euro.demolition.timers.DefuseTimer;
import mc.euro.demolition.timers.DetonationTimer;
import mc.euro.demolition.timers.PlantTimer;
import mc.euro.demolition.util.Version;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Bukkit plugin that adds the Demolition game type to Minecraft servers running BattleArena.
 * 
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
    private int PlantTime;
    private int DetonationTime;
    private int DefuseTime;
    private Material BombBlock;
    private Material BaseBlock;
    private InventoryType Baseinv;
    private int BaseRadius;
    private String FakeName;
    private String ChangeFakeName;
    private int MaxDamage;
    private int DeltaDamage;
    private int DamageRadius;
    private int StartupDisplay;
    private String DatabaseTable;
    
    public ConfigManager manager;
    public CustomConfig basesYml;
    
    
    @Override  
    public void onEnable() {
        
        saveDefaultConfig();

        debug = new DebugOn(this);
        
        Version ba = new Version("BattleArena");
        debug.log("BattleArena version = " + ba.toString());
        debug.log("BattleTracker version = " + Version.getVersion("BattleTracker").toString());
        debug.log("Enjin version = " + Version.getVersion("EnjinMinecraftPlugin").toString());
        // requires 3.9.7.3 or newer
        if (!ba.isCompatible("3.9.7.3")) {
            getLogger().severe("BombArena requires BattleArena v3.9.7.3 or newer.");
            getLogger().info("Disabling BombArena");
            getLogger().info("Please update BattleArena or recompile BombArena "
                    + "to use the old version of SerializerUtil.");
            Bukkit.getPluginManager().disablePlugin(this); 
            return;
        }

        carriers = new HashMap<Integer, String>();
        bases = new HashMap<Integer, Map<Integer, Location>>();
        pTimers = new HashMap<Integer, PlantTimer>();
        detTimers = new HashMap<Integer, DetonationTimer>();
        defTimers = new HashMap<Integer, Map<String, DefuseTimer>>();

        loadDefaultConfig();

        // Database Tables: bt_Demolition_*
        setTracker(this.DatabaseTable);

        // BattleArena.registerCompetition(this, "SndArena", "snd", SndArena.class, new SndExecutor());
        BattleArena.registerCompetition(this, "BombArena", "bomb", BombArena.class, new BombExecutor());
        getServer().dispatchCommand(Bukkit.getConsoleSender(), "bomb stats top " + StartupDisplay);

        manager = new ConfigManager(this);
        basesYml = manager.getNewConfig("bases.yml");

        updateArenasYml(this.BombBlock);
        updateBombArenaConfigYml();

    }

    @Override
    public void onDisable() {
        saveConfig();
    }

    public void loadDefaultConfig() {

        getLogger().info("Loading config.yml");
        PlantTime = getConfig().getInt("PlantTime", 8);
        DetonationTime = getConfig().getInt("DetonationTime", 35);
        DefuseTime = getConfig().getInt("DefuseTime", 1);
        BombBlock = Material.getMaterial(
                getConfig().getString("BombBlock", "TNT").toUpperCase());
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
        debug.log("DefuseTime = " + DefuseTime + " seconds");
        debug.log("BombBlock = " + BombBlock.toString());
        debug.log("BaseBlock = " + BaseBlock.toString());
        debug.log("Baseinv = " + Baseinv.toString());
        
        if (!getConfig().contains("BaseRadius")) {
            getConfig().addDefault("BaseRadius", 3);
        }
        this.BaseRadius = getConfig().getInt("BaseRadius", 3);
        
        boolean b = getConfig().getBoolean("Debug", false);
        if (b) {
            debug = new DebugOn(this);
        } else {
            debug = new DebugOff(this);
        }
    }

    /**
     * Requires BattleArena versions 3.9.7.3 or newer. <br/><br/>
     * <pre>
     * version - behavior 
     * 
     * +397300 - Map<Integer, List<SpawnLocation>> locs = SerializerUtil.parseLocations(cs);
     * -397000 - Map<Integer, Location> locs = SerializerUtil.parseLocations(cs);
     * </pre>
     */
    public List<Location> getBases(String arenaName) {
        // bases.yml
        // PATH = "{arenaName}.{index}"
        String path = arenaName;
        if (basesYml.getConfigurationSection(path) != null
                && basesYml.getConfigurationSection(path).getKeys(false) != null
                && basesYml.getConfigurationSection(path).getKeys(false).size() >= 2) {
            ConfigurationSection cs = basesYml.getConfigurationSection(path);
            /* Requires BattleArena version 3.9.7 or older
            Map<Integer, Location> locs = SerializerUtil.parseLocations(cs);
            List<Location> temp = new ArrayList<Location>();
            for (Location location : locs.values()) {
                debug.log("getBases(String arenaName) location = " + location.toString());
                temp.add(location);
            } */
            Map<Integer, List<SpawnLocation>> locs = SerializerUtil.parseLocations(cs);
            debug.log("getBases() cs = " + cs.toString());
            debug.log("getBases() map = " + locs.toString());
            List<Location> temp = new ArrayList<Location>();
            for (List<SpawnLocation> spawn : locs.values()) {
                debug.log("getBases(String arenaName) location = " + spawn.get(0).getLocation().toString());
                temp.add(spawn.get(0).getLocation());
            }
            debug.log("getBases(String arenaName) size of returning List = " + temp.size());
            return temp;
        }
        getLogger().severe("BombPlugin:getBases(String ArenaName) has failed to return a List of Locations.");
        return null;
    }
    
    public Location getBaseLocation(int matchID, int teamID) {
        return this.bases.get(matchID).get(teamID);
    }
    
    /**
     * Used by assignBases() and setbase command. <br/><br/>
     * 
     * Uses the players location to find the exact location of a nearby BaseBlock. <br/><br/>
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
        return loc;
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
        this.BombBlock = type;                          // IN MEMORY
        this.getConfig().set("BombBlock", type.name()); // update config.yml
        this.saveConfig();                              // save to disk (config.yml)
        this.updateArenasYml(this.BombBlock);           // update arenas.yml
    }
    
    private void updateBombArenaConfigYml() {
        this.debug.log("updating BombArenaConfig.yml");
        /*
        BattleArenaController bc = BattleArena.getBAController();
        for (Arena arena : bc.getArenas().values()) {
            if (arena instanceof BombArena) {
                arena.getParams().setVictoryCondition(VictoryType.fromString("NoTeamsLeft"));
                bc.updateArena(arena);
                getLogger().info("The VictoryCondition for BombArena " + arena.getName() + " has been updated to NoTeamsLeft");
            }
        } */

         CustomConfig bombarena = getConfig("BombArenaConfig.yml");
         bombarena.set("BombArena.victoryCondition", "NoTeamsLeft");
         bombarena.saveConfig();

    }
    private void updateArenasYml(Material x) {
        // PATH = "arenas.{arenaName}.spawns.{index}.spawn"
        /*
arenas:
  arenaName:
    type: BombArena
    spawns:
      '1':
        time: 1 500 500
        spawn: BOMB_BLOCK 1
        loc: world,-429.0,4.0,-1220.0,1.3,3.8
        */
        this.debug.log("updating arenas.yml with " + x.name());
        BattleArenaController bc = BattleArena.getBAController();
        Map<String, Arena> amap = bc.getArenas();
        if (amap.isEmpty()) return;
        for (Arena arena : amap.values()) {
            if (arena.getTimedSpawns() == null) continue;
            if (arena.getArenaType().getName().equalsIgnoreCase("BombArena")
                    && arena.getTimedSpawns().containsKey(1L)) {
                Map<Long, TimedSpawn> tmap = arena.getTimedSpawns();
                Location loc = tmap.get(1L).getSpawn().getLocation();
                
                long fs = 1L;
                long rs = arena.getParams().getMatchTime();
                long ds = rs;
                ItemSpawn item = new ItemSpawn (new ItemStack(this.BombBlock, 1));
                item.setLocation(loc);
                TimedSpawn timedSpawn = new TimedSpawn(fs, rs, ds, item);
                tmap.put(1L, timedSpawn);
                bc.updateArena(arena);
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
        this.setTracker(table);
    }
    
    public void setTracker(String x) {
        ti = new PlayerStats(x);
    }
    
    public PlayerStats getTracker() {
        return ti;
    }
    
    public CustomConfig getConfig(String x) {
        return this.manager.getNewConfig(x);
    }

    double getBaseRadius() {
        return this.BaseRadius;
    }
}

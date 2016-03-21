package mc.euro.demolition;

import java.io.File;
import java.util.Collection;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import mc.alk.arena.BattleArena;
import mc.alk.arena.controllers.BattleArenaController;
import mc.alk.arena.objects.arenas.Arena;
import mc.alk.arena.objects.spawns.ItemSpawn;
import mc.alk.arena.objects.spawns.TimedSpawn;
import mc.alk.arena.objects.victoryconditions.NoTeamsLeft;
import mc.alk.arena.objects.victoryconditions.VictoryType;
import mc.alk.arena.serializers.ArenaSerializer;
import mc.euro.demolition.appljuze.ConfigManager;
import mc.euro.demolition.appljuze.CustomConfig;
import mc.euro.demolition.arenas.BombArena;
import mc.euro.demolition.arenas.EodArena;
import mc.euro.demolition.arenas.SndArena;
import mc.euro.demolition.arenas.factories.BombArenaFactory;
import mc.euro.demolition.arenas.factories.SndArenaFactory;
import mc.euro.demolition.commands.EodExecutor;
import mc.euro.demolition.debug.*;
import mc.euro.demolition.holograms.HologramInterface;
import mc.euro.demolition.holograms.HologramsOff;
import mc.euro.demolition.holograms.HolographicAPI;
import mc.euro.demolition.holograms.HolographicDisplay;
import mc.euro.demolition.tracker.PlayerStats;
import mc.euro.demolition.util.BaseType;
import mc.euro.version.Version;
import mc.euro.version.VersionFactory;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Bukkit plugin that adds the Demolition game types: Sabotage & Search N Destroy. <br/><br/>
 * 
 * @author Nikolai
 */
public class BombPlugin extends JavaPlugin {
    
    private static final Logger log = Logger.getLogger(BombPlugin.class.getCanonicalName());
    
    /**
     * Adds Bombs Planted and Bombs Defused to the database. <br/>
     * WLT.WIN = Bomb Planted Successfully (opponents base was destroyed). <br/>
     * WLT.LOSS = Plant Failure caused by enemy defusal of the bomb. <br/>
     * WLT.TIE = Bomb Defused by the player. <br/>
     * Notice that in the database, Ties = Losses.
     */
    public PlayerStats ti;
    public ConfigManager manager;
    public CustomConfig basesYml;
    
    /**
     * debug = new DebugOn(); <br/>
     * debug = new DebugOff(); <br/>
     * debug.log("x = " + x); <br/>
     * debug.messagePlayer(p, "debug msg"); <br/>
     * debug.msgArenaPlayers(match.getPlayers(), "info"); <br/><br/>
     * 
     */
    public DebugInterface debug;
    private HologramInterface holograms; // HolographicDisplays + HoloAPI
    /**
     * Configuration variables:.
     */
    private int PlantTime;
    private int DetonationTime;
    private int DefuseTime;
    private Sound TimerSound;
    private Material BombBlock;
    private Material BaseBlock;
    private InventoryType Baseinv;
    private int BaseRadius;
    private String ChangeFakeName;
    private int MaxDamage;
    private int DeltaDamage;
    private int DamageRadius;
    private int StartupDisplay;
    private String DatabaseTable;
    
    /**
     * Hinderances to backwards compatibility:. <br/><br/>
     * 
     * <b>BA.Version - Class.method() - return type </b>
     * <pre>
     * 3.9.6+    Spawnable.spawn() returns void
     * 3.9.5.8-  Spawnable.spawn() returns int
     * 
     * https://github.com/BattlePluginsDev/BattleArena/commit/535c0e8aa443dbbd01dd89aa9321b750784fff75
     * </pre>
     */
    @Override  
    public void onEnable() {
        
        saveDefaultConfig();
        
        debug = new DebugOn(this);
        loadDefaultConfig();
        
        Version<Plugin> ba = VersionFactory.getPluginVersion("BattleArena");
        debug.log("BattleArena version = " + ba.toString());
        debug.log("BattleTracker version = " + VersionFactory.getPluginVersion("BattleTracker").toString());
        debug.log("Enjin version = " + VersionFactory.getPluginVersion("EnjinMinecraftPlugin").toString());
        // requires BattleArena v3.9.6+ because of a change in Spawnable.spawn()
        if (!ba.isCompatible("3.9.6")) {
            getLogger().severe("BombArena requires BattleArena v3.9.6 or newer.");
            getLogger().info("Disabling BombArena");
            getLogger().info("Please install BattleArena.");
            getLogger().info("http://dev.bukkit.org/bukkit-plugins/battlearena2/");
            Bukkit.getPluginManager().disablePlugin(this); 
            return;
        }
        
        // Database Tables: bt_Demolition_*
        setTracker(this.DatabaseTable);
        
        if (ba.isCompatible("3.9.8")) {
            SndArenaFactory.registerCompetition(this, "SndArena", "snd", SndArena.class, new EodExecutor(this));
            BombArenaFactory.registerCompetition(this, "BombArena", "bomb", BombArena.class, new EodExecutor(this));
        } else {
            BattleArena.registerCompetition(this, "SndArena", "snd", SndArena.class, new EodExecutor(this));
            BattleArena.registerCompetition(this, "BombArena", "bomb", BombArena.class, new EodExecutor(this));
        }

        if (StartupDisplay > 0) {
            getServer().dispatchCommand(Bukkit.getConsoleSender(), "bomb stats top " + StartupDisplay);
        }

        manager = new ConfigManager(this);
        basesYml = manager.getNewConfig("bases.yml");
        
        updateArenasYml(this.BombBlock);
        updateBombArenaConfigYml();
        updateBasesYml();
        
        saveAllArenas();
        
        getLogger().log(Level.INFO, " has been enabled");
    }
    
    public void loadDefaultConfig() {
        
        boolean b = getConfig().getBoolean("Debug", false);
        if (b) {
            debug = new DebugOn(this);
            getLogger().info("Debugging mode is ON");
        } else {
            debug = new DebugOff(this);
            getLogger().info("Debugging mode is OFF.");
        }

        getLogger().info("Loading config.yml");
        PlantTime = getConfig().getInt("PlantTime", 8);
        DetonationTime = getConfig().getInt("DetonationTime", 35);
        DefuseTime = getConfig().getInt("DefuseTime", 1);
        String s = getConfig().getString("TimerSound", "ENTITY_EXPERIENCE_ORB_PICKUP");
        try {
            TimerSound = Sound.valueOf(s.toUpperCase());
        } catch (IllegalArgumentException ex) {
            try {
                this.TimerSound = Sound.valueOf("ORB_PICKUP");
            } catch (IllegalArgumentException ignored) {
                
            }
        }
        if (TimerSound == null) {
            TimerSound = Sound.values()[0]; // make it non-null
        }
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
        
        if (!getConfig().contains("ShowHolograms")) {
            getConfig().addDefault("ShowHolograms", true);
        }
        boolean ShowHolograms = getConfig().getBoolean("ShowHolograms", true);
        Version HD = VersionFactory.getPluginVersion("HolographicDisplays");
        Version Holoapi = VersionFactory.getPluginVersion("HoloAPI");
        debug.log("HolographicDisplays version = " + HD.toString());
        debug.log("HoloAPI version = " + Holoapi.toString());
        if (ShowHolograms && HD.isCompatible("1.8.5")) {
            this.holograms = new HolographicDisplay(this);
            debug.log("HolographicDisplays support is enabled.");
        } else if (ShowHolograms && Holoapi.isEnabled()) {
            this.holograms = new HolographicAPI(this);
            debug.log("HoloAPI support is enabled.");
        } else {
            this.holograms = new HologramsOff();
            debug.log("Hologram support is disabled.");
            debug.log("Please download HoloAPI or HolographicDisplays to enable Hologram support.");
        }
    }
    
    /**
     * Used to find a nearby BaseBlock. <br/><br/>
     * 
     * Used by assignBases() and setbase() command. <br/><br/>
     * 
     * @param loc Scan blocks near this location.
     * @return Does not return null: If no block is found, the original loc param is returned.
     */
    public Location getExactLocation(Location loc) {
        int length = 5;
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
    
    public HologramInterface holograms() {
        return this.holograms;
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
        this.BombBlock = type;
    }

    public Material getBaseBlock() {
        return BaseBlock;
    }

    public void setBaseBlock(Material type) {
        this.BaseBlock = type;
    }

    public InventoryType getBaseinv() {
        return Baseinv;
    }

    public void setBaseinv(InventoryType type) {
        this.Baseinv = type;
    }

    public String getFakeName() {
        return "Bombs Planted Defused";
    }

    public int getMaxDamage() {
        return MaxDamage;
    }

    public void setMaxDamage(int max) {
        this.MaxDamage = max;
    }

    public int getDeltaDamage() {
        return DeltaDamage;
    }

    public void setDeltaDamage(int delta) {
        this.DeltaDamage = delta;
    }

    public int getDamageRadius() {
        return DamageRadius;
    }

    public void setDamageRadius(int radius) {
        this.DamageRadius = radius;
    }

    public int getStartupDisplay() {
        return StartupDisplay;
    }

    public void setStartupDisplay(int num) {
        this.StartupDisplay = num;
    }

    public String getDatabaseTable() {
        return DatabaseTable;
    }

    public void setDatabaseTable(String table) {
        this.DatabaseTable = table;
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

    public double getBaseRadius() {
        return this.BaseRadius;
    }
    
    public Sound getSound() {
        return TimerSound;
    }
    
    /**
     * ****************************************************************************
     * onDisable(): cancelTimers(), updateConfig().saveConfig(), updateArenasYml().
     * ****************************************************************************
     */
    @Override
    public void onDisable() {
        cancelAndClearTimers();
        updateConfig().saveConfig();
        updateArenasYml(this.BombBlock);
        BattleArena.saveArenas(this);
    }

    private void cancelAndClearTimers() {
        BattleArenaController bc = BattleArena.getBAController();
        Map<String, Arena> amap = bc.getArenas();
        for (Arena arena : amap.values()) {
            if (arena == null) continue;
            boolean isEodArena = (arena instanceof EodArena);
            if (isEodArena) {
                EodArena eodArena = (EodArena) arena;
                eodArena.cancelAndClearTimers();
            }
        }
    }
    /**
     * Only updates non-Integer fields.
     * All Integer fields are updated by EodExecutor.
     * @return an instance of BombPlugin: this.
     */
    private BombPlugin updateConfig() {
        getConfig().set("BaseBlock", this.BaseBlock.name());
        getConfig().set("BombBlock", this.BombBlock.name());
        getConfig().set("DatabaseTable", this.DatabaseTable);
        return this;
    }
    
    private void saveAllArenas() {
        if (debug instanceof DebugOn) {
            ArenaSerializer.saveAllArenas(true); // Verbose
        } else {
            ArenaSerializer.saveAllArenas(false); // Silent
        }
    }
    
    /**
     * Use-case scenario:. <br/><br/>
     * 
     * Admin creates arenas: They get created with the current bomb block. <br/>
     * Admin then changes the bomb block inside config.yml. <br/>
     * Then all the previously created arenas in arenas.yml will need to be updated. <br/>
     * 
     * <b>arenas.yml:</b> PATH = "arenas.{arenaName}.spawns.{index}.spawn" <br/><br/>
     * <pre>
     * arenas:
     *   arenaName:
     *     type: BombArena
     *     spawns:
     *       '1':
     *         time: 1 500 500
     *         spawn: BOMB_BLOCK 1
     *         loc: world,-429.0,4.0,-1220.0,1.3,3.8
     * </pre>
     * 
     * @param x The new bomb block material type.
     */
    private void updateArenasYml(Material x) {
        this.debug.log("updating arenas.yml with " + x.name());
        BattleArenaController bc = BattleArena.getBAController();
        Map<String, Arena> amap = bc.getArenas();
        if (amap.isEmpty()) return;
        for (Arena arena : amap.values()) {
            if (arena.getTimedSpawns() == null) continue;
            if ((arena.getArenaType().getName().equalsIgnoreCase("BombArena") 
                    || arena.getArenaType().getName().equalsIgnoreCase("SndArena")) 
                    && arena.getTimedSpawns().containsKey(1L)) {
                Map<Long, TimedSpawn> tmap = arena.getTimedSpawns();
                TimedSpawn bomb = tmap.get(1L);
                
                long fs = 1L;
                long rs = bomb.getRespawnTime();
                long ds = bomb.getTimeToDespawn();
                ItemSpawn item = new ItemSpawn(new ItemStack(this.BombBlock, 1));
                item.setLocation(bomb.getSpawn().getLocation());
                TimedSpawn timedSpawn = new TimedSpawn(fs, rs, ds, item);
                tmap.put(1L, timedSpawn);
                bc.updateArena(arena);
            }
        }
        // ArenaSerializer.saveAllArenas(true); // moved to onEnabe()
    }
    
    /**
     * Updates BombArenaConfig.yml
     * Sets node "BombArena.victoryCondition" to "NoTeamsLeft"
     */
    private void updateBombArenaConfigYml() {
        this.debug.log("updating BombArenaConfig.yml");
        // This needs to be tested.
        BattleArenaController bc = BattleArena.getBAController();
        Map<String, Arena> amap = bc.getArenas();
        for (Arena arena : amap.values()) {
            if ((arena.getArenaType().getName().equalsIgnoreCase("BombArena")
                    || arena.getArenaType().getName().equalsIgnoreCase("SndArena"))) {
                String name = arena.getParams().getVictoryType().getName();
                if (!name.equals("NoTeamsLeft")) {
                    VictoryType type = VictoryType.getType(NoTeamsLeft.class);
                    arena.getParams().setVictoryCondition(type);
                    bc.updateArena(arena);
                    debug.log("The VictoryCondition for BombArena " + arena.getName() + " has been updated to NoTeamsLeft");
                }
            }
        }
        // ArenaSerializer.saveAllArenas(boolean verbose); // moved to onEnable()
    }
    
    /**
     * Move information from bases.yml to arenas.yml then deletes bases.yml.
     * 
     * We need to delete the file because arenas.yml can change...
     * But if bases.yml is not deleted, then changes in arenas.yml will be reverted 
     * on server restart.
     */
    private boolean updateBasesYml() {
        debug.log("Transferring bases.yml to arenas.yml");
        File file = new File(getDataFolder(), "bases.yml");
        if (!file.exists()) {
            debug.log("Transfer aborted: bases.yml does NOT exist.");
            debug.log("File = " + file.toString());
            return false;
        }
        BattleArenaController bc = BattleArena.getBAController();
        Map<String, Arena> amap = bc.getArenas();
        if (amap.isEmpty()) {
            debug.log("Transfer aborted: No arenas found.");
            return false;
        }
        for (Arena arena : amap.values()) {
            String name = arena.getName();
            if (!basesYml.contains(name)) {
                debug.log("basesYml does NOT contain: " + name);
                continue;
            }
            String type = arena.getArenaType().getName();
            String msg = "" + type + " " + name;
            boolean isBombArena = type.equalsIgnoreCase("BombArena") && (arena instanceof BombArena);
            boolean isSndArena = type.equalsIgnoreCase("SndArena") && (arena instanceof SndArena);
            if (isBombArena) {
                debug.log(msg + " is of type BombArena");
                BombArena bomb = (BombArena) arena;
                if (!bomb.getCopyOfSavedBases().isEmpty()) {
                    debug.log("skipping " + name + " because it already has persistable data for savedBases.");
                    continue;
                }
                Map<Integer, Location> locations = bomb.getOldBases(name);
                for (Integer index : locations.keySet()) {
                    Location loc = locations.get(index);
                    bomb.addSavedBase(loc);
                }
            } else if (isSndArena) {
                debug.log(msg + " is of type SndArena");
                SndArena snd = (SndArena) arena;
                if (!snd.getCopyOfSavedBases().isEmpty()) {
                    debug.log("skipping " + name + " because it already has persistable data for savedBases.");
                    continue;
                }
                Collection<Location> locations = snd.getOldBases(name);
                for (Location loc : locations) {
                    snd.addSavedBase(loc);
                }
            }
            // BattleArena.saveArenas(this); // moved to onEnable()
        }
        // Delete the bases.yml so that it won't revert changes in arenas.yml
        file.delete();
        return true;
    }
}

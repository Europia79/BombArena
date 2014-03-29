package com.github.Europia79.Demolition;

import com.github.Europia79.Demolition.objects.Bomb;
import com.github.Europia79.Demolition.util.DetonateTimer;
import com.github.Europia79.Demolition.util.PlantTimer;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.bukkit.WGBukkit;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import mc.alk.arena.events.players.ArenaPlayerLeaveEvent;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.arenas.Arena;
import mc.alk.arena.objects.events.ArenaEventHandler;
import mc.alk.arena.objects.teams.ArenaTeam;
import mc.alk.tracker.objects.WLT;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;

/**
 * This class listens for all the bomb events and acts accordingly.
 * 
 * @author Nikolai
 * 
 * <pre>
 * Bomb = Hardened Clay 172
 *
 * Listen for 
 * onBombPickup() - set HAT & compass. 
 * onBombCarrierLeave() - if they log out or leave the arena.
 * onBombCarrierDeath() - drop it on the ground. 
 * onBombDrop() - is it outside the map ? 
 * onBombDespawn() - THIS BREAKS ALL OTHER EVENTS. 
 * onBombPlace() - trigger onBombPlant() if close enough. 
 * onBombPlant() - takes 7 sec to plant + 30 sec to blow up. 
 * onPlantFailure() - Self cancelled or caused by death ? 
 * onBombDefuse() - takes 7 sec, declare winners. 
 * 
 * </pre>
 * 
 * multiple listeners are currently not possible
 * because addArenaListener() is not working: 
 * from constructor, init(), or onBegin().
 *
 */
public class BombArenaListener extends Arena {

    Main plugin;

    /**
     * Constructor: gets a reference to Main.java and stores it in the plugin field.
     */
    public BombArenaListener() {
        plugin = (Main) Bukkit.getPluginManager().getPlugin("Demolition");
    }

    /**
     * This method sets plugin.carriers, compass direction, and gives a hat
     * 
     * <pre>
     * 1. Give the bomb carrier a hat so that other players know WHO has the bomb.
     * 2. Set the compass direction for all players to point to the objective:
     *    - This helps attackers find the opponents base.
     *    - This helps defenders find their own base.
     * </pre>
     * @param e PlayerPickupItemEvent: checks to see if they picked up the bomb item, or a different item.
     */
    @ArenaEventHandler
    public void onBombPickup(PlayerPickupItemEvent e) {
        int matchID = getMatch().getID();
        String c = (plugin.carriers.get(matchID) == null) ? null : plugin.carriers.get(matchID);
        e.getPlayer().sendMessage("onBombPickup() Listener works!");
        plugin.debug.messagePlayer(e.getPlayer(), "debug works!");

        // To-Do: sudo player hat bomb
        if (e.getItem().getItemStack().getType() == Material.HARD_CLAY) {
            if (c == null) {
                c = e.getPlayer().getName();
                plugin.carriers.put(matchID, c);
                e.getPlayer().getInventory().setHelmet(new ItemStack(Material.HARD_CLAY));
                ArenaTeam team2 = null;
                try {
                    team2 = getOtherTeam(e.getPlayer());
                } catch (NullPointerException ex) {
                    plugin.getLogger().severe("Stopping match because getOtherTeam() method failed");
                    getMatch().cancelMatch();
                }
                setCompass(getMatch().getPlayers(), team2);
            } else {
                e.setCancelled(true);
                plugin.debug.messagePlayer(e.getPlayer(), 
                        "There can only be ONE bomb per Match. "
                        + c + " currently has the bomb.");
                e.getItem().remove();
            }


        }
    }
    
    /**
     * This method handles the scenario where a player gets the bomb then logs out or leaves the arena.
     * 
     * @param e ArenaPlayerLeaveEvent
     */
    @ArenaEventHandler
    public void onBombCarrierLeave(ArenaPlayerLeaveEvent e) {
        int id = getMatch().getID();
        String c = (plugin.carriers.get(id) == null) ? null : plugin.carriers.get(id);
        
    }
    
    /**
     * This method triggers a new bombDropEvent if a player dies with the bomb.
     * 
     * <pre>
     * It also lets all the players know the location of the bomb.
     *   - But only if it's on the ground, never if a player has it.
     * </pre>
     * 
     * @param e PlayerDeathEvent - Do they have the bomb ?
     */
    @ArenaEventHandler
    public void onBombCarrierDeath(PlayerDeathEvent e) {
        int id = getMatch().getID();
        String c = (plugin.carriers.get(id) == null) ? null : plugin.carriers.get(id);
        Player p = e.getEntity().getPlayer();
        // drop the bomb on the ground
        if (c == null) {
            return;
        }
        if (p.getName().equals(c)) {
            e.setDeathMessage("" + e.getEntity().getPlayer().getName()
                    + " has died and dropped the bomb at "
                    + " " + (int) e.getEntity().getPlayer().getLocation().getX()
                    + " " + (int) e.getEntity().getPlayer().getLocation().getY()
                    + " " + (int) e.getEntity().getPlayer().getLocation().getZ());
            e.getDrops().clear();
            // (Item) new ItemStack(Material.HARD_CLAY causes ClassCastException
            Item bomb = new Bomb(e);
            bomb.setPickupDelay(40);
            PlayerDropItemEvent bombDropEvent = new PlayerDropItemEvent(e.getEntity().getPlayer(), bomb);
            Bukkit.getServer().getPluginManager().callEvent(bombDropEvent);
            if (bombDropEvent.isCancelled()) {
                plugin.getLogger().warning("Something has attempted to cancel the bombDropEvent. "
                        + "Is this intended ? Or is it a bug ?");
            }
            bombDropEvent.getPlayer().getWorld().dropItem(
                        bombDropEvent.getPlayer().getLocation(),
                        bombDropEvent.getItemDrop().getItemStack());
            
        }
        
    }
    
    /**
     * This event handles the scenario when the bomb is thrown on the ground.
     * 
     * <pre>
     * 1. Point the compass to the direction of the bomb.
     * 2. Give a visual aid for the bomb location (not implemented yet).
     * 3. Make sure the bomb didn't get thrown outside the map (not implemented).
     * </pre>
     * 
     * @param e PlayerDropItemEvent - Did they drop the bomb item ? Or another item ?
     */
    @ArenaEventHandler
    public void onBombDrop(PlayerDropItemEvent e) {
        int matchID = getMatch().getID();
        String c = (plugin.carriers.get(matchID) == null) ? null : plugin.carriers.get(matchID);
        Material type = e.getItemDrop().getItemStack().getType();
        // To-do: make sure the bomb didn't get thrown outside the map
        Location loc = e.getItemDrop().getLocation();
        if (type == Material.HARD_CLAY) {
            if (c != null 
                    && e.getPlayer().getName().equals(c)) {
                if (e.getPlayer().getInventory().getHelmet() == new ItemStack(Material.HARD_CLAY)) {
                    e.getPlayer().getInventory().setHelmet(new ItemStack(Material.AIR));
                    
                }
                // sets the carrier to null
                plugin.carriers.remove(matchID);
                // get all arena players inside this Match. 
                // set their compass direction.
                Set<ArenaPlayer> allplayers = getMatch().getPlayers();
                for (ArenaPlayer p : allplayers) {
                    p.getPlayer().setCompassTarget(loc);
                    p.sendMessage("The bomb has been dropped! Follow your compass.");
                }
            } else {
                plugin.getLogger().warning(""
                        + e.getPlayer().getName()
                        + "has tried to drop the bomb without ever picking it up. "
                        + "Are they cheating / exploiting ? Or is this a bug ? "
                        + "Please investigate this incident and if it's a bug, then "
                        + "notify Europia79@hotmail.com OR on the Bukkit forums.");
            }

        }
    }
    
    /**
     * This event breaks ALL other events.
     * 
     * <pre>
     * respawn a new bomb OR cancel the despawn event.
     * </pre>
     * @param e ItemDespawnEvent - Was it the bomb ? Or another item ?
     */
    /*@ArenaEventHandler
    public void onBombDespawn(ItemDespawnEvent e) {
        int id = getMatch().getID();
        String c = (plugin.carriers.get(id) == null) ? null : plugin.carriers.get(id);
        // temporary place holder
        // until I get time to implement this method.
        // respawn a new bomb OR cancel the event
        if (e.getEntity().getItemStack().getType() == Material.HARD_CLAY 
                && c != null) {
            Set<ArenaPlayer> allplayers = getMatch().getPlayers();
            for (ArenaPlayer p : allplayers) {
                plugin.debug.messagePlayer(p.getPlayer(), "Bomb despawned cancelled. ");
            }
            e.setCancelled(true);
        } else {
            plugin.debug.msgArenaPlayers(getMatch().getPlayers(), 
                    "Bomb despawn allowed because " + c + " has the bomb.");
        }

    }*/
    
    /**
     * Handles the scenario when players attempt to place the bomb on the ground like it's a block. <br/><br/>
     * 
     * This method is going to help out new players by checking the distance to the base:<br/>
     * 
     *   - if the distance is small, then trigger onBombPlant(InventoryOpenEvent e). <br/>
     *   - if they're too far away, then give the player helpful hints
     *     about the distance and compass direction to the enemy base. <br/>
     * 
     * @param e BlockPlaceEvent - Is it the bomb block ?
     */
    @ArenaEventHandler
    public void onBombPlace(BlockPlaceEvent e) {
        // Get the coordinates to the base
        // calculate the distance to the base
        // if the distance is small, attempt to trigger onBombPlant()
        if (e.getBlockPlaced().getType() == Material.HARD_CLAY) {
            e.getPlayer().sendMessage("Improper bomb activation!");
            
            // get the other team's base location
            // set the player compass
            // and msg him to follow the compass
            // e.getPlayer().getInventory().addItem(new ItemStack(Material.HARD_CLAY));
            e.setCancelled(true);
            // updateInventory() is deprecated
            // Must eventually find another solution.
            e.getPlayer().updateInventory();
        }
    }
    
    /**
     * This event handles players who access Brewing Stands. <br/>
     * 
     * Main if-statement checks: <br/>
     * - Did they open a Brewing Stand Inventory ? <br/>
     * - Does the player have the bomb ? <br/>
     * - Are they at the enemy base ? <br/><br/>
     * 
     * If so, start the Plant Timer: It takes about 5 to 10 seconds to plant the bomb. <br/><br/>
     * 
     * If not, cancel the InventoryOpenEvent. <br/>
     * 
     * @param e InventoryOpenEvent - Is it a Brewing Stand Inventory ? (Each base must have a brewing stand).
     */
    @ArenaEventHandler
    public void onBombPlant(InventoryOpenEvent e) {
        plugin.debug.log("onBombPlant() has been called.");
        int matchID = getMatch().getID();
        plugin.debug.log("matchID = " + matchID);
        String c = (plugin.carriers.get(matchID) == null) ? null : plugin.carriers.get(matchID);
        plugin.debug.log("plugin.carriers.get(matchID) = " + plugin.carriers.get(matchID));
        Player planter = (Player) e.getPlayer();
        plugin.debug.log("planter = " + planter.getName());
        int teamID = getTeam(planter).getId();
        plugin.debug.log("teamID = " + teamID);
        // ARE THEY AT THE CORRECT BASE ?
        // Use the Match ID to get all the player bases, then
        // get the bomb carriers base:
        // Compare his current position with the position of HIS OWN BASE
        // to make sure he's not trying to plant the bomb at his own base.
        plugin.debug.messagePlayer(planter, "onBombPlant() has been called");
        plugin.debug.log("e.getInventory().getType() = " + e.getInventory().getType());
        plugin.debug.log("carrier, c = " + c);
        plugin.debug.log("e.getPlayer().getName() = " + e.getPlayer().getName());
        plugin.debug.log("planter.getLocation = " + planter.getLocation());
        plugin.debug.log("plugin.bases.get(matchID) = " + plugin.bases.get(matchID).toString());
        plugin.debug.log("plugin.bases.get(matchID).get(teamID) = " + plugin.bases.get(matchID).get(teamID).toString());
        plugin.debug.log("e.getPlayer().getInventory().getHelmet = " + e.getPlayer().getInventory().getHelmet());
        if (e.getInventory().getType() == InventoryType.BREWING 
                && c != null 
                && e.getPlayer().getName().equalsIgnoreCase(c) 
                && plugin.bases.get(matchID).get(teamID).distance(planter.getLocation()) > 30) {
            // converted a single Timer to one for each match.
            if (e.getPlayer().getInventory().getHelmet() == new ItemStack(Material.HARD_CLAY)) {
                e.getPlayer().getInventory().setHelmet(new ItemStack(Material.AIR));
            }
            plugin.pTimers.put(getMatch().getID(), new PlantTimer(e, getMatch()));
            plugin.pTimers.get(getMatch().getID()).runTaskTimer(plugin, 0L, 20L);
        } else if (e.getInventory().getType() == InventoryType.BREWING){
            plugin.debug.messagePlayer(planter, "event.setCancelled(true);");
            e.setCancelled(true);
        }
    }
    
    /**
     * Handles the event where the Bomb Carrier does NOT complete the time required to plant the bomb. <br/>
     * 
     * Notice that are multiple ways to trigger this event: <br/>
     * - The bomb carrier prematurely closes the Brewing Stand 
     *   thereby canceling the plant process. <br/>
     * - The bomb carrier dies. <br/>
     * - Also, when the PlantTimer is finished, it will close the player inventory.
     *   So we need to be careful that it doesn't also cancel the 30 seconds that
     *   it takes to destroy the base. <br/><br/>
     * 
     * @param e InventoryCloseEvent
     */
    @ArenaEventHandler
    public void onBombPlantFailure(InventoryCloseEvent e) {
        int matchID = getMatch().getID();
        String c = (plugin.carriers.get(matchID) == null) ? null : plugin.carriers.get(matchID);
        // Is it a brewing stand ?
        // Are they trying to plant ?
        if (e.getPlayer().getInventory().getType() == InventoryType.BREWING 
                && c != null 
                && e.getPlayer().getName().equalsIgnoreCase(c)) {
            // if this is an actual death or drop then those Events 
            // will handle setting the carrier to null
            plugin.debug.msgArenaPlayers(getMatch().getPlayers(), 
                    "onBombPlantFailure has been called " 
                    + "due to InventoryCloseEvent");
            plugin.pTimers.get(matchID).cancel();
        }

    }
    
    /**
     * This method handles the event when players try to defuse the bomb (by breaking the bomb block). <br/><br/>
     * 
     * Make sure that player (or his teammates) who just planted the bomb
     * cannot turn around and break it: <br/><br/>
     * Such a scenario would produce 2 possible scenarios: <br/>
     * 1. The plant/defuse for the win exploit. <br/>
     * 2. The plant/break exploit (prevents the other team from defusing). <br/><br/>
     * 
     * This version currently contains bug #2.
     * 
     * @param e BlockBreakEvent - Is it the bomb block ?
     */
    @ArenaEventHandler
    public void onBombDefusal(BlockBreakEvent e) {
        int matchID = getMatch().getID();
        String c = (plugin.carriers.get(matchID) == null) ? null : plugin.carriers.get(matchID);
        // Cancel timers and declare the winners
        if (e.getBlock().getType() == Material.HARD_CLAY 
                && getTeam(e.getPlayer()) != getTeam(Bukkit.getServer().getPlayer(c))) {
            Set<ArenaPlayer> allplayers = getMatch().getPlayers();
            for (ArenaPlayer p : allplayers) {
                p.sendMessage("" + e.getPlayer().getName() 
                        + " has defused the bomb.");
                if (getTeam(p) == getTeam(e.getPlayer())) {
                    p.sendMessage("You win!");
                } else {
                    p.sendMessage("You lost!");
                }
            }
            plugin.ti.addPlayerRecord(e.getPlayer().getName(), "Bombs Planted Defused", WLT.TIE);
            plugin.ti.addPlayerRecord(c, "Bombs Planted Defused", WLT.LOSS);
            ArenaTeam t = getTeam(e.getPlayer());
            getMatch().setVictor(t);
            plugin.pTimers.get(matchID).cancel();
        } else if (e.getBlock().getType() == Material.HARD_CLAY 
                && getTeam(e.getPlayer()) == getTeam(Bukkit.getServer().getPlayer(c))) {
            e.getPlayer().sendMessage("If you defuse the bomb, then the other team will win.");
            e.setCancelled(true);
        }
        
    }
    
    /**
     * First method called by BattleArena. <br/><br/>
     * 
     * Do NOT handle anything specific to matches here. <br/><br/>
     * 
     * BattleArena method order (during matches): <br/>
     * 1. onStart() <br/>
     * 2. onComplete() <br/>
     * 3. onFinish() <br/>
     * 
     */
    @Override
    public void onBegin() {
        super.onBegin(); 
        plugin.getLogger().info("onBegin() has been called by Demolition plugin: BombArenaListener.java");
    }
    
    /**
     * onStart() is called at the start of a Match. <br/><br/>
     * 
     * Order: onStart(), onComplete(), onFinish() 
     */
    @Override
    public void onStart() {
        super.onStart();
        plugin.debug.msgArenaPlayers(getMatch().getPlayers(), "onStart");
        // temporary:
        plugin.carriers.clear();
        // World w = getWorldGuardRegion().getWorld();
        assignBases(getMatch().getTeams());
        // plugin.tbases.get(getMatch().getID()).
    }
    
    /**
     * onComplete() is called before money is given. <br/><br/>
     * 
     * Order: onStart(), onComplete(), onFinish()
     */
    @Override
    public void onComplete() {
        super.onComplete();
        int matchID = getMatch().getID();
        plugin.debug.msgArenaPlayers(getMatch().getPlayers(), "onComplete matchID = " + matchID);
    }
    
    /**
     * onFinish() is called after money is given.
     * 
     * Order: onStart(), onComplete(), onFinish()
     */
    @Override
    public void onFinish() {
        super.onFinish();
        int matchID = getMatch().getID();
        plugin.debug.msgArenaPlayers(getMatch().getPlayers(), "onFinish matchID = " + matchID);
        plugin.carriers.remove(matchID);
        plugin.bases.remove(matchID);
    }
    
    /**
     * This is called from onStart() and assigns both Teams to a base. <br/><br/>
     * 
     * Since teams are assigned to a base, we can use this information to prevent them from 
     * trying to destroy their own base. <br/><br/>
     * 
     * And force them to destroy the other teams base. <br/><br/>
     * 
     * @param bothTeams - Assign bases for what teams ?
     */
    public void assignBases(List<ArenaTeam> bothTeams) {
        
        WorldGuardPlugin wg = WGBukkit.getPlugin();
        
        for (ArenaTeam t : bothTeams) {
            Set<Player> playerzSet = t.getBukkitPlayers();
            Player playerOne = null;
            // Use the 1st player on the Team to assign the base
            // for the whole team.
            for (Player first : playerzSet) {
                playerOne = first;
                break;
            }
            World w = playerOne.getWorld();
            RegionManager manager = wg.getRegionManager(w);
            Location loc = playerOne.getLocation();
            ApplicableRegionSet set = manager.getApplicableRegions(loc);
            Vector teleportV = null;
            Vector spawnV = null;            
            for (ProtectedRegion region : set) {
                if (region.getFlag(DefaultFlag.TELE_LOC) != null) {
                    teleportV = region.getFlag(DefaultFlag.TELE_LOC).getPosition();
                }
                if (region.getFlag(DefaultFlag.SPAWN_LOC) != null) {
                    spawnV = region.getFlag(DefaultFlag.SPAWN_LOC).getPosition();
                }
            }
            // COMPARE THESE TWO POINTS WITH THE PLAYER TO DETERMINE
            //        WHICH BASE IS CLOSER.
            Location teleportXYZ = new Location(w,
                    teleportV.getBlockX(), teleportV.getBlockY(), teleportV.getBlockZ());
            Location spawnXYZ = new Location(w, 
                    spawnV.getBlockX(), spawnV.getBlockY(), spawnV.getBlockZ());
            
            double tdistance = playerOne.getLocation().distance(teleportXYZ);
            double sdistance = playerOne.getLocation().distance(spawnXYZ);
            
            if (tdistance < sdistance) {
                assignBase(t.getId(), teleportXYZ);
            } else if ( tdistance > sdistance) {
                assignBase(t.getId(), spawnXYZ);
            } else if (tdistance == sdistance) {
                plugin.getLogger().warning("Could NOT assign bases because " 
                        + "the player's spawn is equi-distance to both.");
                plugin.getLogger().info("Please change the spawn locations " 
                        + "for the teams in the bomb arena.");
            }
            
            
        }
    }
    
    /**
     * DO NOT USE THIS METHOD. This method is used by assignBases() <br/><br/>
     * 
     * @param teamID assign this team to a certain location (base).
     * @param loc This is the location of their own base. (NOT the enemy base).
     */
    private void assignBase(int teamID, Location loc) {
        plugin.debug.log("asssBase(int teamID, Location loc");
        plugin.debug.log("teamID = " + teamID);
        plugin.debug.log("Location loc = " + loc.toString());
        int length = 5;
        int matchID = getMatch().getID();
        plugin.debug.log("matchID = " + matchID);
        Map<Integer, Location> temp = new HashMap<Integer, Location>();

        // Set one corner of the cube to the given location.
        // Uses getBlockN() instead of getN() to avoid casting to an int later.
        int x1 = loc.getBlockX() - length;
        int y1 = loc.getBlockY() - length;
        int z1 = loc.getBlockZ() - length;

        // Figure out the opposite corner of the cube by taking the corner and adding length to all coordinates.
        int x2 = loc.getBlockX() + length;
        int y2 = loc.getBlockY() + length;
        int z2 = loc.getBlockZ() + length;

        World world = loc.getWorld();
        plugin.debug.log("World world = " + world.getName());

        // Loop over the cube in the x dimension.
        for (int xPoint = x1; xPoint <= x2; xPoint++) {
            // Loop over the cube in the y dimension.
            for (int yPoint = y1; yPoint <= y2; yPoint++) {
                // Loop over the cube in the z dimension.
                for (int zPoint = z1; zPoint <= z2; zPoint++) {
                    // Get the block that we are currently looping over.
                    Block currentBlock = world.getBlockAt(xPoint, yPoint, zPoint);
                    // Set the block to type 57 (Diamond block!)
                    if (currentBlock.getType() == Material.BREWING_STAND) {
                        // currentBlock.setType(Material.HARD_CLAY);
                        Location base_loc = new Location(world, xPoint, yPoint, zPoint);
                        plugin.debug.log("base_loc = " + base_loc.toString());
                        temp.put(teamID, base_loc);
                        plugin.debug.log("temp.get(teamID).toString = " + temp.get(teamID).toString());
                        plugin.bases.put(matchID, temp);
                        plugin.debug.log("plugin.bases.get(matchID).toString = "
                                + plugin.bases.get(matchID).toString());
                        plugin.debug.log("plugin.bases.get(matchID).get(teamID) = " 
                                + plugin.bases.get(matchID).get(teamID).toString());
                        
                        // Checking for the correct base below:

                        // plugin.bases.get(matchID).get(teamID).distance(player.getLocation);
                        // plugin.pbases.get(id).get(p.getName()).distance(p.getLocation());
                        // if the distance is less than 20, then don't allow the bomb to be planted.
                    }
                }
            }
        }
    }
    
    /**
     * This method uses a Player input parameter to get the other team.
     * 
     * @param p Use this player to get his opponents Team.
     * @return Returns an ArenaTeam object of the other team.
     * @throws NullPointerException Handles the scenario where a server owner mis-configured a Bomb Arena with only ONE Team.
     */
    public ArenaTeam getOtherTeam(Player p) throws NullPointerException {
        // get the player
        // get his team
        ArenaTeam team1 = getTeam(p);
        // get the OTHER team
        List<ArenaTeam> bothTeams = getTeams();
        for (ArenaTeam t : bothTeams) {
            if (team1 != t) {
                return t;
            }
        }
        plugin.getLogger().warning("getOtherTeam() method failed: The Bomb Arena Type must have two teams!");
        
        throw new NullPointerException();

    }

    /**
     * Once the bomb is picked up, we need to set compass direction. <br/><br/>
     * 
     * This is to help out BOTH attackers and defenders find where they need to go 
     * if they're unfamiliar with the map. <br/><br/>
     * 
     * This method generates an NPE if a player does NOT have a compass. <br/><br/>
     * 
     * Therefore, we need to check to make sure a player has a compass 
     * before we try to set it.
     * 
     * @param players For which players are we setting the compass ?
     * @param team2 Where are we pointing the Compass Direction ? To the other teams base.
     */
    private void setCompass(Set<ArenaPlayer> players, ArenaTeam team2) {
        plugin.debug.log("setCompass(), team2 = " + team2);
        int matchID = getMatch().getID();
        int teamID = team2.getId();
        plugin.debug.log("getMatch().getID() = " + matchID);
        plugin.debug.log("plugin.bases.get(matchID).get(teamID) = "
                + plugin.bases.get(matchID).get(teamID));

        // NullPointerException if the player doesn't have a compass
        // in their inventory
        for (ArenaPlayer p : players) {
            if (p.getInventory().contains(Material.COMPASS)) {
                p.getPlayer().setCompassTarget(plugin.bases.get(matchID).get(teamID)); // 519
                if (p.getTeam().getId() == teamID) {
                } else {
                }
            } else {
                plugin.getLogger().warning(
                        "Players in the bomb Arena type should have a compass so they know " 
                        + "where bombs and bases are located.");
            }
        }
    }
    
    
}

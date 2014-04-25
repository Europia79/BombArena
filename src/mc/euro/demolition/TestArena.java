package mc.euro.demolition;

import mc.euro.demolition.objects.Bomb;
import mc.euro.demolition.util.DetonateTimer;
import mc.euro.demolition.util.PlantTimer;
import java.util.Set;
import mc.alk.arena.events.players.ArenaPlayerLeaveEvent;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.arenas.Arena;
import mc.alk.arena.objects.events.ArenaEventHandler;
import mc.alk.arena.objects.teams.ArenaTeam;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
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
 * This class is used to systemically TEST and DEBUG events. <br/><br/>
 * 
 * For example, one of the events was breaking all other events. <br/>
 * I used this testing class to add methods one by one in order to find the offending event. <br/><br/>
 * 
 * During testing mode, change Main.java to register this class 
 * instead of BombArenaListener.class <br/><br/>
 * 
 * @author Nikolai
 *
 * <pre>
 * Bomb = Hardened Clay 172. 
 *
 * Listen for: 
 * onBombPickup() - set HAT & compass. 
 * onBombCarrierLeave() - if they log out or leave the arena.
 * onBombCarrierDeath() - drop it on the ground. 
 * onBombDrop() - is it outside the map ? 
 * onBombDespawn() - respawn or cancel the event. 
 * onBombPlace() - trigger onBombPlant() if close enough. 
 * onBombPlant() - takes 7 sec to plant + 30 sec to blow up. 
 * onPlantFailure() - Self cancelled or caused by death ? 
 * onBombDefuse() - takes 7 sec, declare winners. 
 * </pre>
 * 
 *
 */
public class TestArena extends Arena {

    BombPlugin plugin;

    /**
     * Constructor for the TESTER class.
     */
    public TestArena() {
        plugin = (BombPlugin) Bukkit.getPluginManager().getPlugin("Demolition");
    }
    
 
}

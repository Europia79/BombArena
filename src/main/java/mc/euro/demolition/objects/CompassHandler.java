package mc.euro.demolition.objects;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.arenas.Arena;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Continually update players compass to a new Location every X ticks. <br/><br/>
 * 
 * This class might be moved to the BattleArena framework so that other 
 * BattleArena extensions, modules, & addons have access.
 * 
 * @author Nikolai
 */
public class CompassHandler extends BukkitRunnable {
    
    Arena arena;
    Collection<Location> locations = new HashSet<Location>();
    
    /**
     * Ideally, I would have liked this constructor to accept just a list of players.
     * But unfortunately, inside Arenas, a player could leave 
     * the arena, and the list would be outdated once that happened. Why ? <br/><br/>
     * 
     * arena.getMatch().getPlayers()                                       <br/><br/>
     * 
     * Because getPlayers() does not return a reference to all the players 
     * in the match. getPlayers() will dynamically create a new list from the 
     * teams, then throw away the generated list. So essentially, it's a snapshot 
     * of all the players in the match at that point in time.
     * And that's why we have to continually call it: To get an updated & current list.
     */
    public CompassHandler(Arena arena) {
        this.arena = arena;
    }
    
    /**
     * Given a list of locations, calculate the closest point & assign it to the 
     * players compass.
     */
    @Override
    public void run() {
        for (ArenaPlayer ap : arena.getMatch().getPlayers()) {
            if (!ap.getPlayer().getInventory().contains(Material.COMPASS)) continue;
            for (Location loc1 : locations) {
                Location loc2 = ap.getPlayer().getCompassTarget();
                loc2 = (loc2 != null) ? loc2 : new Location(ap.getPlayer().getLocation().getWorld(), 0,0,0);
                double distance1 = loc1.distanceSquared(ap.getPlayer().getLocation());
                double distance2 = loc2.distanceSquared(ap.getPlayer().getLocation());
                if (distance1 < distance2) {
                    ap.getPlayer().setCompassTarget(loc1);
                }
            }
        }
    }
    
    public void pointTo(Location loc) {
        List<Location> list = new ArrayList<Location>();
        if (loc != null) list.add(loc);
        pointTo(list);
    }
    
    public void pointTo(Collection<Location> locs) {
        locations = locs;
        run(); // update the players compass immediately when a change occurs.
    }
    
}

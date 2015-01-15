package mc.euro.demolition.holograms;

import com.dsh105.holoapi.HoloAPI;
import com.dsh105.holoapi.api.Hologram;
import com.dsh105.holoapi.api.HologramFactory;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;

/**
 *
 * @author Nikolai
 */
public class HolographicAPI implements HologramInterface {

    Plugin plugin;
    private final AtomicInteger ID = new AtomicInteger(0);
    private final ConcurrentHashMap<Integer, Hologram> hmap = new ConcurrentHashMap<Integer, Hologram>();
    
    public HolographicAPI(Plugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public int createBaseHologram(Location loc) {
        Location center = loc.clone().add(0.5, 1.0, 0.5);
        Hologram hologram = new HologramFactory(plugin)
                .withLocation(center)
                .withText("Target")
                .withSimplicity(true)
                .build();
        int hologramID = addHologram(hologram);
        return hologramID;
    }

    @Override
    public int createBombHologram(Location loc) {
        Location center = loc.clone().add(0.0, 0.2, 0.0);
        Hologram hologram = new HologramFactory(plugin)
                .withLocation(center)
                .withText("Bomb")
                .withSimplicity(true)
                .build();
        int hologramID = addHologram(hologram);
        return hologramID;
    }
    
    @Override
    public void teleport(int id, Location loc) {
        Hologram temp = getHologram(id);
        if (temp != null) {
            temp.move(loc.clone().add(0.0, 0.4, 0.0));
        }
    }
    
    @Override
    public void removeHologram(int id) {
        deleteHologram(id);
    }
    
    protected int addHologram(Hologram hologram) {
        int id = ID.incrementAndGet();
        hmap.put(id, hologram);
        return id;
    }
    
    protected Hologram getHologram(int id) {
        return hmap.get(id);
    }
    
    protected void deleteHologram(int id) {
        if (hmap.containsKey(id)) {
            Hologram hologram = hmap.get(id);
            HoloAPI.getManager().stopTracking(hologram);
            hmap.remove(id);
        }
    }
    
}

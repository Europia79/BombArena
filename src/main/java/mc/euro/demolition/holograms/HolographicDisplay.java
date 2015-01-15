package mc.euro.demolition.holograms;

import com.gmail.filoghost.holograms.api.Hologram;
import com.gmail.filoghost.holograms.api.HolographicDisplaysAPI;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;

/**
 *
 * @author Nikolai
 */
public class HolographicDisplay implements HologramInterface {
    
    Plugin plugin;
    private final AtomicInteger ID = new AtomicInteger(0);
    private final ConcurrentHashMap<Integer, Hologram> hmap = new ConcurrentHashMap<Integer, Hologram>();
    
    public HolographicDisplay(Plugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public int createBaseHologram(Location loc) {
        Location center = loc.clone().add(0.5, 1.2, 0.5);
        Hologram hologram = HolographicDisplaysAPI.createHologram(plugin, center, "Target");
        int hologramID = addHologram(hologram);
        return hologramID;
    }

    @Override
    public int createBombHologram(Location loc) {
        Location center = loc.clone().add(0.0, 0.4, 0.0);
        Hologram hologram = HolographicDisplaysAPI.createHologram(plugin, center, "Bomb");
        int hologramID = addHologram(hologram);
        return hologramID;
    }
    
    @Override
    public void teleport(int id, Location loc) {
        Hologram temp = getHologram(id);
        if (temp != null) {
            temp.teleport(loc.clone().add(0.0, 0.4, 0.0));
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
            hologram.delete();
            hmap.remove(id);
        }
    }

}

package mc.euro.demolition.holograms;

import com.gmail.filoghost.holograms.api.Hologram;
import com.gmail.filoghost.holograms.api.HolographicDisplaysAPI;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;

/**
 *
 * @author Nikolai
 */
public class HolographicDisplay extends HoloManager implements HologramInterface {
    
    Plugin plugin;
    
    public HolographicDisplay(Plugin plugin) {
        super();
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
        getHologram(id).teleport(loc.clone().add(0.0, 0.4, 0.0));
    }
    
    @Override
    public void removeHologram(int id) {
        deleteHologram(id);
    }
    
}

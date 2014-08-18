package mc.euro.demolition.holograms;

import mc.euro.demolition.BombPlugin;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;

/**
 *
 * @author Nikolai
 */
public class HolographicAPI implements HologramInterface {

    Plugin plugin;
    
    public HolographicAPI(Plugin plugin) {
        this.plugin = plugin;
    }
    @Override
    public int createBaseHologram(Location loc) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int createBombHologram(Location loc) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void teleport(int id, Location loc) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void removeHologram(int id) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}

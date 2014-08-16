package mc.euro.demolition.holograms;

import org.bukkit.Location;

/**
 *
 * @author Nikolai
 */
public interface HologramInterface {
    
    public int createBaseHologram(Location loc);
    public int createBombHologram(Location loc);
    public void teleport(int id, Location loc);
    public void removeHologram(int id);
    
}

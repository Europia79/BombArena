package mc.euro.demolition.holograms;

import org.bukkit.Location;

/**
 *
 * @author Nikolai
 */
public class HologramsOff implements HologramInterface {
    
    @Override
    public int createBaseHologram(Location loc) {
        return 0;
    }
    
    @Override
    public int createBombHologram(Location loc) {
        return 0;
    }
    
    @Override
    public void teleport(int id, Location loc) {
        
    }
    
    @Override
    public void removeHologram(int id) {
        
    }
    
}

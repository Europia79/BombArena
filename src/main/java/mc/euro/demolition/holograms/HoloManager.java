package mc.euro.demolition.holograms;

import com.gmail.filoghost.holograms.api.Hologram;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author Nikolai
 */
public abstract class HoloManager {
    
    private final AtomicInteger hologramID = new AtomicInteger(0);
    private final ConcurrentHashMap<Integer, Hologram> hmap = new ConcurrentHashMap<Integer, Hologram>();
    
    public HoloManager() {
    }
    
    private int createId() {
        return hologramID.incrementAndGet();
    }
    
    protected int addHologram(Hologram hologram) {
        int id = createId();
        hmap.put(id, hologram);
        return id;
    }
    
    protected Hologram getHologram(int id) {
        return hmap.get(id);
    }
    
    protected void deleteHologram(int id) {
        Hologram hologram = hmap.get(id);
        hologram.delete();
    }
    
}

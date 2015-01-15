package mc.euro.demolition.util;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryType;

/**
 * Handles the conversion from Material.X to InventoryType.X.
 */
public class BaseType {
    
    private static final Map<Material, InventoryType> emap = initMap();

    private static Map<Material, InventoryType> initMap() {
        Map<Material, InventoryType> temp = new EnumMap<Material, InventoryType>(Material.class);
        temp.put(Material.ANVIL, InventoryType.ANVIL); // 1.4.2
        temp.put(Material.BEACON, InventoryType.BEACON); // 1.4.2
        temp.put(Material.BREWING_STAND, InventoryType.BREWING);
        temp.put(Material.CHEST, InventoryType.CHEST);
        temp.put(Material.ENDER_CHEST, InventoryType.ENDER_CHEST); // 1.3.1
        temp.put(Material.TRAPPED_CHEST, InventoryType.CHEST); // 1.5
        temp.put(Material.ENCHANTMENT_TABLE, InventoryType.ENCHANTING);
        temp.put(Material.DISPENSER, InventoryType.DISPENSER);
        temp.put(Material.DROPPER, InventoryType.DROPPER); // 1.5
        temp.put(Material.FURNACE, InventoryType.FURNACE);
        temp.put(Material.HOPPER, InventoryType.HOPPER); // 1.5
        temp.put(Material.WORKBENCH, InventoryType.WORKBENCH);
        return Collections.unmodifiableMap(temp);

    }

    public static InventoryType convert(Material x) throws IllegalArgumentException {
        if (!emap.containsKey(x)) {
            throw new IllegalArgumentException(x.toString());
        }
        return emap.get(x);
    }
    
    public static boolean containsKey(String x) {
        Material k = Material.valueOf(x.toUpperCase());
        return emap.containsKey(k);
    }
    
    public static boolean containsValue(String x) {
        InventoryType v = InventoryType.valueOf(x.toUpperCase());
        return emap.containsValue(v);
    }
    
    public static Collection<InventoryType> values() {
        return emap.values();
    }
    
}

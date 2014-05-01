package mc.euro.demolition.objects;

import java.util.EnumMap;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryType;

/**
 * Converts from Material.X to InventoryType.X
 */
public class BaseType {
    
    public static EnumMap<Material, InventoryType> emap = new EnumMap(Material.class);
    
    static {
        emap.put(Material.ANVIL, InventoryType.ANVIL);
        emap.put(Material.BEACON, InventoryType.BEACON);
        emap.put(Material.BREWING_STAND, InventoryType.BREWING);
        emap.put(Material.CHEST, InventoryType.CHEST);
        emap.put(Material.ENDER_CHEST, InventoryType.ENDER_CHEST);
        emap.put(Material.TRAPPED_CHEST, InventoryType.CHEST);
        emap.put(Material.ENCHANTMENT_TABLE, InventoryType.ENCHANTING);
        emap.put(Material.DISPENSER, InventoryType.DISPENSER);
        emap.put(Material.DROPPER, InventoryType.DROPPER);
        emap.put(Material.FURNACE, InventoryType.FURNACE);
        emap.put(Material.HOPPER, InventoryType.HOPPER);
        emap.put(Material.WORKBENCH, InventoryType.WORKBENCH);
    }
    
    public static InventoryType convert(Material x) throws IllegalArgumentException {
        if (!emap.containsKey(x)) {
            throw new IllegalArgumentException(x.toString());
        }
        return emap.get(x);
    }
    
    public static boolean contains(String x) {
        Material k = Material.valueOf(x.toUpperCase());
        return emap.containsKey(k);
    }
    
}

package mc.euro.demolition.util;

import org.bukkit.Bukkit;

/**
 *
 * @author Nikolai
 */
public class VersionFormat {
    
    public static int getBAversion() {
        String v = "" + Bukkit.getServer().getPluginManager()
                .getPlugin("BattleArena").getDescription().getVersion();
        return getVersion(v);
    }
    
    public static int getVersion(String version) {
        String noDots = version.replaceAll("[.]", "");
        int length = noDots.length();
        
        StringBuilder sb = new StringBuilder();
        sb.append(noDots);
        
        for (int i = length + 1; i <= 6; i++) {
            sb.append("0");
        }
        String sixDigit = sb.toString();

        int ver = Integer.parseInt(sixDigit);
        
        return ver;
    }
    
}

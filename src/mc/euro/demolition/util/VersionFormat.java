package mc.euro.demolition.util;

import org.bukkit.Bukkit;

/**
 * Adds ability to do version comparison.
 * 
 * @author Nikolai
 */
public class VersionFormat {
    
    public static int getBAversion() {
        String v = "" + Bukkit.getServer().getPluginManager()
                .getPlugin("BattleArena").getDescription().getVersion();
        return getVersion(v);
    }
    
    /**
     * @return By default, it returns a six digit number padded with extra zeros. 
     */
    public static int getVersion(String version) {
        return getVersion(version, 6);
    }
    
    public static int getVersion(String version, int digits) {
        String noDots = version.replaceAll("[.]", "");
        int length = noDots.length();
        
        StringBuilder sb = new StringBuilder();
        sb.append(noDots);
        
        int N = digits;
        for (int i = length + 1; i <= N; i++) {
            sb.append("0");
        }
        String N_Digits = sb.toString();

        int ver = Integer.parseInt(N_Digits);
        
        return ver;
    }
    
}

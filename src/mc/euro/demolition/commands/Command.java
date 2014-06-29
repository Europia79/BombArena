package mc.euro.demolition.commands;

import mc.euro.demolition.util.VersionFormat;

/**
 * This class handles backwards compatibility for different 
 * versions of BattleArena commands. <br/><br/>
 * 
 * <pre>
 * 
 * Version - Syntax
 * 
 * +396200 - /aa addspawn {block} fs=1 rs=500 ds=500 index=1
 * -395850 - /aa addspawn {block} fs=1 rs=500 ds=500 1
 * 
 * </pre>
 * 
 * @author Nikolai
 */
public abstract class Command {

    public static String addspawn(String bomb, int time) {
        // /aa addspawn BOMB_BLOCK fs=1 rs=500 ds=500 index=1
        // /aa addspawn BOMB_BLOCK fs=1 rs=500 ds=500 1
        String cmd1 = "aa addspawn " + bomb
                + " fs=1"
                + " rs=" + time
                + " ds=" + time
                + " index=1";
        String cmd2 = "aa addspawn " + bomb
                + " fs=1"
                + " rs=" + time
                + " ds=" + time
                + " 1";
        String cmd = (getVersion() >= 396000) ? cmd1 : cmd2;
        return cmd;
    }
    
    private static int getVersion() {
        return VersionFormat.getBAversion();
    }
    

}

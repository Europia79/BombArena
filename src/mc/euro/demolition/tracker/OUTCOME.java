/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mc.euro.demolition.tracker;

import mc.alk.tracker.objects.WLT;

/**
 * This class defines wins, losses, and ties <br/><br/>
 * 
 * WIN = getPlantSuccess() <br/>
 * LOSS = getPlantFailure() <br/>
 * TIE = getDefuseSuccess() <br/>
 *
 * @author Nikolai
 */
public class OUTCOME {
    
    public static WLT getPlantSuccess() {
        return WLT.WIN;
    }
    
    public static WLT getPlantFailure() {
        return WLT.LOSS;
    }
    
    public static WLT getDefuseSuccess() {
        return WLT.TIE;
    }
    
}

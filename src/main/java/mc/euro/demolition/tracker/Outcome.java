package mc.euro.demolition.tracker;

import mc.alk.tracker.objects.WLT;

/**
 * This class defines wins, losses, and ties. <br/><br/>
 * <pre>
 * WIN = getPlantSuccess()
 * LOSS = getPlantFailure()
 * TIE = getDefuseSuccess()
 * </pre>
 * @author Nikolai
 */
public class Outcome {
    
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

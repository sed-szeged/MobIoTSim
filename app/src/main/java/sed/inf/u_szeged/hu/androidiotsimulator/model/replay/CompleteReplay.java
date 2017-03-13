package sed.inf.u_szeged.hu.androidiotsimulator.model.replay;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tommy on 1/29/2017. Project name: MobIoTSim-mirrored
 *  
 */

public class CompleteReplay {

    private List<ValueWrapper> wrappers;


    public CompleteReplay() {
        this.wrappers = new ArrayList<>();
    }

    public void addLog(ValueWrapper w) {
        wrappers.add(w);
    }

    @Override
    public String toString() {
        String res = "[";

        for (ValueWrapper w : wrappers) {
            res += w.toString();

            if (w != wrappers.get(wrappers.size() - 1)) {
                res += ",";
            }
        }

        res += "]";

        return res;
    }

    public int getLenght() {
        return wrappers.size();
    }
}

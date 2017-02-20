package sed.inf.u_szeged.hu.androidiotsimulator.model.replay;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tommy on 1/29/2017. Project name: MobIoTSim-mirrored
 *  
 */

public class ValueWrapper {

    List<Value> valueList;

    public ValueWrapper() {
        this.valueList = new ArrayList<>();
    }


    public void addValue(Value v) {
        valueList.add(v);
    }

    @Override
    public String toString() {
        String res = "{";
        for (Value v : valueList) {
            res += "\"" + v.getName() + "\" : " + v.getValue();

            if (v != valueList.get(valueList.size() - 1)) {
                res += ",";
            }
        }

        res += "}";

        return res;
    }
}

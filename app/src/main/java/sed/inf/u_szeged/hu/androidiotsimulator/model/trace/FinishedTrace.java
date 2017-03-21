package sed.inf.u_szeged.hu.androidiotsimulator.model.trace;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tommy on 1/29/2017. Project name: MobIoTSim-mirrored
 *  
 */

public class FinishedTrace {

    @SerializedName("cycles")
    @Expose
    private List<ParameterWrapper> cycles;


    public FinishedTrace() {
        this.cycles = new ArrayList<>();
    }

    public void addLog(ParameterWrapper w) {
        cycles.add(w);
    }

    @Override
    public String toString() {
        String res = "[";

        for (ParameterWrapper w : cycles) {
            res += w.toString();

            if (w != cycles.get(cycles.size() - 1)) {
                res += ",";
            }
        }

        res += "]";

        return res;
    }

    public int getLenght() {
        return cycles.size();
    }

    public List<ParameterWrapper> getCycles() {
        return cycles;
    }

    public void setCycles(List<ParameterWrapper> cycles) {
        this.cycles = cycles;
    }
}

package sed.inf.u_szeged.hu.androidiotsimulator.model.gson.trace.openweather;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by tommy on 3/25/2017. Project name: MobIoTSim-mirrored
 *  
 */

public class OpenweatherTrace {

    @SerializedName("cntcycles")
    @Expose
    private int cntcycles;

    @SerializedName("cycles")
    @Expose
    private List<OpenweatherCycle> cycles;

    @SerializedName("type")
    @Expose
    private String type;

    public int getCntcycles() {
        return cntcycles;
    }

    public void setCntcycles(int cntcycles) {
        this.cntcycles = cntcycles;
    }

    public List<OpenweatherCycle> getCycles() {
        return cycles;
    }

    public void setCycles(List<OpenweatherCycle> cycles) {
        this.cycles = cycles;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}

package sed.inf.u_szeged.hu.androidiotsimulator.model.gson.trace.randomdata;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tommy on 3/13/2017. Project name: MobIoTSim-mirrored
 *  
 */
public class TraceGroup {

    @SerializedName("traceGroup")
    @Expose
    private List<FinishedTrace> traceGroup;


    @SerializedName("cnt")
    @Expose
    private int cnt;

    @SerializedName("type")
    @Expose
    private String type;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setCnt(int cnt) {
        this.cnt = cnt;
    }

    public int getCnt() {
        return cnt;
    }

    public TraceGroup() {
        traceGroup = new ArrayList<>();
    }

    public void add(FinishedTrace trace) {
        traceGroup.add(trace);
    }


    public List<FinishedTrace> getTraceGroup() {
        return traceGroup;
    }

    public void setTraceGroup(List<FinishedTrace> traceGroup) {
        this.traceGroup = traceGroup;
    }
}

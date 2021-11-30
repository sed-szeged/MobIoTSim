package sed.inf.u_szeged.hu.androidiotsimulator.model.gson.devicetype;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class DeviceTypes {

    @SerializedName("results")
    @Expose
    private List<Result> results = null;
    @SerializedName("meta")
    @Expose
    private Meta meta;

    public List<Result> getResults() {
        return results;
    }

    public void setResults(List<Result> results) {
        this.results = results;
    }

    public Meta getMeta() {
        return meta;
    }

    public void setMeta(Meta meta) {
        this.meta = meta;
    }

}

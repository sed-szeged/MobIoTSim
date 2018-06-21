package sed.inf.u_szeged.hu.androidiotsimulator.model.gson.trace.randomdata;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tommy on 1/29/2017. Project name: MobIoTSim-mirrored
 *  
 */

public class ParameterWrapper {

    @SerializedName("parameterList")
    @Expose
    private List<Parameter> parameterList;

    public ParameterWrapper() {
        this.parameterList = new ArrayList<>();
    }


    public void addValue(Parameter v) {
        parameterList.add(v);
    }

    @Override
    public String toString() {
        String res = "{";
        for (Parameter v : parameterList) {
            res += "\"" + v.getName() + "\" : " + v.getValue();

            if (v != parameterList.get(parameterList.size() - 1)) {
                res += ",";
            }
        }

        res += "}";

        return res;
    }

    public List<Parameter> getParameterList() {
        return parameterList;
    }

    public void setParameterList(List<Parameter> parameterList) {
        this.parameterList = parameterList;
    }
}

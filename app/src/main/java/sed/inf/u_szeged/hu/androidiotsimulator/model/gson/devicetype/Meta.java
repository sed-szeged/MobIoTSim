package sed.inf.u_szeged.hu.androidiotsimulator.model.gson.devicetype;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Meta {

    @SerializedName("total_rows")
    @Expose
    private Integer totalRows;

    public Integer getTotalRows() {
        return totalRows;
    }

    public void setTotalRows(Integer totalRows) {
        this.totalRows = totalRows;
    }

}

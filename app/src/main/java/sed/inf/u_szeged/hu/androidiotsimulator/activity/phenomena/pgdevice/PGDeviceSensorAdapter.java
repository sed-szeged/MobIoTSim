package sed.inf.u_szeged.hu.androidiotsimulator.activity.phenomena.pgdevice;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.Iterator;
import java.util.List;

import sed.inf.u_szeged.hu.androidiotsimulator.R;
import sed.inf.u_szeged.hu.androidiotsimulator.model.phenGenerator.general.Measurement;
import sed.inf.u_szeged.hu.androidiotsimulator.model.phenGenerator.general.PhenGenDevice;

public class PGDeviceSensorAdapter extends ArrayAdapter<PGDeviceSensorInfo> {
    private Context context;

    public PGDeviceSensorAdapter(Context context, int resource, List<PGDeviceSensorInfo> items) {
        super(context, resource, items);
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(final int position, View convertView, @NonNull ViewGroup parent) {

        View v = convertView;

        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.listitem_pgdevice_sensor, null);
        }

        final PGDeviceSensorInfo pgDeviceSensorInfo = getItem(position);

        if (pgDeviceSensorInfo != null) {
            TextView sensorName = (TextView) v.findViewById(R.id.pgdeviceSensorList_sensor_name);
            if (sensorName != null) sensorName.setText(pgDeviceSensorInfo.sensorName);

            TextView sensorType = (TextView) v.findViewById(R.id.pgdeviceSensor_type);
            if (sensorType != null) sensorType.setText(pgDeviceSensorInfo.sensorParameterType);

            TextView currentValue = (TextView) v.findViewById(R.id.pgdeviceSensor_currentValue_value);
            if (currentValue != null) currentValue.setText(pgDeviceSensorInfo.sensorValue);
        }

        return v;
    }
}

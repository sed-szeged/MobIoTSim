package sed.inf.u_szeged.hu.androidiotsimulator.activity.phenomena.simulation;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

import sed.inf.u_szeged.hu.androidiotsimulator.R;
import sed.inf.u_szeged.hu.androidiotsimulator.model.device.DeviceGroup;
import sed.inf.u_szeged.hu.androidiotsimulator.model.phenGenerator.general.Simulation;

public class SimulationAdapter extends ArrayAdapter<Simulation> {

    public SimulationAdapter(Context context, int resource, List<Simulation> items) {
        super(context, resource, items);
    }

    @NonNull
    @Override
    public View getView(final int position, View convertView, @NonNull ViewGroup parent) {

        View v = convertView;

        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.listitem_simulation, null);
        }

        final Simulation simulation = getItem(position);

        if (simulation != null) {
            TextView simulationName = (TextView) v.findViewById(R.id.simulationList_item_name);
            if (simulationName != null) simulationName.setText(simulation.getName());

            TextView themeName = (TextView) v.findViewById(R.id.simulationList_item_theme);
            if (themeName != null) themeName.setText(simulation.theme.getName());

        }

        return v;
    }
}

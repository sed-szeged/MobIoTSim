package sed.inf.u_szeged.hu.androidiotsimulator.activity.phenomena.population;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import sed.inf.u_szeged.hu.androidiotsimulator.R;
import sed.inf.u_szeged.hu.androidiotsimulator.model.phenGenerator.general.Population;
import sed.inf.u_szeged.hu.androidiotsimulator.model.phenGenerator.general.Simulation;

public class PopulationAdapter extends ArrayAdapter<Population> {
    private Context context;

    public PopulationAdapter(Context context, int resource, List<Population> items) {
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
            v = vi.inflate(R.layout.listitem_population, null);
        }

        final Population population = getItem(position);

        if (population != null) {
            TextView populationName = (TextView) v.findViewById(R.id.populationsList_item_name);
            if (populationName != null) populationName.setText(population.getPopulationName());

            TextView populationSize = (TextView) v.findViewById(R.id.population_size_caption);
            if (populationSize != null) populationSize.setText(
                    context.getString(R.string.population_size_caption) + " "
                    + population.getActualNumberOfSubjects()
            );

        }

        return v;
    }
}

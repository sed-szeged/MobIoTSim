package sed.inf.u_szeged.hu.androidiotsimulator.activity.phenomena.population;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import sed.inf.u_szeged.hu.androidiotsimulator.R;
import sed.inf.u_szeged.hu.androidiotsimulator.model.phenGenerator.general.PhenGenMain;
import sed.inf.u_szeged.hu.androidiotsimulator.model.phenGenerator.general.Population;
import sed.inf.u_szeged.hu.androidiotsimulator.model.phenGenerator.general.Simulation;

public class PopulationsActivity extends AppCompatActivity {

    public static final int ADD_POPULATION_REQ_CODE = 901;
    public static final int SHOW_POPULATION_REQ_CODE = 902;

    private Simulation currentSimulation;
    private long currentSimulationKey;

    private TextView simulationName;

    private List<Population> availablePopulations;
    private PopulationAdapter populationAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_populations);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        currentSimulationKey = getIntent().getExtras().getLong(PhenGenMain.KEY_SIMULATION);
        currentSimulation = PhenGenMain.getPhenGenMain().getSimulationByKey(currentSimulationKey);

        simulationName = (TextView) findViewById(R.id.simulation_name);
        if (simulationName != null) simulationName.setText(currentSimulation.getName());

        ListView listView = (ListView) findViewById(R.id.populations_lv);
        renewPopulationList();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                showPopulation(populationAdapter.getItem(position));
            }
        });

        TextView themeName = (TextView) findViewById(R.id.simulation_theme);
        if (themeName != null) themeName.setText(currentSimulation.theme.getName());

        FloatingActionButton new_population_fab = (FloatingActionButton) findViewById(R.id.new_population_fab);
        new_population_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createNewPopulation();            }
        });
    }

    private void createNewPopulation() {
        Intent intent = new Intent(this, PopulationSettingsActivity.class);
        Bundle bundle = new Bundle();
        bundle.putLong(PhenGenMain.KEY_SIMULATION, currentSimulationKey);
        bundle.putLong(PhenGenMain.KEY_POPULATION, PhenGenMain.NEW_ELEMENT);
        intent.putExtras(bundle);
        startActivityForResult(intent, ADD_POPULATION_REQ_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        renewPopulationList();
        populationAdapter.notifyDataSetChanged();
    }

    private void showPopulation(Population selectedPopulation) {
        Intent intent = new Intent(this, PopulationSettingsActivity.class);
        Bundle bundle = new Bundle();

        bundle.putLong(PhenGenMain.KEY_SIMULATION, currentSimulationKey);
        bundle.putLong(PhenGenMain.KEY_POPULATION,
                PhenGenMain.getPhenGenMain().getKeyByPopulation(selectedPopulation));

        intent.putExtras(bundle);
        startActivityForResult(intent, SHOW_POPULATION_REQ_CODE);
    }


    private void renewPopulationList() {
        availablePopulations = PhenGenMain.getPhenGenMain().getPopulationList(currentSimulationKey);

        populationAdapter = new PopulationAdapter(this,
                R.layout.listitem_population, availablePopulations);

        ListView listView = (ListView) findViewById(R.id.populations_lv);
        listView.setAdapter(populationAdapter);
    }
}

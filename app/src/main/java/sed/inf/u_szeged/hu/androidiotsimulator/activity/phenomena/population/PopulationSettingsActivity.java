package sed.inf.u_szeged.hu.androidiotsimulator.activity.phenomena.population;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import sed.inf.u_szeged.hu.androidiotsimulator.R;
import sed.inf.u_szeged.hu.androidiotsimulator.activity.phenomena.ActivityUtility;
import sed.inf.u_szeged.hu.androidiotsimulator.activity.phenomena.subject.SubjectsActivity;
import sed.inf.u_szeged.hu.androidiotsimulator.model.phenGenerator.general.Parameter;
import sed.inf.u_szeged.hu.androidiotsimulator.model.phenGenerator.general.PhenGenMain;
import sed.inf.u_szeged.hu.androidiotsimulator.model.phenGenerator.general.Population;
import sed.inf.u_szeged.hu.androidiotsimulator.model.phenGenerator.general.Simulation;

public class PopulationSettingsActivity extends AppCompatActivity {

    public static final int SHOW_SUBJECTS_REQ_CODE = 951;
    public static final int GENERATE_SUBJECTS_REQ_CODE = 961;
    public static final int DELETE_SUBJECTS_REQ_CODE = 972;
    public static final int DELETE_POPULATION_REQ_CODE = 971;

    private Simulation currentSimulation;
    private long currentSimulationKey;
    private Population currentPopulation;
    private long currentPopulationKey;

    private Map<String, Parameter> populationParameters = new LinkedHashMap<>();
    private Map<Parameter, View> views = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_population_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        currentSimulationKey = getIntent().getExtras().getLong(PhenGenMain.KEY_SIMULATION);
        currentSimulation = PhenGenMain.getPhenGenMain().getSimulationByKey(currentSimulationKey);
        currentPopulationKey = getIntent().getExtras().getLong(PhenGenMain.KEY_POPULATION);
        if (currentPopulationKey == PhenGenMain.NEW_ELEMENT) {
            currentPopulation = currentSimulation.createPopulation();
        } else {
            currentPopulation = PhenGenMain.getPhenGenMain().getPopulationByKey(currentPopulationKey);
        }

        TextView simulationName = (TextView) findViewById(R.id.simulation_name);
            if (simulationName != null) simulationName.setText(currentSimulation.getName());
        TextView themeName = (TextView) findViewById(R.id.simulation_theme);
            if (themeName != null) themeName.setText(currentSimulation.theme.getName());
        TextView populationName = (TextView) findViewById(R.id.population_name);
            if (populationName != null) populationName.setText(currentPopulation.getPopulationName());
        TextView populationSize = (TextView) findViewById(R.id.population_size);
            if (populationSize != null) populationSize.setText("" + currentPopulation.getActualNumberOfSubjects());


        ((Button) findViewById(R.id.generate_btn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleAction(GENERATE_SUBJECTS_REQ_CODE);
            }
        });
        ((Button) findViewById(R.id.btn_jump_to_subjects)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleAction(SHOW_SUBJECTS_REQ_CODE);
            }
        });
        ((Button) findViewById(R.id.btn_del_population)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleAction(DELETE_POPULATION_REQ_CODE);
            }
        });

        //Add population settings to the Activity
        populationParameters.putAll(currentPopulation.getPopulationSettings());
        for (Map.Entry<String, Parameter> entry : populationParameters.entrySet()) {
            views.put(entry.getValue(),
                    ActivityUtility.addView(
                            (LinearLayout) findViewById(R.id.populationSettings_list_container),
                            entry.getValue(),
                            entry.getKey()));
            System.out.println("POP_PARAM: " + entry.getKey() + " / " + views.get(entry.getValue()));
        }//for

    }//onCreate

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_population_settings, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        finish();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                boolean success = saveChanges();
                if (! success) return false;

                //on creation of new population
                if (currentPopulationKey == PhenGenMain.NEW_ELEMENT) {
                    PhenGenMain.getPhenGenMain().registratePopulation(currentPopulation);
                }

                setResult(Activity.RESULT_OK);
                finish();
                break;
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return true;
    }

    /**
     *
     * @return Indicates whether saving was successful or not.
     */
    private boolean saveChanges() {
        for (Map.Entry<String, Parameter> parameterEntry : populationParameters.entrySet()) {
            View view = views.get(parameterEntry.getValue());
            if (view instanceof EditText) {
                String value = ((EditText) view).getText().toString().trim();
                if (TextUtils.isEmpty(value)) {
                    ((EditText) view).setError(getString(R.string.population_settings_must_be_filled));
                    return false;
                }
            }
            ActivityUtility.setParameterFromView(view, parameterEntry.getValue());
        }
        return true;
    }

    private void handleAction(int request_code) {
        switch (request_code) {
            case SHOW_SUBJECTS_REQ_CODE:
                saveChanges();
                Class activityToOpen = SubjectsActivity.class;
                Intent intent = new Intent(this, activityToOpen);
                Bundle bundle = new Bundle();
                bundle.putLong(PhenGenMain.KEY_SIMULATION, currentSimulationKey);
                bundle.putLong(PhenGenMain.KEY_POPULATION,
                        PhenGenMain.getPhenGenMain().getKeyByPopulation(currentPopulation));
                intent.putExtras(bundle);
                startActivityForResult(intent, request_code);
                break;
            case GENERATE_SUBJECTS_REQ_CODE:
                boolean success = saveChanges();
                if (!success) return;

                int countGeneratedSubjects = currentSimulation.generateSubjects(currentPopulation).size();
                String message = (countGeneratedSubjects == 0)
                        ? "No subject has been generated"
                        : "" + countGeneratedSubjects + " subject" + (countGeneratedSubjects==1 ? " has" : "s have")
                            + " been generated";
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT)
                        .show();
                break;
            case DELETE_POPULATION_REQ_CODE:
                currentSimulation.deletePopulation(currentPopulation);
                finish();
                break;
        }
    } //handleAction

}

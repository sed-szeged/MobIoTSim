package sed.inf.u_szeged.hu.androidiotsimulator.activity.phenomena.subject;

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

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import sed.inf.u_szeged.hu.androidiotsimulator.R;
import sed.inf.u_szeged.hu.androidiotsimulator.activity.phenomena.ActivityUtility;
import sed.inf.u_szeged.hu.androidiotsimulator.model.phenGenerator.general.Parameter;
import sed.inf.u_szeged.hu.androidiotsimulator.model.phenGenerator.general.PhenGenMain;
import sed.inf.u_szeged.hu.androidiotsimulator.model.phenGenerator.general.Population;
import sed.inf.u_szeged.hu.androidiotsimulator.model.phenGenerator.general.Simulation;
import sed.inf.u_szeged.hu.androidiotsimulator.model.phenGenerator.general.Subject;

public class SubjectSettingsActivity extends AppCompatActivity {

    private Simulation currentSimulation;
    private long currentSimulationKey;
    private Population currentPopulation;
    private long currentPopulationKey;
    private long currentSubjectKey;
    private Subject currentSubject;

    private Map<String, Parameter> subjectParameters = new LinkedHashMap<>();
    private Map<Parameter, View> views = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subject_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        currentSimulationKey = getIntent().getExtras().getLong(PhenGenMain.KEY_SIMULATION);
        currentSimulation = PhenGenMain.getPhenGenMain().getSimulationByKey(currentSimulationKey);
        currentPopulationKey = getIntent().getExtras().getLong(PhenGenMain.KEY_POPULATION);
        currentPopulation = PhenGenMain.getPhenGenMain().getPopulationByKey(currentPopulationKey);
        currentSubjectKey = getIntent().getExtras().getLong(PhenGenMain.KEY_SUBJECT);
        if (currentSubjectKey == PhenGenMain.NEW_ELEMENT) {
            currentSubject = currentSimulation.createSubject(currentPopulation);
        } else {
            currentSubject = PhenGenMain.getPhenGenMain().getSubjectByKey(currentSubjectKey);
        }

        TextView simulationName = (TextView) findViewById(R.id.simulation_name);
            if (simulationName != null) simulationName.setText(currentSimulation.getName());
        TextView themeName = (TextView) findViewById(R.id.simulation_theme);
            if (themeName != null) themeName.setText(currentSimulation.theme.getName());
        TextView populationName = (TextView) findViewById(R.id.population_name);
            if (populationName != null) populationName.setText(currentPopulation.getPopulationName());
        TextView subjectName = (TextView) findViewById(R.id.subject_name);
            if (subjectName != null) subjectName.setText(currentSubject.getSubjectName());

        //Add subject settings to the Activity
        subjectParameters.putAll(currentSubject.getIndividualSettings());
        for (Map.Entry<String, Parameter> entry : subjectParameters.entrySet()) {
            views.put(entry.getValue(),
                    ActivityUtility.addView(
                            (LinearLayout) findViewById(R.id.subjectSettings_list_container),
                            entry.getValue(),
                            entry.getKey()));
        }//for

    }//onCreate

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_subject_settings, menu);
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
                boolean valid = true;

                for (Map.Entry<String, Parameter> parameterEntry : subjectParameters.entrySet()) {
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
                //on creation of new population
                if (currentSubjectKey == PhenGenMain.NEW_ELEMENT) {
                    PhenGenMain.getPhenGenMain().registrateSubject(currentSubject);
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

}

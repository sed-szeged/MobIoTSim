package sed.inf.u_szeged.hu.androidiotsimulator.activity.phenomena.simulation;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import sed.inf.u_szeged.hu.androidiotsimulator.MobIoTApplication;
import sed.inf.u_szeged.hu.androidiotsimulator.R;
import sed.inf.u_szeged.hu.androidiotsimulator.model.phenGenerator.general.Parameter;
import sed.inf.u_szeged.hu.androidiotsimulator.model.phenGenerator.general.PhenGenMain;
import sed.inf.u_szeged.hu.androidiotsimulator.model.phenGenerator.general.Simulation;

public class SimulationSettingsActivity extends AppCompatActivity {

    private Simulation currentSimulation;
    private long currentSimulationKey;

    public static final String KEY_NAME = "NAME";

    private TextView simulationName;
    private TextView themeName;
    private TextView generationFrequency;
    private TextView timeAcceleration;
    private TextView simulatedDateTime;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simulation_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        currentSimulationKey = getIntent().getExtras().getLong(PhenGenMain.KEY_SIMULATION);
        if (currentSimulationKey == PhenGenMain.NEW_ELEMENT) {
            String themeName = getIntent().getExtras().getString(PhenGenMain.KEY_THEME);
            currentSimulation = PhenGenMain.getPhenGenMain().getThemeByName(themeName).makeNewSimulation();
        } else {
            currentSimulation = PhenGenMain.getPhenGenMain().getSimulationByKey(currentSimulationKey);
        }

        simulationName = (TextView) findViewById(R.id.simulation_name);
        if (simulationName != null) simulationName.setText(currentSimulation.getName());

        themeName = (TextView) findViewById(R.id.theme_name);
        if (themeName != null) themeName.setText(currentSimulation.theme.getName());

        generationFrequency = (TextView) findViewById(R.id.generationFrequency);
        Parameter generationFrequencyParam = (generationFrequency == null) ? null :
                currentSimulation.getGeneralParameter(Simulation.KEY_GENERATION_FREQUENCY);
        if (generationFrequencyParam != null)
            generationFrequency.setText(generationFrequencyParam.toString());

        timeAcceleration = (TextView) findViewById(R.id.timeAcceleration);
        Parameter timeAccelerationParam = (timeAcceleration == null) ? null :
                currentSimulation.getGeneralParameter(Simulation.KEY_TIME_ACCELERATION);
        if (timeAccelerationParam != null)
            timeAcceleration.setText(timeAccelerationParam.toString());

        //TODO: datePicker for date part, TimePicker for time part, numeric edittext for millisecs
        simulatedDateTime = (TextView) findViewById(R.id.simulated_datetime);
        Parameter simulatedDateTimeParam = (simulatedDateTime == null) ? null :
                currentSimulation.getGeneralParameter(Simulation.KEY_SIMULATED_DATETIME);
        if (simulatedDateTimeParam != null)
            simulatedDateTime.setText(simulatedDateTimeParam.toString());

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_simulation_settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                boolean valid = true;
                String simulationName = this.simulationName.getText().toString().trim();
                String generationFrequency = this.generationFrequency.getText().toString().trim();
                String timeAcceleration = this.timeAcceleration.getText().toString().trim();

                if (TextUtils.isEmpty(simulationName)) {
                    this.simulationName.setError(getString(R.string.simulation_settings_must_be_filled));
                    return false;
                }
                if (TextUtils.isEmpty(generationFrequency)) {
                    this.generationFrequency.setError(getString(R.string.simulation_settings_must_be_filled));
                    return false;
                }
                if (TextUtils.isEmpty(timeAcceleration)) {
                    this.timeAcceleration.setError(getString(R.string.simulation_settings_must_be_filled));
                    return false;
                }

                Parameter simulationNameParam = currentSimulation.getGeneralParameter(Simulation.KEY_SIMULATION_NAME);
                if (simulationNameParam != null && simulationNameParam.getType() == Parameter.ParameterType.STRING) {
                    ((Parameter.ParamString) simulationNameParam).setString(simulationName);
                }

                Parameter generationFrequencyParam = currentSimulation.getGeneralParameter(Simulation.KEY_GENERATION_FREQUENCY);
                if (generationFrequencyParam != null && generationFrequencyParam.getType() == Parameter.ParameterType.LONG) {
                    ((Parameter.ParamLong) generationFrequencyParam).setValue(Long.parseLong(generationFrequency));
                }

                Parameter timeAccelerationParam = currentSimulation.getGeneralParameter(Simulation.KEY_TIME_ACCELERATION);
                if (timeAccelerationParam != null && timeAccelerationParam.getType() == Parameter.ParameterType.LONG) {
                    ((Parameter.ParamLong) timeAccelerationParam).setValue(Long.parseLong(timeAcceleration));
                }

                //on creation of new simulation
                if (currentSimulationKey == PhenGenMain.NEW_ELEMENT) {
                    PhenGenMain.getPhenGenMain().registrateSimulation(currentSimulation);
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

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        finish();
        return true;
    }


}

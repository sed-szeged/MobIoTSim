package sed.inf.u_szeged.hu.androidiotsimulator.activity.phenomena.simulation;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import sed.inf.u_szeged.hu.androidiotsimulator.R;
import sed.inf.u_szeged.hu.androidiotsimulator.activity.phenomena.pgdevice.PGDevicesActivity;
import sed.inf.u_szeged.hu.androidiotsimulator.activity.phenomena.population.PopulationsActivity;
import sed.inf.u_szeged.hu.androidiotsimulator.activity.phenomena.subject.SubjectsActivity;
import sed.inf.u_szeged.hu.androidiotsimulator.model.phenGenerator.general.PhenGenMain;
import sed.inf.u_szeged.hu.androidiotsimulator.model.phenGenerator.general.Simulation;

public class SimulationDashBoardActivity extends AppCompatActivity {

    private Simulation currentSimulation;
    private long currentSimulationKey;

    private TextView simulationName;

    //Requests with new activities to open (700 < request_code < 800)
    private static final int REQUESTCODE_GENERAL_SETTINGS   = 701;
    private static final int REQUESTCODE_POPULATIONS        = 702;
    private static final int REQUESTCODE_SUBJECTS           = 703;
    private static final int REQUESTCODE_DEVICES            = 704;
    private static final int REQUESTCODE_ATTACH_DEVICES     = 705;
    //Requests without activities (request_code > 800)
    private static final int REQUESTCODE_START_SIMULATION_AND_DEVICES   = 810;
    private static final int REQUESTCODE_STARTSTOP_SIMULATION           = 820;
    private static final int REQUESTCODE_STARTSTOP_DEVICES              = 830;
    private static final int REQUESTCODE_START_DEVICES                  = 831;
    private static final int REQUESTCODE_STOP_DEVICES                   = 832;
    private static final int REQUESTCODE_REG_SUBJECTS                   = 841;
    private static final int REQUESTCODE_REG_DEVICES                    = 842;
    private static final int REQUESTCODE_REG_ATTACHMENTS                = 843;
    private static final int REQUESTCODE_RESET_SIMULATION               = 860;
    private static final int REQUESTCODE_DESTROY_SIMULATION             = 870;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simulation_dash_board);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        currentSimulationKey = getIntent().getExtras().getLong(PhenGenMain.KEY_SIMULATION);
        currentSimulation = PhenGenMain.getPhenGenMain().getSimulationByKey(currentSimulationKey);

        simulationName = (TextView) findViewById(R.id.simulation_name);
        if (simulationName != null) simulationName.setText(currentSimulation.getName());

        TextView themeName = (TextView) findViewById(R.id.simulation_theme);
        if (themeName != null) themeName.setText(currentSimulation.theme.getName());

        ((Button) findViewById(R.id.simulation_generalSettings)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleAction(REQUESTCODE_GENERAL_SETTINGS);
            }
        });
        ((Button) findViewById(R.id.simulation_populations)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleAction(REQUESTCODE_POPULATIONS);
            }
        });
        ((Button) findViewById(R.id.simulation_subjects)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleAction(REQUESTCODE_SUBJECTS);
            }
        });
        ((Button) findViewById(R.id.simulation_devices)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleAction(REQUESTCODE_DEVICES);
            }
        });
        ((Button) findViewById(R.id.simulation_registrate_subjects)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleAction(REQUESTCODE_REG_SUBJECTS);
            }
        });
        ((Button) findViewById(R.id.simulation_registrate_devices)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleAction(REQUESTCODE_REG_DEVICES);
            }
        });

        setSwitchableButtons();

        ((Button) findViewById(R.id.simulation_startStop_simulation)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleAction(REQUESTCODE_STARTSTOP_SIMULATION);
            }
        });
        ((Button) findViewById(R.id.simulation_startStop_devices)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleAction(REQUESTCODE_STARTSTOP_DEVICES);
            }
        });
        ((Button) findViewById(R.id.simulation_destroySimulation)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleAction(REQUESTCODE_DESTROY_SIMULATION);
            }
        });
    }

    private void handleAction(int request_code) {
        if (request_code < 800) {
            Class activityToOpen = null;
            switch(request_code) {
                case REQUESTCODE_GENERAL_SETTINGS: activityToOpen = SimulationSettingsActivity.class; break;
                case REQUESTCODE_POPULATIONS:      activityToOpen = PopulationsActivity.class; break;
                case REQUESTCODE_SUBJECTS:         activityToOpen = SubjectsActivity.class; break;
                case REQUESTCODE_DEVICES:          activityToOpen = PGDevicesActivity.class; break;
            }
            if (activityToOpen != null) {
                Intent intent = new Intent(this, activityToOpen);
                Bundle bundle = new Bundle();
                bundle.putLong(PhenGenMain.KEY_SIMULATION, currentSimulationKey);
                bundle.putLong(PhenGenMain.KEY_POPULATION, PhenGenMain.ALL_ELEMENT);
                intent.putExtras(bundle);
                startActivityForResult(intent, request_code);
            }
        } else {    //request_code > 800
            switch (request_code) {
                case REQUESTCODE_REG_SUBJECTS:
                    if (currentSimulation.getRest_handler()==null) {
                        Toast.makeText(getApplicationContext(),R.string.rest_registration_subject_not_available,
                                Toast.LENGTH_LONG);
                    } else {
                        currentSimulation.getRest_handler().addSubjects(currentSimulation.getSubjects());
                    }
                    break;
                case REQUESTCODE_REG_DEVICES:
                    if (currentSimulation.getRest_handler()==null) {
                        Toast.makeText(getApplicationContext(),R.string.rest_registration_device_not_available,
                                Toast.LENGTH_LONG);
                    } else {
                        currentSimulation.getRest_handler().addDevices(currentSimulation.getDevices());
                    }
                    break;
                case REQUESTCODE_STARTSTOP_SIMULATION:
                    if (currentSimulation.isSimulationRunning()) currentSimulation.pauseSimulation();
                    else currentSimulation.startSimulation();
                    setSwitchableButtons();
                    break;
                case REQUESTCODE_STARTSTOP_DEVICES:
                    if (currentSimulation.areDevicesRunning()) currentSimulation.stopDevices();
                    else currentSimulation.startDevices();
                    setSwitchableButtons();
                    break;
                case REQUESTCODE_DESTROY_SIMULATION:
                    currentSimulation.destroySimulation();
                    finish();
            }
        }

    } //handleAction

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUESTCODE_GENERAL_SETTINGS && resultCode == Activity.RESULT_OK) {
            if (simulationName != null) simulationName.setText(currentSimulation.getName());
        }
    }

    private void setSwitchableButtons() {
        String startStopSimulationButtonCaption = currentSimulation.isSimulationRunning()
                ? getString(R.string.simulation_dashboard_startStop_simulation_STOP)
                : getString(R.string.simulation_dashboard_startStop_simulation_START);
        Button btnStartStopSimulation = (Button) findViewById(R.id.simulation_startStop_simulation);
        btnStartStopSimulation.setText(startStopSimulationButtonCaption);

        String startStopDevicesButtonCaption = currentSimulation.areDevicesRunning()
                ? getString(R.string.simulation_dashboard_startStop_devices_STOP)
                : getString(R.string.simulation_dashboard_startStop_devices_START);
        Button btnStartStopDevices = (Button) findViewById(R.id.simulation_startStop_devices);
        btnStartStopDevices.setText(startStopDevicesButtonCaption);

    }
}

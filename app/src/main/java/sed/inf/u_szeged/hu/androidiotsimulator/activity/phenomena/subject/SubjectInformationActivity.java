package sed.inf.u_szeged.hu.androidiotsimulator.activity.phenomena.subject;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
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

public class SubjectInformationActivity extends AppCompatActivity {

    public static final int EDIT_SUBJECT_REQ_CODE = 954;

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
        setContentView(R.layout.activity_subject_information);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        currentSimulationKey = getIntent().getExtras().getLong(PhenGenMain.KEY_SIMULATION);
        currentSimulation = PhenGenMain.getPhenGenMain().getSimulationByKey(currentSimulationKey);
        currentPopulationKey = getIntent().getExtras().getLong(PhenGenMain.KEY_POPULATION);
        currentPopulation = PhenGenMain.getPhenGenMain().getPopulationByKey(currentPopulationKey);
        currentSubjectKey = getIntent().getExtras().getLong(PhenGenMain.KEY_SUBJECT);
        currentSubject = PhenGenMain.getPhenGenMain().getSubjectByKey(currentSubjectKey);

        TextView simulationName = (TextView) findViewById(R.id.simulation_name);
            if (simulationName != null) simulationName.setText(currentSimulation.getName());
        TextView themeName = (TextView) findViewById(R.id.simulation_theme);
            if (themeName != null) themeName.setText(currentSimulation.theme.getName());
        TextView populationName = (TextView) findViewById(R.id.population_name);
            if (populationName != null) populationName.setText(currentPopulation.getPopulationName());
        TextView subjectName = (TextView) findViewById(R.id.subject_name);
            if (subjectName != null) subjectName.setText(currentSubject.getSubjectName());
        TextView subjectQuickInfo = (TextView) findViewById(R.id.subject_quickInfo);
            if (subjectQuickInfo != null) subjectQuickInfo.setText(currentSubject.getQuickInfo());

        ((Button) findViewById(R.id.btn_subject_settings)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editSubject();
            }
        });
        ((Button) findViewById(R.id.btn_subject_remove)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeSubject();
            }
        });

        //Add subject infos to the Activity
        subjectParameters.putAll(currentSubject.getIndividualInformation());
        for (Map.Entry<String, Parameter> entry : subjectParameters.entrySet()) {
            views.put(entry.getValue(),
                    ActivityUtility.addView(
                            (LinearLayout) findViewById(R.id.subjectInformation_list_container),
                            entry.getValue(),
                            entry.getKey(),
                            false
                    ));
        }//for

    }//onCreate

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        finish();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case EDIT_SUBJECT_REQ_CODE:
                recreate();
                break;
        }
    }

    private void editSubject() {
        Intent intent = new Intent(this, SubjectSettingsActivity.class);
        Bundle bundle = new Bundle();

        bundle.putLong(PhenGenMain.KEY_SIMULATION, currentSimulationKey);
        bundle.putLong(PhenGenMain.KEY_POPULATION, currentPopulationKey);
        bundle.putLong(PhenGenMain.KEY_SUBJECT,
                PhenGenMain.getPhenGenMain().getKeyBySubject(currentSubject));
        intent.putExtras(bundle);
        startActivityForResult(intent, EDIT_SUBJECT_REQ_CODE);
    }

    private void removeSubject() {
        currentSimulation.deleteSubject(currentSubject);
        finish();
    }
}

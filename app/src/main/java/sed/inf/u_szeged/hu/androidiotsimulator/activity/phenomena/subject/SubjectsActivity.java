package sed.inf.u_szeged.hu.androidiotsimulator.activity.phenomena.subject;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.List;

import sed.inf.u_szeged.hu.androidiotsimulator.R;
import sed.inf.u_szeged.hu.androidiotsimulator.model.phenGenerator.general.PhenGenMain;
import sed.inf.u_szeged.hu.androidiotsimulator.model.phenGenerator.general.Population;
import sed.inf.u_szeged.hu.androidiotsimulator.model.phenGenerator.general.Simulation;
import sed.inf.u_szeged.hu.androidiotsimulator.model.phenGenerator.general.Subject;

public class SubjectsActivity extends AppCompatActivity {

    public static final int SHOW_SUBJECTINFO_REQ_CODE = 952;
    public static final int ADD_SUBJECT_REQ_CODE = 953;

    private Simulation currentSimulation;
    private long currentSimulationKey;
    private Population currentPopulation;
    private long currentPopulationKey;

    private List<Subject> availableSubjects;
    private SubjectAdapter subjectAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subjects);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        currentSimulationKey = getIntent().getExtras().getLong(PhenGenMain.KEY_SIMULATION);
        currentSimulation = PhenGenMain.getPhenGenMain().getSimulationByKey(currentSimulationKey);

        currentPopulationKey = getIntent().getExtras().getLong(PhenGenMain.KEY_POPULATION);
        currentPopulation = PhenGenMain.getPhenGenMain().getPopulationByKey(currentPopulationKey);

        TextView simulationName = (TextView) findViewById(R.id.simulation_name);
        if (simulationName != null) simulationName.setText(currentSimulation.getName());

        TextView themeName = (TextView) findViewById(R.id.simulation_theme);
        if (themeName != null) themeName.setText(currentSimulation.theme.getName());

        TextView populationName = (TextView) findViewById(R.id.population_name);
        TextView populationSize = (TextView) findViewById(R.id.population_size);
        if (currentPopulation != null) {
            if (populationName != null) populationName.setText(currentPopulation.getPopulationName());
            if (populationSize != null) populationSize.setText("" + currentPopulation.getActualNumberOfSubjects());
        } else {
            if (populationName != null) populationName.setText(R.string.subjects_no_population_filter);
            if (populationSize != null) populationSize.setText("" + currentSimulation.getActualNumberOfSubjects());
        }


        ListView listView = (ListView) findViewById(R.id.subjects_lv);
        renewSubjectList();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                showSubjectInformation(subjectAdapter.getItem(position));
            }
        });



        FloatingActionButton new_subject_fab = (FloatingActionButton) findViewById(R.id.new_subject_fab);
        new_subject_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createNewSubject();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case SHOW_SUBJECTINFO_REQ_CODE:
                subjectAdapter.notifyDataSetChanged();
                break;
            case ADD_SUBJECT_REQ_CODE:
                if (resultCode == Activity.RESULT_OK) {
                    renewSubjectList();
                    subjectAdapter.notifyDataSetChanged();
                }
                break;
        }
    }

    private void createNewSubject() {
        if (currentPopulationKey != PhenGenMain.ALL_ELEMENT) {
            createNewSubject(currentPopulation);
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(SubjectsActivity.this);
            builder.setTitle(R.string.simulations_new_simulation_theme);

            ArrayAdapter<Population> adp = new ArrayAdapter<>(SubjectsActivity.this,
                    android.R.layout.simple_spinner_dropdown_item, currentSimulation.getPopulations());

            final Spinner sp = new Spinner(SubjectsActivity.this);
            sp.setAdapter(adp);
            builder.setView(sp);
            builder.setPositiveButton(R.string.ok_btn, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(SubjectsActivity.this, SubjectSettingsActivity.class);
                    Bundle bundle = new Bundle();
                    if (sp.getSelectedItem() != null) {
                        createNewSubject( (Population) sp.getSelectedItem());
                    }
                }
            });
            builder.setNegativeButton(R.string.cancel_btn, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    return; //don't create new subject
                }
            });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }
    }//createNewSubject()

    private void createNewSubject(Population population) {
        Intent intent = new Intent(this, SubjectSettingsActivity.class);
        Bundle bundle = new Bundle();

        bundle.putLong(PhenGenMain.KEY_SIMULATION, currentSimulationKey);
        bundle.putLong(PhenGenMain.KEY_POPULATION,
                PhenGenMain.getPhenGenMain().getKeyByPopulation(population));
        bundle.putLong(PhenGenMain.KEY_SUBJECT, PhenGenMain.NEW_ELEMENT);

        intent.putExtras(bundle);
        startActivityForResult(intent, ADD_SUBJECT_REQ_CODE);
    }

    private void showSubjectInformation(Subject selectedSubject) {
        Intent intent = new Intent(this, SubjectInformationActivity.class);
        Bundle bundle = new Bundle();

        bundle.putLong(PhenGenMain.KEY_SIMULATION, currentSimulationKey);
        bundle.putLong(PhenGenMain.KEY_POPULATION,
                PhenGenMain.getPhenGenMain().getKeyByPopulation(selectedSubject.getParentPopulation()));
        bundle.putLong(PhenGenMain.KEY_SUBJECT,
                PhenGenMain.getPhenGenMain().getKeyBySubject(selectedSubject));

        intent.putExtras(bundle);
        startActivityForResult(intent, SHOW_SUBJECTINFO_REQ_CODE);
    }

    private void renewSubjectList() {
        availableSubjects = currentPopulation != null
                ? PhenGenMain.getPhenGenMain().getSubjectListInPopulation(currentPopulationKey)
                : PhenGenMain.getPhenGenMain().getSubjectListInSimulation(currentSimulationKey);

        subjectAdapter = new SubjectAdapter(this,
                R.layout.listitem_subject, availableSubjects);

        ListView listView = (ListView) findViewById(R.id.subjects_lv);
        listView.setAdapter(subjectAdapter);
    }

}

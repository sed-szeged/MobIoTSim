package sed.inf.u_szeged.hu.androidiotsimulator.activity.phenomena.simulation;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;

import java.util.List;

import sed.inf.u_szeged.hu.androidiotsimulator.R;
import sed.inf.u_szeged.hu.androidiotsimulator.model.phenGenerator.general.PhenGenMain;
import sed.inf.u_szeged.hu.androidiotsimulator.model.phenGenerator.general.Simulation;
import sed.inf.u_szeged.hu.androidiotsimulator.model.phenGenerator.general.Theme;

public class SimulationsActivity extends AppCompatActivity {

    public static final int ADD_SIMULATION_REQ_CODE = 801;
    public static final int SHOW_SIMULATION_REQ_CODE = 802;

    private List<Simulation> availableSimulations;
    private SimulationAdapter simulationsAdapter;

    private PhenGenMain phenGen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phenomena_simulations);

        phenGen = PhenGenMain.getPhenGenMain();
        phenGen.setContext(getApplicationContext());
        if (!phenGen.isLoaded()) phenGen.loadDefaultSimulations();

        ListView listView = (ListView) findViewById(R.id.simulations_lv);
        renewSimulationList();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                showSimulation(simulationsAdapter.getItem(position));
            }
        });

        ((FloatingActionButton) findViewById(R.id.new_simulation_fab)).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                createNewSimulation();
            }
        });
    }

    private List<Simulation> getSimulationsFromJSON() {
        //TODO: maybe a filename is needed as parameter
        return null;
    }

    private List<Simulation> getSimulationFromDB() {
        //TODO: SQLite
        return null;
    }


    private void showSimulation(Simulation selectedSimulation) {
        Intent intent = new Intent(this, SimulationDashBoardActivity.class);
        Bundle bundle = new Bundle();

        bundle.putLong(PhenGenMain.KEY_SIMULATION,
                phenGen.getKeyBySimulation(selectedSimulation));

        intent.putExtras(bundle);
        startActivityForResult(intent, SHOW_SIMULATION_REQ_CODE);

        /*Toast.makeText(getApplicationContext(),
                selectedSimulation == null ? "---" :
                        selectedSimulation.getName() + " / " + phenGen.getKeyBySimulation(selectedSimulation),
                Toast.LENGTH_SHORT)
                .show();*/

    }

    private void createNewSimulation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(SimulationsActivity.this);
        builder.setTitle(R.string.simulations_new_simulation_theme);

        ArrayAdapter<Theme> adp = new ArrayAdapter<>(SimulationsActivity.this,
                android.R.layout.simple_spinner_dropdown_item, phenGen.getAvailableThemes());

        final Spinner sp = new Spinner(SimulationsActivity.this);
        sp.setAdapter(adp);
        builder.setView(sp);
        builder.setPositiveButton(R.string.ok_btn, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(SimulationsActivity.this, SimulationSettingsActivity.class);
                Bundle bundle = new Bundle();
                if (sp.getSelectedItem() != null) {
                    bundle.putString(PhenGenMain.KEY_THEME, ((Theme) sp.getSelectedItem()).getName());
                    bundle.putLong(PhenGenMain.KEY_SIMULATION, PhenGenMain.NEW_ELEMENT);
                    intent.putExtras(bundle);
                    startActivityForResult(intent, ADD_SIMULATION_REQ_CODE);
                }

                /*Toast.makeText(getApplicationContext(),
                        sp.getSelectedItem()==null ? "---" : ((Theme) sp.getSelectedItem()).getName(),
                        Toast.LENGTH_SHORT)
                        .show();*/
            }
        });
        builder.setNegativeButton(R.string.cancel_btn, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case SHOW_SIMULATION_REQ_CODE:
                simulationsAdapter.notifyDataSetChanged();
                break;
            case ADD_SIMULATION_REQ_CODE:
                if (resultCode == Activity.RESULT_OK) {
                    renewSimulationList();
                    simulationsAdapter.notifyDataSetChanged();
                }
                break;
        }
    }

    private void renewSimulationList() {
        availableSimulations = phenGen.getSimulationList(null); //There is currently no filter for simulations

        simulationsAdapter = new SimulationAdapter(this,
                R.layout.listitem_simulation, availableSimulations);

        ListView listView = (ListView) findViewById(R.id.simulations_lv);
        listView.setAdapter(simulationsAdapter);
    }
}
package sed.inf.u_szeged.hu.androidiotsimulator.activity.phenomena;

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
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.List;

import sed.inf.u_szeged.hu.androidiotsimulator.R;
import sed.inf.u_szeged.hu.androidiotsimulator.activity.phenomena.pgdevice.PGDeviceAdapter;
import sed.inf.u_szeged.hu.androidiotsimulator.activity.phenomena.pgdevice.PGDeviceSelectTypeAdapter;
import sed.inf.u_szeged.hu.androidiotsimulator.activity.phenomena.pgdevice.PGDeviceSettingsActivity;
import sed.inf.u_szeged.hu.androidiotsimulator.model.phenGenerator.general.PhenGenDevice;
import sed.inf.u_szeged.hu.androidiotsimulator.model.phenGenerator.general.PhenGenDeviceType;
import sed.inf.u_szeged.hu.androidiotsimulator.model.phenGenerator.general.PhenGenMain;
import sed.inf.u_szeged.hu.androidiotsimulator.model.phenGenerator.general.Simulation;

public class PGAttachDeviceToSubjectActivity extends AppCompatActivity {

    public static final int ADD_PGDEVICE_REQ_CODE = 1101;
    public static final int SHOW_PGDEVICE_REQ_CODE = 1102;

    private Simulation currentSimulation;
    private long currentSimulationKey;

    private List<PhenGenDevice> availablePGDevices;
    private PGDeviceAdapter pgDeviceAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pgdevices);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        currentSimulationKey = getIntent().getExtras().getLong(PhenGenMain.KEY_SIMULATION);
        currentSimulation = PhenGenMain.getPhenGenMain().getSimulationByKey(currentSimulationKey);

        TextView simulationName = (TextView) findViewById(R.id.simulation_name);
        if (simulationName != null) simulationName.setText(currentSimulation.getName());

        TextView themeName = (TextView) findViewById(R.id.simulation_theme);
        if (themeName != null) themeName.setText(currentSimulation.theme.getName());

        ListView listView = (ListView) findViewById(R.id.pgdevices_lv);
        renewPGDeviceList();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                showPGDevice(pgDeviceAdapter.getItem(position));
            }
        });

        FloatingActionButton new_pgdevice_fab = (FloatingActionButton) findViewById(R.id.new_pgdevice_fab);
        new_pgdevice_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createNewPGDevice();
            }
        });
    }

    private void renewPGDeviceList() {
        availablePGDevices = PhenGenMain.getPhenGenMain().getPGDeviceList(currentSimulationKey);

        pgDeviceAdapter = new PGDeviceAdapter(this,
                R.layout.listitem_pgdevice, availablePGDevices);

        ListView listView = (ListView) findViewById(R.id.pgdevices_lv);
        listView.setAdapter(pgDeviceAdapter);
    }

    private void showPGDevice(PhenGenDevice selectedPGDevice) {
        Intent intent = new Intent(this, PGDeviceSettingsActivity.class);
        Bundle bundle = new Bundle();

        bundle.putLong(PhenGenMain.KEY_SIMULATION, currentSimulationKey);
        bundle.putLong(PhenGenMain.KEY_PGDEVICE,
                PhenGenMain.getPhenGenMain().getKeyByPGDevice(selectedPGDevice));

        intent.putExtras(bundle);
        startActivityForResult(intent, SHOW_PGDEVICE_REQ_CODE);
    }

    private void createNewPGDevice() {
        AlertDialog.Builder builder = new AlertDialog.Builder(PGAttachDeviceToSubjectActivity.this);
        builder.setTitle(R.string.pgdevice_new_device_type);

        final List<PhenGenDeviceType> availableTypes = currentSimulation.theme.getPhenGenDeviceTypes();
        PGDeviceSelectTypeAdapter adp = new PGDeviceSelectTypeAdapter(PGAttachDeviceToSubjectActivity.this,
                R.layout.listitem_pgdevice_newdevice_type, availableTypes);

        final Spinner sp = new Spinner(PGAttachDeviceToSubjectActivity.this);
        sp.setId(View.generateViewId());
        sp.setAdapter(adp);
        builder.setView(sp);
        builder.setPositiveButton(R.string.ok_btn, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(PGAttachDeviceToSubjectActivity.this, PGDeviceSettingsActivity.class);
                Bundle bundle = new Bundle();
                //if (sp.getSelectedItem() != null) {
                    bundle.putLong(PhenGenMain.KEY_SIMULATION, currentSimulationKey);
                    bundle.putLong(PhenGenMain.KEY_PGDEVICE, PhenGenMain.NEW_ELEMENT);
                    bundle.putString( PhenGenMain.KEY_PGDEVICE_TYPE,
                            ((PhenGenDeviceType) availableTypes.get(sp.getSelectedItemPosition()))
                                    .getDeviceInnerType() );
                    intent.putExtras(bundle);
                    startActivityForResult(intent, ADD_PGDEVICE_REQ_CODE);
                //}

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
            case SHOW_PGDEVICE_REQ_CODE:
                pgDeviceAdapter.notifyDataSetChanged();
                break;
            case ADD_PGDEVICE_REQ_CODE:
                if (resultCode == Activity.RESULT_OK) {
                    renewPGDeviceList();
                    pgDeviceAdapter.notifyDataSetChanged();
                }
                break;
        }
    }

}

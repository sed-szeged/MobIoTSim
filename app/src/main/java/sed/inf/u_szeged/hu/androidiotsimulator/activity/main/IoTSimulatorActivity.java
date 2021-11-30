package sed.inf.u_szeged.hu.androidiotsimulator.activity.main;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.DocumentsContract;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;

import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import sed.inf.u_szeged.hu.androidiotsimulator.MobIoTApplication;
import sed.inf.u_szeged.hu.androidiotsimulator.R;
import sed.inf.u_szeged.hu.androidiotsimulator.activity.chart.ChartActivity;
import sed.inf.u_szeged.hu.androidiotsimulator.activity.cloud.CloudFragment;
import sed.inf.u_szeged.hu.androidiotsimulator.activity.device.DeviceSettingsActivity;
import sed.inf.u_szeged.hu.androidiotsimulator.activity.device.DevicesFragment;
import sed.inf.u_szeged.hu.androidiotsimulator.model.device.Device;
import sed.inf.u_szeged.hu.androidiotsimulator.model.device.DeviceGroup;
import sed.inf.u_szeged.hu.androidiotsimulator.model.device.SensorDataWrapper;
import sed.inf.u_szeged.hu.androidiotsimulator.model.gson.device.GsonDevice;

import static sed.inf.u_szeged.hu.androidiotsimulator.MobIoTApplication.KEY_DEVICES;
import static sed.inf.u_szeged.hu.androidiotsimulator.MobIoTApplication.getActivity;
import static sed.inf.u_szeged.hu.androidiotsimulator.activity.device.DevicesFragment.ADD_DEVICE_SETTINGS_REQ_CODE;
import static sed.inf.u_szeged.hu.androidiotsimulator.activity.device.DevicesFragment.EDIT_DEVICE_SETTINGS_REQ_CODE;
import static sed.inf.u_szeged.hu.androidiotsimulator.activity.device.DevicesFragment.IMPORT_DEVICE_REQ_CODE;
import static sed.inf.u_szeged.hu.androidiotsimulator.activity.device.DevicesFragment.MSG_W_CHART;
import static sed.inf.u_szeged.hu.androidiotsimulator.activity.device.DevicesFragment.MSG_W_DELETE;
import static sed.inf.u_szeged.hu.androidiotsimulator.activity.device.DevicesFragment.MSG_W_EDIT;
import static sed.inf.u_szeged.hu.androidiotsimulator.activity.device.DevicesFragment.MSG_W_SWITCH;
import static sed.inf.u_szeged.hu.androidiotsimulator.activity.device.DevicesFragment.MSG_W_WARNING;
import static sed.inf.u_szeged.hu.androidiotsimulator.activity.device.DevicesFragment.deviceGroupAdapter;
import static sed.inf.u_szeged.hu.androidiotsimulator.activity.device.DevicesFragment.deviceGroupList;

import static sed.inf.u_szeged.hu.androidiotsimulator.activity.device.DevicesFragment.saveDevicesToPrefs;

public class IoTSimulatorActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private DrawerLayout drawer;
    public static HashMap<String,String> deviceKeysBuffer = new HashMap<>();
    Gson gson = new Gson();

    public Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_W_WARNING:
                    System.out.println("msg warning");
                    warning(msg.getData());
                    break;
                case MSG_W_EDIT:
                    System.out.println("msg edit");
                    edit(msg.arg1);
                    break;
                case MSG_W_DELETE:
                    System.out.println("msg delete");
                    delete(msg.arg1);
                    break;
                case MSG_W_SWITCH:
                    System.out.println("msg switch");
                    switchIt();
                    break;
                case MSG_W_CHART:
                    System.out.println("msg Chart ");
                    Intent intent = new Intent(getActivity(), ChartActivity.class);
                    intent.putExtra("position", msg.arg1);
                    startActivity(intent);
                    break;
            }
        }
    };
    private void warning(Bundle data) {
        int count = deviceGroupAdapter.getCount();
        for (int i = 0; i < count; i++) {
            DeviceGroup deviceGroup = deviceGroupAdapter.getItem(i);
            for (Device d : deviceGroup.getDeviceGroup()) {
               if (d.getDeviceID().equals(data.get(DeviceSettingsActivity.KEY_DEVICE_ID))) {
                    d.setWarning(true);
                    break;
               }
            }
        }
        deviceGroupAdapter.notifyDataSetInvalidated();
    }

    private void switchIt() {
        deviceGroupAdapter.notifyDataSetInvalidated();
    }

    private void delete(int position) {

        System.out.println("DEVICES_STR IN delete before actual delete: " + MobIoTApplication.loadData(MobIoTApplication.KEY_DEVICES));
        System.out.println("DELETE " + deviceGroupAdapter.getItem(position));
        DeviceGroup deviceGroup = deviceGroupList.get(position);

        deviceGroup.removeFromCloud();
        deviceGroupList.remove(position);
        deviceGroupAdapter.notifyDataSetChanged();
        System.out.println("DEVICES_STR IN delete: " + MobIoTApplication.loadData(MobIoTApplication.KEY_DEVICES+CloudFragment.account.getId()));
        saveDevicesToPrefs();
        for(Device d: deviceGroup.getDeviceGroup()){
            d.stop(IoTSimulatorActivity.this);
            String devId = d.getDeviceID();
            deviceKeysBuffer.remove(devId);
            MobIoTApplication.deleteDeviceId(devId);
        }
    }
    private void importDeviceActivityResult(Intent data) {
        if (data != null) {
            // Get the URI of the selected file
            final Uri uri = data.getData();
            System.out.println("Uri = " + uri.toString());
            try {
                // Get the file path from the URI
                final String path = getRealPathFromURI(uri);
                Toast.makeText(getActivity(),
                    "Device imported: " + path, Toast.LENGTH_LONG).show();

                System.out.println(MobIoTApplication.getStringFromFile(path));

                String jsonStr = MobIoTApplication.getStringFromFile(path);
                GsonDevice obj = gson.fromJson(jsonStr, GsonDevice.class);

                Device importedBaseDevice = Device.fromJson(obj);
                DeviceGroup deviceGroup = new DeviceGroup(importedBaseDevice, getActivity().getApplicationContext());
                deviceGroup.uploadDeviceGroup(this);
                deviceGroupList.add(deviceGroup);
                deviceGroupAdapter.notifyDataSetChanged();
                saveDevicesToPrefs();

            } catch (Exception e) {
                System.out.println("DeviceSettingsActivity" + " File select error" + e);
            }
        }
    }

    private String getRealPathFromURI(Uri contentURI) {
        final String docId = DocumentsContract.getDocumentId(contentURI);
        final String[] split = docId.split(":");
        return Environment.getExternalStorageDirectory() + "/" + split[1];
    }
    private boolean editDeviceActivityResult(Intent data) {
        Bundle bundle = data.getExtras();
        if (bundle != null) {
            System.out.println("onActivityResult edit bundle " + bundle.toString());
            Integer position = Integer.parseInt(bundle.getString(DeviceSettingsActivity.KEY_EDIT_IT));

            Device editedDevice = getDeviceFromBundle(bundle); //device kerül be (deviceId: MobIoTSimDevice01 és egyéb adatok az űrlapról, új adatok)
            Device originalDevice = deviceGroupList.get(position).getBaseDevice();

            if (deviceGroupList.get(position).getBaseDevice().equals(editedDevice)) { //basedevice tartalmazza az eredeti adatokat, MobIoTSimDevice02_001
                System.out.println("NOT Edited");
                return true;
            }

            deviceGroupList.get(position).updateDeviceGroup(editedDevice,this );
            deviceGroupList.get(position).updateDeviceKeysInSharedPreferences(originalDevice, editedDevice,IoTSimulatorActivity.this);

            deviceGroupList.get(position).getBaseDevice().setDeviceID(editedDevice.getDeviceID());
            deviceGroupList.get(position).getBaseDevice().setPassword(editedDevice.getPassword());
            deviceGroupList.get(position).getBaseDevice().setTopics(editedDevice.getTopics());
            deviceGroupList.get(position).getBaseDevice().setType(editedDevice.getType());
            deviceGroupList.get(position).getBaseDevice().setHasTraceData(true);
            deviceGroupList.get(position).getBaseDevice().setTraceFileLocation(editedDevice.getTraceFileLocation());
            deviceGroupList.get(position).getBaseDevice().setSensors(editedDevice.getSensors());
            deviceGroupList.get(position).getBaseDevice().setFreq(editedDevice.getFreq());

            deviceGroupAdapter.notifyDataSetChanged();
            saveDevicesToPrefs();
        } else {
            System.out.println("onActivityResult edit bundle NULL");
        }
        return false;
    }

    private void addDeviceActivityResult(Intent data) {
        Bundle bundle = data.getExtras();
        if (bundle != null) {
            System.out.println("onActivityResult add bundle " + bundle.toString());

            Device device = getDeviceFromBundle(bundle);
            DeviceGroup group = new DeviceGroup(device, getActivity().getApplicationContext());
            group.uploadDeviceGroup(this);
            System.out.println("Add deviceGroupList: " + device.toString());
            deviceGroupAdapter.add(group);


            deviceGroupAdapter.notifyDataSetChanged();
            saveDevicesToPrefs();
        } else {
            System.out.println("onActivityResult add bundle NULL");
        }
    }

    @NonNull
    private Device getDeviceFromBundle(Bundle bundle) {
        return new Device(
                //bundle.getString(DeviceSettingsActivity.KEY_ORGANIZATION_ID),
                //bundle.getString(DeviceSettingsActivity.KEY_TYPE_ID),
                bundle.getString(DeviceSettingsActivity.KEY_DEVICE_ID),
                bundle.getString(DeviceSettingsActivity.KEY_PASSWORD),
                bundle.getString(DeviceSettingsActivity.KEY_TOPICS),
                bundle.getString(DeviceSettingsActivity.KEY_TOKEN),
                bundle.getString(DeviceSettingsActivity.KEY_TYPE),
                Double.parseDouble(bundle.getString(DeviceSettingsActivity.KEY_FREQ)),
                SensorDataWrapper.sensorDataFromSerial(bundle.getString(DeviceSettingsActivity.KEY_SENSORS)),
                bundle.getString(DeviceSettingsActivity.KEY_TRACE_LOCATION),
                Integer.parseInt(bundle.getString(DeviceSettingsActivity.KEY_NUM_OF_DEVICES)),
                Boolean.parseBoolean(bundle.getString(DeviceSettingsActivity.KEY_SAVE_TRACE)),
                bundle.getString(DeviceSettingsActivity.KEY_USER_ID));
    }

    public void edit(int i) {
        Intent intent = new Intent(getActivity(), DeviceSettingsActivity.class);
        Bundle bundle = new Bundle();

        //bundle.putString(DeviceSettingsActivity.KEY_ORGANIZATION_ID,deviceGroupAdapter.getItem(i).getBaseDevice().getOrganizationID());
        //bundle.putString(DeviceSettingsActivity.KEY_TYPE_ID, deviceGroupAdapter.getItem(i).getBaseDevice().getTypeID());
        bundle.putString(DeviceSettingsActivity.KEY_DEVICE_ID, deviceGroupAdapter.getItem(i).getBaseDevice().getDeviceID());
        bundle.putString(DeviceSettingsActivity.KEY_PASSWORD, deviceGroupAdapter.getItem(i).getBaseDevice().getPassword());
        bundle.putString(DeviceSettingsActivity.KEY_TOPICS, deviceGroupAdapter.getItem(i).getBaseDevice().getTopics());
        bundle.putString(DeviceSettingsActivity.KEY_TOKEN, deviceGroupAdapter.getItem(i).getBaseDevice().getToken());
        bundle.putString(DeviceSettingsActivity.KEY_TYPE, deviceGroupAdapter.getItem(i).getBaseDevice().getType());
        bundle.putString(DeviceSettingsActivity.KEY_FREQ, String.valueOf(deviceGroupAdapter.getItem(i).getBaseDevice().getFreq()));
        bundle.putString(DeviceSettingsActivity.KEY_SENSORS, String.valueOf(deviceGroupAdapter.getItem(i).getBaseDevice().getSensors()));
        bundle.putString(DeviceSettingsActivity.KEY_EDIT_IT, String.valueOf(i));
        bundle.putString(DeviceSettingsActivity.KEY_TRACE_LOCATION, deviceGroupAdapter.getItem(i).getBaseDevice().getTraceFileLocation());
        bundle.putString(DeviceSettingsActivity.KEY_NUM_OF_DEVICES, String.valueOf(deviceGroupAdapter.getItem(i).getBaseDevice().getNumOfDevices()));
        bundle.putString(DeviceSettingsActivity.KEY_SAVE_TRACE, String.valueOf(deviceGroupAdapter.getItem(i).getBaseDevice().isSaveTrace()));
        bundle.putString("title", getString(R.string.title_activity_device_settings_edit)); //set title of toolbar: Edit Device
        intent.putExtras(bundle);
        startActivityForResult(intent, EDIT_DEVICE_SETTINGS_REQ_CODE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //SIGN-IN
        /*
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        */
        MobIoTApplication.setActivity(this);
        checkInternetConnection();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        if (savedInstanceState == null) {
            getSupportActionBar().setTitle(getString(R.string.devices));
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                new DevicesFragment()).commit();

            navigationView.setCheckedItem(R.id.nav_devices);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
       switch(item.getItemId()){
           case R.id.nav_cloud:
               getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,new CloudFragment()).commit();
               getSupportActionBar().setTitle(getString(R.string.cloud));
               break;
           case R.id.nav_devices:
               getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,new DevicesFragment()).commit();
               getSupportActionBar().setTitle(getString(R.string.devices));
               break;
           case R.id.nav_info:
               getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,new InformationFragment()).commit();
               getSupportActionBar().setTitle(getString(R.string.information));
               break;
           case R.id.nav_login:
               getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,new SignInButtonActivity()).commit();
               getSupportActionBar().setTitle(getString(R.string.information));
               break;
       }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ADD_DEVICE_SETTINGS_REQ_CODE) {
            if (resultCode == RESULT_OK) {
                addDeviceActivityResult(data);
            }
        }

        if (requestCode == EDIT_DEVICE_SETTINGS_REQ_CODE) {//Editsave
            if (resultCode == RESULT_OK) {
                if (editDeviceActivityResult(data)) return;
            }
        }

        if (requestCode == IMPORT_DEVICE_REQ_CODE) {
            if (resultCode == RESULT_OK) {
                importDeviceActivityResult(data);
            }
        }

    }
    private void checkInternetConnection() {
        ConnectivityManager conMgr =  (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = conMgr.getActiveNetworkInfo();
        if (netInfo == null){
            new AlertDialog.Builder(this)
                    .setTitle(getResources().getString(R.string.error))
                    .setMessage(getResources().getString(R.string.internet_error))
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            closeActivity();
                        }
                    }).show();
        }
    }

    private void closeActivity() {
        this.finish();
    }
}


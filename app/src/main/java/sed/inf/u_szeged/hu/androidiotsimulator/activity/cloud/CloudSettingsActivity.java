package sed.inf.u_szeged.hu.androidiotsimulator.activity.cloud;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.Arrays;
import java.util.List;

import sed.inf.u_szeged.hu.androidiotsimulator.R;
import sed.inf.u_szeged.hu.androidiotsimulator.model.cloudsettings.CloudSettingsWrapper;

public class CloudSettingsActivity extends AppCompatActivity {

    public static final String KEY_TYPE = "TYPE";
    public static final String KEY_NAME = "NAME";
    public static final String KEY_ORGANIZATION_ID = "ORGANIZATION_ID";
    public static final String KEY_APPLICATION_ID = "APPLICATION_ID";
    public static final String KEY_AUTH_KEY = "AUTH_KEY";
    public static final String KEY_AUTH_TOKEN = "AUTH_TOKEN";
    public static final String KEY_CONNECTION_TYPE = "CONNECTION_TYPE";
    public static final String KEY_EVENT_ID = "EVENT_ID";
    public static final String KEY_COMMAND_ID = "COMMAND_ID";
    public static final String KEY_EDIT_IT = "EDIT_IT";
    public static final String KEY_CONNECTION_PROTOCOL = "CONNECTION_PROTOCOL";

    public static final String KEY_PORT = "PORT_NUMBER";
    public static final String KEY_BROKER_URL = "BROKER_URL";
    public static final String KEY_BROKER_URL_PREFIX = "BROKER_URL_PREFIX";
    public static final String KEY_BROKER_URL_HOST = "BROKER_URL_HOST";

    String editId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cloud_settings);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        init();
    }

    private void init() {
        Bundle bundle = getIntent().getExtras();
        editId = bundle.getString(KEY_EDIT_IT);
        setData(bundle);

//        findViewById(R.id.ok_btn).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent();
//                intent.putExtras(getData());
//                setResult(Activity.RESULT_OK, intent);
//                finish();
//            }
//        });
//
//        findViewById(R.id.cancel_btn).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                finish();
//            }
//        });

        Spinner spinner = (Spinner) findViewById(R.id.connection_protocol_spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.connection_types, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);

        ((Button) findViewById(R.id.generate_url)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String portNumStr = ((EditText) findViewById(R.id.show_url_et_port)).getText().toString();
                if (portNumStr.isEmpty()) portNumStr = "0";
                ((EditText) findViewById(R.id.show_url_et)).setText(CloudSettingsWrapper.createURL(
                        ((EditText) findViewById(R.id.show_url_et_prefix)).getText().toString(),
                        ((TextView) findViewById(R.id.type)).getText().toString(),
                        ((EditText) findViewById(R.id.organization_id_et)).getText().toString(),
                        ((EditText) findViewById(R.id.show_url_et_host)).getText().toString(),
                        Integer.parseInt(portNumStr)
                ));
            }
        });
    }

    private Bundle getData() {
        Bundle bundle = new Bundle();

        String type = ((TextView) findViewById(R.id.type)).getText().toString();
        String name = ((EditText) findViewById(R.id.name_et)).getText().toString();
        String organizationId = ((EditText) findViewById(R.id.organization_id_et)).getText().toString();
        String appid = ((EditText) findViewById(R.id.app_id_et)).getText().toString();
        String authkey = ((EditText) findViewById(R.id.key_et)).getText().toString();
        String authtoken = ((EditText) findViewById(R.id.token_et)).getText().toString();
        String commandId = ((EditText) findViewById(R.id.command_id_et)).getText().toString();
        String eventId = ((EditText) findViewById(R.id.event_id_et)).getText().toString();
        Spinner spinner = (Spinner) findViewById(R.id.connection_protocol_spinner);
        int port = Integer.parseInt( ((EditText) findViewById(R.id.show_url_et_port)).getText().toString() );
        String broker_scheme = ((EditText) findViewById(R.id.show_url_et_prefix)).getText().toString();
        String broker_host = ((EditText) findViewById(R.id.show_url_et_host)).getText().toString();
        String broker_url = ((EditText) findViewById(R.id.show_url_et)).getText().toString();

        bundle.putString(KEY_TYPE, type);
        bundle.putString(KEY_NAME, name);
        bundle.putString(KEY_ORGANIZATION_ID, organizationId);
        bundle.putString(KEY_APPLICATION_ID, appid);

        bundle.putString(KEY_AUTH_KEY, authkey);
        bundle.putString(KEY_AUTH_TOKEN, authtoken);
        bundle.putString(KEY_COMMAND_ID, commandId);
        bundle.putString(KEY_EVENT_ID, eventId);
        bundle.putString(KEY_CONNECTION_PROTOCOL, spinner.getSelectedItem().toString());

        bundle.putInt(KEY_PORT, port);
        bundle.putString(KEY_BROKER_URL_PREFIX, broker_scheme);
        bundle.putString(KEY_BROKER_URL_HOST, broker_host);
        bundle.putString(KEY_BROKER_URL, broker_url);

        if (editId != null) {
            bundle.putString(KEY_EDIT_IT, editId);
        }

        return bundle;
    }

    private void setData(Bundle bundle) {
        String type = bundle.getString(KEY_TYPE);
        if (type != null) {
            ((TextView) findViewById(R.id.type)).setText(type);
        }

        String name = bundle.getString(KEY_NAME);
        if (name != null) {
            ((EditText) findViewById(R.id.name_et)).setText(name);
        }

        String organizationId = bundle.getString(KEY_ORGANIZATION_ID);
        if (organizationId != null) {
            ((EditText) findViewById(R.id.organization_id_et)).setText(organizationId);
        }

        String appId = bundle.getString(KEY_APPLICATION_ID);
        if (appId != null) {
            ((EditText) findViewById(R.id.app_id_et)).setText(appId);
        }

        String authToken = bundle.getString(KEY_AUTH_TOKEN);
        if (authToken != null) {
            ((EditText) findViewById(R.id.token_et)).setText(authToken);
        }

        String authKey = bundle.getString(KEY_AUTH_KEY);
        if (authKey != null) {
            ((EditText) findViewById(R.id.key_et)).setText(authKey);
        }

        String commandId = bundle.getString(KEY_COMMAND_ID);
        if (commandId != null) {
            ((EditText) findViewById(R.id.command_id_et)).setText(commandId);
        }

        String eventId = bundle.getString(KEY_EVENT_ID);
        if (eventId != null) {
            ((EditText) findViewById(R.id.event_id_et)).setText(eventId);
        }

        String connectionType = bundle.getString(KEY_CONNECTION_PROTOCOL);
        if (connectionType != null) {
            List<String> connectionTypeItems = Arrays.asList(getResources().getStringArray(R.array.connection_types));
            int position = connectionTypeItems.indexOf(connectionType);
            ((Spinner) findViewById(R.id.connection_protocol_spinner)).setSelection(position);

        }

        String portNumber = "" + bundle.getInt(KEY_PORT);
        ((EditText) findViewById(R.id.show_url_et_port)).setText(portNumber);

        String broker_url_prefix = bundle.getString(KEY_BROKER_URL_PREFIX);
        if (broker_url_prefix != null) {
            ((EditText) findViewById(R.id.show_url_et_prefix)).setText(broker_url_prefix);
        }

        String broker_url_host = bundle.getString(KEY_BROKER_URL_HOST);
        if (broker_url_host != null) {
            ((EditText) findViewById(R.id.show_url_et_host)).setText(broker_url_host);
        }

        String broker_url_full = bundle.getString(KEY_BROKER_URL);
        if (broker_url_full != null) {
            ((EditText) findViewById(R.id.show_url_et)).setText(broker_url_full);
        }

    }

    private boolean areFieldsFilled() {
        boolean result = true;
        if (((EditText) findViewById(R.id.name_et)).getText().toString().equals("")) {
            ((EditText) findViewById(R.id.name_et)).setError(getString(R.string.empty_field));
            result = false;
        }

        if (((EditText) findViewById(R.id.organization_id_et)).getText().toString().equals("")) {
            ((EditText) findViewById(R.id.organization_id_et)).setError(getString(R.string.empty_field));
            result = false;
        }

        if (((EditText) findViewById(R.id.app_id_et)).getText().toString().equals("")) {
            ((EditText) findViewById(R.id.app_id_et)).setError(getString(R.string.empty_field));
            result = false;
        }

        if (((EditText) findViewById(R.id.key_et)).getText().toString().equals("")) {
            ((EditText) findViewById(R.id.key_et)).setError(getString(R.string.empty_field));
            result = false;
        }

        if (((EditText) findViewById(R.id.token_et)).getText().toString().equals("")) {
            ((EditText) findViewById(R.id.token_et)).setError(getString(R.string.empty_field));
            result = false;
        }

        if (((EditText) findViewById(R.id.command_id_et)).getText().toString().equals("")) {
            ((EditText) findViewById(R.id.command_id_et)).setError(getString(R.string.empty_field));
            result = false;
        }

        if (((EditText) findViewById(R.id.event_id_et)).getText().toString().equals("")) {
            ((EditText) findViewById(R.id.event_id_et)).setError(getString(R.string.empty_field));
            result = false;
        }

        if (((EditText) findViewById(R.id.show_url_et_port)).getText().toString().equals("")) {
            ((EditText) findViewById(R.id.show_url_et_port)).setError(getString(R.string.empty_field));
            result = false;
        }

        if (((EditText) findViewById(R.id.show_url_et_prefix)).getText().toString().equals("")) {
            ((EditText) findViewById(R.id.show_url_et_prefix)).setError(getString(R.string.empty_field));
            result = false;
        }

        if (((EditText) findViewById(R.id.show_url_et_host)).getText().toString().equals("")) {
            ((EditText) findViewById(R.id.show_url_et_host)).setError(getString(R.string.empty_field));
            result = false;
        }

        if (((EditText) findViewById(R.id.show_url_et)).getText().toString().equals("")) {
            ((EditText) findViewById(R.id.show_url_et)).setError(getString(R.string.empty_field));
            result = false;
        }

        return result;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_cloud_settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_save) {
            if (areFieldsFilled()) {
                Intent intent = new Intent();
                intent.putExtras(getData());
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
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

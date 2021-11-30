package sed.inf.u_szeged.hu.androidiotsimulator.activity.cloud;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

//import org.apache.http.*;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import sed.inf.u_szeged.hu.androidiotsimulator.R;

public class CloudSettingsActivity extends AppCompatActivity  {

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

    public static String KEY_GOOGLE_USERNAME = "GOOGLE_USERNAME";
    public static String KEY_GOOGLE_TOKEN = "GOOGLE_TOKEN";

    String editId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cloud_settings);

        Toolbar toolbar = findViewById(R.id.toolbar);
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
        getSupportActionBar().setTitle(bundle.getString("title"));
        //Toroltem innen egy eredetileg is kikommentezett reszt

        Spinner spinner = (Spinner) findViewById(R.id.connection_protocol_spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.connection_types, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);

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
        Spinner spinner = findViewById(R.id.connection_protocol_spinner);

        bundle.putString(KEY_TYPE, type);
        bundle.putString(KEY_NAME, name);
        bundle.putString(KEY_ORGANIZATION_ID, organizationId);
        bundle.putString(KEY_APPLICATION_ID, appid);

        bundle.putString(KEY_AUTH_KEY, authkey);
        bundle.putString(KEY_AUTH_TOKEN, authtoken);
        bundle.putString(KEY_COMMAND_ID, commandId);
        bundle.putString(KEY_EVENT_ID, eventId);
        bundle.putString(KEY_CONNECTION_PROTOCOL, spinner.getSelectedItem().toString());

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


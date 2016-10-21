package sed.inf.u_szeged.hu.androidiotsimulator.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import sed.inf.u_szeged.hu.androidiotsimulator.R;

public class CloudSettingsActivity extends AppCompatActivity {

    public static final String KEY_TYPE = "TYPE";
    public static final String KEY_NAME = "NAME";
    public static final String KEY_ORGATNISATION_ID = "ORGATNISATION_ID";
    public static final String KEY_APPLICATION_ID = "APPLICATION_ID";
    public static final String KEY_AUTH_KEY = "AUTH_KEY";
    public static final String KEY_AUTH_TOKEN = "AUTH_TOKEN";
    public static final String KEY_CONNECTION_TYPE = "CONNECTION_TYPE";
    public static final String KEY_EDIT_IT = "EDIT_IT";

    String editId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cloud_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        init();
    }

    private void init() {

        Bundle bundle = getIntent().getExtras();
        editId = bundle.getString(KEY_EDIT_IT);
        setData(bundle);

        findViewById(R.id.ok_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent();
                intent.putExtras(getData());
                setResult(Activity.RESULT_OK, intent);
                finish();

            }
        });

        findViewById(R.id.cancel_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Spinner spinner = (Spinner) findViewById(R.id.connection_type_spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.connection_types, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String item = (String) parent.getItemAtPosition(position);
                //Toast.makeText(CloudActivity.this, "Selected: " + item, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    private Bundle getData() {
        Bundle bundle = new Bundle();

        String type = ((TextView)findViewById(R.id.type)).getText().toString();
        String name = ((EditText)findViewById(R.id.name_et)).getText().toString();
        String organizationId = ((EditText)findViewById(R.id.organizatio_id_et)).getText().toString();
        String appid = ((EditText)findViewById(R.id.app_id_et)).getText().toString();
        String authkey = ((EditText)findViewById(R.id.key_et)).getText().toString();
        String authtoken = ((EditText)findViewById(R.id.token_et)).getText().toString();

        bundle.putString(KEY_TYPE, type);
        bundle.putString(KEY_NAME, name);
        bundle.putString(KEY_ORGATNISATION_ID, organizationId);
        bundle.putString(KEY_APPLICATION_ID, appid);

        bundle.putString(KEY_AUTH_KEY, authkey);
        bundle.putString(KEY_AUTH_TOKEN, authtoken);


        if(editId != null) {
            bundle.putString(KEY_EDIT_IT, editId);
        }

        return bundle;
    }

    private void setData(Bundle bundle) {
        String type = bundle.getString(KEY_TYPE);
        if(type != null) {
            ((TextView) findViewById(R.id.type)).setText(type);
        }

        String name = bundle.getString(KEY_NAME);
        if(name != null) {
            ((EditText) findViewById(R.id.name_et)).setText(name);
        }

        String organizationId = bundle.getString(KEY_ORGATNISATION_ID);
        if(organizationId != null) {
            ((EditText) findViewById(R.id.organizatio_id_et)).setText(organizationId);
        }

        if(type != null && !type.equals("BLUEMIX")){

                ((EditText) findViewById(R.id.app_id_et)).setVisibility(View.GONE);
                ((EditText) findViewById(R.id.token_et)).setVisibility(View.GONE);
                ((EditText) findViewById(R.id.key_et)).setVisibility(View.GONE);

        }else{
            String appId = bundle.getString(KEY_APPLICATION_ID);
            if(appId != null) {
                ((EditText) findViewById(R.id.app_id_et)).setText(appId);
            }

            String authToken = bundle.getString(KEY_AUTH_TOKEN);
            if(authToken != null) {
                ((EditText) findViewById(R.id.token_et)).setText(authToken);
            }

            String authKey = bundle.getString(KEY_AUTH_KEY);
            if(authKey != null) {
                ((EditText) findViewById(R.id.key_et)).setText(authKey);
            }
        }

    }

}

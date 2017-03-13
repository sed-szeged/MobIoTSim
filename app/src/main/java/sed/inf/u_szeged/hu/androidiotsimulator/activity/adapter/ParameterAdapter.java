package sed.inf.u_szeged.hu.androidiotsimulator.activity.adapter;

import android.content.Context;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.List;

import sed.inf.u_szeged.hu.androidiotsimulator.MobIoTApplication;
import sed.inf.u_szeged.hu.androidiotsimulator.R;
import sed.inf.u_szeged.hu.androidiotsimulator.activity.device.DeviceSettingsActivity;
import sed.inf.u_szeged.hu.androidiotsimulator.model.device.SensorData;

/**
 * Created by tommy on 11/13/2016. Project name: MobIoTSim-mirrored
 *  
 */

public class ParameterAdapter extends ArrayAdapter<SensorData> implements View.OnClickListener {

    private List<SensorData> dataSet;
    private Context mContext;

    public ParameterAdapter(List<SensorData> data, Context context) {
        super(context, R.layout.parameter_layout, data);
        this.dataSet = data;
        this.mContext = context;

    }

    @Override
    public void onClick(View view) {

    }

    public int getCount() {
        return dataSet.size();
    }

    public long getItemId(int position) {
        return position;
    }

    public List<SensorData> getResult() {
        return dataSet;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        SensorData dataModel = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder; // view lookup cache stored in tag

        if (convertView == null) {

            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.parameter_layout, parent, false);
            viewHolder.paramName = (EditText) convertView.findViewById(R.id.param_name);
            viewHolder.maxInput = (EditText) convertView.findViewById(R.id.param_max);
            viewHolder.minInput = (EditText) convertView.findViewById(R.id.param_min);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }


        final Button deleteBtn = (Button) convertView.findViewById(R.id.remove_param_btn);
        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("deleteBtn clicked " + position);

                if (MobIoTApplication.getActivity() instanceof DeviceSettingsActivity) {
                    DeviceSettingsActivity deviceSettingsActivity = (DeviceSettingsActivity) MobIoTApplication.getActivity();
                    Message message = new Message();
                    message.what = DeviceSettingsActivity.MSG_W_DELETE_PARAMETER;
                    message.arg1 = position;
                    message.setTarget(deviceSettingsActivity.handler);

                    message.sendToTarget();
                }
            }
        });

        viewHolder.paramName.setText(dataModel.getName());
        viewHolder.maxInput.setText(dataModel.getMaxValue());
        viewHolder.minInput.setText(dataModel.getMinValue());

        viewHolder.paramName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    final EditText paramName = (EditText) v;
                    dataSet.get(position).setName(paramName.getText().toString());
                }
            }
        });


        viewHolder.maxInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    final EditText maxValue = (EditText) v;
                    dataSet.get(position).setMaxValue(maxValue.getText().toString());
                }
            }
        });

        viewHolder.minInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    final EditText minValue = (EditText) v;
                    dataSet.get(position).setMinValue(minValue.getText().toString());
                }
            }
        });

        // Return the completed view to render on screen
        return convertView;
    }

    private static class ViewHolder {
        TextView paramName;
        EditText maxInput;
        EditText minInput;
    }


}

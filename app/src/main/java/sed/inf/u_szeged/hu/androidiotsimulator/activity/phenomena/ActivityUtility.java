package sed.inf.u_szeged.hu.androidiotsimulator.activity.phenomena;

import android.content.Context;
import android.content.res.Resources;
import android.text.InputType;
import android.text.Layout;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import sed.inf.u_szeged.hu.androidiotsimulator.model.phenGenerator.general.Parameter;
import sed.inf.u_szeged.hu.androidiotsimulator.model.phenGenerator.packHC.HC_Population_Persons;

public class ActivityUtility {

    public static View addView(LinearLayout parent, Parameter parameter, String parameterKey) {
        return addView(parent, parameter, parameterKey, true);
    }

    public static View addView(LinearLayout parent, Parameter parameter, String parameterKey, boolean isEnabled) {

        //container
        LinearLayout container = new LinearLayout(parent.getContext());
            container.setLayoutParams(new LayoutParams(
                    LayoutParams.MATCH_PARENT,
                    LayoutParams.WRAP_CONTENT
            ));
            container.setOrientation(LinearLayout.HORIZONTAL);
            container.setPadding(5, 20, 5, 10);

        //Caption
        TextView caption = new TextView(parent.getContext());
        caption.setLayoutParams(new LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.MATCH_PARENT
        ));
        String text = getTextForParameterByKey(parent.getContext(), parameterKey);
        caption.setText(text);
        caption.setPadding(0,0,40,0);
        container.addView(caption);

        //Info
        final TextView info = new TextView(parent.getContext());
        info.setLayoutParams(new LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.MATCH_PARENT
        ));
        info.setText("");
        container.addView(info);

        //Input
        View inputView = null;
        switch (parameter.getType()) {
            case BOOLEAN:
                Switch sw = new Switch(parent.getContext());
                sw.setChecked(((Parameter.ParamBoolean) parameter).isTrue());
                inputView = sw;
                break;
            case STRING:
                EditText et = new EditText(parent.getContext());
                et.setText( ((Parameter.ParamString) parameter).toString() );
                inputView = et;
                break;
            case INTEGER:
                Parameter.ParamInteger.ParamIntegerRestriction pIntRestr
                        = ((Parameter.ParamInteger) parameter).getMinmax();
                if ( pIntRestr != null) {
                    SeekBar seekbarInt = new SeekBar(parent.getContext());
                    seekbarInt.setMax(pIntRestr.max);
                    seekbarInt.setProgress(((Parameter.ParamInteger) parameter).getValue());
                    seekbarInt.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                        @Override
                        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                            info.setText(String.valueOf(i));
                        }

                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {

                        }

                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {

                        }
                    });
                    info.setText(parameter.toString());
                    inputView = seekbarInt;
                } else {
                    EditText etNum = new EditText(parent.getContext());
                    etNum.setText(parameter.toString());
                    etNum.setInputType(InputType.TYPE_CLASS_NUMBER);
                    inputView = etNum;
                }
                break;
            case LONG:
                EditText etNum = new EditText(parent.getContext());
                etNum.setText( parameter.toString() );
                etNum.setInputType(InputType.TYPE_CLASS_NUMBER);
                inputView = etNum;
                break;
            case DECIMAL:
                EditText etDec = new EditText(parent.getContext());
                etDec.setText( parameter.toString() );
                etDec.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                inputView = etDec;
                break;
            case DOUBLE:
                EditText etDouble = new EditText(parent.getContext());
                etDouble.setText( parameter.toString() );
                etDouble.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                inputView = etDouble;
                break;
            case DATETIME:
                break;
            case ENUM:
                Spinner spin = new Spinner(parent.getContext());
                ArrayAdapter adapter = new ArrayAdapter<String>( parent.getContext(),
                        android.R.layout.simple_spinner_dropdown_item,
                        ((Parameter.ParamEnum) parameter).getParamEnumType().getPossibleMembers()
                );
                spin.setAdapter(adapter);
                spin.setSelection(adapter.getPosition(
                        ((Parameter.ParamEnum) parameter).toString()
                ));
                inputView = spin;
                break;
        }

        if (inputView != null) {
            inputView.setLayoutParams(new LayoutParams(
                    LayoutParams.MATCH_PARENT,
                    LayoutParams.MATCH_PARENT
            ));
            inputView.setEnabled(isEnabled);
            container.addView(inputView);
        }

        parent.addView(container);


        return inputView;
    }

    private static String getTextForParameterByKey(Context context, String parameterKey) {
        Resources res = context.getResources();
        String stringResourceName = "PARAMETER_" + parameterKey;
        int id = res.getIdentifier(stringResourceName, "string", context.getPackageName());
        String text;
        if (id != 0) {
            text = res.getString(id);
        } else {
            text = parameterKey;
        }
        return text;
    }

    public static void setParameterFromView(View sourceView, Parameter parameter) {
        switch (parameter.getType()) {
            case BOOLEAN:
                Switch sw = (Switch) sourceView;
                ((Parameter.ParamBoolean) parameter).setValue(sw.isChecked());
                break;
            case STRING:
                EditText et = (EditText) sourceView;
                ((Parameter.ParamString) parameter).setString(et.getText().toString().trim());
                break;
            case INTEGER:
                Parameter.ParamInteger.ParamIntegerRestriction pIntRestr
                        = ((Parameter.ParamInteger) parameter).getMinmax();
                int valueInt;
                if (pIntRestr != null) {
                    SeekBar seekbarInt = (SeekBar) sourceView;
                    valueInt = seekbarInt.getProgress();
                } else {
                    EditText etInt = (EditText) sourceView;
                    valueInt = Integer.parseInt(etInt.getText().toString().trim());
                }
                ((Parameter.ParamInteger) parameter).setValue(valueInt);
                break;
            case LONG:
                EditText etLong = (EditText) sourceView;
                long valueLong = Long.parseLong(etLong.getText().toString().trim());
                ((Parameter.ParamLong) parameter).setValue(valueLong);
                break;
            case DECIMAL:
                EditText etDec = (EditText) sourceView;
                double valueDec = Double.parseDouble(etDec.getText().toString().trim());
                ((Parameter.ParamDecimal) parameter).setRawValue(valueDec);
                break;
            case DOUBLE:
                EditText etDouble = (EditText) sourceView;
                double valueDouble = Double.parseDouble(etDouble.getText().toString().trim());
                ((Parameter.ParamDouble) parameter).setValue(valueDouble);
                break;
            case DATETIME:
                break;
            case ENUM:
                Spinner spin = (Spinner) sourceView;
                String valueEnum = spin.getSelectedItem().toString();
                ((Parameter.ParamEnum) parameter).setValue(valueEnum);
                break;
        }
    }//setParameterFromView
}

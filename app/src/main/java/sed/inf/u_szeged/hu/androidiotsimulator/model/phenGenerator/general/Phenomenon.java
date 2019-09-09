package sed.inf.u_szeged.hu.androidiotsimulator.model.phenGenerator.general;

import java.util.Date;

public class Phenomenon {
    public static class PhenomTargetInt extends Parameter.ParamInteger {
        //int current_value;	//--> helyette a ParamInt value-je
        public int start_value, target_value;
        public Date start_time, target_time;

        public void calc_current_value(Date current_time) {
            if (value == target_value) {
                return;
            } else if (target_time.getTime() <= current_time.getTime()) {
                value = target_value;
            } else {
                long elapsedTime = current_time.getTime() - start_time.getTime();
                int full_change_value = target_value - start_value;
                long full_change_time = target_time.getTime() - start_time.getTime();
                value = start_value + (int)(full_change_value * elapsedTime / full_change_time);
            }
        }

        //overwrites previous target, even if it has not been reached
        public void setTargetValue(int new_target_value, Date new_target_time, Date current_time) {
            start_value = value;
            target_value = new_target_value;
            start_time = new Date(current_time.getTime());
            target_time = new_target_time;
        }

        public int getCurrentValue() {
            return value;
        }

        public PhenomTargetInt(int value) {
            super(value);
            start_value = target_value = value;
        }
    }

    /*
    //Must be fixed like PhenomInt
    public static class PhenomTargetDecimal extends Parameter.ParamDecimal {
        public double start_value, target_value;
        public Date start_time, target_time;

        public void calc_current_value(Date current_time) {
            if (target_time.getTime() <= current_time.getTime()) {
                rawValue = target_value;
            } else {
                long elapsedTime = current_time.getTime() - start_time.getTime();
                double full_change_value = target_value - start_value;
                long full_change_time = target_time.getTime() - start_time.getTime();
                rawValue += full_change_value * elapsedTime / full_change_time;
            }
        }

        //overwrites previous target, even if it has not been reached
        public void setTargetValue(int new_target_value, Date new_target_time, Date current_time) {
            start_value = rawValue;
            target_value = new_target_value;
            start_time = current_time;
            target_time = new_target_time;
        }

        public double getCurrentRawValue() {
            return rawValue;
        }

        public PhenomTargetDecimal(double rawValue, int decimalPlaces) {
            super(rawValue, decimalPlaces);
            start_value = target_value = rawValue;
        }
    }*/
}

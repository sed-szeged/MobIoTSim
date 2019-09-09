package sed.inf.u_szeged.hu.androidiotsimulator.model.phenGenerator.general;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public abstract class Parameter {

    private static Random rnd = new Random();

    @Override
    public abstract String toString();

    //private boolean canBeEmpty;
    //public abstract boolean isEmpty();
    //public abstract void setEmpty();

    public ParameterType getType() {
        if (this instanceof ParamBoolean) return ParameterType.BOOLEAN;
        if (this instanceof ParamString) return ParameterType.STRING;
        if (this instanceof ParamInteger) return ParameterType.INTEGER;
        if (this instanceof ParamLong) return ParameterType.LONG;
        if (this instanceof ParamDecimal) return ParameterType.DECIMAL;
        if (this instanceof ParamDouble) return ParameterType.DOUBLE;
        if (this instanceof ParamDateTime) return ParameterType.DATETIME;
        if (this instanceof ParamEnum) return ParameterType.ENUM;
        if (this instanceof ParamRange) return ParameterType.RANGE;
        if (this instanceof ParamNull) return ParameterType.NULL;
        if (this instanceof ParamObject) return ParameterType.OBJECT;
        return null;
    }

    public enum ParameterType {
        STRING, INTEGER, LONG, DECIMAL, DOUBLE, DATETIME, ENUM, BOOLEAN, RANGE, NULL, OBJECT, ARRAY
    }

    /**
     * Used for special purposes (for example: no data is available)
     */
    public static class ParamNull extends Parameter {
        @Override
        public String toString() {
            return null;
        }
    }

    public static class ParamBoolean extends Parameter {
        private boolean value;

        public ParamBoolean(boolean isTrue) {
            this.value = isTrue;
        }

        public void setValue(boolean value) {
            this.value = value;
        }

        public boolean isTrue() {
            return value;
        }

        @Override
        public String toString() {
            return value ? "1" : "0";
        }
    }

    public static class ParamString extends Parameter {
        private String value;
        private int maxLength = 0;

        public ParamString(String value) {this.value = value;}
        public ParamString(String value, int maxLength) {
            this.value = value;
            this.maxLength = maxLength;
        }

        @Override
        public String toString() {
            return value;
        }

        public void setString(String value) {
            this.value = (maxLength == 0 || value == null)
                    ? value
                    : value.substring(0, Math.min(maxLength, value.length()));
        }

        public int getMaxLength() {
            return maxLength;
        }

        public void setMaxLength(int maxLength) {
            this.maxLength = maxLength;
        }

    }

    public static class ParamEnum extends Parameter {
        private ParamEnumGroup type;
        private String value;

        @Override
        public String toString() {
            return value;
        }

        public ParamEnum(ParamEnumGroup enumType, String value) {
            this.type = enumType;
            setValue(value);
        }

        public void setValue(String value) {
            if (type != null) {
                for (String s : type.getPossibleMembers()) {
                    if (value.equals(s)) {
                        this.value = value;
                        return;
                    }
                }
                this.value = null;
            }
        }

        public ParamEnumGroup getParamEnumType() {
            return type;
        }

        public static class ParamEnumGroup {
            private List<String> possibleMembers = new ArrayList<>();
            public ParamEnumGroup(List<String> members) { possibleMembers.addAll(members); }
            public ParamEnumGroup(String[] members) {possibleMembers.addAll(Arrays.asList(members));}
            public List<String> getPossibleMembers() { return possibleMembers; }
        }
    }

    public static class ParamInteger extends Parameter {
        protected int value;
        protected ParamIntegerRestriction minmax;
        public ParamInteger(int value) { this.value = value;}
        public ParamInteger(int value, ParamIntegerRestriction minmax) {
            this.value = value;
            this.minmax = minmax;
        }
        @Override
        public String toString() {
            return String.valueOf(value);
        }
        public int getValue() { return value; }
        public void setValue(int value) {
            if (minmax != null) {
                if (value < minmax.min) value = minmax.min;
                if (value > minmax.max) value = minmax.max;
            }
            this.value = value;
        }
        public ParamIntegerRestriction getMinmax() { return minmax;}

        public static class ParamIntegerRestriction {
            public final int min, max;
            public ParamIntegerRestriction(int min, int max) {
                this.min = min;
                this.max = max;
            }
        }
        public static final ParamIntegerRestriction RATE = new ParamIntegerRestriction(0, 100);
    }

    public static class ParamLong extends Parameter {
        private long value;
        public ParamLong(long value) { this.value = value;}
        @Override
        public String toString() {
            return String.valueOf(value);
        }
        public long getValue() { return value; }
        public void setValue(long value) {
            this.value = value;
        }
    }

    public static class ParamRange extends Parameter {
        private int a, b;
        public ParamRange(int a, int b) { this.a=a; this.b=b; setAscending();}
        public String toString() { return "" + a + "," + b; }
        public int getA() {return a;}
        public int getB() {return b;}
        public int rangeLength() { return b - a;}
        private void setAscending() {
            if ( a > b) {
                int temp = a;
                a = b;
                b = temp;
            }
        }
        public void setA(int a) {this.a = a; setAscending();}
        public void setB(int b) {this.b = b; setAscending();}
    }

    //Stored as double with information of decimal places used only at string conversion
    public static class ParamDecimal extends Parameter {
        protected double rawValue;
        protected int decimalPlaces;

        public ParamDecimal(double rawValue, int decimalPlaces) {
            setRawValue(rawValue);
            setDecimalPlaces(decimalPlaces);
        }

        @Override
        public String toString() {
            return String.format("%." + decimalPlaces +".f", rawValue);
        }

        public int getDecimalPlaces() {
            return decimalPlaces;
        }

        public void setDecimalPlaces(int decimalPlaces) {
            this.decimalPlaces = Math.max(0, decimalPlaces);
        }

        public double getRawValue() {
            return rawValue;
        }

        public void setRawValue(double rawValue) {
            this.rawValue = rawValue;
        }

    }

    public static class ParamDouble extends Parameter {
        private double value;
        @Override
        public String toString() {
            return String.valueOf(value);
        }
        public double getValue() { return value; }
        public void setValue(double value) {
            this.value = value;
        }
    }

    public static class ParamDateTime extends Parameter {
        private Date value;
        public ParamDateTime(Date value) {this.value = value;}

        @Override
        public String toString() {
            return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                    .format(value);
        }

        public void setValue(Date value) { this.value = value; }
        public Date getValue() { return value; }

        public void addMilliSecs(long milliseconds) {
            value.setTime(value.getTime() + milliseconds);
        }
    }

    public static class ParamObject extends Parameter {
        private Map<String, Parameter> parameters = new LinkedHashMap<>();

        public ParamObject() {}
        public ParamObject(Map<String, Parameter> parameters) {this.parameters = parameters;}
        public void addParameter(String parameterName, Parameter parameter) {
            parameters.put(parameterName, parameter);
        }
        public Map<String, Parameter> getParameters() {return parameters;}
        public void removeParameter(Parameter parameter) { parameters.remove(parameter);}

        @Override
        public String toString() {
            String string = "";
            for(Map.Entry<String, Parameter> p : parameters.entrySet()) {
                string += p.getKey() + ": " + p.getValue() + "\n";
            }
            return string;
        }
    }

    public static int getRandom(int a, int b) {
        return rnd.nextInt(b - a + 1) + a;
    }

    public static double getRandom(double a, double b) {
        return rnd.nextDouble() * (b - a + 1) + a;
    }

}

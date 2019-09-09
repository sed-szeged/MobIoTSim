package sed.inf.u_szeged.hu.androidiotsimulator.model.phenGenerator.general;

import java.util.Map;

public class Parameter_JSON {
    public static String getJsonValueFromParameter(Parameter parameter) {

        if (parameter instanceof Parameter.ParamBoolean)
            return String.valueOf( ((Parameter.ParamBoolean) parameter).isTrue() );

        else if (parameter instanceof Parameter.ParamString
            || parameter instanceof Parameter.ParamString)
            return "\"" + parameter.toString() + "\"";

        else if (parameter instanceof Parameter.ParamInteger
            || parameter instanceof Parameter.ParamLong
            || parameter instanceof Parameter.ParamDecimal
            || parameter instanceof Parameter.ParamDouble)
            return parameter.toString();

        else if (parameter instanceof Parameter.ParamRange) {
            Parameter.ParamRange range = (Parameter.ParamRange) parameter;
            return "[" + range.getA() + ", " + "]";
        }

        else if (parameter instanceof Parameter.ParamNull)
            return "null";

        else if (parameter instanceof Parameter.ParamObject) {
            Parameter.ParamObject object = (Parameter.ParamObject) parameter;
            StringBuilder str = new StringBuilder("{");
            for (Map.Entry<String, Parameter> subParameter : object.getParameters().entrySet()) {
                str.append("\"" + subParameter.getKey() + "\":");
                str.append( getJsonValueFromParameter(subParameter.getValue()) + ", ");
            }
            if (str.charAt(str.length()-1) == ',') str.deleteCharAt(str.length()-1);
            str.append('}');
            return str.toString();
        }

        else
            return "\"" + parameter.toString() + "\"";

    }
}

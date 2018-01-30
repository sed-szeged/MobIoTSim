package sed.inf.u_szeged.hu.androidiotsimulator.model.device;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.Calendar;

/**
 * Created by Pflanzner on 2017. 03. 15..
 */

public class DataGenerator {

    final static int DEFAULT_VALUE_STATUS = 10;
    final static int DEFAULT_VALUE_TEMP = 15;
    final static int DEFAULT_VALUE_PRESSURE = 1000;
    final static int DEFAULT_VALUE_HUMIDITY = 70;
    final static int DEFAULT_VALUE_WIND = 2;
    final static int DEFAULT_VALUE_BRIGHTNESS = 350;
    final static int DEFAULT_VALUE_GAS = 410;
    final static int DEFAULT_VALUE_NOISE = 5;

    final static int LIMIT_STATUS = 10;
    final static int LIMIT_TEMP = 20;
    final static int LIMIT_PRESSURE = 20;
    final static int LIMIT_HUMIDITY = 15;
    final static int LIMIT_WIND = 2;
    final static int LIMIT_BRIGHTNESS = 20;
    final static int LIMIT_GAS = 20;
    final static int LIMIT_NOISE = 2;


    final static String baseSchema = "{ \"d\" : { %s } }";
    final static String simpleSchema = " \"status\" : %s ";
    final static String multiSchema = " \"temp\" : %s , \"pressure\" : %s , \"humidity\" : %s , \"wind\" : %s  ";
    final static String weatherSchema = "\"coord\":{\"lon\":20.15,\"lat\":46.25},\"sys\":{\"type\":1,\"id\":5732,\"message\":0.1651,\"country\":\"HU\",\"sunrise\":1489467152,\"sunset\":1489509857},\"weather\":[{\"id\":800,\"main\":\"Clear\",\"description\":\"Sky is Clear\",\"icon\":\"01n\"}],\"main\":{\"temp\":%s,\"pressure\":%s,\"humidity\":%s,\"temp_min\":2,\"temp_max\":5},\"visibility\":10000,\"wind\":{\"speed\":%s,\"deg\":20},\"clouds\":{\"all\":0},\"dt\":1489452300,\"id\":715429,\"name\":\"Szeged\"";
    final static String weatherGroupSchema = "\"cnt\":5,\"list\":[{\"coord\":{\"lon\":20.15,\"lat\":46.25},\"sys\":{\"type\":1,\"id\":5732,\"message\":0.1651,\"country\":\"HU\",\"sunrise\":1489467152,\"sunset\":1489509857},\"weather\":[{\"id\":800,\"main\":\"Clear\",\"description\":\"Sky is Clear\",\"icon\":\"01n\"}],\"main\":{\"temp\":%s,\"pressure\":%s,\"humidity\":%s,\"temp_min\":2,\"temp_max\":5},\"visibility\":10000,\"wind\":{\"speed\":%s,\"deg\":20},\"clouds\":{\"all\":0},\"dt\":1489452300,\"id\":715429,\"name\":\"Szeged\"},{\"coord\":{\"lon\":19.04,\"lat\":47.5},\"sys\":{\"type\":1,\"id\":5724,\"message\":0.1848,\"country\":\"HU\",\"sunrise\":1489467435,\"sunset\":1489510106},\"weather\":[{\"id\":800,\"main\":\"Clear\",\"description\":\"Sky is Clear\",\"icon\":\"01n\"}],\"main\":{\"temp\":%s,\"pressure\":%s,\"humidity\":%s,\"temp_min\":2,\"temp_max\":2},\"visibility\":10000,\"wind\":{\"speed\":%s,\"deg\":196.503},\"clouds\":{\"all\":0},\"dt\":1489453200,\"id\":3054643,\"name\":\"Budapest\"},{\"coord\":{\"lon\":24.94,\"lat\":60.17},\"sys\":{\"type\":1,\"id\":5019,\"message\":0.1626,\"country\":\"FI\",\"sunrise\":1489466254,\"sunset\":1489508458},\"weather\":[{\"id\":803,\"main\":\"Clouds\",\"description\":\"broken clouds\",\"icon\":\"04n\"}],\"main\":{\"temp\":%s,\"pressure\":%s,\"temp_min\":0,\"temp_max\":0,\"humidity\":%s},\"visibility\":10000,\"wind\":{\"speed\":%s,\"deg\":200},\"clouds\":{\"all\":75},\"dt\":1489452600,\"id\":658225,\"name\":\"Helsinki\"},{\"coord\":{\"lon\":-64.7,\"lat\":10.13},\"sys\":{\"message\":0.1707,\"country\":\"VE\",\"sunrise\":1489487146,\"sunset\":1489530583},\"weather\":[{\"id\":802,\"main\":\"Clouds\",\"description\":\"scattered clouds\",\"icon\":\"03n\"}],\"main\":{\"temp\":%s,\"temp_min\":25.782,\"temp_max\":25.782,\"pressure\":%s,\"sea_level\":1026.32,\"grnd_level\":1022.11,\"humidity\":%s},\"wind\":{\"speed\":%s,\"deg\":50.5029},\"clouds\":{\"all\":36},\"dt\":1489455434,\"id\":3648559,\"name\":\"Barcelona\"},{\"coord\":{\"lon\":-80.19,\"lat\":25.77},\"sys\":{\"type\":1,\"id\":689,\"message\":0.1716,\"country\":\"US\",\"sunrise\":1489490993,\"sunset\":1489534171},\"weather\":[{\"id\":804,\"main\":\"Clouds\",\"description\":\"overcast clouds\",\"icon\":\"04n\"}],\"main\":{\"temp\":%s,\"pressure\":%s,\"humidity\":%s,\"temp_min\":24,\"temp_max\":26},\"visibility\":16093,\"wind\":{\"speed\":%s,\"deg\":160},\"clouds\":{\"all\":90},\"dt\":1489453080,\"id\":4164138,\"name\":\"Miami\"}]";
    final static String smartCitySchema = " \"date\" : %s , \"temperature\" : %s , \"brightness\" : %s , \"humidity\" : %s , \"pressure\" : %s , \"gas\" : %s , \"noise\" : %s , \"altitude\" : 75 , \"latitide\" : 46.25 , \"longitude\" : 20.15 ";

    //TODO
    //ThreadLocalRandom random = new ThreadLocalRandom();
    static Random random = new Random();

    enum MSG_TYPE { SIMPLE_STATIC, SIMPLE_RANDOM, SIMPLE_RANDOM_LIMIT, SIMPLE_RANDOM_STEP,
                    MULTI_STATIC, MULTI_RANDOM_STEP, MULTI_RANDOM_LIMIT,
                    WEATHER_RANDOM, WEATHER_RANDOM_STEP, WEATHER_IDX, WEATHER_IDX_STEP,
                    WEATHER_GROUP_RANDOM, WEATHER_GROUP_RANDOM_STEP};

    public static String getNextMsg(MSG_TYPE msgType){
        String msg;
        switch (msgType){
            case SIMPLE_STATIC: msg = getNextSimpleStaticMsg(); break;
            case SIMPLE_RANDOM: msg = getNextSimpleRandomMsg(); break;
            case SIMPLE_RANDOM_LIMIT: msg = getNextSimpleRandomLimitMsg(); break;
            //case SIMPLE_RANDOM_STEP: msg = getNextSimpleRandomStepMsg(); break;

            case MULTI_STATIC: msg = getNextMultiStaticMsg(); break;
            //case MULTI_RANDOM_STEP: msg = getNextMultiRandomStepMsg(); break;
            case MULTI_RANDOM_LIMIT: msg = getNextMultiRandomLimitMsg(); break;

            case WEATHER_RANDOM: msg = getNextWeatherRandomMsg(); break;
            case WEATHER_RANDOM_STEP: msg = getNextWeatherRandomMsgWithRandomStep(); break;
            //case WEATHER_IDX: msg = getNextWeatherMsg(); break;
            //case WEATHER_IDX_STEP: msg = getNextWeatherMsgWithRandomStep(); break;
            case WEATHER_GROUP_RANDOM: msg = getNextWeatherGroupRandomMsg(); break;
            case WEATHER_GROUP_RANDOM_STEP: msg = getNextWeatherGroupRandomMsgWithRandomStep(); break;

            default:
               // MyLog.e("Unknown message type");
                return getNextSimpleStaticMsg();
        }
        return msg;
    }

    public static String getNextMsg(MSG_TYPE msgType, int...prev){
        String msg;
        switch (msgType){
            case SIMPLE_RANDOM_STEP: msg = getNextSimpleRandomStepMsg(prev[0]); break;

            case MULTI_RANDOM_STEP: msg = getNextMultiRandomStepMsg(prev[0], prev[1], prev[2], prev[3]); break;

            case WEATHER_IDX: msg = getNextWeatherMsg(prev[0]); break;
            case WEATHER_IDX_STEP: msg = getNextWeatherMsgWithRandomStep(prev[0]); break;

            default:
                msg = getNextMsg(msgType);
        }
        return msg;
    }

    //simple
    public static String getNextSimpleStaticMsg() {
        return String.format(baseSchema, String.format(simpleSchema, DEFAULT_VALUE_STATUS));
    }

    public static String getNextSimpleRandomMsg() {
        return String.format(baseSchema, String.format(simpleSchema, random.nextInt(DEFAULT_VALUE_STATUS + 1)));
    }

    public static String getNextSimpleRandomLimitMsg() {
        return String.format(baseSchema, String.format(simpleSchema, DEFAULT_VALUE_STATUS - LIMIT_STATUS + random.nextInt(LIMIT_STATUS * 2 + 1)));
    }

    public static String getNextSimpleRandomStepMsg(int prev) {
        return String.format(baseSchema, String.format(simpleSchema, prev - 1 + random.nextInt(3)));
    }

    //multi
    public static String getNextMultiStaticMsg() {
        return String.format(baseSchema, String.format(multiSchema, DEFAULT_VALUE_TEMP, DEFAULT_VALUE_PRESSURE, DEFAULT_VALUE_HUMIDITY, DEFAULT_VALUE_WIND));
    }


    public static String getNextMultiRandomStepMsg(int prevTemp, int prevPress, int prevHum, int prevWind) {
        return String.format(baseSchema, String.format(multiSchema,
                prevTemp - 1 + random.nextInt(3),
                prevPress - 1 + random.nextInt(3),
                prevHum - 1 + random.nextInt(3),
                prevWind - 1 + random.nextInt(3)
        ));
    }

    public static String getNextMultiRandomLimitMsg() {
        return String.format(baseSchema, String.format(multiSchema,
                DEFAULT_VALUE_TEMP - LIMIT_TEMP + random.nextInt(LIMIT_TEMP * 2 + 1),
                DEFAULT_VALUE_PRESSURE - LIMIT_PRESSURE + random.nextInt(LIMIT_PRESSURE * 2 + 1),
                DEFAULT_VALUE_HUMIDITY - LIMIT_HUMIDITY + random.nextInt(LIMIT_HUMIDITY * 2 + 1),
                DEFAULT_VALUE_WIND - LIMIT_WIND + random.nextInt(LIMIT_WIND * 2 + 1)
        ));
    }

    public static String getNextWeatherRandomMsg() {
        return String.format(baseSchema, String.format(weatherSchema,
                DEFAULT_VALUE_TEMP - LIMIT_TEMP + random.nextInt(LIMIT_TEMP * 2 + 1),
                DEFAULT_VALUE_PRESSURE - LIMIT_PRESSURE + random.nextInt(LIMIT_PRESSURE * 2 + 1),
                DEFAULT_VALUE_HUMIDITY - LIMIT_HUMIDITY + random.nextInt(LIMIT_HUMIDITY * 2 + 1),
                DEFAULT_VALUE_WIND - LIMIT_WIND + random.nextInt(LIMIT_WIND * 2 + 1)));
    }

    private static String getNextWeatherRandomMsgWithRandomStep() {
        return null;
    }

    public static String getNextWeatherMsg(int idx) {
        return null;
    }

    public static String getNextWeatherMsgWithRandomStep(int idx) {
        return null;
    }

    public static String getNextWeatherGroupRandomMsg() {
        return String.format(baseSchema, String.format(weatherGroupSchema,
                DEFAULT_VALUE_TEMP - LIMIT_TEMP + random.nextInt(LIMIT_TEMP * 2 + 1),
                DEFAULT_VALUE_PRESSURE - LIMIT_PRESSURE + random.nextInt(LIMIT_PRESSURE * 2 + 1),
                DEFAULT_VALUE_HUMIDITY - LIMIT_HUMIDITY + random.nextInt(LIMIT_HUMIDITY * 2 + 1),
                DEFAULT_VALUE_WIND - LIMIT_WIND + random.nextInt(LIMIT_WIND * 2 + 1),
                DEFAULT_VALUE_TEMP - LIMIT_TEMP + random.nextInt(LIMIT_TEMP * 2 + 1),
                DEFAULT_VALUE_PRESSURE - LIMIT_PRESSURE + random.nextInt(LIMIT_PRESSURE * 2 + 1),
                DEFAULT_VALUE_HUMIDITY - LIMIT_HUMIDITY + random.nextInt(LIMIT_HUMIDITY * 2 + 1),
                DEFAULT_VALUE_WIND - LIMIT_WIND + random.nextInt(LIMIT_WIND * 2 + 1),
                DEFAULT_VALUE_TEMP - LIMIT_TEMP + random.nextInt(LIMIT_TEMP * 2 + 1),
                DEFAULT_VALUE_PRESSURE - LIMIT_PRESSURE + random.nextInt(LIMIT_PRESSURE * 2 + 1),
                DEFAULT_VALUE_HUMIDITY - LIMIT_HUMIDITY + random.nextInt(LIMIT_HUMIDITY * 2 + 1),
                DEFAULT_VALUE_WIND - LIMIT_WIND + random.nextInt(LIMIT_WIND * 2 + 1),
                DEFAULT_VALUE_TEMP - LIMIT_TEMP + random.nextInt(LIMIT_TEMP * 2 + 1),
                DEFAULT_VALUE_PRESSURE - LIMIT_PRESSURE + random.nextInt(LIMIT_PRESSURE * 2 + 1),
                DEFAULT_VALUE_HUMIDITY - LIMIT_HUMIDITY + random.nextInt(LIMIT_HUMIDITY * 2 + 1),
                DEFAULT_VALUE_WIND - LIMIT_WIND + random.nextInt(LIMIT_WIND * 2 + 1),
                DEFAULT_VALUE_TEMP - LIMIT_TEMP + random.nextInt(LIMIT_TEMP * 2 + 1),
                DEFAULT_VALUE_PRESSURE - LIMIT_PRESSURE + random.nextInt(LIMIT_PRESSURE * 2 + 1),
                DEFAULT_VALUE_HUMIDITY - LIMIT_HUMIDITY + random.nextInt(LIMIT_HUMIDITY * 2 + 1),
                DEFAULT_VALUE_WIND - LIMIT_WIND + random.nextInt(LIMIT_WIND * 2 + 1)
        ));
    }

    private static String getNextWeatherGroupRandomMsgWithRandomStep() {
        return null;
    }


    public static String getNextSmartCityRandomMsg() {
        return String.format(baseSchema, String.format(smartCitySchema,
                Calendar.getInstance().getTime(),
                DEFAULT_VALUE_TEMP - LIMIT_TEMP + random.nextInt(LIMIT_TEMP * 2 + 1),
                DEFAULT_VALUE_BRIGHTNESS - LIMIT_BRIGHTNESS + random.nextInt(LIMIT_BRIGHTNESS * 2 + 1),
                DEFAULT_VALUE_HUMIDITY - LIMIT_HUMIDITY + random.nextInt(LIMIT_HUMIDITY * 2 + 1),
                DEFAULT_VALUE_PRESSURE - LIMIT_PRESSURE + random.nextInt(LIMIT_PRESSURE * 2 + 1),
                DEFAULT_VALUE_GAS - LIMIT_GAS + random.nextInt(LIMIT_GAS * 2 + 1),
                DEFAULT_VALUE_NOISE - LIMIT_NOISE + random.nextInt(LIMIT_NOISE * 2 + 1)
                ));
    }

}

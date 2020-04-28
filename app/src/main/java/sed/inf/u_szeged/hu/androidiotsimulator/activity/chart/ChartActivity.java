package sed.inf.u_szeged.hu.androidiotsimulator.activity.chart;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.MotionEvent;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.collection.CircularArray;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.StringTokenizer;

import sed.inf.u_szeged.hu.androidiotsimulator.R;
import sed.inf.u_szeged.hu.androidiotsimulator.model.device.Device;
import sed.inf.u_szeged.hu.androidiotsimulator.model.device.DeviceGroup;
import sed.inf.u_szeged.hu.androidiotsimulator.model.gson.trace.randomdata.Parameter;

import static sed.inf.u_szeged.hu.androidiotsimulator.activity.device.DevicesFragment.deviceGroupList;

public class ChartActivity extends AppCompatActivity implements OnChartGestureListener, OnChartValueSelectedListener {
    private LineChart chart;
    private ArrayList<ILineDataSet> dataSets;
    private float maxAxisValue=0;
    private float minAxisValue=100;
    public static final int chartMaxEntries = 120;

    private class DataSetSums {
        private int valueSum;
        private int elements;
        private String paramName;

        DataSetSums(String paramName ,int value){
            this.paramName = paramName;
            this.valueSum += value;
            this.elements++;
        }

        public void addValue(int paramValue){
            this.valueSum+=paramValue;
            this.elements++;
        }
        public float getAverage (){
            return (float) valueSum / (float) elements;
        }

        public int getValueSum() { return valueSum; }
        public int getElements() { return elements; }
        public String getParamName() { return paramName; }

    }

@Override
    public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
    System.out.println("onChartGestureStart at: " + me.getX() + ", " + me.getY());

    }

    @Override
    public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
        System.out.println("onChartGestureEnd at: " + me.getX()+ ", " + me.getY());

    }

    @Override
    public void onChartLongPressed(MotionEvent me) {
        System.out.println("onChartLongPressed");
    }

    @Override
    public void onChartDoubleTapped(MotionEvent me) {
        System.out.println("onChartDoubleTapped");
    }

    @Override
    public void onChartSingleTapped(MotionEvent me) {
        System.out.println("onChartSingleTapped");
    }

    @Override
    public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {
        System.out.println("onChartFling");
    }

    @Override
    public void onChartScale(MotionEvent me, float scaleX, float scaleY) {
        System.out.println("onChartScale");
    }

    @Override
    public void onChartTranslate(MotionEvent me, float dX, float dY) {
        System.out.println("onChartTranslate");
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {
        System.out.println("onValueSelected");
    }

    @Override
    public void onNothingSelected() {
        System.out.println("onNothingSelected");
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        chart = (LineChart) findViewById(R.id.linechart);
        chart.setOnChartGestureListener(ChartActivity.this);
        chart.setOnChartValueSelectedListener(ChartActivity.this);

        // enable touch gestures
        chart.setTouchEnabled(true);

        // enable scaling and dragging
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        chart.setDrawGridBackground(false);
        chart.setDrawBorders(true);

        // if disabled, scaling can be done on x- and y-axis separately
        chart.setPinchZoom(true);

        // set an alternative background color
        chart.setBackgroundColor(Color.LTGRAY);
        chart.setGridBackgroundColor(Color.LTGRAY);


        LineData data = new LineData();
        dataSets = new ArrayList<>();
        maxAxisValue = 0;
        minAxisValue = 100;
        data.setValueTextColor(Color.BLACK);

        // add empty data
        chart.setData(data);

        // get the legend (only possible after setting data)
        Legend l = chart.getLegend();

        chart.getDescription().setText(getString(R.string.chart_description));
        chart.animateXY(1500,1500);

        // modify the legend ...
        l.setForm(Legend.LegendForm.LINE);
        l.setTextColor(Color.BLACK);
        XAxis xl = chart.getXAxis();

        xl.setTextColor(Color.BLACK);
        xl.setDrawGridLines(true);
        xl.setAvoidFirstLastClipping(true);
        xl.setEnabled(true);

        YAxis leftAxis = chart.getAxisLeft();

        leftAxis.setTextColor(Color.BLACK);
        leftAxis.setAxisMaximum(100f);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setDrawGridLines(true);

        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setEnabled(false);

        addDataSet(leftAxis);
    }

    private void addDataSet(YAxis leftAxis) {
        int position = Integer.parseInt(getIntent().getExtras().get("position").toString());
        DeviceGroup deviceGroup = deviceGroupList.get(position);
        ArrayList<DataSetSums> dataSetSumsList = new ArrayList<>();
        String devicegroupName = deviceGroup.getBaseDevice().getDeviceID();

        for (int deviceNo = 0; deviceNo < deviceGroup.getDeviceGroup().size(); deviceNo++) {
            ArrayList dataArr = new ArrayList(deviceGroup.getDeviceGroup().get(deviceNo).getDataSent());

            for (int msgNo = 0; msgNo < dataArr.size(); msgNo++) {
                String deviceMsg = dataArr.get(msgNo).toString();
                String[] dataSplitted = unwrapData(deviceMsg);
                int paramValue = Integer.parseInt(dataSplitted[1]);

                if (dataSetSumsList.size() > msgNo){
                    dataSetSumsList.get(msgNo).addValue(paramValue);
                }else{
                    dataSetSumsList.add(msgNo, new DataSetSums(dataSplitted[0], paramValue));
                }
            }
        }
        if (dataSetSumsList.size() > 0) {
            int deviceNo = dataSetSumsList.get(0).getElements();
            String paramName = dataSetSumsList.get(0).getParamName();
            for (int msgNo = 0; msgNo < dataSetSumsList.size(); msgNo++) {
                DataSetSums dataEntries = dataSetSumsList.get(msgNo);
                if (deviceNo > 1) {
                    addEntry(devicegroupName, paramName, dataEntries.getAverage(), leftAxis, deviceNo);
                } else if (deviceNo == 1) {
                    addEntry(devicegroupName, dataEntries.getParamName(), dataEntries.getValueSum(), leftAxis, deviceNo);
                }
            }
        }
    }

    private String[] unwrapData( String dataIn){
        final int paramNameDelimIndex = 5;
        final int paramValueDelimIndex = 2;
        String[] dataOut = new String[2];

        StringTokenizer sTokenizer = new StringTokenizer(dataIn, " ");
        String paramName="",value="";

        for(int i = 0; i<paramNameDelimIndex; i++) {
            paramName = sTokenizer.nextToken();
        }
        paramName = paramName.substring(1,paramName.length()-1);
        for(int i = 0; i<paramValueDelimIndex; i++) {
            value = sTokenizer.nextToken();
        }

        dataOut[0]=paramName;
        dataOut[1]=value;

        return dataOut;
    }

    private void addEntry(String devicegroupName, String paramName, float paramValue, YAxis leftAxis, int deviceNo) {

        setAxisMinMaxValues(paramValue,leftAxis);

        System.out.println("Adding entry: " + paramName + ":" + paramValue);
        LineData data = chart.getData();

        if (data != null) {

            ILineDataSet set = data.getDataSetByIndex(0);

            if (set == null) {
                String legend;
                devicegroupName = (devicegroupName==null || devicegroupName.equals(""))?"Selected device":devicegroupName;

                if (deviceNo > 1){
                    legend="Average " + paramName + " parameter of " + deviceNo + " devices of " + devicegroupName;
                }else{
                    legend = paramName + " parameter of " + devicegroupName;
                }
                set = createSet(legend);
                data.addDataSet(set);
            }

            data.addEntry(new Entry(set.getEntryCount(), (float) paramValue), 0);
            data.notifyDataChanged();

            // let the chart know it's data has changed
            chart.notifyDataSetChanged();

            // limit the number of visible entries
            chart.setVisibleXRangeMaximum(chartMaxEntries);
            // chart.setVisibleYRange(30, AxisDependency.LEFT);

            // move to the latest entry
            chart.moveViewToX(data.getEntryCount());
        }
    }

    private void setAxisMinMaxValues(float paramValue, YAxis leftAxis) {

        maxAxisValue = maxAxisValue < paramValue ? paramValue : maxAxisValue;
        minAxisValue = minAxisValue > paramValue ? paramValue : minAxisValue;
        leftAxis.setAxisMaximum( maxAxisValue+1f ); System.out.println("Max axis value set to: " + (maxAxisValue+1f));
        leftAxis.setAxisMinimum( minAxisValue-1f ); System.out.println("Min axis value set to: " + (minAxisValue-1f));
    }

    private LineDataSet createSet(String dataToShow) {

        dataToShow = (dataToShow.isEmpty() || dataToShow == null)?"Sensor data":dataToShow;

        LineDataSet set = new LineDataSet(null, dataToShow );
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(getRandomColor());
        set.setCircleColor(Color.BLACK);
        set.setLineWidth(2f);
        set.setCircleRadius(2f);
        set.setFillAlpha(65);
        set.setFillColor(ColorTemplate.getHoloBlue());
        set.setHighLightColor(Color.rgb(244, 117, 117));
        set.setValueTextColor(Color.BLACK);
        set.setValueTextSize(9f);
        set.setDrawValues(false);
        return set;
    }

    private int getRandomColor() {

        Random rnd = new Random();
        int color =  Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
        return color;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}

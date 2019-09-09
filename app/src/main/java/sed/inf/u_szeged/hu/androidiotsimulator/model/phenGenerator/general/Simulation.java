package sed.inf.u_szeged.hu.androidiotsimulator.model.phenGenerator.general;

import android.content.Context;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sed.inf.u_szeged.hu.androidiotsimulator.MobIoTApplication;
import sed.inf.u_szeged.hu.androidiotsimulator.activity.cloud.CloudSettingsActivity;
import sed.inf.u_szeged.hu.androidiotsimulator.controller.RESTTools_bluemix;
import sed.inf.u_szeged.hu.androidiotsimulator.controller.RESTTools_edgeGateway;
import sed.inf.u_szeged.hu.androidiotsimulator.model.device.Device;
import sed.inf.u_szeged.hu.androidiotsimulator.model.device.DeviceGroup;
import sed.inf.u_szeged.hu.androidiotsimulator.model.device.SensorData;
import sed.inf.u_szeged.hu.androidiotsimulator.model.device.SensorDataWrapper;

public abstract class Simulation implements Runnable {

    private volatile boolean isSimulationRunning = false;
    private volatile boolean areDevicesRunning = false;
    public final Theme theme;
    protected long id;
    protected REST_handler rest_handler;

    protected Parameter.ParamString name = new Parameter.ParamString("untitled");

    /* How often generatePhenomena will be called in milliseconds.
    It affects computation capacity.
    Not to be confused with timeAcceleration which affects only simulation date and time!
    */
    protected Parameter.ParamLong generationFrequency = new Parameter.ParamLong(1000);

    /* Simulated milliseconds per real seconds,
          default is realtime simulation: 1sec/1sec = 1000millisec/1sec
       Higher value = faster simulation; lower value = slower simulation
     */
    protected Parameter.ParamLong timeAcceleration = new Parameter.ParamLong(600000);

    /*
     Date and time of simulation.
     Grows with timeAcceleration's value in every second in average.
     (More precisely it grows when generatePhenoma is called (in every 'generationFrequency' milliseconds):
            and grows then with [timeAcceleration * generationFrequency / 1000] milliseconds.)
     Defaults to current date and time.
     */
    protected Parameter.ParamDateTime simulatedDateTime = new Parameter.ParamDateTime(new Date());


    //protected List<Phenomenon> generalPhenomena = new ArrayList<>();
    protected Map<String, Parameter> generalPhenomena = new HashMap<>();
    protected List<Population> populations = new ArrayList<>();
    protected List<Subject> subjects = new ArrayList<>();
    protected List<DeviceGroup> deviceGroups = new ArrayList<>();
    protected List<PhenGenDevice> devices = new ArrayList<>();
    protected List<Measurement> measurements = new ArrayList<>();

    public static final String KEY_SIMULATION_NAME = "SIMULATION NAME";
    public static final String KEY_GENERATION_FREQUENCY = "GENERATION FREQUENCY";
    public static final String KEY_SIMULATED_DATETIME = "SIMULATED DATETIME";
    public static final String KEY_TIME_ACCELERATION = "TIME ACCELERATIION";

    protected Simulation(Theme theme) {
        this.theme = theme;
        /*generalPhenomena.add(new Phenomenon(KEY_SIMULATION_NAME, name));
        generalPhenomena.add(new Phenomenon(KEY_GENERATION_FREQUENCY, generationFrequency));
        generalPhenomena.add(new Phenomenon(KEY_TIME_ACCELERATION, timeAcceleration));
        generalPhenomena.add(new Phenomenon(KEY_SIMULATED_DATETIME, simulatedDateTime));*/
        generalPhenomena.put(KEY_SIMULATION_NAME, name);
        generalPhenomena.put(KEY_GENERATION_FREQUENCY, generationFrequency);
        generalPhenomena.put(KEY_TIME_ACCELERATION, timeAcceleration);
        generalPhenomena.put(KEY_SIMULATED_DATETIME, simulatedDateTime);
    }

    /*public Phenomenon getGeneralPhenomenon(String name) {
        Parameter param = generalPhenomena.get(name);
        return (param == null) ? null : new Phenomenon(name, param);
    }*/

    /*
     Starts simulation by generating data for phenomena
     */
    public void startSimulation() {
        new Thread(this).start();
    }

    /*
     Stops the simulation, but can be continued by starting it again.
     */
    public void pauseSimulation() {
        isSimulationRunning = false;
    }

    @Override
    public void run() {
        isSimulationRunning = true;
        while (isSimulationRunning) {
            try {
                generatePhenomena();    //usually it is overridden
                //log();
                refreshMeasurements();
                Thread.currentThread().sleep(generationFrequency.getValue());
            } catch (InterruptedException e) {
                //...
            }
        }
    }

    private void log() {
        StringBuilder logText = new StringBuilder("");
        logText.append(">> PHENOMENA GENERATION -- " + simulatedDateTime.getValue().toString() + "\n");
        for (Subject s : subjects) {
            logText.append(">    Subject: " + s.toString() + "\n");
            for (Map.Entry<String, Parameter> entry : s.getIndividualInformation().entrySet()) {
                logText.append(">>>   * " + entry.getKey() + ": " + entry.getValue() + "\n");
            }
        }
        System.out.println(logText.toString());
    }

    /*
     Resets simulation by setting some data back to a base value
     */
    public void resetSimulation() {
        //can be done by overriding this method
        //not for now
    }


    public void startDevices() {
        areDevicesRunning = true;
        for (PhenGenDevice device : devices) {
            if (device.deviceOn) new Thread(device).start();
        }
    }

    public void stopDevices() {
        areDevicesRunning = false;
        for (Device device : devices) {
            device.stop(null);
        }
    }


    public void generatePhenomena() {
        simulatedDateTime.addMilliSecs(
                timeAcceleration.getValue() * generationFrequency.getValue() / 1000
        );
    }

    protected void refreshMeasurements() {
        System.out.println("Measurements COUNT = " + measurements.size());
        for (Measurement measurement : measurements) {
            measurement.refreshData();
        }
    }

    public Population createPopulation() {
        Population newPopulation = newPopulation();
        populations.add(newPopulation);
        return newPopulation;
    }
    protected abstract Population newPopulation();

    public Subject createSubject(Population population) {
        Subject newSubject = population.createSubject(this);
        newSubject.parentSimulation = this;
        newSubject.parentPopulation = population;
        population.subjects.add(newSubject);    //add it to the population
        this.subjects.add(newSubject);          //add it to simulation full list too
        return newSubject;
    }

    public List<Subject> generateSubjects(Population population) {
        List<Subject> newSubjects = population.generateSubjects(this);
        for (Subject s : newSubjects) {
            s.parentSimulation = this;
            s.parentPopulation = population;
        }
        population.subjects.addAll(newSubjects);    //add them to the population
        this.subjects.addAll(newSubjects);          //add them to simulation full list too
        return newSubjects;
    }

    public PhenGenDevice createDevice(PhenGenDeviceType pgdeviceType) {
        PhenGenDevice newDevice = PhenGenDevice.getPGDevice(pgdeviceType);
        devices.add(newDevice);
        setDeviceGroup(newDevice);
        return newDevice;
    }




    public List<Population> getPopulations() {
        return Collections.unmodifiableList(populations);
    }
    public List<Subject> getSubjects() {
        return Collections.unmodifiableList(subjects);
    }
    public List<PhenGenDevice> getDevices() {
        return Collections.unmodifiableList(devices);
    }
    public long getActualNumberOfSubjects() { return subjects.size(); }
    public long getId() {return id;}
    public void setId(long id) {this.id = id;}
    public String getName() {
        return name.toString();
    }
    public Parameter getGeneralParameter(String parameterKey) { return generalPhenomena.get(parameterKey); }
    public boolean isSimulationRunning() { return isSimulationRunning; }
    public boolean areDevicesRunning() { return areDevicesRunning; }

    protected void setDeviceGroup(PhenGenDevice pgdevice) {
        String type = pgdevice.pgdeviceType.deviceInnerType;
        DeviceGroup groupForNewDevice = null;

        System.out.println("Device group size" + deviceGroups.size());
        for (DeviceGroup group : deviceGroups) {
            System.out.println("   Type: " + group.getBaseDevice().getType() + ", size:" + group.getDevicesList().size());
            if (group.getBaseDevice().getType().equals(type)) {
                groupForNewDevice = group;
                break;
            }
        }

        boolean isNewGroup = false;
        if (groupForNewDevice == null) {
            groupForNewDevice = createNewDeviceGroup(pgdevice);
            isNewGroup = true;
        }

        pgdevice.setDeviceGroup(groupForNewDevice);
        deviceGroups.add(groupForNewDevice);
        groupForNewDevice.getDevicesList().add(pgdevice);
        groupForNewDevice.getBaseDevice().setNumOfDevices(
                groupForNewDevice.getBaseDevice().getNumOfDevices() + 1
        );

        /*if (isNewGroup) {
            //Save device to prefs - based on DevicesActivity saveDevicesToPrefs method
            String alreadySavedDevices = MobIoTApplication.loadData(MobIoTApplication.KEY_DEVICES);
            StringBuilder sb = new StringBuilder(alreadySavedDevices);
            sb.append("<");
            Device d = groupForNewDevice.getBaseDevice();
            sb.append(d.getSerial());
            MobIoTApplication.saveData(MobIoTApplication.KEY_DEVICES, sb.toString());
        }*/
    }

    protected DeviceGroup createNewDeviceGroup(PhenGenDevice pgdevice) {
        String organizationID = MobIoTApplication.loadData(CloudSettingsActivity.KEY_ORGANIZATION_ID);
        String typeID = pgdevice.pgdeviceType.deviceOuterType;
        String token = pgdevice.getToken();
        String type = pgdevice.pgdeviceType.deviceInnerType;
        String deviceID = type;     //or better: "SimulationName - type"
        double freq = 1.0;
        ArrayList<SensorData> sensorDataList = new ArrayList<>();
        for (PhenGenDeviceSensorType pgdsensorType : pgdevice.pgdeviceType.sensorTypes) {
            //min and max values are not important here (they won't be used for data gerenation)
            sensorDataList.add(new SensorData(pgdsensorType.getName(), "0", "0"));
        }
        SensorDataWrapper sensors = new SensorDataWrapper(sensorDataList);

        Device baseDevice = new Device(organizationID, typeID, deviceID, token,
                type, freq, sensors, "random", 1, false);
        DeviceGroup newGroup = new DeviceGroup(baseDevice, PhenGenMain.getPhenGenMain().getContext());
        return newGroup;
    }

    public boolean attachDevice (PhenGenDevice device, Subject subject) {
        detachDevice(device);
        for (PhenGenDeviceSensorType sensor : device.pgdeviceType.getSensorTypes()) {
            Measurement m = new Measurement(subject, device, sensor);
            this.measurements.add(m);
            device.attachedMeasurements.put(sensor.getName(), m);
        }
        device.deviceOn = true;
        subject.attachedDevices.add(device);
        return true;
    }

    public boolean detachDevice (PhenGenDevice device) {
        for (Measurement m : device.attachedMeasurements.values()) {
            m.subject.attachedDevices.remove(device);
            this.measurements.remove(m);
        }
        device.deviceOn = false;
        device.attachedMeasurements.clear();
        return true;
    }

    public boolean deleteSubject (Subject subject) {
        for (PhenGenDevice d : subject.attachedDevices) {
            detachDevice(d);
        }
        subject.parentPopulation.subjects.remove(subject);
        this.subjects.remove(subject);
        PhenGenMain.getPhenGenMain().removeSubject(subject);
        return true;
    }

    public boolean deletePopulation (Population population) {
        ArrayList<Subject> subjectsToDelete = new ArrayList<>(population.subjects);
        for (Subject subject : subjectsToDelete) {
            deleteSubject(subject);
        }
        this.populations.remove(population);
        PhenGenMain.getPhenGenMain().removePopulation(population);
        return true;
    }

    public boolean deleteDevice (PhenGenDevice device) {
        this.detachDevice(device);
        this.devices.remove(device);
        PhenGenMain.getPhenGenMain().removePGDevice(device);
        return true;
    }

    public boolean destroySimulation () {
        stopDevices();
        pauseSimulation();
        ArrayList<Population> popsToDel = new ArrayList<>(populations);
        ArrayList<PhenGenDevice> devsToDel = new ArrayList<>(devices);
        for (Population population: popsToDel) {
            deletePopulation(population);
        }
        for (PhenGenDevice device : devsToDel) {
            deleteDevice(device);
        }
        PhenGenMain.getPhenGenMain().removeSimulation(this);
        return true;
    }


    public REST_handler getRest_handler() { return rest_handler;}

    public abstract class REST_handler {
        protected RESTTools_edgeGateway resttools;
        public REST_handler(String path_add_device, String path_add_subject, Context context) {
            String gateway_url_host = MobIoTApplication.loadData(CloudSettingsActivity.KEY_BROKER_URL_HOST);
            String gateway_url_port = "3000";   //TODO: later it should be set from GUI
            resttools = new RESTTools_edgeGateway(gateway_url_host, gateway_url_port,
                    path_add_device, path_add_subject, context);
        }

        public abstract String getSubjectJSON(Subject subject);
        public abstract String getDeviceJSON(PhenGenDevice pgdevice);

        public void addSubjects(List<Subject> subjectList) {
            for (Subject subject : subjectList) {
                resttools.addSubject(getSubjectJSON(subject));
            }
        }

        public void addDevices(List<PhenGenDevice> pgdeviceList) {
            for (PhenGenDevice pgdevice : pgdeviceList) {
                resttools.addDevices(getDeviceJSON(pgdevice));
            }
        }

    }
}

# MobIoTSim

When using the simulator for scientific research please cite the following paper:
[Kertesz A., Pflanzner T., Gyimothy T., A Mobile IoT Device Simulator for IoT-Fog-Cloud Systems. JOURNAL OF GRID COMPUTING, 17 : 3, pp. 529-551. (2019)](https://www.researchgate.net/publication/327906907_A_Mobile_IoT_Device_Simulator_for_IoT-Fog-Cloud_Systems) DOI: [10.1007/s10723-018-9468-9](https://link.springer.com/article/10.1007/s10723-018-9468-9)

### MobIoTSim with Bluemix

Here you can find information about MobIoTSim, an Android application for simulating an Internet of Things environment.
First you have to register your device in Bluemix, to have a device id and other data for authentication. Then you can start the MobIoTSim application and add devices to simulate. You can visualise the data in Bluemix.

#### Register devices in Bluemix
Some kind of authentication is always required for devices to connect to the cloud. There are two options with Bluemix. There is a demo service, where everybody freely can register devices, and the authentication is less strict. The other option is when the developer wants to deploy her or his own cloud application with an IoT service.
##### Demo version ( Bluemix Play )
If you don’t want to create a Bluemix account, you can still try out the platform with various demo projects. One of these project is the Play project, where everybody can register devices:
https://play.internetofthings.ibmcloud.com/dashboard/#/devices/browse
For the MobIoTSim application there are some already registred devices that you can use, but it is possible that other users will use the same preregistered device, which can lead to unexpected behaviour (connection failures).

<img src="https://github.com/sed-szeged/MobIoTSim/raw/master/docs/mobiotsimdevice01.png" width="720">

The information about the already registered devices are the followings:
(Organization ID: play)
Device type: MobIoTSimType
Device ID: MobIoTSimDevice01
and
(Organization ID: play)
Device type: MobIoTSimType
Device ID: MobIoTSimDevice02

If you want to register other devices, just click on the +Add Device button. There is an option to select or create a device type. You can select the already existing MobIoTSimType or create a new type by clicking on the Create Device type button.


The next step is to create a Device, where the only required field is the device id, which is a unique name for the device. At the end of the process the authentication information is displayed for the new device.


#### Own Bluemix application
If you want to deploy your own cloud application which can handle IoT devices, you should register the devices in your own dashboard. Further information can be found at the end of this tutorial, in the visualisation part. Basically after creating a cloud application, an IoT service should be added, where you can register devices. For more information visit the Bluemix tutorials: http://www.ibm.com/developerworks/learn/cloud/bluemix/quick-start/index.html

#### MobIoTSim
The MobIoTSim is an Android application for simulating devices in an Internet of Things environment. The application can be downloaded from https://github.com/sed-szeged/MobIoTSim.
After starting the application there are 3 buttons on the main screen. The Cloud button is to add or edit the cloud connection settings, the Devices button is to add, edit, start and stop devices, and the third, Information button is to provide information about the application in general.

##### Cloud
In this part of the application the connection settings can be managed for connected clouds.

There is a built-in cloud connection called “Bluemix quickstart” to connect to the Bluemix IoT demo project. This connection can be deleted, the recreation is easy, because there is a separate type for it. The demo project visualises the data from the device in a chart at https://quickstart.internetofthings.ibmcloud.com/. For more information please refer to the visualisation part.

<img src="https://github.com/sed-szeged/MobIoTSim/raw/master/docs/cloud_settings_bmdemo.png" width="240">

The selected cloud from the drop down list is the currently actual one, so the devices will send the data to it (i.e. usually to a gateway service in that cloud). To add your own Bluemix application as a target, you can edit the myBluemix built-in cloud, or create a new, custom Bluemix typed cloud. 

##### Devices
This part of the application manages the devices.

You can create, edit, remove, start and stop devices. There are two built-in devices, so you can try out the simulator with the quickstart application (as a gateway) in Bluemix. Just click on the Start button of a selected device and it starts to send data to the Bluemix quickstart demo project.

In the device settings screen, you can edit the type ID, device ID, authentication token, which are known from the Bluemix device registration. The rest of the fields are for the data generalization part.

<img src="https://github.com/sed-szeged/MobIoTSim/raw/master/docs/devices.png" width="240">

<img src="https://github.com/sed-szeged/MobIoTSim/raw/master/docs/device_settings.png" width="240">

#### Visualise the data in Bluemix
There are two options to visualise the data generated by the MobIoTSim application. The Bluemix quickstart demo project is the fastest way to test it. The other option is a bit more time consuming, but you can deploy your own cloud application which can handle (process and also react to) the data from the simulated IoT sensors.
##### Quickstart
Visit the https://quickstart.internetofthings.ibmcloud.com/ URL, where after accepting the IBM’s Term of Use, type in the device ID (displayed in the simulator). The chart will visualise the data from the given device in real-time.

<img src="https://github.com/sed-szeged/MobIoTSim/raw/master/docs/quickstart.png" width="720">


#### Deploy an own application
You can deploy a custom cloud application to Bluemix and add an IoT service, where you can register devices and communicate with them by the MQTT protocol. There are many runtimes supported. To get started with Bluemix and the IoT capabilities, the following tutorials are recommended:

General tutorial: 
https://console.ng.bluemix.net/docs/starters/IoT/iot500.html
Deploying a visualisation app: 
https://console.ng.bluemix.net/docs/services/IoT/visualizingdata_sample.html

There is a revised version of the Bluemix visualisation application in the following git repo:
https://github.com/sed-szeged/MobIoTSimBluemixGateway

<img src="https://github.com/sed-szeged/MobIoTSim/raw/master/docs/visualisation.png" width="720">

#UPDATE
####V1.1
New way to select device type

<img src="https://github.com/sed-szeged/MobIoTSim/raw/master/docs/device_type.png" width="240">

More real life like data generation. Instead of random data, we increase or decrease the previous value.

<img src="https://github.com/sed-szeged/MobIoTSim/raw/master/docs/data.png" width="720">

A device can send more parameters at the same time.

<img src="https://github.com/sed-szeged/MobIoTSim/raw/master/docs/more_param.png" width="240">

The devices can be saved to a file.
( /Android/data/sed.inf.u_szeged.hu.androidiotsimulator/files/SavedDevices )

New device type: thermostat.
<img src="https://github.com/sed-szeged/MobIoTSim/raw/master/docs/thermostat.png" width="240">



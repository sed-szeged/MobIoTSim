package sed.inf.u_szeged.hu.androidiotsimulator.controller;

import android.content.Context;

import java.util.List;

import sed.inf.u_szeged.hu.androidiotsimulator.model.gson.devicetype.Result;

public class RESTTools_edgeGateway extends RESTTools_bluemix {

    private String gateway_url;
    private String path_add_device;
    private String path_add_subject;

    public RESTTools_edgeGateway(String gateway_url_host, String gateway_url_port,
                                 String path_add_device, String path_add_subject,
                                 Context context) {
        super("", "", "", context);
        this.gateway_url = "http://" + gateway_url_host + ":" + gateway_url_port;
        this.path_add_device = path_add_device;
        this.path_add_subject = path_add_subject;
    }

    public void addDevices(String strJson) {
        String url = gateway_url + path_add_device;
        new PostingTask(strJson, url, username, password, "Add devices").execute();
    }

    public void removeDevice(String strJson) {
        //TODO
    }

    public void addDeviceType(String strJson) {
        //not necessary
    }

    public List<Result> getDeviceTypes() {
        //not necessary
        return null;
    }

    public void addSubject(String strJson) {
        String url = gateway_url + path_add_subject;
        new PostingTask(strJson, url, username, password, "Add subject").execute();
    }


}

package com.dds.textrecognition.Model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class DrawingDataRequest {
    @SerializedName("device")
    private String device;
    @SerializedName("options")
    private String options;
    @SerializedName("requests")
    private List<Request> requests;

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public String getOptions() {
        return options;
    }

    public void setOptions(String options) {
        this.options = options;
    }

    public List<Request> getRequests() {
        return requests;
    }

    public void setRequests(List<Request> requests) {
        this.requests = requests;
    }
}

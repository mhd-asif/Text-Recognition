package com.dds.textrecognition.Model;

import java.util.List;

public class GoogleApiResult {
    private String status;
    private List<String> output;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<String> getOutput() {
        return output;
    }

    public void setOutput(List<String> output) {
        this.output = output;
    }
}

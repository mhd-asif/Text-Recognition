package com.dds.textrecognition.Model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class Request {
    @SerializedName("ink")
    private List<List<List<Integer>>> inks = new ArrayList<>();
    @SerializedName("language")
    private String language;

    public List<List<List<Integer>>> getInks() {
        return inks;
    }

    public void setInks(List<List<List<Integer>>> inks) {
        this.inks = inks;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
}

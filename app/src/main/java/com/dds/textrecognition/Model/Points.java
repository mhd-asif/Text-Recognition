package com.dds.textrecognition.Model;

import java.util.ArrayList;
import java.util.List;

public class Points {
    private List<Integer> xList;
    private List<Integer> yList;
    private List<Integer> timeStampList;

    public Points() {
        xList = new ArrayList<>();
        yList = new ArrayList<>();
        timeStampList = new ArrayList<>();
    }

    public List<Integer> getxList() {
        return xList;
    }


    public List<Integer> getyList() {
        return yList;
    }


    public List<Integer> getTimeStampList() {
        return timeStampList;
    }


    public void add(int x, int y, int timestamp) {
        xList.add(x);
        yList.add(y);
        timeStampList.add(timestamp);
    }
}

package com.dds.textrecognition;

import java.util.ArrayList;
import java.util.List;

public class InkData {
    private static List<List<List<Integer>>> inkList = new ArrayList<>();
    private static List<List<Integer>> strokes = new ArrayList<>();

    public static List<List<List<Integer>>> getInkList() {
        return inkList;
    }

    public static List<List<Integer>> getStrokeList() {
        return strokes;
    }

    public static void addPointsToStroke (List<Integer> coords) {
        strokes.add(coords);
    }

    public static void addStrokeToInk () {
        inkList.add(strokes);
        strokes = new ArrayList<>();
    }

    public static void clearAll() {
        inkList = new ArrayList<>();
    }
}

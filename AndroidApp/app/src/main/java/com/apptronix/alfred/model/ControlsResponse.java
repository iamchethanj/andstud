package com.apptronix.alfred.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Maha Perriyava on 10/5/2017.
 */

public class ControlsResponse {

    @SerializedName("results")
    private List<Control> results;

    public List<Control> getResults() {
        return results;
    }

}

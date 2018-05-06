package com.apptronix.alfred.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Maha Perriyava on 10/5/2017.
 */

public class Control {

    @SerializedName("name")
    private String name;

    @SerializedName("status")
    private int status;

    public Control(String name, int status){
        this.name=name;
        this.status=status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}

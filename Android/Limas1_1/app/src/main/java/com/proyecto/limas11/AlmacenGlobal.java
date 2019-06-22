package com.proyecto.limas11;

import android.app.Application;

public class AlmacenGlobal extends Application {

    private String dirBluetooth;

    public String getDirBluetooth() {
        return dirBluetooth;
    }

    public void setDirBluetooth(String dirBluetooth) {
        this.dirBluetooth = dirBluetooth;
    }
}

package com.mb.mapbox_flight;

import android.graphics.Point;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by alex on 17-2-25.
 */

public class Drone {
    private final Float step = 0.1f;

    interface Bullet {

    }

    Array[] location = new Array[2];
    // direct in RAD.
    Float direction = 0.0f;
    String Name = "";

    private void takeOff (Array[] origin) {
        if (origin.length == 2) {
            this.location = origin;
        } else {
            System.out.println("Input Origin format incorrect !");
        }
    }

    private void accelerate() {
        // if took off. go accelerate

    }
}


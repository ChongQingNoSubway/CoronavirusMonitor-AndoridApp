package com.example.myapplication;

import com.example.myapplication.util.Utility;
import java.util.List;


public class RespiratoryRateMonitor {
    // minimum values required for calculation
    final int WINDOW_SIZE = 10;
    // recording time(in seconds)
    final int RECORDING_TIME_IN_SECONDS = (int)(MainActivity.RECORDING_TIME / 1000);

    /**
     * Finds respiratory rate by identifying number of peaks per second
     * @param values
     * @return respiratory rate for given axis values(X, Y or Z)
     */
    protected float findRate(Float[] values) {

        Float[] smoothed = Utility.applySmoothingTo1DArray(values);

        int zeroCrossings = Utility.findZeroCrossingsIn1DArray(smoothed);

        float peaks = (float)(zeroCrossings / 2);
        float rate = ((peaks / RECORDING_TIME_IN_SECONDS) * 60);

        System.out.println("Respiratory rate(over 1-D) = " + rate);
        return rate;
    }

    /**
     * Finds the respiratory rate from accelerometer input values for averaged-out x,y,z axes values
     * @param readings 3D float array readings for each dimension- X,Y,Z
     * @return respiratory rate
     */
    protected float calculateRespiratoryRate(List<float[]> readings) {

        int total = readings.size();

        if(total < WINDOW_SIZE) {

            System.out.println("ERROR: Duration of [" + total + "] is less than the required limit of [" + WINDOW_SIZE + "]!");
            return -1f;
        }

        // accelerometer values for each dim
        Float[] x = new Float[total];
        Float[] y = new Float[total];
        Float[] z = new Float[total];

        for(int i = 0; i < total; ++i) {

            x[i] = readings.get(i)[0];
            y[i] = readings.get(i)[1];
            z[i] = readings.get(i)[2];
        }

        float rateX = findRate(x);
        float rateY = findRate(y);
        float rateZ = findRate(z);

        // Average out values over all dim results
        float rate = (rateX + rateY + rateZ) / 3;

        System.out.println("Final respiratory rate = " + rate);
        return rate;
    }
}

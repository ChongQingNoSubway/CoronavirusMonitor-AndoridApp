package com.example.myapplication.util;

public class Utility {
    private Utility() {

    }

    /**
     * Finds average of all values in the list by (SUM / #ELEMENTS)
     * @param values
     * @return average
     */
    public static <E> float findAverageOf1DArray(E[] values) {

        float sum = 0;
        int n = values.length;

        if(n == 0) { System.out.println("ERROR: Empty array!"); return 0f; }

        for(E f : values) { sum += (Float)f; }

        float avg = sum / n;

        //System.out.println("Average: " + avg);
        return avg;
    }

    /**
     * Apply smoothing to all values in the list by taking average of neighboring values along with global average
     * @param values
     * @return smoothed values
     */
    public static <E> Float[] applySmoothingTo1DArray(E[] values) {

        final float alpha = 0.85f;

        float weighted = findAverageOf1DArray(values) * alpha;
        int n = values.length;
        Float[] smooth = new Float[n];

        for(int i = 0; i < n; ++i) {

            float curr = (Float)values[i];
            float prev = (i == 0) ? (Float)values[n-1] : (Float)values[i-1];
            float next = (i + 1 == n) ? (Float)values[0] : (Float)values[i+1];

            float improved = findAverageOf1DArray(new Float[]{weighted, prev, curr, next});

            smooth[i] = improved;
        }

        //System.out.println("Smoothed values: ");
        //for(E e : values) { System.out.print(e + " "); }
        return smooth;
    }

    /**
     * Finds the number of times the values plotted curve would cross the 0-axis
     * @param values
     * @return number of zero crossings
     */
    public static <E> int findZeroCrossingsIn1DArray(E[] values) {

        float avg = findAverageOf1DArray(values);
        boolean left;
        int zeroCrossings = 0;

        if((Float)values[0] >= avg) {

            left = true;

            for(int i=0;i<values.length;i++) {

                if(left) {

                    if((Float)values[i] < avg){

                        zeroCrossings++;
                        left = false;
                    }
                }
                else {

                    if((Float)values[i] >= avg) {

                        zeroCrossings++;
                        left = true;
                    }
                }
            }
        }
        else {

            left = false;

            for(int i=0;i<values.length;i++) {

                if(left){

                    if((Float)values[i] < avg) {

                        zeroCrossings++;
                        left = false;
                    }
                }
                else{

                    if((Float)values[i] >= avg) {

                        zeroCrossings++;
                        left = true;
                    }
                }
            }
        }

        System.out.println("ZeroCrossings: "+ zeroCrossings);
        return zeroCrossings;
    }
}

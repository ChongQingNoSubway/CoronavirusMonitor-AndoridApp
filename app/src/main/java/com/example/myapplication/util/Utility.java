package com.example.myapplication.util;

public class Utility {
    private Utility() {

    }


    public static <E> float findAverageOf1DArray(E[] values) {

        float sum = 0;
        int n = values.length;

        if(n == 0) { System.out.println("ERROR: Empty array!"); return 0f; }

        for(E f : values) { sum += (Float)f; }

        float avg = sum / n;

        //System.out.println("Average: " + avg);
        return avg;
    }


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

        return smooth;
    }


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

package com.example.myapplication;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.support.annotation.Nullable;


import com.example.myapplication.util.dataBaseSort;

import java.util.HashMap;



public class DatabaseOperate extends SQLiteOpenHelper {


        private String DB_PATH = null;
        private static Integer Version = 1;
        public static final String DB_NAME = "wenhui_zhu";
        public static final int DB_VERSION = 1;
        public static final String TABLE_NAME = "CoronavirusSymptomMonitor";
        public static final String USERNAME = "wenhui";
        public static final String DATE = "date";
        public static final String HEART_RATE = "heartRate";
        public static final String RESPIRATORY_RATE = "respiratoryRate";
        public static final String NAUSEA = "nausea";
        public static final String HEADACHE = "headache";
        public static final String DIARRHEA = "diarrhea";
        public static final String SORE_THROAT = "soreThroat";
        public static final String FEVER = "fever";
        public static final String MUSCLE_ACHE = "muscleAche";
        public static final String LOSS_OF_SMELL_OR_TASTE = "lossOfSmellOrTaste";
        public static final String COUGH = "cough";
        public static final String SHORTNESS_OF_BREATH = "shortnessOfBreath";
        public static final String Feeling_TIRED = "FeelingTired";

    public DatabaseOperate(@Nullable Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        this.DB_PATH = context.getDatabasePath(DB_NAME).getAbsolutePath();
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATION_TABLE = "CREATE TABLE if NOT EXISTS SignAndSymptoms ( "
                + "username TEXT, " + "date TEXT, "
                + "heartRate REAL, " + "respiratoryRate REAL, " + "nausea REAL, "
                + "headAche REAL, " + "diarrhea REAL, " + "soreThroat REAL, "
                + "fever REAL, " + "muscleAche REAL, " + "lossOfSmellOrTaste REAL, "
                + "cough REAL, " + "shortnessOfBreath Real, " + "FeelingTired Real, "
                + "PRIMARY KEY(username,date))";

        db.execSQL(CREATION_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        this.onCreate(db);
    }


    public void insert(dataBaseSort dataBaseSort, String type) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(USERNAME, dataBaseSort.getUsername());
        values.put(DATE, dataBaseSort.getDate());
        values.put(HEART_RATE, dataBaseSort.getHeartRate());
        values.put(RESPIRATORY_RATE, dataBaseSort.getRespiratoryRate());
        values.put(NAUSEA, dataBaseSort.getNausea());
        values.put(HEADACHE, dataBaseSort.getHeadache());
        values.put(DIARRHEA, dataBaseSort.getDiarrhea());
        values.put(SORE_THROAT, dataBaseSort.getSoreThroat());
        values.put(FEVER, dataBaseSort.getFever());
        values.put(MUSCLE_ACHE, dataBaseSort.getMuscleAche());
        values.put(LOSS_OF_SMELL_OR_TASTE, dataBaseSort.getLossOfSmellOrTaste());
        values.put(COUGH, dataBaseSort.getCough());
        values.put(SHORTNESS_OF_BREATH, dataBaseSort.getShortnessOfBreath());
        values.put(Feeling_TIRED, dataBaseSort.getTiredness());

        try {
            if (!isDataPresent(dataBaseSort.getUsername(), dataBaseSort.getDate())) {
                db.insert(TABLE_NAME,null, values);
                db.close();
            } else {
                if (type.equalsIgnoreCase("symptoms"))
                    update(dataBaseSort);
                else if (type.equalsIgnoreCase("signs"))
                    updateSignsData(dataBaseSort);
            }

        } catch (Exception e) {
            System.out.println("ERROR: Database Insertion Failed! Exception - " + e);
        }

    }


    public boolean isDataPresent(String username, String currentDate) {

        boolean isPresent = false;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT *" +
                        " FROM " + TABLE_NAME +
                        " WHERE username = ?" +
                        " AND date = ?",
                new String[] {username, currentDate});

        if (cursor.moveToFirst() && cursor.getCount() > 0) { isPresent = true; }

        cursor.close();
        return isPresent;
    }



    public Float[] getSignsData(String username, String currentDate) {

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT heartRate, respiratoryRate" +
                        " FROM " + TABLE_NAME +
                        " WHERE username = ?" +
                        " AND date = ?",
                new String[] {username, currentDate});

        Float[] rates = new Float[2]; // 0 - heart rate, 1 - respiratory rate

        if(cursor.moveToFirst() && cursor.getCount() > 0) {

            rates[0] = cursor.getFloat(0);
            rates[1] = cursor.getFloat(1);
        }
        else {

            rates[0] = 0f;
            rates[1] = 0f;
        }

        cursor.close();
        return rates;
    }

    /**
     * Updates data in the database
     * @param dataBaseSort
     */
    public void update(dataBaseSort dataBaseSort) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues symptomsValue = new ContentValues();
        symptomsValue.put(NAUSEA, dataBaseSort.getNausea());
        symptomsValue.put(HEADACHE, dataBaseSort.getHeadache());
        symptomsValue.put(DIARRHEA, dataBaseSort.getDiarrhea());
        symptomsValue.put(SORE_THROAT, dataBaseSort.getSoreThroat());
        symptomsValue.put(FEVER, dataBaseSort.getFever());
        symptomsValue.put(MUSCLE_ACHE, dataBaseSort.getMuscleAche());
        symptomsValue.put(LOSS_OF_SMELL_OR_TASTE, dataBaseSort.getLossOfSmellOrTaste());
        symptomsValue.put(COUGH, dataBaseSort.getCough());
        symptomsValue.put(SHORTNESS_OF_BREATH, dataBaseSort.getShortnessOfBreath());
        symptomsValue.put(Feeling_TIRED, dataBaseSort.getTiredness());
        try {
            db.update(TABLE_NAME, symptomsValue, "username=? and date=?",
                    new String[]{dataBaseSort.getUsername(), dataBaseSort.getDate()});
            db.close();
        } catch (Exception e) {
            System.out.println("ERROR: Symptoms update failed! Exception - " + e);
        }

    }


    public void updateSignsData(dataBaseSort dataBaseSort) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues signsValue = new ContentValues();
        signsValue.put(HEART_RATE, dataBaseSort.getHeartRate());
        signsValue.put(RESPIRATORY_RATE, dataBaseSort.getRespiratoryRate());
        try {
            db.update(TABLE_NAME, signsValue, "username=? and date=?",
                    new String[]{dataBaseSort.getUsername(), dataBaseSort.getDate()});
            db.close();
        } catch (Exception e) {
            System.out.println("ERROR: Signs update failed! Exception - " + e);
        }
    }

}
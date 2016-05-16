package com.example.fredrikhansson.komigennuraa;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.util.Calendar;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    int urgency;

    Button b1;
    Button b2;
    Button b3;
    Button b4;

    DBHelper mydb;
    String busId;
    String symptom;
    String status;
    boolean symptomSelected = false;
    boolean gradeSelected = false;

    Calendar calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button sendButton = (Button)findViewById(R.id.sendButton);
        sendButton.setEnabled(false);

        b1 = (Button) findViewById(R.id.colorButton1);
        b2 = (Button) findViewById(R.id.colorButton2);
        b3 = (Button) findViewById(R.id.colorButton3);
        b4 = (Button) findViewById(R.id.colorButton4);


        calendar = Calendar.getInstance();

        busId = "100021";//Vin_Num_001
        status = "uncompleted";

        //Setting the context for the database to the shared database
        Context sharedContext = null;
        try {
            sharedContext = this.createPackageContext("crispit.errorextractor", Context.CONTEXT_INCLUDE_CODE);
            if (sharedContext == null) {
                return;
            }
        } catch (Exception e) {
            String error = e.getMessage();
            return;
        }

        mydb = new DBHelper(sharedContext);


    }


    //Method for showing all error reports in the database via a button
    public void displayReport (View V){
        Intent i = new Intent(this, TestList.class);
        Bundle bundle = new Bundle();
        bundle.putString("busId",busId);
        i.putExtras(bundle);
        startActivity(i);
    }

    //Method for adding reports in the database via a button
    public void addReport (View v){

        new RetrieveBusData().execute("Test");
        resetScreen();

        new AlertDialog.Builder(this)
                .setTitle("Klart!")
                .setMessage("Din felrapport har nu skickats in och behandlas inom kort! Tack!")
                .setNegativeButton("OK", null)
                .show();
    }

    //Method that is called when selecting any of the color buttons
    public void selectColor (View v) {
        v.setSelected(true);
        gradeSelected = true;

        switch (v.getId()) {
            case R.id.colorButton1:
                b2.setSelected(false);
                b3.setSelected(false);
                b4.setSelected(false);
                urgency = 1;
                break;
            case R.id.colorButton2:
                b1.setSelected(false);
                b3.setSelected(false);
                b4.setSelected(false);
                urgency = 2;
                break;
            case R.id.colorButton3:
                b1.setSelected(false);
                b2.setSelected(false);
                b4.setSelected(false);
                urgency = 3;
                break;
            case R.id.colorButton4:
                b1.setSelected(false);
                b2.setSelected(false);
                b3.setSelected(false);
                urgency = 4;
                break;
        }
        unLockSend();
    }

    //Metod for opening a list of symptoms
    public void symptomList(View V){
        Intent i = new Intent(this, SymptomList.class);
        startActivityForResult(i, 1);
    }

    //Method for catching the name of the symptom and adding it to an error report
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == 1) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                symptom = data.getStringExtra("symptom");
                Button symptomButton = (Button)findViewById(R.id.symptom);

                symptomButton.setText(symptom + " ✔");

                symptomSelected = true;
                unLockSend();
            }
        }
    }

    //Method for unlocking the send button once a symptom and grade has been selected
    public void unLockSend(){

        if (gradeSelected && symptomSelected){
            Button sendButton = (Button)findViewById(R.id.sendButton);
            sendButton.setEnabled(true);
            sendButton.setBackgroundResource(R.drawable.buttonn);
        }

    }

    //Method for resetting the screen after sending an error report
    public void resetScreen(){

        symptomSelected = false;
        gradeSelected = false;
        Button symptomButton = (Button)findViewById(R.id.symptom);
        symptomButton.setText("Klicka här ▼");

        Button sendButton = (Button)findViewById(R.id.sendButton);
        sendButton.setEnabled(false);
        sendButton.setBackgroundResource(R.drawable.buttonn_grey);

        b1.setSelected(false);
        b2.setSelected(false);
        b3.setSelected(false);
        b4.setSelected(false);


    }

    //Class where the new error report is inserted to the databases
    private class RetrieveBusData extends AsyncTask<String, String, String> {

        private Exception exception;

        protected String doInBackground(String... str) {
            try {
                HashMap<String,String> map = BusData.getAllBusInfo(busId);
                mydb.insertErrorReport(mydb.getNewErrorId(), symptom, "Kommentar saknas...", busId,
                        calendar.get(Calendar.YEAR)+"-"+(calendar.get(Calendar.MONTH)+1)+"-"+calendar.get(Calendar.DAY_OF_MONTH)+", "+calendar.get(Calendar.HOUR)+":"+calendar.get(Calendar.MINUTE)+":"+calendar.get(Calendar.SECOND),
                        urgency, status, map.get("Accelerator_Pedal_Position"),map.get("Ambient_Temperature"),map.get("At_Stop"),
                        map.get("Cooling_Air_Conditioning"),map.get("Driver_Cabin_Temperature"),map.get("Fms_Sw_Version_Supported"),
                        map.get("GPS"),map.get("GPS2"),map.get("GPS_NMEA"),map.get("Journey_Info"), map.get("Mobile_Network_Cell_Info"), map.get("Mobile_Network_Signal_Strength"),
                        map.get("Next_Stop"),map.get("Offroute"),map.get("Online_Users"), map.get("Opendoor"),
                        map.get("Position_Of_Doors"), map.get("Pram_Request"), map.get("Ramp_Wheel_Chair_Lift"),
                        map.get("Status_2_Of_Doors"), map.get("Stop_Pressed"), map.get("Stop_Request"),
                        map.get("Total_Vehicle_Distance"), map.get("Turn_Signals"),map.get("Wlan_Connectivity"));

            } catch (Exception e) {
                this.exception = e;
                return "Could not insert!";
            }
            return "Insertion successful!";
        }

    }

}


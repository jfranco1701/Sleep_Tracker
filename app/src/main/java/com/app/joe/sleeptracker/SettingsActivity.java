package com.app.joe.sleeptracker;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class SettingsActivity extends AppCompatActivity {

    private SharedPreferences preferenceSettings;
    private SharedPreferences.Editor preferenceEditor;

    private static final int PREFERENCE_MODE_PRIVATE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        PreferenceManager.Init(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Button btnSelectDevice = (Button)findViewById(R.id.btnSelectDevice);
        btnSelectDevice.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {
                        Intent intent = new Intent(SettingsActivity.this, MWScanActivity.class);
                        startActivityForResult(intent, 1);
                    }
                }
        );

        TextView textViewSelectedDevice = (TextView)findViewById(R.id.textViewSelectedDevice);
        textViewSelectedDevice.setText("Selected Device: " + PreferenceManager.readMACAddress());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1) {
            if(resultCode == Activity.RESULT_OK){
                String mac=data.getStringExtra("MAC");

                PreferenceManager.writeMACAddress(mac);

                TextView textViewSelectedDevice = (TextView)findViewById(R.id.textViewSelectedDevice);
                textViewSelectedDevice.setText("Selected Device: " + PreferenceManager.readMACAddress());
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }
    }

}

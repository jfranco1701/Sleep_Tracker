package com.app.joe.sleeptracker;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import com.mbientlab.metawear.MetaWearBleService;
import com.mbientlab.metawear.MetaWearBoard;
import com.mbientlab.metawear.UnsupportedModuleException;
import com.mbientlab.metawear.module.IBeacon;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.util.Log;
import android.widget.Toast;

import static com.mbientlab.metawear.AsyncOperation.CompletionHandler;
import static com.mbientlab.metawear.MetaWearBoard.ConnectionStateHandler;

public class MainActivity extends AppCompatActivity implements ServiceConnection {

    private ListView mDrawerList;
    private ArrayAdapter<String> mAdapter;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private String mActivityTitle;
    private String deviceMACAddress = "";

    private static final int PREFERENCE_MODE_PRIVATE = 0;
    private static final int NO_DEVICE_SELECTED = 99;
    private static final int DEVICE_DISCONNECTED = 0;
    private static final int DEVICE_CONNECTED = 1;

    private MetaWearBleService.LocalBinder serviceBinder;
    private MetaWearBoard mwBoard;

    public MetaWearBoard getBoard(){
        return mwBoard;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        PreferenceManager.Init(this);

        setContentView(R.layout.activity_main);

        Button btnConnect = (Button)findViewById(R.id.btnConnect);
        btnConnect.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {

                        final ProgressDialog connectDialog = new ProgressDialog(MainActivity.this);
                        connectDialog.setTitle(getString(R.string.title_connecting));
                        connectDialog.setMessage(getString(R.string.message_wait));
                        connectDialog.setCancelable(false);
                        connectDialog.setCanceledOnTouchOutside(false);
                        connectDialog.setIndeterminate(true);
                        connectDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.label_cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                mwBoard.disconnect();
                            }
                        });
/*                        connectDialog.show();*/

                        mwBoard.connect();
                    }
                }
        );

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mDrawerList = (ListView)findViewById(R.id.navList);
        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        mActivityTitle = getTitle().toString();

        addDrawerItems();
        setupDrawer();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

//        PreferenceManager.writeMACAddress("");

        deviceMACAddress = PreferenceManager.readMACAddress();

        if (deviceMACAddress == ""){
            setStatus(NO_DEVICE_SELECTED);
        }
        else{
            // Bind the service when the activity is created
            getApplicationContext().bindService(new Intent(this, MetaWearBleService.class), this, BIND_AUTO_CREATE);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Unbind the service when the activity is destroyed
        getApplicationContext().unbindService(this);
    }

/*    private void getMetaWareDevice(){
        final BluetoothManager btManager  = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        final BluetoothDevice btDevice = btManager.getAdapter().getRemoteDevice(deviceMACAddress);
        mwBoard= binder.getMetaWearBoard(btDevice);

        final ProgressDialog connectDialog = new ProgressDialog(this);
        connectDialog.setTitle(getString(R.string.title_connecting));
        connectDialog.setMessage(getString(R.string.message_wait));
        connectDialog.setCancelable(false);
        connectDialog.setCanceledOnTouchOutside(false);
        connectDialog.setIndeterminate(true);
        connectDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.label_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mwBoard.disconnect();
            }
        });
        connectDialog.show();
    }*/

    private void setStatus(int status){
        TextView textViewStatus = (TextView)findViewById(R.id.textViewStatus);
        String strStatus = "";

        switch (status){
            case 0: textViewStatus.setText("Disconnected");
                textViewStatus.setTextColor(Color.BLACK);
                break;
            case 1: textViewStatus.setText("Connected");
                textViewStatus.setTextColor(Color.GREEN);
                break;
            case 99: textViewStatus.setText("No Device Selected");
                textViewStatus.setTextColor(Color.RED);
                break;
            default: textViewStatus.setText("Error");
                textViewStatus.setTextColor(Color.RED);
                break;
        }
    }

    private void addDrawerItems() {
        String[] osArray = { "View Tracking History", "Settings", "About" };
        mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, osArray);
        mDrawerList.setAdapter(mAdapter);

        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent;

                switch (position) {
                    case 0:
                        intent = new Intent(view.getContext(), ViewActivity.class);
                        startActivityForResult(intent, 0);
                        break;
                    case 1:
                        intent = new Intent(view.getContext(), SettingsActivity.class);
                        startActivityForResult(intent, 0);
                        break;
                    case 2:
                        intent = new Intent(view.getContext(), AboutActivity.class);
                        startActivityForResult(intent, 0);
                        break;
                }
            }
        });
    }

    private void setupDrawer() {
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close) {

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getSupportActionBar().setTitle("Sleep Tracker Navigation");
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                getSupportActionBar().setTitle(mActivityTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.addDrawerListener(mDrawerToggle);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

/*    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }*/

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }

        // Activate the navigation drawer toggle
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        // Typecast the binder to the service's LocalBinder class
//        serviceBinder = (MetaWearBleService.LocalBinder) service;

        MetaWearBleService.LocalBinder binder = (MetaWearBleService.LocalBinder) service;

        final BluetoothManager btManager= (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        final BluetoothDevice remoteDevice= btManager.getAdapter().getRemoteDevice(deviceMACAddress);

        binder.executeOnUiThread();
        mwBoard= binder.getMetaWearBoard(remoteDevice);
        mwBoard.setConnectionStateHandler(new ConnectionStateHandler() {
            @Override
            public void connected() {
                Toast.makeText(MainActivity.this, "Connected", Toast.LENGTH_LONG).show();

                setStatus(DEVICE_CONNECTED);

                // get fragment manager
                FragmentManager fm = getFragmentManager();

// add
                FragmentTransaction ft = fm.beginTransaction();

                InfoFragment infFragment = new InfoFragment();
                ft.add(R.id.infocontainer, infFragment);
                ft.attach(infFragment);
                ft.commit();

                Log.i("test", "Connected");
                Log.i("test", "MetaBoot? " + mwBoard.inMetaBootMode());

                mwBoard.readDeviceInformation().onComplete(new CompletionHandler<MetaWearBoard.DeviceInformation>() {
                    @Override
                    public void success(MetaWearBoard.DeviceInformation result) {
                        Log.i("test", "Device Information: " + result.toString());
                    }

                    @Override
                    public void failure(Throwable error) {
                        Log.e("test", "Error reading device information", error);
                    }
                });

                try {
                    mwBoard.getModule(IBeacon.class).readConfiguration().onComplete(new CompletionHandler<IBeacon.Configuration>() {
                        @Override
                        public void success(IBeacon.Configuration result) {
                            Log.i("test", result.toString());
                        }

                        @Override
                        public void failure(Throwable error) {
                            Log.e("test", "Error reading ibeacon configuration", error);
                        }
                    });
                } catch (UnsupportedModuleException e) {
                    Log.e("test", "Cannot get module", e);
                }
            }
            @Override
            public void disconnected() {
                setStatus(DEVICE_DISCONNECTED);

                Toast.makeText(MainActivity.this, "Disconnected", Toast.LENGTH_LONG).show();
                Log.i("test", "Disconnected");
            }

            @Override
            public void failure(int status, final Throwable error) {
                Toast.makeText(MainActivity.this, error.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                Log.e("test", "Error connecting", error);
            }
        });

        if (mwBoard.isConnected()) {
            setStatus(DEVICE_CONNECTED);
        }
        else{
            setStatus(DEVICE_DISCONNECTED);
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {

    }
}

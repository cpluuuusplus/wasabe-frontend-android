/*
 * Copyright 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pfe.ensai.com.wasabe.activities;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import pfe.ensai.com.wasabe.R;
import pfe.ensai.com.wasabe.rest.CallAPI;
import pfe.ensai.com.wasabe.util.DeviceInfo;

/**
 * Sample application demonstrating how to test whether a device is connected,
 * and if so, whether the connection happens to be wifi or mobile (it could be
 * something else).
 * <p/>
 * This sample uses the logging framework to display log output in the log
 * fragment (LogFragment).
 */
public class MainActivity extends Activity implements AdapterView.OnItemSelectedListener {

    public static final String TAG = "MainActivity";
    public static DeviceInfo di;
    // Location related
    public LocationManager locationManager;
    // In order to execute the recurring requests
    ScheduledThreadPoolExecutor executor_;
    private Double latitude;
    private Double longitude;
    private Double precision;
    private String destination;
    // Device identifier, filled in by getDeviceIdentifier()
    private Double deviceID = -1.0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView( pfe.ensai.com.wasabe.R.layout.activity_main);


        // Population de la Spinner des echangeurs
        Spinner spinner = (Spinner) findViewById(R.id.ou_aller);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.sorties_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);

        di = new DeviceInfo();

        executor_ = new ScheduledThreadPoolExecutor(5);
        executor_.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "Running a deviceInfo request");
                // send DeviceInfo via REST
                sendDeviceInfo();

            }
        }, 0, 5, TimeUnit.HOURS);

/*
        if(connectedToInternet()) {







        }else {
            // Tell the user that internet is required
            TextView progress = (TextView) findViewById(R.id.resultat);
            progress.setText("Erreur : Aucune connexion ! Prière de vous connecter à internet afin d'utiliser cette application.");

        }

*/


        // Get DeviceInfo's ID and attribute it to the device


    }


    /**
     * GESTION INTERFACE
     */


    /**
     * Required 1/2 for spinner to update values
     *
     * @param arg0
     * @param arg1
     * @param arg2
     * @param arg3
     */
    @Override
    public void onItemSelected(AdapterView<?> arg0, View arg1,
                               int arg2, long arg3) {
        // Rien ne se paaaasse
    }

    /**
     * Required 1/2 for spinner to update values
     */
    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
        // Rien ne se paaaasse

    }


    public void validerDestination(View v) {
        Spinner spinner = (Spinner) findViewById(R.id.ou_aller);
        Log.d(TAG, " Position sélectionnée : " + spinner.getSelectedItem().toString());

        // initialiser Destination
        di.setDestination(spinner.getSelectedItem().toString());


        // Appel Async
        // send DeviceInfo via REST
        sendDeviceInfo();


    }


    /**
     *
     * FIN GESTION INTERFACE
     */


    /**
     *
     * UTILITAIRES
     */


    /**
     * Envoi de DeviceInfo (contient l'appel asynchrone)
     */
    private void sendDeviceInfo() {
        locateDevice();
        Double unixtimestamp = (double) System.currentTimeMillis() / 1000;
        di.setTemps(unixtimestamp);


        JSONObject jso = createJSONfromDeviceInfo(di);
        String stringedJsonDeviceInfo = "{\"deviceInfo\" :"+ jso.toString() + "}";

        TextView progress = (TextView) findViewById(R.id.resultat);
        new CallAPI(this).execute(stringedJsonDeviceInfo);

    }


    /**
     * Creates a JSON object from a DeviceInfo
     *
     * @param di deviceinfo
     * @return JSONObject
     */
    private JSONObject createJSONfromDeviceInfo(DeviceInfo di) {
        JSONObject res = new JSONObject();

        try {
            res.put("temps", di.getTemps());
            res.put("longitude", di.getLongitude());
            res.put("latitude", di.getLatitude());
            res.put("precision", di.getPrecision());
            res.put("id", di.getId());
            res.put("destination", di.getDestination());

        } catch (JSONException je) {
            System.out.println(TAG + " Exception JSON while creating JSONized DeviceInfo");
            je.printStackTrace();
        }
        return res;
    }

    /**
     * Check whether the device is connected, and if so, whether the connection
     * is wifi or mobile (it could be something else).
     */
    private boolean connectedToInternet() {
        // BEGIN_INCLUDE(connect)
        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeInfo = connMgr.getActiveNetworkInfo();
        if (activeInfo != null && activeInfo.isConnected()) {
            Log.i(TAG, "Connecté à Internet");
            return true;
        } else {
            Log.i(TAG, "Pas connecté à internet");
            return false;
        }
        // END_INCLUDE(connect)
    }


    /**
     * Localiser le device
     */
    private void locateDevice() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        Location lastLocation = locationManager
                .getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (lastLocation != null) {
            updateLoc(lastLocation);
        } else {
            lastLocation = locationManager
                    .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (lastLocation != null) {
                updateLoc(lastLocation);
            } else {
                di.setLatitude(-1000.0);
                di.setLongitude(-1000.0);
                di.setPrecision(-1000.0);
            }
        }
    }

    private void updateLoc(Location location) {
        di.setLatitude(location.getLatitude());
        di.setLongitude(location.getLongitude());
        di.setPrecision(Double.parseDouble(location.getAccuracy() + ""));
    }


}

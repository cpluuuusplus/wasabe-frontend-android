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
import android.content.SharedPreferences;
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
import android.widget.Toast;

import org.json.JSONObject;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import pfe.ensai.com.wasabe.R;
import pfe.ensai.com.wasabe.rest.CallAPI;
import pfe.ensai.com.wasabe.util.DeviceInfo;

/**
 * Cette activite permet de selectionner une porte du peripherique
 * de Rennes; d'envoyer un relevé geolocalisé et
 */
public class MainActivity extends Activity implements AdapterView.OnItemSelectedListener {

    public static final String TAG = "MainActivity";
    private static DeviceInfo di;


    // Location related
    public LocationManager locationManager;
    // In order to execute the recurring requests
    ScheduledThreadPoolExecutor executor_;
    // Device identifier, filled in by getDeviceIdentifier()


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(pfe.ensai.com.wasabe.R.layout.activity_main);


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

        executor_ = new ScheduledThreadPoolExecutor(1);
        executor_.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {

                Log.i("MainActivity:oncreate", "Envoi de DI programmé");
                sendDeviceInfo();

            }
        }, 0, 10, TimeUnit.SECONDS);
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
    }

    /**
     * Required 2/2 for spinner to update values
     */
    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
    }


    /**
     * @param v
     */
    public void validerDestination(View v) {
        Spinner spinner = (Spinner) findViewById(R.id.ou_aller);
        Log.d(TAG, " Position sélectionnée : " + spinner.getSelectedItem().toString());

        // recuperer preferences

        SharedPreferences preferences = this.getSharedPreferences("DeviceInfoIdentifiant", Context.MODE_PRIVATE);
        String record = preferences.getString("DeviceInfoIdentifiant", "");
        Toast.makeText(this, "Identifiant provenant des prefs: " + record, Toast.LENGTH_LONG).show();
        // Sauvegarde sur le device des prefe
        di.setId(record);
        // inliser Destination
        di.setDestination(spinner.getSelectedItem().toString());

        // Appel Async
        // send DeviceInfo via REST
        sendDeviceInfo();
    }


    public void validerTest(View v) {
        String s = "{\"tempsTotal\":1.826527654030028,\"troncons\":[\n" +
                "{\"nomAffiche\":\"14\",\"vitesseMoyenne\":81.208385223939242,\"tempsParcours\":0.12730813645125325,\"etat\":{\"tempsEtatConstant\":8,\"indicatif\":\"fluide\"}}, {\"nomAffiche\":\"13\",\"vitesseMoyenne\":85,\"tempsParcours\":0.3078532625598217,\"etat\":{\"tempsEtatConstant\":7,\"indicatif\":\"fluide\"}}, {\"nomAffiche\":\"12\",\"vitesseMoyenne\":75.120752007668598,\"tempsParcours\":0.16150507600761196,\"etat\":{\"tempsEtatConstant\":6,\"indicatif\":\"fluide\"}}, {\"nomAffiche\":\"11\",\"vitesseMoyenne\":45.241038466751332,\"tempsParcours\":0.08678705523860714,\"etat\":{\"tempsEtatConstant\":7,\"indicatif\":\"ralentie\"}}, {\"nomAffiche\":\"10\",\"vitesseMoyenne\":14.489596210090651,\"tempsParcours\":0.19986795358353263,\"etat\":{\"tempsEtatConstant\":7,\"indicatif\":\"ralentie\"}}, {\"nomAffiche\":\"9\",\"vitesseMoyenne\":6.004624677937102,\"tempsParcours\":0.2602518199323539,\"etat\":{\"tempsEtatConstant\":7,\"indicatif\":\"ralentie\"}}, {\"nomAffiche\":\"8\",\"vitesseMoyenne\":84.3054593822089515,\"tempsParcours\":0.15513759888462447,\"etat\":{\"tempsEtatConstant\":7,\"indicatif\":\"fluide\"}}, {\"nomAffiche\":\"7\",\"vitesseMoyenne\":78.313425059452653,\"tempsParcours\":0.21954427123607673,\"etat\":{\"tempsEtatConstant\":6,\"indicatif\":\"fluide\"}}, {\"nomAffiche\":\"6\",\"vitesseMoyenne\":85.376845964222746,\"tempsParcours\":0.17775497877851865,\"etat\":{\"tempsEtatConstant\":7,\"indicatif\":\"ralentie\"}}, {\"nomAffiche\":\"5\",\"vitesseMoyenne\":45.438582200665044,\"tempsParcours\":0.1305175013576274,\"etat\":{\"tempsEtatConstant\":7,\"indicatif\":\"ralentie\"}}]}\n";
        (new CallAPI(this)).populateItineraireTableFromString(s);
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
        // Si le device a pu être localisé
        if (locateDeviceInSpaceTime()) {
            // Creation du JSON
            JSONObject jso = di.createJSONfromDeviceInfo();
            // Passage du JSON en String
            String stringedJsonDeviceInfo = "{\"deviceInfo\" :" + jso.toString() + "}";
            // Exécution de l'appel asynchrone
            (new CallAPI(this)).execute(stringedJsonDeviceInfo);
        } else {
            Log.e(TAG, "Aucune requete envoyée car on n'a pas la localisation0");
        }
    }


    /**
     * Check whether the device is connected, and if so, whether the connection
     * is wifi or mobile (it could be something else).


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
     */


    /**
     * Localiser le device
     *
     * @return si il a bien été localisé
     */
    private boolean locateDeviceInSpaceTime() {
        // set time
        di.setTemps((double) System.currentTimeMillis() / 1000);

        // set location
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Location lastLocation = locationManager
                .getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (lastLocation != null) {
            // Location provided by GPS
            di.setLatitude(lastLocation.getLatitude());
            di.setLongitude(lastLocation.getLongitude());
            di.setPrecision(Double.parseDouble(lastLocation.getAccuracy() + ""));
            return true;
        } else {
            lastLocation = locationManager
                    .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (lastLocation != null) {
                // location provided by network provider (cell tower triangulation)
                di.setLatitude(lastLocation.getLatitude());
                di.setLongitude(lastLocation.getLongitude());
                di.setPrecision(Double.parseDouble(lastLocation.getAccuracy() + ""));
                return true;
            } else {
                Log.e(TAG, "No location available for device; using test data input inside MainActivity");
                di.setLongitude(-1.627519126161682);
                di.setLatitude(48.091573052747819);
                di.setPrecision(20.0);
                return true;
            }
        }
    }

    /**
     * Sets the DeviceInfo's id, convenience method for CallAPI
     *
     * @param newId
     */
    public void setDeviceInfoId(String newId) {
        di.setId(newId);
    }


}

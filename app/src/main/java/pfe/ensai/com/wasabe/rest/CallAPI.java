package pfe.ensai.com.wasabe.rest;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;

import pfe.ensai.com.wasabe.R;
import pfe.ensai.com.wasabe.activities.MainActivity;

/**
 * Created by nicolas on 08/02/15.
 */
public class CallAPI extends AsyncTask<String, String, String> {


    public static final String TAG = "CallAPI";

    // Context needed to update UI thread
    private MainActivity act = null;

    public CallAPI(MainActivity act) {
        this.act = act;
    }

    /**
     * Utilitaire
     *
     * @param inputStream
     * @return
     * @throws IOException
     */
    private static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while ((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;

    }

    /**
     * Cette classe effectue l'envoi des information sur le device (en JSON)
     * et renvoie une réponse http contenant, si tout va bien, un JSON
     * correspondant à la réponse du serveur
     *
     * @param url
     * @param jsonString
     * @return
     */
    private static HttpResponse sendJsonString(String url, String jsonString, Activity act) {


        System.out.println("CallAPI::sendJsonString : Will send " + jsonString + " to " + url);
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost post = new HttpPost(url);


        // On tente de convertir le DeviceInfo JSONisé Stringizé en StringEntity
        StringEntity se = null;
        try {
            se = new StringEntity(jsonString);
        } catch (UnsupportedEncodingException e) {
            System.err.println("UnsupportedEncodingException ;" + e.getMessage());
        }
        // Construction de la requete POST

        se.setContentEncoding("UTF-8");
        se.setContentType("application/json");

        // Populate t
        post.setEntity(se);
        post.setHeader("Accept", "application/json");
        post.setHeader("Content-type", "application/json");


        // On tente l'envoi de la requete au serveur et la réception du message
        HttpResponse httpResponse = null;
        try {
            httpResponse = httpclient.execute(post);
        } catch (ClientProtocolException e) {
            System.err.println("ClientProtocolException ;" + e.getMessage());
        } catch (IOException e) {
            System.err.println("IOException ;" + e.getMessage());
        }
        return httpResponse;
    }

    @Override
    protected void onPreExecute() {
        TextView tv = (TextView) act.findViewById(R.id.resultat);
        tv.setText("Envoi en cours");

    }


    /***
     *
     * UTILITAIRES
     *
     *
     */

    @Override
    protected String doInBackground(String... params) {

        String json = params[0]; // JSONized DeviceInfo

        // HTTP POST
        Log.i(TAG, "Entered AsyncTask");
        Log.i(TAG, "DeviceInfo :" + json);

        // Envoi de la requête
        HttpResponse httpResponse = sendJsonString("http://10.0.2.2:8080/Wasabe-Server/deviceinfo", json, act);


        if (httpResponse != null) {
            System.out.println("Contenu de la HTTPResponse : " + httpResponse.toString());

            // On tente d'ouvrir le resultat -> InputStream
            InputStream resultIS = null;
            try {
                resultIS = httpResponse.getEntity().getContent();
                //vérif
                System.out.println("Contenu de la InputStream recue : " + resultIS.toString());

            } catch (IOException e) {
                System.err.println("IOException " + e.getMessage());
            } catch (NullPointerException e) {
                System.err.println("Il n'y a rien dans le InputStream ;" + e.getMessage());
            }

            // On tente une conversion en String
            String resultS = null;
            try {
                resultS = convertInputStreamToString(resultIS);
            } catch (IOException e) {
                System.err.println("Impossible de convertir le InputStream en String ;" + e.getMessage());
            }
            // vérif
            System.out.println("Contenu du String recu : " + resultS.toString());
            return resultS;
        } else {
            act.runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(act, "Aucune réponse du serveur (httpresponse null)!", Toast.LENGTH_SHORT).show();
                }
            });
            return "";
        }

    }

    @Override
    protected void onPostExecute(String resultS) {
        TextView tv = (TextView) act.findViewById(R.id.resultat);

        if (resultS.isEmpty()) {
            Log.i("CallAPI", "Réponse vide recue");
            tv.setText("Données enregistrées");
            // Il n'y a rien dans le résultat
            //tv.setText("Réponse vide reçue !");

        } else {
            if (resultS.contains("identifiant")) {
                tv.setText("Inscription effectuée");
                updateDeviceInfoFromString(resultS);
            } else if (resultS.contains("tempsTotal")) {
                tv.setText("Itinéraire reçu");
                populateItineraireTableFromString(resultS);
            } else if (resultS.contains("pasSurPeriph")) {
                tv.setText("Vous n'êtes pas sur le périphérique");
            }
        }
    }

    /**
     * Populates the list view with output from the "itineraire"  string which is a json
     *
     * @param s
     */
    public void populateItineraireTableFromString(String s) {

        try {

            JSONObject itineraireJ = new JSONObject(s);

            Double tempsTotalMinutes = itineraireJ.getDouble("tempsTotal");
            TextView tv = (TextView) act.findViewById(R.id.resultat);
            tv.setText("Arrivée prévue dans "+ String.format("%.0f", tempsTotalMinutes) +" minutes.");


            TableLayout tableContainer = (TableLayout) act.findViewById(R.id.elv_itineraire);
            tableContainer.removeAllViews();
            TableRow row = new TableRow(act);
            LinearLayout.LayoutParams lp_wc_wc = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, (LinearLayout.LayoutParams.WRAP_CONTENT));


            // Nom Troncon
            final TextView tvLabelNomTroncon = new TextView(act.getApplicationContext());
            tvLabelNomTroncon.setLayoutParams(lp_wc_wc);
            tvLabelNomTroncon.setText("Numéro de \n sortie");
            tvLabelNomTroncon.setTextColor(0xFF000000);
            row.addView(tvLabelNomTroncon, new TableRow.LayoutParams(1));


            // vitesse moyenne
            final TextView tvLabelVitesseMoyenne = new TextView(act.getApplicationContext());
            tvLabelVitesseMoyenne.setLayoutParams(lp_wc_wc);
            tvLabelVitesseMoyenne.setText("Vitesse \n Moyenne");
            tvLabelVitesseMoyenne.setTextColor(0xFF000000);
            row.addView(tvLabelVitesseMoyenne, new TableRow.LayoutParams(2));


            // Fluidité
            final TextView tvLabelfluidite = new TextView(act.getApplicationContext());
            tvLabelfluidite.setLayoutParams(lp_wc_wc);
            tvLabelfluidite.setText("Trafic");
            tvLabelfluidite.setTextColor(0xFF000000);

            row.addView(tvLabelfluidite, new TableRow.LayoutParams(3));

            // tempsFluidite
            final TextView tvLabeltempsFluidite = new TextView(act.getApplicationContext());
            tvLabeltempsFluidite.setLayoutParams(lp_wc_wc);
            tvLabeltempsFluidite.setText(" ");
            tvLabeltempsFluidite.setTextColor(0xFF000000);
            row.addView(tvLabeltempsFluidite, new TableRow.LayoutParams(3));

            tableContainer.addView(row, new TableLayout.LayoutParams());


            // On extrait l'array de troncons
            JSONArray tronconsJ =  itineraireJ.getJSONArray("troncons");

            for (int i = 0; i < tronconsJ.length(); i++) {

                /* On extrait le i-eme troncon individuel */
                TableRow rowData = new TableRow(act);
                JSONObject tronconJ = (JSONObject) tronconsJ.get(i);


                // Nom orienté du troncon
                String nomTroncon = tronconJ.getString("nomAffiche");
                // vitesse moyenne
                Double vitesseMoyenne = tronconJ.getDouble("vitesseMoyenne");

                String vitesseMoyenneFormattee = String.format("%.0f", vitesseMoyenne);



                Resources resource = act.getResources();

                if(vitesseMoyenne > 80){
                    rowData.setBackgroundColor(resource.getColor(R.color.lightblue));
                }else if (vitesseMoyenne > 60){
                    rowData.setBackgroundColor(resource.getColor(R.color.yellowgreen));
                }else if (vitesseMoyenne > 40){
                    rowData.setBackgroundColor(resource.getColor(R.color.grey));
                }else if (vitesseMoyenne > 20){
                    rowData.setBackgroundColor(resource.getColor(R.color.orange));
                }else if (vitesseMoyenne > 0) {
                    rowData.setBackgroundColor(resource.getColor(R.color.red));
                }
                rowData.setPadding(1,1,1,1);


                JSONObject etatJ = (JSONObject) tronconJ.getJSONObject("etat");
                // Fluidité
                String fluidite = etatJ.getString("indicatif");
                // tempsFluidite
                Double tempsFluidite = etatJ.getDouble("tempsEtatConstant");
                String tempsFluiditeFormatte = String.format("%.0f", tempsFluidite);

                final TextView tvNomTroncon = new TextView(act.getApplicationContext());
                tvNomTroncon.setLayoutParams(lp_wc_wc);
                tvNomTroncon.setText(nomTroncon);
                tvNomTroncon.setTextColor(0xFF000000);
                rowData.addView(tvNomTroncon, new TableRow.LayoutParams(1));

                final TextView tvVitesseMoyenne = new TextView(act.getApplicationContext());
                tvVitesseMoyenne.setLayoutParams(lp_wc_wc);
                tvVitesseMoyenne.setText(" " + vitesseMoyenneFormattee + " km/h");
                tvVitesseMoyenne.setTextColor(0xFF000000);
                rowData.addView(tvVitesseMoyenne, new TableRow.LayoutParams(2));

                final TextView tvfluidite = new TextView(act.getApplicationContext());
                tvfluidite.setLayoutParams(lp_wc_wc);
                tvfluidite.setText(fluidite);
                tvfluidite.setTextColor(0xFF000000);
                rowData.addView(tvfluidite, new TableRow.LayoutParams(3));

                final TextView tvtempsFluidite = new TextView(act.getApplicationContext());
                tvtempsFluidite.setLayoutParams(lp_wc_wc);
                tvtempsFluidite.setText(" depuis " + tempsFluiditeFormatte + " minutes");
                tvtempsFluidite.setTextColor(0xFF000000);
                rowData.addView(tvtempsFluidite, new TableRow.LayoutParams(3));


                tableContainer.addView(rowData, new TableLayout.LayoutParams());
            }
        }catch(JSONException e){
            Log.e("CallAPI", "Le JSON d'itinéraire est malformé");
        }


    }


    /**
     * Cette methode met à jour le deviceinfo de l'activité principale
     * avec celui fourni par le parametre resultS, qui doit être de la forme suivante :
     * {
     * "identifiant": "550adeb1555a8485a5775453"
     * }
     * <p/>
     * Il met aussi à jour la préférence stockée dans DeviceInfoIdentifiant avec l'identifiant fourni
     *
     * @param resultS
     */
    public void updateDeviceInfoFromString(String resultS) {
        try {
            // Récupération de l'identifiant
            JSONObject jsonId = new JSONObject(resultS);
            String identifiant = jsonId.getString("identifiant");

            // Mise à jour du deviceinfo courant de l'activité
            act.setDeviceInfoId(identifiant);

            // Mise à jour des préférences
            SharedPreferences sp = act.getSharedPreferences("DeviceInfoIdentifiant", Context.MODE_PRIVATE);
            SharedPreferences.Editor edit = sp.edit();
            edit.putString("DeviceInfoIdentifiant", identifiant);
            edit.apply();

        } catch (JSONException e) {
            act.runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(act, "La String de la réponse n'est pas au bon format!", Toast.LENGTH_SHORT).show();
                }
            });
            e.printStackTrace();
        }
    }


    private HttpHost GetHost(URI uri) {
        HttpHost host;
        int port = uri.getPort();
        if (port == -1) {
            port = uri.getScheme().equalsIgnoreCase("https") ? 443 : 80;
        }
        host = new HttpHost(uri.getHost(), port, uri.getScheme());
        return host;
    }


}

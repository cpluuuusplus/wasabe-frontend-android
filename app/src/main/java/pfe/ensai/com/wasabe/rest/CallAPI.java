package pfe.ensai.com.wasabe.rest;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import pfe.ensai.com.wasabe.R;
import pfe.ensai.com.wasabe.activities.MainActivity;
import pfe.ensai.com.wasabe.util.DeviceInfo;

import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;

import static java.net.InetAddress.getLocalHost;

/**
 * Created by nicolas on 08/02/15.
 */
public class CallAPI extends AsyncTask<String, String, String> {


    public static final String TAG = "CallAPI";

    // Context needed to update UI thread
    private MainActivity act = null;

    public CallAPI(MainActivity act){
        this.act = act;
    }




    @Override
    protected void onPreExecute() {

    }



    @Override
    protected void onProgressUpdate(String... values) {
        if (!isCancelled()) {
            ProgressDialog pd = new ProgressDialog(act);
            pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            pd.setMessage("Working...");
            pd.setIndeterminate(true);
            pd.setCancelable(false);
        }
    }


    @Override
    protected String doInBackground(String... params) {

        String json = params[0]; // JSONized DeviceInfo


        // HTTP POST

        Log.i(TAG, "Entered AsyncTask");
        Log.i(TAG, "DeviceInfo :" + json);


        // Envoi de la requête
        HttpResponse httpResponse = sendDeviceInfo("http://10.0.2.2:8080/Wasabe-Server/deviceinfo", json, act);



        //vérif
        // System.out.println("Contenu de la HTTPResponse : "+httpResponse.toString());

        // On tente d'ouvrir le resultat -> InputStream
        InputStream resultIS = null;
        try {
            resultIS = httpResponse.getEntity().getContent();
            //vérif
            System.out.println("Contenu du InputStream : "+resultIS.toString());

        }catch(IOException e){
            System.err.println("IOException "+ e.getMessage());


        }catch(NullPointerException e){
            System.err.println("Il n'y a rien dans le InputStream ;"+ e.getMessage());
        }



        // On tente une conversion en String
        String resultS = null;
        try{
            resultS = convertInputStreamToString(resultIS);
        }catch(IOException e){
            System.err.println("Impossible de convertir le InputStream en String ;"+ e.getMessage());

        }

        // vérif
        System.out.println("Contenu du String : "+resultS.toString());

        return resultS;


    }


    @Override
    protected void onPostExecute(String resultS) {

        DeviceInfo di = decodeDeviceInfo(resultS);
        TextView tv = (TextView) act.findViewById(R.id.resultat);

        // statusTextView.setText("Requete reçue (identifiant attribué : "+di.getId()+")");

        // C'est un peu bancal mais ça marche
        if(di != null && di.getId() != 0.0){
            tv.setText("Requête reçue");
            // Update the MainActivity's DeviceInfo
            MainActivity.di.setId(di.getId());
        }else{
            tv.setText("Erreur de communication avec le serveur");
        }


    }


    /***
     *
     * UTILITAIRES
     *
     *
     */


    /**
     * Utilitaire
     *
     * @param inputStream
     * @return
     * @throws IOException
     */
    private static String convertInputStreamToString(InputStream inputStream) throws IOException{
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;

    }


    /**
     *
     * Cette classe effectue l'envoi des information sur le device (en JSON)
     * et renvoie une réponse http contenant, si tout va bien, un JSON
     * correspondant à la réponse du serveur
     *
     * @param url
     * @param jsonString
     * @return
     */
    private static HttpResponse sendDeviceInfo(String url, String jsonString, Activity act){


        System.out.println("CallAPI::sendDeviceInfo : URL is : " + url);
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost post = new HttpPost(url);



        // On tente de convertir le DeviceInfo JSONisé Stringizé en StringEntity
         StringEntity se = null;
        try {
            se = new StringEntity(jsonString);
        }catch (UnsupportedEncodingException e) {
            System.err.println("UnsupportedEncodingException ;"+ e.getMessage());
        }
        // Construction de la requete POST

        se.setContentEncoding("UTF-8");
        se.setContentType("application/json");

        // Populate te
        post.setEntity(se);
        post.setHeader("Accept", "application/json");
        post.setHeader("Content-type", "application/json");



        // On tente l'envoi de la requete au serveur et la réception du message
        HttpResponse httpResponse = null;
        try {
            httpResponse = httpclient.execute(post);
        }catch(ClientProtocolException e){
            System.err.println("ClientProtocolException ;"+ e.getMessage());
        }catch(IOException e){
            System.err.println("IOException ;"+ e.getMessage());

        }
        return httpResponse;
    }



    private static DeviceInfo decodeDeviceInfo(String resultS){
        // On tente de coercer le résultat en JSON
        JSONObject resultJ = null;
        // Si c'est bon on crée le DeviceInfo contenant l'identifiant à jour
        DeviceInfo di = null;

        try {
            resultJ = new JSONObject(resultS);

            // Extraction de l'information
            double temps = resultJ.getDouble("temps");
            double longitude = resultJ.getDouble("longitude");
            double latitude = resultJ.getDouble("latitude");
            double precision = resultJ.getDouble("precision");
            double id = resultJ.getDouble("id");
            String destination = resultJ.getString("destination");

            di = new DeviceInfo(temps, longitude, latitude, precision, id, destination);
        }catch(JSONException e){
            System.err.println("Le résultat du InputStream n'a pas pu être coercé en JSON ;"+ e.getMessage());

        }

        return di;
    }



    private HttpHost GetHost(URI uri){
        HttpHost host;
        int port=uri.getPort();
        if (port == -1) {
            port=uri.getScheme().equalsIgnoreCase("https") ? 443 : 80;
        }
        host=new HttpHost(uri.getHost(),port,uri.getScheme());
        return host;
    }


} // end CallAPI


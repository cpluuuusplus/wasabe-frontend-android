package pfe.ensai.com.wasabe.util;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by nicolas on 08/02/15.
 */
public class DeviceInfo {
    // Attributes
    private Double temps; // Temps UNIX associé à ce deviceInfo
    private Double latitude; // Mesure de latitude
    private Double longitude; // Mesure de longitude
    private Double precision; // Précision de la mesure de longitude/latitude
    private String id; // Identifiant du device (attribué par le serveur
    private String destination; // nom de porte auquel l'utilisateur veut aller

    // Constructor
    public DeviceInfo() {
        temps = 0.0;
        latitude = 0.0;
        longitude = 0.0;
        precision = 0.0;
        id = "";
        destination = "";
    }

    public DeviceInfo(Double temps, Double latitude, Double longitude, Double precision, String id, String destination) {
        this.temps = temps;
        this.latitude = latitude;
        this.longitude = longitude;
        this.precision = precision;
        this.id = id;
        this.destination = destination;
        System.out.println("DeviceInfo:: DeviceInfo initialisée :" + temps + " " + latitude + " " + longitude + " " + precision + " " + id + " " + destination);
    }


    /**
     * Constructuer sans temps, à utiliser apres la reception des informations
     *
     * @param id
     * @param longitude
     * @param latitude
     * @param precision
     * @param destination
     */
    public DeviceInfo(String id, Double longitude, Double latitude, double precision, String destination) {
        this.temps = 0.0;
        this.latitude = latitude;
        this.longitude = longitude;
        this.precision = precision;
        this.id = id;
        this.destination = destination;
    }

 /*   public DeviceInfo(String resultS){
        JSONObject resultJ = null;
        JSONObject deviceInfoJ = null;
        // On tente de coercer le résultat en JSON
        // Si c'est bon on crée le DeviceInfo contenant l'identifiant à jour
        try {
            resultJ = new JSONObject(resultS);
            deviceInfoJ = resultJ.getJSONObject("deviceInfo");
            // Extraction de l'information
            longitude = deviceInfoJ.getDouble("longitude");
            latitude = deviceInfoJ.getDouble("latitude");
            precision = deviceInfoJ.getDouble("precision");
            id = deviceInfoJ.getString("id");
            destination = deviceInfoJ.getString("destination");

        }catch(JSONException e){
            System.err.println("Le résultat du InputStream n'a pas pu être parsé en JSON ;"+ e.getMessage());

        }
    }*/


    // Getters, Setters


    public Double getTemps() {
        return temps;
    }

    public void setTemps(Double temps) {
        this.temps = temps;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getPrecision() {
        return precision;
    }

    public void setPrecision(Double precision) {
        this.precision = precision;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    @Override
    public String toString() {
        return "{idDevice : " + id + " ; lon:" + longitude + " ; lat" + latitude + " ; precision: " + precision + " destination: " + destination + "}";

    }


    /**
     * Creates a JSON object from the DeviceInfo it's called from
     *
     * @return JSONObject
     */
    public JSONObject createJSONfromDeviceInfo() {
        JSONObject res = new JSONObject();

        try {
            res.put("id", id);
            res.put("longitude", longitude);
            res.put("latitude", latitude);
            res.put("precision", precision);
            res.put("temps", temps);

            String encodedDestination = "";
            // Encode the destination (make it a number)
            if (!destination.isEmpty()) {
                encodedDestination = destination.split(" ")[0];
            }
            res.put("destination", encodedDestination);

        } catch (JSONException je) {
            System.out.println(" Exception JSON while creating JSONized DeviceInfo");
            je.printStackTrace();
        }

        return res;
    }

}

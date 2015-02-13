package pfe.ensai.com.wasabe.util;

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
    public DeviceInfo(){
        temps = 0.0;
        latitude = 0.0;
        longitude = 0.0;
        precision = 0.0;
        id = "";
        destination = "";
    }

    public DeviceInfo(Double temps, Double latitude, Double longitude, Double precision, String id, String destination){
        this.temps = temps;
        this.latitude = latitude;
        this.longitude = longitude;
        this.precision = precision;
        this.id = id;
        this.destination = destination;
        System.out.println("DeviceInfo:: DeviceInfo initialisée :" + temps + " " + latitude + " " + longitude + " " + precision +" " + id +" "+ destination);
    }

    public DeviceInfo(String exception){
        System.out.println("DeviceInfo:: Exception générée");
    }

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
}

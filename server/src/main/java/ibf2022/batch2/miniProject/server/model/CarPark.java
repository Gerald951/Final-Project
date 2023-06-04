package ibf2022.batch2.miniProject.server.model;

public class CarPark {

    private String carParkId;
    private String address;
    private String latitude;
    private String longitude;
    private Integer distance;
    private String cost;
    private String lotsAvailable;

    public String getCarParkId() {
        return carParkId;
    }
    public void setCarParkId(String carParkId) {
        this.carParkId = carParkId;
    }
    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
    }
    public String getLatitude() {
        return latitude;
    }
    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }
    public String getLongitude() {
        return longitude;
    }
    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    @Override
    public String toString() {
        return "CarPark [carParkId=" + carParkId + ", address=" + address + ", latitude=" + latitude + ", longitude="
                + longitude + "]\n";
    }
    public String getCost() {
        return cost;
    }
    public void setCost(String cost) {
        this.cost = cost;
    }
    public String getLotsAvailable() {
        return lotsAvailable;
    }
    public void setLotsAvailable(String lotsAvailable) {
        this.lotsAvailable = lotsAvailable;
    }
    public Integer getDistance() {
        return distance;
    }
    public void setDistance(Integer distance) {
        this.distance = distance;
    }

    

    
    
}

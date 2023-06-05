package ibf2022.batch2.miniProject.server.model;

import java.util.List;

public class Destination {
    private String destination;
    private Integer distance;
    private List<String> listOfParkedTime;
    private List<String> listOfExitTime;
    private List<String> dayOfWeek;
    public String getDestination() {
        return destination;
    }
    public void setDestination(String destination) {
        this.destination = destination;
    }
    public Integer getDistance() {
        return distance;
    }
    public void setDistance(Integer distance) {
        this.distance = distance;
    }
    public List<String> getListOfParkedTime() {
        return listOfParkedTime;
    }
    public void setListOfParkedTime(List<String> listOfParkedTime) {
        this.listOfParkedTime = listOfParkedTime;
    }
    public List<String> getListOfExitTime() {
        return listOfExitTime;
    }
    public void setListOfExitTime(List<String> listOfExitTime) {
        this.listOfExitTime = listOfExitTime;
    }
    
    @Override
    public String toString() {
        return "Destination [destination=" + destination + ", distance=" + distance + ", listOfParkedTime="
                + listOfParkedTime + ", listOfExitTime=" + listOfExitTime + ", dayOfWeek=" + dayOfWeek + "]\n";
    }
    public List<String> getDayOfWeek() {
        return dayOfWeek;
    }
    public void setDayOfWeek(List<String> dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }
    
   
}

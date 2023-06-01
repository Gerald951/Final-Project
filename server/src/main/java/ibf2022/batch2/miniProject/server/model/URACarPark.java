package ibf2022.batch2.miniProject.server.model;

public class URACarPark {
    private String weekday_min;
    private String weekday_rate;
    private String satday_rate;
    private String sunPH_rate;
    private String end_time;
    private String start_time;

    public String getWeekday_rate() {
        return weekday_rate;
    }
    public void setWeekday_rate(String weekday_rate) {
        this.weekday_rate = weekday_rate;
    }
    public String getSatday_rate() {
        return satday_rate;
    }
    public void setSatday_rate(String satday_rate) {
        this.satday_rate = satday_rate;
    }
    public String getSunPH_rate() {
        return sunPH_rate;
    }
    public void setSunPH_rate(String sunPH_rate) {
        this.sunPH_rate = sunPH_rate;
    }
    public String getEnd_time() {
        return end_time;
    }
    public void setEnd_time(String end_time) {
        this.end_time = end_time;
    }
    public String getStart_time() {
        return start_time;
    }
    public void setStart_time(String start_time) {
        this.start_time = start_time;
    }
    public String getWeekday_min() {
        return weekday_min;
    }
    public void setWeekday_min(String weekday_min) {
        this.weekday_min = weekday_min;
    }
    @Override
    public String toString() {
        return "URACarPark [weekday_min=" + weekday_min + ", weekday_rate=" + weekday_rate + ", satday_rate="
                + satday_rate + ", sunPH_rate=" + sunPH_rate + ", end_time=" + end_time + ", start_time=" + start_time
                + "]\n";
    }

    

    
    

}

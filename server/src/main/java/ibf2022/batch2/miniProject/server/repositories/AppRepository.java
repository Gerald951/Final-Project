package ibf2022.batch2.miniProject.server.repositories;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Repository;

import ibf2022.batch2.miniProject.server.exceptions.CarparkIdException;
import ibf2022.batch2.miniProject.server.exceptions.CoordinatesException;
import ibf2022.batch2.miniProject.server.exceptions.NearbyCarparkException;
import ibf2022.batch2.miniProject.server.model.CarPark;
import ibf2022.batch2.miniProject.server.model.Coordinates;
import ibf2022.batch2.miniProject.server.model.ShoppingCarPark;
// import ibf2022.batch2.miniProject.server.model.URACarPark;

@Repository
public class AppRepository {

    public static final String GET_COORDINATES = "select * from shoppingParkingLocation where address=?";
    public static final String SET_LATITUDE = "set @userLatitude=?";
    public static final String SET_LONGITUDE = "set @userLongitude=?";
    public static final String GET_NEARBY_CARPARKS = "select *,(6371000 * Acos (Cos (Radians(@userLatitude)) * Cos(Radians(latitude)) * Cos(Radians(longitude) - Radians(@userLongitude)) + Sin (Radians(@userLatitude)) * Sin(Radians(latitude)))) as distance_m"
                                                        + " from parkingLocation having distance_m < ? order by distance_m";
    public static final String GET_CARPARKS_ID = "select * from parkingLocation where address=?";
    // public static final String GET_URA_RATES_A = "select * from URAcarpark where carpark_id=? and tend_time > TIME(?)";
    // public static final String GET_URA_RATES_B = "select * from URAcarpark where carpark_id=?";
    public static final String GET_SHOPPING_RATES_A = "select * from shoppingCarparkRate where carpark_id=? and range_start<=? and range_end>=? and tend_time > TIME(?)";
    public static final String GET_SHOPPING_RATES_B = "select * from shoppingCarparkRate where carpark_id=? and range_start<=? and range_end>=? and tstart_time < TIME(?)";
    // public static final String MYSQL_URL = "jdbc:mysql://localhost:3306/database";
    // public static final String MYSQL_USER = "root";
    // public static final String MYSQL_PASSWORD = "Sa84684663";


    @Autowired
    private JdbcTemplate jdbcTemplate;

    public Coordinates getCoordinates(String destination) {
        Coordinates coord = jdbcTemplate.query(GET_COORDINATES, new ResultSetExtractor<Coordinates>() {
            @Override
            public Coordinates extractData(ResultSet rs) throws SQLException {
                if (rs.next()) {
                    Coordinates coordinates = new Coordinates();
                    coordinates.setLatitude(Double.parseDouble(rs.getString("latitude")));
                    coordinates.setLongitude(Double.parseDouble(rs.getString("longitude")));
                    return coordinates;
                } else {
                    throw new CoordinatesException("Coordinates Not Found.");
                }
            }
        }, destination);

        return coord;
    }

    public List<CarPark> getNearbyCarparks(Coordinates coord, Integer distance) {
        jdbcTemplate.update(SET_LATITUDE, coord.getLatitude());
        jdbcTemplate.update(SET_LONGITUDE, coord.getLongitude());

        List<CarPark> listOfCP = jdbcTemplate.query(GET_NEARBY_CARPARKS, new ResultSetExtractor<List<CarPark>>() {
            @Override
            public List<CarPark> extractData(ResultSet rs) throws SQLException, DataAccessException {
                List<CarPark> listOfCarParks = new LinkedList<>();
                while (rs.next()) {
                    CarPark carPark = new CarPark();
                    carPark.setCarParkId(rs.getString("carpark_id"));
                    carPark.setAddress(rs.getString("address"));
                    carPark.setLatitude(Double.toString(rs.getDouble("latitude")));
                    carPark.setLongitude(Double.toString(rs.getDouble("longitude")));
                    carPark.setDistance((int) Math.ceil(rs.getDouble("distance_m")));
                    listOfCarParks.add(carPark);
                }

                if (listOfCarParks.isEmpty()) {
                    throw new NearbyCarparkException("There are no nearby carparks");
                } else {
                    return listOfCarParks;
                }
            }
        }
        , distance);

        return listOfCP;
        
    }
    
    public String getCarparkId(String address) {
        String Id = jdbcTemplate.query(GET_CARPARKS_ID, new ResultSetExtractor<String>() {
            @Override
            public String extractData(ResultSet rs) throws SQLException {
                if (rs.next()) {
                    return rs.getString("carpark_id");
                } else {
                    throw new CarparkIdException("No Carpark Id Found.");
                }
            }
        }, address);

        return Id;
    }

    // public List<URACarPark> checkURAcarparkA(String carParkId, String startTime) {
    //     List<URACarPark> listOfURAcp = jdbcTemplate.query(GET_URA_RATES_A, new ResultSetExtractor<List<URACarPark>>() {
    //         @Override
    //         public List<URACarPark> extractData(ResultSet rs) throws SQLException {
    //             List<URACarPark> listOfURAcarparks = new LinkedList<>();
    //             while(rs.next()) {
    //                 URACarPark uraCarPark = new URACarPark();
    //                 uraCarPark.setWeekday_min(rs.getString("weekday_min"));
    //                 uraCarPark.setWeekday_rate(rs.getString("weekday_rate"));
    //                 uraCarPark.setSatday_rate(rs.getString("satday_rate"));
    //                 uraCarPark.setSunPH_rate(rs.getString("sunPH_rate"));
    //                 uraCarPark.setEnd_time(rs.getString("tend_time"));
    //                 uraCarPark.setStart_time(rs.getString("tstart_time"));
    //                 listOfURAcarparks.add(uraCarPark);
    //             }

    //             if (listOfURAcarparks.isEmpty()) {
    //                 return null;
    //             } else {
    //                 return listOfURAcarparks;
    //             }
    //         }
    //     }, carParkId, startTime);

    //     return listOfURAcp;
    // }

    // public List<URACarPark> checkURAcarparkB(String carparkId) {
    //     List<URACarPark> listOfURAcp = jdbcTemplate.query(GET_URA_RATES_B, new ResultSetExtractor<List<URACarPark>>() {
    //         @Override
    //         public List<URACarPark> extractData(ResultSet rs) throws SQLException {
    //             List<URACarPark> listOfURAcarparks = new LinkedList<>();
    //             while(rs.next()) {
    //                 URACarPark uraCarPark = new URACarPark();
    //                 uraCarPark.setWeekday_min(rs.getString("weekday_min"));
    //                 uraCarPark.setWeekday_rate(rs.getString("weekday_rate"));
    //                 uraCarPark.setSatday_rate(rs.getString("satday_rate"));
    //                 uraCarPark.setSunPH_rate(rs.getString("sunPH_rate"));
    //                 uraCarPark.setEnd_time(rs.getString("end_time"));
    //                 uraCarPark.setStart_time(rs.getString("start_time"));
    //                 listOfURAcarparks.add(uraCarPark);
    //             }

    //             if (listOfURAcarparks.isEmpty()) {
    //                 return null;
    //             } else {
    //                 return listOfURAcarparks;
    //             }
    //         }
    //     }, carparkId);

    //     return listOfURAcp;

    // }

    public List<ShoppingCarPark> getShoppingCarparkA(String carparkId, Integer dayOfWeekInt, String startTimeString) {
        List<ShoppingCarPark> listOfShopCP = jdbcTemplate.query(GET_SHOPPING_RATES_A, new ResultSetExtractor<List<ShoppingCarPark>>() {
            @Override
            public List<ShoppingCarPark> extractData(ResultSet rs) throws SQLException {
                List<ShoppingCarPark> listOfShoppingCP = new LinkedList<>();
                while(rs.next()) {
                    ShoppingCarPark sCP = new ShoppingCarPark();
                    sCP.setCarpark_id(rs.getString("carpark_id"));
                    sCP.setAddress(rs.getString("address"));
                    sCP.setRange_start(rs.getInt("range_start"));
                    sCP.setRange_end(rs.getInt("range_end"));
                    sCP.setStart_time(rs.getString("tstart_time"));
                    sCP.setEnd_time(rs.getString("tend_time"));
                    sCP.setMin1(rs.getString("min1"));
                    sCP.setRate1(rs.getString("rate1"));
                    sCP.setMin2(rs.getString("min2"));
                    sCP.setRate2(rs.getString("rate2"));
                    sCP.setMin3(rs.getString("min3"));
                    sCP.setRate3(rs.getString("rate3"));
                    sCP.setMin4(rs.getString("min4"));
                    sCP.setRate4(rs.getString("rate4"));
                    listOfShoppingCP.add(sCP);
                }

                if (listOfShoppingCP.isEmpty()) {
                    return null;
                } else {
                    return listOfShoppingCP;
                }
            }
        }, carparkId, dayOfWeekInt, dayOfWeekInt, startTimeString);

        return listOfShopCP;
    }

    public List<ShoppingCarPark> getShoppingCarparkB(String carparkId, Integer dayOfWeekInt, String endTimeString) {
        List<ShoppingCarPark> listOfShopCP = jdbcTemplate.query(GET_SHOPPING_RATES_B, new ResultSetExtractor<List<ShoppingCarPark>>() {
            @Override
            public List<ShoppingCarPark> extractData(ResultSet rs) throws SQLException {
                List<ShoppingCarPark> listOfShoppingCP = new LinkedList<>();
                while(rs.next()) {
                    ShoppingCarPark sCP = new ShoppingCarPark();
                    sCP.setCarpark_id(rs.getString("carpark_id"));
                    sCP.setAddress(rs.getString("address"));
                    sCP.setRange_start(rs.getInt("range_start"));
                    sCP.setRange_end(rs.getInt("range_end"));
                    sCP.setStart_time(rs.getString("tstart_time"));
                    sCP.setEnd_time(rs.getString("tend_time"));
                    sCP.setMin1(rs.getString("min1"));
                    sCP.setRate1(rs.getString("rate1"));
                    sCP.setMin2(rs.getString("min2"));
                    sCP.setRate2(rs.getString("rate2"));
                    sCP.setMin3(rs.getString("min3"));
                    sCP.setRate3(rs.getString("rate3"));
                    sCP.setMin4(rs.getString("min4"));
                    sCP.setRate4(rs.getString("rate4"));
                    listOfShoppingCP.add(sCP);
                }

                if (listOfShoppingCP.isEmpty()) {
                    return null;
                } else {
                    return listOfShoppingCP;
                }
            }
        }, carparkId, dayOfWeekInt, dayOfWeekInt, endTimeString);

        return listOfShopCP;
    }




    
    
}

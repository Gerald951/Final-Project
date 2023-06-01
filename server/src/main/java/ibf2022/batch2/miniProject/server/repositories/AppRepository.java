package ibf2022.batch2.miniProject.server.repositories;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
import ibf2022.batch2.miniProject.server.model.URACarPark;

@Repository
public class AppRepository {

    public static final String GET_COORDINATES = "select * from shoppingParkingLocation where address=?";
    public static final String SET_LATITUDE = "set @userLatitude=?";
    public static final String SET_LONGITUDE = "set @userLongitude=?";
    public static final String GET_NEARBY_CARPARKS = "select *,(6371000 * Acos (Cos (Radians(@userLatitude)) * Cos(Radians(latitude)) * Cos(Radians(longitude) - Radians(@userLongitude)) + Sin (Radians(@userLatitude)) * Sin(Radians(latitude)))) as distance_m"
                                                        + " from parkingLocation having distance_m < ? order by distance_m";
    public static final String GET_CARPARKS_ID = "select * from parkingLocation where address=?";
    public static final String GET_URA_RATES_A = "select * from URAcarpark where carpark_id=? and tstart_time < TIME(?)";
    public static final String GET_URA_RATES_B = "select * from URAcarpark where carpark_id=? and tstart_time < TIME(?)";
    public static final String MYSQL_URL = "jdbc:mysql://localhost:3306/database";
    public static final String MYSQL_USER = "root";
    public static final String MYSQL_PASSWORD = "Sa84684663";


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
                    carPark.setDistance(rs.getDouble("distance_m"));
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

    public List<URACarPark> checkURAcarparkA(String carParkId, String exitTime) {
        List<URACarPark> listOfURAcp = jdbcTemplate.query(GET_URA_RATES_A, new ResultSetExtractor<List<URACarPark>>() {
            @Override
            public List<URACarPark> extractData(ResultSet rs) throws SQLException {
                List<URACarPark> listOfURAcarparks = new LinkedList<>();
                while(rs.next()) {
                    URACarPark uraCarPark = new URACarPark();
                    uraCarPark.setWeekday_min(rs.getString("weekday_min"));
                    uraCarPark.setWeekday_rate(rs.getString("weekday_rate"));
                    uraCarPark.setSatday_rate(rs.getString("satday_rate"));
                    uraCarPark.setSunPH_rate(rs.getString("sunPH_rate"));
                    uraCarPark.setEnd_time(rs.getString("end_time"));
                    uraCarPark.setStart_time(rs.getString("start_time"));
                    listOfURAcarparks.add(uraCarPark);
                }

                if (listOfURAcarparks.isEmpty()) {
                    return null;
                } else {
                    return listOfURAcarparks;
                }
            }
        }, carParkId, exitTime);

        return listOfURAcp;
    }

    public Map<String,String> getURAcarparkCostB(String carparkId, String timeNow) {

        try {
            Connection connection = DriverManager.getConnection(MYSQL_URL, MYSQL_USER, MYSQL_PASSWORD);

            // Use ResultSet.TYPE_SCROLL_INSENSITIVE for a result set that allows resultSet to navigate in any direction
            PreparedStatement statement = connection.prepareStatement(GET_URA_RATES_B, ResultSet.TYPE_SCROLL_INSENSITIVE);

            // Set the parameters for the prepared statement
            statement.setString(1, carparkId);
            statement.setString(2, timeNow);

            ResultSet resultSet = statement.executeQuery();

            // Perform operations on the result set
            resultSet.last();
                if (resultSet.getString("weekday_min").equalsIgnoreCase("510 mins")) {
                    resultSet.previous();
                    Map<String, String> maps = new HashMap<>();
                    maps.put("weekday_rate", resultSet.getString("weekday_rate"));
                    maps.put("satday_rate", resultSet.getString("satday_rate"));
                    maps.put("sunPH_rate", resultSet.getString("sunPH_rate"));

                    // close the resources when you're done
                    resultSet.close();
                    statement.close();
                    connection.close();

                    return maps;
                } else {
                    Map<String, String> maps = new HashMap<>();
                    maps.put("weekday_rate", Integer.toString(0));
                    maps.put("satday_rate", Integer.toString(0));
                    maps.put("sunPH_rate", Integer.toString(0));

                    resultSet.close();
                    statement.close();
                    connection.close();

                    return maps;
                }
    
        } catch (Exception e) {
            e.getStackTrace();
            return null;
        }
        
        // Map<String, String> cost = jdbcTemplate.query(GET_URA_RATES_B, new ResultSetExtractor<Map<String, String>>() {
        //     @Override
        //     public Map<String, String> extractData(ResultSet rs) throws SQLException {
        //         rs.last();
        //         if (rs.getString("weekday_min").equalsIgnoreCase("510 mins")) {
        //             rs.previous();
        //             Map<String, String> maps = new HashMap<>();
        //             maps.put("weekday", rs.getString("weekday_rate"));
        //             maps.put("satday_rate", rs.getString("satday_rate"));
        //             maps.put("sunPH_rate", rs.getString("sunPH_rate"));
        //             return maps;
        //         } else {
        //             return null;
        //         }
        //     }
        // }, carparkId, timeNow);

    }




    
    
}

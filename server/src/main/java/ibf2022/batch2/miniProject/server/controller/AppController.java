package ibf2022.batch2.miniProject.server.controller;

import java.text.ParseException;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import ibf2022.batch2.miniProject.server.Utils;
import ibf2022.batch2.miniProject.server.exceptions.NearbyCarparkException;
import ibf2022.batch2.miniProject.server.model.CarPark;
import ibf2022.batch2.miniProject.server.model.Destination;
import ibf2022.batch2.miniProject.server.services.AppServices;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;

@CrossOrigin(origins = "https://mini-project-angular-blue.vercel.app", maxAge = 3600, allowCredentials = "true")
@Controller
@RequestMapping("/api")
public class AppController {

    @Autowired
    private AppServices appServices;

    @GetMapping(path="/search/lot")
    public ResponseEntity<String> searchLotAvailability(@RequestParam(required = true) String destinationId, @RequestParam(required = true) String carparkId) {
        String lotAvailability = appServices.getLotAvailability(destinationId, carparkId);

       
        Integer lotInteger = Integer.parseInt(lotAvailability);
            if (lotInteger > 10) {
            JsonObject ok = Json.createObjectBuilder().add("OK", lotAvailability).build();

            return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(ok.toString());
        } else if (lotInteger > 0 && lotInteger <= 10) {
            JsonObject notOk = Json.createObjectBuilder().add("Not OK", lotAvailability).build();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(notOk.toString());
        } else {
            JsonObject err = Json.createObjectBuilder().add("error", "Internal Server Error During Lot Retrieval.").build();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(err.toString());
        }
        
    }

    @PostMapping(path="/search/redirect", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<String> searchNearbyCarParks(@RequestBody MultiValueMap<String, String> formData) {
        System.out.println("HELPPPPP");
        Destination destination = new Destination();

        destination.setId(formData.getFirst("id"));
        destination.setDestination(formData.getFirst("destination"));
        destination.setDistance(Integer.parseInt(formData.getFirst("distance")));
        destination.setListOfParkedTime(Arrays.asList(formData.getFirst("listOfParkedTime").split(", ")));
        destination.setListOfExitTime(Arrays.asList(formData.getFirst("listOfExitTime").split(", ")));
        destination.setDayOfWeek(Arrays.asList(formData.getFirst("dayOfWeek").split(", ")));

        try {
            List<CarPark> listOfCarParks = appServices.getNearbyCarParks(destination.getId(), destination.getDestination(), destination.getDistance(), destination.getListOfParkedTime(), destination.getListOfExitTime(), destination.getDayOfWeek());
            
            JsonArrayBuilder arr = Json.createArrayBuilder();
            System.out.println("SUCCESS");
            for (int i = 0; i<listOfCarParks.size(); i++) {
                System.out.println(listOfCarParks.get(i).getAddress().trim());
                JsonObject jo = Json.createObjectBuilder().add("carParkId", listOfCarParks.get(i).getCarParkId())
                                                            .add("address", Utils.capitalizeFirstLetter(listOfCarParks.get(i).getAddress().trim()))
                                                            .add("latitude", listOfCarParks.get(i).getLatitude())
                                                            .add("longitude", listOfCarParks.get(i).getLongitude())
                                                            .add("distance", listOfCarParks.get(i).getDistance())
                                                            .add("cost", listOfCarParks.get(i).getCost())
                                                            .add("lotsAvailable", listOfCarParks.get(i).getLotsAvailable())
                                                            .build();
                arr.add(jo);                                            
            }

            JsonArray array = arr.build();

            Destination dest = Utils.insertQuotes(destination);

            appServices.insertDocument(dest, array);

            return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(array.toString());
        } catch (NearbyCarparkException | ParseException e) {
            JsonObject err = Json.createObjectBuilder().add("error", "No Nearby CarParks Found.").build();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).contentType(MediaType.APPLICATION_JSON).body(err.toString());
        }
        
    }

    @GetMapping(path="/search/{id}/{distance}")
    public ResponseEntity<String> searchDocument(@PathVariable(required = true) String id, @PathVariable(required = true) String distance) {
        String joString = appServices.getCarParkById(id, Integer.parseInt(distance));

        // try {
            // JsonObject result = Utils.stringToJson(joString);
            return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(joString);
            
        // } catch (IOException e) {
        //     // TODO Auto-generated catch block
        //     JsonObject err = Json.createObjectBuilder().add("error", "No document of id=%s is found".formatted(id)).build();
        //     return ResponseEntity.status(HttpStatus.NOT_FOUND).contentType(MediaType.APPLICATION_JSON).body(err.toString());
        // }

        
    }

    @DeleteMapping(path="/delete/{id}")
    public ResponseEntity<String> deleteDocument(@PathVariable(required = true) String id) {
        if (appServices.deleteRecord(id) == true) {
            JsonObject ok = Json.createObjectBuilder().add("OK", "Records Deleted").build();

            return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(ok.toString());
        } else {
            JsonObject ok = Json.createObjectBuilder().add("NOT OK", "Problems in record deletion").build();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(ok.toString());
        }
    }
}

    
    


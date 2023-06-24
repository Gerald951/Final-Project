package ibf2022.batch2.miniProject.server.repositories;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;

import ibf2022.batch2.miniProject.server.model.CarPark;
import ibf2022.batch2.miniProject.server.model.Destination;
import jakarta.json.JsonArray;

@Repository
public class MongoRepository {

    @Autowired
    private MongoTemplate mongoTemplate;

    public Boolean insertDestination(Destination destination, JsonArray listOfCP) {
        Document doc = new Document();

        doc.append("id", destination.getId()).append("destination", destination.getDestination()).append("distance", destination.getDistance())
            .append("listOfParkedTime", destination.getListOfParkedTime().toString()).append("listOfExitTime", destination.getListOfExitTime().toString())
            .append("dayOfWeek", destination.getDayOfWeek().toString()).append("listOfCarParks", listOfCP.toString());
		
		Document inserted = mongoTemplate.insert(doc, "archives");

		if (inserted !=null) {
			return true;
		} else {
			return false;
		}
        
    }

	public Boolean insertCarpark(String id, CarPark cp) {

		Query query = Query.query(Criteria.where("id").is(id).and("carparkId").is(cp.getCarParkId()));

		Document result = mongoTemplate.findOne(query, Document.class, "carpark");

		if (result == null) {
			// insert a new doc
			Document doc = new Document();

			SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss");

			doc.append("id", id).append("carparkId", cp.getCarParkId())
			.append("carparkAddress", cp.getAddress()).append("lotAvailability", cp.getLotsAvailable()).append("time", dateFormat.format(new Date()));

			Document inserted = mongoTemplate.insert(doc, "carpark");

			if (inserted !=null) {
				return true;
			} else {
				return false;
			}

		} else {
			// update and replace existing document
			Update update = new Update();

			SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss");

			update.set("lotAvailability", cp.getLotsAvailable()).set("time", dateFormat.format(new Date()));

			UpdateResult updatedResult = mongoTemplate.updateFirst(query, update, "carpark");

			if (updatedResult.getMatchedCount() > 0) {
				return true;
			} else {
				return false;
			}

		}   

	}

	public String getLotAvailability(String id, String carparkId) {
		Query query = Query.query(Criteria.where("id").is(id).and("carparkId").is(carparkId));

		Document result = mongoTemplate.findOne(query, Document.class, "carpark");

		if (result == null) {
			return null;
		} else {
			return (String) result.get("lotAvailability");
		}
	}
    
    public String getCarParkById(String id, Integer distance) {
		Query query = Query.query(Criteria.where("id").is(id).and("distance").is(distance));

		Document result = mongoTemplate.findOne(query, Document.class, "archives");

		if (result == null) {
			return null;
		} else {
			return result.toJson();
		}

		
	}

	public Boolean deleteRecord(String id) {
		Query query = new Query(Criteria.where("id").is(id));
    	DeleteResult result = mongoTemplate.remove(query, "archives");
		DeleteResult result2 = mongoTemplate.remove(query, "carpark");
		
		String count = Long.toString(result.getDeletedCount());
		String count2 = Long.toString(result2.getDeletedCount());

		return Integer.parseInt(count) != 0 && Integer.parseInt(count2) != 0 ? true : false ;
	}
    
}

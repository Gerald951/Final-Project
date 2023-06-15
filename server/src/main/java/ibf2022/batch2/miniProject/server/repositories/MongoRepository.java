package ibf2022.batch2.miniProject.server.repositories;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.mongodb.client.result.DeleteResult;

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
		
		String count = Long.toString(result.getDeletedCount());

		return Integer.parseInt(count) != 0 ? true : false ;
	}
    
}

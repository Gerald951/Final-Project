package ibf2022.batch2.miniProject.server.services;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import ibf2022.batch2.miniProject.server.Utils;
import ibf2022.batch2.miniProject.server.exceptions.CoordinatesException;
import ibf2022.batch2.miniProject.server.exceptions.NearbyCarparkException;
import ibf2022.batch2.miniProject.server.model.CarPark;
import ibf2022.batch2.miniProject.server.model.Coordinates;
import ibf2022.batch2.miniProject.server.model.Destination;
import ibf2022.batch2.miniProject.server.model.ShoppingCarPark;
import ibf2022.batch2.miniProject.server.repositories.AppRepository;
import ibf2022.batch2.miniProject.server.repositories.MongoRepository;
import jakarta.json.JsonArray;

@Service
public class AppServices {

    private final String shoppingCenters_URL = "https://sgcarparks.atpeaz.com/";
    private final String carParks_URL = "https://sgcarparks.atpeaz.com/h";
	List<CarPark> listOfCarParks = null;
	String destinationId = null;

    @Autowired
    private AppRepository appRepository;

	@Autowired
	private MongoRepository mongoRepository;
    
	@Scheduled(fixedDelay = 8500)
    public void getLotAvailability() {
		
		if (listOfCarParks != null && destinationId != null) {

			for (CarPark c : listOfCarParks) {
				if (Utils.isShopping(c.getCarParkId())) {
					Integer shoppingCenterID = Utils.getMapOfLocationId().get(c.getAddress().trim());
					System.out.println(c.getAddress());

					try {
						URL obj = new URL(shoppingCenters_URL);
						HttpURLConnection con = (HttpURLConnection) obj.openConnection();
						// optional request header
						con.setRequestProperty("User-Agent", "Mozilla/5.0");
						int responseCode = con.getResponseCode();
						System.out.println("Response code: " + responseCode);
						BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
						String inputLine;
						StringBuilder response = new StringBuilder();

						while ((inputLine = in.readLine()) != null) {
						response.append(inputLine);
						}

						in.close();
						String html = response.toString();
						// System.out.println(html);

						//Parse HTML content using Jsoup
						Document doc = Jsoup.parse(html);
						// get the season parking lot availability
						
						String trId = "tr#" + Integer.toString(shoppingCenterID); 
						
						Element trElement = doc.selectFirst(trId);

						if (trElement != null) {
							Element tdElement = trElement.selectFirst("td:nth-child(2)");
							c.setLotsAvailable(Utils.checkForLot(tdElement.text()));
							mongoRepository.insertCarpark(destinationId, c);

						} else {
							c.setLotsAvailable("No Data");
							mongoRepository.insertCarpark(destinationId, c);
						}	

					} catch (Exception e) {
						System.out.println(e.getMessage());
						c.setLotsAvailable("No Data");
						mongoRepository.insertCarpark(destinationId, c);
					}
				} else {
					
					String carparkId = appRepository.getCarparkId(c.getAddress());
					
					System.out.println(carparkId);

					try {
						URL obj = new URL(carParks_URL);
						HttpURLConnection con = (HttpURLConnection) obj.openConnection();
						// optional request header
						con.setRequestProperty("User-Agent", "Mozilla/5.0");
						int responseCode = con.getResponseCode();
						System.out.println("Response code: " + responseCode);
						BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
						String inputLine;
						StringBuilder response = new StringBuilder();

						while ((inputLine = in.readLine()) != null) {
						response.append(inputLine);
						}

						in.close();
						String html = response.toString();
						// System.out.println(html);

						//Parse HTML content using Jsoup
						Document doc = Jsoup.parse(html);
						// get the season parking lot availability
						String trId = "tr#" + carparkId; 
						Element trElement = doc.selectFirst(trId);

						if (trElement != null) {
							Element tdElement = trElement.selectFirst("td:nth-child(2)");
							c.setLotsAvailable(Utils.checkForLot(tdElement.text()));
							mongoRepository.insertCarpark(destinationId, c);
						} else {
							c.setLotsAvailable("No Data");
							mongoRepository.insertCarpark(destinationId, c);
						}

					} catch (Exception e) {
						System.out.println(e.getMessage());
						c.setLotsAvailable("No Data");
						mongoRepository.insertCarpark(destinationId, c);
					}
				}
			}
			
		} else {
			
		}
        

    }


	public Integer getLotAvailability(String sc) {
		Integer shoppingCenterID = Utils.getMapOfLocationId().get(sc);
		System.out.println(sc);

		try {
			URL obj = new URL(shoppingCenters_URL);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			// optional request header
			con.setRequestProperty("User-Agent", "Mozilla/5.0");
			int responseCode = con.getResponseCode();
			System.out.println("Response code: " + responseCode);
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuilder response = new StringBuilder();

			while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
			}

			in.close();
			String html = response.toString();
			// System.out.println(html);

			//Parse HTML content using Jsoup
			Document doc = Jsoup.parse(html);
			// get the season parking lot availability
			
			String trId = "tr#" + Integer.toString(shoppingCenterID); 
			
			Element trElement = doc.selectFirst(trId);

			if (trElement != null) {
				Element tdElement = trElement.selectFirst("td:nth-child(2)");
				return Integer.parseInt(tdElement.text());
			} else {
				return null;
			}	

		} catch (Exception e) {
			System.out.println(e.getMessage());
			return 0;
		}
	}

	public String getCarParkLotAvailability(String cp) {
		System.out.println(cp.toUpperCase());
		String carparkId = appRepository.getCarparkId(cp.toUpperCase());
		
		System.out.println(carparkId);

		try {
			URL obj = new URL(carParks_URL);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			// optional request header
			con.setRequestProperty("User-Agent", "Mozilla/5.0");
			int responseCode = con.getResponseCode();
			System.out.println("Response code: " + responseCode);
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuilder response = new StringBuilder();

			while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
			}

			in.close();
			String html = response.toString();
			// System.out.println(html);

			//Parse HTML content using Jsoup
			Document doc = Jsoup.parse(html);
			// get the season parking lot availability
			String trId = "tr#" + carparkId; 
			Element trElement = doc.selectFirst(trId);

			if (trElement != null) {
				Element tdElement = trElement.selectFirst("td:nth-child(2)");
				return tdElement.text();
			} else {
				return null;
			}

		} catch (Exception e) {
			System.out.println(e.getMessage());
			return null;
		}
	}

    public List<CarPark> getNearbyCarParks(String id, String destination, Integer distance, List<String> parkedTime, List<String> exitTime, List<String> dayOfWeek) throws CoordinatesException, NearbyCarparkException, ParseException {
		Coordinates coord = appRepository.getCoordinates(destination);

		destinationId = id;
		listOfCarParks = appRepository.getNearbyCarparks(coord, distance);
		System.out.println(listOfCarParks);

		// Get Lot Availability
		for (CarPark c : listOfCarParks) {
			String input;
			if (Utils.isShopping(c.getCarParkId())) {
				
				input = Integer.toString(getLotAvailability(c.getAddress().trim()));
			} else {
				
				input = getCarParkLotAvailability(c.getAddress());
			}
			
			if (input.equals("0*")) {
				c.setLotsAvailable(Integer.toString(0));
			} else {
				c.setLotsAvailable(input);
			}
			
		}

		// in 24h format
		List<String> startTimeString = parkedTime;
		List<String> endTimeString = exitTime;
		List<String> dayOfWeekString = dayOfWeek;

		// Get Total Rates
		for (CarPark c : listOfCarParks) {
			System.out.println("carpark address:%s".formatted(c.getAddress()));
			if (Utils.getListOfHDBCarParks().contains(c.getCarParkId())) {
				// CarPark is HDB
				if (Utils.isCarparkCentral(c.getCarParkId())) {
					// CarPark is within central
					Double totalCost = 0.0;
					for (int i = 0; i<startTimeString.size(); i++) {
						totalCost += Utils.checkTimeForHDBRates(startTimeString.get(i), endTimeString.get(i), dayOfWeekString.get(i));
					}
					
					c.setCost(Utils.toTwoDecimalPlaces(totalCost));
					
				} else {
					// HDB outside central rates
					Double totalCost = 0.0;

					for (int i = 0; i<startTimeString.size(); i++) {
						totalCost += Utils.getHDBRates(startTimeString.get(i), endTimeString.get(i), 0.60);
					}
					
					c.setCost(Utils.toTwoDecimalPlaces(totalCost));
									
				}
			} else if (Utils.isShopping(c.getCarParkId())) {	
				// Shopping centers charges
				Double totalCost = 0.0;
				for (int i = 0; i<startTimeString.size(); i++) {
					if (i==0) {
						Integer dayOfWeekInt = Utils.dayOfWeekInteger(dayOfWeekString.get(i));

						List<ShoppingCarPark> listOfShoppingCP = appRepository.getShoppingCarparkA(c.getCarParkId(), dayOfWeekInt, startTimeString.get(i));

						Double cost = Utils.getTotalShoppingCostA(listOfShoppingCP, startTimeString.get(i), endTimeString.get(i));
						totalCost += cost;
					} else {
						Integer dayOfWeekInt = Utils.dayOfWeekInteger(dayOfWeekString.get(i));

						List<ShoppingCarPark> listOfShoppingCPB = appRepository.getShoppingCarparkB(c.getCarParkId(), dayOfWeekInt, endTimeString.get(i));

						Double cost = Utils.getTotalShoppingCostB(listOfShoppingCPB, startTimeString.get(i), endTimeString.get(i));
						totalCost += cost;
					}
				}

				c.setCost(Utils.toTwoDecimalPlaces(totalCost));
			
			} else {
				
				c.setCost("No Data");
			}
		}

		return listOfCarParks;
	}

	public Boolean insertDocument(Destination destination, JsonArray arr) {
		return mongoRepository.insertDestination(destination, arr);
	}

	public String getLotAvailability(String id, String carparkId) {
		return mongoRepository.getLotAvailability(id, carparkId);
	}

	public String getCarParkById(String id, Integer distance) {
		return mongoRepository.getCarParkById(id, distance);
	}

	public Boolean deleteRecord(String id) {
		listOfCarParks = null;
		destinationId = null;
		return mongoRepository.deleteRecord(id);
	}
}

				
						
					
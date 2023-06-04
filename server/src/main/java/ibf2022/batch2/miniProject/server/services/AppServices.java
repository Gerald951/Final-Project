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
import org.springframework.stereotype.Service;

import ibf2022.batch2.miniProject.server.Utils;
import ibf2022.batch2.miniProject.server.exceptions.CoordinatesException;
import ibf2022.batch2.miniProject.server.exceptions.NearbyCarparkException;
import ibf2022.batch2.miniProject.server.model.CarPark;
import ibf2022.batch2.miniProject.server.model.Coordinates;
import ibf2022.batch2.miniProject.server.model.ShoppingCarPark;
import ibf2022.batch2.miniProject.server.model.URACarPark;
import ibf2022.batch2.miniProject.server.repositories.AppRepository;

@Service
public class AppServices {

    private final String shoppingCenters_URL = "https://sgcarparks.atpeaz.com/";
    private final String carParks_URL = "https://sgcarparks.atpeaz.com/h";

    @Autowired
    private AppRepository appRepository;
    
    public Integer getLotAvailability(String shoppingCenter) {
		
        Integer shoppingCenterID = Utils.getMapOfLocationId().get(shoppingCenter);

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

    public String getCarParkLotAvailability(String carPark) {
		String carparkId = appRepository.getCarparkId(carPark);

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

    public List<CarPark> getNearbyCarParks(String destination, Integer distance, String parkedTime, String exitTime, String dayOfWeek) throws CoordinatesException, NearbyCarparkException, ParseException {
		Coordinates coord = appRepository.getCoordinates(destination);
		List<CarPark> listOfCarParks = appRepository.getNearbyCarparks(coord, distance);

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
		String startTimeString = parkedTime;
		String endTimeString = exitTime;
		String dayOfWeekString = dayOfWeek;

		// Get Total Rates
		for (CarPark c : listOfCarParks) {
			if (Utils.getListOfHDBCarParks().contains(c.getCarParkId())) {
				// CarPark is HDB
				if (Utils.isCarparkCentral(c.getCarParkId())) {
					// CarPark is within central
					try {
						String cost = Utils.checkTimeForHDBRates(parkedTime, exitTime);
						c.setCost(cost);
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
						
				} else {
					// HDB outside central rates
					try {
						String roundedCost = Utils.getHDBRates(startTimeString, endTimeString, 0.60);
						c.setCost(roundedCost);
					} catch (ParseException e) {
						e.getStackTrace();
					}					
				}
			} else if (Utils.isShopping(c.getCarParkId())) {	
				// Shopping centers charges
				Integer dayOfWeekInt = Utils.dayOfWeekInteger(dayOfWeekString);

				List<ShoppingCarPark> listOfShoppingCP = appRepository.getURAcarparkCostB(c.getCarParkId(), dayOfWeekInt, endTimeString);


			} else {
				// URA charges
				List<URACarPark> listOfURAcp = appRepository.checkURAcarparkA(c.getCarParkId(), endTimeString);
				double totalCost = 0;

				if (listOfURAcp != null) {
					String tStart24hFormat = Utils.get24hDateFormat(listOfURAcp.get(0).getStart_time());
					
					// check if tstart(cp start time) is after startTime
					if (Utils.isAfter(tStart24hFormat, startTimeString)) {
						Boolean isOvernight = (listOfURAcp.get(listOfURAcp.size()-1).getWeekday_min().equalsIgnoreCase("510 mins"));
						// check if there is overnight charging
						if (isOvernight) {
							if  (listOfURAcp.get(listOfURAcp.size()-1).getWeekday_min().equalsIgnoreCase("510 mins")) {
								listOfURAcp.remove(listOfURAcp.size()-1);
							}

							for (int i = 0; i < listOfURAcp.size()-1; i++) {
								if (i == listOfURAcp.size() - 1) {
									Long secondsDifference = Utils.getSecondsA(listOfURAcp.get(i).getStart_time(), endTimeString);
									String rate = null;
									switch (Utils.getRateBasedOnDay(dayOfWeekString)) {
										case "sunPH_rate":
											rate = listOfURAcp.get(i).getSunPH_rate();
										case "satday_rate":
											rate = listOfURAcp.get(i).getSatday_rate();
										default:
											rate = listOfURAcp.get(i).getWeekday_rate();
									}

									Double cost = secondsDifference*Double.parseDouble(rate)/30/60;
									totalCost += cost;
								} else {
									Long secondsDifference = Utils.getSecondsB(listOfURAcp.get(i).getStart_time(), listOfURAcp.get(i).getEnd_time());
									String rate = null;
									switch (Utils.getRateBasedOnDay(dayOfWeekString)) {
										case "sunPH_rate":
											rate = listOfURAcp.get(i).getSunPH_rate();
										case "satday_rate":
											rate = listOfURAcp.get(i).getSatday_rate();
										default:
											rate = listOfURAcp.get(i).getWeekday_rate();
									}

									Double cost = secondsDifference*Double.parseDouble(rate)/30/60;
									totalCost += cost;
								}
							}
							c.setCost(Double.toString(totalCost));
						} else {
							for (int i = 0; i<listOfURAcp.size(); i++) {
								if (i == listOfURAcp.size()-1) {
									Long secondsDifference = Utils.getSecondsA(listOfURAcp.get(i).getStart_time(), endTimeString);
									String rate = null;
									switch (Utils.getRateBasedOnDay(dayOfWeekString)) {
										case "sunPH_rate":
											rate = listOfURAcp.get(i).getSunPH_rate();
										case "satday_rate":
											rate = listOfURAcp.get(i).getSatday_rate();
										default:
											rate = listOfURAcp.get(i).getWeekday_rate();
									}

									Double cost = secondsDifference*Double.parseDouble(rate)/30/60;
									totalCost += cost;
								} else {
									Long secondsDifference = Utils.getSecondsB(listOfURAcp.get(i).getStart_time(), listOfURAcp.get(i).getEnd_time());
									String rate = null;
									switch (Utils.getRateBasedOnDay(dayOfWeekString)) {
										case "sunPH_rate":
											rate = listOfURAcp.get(i).getSunPH_rate();
										case "satday_rate":
											rate = listOfURAcp.get(i).getSatday_rate();
										default:
											rate = listOfURAcp.get(i).getWeekday_rate();
									}

									Double cost = secondsDifference*Double.parseDouble(rate)/30/60;
									totalCost += cost;
								}
							}
							c.setCost(Double.toString(totalCost));
						}
					} else {
						if  (listOfURAcp.get(listOfURAcp.size()-1).getWeekday_min().equalsIgnoreCase("510 mins")) {
							listOfURAcp.remove(listOfURAcp.size()-1);
						}

						for (int i = 0; i<listOfURAcp.size(); i++) {
							if (i == 0) {
								Long secondsDifference = Utils.getSecondsC(startTimeString, listOfURAcp.get(i).getEnd_time());
								String rate = null;
								switch (Utils.getRateBasedOnDay(dayOfWeekString)) {
									case "sunPH_rate":
										rate = listOfURAcp.get(i).getSunPH_rate();
									case "satday_rate":
										rate = listOfURAcp.get(i).getSatday_rate();
									default:
										rate = listOfURAcp.get(i).getWeekday_rate();
								}

								Double cost = secondsDifference*Double.parseDouble(rate)/30/60;
								totalCost += cost;
							} else if (i == listOfURAcp.size()-1) {
								Long secondsDifference = Utils.getSecondsA(listOfURAcp.get(i).getStart_time(), endTimeString);
								String rate = null;
									switch (Utils.getRateBasedOnDay(dayOfWeekString)) {
										case "sunPH_rate":
											rate = listOfURAcp.get(i).getSunPH_rate();
										case "satday_rate":
											rate = listOfURAcp.get(i).getSatday_rate();
										default:
											rate = listOfURAcp.get(i).getWeekday_rate();
									}

									Double cost = secondsDifference*Double.parseDouble(rate)/30/60;
									totalCost += cost;
							} else {
								Long secondsDifference = Utils.getSecondsB(listOfURAcp.get(i).getStart_time(), listOfURAcp.get(i).getEnd_time());
								String rate = null;
								switch (Utils.getRateBasedOnDay(dayOfWeekString)) {
									case "sunPH_rate":
										rate = listOfURAcp.get(i).getSunPH_rate();
									case "satday_rate":
										rate = listOfURAcp.get(i).getSatday_rate();
									default:
										rate = listOfURAcp.get(i).getWeekday_rate();
								}

								Double cost = secondsDifference*Double.parseDouble(rate)/30/60;
								totalCost += cost;
							}
						}
						c.setCost(Double.toString(totalCost));
							
					}
				
				} else {
					// NULL (parking done after 12am)
					List<URACarPark> listOfURAcpB = appRepository.getURAcarparkCostB(c.getCarParkId());

					if (listOfURAcpB != null) {
						Boolean isOvernight = (listOfURAcpB.get(listOfURAcpB.size()-1).getWeekday_min()).equalsIgnoreCase("510 mins");

						if (isOvernight) {
							// Overnight charging
							String tstart24hFormat = Utils.get24hDateFormat(listOfURAcpB.get(0).getStart_time());

							if (Utils.isAfter(tstart24hFormat, startTimeString)) {
								if  (listOfURAcpB.get(listOfURAcpB.size()-1).getWeekday_min().equalsIgnoreCase("510 mins")) {
									listOfURAcpB.remove(listOfURAcpB.size()-1);
								}
								// tStart is after startTimeString
								for (int i = 0; i < listOfURAcpB.size()-1; i++) {
									if (i == listOfURAcpB.size() - 1) {
										Long secondsDifference = Utils.getSecondsA(listOfURAcpB.get(i).getStart_time(), endTimeString);
										String rate = null;
										switch (Utils.getRateBasedOnDay(dayOfWeekString)) {
											case "sunPH_rate":
												rate = listOfURAcpB.get(i).getSunPH_rate();
											case "satday_rate":
												rate = listOfURAcpB.get(i).getSatday_rate();
											default:
												rate = listOfURAcpB.get(i).getWeekday_rate();
										}
	
										Double cost = secondsDifference*Double.parseDouble(rate)/30/60;
										totalCost += cost;
									} else {
										Long secondsDifference = Utils.getSecondsB(listOfURAcpB.get(i).getStart_time(), listOfURAcpB.get(i).getEnd_time());
										String rate = null;
										switch (Utils.getRateBasedOnDay(dayOfWeekString)) {
											case "sunPH_rate":
												rate = listOfURAcpB.get(i).getSunPH_rate();
											case "satday_rate":
												rate = listOfURAcpB.get(i).getSatday_rate();
											default:
												rate = listOfURAcpB.get(i).getWeekday_rate();
										}
	
										Double cost = secondsDifference*Double.parseDouble(rate)/30/60;
										totalCost += cost;
									}
								}
								c.setCost(Double.toString(totalCost));

							} else {
								if  (listOfURAcpB.get(listOfURAcpB.size()-1).getWeekday_min().equalsIgnoreCase("510 mins")) {
									listOfURAcpB.remove(listOfURAcpB.size()-1);
								}

								for (int i = 0; i<listOfURAcpB.size(); i++) {
									if (i == 0) {
										Long secondsDifference = Utils.getSecondsC(startTimeString, listOfURAcpB.get(i).getEnd_time());
										String rate = null;
											switch (Utils.getRateBasedOnDay(dayOfWeekString)) {
												case "sunPH_rate":
													rate = listOfURAcpB.get(i).getSunPH_rate();
												case "satday_rate":
													rate = listOfURAcpB.get(i).getSatday_rate();
												default:
													rate = listOfURAcpB.get(i).getWeekday_rate();
											}
			
											Double cost = secondsDifference*Double.parseDouble(rate)/30/60;
											totalCost += cost;
									} else if (i == listOfURAcpB.size()-1) {
										Long secondsDifference = Utils.getSecondsA(listOfURAcpB.get(i).getStart_time(), endTimeString);
										String rate = null;
											switch (Utils.getRateBasedOnDay(dayOfWeekString)) {
												case "sunPH_rate":
													rate = listOfURAcpB.get(i).getSunPH_rate();
												case "satday_rate":
													rate = listOfURAcpB.get(i).getSatday_rate();
												default:
													rate = listOfURAcpB.get(i).getWeekday_rate();
											}
		
											Double cost = secondsDifference*Double.parseDouble(rate)/30/60;
											totalCost += cost;
									} else {
										Long secondsDifference = Utils.getSecondsB(listOfURAcpB.get(i).getStart_time(), listOfURAcpB.get(i).getEnd_time());
										String rate = null;
										switch (Utils.getRateBasedOnDay(dayOfWeekString)) {
											case "sunPH_rate":
												rate = listOfURAcpB.get(i).getSunPH_rate();
											case "satday_rate":
												rate = listOfURAcpB.get(i).getSatday_rate();
											default:
												rate = listOfURAcpB.get(i).getWeekday_rate();
										}
		
										Double cost = secondsDifference*Double.parseDouble(rate)/30/60;
										totalCost += cost;
									}
								}
								c.setCost(Double.toString(totalCost));

							}
						} else {
							String tstart24hFormat = Utils.get24hDateFormat(listOfURAcpB.get(0).getStart_time());

							if (Utils.isAfter(tstart24hFormat, startTimeString)) {
								for (int i = 0; i<listOfURAcpB.size(); i++) {
									Long secondsDifference = Utils.getSecondsB(listOfURAcpB.get(i).getStart_time(), listOfURAcpB.get(i).getEnd_time());
									String rate = null;
									switch (Utils.getRateBasedOnDay(dayOfWeekString)) {
										case "sunPH_rate":
											rate = listOfURAcpB.get(i).getSunPH_rate();
										case "satday_rate":
											rate = listOfURAcpB.get(i).getSatday_rate();
										default:
											rate = listOfURAcpB.get(i).getWeekday_rate();
									}
	
									Double cost = secondsDifference*Double.parseDouble(rate)/30/60;
									totalCost += cost;
								}
								c.setCost(Double.toString(totalCost));
							} else {
								for (int i = 0; i<listOfURAcpB.size(); i++) {
									if (i == 0) {
										Long secondsDifference = Utils.getSecondsC(startTimeString, listOfURAcpB.get(i).getEnd_time());
										String rate = null;
											switch (Utils.getRateBasedOnDay(dayOfWeekString)) {
												case "sunPH_rate":
													rate = listOfURAcpB.get(i).getSunPH_rate();
												case "satday_rate":
													rate = listOfURAcpB.get(i).getSatday_rate();
												default:
													rate = listOfURAcpB.get(i).getWeekday_rate();
											}
			
											Double cost = secondsDifference*Double.parseDouble(rate)/30/60;
											totalCost += cost;
									} else {
										Long secondsDifference = Utils.getSecondsB(listOfURAcpB.get(i).getStart_time(), listOfURAcpB.get(i).getEnd_time());
										String rate = null;
										switch (Utils.getRateBasedOnDay(dayOfWeekString)) {
											case "sunPH_rate":
												rate = listOfURAcpB.get(i).getSunPH_rate();
											case "satday_rate":
												rate = listOfURAcpB.get(i).getSatday_rate();
											default:
												rate = listOfURAcpB.get(i).getWeekday_rate();
										}

										Double cost = secondsDifference*Double.parseDouble(rate)/30/60;
										totalCost += cost;
									}
								}
								c.setCost(Double.toString(totalCost));
							}
						}
					} else {
						c.setCost("No Data");
					}
				}
			}
		}

		return listOfCarParks;
	}
}

				
						
					
package ibf2022.batch2.miniProject.server;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.opencsv.CSVReader;

import ibf2022.batch2.miniProject.server.model.ShoppingCarPark;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;

public class Utils {
    
    public final static String[] carParksWithinCentral = {"ACB", "BBB", "BRB1", "CY", "DUXM", "HLM", "KAB", "KAM", "KAS", "PRM", "SLS", "SR1", "SR2", "TPM", "UCS", "WCB"};
	public final static String[] shoppingCenters = {"Harbourfront Centre", "Resorts World Sentosa", "VivoCity P2", "VivoCity P3", "Sentosa", "Westgate", "IMM Building", "JCube", "National Gallery", "Singapore Flyer", 
													"Millenia Singapore", "The Esplanade", "Raffles City", "Marina Square", "Suntec City", "Marina Bay Sands", "Centrepoint", "Cineleisure", "Orchard Point", "Concorde Hotel", "Plaza Singapura",
													"The Cathay", "Mandarin Hotel", "Wisma Atria", "The Heeren", "Ngee Ann City", "Orchard Central", "Wheelock Place", "Orchard Gateway", "Tang Plaza", "Far East Plaza", "Paragon",
													"313@Somerset", "The Atrium@Orchard", "Bukit Panjang Plaza", "Clarke Quay", "The Star Vista", "Funan Mall", "Lot One", "Tampines Mall", "Junction 8", "Bedok Mall", "Bugis+"};
	public final static Integer[] shoppingCenters_ID = {19,26,50,16,17,43,53,54,56,6,5,4,3,2,1,29,21,11,7,22,9,10,12,14,8,13,27,15,52,18,20,55,24,57,58,59,60,66,62,63,64,65,61};
	public final static String[] shoppingCenters_rates = {"Harbourfront Centre", "Resorts World Sentosa RWS", "VivoCity P2", "VivoCity P3", "Sentosa", "Westgate", "IMM Building", "JCube", "National Gallery", "Singapore Flyer", 
													"Millenia Walk", "The Esplanade", "Raffles City", "Marina Square", "Suntec City", "Marina Bay Sands MBS", "Centrepoint", "Cineleisure Orchard", "Orchard Point", "Concorde Hotel", "Plaza Singapura",
													"The Cathay", "Mandarin Hotel", "Wisma Atria", "The Heeren", "Ngee Ann City Takashimaya", "Orchard Central", "ION Orchard", "Wheelock Place", "Orchard Gateway", "Tang Plaza", "Far East Plaza", "Paragon",
													"313@Somerset", "The Atrium@Orchard", "Bukit Panjang Plaza", "Clarke Quay", "The Star Vista", "Funan", "Lot One", "Tampines Mall", "Junction 8", "Bedok Mall", "Bugis Plus"};

	public final static String[] HDB_CARPARK_WITHIN_CENTRAL = {"ACB", "BBB", "BRB1", "CY", "DUXM", "HLM", "KAB", "KAM", "KAS", "PRM", "SLS", "SR1", "SR2", "TPM", "UCS", "WCB"};


    public static List<String> getListOfHDBCarParks() {
        String rootDirectory = System.getProperty("user.dir");
        String relativePath = "src/main/resources/HDB Carpark.csv";

        Path filePath = Paths.get(rootDirectory, relativePath);
        String absolutePath = filePath.toAbsolutePath().toString();

		List<String> list = new ArrayList<>();
		try {
			
			File file = new File(absolutePath);
			FileReader rdr = new FileReader(file);
			list = getColumnAsList(0, rdr);

		} catch (Exception e) {
			// TODO: handle exception
		}

		return list;
    }

    public static List<String> getColumnAsList(int columnNumber, FileReader rdr) {
		List<String> columnList = new ArrayList<>();

		try (CSVReader reader = new CSVReader(rdr)) {
			String[] nextLine;
			while ((nextLine = reader.readNext()) != null) {
				if (columnNumber < nextLine.length) {
					columnList.add(nextLine[columnNumber]);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return columnList;
	}

	public static Map<String, Integer> getMapOfLocationId() {
		Map<String, Integer> maps = new HashMap<>();

		for (int i = 0; i<shoppingCenters.length; i++) {
			maps.put(shoppingCenters[i], shoppingCenters_ID[i]);
		}

		return maps;
	}

	public static Boolean isCarparkCentral(String carparkId) {
		Boolean b = false;

		for (String s : HDB_CARPARK_WITHIN_CENTRAL) {
			if (s.equalsIgnoreCase(carparkId)) {
				b = true;
				break;
			} else {
				continue;
			}
		}

		return b;
	}

	public static Double checkTimeForHDBRates(String startTime, String endTime, String dayOfWeek) throws ParseException {

		//define the parking start and end times
		LocalTime start = LocalTime.parse(startTime);
        LocalTime end = LocalTime.parse(endTime);

		//define carpark start and end time
		LocalTime carParkStart = LocalTime.of(7, 0, 0);
		LocalTime carParkEnd = LocalTime.of(17,0,0);

		if (carParkStart.isBefore(start) && carParkEnd.isAfter(end) && !dayOfWeek.equalsIgnoreCase("Sun")) {
			return getHDBRates(startTime, endTime, 1.20);
		} else if (carParkStart.isBefore(start) && carParkEnd.isBefore(end) && !dayOfWeek.equalsIgnoreCase("Sun")) {
			
				return getHDBRates(startTime, "17:00:00", 1.20) 
					+ getHDBRates("17:00:00", endTime, 0.60);
			
			
		} else if (carParkStart.isAfter(start) && carParkEnd.isAfter(end) && !dayOfWeek.equalsIgnoreCase("Sun")) {
			return getHDBRates(startTime, "07:00:00", 0.60)
					+ getHDBRates("07:00:00", endTime, 1.20);
			
		} else if (carParkStart.isAfter(start) && carParkEnd.isBefore(end) && !dayOfWeek.equalsIgnoreCase("Sun")){
			return getHDBRates(startTime, "07:00:00", 0.60)
					+ getHDBRates("07:00:00", "17:00:00", 1.20)
					+ getHDBRates("17:00:00", endTime, 0.60);
		} else {
			return getHDBRates(startTime, endTime, 0.60);
		}
	}

	public static Double getHDBRates(String startTime, String endTime, Double rate) throws ParseException {
		SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
		Date startTimeDate = format.parse(startTime);
		Date endTimeDate = format.parse(endTime);

		long durationInMillis = endTimeDate.getTime() - startTimeDate.getTime();

		return getHDBRoundedCost(durationInMillis, rate);
		
	
	}

	public static Double getHDBRoundedCost(Long durationInMillis, Double rate) {
		long secondsDifference = TimeUnit.MILLISECONDS.toSeconds(durationInMillis);
		double totalCost = rate*secondsDifference/30/60;

		String roundedCost = toTwoDecimalPlaces(totalCost);

		return Double.parseDouble(roundedCost);
	}

	public static Boolean isAfter(String tStart, String startTimeString) {
		LocalTime startTimeCP = LocalTime.parse(tStart);
        LocalTime startTimeParked = LocalTime.parse(startTimeString);

		return startTimeCP.isAfter(startTimeParked) ? true : false;

	}

	public static Long getMinutes(String startTime, String endTime) throws ParseException {
		String startTimeString = startTime;
		String endTimeString = endTime;

		SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");

		Date startTimeDate = format.parse(startTimeString);
		Date endTimeDate = format.parse(endTimeString);

		long durationInMillis = endTimeDate.getTime() - startTimeDate.getTime();
		System.out.println(durationInMillis);
		
		long minutesDifference = TimeUnit.MILLISECONDS.toMinutes(durationInMillis);
		if (durationInMillis % 60000 > 0) {
			minutesDifference++;
		}

		return minutesDifference;
	}

	public static List<String> getListOfMin(ShoppingCarPark sCP) {
		List<String> listOfMin  = new LinkedList<>();
		listOfMin.add(sCP.getMin1());
		listOfMin.add(sCP.getMin2());
		listOfMin.add(sCP.getMin3());
		listOfMin.add(sCP.getMin4());
		return listOfMin;
	}

	public static List<String> getListOfRates(ShoppingCarPark sCP) {
		List<String> listOfRates = new LinkedList<>();
		listOfRates.add(sCP.getRate1());
		listOfRates.add(sCP.getRate2());
		listOfRates.add(sCP.getRate3());
		listOfRates.add(sCP.getRate4());
		return listOfRates;
	}

	public static Double getTotalShoppingCostA(List<ShoppingCarPark> sCP, String startTimeString, String endTimeString) throws ParseException{
		Long minutes = (long) 0;

		if (sCP.size() > 1) {
			for (int i = 0; i<sCP.size(); i++) {
				Long min = (long) 0;
				if (i==0) {
					min = getMinutes(startTimeString, sCP.get(0).getEnd_time());
				} else if (i==sCP.size()-1) {
					min = getMinutes(sCP.get(sCP.size()-1).getStart_time(), endTimeString);
				} else {
					min = getMinutes(sCP.get(i).getStart_time(), sCP.get(i).getEnd_time());
				}
				minutes += min;
			}
		} else {
			minutes = getMinutes(startTimeString, endTimeString);
		}

		Double cost = getCulmulativeCost(minutes, getListOfMin(sCP.get(0)), getListOfRates(sCP.get(0)));
		return cost;
	}

	public static Double getTotalShoppingCostB(List<ShoppingCarPark> sCP, String startTimeString, String endTimeString) throws ParseException{
		Long minutes = (long) 0;

		if (sCP.size() > 1) {
			for (int i = 0; i<sCP.size(); i++) {
				Long min = (long) 0;
				if (i==0) {
					min = getMinutes(sCP.get(0).getStart_time(), sCP.get(0).getEnd_time());
				} else if (i==sCP.size()-1) {
					min = getMinutes(sCP.get(sCP.size()-1).getStart_time(), endTimeString);
				} else {
					min = getMinutes(sCP.get(i).getStart_time(), sCP.get(i).getEnd_time());
				}
				minutes += min;
			}
		} else {
			minutes = getMinutes(sCP.get(0).getStart_time(), endTimeString);
		}		

		Double cost = getCulmulativeCost(minutes, getListOfMin(sCP.get(0)), getListOfRates(sCP.get(0)));
		return cost;
	}

	public static Double getCulmulativeCost(Long minutes, List<String> listOfMin, List<String> listOfRates) {
		int i = 0;
		int z = 0;
		Double cost = (double) 0;

		if (listOfMin.get(0).equals("0")) {
			return Double.parseDouble(listOfRates.get(0));
		} else if (listOfMin.get(0).equals("-")) {
			return Double.parseDouble("0");
		} else if ((listOfMin.get(0).equals("1")) || (listOfMin.get(0).equals("15"))) {
			Long rounds = minutes/(Long.parseLong(listOfMin.get(0)));
			if (minutes % (Long.parseLong(listOfMin.get(0))) > 0) {
				rounds ++;
			}

			Double calculatedCost = rounds * Double.parseDouble(listOfRates.get(0));

			return calculatedCost;
		} else {
			while (minutes > 0) {
				minutes -= Long.parseLong(listOfMin.get(i));

				if (i<3) {
					i++;
					continue;
				} else {
					z++;
					continue;
				}
			}

			for (int j = 0; j<i; j++) {
				cost += Double.parseDouble(listOfRates.get(j));
			}

			for (int a = 0; a<z; a++) {
				cost += Double.parseDouble(listOfRates.get(3));
			}

			return cost;
		}
	}

	public static Boolean isShopping(String id) {
		Boolean isShopping = false;

		try {
			Integer shoppingId = Integer.parseInt(id);
			for (Integer i : shoppingCenters_ID) {
				if (i == shoppingId) {
					isShopping = true;
					break;
				} else {
					continue;
				}
			}
			
			return isShopping;
		} catch (NumberFormatException e) {
			return isShopping;
		}
		

	}

	public static Integer dayOfWeekInteger(String dayOfWeek) {
		switch (dayOfWeek) {
			case "Mon":
				return 1;
			case "Tue":
				return 2;
			case "Wed":
				return 3;
			case "Thu":
				return 4;
			case "Fri":
				return 5;
			case "Sat":
				return 6;
			default:
				return 7;
		}
	}

	public static String toTwoDecimalPlaces(double cost) {
		DecimalFormat decimalFormat = (DecimalFormat) NumberFormat.getInstance(Locale.US);
		decimalFormat.applyPattern("#0.00");
		decimalFormat.setRoundingMode(java.math.RoundingMode.HALF_UP);

		String roundedCost = decimalFormat.format(cost);

		return roundedCost;
	}

	public static JsonObject stringToJson(String s) throws IOException {
        try (InputStream is = new ByteArrayInputStream(s.getBytes())) {
            JsonReader jrd = Json.createReader(is);
            JsonObject jo = jrd.readObject();
            return jo;
        }
    }

	
    
}

package ibf2022.batch2.miniProject.server;

import java.io.File;
import java.io.FileReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.opencsv.CSVReader;

public class Utils {
    
    public final static String[] carParksWithinCentral = {"ACB", "BBB", "BRB1", "CY", "DUXM", "HLM", "KAB", "KAM", "KAS", "PRM", "SLS", "SR1", "SR2", "TPM", "UCS", "WCB"};
	public final static String[] shoppingCenters = {"Harbourfront Centre", "Resorts World Sentosa", "VivoCity P2", "VivoCity P3", "Sentosa", "Westgate", "IMM Building", "JCube", "National Gallery", "Singapore Flyer", 
													"Millenia Singapore", "The Esplanade", "Raffles City", "Marina Square", "Suntec City", "Marina Bay Sands", "Centrepoint", "Cineleisure", "Orchard Point", "Concorde Hotel", "Plaza Singapura",
													"The Cathay", "Mandarin Hotel", "Wisma Atria", "The Heeren", "Ngee Ann City", "Orchard Central", "ION Orchard", "Wheelock Place", "Orchard Gateway", "Tang Plaza", "Far East Plaza", "Paragon",
													"313@Somerset", "The Atrium@Orchard", "Bukit Panjang Plaza", "Clarke Quay", "The Star Vista", "Funan Mall", "Lot One", "Tampines Mall", "Junction 8", "Bedok Mall", "Bugis+"};
	public final static Integer[] shoppingCenters_ID = {19,26,50,16,17,43,53,54,56,6,5,4,3,2,1,29,21,11,7,22,9,10,12,14,8,13,27,23,15,52,18,20,55,24,57,58,59,60,66,62,63,64,65,61};
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

	public static String checkTimeForHDBRates(String startTime, String endTime) throws ParseException {
		//define the day
		LocalTime currTime = LocalTime.now();
		DayOfWeek day = DayOfWeek.from(currTime);

		//define the parking start and end times
		LocalTime start = LocalTime.parse(startTime);
        LocalTime end = LocalTime.parse(endTime);

		//define carpark start and end time
		LocalTime carParkStart = LocalTime.of(7, 0, 0);
		LocalTime carParkEnd = LocalTime.of(17,0,0);

		if (carParkStart.isBefore(start) && carParkEnd.isAfter(end) && day!=DayOfWeek.SUNDAY) {
			return getHDBRates(startTime, endTime, 1.20);
		} else if (carParkStart.isBefore(start) && carParkEnd.isBefore(end) && day!=DayOfWeek.SUNDAY) {
			return toTwoDecimalPlaces(getIntermediateRates(startTime, "17:00:00", 1.20) 
					+ getIntermediateRates("17:00:00", endTime, 0.60));
		} else {
			return getHDBRates(startTime, endTime, 0.60);
		}
	}

	public static String getHDBRates(String startTime, String endTime, Double rate) throws ParseException {
		SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
		Date startTimeDate = format.parse(startTime);
		Date endTimeDate = format.parse(endTime);

		long durationInMillis = endTimeDate.getTime() - startTimeDate.getTime();
		long secondsDifference = TimeUnit.MILLISECONDS.toSeconds(durationInMillis);
		double totalCost = rate*secondsDifference/30/60;

		String roundedCost = toTwoDecimalPlaces(totalCost);

		return roundedCost;
	}

	public static Double getIntermediateRates(String startTime, String endTime, Double rate) throws ParseException {
		SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
		Date startTimeDate = format.parse(startTime);
		Date endTimeDate = format.parse(endTime);

		long durationInMillis = endTimeDate.getTime() - startTimeDate.getTime();
		long secondsDifference = TimeUnit.MILLISECONDS.toSeconds(durationInMillis);
		double totalCost = rate*secondsDifference/30/60;

		return totalCost;
	}


	public static String get24hDateFormat(String time) {
		String timeString = time;
        DateFormat inputFormat = new SimpleDateFormat("h.mm a");
        DateFormat outputFormat = new SimpleDateFormat("HH:mm:ss");

        try {
            Date date = inputFormat.parse(timeString);
            String convertedTime = outputFormat.format(date);
            return convertedTime;
        } catch (ParseException e) {
            e.printStackTrace();
			return null;
        }
	}

	public static Boolean isAfter(String tStart, String startTimeString) {
		LocalTime startTimeCP = LocalTime.parse(tStart);
        LocalTime startTimeParked = LocalTime.parse(startTimeString);

		return startTimeCP.isAfter(startTimeParked) ? true : false;

	}

	public static Long getSecondsA(String tStart, String endTimeString) throws ParseException {
		String startTimeCP = get24hDateFormat(tStart);

		SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
		Date startTimeDate = format.parse(startTimeCP);
		Date endTimeDate = format.parse(endTimeString);

		long durationInMillis = endTimeDate.getTime() - startTimeDate.getTime();
		long secondsDifference = TimeUnit.MILLISECONDS.toSeconds(durationInMillis);

		return secondsDifference;
	}

	public static Long getSecondsB(String tStart, String tEnd) throws ParseException {
		String startTimeCP = get24hDateFormat(tStart);
		String endTimeCP = get24hDateFormat(tEnd);

		SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
		Date startTimeDate = format.parse(startTimeCP);
		Date endTimeDate = format.parse(endTimeCP);

		long durationInMillis = endTimeDate.getTime() - startTimeDate.getTime();
		long secondsDifference = TimeUnit.MILLISECONDS.toSeconds(durationInMillis);

		return secondsDifference;
	}

	public static Long getSecondsC(String startTimeString, String tEnd) throws ParseException {
		String endTimeCP = get24hDateFormat(tEnd);

		SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
		Date startTimeDate = format.parse(startTimeString);
		Date endTimeDate = format.parse(endTimeCP);

		long durationInMillis = endTimeDate.getTime() - startTimeDate.getTime();
		long secondsDifference = TimeUnit.MILLISECONDS.toSeconds(durationInMillis);

		return secondsDifference;
	}

	public static String getRateBasedOnDay(String dayOfWeek) {
		switch (dayOfWeek) {
			case "Sun":
				return("sunPH_rate");
			case "Sat":
				return("satday_rate");
			default:
				return("weekday_rate");
		}
	}

	public static String getDayOfWeek() {
		Date dateNow = new Date();
		SimpleDateFormat format = new SimpleDateFormat("E");
		String dayOfWeek = format.format(dateNow);

		return dayOfWeek;
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

	public static String toTwoDecimalPlaces(double cost) {
		DecimalFormat decimalFormat = (DecimalFormat) NumberFormat.getInstance(Locale.US);
		decimalFormat.applyPattern("#0.00");
		decimalFormat.setRoundingMode(java.math.RoundingMode.HALF_UP);

		String roundedCost = decimalFormat.format(cost);

		return roundedCost;
	}

	
    
}

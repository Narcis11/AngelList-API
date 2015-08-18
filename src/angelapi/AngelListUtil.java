package angelapi;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class AngelListUtil {
		private static String HTTP_GET = "GET";
		final static String QUERY_ACCESS_TOKEN ="access_token";
		final static String accessToken = "c79af3178afd7f7ab3ccac4ebad366eb4e54ca7c07384a56";
		static String mErrorMesssage;
		
		public static HashMap getInvestorInfo(String[] investorList, String BASE_INVESTOR_SEARCH_URL){
			//int[] investorIds = new int[investorList.length];
			HashMap investorInfo = new HashMap();
			final String NO_RESULT = "[]";
			String toUseInvestorURL;
			String name;
			String id;
			int countInvestors = 0;
			final String JSON_ID = "id";
			final String JSON_NAME = "name";
			try {
				for (int i = 0; i < investorList.length; i ++) {
					//construct the URL
					toUseInvestorURL = BASE_INVESTOR_SEARCH_URL + investorList[i];
					URL urlObject = new URL(toUseInvestorURL);
					System.out.println(toUseInvestorURL);
					//establish the connection
					HttpURLConnection httpConnection = (HttpURLConnection) urlObject.openConnection();
					httpConnection.setRequestMethod(HTTP_GET);
					int responseCode = httpConnection.getResponseCode();
					//process the output
					BufferedReader in = new BufferedReader(
					        new InputStreamReader(httpConnection.getInputStream()));
					String inputLine;
					StringBuffer response = new StringBuffer();
			 
					while ((inputLine = in.readLine()) != null) {
						response.append(inputLine);
					}
					in.close();
					toUseInvestorURL = "";
					//print result
					//System.out.println("Result is: " + response.toString());
					//parse the result
					String jsonResponse = response.toString();
					if (!jsonResponse.equals(NO_RESULT)) {

					JSONArray investorJsonArray = new JSONArray(jsonResponse);
					if (investorJsonArray.length() > 1) {
						for (int x = 0; x < investorJsonArray.length(); x++) {
							JSONObject investorJsonObject = investorJsonArray.getJSONObject(x);
							id = String.valueOf(investorJsonObject.getInt(JSON_ID));
							name = investorJsonObject.getString(JSON_NAME);
							String returnedLastName = name.substring(name.indexOf(" ") + 1);
							String originalLastName = Constants.originalInvestorList[i].substring(Constants.originalInvestorList[i].indexOf(" ") + 1);
							if (returnedLastName.equals(originalLastName)){//we have a match 
								investorInfo.put(id, name); 
								}
						}
					}
					else {//when only one investor is returned
						JSONObject investorJsonObject = investorJsonArray.getJSONObject(0);
						id = String.valueOf(investorJsonObject.getInt(JSON_ID));
						name = investorJsonObject.getString(JSON_NAME);
						investorInfo.put(id, name); 
					}
					}
				}
				
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			
			return investorInfo;
			
		}
		
		public static ArrayList<Integer> getStartupsIds(int investorId, String toUseStartupURL) {
			//int[] startupsIds = new int[100];
			ArrayList<Integer> startupsIds = new ArrayList<Integer>();
			final String ERROR_MESSAGE = "error";
			final String ROOT_NODE = "startup_roles";
			final String JSON_TOTAL = "total";
			final String JSON_LAST_PAGE = "last_page";
			final String JSON_STARTUP = "startup";
			final String JSON_ID = "id";
			final String JSON_SUCCESS = "success";
			final String SUCCESS_FALSE = "false";
			int startupId;
			boolean flag_next_page = true;//used to determine whether the investor is involved in more than 50 startups
			int lastPage = 0;
			int countLoops = 0;
			
			while (flag_next_page) {
			try {
				if (lastPage > 1)
					toUseStartupURL = toUseStartupURL + "?page=" + String.valueOf(lastPage);
				//System.out.println("lastPage/countLoops at start: " + lastPage + "/" + countLoops);
				URL urlObject = new URL(toUseStartupURL);
				System.out.println("toUseStartupURL: " + toUseStartupURL);
				//establish the connection
				HttpURLConnection httpConnection = (HttpURLConnection) urlObject.openConnection();
				httpConnection.setRequestMethod(HTTP_GET);
				//int responseCode = httpConnection.getResponseCode();
				//process the output
				BufferedReader in = new BufferedReader(
				        new InputStreamReader(httpConnection.getInputStream()));
				String inputLine;
				StringBuffer response = new StringBuffer();
		 
				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
				in.close();
			//	System.out.println("***Before JSON parsing***");
				//parse the result
				String jsonResponse = response.toString();
				JSONObject allInfoObject = new JSONObject(jsonResponse);
				try {
				mErrorMesssage = allInfoObject.getString(JSON_SUCCESS);
			//	System.out.println("errorMessage: " + mErrorMesssage);
				}
				catch (JSONException e) {
					mErrorMesssage = "";
				}
				if (!mErrorMesssage.equals(SUCCESS_FALSE)) {
					int total = allInfoObject.getInt(JSON_TOTAL);
					lastPage = allInfoObject.getInt(JSON_LAST_PAGE);
				//	System.out.println("lastPage from JSON: " + lastPage);
				//	System.out.println("last page/countLoops: " + lastPage + "/" +  countLoops);
					//determine whether another loop is necessary
					if (lastPage > countLoops + 1 )  
						flag_next_page = true;
					else 
						flag_next_page = false;
					JSONArray startupsArray = allInfoObject.getJSONArray(ROOT_NODE);
					for (int a = 0; a < startupsArray.length(); a++) {
						JSONObject allInfoJson = startupsArray.getJSONObject(a);
						JSONObject startupJson = allInfoJson.getJSONObject(JSON_STARTUP);
						startupId = startupJson.getInt(JSON_ID);
						startupsIds.add(startupId);
					}
					//JSONObject startupDetailsObject = startupsJsonObject.getJSONObject(ROOT_NODE);
					//System.out.println("startupsJsonObject: " + startupsJsonObject.toString());
					//System.out.println("startupDetailsObject: " + startupDetailsObject.toString());
					countLoops = countLoops + 1; 
				}
			}
			catch(Exception e){
				e.printStackTrace();
				flag_next_page = false;
			}
		}
			return startupsIds;
		}
		
		public static String getInvestorNameOrLocation(int investorId, boolean returnLocation) {
			String investorsName = "";
			String investorsLocation = "";
			final String JSON_NAME = "name";
			final String JSON_LOCATIONS = "locations";
			final String JSON_LOCATION_NAME = "display_name";
			String toUseURL = "https://api.angel.co/1/users/" + investorId + "?" + QUERY_ACCESS_TOKEN + "=" + accessToken;
			try {
				URL urlObject = new URL(toUseURL);
				//establish the connection
				HttpURLConnection httpConnection = (HttpURLConnection) urlObject.openConnection();
				httpConnection.setRequestMethod(HTTP_GET);
				int responseCode = httpConnection.getResponseCode();
				//process the output
				BufferedReader in = new BufferedReader(
				        new InputStreamReader(httpConnection.getInputStream()));
				String inputLine;
				StringBuffer response = new StringBuffer();
		 
				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
				in.close();
				
				String jsonResponse = response.toString();
				JSONObject investorDetailsObject = new JSONObject(jsonResponse);
				investorsName = investorDetailsObject .getString(JSON_NAME);
				if (returnLocation) {
					JSONArray locationsArray = investorDetailsObject.getJSONArray(JSON_LOCATIONS);
					for (int i = 0; i < locationsArray.length(); i++) {
						JSONObject locationDetails = locationsArray.getJSONObject(i);
						if (i < locationsArray.length() -1)
							investorsLocation += locationDetails.getString(JSON_LOCATION_NAME).trim() + ", ";
						else
							investorsLocation += locationDetails.getString(JSON_LOCATION_NAME).trim();
					}
				}
				
			}
				catch(Exception e){
					e.printStackTrace();
				}
			
			if (returnLocation)
				return investorsLocation;
			else
				return investorsName;
			
		}
		
		public static ArrayList<String> getEligibleStartups(ArrayList<Integer> startupsIds, String baseUrl ) {
			ArrayList<String> eligibleStartups = new ArrayList<String>();
			String toUseUrl;
			JSONObject startupDetailsObject;
			JSONArray startupMarketsArray;
			JSONObject startupMarket;
			final String JSON_MARKETS = "markets";
			final String JSON_MARKET_NAME = "name";
			final String JSON_STARTUP_DESCRIPTION = "product_desc";
			final String JSON_STARTUP_NAME = "name";
			String[] startupMarkets = new String[50];
			String startupDescription;
			String startupName;
			
			//System.out.println("startupsIds.size(): " + startupsIds.size());
			for (int i = 0; i < startupsIds.size(); i++) {

				toUseUrl = baseUrl + startupsIds.get(i) + "?" + QUERY_ACCESS_TOKEN + "=" + accessToken;
				//System.out.println("***Startup id is: " + startupsIds.get(i));
				try {
					URL urlObject = new URL(toUseUrl);
					//establish the connection
					HttpURLConnection httpConnection = (HttpURLConnection) urlObject.openConnection();
					httpConnection.setRequestMethod(HTTP_GET);
					int responseCode = httpConnection.getResponseCode();
					//process the output
					BufferedReader in = new BufferedReader(
					        new InputStreamReader(httpConnection.getInputStream()));
					String inputLine;
					StringBuffer response = new StringBuffer();
			 
					while ((inputLine = in.readLine()) != null) {
						response.append(inputLine);
					}
					in.close();
					
					//parse the result
					startupDetailsObject = new JSONObject(response.toString());
					//get the startup's description
					try {
						startupDescription = startupDetailsObject.getString(JSON_STARTUP_DESCRIPTION);
					}
					catch (JSONException e) {
						startupDescription = "";
					}
					//get the startup's name
					try {
						startupName = startupDetailsObject.getString(JSON_STARTUP_NAME);
					}
					catch (JSONException j) {
						startupName = "";
					}
					startupMarketsArray = startupDetailsObject.getJSONArray(JSON_MARKETS);
					//System.out.println("startupMarketsArray: " + startupMarketsArray.toString());
					//System.out.println("startupMarketsArray.length(): " + startupMarketsArray.length());
					for (int j = 0; j < startupMarketsArray.length(); j++) {
						startupMarket = startupMarketsArray.getJSONObject(j);
						//System.out.println("Market is: " + startupMarket.getString(JSON_MARKET_NAME));
						if (checkEligibility(startupMarket.getString(JSON_MARKET_NAME), startupDescription)) {
							if (startupName.equals("")) //if the name is not available, we use the id
								eligibleStartups.add(String.valueOf(startupsIds.get(i)));
							else 
								eligibleStartups.add(startupName);
							
							//System.out.println("Eligible startup/market : " + startupsIds.get(i) + "/" + startupMarket.getString(JSON_MARKET_NAME));
							break;//exit the inner loop
						}
					}
				}
				catch (Exception e) {
					e.printStackTrace();
				}
 			}
			
			
			return eligibleStartups;
		}
		
		public static boolean checkEligibility(String startupMarket, String startupDescription) {
/*			String[] eligibleMarkets = {"consumer electronics", "transportation", "embedded hardware and software", "Real Time Gps Fleet Monitoring Services",
					"gps", "bicycles", "real time gps fleet monitoring services", "hardware", "internet of things", "hardware + software", "displays", "enterprise hardware",
					"communications hardware", "sensors", "3d printing", "cell phones", "manufacturing","location based services", "home automation"};*/
		//	System.out.println("Description is: " + startupDescription);
			String[] eligibleMarkets = {"electronics", "transportation", "embedded", "hardware", "fleet", "monitoring",
					"gps", "bicycles", "things", "displays", "communications", "sensors", "3d", "printing", "phones", "manufacturing","automation"};
			
			String[] eligibleKeywords = {"electronics","embedded", "hardware","bicycles", "internet of things","sensors","3d","automation"};
			//check if the market corresponds
			for (int i = 0; i < eligibleMarkets.length; i++) 
				if (startupMarket.toLowerCase().contains(eligibleMarkets[i])) 
					return true;
			//if not, maybe their description gives them away	
			for (int j = 0; j < eligibleKeywords.length; j++)
				if (startupDescription.toLowerCase().contains(eligibleKeywords[j]))
					return true;
			//this means the startup is not eligible
			return false;
		}
		
		public static ArrayList<Integer> removeDuplicates(ArrayList<Integer> list) {

			// Store unique items in result.
			ArrayList<Integer> result = new ArrayList<>();

			// Record encountered Strings in HashSet.
			HashSet<Integer> set = new HashSet<>();

			// Loop over argument list.
			for (Integer item : list) {

			    // If String is not in set, add it to the list and the set.
			    if (!set.contains(item)) {
				result.add(item);
				set.add(item);
			    }
			}
			return result;
		}
		//prepare the array for the API call
		public static String[] cleanInvestorsList(String[] investorList) {
			for (int i = 0; i < investorList.length; i++) {
				//investorList[i] = investorList[i].trim().replace(" ", "%20");
				investorList[i] = AngelListUtil.StringToUnicode(investorList[i]);
			}
			return investorList;
		}
		

		
		private static String StringToUnicode(String input) {
			HashMap replaceMap = new HashMap<String, String>();
			ArrayList<String> charactersList = new ArrayList<String>();
			//create the map
			replaceMap.put("ö", "&#214;");
			charactersList.add("ö");
			replaceMap.put("ü", "&#220;");
			charactersList.add("ü");
			replaceMap.put(" ", "%20");
			charactersList.add(" ");
			
			String[] inputArray = input.split("");
			for (int i = 0; i < inputArray.length; i++) {
				for (int j = 0; j < charactersList.size(); j++) {
					//System.out.println("charactersList.get(j): " + charactersList.get(j));
					if (inputArray[i].equals(charactersList.get(j))) {
						input = input.replaceAll(inputArray[i], String.valueOf(replaceMap.get(charactersList.get(j))) );
						System.out.println("Returned investor: " + input);
					}
				}
			}
			//System.out.println("Returned input is: " + input);
			return input;
		}
		
		private static String replaceHtmlWithUnicode(String input) {
			HashMap replaceMap = new HashMap<String, String>();
			ArrayList<String> charactersList = new ArrayList<String>();
			//create the map
			replaceMap.put("&#214;", "\u00f6");//ö
			charactersList.add("&#214;");
			replaceMap.put("&#220;", "\u00fc");
			charactersList.add("&#220;");
			
			String[] inputArray = input.split("");
			for (int i = 0; i < inputArray.length; i++) {
				for (int j = 0; j < charactersList.size(); j++) {
					//System.out.println("charactersList.get(j): " + charactersList.get(j));
					if (inputArray[i].equals(charactersList.get(j))) {
						input = input.replaceAll(inputArray[i], String.valueOf(replaceMap.get(charactersList.get(j))) );
						System.out.println("Returned investor: " + input);
					}
				}
			}
			//System.out.println("Returned input is: " + input);
			return input;
		}

		public static String removeUTFCharacters(String data){
			Pattern p = Pattern.compile("\\\\u(\\p{XDigit}{4})");
			Matcher m = p.matcher(data);
			StringBuffer buf = new StringBuffer(data.length());
			while (m.find()) {
				String ch = String.valueOf((char) Integer.parseInt(m.group(1), 16));
				m.appendReplacement(buf, Matcher.quoteReplacement(ch));
			}
				m.appendTail(buf);
				return buf.toString();
			}
		
}

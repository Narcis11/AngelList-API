package angelapi;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class AngelApiCall {
	
	
	public static void main (String[] args) {
		//Arrays
		String[] investorList = {"Christian Dörffer", "Christopher Münchhoff"};
		
		HashMap investorStartups = new HashMap();//used to store each investor and the eligible startups
		int[] investorIds = new int[investorList.length];
		ArrayList<String> eligibleStartups = new ArrayList<String>();
		String listOfStartups = "";
		String investorsLocation = "";
		HashMap investorInfo = new HashMap();//used to store investor's id and name
		ArrayList<Integer> startupsIds = new ArrayList<Integer>();
		final String HTTP_GET = "GET";
		final String HTTP_POST = "POST";
		//used to get authorization
		final String BASE_AUTHORIZE_URI = "https://angel.co/api/oauth/authorize";
		final String QUERY_CLIENT_ID = "client_id";
		final String QUERY_CLIENT_SECRET = "client_secret";
		final String QUERY_RESPONSE_TYPE = "response_type";
		final String QUERY_CODE = "code";
		final String QUERY_GRANT_TYPE = "grant_type";
		final String clientId = "9419535c903faf85aeb4104dd24e4a8a6d6941f4761dbda0";
		final String clientSecret = "a40c2428bd95b40b7cf1020f3d610f7d63e9a49ff00b790f";
		final String responseType = "code";
		final String grantType = "authorization_code";
		final String code = "bc80faff006eba3c1866d628725e8cfd24b1d0818e04c0b4";
		final String authorizeURL = "https://angel.co/api/oauth/authorize?" + QUERY_CLIENT_ID + "=" + clientId + "&"
							+ QUERY_RESPONSE_TYPE + "=" + responseType;
		final String getTokenURL = "https://angel.co/api/oauth/token?" + QUERY_CLIENT_ID + "=" + clientId + "&" +
							QUERY_CLIENT_SECRET + "=" + clientSecret + "&" + QUERY_CODE + "=" + code + "&" + QUERY_GRANT_TYPE + 
							"=" + grantType;
		
		//used in various calls
		final String QUERY_SEARCH_TYPE = "type";
		final String QUERY_ACCESS_TOKEN ="access_token";
		final String QUERY = "query";
		final String QUERY_USER_ID = "user_id";
		//values used in search
		final String accessToken = "c79af3178afd7f7ab3ccac4ebad366eb4e54ca7c07384a56";
		final String searchTypeUser = "User";
		
		final String BASE_INVESTOR_SEARCH_URL = "https://api.angel.co/1/search?" + QUERY_ACCESS_TOKEN + "=" + accessToken
										+ "&" + QUERY_SEARCH_TYPE + "=" + searchTypeUser + "&" + QUERY + "=";
		final String BASE_STARTUP_SEARCH_URL = "https://api.angel.co/1/startup_roles?v=1&" + QUERY_ACCESS_TOKEN + "=" + accessToken
												+ "&" + QUERY_USER_ID + "=";	
		
		final String BASE_STARTUP_MARKET_URL = "https://api.angel.co/1/startups/";
		//https://api.angel.co/1/startups/489756?access_token=c79af3178afd7f7ab3ccac4ebad366eb4e54ca7c07384a56
		String toUseInvestorURL; 
		//replace the spaces and special characters with their Unicode equivalent; needed for the API call
		investorList = AngelListUtil.cleanInvestorsList(investorList);
		//get the ids and names of investors
		try {
			investorInfo = AngelListUtil.getInvestorInfo(investorList, BASE_INVESTOR_SEARCH_URL);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	    // Get a set of the entries
	    Set set = investorInfo.entrySet();
	    // Get an iterator
	    Iterator iterator = set.iterator();
		while (iterator.hasNext()) {
			Map.Entry mapEntry = (Map.Entry) iterator.next();
			//System.out.println("***Investor name: " + mapEntry.getValue() + "****");
			String investorStringId = (String) mapEntry.getKey();
			int investorId = Integer.valueOf(investorStringId);
			System.out.println("investorStringId: " + investorStringId);
			if (investorId > 0 ) {
				//System.out.println("investorId: " + investorId);
				//get the startups in which this investor has been involved
				startupsIds = AngelListUtil.getStartupsIds(investorId, BASE_STARTUP_SEARCH_URL + investorId);
				//clean the list
				startupsIds = AngelListUtil.removeDuplicates(startupsIds);
				//get the eligible startups
				eligibleStartups = AngelListUtil.getEligibleStartups(startupsIds, BASE_STARTUP_MARKET_URL);
				
				//add the eligible startups to the hash map
				for (int x = 0; x < eligibleStartups.size(); x++) {
					//create the list of startups associated to this investor
					if (x < eligibleStartups.size() -1)
						listOfStartups += eligibleStartups.get(x).trim() + ", ";
					//we add to the HasMap at the last iteration
					if (x == eligibleStartups.size() -1 ) {
						//add the last startup without a comma at the end
						listOfStartups += eligibleStartups.get(x);
						//get the investor's location
						investorsLocation = AngelListUtil.getInvestorNameOrLocation(investorId, true);
						//add the name, location and startups to the HashMap
						investorStartups.put(mapEntry.getValue() + " - " + investorsLocation, listOfStartups);
						listOfStartups = "";//we have to reinitialise it every time because of the +=
					}
				}
			}
		}
		
		//display the list of eligible investors and their startups
		Set investorStartupSet = investorStartups.entrySet();
		Iterator investorIterator = investorStartupSet.iterator();
		while (investorIterator.hasNext()) {
			Map.Entry investorMapEntry = (Map.Entry) investorIterator.next();
			System.out.println(investorMapEntry.getKey() + " - " + investorMapEntry.getValue());
		}
		
	}
}

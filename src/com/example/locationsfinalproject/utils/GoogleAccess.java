package com.example.locationsfinalproject.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import android.app.Application;

/**
 * a helper class to perform google API requests over a network.
 */
public class GoogleAccess extends Application {

	
	
	/**	 the base url for the API */
	public final static String SEARCH_API = "https://maps.googleapis.com/maps/api/place/";
	//parameters for the API
	public final static String OUTPUT = "/json";
	private static final String RANKBY = "distance";
	
	/** the cloud project's API-KEY */
	private static final String APP_KEY = "AIzaSyAmubiCWHBZypLITwEL6Pjx4uftQILF_Tw";
	/**
	 * performs a Places API search request<br>
	 * this will send a request to the Google Places API with a search string<br>
	 * and return the result JSON string.<br>
	 * 
	 * @param q
	 *            - the search string
	 * @return a JSON string with the results from the API<br>
	 *         (or null if something went wrong)
	 */
	public static String searchLocation(String q, String action, double latitude, double longitude, String radius) {

		BufferedReader input = null;
		HttpURLConnection connection = null;
		StringBuilder response = new StringBuilder();

		try {
			// query string:
			String queryString = "";
			if (action.equals("nearbysearch")) {
				queryString += "?keyword=" + URLEncoder.encode(q, "utf-8");
			} else {
				queryString += "?query=" + URLEncoder.encode(q, "utf-8");
			}
			
			// check if it's textsearch or nearbysearch
			String actionString = "";
			actionString = action;

			String latitudeString = latitude+"";
			String longitudeString = longitude+"";
			if (action.equals("nearbysearch")) {

				queryString += "&sensor=true&location=" + latitudeString + ","
						+ longitudeString + "&radius=" + radius + "&rankBy=" + RANKBY;
			} else {
				queryString += "&sensor=false";
			}

			queryString += "&key=" + APP_KEY;

			// prepare a URL object :
			URL url = new URL(SEARCH_API + actionString + OUTPUT + queryString);

			// open a connection
			connection = (HttpURLConnection) url.openConnection();

			// check the result status of the connection:
			if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
				// not good.
				return null;
			}

			// get the input stream from the connection
			// and make it into a buffered char stream.
			input = new BufferedReader(new InputStreamReader(
					connection.getInputStream()));

			// read from the buffered stream line by line:
			String line = "";
			while ((line = input.readLine()) != null) {
				// append to the string builder:
				response.append(line + "\n");
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} finally {

			// close the stream if it exists:
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			// close the connection if it exists:
			if (connection != null) {
				connection.disconnect();
			}
		}

		// get the string from the string builder:
		return response.toString();
	}
	
}
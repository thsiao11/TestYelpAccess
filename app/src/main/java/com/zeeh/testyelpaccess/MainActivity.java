package com.zeeh.testyelpaccess;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.scribe.builder.ServiceBuilder;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.oauth.OAuthService;

/**
 * Code sample for accessing the Yelp API V2.
 * <p/>
 * This program demonstrates the capability of the Yelp API version 2.0 by using the Search API to
 * query for businesses by a search term and location, and the Business API to query additional
 * information about the top result from the search query.
 * <p/>
 * <p/>
 * See <a href="http://www.yelp.com/developers/documentation">Yelp Documentation</a> for more info.
 */

public class MainActivity extends AppCompatActivity {

    private static final String CONSUMER_KEY = "RrVrUQDZg6eFzXSiSWDcYg";
    private static final String CONSUMER_SECRET = "cedUiQTDNKiy8-hTOp1FZLYqqUo";
    private static final String TOKEN = "o23-IQTWPAAugx-YHtF7u6x9ilaV4SIk";
    private static final String TOKEN_SECRET = "DKRuQ3FE009yGhlPMWwpRt9geSI";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new YelpAPIAsyncTask(CONSUMER_KEY, CONSUMER_SECRET, TOKEN, TOKEN_SECRET).execute();
//        yelpApi.queryAPI(yelpApi, "dinner", "San Francisco, CA");

    }

}

class YelpAPIAsyncTask extends AsyncTask<Void, Void, Void> {

    static String LOG_TAG = "YelpAPI";
    private static final String API_HOST = "api.yelp.com";
    private static final String DEFAULT_TERM = "dinner";
    private static final String DEFAULT_LOCATION = "San Francisco, CA";
    private static final int SEARCH_LIMIT = 3;
    private static final String SEARCH_PATH = "/v2/search";
    private static final String BUSINESS_PATH = "/v2/business";

    private static final String CONSUMER_KEY = "";
    private static final String CONSUMER_SECRET = "";
    private static final String TOKEN = "";
    private static final String TOKEN_SECRET = "";

    OAuthService service;
    Token accessToken;

    /**
     * Setup the Yelp API OAuth credentials.
     *
     * @param consumerKey Consumer key
     * @param consumerSecret Consumer secret
     * @param token Token
     * @param tokenSecret Token secret
     */
    public YelpAPIAsyncTask(String consumerKey, String consumerSecret, String token, String tokenSecret) {
        this.service =
                new ServiceBuilder().provider(TwoStepOAuth.class).apiKey(consumerKey)
                        .apiSecret(consumerSecret).build();
        this.accessToken = new Token(token, tokenSecret);
        Log.d(LOG_TAG, "accessToken: " + this.accessToken);
//        queryAPI(this, "food", "San Francisco, CA");
    }

    /**
     * Creates and sends a request to the Search API by term and location.
     * <p>
     * See <a href="https://www.yelp.com/developers/documentation/v2/search_api">Yelp Search API V2</a>
     * for more info.
     *
     * @param term <tt>String</tt> of the search term to be queried
     * @param location <tt>String</tt> of the location
     * @return <tt>String</tt> JSON Response
     */
    public String searchForBusinessesByLocation(String term, String location) {
        OAuthRequest request = createOAuthRequest(SEARCH_PATH);
        request.addQuerystringParameter("term", term);
        request.addQuerystringParameter("location", location);
        request.addQuerystringParameter("limit", String.valueOf(SEARCH_LIMIT));
        return sendRequestAndGetResponse(request);
    }

    /**
     * Creates and sends a request to the Business API by business ID.
     * <p>
     * See <a href="https://www.yelp.com/developers/documentation/v2/business">Yelp Business API V2</a>
     * for more info.
     *
     * @param businessID <tt>String</tt> business ID of the requested business
     * @return <tt>String</tt> JSON Response
     */
    public String searchByBusinessId(String businessID) {
        OAuthRequest request = createOAuthRequest(BUSINESS_PATH + "/" + businessID);
        return sendRequestAndGetResponse(request);
    }

    /**
     * Creates and returns an {@link OAuthRequest} based on the API endpoint specified.
     *
     * @param path API endpoint to be queried
     * @return <tt>OAuthRequest</tt>
     */
    private OAuthRequest createOAuthRequest(String path) {
        OAuthRequest request = new OAuthRequest(Verb.GET, "https://" + API_HOST + path);
        return request;
    }

    /**
     * Sends an {@link OAuthRequest} and returns the {@link Response} body.
     *
     * @param request {@link OAuthRequest} corresponding to the API request
     * @return <tt>String</tt> body of API response
     */
    private String sendRequestAndGetResponse(OAuthRequest request) {
        request.getCompleteUrl();
        Log.d(LOG_TAG, "getBodyComplete: " + request.getCompleteUrl());
        this.service.signRequest(this.accessToken, request);
//        Log.d(LOG_TAG, "request: " + request.getQueryStringParams());
        Log.d(LOG_TAG, "request: " + request.getOauthParameters());
        Response response = request.send();
        return response.getBody();
    }

    /**
     * Queries the Search API based on the command line arguments and takes the first result to query
     * the Business API.
     *
     * @param yelpApi <tt>YelpAPI</tt> service instance
     * @param 'yelpApiCli <tt>YelpAPICLI</tt> command line arguments
     */
    static void queryAPI(YelpAPIAsyncTask yelpApi, String term, String location) {
        String searchResponseJSON =
                yelpApi.searchForBusinessesByLocation(term, location);

        JSONParser parser = new JSONParser();
        JSONObject response = null;
        try {
            response = (JSONObject) parser.parse(searchResponseJSON);
        } catch (ParseException pe) {
            Log.d(LOG_TAG, "Error: could not parse JSON response:" + searchResponseJSON);
            return;
        }

        JSONArray businesses = (JSONArray) response.get("businesses");
        JSONObject firstBusiness = (JSONObject) businesses.get(0);
        String firstBusinessID = firstBusiness.get("id").toString();
        Log.d(LOG_TAG, String.format(
                "%s businesses found, querying business info for the top result \"%s\" ...",
                businesses.size(), firstBusinessID));

        // Select the first business and display business details
        String businessResponseJSON = yelpApi.searchByBusinessId(firstBusinessID.toString());
        Log.d(LOG_TAG, (String.format("Result for business \"%s\" found:", firstBusinessID)));
        Log.d(LOG_TAG, businessResponseJSON);
    }

    @Override
    protected Void doInBackground(Void... params) {
        queryAPI(this, "food", "San Francisco, CA");

        return null;
    }
}

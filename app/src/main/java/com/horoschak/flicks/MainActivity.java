package com.horoschak.flicks;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import com.horoschak.flicks.models.Config;
import com.horoschak.flicks.models.Movie;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity {
    // Constant values for API requests
    public static final String BASE_URL = "https://api.themoviedb.org/3";
    public static final String API_KEY_PARAM = "api_key";
    // Tag for logging activity from api requests
    public static final String TAG = "MovieListActivity";

    // instance fields
    AsyncHttpClient client;
    // List of movies playing now
    ArrayList<Movie> movies;
    // recycler view
    RecyclerView rvMovies;
    // Adapter wired to recycler view
    MovieAdapter adapter;
    // image config
    Config config;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // initialize client
        client = new AsyncHttpClient();
        // initialize movies list
        movies = new ArrayList<>();
        // initialize adapter, cant reinitialize movies after this
        adapter = new MovieAdapter(movies);

        //Resolve recycler view and connect a layout manager and adapter
        rvMovies = (RecyclerView) findViewById(R.id.tvTitle);
        rvMovies.setLayoutManager(new LinearLayoutManager(this));
        rvMovies.setAdapter(adapter );

        // get api configuration on start
        getConfiguration();
    }

    // get the list of current movies
    private void getNowPlaying() {
        // Create url
        String url = BASE_URL + "/movie/now_playing";
        // Set parameters
        RequestParams params = new RequestParams();
        // API Key
        params.add(API_KEY_PARAM, getString(R.string.api_key));
        // Execute get request and receive JSON response
        client.get(url, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // load response into movies list
                try {
                    JSONArray results = response.getJSONArray("results");
                    // iterate through result and create movie object
                    for (int i = 0; i < results.length(); i++) {
                        Movie movie = new Movie(results.getJSONObject(i));
                        movies.add(movie);
                        // notify row was added
                        adapter.notifyItemInserted(movies.size() - 1);
                    }
                    Log.i(TAG, String.format("Loaded %s movies", results.length()));
                } catch (JSONException e) {
                    logError("Failed to parse now playing movies", e, true);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                logError("Failed to get data from now playing endpoint", throwable, true);
            }
        });
    }

    // get configuration from api
    private void getConfiguration() {
        // Create url
        String url = BASE_URL + "/configuration";
        // Set parameters
        RequestParams params = new RequestParams();
        // API Key
        params.add(API_KEY_PARAM, getString(R.string.api_key));
        // Execute get request and receive JSON response
        client.get(url, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    config = new Config(response);
                    Log.i(TAG, String.format("Loaded configuration with imageBaseUrl %s and posterSize %s", config.getImageBaseUrl(), config.getPosterSize()));
                    // pass config to adapter
                    adapter.setConfig(config);
                    // get now playing movie list
                    getNowPlaying();
                } catch (JSONException e) {
                    logError("Failed Parsing Configuration", e, true);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                logError("Failed to get configuration", throwable, true);
            }
        });
    }

    // Handle errors: Log and alert user
    private void logError(String message, Throwable error, Boolean alertUser) {
        Log.e(TAG, message, error);
        // Alert user
        if (alertUser) {
            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
        }
    }
}

package com.horoschak.flicks;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.horoschak.flicks.models.Movie;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import cz.msebera.android.httpclient.Header;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

public class MovieDetailsActivity extends AppCompatActivity {

    // Constant values for API requests
    public static final String BASE_URL = "https://api.themoviedb.org/3";
    public static final String API_KEY_PARAM = "api_key";
    // Tag for logging activity from api requests
    public static final String TAG = "MovieDetailsActivity";

    AsyncHttpClient client;
    // movie to display
    Movie movie;
    // id for placeholder image
    int placeholderId;
    boolean isPortrait;

    // view objects
    TextView tvTitle;
    TextView tvOverview;
    RatingBar rbVoteAverage;
    ImageView ivTrailerHolder;
    ImageView ivTrailerHolderLand;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);
        // get orientation
        isPortrait = this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
        client = new AsyncHttpClient();
        // unwrap the movie passed in with intent
        movie = (Movie) Parcels.unwrap(getIntent().getParcelableExtra(Movie.class.getSimpleName()));
        Log.d("MovieDetailsActivity", String.format("Showing details for '%s'", movie.getTitle()));
        // resolve view objects
        tvTitle = (TextView) findViewById(R.id.tvTitle);
        tvOverview = (TextView) findViewById(R.id.tvOverview);
        rbVoteAverage = (RatingBar) findViewById(R.id.rbVoteAverage);
        ivTrailerHolder = (ImageView) findViewById(R.id.ivTrailerHolder);
        ivTrailerHolderLand = (ImageView) findViewById(R.id.ivTrailerHolderLand);

        // placeholder id
        placeholderId = R.drawable.flicks_backdrop_placeholder;

        // set title and overview
        tvTitle.setText(movie.getTitle());
        tvOverview.setText(movie.getOverview());

        // vote average is 0 - 10, divide by 2 for 5
        float voteAverage = movie.getVoteAverage().floatValue();
        rbVoteAverage.setRating(voteAverage = voteAverage > 0 ? voteAverage / 2.0f : voteAverage);
        // load images
        loadImage();
        // set up click listener for video
        if (isPortrait) {
            setUpImageListenerPortrait();
        } else {
            setUpImageListenerLandscape();
        }
    }

    // get video id for movie TODO use toast if no video exists
    private void getVideoId() {
        // If the video key was already fetched, don't need to get it again
        if (movie.getVideo_key() != null) {
            launchVideoActivity();
            return;
        }
        // get url for api
        String url = BASE_URL + "/movie/" + movie.getId() + "/videos";
        // set up parameters
        RequestParams params = new RequestParams();
        params.add(API_KEY_PARAM, getString(R.string.api_key));
        // Execute get request and receive JSON response
        client.get(url, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    JSONArray results = response.getJSONArray("results");
                    // get the first and only json object
                    JSONObject videoResult = results.getJSONObject(0);
                    //set video key for movie
                    movie.setVideo_key(videoResult.getString("key"));
                    Log.i(TAG, String.format("Loaded %s id: %s", movie.getTitle(), movie.getVideo_key()));
                } catch (JSONException e) {
                    logError("Failed to parse now video ID", e, true);
                }
                // launch new activity
                launchVideoActivity();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                logError("Failed to get data from video ID endpoint", throwable, true);
            }
        });
    }

    // load backdrop image
    private void loadImage() {
        if (isPortrait) {
            // Portrait view
            Glide.with(this)
                    .load(movie.getBackdropImageUrl())
                    .bitmapTransform(new RoundedCornersTransformation(this, 25, 0)) // Extra: round image corners
                    .placeholder(placeholderId) // Extra: placeholder for every image until load or error
                    .error(placeholderId)
                    .into(ivTrailerHolder);
        } else {
            // Landscape view
            Glide.with(this)
                    .load(movie.getBackdropImageUrl())
                    .bitmapTransform(new RoundedCornersTransformation(this, 25, 0)) // Extra: round image corners
                    .placeholder(placeholderId) // Extra: placeholder for every image until load or error
                    .error(placeholderId)
                    .into(ivTrailerHolderLand);
        }

    }

    // handle on click for video trailer portrait
    private void setUpImageListenerPortrait() {
        ivTrailerHolder.setOnClickListener(new ImageView.OnClickListener() {
            @Override
            public void onClick(View v) {
                // get video Id and then launch new activity
                getVideoId();
            }
        });
    }

    // handle on click for video trailer landscape
    private void setUpImageListenerLandscape() {
        ivTrailerHolderLand.setOnClickListener(new ImageView.OnClickListener() {
            @Override
            public void onClick(View v) {
                // get video Id and then launch new activity
                getVideoId();
            }
        });
    }

    private void launchVideoActivity() {
        if (movie.getVideo_key() != null) {
            Intent intent = new Intent(MovieDetailsActivity.this, MovieTrailerActivity.class);
            intent.putExtra("video_id", movie.getVideo_key());
            MovieDetailsActivity.this.startActivity(intent);
        } else { // let user know, no video exists
            Toast.makeText(getApplicationContext(), "No video for this movie", Toast.LENGTH_LONG).show();
        }
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


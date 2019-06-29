package com.horoschak.flicks.models;

import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcel;

@Parcel // Means class is parcelable
public class Movie {

    // values from API, make public for parcel
    String title;
    String overview;
    Double voteAverage;
    Integer id;
    // will be populated when details tab is opened
    String video_key;
    // only path
    String posterPath;
    String backdropPath;
    // Populated in movie adapter for image, only use backdrop for detail screen
    String backdropImageUrl;


    public Movie() {
    }

    // initialize Json data
    public Movie(JSONObject object) throws JSONException {
        title = object.getString("title");
        overview = object.getString("overview");
        posterPath = object.getString("poster_path");
        backdropPath = object.getString("backdrop_path");
        voteAverage = object.getDouble("vote_average");
        id = object.getInt("id");
        // set unknown values to null until populated
        video_key = null;
        backdropImageUrl= null;
    }

    public void setBackdropImageUrl(String backdropImageUrl) {
        this.backdropImageUrl = backdropImageUrl;
    }

    public void setVideo_key(String video_key) {
        this.video_key = video_key;
    }

    public String getVideo_key() {
        return video_key;
    }

    public String getTitle() {
        return title;
    }

    public String getOverview() {
        return overview;
    }

    public String getPosterPath() {
        return posterPath;
    }

    public String getBackdropPath() {
        return backdropPath;
    }

    public Double getVoteAverage() {
        return voteAverage;
    }

    public Integer getId() {
        return id;
    }

    public String getBackdropImageUrl() {
        return backdropImageUrl;
    }
}

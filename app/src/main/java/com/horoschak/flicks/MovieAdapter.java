package com.horoschak.flicks;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.horoschak.flicks.models.Config;
import com.horoschak.flicks.models.Movie;

import org.parceler.Parcels;

import java.util.ArrayList;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;


public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.ViewHolder> {
    // List of movies
    ArrayList<Movie> movies;
    // Config model
    Config config;
    // Context for rendering
    Context context;

    // Initialize list
    public MovieAdapter(ArrayList<Movie> movies) {
        this.movies = movies;
    }

    public Config getConfig() {
        return config;
    }

    public void setConfig(Config config) {
        this.config = config;
    }

    // Creates and inflates a new view
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // get the context and create the inflater
        context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        // Create the view using the item movie layout
        View movieView = inflater.inflate(R.layout.item_movie, parent, false);
        // Create a new ViewHolder
        return new ViewHolder(movieView);
    }

    // binds an inflated view to a new item
    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        // get the movie data at the specified location
        Movie movie = movies.get(position);
        // populate view with movie data
        viewHolder.tvTitle.setText(movie.getTitle());
        viewHolder.tvOverview.setText(movie.getOverview());

        // Determine current orientation
        boolean isPortrait = context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;

        //build url for poster image
        String imageUrl = null;

        // If in portrait mode, load the poster image
        if (isPortrait) {
            imageUrl = config.getImageUrl(config.getPosterSize(), movie.getPosterPath());
        } else {
            imageUrl = config.getImageUrl(config.getBackdropSize(), movie.getBackdropPath());
        }

        //set backdrop information for class of not done so already
        if (movie.getBackdropImageUrl() == null) {
            movie.setBackdropImageUrl(config.getImageUrl(config.getBackdropSize(), movie.getBackdropPath()));
        }

        // get the correct placeholder and imageview for the current orientation
        int placeholderId = isPortrait ? R.drawable.flicks_movie_placeholder : R.drawable.flicks_backdrop_placeholder;
        ImageView imageView = isPortrait ? viewHolder.ivPosterImage : viewHolder.ivBackdropImage;

        // load image using glide
        Glide.with(context)
                .load(imageUrl)
                .bitmapTransform(new RoundedCornersTransformation(context, 25, 0)) // Extra: round image corners
                .placeholder(placeholderId) // Extra: placeholder for every image until load or error
                .error(placeholderId)
                .into(imageView);
    }

    // Returns the size of the data set
    @Override
    public int getItemCount() {
        return movies.size() ;
    }

    // create viewholder, cannot be static for clickable action
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        // track view objects
        ImageView ivPosterImage;
        ImageView ivBackdropImage;
        TextView tvTitle;
        TextView tvOverview;

        // When user clicks row show movie details in new activity
        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            // ensure the position is in the row
            if (position != RecyclerView.NO_POSITION) {
                // get movie at position, wont work if static
                Movie movie = movies.get(position);
                // Create intent for new activity
                Intent intent = new Intent(context, MovieDetailsActivity.class);
                // serialize the movie using parceler, use short name as key
                intent.putExtra(Movie.class.getSimpleName(), Parcels.wrap(movie));
                // start activity
                context.startActivity(intent);
            }
        }

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPosterImage = (ImageView) itemView.findViewById(R.id.ivPosterImage);
            ivBackdropImage = (ImageView) itemView.findViewById(R.id.ivBackdropImage);
            tvTitle = (TextView) itemView.findViewById(R.id.tvTitle);
            tvOverview = (TextView) itemView.findViewById(R.id.tvOverview);
            itemView.setOnClickListener(this);
        }
    }
}

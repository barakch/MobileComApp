package apps.shay.barak.mobilecomapp.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;


import java.util.List;
import apps.shay.barak.mobilecomapp.R;
import apps.shay.barak.mobilecomapp.model.Series;
import apps.shay.barak.mobilecomapp.model.User;

public class SeriesListAdapter extends RecyclerView.Adapter<SeriesListAdapter.SeriesViewHolder> {

    private final String TAG = "SeriesListAdapter";
    private List<SeriesWithKey> seriesList;
    private User user;

    public SeriesListAdapter(List<SeriesWithKey> seriesList, User user) {
        this.seriesList = seriesList;
        this.user = user;
    }


    @NonNull
    @Override
    public SeriesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.e(TAG, "onCreateViewHolder() >>");

        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_series, parent, false);

        Log.e(TAG, "onCreateViewHolder() <<");
        return new SeriesViewHolder(parent.getContext(), itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull SeriesViewHolder holder, int position) {
        Log.e(TAG, "onBindViewHolder() >> " + position);
        Series series = seriesList.get(position).getSeries();
        String seriesKey = seriesList.get(position).getKey();

        holder.setSelectedSeries(series);
        holder.setSelectedSongKey(seriesKey);
        holder.setSongFile(series.getFileSong());
        holder.setThumbFile(series.getThumbImage());

        holder.name.setText(series.getName());
        holder.noOfSeasons.setText("Seasons: "+series.getNoOfSeasons());
        holder.genre.setText("Genre: "+series.getGenre());
        holder.price.setText("$" + series.getPrice());
        holder.rating.setRating(series.getRating());
        holder.reviewsCount.setVisibility(View.INVISIBLE);
    }

    @Override
    public int getItemCount() {
        return seriesList.size();
    }

    public class SeriesViewHolder extends RecyclerView.ViewHolder {

        CardView seriesCard;
        ImageView thumbImage;
        TextView name;
        TextView noOfSeasons;
        TextView genre;
        TextView price;
        TextView reviewsCount;
        String songFile;
        String thumbFile;
        Context context;
        RatingBar rating;
        Series selectedSeries;
        String selectedSongKey;

        public SeriesViewHolder(Context context, View parent) {
            super(parent);
            this.context = context;
            seriesCard = (CardView) parent.findViewById(R.id.cv_series);
            thumbImage = (ImageView) parent.findViewById(R.id.img_series);
            name = (TextView) parent.findViewById(R.id.tv_series_name);
            noOfSeasons = (TextView) parent.findViewById(R.id.tv_no_seasons);
            genre = (TextView) parent.findViewById(R.id.tv_genre);
            price = (TextView) parent.findViewById(R.id.tv_series_price);
            reviewsCount = (TextView) parent.findViewById(R.id.tv_series_review_count);
            rating = (RatingBar) parent.findViewById(R.id.rb_series_rating);
        }

        public void setSongFile(String songFile) {
            this.songFile = songFile;
        }

        public void setThumbFile(String thumbFile) {
            this.thumbFile = thumbFile;
        }

        public void setSelectedSeries(Series selectedSeries) {
            this.selectedSeries = selectedSeries;
        }

        public void setSelectedSongKey(String selectedSongKey) {
            this.selectedSongKey = selectedSongKey;
        }
    }
}

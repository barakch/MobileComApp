package apps.shay.barak.mobilecomapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import java.util.List;
import apps.shay.barak.mobilecomapp.R;
import apps.shay.barak.mobilecomapp.activities.SeriesDetailsActivity;
import apps.shay.barak.mobilecomapp.model.Series;
import apps.shay.barak.mobilecomapp.model.User;

public class SeriesListAdapter extends RecyclerView.Adapter<SeriesListAdapter.SeriesViewHolder> implements Filterable {

    private final String TAG = "SeriesListAdapter";
    private List<SeriesWithKey> seriesList;
    private List<SeriesWithKey> seriesFilteredList;
    private User user;

    public SeriesListAdapter(List<SeriesWithKey> seriesList, User user) {
        this.seriesList = seriesList;
        this.seriesFilteredList = seriesList;
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
    public void onBindViewHolder(@NonNull final SeriesViewHolder holder, int position) {
        Log.e(TAG, "onBindViewHolder() >> " + position);
        final Series series = seriesFilteredList.get(position).getSeries();
        final String seriesKey = seriesFilteredList.get(position).getKey();

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

        if(series.getExplicitImageUrl() == null) {
            holder.thumbImage.setImageDrawable(null);
            StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("images/series/");
            storageRef.child(series.getThumbImage()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    series.setExplicitImageUrl(uri.toString());
                    Picasso.get().load(uri).into(holder.thumbImage);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                }
            });
        }else{
            Picasso.get().load(series.getExplicitImageUrl()).into(holder.thumbImage);
        }

        holder.seriesCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Context context = view.getContext();
                Intent intent = new Intent(context, SeriesDetailsActivity.class);
                intent.putExtra("series", series);
                intent.putExtra("key", seriesKey);
                intent.putExtra("user",user);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return seriesFilteredList.size();
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

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    seriesFilteredList = seriesList;
                } else {
                    List<SeriesWithKey> filteredList = new ArrayList<>();
                    for (SeriesWithKey row : seriesList) {

                        // name match condition. this might differ depending on your requirement
                        // here we are looking for name or phone number match
                        if (row.getSeries().getName().toLowerCase().contains(charString.toLowerCase()) || row.getSeries().getName().contains(charSequence)) {
                            filteredList.add(row);
                        }
                    }

                    seriesFilteredList = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = seriesFilteredList;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                seriesFilteredList = (ArrayList<SeriesWithKey>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }
}

package apps.shay.barak.mobilecomapp.activities;

import android.content.Intent;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.youtube.player.YouTubeStandalonePlayer;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import com.yarolegovich.lovelydialog.LovelyStandardDialog;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import apps.shay.barak.mobilecomapp.R;
import apps.shay.barak.mobilecomapp.Utils.AnalyticsManager;
import apps.shay.barak.mobilecomapp.Utils.AnonymousHelper;
import apps.shay.barak.mobilecomapp.adapter.ReviewsAdapter;
import apps.shay.barak.mobilecomapp.model.Review;
import apps.shay.barak.mobilecomapp.model.Series;
import apps.shay.barak.mobilecomapp.model.User;

public class SeriesDetailsActivity extends AppCompatActivity implements View.OnClickListener {

    public final String TAG = "SeriesDetailsActivity";
    private Series series;
    private String key;
    private User user;

    private FloatingActionButton writeReview;
    private Button buyPlaySeries;
    private ImageView detailsImg;
    private RecyclerView recyclerViewReviews;
    private boolean seriesWasPurchased;
    private DatabaseReference seriesReviewsRef;
    private List<Review> reviewsList = new ArrayList<>();
    private AnalyticsManager analyticsManager;
    private boolean showOnce=true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_series_details);
        analyticsManager = AnalyticsManager.getInstance(getApplicationContext());
        key = getIntent().getStringExtra("key");
        series = getIntent().getParcelableExtra("series");
        user = getIntent().getParcelableExtra("user");
        int discount = getIntent().getIntExtra("discount", -1);
        int oldPrice= getIntent().getIntExtra("old_price", -1);
        if(discount > 0 && showOnce){
            showDiscountMessage(discount, oldPrice);
        }

        ((TextView) findViewById(R.id.tv_details_name)).setText(series.getName());
        ((TextView) findViewById(R.id.tv_details_no_seasons)).setText("Seasons: " + series.getNoOfSeasons());
        ((TextView) findViewById(R.id.tv_details_created)).setText("Created by: " + series.getCreatedBy());
        ((TextView) findViewById(R.id.tv_details_date)).setText("Publish date: " + series.getPublishDate());
        ((TextView) findViewById(R.id.tv_details_genre)).setText("Genre: " + series.getGenre());
        writeReview = findViewById(R.id.btn_new_review);
        writeReview.setOnClickListener(this);
        detailsImg = findViewById(R.id.img_details);
        if (series.getExplicitImageUrl() == null) {
            StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("images/series/");
            storageRef.child(series.getThumbImage()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    series.setExplicitImageUrl(uri.toString());
                    Picasso.get().load(uri).into(detailsImg);
                }
            });
        } else {
            Picasso.get().load(series.getExplicitImageUrl()).into(detailsImg);
        }

        buyPlaySeries = findViewById(R.id.btn_buy_series);
        buyPlaySeries.setText("Buy " + series.getPrice() + "$");
        Iterator i = user.getMyTvShows().iterator();
        while (i.hasNext()) {
            if (i.next().equals(key)) {
                seriesWasPurchased = true;
                buyPlaySeries.setText("PLAY");
                break;
            }
        }
        buyPlaySeries.setOnClickListener(this);

        recyclerViewReviews = findViewById(R.id.series_reviews);
        recyclerViewReviews.setHasFixedSize(true);
        recyclerViewReviews.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerViewReviews.setItemAnimator(new DefaultItemAnimator());
        ReviewsAdapter reviewsAdapter = new ReviewsAdapter(reviewsList);
        recyclerViewReviews.setAdapter(reviewsAdapter);
        seriesReviewsRef = FirebaseDatabase.getInstance().getReference("series/" + key + "/reviews");
        seriesReviewsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Review review = dataSnapshot.getValue(Review.class);
                    reviewsList.add(review);
                }
                recyclerViewReviews.getAdapter().notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "onCancelled(Review) >>" + databaseError.getMessage());
            }
        });

        analyticsManager.trackSeriesEvent("series_view", series);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.btn_buy_series:
                buyOrPlaySeries();
                break;

            case R.id.btn_new_review:
                addNewReview();
                break;

        }
    }

    private void addNewReview() {
        if (user.isAnonymous()) {
            AnonymousHelper.onMethodUnAllowed(this, FirebaseAuth.getInstance());
            return;
        }

        if(!user.getMyTvShows().contains(key)){
            new LovelyStandardDialog(this)
                    .setTitle("You are not allowed to write a review")
                    .setMessage("You must purchase the series in order to write a review").setPositiveButtonText("OK").show();
            return;
        }

        Log.e(TAG, "writeReview.onClick() >>");
        Intent intent = new Intent(getApplicationContext(), ReviewActivity.class);
        intent.putExtra("series", series);
        intent.putExtra("key", key);
        intent.putExtra("user", user);
        startActivity(intent);
        finish();
    }

    private void buyOrPlaySeries() {
        if (user.isAnonymous()) {
            AnonymousHelper.onMethodUnAllowed(this, FirebaseAuth.getInstance());
            return;
        }

        if (seriesWasPurchased) {
            Log.e(TAG, "buyPlay.onClick() >> Playing purchased series");
            playCurrentSeries(series);
            analyticsManager.trackSeriesEvent("series_play", series);

        } else {
            //Purchase the song.
            Log.e(TAG, "buyPlay.onClick() >> Purchase the series");
            user.getMyTvShows().add(key);
            user.upgdateTotalPurchase(series.getPrice());
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users");
            userRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(user);
            seriesWasPurchased = true;
            buyPlaySeries.setText("PLAY");
            analyticsManager.trackPurchase(series);
        }
    }


    private void playCurrentSeries(Series series) {
        String videoID = "";
        switch (series.getFileSong()) {
            case "1.mp3":
                videoID = "U7elNhHwgBU";
                break;

            case "2.mp3":
                videoID = "5hAXVqrljbs";
                break;

            case "3.mp3":
                videoID = "vY0qzXi5oJg";
                break;

            case "4.mp3":
                videoID = "ULwUzF1q5w4";
                break;

            case "5.mp3":
                videoID = "NspqGM0DbbQ";

                break;
        }

        try {
            Intent intent = YouTubeStandalonePlayer.createVideoIntent(this, "AIzaSyAcHc4SgcqGp1Nr1nk_MVbgFW1VjSCwnTI", videoID);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "You must install Youtube app in order to watch this series", Toast.LENGTH_SHORT);
        }
    }

    @Override
    public void onBackPressed() {
        if (getParent() != null)
            super.onBackPressed();
        else {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void showDiscountMessage(int discount, int oldPrice){
        new LovelyStandardDialog(this)
                .setTitle("Get " +series.getName()+ " "+discount+"% off now!")
                .setMessage("Pay only " + series.getPrice() +"$ instead of " + oldPrice+"$").setPositiveButtonText("OK")
                .show();
        showOnce = false;
    }
}

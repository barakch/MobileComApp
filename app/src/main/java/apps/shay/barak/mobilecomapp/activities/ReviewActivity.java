package apps.shay.barak.mobilecomapp.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import apps.shay.barak.mobilecomapp.R;
import apps.shay.barak.mobilecomapp.model.Review;
import apps.shay.barak.mobilecomapp.model.Series;
import apps.shay.barak.mobilecomapp.model.User;

public class ReviewActivity extends AppCompatActivity {

    private final String TAG = "ReviewActivity";
    private Series series;
    private String key;
    private User user;
    private int prevRating = -1;

    private TextView userReview;
    private RatingBar userRating;
    private DatabaseReference seriesRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.e(TAG, "onCreate() >>");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

        key = getIntent().getStringExtra("key");
        series = getIntent().getParcelableExtra("series");
        user = getIntent().getParcelableExtra("user");

        userReview = findViewById(R.id.new_user_review);
        userRating = findViewById(R.id.new_user_rating);


        seriesRef = FirebaseDatabase.getInstance().getReference("series/" + key);
        seriesRef.child("/reviews/" +  FirebaseAuth.getInstance().getCurrentUser().getUid()).
                addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        Review review = snapshot.getValue(Review.class);
                        if (review != null) {
                            userReview.setText(review.getUserReview());
                            userRating.setRating(review.getUserRating());
                            prevRating = review.getUserRating();
                        }
                        Log.e(TAG, "onDataChange(Review) <<");
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e(TAG, "onCancelled(Review) >>" + databaseError.getMessage());
                    }
                });
        Log.e(TAG, "onCreate() <<");
    }

    public void onSubmitClick(View v) {

        Log.e(TAG, "onSubmitClick() >>");


        seriesRef.runTransaction(new Transaction.Handler() {

            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {

                Log.e(TAG, "doTransaction() >>" );


                Series series = mutableData.getValue(Series.class);

                if (series == null ) {
                    Log.e(TAG, "doTransaction() << series is null" );
                    return Transaction.success(mutableData);
                }

                if (prevRating == -1) {
                    // Increment the review count and rating only in case the user enters a new review
                    series.incrementReviewCount();
                    series.incrementRating((int)userRating.getRating());
                } else{
                    series.incrementRating((int)userRating.getRating() - prevRating);
                }

                mutableData.setValue(series);
                Log.e(TAG, "doTransaction() << series was set");
                return Transaction.success(mutableData);

            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean committed, DataSnapshot dataSnapshot) {

                Log.e(TAG, "onComplete() >>" );

                if (databaseError != null) {
                    Log.e(TAG, "onComplete() << Error:" + databaseError.getMessage());
                    return;
                }

                if (committed) {
                    Review review = new Review(
                            userReview.getText().toString(),
                            (int)userRating.getRating(),
                            user.getEmail());

                    seriesRef.child("/reviews/" + FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(review);
                }


                Intent intent = new Intent(getApplicationContext(),SeriesDetailsActivity.class);
                intent.putExtra("series", series);
                intent.putExtra("key", key);
                intent.putExtra("user",user);
                startActivity(intent);
                finish();

                Log.e(TAG, "onComplete() <<" );
            }
        });



        Log.e(TAG, "onSubmitClick() <<");
    }


    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(),SeriesDetailsActivity.class);
        intent.putExtra("series", series);
        intent.putExtra("key", key);
        intent.putExtra("user",user);
        startActivity(intent);
        finish();
    }
}

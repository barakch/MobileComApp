package apps.shay.barak.mobilecomapp.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import java.util.List;

import apps.shay.barak.mobilecomapp.R;
import apps.shay.barak.mobilecomapp.model.Review;

public class ReviewsAdapter extends  RecyclerView.Adapter<ReviewsAdapter.ReviewViewHolder> {
    private final String TAG = "ReviewsAdapter";
    private List<Review> reviewsList;

    public ReviewsAdapter(List<Review> reviewsList) {
        this.reviewsList = reviewsList;
    }

    @Override
    public ReviewViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_review, parent, false);
        return new ReviewViewHolder(parent.getContext(),itemView);
    }

    @Override
    public void onBindViewHolder(ReviewViewHolder holder, int position) {
        Log.e(TAG,"onBindViewHolder() >> " + position);
        Review review = reviewsList.get(position);
        holder.getUserMail().setText(review.getUserEmail());
        holder.getUserReview().setText(review.getUserReview());
        holder.getUserRating().setRating(review.getUserRating());
        Log.e(TAG,"onBindViewHolder() << "+ position);
    }

    @Override
    public int getItemCount() {
        return reviewsList.size();
    }

    public class ReviewViewHolder extends RecyclerView.ViewHolder {

        private TextView userReview;
        private TextView userMail;
        private RatingBar userRating;

        public ReviewViewHolder(Context context, View view) {

            super(view);
            userReview = view.findViewById(R.id.user_review);
            userMail = view.findViewById(R.id.user_mail);
            userRating = view.findViewById(R.id.user_rating);

        }

        public TextView getUserReview() {
            return userReview;
        }

        public void setUserReview(TextView userReview) {
            this.userReview = userReview;
        }

        public TextView getUserMail() {
            return userMail;
        }

        public void setUserMail(TextView userMail) {
            this.userMail = userMail;
        }

        public RatingBar getUserRating() {
            return userRating;
        }

        public void setUserRating(RatingBar userRating) {
            this.userRating = userRating;
        }
    }
}

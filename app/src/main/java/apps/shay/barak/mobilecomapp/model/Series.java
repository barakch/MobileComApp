package apps.shay.barak.mobilecomapp.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Map;

public class Series implements Parcelable {

    private String countryOfOrigin;
    private String createdBy;
    private String genre;
    private int noOfSeasons;
    private String publishDate;
    private String fileSong;
    private String thumbImage;
    private String name;
    private int price;
    private int rating;
    private int reviewsCount;
    private Map<String, Review> reviews;
    private String explicitImageUrl;

    public Series() {
    }

    public Series(String countryOfOrigin, String createdBy, String genre, int noOfSeasons, String publishDate, String fileSong, String thumbImage, String name, int price, int rating, int reviewsCount, Map<String, Review> reviews) {
        this.countryOfOrigin = countryOfOrigin;
        this.createdBy = createdBy;
        this.genre = genre;
        this.noOfSeasons = noOfSeasons;
        this.publishDate = publishDate;
        this.fileSong = fileSong;
        this.thumbImage = thumbImage;
        this.name = name;
        this.price = price;
        this.rating = rating;
        this.reviewsCount = reviewsCount;
        this.reviews = reviews;
    }

    public String getCountryOfOrigin() {
        return countryOfOrigin;
    }

    public void setCountryOfOrigin(String countryOfOrigin) {
        this.countryOfOrigin = countryOfOrigin;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public int getNoOfSeasons() {
        return noOfSeasons;
    }

    public void setNoOfSeasons(int noOfSeasons) {
        this.noOfSeasons = noOfSeasons;
    }

    public String getPublishDate() {
        return publishDate;
    }

    public void setPublishDate(String publishDate) {
        this.publishDate = publishDate;
    }

    public String getFileSong() {
        return fileSong;
    }

    public void setFileSong(String fileSong) {
        this.fileSong = fileSong;
    }

    public String getThumbImage() {
        return thumbImage;
    }

    public void setThumbImage(String thumbImage) {
        this.thumbImage = thumbImage;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public int getReviewsCount() {
        return reviewsCount;
    }

    public void setReviewsCount(int reviewsCount) {
        this.reviewsCount = reviewsCount;
    }

    public Map<String, Review> getReviews() {
        return reviews;
    }

    public void setReviews(Map<String, Review> reviews) {
        this.reviews = reviews;
    }

    public void incrementReviewCount() { reviewsCount++;}

    public void incrementRating(int newRating) { rating+=newRating;}

    public String getExplicitImageUrl() {
        return explicitImageUrl;
    }

    public void setExplicitImageUrl(String explicitImageUrl) {
        this.explicitImageUrl = explicitImageUrl;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(countryOfOrigin);
        parcel.writeString(createdBy);
        parcel.writeString(genre);
        parcel.writeInt(noOfSeasons);
        parcel.writeString(publishDate);
        parcel.writeString(fileSong);
        parcel.writeString(thumbImage);
        parcel.writeString(name);
        parcel.writeInt(price);
        parcel.writeInt(rating);
        parcel.writeInt(reviewsCount);
        parcel.writeString(explicitImageUrl);
    }

    private Series(Parcel in) {
        this.countryOfOrigin = in.readString();
        this.createdBy = in.readString();
        this.genre = in.readString();
        this.noOfSeasons = in.readInt();
        this.publishDate = in.readString();
        this.fileSong = in.readString();
        this.thumbImage = in.readString();
        this.name = in.readString();
        this.price = in.readInt();
        this.rating = in.readInt();
        this.reviewsCount = in.readInt();
        this.explicitImageUrl = in.readString();
    }

    public static final Parcelable.Creator<Series> CREATOR = new Parcelable.Creator<Series>() {
        @Override
        public Series createFromParcel(Parcel parcel) {
            return new Series(parcel);
        }

        @Override
        public Series[] newArray(int size) {
            return new Series[size];
        }
    };

}

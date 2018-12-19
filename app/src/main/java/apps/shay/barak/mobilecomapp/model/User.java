package apps.shay.barak.mobilecomapp.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class User implements Parcelable {

    private String email;
    private int totalPurchase;
    private List<String> myTvShows = new ArrayList<>();

    public User() {
    }

    public User(String email, int totalPurchase, List<String> myTvShows) {
        this.email = email;
        this.totalPurchase = totalPurchase;
        this.myTvShows = myTvShows;
    }

    public String getEmail() {
        return email;
    }


    public void upgdateTotalPurchase(int newPurcahsePrice) {
        this.totalPurchase += newPurcahsePrice;
    }

    public List<String> getMyTvShows() {
        return myTvShows;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(email);
        parcel.writeList(myTvShows);
    }

    public User(Parcel in) {
        this.email = in.readString();
        in.readList(myTvShows,String.class.getClassLoader());
    }

    public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>() {
        @Override
        public User createFromParcel(Parcel source) {
            return new User(source);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };
}

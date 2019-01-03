package apps.shay.barak.mobilecomapp.Utils;

import android.content.Context;
import android.os.Bundle;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.mixpanel.android.mpmetrics.MixpanelAPI;
import java.util.HashMap;
import java.util.Map;
import apps.shay.barak.mobilecomapp.model.Series;

public class AnalyticsManager {
    private static String TAG = "AnalyticsManager";
    public static final String MIXPANEL_TOKEN = "6b50c5cd30d34f3d8fe2e6c5593ac3b8";
    private static AnalyticsManager mInstance = null;
    private FirebaseAnalytics mFirebaseAnalytics;
    private MixpanelAPI mMixpanel;

    private AnalyticsManager(Context context) {
        init(context);
    }

    public static AnalyticsManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new AnalyticsManager(context);
        }
        return (mInstance);
    }

    private void init(Context context) {
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(context);
        mMixpanel = MixpanelAPI.getInstance(context, MIXPANEL_TOKEN);
    }

    public void trackSearchEvent(String searchString) {
        String eventName = "search";

        //Firebase
        Bundle params = new Bundle();
        params.putString(FirebaseAnalytics.Param.SEARCH_TERM, searchString);
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SEARCH, params);


        //Mixpanel
        Map<String, Object> eventParams2 = new HashMap<String, Object>();
        eventParams2.put("search term", searchString);
        mMixpanel.trackMap(eventName, eventParams2);

    }

    public void trackSignupEvent(String signupMethod) {
        //Firebase
        String eventName = "signup";
        Bundle params = new Bundle();
        params.putString(FirebaseAnalytics.Param.SIGN_UP_METHOD, signupMethod);
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SIGN_UP, params);


        //Mixpanel
        Map<String, Object> eventParams2 = new HashMap<String, Object>();
        eventParams2.put("signup method", signupMethod);
        mMixpanel.trackMap(eventName, eventParams2);
    }


    public void trackLoginEvent(String loginMethod) {

        String eventName = "login";
        Bundle params = new Bundle();
        params.putString(FirebaseAnalytics.Param.SIGN_UP_METHOD, loginMethod);
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.LOGIN, params);

        //Mixpanel
        Map<String, Object> eventParams2 = new HashMap<String, Object>();
        eventParams2.put("signup method", loginMethod);
        mMixpanel.trackMap(eventName, eventParams2);
    }

    public void trackSeriesEvent(String event, Series series) {
        Bundle params = new Bundle();
        params.putString("series_genre", series.getGenre());
        params.putString("series_name", series.getName());
        params.putString("series_reviews_count", String.valueOf(series.getReviewsCount()));
        params.putDouble("series_price", series.getPrice());
        params.putDouble("series_rating", series.getRating());
        mFirebaseAnalytics.logEvent(event, params);

        //Mixpanel
        Map<String, Object> eventParams2 = new HashMap<String, Object>();
        eventParams2.put("series_genre", series.getGenre());
        eventParams2.put("series_name", series.getName());
        eventParams2.put("series_reviews_count", String.valueOf(series.getReviewsCount()));
        eventParams2.put("series_price", String.valueOf(series.getPrice()));
        eventParams2.put("series_rating", String.valueOf(series.getRating()));
        mMixpanel.trackMap(event, eventParams2);
    }

    public void trackPurchase(Series series) {

        String eventName = "purchase";
        Bundle params = new Bundle();
        params.putDouble(FirebaseAnalytics.Param.PRICE, series.getPrice());
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.ECOMMERCE_PURCHASE, params);


        //Mixpanel
        Map<String, Object> eventParams2 = new HashMap<String, Object>();
        eventParams2.put("series_genre", series.getGenre());
        eventParams2.put("series_name", series.getName());
        eventParams2.put("series_reviews_count", String.valueOf(series.getReviewsCount()));
        eventParams2.put("series_price", String.valueOf(series.getPrice()));
        eventParams2.put("series_rating", String.valueOf(series.getRating()));
        mMixpanel.trackMap(eventName, eventParams2);
    }

    public void trackSeriesRating(Series series, int userRating) {
        String eventName = "series_rating";

        Bundle params = new Bundle();
        params.putString("series_genre", series.getGenre());
        params.putString("series_name", series.getName());
        params.putString("series_reviews_count", String.valueOf(series.getReviewsCount()));
        params.putDouble("series_price", series.getPrice());
        params.putDouble("series_total_rating", series.getRating());
        params.putDouble("series_user_rating", userRating);
        mFirebaseAnalytics.logEvent(eventName, params);

        //Mixpanel
        Map<String, Object> eventParams2 = new HashMap<String, Object>();
        eventParams2.put("series_genre", series.getGenre());
        eventParams2.put("series_name", series.getName());
        eventParams2.put("series_reviews_count", String.valueOf(series.getReviewsCount()));
        eventParams2.put("series_price", String.valueOf(series.getPrice()));
        eventParams2.put("series_total_rating", String.valueOf(series.getRating()));
        eventParams2.put("series_user_rating", String.valueOf(userRating));
        mMixpanel.trackMap(eventName, eventParams2);
    }

    public void trackSortEvent(String sortBy) {
        String eventName = "series_sort";

        Bundle params = new Bundle();
        params.putString("sort_by", sortBy);
        mFirebaseAnalytics.logEvent(eventName, params);

        //Mixpanel
        Map<String, Object> eventParams2 = new HashMap<String, Object>();
        eventParams2.put("sort_by", sortBy);
        mMixpanel.trackMap(eventName, eventParams2);

    }

    public void setUserID(String id, boolean newUser) {
        mFirebaseAnalytics.setUserId(id);


        if (newUser) {
            mMixpanel.alias(id, null);
        }
        mMixpanel.identify(id);
        mMixpanel.getPeople().identify(mMixpanel.getDistinctId());
        mMixpanel.getPeople().initPushHandling("163007701898");
    }

    public void setUserProperty(String name, String value) {
        mFirebaseAnalytics.setUserProperty(name, value);

        mMixpanel.getPeople().set(name, value);
    }
}

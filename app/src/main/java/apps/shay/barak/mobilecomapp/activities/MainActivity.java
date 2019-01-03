package apps.shay.barak.mobilecomapp.activities;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import com.facebook.AccessToken;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import apps.shay.barak.mobilecomapp.R;
import apps.shay.barak.mobilecomapp.Utils.AnalyticsManager;
import apps.shay.barak.mobilecomapp.adapter.SeriesListAdapter;
import apps.shay.barak.mobilecomapp.adapter.SeriesWithKey;
import apps.shay.barak.mobilecomapp.model.Series;
import apps.shay.barak.mobilecomapp.model.User;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static String TAG = "MainActivity";
    private FirebaseAuth mAuth;
    private DatabaseReference allSeriesRef;
    private DatabaseReference myUserRef;
    private List<SeriesWithKey> seriesList = new ArrayList<>();
    private RecyclerView recyclerView;
    private SeriesListAdapter seriesListAdapter;
    private EditText searchField;
    private User myUser;
    private AnalyticsManager analyticsManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
        analyticsManager = AnalyticsManager.getInstance(getApplicationContext());
        analyticsManager.setUserProperty("enable_push_campaigns","true");


        recyclerView = findViewById(R.id.series_list);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        findViewById(R.id.btn_search_series).setOnClickListener(this);
        searchField = findViewById(R.id.ed_search_series);

        FirebaseUser fbUser = FirebaseAuth.getInstance().getCurrentUser();
        if (fbUser != null) {
            myUserRef = FirebaseDatabase.getInstance().getReference("Users/" + fbUser.getUid());
            myUserRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    myUser = snapshot.getValue(User.class);
                    getAllSeries();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e(TAG, "onCancelled(Users) >>" + databaseError.getMessage());
                }
            });
        } else {
            getAllSeries();
        }

        searchField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    searchItem(searchField.getText().toString());
                    return true;
                }
                return false;
            }
        });

        searchField.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void afterTextChanged(Editable mEdit)
            {
                searchItem(mEdit.toString());
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after){}

            public void onTextChanged(CharSequence s, int start, int before, int count){}
        });
    }

    private void getAllSeries() {
        seriesList.clear();
        seriesListAdapter = new SeriesListAdapter(seriesList, myUser, this);
        recyclerView.setAdapter(seriesListAdapter);

        getAllSeriessUsingChildListenrs();
    }

    private void getAllSeriessUsingChildListenrs() {

        allSeriesRef = FirebaseDatabase.getInstance().getReference("series");

        allSeriesRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot snapshot, String previousChildName) {
                SeriesWithKey seriesWithKey = new SeriesWithKey(snapshot.getKey(), snapshot.getValue(Series.class));
                seriesList.add(seriesWithKey);
                recyclerView.getAdapter().notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot snapshot, String previousChildName) {
                Series series = snapshot.getValue(Series.class);
                String key = snapshot.getKey();

                for (int i = 0; i < seriesList.size(); i++) {
                    SeriesWithKey seriesWithKey = (SeriesWithKey) seriesList.get(i);
                    if (seriesWithKey.getKey().equals(snapshot.getKey())) {
                        seriesWithKey.setSeries(series);
                        recyclerView.getAdapter().notifyDataSetChanged();
                        break;
                    }
                }
            }

            @Override
            public void onChildMoved(DataSnapshot snapshot, String previousChildName) {

            }

            @Override
            public void onChildRemoved(DataSnapshot snapshot) {
                Series series = snapshot.getValue(Series.class);
                String key = snapshot.getKey();

                for (int i = 0; i < seriesList.size(); i++) {
                    SeriesWithKey songWithKey = (SeriesWithKey) seriesList.get(i);
                    if (songWithKey.getKey().equals(snapshot.getKey())) {
                        seriesList.remove(i);
                        recyclerView.getAdapter().notifyDataSetChanged();
                        Log.e(TAG, "onChildRemoved(Series) >> i=" + i);
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

                Log.e(TAG, "onCancelled(Series) >>" + databaseError.getMessage());
            }
        });

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.btn_search_series:
                String hint = searchField.getText().toString();
                if (hint == null)
                    return;
                searchItem(hint);
                break;
        }
    }


    void searchItem(String hint) {
        if(hint == null || seriesListAdapter == null)
            return;

        seriesListAdapter.getFilter().filter(hint);
        analyticsManager.trackSearchEvent(hint);
    }


    public void onRadioButtonCLick(View v) {
        switch (v.getId()) {
            case R.id.rb_order_by_price:
                ((RadioButton) findViewById(R.id.rb_order_by_reviews)).setChecked(false);
                ((RadioButton) findViewById(R.id.rb_order_by_price)).setChecked(true);
                orderByPrice();
                break;
            case R.id.rb_order_by_reviews:
                ((RadioButton) findViewById(R.id.rb_order_by_reviews)).setChecked(true);
                ((RadioButton) findViewById(R.id.rb_order_by_price)).setChecked(false);
                orderByRating();
                break;
        }
    }

    private void orderByPrice() {
        Collections.sort(seriesListAdapter.getSeriesList(), new Comparator<SeriesWithKey>() {
            @Override
            public int compare(SeriesWithKey series1, SeriesWithKey series2) {
                if (series1.getSeries().getPrice() > series2.getSeries().getPrice())
                    return 1;
                if (series1.getSeries().getPrice() == series2.getSeries().getPrice())
                    return 0;
                else
                    return -1;
            }
        });
        seriesListAdapter.notifyDataSetChanged();
        analyticsManager.trackSortEvent("price");
    }

    private void orderByRating() {
        Collections.sort(seriesListAdapter.getSeriesList(), new Comparator<SeriesWithKey>() {
            @Override
            public int compare(SeriesWithKey series1, SeriesWithKey series2) {
                if (Series.getCombainedRating(series1.getSeries()) > Series.getCombainedRating(series2.getSeries()))
                    return -1;
                if (Series.getCombainedRating(series1.getSeries()) == Series.getCombainedRating(series2.getSeries()))
                    return 0;
                else
                    return 1;
            }
        });
        seriesListAdapter.notifyDataSetChanged();
        analyticsManager.trackSortEvent("rating");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.log_out) {
            logOut();
        }
        return true;
    }


    private void logOut() {
        if (FirebaseAuth.getInstance() == null || FirebaseAuth.getInstance().getCurrentUser() == null) {
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        for (UserInfo user : FirebaseAuth.getInstance().getCurrentUser().getProviderData()) {
            Log.d(TAG, "User provider is:" + user.getProviderId());
            if (user.getProviderId().equals("facebook.com")) {
                System.out.println("User is signed in with Facebook");
                facebookSignout();
                return;
            } else if (user.getProviderId().equals("google.com")) {
                System.out.println("User is signed in with google");
                googleSignout();
                return;
            }
        }

        removeFirebaseUserAndOpenLoginActivity();
    }

    void googleSignout() {
        // Google sign out
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, gso);
        googleSignInClient.signOut().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        removeFirebaseUserAndOpenLoginActivity();
                    }
                });
    }

    private void facebookSignout() {
        if (isFacebookLoggedinLoggedIn())
            LoginManager.getInstance().logOut();
        removeFirebaseUserAndOpenLoginActivity();
    }

    public boolean isFacebookLoggedinLoggedIn() {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        return accessToken != null;
    }

    void removeFirebaseUserAndOpenLoginActivity() {
        mAuth.signOut();
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onNewIntent(Intent intent)
    {
        super.onNewIntent(intent);
    }
}

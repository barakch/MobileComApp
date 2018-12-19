package apps.shay.barak.mobilecomapp.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.ArrayList;
import java.util.List;
import apps.shay.barak.mobilecomapp.R;
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
    private User myUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
        recyclerView = findViewById(R.id.series_list);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        getAllSeries();
    }

    private void getAllSeries(){
        seriesList.clear();
        seriesListAdapter = new SeriesListAdapter(seriesList, null);
        recyclerView.setAdapter(seriesListAdapter);

        getAllSeriessUsingChildListenrs();
    }

    private void getAllSeriessUsingChildListenrs() {

        allSeriesRef = FirebaseDatabase.getInstance().getReference("series");

        allSeriesRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot snapshot, String previousChildName){

                Log.e(TAG, "onChildAdded(Series) >> " + snapshot.getKey());

                SeriesWithKey seriesWithKey = new SeriesWithKey(snapshot.getKey(),snapshot.getValue(Series.class));
                seriesList.add(seriesWithKey);
                recyclerView.getAdapter().notifyDataSetChanged();

                Log.e(TAG, "onChildAdded(Series) <<" + seriesWithKey.getSeries().getName());

            }
            @Override
            public void onChildChanged(DataSnapshot snapshot, String previousChildName){

                Log.e(TAG, "onChildChanged(Series) >> " + snapshot.getKey());

                Series series = snapshot.getValue(Series.class);
                String key = snapshot.getKey();

                for (int i = 0 ; i < seriesList.size() ; i++) {
                    SeriesWithKey seriesWithKey = (SeriesWithKey) seriesList.get(i);
                    if (seriesWithKey.getKey().equals(snapshot.getKey())) {
                        seriesWithKey.setSeries(series);
                        recyclerView.getAdapter().notifyDataSetChanged();
                        break;
                    }
                }

                Log.e(TAG, "onChildChanged(Series) <<");

            }
            @Override
            public void onChildMoved(DataSnapshot snapshot, String previousChildName){

                Log.e(TAG, "onChildMoved(Series) >> " + snapshot.getKey());


                Log.e(TAG, "onChildMoved(Series) << Doing nothing");

            }
            @Override
            public void onChildRemoved(DataSnapshot snapshot){

                Log.e(TAG, "onChildRemoved(Series) >> " + snapshot.getKey());

                Series series =snapshot.getValue(Series.class);
                String key = snapshot.getKey();

                for (int i = 0 ; i < seriesList.size() ; i++) {
                    SeriesWithKey songWithKey = (SeriesWithKey) seriesList.get(i);
                    if (songWithKey.getKey().equals(snapshot.getKey())) {
                        seriesList.remove(i);
                        recyclerView.getAdapter().notifyDataSetChanged();
                        Log.e(TAG, "onChildRemoved(Series) >> i="+i);
                        break;
                    }
                }

                Log.e(TAG, "onChildRemoved(Series) <<");

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

                Log.e(TAG, "onCancelled(Series) >>" + databaseError.getMessage());
            }
        });

    }


//    @Override
//    protected void onStart() {
//        super.onStart();
//        if(mAuth.getCurrentUser() == null) {
//            logOut();
//            return;
//        }
//
//        updateUI(mAuth.getCurrentUser());
//    }

    @Override
    public void onClick(View view) {

    }

    private void updateUI(FirebaseUser user){

    }


//    private void logOut() {
//        if(FirebaseAuth.getInstance() == null || FirebaseAuth.getInstance().getCurrentUser() == null){
//            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
//            startActivity(intent);
//            finish();
//            return;
//        }
//
//        showProgressDialog();
//        for (UserInfo user : FirebaseAuth.getInstance().getCurrentUser().getProviderData()) {
//            Log.d(TAG, "User provider is:" + user.getProviderId());
//            if (user.getProviderId().equals("facebook.com")) {
//                System.out.println("User is signed in with Facebook");
//                facebookSignout();
//                return;
//            } else if (user.getProviderId().equals("google.com")) {
//                System.out.println("User is signed in with google");
//                googleSignout();
//                return;
//            }
//        }
//
//        removeFirebaseUserAndOpenLoginActivity();
//    }
//
//    void googleSignout() {
//        // Google sign out
//        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//                .requestIdToken(getString(R.string.default_web_client_id))
//                .requestEmail()
//                .build();
//        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, gso);
//        googleSignInClient.signOut().addOnCompleteListener(this,
//                new OnCompleteListener<Void>() {
//                    @Override
//                    public void onComplete(@NonNull Task<Void> task) {
//                        removeFirebaseUserAndOpenLoginActivity();
//                    }
//                });
//    }
//
//    private void facebookSignout() {
//        if (isFacebookLoggedinLoggedIn())
//            LoginManager.getInstance().logOut();
//        removeFirebaseUserAndOpenLoginActivity();
//    }
//
//    public boolean isFacebookLoggedinLoggedIn() {
//        AccessToken accessToken = AccessToken.getCurrentAccessToken();
//        return accessToken != null;
//    }
//
//    void removeFirebaseUserAndOpenLoginActivity() {
//        mAuth.signOut();
//        hideProgressDialog();
//        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
//        startActivity(intent);
//        finish();
//    }
}

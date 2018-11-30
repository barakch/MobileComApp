package apps.shay.barak.mobilecomapp.activities;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
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
import com.squareup.picasso.Picasso;
import com.yarolegovich.lovelydialog.LovelyProgressDialog;
import apps.shay.barak.mobilecomapp.R;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static String TAG = "MainActivity";
    private FirebaseAuth mAuth;
    ImageView imgUser;
    TextView tvName, tvEmail;
    private LovelyProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();

        imgUser = findViewById(R.id.img_user);
        tvName = findViewById(R.id.tv_name);
        tvEmail = findViewById(R.id.tv_email);
        findViewById(R.id.btn_logout).setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(mAuth.getCurrentUser() == null) {
            logOut();
            return;
        }

        updateUI(mAuth.getCurrentUser());
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.btn_logout:
                logOut();

                break;
        }
    }

    private void updateUI(FirebaseUser user){
        String name = user.getDisplayName();
        String email = user.getEmail();
        user.getPhotoUrl();

        tvName.setText("Display Name: "+name);
        tvEmail.setText("Email: "+email);
        Picasso.get().load(user.getPhotoUrl()).placeholder(R.drawable.place_holder_user).into(imgUser);

    }


    private void logOut() {
        showProgressDialog();
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
        hideProgressDialog();
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(intent);
        finish();
    }

    public void showProgressDialog() {
        if(progressDialog != null){
            return;
        }

        progressDialog = new LovelyProgressDialog(this)
                .setTitle("Log out...");
        progressDialog.show();
    }

    public void hideProgressDialog() {
        if(progressDialog != null)
            progressDialog.dismiss();
        progressDialog = null;
    }
}

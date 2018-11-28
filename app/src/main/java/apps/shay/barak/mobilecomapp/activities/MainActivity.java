package apps.shay.barak.mobilecomapp.activities;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.facebook.AccessToken;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserInfo;

import apps.shay.barak.mobilecomapp.R;

public class MainActivity extends AppCompatActivity {

    public static String TAG = "MainActivity";
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();


        findViewById(R.id.btn_logout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
        });
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
}

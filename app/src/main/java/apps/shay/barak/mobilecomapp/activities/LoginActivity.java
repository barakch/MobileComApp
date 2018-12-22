package apps.shay.barak.mobilecomapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.BuildConfig;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.yarolegovich.lovelydialog.LovelyProgressDialog;
import com.yarolegovich.lovelydialog.LovelyStandardDialog;
import java.util.Arrays;
import apps.shay.barak.mobilecomapp.R;
import apps.shay.barak.mobilecomapp.Utils.Validator;
import apps.shay.barak.mobilecomapp.model.User;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener, FacebookCallback<LoginResult> {

    public static String TAG = "LoginActivity";
    private static final int RC_SIGN_IN = 9001;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseRemoteConfig mRemoteConfig;
    private CallbackManager callbackManager;
    private EditText edEmail, edPassword;
    private LovelyProgressDialog progressDialog;
    private TextView btnAnnonymousLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();

        //Init views and UI listeners
        edEmail = findViewById(R.id.ed_signin_email);
        edPassword = findViewById(R.id.ed_signin_password);
        findViewById(R.id.btn_login_email).setOnClickListener(this);
        findViewById(R.id.btn_login_google).setOnClickListener(this);
        findViewById(R.id.tv_forgot_pass).setOnClickListener(this);
        findViewById(R.id.tv_signup).setOnClickListener(this);
        findViewById(R.id.btn_login_facebook).setOnClickListener(this);
        btnAnnonymousLogin = findViewById(R.id.tv_anonymous);
        btnAnnonymousLogin.setOnClickListener(this);

        // Configure Google Login
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        //Configure Facebook Login
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
        callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(callbackManager, this);

        //Check if anonymous login is enabled
        mRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG)
                .build();
        mRemoteConfig.setConfigSettings(configSettings);
        mRemoteConfig.fetch(0).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Log.d(TAG, "RemoteConfig loaded: " + task.isSuccessful());
                mRemoteConfig.activateFetched();
                updateUI();
            }
        });
    }

    private void updateUI(){
        if(mRemoteConfig.getBoolean("allow_annonymous")){
            btnAnnonymousLogin.setVisibility(View.VISIBLE);
        }else{
            btnAnnonymousLogin.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.btn_login_email:
                String emailAddress = edEmail.getText().toString();
                String password = edPassword.getText().toString();

                if (!Validator.validateEmail(emailAddress)) {
                    Toast.makeText(LoginActivity.this, "Email address is not valid", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!Validator.validatePassword(password)) {
                    Toast.makeText(LoginActivity.this, "Invalid password", Toast.LENGTH_SHORT).show();
                    return;
                }

                loginWithEmail(emailAddress, password);
                break;

            case R.id.btn_login_google:
                loginWithGoogle();
                break;

            case R.id.btn_login_facebook:
                loginWithFacebook();
                break;

            case R.id.tv_forgot_pass:
                sendPasswordResetEmail();

                break;
            case R.id.tv_signup:
                openEmailSignupActivity();
                break;

            case R.id.tv_anonymous:
                signupAnonymously();
                break;
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Pass the activity result back to the Facebook SDK
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
                Toast.makeText(getApplicationContext(), "Google sign in failed", Toast.LENGTH_SHORT).show();
            }
        }
    }


    /**
     * Login with Email
     */
    private void loginWithEmail(String email, String password) {
        showProgressDialog();
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            hideProgressDialog();
                            openMainActivity();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            hideProgressDialog();
                            new LovelyStandardDialog(LoginActivity.this)
                                    .setTitle("We have an error")
                                    .setMessage(task.getException().getMessage()).setPositiveButtonText("OK")
                                    .show();
                        }
                    }
                });

    }

    private void sendPasswordResetEmail() {
        final String emailAddress = edEmail.getText().toString();
        if (!Validator.validateEmail(emailAddress)) {
            Toast.makeText(LoginActivity.this, "Email address is not valid", Toast.LENGTH_SHORT).show();
            return;
        }

        new LovelyStandardDialog(LoginActivity.this)
                .setTitle("Reset password")
                .setMessage("Would you like us to send you an email in order to reset your account password?").setPositiveButton("Yes", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.sendPasswordResetEmail(emailAddress)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Log.d(TAG, "Email sent.");
                                    Toast.makeText(LoginActivity.this, "An email was sent to you", Toast.LENGTH_SHORT).show();
                                } else {
                                    new LovelyStandardDialog(LoginActivity.this)
                                            .setTitle("We have an error")
                                            .setMessage(task.getException().getMessage()).setPositiveButtonText("OK")
                                            .show();
                                }
                            }
                        });
            }
        }).setNegativeButtonText("No").show();
    }


    /**
     * Login with Facebook
     */
    private void loginWithFacebook() {
        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("email", "public_profile"));
    }

    //Facebook Login result (FacebookCallback<LoginResult> implementation)
    @Override
    public void onSuccess(LoginResult loginResult) {
        Log.d(TAG, "facebook:onSuccess:" + loginResult);
        handleFacebookAccessToken(loginResult.getAccessToken());
    }

    @Override
    public void onCancel() {
        Log.d(TAG, "facebook:onCancel");
        // ...
    }

    @Override
    public void onError(FacebookException error) {
        Log.d(TAG, "facebook:onError", error);

        if (error != null && error.getCause() != null) {
            String errMsg = error.getCause().toString();
            if (error.getCause().getMessage() != null)
                errMsg = error.getCause().getMessage();
            new LovelyStandardDialog(LoginActivity.this)
                    .setTitle("We have an error")
                    .setMessage(""+errMsg).setPositiveButtonText("OK")
                    .show();
        }
    }

    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);
        showProgressDialog();

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            openMainActivity();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());

                            new LovelyStandardDialog(LoginActivity.this)
                                    .setTitle("We have an error")
                                    .setMessage(task.getException().getMessage()).setPositiveButtonText("OK")
                                    .show();
                            LoginManager.getInstance().logOut();

                        }
                        hideProgressDialog();
                    }
                });
    }


    /**
     * Login with Google
     */
    private void loginWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());
        showProgressDialog();

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            openMainActivity();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            new LovelyStandardDialog(LoginActivity.this)
                                    .setTitle("We have an error")
                                    .setMessage(task.getException().getMessage()).setPositiveButtonText("OK")
                                    .show();
                        }

                        hideProgressDialog();
                    }
                });
    }

    /**
     * Anonymous signup
     */
     private void signupAnonymously(){
         showProgressDialog();
         mAuth.signInAnonymously()
                 .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                     @Override
                     public void onComplete(@NonNull Task<AuthResult> task) {
                         if (task.isSuccessful()) {
                             // Sign in success, update UI with the signed-in user's information
                             Log.d(TAG, "signInAnonymously:success");
                             openMainActivity();
                         } else {
                             // If sign in fails, display a message to the user.
                             Log.w(TAG, "signInAnonymously:failure", task.getException());
                             new LovelyStandardDialog(LoginActivity.this)
                                     .setTitle("We have an error")
                                     .setMessage(task.getException().getMessage()).setPositiveButtonText("OK")
                                     .show();
                         }
                         hideProgressDialog();
                     }
                 });
     }

    /**
     * Helpers
     */
    public void showProgressDialog() {
        if (progressDialog != null) {
            return;
        }

        progressDialog = new LovelyProgressDialog(this)
                .setTitle("Connecting...");
        progressDialog.show();
    }

    public void hideProgressDialog() {
        if (progressDialog != null)
            progressDialog.dismiss();
        progressDialog = null;
    }

    public void openMainActivity() {
        createNewUser();
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(intent);
        LoginActivity.this.finish();
    }

    public void openEmailSignupActivity() {
        Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
        startActivity(intent);
        finish();
    }

    private void createNewUser() {
        FirebaseUser user = mAuth.getCurrentUser();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users");

        if (user == null) {
            Log.e(TAG, "createNewUser() << Error user is null");
            return;
        }

        userRef.child(user.getUid()).setValue(new User(user.getEmail(),0,null));
        Log.e(TAG, "createNewUser() <<");
    }
}

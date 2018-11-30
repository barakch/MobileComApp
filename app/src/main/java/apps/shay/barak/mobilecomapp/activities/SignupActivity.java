package apps.shay.barak.mobilecomapp.activities;

import apps.shay.barak.mobilecomapp.R;
import apps.shay.barak.mobilecomapp.Utils.Validator;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import com.facebook.login.LoginManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.yarolegovich.lovelydialog.LovelyProgressDialog;
import com.yarolegovich.lovelydialog.LovelyStandardDialog;


public class SignupActivity extends AppCompatActivity implements View.OnClickListener {

    public static String TAG = "SingupActivity";
    public static final int PICK_IMAGE = 111;
    private FirebaseAuth mAuth;
    private EditText edEmail, edPassword, edName;
    private String imagePath;
    private LovelyProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        mAuth = FirebaseAuth.getInstance();

        edEmail = findViewById(R.id.ed_email);
        edPassword = findViewById(R.id.ed_password);
        edName = findViewById(R.id.ed_name);
        edEmail = findViewById(R.id.ed_email);

        findViewById(R.id.btn_select_image).setOnClickListener(this);
        findViewById(R.id.btn_signup_email).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_signup_email:
                validateSubmittedData();
                break;

            case R.id.btn_select_image:
                selectImage();
                break;
        }
    }

    private void selectImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_IMAGE) {
            if (data != null) {
                imagePath = data.getDataString();
            }
        }
    }

    private void validateSubmittedData() {
        if (!Validator.validateUserName(edName.getText().toString())) {
            if (edName.getText().toString().length() == 0)
                Toast.makeText(getApplicationContext(), "Please enter your name", Toast.LENGTH_SHORT).show();
            else if (edName.getText().toString().length() < 2)
                Toast.makeText(getApplicationContext(), "Full Name is too short", Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(getApplicationContext(), "Full Name is not valid", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Validator.validateEmail(edEmail.getText().toString())) {
            if (edEmail.getText().toString().length() <= 0)
                Toast.makeText(getApplicationContext(), "Please enter an Email Address", Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(getApplicationContext(), "Email address is not valid", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Validator.validatePassword(edPassword.getText().toString())) {
            if (edPassword.getText().toString().length() < 6)
                Toast.makeText(getApplicationContext(), "Password is not valid, you need at least 6 characters", Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(getApplicationContext(), "Password is not valid, you must use A-Z and numbers only", Toast.LENGTH_SHORT).show();
            return;
        }

        if (imagePath == null) {
            Toast.makeText(getApplicationContext(), "Please select your avatar image", Toast.LENGTH_SHORT).show();
            return;
        }

        createUser(edEmail.getText().toString(), edPassword.getText().toString(), edName.getText().toString(), imagePath);
    }


    private void createUser(String email, String password, final String name, final String imagePath) {
        showProgressDialog();
        Log.d(TAG, "createUserWithEmail: start");
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateProfile(name, imagePath);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.d(TAG, "createUserWithEmail:failure", task.getException());
                            hideProgressDialog();
                            new LovelyStandardDialog(SignupActivity.this)
                                    .setTitle("We have an error")
                                    .setMessage(task.getException().getMessage()).setPositiveButtonText("OK")
                                    .show();
                            LoginManager.getInstance().logOut();                        }
                    }
                });
    }

    private void updateProfile(String name, String imagePath) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .setPhotoUri(Uri.parse(imagePath))
                .build();

        user.updateProfile(profileUpdates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "User profile updated.");
                            sendVerificationEmail(mAuth.getCurrentUser());
                            openMainActivity();
                        }
                    }
                });
    }

    private void sendVerificationEmail(FirebaseUser user){
        user.sendEmailVerification()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Email sent.");
                            Toast.makeText(SignupActivity.this, "A Verification Email was sent to you, please check your email", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void openMainActivity() {
        hideProgressDialog();
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish();
    }

    public void openLoginActivity() {
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(intent);
        finish();
    }

    public void showProgressDialog() {
        if(progressDialog != null){
            return;
        }

        progressDialog = new LovelyProgressDialog(this)
                .setTitle("Connecting...");
        progressDialog.show();
    }

    public void hideProgressDialog() {
        if(progressDialog != null)
            progressDialog.dismiss();
        progressDialog = null;
    }
}

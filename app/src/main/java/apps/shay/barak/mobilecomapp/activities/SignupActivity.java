package apps.shay.barak.mobilecomapp.activities;

import apps.shay.barak.mobilecomapp.R;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SignupActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText edEmail, edPassword;
    public static String TAG = "SingupActivity";
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        edEmail = findViewById(R.id.ed_email);
        edPassword = findViewById(R.id.ed_password);

        findViewById(R.id.btn_signup_email).setOnClickListener(this);
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btn_signup_email && validateSubmittedData()) {
            createUser(edEmail.getText().toString(),edPassword.getText().toString());
        }
    }

    private boolean validateSubmittedData() {
        if (!validateEmail(edEmail.getText().toString())) {
            if (edEmail.getText().toString().length() <= 0)
                Toast.makeText(getApplicationContext(), "Please enter an Email Address", Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(getApplicationContext(), "Email is not valid", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!validatePassword(edPassword.getText().toString())) {
            if (edPassword.getText().toString().length() < 6)
                Toast.makeText(getApplicationContext(), "Password is not valid, you need at least 6 characters", Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(getApplicationContext(), "Password is not valid, you must use A-Z and numbers only", Toast.LENGTH_SHORT).show();
            return false;
        }

        return  true;
    }


    private boolean validateEmail(String email) {
        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    private boolean validatePassword(String password) {
        String expression = "[a-zA-Z0-9\\!\\@\\#\\$]{8,24}";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(password);
        return matcher.matches();
    }


    @Override
    protected void onStart() {
        super.onStart();

        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
//        if(currentUser != null)
//            //Do something
    }


    private void createUser(String email, String password) {
        Log.d(TAG, "createUserWithEmail:enter");

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            //updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.d(TAG, "createUserWithEmail:failure", task.getException());
//                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
//                            Toast.makeText(EmailPasswordActivity.this, "Authentication failed.",
//                                    Toast.LENGTH_SHORT).show();
//                            updateUI(null);
                        }
                    }
                });
    }
}

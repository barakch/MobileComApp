package apps.shay.barak.mobilecomapp.Utils;

import android.app.Activity;
import android.content.Intent;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.yarolegovich.lovelydialog.LovelyStandardDialog;

import apps.shay.barak.mobilecomapp.activities.LoginActivity;
import apps.shay.barak.mobilecomapp.activities.MainActivity;

public class AnonymousHelper {

    public static void onMethodUnAllowed(final Activity activity, final FirebaseAuth mAuth) {
        new LovelyStandardDialog(activity)
                .setTitle("You are anonymous user")
                .setMessage("You must signup in order to do this action").setPositiveButton("Ok", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.signOut();
                Intent intent = new Intent(activity, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                        Intent.FLAG_ACTIVITY_NEW_TASK);
                activity.startActivity(intent);

                if(activity.getParent() != null)
                    activity.getParent().finish();
                activity.finish();
            }
        }).setNegativeButtonText("Later").show();
    }
}

package apps.shay.barak.mobilecomapp.notifications;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

public class PushTokenHandler extends FirebaseInstanceIdService {

    private static final String TAG = "PushTokenHandler";
    private static String deviceToken;

    @Override
    public void onTokenRefresh() {

        Log.e(TAG, "onTokenRefresh() >>");
        // Get updated InstanceID token.
        String deviceToken = FirebaseInstanceId.getInstance().getToken();

        //send registration to server
        //sendRegistrationToServer(deviceToken);

        Log.e(TAG, "onTokenRefresh() << deviceToken="+deviceToken);
    }
}

package apps.shay.barak.mobilecomapp.Utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Validator {


    public static boolean validateEmail(String email) {
        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    public static boolean validatePassword(String password) {
        String expression = "[a-zA-Z0-9\\!\\@\\#\\$]{8,24}";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(password);
        return matcher.matches();
    }

}

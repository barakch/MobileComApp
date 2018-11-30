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

//    public static boolean validatePassword(String password) {
//        String expression = "[a-zA-Z0-9\\!\\@\\#\\$]{6,24}";
//        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
//        Matcher matcher = pattern.matcher(password);
//        return matcher.matches();
//    }

    public static boolean validatePassword(String password) {
        if (password == null || password.isEmpty() || password.length() < 6)
            return false;
        return true;
    }

    public static boolean validateUserName(String name) {
        if (name == null || name.isEmpty() || name.length() < 2)
            return false;

        String expression = "^[a-zA-Z\\s]*$";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(name);
        return matcher.matches();
    }

}

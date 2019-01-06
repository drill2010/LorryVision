package name.marinchenko.lorryvision.util.feedback;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Email address validator.
 */

public class EmailValidator {

    private final static Pattern EMAIL_PATTERN =
            Pattern.compile("^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$");

    public static boolean validate(final String email) {
        final Matcher matcher = EMAIL_PATTERN.matcher(email);
        return matcher.matches();
    }
}

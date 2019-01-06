package name.marinchenko.lorryvision.util.feedback;

/**
 * General class for transferable messages with email.
 */

public abstract class Transferable {

    final static String EMAIL_TO = "marinchenkova@rambler.ru";

    private String emailTo;
    private String subject;
    private String message;

    protected abstract void wrap();

    public String getEmailTo() { return emailTo; }
    public String getSubject() { return subject; }
    public String getMessage() { return message; }

    void setEmailTo(final String emailTo) { this.emailTo = emailTo; }
    void setSubject(final String subject) { this.subject = subject; }
    void setMessage(final String message) { this.message = message; }
}

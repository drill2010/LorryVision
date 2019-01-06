package name.marinchenko.lorryvision.util.feedback;


/**
 * Feedback message to be sent.
 */

public class FeedbackMessage extends Transferable {

    private final String emailFrom;
    private final String subject;
    private final String feedbackType;
    private final String message;

    public FeedbackMessage(final String emailFrom,
                           final String subject,
                           final String feedbackType,
                           final String message) {
        this.emailFrom = emailFrom;
        this.subject = subject;
        this.feedbackType = feedbackType;
        this.message = message;

        wrap();
    }

    @Override
    protected void wrap() {
        setEmailTo(EMAIL_TO);
        setSubject(this.feedbackType + ": " + this.subject);
        setMessage(this.message);
    }


}

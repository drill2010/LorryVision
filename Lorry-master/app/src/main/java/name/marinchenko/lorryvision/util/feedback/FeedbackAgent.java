package name.marinchenko.lorryvision.util.feedback;

import android.content.Context;

import java.util.PriorityQueue;

/**
 * FeedbackAgent is a class to operate with feedback messages.
 */

public class FeedbackAgent {

    private final PriorityQueue<Transferable> sendQueue = new PriorityQueue<>();

    public void add(final Context context,
                    final Transferable msg) {
        this.sendQueue.add(msg);
        send(context);
    }

    private void send(final Context context) {
        final Transferable msg = this.sendQueue.poll();
        //TODO send email
    }

    private void init(){
        //TODO restore saved transferables to sendQueue
    }

    private void save() {
        //TODO save transferables
    }
}

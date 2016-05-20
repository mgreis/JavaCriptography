package chat.client;

import javax.net.ssl.SSLSocket;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Calendar;
import java.util.GregorianCalendar;

public final class Scheduler extends TimerTask {

    private final static long fONCE_PER_SECOND = 1000;
    private final static long fONCE_PER_MINUTE = 1000 * 60;
    private final static long fONCE_PER_HOUR = 1000 * 60 * 60;
    private final static long fONCE_PER_DAY = 1000 * 60 * 60 * 24;
    private SSLSocket socket;

    /**
     * Construct and use a TimerTask and Timer.
     *
     */
    public Scheduler(SSLSocket socket) {
        System.out.println("Starting Scheduler!");
        this.socket = socket;

        //perform the task at a given time interval starting now
        Timer timer = new Timer();
        Calendar now = new GregorianCalendar();
        timer.scheduleAtFixedRate(this, now.getTime(), fONCE_PER_MINUTE);
    }

    /**
     * Implements TimerTask's abstract run method.
     */
    @Override
    public void run() {
        //DO STUFF
        System.out.println("Scheduling...");
        Dispatcher dispatcher = new Dispatcher(socket);
        dispatcher.start();
    }

}

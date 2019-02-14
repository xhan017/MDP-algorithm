package Model;

import View.DisplayMapGUI;

import java.time.Duration;
import java.time.Instant;
import java.util.Timer;

public class Stopwatch extends Thread {
    private Timer timer = new Timer();
    private Instant starts, ends;

    public void run() {
        starts = Instant.now();
        while (!Variables.TIMER_STOP) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            ends = Instant.now();
            DisplayMapGUI.timerLabel.setText(String.format("%.3f seconds", Duration.between(starts, ends).toMillis()/1000.0f));
        }
    }

}

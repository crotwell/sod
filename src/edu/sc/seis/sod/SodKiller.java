package edu.sc.seis.sod;
import java.util.Timer;
import java.util.TimerTask;

public class SodKiller {
	public static void main(String[] args) {
		Timer timer = new Timer();
		TimerTask task = new TimerTask() {
			public void run() {
				System.exit(0);
			}
		};
		long delay = 0;
		for (int i=0; i<args.length; i++) {
			if(args[i].equals("-delay")) {
				delay = (new Integer(args[i+1]).intValue()) * 1000;
			}
		}
			Start.main(args);
			timer.schedule(task,delay);
	}
}

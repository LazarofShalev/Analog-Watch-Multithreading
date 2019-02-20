
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javafx.application.Platform;

public class Tick implements Runnable {

	private int sleepTime = 1000;
	private ClockPane clockPane;
	private Lock lock = new ReentrantLock();
	private Condition stop = lock.newCondition();
	private boolean pause = false;
	private boolean resume = false;

	public Tick(ClockPane clockPane) {
		this.clockPane = clockPane;
	}

	@Override
	public void run() {
		moveClock();
	}

	public void moveClock() {
		while (true) {
			lock.lock();
			if (pause)
				try {
					stop.await();
				} catch (InterruptedException e) {
					e.printStackTrace();
				} finally {
					lock.unlock();
				}
			else {
				resume = true;
				clockPane.setCurrentTime();
				Platform.runLater(() -> clockPane.paintClock());

				try {
					Thread.sleep(sleepTime);
				} catch (InterruptedException e) {
					e.printStackTrace();
				} finally {
					lock.unlock();
				}
			}
		}
	}

	public void pause() {
		pause = true;
		resume = false;
	}

	public void play() {
		if (resume)
			return;
		lock.lock();
		pause = false;
		stop.signal();
		lock.unlock();
	}
}

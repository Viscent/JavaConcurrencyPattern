package vh.activeobject.lib;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class PerformanceTest {
	private AtomicInteger reqSubmitted;
	private AtomicInteger reqFailed;
	private AtomicLong resDelay;
	private CyclicBarrier barrier;
	private int N = 100;
	private Runnable runnable = new Runnable() {
		private final SampleActiveObjectImpl sao = new SampleActiveObjectImpl();

		@Override
		public void run() {
			try {
				barrier.await();
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (BrokenBarrierException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			long begin;
			String result;
			while (true) {
				begin = System.currentTimeMillis();
				result=sao.doProcess("sync", 1);
				processResult(result);
				resDelay.addAndGet(System.currentTimeMillis() - begin);
				reqSubmitted.incrementAndGet();
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
				}
			}

		}

	};
	
	@SuppressWarnings("unused")
  private void processResult(String s){
		
	}

	@Before
	public void setUp() throws Exception {
		barrier = new CyclicBarrier(N);
		reqSubmitted = new AtomicInteger(0);
		reqFailed = new AtomicInteger(0);
		resDelay = new AtomicLong(0);

	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() throws InterruptedException {
		Thread t;
		for (int i = 0; i < N; i++) {
			t = new Thread(runnable);
			t.start();
		}

		new Timer().schedule(new TimerTask() {

			@Override
			public void run() {
				System.out.println(reqSubmitted + "," + reqFailed + "," + resDelay);
				reqSubmitted.set(0);
				reqFailed.set(0);
				resDelay.set(0);
			}

		}, 2000, 2 * 1000);
		Thread.sleep(3600 * 1000);
	}

}

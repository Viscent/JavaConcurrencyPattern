package vh.activeobject.lib;

import static org.junit.Assert.*;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class AsyncPerformanceTest {

	private AtomicInteger reqSubmitted;
	private AtomicInteger reqFailed;
	private AtomicLong resDelay;
	private CyclicBarrier barrier;
	private int N = 100;
	private Runnable runnable;
	
	private BlockingQueue<Runnable> q;

	@SuppressWarnings("unused")
	private void processResult(String s) {

	}

	@Before
	public void setUp() throws Exception {
		barrier = new CyclicBarrier(N);
		reqSubmitted = new AtomicInteger(0);
		reqFailed = new AtomicInteger(0);
		resDelay = new AtomicLong(0);
		
		q=new ArrayBlockingQueue<Runnable>(300);

		runnable = new Runnable() {
			private final SampleActiveObject sao = ActiveObjectProxy.newInstance(
			    SampleActiveObject.class, new SampleActiveObjectImpl(),
			    new ThreadPoolExecutor(100, 200, 60 * 3600,
							TimeUnit.SECONDS, q));
			    //Executors.newCachedThreadPool());

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
				Future<String> result;
				while (true) {
					begin = System.currentTimeMillis();
					try {
						result = sao.process("sync", 1);
						try {
							Thread.sleep(50);
						} catch (InterruptedException e) {
						}
						
						processResult(result.get());
						resDelay.addAndGet(System.currentTimeMillis() - begin);
					} catch (Exception e1) {
						reqFailed.incrementAndGet();
					} finally {
						reqSubmitted.incrementAndGet();

					}
				

				}

			}

		};

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
				System.out.println(reqSubmitted + "," + reqFailed + "," + resDelay+ "," + q.size());
				reqSubmitted.set(0);
				reqFailed.set(0);
				resDelay.set(0);

			}

		}, 2000, 2 * 1000);
		Thread.sleep(3600 * 1000);
	}

}

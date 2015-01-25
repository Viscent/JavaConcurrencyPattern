package io.github.viscent.mtpattern.tpt.example;

import io.github.viscent.mtpattern.tpt.AbstractTerminatableThread;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


public class ProducerConsumerStop {
	private static class SampleConsumer<P> {
		private final BlockingQueue<P> queue = new LinkedBlockingQueue<P>();

		private AbstractTerminatableThread workThread 
																	= new AbstractTerminatableThread() {
			@Override
			protected void doRun() throws Exception {
				terminationToken.reservations.decrementAndGet();
				P product = queue.take();
				// ...
				System.out.println(product);
			}

		};

		public void placeProduct(P product) {
			if (workThread.terminationToken.isToShutdown()) {
				throw new IllegalStateException("Thread shutdown");
			}
			try {
				queue.put(product);
				workThread.terminationToken.reservations.incrementAndGet();
			} catch (InterruptedException e) {

			}
		}

		public void shutdown() {
			workThread.terminate();
		}

		public void start() {
			workThread.start();
		}
	}

	public void test() {
		final SampleConsumer<String> aConsumer = new SampleConsumer<String>();

		AbstractTerminatableThread aProducer = new AbstractTerminatableThread() {
			private int i = 0;

			@Override
			protected void doRun() throws Exception {
				aConsumer.placeProduct(String.valueOf(i));
			}

			@Override
			protected void doCleanup(Exception cause) {
				// 生产者线程停止完毕后再请求停止消费者线程
				aConsumer.shutdown();
			}

		};

		aProducer.start();
		aConsumer.start();
	}
}

package vh.activeobject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Before;

public class Test {
	private RequestPersistence persistence;
	private ThreadPoolExecutor executor;
	private Attachment attachment;

	@Before
	public void setUp() {
		persistence = AsyncRequestPersistence.getInstance();
		executor = new ThreadPoolExecutor(80, 200, 60 * 3600,
				TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(300));
		try {
			File file = new File("/home/viscent/tmp/callstack.png");
			ByteBuffer contentBuf = ByteBuffer.allocate((int) file.length());
			FileInputStream fin = new FileInputStream(file);
			try {
				fin.getChannel().read(contentBuf);
			} finally {
				fin.close();
			}
			attachment = new Attachment();
			attachment.setContentType("image/png");
			attachment.setContent(contentBuf.array());
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	final AtomicInteger counter = new AtomicInteger(0);
	class RequestSenderThread extends Thread{
		private int chunkSize;
		private int timeSpan;
		
		public RequestSenderThread(int chunkSize,int timeSpan){
			this.chunkSize=chunkSize;
			this.timeSpan= timeSpan;
		}
		
		@Override
		public void run() {
			int sleepCount=(chunkSize/timeSpan);
			for(int i=0;i<chunkSize;i++){
				
				executor.execute(new Runnable() {

					@Override
					public void run() {
						MMSDeliverRequest request = new MMSDeliverRequest();
						request.setTransactionID(String.valueOf(counter.incrementAndGet()));
						request.setSenderAddress("13612345678");
						request.setTimeStamp(new Date());
						request.setExpiry((new Date().getTime() + 3600000) / 1000);

						request.setSubject("Hi");
						request.getRecipient().addTo("776");
						request.setAttachment(attachment);

						persistence.store(request);

					}
				});
				
				//System.out.println(this.getId()+" sent "+i+1);
				if(0==(i%sleepCount)){
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				
			}
		}
		
	}

	@org.junit.Test
	public void testFaultIsolation() {
		RequestSenderThread sender;
		for(int i=0;i<10;i++){
			sender=new RequestSenderThread(200000,10000);
			sender.start();
		}

		try {
			executor.awaitTermination(2L, TimeUnit.HOURS);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}

	}
	
	
	public void testTimeConsumption(){
		MMSDeliverRequest request = new MMSDeliverRequest();
		request.setTransactionID(String.valueOf(counter.incrementAndGet()));
		request.setSenderAddress("13612345678");
		request.setTimeStamp(new Date());
		request.setExpiry((new Date().getTime() + 3600000) / 1000);

		request.setSubject("Hi");
		request.getRecipient().addTo("776");
		request.setAttachment(attachment);
		DiskbasedRequestPersistence rp=new DiskbasedRequestPersistence();
		long start=System.currentTimeMillis();
		rp.store(request);
		//About took 15ms to write a single file of 218KB
		System.out.println("Took "+(System.currentTimeMillis()-start));
	}
	
//	private void a(){
//		ActiveObject ao=...;
//		Future<String> future=ao.doSomething("e");
//		//其它代码
//		String result=future.get();
//		System.out.println(result);
//	}

}

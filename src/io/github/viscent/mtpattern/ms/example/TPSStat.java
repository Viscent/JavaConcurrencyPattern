package io.github.viscent.mtpattern.ms.example;

import io.github.viscent.mtpattern.tpt.AbstractTerminatableThread;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.SequenceInputStream;
import java.lang.Thread.UncaughtExceptionHandler;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

public class TPSStat {

	public static void main(String[] args) throws Exception {
		try {
			TimeUnit.SECONDS.sleep(20);
		} catch (InterruptedException e) {
			;
		}
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		System.out.println("Started at:" + sdf.format(new Date()));
		String logBaseDir="/home/viscent/tmp/tps/";
		String excludedOperationNames = "";
		String includedOperationNames = "*";
		String destinationSysName = "*";
		Master processor = new Master(logBaseDir,excludedOperationNames,includedOperationNames,destinationSysName);
		BufferedReader fileNamesReader = new BufferedReader(new FileReader(
		    "/home/viscent/tmp/in.dat"));

		ConcurrentMap<String, AtomicInteger> result = processor.calculate(
		    fileNamesReader);
		for (String timeRange : result.keySet()) {
			System.out.println(timeRange + "," + result.get(timeRange));
		}
		System.out.println("Finished at:" + sdf.format(new Date()));
		try {
			TimeUnit.SECONDS.sleep(600);
		} catch (InterruptedException e) {
			;
		}

	}

	private static class Master {
		private final String logFileBaseDir;
		private final String excludedOperationNames;
		private final String includedOperationNames;
		private final String destinationSysName;
		private static final int WORKER_COUNT = Runtime.getRuntime()
		    .availableProcessors() ;

		public Master(String logFileBaseDir,String excludedOperationNames ,String includedOperationNames,String destinationSysName) {
			this.logFileBaseDir=logFileBaseDir;
			this.excludedOperationNames=excludedOperationNames;
			this.includedOperationNames=includedOperationNames;
			this.destinationSysName=destinationSysName;
		}

		public ConcurrentMap<String, AtomicInteger> calculate(
		    BufferedReader fileNamesReader)
		    throws IOException {
			ConcurrentMap<String, AtomicInteger> repository = new ConcurrentSkipListMap<String, AtomicInteger>();
			
			//创建工作者线程
			Worker[] workers = createAndStartWorkers(repository,WORKER_COUNT);
			
			//指派任务给工作者线程
			dispatchTask(fileNamesReader, workers);

			//等待工作者线程处理结束
			for (int i = 0; i < WORKER_COUNT; i++) {
				workers[i].terminate(true);
			}

			//返回处理结果
			return repository;
		}
		
		private Worker[] createAndStartWorkers(ConcurrentMap<String, AtomicInteger> repository,int numberOfWorkers){
			Worker[] workers = new Worker[WORKER_COUNT];
			Worker worker;
			UncaughtExceptionHandler eh=new UncaughtExceptionHandler(){

				@Override
        public void uncaughtException(Thread t, Throwable e) {
	        e.printStackTrace();
	        
        }
				
			};
			for (int i = 0; i < WORKER_COUNT; i++) {
				worker = new Worker(repository, excludedOperationNames,
				    includedOperationNames, destinationSysName);
				workers[i]=worker;
				worker.setUncaughtExceptionHandler(eh);
				worker.start();
			}
			return workers;
		}

		private void dispatchTask(BufferedReader fileNamesReader, Worker[] workers)
		    throws IOException {

			String line;
			Set<String> fileNames = new HashSet<String>();

			int fileCount = 0;
			int N = 5;

			int workerIndex = -1;
			BufferedReader logFileReader;
			while ((line = fileNamesReader.readLine()) != null)

			{

				fileNames.add(line);
				fileCount++;
				if (0 == (fileCount % N)) {
					// 工作者线程间的负载均衡：采用简单的轮询选择worker
					workerIndex = (workerIndex + 1) % WORKER_COUNT;
					logFileReader = makeReaderFrom(fileNames);
					workers[workerIndex].submitWorkload(logFileReader);

					fileNames = new HashSet<String>();
					fileCount = 0;
				}

			}

			if (fileCount > 0) {
				logFileReader = makeReaderFrom(fileNames);
				workerIndex = (workerIndex + 1) % WORKER_COUNT;
				workers[workerIndex].submitWorkload(logFileReader);
			}
		}

		private BufferedReader makeReaderFrom(final Set<String> logFileNames) {
			BufferedReader logFileReader;
			InputStream in = new SequenceInputStream(new Enumeration<InputStream>() {
				private Iterator<String> iterator = logFileNames.iterator();

				@Override
				public boolean hasMoreElements() {
					return iterator.hasNext();
				}

				@Override
				public InputStream nextElement() {
					String fileName = iterator.next();
					InputStream in = null;
					try {
						in = new FileInputStream(logFileBaseDir + fileName);
					} catch (FileNotFoundException e) {
						throw new RuntimeException(e);
					}
					return in;
				}

			});
			logFileReader = new BufferedReader(new InputStreamReader(in));
			return logFileReader;
		}

	}

	private static class Worker extends AbstractTerminatableThread {
		private static final Pattern SPLIT_PATTERN = Pattern.compile("\\|");
		private final ConcurrentMap<String, AtomicInteger> repository;
		private final BlockingQueue<BufferedReader> workQueue;

		private final String selfDevice = "ESB";
		private final String excludedOperationNames;
		private final String includedOperationNames;
		private final String destinationSysName;

		public Worker(ConcurrentMap<String, AtomicInteger> repository,
		    String excludedOperationNames, String includedOperationNames,
		    String destinationSysName) {
			this.repository = repository;
			workQueue = new LinkedBlockingQueue<BufferedReader>();
			this.excludedOperationNames = excludedOperationNames;
			this.includedOperationNames = includedOperationNames;
			this.destinationSysName = destinationSysName;
		}

		public void submitWorkload(BufferedReader taskWorkload) {
			try {
				workQueue.put(taskWorkload);
				terminationToken.reservations.incrementAndGet();
			} catch (InterruptedException e) {
				;
			}
		}

		@Override
		protected void doRun() throws Exception {
			BufferedReader logFileReader = workQueue.take();

			String interfaceLogRecord;
			String[] recordParts;
			String timeStamp;
			AtomicInteger reqCounter;
			AtomicInteger existingReqCounter;
			int i=0;
			try {
				while ((interfaceLogRecord = logFileReader.readLine()) != null) {
					recordParts = SPLIT_PATTERN.split(interfaceLogRecord, 0);
					if(0==((++i) % 10000)){
						Thread.sleep(100);
						i=0;
					}
					
					// 跳过无效记录
					if (recordParts.length < 7) {
						continue;
					}

					if (("request".equals(recordParts[2])) &&

					(recordParts[6].startsWith(selfDevice))) {

						timeStamp = recordParts[0];

						timeStamp = new String(timeStamp.substring(0, 19).toCharArray());

						String operName = recordParts[4];

						reqCounter = new AtomicInteger(0);

						existingReqCounter = repository.putIfAbsent(timeStamp, reqCounter);
						if (null == existingReqCounter) {

						} else {
							reqCounter = existingReqCounter;
						}

						if (isSourceNEEligible(recordParts[5])) {

							if (excludedOperationNames.contains(operName + ",")) {

								continue;

							}

							if ("*".equals(includedOperationNames)) {
								reqCounter.incrementAndGet();

							} else {
								if (includedOperationNames.contains(operName + ",")) {
									reqCounter.incrementAndGet();
								}
							}

						}
					}
				}

			} finally {
				terminationToken.reservations.decrementAndGet();
				logFileReader.close();

			}

		}

		private boolean isSourceNEEligible(String sourceNE) {

			boolean result = false;

			if ("*".equals(destinationSysName)) {

				result = true;

			}

			else if (destinationSysName.equals(sourceNE)) {

				result = true;

			}

			return result;

		}

	}

}

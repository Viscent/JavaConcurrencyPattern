package io.github.viscent.mtpattern.promise.example;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPReply;

public class FTPClientUtil {
//	private static ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
//	    1, Runtime.getRuntime().availableProcessors() * 2, 60, TimeUnit.SECONDS,
//	    new LinkedBlockingQueue<Runnable>());
	
	private final FTPClient ftp = new FTPClient();
	private final Map<String, Boolean> dirCreateMap = new HashMap<String, Boolean>();

	// 私有构造方法
	private FTPClientUtil() {

	}

	public static Future<FTPClientUtil> newInstance(final String ftpServer,
	    final String userName, final String password) {

		Callable<FTPClientUtil> callable = new Callable<FTPClientUtil>() {

			@Override
			public FTPClientUtil call() throws Exception {
				FTPClientUtil self = new FTPClientUtil();
				self.init(ftpServer, userName, password);
				return self;
			}

		};
		final FutureTask<FTPClientUtil> task = new FutureTask<FTPClientUtil>(
		    callable);

		// 下面这行代码与本案例的实际代码并不一致，这是为了讨论方便。
		new Thread(task).start();

//		threadPoolExecutor.execute(task);
		return task;
	}

	private void init(String ftpServer, String userName, String password)
	    throws Exception {

		FTPClientConfig config = new FTPClientConfig();
		ftp.configure(config);

		int reply;
		ftp.connect(ftpServer);

		System.out.print(ftp.getReplyString());

		reply = ftp.getReplyCode();

		if (!FTPReply.isPositiveCompletion(reply)) {
			ftp.disconnect();
			throw new RuntimeException("FTP server refused connection.");
		}
		boolean isOK = ftp.login(userName, password);
		if (isOK) {
			System.out.println(ftp.getReplyString());

		} else {
			throw new RuntimeException("Failed to login." + ftp.getReplyString());
		}

		reply = ftp.cwd("~/subspsync");
		if (!FTPReply.isPositiveCompletion(reply)) {
			ftp.disconnect();
			throw new RuntimeException("Failed to change working directory.reply:"
			    + reply);
		} else {

			System.out.println(ftp.getReplyString());
		}

		ftp.setFileType(FTP.ASCII_FILE_TYPE);

	}

	public void upload(File file) throws Exception {
		InputStream dataIn = new BufferedInputStream(new FileInputStream(file),
		    1024 * 8);
		boolean isOK;
		String dirName = file.getParentFile().getName();
		String fileName = dirName + '/' + file.getName();
		ByteArrayInputStream checkFileInputStream = new ByteArrayInputStream(
		    "".getBytes());

		try {
			if (!dirCreateMap.containsKey(dirName)) {
				ftp.makeDirectory(dirName);
				dirCreateMap.put(dirName, null);
			}

			try {
				isOK = ftp.storeFile(fileName, dataIn);
			} catch (IOException e) {
				throw new RuntimeException("Failed to upload " + file, e);
			}
			if (isOK) {
				ftp.storeFile(fileName + ".c", checkFileInputStream);

			} else {

				throw new RuntimeException("Failed to upload " + file + ",reply:" + ","
				    + ftp.getReplyString());
			}
		} finally {
			dataIn.close();
		}

	}

	public void disconnect() {
		if (ftp.isConnected()) {
			try {
				ftp.disconnect();
			} catch (IOException ioe) {
				// do nothing
			}
		}
		//省略与清单6-2中相同的代码
	}

	public static void dispose() {
//		threadPoolExecutor.shutdown();
	}
}

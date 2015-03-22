package io.github.viscent.mtpattern.promise.example;

import java.io.File;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class DataSyncTask implements Runnable {
	private final Map<String, String> taskParameters;

	public DataSyncTask(Map<String, String> taskParameters) {
		this.taskParameters = taskParameters;
	}

	@Override
	public void run() {
		String ftpServer = taskParameters.get("server");
		String ftpUserName = taskParameters.get("userName");
		String password = taskParameters.get("password");
		
		//先开始初始化FTP客户端实例
		Future<FTPClientUtil> ftpClientUtilPromise = FTPClientUtil.newInstance(
		    ftpServer, ftpUserName, password);

		//查询数据库生成本地文件
		generateFilesFromDB();

		FTPClientUtil ftpClientUtil = null;
		try {
			// 获取初始化完毕的FTP客户端实例
			ftpClientUtil = ftpClientUtilPromise.get();
		} catch (InterruptedException e) {
			;
		} catch (ExecutionException e) {
			throw new RuntimeException(e);
		}
		
		// 上传文件
		uploadFiles(ftpClientUtil);
		
		//省略其它代码
	}

	private void generateFilesFromDB() {
		// 省略其它代码
	}

	private void uploadFiles(FTPClientUtil ftpClientUtil) {
		Set<File> files = retrieveGeneratedFiles();
		for (File file : files) {
			try {
				ftpClientUtil.upload(file);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	private Set<File> retrieveGeneratedFiles() {
		Set<File> files = new HashSet<File>();
		// 省略其它代码
		return files;
	}

}

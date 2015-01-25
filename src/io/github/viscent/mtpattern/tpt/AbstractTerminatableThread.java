package io.github.viscent.mtpattern.tpt;

public abstract class AbstractTerminatableThread extends Thread 
 implements Terminatable {
	public final TerminationToken terminationToken;

	public AbstractTerminatableThread() {
		super();
		this.terminationToken = new TerminationToken();
	}

	/**
	 * 
	 * @param terminationToken 线程间共享的线程终止标志实例
	 */
	public AbstractTerminatableThread(TerminationToken terminationToken) {
		super();
		this.terminationToken = terminationToken;
	}

	protected abstract void doRun() throws Exception;

	protected void doCleanup(Exception cause) {
		//do nothing
	}

	protected void doTerminiate() {
		//do nothing
	}

	@Override
	public void run() {
		Exception ex = null;
		try {
			while (true) {
				/*
				 * 在执行线程的处理逻辑前先判断线程停止的标志。
				 */
				if (terminationToken.isToShutdown()
				    && terminationToken.reservations.get()<=0) {
					break;
				}
				doRun();
			}

		} catch (Exception e) {
			// Allow the thread to terminate in response of an interrupt invocation
			ex = e;
		} finally {
			doCleanup(ex);
		}
	}

	@Override
	public void interrupt() {
		terminate();
	}

	@Override
	public void terminate() {
		terminationToken.setToShutdown(true);
		try {
			doTerminiate();
		} finally {
			// 若无待处理的任务，则试图强制终止线程
			if (terminationToken.reservations.get()<=0) {
				super.interrupt();
			}
		}
	}
	
	public void terminate(boolean waitUtilThreadTerminated){
		terminate();
		if(waitUtilThreadTerminated){
			try {
	      this.join();
      } catch (InterruptedException e) {
	      Thread.currentThread().interrupt();
      }
		}
	}

}

package io.github.viscent.mtpattern.tpt.example;

import io.github.viscent.mtpattern.tpt.AbstractTerminatableThread;
import io.github.viscent.mtpattern.tpt.TerminationToken;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;



public class AlarmMgr {
	private final BlockingQueue<AlarmInfo> alarms = new LinkedBlockingQueue<AlarmInfo>();
	// 告警系统客户端API
	private final AlarmAgent alarmAgent = new AlarmAgent();
	// 告警发送线程
	private final AbstractTerminatableThread alarmSendingThread;

	private boolean shutdownRequested = false;

	private static final AlarmMgr INSTANCE = new AlarmMgr();

	private AlarmMgr() {
		alarmSendingThread = new AbstractTerminatableThread() {
			@Override
			protected void doRun() throws Exception {
				if (alarmAgent.waitUntilConnected()) {
					AlarmInfo alarm;
					alarm = alarms.take();
					terminationToken.reservations.decrementAndGet();
					try {
						alarmAgent.sendAlarm(alarm);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}

			@Override
			protected void doCleanup(Exception exp) {
				if (null != exp) {
					exp.printStackTrace();
				}
				alarmAgent.disconnect();
			}

		};

		alarmAgent.init();
	}

	public static AlarmMgr getInstance() {
		return INSTANCE;
	}

	public void sendAlarm(AlarmType type, String id, String extraInfo) {
		final TerminationToken terminationToken = alarmSendingThread.terminationToken;
		if (terminationToken.isToShutdown()) {
			// log the alarm
			System.err.println("rejected alarm:" + id + "," + extraInfo);
			return;

		}
		try {
			AlarmInfo alarm = new AlarmInfo(id, type);
			alarm.setExtraInfo(extraInfo);
			terminationToken.reservations.incrementAndGet();
			alarms.add(alarm);
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	public void init() {
		alarmSendingThread.start();
	}

	public synchronized void shutdown() {
		if (shutdownRequested) {
			throw new IllegalStateException("shutdown already requested!");
		}
		
		alarmSendingThread.terminate();
		shutdownRequested = true;
	}

	public int pendingAlarms() {
		return alarmSendingThread.terminationToken.reservations.get();
	}

}

class AlarmInfo {
	private String id;
	private String extraInfo;
	private AlarmType type;

	public AlarmInfo(String id, AlarmType type) {
		this.id = id;
		this.type = type;

	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getExtraInfo() {
		return extraInfo;
	}

	public void setExtraInfo(String extraInfo) {
		this.extraInfo = extraInfo;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((extraInfo == null) ? 0 : extraInfo.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AlarmInfo other = (AlarmInfo) obj;
		if (extraInfo == null) {
			if (other.extraInfo != null)
				return false;
		} else if (!extraInfo.equals(other.extraInfo))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "AlarmInfo [type=" + type + ",id=" + id + ", extraInfo=["
		    + extraInfo + "]]";
	}

}

class AlarmAgent {
	// 省略其它代码
	private volatile boolean connectedToServer = false;

	public void sendAlarm(AlarmInfo alarm) throws Exception {
		// 省略其它代码
		System.out.println("Sending " + alarm);
		try {
			Thread.sleep(50);
		} catch (Exception e) {

		}
	}

	public void init() {
		// 省略其它代码
		connectedToServer = true;
	}

	public void disconnect() {
		// 省略其它代码
		System.out.println("disconnected from alarm server.");
	}

	public boolean waitUntilConnected() {
		// 省略其它代码
		return connectedToServer;
	}
}

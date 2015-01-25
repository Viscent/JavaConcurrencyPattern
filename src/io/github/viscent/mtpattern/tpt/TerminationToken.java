package io.github.viscent.mtpattern.tpt;

import java.util.concurrent.atomic.AtomicInteger;

public class TerminationToken {
	//使用volatile修饰，以保证无需显示锁的情况下该变量的内存可见性
	protected volatile boolean toShutdown = false;
	public final AtomicInteger reservations = new AtomicInteger(0);

	public boolean isToShutdown() {
		return toShutdown;
	}

	protected void setToShutdown(boolean toShutdown) {
		this.toShutdown = true;
	}

}
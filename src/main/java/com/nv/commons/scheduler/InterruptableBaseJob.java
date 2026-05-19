package com.nv.commons.scheduler;

import java.util.concurrent.atomic.AtomicReference;

import com.nv.commons.utils.LogUtils;
import org.quartz.InterruptableJob;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * 讓被鎖死的Thread能被強制中斷
 *
 * @author Neutec
 */
public abstract class InterruptableBaseJob implements InterruptableJob {

	protected AtomicReference<Thread> reference = new AtomicReference<>();
	protected long executedTime = System.currentTimeMillis();

	public abstract void run(JobExecutionContext arg) throws JobExecutionException;

	@Override
	public void execute(JobExecutionContext arg) throws JobExecutionException {
		try {
			reference.set(Thread.currentThread());
			executedTime = System.currentTimeMillis();
			run(arg);
		} finally {
			reference.set(null);
		}
	}

	@Override
	public void interrupt() {
		// 避免誤觸，讓超過20秒的才能被中斷
		if (System.currentTimeMillis() - executedTime < 20 * 1000) {
			return;
		}

		Thread thread = reference.getAndSet(null);
		if (thread != null) {
			thread.interrupt();
			LogUtils.SYS.info("interrupt " + getClass().getName());
		}
	}
}

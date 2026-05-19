package com.nv.commons.constants;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.nv.commons.utils.LogUtils;

public enum ThreadPoolType {

	ACCOUNT_UPDATE_LOG(10, 200, 10L, TimeUnit.MINUTES, // 空閒10分鐘的多餘thread會銷毀
		new SynchronousQueue<>()) {
		@Override
		RejectedExecutionHandler handleRejection() {
			return (runnable, executor) -> {
				if (!executor.isShutdown()) {
					LogUtils.SYS.error("All threads in the {} pool are currently busy, {}", this.name(),
						executor.toString());
					runnable.run();
				}
			};
		}
	},
	;

	private final ThreadPoolExecutor executor;

	ThreadPoolType(
		int corePoolSize, // 初始值
		int maximumPoolSize, // 最大值
		long keepAliveTime, // 空閒??的時間多餘thread會銷毀
		TimeUnit unit,
		BlockingQueue<Runnable> workQueue) {

		this.executor = new ThreadPoolExecutor(//
			corePoolSize,
			maximumPoolSize,
			keepAliveTime,
			unit,
			workQueue, //
			this.handleRejection()
		);

	}

	abstract RejectedExecutionHandler handleRejection();

//    public int getActiveCount() {
//        return this.executor.getActiveCount();
//    }

    public int getPoolSize() {
        return this.executor.getPoolSize();
    }

//	public double getActiveThreadPercent() {
//		return (1 - ((double) getActiveCount() / (double) getPoolSize())) * 100 ;
//	}

	public ThreadPoolExecutor getExecutor() {
		return executor;
	}

	public void shutdown() {
		try {
			// previously submittedtasks are executed, but no new tasks will be accepted.
			this.executor.shutdown();

			// service.getActiveCount()  = 正在 active 執行中的 task count
			// service.getTaskCount()    = 已經開始執行 + 已經 schedule 隨時會執行的 task count
			// service.getQueue().size() = queue 中尚未開始執行的 task count
			long taskCount = this.executor.getTaskCount();

			if (taskCount > 0) {
				// 等待還在執行的 job 完成, total 最多等 2分鐘
				this.executor.awaitTermination(2L, TimeUnit.MINUTES);
			}
		} catch (InterruptedException e) {
			LogUtils.SYS.error(e.getMessage(), e);
		}
	}
}

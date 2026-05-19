package com.nv.commons.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

import com.nv.commons.utils.LogUtils;

/**
 * 全域的ThreadPool <br/>
 * 1. 通用型Thread，避免零散的new Thread，可執行各類型工作<br/>
 * 2. 簡化執行緒的撰寫方式 <br/>
 * 3. 可取得執行過程中的exception<br/>
 * 4. 適用的情境增加，請參考TestGlobalThreadPool<br/>
 *
 * @author Alan Hu
 */
public class GlobalThreadPool {

	private static final ExecutorService service = Executors.newVirtualThreadPerTaskExecutor();

	/**
	 * 執行多個工作，並取得執行結果或是例外
	 *
	 * @param tasks
	 * @param semaphore 限制可使用的執行緒數量
	 * @param executor  指定執行的ExecutorService，如果沒有指定，使用內部的ExecutorService
	 * @param <T>
	 * @return
	 */
	private static <T> BlockingQueue<Supplier<T>> call(List<ThreadProcessor<T>> tasks, Semaphore semaphore,
		ExecutorService executor) {
		BlockingQueue<Supplier<T>> queue = new LinkedBlockingQueue<>(tasks.size());

		for (ThreadProcessor<T> task : tasks) {
			Runnable run = () -> {
				Supplier<T> result;
				try {
					T t = task.process();
					result = () -> t;

				} catch (Throwable e) {
					// 紀錄執行期的錯誤
					LogUtils.SYS.error(e.getMessage(), e);

					result = () -> {
						if (e instanceof RuntimeException) {
							throw (RuntimeException) e;
						} else {
							throw new RuntimeException(e);
						}
					};
				} finally {
					if (semaphore != null) {
						semaphore.release();
					}
				}
				// 表示執行完成
				queue.add(result);

			};

			if (semaphore != null) {
				try {
					semaphore.acquire();
				} catch (InterruptedException ignored) {

				}
			}

			if (executor != null) {
				executor.execute(run);
			} else {
				service.execute(run);
			}
		}
		return queue;
	}

	public static <T> BlockingQueue<Supplier<T>> call(List<ThreadProcessor<T>> tasks, ExecutorService executor) {
		return call(tasks, null, executor);
	}

	public static <T> BlockingQueue<Supplier<T>> call(List<ThreadProcessor<T>> tasks, int maximumThreadSize) {
		if (maximumThreadSize <= 0) {
			throw new IllegalArgumentException();
		}
		return call(tasks, new Semaphore(maximumThreadSize), null);
	}

	public static <T> BlockingQueue<Supplier<T>> call(List<ThreadProcessor<T>> tasks, ExecutorService executor, int maximumThreadSize) {
		if (maximumThreadSize <= 0) {
			throw new IllegalArgumentException();
		}
		return call(tasks, new Semaphore(maximumThreadSize), executor);
	}


	public static <T> BlockingQueue<Supplier<T>> call(List<ThreadProcessor<T>> tasks) {
		return call(tasks, null, null);
	}

	/**
	 * 執行多個工作，並指定執行的上限時間，超過上限時間的會被中斷，未超過上限時間的則回傳執行結果或是例外
	 *
	 * @param tasks
	 * @param secondTimeout 執行的上限時間
	 * @param semaphore     限制可使用的執行緒數量
	 * @param executor      指定執行的ExecutorService，如果沒有指定，使用內部的ExecutorService
	 * @param <T>
	 * @return
	 */
	private static <T> BlockingQueue<Supplier<T>> schedule(List<ThreadProcessor<T>> tasks, long secondTimeout,
		Semaphore semaphore, ExecutorService executor) {
		BlockingQueue<Supplier<T>> queue = new LinkedBlockingQueue<>(tasks.size());
		List<Future> futures = new ArrayList<>();

		for (ThreadProcessor<T> task : tasks) {
			Runnable run = () -> {
				Supplier<T> result;
				try {
					T t = task.process();
					result = () -> t;

				} catch (Throwable e) {
					// 紀錄執行期的錯誤
					LogUtils.SYS.error(e.getMessage(), e);

					result = () -> {
						if (e instanceof RuntimeException) {
							throw (RuntimeException) e;
						} else {
							throw new RuntimeException(e);
						}
					};
				} finally {
					if (semaphore != null) {
						semaphore.release();
					}
				}
				// 表示執行完成
				queue.add(result);
			};

			if (semaphore != null) {
				try {
					semaphore.acquire();
				} catch (InterruptedException ignored) {
				}
			}

			if (executor != null) {
				futures.add(executor.submit(run));
			} else {
				futures.add(service.submit(run));
			}
		}

		long timeLeft;
		Future<T> future;
		boolean cancel = false;
		long endTime = System.currentTimeMillis() + secondTimeout * 1000;

		for (int i = 0; i < futures.size(); i++) {
			future = futures.get(i);

			try {
				// 如果目前是cancel，未執行完的都取消
				if (cancel && !future.isDone()) {
					future.cancel(true);
					continue;
				}
				// 計算剩餘時間
				timeLeft = endTime - System.currentTimeMillis();
				// 走中斷流程
				if (timeLeft <= 0) {
					throw new TimeoutException("over " + secondTimeout + " sec.");
				}

				future.get(timeLeft, TimeUnit.MILLISECONDS);

			} catch (TimeoutException | InterruptedException | ExecutionException e) {
				if (!future.isDone()) {
					future.cancel(true);
				}
				cancel = true;
			}
		}

		return queue;
	}

	public static <T> BlockingQueue<Supplier<T>> schedule(List<ThreadProcessor<T>> tasks, long secondTimeout,
		ExecutorService executor) {

		return schedule(tasks, secondTimeout, null, executor);

	}

	public static <T> BlockingQueue<Supplier<T>> schedule(List<ThreadProcessor<T>> tasks, long secondTimeout,
		int maximumThreadSize) {
		if (maximumThreadSize <= 0) {
			throw new IllegalArgumentException();
		}
		return schedule(tasks, secondTimeout, new Semaphore(maximumThreadSize), null);
	}

	public static <T> BlockingQueue<Supplier<T>> schedule(List<ThreadProcessor<T>> tasks, long secondTimeout) {
		return schedule(tasks, secondTimeout, null, null);
	}

	/**
	 * 1. 執行多個工作，並等待結束後才返回<br>
	 * 2. 這邊的設計並不需要使用countdown
	 *
	 * @param tasks
	 * @param semaphore 限制可使用的執行緒數量
	 * @param executor  指定執行的ExecutorService，如果沒有指定，使用內部的ExecutorService
	 * @throws InterruptedException
	 */
	private static void await(List<Runnable> tasks, Semaphore semaphore, ExecutorService executor) {
		if (tasks.size() == 0) {
			return;
		}

		BlockingQueue<Supplier<Object>> queue = new LinkedBlockingQueue<>(tasks.size());
		// 執行
		for (Runnable task : tasks) {
			Runnable run = () -> {
				try {
					task.run();

				} catch (Throwable e) {
					// 紀錄執行期的錯誤
					LogUtils.SYS.error(e.getMessage(), e);
				} finally {
					// 表示執行完成，這邊因為不需要返回結果，所以直接回傳null
					queue.add(() -> null);
					if (semaphore != null) {
						semaphore.release();
					}
				}
			};

			if (semaphore != null) {
				try {
					semaphore.acquire();
				} catch (InterruptedException ignored) {
				}
			}

			if (executor != null) {
				executor.execute(run);
			} else {
				service.execute(run);
			}
		}

		try {
			for (int i = 0; i < tasks.size(); i++) {
				queue.take();
			}
		} catch (InterruptedException e) {
			LogUtils.SYS.error(e.getMessage(), e);
		}
	}

	public static void await(List<Runnable> tasks) {
		await(tasks, null, null);
	}

	public static void await(List<Runnable> tasks, ExecutorService executor) {
		await(tasks, null, executor);
	}

	public static void await(List<Runnable> tasks, int maximumThreadSize, ExecutorService executor) {
		if (maximumThreadSize <= 0) {
			throw new IllegalArgumentException();
		}
		await(tasks, new Semaphore(maximumThreadSize), executor);
	}

	public static void await(List<Runnable> tasks, int maximumThreadSize) {
		if (maximumThreadSize <= 0) {
			throw new IllegalArgumentException();
		}
		await(tasks, new Semaphore(maximumThreadSize), null);
	}

	/**
	 * 執行多個工作，但不等待結果
	 *
	 * @param tasks
	 * @param semaphore 限制可使用的執行緒數量
	 * @param executor  指定執行的ExecutorService，如果沒有指定，使用內部的ExecutorService
	 */
	private static void execute(List<Runnable> tasks, Semaphore semaphore, ExecutorService executor) {
		for (Runnable task : tasks) {
			Runnable run = () -> {
				try {
					task.run();

				} catch (Throwable e) {
					// 紀錄執行期的錯誤
					LogUtils.SYS.error(e.getMessage(), e);
				} finally {
					if (semaphore != null) {
						semaphore.release();
					}
				}
			};

			if (semaphore != null) {
				try {
					semaphore.acquire();
				} catch (InterruptedException ignored) {
				}
			}

			if (executor != null) {
				executor.execute(run);
			} else {
				service.execute(run);
			}
		}
	}

	public static void execute(List<Runnable> tasks, ExecutorService executor) {
		execute(tasks, null, executor);
	}

	public static void execute(List<Runnable> tasks, int maximumThreadSize) {
		if (maximumThreadSize <= 0) {
			throw new IllegalArgumentException();
		}
		execute(tasks, new Semaphore(maximumThreadSize), null);
	}

	public static void execute(List<Runnable> tasks) {
		execute(tasks, null, null);
	}

	public static void execute(List<Runnable> tasks, int maximumThreadSize, ExecutorService executor) {
		if (maximumThreadSize <= 0) {
			throw new IllegalArgumentException();
		}
		execute(tasks, new Semaphore(maximumThreadSize), executor);
	}

	/**
	 * 執行單一個工作，但不等待結果
	 *
	 * @param runnable
	 * @param executor
	 */
	public static void execute(Runnable runnable, ExecutorService executor) {

		final Runnable runnableLogThrowable = () -> {
			try {
				runnable.run();

			} catch (Throwable e) {
				LogUtils.SYS.error(e.getMessage(), e);
			}
		};

		if (executor != null) {
			executor.execute(runnableLogThrowable);
		} else {
			service.execute(runnableLogThrowable);
		}
	}

	/**
	 * 執行單一個工作，但不等待結果
	 *
	 * @param runnable
	 */
	public static void execute(Runnable runnable) {
		execute(runnable, null);
	}

	public static void shutdown() {
		try {
			// previously submittedtasks are executed, but no new tasks will be accepted.
			service.shutdown();
			service.awaitTermination(2L, TimeUnit.MINUTES);
		} catch (InterruptedException e) {
			LogUtils.SYS.error(e.getMessage(), e);
		}
	}


}

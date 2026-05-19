package com.nv.commons.model.database;

import java.util.concurrent.ThreadLocalRandom;

import org.apache.tomcat.jdbc.pool.PoolProperties;

public class DBPoolProperties extends PoolProperties {

	// 預設為一天的時間
	public static final long TOTAL_MILLISECONDS_OF_A_DAY = 24 * 60 * 60 * 1000;

	private volatile long maxAge = TOTAL_MILLISECONDS_OF_A_DAY;

	@Override
	public void setMaxAge(long maxAge) {
		this.maxAge = maxAge;
	}

	@Override
	public long getMaxAge() {
		ThreadLocalRandom random = ThreadLocalRandom.current();
		// 如果超過一天，以1/20的機率(0~19)，慢慢的關閉連線，超過maxAge * 2就強制關閉
		if (random.nextInt(20) > 0) {
			return this.maxAge * 2;
		} else {
			return this.maxAge;
		}

	}

}

package com.nv.commons.cache;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.nv.commons.constants.ServerNodeType;
import com.nv.commons.manager.GlobalThreadPool;
import com.nv.commons.system.SystemInfo;
import com.nv.commons.utils.DateUtils;
import com.nv.commons.utils.LogUtils;
import org.apache.logging.log4j.Logger;

public abstract class AbstractCache {

	protected Logger logger = LogUtils.SYS;

	public abstract void update();

	protected abstract void init();

	public void eager(ServerNodeType... types) {
		if (isExecuted(types)) {
			long start = System.currentTimeMillis();
			init();
			logger.debug("Initializing cache:{} takes:{}s", this.getClass().getSimpleName(),
				DateUtils.secondsElapsedSince(start));
		}
	}

	//TODO prod env 仍舊走eager，先在線上觀望若有需要再指定server做lazy
	public void lazy(ServerNodeType... types) {
		if(!isExecuted(types)){
			return;
		}

		eager(types);
	}

	public abstract void refresh();

	public abstract String getCacheInfo();

	private boolean isExecuted(ServerNodeType... types) {
		boolean isExecuted = true;
		if (types.length > 0) {
			int type = SystemInfo.getInstance().getServerType();

			isExecuted = Arrays.stream(types)
				.map(ServerNodeType::unique)
				.anyMatch(o -> (type & o) > 0);
		}

		return isExecuted;
	}

}

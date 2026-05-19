package com.nv.commons.bo;

import com.nv.commons.cache.RemotingCaller;
import com.nv.commons.constants.CacheType;
import com.nv.commons.exceptions.ParameterNotFoundException;
import com.nv.commons.utils.LogUtils;
import com.nv.commons.utils.RequestParser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Title: com.neutec.nepal.common.bo.RemotingBO<br>
 * Description: server被sync時, 觸發cache update bo
 *
 */
public class RemotingBO {

	/**
	 * 這邊會透過反射機制去reload cache，所以掃描時不會被引用，但實際上會被引用到
	 * 根據 parameter reload cache
	 * @see RemotingCaller#executeMethod(String, HttpServletRequest, HttpServletResponse)
	 *
	 */
	public static boolean reloadCache(HttpServletRequest request, HttpServletResponse response)
		throws Exception, ParameterNotFoundException {
		int cacheTypeId = RequestParser.getIntParameter(request, "cacheType", -1);
		if (cacheTypeId >= 0) {

			CacheType cacheType = CacheType.getInstanceOf(cacheTypeId);
			if (cacheType != null) {
				LogUtils.SYS.info("received reload sync : " + cacheType.name() + " start.");

				cacheType.update();
				return true;
			}
		}
		return false;
	}
}

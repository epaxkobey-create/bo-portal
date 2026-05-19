package com.nv.commons.bo;

import com.nv.commons.cache.VendorCache;
import com.nv.commons.constants.CacheType;
import com.nv.commons.dao.WebsiteVendorDAO;
import com.nv.commons.dto.WebsiteVendor;
import com.nv.commons.utils.DbExecutor;
import com.nv.commons.utils.DbUtils;
import com.nv.commons.utils.NotifyUtils;

public class WebsiteVendorBO {

	public static void updateWebsiteVendor(WebsiteVendor websiteVendor) throws Exception {
		DbExecutor.update(conn ->
			WebsiteVendorDAO.updateWebsiteVendor(conn, websiteVendor)
			);

		VendorCache.getInstance().refresh();
		NotifyUtils.updateCache(CacheType.VENDOR_CACHE);
	}
}

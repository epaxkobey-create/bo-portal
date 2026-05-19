package com.nv.commons.cache;

import java.sql.Connection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

import com.nv.commons.cache.key.AccountContactInfoKey;
import com.nv.commons.constants.WebSiteType;
import com.nv.commons.dao.AccountContactInfoDAO;
import com.nv.commons.dto.AccountContactInfo;
import com.nv.commons.model.database.DBPool;
import com.nv.commons.utils.AccountUtils;
import com.nv.commons.utils.LogUtils;
import org.jetbrains.annotations.NotNull;

public class PlayerAccountContactInfoLocalCache {

	private static final PlayerAccountContactInfoLocalCache instance = new PlayerAccountContactInfoLocalCache();

	// key = webSiteType.unique() + "_" + userID
	private final Map<String, List<AccountContactInfo>> cache = new ConcurrentHashMap<>();

	public PlayerAccountContactInfoLocalCache() {
	}

	public static PlayerAccountContactInfoLocalCache getInstance() {
		return instance;
	}

	/**
	 * key = website_type & contact_type, value = content
	 */
	private final Map<String, Set<String>> contactInfoSet = new HashMap<>();

	public boolean addContactInfo(int websiteType, int contactType, String content) {

		String contactKey = "chk_duplicate_contact_info_" + websiteType + "_" + contactType;

		Set<String> rSet = contactInfoSet.computeIfAbsent(contactKey, contactKeyStr ->
			new ConcurrentSkipListSet<>()
		);

		return rSet.add(content);
	}


	public void removeContactInfo(int websiteType, int contactType, String data) {
		String contactKey = "chk_duplicate_contact_info_" + websiteType + "_" + contactType;
		Set<String> rSet = contactInfoSet.computeIfAbsent(contactKey, contactKeyStr ->
			new ConcurrentSkipListSet<>()
		);

		rSet.remove(data);
	}

	@NotNull
	public List<AccountContactInfo> get(String userKey) {
		List<AccountContactInfo> contactInfo = cache.get(userKey);

		if (null != contactInfo) {
			return contactInfo;
		}

		final String[] userKeys = AccountUtils.parseUserKeyEnhancement(userKey);
		final WebSiteType webSiteType = WebSiteType.getInstance(Integer.parseInt(userKeys[0]));
		final String userId = userKeys[1];

		//如果不存在, 代表還沒有取過DB, 撈DB 回填.
		List<AccountContactInfo> listDB = Collections.emptyList();
		try (Connection conn = DBPool.getReadConnection()) {

			listDB = Optional.ofNullable(
					AccountContactInfoDAO.findAccountContactDataByUserId(conn, userId, webSiteType))
				.orElse(Collections.emptyList());
			//如果是空的, 還是要建一筆Key回去, 這次登入就不會再取.
			put(userKey, listDB);
		} catch (Exception e) {
			LogUtils.accountContactInfo.error(e.getMessage(), e);
		}
		return listDB;
	}

	public void put(String userKey, List<AccountContactInfo> accountContactInfoList) {
		if (accountContactInfoList == null) {
			return;
		}
		cache.put(userKey, accountContactInfoList);
	}

	public void remove(String userKey) {
		cache.remove(userKey);
	}

	public String getAccountContactInfo(String userKey, AccountContactInfoKey accountContactInfoKey) {
		return get(userKey).stream()
			.filter(k -> k.getContactType() == accountContactInfoKey.getContactType())
			.filter(k -> k.getContentNo() == accountContactInfoKey.getContentNo())
			.map(AccountContactInfo::getContent)
			.findAny()
			.orElse(null);
	}
}

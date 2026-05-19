package com.nv.commons.cache;

import com.nv.commons.constants.DocumentType;
import com.nv.commons.dto.AccountDocument;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerAccountDocumentCache {

	private static final PlayerAccountDocumentCache instance = new PlayerAccountDocumentCache();

	// TODO 這邊僅有寫入，但沒有查詢，檢查是否還需要
	private final Map<String, List<AccountDocument>> accountDocumentCache = new ConcurrentHashMap<>();

	// TODO 這邊僅有寫入，但沒有查詢，檢查是否還需要
	private final Map<String, EnumMap<DocumentType, List<AccountDocument>>> otherDocumentCache = new ConcurrentHashMap<>();

	public PlayerAccountDocumentCache() {
	}

	public static PlayerAccountDocumentCache getInstance() {
		return instance;
	}


	public void setAccountDocumentList(String userKey, List<AccountDocument> accountDocumentList) {
		if (accountDocumentList == null) {
			return;
		}
		accountDocumentCache.put(userKey, accountDocumentList);
	}

	/**
	 * 存放document group type 非 document、bank類型
	 */
	public void setOtherDocumentList(String userKey, EnumMap<DocumentType, List<AccountDocument>> bankDocumentList) {
		if (bankDocumentList == null) {
			return;
		}
		otherDocumentCache.put(userKey, bankDocumentList);
	}
}

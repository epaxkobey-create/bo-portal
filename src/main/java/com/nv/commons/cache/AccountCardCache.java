package com.nv.commons.cache;

import java.sql.Connection;
import java.sql.Timestamp;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.nv.commons.dao.AccountCardDAO;
import com.nv.commons.dto.AccountCard;
import com.nv.commons.model.database.DBPool;
import com.nv.commons.utils.JSONUtils;
import com.nv.commons.utils.LogUtils;

/**
 * Cache for AccountCard with DB table AccountCard
 */
public class AccountCardCache extends AbstractCache {

	// <cardId, accountCard>
	private final ConcurrentHashMap<Long, AccountCard> cache = new ConcurrentHashMap<>();

	// 誤差值
	private static final long ERROR_VALUE = 1000;

	private long lastUpdateTime = 0;

	private static final AccountCardCache instance = new AccountCardCache();

	public AccountCardCache() {
	}

	public static AccountCardCache getInstance() {
		return instance;
	}

	@Override
	protected void init() {

		try (Connection conn = DBPool.getReadConnection()) {

			List<AccountCard> list = AccountCardDAO.findAll(conn);

			for (AccountCard card : list) {

				cache.put(card.getId(), card);
			}

			lastUpdateTime = System.currentTimeMillis();
		} catch (Exception e) {
			LogUtils.SYS.error(e.getMessage(), e);
		}
	}

	@Override
	public void update() {

		try (Connection conn = DBPool.getReadConnection()) {

			Timestamp queryTime = new Timestamp(lastUpdateTime);

			long maxUpdateTime = queryTime.getTime();

			boolean isUpdated = false;

			List<AccountCard> list = AccountCardDAO.findAllByUpdateTime(conn, queryTime);

			for (AccountCard cardInDB : list) {

				cache.put(cardInDB.getId(), cardInDB);

				isUpdated = true;
				maxUpdateTime = Math.max(maxUpdateTime, cardInDB.getUpdateTime().getTime());
			}

			if (isUpdated) {
				lastUpdateTime = maxUpdateTime - ERROR_VALUE;
			} else {
				lastUpdateTime = maxUpdateTime;
			}
		} catch (Exception e) {
			LogUtils.SYS.error(e.getMessage(), e);
		}
	}

	@Override
	public void refresh() {
		update();
	}

	@Override
	public String getCacheInfo() {
		return JSONUtils.toJsonString(cache.values());
	}

	public AccountCard getCard(Long cardId) {
		return cache.get(cardId);
	}
}

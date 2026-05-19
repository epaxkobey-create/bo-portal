package com.nv.commons.bo;

import com.nv.commons.cache.GameCache;
import com.nv.commons.constants.CacheType;
import com.nv.commons.constants.GameStatusType;
import com.nv.commons.constants.GameType;
import com.nv.commons.dao.GameDAO;
import com.nv.commons.dto.Game;
import com.nv.commons.model.database.DBPool;
import com.nv.commons.utils.DbUtils;
import com.nv.commons.utils.LogUtils;
import com.nv.commons.utils.NotifyUtils;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class GameBO {

	public static Game getGameById(int id) {
		try (Connection conn = DBPool.getReadConnection()) {
			return GameDAO.findGameById(conn, id);
		} catch (Exception e) {
			LogUtils.SYS.error(e.getMessage(), e);
		}
		return null;
	}

	//避免超過long長度限制改用get String
	public static String getProviderAccountSeq(String providerSystemCode) throws SQLException {

		try (Connection conn = DBPool.getReadConnection()) {
			return GameDAO.getProviderAccountSeq(conn, providerSystemCode);
		}

	}


	public static void updateGameStatus(int gameId, GameStatusType gameStatusType) {
		Connection conn = null;
		try {
			conn = DBPool.getWriteConnection();
			conn.setAutoCommit(false);

			GameDAO.updateGameStatus(conn, gameId, gameStatusType);

			conn.commit();

			GameCache.getInstance().update();
			NotifyUtils.updateCache(CacheType.GAME_CACHE);

		} catch (Exception e) {
			DbUtils.rollback(conn);
			LogUtils.SYS.error(e.getMessage(), e);
		} finally {
			DbUtils.close(conn);
		}
	}

	public static void updateGameDisplayOrder(int gameId, int displayOrder) {
		Connection conn = null;
		try {
			conn = DBPool.getWriteConnection();
			conn.setAutoCommit(false);

			GameDAO.updateGameDisplayOrder(conn, gameId, displayOrder);

			conn.commit();

			GameCache.getInstance().update();
			NotifyUtils.updateCache(CacheType.GAME_CACHE);

		} catch (Exception e) {
			DbUtils.rollback(conn);
			LogUtils.SYS.error(e.getMessage(), e);
		} finally {
			DbUtils.close(conn);
		}
	}

	public static List<Integer> getAvailableGameTypeList() {
		//TODO: 從DB讀取設定檔
		return List.of(GameType.Sport.unique());
	}
}

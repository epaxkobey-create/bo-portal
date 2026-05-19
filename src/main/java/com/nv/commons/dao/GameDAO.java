package com.nv.commons.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

import com.nv.commons.constants.GameStatusType;
import com.nv.commons.dto.Game;
import com.nv.commons.exceptions.Deviation;
import com.nv.commons.model.database.DBQueryRunner;
import org.intellij.lang.annotations.Language;

public class GameDAO {

	/**
	 * 取得所有遊戲清單
	 *
	 */
	public static List<Game> findAll(Connection conn) throws Exception {
		String sql = "SELECT * FROM game";
		return DBQueryRunner.getBeanList(conn, Game.class, sql);
	}

	public static List<Game> find(Connection conn, Timestamp updateTime) throws Exception {
		String sql = "SELECT * FROM game WHERE update_time > ?";
		return DBQueryRunner.getBeanList(conn, Game.class, sql, updateTime);
	}

	public static Game findGameById(Connection conn, int gameId) throws Exception {

		@Language("Oracle")
		String sql = "SELECT * FROM game WHERE id = ? ";
		return DBQueryRunner.getBean(conn, Game.class, sql, gameId);
	}



	//避免超過long長度限制改用get String
	public static String getProviderAccountSeq(Connection conn, String providerSystemCode) throws SQLException {
		// All existing providers use [A-Za-z0-9_] only (e.g., "FC", "PT").
		// This whitelist prevents SQL injection via dynamic sequence names.
		// Update the pattern if a provider system code ever requires $ or # characters.
		if (providerSystemCode == null || !providerSystemCode.matches("[A-Za-z0-9_]+")) {
			throw new Deviation("Invalid providerSystemCode: " + providerSystemCode);
		}
		String sql = "SELECT " + providerSystemCode + "_account_seq.NEXTVAL FROM DUAL";
		return DBQueryRunner.getString(conn, sql);
	}

	public static void updateGameStatus(Connection conn, int gameId, GameStatusType gameStatusType) throws Exception {

		String sql = "UPDATE game SET status = ?, update_time = SYSTIMESTAMP WHERE id = ? ";

		DBQueryRunner.update(conn, sql, gameStatusType.unique(), gameId);
	}

	public static void updateGameDisplayOrder(Connection conn, int gameId, int displayOrder) throws Exception {

		String sql = "UPDATE game SET display_order = ?, update_time = SYSTIMESTAMP WHERE id = ? ";

		DBQueryRunner.update(conn, sql, displayOrder, gameId);
	}
}

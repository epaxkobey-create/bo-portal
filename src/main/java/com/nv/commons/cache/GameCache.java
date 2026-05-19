package com.nv.commons.cache;

import com.nv.commons.bo.GameBO;
import com.nv.commons.cache.key.GameKey;
import com.nv.commons.constants.GameStatusType;
import com.nv.commons.constants.GameType;
import com.nv.commons.constants.PlatformType;
import com.nv.commons.constants.SystemConstants;
import com.nv.commons.dao.GameDAO;
import com.nv.commons.dto.Game;
import com.nv.commons.dto.PageResult;
import com.nv.commons.model.database.DBPool;
import com.nv.commons.service.GameService;
import com.nv.commons.utils.DateUtils;
import com.nv.commons.utils.JSONUtils;
import com.nv.commons.utils.JsonGenerateProcessor;
import com.nv.commons.utils.LogUtils;
import com.nv.commons.utils.PageUtils;
import org.apache.commons.lang3.StringUtils;

import java.sql.Connection;
import java.sql.Timestamp;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author Neutec
 */
public class GameCache extends AbstractCache {

	private static final GameCache instance = new GameCache();
	// 誤差值
	private static final long ERROR_VALUE = 1000;
	private Timestamp lastUpdateTime = new Timestamp(0);

	private Map<Integer, Game> idToGameCache;

	// < [vendorId, gameType, platformType] , <gameCode, game> >
	private Map<GameKey, Map<String, Integer>> gameCache;

	private Map<String, Integer> gameCacheForTxn;


	private GameCache() {
	}

	public static GameCache getInstance() {
		return instance;
	}

	@Override
	protected void init() {

		if (StringUtils.isNotEmpty(System.getProperty(SystemConstants.RUN_JUNIT))) {
			if (this.gameCache != null && !this.gameCache.isEmpty()) {
				return;
			}
		}

		Map<GameKey, Map<String, Integer>> tempGameCache = new ConcurrentHashMap<>();

		Map<String, Integer> tempGameCacheForTxn = new ConcurrentHashMap<>();

		Map<Integer, Game> tempIdToGameCache = new ConcurrentHashMap<>();

		try {
			List<Game> gameList;
			try (Connection conn = DBPool.getReadConnection()) {

				gameList = GameDAO.findAll(conn);
			}

			for (Game game : gameList) {

				tempIdToGameCache.put(game.getId(), game);

				List<PlatformType> platformTypeList = PlatformType.getPlatformTypes(game.getPlatformType());

				for (PlatformType platformType : platformTypeList) {

					GameKey gameKey = new GameKey(game.getVendorId(), game.getGameType(), platformType.unique());

					Map<String, Integer> codeToGame = tempGameCache.computeIfAbsent(gameKey,
						k -> new ConcurrentHashMap<>());

					codeToGame.put(game.getCode(), game.getId());
				}

				String gameUniqueCode = game.getCode();

				tempGameCacheForTxn.put(this.buildKey(game.getVendorId(), gameUniqueCode), game.getId());
			}

			this.idToGameCache = tempIdToGameCache;

			this.gameCache = tempGameCache;

			this.gameCacheForTxn = tempGameCacheForTxn;

		} catch (Exception ex) {
			LogUtils.SYS.error("error while fetch game list", ex);
		}
	}


	public Game getGame(int vendorId, String gameCode, GameType gameType, PlatformType platformType) {

		GameKey gameKey = new GameKey(vendorId, gameType.unique(), platformType.unique());

		Map<String, Integer> codeToGame = this.gameCache.get(gameKey);

		if (codeToGame == null || codeToGame.get(gameCode) == null) {
			return null;
		}
		return getGameById(codeToGame.get(gameCode));
	}

	public List<Game> getGames(Predicate<Game> predicate) {
		return gameCache.values().stream()
			.flatMap(o -> o.values().stream())
			.map(this::getGameById)
			.filter(predicate)
			.distinct()
			.collect(Collectors.toList());
	}


	// for BO
	public PageResult<Game> searchGames(Predicate<Game> predicate, String column, int pageNumber,
		int showCount) {

		Comparator<Game> gameComparator = GameService.generateGameComparator(column);

		List<Game> tmpGameList = getGames(predicate).stream()
			.sorted(gameComparator)
			.collect(Collectors.toList());

		return PageUtils.getPage(tmpGameList, pageNumber, showCount, null);
	}


	/*
	 * worst case: loop all data and find nothing
	 */
	public Game getGameById(int gameId) {

		Game returnGame = idToGameCache.get(gameId);

		if (returnGame != null) {
			return returnGame;
		}

		if (gameId == 0) {
			Game gameNotFound = new Game();
			gameNotFound.setId(gameId);
			gameNotFound.setVendorId(gameId);
			gameNotFound.setGameType(GameType.OTHERS.unique());
			gameNotFound.setName("未找到游戏名称");
			gameNotFound.setNameEn("Game Name Not Found");
			gameNotFound.setCode("");
			gameNotFound.setStatus(GameStatusType.UNKNOWN.unique());
			gameNotFound.setDisplayOrder(999);
			gameNotFound.setIconPath("/");
			gameNotFound.setPlatformType(3);
			returnGame = gameNotFound;
			idToGameCache.put(gameId, returnGame);
		}

		if (returnGame == null) {
			returnGame = GameBO.getGameById(gameId);
			if (returnGame != null) {
				idToGameCache.put(gameId, returnGame);
			}
		}
		return returnGame;
	}

	/**
	 * For GameAPI used
	 */
	public Game getGame(int vendorId, String code) {
		Integer gameId = gameCacheForTxn.get(this.buildKey(vendorId, code));
		return getGameById(gameId == null ? 0 : gameId);
	}

	@Override
	public void update() {
		try (Connection conn = DBPool.getReadConnection()) {

			boolean isUpdated = false;

			Timestamp maxUpdatedTime = Timestamp.from(lastUpdateTime.toInstant());

			List<Game> gameList = GameDAO.find(conn, maxUpdatedTime);

			for (Game gameInDB : gameList) {

				maxUpdatedTime = DateUtils.max(maxUpdatedTime, gameInDB.getUpdateTime());

				Optional<Game> gameInCache = gameCache.values().stream()
					.flatMap(m -> m.values().stream())
					.filter(gameId -> gameId == gameInDB.getId())
					.map(gameId -> idToGameCache.get(gameId))
					.findFirst();

				gameInCache.ifPresent(game ->
					PlatformType.getPlatformTypes(game.getPlatformType()).forEach(
						platformType -> {

							GameKey gameKey = new GameKey(game.getVendorId(), game.getGameType(), platformType.unique());

							gameCache.get(gameKey).remove(game.getCode());
						})
				);

				PlatformType.getPlatformTypes(gameInDB.getPlatformType())
					.stream()
					.map(type -> new GameKey(gameInDB.getVendorId(), gameInDB.getGameType(), type.unique()))
					.forEach(gameKey -> gameCache.computeIfAbsent(gameKey, o -> new ConcurrentHashMap<>())
						.put(gameInDB.getCode(), gameInDB.getId()));

				String gameUniqueCode = gameInDB.getCode();

				gameCacheForTxn.put(this.buildKey(gameInDB.getVendorId(), gameUniqueCode), gameInDB.getId());

				idToGameCache.put(gameInDB.getId(), gameInDB);

				isUpdated = isUpdated ||
					gameInCache.isEmpty() ||
					gameInDB.getUpdateTime().compareTo(gameInCache.get().getUpdateTime()) > 0;
			}

			if (isUpdated) {
				lastUpdateTime = new Timestamp(maxUpdatedTime.getTime() - ERROR_VALUE);
				update();
			} else {
				lastUpdateTime = maxUpdatedTime;
			}

		} catch (Exception ex) {
			LogUtils.operator.error(ex.getMessage(), ex);
		}
		LogUtils.operator.info("update game cache.");

	}

	@Override
	public void refresh() {
		init();
		LogUtils.SYS.info("refresh game cache.");
	}

	@Override
	public String getCacheInfo() {

		JsonGenerateProcessor processor = (jGenerator) -> {

			jGenerator.writeArrayFieldStart("cache");

			jGenerator.writeStartObject();
			jGenerator.writeArrayFieldStart("idToGameCache");

			for (Map.Entry<Integer, Game> entry : idToGameCache.entrySet()) {
				jGenerator.writeStartObject();
				jGenerator.writeObjectField(entry.getKey().toString(), entry.getValue());
				jGenerator.writeEndObject();
			}

			jGenerator.writeEndArray(); // idToGameCache
			jGenerator.writeEndObject();

			jGenerator.writeStartObject();
			jGenerator.writeArrayFieldStart("gameCache");

			for (Map.Entry<GameKey, Map<String, Integer>> entry : gameCache.entrySet()) {
				GameKey gameKey = entry.getKey();
				Map<String, Integer> codeToGame = entry.getValue();

				jGenerator.writeStartObject();
				jGenerator.writeObjectField("gameKey", gameKey);
				jGenerator.writeArrayFieldStart("codeToGame");
				for (Map.Entry<String, Integer> codeToGameEntry : codeToGame.entrySet()) {
					Game game = getGameById(codeToGameEntry.getValue());

					jGenerator.writeStartObject();
					jGenerator.writeObjectField(
						game.getCode() + " - " + game.getName(), game);
					jGenerator.writeEndObject();
				}
				jGenerator.writeEndArray(); // codeToGame
				jGenerator.writeEndObject();
			}

			jGenerator.writeEndArray(); // gameCache
			jGenerator.writeEndObject();

			jGenerator.writeEndArray(); // cache
		};
		return JSONUtils.getJSONString(processor);
	}

	/**
	 * 組成取遊戲資料map key
	 */
	private String buildKey(int vendorId, String gameCode) {
		return vendorId + "," + gameCode;
	}

}

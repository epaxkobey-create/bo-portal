package com.nv.commons.provider.dto.fc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;

public class FCGameIconListResponseDTO extends FCBaseRs {

	@JsonProperty("GetGameIconList")
	private GetGameIconListDTO getGameIconList;

	public GetGameIconListDTO getGetGameIconList() {
		return getGameIconList;
	}

	public void setGetGameIconList(GetGameIconListDTO getGameIconList) {
		this.getGameIconList = getGameIconList;
	}

	/**
	 * 游戏图标列表DTO - 通用版本，支持任意游戏分类
	 */
	public static class GetGameIconListDTO {

		// 使用Map来动态存储所有游戏分类
		// Key: 游戏分类名称 (fishing, arcade, slot, table, etc.)
		// Value: 该分类下的所有游戏 (gameId -> GameInfoDTO)
		@JsonAnySetter
		@JsonAnyGetter
		private Map<String, Map<String, GameInfoDTO>> gameCategories = new HashMap<>();

		/**
		 * 获取所有游戏分类
		 */
		public Map<String, Map<String, GameInfoDTO>> getGameCategories() {
			return gameCategories;
		}

		/**
		 * 设置游戏分类
		 */
		public void setGameCategories(Map<String, Map<String, GameInfoDTO>> gameCategories) {
			this.gameCategories = gameCategories;
		}

		/**
		 * 获取指定分类的游戏列表
		 */
		public Map<String, GameInfoDTO> getGamesByCategory(String categoryName) {
			return gameCategories.get(categoryName);
		}

		/**
		 * 添加游戏分类
		 */
		public void addGameCategory(String categoryName, Map<String, GameInfoDTO> games) {
			gameCategories.put(categoryName, games);
		}

		/**
		 * 获取指定分类下的指定游戏
		 */
		public GameInfoDTO getGame(String categoryName, String gameId) {
			Map<String, GameInfoDTO> categoryGames = gameCategories.get(categoryName);
			return categoryGames != null ? categoryGames.get(gameId) : null;
		}

		/**
		 * 获取所有游戏（扁平化）
		 */
		public List<GameWithCategoryDTO> getAllGames() {
			List<GameWithCategoryDTO> allGames = new ArrayList<>();
			for (Map.Entry<String, Map<String, GameInfoDTO>> categoryEntry : gameCategories.entrySet()) {
				String categoryName = categoryEntry.getKey();
				for (Map.Entry<String, GameInfoDTO> gameEntry : categoryEntry.getValue().entrySet()) {
					String gameId = gameEntry.getKey();
					GameInfoDTO gameInfo = gameEntry.getValue();
					allGames.add(new GameWithCategoryDTO(categoryName, gameId, gameInfo));
				}
			}
			return allGames;
		}

		/**
		 * 检查是否包含指定分类
		 */
		public boolean hasCategory(String categoryName) {
			return gameCategories.containsKey(categoryName);
		}

		/**
		 * 获取游戏总数
		 */
		public int getTotalGameCount() {
			return gameCategories.values().stream()
				.mapToInt(Map::size)
				.sum();
		}

		public static class GameWithCategoryDTO {

			private String categoryName;
			private String gameId;
			private GameInfoDTO gameInfo;

			public GameWithCategoryDTO() {
			}

			public GameWithCategoryDTO(String categoryName, String gameId, GameInfoDTO gameInfo) {
				this.categoryName = categoryName;
				this.gameId = gameId;
				this.gameInfo = gameInfo;
			}

			public String getCategoryName() {
				return categoryName;
			}

			public void setCategoryName(String categoryName) {
				this.categoryName = categoryName;
			}

			public String getGameId() {
				return gameId;
			}

			public void setGameId(String gameId) {
				this.gameId = gameId;
			}

			public GameInfoDTO getGameInfo() {
				return gameInfo;
			}

			public void setGameInfo(GameInfoDTO gameInfo) {
				this.gameInfo = gameInfo;
			}

			@Override
			public String toString() {
				return "GameWithCategoryDTO{" +
					"categoryName='" + categoryName + '\'' +
					", gameId='" + gameId + '\'' +
					", gameInfo=" + gameInfo +
					'}';
			}
		}

		/**
		 * 游戏信息DTO
		 */
		public static class GameInfoDTO {

			@JsonProperty("status")
			private String status;

			@JsonProperty("gameNameOfChinese")
			private String gameNameOfChinese;

			@JsonProperty("gameNameOfEnglish")
			private String gameNameOfEnglish;

			@JsonProperty("enUrl")
			private String enUrl;

			@JsonProperty("cnUrl")
			private String cnUrl;

			public String getStatus() {
				return status;
			}

			public void setStatus(String status) {
				this.status = status;
			}

			public String getGameNameOfChinese() {
				return gameNameOfChinese;
			}

			public void setGameNameOfChinese(String gameNameOfChinese) {
				this.gameNameOfChinese = gameNameOfChinese;
			}

			public String getGameNameOfEnglish() {
				return gameNameOfEnglish;
			}

			public void setGameNameOfEnglish(String gameNameOfEnglish) {
				this.gameNameOfEnglish = gameNameOfEnglish;
			}

			public String getEnUrl() {
				return enUrl;
			}

			public void setEnUrl(String enUrl) {
				this.enUrl = enUrl;
			}

			public String getCnUrl() {
				return cnUrl;
			}

			public void setCnUrl(String cnUrl) {
				this.cnUrl = cnUrl;
			}

			@Override
			public String toString() {
				return "GameInfoDTO{" +
					"status='" + status + '\'' +
					", gameNameOfChinese='" + gameNameOfChinese + '\'' +
					", gameNameOfEnglish='" + gameNameOfEnglish + '\'' +
					", enUrl='" + enUrl + '\'' +
					", cnUrl='" + cnUrl + '\'' +
					'}';
			}
		}
	}
}











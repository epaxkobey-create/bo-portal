package com.nv.commons.cache;

import java.sql.Connection;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.nv.commons.constants.CurrencyType;
import com.nv.commons.dao.BankDAO;
import com.nv.commons.dto.Bank;
import com.nv.commons.model.database.DBPool;
import com.nv.commons.utils.JSONUtils;
import com.nv.commons.utils.LogUtils;

public class BankCache extends AbstractCache {

	private ConcurrentHashMap<CurrencyType, ConcurrentHashMap<Integer, Bank>> cache = new ConcurrentHashMap<>();

	private static final BankCache instance = new BankCache();

	private BankCache() {
	}

	public static BankCache getInstance() {
		return instance;
	}

	@Override
	protected void init() {

		try (Connection conn = DBPool.getReadConnection()) {

			List<Bank> bankList = BankDAO.findAll(conn);

			cache = generateFullCache(bankList);

		} catch (Exception ex) {
			LogUtils.SYS.error("error while fetch Bank list", ex);
		}
	}

	@Override
	public void update() {

		init();

		LogUtils.SYS.info("update Bank Cache");
	}

	public Bank getBank(int id) {

		for (CurrencyType currencyType : CurrencyType.values()) {

			Bank bank = cache.computeIfAbsent(currencyType, k -> new ConcurrentHashMap<>()).get(id);

			if (bank != null) {
				return bank;
			}
		}
		return null;
	}

	public Bank getBank(CurrencyType currencyType, int id) {
		return cache.computeIfAbsent(currencyType, k -> new ConcurrentHashMap<>()).get(id);
	}

	// allBank，不經過任何篩選
	public List<Bank> getAll(CurrencyType currencyType) {
		Collection<Bank> banks = cache.computeIfAbsent(currencyType, k -> new ConcurrentHashMap<>()).values();
		return banks.stream().sorted(Comparator.comparing(Bank::getId)).collect(Collectors.toList());
	}


	private ConcurrentHashMap<CurrencyType, ConcurrentHashMap<Integer, Bank>> generateFullCache(List<Bank> bankList) {

		ConcurrentHashMap<CurrencyType, ConcurrentHashMap<Integer, Bank>> tempCache = new ConcurrentHashMap<>();

		for (Bank bank : bankList) {

			List<Integer> currencyTypeIdList = getCurrencyListOfBank(bank);

			for (Integer currencyTypeId : currencyTypeIdList) {
				try {
					CurrencyType currencyType = CurrencyType.getInstance(currencyTypeId);

					tempCache.computeIfAbsent(currencyType, k -> new ConcurrentHashMap<>())
						.put(bank.getId(), bank);

				} catch (Exception e) {
					LogUtils.SYS.error("{}: {}", bank.getBankName(), e.getMessage(), e);
				}
			}
		}
		return tempCache;
	}

	private List<Integer> getCurrencyListOfBank(Bank bank) {
		if (bank.getCurrencyTypeId() == 0) {
			// 代表這個銀行支援所有幣別，因此要把所有幣別的 ID 都加入清單
			return Arrays.stream(CurrencyType.values()).map(CurrencyType::unique).collect(Collectors.toList());
		}
		return List.of(bank.getCurrencyTypeId());
	}

	@Override
	public void refresh() {
		init();
		LogUtils.SYS.info("refresh Bank Cache");
	}

	@Override
	public String getCacheInfo() {
		return JSONUtils.toJsonString(cache.values());
	}

}
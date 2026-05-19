package com.nv.commons.provider.service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import com.nv.commons.constants.LanguageType;
import com.nv.commons.constants.PlatformType;
import com.nv.commons.dto.AccountProvider;
import com.nv.commons.dto.Game;
import com.nv.commons.provider.dto.APIResponse;

public interface ProviderService {
	// Methods from ProviderProxy
//	BigDecimal getActualTransferAmount(BigDecimal amount);
	BigDecimal getBalance(AccountProvider accountProvider) throws Exception;
	void getBalance(AccountProvider... accountProviders) throws Exception;
//	void syncAccountProviderBalance(Set<AccountProvider> accountProviderSet);
//	void syncAccountProviderBalanceByGroup(Set<AccountProvider> accountProviderSet, int group);
//	void syncMultiAccountProviderBalance(Set<AccountProvider> accountProviderSet, int count);
//	void updateAccountProviderBalance(List<GameTransaction> gameTxnList);
//	void updateAccountProviderBalance(List<GameTransaction> gameTxnList, int group);

	// Methods from ProviderProxy
	APIResponse getGameTransaction(Date startDate, Date endDate) throws Exception;
	String getGameLoginUrl(AccountProvider accountProvider, Game game, PlatformType platformType, String loginIP, LanguageType languageType) throws Exception;
	String getViewGameUrl(Game game, PlatformType platformType, String fromIp, String requestURL, LanguageType languageType) throws Exception;
	boolean updateAccount(AccountProvider accountProvider, String oldPassword) throws Exception;
	boolean kickOutAllUser() throws Exception;
	boolean kickOutUser(AccountProvider accountProvider) throws Exception;
	boolean kickOutByGame(Game game) throws Exception;
	List<String> getAllActiveUsers()throws Exception;
}
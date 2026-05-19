package com.nv.commons.bo;

import java.sql.Connection;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.nv.commons.cache.SystemSettingCache;
import com.nv.commons.constants.AccountUpdateType;
import com.nv.commons.constants.SystemSettingKeyConstants;
import com.nv.commons.constants.WebSiteType;
import com.nv.commons.dao.AccountCardDAO;
import com.nv.commons.dao.AccountUpdateLogDAO;
import com.nv.commons.dto.Account;
import com.nv.commons.dto.AccountCard;
import com.nv.commons.dto.BinData;
import com.nv.commons.exceptions.Deviation;
import com.nv.commons.model.database.DBPool;
import com.nv.commons.utils.AccountUpdateLogUtils;
import com.nv.commons.utils.DbExecutor;
import com.nv.commons.utils.DbUtils;
import com.nv.commons.utils.JSONUtils;
import com.nv.commons.utils.JsonGenerateProcessor;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

public class AccountCardBO {

	public static AccountCard findFirstActiveCardByUserId(Account account) throws Exception {

		List<AccountCard> accountCardList = getAccountCardList(account.getUserId(), account.getWebsiteType());

		if (accountCardList.isEmpty())
			return null;

		return accountCardList.getFirst();
	}

	public static AccountCard addAccountCard(Account account, String normalizedCardNo, String cardHolderName,
		String expMonthYear, String updaterIp)
		throws Exception {

		if (!isValidLuhn(normalizedCardNo)) {
			throw new Deviation("Invalid card number.");
		}

		String normBin = normalizeBin(normalizedCardNo.substring(0, 8));
		BinData binData = Optional.ofNullable(BinDataBO.findByBin(normBin))
			.orElseThrow(() -> new Deviation("Only VISA / MasterCard / JCB cards are supported."));

		String allowedCardNumbersStr = SystemSettingCache.getInstance()
			.getByKey(SystemSettingKeyConstants.ALLOWED_CARD_NUMBERS).getValue();

		if (StringUtils.isNotEmpty(allowedCardNumbersStr)) {
			Set<String> allowedCardNumbers = Arrays.stream(allowedCardNumbersStr.split(","))
				.map(String::trim)
				.collect(Collectors.toSet());
			if (!allowedCardNumbers.contains(normalizedCardNo)) {
				throw new Deviation("Card number is not allowed.");
			}
		}

		Connection conn = null;
		try {
			conn = DBPool.getWriteConnection();
			conn.setAutoCommit(false);

			AccountCard accountCard = new AccountCard();
			accountCard.setUserId(account.getUserId());
			accountCard.setWebsiteType(account.getWebsiteType());
			accountCard.setCardNo(normalizedCardNo);
			accountCard.setCardholderName(cardHolderName);
			accountCard.setExpMonthYear(expMonthYear);
			accountCard.setBankName(binData.getBankName());
			accountCard.setCardSchemeType(binData.getCardSchemeType());
			accountCard.setStatus(1);

			int result = AccountCardDAO.insert(conn, accountCard);

			if (result != 1) {
				throw new Deviation("Insert account card failed.");
			}

			AccountUpdateLogUtils.setInfo(account.getUserId(), updaterIp);

			AccountUpdateLogDAO.insert(conn, AccountUpdateLogUtils.getAccountUpdateLog(
				account.getUserId(), AccountUpdateType.CREDIT_CARD,
				"", JSONUtils.toJsonString(accountCard),
				"User add new account card."));

			conn.commit();

			return accountCard;
		} catch (Exception e) {
			DbUtils.rollback(conn);
			throw e;
		} finally {
			DbUtils.close(conn);
		}
	}

	/**
	 * Luhn algorithm (Mod 10) — validates credit card number checksum.
	 */
	private static boolean isValidLuhn(String cardNumber) {
		int sum = 0;
		boolean alternate = false;
		for (int i = cardNumber.length() - 1; i >= 0; i--) {
			int digit = cardNumber.charAt(i) - '0';
			if (alternate) {
				digit *= 2;
				if (digit > 9)
					digit -= 9;
			}
			sum += digit;
			alternate = !alternate;
		}
		return sum % 10 == 0;
	}

	public static AccountCard findById(long id) {
		Connection conn = null;
		try {
			conn = DBPool.getReadConnection();
			return AccountCardDAO.findById(conn, id);
		} catch (Exception e) {
			DbUtils.rollback(conn);
			throw new RuntimeException(e);
		} finally {
			DbUtils.close(conn);
		}
	}

	private static String normalizeBin(String bin) throws Exception {
		if (bin == null) {
			throw new Exception("BIN is required.");
		}
		String d = bin.replaceAll("\\s|-", "");
		if (!d.matches("\\d{8}")) {
			throw new Exception("BIN must be 8 digits.");
		}
		return d;
	}

	public static String maskCardNumber(String number) {
		if (number == null || number.length() < 4)
			return "**** **** **** ****";
		String last4 = number.substring(number.length() - 4);
		return "**** **** **** " + last4;
	}

	public static boolean removeAccountCard(String userId, WebSiteType webSiteType, String updater, String updaterIp)
		throws Exception {

		Account account = AccountBO.getByUserId(userId, webSiteType);

		AccountCard accountCard = AccountCardBO.findFirstActiveCardByUserId(account);

		return DbExecutor.update(conn -> {

			boolean isUpdated = AccountCardDAO.updateAsInactive(conn, userId, webSiteType.unique()) > 0;

			if (isUpdated) {

				AccountUpdateLogUtils.setInfo(updater, updaterIp);

				return AccountUpdateLogDAO.insert(conn, AccountUpdateLogUtils.getAccountUpdateLog(
					userId, AccountUpdateType.CREDIT_CARD,
					JSONUtils.toJsonString(accountCard), "",
					"Admin remove account card")) > 0;
			}

			return false;
		});
	}

	@NotNull
	public static List<AccountCard> getAccountCardList(String userId, int webSiteType)
		throws Exception {

		List<AccountCard> accountCardList = DbExecutor.query(conn ->
			AccountCardDAO.findActiveListByUserId(conn, userId, webSiteType)
		);

		if (accountCardList == null || accountCardList.isEmpty()) {
			return Collections.emptyList();
		}
		return accountCardList;
	}

	public static String getAccountCardsByUserId(String userId, int webSiteType)
		throws Exception {

		List<AccountCard> accountCardList = getAccountCardList(userId, webSiteType);

		JsonGenerateProcessor generateProcessor = (jGenerator -> {
			jGenerator.writeArrayFieldStart("data");
			for (AccountCard accountCard : accountCardList) {
				jGenerator.writeStartObject();
				jGenerator.writeStringField("bankName", accountCard.getBankName());
				jGenerator.writeStringField("cardNumber", accountCard.getCardNo());
				jGenerator.writeNumberField("value", accountCard.getId());
				jGenerator.writeEndObject();
			}
			jGenerator.writeEndArray();
		});

		return JSONUtils.getJSONString(generateProcessor);

	}
}

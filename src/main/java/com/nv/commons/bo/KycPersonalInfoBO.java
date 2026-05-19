package com.nv.commons.bo;

import java.sql.Connection;
import java.util.List;
import java.util.Objects;

import com.nv.commons.constants.AccountUpdateType;
import com.nv.commons.constants.CurrencyType;
import com.nv.commons.constants.WebSiteType;
import com.nv.commons.dao.KycPersonalInfoDAO;
import com.nv.commons.dto.AccountUpdateLog;
import com.nv.commons.dto.KycPersonalInfo;
import com.nv.commons.dto.UpdateRecord;
import com.nv.commons.model.database.DBPool;
import com.nv.commons.utils.AccountUtils;
import com.nv.commons.utils.DbUtils;
import com.nv.commons.utils.JSONUtils;
import com.nv.commons.utils.LogUtils;

public class KycPersonalInfoBO {

	public static KycPersonalInfo find(long accountDocumentId, String userId, WebSiteType webSiteType) {
		try (Connection conn = DBPool.getReadConnection()) {
			return KycPersonalInfoDAO.find(conn, accountDocumentId, userId, webSiteType);
		} catch (Exception e) {
			LogUtils.SYS.error(e.getMessage(), e);
		}
		return null;
	}

	public static boolean insert(KycPersonalInfo kycPersonalInfo) {
		Connection conn = null;

		try {
			conn = DBPool.getWriteConnection();
			conn.setAutoCommit(false);

			KycPersonalInfoDAO.insert(conn, kycPersonalInfo);

			conn.commit();

		} catch (Exception e) {
			DbUtils.rollback(conn);
			LogUtils.SYS.error(e.getMessage(), e);
			return false;
		} finally {
			DbUtils.close(conn);
		}

		return true;
	}

	public static boolean update(KycPersonalInfo kycPersonalInfo, CurrencyType currencyType,
		List<AccountUpdateLog> accountUpdateLogList, String updater, String updaterIp) {
		Connection conn = null;

		try {
			conn = DBPool.getWriteConnection();
			conn.setAutoCommit(false);

			KycPersonalInfo kycPersonalInfoOld = find(kycPersonalInfo.getAccountDocumentId(), kycPersonalInfo.getUserId(),
				WebSiteType.getInstance(kycPersonalInfo.getWebsiteType()));

			int updateCount = KycPersonalInfoDAO.update(conn, kycPersonalInfo);

			LogUtils.accountDocument.info("Kyc personal info updated");

			if (updateCount != 1) {
				throw new Exception("KycPersonalInfo Update Error");
			}

			if (accountUpdateLogList != null) {
				List<Object[]> updates = List.of(
					new Object[] { AccountUpdateType.ADDRESS, "Address",
						JSONUtils.getJSONString("Street", kycPersonalInfoOld.getStreet(),"City",kycPersonalInfoOld.getCity(),"Postal code", kycPersonalInfoOld.getPostalCode() ),
						JSONUtils.getJSONString("Street",kycPersonalInfo.getStreet(),"City", kycPersonalInfo.getCity(),"Postal code",  kycPersonalInfo.getPostalCode() ),
					},
					new Object[] { AccountUpdateType.FULL_NAME, "Full name", kycPersonalInfoOld.getFullName(), kycPersonalInfo.getFullName() },
					new Object[] { AccountUpdateType.DOB, "Date of birth", kycPersonalInfoOld.getDobStr(), kycPersonalInfo.getDobStr() },
					new Object [] {AccountUpdateType.DOCUMENT_NO, "Document No", kycPersonalInfoOld.getDocumentNo(), kycPersonalInfo.getDocumentNo()}
				);

				updates.forEach(update -> {
					AccountUpdateType type = (AccountUpdateType) update[0];
					String fieldName = (String) update[1];
					String before = Objects.toString(update[2], "-");
					String after = Objects.toString(update[3], "-");

					if (!before.equals(after)) {
						accountUpdateLogList.add(AccountUtils.getAccountUpdateLog(
							kycPersonalInfo.getUserId(), kycPersonalInfo.getWebsiteType(), type,
							new UpdateRecord(before, after,  fieldName ),
							updater, updaterIp, currencyType.unique()));
					}
				});
			}

			conn.commit();

		} catch (Exception e) {
			DbUtils.rollback(conn);
			LogUtils.SYS.error(e.getMessage(), e);
			return false;
		} finally {
			DbUtils.close(conn);
		}

		return true;
	}

	public static boolean isExistRecord(long accountDocumentId, WebSiteType webSiteType, String userId) {
		try (Connection conn = DBPool.getReadConnection()) {
			return KycPersonalInfoDAO.isExistRecord(conn, accountDocumentId, userId, webSiteType);
		} catch (Exception e) {
			LogUtils.SYS.error(e.getMessage(), e);
		}
		return false;
	}
}

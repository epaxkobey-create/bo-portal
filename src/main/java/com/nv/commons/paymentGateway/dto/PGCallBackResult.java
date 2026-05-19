package com.nv.commons.paymentGateway.dto;

import com.nv.commons.constants.PGCallBackStatusType;
import com.nv.commons.dto.MoneyTransaction;

public class PGCallBackResult {

    private MoneyTransaction moneyTransaction;
    //call back 加密是否符合
    private boolean isEncryptCheck = false;
    //call back 狀態是否成功
    private PGCallBackStatusType pgCallBackStatusType = PGCallBackStatusType.PENDING;

    private String checkPaymentMsg;

    public MoneyTransaction getMoneyTransaction() {
        return moneyTransaction;
    }

    public void setMoneyTransaction(MoneyTransaction moneyTransaction) {
        this.moneyTransaction = moneyTransaction;
    }

    public boolean isEncryptCheck() {
        return isEncryptCheck;
    }

    public void setEncryptCheck(boolean encryptCheck) {
        isEncryptCheck = encryptCheck;
    }

    public PGCallBackStatusType getPgCallBackStatusType() {
        return pgCallBackStatusType;
    }

    public void setPgCallBackStatusType(PGCallBackStatusType pgCallBackStatusType) {
        this.pgCallBackStatusType = pgCallBackStatusType;
    }

    public String getCheckPaymentMsg() {
        return checkPaymentMsg;
    }

    public void setCheckPaymentMsg(String checkPaymentMsg) {
        this.checkPaymentMsg = checkPaymentMsg;
    }
}

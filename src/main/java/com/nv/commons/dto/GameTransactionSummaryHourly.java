package com.nv.commons.dto;

import com.nv.commons.annotation.Column;

public class GameTransactionSummaryHourly extends GameTransactionSummary{

	@Column(name = "IS_SUMMARIZED")
	private int isSummarized;

	public int getIsSummarized() {
		return isSummarized;
	}

	public void setIsSummarized(int isSummarized) {
		this.isSummarized = isSummarized;
	}

}

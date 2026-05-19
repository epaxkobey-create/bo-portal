package com.nv.commons.provider.dto.fc;

import com.nv.commons.provider.dto.APIResponse;

public class FCBaseRs implements APIResponse {

	private static final int SUCCESS = 0;

	public int Result = -1;

	@Override
	public boolean isSuccess() {
		return SUCCESS == Result;
	}

	public int getResult() {
		return Result;
	}

	public void setResult(int result) {
		this.Result = result;
	}
}






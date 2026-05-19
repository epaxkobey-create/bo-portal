if (typeof (SubGameTransactionHandler) == 'undefined') {
	SubGameTransactionHandler = {};
}

(function() {

	const timeZoneZero = ['JILI', 'KM', 'SG', 'RT', 'MG', 'LUDO', 'EVO'];
	const timeZoneEight = ['PT', 'PG', 'JDB', 'AE', 'BPOKER', 'CRICKET'];
	const timeZoneMinusFour = ['Saba'];

	SubGameTransactionHandler.init = function() {
		CurrencyUtil.updateSetting({
			"precision": 2,
			"trailingZeros": false,
			"roundingMode": CurrencyUtil.roundingMode.HALF_UP
		});
		SubGameTransactionHandler.search();
	};

	SubGameTransactionHandler.search = function() {
		var $searchForm = $('[name=searchForm]');
		var userId = $searchForm.find('[name=userId]').val();
		var txnId = $searchForm.find('[name=gameTxnId]').val();
		var txnDate = $searchForm.find('[name=txnDate]').val();
		var settleDate = $searchForm.find('[name=settleDate]').val();
		var txnStatus = $searchForm.find('[name=txnStatus]').val();

		$.ajax({
			type: "POST",
			url: '/manager/member/searchGameTransactionDetail',
			data: {
				'id': txnId,
				'txnDate': txnDate,
				'settleDate': settleDate,
				'txnStatus': txnStatus,
				'userId': userId
			},
			dataType: 'JSON',
			success: function(data) {
				if (data.error) {
					NotifyHandler.errorMsg(data.error);
					return;
				}
				renderGameTxn(data);
			}
		});
	};

	var arrayConstructor = [].constructor;
	var objectConstructor = {}.constructor;

	function renderGameTxn(data) {
		data = data.gameTxn[0];
		var $gameForm = $('[name=gameForm]');
		$gameForm.find('#settledTimeDiv').hide();

		$gameForm.find('#userID').text(data.userID);
//		$gameForm.find('#vendorTxnID').text(data.vendorTxnID).append('&nbsp;');
		let $link;
		if (parseInt(data.gameTypeId) === 4) {
			$link = `${data.vendorTxnID}`;
		} else {
			$link = $(`<a style='text-decoration: underline;' class='user-link' href='javascript:void(0);'>${data.recordId}</a>`);
			$link.on('click', function() {
				SubGameTransactionHandler.getOriginReport(data.userID, data.gameTypeId, data.recordId, data.vendorID);
			});
		}
		$gameForm.find('#txnID').html(data.txnID).append('&nbsp;');
		$gameForm.find('#vendorTxnID').html($link).append('&nbsp;');
		$gameForm.find('#vendor').text(data.vendorName);
		$gameForm.find('#gameType').text(data.gameType);
		$gameForm.find('#txnTime').text(DateUtil.format(data.txnTime, PageConfig.DateHourMinuteSecondPattern));
		$gameForm.find('#createTime').text(DateUtil.format(data.createTime, PageConfig.DateHourMinuteSecondPattern));
		// $gameForm.find('#currency').text(data.currency);
		$gameForm.find('#odds').html(CurrencyUtil.formatter(data.odds));
		$gameForm.find('#oddsType').html(data.oddsType);
		// if (PageConfig.accountCurrency == CurrencyType.CNY.value) {
		// 	$gameForm.find('#gameName').text(data.gameName);
		// } else {
		// 	$gameForm.find('#gameName').text(data.gameNameEn);
		// }
		$gameForm.find('#gameName').text(data.gameNameEn);
		$gameForm.find('#status').text(data.systemTxnStatusName);

		let $settleTime;
		let $betAmount;
		let $winAmount;
		let $profitLossAmount;
		let $turnoverAmount;

		if (data.systemTxnStatus !== PageConfig.SystemTxnStatusType.UNSETTLED) {
			$settleTime = DateUtil.format(data.vendorTxnLastModifyTime, PageConfig.DateHourMinuteSecondPattern);
			$betAmount = CurrencyUtil.formatter(data.betAmount);
			$winAmount = CurrencyUtil.formatter(data.winAmount);

			if (data.systemTxnStatus === PageConfig.SystemTxnStatusType.SETTLED) {
				$profitLossAmount = CurrencyUtil.formatter(data.profitLoss);
				$turnoverAmount = CurrencyUtil.formatter(data.turnover);
			} else {
				$profitLossAmount = '-';
				$turnoverAmount = '-';
			}
		} else {
			$settleTime = '-';
			$betAmount = CurrencyUtil.formatter(data.betAmount);
			$winAmount = CurrencyUtil.formatter(data.betAmount);
			$profitLossAmount = CurrencyUtil.formatter(0.00);
			$turnoverAmount = CurrencyUtil.formatter(0.00);
		}

		$gameForm.find('#settleTime').text($settleTime);

		$gameForm.find('#betAmount').html($betAmount);
		$gameForm.find('#winAmount').html($winAmount);
		$gameForm.find('#profit').html($profitLossAmount);
		$gameForm.find('#turnover').html($turnoverAmount);

		$gameForm.find('#realBetAmount').html(CurrencyUtil.formatter(data.realBetAmount));
		$gameForm.find('#adjustAmount').html(CurrencyUtil.formatter(data.adjustAmount));
		$gameForm.find('#progressBetAmount').html(CurrencyUtil.formatter(data.progressBetAmount));
		$gameForm.find('#progressProfitLoss').html(CurrencyUtil.formatter(data.progressProfitLoss));

		if (data.gameInfoJson) {

			try {
				const indexOfTimeZoneZero = timeZoneZero.indexOf(data.vendorID);
				const indexOfTimeZoneEight = timeZoneEight.indexOf(data.vendorID);
				const indexOfTimeZoneMinusFour = timeZoneMinusFour.indexOf(data.vendorID);
				const matchVendorTimeZone = indexOfTimeZoneZero > -1 || indexOfTimeZoneEight > -1 || indexOfTimeZoneMinusFour > -1;

				if (matchVendorTimeZone) {
					JsCache.get('#originalData #templateTimeZone').show();
					if (indexOfTimeZoneZero > -1) {
						JsCache.get('#originalData #templateTimeZone #timeZone').text('GMT+0');
					}
					if (indexOfTimeZoneEight > -1) {
						JsCache.get('#originalData #templateTimeZone #timeZone').text('GMT+8');
					}
					if (indexOfTimeZoneMinusFour > -1) {
						JsCache.get('#originalData #templateTimeZone #timeZone').text('GMT-4');
					}
				}

				// var json = JSONbig.parse(data.gameInfoJson.replace(/\\/g, '\\\\'));
				var json = JSONbig.parse(data.gameInfoJson);

				Object.keys(json).forEach(function(key) {
					var value = json[key];
					if (value instanceof arrayConstructor || value instanceof objectConstructor) {
						value = JSON.stringify(value);
					}

					var templateTr = JsCache.get('#templateTr').attr('id', '').clone().show();
					templateTr.find('#key').attr('id', '').text(key);
					templateTr.find('#value').attr('id', '').text(value == null ? '' : value);
					JsCache.get('#originalData > tbody').append(templateTr);
				});
			} catch (e) {
				var templateTr = JsCache.get('#templateTr').attr('id', '').clone().show();
				templateTr.find('#key').attr('id', '').remove();
				templateTr.find('#value').attr('id', '').text(data.gameInfoJson);
				JsCache.get('#originalData > tbody').append(templateTr);
			}
		}
		if (data.resultUrl) {
			var gameTxnResult = JSON.parse(data.resultUrl);

			if (gameTxnResult['type'] == 'url') {
				$.each(gameTxnResult['result'].split('#@'), function(i, el) {
					var templateLink = JsCache.get('#tempHyperlink').attr('id', '').clone().show();
					templateLink.removeAttr('id');
					templateLink.attr('href', el);
					templateLink.attr('onclick', "window.open(this.href, '', 'width=1200,height=640,top=5,left=5,toolbar=no,menubar=no,resizable=no,scrollbars=yes,status=no,location=no,directories=no'); return false; ");
					$gameForm.find('#vendorTxnID').append(templateLink);
				});
			} else if (gameTxnResult['type'] == 'html') {
				var templateLink = JsCache.get('#tempHyperlink').attr('id', '').clone().show();
				templateLink.removeAttr('id');
				templateLink.on('click', function(e) {
					var myWindow = window.open('', '', 'width=1200,height=640,top=5,left=5,toolbar=no,menubar=no,resizable=no,scrollbars=yes,status=no,location=no,directories=no');
					myWindow.document.write(gameTxnResult['result']);
					myWindow.onbeforeunload = function(e) {
						myWindow = null;
					};
				});
				$gameForm.find('#vendorTxnID').append(templateLink);
			}
		}
	}



	SubGameTransactionHandler.getOriginReport  = function(userId, gameType, recordId, vendorId){
		$.ajax({
			type: "GET",
			url: '/manager/member/getOriginBetReport',
			dataType: 'JSON',
			data: {
				userId,
				gameType,
				recordId,
				vendorId
			},
			success: function(data) {
				if (data.error) {
					NotifyHandler.errorMsg(data.error);
					return;
				}

				window.open(data.Url,"_blank", 'width=1200,height=640,top=5,left=5,toolbar=no,menubar=no,resizable=no,scrollbars=yes,status=no,location=no,directories=no');
			},
			beforeSend: function() {
				App.blockUI($("body"));
			},
			complete: function() {
				App.unblockUI($("body"));
			}
		});
	}

})();
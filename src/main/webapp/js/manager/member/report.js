if (typeof (ReportHandler) == 'undefined') {
	ReportHandler = {};
}

(function () {

	let withdrawalExcludeCol = [0];
	ReportHandler.init = function () {
		CurrencyUtil.updateSetting({
			"precision": 2,
			"trailingZeros": true,
			"roundingMode": CurrencyUtil.roundingMode.HALF_UP
		});

		// auto toggle tab
		if (PageConfig.enableViewLoginLog) {
			ReportHandler.toggleTab(tabs['Login']);
		} else {
			ReportHandler.toggleTab(tabs['UpdateLog']);
		}

		DateRangeHandler.init(PageConfig.lang);
		dateRangeInitial();
		closeBetDetailsEvent();

		if (!PageConfig.isAllowCustomerFee) {
			withdrawalExcludeCol.push(8);
		}
	};

	var tabs = {
		'Login': 1,
		'UpdateLog': 0,
		'Deposit': 3,
		'Withdrawal': 4,
		'Adjustment': 5,
		'Unsettled': 6,
		'Settled': 2
	};

	ReportHandler.toggleTab = function (tabIdx) {
		var tabbable = $('.tabbable');

		tabbable.find("[id^='tab']").removeClass('active');
		tabbable.find('#tab' + tabIdx).addClass('active');

		tabbable.find("[id^='box_tab']").removeClass('active');
		tabbable.find('#box_tab' + tabIdx).addClass('active');
		if (tabs['Login'] === tabIdx) {
			ReportHandler.searchProfileLoginLog();
		} else if (tabs['UpdateLog'] === tabIdx) {
			ReportHandler.searchProfileUpdateLog();
		} else if (tabs['Deposit'] === tabIdx) {
			ReportHandler.searchProfileDepositRecord();
		} else if (tabs['Withdrawal'] === tabIdx) {
			ReportHandler.searchProfileWithdrawalRecord();
		} else if (tabs['Adjustment'] === tabIdx) {
			ReportHandler.searchProfileAdjustmentRecord();
		} else if (tabs['Unsettled'] === tabIdx) {
			ReportHandler.searchProfileReportBetUnsettled();
		} else if (tabs['Settled'] === tabIdx) {
			ReportHandler.searchProfileReportBetSettled();
		} else {
			ReportHandler.searchProfileReportBetSettled();
		}

	};

	var dataTableOptions = [];
	ReportHandler.searchProfileReportBetUnsettled = function () {
		var tabId = tabs['Unsettled'];
		if (!dataTableOptions[tabId]) {
			dataTableOptions[tabId] = {
				tableSelector: '#searchProfileReportBetUnsettledTable',
				formSelector: "[name='searchProfileReportBetUnsettledForm']",//optional
				sAjaxSource: '/manager/member/searchProfileReportBetUnsettled',
				aaSorting: [[1, "asc"]],
				excludeColVis: [0],
				iDisplayLength: PageConfig.pageSize,
				aoColumns: [
					{
						"mData": null,
						"bSortable": false,
					},
					{
						"mData": "txnTimestamp",
						"mRender": function (data, type, full) {
							if (type === 'display') {
								return DateUtil.format(data, PageConfig.DateHourMinuteSecondPattern)
							}
							return data;
						}
					},
					{
						"mData": "createTimestamp",
						"mRender": function (data, type, full) {
							if (type === 'display') {
								return DateUtil.format(data, PageConfig.DateHourMinuteSecondPattern)
							}
							return data;
						}
					},
					{
						"mData": "vendorName"
					},
					{
						"mData": "gameType"
					},
					{
						"mData": "gameName"
					},
					{
						"mData": "odds",
						"mRender": function(data, type, full) {
							if (type === 'display') {
								if (data === null || data === undefined) {
									return "-";
								}
								return CurrencyUtil.formatter(data);
							}
							return data;
						}
					},
					{
						"mData": "oddsType",
						"mRender": function(data, type, full) {
							if (type === 'display') {
								return data || "-";
							}
							return data;
						}
					},
					{
						"mData": "betAmount",
						"mRender": function (data, type, full) {
							if (type === 'display') {
								return CurrencyUtil.formatter(data);
							}
							return data;
						}
					},
					// {
					// 	"mData": null,
					// 	"bSortable": false,
					// 	"mRender": function (data, type, full) {
					// 		if (type === 'display') {
					// 			return `<button type="button" data-type="${AccountSummaryReportType.BET.unique()}" data-date="${full.txnTimestamp}" onclick="ReportHandler.gotoDetail(this);"><i class="icon-search"></i></button>`;
					// 		}
					// 		return data;
					// 	},
					// },
					{
						"mData": "id",
						"bSortable": false,
						"mRender": function (data, type, full) {
							if (type === 'display') {
								return '<input type="hidden" id="gameTxnId" value="' + data + '">'
									+ '<input type="hidden" id="txnTime" value="' + full.txnTimestamp + '">'
									+ '<input type="hidden" id="settleTime" value="' + full.settleTimestamp + '">'
									+ '<input type="hidden" id="txnStatus" value="-1">'
									+ '<ul class="table-controls">'
									+ '<li><button class="btn btn-xs" onclick="ReportHandler.openGameTxnData(this)"><i class="icon-search"></i></button></li>'
									+ '</ul>'
							}
							return data;
						}
					}
				],
				aoColumnDefs: [{"sClass": "dt-body-right", "aTargets": [6, 8]}],
				"fnFooterCallback": function (nRow, aaData, iStart, iEnd, aiDisplay) {
					var iPageBetAmount = 0;
					var len = iEnd - iStart;
					for (var i = 0; i < len; i++) {
						var data = aaData[aiDisplay[i]];
						if (data) {
							iPageBetAmount += (parseFloat(data.betAmount) || 0);
						}
					}
					var footer = $(nRow);
					footer.find("[name='subTotal']").html('<div style="text-align: right;">' + CurrencyUtil.formatter(iPageBetAmount) + '</div>');
				}
			};
		}

		var callBack = function (response) {
			var totalAmount = response.iTotalAmount;
			var grandTotal = totalAmount && totalAmount.totalBetAmount ? totalAmount.totalBetAmount : 0;
			$('#searchProfileReportBetUnsettledTable').find("[name='grandTotal']").html('<div style="text-align: right;">' + CurrencyUtil.formatter(grandTotal) + '</div>');
		};

		var isEnableSequence = true;
		dataTableOptions[tabId].dataTableRef = DataTableHandler.create(dataTableOptions[tabId], isEnableSequence, PageConfig.lang, callBack);
		$('#profileReportBetUnsettledResultTable').show();
	};

	ReportHandler.searchProfileReportBetSettled = function () {
		var tabId = tabs['Settled'];
		if (!dataTableOptions[tabId]) {
			dataTableOptions[tabId] = {
				tableSelector: '#searchProfileReportBetSettledTable',
				formSelector: "[name='searchProfileReportBetSettledForm']",//optional
				sAjaxSource: '/manager/member/searchProfileReportBetSummary',
				aaSorting: [[1, "desc"]],
				excludeColVis: [0],
				iDisplayLength: PageConfig.pageSize,
				aoColumns: [
					{
						"mData": null,
						"bSortable": false,
					},
					{
						"mData": "transactionTime",
						"mRender": function (data, type, full) {
							if (type === 'display') {
								return DateUtil.format(data, PageConfig.DateFormatPattern)
							}
							return data;
						}
					},
					{
						"mData": "amount",
						"mRender": function (data, type, full) {
							if (type === 'display') {
								return CurrencyUtil.formatter(data);
							}
							return data;
						}
					},
					{
						"mData": "profit",
						"mRender": function (data, type, full) {
							if (type === 'display') {
								return CurrencyUtil.formatter(data);
							}
							return data;
						}
					},
					{
						"mData": "turnover",
						"mRender": function (data, type, full) {
							if (type === 'display') {
								return CurrencyUtil.formatter(data);
							}
							return data;
						}
					},
					{
						"mData": null,
						"bSortable": false,
						"mRender": function (data, type, full) {
							if (type === 'display') {
								return `<button type="button" data-type="${full.paymentType}" data-date="${full.transactionTime}" onclick="ReportHandler.gotoDetail(this);"><i class="icon-search"></i></button>`;
							}
							return data;
						},
					}
				],
				aoColumnDefs: [{"sClass": "dt-body-right", "aTargets": [2, 3, 4]}]
			};
		}

		var isEnableSequence = true;
		dataTableOptions[tabId].dataTableRef = DataTableHandler.create(dataTableOptions[tabId], isEnableSequence, PageConfig.lang);
		$('#profileReportBetSettledResultTable').show();
	};

	var dataTableTodayBetDetailsOptions;
	ReportHandler.searchProfileTodayBetDetails = function (element) {
		var vendorId = $(element).data('vendor');

		var searchTodayBetDetailsForm = $("[name='searchTodayBetDetailsForm']");
		searchTodayBetDetailsForm.find("[name='vendorId']").val(vendorId);
		searchTodayBetDetailsForm.find("[name='reinitPage']").val('true');
		if (!dataTableTodayBetDetailsOptions) {
			dataTableTodayBetDetailsOptions = {
				tableSelector: '#searchProfileTodayBetDetailsTable',
				formSelector: "[name='searchTodayBetDetailsForm']",//optional
				sAjaxSource: '/manager/member/searchProfileTodayBetDetails',
				aaSorting: [[0, "desc"]],
				iDisplayLength: PageConfig.pageSize,
				aoColumns: [
					{"mData": "txnTimeZone"},
					{"mData": "settleTimeZone"},
					{"mData": "createTimeZone"},
					{
						"mData": "vendorName",
						"bSortable": false,
					},
					{"mData": "gameType"},
					{
						"mData": "gameName",
						"mRender": function (data, type, full) {
							if (type === 'display') {
								if (PageConfig.accountCurrency == CurrencyType.CNY.value) {
									return data;
								} else {
									return full.gameNameEn;
								}
							}
							return data;
						}
					},
					{
						"mData": "systemTxnStatus",
						"mRender": function (data, type, full) {
							if (type === 'display') {
								var systemTxnStatusType = PageConfig.SystemTxnStatusType[data];
								if (systemTxnStatusType) {
									return '<span class="label ' + systemTxnStatusType.simpleCss + '">' + systemTxnStatusType.simpleName + '</span>';
								}
							}
							return data;
						}
					},
					{
						"mData": "betAmount",
						"mRender": function (data, type, full) {
							if (type === 'display') {
								return CurrencyUtil.formatter(data);
							}
							return data;
						}
					},
					{
						"mData": "profitLoss",
						"mRender": function (data, type, full) {
							if (type === 'display') {
								if (data == null) {
									return '-';
								}
								return CurrencyUtil.formatter(data);
							}
							return data;
						}
					},
					{
						"mData": "turnover",
						"mRender": function (data, type, full) {
							if (type === 'display') {
								if (data == null) {
									return '-';
								}
								return CurrencyUtil.formatter(data);
							}
							return data;
						}
					},
					{
						"mData": "betType",
						"bVisible": PageConfig.enableBetType
					},
					{
						"mData": "isBonusWallet",
						"mRender": function (data, type, full) {
							if (type === 'display') {
								return data === 0 ? I18N.get('form.text.no') : I18N.get('form.text.yes');
							}
							return data;
						},
						"bVisible": false
					},
					{
						"mData": "gameTxnId",
						"bSortable": false,
						"mRender": function (data, type, full) {
							if (type === 'display') {
								return '<input type="hidden" id="gameTxnId" value="' + data + '"><input type="hidden" id="txnTime" value="' + full.txnTime + '">'
									+ '<input type="hidden" id="txnStatus" value="' + full.systemTxnStatus + '">'
									+ '<ul class="table-controls">'
									+ '<li><button class="btn btn-xs" onclick="ReportHandler.openGameTxnData(this)"><i class="icon-search"></i></button></li>'
									+ '</ul>'
							}
							return data;
						}
					}
				],
				aoColumnDefs: [{"sClass": "dt-body-right", "aTargets": [7, 8, 9]}],
				"fnFooterCallback": function (nRow, aaData, iStart, iEnd, aiDisplay) {
					var iPageBetAmount = 0;
					var iPageProfitLoss = 0;
					var iPageTurnover = 0;

					var dashCaseProfitLoss = true;
					var dashCaseTurnover = true;

					var len = iEnd - iStart;
					for (var i = 0; i < len; i++) {
						var data = aaData[aiDisplay[i]];
						if (data) {
							iPageBetAmount += (parseFloat(data.betAmount) || 0);
							if (null != data.profitLoss) {
								dashCaseProfitLoss = false;
								iPageProfitLoss += (parseFloat(data.profitLoss) || 0);
							}
							if (null != data.turnover) {
								dashCaseTurnover = false;
								iPageTurnover += (parseFloat(data.turnover) || 0);
							}
						}
					}
					var footer = $(nRow);
					footer.find("[name='betAmount']").html('<div style="text-align: right;">' + CurrencyUtil.formatter(iPageBetAmount) + '</div>');
					if (dashCaseProfitLoss) {
						footer.find("[name='profitLoss']").html('-');
					} else {
						footer.find("[name='profitLoss']").html('<div style="text-align: right;">' + CurrencyUtil.formatter(iPageProfitLoss) + '</div>');
					}
					if (dashCaseTurnover) {
						footer.find("[name='turnover']").html('-');
					} else {
						footer.find("[name='turnover']").html('<div style="text-align: right;">' + CurrencyUtil.formatter(iPageTurnover) + '</div>');
					}
				}
			};
		}

		var callBack = function (response) {

			var totalAmount = response.iTotalAmount;

			var totalBetAmount = totalAmount.totalBetAmount ? totalAmount.totalBetAmount : 0;
			var totalProfitLoss = totalAmount.totalProfitLoss ? totalAmount.totalProfitLoss : 0;
			var totalTurnover = totalAmount.totalTurnover ? totalAmount.totalTurnover : 0;

			$('#searchProfileTodayBetDetailsTable').find("[name='totalBetAmount']").html('<div style="text-align: right;">' + CurrencyUtil.formatter(totalBetAmount) + '</div>');
			$('#searchProfileTodayBetDetailsTable').find("[name='totalProfitLoss']").html(
				totalProfitLoss == 0 ? '-' : ('<div style="text-align: right;">' + CurrencyUtil.formatter(totalProfitLoss) + '</div>'));
			$('#searchProfileTodayBetDetailsTable').find("[name='totalTurnover']").html(
				totalTurnover == 0 ? '-' : ('<div style="text-align: right;">' + CurrencyUtil.formatter(totalTurnover) + '</div>'));

			$('#todayBetDetailsModal').modal('show');
		};

		var isEnableSequence = false;
		dataTableTodayBetDetailsOptions.dataTableRef = DataTableHandler.create(dataTableTodayBetDetailsOptions, isEnableSequence, PageConfig.lang, callBack);
	};

	ReportHandler.searchProfileUpdateLog = function () {
		var tabId = tabs['UpdateLog'];
		if (dataTableOptions[tabId]?.dataTableRef) {
			const table = $(dataTableOptions[tabId].tableSelector).DataTable();
			table.fnDestroy();

		}
		dataTableOptions[tabId] = {
			tableSelector: '#searchProfileUpdateLogTable',
			formSelector: "[name='searchProfileUpdateLogForm']",//optional
			sAjaxSource: '/manager/member/searchProfileUpdateLog',
			aaSorting: [[5, "desc"]],
			excludeColVis: [0],
			iDisplayLength: PageConfig.pageSize,
			aoColumns: [
				{
					"mData": null,
					"bSortable": false,
				},
				{
					"mData": "logType",
					"mRender": function (data, type, full) {
						if (type === 'display') {
							var accountUpdateType = PageConfig.AccountUpdateType[data];
							return I18N.get(accountUpdateType.getFullName);
						}
						return data;
					}
				},
				{
					"mData": "beforeUpdate",
					"bSortable": false,
					"mRender": function (data, type, full) {
						if (type === 'display') {
							var displayName = getDisplayName(full.logType, data);
							return displayName == 'null' ? '-' : `<div class="text-wrap">${displayName}</div>`
						}
						return data;
					}
				},
				{
					"mData": "afterUpdate",
					"bSortable": false,
					"mRender": function (data, type, full) {
						if (type === 'display') {
							// var displayName = getDisplayName(full.logType, data);
							return `<div class="text-wrap">${getDisplayName(full.logType, data)}</div>`;
						}
						return data;
					}
				},
				{"mData": "updater"},
				{
					"mData": "updateTime",
					"sWidth": "180px"

				},
				{
					"mData": "updaterIp",
					"sWidth": "100px"
				}
			]
		};


		var isEnableSequence = true;
		dataTableOptions[tabId].dataTableRef = DataTableHandler.create(dataTableOptions[tabId], isEnableSequence, PageConfig.lang);
		$(dataTableOptions[tabId].tableSelector).on('order.dt', function () {
			const order = dataTableOptions[tabId].dataTableRef.order();
			dataTableOptions[tabId].aaSorting = order;
		});

		$('#profileUpdateLogTable').show();
	}

	function getDisplayName(logType, data) {
		var displayName = '-';
		switch (logType) {
			case 7: // status
				if (data) {
					var accountStatusType = PageConfig.AccountStatusType[data];
					displayName = '<span class="label ' + accountStatusType.css + '">' + I18N.get(accountStatusType.fullName) + '</span>';
				}
				break;
			case 2:
			case 3:
			case 4:
			case 31:
			case 50:
				if (data == 1) {
					displayName = I18N.get('form.text.backOffice.report.allowed');
				} else if (data == 0) {
					displayName = I18N.get('form.text.backOffice.report.notAllowed');
				}
				break;
			case 46:
				if (data == 1) {
					displayName = I18N.get('form.text.on');
				} else if (data == 0) {
					displayName = I18N.get('form.text.off');
				}
				break;
			case 54:
				if (data) {
					const css = data.toLowerCase() === "unverified" ? "label-default" : "label-success";
					displayName = '<span class="label ' + css + '">' + data + '</span>';
				}
				break;
			case 55:
			case 56:
			case 57:
				if (data !== '-' && data !== "") {
					displayName = `<button type="button" onclick="ReportHandler.viewKycImage('${data}')"><i class="icon-search"></i></button>`;
				}
				break;
			case 58:
				var kycDocumentStatuses = PageConfig.DocumentStatusType[data];
				if (kycDocumentStatuses) {
					displayName = `<span class="label ${kycDocumentStatuses.css}">${I18N.get(kycDocumentStatuses.columnName)}</span>`;
				}
				break;
			case 61:
				if (data !== '-') {
					displayName = DateUtil.format(DateUtil.convert(data), PageConfig.DateFormatPattern);
				}
				break;
			case 69:
				data = data === '' ? '{}' : data;
				var accountCard = JSON.parse(data);
				if (accountCard) {
					displayName = I18N.get('form.text.account.card.bank') + ': ' + (accountCard['bankName'] ? accountCard['bankName'] : '-')
						+ '<br>' + I18N.get('form.text.account.card.cardBrand') + ': ' + (accountCard['cardSchemeType'] ? accountCard['cardSchemeType'] : '-')
						+ '<br>' + I18N.get('form.text.account.card.cardNumber') + ': ' + (accountCard['cardNo'] ? formatCardNumber(accountCard['cardNo']) : '-')
						+ '<br>' + I18N.get('form.text.account.card.expiryDate') + ': ' + (accountCard['expMonthYear'] ? accountCard['expMonthYear'] : '-')
						+ '<br>' + I18N.get('form.text.account.card.cardholderName') + ': ' + (accountCard['cardholderName'] ? accountCard['cardholderName'] : '-');
				}
				break;
			case 78:
				data = data === '' ? '{}' : data;
				var bankAccount = JSON.parse(data);
				if (bankAccount) {
					displayName = I18N.get('form.text.bank.bankName') + ': ' + (bankAccount['bankName'] ? bankAccount['bankName'] : '-')
						+ '<br>' + I18N.get('form.text.bank.accountNumber') + ': ' + (bankAccount['bankAccNumber'] ? formatAccountNumber(bankAccount['bankAccNumber']) : '-')
				}
				break;
			default:
				if (data) {
					displayName = data;
				}
				break;
		}
		return displayName;
	}

	ReportHandler.searchProfileLoginLog = function () {
		// let count = 0;
		let tabId = tabs['Login'];
		if (dataTableOptions[tabId]?.dataTableRef) {
			const table = $(dataTableOptions[tabId].tableSelector).DataTable();
			table.fnDestroy();
		}
		dataTableOptions[tabId] = {
			tableSelector: '#searchProfileIPTable',
			formSelector: "[name='searchProfileIPForm']",//optional
			sAjaxSource: '/manager/member/searchProfileLoginLog',
			aaSorting: [[2, "desc"]],
			excludeColVis: [0],
			iDisplayLength: PageConfig.pageSize,
			aoColumns: [
				{
					"mData": null,
					"bSortable": false,
				},
				{"mData": "ip"},
				{"mData": "loginDate"},
				{"mData": "country"},
				{"mData": "deviceType"},
				{"mData": "userAgentType"},
			]
		};

		const isEnableSequence = true;
		dataTableOptions[tabId].dataTableRef = DataTableHandler.create(dataTableOptions[tabId], isEnableSequence, PageConfig.lang);
		$('#profileIPResultTable').show();
	};

	ReportHandler.searchProfileBonus = function () {
		var tabId = tabs['Bonus'];
		if (!dataTableOptions[tabId]) {
			dataTableOptions[tabId] = {
				tableSelector: '#searchProfileBonusTable',
				formSelector: "[name='searchProfileBonusForm']",//optional
				sAjaxSource: '/manager/member/searchProfileBonus',
				aaSorting: [[10, "desc"]],
				excludeColVis: [0, 15, 16],
				iDisplayLength: PageConfig.pageSize,
				aoColumns: [
					{
						"mData": null,
						"bSortable": false,
					},
					{
						"mData": "bonusCode",
						"bSortable": false,
					},
					{"mData": "bonusTitle"},
					{
						"mData": "bonus",
						"mRender": function (data, type, full) {
							if (type === 'display') {
								return CurrencyUtil.formatter(data);
							}
							return data;
						}
					},
					{
						"mData": "recycleAmount",
						"mRender": function (data, type, full) {
							if (type === 'display') {
								var html = CurrencyUtil.formatter(data);
								if (full.bonusWallet === true && AccountBonusTurnoverStatusType.INACTIVE.value !== full.status) {
									html += ` <span class="glyphicon glyphicon-search" onclick="ReportHandler.gotoBonusWallet(${full.id}, ${data});"></span>`;
								}
								return html;
							}
							return data;
						},
						"bVisible": false
					},
					{
						"mData": "cancelFee",
						"mRender": function (data, type, full) {
							if (type === 'display') {
								return CurrencyUtil.formatter(data);
							}
							return data;
						},
						"bVisible": false
					},
					{
						"mData": "completedTurnover",
						"mRender": function (data, type, full) {
							if (type === 'display') {
								return CurrencyUtil.formatter(data);
							}
							return data;
						}
					},
					{
						"mData": "requirementTurnover",
						"mRender": function (data, type, full) {
							if (type === 'display') {
								return CurrencyUtil.formatter(data);
							}
							return data;
						}
					},
					{
						"mData": "percentage",
						"mRender": function (data, type, full) {
							if (type === 'display') {
								return CurrencyUtil.formatter(data * 100);
							}
							return data;
						}
					},
					{
						"mData": "multiplier",
						"mRender": function (data, type, full) {
							if (type === 'display') {
								return CurrencyUtil.formatter(data);
							}
							return data;
						}
					},
					{
						"mData": "createTime"
					},
					{
						"mData": "status",
						"mRender": function (data, type, full) {
							if (type === 'display') {
								var accountBonusTurnoverStatusType = PageConfig.AccountBonusTurnoverStatusType[data];
								if (accountBonusTurnoverStatusType) {
									return '<span class="label ' + accountBonusTurnoverStatusType.css + '">' + I18N.get(accountBonusTurnoverStatusType.fullName) + '</span>';
								}
								return data;
							}
							return data;
						}
					},
					{
						"mData": "activatedTime", "bVisible": false
					},
					{"mData": "forceCloser"},
					{
						"mData": "closedTime"
					},
					{
						"mData": "forceCloseNote",
						"fnCreatedCell": function (nTd, sData, oData, iRow, iCol) {
							$(nTd).css('word-break', 'break-all')
						}
					},
					{
						"mData": "updater",
						"bSortable": false,
					},
					{
						"mData": null,
						"bSortable": false,
						"mRender": function (data, type, full) {
							if (type === 'display' && PageConfig.enableDoForceServe) {
								const allowAction = (AccountBonusTurnoverStatusType.ACTIVE.unique() === full.status)
									|| (AccountBonusTurnoverStatusType.SUSPEND.unique() === full.status && PageConfig.bonusUpdater === full.updater);
								const allowTransfer = (AccountBonusTurnoverStatusType.COMPLETE.unique() === full.status)
									&& (full.bonusWallet === true);
								if (allowAction) {
									return `<a style="text-decoration: underline;" data-id="${full.id}" data-iswallet="${full.bonusWallet}" onclick="ReportHandler.gotoForceServe(this);">${I18N.get("form.text.backOffice.action")}</a>`;
								}
								if (allowTransfer) {
									return `<a style="text-decoration: underline;" data-id="${full.id}" onclick="ReportHandler.gotoReturnToMain(this);">${I18N.get("form.text.backOffice.status.returnToMain")}</a>`;
								}
							}
							return data;
						}
					},
					{
						"mData": null,
						"bSortable": false,
						"mRender": function (data, type, full) {
							if (type === 'display') {
								return `<a style="text-decoration: underline;" data-turnoverid="${full.id}" data-bonuswallet="${full.bonusWallet}" onclick="ReportHandler.gotoBonusWalletBetDetails(this);">${I18N.get('form.text.report.viewIcon')}</a>`;
							}
							return data;
						}
					},
				],
				aoColumnDefs: [{"sClass": "dt-body-right", "aTargets": [3, 4, 5, 6, 7]}],
				"fnFooterCallback": function (nRow, aaData, iStart, iEnd, aiDisplay) {
					var iPageTotalBonus = 0;
					var iPageTotalRecycleAmount = 0;
					var iPageTotalCancelFee = 0;
					for (var i = 0; i < (iEnd - iStart); i++) {
						var data = aaData[aiDisplay[i]];
						if (data) {
							iPageTotalBonus += (parseFloat(data.bonus) || 0);
							iPageTotalRecycleAmount += (parseFloat(data.recycleAmount) || 0);
							iPageTotalCancelFee += (parseFloat(data.cancelFee) || 0);
						}
					}
					var footer = $(nRow);
					footer.find("[name='totalBonus']").html('<div style="text-align: right;">' + CurrencyUtil.formatter(iPageTotalBonus) + '</div>');
					footer.find("[name='totalRecycleAmount']").html('<div style="text-align: right;">' + CurrencyUtil.formatter(iPageTotalRecycleAmount) + '</div>');
					footer.find("[name='totalCancelFee']").html('<div style="text-align: right;">' + CurrencyUtil.formatter(iPageTotalCancelFee) + '</div>');
				},
				"fnRowCallback": function (nRow, aData, iDisplayIndex, iDisplayIndexFull) {
					var numStart = this.fnPagingInfo().iStart;
					var index = numStart + iDisplayIndexFull + 1;
					$('td:eq(0)', nRow).html(index);
					if (PageConfig.bonusTurnoverId > 0 && aData.id === parseInt(PageConfig.bonusTurnoverId)) {
						$('td', nRow).css('backgroundColor', '#a2c2ef');
					}
				}
			};
		}

		var callback = function (response) {

			var totalAmount = response.iTotalAmount;

			var totalBonus = totalAmount.totalBonus ? totalAmount.totalBonus : 0;
			var totalCancelFee = totalAmount.totalCancelFee ? totalAmount.totalCancelFee : 0;
			var totalRecycleAmount = totalAmount.totalRecycleBalance ? totalAmount.totalRecycleBalance - totalAmount.totalCancelFee : 0;

			$('#searchProfileBonusTable').find("[name='totalTotalBonus']").html('<div style="text-align: right;">' + CurrencyUtil.formatter(totalBonus) + '</div>');
			$('#searchProfileBonusTable').find("[name='totalTotalRecycleAmount']").html('<div style="text-align: right;">' + CurrencyUtil.formatter(totalRecycleAmount) + '</div>');
			$('#searchProfileBonusTable').find("[name='totalTotalCancelFee']").html('<div style="text-align: right;">' + CurrencyUtil.formatter(totalCancelFee) + '</div>');
		}

		var isEnableSequence = true;
		dataTableOptions[tabId].dataTableRef = DataTableHandler.create(dataTableOptions[tabId], isEnableSequence, PageConfig.lang, callback);
		$('#profileBonusResultTable').show();
	};

	ReportHandler.gotoForceServe = function (element) {
		const forceServeForm = $("[name='forceServeForm']");
		forceServeForm.get(0).reset();
		forceServeForm.validate();

		const accountBonusTurnoverId = $(element).data('id');
		const isBonusWallet = $(element).data('iswallet');
		const forceServeModal = $('#forceServeModal');
		forceServeForm.find("[name='accountBonusTurnoverId']").val(accountBonusTurnoverId);
		forceServeForm.find("[name='bonusWallet']").val(isBonusWallet);

		const forceServeStoredRemark = forceServeForm.find('#forceServeStoredRemark');
		var selector = forceServeStoredRemark[0];
		while (selector.options.length > 0) {
			selector.remove(0);
		}
		var frag = document.createDocumentFragment();
		var option = document.createElement("option");
		option.value = -1;
		option.text = '-- ' + I18N.get('ui.text.deposit.select_remark') + ' --';
		frag.appendChild(option);

		selector.appendChild(frag);
		forceServeStoredRemark.val(-1);

		if (isBonusWallet === true) {
			$.ajax({
				type: "POST",
				url: '/manager/member/getBonusWalletForceServeInfo',
				dataType: 'JSON',
				data: {
					bonusTurnoverId: accountBonusTurnoverId
				},
				success: function (data) {
					if (data.error) {
						NotifyHandler.errorMsg(data.error);
						return;
					}
					forceServeModal.find("[name=bonusComplete]").removeAttr('onclick');
					forceServeModal.find("[name=bonusComplete]").attr('onclick', 'ReportHandler.forceServe(this)');
					forceServeModal.find("[name=bonusWalletComplete]").show();

					forceServeModal.find('[id^=walletDetail_]').hide();

					const walletInfo = data.walletInfo;
					if (walletInfo !== null) {
						forceServeModal.find('[id^=walletDetail_]').show();
						forceServeForm.find("[name=deposit]").html(CurrencyUtil.formatter(walletInfo.initDeposit));
						forceServeForm.find("[name=bonus]").html(CurrencyUtil.formatter(walletInfo.bonus));
						forceServeForm.find("[name=profit]").html(CurrencyUtil.formatter(walletInfo.profit));
						forceServeForm.find("[name=cancelFee]").html(CurrencyUtil.formatter(walletInfo.cancelFee));
						forceServeForm.find("[name=refund]").html(CurrencyUtil.formatter(walletInfo.realAmount));
					}
					forceServeModal.modal('show');
				},
				beforeSend: function () {
					App.blockUI($("body"));
				},
				complete: function () {
					App.unblockUI($("body"));
				}
			});
		} else {

			if (forceServeModal.find('[id^=walletDetail_]').length > 0) {
				forceServeModal.find('[id^=walletDetail_]').hide();
			}

			forceServeModal.find("[name=bonusComplete]").removeAttr('onclick');
			forceServeModal.find("[name=bonusComplete]").attr('onclick', 'ReportHandler.completeBonus(this)');
			forceServeModal.find("[name=bonusWalletComplete]").hide();
			forceServeModal.modal('show');
		}
	};

	let dataTableBonusWalletBetDetailsOptions;
	ReportHandler.gotoBonusWalletBetDetails = function (element) {
		const accountBonusTurnoverId = $(element).data('turnoverid');
		const isBonusWallet = $(element).data('bonuswallet');
		const searchBonusBetDetailForm = $("[name='searchBonusBetDetailForm']");
		searchBonusBetDetailForm.find('[name=turnoverId]').val(accountBonusTurnoverId);

		let url;
		if (isBonusWallet) {
			url = '/manager/member/searchBetDetailsByBonusWallet';
		} else {
			url = '/manager/member/searchBonusMappingGameTxnDetails';
		}

		if (!dataTableBonusWalletBetDetailsOptions) {
			dataTableBonusWalletBetDetailsOptions = {
				tableSelector: '#bonusBetDetailsTable',
				formSelector: "[name='searchBonusBetDetailForm']",//optional
				sAjaxSource: url,
				aaSorting: [[0, "desc"]],
				hideColVis: true,
				iDisplayLength: PageConfig.pageSize,
				aoColumns: [
					{"mData": "txnTimeZone"},
					{"mData": "settleTimeZone"},
					{"mData": "createTimeZone"},
					{"mData": "vendorName"},
					{"mData": "gameType"},
					{
						"mData": "gameName",
						"mRender": function (data, type, full) {
							if (type === 'display') {
								if (PageConfig.accountCurrency == CurrencyType.CNY.value) {
									return data;
								} else {
									return full.gameNameEn;
								}
							}
							return data;
						}
					},
					{
						"mData": "betAmount",
						"mRender": function (data, type, full) {
							if (type === 'display') {
								return CurrencyUtil.formatter(data);
							}
							return data;
						}
					},
					{
						"mData": "profitLoss",
						"mRender": function (data, type, full) {
							if (type === 'display') {
								return CurrencyUtil.formatter(data);
							}
							return data;
						}
					},
					{
						"mData": "turnover",
						// 顯示非獎金錢包有哪些gameTxn打時,為減少DB query負擔,
						// 不 join table: gameTxn和BonusTurnoverMapping,打的金額不在query內
						"bSortable": isBonusWallet,
						"mRender": function (data, type, full) {
							if (type === 'display') {
								return CurrencyUtil.formatter(data);
							}
							return data;
						}
					},
					{
						"mData": "id",
						"bSortable": false,
						"mRender": function (data, type, full) {
							if (type === 'display') {
								return '<input type="hidden" id="gameTxnId" value="' + data + '">'
									+ '<input type="hidden" id="txnTime" value="' + full.txnTime + '">'
									+ '<input type="hidden" id="settleTime" value="' + full.settleTime + '">'
									+ '<input type="hidden" id="txnStatus" value="-1">'
									+ '<ul class="table-controls">'
									+ '<li><button class="btn btn-xs" onclick="ReportHandler.openGameTxnData(this)"><i class="icon-search"></i></button></li>'
									+ '</ul>'
							}
							return data;
						}
					}
				],
				aoColumnDefs: [{"sClass": "dt-body-right", "aTargets": [6, 7, 8]}],
				"fnFooterCallback": function (nRow, aaData, iStart, iEnd, aiDisplay) {
					var iPageBetAmount = 0;
					var iPageProfitLoss = 0;
					var iPageTurnover = 0;
					var len = iEnd - iStart;
					for (var i = 0; i < len; i++) {
//		            for (var i=iStart ; i<iEnd; i++) {
						var data = aaData[aiDisplay[i]];
						if (data) {
							iPageBetAmount += (parseFloat(data.amount) || 0);
							iPageProfitLoss += (parseFloat(data.profitLoss) || 0);
							iPageTurnover += (parseFloat(data.turnover) || 0);
						}
					}
					var footer = $(nRow);
					footer.find("[name='betAmount']").html('<div style="text-align: right;">' + CurrencyUtil.formatter(iPageBetAmount) + '</div>');
					footer.find("[name='profitLoss']").html('<div style="text-align: right;">' + CurrencyUtil.formatter(iPageProfitLoss) + '</div>');
					footer.find("[name='turnover']").html('<div style="text-align: right;">' + CurrencyUtil.formatter(iPageTurnover) + '</div>');
				}
			};
		} else {
			$('#bonusBetDetailsTable').DataTable().fnSettings().sAjaxSource = url;
		}

		const callBack = function (response) {
			const totalAmount = response.iTotalAmount;

			const totalBetAmount = totalAmount.betAmount ? totalAmount.betAmount : 0;
			const totalProfitLoss = totalAmount.profitLoss ? totalAmount.profitLoss : 0;
			const totalTurnover = totalAmount.turnover ? totalAmount.turnover : 0;

			$('#bonusBetDetailsTable').find("[name='totalBetAmount']").html('<div style="text-align: right;">' + CurrencyUtil.formatter(totalBetAmount) + '</div>');
			$('#bonusBetDetailsTable').find("[name='totalProfitLoss']").html('<div style="text-align: right;">' + CurrencyUtil.formatter(totalProfitLoss) + '</div>');
			$('#bonusBetDetailsTable').find("[name='totalTurnover']").html('<div style="text-align: right;">' + CurrencyUtil.formatter(totalTurnover) + '</div>');

			$('#bonusBetDetailsModal').modal('show');
		};

		const isEnableSequence = false;
		dataTableBonusWalletBetDetailsOptions.dataTableRef = DataTableHandler.create(dataTableBonusWalletBetDetailsOptions, isEnableSequence, PageConfig.lang, callBack);
	};

	var dataTableBetDetailsOptions;
	var gotoBetSummaryDetails = function (paymentType, transactionDate) {
		var searchDetailGlobalForm = $("[name='searchDetailGlobalForm']");
		searchDetailGlobalForm.find("[name='transactionDate']").val(transactionDate);

		if (!dataTableBetDetailsOptions) {
			dataTableBetDetailsOptions = {
				tableSelector: '#betSummaryDetailsTable',
				formSelector: "[name='searchDetailGlobalForm']",//optional
				sAjaxSource: '/manager/member/searchBetSummaryDetailsFromTransaction',
				aaSorting: [[0, "desc"]],
				hideColVis: true,
				iDisplayLength: PageConfig.pageSize,
				aoColumns: [
					{
						"mData": "summaryDate",
						"mRender": function (data, type, full) {
							if (type === 'display') {
								return DateUtil.format(data, PageConfig.DateFormatPattern)
							}
							return data;
						}
					},
					{
						"mData": "vendorName"
					},
					{
						"mData": "gameTypeName"
					},
					{
						"mData": "amount",
						"mRender": function (data, type, full) {
							if (type === 'display') {
								return CurrencyUtil.formatter(data);
							}
							return data;
						}
					},
					{
						"mData": "profit",
						"mRender": function (data, type, full) {
							if (type === 'display') {
								return CurrencyUtil.formatter(data);
							}
							return data;
						}
					},
					{
						"mData": "turnover",
						"mRender": function (data, type, full) {
							if (type === 'display') {
								return CurrencyUtil.formatter(data);
							}
							return data;
						}
					},
					{
						"mData": null,
						"bSortable": false,
						"mRender": function (data, type, full) {
							if (type === 'display') {
								return `<button type="button" data-type="${paymentType}" data-vendor="${full.vendorId}" data-game-type="${full.gameType ? full.gameType : ''}" data-date="${full.summaryDate}" onclick="ReportHandler.gotoBetDetails(this);"><i class="icon-search"></i></button>`;
							}
							return data;
						},
					}
				],
				aoColumnDefs: [{"sClass": "dt-body-right", "aTargets": [3, 4, 5]}],
				"fnFooterCallback": function (nRow, aaData, iStart, iEnd, aiDisplay) {
					var iPageAmount = 0;
					var iPageProfit = 0;
					var iPageTurnover = 0;
					var len = iEnd - iStart;
					for (var i = 0; i < len; i++) {
//		            for (var i=iStart ; i<iEnd; i++) {
						var data = aaData[aiDisplay[i]];
						if (data) {
							iPageAmount += (parseFloat(data.amount) || 0);
							iPageProfit += (parseFloat(data.profit) || 0);
							iPageTurnover += (parseFloat(data.turnover) || 0);
						}
					}
					$(nRow).find("[name='amount']").html('<div style="text-align: right;">' + CurrencyUtil.formatter(iPageAmount) + '</div>');
					$(nRow).find("[name='profit']").html('<div style="text-align: right;">' + CurrencyUtil.formatter(iPageProfit) + '</div>');
					$(nRow).find("[name='turnover']").html('<div style="text-align: right;">' + CurrencyUtil.formatter(iPageTurnover) + '</div>');
				}
			};
		}

		var callBack = function (response) {

			var totalAmount = response.iTotalAmount;

			var totalBetAmount = totalAmount.totalBetAmount ? totalAmount.totalBetAmount : 0;
			var totalProfitLoss = totalAmount.totalProfitLoss ? totalAmount.totalProfitLoss : 0;
			var totalTurnover = totalAmount.totalTurnover ? totalAmount.totalTurnover : 0;

			$('#betSummaryDetailsTable').find("[name='totalAmount']").html('<div style="text-align: right;">' + CurrencyUtil.formatter(totalBetAmount) + '</div>');
			$('#betSummaryDetailsTable').find("[name='totalProfit']").html('<div style="text-align: right;">' + CurrencyUtil.formatter(totalProfitLoss) + '</div>');
			$('#betSummaryDetailsTable').find("[name='totalTurnover']").html('<div style="text-align: right;">' + CurrencyUtil.formatter(totalTurnover) + '</div>');

			$('#betSummaryDetailsModal').modal('show');
		};

		var isEnableSequence = false;
		dataTableBetDetailsOptions.dataTableRef = DataTableHandler.create(dataTableBetDetailsOptions, isEnableSequence, PageConfig.lang, callBack);
	};

	var dataTableDetailsOptions = [];
	ReportHandler.gotoBetDetails = function (element) {

		var paymentType = $(element).data('type');
		var transactionDate = $(element).data('date');
		var vendorId = $(element).data('vendor');
		var gameType = $(element).data('game-type');

		var searchBetDetailForm = $("[name='searchBetDetailForm']");
		searchBetDetailForm.find("[name='transactionDate']").val(transactionDate);
		searchBetDetailForm.find("[name='vendorId']").val(vendorId);
		searchBetDetailForm.find("[name='gameType']").val(gameType);
		searchBetDetailForm.find("[name='reinitPage']").val('true');
		if (!dataTableDetailsOptions[paymentType]) {
			dataTableDetailsOptions[paymentType] = {
				tableSelector: '#betDetailsTable',
				formSelector: "[name='searchBetDetailForm']",//optional
				sAjaxSource: '/manager/member/searchBetDetails',
				aaSorting: [[0, "desc"]],
				hideColVis: true,
				iDisplayLength: PageConfig.pageSize,
				aoColumns: [
					{
						"mData": "txnTimestamp",
						"mRender": function (data, type, full) {
							if (type === 'display') {
								return DateUtil.format(data, PageConfig.DateHourMinuteSecondPattern)
							}
							return data;
						}
					},
					{
						"mData": "settleTimestamp",
						"mRender": function (data, type, full) {
							if (type === 'display') {
								return DateUtil.format(data, PageConfig.DateHourMinuteSecondPattern)
							}
							return data;
						}
					},
					{
						"mData": "createTimestamp",
						"mRender": function (data, type, full) {
							if (type === 'display') {
								return DateUtil.format(data, PageConfig.DateHourMinuteSecondPattern)
							}
							return data;
						}
					},
					{
						"mData": "vendorName"
					},
					{
						"mData": "gameType"
					},
					{
						"mData": "gameNameEn"
					},
					{
						"mData": "odds",
						"mRender": function(data, type, full) {
							if (type === 'display') {
								if (data === null || data === undefined) {
									return "-";
								}
								return CurrencyUtil.formatter(data);
							}
							return data;
						}
					},
					{
						"mData": "oddsType",
						"mRender": function(data, type, full) {
							if (type === 'display') {
								return data || "-";
							}
							return data;
						}
					},
					{
						"mData": "balanceBefore",
						"mRender": function (data, type, full) {
							if (type === 'display') {
								return CurrencyUtil.formatter(data);
							}
							return data;
						}
					},
					{
						"mData": "betAmount",
						"mRender": function (data, type, full) {
							if (type === 'display') {
								return CurrencyUtil.formatter(data);
							}
							return data;
						}
					},
					{
						"mData": "profitLoss",
						"mRender": function (data, type, full) {
							if (type === 'display') {
								if (parseInt(full.txnStatus) !== PageConfig.SystemTxnStatusType.SETTLED) {
									return "-";
								} else {
									return CurrencyUtil.formatter(data);
								}
							}
							return data;
						}
					},
					{
						"mData": "balanceAfter",
						"mRender": function (data, type, full) {
							if (type === 'display') {
								return CurrencyUtil.formatter(data);
							}
							return data;
						}
					},
					{
						"mData": "turnover",
						"mRender": function (data, type, full) {
							if (type === 'display') {
								if (parseInt(full.txnStatus) !== PageConfig.SystemTxnStatusType.SETTLED) {
									return "-";
								} else {
									return CurrencyUtil.formatter(data);
								}
							}
							return data;
						}
					},
					// {
					// 	"mData": "betType",
					// 	bVisible: PageConfig.enableBetType
					// },
					// {
					// 	"mData": "isBonusWallet",
					// 	"mRender": function(data, type, full) {
					// 		if (type === 'display') {
					// 			return data === 0 ? I18N.get('form.text.no') : I18N.get('form.text.yes');
					// 		}
					// 		return data;
					// 	},
					// 	"bVisible": false
					// },
					{
						"mData": "id",
						"bSortable": false,
						"mRender": function (data, type, full) {
							if (type === 'display') {
								return '<input type="hidden" id="gameTxnId" value="' + data + '">'
									+ '<input type="hidden" id="txnTime" value="' + full.txnTimestamp + '">'
									+ '<input type="hidden" id="settleTime" value="' + full.settleTimestamp + '">'
									+ '<input type="hidden" id="txnStatus" value="-1">'
									+ '<ul class="table-controls">'
									+ '<li><button class="btn btn-xs" onclick="ReportHandler.openGameTxnData(this)"><i class="icon-search"></i></button></li>'
									+ '</ul>'
							}
							return data;
						}
					}
				],
				aoColumnDefs: [{"sClass": "dt-body-right", "aTargets": [6, 8, 9, 10, 11, 12]}],
				"fnFooterCallback": function (nRow, aaData, iStart, iEnd, aiDisplay) {
					var iPageBetAmount = 0;
					var iPageProfitLoss = 0;
					var iPageTurnover = 0;
					var len = iEnd - iStart;
					for (var i = 0; i < len; i++) {
//		            for (var i=iStart ; i<iEnd; i++) {
						var data = aaData[aiDisplay[i]];
						if (data) {
							iPageBetAmount += (parseFloat(data.betAmount) || 0);
							iPageProfitLoss += parseInt(data.txnStatus) !== PageConfig.SystemTxnStatusType.SETTLED ? 0 : (parseFloat(data.profitLoss) || 0);
							iPageTurnover += parseInt(data.txnStatus) !== PageConfig.SystemTxnStatusType.SETTLED ? 0 : (parseFloat(data.turnover) || 0);
						}
					}
					var footer = $(nRow);
					footer.find("[name='betAmount']").html('<div style="text-align: right;">' + CurrencyUtil.formatter(iPageBetAmount) + '</div>');
					footer.find("[name='profitLoss']").html('<div style="text-align: right;">' + CurrencyUtil.formatter(iPageProfitLoss) + '</div>');
					footer.find("[name='turnover']").html('<div style="text-align: right;">' + CurrencyUtil.formatter(iPageTurnover) + '</div>');
				}
			};
		}
		var callBack = function (response) {
			var totalAmount = response.iTotalAmount;

			var totalBetAmount = totalAmount.totalBetAmount ? totalAmount.totalBetAmount : 0;
			var totalProfitLoss = totalAmount.totalProfitLoss ? totalAmount.totalProfitLoss : 0;
			var totalTurnover = totalAmount.totalTurnover ? totalAmount.totalTurnover : 0;

			$('#betDetailsTable').find("[name='totalBetAmount']").html('<div style="text-align: right;">' + CurrencyUtil.formatter(totalBetAmount) + '</div>');
			$('#betDetailsTable').find("[name='totalProfitLoss']").html('<div style="text-align: right;">' + CurrencyUtil.formatter(totalProfitLoss) + '</div>');
			$('#betDetailsTable').find("[name='totalTurnover']").html('<div style="text-align: right;">' + CurrencyUtil.formatter(totalTurnover) + '</div>');

			$('#betDetailsModal').modal('show');
		};

		var isEnableSequence = false;
		dataTableDetailsOptions[paymentType].dataTableRef = DataTableHandler.create(dataTableDetailsOptions[paymentType], isEnableSequence, PageConfig.lang, callBack);

		// set balanceBefore and balanceAfter columns visibility according to gameType
		let toHide = parseInt(gameType) !== GameType.Sport.value;
		var dtRef = dataTableDetailsOptions[paymentType].dataTableRef;
		if (dtRef) {
			dtRef.fnSetColumnVis(8, toHide); // balanceBefore
			dtRef.fnSetColumnVis(11, toHide); // balanceAfter
			dtRef.fnSetColumnVis(6, !toHide); // odds
			dtRef.fnSetColumnVis(7, !toHide); // oddsType
		}
	};

	var gotoDepositDetails = function (paymentType, transactionDate) {
		var searchDetailGlobalForm = $("[name='searchDetailGlobalForm']");
		searchDetailGlobalForm.find("[name='transactionDate']").val(transactionDate);

		if (!dataTableDetailsOptions[paymentType]) {
			dataTableDetailsOptions[paymentType] = {
				tableSelector: '#depositDetailsTable',
				formSelector: "[name='searchDetailGlobalForm']",//optional
				sAjaxSource: '/manager/member/searchDepositDetails',
				aaSorting: [[0, "desc"]],
				hideColVis: true,
				iDisplayLength: PageConfig.pageSize,
				aoColumns: [
					{"mData": "createTime"},
					{"mData": "approvedTime"},
					{"mData": "approvedUserid"},
					{"mData": "id"},
					{"mData": "fromBankName"},
					{"mData": "toBankName"},
					{
						"mData": "amount",
						"mRender": function (data, type, full) {
							if (type === 'display') {
								return CurrencyUtil.formatter(data);
							}
							return data;
						}
					},
					{
						"mData": "bonus",
						"mRender": function (data, type, full) {
							if (type === 'display') {
								return CurrencyUtil.formatter(data);
							}
							return data;
						}
					},
					{
						"mData": "totalAmount",
						"mRender": function (data, type, full) {
							if (type === 'display') {
								return CurrencyUtil.formatter(data);
							}
							return data;
						}
					}
				],
				aoColumnDefs: [{"sClass": "dt-body-right", "aTargets": [6, 7, 8]}],
				"fnFooterCallback": function (nRow, aaData, iStart, iEnd, aiDisplay) {
					var iPageAmount = 0;
					var iPageBonus = 0;
					var iPageTotalAmount = 0;
					var len = iEnd - iStart;
					for (var i = 0; i < len; i++) {
//		            for (var i=iStart ; i<iEnd; i++) {
						var data = aaData[aiDisplay[i]];
						if (data) {
							iPageAmount += (parseFloat(data.amount) || 0);
							iPageBonus += (parseFloat(data.bonus) || 0);
							iPageTotalAmount += (parseFloat(data.totalAmount) || 0);
						}
					}
					var footer = $(nRow);
					footer.find("[name='amount']").html('<div style="text-align: right;">' + CurrencyUtil.formatter(iPageAmount) + '</div>');
					footer.find("[name='bonus']").html('<div style="text-align: right;">' + CurrencyUtil.formatter(iPageBonus) + '</div>');
					footer.find("[name='totalAmount']").html('<div style="text-align: right;">' + CurrencyUtil.formatter(iPageTotalAmount) + '</div>');
				}
			};
		}
		var isEnableSequence = false;
		dataTableDetailsOptions[paymentType].dataTableRef = DataTableHandler.create(dataTableDetailsOptions[paymentType], isEnableSequence, PageConfig.lang, function () {
			$('#depositDetailsModal').modal('show');
		});
	};

	var gotoAdjustmentDetails = function (paymentType, transactionDate) {
		var searchDetailGlobalForm = $("[name='searchDetailGlobalForm']");
		searchDetailGlobalForm.find("[name='transactionDate']").val(transactionDate);

		if (!dataTableDetailsOptions[paymentType]) {
			dataTableDetailsOptions[paymentType] = {
				tableSelector: '#adjustmentDetailsTable',
				formSelector: "[name='searchDetailGlobalForm']",//optional
				sAjaxSource: '/manager/member/searchAdjustmentDetails',
				aaSorting: [[0, "desc"]],
				hideColVis: true,
				iDisplayLength: PageConfig.pageSize,
				aoColumns: [
					{"mData": "createTime"},
					{"mData": "id"},
					{
						"mData": "amount",
						"mRender": function (data, type, full) {
							if (type === 'display') {
								return CurrencyUtil.formatter(data);
							}
							return data;
						}
					}
				],
				aoColumnDefs: [{"sClass": "dt-body-right", "aTargets": [2]}],
				"fnFooterCallback": function (nRow, aaData, iStart, iEnd, aiDisplay) {
					var iPageAmount = 0;
					var len = iEnd - iStart;
					for (var i = 0; i < len; i++) {
//		            for (var i=iStart ; i<iEnd; i++) {
						var data = aaData[aiDisplay[i]];
						if (data) {
							iPageAmount += (parseFloat(data.amount) || 0);
						}
					}
					$(nRow).find("[name='amount']").html('<div style="text-align: right;">' + CurrencyUtil.formatter(iPageAmount) + '</div>');
				}
			};
		}
		var isEnableSequence = false;
		dataTableDetailsOptions[paymentType].dataTableRef = DataTableHandler.create(dataTableDetailsOptions[paymentType], isEnableSequence, PageConfig.lang, function () {
			$('#adjustmentDetailsModal').modal('show');
		});
	};

	var gotoRevenueAdjustmentDetails = function (paymentType, transactionDate) {
		var searchDetailGlobalForm = $("[name='searchDetailGlobalForm']");
		searchDetailGlobalForm.find("[name='transactionDate']").val(transactionDate);

		if (!dataTableDetailsOptions[paymentType]) {
			dataTableDetailsOptions[paymentType] = {
				tableSelector: '#revenueAdjustmentDetailsTable',
				formSelector: "[name='searchDetailGlobalForm']",//optional
				sAjaxSource: '/manager/member/searchRevenueAdjustmentDetails',
				aaSorting: [[0, "desc"]],
				hideColVis: true,
				iDisplayLength: PageConfig.pageSize,
				aoColumns: [
					{"mData": "createTime"},
					{"mData": "id"},
					{
						"mData": "amount",
						"mRender": function (data, type, full) {
							if (type === 'display') {
								return CurrencyUtil.formatter(data);
							}
							return data;
						}
					}
				],
				aoColumnDefs: [{"sClass": "dt-body-right", "aTargets": [2]}],
				"fnFooterCallback": function (nRow, aaData, iStart, iEnd, aiDisplay) {
					var iPageAmount = 0;
					var len = iEnd - iStart;
					for (var i = 0; i < len; i++) {
//		            for (var i=iStart ; i<iEnd; i++) {
						var data = aaData[aiDisplay[i]];
						if (data) {
							iPageAmount += (parseFloat(data.amount) || 0);
						}
					}
					$(nRow).find("[name='amount']").html('<div style="text-align: right;">' + CurrencyUtil.formatter(iPageAmount) + '</div>');
				}
			};
		}
		var isEnableSequence = false;
		dataTableDetailsOptions[paymentType].dataTableRef = DataTableHandler.create(dataTableDetailsOptions[paymentType], isEnableSequence, PageConfig.lang, function () {
			$('#revenueAdjustmentDetailsModal').modal('show');
		});
	};

	var gotoWithdrawalsDetails = function (paymentType, transactionDate) {
		var searchDetailGlobalForm = $("[name='searchDetailGlobalForm']");
		searchDetailGlobalForm.find("[name='transactionDate']").val(transactionDate);

		if (!dataTableDetailsOptions[paymentType]) {
			dataTableDetailsOptions[paymentType] = {
				tableSelector: '#withdrawalsDetailsTable',
				formSelector: "[name='searchDetailGlobalForm']",//optional
				sAjaxSource: '/manager/member/searchWithdrawalsDetails',
				aaSorting: [[0, "desc"]],
				hideColVis: true,
				iDisplayLength: PageConfig.pageSize,
				aoColumns: [
					{"mData": "createTime"},
					{"mData": "approvedTime"},
					{"mData": "approvedUserid"},
					{"mData": "id"},
					{"mData": "toBankName"},
					{
						"mData": "amount",
						"mRender": function (data, type, full) {
							if (type === 'display') {
								return CurrencyUtil.formatter(data);
							}
							return data;
						}
					}
				],
				aoColumnDefs: [{"sClass": "dt-body-right", "aTargets": [5]}],
				"fnFooterCallback": function (nRow, aaData, iStart, iEnd, aiDisplay) {
					var iPageAmount = 0;
					var len = iEnd - iStart;
					for (var i = 0; i < len; i++) {
//		            for (var i=iStart ; i<iEnd; i++) {
						var data = aaData[aiDisplay[i]];
						if (data) {
							iPageAmount += (parseFloat(data.amount) || 0);
						}
					}
					$(nRow).find("[name='amount']").html('<div style="text-align: right;">' + CurrencyUtil.formatter(iPageAmount) + '</div>');
				}
			};
		}
		var isEnableSequence = false;
		dataTableDetailsOptions[paymentType].dataTableRef = DataTableHandler.create(dataTableDetailsOptions[paymentType], isEnableSequence, PageConfig.lang, function () {
			$('#withdrawalsDetailsModal').modal('show');
		});
	};

	var gotoTransferDetails = function (paymentType, transactionDate) {
		var searchDetailGlobalForm = $("[name='searchDetailGlobalForm']");
		searchDetailGlobalForm.find("[name='transactionDate']").val(transactionDate);

		if (!dataTableDetailsOptions[paymentType]) {
			dataTableDetailsOptions[paymentType] = {
				tableSelector: '#transferDetailsTable',
				formSelector: "[name='searchDetailGlobalForm']",//optional
				sAjaxSource: '/manager/member/searchTransferDetails',
				aaSorting: [[0, "desc"]],
				hideColVis: true,
				iDisplayLength: PageConfig.pageSize,
				aoColumns: [
					{"mData": "createTime"},
					{"mData": "id"},
					{
						"mData": "from",
						"bSortable": false,
					},
					{
						"mData": "to",
						"bSortable": false,
					},
					{
						"mData": "amount",
						"mRender": function (data, type, full) {
							if (type === 'display') {
								return CurrencyUtil.formatter(data);
							}
							return data;
						}
					}
				],
				aoColumnDefs: [{"sClass": "dt-body-right", "aTargets": [4]}],
				"fnFooterCallback": function (nRow, aaData, iStart, iEnd, aiDisplay) {
					var iPageAmount = 0;
					var len = iEnd - iStart;
					for (var i = 0; i < len; i++) {
//		            for (var i=iStart ; i<iEnd; i++) {
						var data = aaData[aiDisplay[i]];
						if (data) {
							iPageAmount += (parseFloat(data.amount) || 0);
						}
					}
					$(nRow).find("[name='amount']").html('<div style="text-align: right;">' + CurrencyUtil.formatter(iPageAmount) + '</div>');
				}
			};
		}
		var isEnableSequence = false;
		dataTableDetailsOptions[paymentType].dataTableRef = DataTableHandler.create(dataTableDetailsOptions[paymentType], isEnableSequence, PageConfig.lang, function () {
			$('#transferDetailsModal').modal('show');
		});
	};

	var gotoTurnoverBonusDetails = function (paymentType, transactionDate) {
		var searchDetailGlobalForm = $("[name='searchDetailGlobalForm']");
		searchDetailGlobalForm.find("[name='transactionDate']").val(transactionDate);

		if (!dataTableDetailsOptions[paymentType]) {
			dataTableDetailsOptions[paymentType] = {
				tableSelector: '#turnoverBonusDetailsTable',
				formSelector: "[name='searchDetailGlobalForm']",//optional
				sAjaxSource: '/manager/member/searchTurnoverBonusDetails',
				aaSorting: [[0, "desc"]],
				hideColVis: true,
				iDisplayLength: PageConfig.pageSize,
				aoColumns: [
					{"mData": "createTime"},
					{"mData": "closedTime", "bVisible": false},
					{"mData": "bonusTitle"},
					{
						"mData": "bonus",
						"mRender": function (data, type, full) {
							if (type === 'display') {
								return CurrencyUtil.formatter(data);
							}
							return data;
						}
					}
				],
				aoColumnDefs: [{"sClass": "dt-body-right", "aTargets": [3]}],
				"fnFooterCallback": function (nRow, aaData, iStart, iEnd, aiDisplay) {
					var iPageBonus = 0;
					var len = iEnd - iStart;
					for (var i = 0; i < len; i++) {
//		            for (var i=iStart ; i<iEnd; i++) {
						var data = aaData[aiDisplay[i]];
						if (data) {
							iPageBonus += (parseFloat(data.bonus) || 0);
						}
					}
					$(nRow).find("[name='bonus']").html('<div style="text-align: right;">' + CurrencyUtil.formatter(iPageBonus) + '</div>');
				}
			};
		}
		var isEnableSequence = false;
		dataTableDetailsOptions[paymentType].dataTableRef = DataTableHandler.create(dataTableDetailsOptions[paymentType], isEnableSequence, PageConfig.lang, function () {
			$('#turnoverBonusDetailsModal').modal('show');
		});
	};

	var gotoLossBonusDetails = function (paymentType, transactionDate) {
		var searchDetailGlobalForm = $("[name='searchDetailGlobalForm']");
		searchDetailGlobalForm.find("[name='transactionDate']").val(transactionDate);

		if (!dataTableDetailsOptions[paymentType]) {
			dataTableDetailsOptions[paymentType] = {
				tableSelector: '#lossBonusDetailsTable',
				formSelector: "[name='searchDetailGlobalForm']",//optional
				sAjaxSource: '/manager/member/searchLossBonusDetails',
				aaSorting: [[0, "desc"]],
				hideColVis: true,
				iDisplayLength: PageConfig.pageSize,
				aoColumns: [
					{"mData": "createTime"},
					{"mData": "closedTime", "bVisible": false},
					{"mData": "bonusTitle"},
					{
						"mData": "bonus",
						"mRender": function (data, type, full) {
							if (type === 'display') {
								return CurrencyUtil.formatter(data);
							}
							return data;
						}
					}
				],
				aoColumnDefs: [{"sClass": "dt-body-right", "aTargets": [3]}],
				"fnFooterCallback": function (nRow, aaData, iStart, iEnd, aiDisplay) {
					var iPageBonus = 0;
					var len = iEnd - iStart;
					for (var i = 0; i < len; i++) {
//		            for (var i=iStart ; i<iEnd; i++) {
						var data = aaData[aiDisplay[i]];
						if (data) {
							iPageBonus += (parseFloat(data.bonus) || 0);
						}
					}
					$(nRow).find("[name='bonus']").html('<div style="text-align: right;">' + CurrencyUtil.formatter(iPageBonus) + '</div>');
				}
			};
		}
		var isEnableSequence = false;
		dataTableDetailsOptions[paymentType].dataTableRef = DataTableHandler.create(dataTableDetailsOptions[paymentType], isEnableSequence, PageConfig.lang, function () {
			$('#lossBonusDetailsModal').modal('show');
		});
	};

	var gotoIssueBonusDetails = function (paymentType, transactionDate) {
		var searchDetailGlobalForm = $("[name='searchDetailGlobalForm']");
		searchDetailGlobalForm.find("[name='transactionDate']").val(transactionDate);

		if (!dataTableDetailsOptions[paymentType]) {
			dataTableDetailsOptions[paymentType] = {
				tableSelector: '#issueBonusDetailsTable',
				formSelector: "[name='searchDetailGlobalForm']",//optional
				sAjaxSource: '/manager/member/searchIssueBonusDetails',
				aaSorting: [[0, "desc"]],
				hideColVis: true,
				iDisplayLength: PageConfig.pageSize,
				aoColumns: [
					{"mData": "createTime"},
					{"mData": "closedTime", "bVisible": false},
					{"mData": "bonusTitle"},
					{
						"mData": "bonus",
						"mRender": function (data, type, full) {
							if (type === 'display') {
								return CurrencyUtil.formatter(data);
							}
							return data;
						}
					}
				],
				aoColumnDefs: [{"sClass": "dt-body-right", "aTargets": [3]}],
				"fnFooterCallback": function (nRow, aaData, iStart, iEnd, aiDisplay) {
					var iPageBonus = 0;
					var len = iEnd - iStart;
					for (var i = 0; i < len; i++) {
//		            for (var i=iStart ; i<iEnd; i++) {
						var data = aaData[aiDisplay[i]];
						if (data) {
							iPageBonus += (parseFloat(data.bonus) || 0);
						}
					}
					$(nRow).find("[name='bonus']").html('<div style="text-align: right;">' + CurrencyUtil.formatter(iPageBonus) + '</div>');
				}
			};
		}
		var isEnableSequence = false;
		dataTableDetailsOptions[paymentType].dataTableRef = DataTableHandler.create(dataTableDetailsOptions[paymentType], isEnableSequence, PageConfig.lang, function () {
			$('#issueBonusDetailsModal').modal('show');
		});
	};

	var gotoSpecialBonusDetails = function (paymentType, transactionDate) {
		var searchDetailGlobalForm = $("[name='searchDetailGlobalForm']");
		searchDetailGlobalForm.find("[name='transactionDate']").val(transactionDate);

		if (!dataTableDetailsOptions[paymentType]) {
			dataTableDetailsOptions[paymentType] = {
				tableSelector: '#specialBonusDetailsTable',
				formSelector: "[name='searchDetailGlobalForm']",//optional
				sAjaxSource: '/manager/member/searchSpecialBonusDetails',
				aaSorting: [[0, "desc"]],
				hideColVis: true,
				iDisplayLength: PageConfig.pageSize,
				aoColumns: [
					{"mData": "createTime"},
					{"mData": "closedTime", "bVisible": false},
					{"mData": "bonusTitle"},
					{
						"mData": "bonus",
						"mRender": function (data, type, full) {
							if (type === 'display') {
								return CurrencyUtil.formatter(data);
							}
							return data;
						}
					}
				],
				aoColumnDefs: [{"sClass": "dt-body-right", "aTargets": [3]}],
				"fnFooterCallback": function (nRow, aaData, iStart, iEnd, aiDisplay) {
					var iPageBonus = 0;
					var len = iEnd - iStart;
					for (var i = 0; i < len; i++) {
//		            for (var i=iStart ; i<iEnd; i++) {
						var data = aaData[aiDisplay[i]];
						if (data) {
							iPageBonus += (parseFloat(data.bonus) || 0);
						}
					}
					$(nRow).find("[name='bonus']").html('<div style="text-align: right;">' + CurrencyUtil.formatter(iPageBonus) + '</div>');
				}
			};
		}
		var isEnableSequence = false;
		dataTableDetailsOptions[paymentType].dataTableRef = DataTableHandler.create(dataTableDetailsOptions[paymentType], isEnableSequence, PageConfig.lang, function () {
			$('#specialBonusDetailsModal').modal('show');
		});
	};

	var gotoDepositBonusDetails = function (paymentType, transactionDate) {
		var searchDetailGlobalForm = $("[name='searchDetailGlobalForm']");
		searchDetailGlobalForm.find("[name='transactionDate']").val(transactionDate);

		if (!dataTableDetailsOptions[paymentType]) {
			dataTableDetailsOptions[paymentType] = {
				tableSelector: '#depositBonusDetailsTable',
				formSelector: "[name='searchDetailGlobalForm']",//optional
				sAjaxSource: '/manager/member/searchDepositBonusDetails',
				aaSorting: [[0, "desc"]],
				hideColVis: true,
				iDisplayLength: PageConfig.pageSize,
				aoColumns: [
					{"mData": "createTime"},
					{"mData": "closedTime", "bVisible": false},
					{"mData": "bonusTitle"},
					{
						"mData": "bonus",
						"mRender": function (data, type, full) {
							if (type === 'display') {
								return CurrencyUtil.formatter(data);
							}
							return data;
						}
					}
				],
				aoColumnDefs: [{"sClass": "dt-body-right", "aTargets": [3]}],
				"fnFooterCallback": function (nRow, aaData, iStart, iEnd, aiDisplay) {
					var iPageBonus = 0;
					var len = iEnd - iStart;
					for (var i = 0; i < len; i++) {
//		            for (var i=iStart ; i<iEnd; i++) {
						var data = aaData[aiDisplay[i]];
						if (data) {
							iPageBonus += (parseFloat(data.bonus) || 0);
						}
					}
					$(nRow).find("[name='bonus']").html('<div style="text-align: right;">' + CurrencyUtil.formatter(iPageBonus) + '</div>');
				}
			};
		}
		var isEnableSequence = false;
		dataTableDetailsOptions[paymentType].dataTableRef = DataTableHandler.create(dataTableDetailsOptions[paymentType], isEnableSequence, PageConfig.lang, function () {
			$('#depositBonusDetailsModal').modal('show');
		});
	};

	var gotoRecycleBalanceDetails = function (paymentType, transactionDate) {
		var searchDetailGlobalForm = $("[name='searchDetailGlobalForm']");
		searchDetailGlobalForm.find("[name='transactionDate']").val(transactionDate);

		if (!dataTableDetailsOptions[paymentType]) {
			dataTableDetailsOptions[paymentType] = {
				tableSelector: '#recycleBalanceDetailsTable',
				formSelector: "[name='searchDetailGlobalForm']",//optional
				sAjaxSource: '/manager/member/searchRecycleBalanceDetails',
				aaSorting: [[0, "desc"]],
				hideColVis: true,
				iDisplayLength: PageConfig.pageSize,
				aoColumns: [
					{"mData": "createTime"},
					{"mData": "bonusTitle"},
					{"mData": "closedTime"},
					{
						"mData": "balance",
						"mRender": function (data, type, full) {
							if (type === 'display') {
								return CurrencyUtil.formatter(data);
							}
							return data;
						}
					}
				],
				aoColumnDefs: [{"sClass": "dt-body-right", "aTargets": [3]}],
				"fnFooterCallback": function (nRow, aaData, iStart, iEnd, aiDisplay) {
					var iPageBalance = 0;
					var len = iEnd - iStart;
					for (var i = 0; i < len; i++) {
//		            for (var i=iStart ; i<iEnd; i++) {
						var data = aaData[aiDisplay[i]];
						if (data) {
							iPageBalance += (parseFloat(data.balance) || 0);
						}
					}
					$(nRow).find("[name='balance']").html('<div style="text-align: right;">' + CurrencyUtil.formatter(iPageBalance) + '</div>');
				}
			};
		}
		var isEnableSequence = false;
		dataTableDetailsOptions[paymentType].dataTableRef = DataTableHandler.create(dataTableDetailsOptions[paymentType], isEnableSequence, PageConfig.lang, function () {
			$('#recycleBalanceDetailsModal').modal('show');
		});
	};

	var gotoReferralCommissionDetails = function (paymentType, transactionDate) {
		var searchDetailGlobalForm = $("[name='searchDetailGlobalForm']");
		searchDetailGlobalForm.find("[name='transactionDate']").val(transactionDate);

		if (!dataTableDetailsOptions[paymentType]) {
			dataTableDetailsOptions[paymentType] = {
				tableSelector: '#referralCommissionDetailsTable',
				formSelector: "[name='searchDetailGlobalForm']",//optional
				sAjaxSource: '/manager/member/searchReferralCommissionDetails',
				aaSorting: [[0, "desc"]],
				hideColVis: true,
				iDisplayLength: PageConfig.pageSize,
				aoColumns: [
					{"mData": "receivedTime"},
					{
						"mData": "amount",
						"mRender": function (data, type, full) {
							if (type === 'display') {
								return CurrencyUtil.formatter(data);
							}
							return data;
						}
					}
				],
				aoColumnDefs: [{"sClass": "dt-body-right", "aTargets": [1]}],
				"fnFooterCallback": function (nRow, aaData, iStart, iEnd, aiDisplay) {
					var iPageBalance = 0;
					var len = iEnd - iStart;
					for (var i = 0; i < len; i++) {
						var data = aaData[aiDisplay[i]];
						if (data) {
							iPageBalance += (parseFloat(data.amount) || 0);
						}
					}
					$(nRow).find("[name='amount']").html('<div style="text-align: right;">' + CurrencyUtil.formatter(iPageBalance) + '</div>');
				}
			};
		}
		var isEnableSequence = false;
		dataTableDetailsOptions[paymentType].dataTableRef = DataTableHandler.create(dataTableDetailsOptions[paymentType], isEnableSequence, PageConfig.lang, function () {
			$('#referralCommissionDetailsModal').modal('show');
		});
	};

	var closeBetDetailsEvent = function () {
		$('#betDetailsModal').on('hidden.bs.modal', function (e) {
			$('#betSummaryDetailsModal').modal('show');
		});
	};

	var dateRangeInitial = function () {

		var dateOptionWithTime = {
			"singleDatePicker": false,
			"timePicker": true,
			timePicker24Hour: false,
			autoUpdateInput: true,
			autoApply: false,
			locale: DateRangeHandler.changeLanguage('DD/MM/YYYY HH:mm:ss', PageConfig.lang),
			"maxDate": PageConfig.date.today,
			"minDate": PageConfig.date.todayHalfYearAgo,
			startDate: PageConfig.date.todayOneMonthAgo,
			endDate: PageConfig.date.today
		};

		var dateOptionOnlyDate = {
			"singleDatePicker": false,
			"timePicker": false,
			autoUpdateInput: true,
			autoApply: false,
			locale: DateRangeHandler.changeLanguage('DD/MM/YYYY', PageConfig.lang),  // 只显示日期格式
			"maxDate": PageConfig.date.today,
			"minDate": PageConfig.date.todayHalfYearAgo,
			startDate: PageConfig.date.todayOneMonthAgoOnlyDate,
			endDate: PageConfig.date.todayOnlyDate
		};

		$('[name$=Daterange]').each(function () {
			let dateRangeId = $(this).attr('id');
			if ($(this).attr('id') === 'searchProfileReportDaterangeForBet'
				|| $(this).attr('id') === 'searchProfileReportDaterangeForBetUnsettled'
				|| $(this).attr('id') === 'searchProfileReportDaterangeForBetSettled') {

				$(this).daterangepicker(dateOptionOnlyDate);
				$(this).val(PageConfig.date.todayOneMonthAgoOnlyDate + " - " + PageConfig.date.todayOnlyDate);

				var startDate = moment(PageConfig.date.todayOneMonthAgoOnlyDate, 'DD/MM/YYYY').startOf('day');
				var endDate = moment(PageConfig.date.todayOnlyDate, 'DD/MM/YYYY').endOf('day');

				$(this).parent().find('[name="searchDateRange"]').val(
					startDate.format('DD/MM/YYYY HH:mm:ss') + '-' + endDate.format('DD/MM/YYYY HH:mm:ss')
				);
			} else {

				$(this).daterangepicker(dateOptionWithTime);
				$(this).val(PageConfig.date.todayOneMonthAgo + " - " + PageConfig.date.today);
			}
		});

		$('[name$=Daterange]').on('apply.daterangepicker', function (e, picker) {

			var $this = $(this);
			var startDate, endDate;

			if ($this.attr('id') === 'searchProfileReportDaterangeForBet'
				|| $this.attr('id') === 'searchProfileReportDaterangeForBetUnsettled'
				|| $this.attr('id') === 'searchProfileReportDaterangeForBetSettled') {

				$this.val(
					picker.startDate.format('DD/MM/YYYY') + ' - ' + picker.endDate.format('DD/MM/YYYY')
				);

				startDate = moment(picker.startDate).startOf('day');
				endDate = moment(picker.endDate).endOf('day');

			} else {

				// 带时间的格式
				startDate = moment(picker.startDate);
				startDate.set({second: 0});
				endDate = moment(picker.endDate);
				endDate.set({second: 59});
				picker.setEndDate(endDate);

				$this.val(
					startDate.format('DD/MM/YYYY HH:mm:ss') + ' - ' + endDate.format('DD/MM/YYYY HH:mm:ss')
				);
			}

			var apiValue = startDate.format('DD/MM/YYYY HH:mm:ss') + '-' + endDate.format('DD/MM/YYYY HH:mm:ss');

			$this.parent().find('[name="searchDateRange"]').val(apiValue);
		});

		$('[name$=Daterange]').on('cancel.daterangepicker', function (e, picker) {
			$(this).val('');
			$(this).parent().find('[name="searchDateRange"]').val('');
		});

		$("input[id^=searchProfile]").keydown(false);

		$('.fa-calendar').hide();
	};

	ReportHandler.gotoDetail = function (element) {
		const paymentType = $(element).data('type');
		const transactionDate = $(element).data('date');

		if (paymentType === AccountSummaryReportType.BET.unique()) {
			gotoBetSummaryDetails(paymentType, transactionDate);
			return;
		}
		if (paymentType === AccountSummaryReportType.DEPOSIT.unique()) {
			gotoDepositDetails(paymentType, transactionDate);
			return;
		}
		if (paymentType === AccountSummaryReportType.ADJUSTMENT.unique()) {
			gotoAdjustmentDetails(paymentType, transactionDate);
			return;
		}
		if (paymentType === AccountSummaryReportType.WITHDRAWALS.unique()) {
			gotoWithdrawalsDetails(paymentType, transactionDate);
			return;
		}
		if (paymentType === AccountSummaryReportType.TRANSFER.unique()) {
			gotoTransferDetails(paymentType, transactionDate);
			return;
		}
		if (paymentType === AccountSummaryReportType.TURNOVER_BONUS.unique()) {
			gotoTurnoverBonusDetails(paymentType, transactionDate);
			return;
		}
		if (paymentType === AccountSummaryReportType.LOSS_BONUS.unique()) {
			gotoLossBonusDetails(paymentType, transactionDate);
			return;
		}
		if (paymentType === AccountSummaryReportType.ISSUE_BONUS.unique()) {
			gotoIssueBonusDetails(paymentType, transactionDate);
			return;
		}
		if (paymentType === AccountSummaryReportType.SPECIAL_BONUS.unique()) {
			gotoSpecialBonusDetails(paymentType, transactionDate);
			return;
		}
		if (paymentType === AccountSummaryReportType.DEPOSIT_BONUS.unique()) {
			gotoDepositBonusDetails(paymentType, transactionDate);
		}
		if (paymentType === AccountSummaryReportType.RECYCLE_BALANCE.unique()) {
			gotoRecycleBalanceDetails(paymentType, transactionDate);
		}
		if (paymentType === AccountSummaryReportType.REVENUE_ADJUSTMENT.unique()) {
			gotoRevenueAdjustmentDetails(paymentType, transactionDate);
		}
		if (paymentType === AccountSummaryReportType.REFERRAL_COMMISSION.unique()) {
			gotoReferralCommissionDetails(paymentType, transactionDate);
		}
	};

	ReportHandler.exportTodayBetReport = function () {

		const visibleColumns = ColVisHandler.getVisibleColumns(dataTableOptions[tabs["Today's Bet"]].dataTableRef);

		const searchForm = $("[name='searchProfileGlobalForm']");
		if (!searchForm.valid()) {
			return;
		}

		searchForm.find('[name=visibleColumns]').val(visibleColumns);

		ExcelUtils.schedule(ReportExportType.TODAY_BET, '/manager/member/exportTodayBetReport', searchForm.serialize(), $('[name=exportTodayBet]'));
	};

	ReportHandler.openGameTxnData = function (e) {
		var $rowParent = $(e).closest('tr');
		var txnId = $rowParent.find('#gameTxnId').val();
		var txnTime = $rowParent.find('#txnTime').val();
		var txnStatus = $rowParent.find('#txnStatus').val();
		var settleTime = "";
		if ($rowParent.find('#settleTime').length > 0) {
			settleTime = $rowParent.find('#settleTime').val();
		}

		var panel = window.open("/page/manager/member/subGameTransaction.jsp?gameTxnId=" + txnId + "&txnTime=" + txnTime + "&settleTime=" + settleTime
			+ "&txnStatus=" + txnStatus + "&currency=" + PageConfig.accountCurrency + "&userId=" + PageConfig.userId,
			"_blank",
			"width=1200,height=640,top=5,left=5,toolbar=no,menubar=no,resizable=no,scrollbars=yes,status=no,location=no,directories=no");
	};

	ReportHandler.viewKycImage = function (data) {
		const $viewDocumentPhoto = $('#viewDocumentPhoto');
		const $photo = $viewDocumentPhoto.find('[name=photo]');

		$.ajax({
			type: "POST",
			url: '/manager/member/viewKycImage',
			data: {
				imagePath: data
			},
			success: function (data) {

				if (data.error) {
					NotifyHandler.errorMsg(data.error);
					return;
				}

				let imageUrl = data.path;

				if (imageUrl) {
					$photo.empty();
					const image = new Image();
					image.src = 'data:image/png;base64,' + imageUrl;
					$photo.append(image);
				} else {
					$photo.empty();
					$photo.append(`<span>${I18N.get("msg.info.profile.noDocumentImage")}</span>`);
				}

				$viewDocumentPhoto.modal('show');
			}
		});
	};

	ReportHandler.searchProfileDepositRecord = function () {
		var tabId = tabs['Deposit'];
		if (dataTableOptions[tabId]?.dataTableRef) {
			const table = $(dataTableOptions[tabId].tableSelector).DataTable();
			table.fnDestroy();
		}

		const form = $("[name='searchDepositRecordForm']")
		if (!form.data("validator")) {
			form.validate({
				rules: {
					minAmount: {
						minMaxAmountChecking: true
					},
					maxAmount: {
						minMaxAmountChecking: true
					}
				},
				messages: {
					minAmount: {
						minMaxAmountChecking: "The Min amount must not be greater than the Max amount."
					},
					maxAmount: {
						minMaxAmountChecking: "The Min amount must not be greater than the Max amount."
					}

				}, ignore: [],

				errorPlacement: function (error, element) {
					var errorContainer = element.closest('.form-group').find(".error-msg-amount-validation");
					if (errorContainer.length > 0) {
						errorContainer.html(error);
					} else {
						error.insertAfter(element);
					}
				}
			});
		}

		$("#amount_label_id").text(I18N.get("form.text.af.ui.amount") + " (" + PageConfig.managerCurrencySymbol + ")")
		dataTableOptions[tabId] = {
			tableSelector: '#searchProfileDepositRecordTable',
			formSelector: "[name='searchDepositRecordForm']",//optional
			sAjaxSource: '/manager/member/getMoneyTransactionRecord',
			aaSorting: [[5, "desc"]],
			excludeColVis: [0],
			iDisplayLength: PageConfig.pageSize,
			aoColumns: [
				{
					"mData": null,
					"bSortable": false,
				},
				{
					"mData": "transactionId",
				},
				{
					"mData": "amount",
					"mRender": function (data, type, full) {
						return CurrencyUtil.formatter(data);
					}
				},
				{
					"mData": "status",
					"mRender": function (data, type, full) {
						if (type === 'display') {
							var depositStatus = PageConfig.MoneyTransactionStatusType[data];
							console.log("depositStatus ", depositStatus)
							if (depositStatus) {
								return '<span class="label ' + depositStatus.css + '">' + I18N.get(depositStatus.getFullName) + '</span>';
							}
							return data;
						}
						return data;
					}
				},
				{"mData": "createdBy"},
				{
					"mData": "createdTime",
					"mRender": function (data, type, full) {
						if (type === 'display') {
							return moment(data).format("DD/MM/YYYY HH:mm:ss");
						}
						return data;
					}
				},
				{
					"mData": "updatedBy",
					"mRender": function (data, type, full) {
						if (data) {
							return data
						}
						return "-";
					}
				},
				{
					"mData": "updatedTime",
					"mRender": function (data, type, full) {
						if (data) {
							if (type === 'display' && data) {
								return moment(data).format("DD/MM/YYYY HH:mm:ss");
							}
							return data;
						}
						return "-";
					}
				},
			],
			aoColumnDefs: [{"sClass": "dt-body-right", "aTargets": [2]}],
		};

		var callBack = function (response) {
			var totalAmount = response.iTotalAmount;

			var grandTotal = totalAmount.grandTotal ?? 0;
			var totalAmountCurrentPage = totalAmount.totalAmount ?? 0;

			$('#searchProfileDepositRecordTable').find("[name='grandTotal']").html('<div style="text-align: right;">' + CurrencyUtil.formatter(grandTotal) + '</div>');
			$('#searchProfileDepositRecordTable').find("[name='subTotal']").html('<div style="text-align: right;">' + CurrencyUtil.formatter(totalAmountCurrentPage) + '</div>');
		};

		var isEnableSequence = true;
		dataTableOptions[tabId].dataTableRef = DataTableHandler.create(dataTableOptions[tabId], isEnableSequence, PageConfig.lang, callBack);
		$(dataTableOptions[tabId].tableSelector).on('order.dt', function () {
			const order = dataTableOptions[tabId].dataTableRef.order();
			dataTableOptions[tabId].aaSorting = order;
		});

		$('#profileDepositRecordTable').show();
	}

	ReportHandler.searchProfileWithdrawalRecord = function () {
		var tabId = tabs['Withdrawal'];
		if (dataTableOptions[tabId]?.dataTableRef) {
			const table = $(dataTableOptions[tabId].tableSelector).DataTable();
			table.fnDestroy();
		}

		const form = $("[name='searchWithdrawalRecordForm']")
		if (!form.data("validator")) {
			form.validate({
				rules: {
					minAmount: {
						minMaxAmountChecking: true
					},
					maxAmount: {
						minMaxAmountChecking: true
					}
				},
				messages: {
					minAmount: {
						minMaxAmountChecking: "The Min amount must not be greater than the Max amount."
					},
					maxAmount: {
						minMaxAmountChecking: "The Min amount must not be greater than the Max amount."
					}

				}, ignore: [],

				errorPlacement: function (error, element) {
					var errorContainer = element.closest('.form-group').find(".error-msg-amount-validation");
					if (errorContainer.length > 0) {
						errorContainer.html(error);
					} else {
						error.insertAfter(element);
					}
				}
			});
		}

		dataTableOptions[tabId] = {
			tableSelector: '#searchProfileWithdrawalRecordTable',
			formSelector: "[name='searchWithdrawalRecordForm']",//optional
			sAjaxSource: '/manager/member/getMoneyTransactionRecord',
			aaSorting: [[5, "desc"]],
			excludeColVis: [0],
			iDisplayLength: PageConfig.pageSize,
			aoColumns: [
				{
					"mData": null,
					"bSortable": false,
				},
				{
					"mData": "transactionId",
				},
				{
					"mData": "amount",
					"mRender": function (data, type, full) {
						return CurrencyUtil.formatter(data);
					}
				},
				{
					"mData": "status",
					"mRender": function (data, type, full) {
						if (type === 'display') {
							var depositStatus = PageConfig.MoneyTransactionStatusType[data];
							console.log("depositStatus ", depositStatus)
							if (depositStatus) {
								return '<span class="label ' + depositStatus.css + '">' + I18N.get(depositStatus.getFullName) + '</span>';
							}
							return data;
						}
						return data;
					}
				},
				{"mData": "createdBy"},
				{
					"mData": "createdTime",
					"mRender": function (data, type, full) {
						if (type === 'display') {
							return moment(data).format("DD/MM/YYYY HH:mm:ss");
						}
						return data;
					}
				},
				{
					"mData": "updatedBy",
					"mRender": function (data, type, full) {
						if (data) {
							return data
						}
						return "-";
					}
				},
				{
					"mData": "updatedTime",
					"mRender": function (data, type, full) {
						if (data) {
							if (type === 'display' && data) {
								return moment(data).format("DD/MM/YYYY HH:mm:ss");
							}
							return data;
						}
						return "-";
					}
				},
			],
			aoColumnDefs: [{"sClass": "dt-body-right", "aTargets": [2]}],
		};

		var callBack = function (response) {
			var totalAmount = response.iTotalAmount;

			var grandTotal = totalAmount.grandTotal ?? 0;
			var totalAmountCurrentPage = totalAmount.totalAmount ?? 0;

			$('#searchProfileWithdrawalRecordTable').find("[name='grandTotal']").html('<div style="text-align: right;">' + CurrencyUtil.formatter(grandTotal) + '</div>');
			$('#searchProfileWithdrawalRecordTable').find("[name='subTotal']").html('<div style="text-align: right;">' + CurrencyUtil.formatter(totalAmountCurrentPage) + '</div>');
		};

		var isEnableSequence = true;
		dataTableOptions[tabId].dataTableRef = DataTableHandler.create(dataTableOptions[tabId], isEnableSequence, PageConfig.lang, callBack);
		$(dataTableOptions[tabId].tableSelector).on('order.dt', function () {
			const order = dataTableOptions[tabId].dataTableRef.order();
			dataTableOptions[tabId].aaSorting = order;
		});

		$('#searchProfileWithdrawalRecordTable').show();
	}

	ReportHandler.searchProfileAdjustmentRecord = function () {
		var tabId = tabs['Adjustment'];
		if (dataTableOptions[tabId]?.dataTableRef) {
			const table = $(dataTableOptions[tabId].tableSelector).DataTable();
			table.fnDestroy();
		}
		dataTableOptions[tabId] = {
			tableSelector: '#searchProfileAdjustmentRecordTable',
			formSelector: "[name='searchAdjustmentRecordForm']",//optional
			sAjaxSource: '/manager/member/getMoneyTransactionRecord',
			aaSorting: [[4, "desc"]],
			excludeColVis: [0],
			iDisplayLength: PageConfig.pageSize,
			aoColumns: [
				{
					"mData": null,
					"bSortable": false,
				},
				{
					"mData": "transactionId",
				},
				{
					"mData": "amount",
					"mRender": function (data, type, full) {
						return CurrencyUtil.formatter(data);
					}
				},
				{
					"mData": "createdBy"
				},
				{
					"mData": "createdTime",
					"bSortable": true,
					"mRender": function (data, type, full) {
						if (type === 'display') {
							return moment(data).format("DD/MM/YYYY HH:mm:ss");
						}
						return data;
					}
				},
			],
			aoColumnDefs: [{"sClass": "dt-body-right", "aTargets": [2]}],
		};

		var callBack = function (response) {
			var totalAmount = response.iTotalAmount;

			var grandTotal = totalAmount.grandTotal ?? 0;
			var totalAmountCurrentPage = totalAmount.totalAmount ?? 0;

			$('#searchProfileAdjustmentRecordTable').find("[name='grandTotal']").html('<div style="text-align: right;">' + CurrencyUtil.formatter(grandTotal) + '</div>');
			$('#searchProfileAdjustmentRecordTable').find("[name='subTotal']").html('<div style="text-align: right;">' + CurrencyUtil.formatter(totalAmountCurrentPage) + '</div>');
		};

		var isEnableSequence = true;
		dataTableOptions[tabId].dataTableRef = DataTableHandler.create(dataTableOptions[tabId], isEnableSequence, PageConfig.lang, callBack);
		$(dataTableOptions[tabId].tableSelector).on('order.dt', function () {
			const order = dataTableOptions[tabId].dataTableRef.order();
			dataTableOptions[tabId].aaSorting = order;
		});

		$('#searchProfileAdjustmentRecordTable').show();
	}

	let formatCardNumber = function (cardNumber, chunkSize = 4, separator = ' ') {
		return cardNumber.replace(new RegExp(`(.{${chunkSize}})`, 'g'), `$1${separator}`).trim();
	}

	ReportHandler.onChangeSeparatorAdvanced = function (input, options = {}) {
		const config = {
			allowNegative: true,        // 允许负数
			minValue: -Infinity,        // 🔧 修复：改为 -Infinity 以支持负数
			maxValue: Infinity,
			decimalPlaces: 2,
			thousandSeparator: ',',
			decimalSeparator: '.',
			defaultValue: '0.00',
			autoAddDecimals: true,
			...options
		};

		const originalCursorPos = input.selectionStart;
		const originalValue = input.value;

		// 🔧 修复：改进负号处理逻辑
		let cleanValue = input.value;

		if (cleanValue === '') {
			input.value = null;
			return;
		}

		// 先检查是否有负号，并记住位置
		const hasNegativeAtStart = config.allowNegative && cleanValue.startsWith('-');

		// 构建清理正则表达式 - 移除所有非数字、小数点、负号的字符
		const cleanRegex = new RegExp(`[^\\d${config.decimalSeparator.replace('.', '\\.')}${config.allowNegative ? '-' : ''}]`, 'g');
		cleanValue = cleanValue.replace(cleanRegex, '');

		// 🔧 修复：更好的负号处理
		if (config.allowNegative) {
			// 移除所有负号
			const negativeCount = (cleanValue.match(/-/g) || []).length;
			cleanValue = cleanValue.replace(/-/g, '');

			// 如果有奇数个负号，或者原始输入以负号开始，则为负数
			const shouldBeNegative = hasNegativeAtStart || (negativeCount % 2 === 1);
			if (shouldBeNegative && cleanValue !== '') {
				cleanValue = '-' + cleanValue;
			}
		} else {
			// 不允许负数时移除所有负号
			cleanValue = cleanValue.replace(/-/g, '');
		}

		// 处理多个小数点
		const decimalMatches = cleanValue.match(new RegExp(`\\${config.decimalSeparator.replace('.', '\\.')}`, 'g'));
		if (decimalMatches && decimalMatches.length > 1) {
			const firstDotIndex = cleanValue.indexOf(config.decimalSeparator);
			cleanValue = cleanValue.substring(0, firstDotIndex + 1) +
				cleanValue.substring(firstDotIndex + 1).replace(new RegExp(`\\${config.decimalSeparator.replace('.', '\\.')}`, 'g'), '');
		}

		// 验证和处理数值
		if (!cleanValue || cleanValue === '-') {
			input.value = config.defaultValue;
			return;
		}

		const num = parseFloat(cleanValue);
		if (isNaN(num)) {
			input.value = config.defaultValue;
			return;
		}

		// 🔧 修复：正确的范围检查，支持负数
		if (num < config.minValue || num > config.maxValue) {
			const boundedValue = Math.max(config.minValue, Math.min(config.maxValue, num));
			cleanValue = boundedValue.toString();
		}

		// 🔧 修复：改进的格式化逻辑
		const isNegative = parseFloat(cleanValue) < 0;
		const absoluteValue = Math.abs(parseFloat(cleanValue)).toString();

		// 分离整数和小数部分
		const parts = absoluteValue.split('.');
		let integerPart = parts[0];
		let decimalPart = parts[1];

		// 处理小数位数
		if (config.decimalPlaces > 0) {
			if (decimalPart) {
				decimalPart = decimalPart.substring(0, config.decimalPlaces);
			} else if (config.autoAddDecimals) {
				decimalPart = '0'.repeat(config.decimalPlaces);
			}
		}

		// 添加千位分隔符到整数部分
		const formattedInteger = integerPart.replace(/\B(?=(\d{3})+(?!\d))/g, config.thousandSeparator);

		// 🔧 修复：正确组合负号、整数和小数部分
		let finalValue = (isNegative ? '-' : '') + formattedInteger;
		if (config.decimalPlaces > 0 && decimalPart !== undefined) {
			finalValue += config.decimalSeparator + decimalPart;
		}

		input.value = finalValue;

		// 恢复光标位置
		const newCursorPos = calculateCursorPosition(originalValue, finalValue, originalCursorPos);
		setTimeout(() => {
			input.setSelectionRange(newCursorPos, newCursorPos);
		}, 0);
	};

	function calculateCursorPosition(oldValue, newValue, oldPos) {
		// 计算光标前的字符变化
		let oldPrefix = oldValue.substring(0, oldPos);
		let newPrefix = '';

		// 尝试找到对应的位置
		let charCount = 0;
		for (let i = 0; i < newValue.length; i++) {
			const char = newValue[i];
			if (char !== ',' && char !== ' ') { // 忽略分隔符
				charCount++;
			}

			// 计算原字符串中非分隔符字符数
			const oldCharCount = oldPrefix.replace(/[,\s]/g, '').length;

			if (charCount >= oldCharCount) {
				newPrefix = newValue.substring(0, i + 1);
				break;
			}
		}

		return Math.min(newPrefix.length, newValue.length);
	}

	ReportHandler.onKeyDown = function (event) {
		const input = event.target;
		const currentValue = input.value;
		const selectionStart = input.selectionStart || 0;
		const selectionEnd = input.selectionEnd || 0;

		const allowedKeys = [
			'Backspace',
			'Delete',
			'Tab',
			'Escape',
			'Enter',
			'ArrowLeft',
			'ArrowRight',
			'ArrowUp',
			'ArrowDown',
			'Home',
			'End',
		];

		// Allow navigation and control keys
		if (allowedKeys.includes(event.key)) {
			return;
		}

		// Allow Ctrl+A, Ctrl+C, Ctrl+V, Ctrl+X, Ctrl+Z
		if (
			event.ctrlKey &&
			['a', 'c', 'v', 'x', 'z'].includes(event.key.toLowerCase())
		) {
			return;
		}

		// Allow digits
		if (/^\d$/.test(event.key)) {
			// Check if we're adding digits after decimal point
			const decimalIndex = currentValue.indexOf('.');
			if (decimalIndex !== -1) {
				// If cursor is after decimal point
				if (selectionStart > decimalIndex) {
					const digitsAfterDecimal = currentValue.slice(decimalIndex + 1);
					// Block if already have 2 digits after decimal and no text is selected
					if (digitsAfterDecimal.length >= 2 && selectionStart === selectionEnd) {
						event.preventDefault();
						return;
					}
				}
			}
			return;
		}

		// Allow decimal point
		if (event.key === '.') {
			// Block if decimal point already exists
			if (currentValue.indexOf('.') !== -1) {
				event.preventDefault();
				return;
			}
			return;
		}

		// Block all other keys
		event.preventDefault();
	};

	function formatAccountNumber(accountNumber) {
		return accountNumber
			.replace(/\s/g, '')
			.replace(/(.{4})/g, '$1 ')
			.trim();
	}

})();




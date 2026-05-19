if (typeof (BetReportHandler) == 'undefined') {
	BetReportHandler = {};
}

(function() {

	var currentTab = 'SETTLED';

	function errorPlacementForAmountValidation(error, element) {
		var errorContainer = element.closest('.form-group').find(".error-msg-amount-validation");
		if (errorContainer.length > 0) {
			errorContainer.html(error);
			errorContainer.show();
		} else {
			error.insertAfter(element);
		}
	}

	BetReportHandler.init = function() {
		dateRangeInitial();
		DateRangeHandler.bindEvent();
	};

	BetReportHandler.initValidator = function() {
		const form = $("[name='searchForm']");
		form.removeData('validator');

		form.validate({
			rules: {
				minAmount: {
					number: false,
					minMaxAmountChecking: true
				},
				maxAmount: {
					number: false,
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
			},
			ignore: ':hidden',
			errorPlacement: errorPlacementForAmountValidation
		});
	};

	BetReportHandler.switchTab = function(tab) {
		if (currentTab === tab) return;
		currentTab = tab;

		// Update tab active state
		$('#betStatusTabs li').removeClass('active');
		$('#betTab-' + tab).addClass('active');

		// Update txnStatus hidden field
		$('#txnStatus').val(tab);

		// Show/hide Win/Loss Amount Range
		if (tab === 'SETTLED') {
			$('#winLossAmountRangeContainer').show();
		} else {
			$('#winLossAmountRangeContainer').hide();
			$('#minAmount').val('');
			$('#maxAmount').val('');
		}

		if (tab === 'SETTLED') {
			$('#settledTableWrapper').show();
			$('#unsettledTableWrapper').hide();
		} else {
			$('#settledTableWrapper').hide();
			$('#unsettledTableWrapper').show();
		}

		$('#resultContainer').hide();

		// Clear validation and re-run validator so :hidden ignore applies correctly
		$('#searchForm').validate().resetForm();
		$('.error-msg-amount-validation').empty().hide();
		BetReportHandler.initValidator();
	};

	var dateRangeInitial = function() {

		var dateOption = {
			"singleDatePicker": false,
			"timePicker": true,
			timePicker24Hour: false,
			autoUpdateInput: true,
			autoApply: true,
			locale: DateRangeHandler.changeLanguage('DD/MM/YYYY HH:mm:ss', PageConfig.lang),
			"maxDate": PageConfig.date.today,
			"minDate": PageConfig.date.todayHalfYearAgo,
			startDate: PageConfig.date.todayOneMonthAgo,
			endDate: PageConfig.date.today
		};

		$('#transactionDaterange').daterangepicker(dateOption);
		$('#transactionDaterange').val(PageConfig.date.todayOneMonthAgo + " - " + PageConfig.date.today);

		$('#transactionDaterange').on('apply.daterangepicker', function(e, picker) {
			var startDate = moment(picker.startDate);
			startDate.set({second: 0});
			var endDate = moment(picker.endDate);
			endDate.set({second: 59});
			picker.setEndDate(endDate);
			this.val(String(startDate.format('DD/MM/YYYY HH:mm:ss')) + '-' + String(endDate.format('DD/MM/YYYY HH:mm:ss')));
		});

		$('#transactionDaterange').on('cancel.daterangepicker', function(e, picker) {
			$(this).val('');
			this.val("");
		});

		$('.fa-calendar').hide();
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

	// ====================================================
	// 修复版本：完全支持负数的千位分隔符函数
	// ====================================================

	BetReportHandler.onChangeSeparatorAdvanced = function(input, options = {}) {
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

	BetReportHandler.openGameTxnData = function(txnId, txnTime, txnStatus, settleTime, userId) {
		var url = "/page/manager/member/subGameTransaction.jsp?gameTxnId=" + txnId +
			"&txnTime=" + txnTime +
			"&settleTime=" + settleTime +
			"&txnStatus=" + txnStatus +
			"&currency=" + PageConfig.currency +
			"&userId=" + userId;

		var panel = window.open(url,
			"_blank",
			"width=1200,height=640,top=5,left=5,toolbar=no,menubar=no,resizable=no,scrollbars=yes,status=no,location=no,directories=no");

		if (panel) {
			panel.focus();
		}
	};

	BetReportHandler.openProfile = function(userId) {
		var panel = window.open("/page/manager/member/profile.jsp?userId=" + encodeURIComponent(userId) + "&currency=" + PageConfig.currency, "_winPROFILE", "width=1200,height=640,top=5,left=5," +
			"toolbar=no,menubar=no,resizable=no,scrollbars=yes,status=no,location=no,directories=no");
		if (panel) {
			panel.focus();
		}
	};

	//============================================================
	// Pageable Bet report
	//============================================================

	var dataTableOptionsSettled;
var dataTableOptionsUnsettled;

	const escapeHtml = (str) => {
		if (!str) return '';
		const div = document.createElement('div');
		div.textContent = str;
		return div.innerHTML;
	};

	const escapeJs = (str) => {
		if (!str) return '';
		return String(str)
			.replace(/\\/g, '\\\\')
			.replace(/'/g, "\\'")
			.replace(/"/g, '\\"')
			.replace(/\n/g, '\\n')
			.replace(/\r/g, '\\r')
			.replace(/\t/g, '\\t')
			.replace(/</g, '\\x3c')
			.replace(/>/g, '\\x3e')
			.replace(/\u2028/g, '\\u2028')
			.replace(/\u2029/g, '\\u2029');
	};

	BetReportHandler.searchReport = function() {

		var isSettled = currentTab === 'SETTLED';

		if (isSettled && !dataTableOptionsSettled) {
			dataTableOptionsSettled = {
				tableSelector: '#betReportTableSettled',
				formSelector: "[name='searchForm']",
				sAjaxSource: '/manager/member/searchBetReport',
				aaSorting: [[2, 'desc', 1]],
				iDisplayLength: PageConfig.pageSize,
				showAllColumn: true,
				aoColumns: [
					{
						"mData": null,
						"bSortable": false
					},
					{
						"mData": "userId",
						"mRender": function(data, type, full) {
							if (type === "display") {
								return `<a style='text-decoration: underline;' id='userId' onclick='BetReportHandler.openProfile("${escapeJs(data)}");' href='javascript:void(0);'>${escapeHtml(data) || '-'}</a>`;
							}
							return data || "-";
						},
						"bSortable": false
					},
					{
						"mData": "transactionTime",
						"mRender": function(data, type, full) {
							if (type === 'display') {
								return data ? DateUtil.format(data, "dd/MM/YYYY HH:mm:ss") : "-";
							}
							return data;
						}
					},
					{
						"mData": "settleTime",
						"mRender": function(data, type, full) {
							if (type === 'display') {
								return data ? DateUtil.format(data, "dd/MM/YYYY HH:mm:ss") : "-";
							}
							return data;
						}
					},
					{
						"mData": "createTime",
						"mRender": function(data, type, full) {
							if (type === 'display') {
								return data ? DateUtil.format(data, "dd/MM/YYYY HH:mm:ss") : "-";
							}
							return data;
						}
					},
					{
						"mData": "vendorName",
						"mRender": function(data, type, full) {
							if (type === 'display') {
								return data || "-";
							}
							return data;
						}
					},
					{
						"mData": "gameType",
						"mRender": function(data, type, full) {
							if (type === 'display') {
								return data || "-";
							}
							return data;
						}
					},
					{
						"mData": "gameName",
						"mRender": function(data, type, full) {
							if (type === 'display') {
								return data || "-";
							}
							return data;
						}
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
						"mData": "profitLoss",
						"mRender": function(data, type, full) {
							if (type === 'display') {
								if (data === null || data === undefined
									|| parseInt(full.txnStatus) !== PageConfig.SystemTxnStatusType.SETTLED) {
									return "-";
								}
								return CurrencyUtil.formatter(data);
							}
							return data;
						}
					},
					{
						"mData": "turnover",
						"mRender": function(data, type, full) {
							if (type === 'display') {
								if (data === null || data === undefined
									|| parseInt(full.txnStatus) !== PageConfig.SystemTxnStatusType.SETTLED) {
									return "-";
								}
								return CurrencyUtil.formatter(data);
							}
							return data;
						}
					},
					{
						"mData": "id",
						"mRender": function(data, type, full) {
							if (type === 'display') {
								return `<button class="btn btn-sm" onclick="BetReportHandler.openGameTxnData('${escapeJs(full.id)}', '${escapeJs(full.transactionTime)}', '${escapeJs(full.transactionStatus)}', '${escapeJs(full.settleTime)}','${escapeJs(full.userId)}')"><i class="icon-search"></i></button>`;
							}
							return data;
						},
						"bSortable": false
					}
				],
				aoColumnDefs: [{"sClass": "dt-body-right", "aTargets": [8, 10, 11, 12]}]
			};
		} else if (!isSettled && !dataTableOptionsUnsettled) {
			dataTableOptionsUnsettled = {
				tableSelector: '#betReportTableUnsettled',
				formSelector: "[name='searchForm']",
				sAjaxSource: '/manager/member/searchBetReport',
				aaSorting: [[2, 'asc', 0]],
				iDisplayLength: PageConfig.pageSize,
				showAllColumn: true,
				aoColumns: [
					{
						"mData": null,
						"bSortable": false
					},
					{
						"mData": "userId",
						"mRender": function(data, type, full) {
							if (type === "display") {
								return `<a style='text-decoration: underline;' id='userId' onclick='BetReportHandler.openProfile("${escapeJs(data)}");' href='javascript:void(0);'>${escapeHtml(data) || '-'}</a>`;
							}
							return data || "-";
						},
						"bSortable": false
					},
					{
						"mData": "transactionTime",
						"mRender": function(data, type, full) {
							if (type === 'display') {
								return data ? DateUtil.format(data, "dd/MM/YYYY HH:mm:ss") : "-";
							}
							return data;
						}
					},
					{
						"mData": "createTime",
						"mRender": function(data, type, full) {
							if (type === 'display') {
								return data ? DateUtil.format(data, "dd/MM/YYYY HH:mm:ss") : "-";
							}
							return data;
						}
					},
					{
						"mData": "vendorName",
						"mRender": function(data, type, full) {
							if (type === 'display') {
								return data || "-";
							}
							return data;
						}
					},
					{
						"mData": "gameType",
						"mRender": function(data, type, full) {
							if (type === 'display') {
								return data || "-";
							}
							return data;
						}
					},
					{
						"mData": "gameName",
						"mRender": function(data, type, full) {
							if (type === 'display') {
								return data || "-";
							}
							return data;
						}
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
						"mData": "id",
						"mRender": function(data, type, full) {
							if (type === 'display') {
								return `<button class="btn btn-sm" onclick="BetReportHandler.openGameTxnData('${escapeJs(full.id)}', '${escapeJs(full.transactionTime)}', '${escapeJs(full.transactionStatus)}', '${escapeJs(full.settleTime)}','${escapeJs(full.userId)}')"><i class="icon-search"></i></button>`;
							}
							return data;
						},
						"bSortable": false
					}
				],
				aoColumnDefs: [{"sClass": "dt-body-right", "aTargets": [7, 9]}]
			};
		}

		var dataTableOptions = isSettled ? dataTableOptionsSettled : dataTableOptionsUnsettled;

		var searchForm = $("[name='searchForm']");
		if (!searchForm.data("validator")) {
			searchForm.validate({
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
				},
				ignore: ':hidden',
				errorPlacement: errorPlacementForAmountValidation
			});
		}

		var tableSelector = dataTableOptions.tableSelector;
		var callBack = function(response) {
			var subtotals = response.subTotals;
			var grandTotals = response.grandTotals;

			$(tableSelector).find("[name='subTotalBetAmount']").html('<div class="text-right">' + CurrencyUtil.formatter(subtotals.betAmount) + '</div>');
			$(tableSelector).find("[name='grandTotalBetAmount']").html('<div class="text-right">' + CurrencyUtil.formatter(grandTotals.betAmount) + '</div>');

			if (isSettled) {
				$(tableSelector).find("[name='subTotalProfitLoss']").html('<div class="text-right">' + CurrencyUtil.formatter(subtotals.profitLoss) + '</div>');
				$(tableSelector).find("[name='subTotalTurnover']").html('<div class="text-right">' + CurrencyUtil.formatter(subtotals.turnover) + '</div>');
				$(tableSelector).find("[name='grandTotalProfitLoss']").html('<div class="text-right">' + CurrencyUtil.formatter(grandTotals.profitLoss) + '</div>');
				$(tableSelector).find("[name='grandTotalTurnover']").html('<div class="text-right">' + CurrencyUtil.formatter(grandTotals.turnover) + '</div>');
			}
		};

		dataTableOptions.dataTableRef = DataTableHandler.create(dataTableOptions, true, PageConfig.lang, callBack);

		$(dataTableOptions.tableSelector)
			.off('order.dt')
			.on('order.dt', function() {
				const order = dataTableOptions.dataTableRef.order();
				dataTableOptions.aaSorting = order;
			});

		$('#resultContainer').show();
	};
})();

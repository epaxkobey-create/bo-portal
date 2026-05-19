if (typeof (DepositHandler) == 'undefined') {
	DepositHandler = {};
}

(function () {

	let excludeColumn = [0, 9];
	DepositHandler.init = function () {
		CurrencyUtil.updateSetting({
			"precision": 2,
			"trailingZeros": true,
			"roundingMode": CurrencyUtil.roundingMode.HALF_UP
		});

		ReportUtil.setDefPaymentConditionOfTimeRange('createDateStart', 'createDateEnd');
		ReportUtil.validationMaxDate('createTimeEnd', PageConfig.date.today);
		ReportUtil.validationMinDate('createTimeStart', PageConfig.date.todayHalfYearAgo);
		ReportUtil.validationMaxDate('approvedDateEnd', PageConfig.date.today);
		ReportUtil.validationMinDate('approvedDateStart', PageConfig.date.todayHalfYearAgo);

		bindDate();
		bindCreateDepositEvent();
		bindUnlockDepositEvent();

		if (!PageConfig.showTransferType) {
			excludeColumn.push(13);
		}

		$("#checkAll").click(function () {
			$('input:checkbox[name=depositId]').not(this).prop('checked', this.checked);
			$.uniform.update();
		});
	};


	let bindDate = function () {
		$('.singleDateTimePicker').each2(function () {
			var $input = $(this);
			var id = $input.attr('id');

			var dateOptions = {
				"maxDate": PageConfig.date.today
			}

			DateRangeHandler.singleDateTimePicker(dateOptions, PageConfig.lang);
			if (id === "createdSince") {
				$input.val(PageConfig.date.todayOneMonthAgo);
				$input.parent().find('[name="createdSince"]').val(
					PageConfig.date.todayOneMonthAgo
				);
			}

			DateRangeHandler.bindEvent();
		});
	}

	var dataTableOptions;
	DepositHandler.search = function () {

		const form = $("[name='searchForm']");
		if (!form.data("validator")) {
			form.validate({
				rules: {
					minAmount: {
						minMaxAmountChecking: true
					},
					maxAmount: {
						minMaxAmountChecking: true
					},
					transactionId: {
						maxlength: 10,
						pattern: '^D(\\d{8,9})$'
					}
				},
				messages: {
					minAmount: {
						minMaxAmountChecking: "The Min amount must not be greater than the Max amount."
					},
					maxAmount: {
						minMaxAmountChecking: "The Min amount must not be greater than the Max amount."
					},
					transactionId: {
						pattern: I18N.get('msg.error.validation.transactionIdIsInvalid')
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
			})
		}


		console.log(!dataTableOptions)
		if (!dataTableOptions) {
			dataTableOptions = {
				tableSelector: '#depositSearchTable',
				formSelector: "[name='searchForm']",//optional
				sAjaxSource: '/manager/payment/searchDeposit',
				aaSorting: [[6, "desc"]],
				iDisplayLength: PageConfig.pageSize,
				showAllColumn: true,
				aoColumns: [
					{
						"mData": "transactionId",
						"mRender": function (data, type, full) {
							if (type === 'display') {
								if (full.status === 0) {
									return `<label class="checkbox-inline"><input type="checkBox" class="uniform" name="depositId" value="${data}"/></label>`
								}
							}
							return "";
						},
						"bSortable": false,
					},
					{
						"mData": "email",
						"mRender": function (data, type, full) {
							if (type === 'display') {
								return "<a style='text-decoration: underline;' id='email' onclick='DepositHandler.openProfile(this);' href='javascript: void(0);'>" + data + "</a>"
									+ "<input type='hidden' id='currency' value='" + full?.currencyTypeId + "'>";
							}
							return data;
						},
					},
					{
						"mData": "transactionId",
						"mRender": function (data, type, full) {
							if (type === 'display') {
								return "<a style='text-decoration: underline;' data-id='" + data + "' onclick='DepositHandler.gotoDetail(this);' href='javascript: void(0);'>" + data + "</a>";
							}
							return data;
						},
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
					// {
					// 	"mData": null,
					// 	"mRender": function(data, type, full) {
					//
					// 		const label = `<button data-id='${full?.transactionId}' onclick='DepositHandler.gotoDetail(this);'><i class="icos-settings-2"></i></button>`
					// 		return label;
					//
					// 	},
					// 	"bSortable": false,
					// }

				],
				aoColumnDefs: [{"sClass": "dt-body-right", "aTargets": [3]}]
			};
		}

		// var searchForm = $("[name='searchForm']");
		// if (!searchForm.data("validator")) {
		// 	searchForm.validate();
		// 	// searchForm.find("[name=userId]").rules("add", {maxlength: 15, userId: true});
		//
		// 	searchForm.find("[name=transactionId]").rules("add", {
		// 		maxlength: 10,
		// 		pattern: '^D(\\d{8,9})$',
		// 		messages: {
		// 			pattern: I18N.get('msg.error.validation.transactionIdIsInvalid')
		// 		}
		// 	});
		// }


		var callBack = function () {

			let selector = dataTableOptions.tableSelector;
			let datatableToolbars = [];
			if ($('#batchApproveButton').size()) {
				datatableToolbars.push($('#batchApproveButton').clone());
			}

			if (datatableToolbars.length > 0) {
				DataTableHandler.addMultiToolBar(selector, datatableToolbars);
			}

			const buttonGroup = dataTableOptions.tableSelector + '_btn';
			$(selector).find('input:checkbox').click(function () {
				$(buttonGroup).find('[name=batchApprove]').hide();
				if ($('input:checkbox[name=depositId]:checked').not("#checkAll").length > 0) {
					$(buttonGroup).find('[name=batchApprove]').show();
				}
			});

			if ($('#checkAll').prop('checked')) {
				$('#checkAll').prop('checked', false);
				$('#checkAll').uniform();
			}
			$('[data-toggle="tooltip"]').tooltip();

		}

		var isEnableSequence = false;
		dataTableOptions.dataTableRef = DataTableHandler.create(dataTableOptions, isEnableSequence, PageConfig.lang, callBack);
		$('#resultTable').show();
	};

	DepositHandler.export = function () {

		const visibleColumns = ColVisHandler.getVisibleColumns(dataTableOptions.dataTableRef);

		const searchForm = $("[name='searchForm']");

		var createDateIsRequire = false;
		if (searchForm.find("[name=createDateStart]").val() || searchForm.find("[name=createDateEnd]").val()) {
			createDateIsRequire = true;
		}
		searchForm.find("[name=createDateStart]").rules("add", {required: createDateIsRequire});
		searchForm.find("[name=createDateEnd]").rules("add", {required: createDateIsRequire});

		var approvedDateIsRequire = false;
		if (searchForm.find("[name=approvedDateStart]").val() || searchForm.find("[name=approvedDateEnd]").val()) {
			approvedDateIsRequire = true;
		}
		searchForm.find("[name=approvedDateStart]").rules("add", {required: approvedDateIsRequire});
		searchForm.find("[name=approvedDateEnd]").rules("add", {required: approvedDateIsRequire});

		if (!searchForm.valid()) {
			return;
		}

		searchForm.find('[name=visibleColumns]').val(visibleColumns);
		//searchForm.find('[name=extension]').val($('[name=exportType]:checked').val());

		ExcelUtils.schedule(ReportExportType.DEPOSIT, '/manager/payment/exportDeposit', searchForm.serialize(), $('[name=export]'));

		//ExcelUtils.openExportType(callBack);
	};

	DepositHandler.goToBatchApprove = function () {

		var batchDeposit = [];
		$('input:checkbox[name=depositId]:checked').each(function () {
			var $this = $(this);
			var $currentRow = $this.parents('tr');
			batchDeposit.push($currentRow.find('[name=depositId]').val());
		});

		if (batchDeposit.length === 0) {
			return;
		}
		let model = $('#batchApproveDepositModal');
		model.modal("show");
		$.ajax({
			type: 'POST',
			url: '/manager/payment/getBatchMoneyTransactionDetails',
			data: {
				batchDeposit: JSON.stringify(batchDeposit)
			},
			dataType: 'JSON',
			success: function (data) {
				if (data.error) {
					NotifyHandler.errorMsg(data.error);
					return;
				}

				batchDepositCallBack(data?.aaData);

			},
		});
	};

	function batchDepositCallBack(data) {
		const $container = $('#batchDepositContainer');
		$container.empty();

		if (!data || data.length === 0) {
			// 显示无数据提示
			$container.html(`
                    <tr>
                        <td colspan="14" class="text-center">No data available</td>
                    </tr>
                `);
			return;
		}

		$.each(data, function (index, element) {
			console.log("element: ",element)
			const $row = $('<tr>');
			$row.append(`<td id="email_id">${element?.email}</td>`);
			$row.append(`<td id ="transaction_id_id">${element?.transactionId}</td>`);
			$row.append(`<td id="created_time_id">${moment(element.createdTime).format("DD/MM/YYYY HH:mm:ss")}</td>`);
			$row.append(`<td id="payment_method_id">${element?.paymentMethod ? I18N.get(PageConfig.paymentTypeJson[element.paymentMethod].getFullName) : "-"}</td>`);

			$row.append(`<td id="bank_id">${element.bank}</td>`);
			$row.append(`<td id="card_number_id">${formatCardNumber(element.cardNumber)}</td>`);
			$row.append(`<td id="amoun_id">${CurrencyUtil.formatter(element?.amount)}</td>`);
			$container.append($row);
		});
	}

	DepositHandler.openProfile = function (pElement) {
		var element = $(pElement).closest('tr');
		var panel = window.open("/page/manager/member/profile.jsp?userId=" + element.find('#email').text() + "&currency=" + element.find('#currency').val(), "_winPROFILE", "width=1200,height=640,top=5,left=5," +
			"toolbar=no,menubar=no,resizable=no,scrollbars=yes,status=no,location=no,directories=no");
		if (panel) {
			panel.focus();
		}
	};

	var bindCreateDepositEvent = function () {
		$('#createDepositModal').on('shown.bs.modal', function () {
			$('#createDepositModal').find('[name=resetButton]').show();
			if (DepositHandler.cacheData && Object.keys(DepositHandler.cacheData).length > 0) {
				if (DepositHandler.cacheData.oldDeposit !== undefined) {
					if (DepositHandler.cacheData.oldDeposit.userId == DepositHandler.cacheData.oldDeposit.creator) {
						$('#createDepositModal').find('[name=resetButton]').hide();
					}
				}
			}
		});
		$('#createDepositModal').on('hidden.bs.modal', function (e) {
			DepositHandler.resetCreateDeposit();
		});

		$("#closeErrorList").on("click", function () {
			$("div[name='errorList']").hide();
		});

		$("[name='batchCreateDepositForm']").find('.browse').on("click", function () {
			$("div[name='errorList']").hide();
			$("[name='errorList']").hide();
			$('#checkResultStatus').empty();
			$("div[name='errorTips'] .col-md-12").hide();
			$("div.modal-footer[name='body-batchCreateDeposit']").hide();
		});

	};

	var bindUnlockDepositEvent = function () {
		$('#approveDepositModal').on('hidden.bs.modal', function (e) {
			var approveDepositForm = $("[name='approveDepositForm']");
			approveDepositForm.find(".remove").trigger('click');
			var operation = approveDepositForm.find("[name='operation']").val();
			var transactionId = approveDepositForm.find("[name='transactionId']").val();
			$.ajax({
				type: "POST",
//				url : '/manager/payment/unlockLocalBankDeposit',
				url: '/manager/payment/unlockDeposit',
				dataType: 'JSON',
				data: {
					transactionId: transactionId
				},
				success: function (data) {
					if (data.error) {
						NotifyHandler.errorMsg(data.error);
						return;
					}
					dataTableOptions.dataTableRef.fnDraw(dataTableOptions.dataTableRef.fnSettings());
				}
			});
		});
		$('#batchApproveDepositModal').on('hidden.bs.modal', function (e) {
			let batchApproveDepositForm = $('#batchApproveDepositModal').find("[name='" + currentBatchApprovedTab + "']").find('.form-horizontal');
			batchApproveDepositForm.find(".remove").trigger('click');
			var transactionIds = batchApproveDepositForm.find("input[name='transactionId']").val();
			$.ajax({
				type: "POST",
				url: '/manager/payment/batchUnlockDeposit',
				dataType: 'JSON',
				data: {
					transactionId: transactionIds
				},
				success: function (data) {
					if (data.error) {
						NotifyHandler.errorMsg(data.error);
						return;
					}
				}
			});
		});
		$('#approvePGDepositModal').on('hidden.bs.modal', function (e) {
			var transactionId = $('#approvePGDepositModal').find("[name='transactionId']").val();
			$.ajax({
				type: "POST",
//				url : '/manager/payment/unlockLocalBankDeposit',
				url: '/manager/payment/unlockDeposit',
				dataType: 'JSON',
				data: {
					transactionId: transactionId
				},
				success: function (data) {
					if (data.error) {
						NotifyHandler.errorMsg(data.error);
						return;
					}
					dataTableOptions.dataTableRef.fnDraw(dataTableOptions.dataTableRef.fnSettings());
				}
			});
		});

		$('#externalMessageModal').on('hidden.bs.modal', function (e) {
			var transactionId = $('#externalMessageModal').find("[name='transactionId']").val();
			$.ajax({
				type: "POST",
//				url : '/manager/payment/unlockLocalBankDeposit',
				url: '/manager/payment/unlockDeposit',
				dataType: 'JSON',
				data: {
					transactionId: transactionId
				},
				success: function (data) {
					if (data.error) {
						NotifyHandler.errorMsg(data.error);
						return;
					}
					dataTableOptions.dataTableRef.fnDraw(dataTableOptions.dataTableRef.fnSettings());
				}
			});
		});
	};

	DepositHandler.batchApproveReject = function () {
		const container = $("#batchDepositContainer")
		const transactionIds = [];

		container.find('tr').each(function () {
			const transactionId = $(this).find('#transaction_id_id').text().trim();
			if (transactionId && transactionId !== 'No data available') {
				transactionIds.push(transactionId);
			}
		});
		toDoBatchApproveReject(transactionIds, function () {
			$('#batchApproveDepositModal').modal('hide');
			dataTableOptions.dataTableRef.fnDraw(dataTableOptions.dataTableRef.fnSettings());
		});
	}

	DepositHandler.singleRejectDeposit = function (ids) {
		const array = [ids]
		toDoBatchApproveReject(array, function () {
			$("#detailModal").modal('hide');
			dataTableOptions.dataTableRef.fnDraw(dataTableOptions.dataTableRef.fnSettings());
		});

		console.log(array)

	}

	DepositHandler.singleApproveDeposit = function (ids) {
		const array = [ids]
		toDoBatchApproveDeposit(array, function () {
			$("#detailModal").modal('hide');
			dataTableOptions.dataTableRef.fnDraw(dataTableOptions.dataTableRef.fnSettings());
		});

		console.log(array)

	}

	function toDoBatchApproveReject(transactionIds, callback) {
		$.ajax({
			type: "POST",
			url: '/manager/payment/disapproveDeposit',
			dataType: 'JSON',
			data: {
				batchDeposit: JSON.stringify(transactionIds)
			},
			success: function (data) {
				if (data.error) {
					NotifyHandler.errorMsg(data.error);
					return;
				}
				NotifyHandler.successMsg(data.message);
				callback();
			}
		});
	}

	function toDoBatchApproveDeposit(transactionIds, callback) {
		$.ajax({
			type: "POST",
			url: '/manager/payment/approveDeposit',
			dataType: 'JSON',
			data: {
				batchDeposit: JSON.stringify(transactionIds)
			},
			success: function (data) {
				if (data.error) {
					NotifyHandler.errorMsg(data.error);
					return;
				}
				NotifyHandler.successMsg(data.message);
				callback();
			}
		});
	}


	DepositHandler.batchApproveDeposit = function () {
		const container = $("#batchDepositContainer")
		const transactionIds = [];

		container.find('tr').each(function () {
			const transactionId = $(this).find('#transaction_id_id').text().trim();
			if (transactionId && transactionId !== 'No data available') {
				transactionIds.push(transactionId);
			}
		});
		toDoBatchApproveDeposit(transactionIds, function () {
			$('#batchApproveDepositModal').modal('hide');
			dataTableOptions.dataTableRef.fnDraw(dataTableOptions.dataTableRef.fnSettings());
		});
	}

	DepositHandler.cacheData = {};

	DepositHandler.bonusExpired = true;

	DepositHandler.gotoDetail = function (element) {
		var transactionId = $(element).data('id');

		$.ajax({
			type: "GET",
			url: '/manager/payment/getMoneyTransactionDetail',
			data: {
				transactionId: transactionId
			},
			dataType: 'JSON',
			success: function (data) {
				if (data.error) {
					NotifyHandler.errorMsg(data.error);
					return;
				}
				var detailModal = $('#detailModal');
				var element = detailModal.children().detach();
				element.find("[name='transactionId']").text(transactionId)
				element.find("[name='email']").text(data.email);
				element.find("[name='createdTime']").text(moment(data.createTime).format("DD/MM/YYYY HH:mm:ss"));
				element.find("[name='amount']").html(PageConfig.managerCurrencySymbol + " " + CurrencyUtil.formatter(data.amount));

				var depositStatus = PageConfig.MoneyTransactionStatusType[data.status];
				if (depositStatus) {
					const statusLabel = '<span class="label ' + depositStatus.css + '">' + I18N.get(depositStatus.getFullName) + '</span>';
					element.find("[name='status']").html(statusLabel);
				} else {
					element.find("[name='status']").html("-");
				}
				if (data.updateTime) {
					element.find("[name='updatedTime']").text(moment(data.updateTime).format("DD/MM/YYYY HH:mm:ss"));
				} else {
					element.find("[name='updatedTime']").text("-");
				}


				element.find("[name='paymentMethod']").text(data?.paymentType ? I18N.get(PageConfig.paymentTypeJson[data.paymentType].getFullName) : "-");

				element.find("[name='bank']").text(data?.fromBankName ?? "-");

				element.find("[name='cardNumber']").text(data?.fromBankNumber ? formatCardNumber(data.fromBankNumber) : "-");

				if (data?.status === 0) {
					const buttonHtml = `
       				 <div class="row" style="display: flex; justify-content: flex-end; margin-top: 20px; padding-right: 10px;">
            			<button class="btn btn-primary" style="margin-right: 10px;" onclick="DepositHandler.singleRejectDeposit('${transactionId}')">Disapprove</button>
            			<button class="btn btn-primary" onclick="DepositHandler.singleApproveDeposit('${transactionId}')">Approve</button>
        			</div>`


					element.find("#buttonsPlaceHolder").html(buttonHtml);
				} else {
					element.find("#buttonsPlaceHolder").empty();
				}


				detailModal.append(element);

				detailModal.modal("show");
			}
		});
	};

	DepositHandler.closeDetail = function () {
		var detailModal = $('#detailModal');
		detailModal.find('.showImage').text(I18N.get('form.text.backOffice.show'));
		detailModal.find('.attachmentBtnGroup').hide();
		detailModal.find('[name^=preImage_]').attr('src', '').hide();
		detailModal.modal('hide');
	}

	let formatCardNumber = function (cardNumber, chunkSize = 4, separator = ' ') {
		return cardNumber.replace(new RegExp(`(.{${chunkSize}})`, 'g'), `$1${separator}`).trim();
	}

	DepositHandler.onChangeSeparatorAdvanced = function (input, options = {}) {
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

	DepositHandler.onKeyDown = function (event) {
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
})();



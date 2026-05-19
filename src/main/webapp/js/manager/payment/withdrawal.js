if (typeof (WithdrawalHandler) == 'undefined') {
	WithdrawalHandler = {};
}

(function () {
	let defaultConditions;

	let excludeColumn = [0, 26];
	WithdrawalHandler.init = function () {
		CurrencyUtil.updateSetting({
			"precision": 2,
			"trailingZeros": true,
			"roundingMode": CurrencyUtil.roundingMode.HALF_UP
		});

		ReportUtil.setDefPaymentConditionOfTimeRange('createDateStart', 'createDateEnd');
		ReportUtil.validationMaxDate('createTimeEnd', PageConfig.date.today);
		ReportUtil.validationMinDate('createTimeStart', PageConfig.date.todayHalfYearAgo);
		ReportUtil.validationMaxDate('verifyDateEnd', PageConfig.date.today);
		ReportUtil.validationMinDate('verifyDateStart', PageConfig.date.todayHalfYearAgo);
		ReportUtil.validationMaxDate('approvedDateEnd', PageConfig.date.today);
		ReportUtil.validationMinDate('approvedDateStart', PageConfig.date.todayHalfYearAgo);

		bindDate();
		iniSelect2UserId();
		if (!PageConfig.allowCustomerFee) {
			excludeColumn.push(22);
			excludeColumn.push(24);
		}

		$("#checkAll").click(function () {
			$('input:checkbox[name=withdrawalId]').not(this).prop('checked', this.checked);
			$.uniform.update();
		});
	};

	function bindDate() {
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
	WithdrawalHandler.search = function () {
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
				},
				messages: {
					minAmount: {
						minMaxAmountChecking: "The Min amount must not be greater than the Max amount."
					},
					maxAmount: {
						minMaxAmountChecking: "The Min amount must not be greater than the Max amount."
					},

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

		if (!dataTableOptions) {
			dataTableOptions = {
				tableSelector: '#withdrawalSearchTable',
				formSelector: "[name='searchForm']",//optional
				sAjaxSource: '/manager/payment/searchWithdrawal',
				aaSorting: [[6, "desc"]],
				iDisplayLength: PageConfig.pageSize,
				showAllColumn: true,
				aoColumns: [
					{
						"mData": "transactionId",
						"mRender": function (data, type, full) {
							if (type === 'display') {
								if (full.status === 0) {
									return `<label class="checkbox-inline"><input type="checkBox" class="uniform" name="withdrawalId" value="${data}"/></label>`
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
								return "<a style='text-decoration: underline;' id='email' onclick='WithdrawalHandler.openProfile(this);' href='javascript: void(0);'>" + data + "</a>"
									+ "<input type='hidden' id='currency' value='" + full?.currencyTypeId + "'>";
							}
							return data;
						},
					},
					{
						"mData": "transactionId",
						"mRender": function (data, type, full) {
							if (type === 'display') {
								return "<a style='text-decoration: underline;' data-id='" + data + "' onclick='WithdrawalHandler.gotoDetail(this);' href='javascript: void(0);'>" + data + "</a>";
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
				],
				aoColumnDefs: [{"sClass": "dt-body-right", "aTargets": [3]}]
			};
		}

		var searchForm = $("[name='searchForm']");
		if (!searchForm.data("validator")) {
			searchForm.validate();
		}


		var callBack = function () {

			const tableSelector = dataTableOptions.tableSelector;
			let datatableToolbars = [];


			// datatableToolbars.push($('#exportButton').clone());


			datatableToolbars.push($('#batchApproveButton').clone());


			DataTableHandler.addMultiToolBar(tableSelector, datatableToolbars);
			const buttonGroup = dataTableOptions.tableSelector + '_btn';
			$(tableSelector).find('input:checkbox').click(function () {
				$(buttonGroup).find('[name=batchApprove]').hide();
				if ($('input:checkbox[name=withdrawalId]:checked').not("#checkAll").length > 0) {
					$(buttonGroup).find('[name=batchApprove]').show();
				}
			});

			if ($('#checkAll').prop('checked')) {
				$('#checkAll').prop('checked', false);
				$('#checkAll').uniform();
			}

			$('[name=searchForm]').find('[name=searchFromMenu]').val('false');
			$('[data-toggle="tooltip"]').tooltip();
		};

		var isEnableSequence = false;
		dataTableOptions.dataTableRef = DataTableHandler.create(dataTableOptions, isEnableSequence, PageConfig.lang, callBack);
		$('#resultTable').show();
	};

	WithdrawalHandler.export = function () {

		const visibleColumns = ColVisHandler.getVisibleColumns(dataTableOptions.dataTableRef);

		const searchForm = $("[name='searchForm']");

		var createDateIsRequire = false;
		if (searchForm.find("[name=createDateStart]").val() || searchForm.find("[name=createDateEnd]").val()) {
			createDateIsRequire = true;
		}
		searchForm.find("[name=createDateStart]").rules("add", {required: createDateIsRequire});
		searchForm.find("[name=createDateEnd]").rules("add", {required: createDateIsRequire});

		var verifyDateIsRequire = false;
		if (searchForm.find("[name=verifyDateStart]").val() || searchForm.find("[name=verifyDateEnd]").val()) {
			verifyDateIsRequire = true;
		}
		searchForm.find("[name=verifyDateStart]").rules("add", {required: verifyDateIsRequire});
		searchForm.find("[name=verifyDateEnd]").rules("add", {required: verifyDateIsRequire});

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

		ExcelUtils.schedule(ReportExportType.WITHDRAWAL, '/manager/payment/exportWithdrawal', searchForm.serialize(), $('[name=export]'));

		//ExcelUtils.openExportType(callBack);
	};

	WithdrawalHandler.openProfile = function (pElement) {
		var element = $(pElement).closest('tr');
		var panel = window.open("/page/manager/member/profile.jsp?userId=" + element.find('#userId').text() + "&currency=" + element.find('#currency').val(), "_winPROFILE", "width=1010,height=640,top=5,left=5," +
			"toolbar=no,menubar=no,resizable=no,scrollbars=yes,status=no,location=no,directories=no");
		if (panel) {
			panel.focus();
		}
	};

	// WithdrawalHandler.cacheVerifyWithdrawal = {};
	let verifyAction = false;


	let toDoBatchProcessWithdrawalModalUnLock = true;


	WithdrawalHandler.cacheData = {};

	WithdrawalHandler.gotoCreateWithdrawal = function () {
		var searchBalanceAndAccountBankForm = $("[name='searchBalanceAndAccountBankForm']");

		if (!searchBalanceAndAccountBankForm.data("validator")) {
			searchBalanceAndAccountBankForm.validate({ignore: []});

			searchBalanceAndAccountBankForm.find("[name=email]").rules("add", {
				required: true,
				maxlength: 50,
			});
		}
		searchBalanceAndAccountBankForm.get(0).reset();
		searchBalanceAndAccountBankForm.data("validator").resetForm();
		searchBalanceAndAccountBankForm.find('#emailSelect2').val(null).trigger('change'); // this will trigger $('#emailSelect2').change

		var balance = PageConfig.availableBalance;
		var createForm = $("[name='createForm']");
		if (!createForm.data("validator")) {
			createForm.validate({ignore: []});
			createForm.find("#amountTest").rules("add", {
				required: true,
				DBNumber: [20, 2],
				minStrict: 0.00,
				maxNumber: balance,
				messages: {
					DBNumber: "only number",
					minStrict: "Withdrawal amount cannot be 0.",
					maxNumber: `Not enough balance ${PageConfig.managerCurrencySymbol}${CurrencyUtil.formatter(balance)}`
				}
			});
			createForm.find("[name='id']").rules("add", {
				required: true,
			})

		}
		createForm.get(0).reset();
		createForm.data("validator").resetForm();
		createForm.find("[name='amount']").val('');
		createForm.find("#checkboxAmount").empty();
		createForm.find('#generalDiv').hide();

		$('#createWithdrawalModal').modal('show');
	};

	WithdrawalHandler.handleCheckboxChange = function () {

		// Fix: Both checkbox and amount field are in createForm
		const createForm = $("[name='createForm']");
		const $checkbox = createForm.find("#withdrawalAmountCheckbox");
		const $amountField = createForm.find("#amountTest");

		console.log($checkbox.is(':checked'))


		if ($checkbox.is(':checked')) {
			const maxBalance = PageConfig.availableBalance ?? 0
			$amountField.val(maxBalance);
			$amountField.prop('disabled', true);

			// 更新显示文本
			const formattedAmount = CurrencyUtil.formatter(maxBalance);
			// $("#withdrawalAmountText").text(`提取全部余额 ${PageConfig.managerCurrencySymbol} ${formattedAmount}`);

		} else {
			// 取消打勾时：清空并启用输入框
			$amountField.val('');
			$amountField.prop('disabled', false);
			$amountField.focus(); // 自动聚焦到输入框

			// 更新显示文本
			const maxBalance = $amountField.attr('data-max-balance') || 0;
			const formattedBalance = CurrencyUtil.formatter(maxBalance);
		}
	};

	var resetWithdrawalForm = function () {
		const form = $("[name='searchBalanceAndAccountBankForm']");

		// 清空所有输入框
		form.find("#emailSelect2").val(null).trigger("change"); // this will trigger $('#emailSelect2').change

		const createForm = $("[name='createForm']");
		createForm.find("#amountTest").val('').prop('disabled', false);

		// 移除所有验证规则和错误消息
		createForm.find("#amountTest").rules('remove');
		form.find('label.error').remove();
		form.find('.error').removeClass('error');
		createForm.find('label.error').remove();
		createForm.find('.error').removeClass('error');

		// 清空checkbox区域
		createForm.find("#checkboxAmount").empty();

		// 隐藏generalDiv
		createForm.find('#generalDiv').hide();

		// 清空存储的数据属性
		createForm.find("#amountTest").removeAttr('data-max-balance');

		// 重置表单验证状态
		if (form.data('validator')) {
			form.data('validator').resetForm();
		}

		if (createForm.data('validator')) {
			createForm.data("validator").resetForm();
		}

		// 清空任何pending的timeout
		clearTimeout(createForm.find("#amountTest").data('timeout'));

		// 移除checkbox事件绑定（避免重复绑定）
		$("#withdrawalAmountCheckbox").off('change');
		$("#amountTest").off('.validation');

	};


	var getAvailableBalance = function () {
		const form = $("[name ='searchBalanceAndAccountBankForm']");
		const createForm = $("[name='createForm']");
		const email = form.find("#emailSelect2").val();
		$.ajax({
			type: "GET",
			url: '/manager/payment/getAvailableBalance',
			data: {
				email: email
			},
			dataType: 'JSON',
			success: function (data) {
				if (data.error) {
					NotifyHandler.errorMsg(data.error);
					return;
				}

				const balance = data?.data[0]?.balance ?? 0;
				const formattedBalance = CurrencyUtil.formatter(balance);
				PageConfig.availableBalance = balance;

				// createForm.find("#amountTest").attr('data-max-balance', balance);
				const html = `
                <input type="checkbox" id="withdrawalAmountCheckbox" onchange="WithdrawalHandler.handleCheckboxChange()">
                <span id="withdrawalAmountText">
                    Withdrawal amount ${PageConfig.managerCurrencySymbol} ${formattedBalance}
                </span>`;

				// Fix: checkboxAmount is in createForm, not searchBalanceAndAccountBankForm
				createForm.find("#checkboxAmount").html(html);

				const $checkbox = createForm.find("#withdrawalAmountCheckbox");

				if (balance === 0) {
					$checkbox.prop('disabled', true);
				}
			},
			complete: function () {
				// element.disabled = false;
			}
		});
	}

	WithdrawalHandler.validateMaxAmount = function (element, maxAmount) {
		const inputValue = parseFloat(element.value);
		if (isNaN(inputValue)) {
			element.value = 0;
			return;
		}

		let finalValue;

		if (inputValue > maxAmount) {
			finalValue = maxAmount;
		} else if (inputValue < 0) {
			finalValue = 0;
		} else {
			finalValue = inputValue;
		}

		element.value = parseFloat(finalValue.toFixed(2));
	}
	var getBankCard = function () {

		const form = $("[name='searchBalanceAndAccountBankForm']");
		const email = form.find("#emailSelect2").val();

		$.ajax({
			type: "GET",
			url: '/manager/payment/getAccountCardByUser',
			data: {
				email: email
			},
			dataType: 'JSON',
			success: function (data) {
				if (data.error) {
					NotifyHandler.errorMsg(data.error);
					return;
				}
				populateWithdrawalMethod(data.data)
				console.log("data", data)


			},
			complete: function () {
				// element.disabled = false;
			}
		});

	}

	var populateWithdrawalMethod = function (bankAccounts) {
		const $select = $('#bankAccountNumberId');
		const form = $("[name='searchBalanceAndAccountBankForm']");

		// 清空现有选项
		$select.empty();

		if (!bankAccounts || bankAccounts.length === 0) {
			$select.append('<option value="" disabled selected>No available account card </option>');
			return;
		}

		// 添加银行卡选项
		$.each(bankAccounts, function (index, item) {
			var $option = $('<option></option>')
				.attr('value', item.value)
				.text(`${item.bankName} (${formatAccountNumber(item.cardNumber)})`)
				.data('cardInfo', item); // 存储完整的银行卡信息

			$select.append($option);
		});

		$select.prop('disabled', false);


	};

	WithdrawalHandler.resetCreateWithdrawal = function () {
		var searchBalanceAndAccountBankForm = $("[name='searchBalanceAndAccountBankForm']");
		searchBalanceAndAccountBankForm.get(0).reset();
		searchBalanceAndAccountBankForm.data("validator").resetForm();
		searchBalanceAndAccountBankForm.find('#emailSelect2').val(null).trigger('change'); // this will trigger $('#emailSelect2').change

		var createForm = $("[name='createForm']");
		createForm.get(0).reset();
		createForm.find("[name='amount']").val('').prop('disabled', false);;
		createForm.find("#checkboxAmount").empty();
		createForm.find('#generalDiv').hide();
	};

	WithdrawalHandler.getWithdrawalInfoForCreate = function () {
		var searchBalanceAndAccountBankForm = $("[name='searchBalanceAndAccountBankForm']");
		if (!searchBalanceAndAccountBankForm.data("validator")) {
			searchBalanceAndAccountBankForm.validate({
				ignore: [] // Don't ignore hidden fields (Select2)
			});
			searchBalanceAndAccountBankForm.find("[name='email']").rules("add", {
				required: true,
				maxlength: 50,
			});
		}
		// Validate the form - this will clear validation error if email is now valid
		if (!searchBalanceAndAccountBankForm.valid()) {
			return;
		}

		// If valid, show general div and fetch data
		$('#generalDiv').show();
		getBankCard();
		getAvailableBalance();
	};

	WithdrawalHandler.create = function () {

		var searchBalanceAndAccountBankForm = $("[name='searchBalanceAndAccountBankForm']");

		searchBalanceAndAccountBankForm.removeData("validator");

		if (!searchBalanceAndAccountBankForm.data("validator")) {
			searchBalanceAndAccountBankForm.validate({
				ignore: []
			});

			searchBalanceAndAccountBankForm.find("[name=email]").rules("add", {
				required: true,
				maxlength: 50,
			});
		}


		if (!searchBalanceAndAccountBankForm.valid()) {
			return;
		}

		var balance = PageConfig.availableBalance;

		var createForm = $("[name='createForm']");

		// Initialize validator if not exists
		if (!createForm.data("validator")) {
			createForm.validate({ignore: []});
		}

		// Add amount validation rules BEFORE validating
		createForm.find("#amountTest").rules("add", {
			required: true,
			DBNumber: [20, 2],
			minStrict: 0.00,
			maxNumber: balance,
		});

		// Add bank selection validation rules BEFORE validating
		createForm.find("[name='id']").rules("add", {
			required: true,
		});

		// Validate the form after rules are added
		if (!createForm.valid()) {
			return;
		}
		var serializedData = createForm.serialize();

		var $amountField = $("#amountTest");
		var $email = $("#emailSelect2");
		if ($amountField.prop('disabled') && $amountField.val()) {
			serializedData += (serializedData ? '&' : '') + 'amount=' + encodeURIComponent($amountField.val()) + '&email=' + $email.val();
		}else{
			serializedData += (serializedData ? '&' : '') + '&email=' + $email.val();
		}

		console.log(serializedData)
		$.ajax({
			type: "POST",
			url: '/manager/payment/createWithdrawal',
			dataType: 'JSON',
			data: serializedData,
			success: function (data) {
				if (data.error) {
					NotifyHandler.errorMsg(data.error);
					return;
				}
				if (data.status === '500') {
					NotifyHandler.errorMsg(data.message);
					return;
				}
				NotifyHandler.successMsg(data.message);
				$('#createWithdrawalModal').modal('hide');

				if (dataTableOptions) {
					dataTableOptions.dataTableRef.fnDraw(dataTableOptions.dataTableRef.fnSettings());
				} else {
					WithdrawalHandler.search();
				}
			}
		});
	};


	WithdrawalHandler.goToBatchApprove = function () {

		var batchWithdraw = [];
		$('input:checkbox[name=withdrawalId]:checked').each(function () {
			var $this = $(this);
			var $currentRow = $this.parents('tr');
			batchWithdraw.push($currentRow.find('[name=withdrawalId]').val());
		});

		if (batchWithdraw.length === 0) {
			return;
		}

		$.ajax({
			type: 'POST',
			url: '/manager/payment/getBatchMoneyTransactionDetails',
			data: {
				batchDeposit: JSON.stringify(batchWithdraw)
			},
			dataType: 'JSON',
			success: function (data) {
				if (data.error) {
					NotifyHandler.errorMsg(data.error);
					return;
				}

				WithdrawalHandler.cacheApproveWithdrawal = data;
				let model = $('#batchApproveWithdrawalModal');
				model.modal('show');
				batchWithdrawalCallBack(data?.aaData);
			},
		});

	};

	function batchWithdrawalCallBack(data) {
		const $container = $('#batchWithdrawalContainer');
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
			const $row = $('<tr>');
			$row.append(`<td id="email_id">${element?.email}</td>`);
			$row.append(`<td id ="transaction_id_id">${element?.transactionId}</td>`);
			$row.append(`<td id="created_time_id">${moment(element.createdTime).format("DD/MM/YYYY HH:mm:ss")}</td>`);
			$row.append(`<td id="payment_method_id">${element?.paymentMethod ? I18N.get(PageConfig.paymentTypeJson[element.paymentMethod].getFullName) : "-"}</td>`);
			$row.append(`<td id="bank_id">${element?.toBankName ?? "-"}</td>`);
			$row.append(`<td id="card_number_id ">${element?.toBankNumber ? formatAccountNumber(element.toBankNumber) : "-"}</td>`);
			$row.append(`<td id="amoun_id">${CurrencyUtil.formatter(element?.amount)}</td>`);
			$container.append($row);
		});
	}

	var iniSelect2UserId = function () {

		var options = {
			minimumInputLength: 3,
			multiple: false,
			ajaxPath: '/manager/member/getAllUserIdForMemberSearch',
			ajaxParam: {
				currencyType: function () {
					return $('#currencySelect2').val()
				},
				selectUserId: function () {
					return $("[name='searchBalanceAndAccountBankForm'] #emailSelect2").val();
				},
			},
			initSelection: function (element, callback) {
				callback($.map(element.val().split(','), function (id) {
					return {id: id, text: id};
				}));
			}
		};

		Select2Handler.search(options, JsCache.get('#emailSelect2'), true);

		// Bind change event once during initialization (like adjustment.js)
		$('#emailSelect2').change(function () {
			if ($(this).val() === '') {
				return;
			}
			WithdrawalHandler.getWithdrawalInfoForCreate();
		});
	};


	WithdrawalHandler.openProfile = function (pElement) {
		var element = $(pElement).closest('tr');
		var panel = window.open("/page/manager/member/profile.jsp?userId=" + element.find('#email').text() + "&currency=" + element.find('#currency').val(), "_winPROFILE", "width=1200,height=640,top=5,left=5," +
			"toolbar=no,menubar=no,resizable=no,scrollbars=yes,status=no,location=no,directories=no");
		if (panel) {
			panel.focus();
		}
	};


	WithdrawalHandler.gotoDetail = function (element) {
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

				element.find("[name='withdrawMethod']").text(data?.paymentType ? I18N.get(PageConfig.paymentTypeJson[data.paymentType].getFullName) : "-");

				element.find("[name='bank']").text(data?.toBankName ?? "-");

				element.find("[name='accountNumber']").text(data?.toBankNumber ? formatAccountNumber(data.toBankNumber) : "-");

				if (data?.status === 0) {
					const buttonHtml = `
       				 <div class="row" style="display: flex; justify-content: flex-end; margin-top: 20px; padding-right: 10px;">
            			<button class="btn btn-primary" style="margin-right: 10px;" onclick="WithdrawalHandler.singleRejectWithdrawal('${transactionId}')">Disapprove</button>
            			<button class="btn btn-primary" onclick="WithdrawalHandler.singleApproveWithdrawal('${transactionId}')">Approve</button>
        			</div>`


					element.find("#buttonsPlaceHolder").html(buttonHtml);
				} else {
					element.find("#buttonsPlaceHolder").empty();
				}


				detailModal.append(element);

				detailModal.modal('show');
			}
		});
	};

	function toDoBatchReject(transactionIds, callback) {
		$.ajax({
			type: "POST",
			url: '/manager/payment/disapproveWithdrawal',
			dataType: 'JSON',
			data: {
				batchWithdrawal: JSON.stringify(transactionIds)
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


	WithdrawalHandler.singleRejectWithdrawal = function (ids) {
		const array = [ids]
		toDoBatchReject(array, function () {
			$("#detailModal").modal('hide');
			dataTableOptions.dataTableRef.fnDraw(dataTableOptions.dataTableRef.fnSettings());
		});

		console.log(array)

	}

	WithdrawalHandler.singleApproveWithdrawal = function (ids) {
		const array = [ids]
		toDoBatchApproveWithdrawal(array, function () {
			$("#detailModal").modal('hide');
			dataTableOptions.dataTableRef.fnDraw(dataTableOptions.dataTableRef.fnSettings());
		});

		console.log(array)

	}

	function toDoBatchApproveWithdrawal(transactionIds, callback) {
		$.ajax({
			type: "POST",
			url: '/manager/payment/approveWithdrawal',
			dataType: 'JSON',
			data: {
				batchWithdrawal: JSON.stringify(transactionIds)
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

	WithdrawalHandler.batchApproveWithdrawal = function () {
		const container = $("#batchWithdrawalContainer")
		const transactionIds = [];

		container.find('tr').each(function () {
			const transactionId = $(this).find('#transaction_id_id').text().trim();
			if (transactionId && transactionId !== 'No data available') {
				transactionIds.push(transactionId);
			}
		});
		toDoBatchApproveWithdrawal(transactionIds, function () {
			$('#batchApproveWithdrawalModal').modal('hide');
			dataTableOptions.dataTableRef.fnDraw(dataTableOptions.dataTableRef.fnSettings());
		});
	}


	WithdrawalHandler.batchReject = function () {
		const container = $("#batchWithdrawalContainer")
		const transactionIds = [];

		container.find('tr').each(function () {
			const transactionId = $(this).find('#transaction_id_id').text().trim();
			if (transactionId && transactionId !== 'No data available') {
				transactionIds.push(transactionId);
			}
		});
		toDoBatchReject(transactionIds, function () {
			$('#batchApproveWithdrawalModal').modal('hide');
			dataTableOptions.dataTableRef.fnDraw(dataTableOptions.dataTableRef.fnSettings());
		});
	}

	// let formatCardNumber = function(cardNumber, chunkSize = 4, separator = ' ') {
	// 	return cardNumber.replace(new RegExp(`(.{${chunkSize}})`, 'g'), `$1${separator}`).trim();
	// }


	WithdrawalHandler.onChangeSeparatorAdvanced = function (input, options = {}) {
		const config = {
			allowNegative: false,        // 允许负数
			minValue: -Infinity,        // 🔧 修复：改为 -Infinity 以支持负数
			maxValue: Infinity,
			decimalPlaces: 2,
			thousandSeparator: ',',
			decimalSeparator: '.',
			defaultValue: '0.00',
			autoAddDecimals: true,
			restrictInput: true,
			...options
		};

		if (config.restrictInput) {
			let value = input.value;

			// 只允许数字和一个小数点
			value = value.replace(/[^0-9.]/g, '');

			// 确保只有一个小数点
			const parts = value.split('.');
			if (parts.length > 2) {
				value = parts[0] + '.' + parts.slice(1).join('');
			}

			// 限制小数点后只能有2位数字
			if (parts[1] && parts[1].length > 2) {
				value = parts[0] + '.' + parts[1].substring(0, 2);
			}

			// 如果值被修改了，更新input并返回
			if (value !== input.value) {
				input.value = value;
				return;
			}
		}
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


	WithdrawalHandler.restrictInput = function (element) {
		let value = element.value;

		// 只允许数字和一个小数点
		value = value.replace(/[^0-9.]/g, '');

		// 确保只有一个小数点
		const parts = value.split('.');
		if (parts.length > 2) {
			value = parts[0] + '.' + parts.slice(1).join('');
		}

		// 限制小数点后只能有2位数字
		if (parts[1] && parts[1].length > 2) {
			value = parts[0] + '.' + parts[1].substring(0, 2);
		}

		element.value = value;
	}

	WithdrawalHandler.onKeyDown = function (event) {
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




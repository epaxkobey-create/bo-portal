if (typeof (SearchHandler) == 'undefined') {
	SearchHandler = {};
}

(function () {
	let defaultConditions;

	SearchHandler.init = function () {
		CurrencyUtil.updateSetting({
			"precision": 2,
			"trailingZeros": true,
			"roundingMode": CurrencyUtil.roundingMode.HALF_UP
		});

		if (!PageConfig.enableShowEmail) {
			PageConfig.excludeMemberCol.push(4);
			PageConfig.excludeProviderCol.push(6);
		}
		initLastLoginTimeDate();

		if (passwordReqHandler) {
			passwordReqHandler.destroy();
		}
		passwordReqHandler = createPasswordRequirementsHandler();

		// 初始化 createEmail 自動轉小寫功能
		initCreateEmailLowerCase();

		// SearchHandler.iniSelect2AffiliateUserId();
		$('#updateStatusModal').on('hidden.bs.modal', function () {
			$(this).removeData('originalStatus');
			$(this).removeData('originalUserId');
		});

		$('#createAccountModal').on("hidden.bs.modal", function () {
			resetPasswordForm();
		});
	};

	let initCreateEmailLowerCase = function () {
		// 當 createEmail 欄位失去焦點時，自動轉換為小寫
		$('#createEmail').on('blur', function () {
			const $input = $(this);
			const originalValue = $input.val();
			if (originalValue) {
				const lowerCaseValue = originalValue.toLowerCase();
				if (originalValue !== lowerCaseValue) {
					$input.val(lowerCaseValue);
					// 觸發 change 事件，確保驗證器知道值已更改
					$input.trigger('change');
				}
			}
		});
	};

	let initLastLoginTimeDate = function () {
		$('.singleDateTimePicker').each2(function () {
			var $input = $(this);
			var id = $input.attr('id');

			var dateOptions = {
				"singleDatePicker": true,
				autoUpdateInput: false,
				showDropdowns: true,
				timePicker24Hour: false,
				timePicker: true,
				locale: DateRangeHandler.changeLanguage('DD/MM/YYYY HH:mm:ss', PageConfig.lang),

			}

			DateRangeHandler.singleDateTimePicker(dateOptions, PageConfig.lang);

			if (id === "lastLoginSince") {
				$input.val(PageConfig.todayOneMonthAgo);
				$input.parent().find('[name="singleDateTimePicker"]').val(
					PageConfig.todayOneMonthAgo
				);
			}

			DateRangeHandler.bindEvent();

		});


	}


	var dataTableOptions;


	SearchHandler.openUpdateStatusModal = function (userId, status) {
		console.log(status);
		console.log(userId);
		let updateStatusModal = $('#updateStatusModal');
		let element = updateStatusModal.children().detach();
		updateStatusModal.data("originalStatus", status);
		element.find("[name='status']").val(status);
		element.find("[name='userId']").val(userId);
		$.uniform.update();//Chrome、IE要這樣
		updateStatusModal.append(element);
		$('#updateStatusModal').modal('show');
	}

	SearchHandler.resetStatus = function () {

		let updateStatusForm = $("[name='updateStatusForm']");
		updateStatusForm.get(0).reset();
		loadStatus();
	};


	const escapeHtml = (str) => {
		if (!str) return '';
		const div = document.createElement('div');
		div.textContent = str;
		return div.innerHTML;
	};

	const escapeAttr = (str) => {
		if (!str) return '';
		return String(str)
			.replace(/&/g, '&amp;')
			.replace(/"/g, '&quot;')
			.replace(/'/g, '&#39;')
			.replace(/</g, '&lt;')
			.replace(/>/g, '&gt;');
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

	SearchHandler.search = function () {

		checkDefaultCondition(defaultConditions, $("#searchForm").serializeArray());

		if (dataTableOptions?.dataTableRef) {
			const table = $(dataTableOptions.tableSelector).DataTable();
			table.fnDestroy();
		}


		const defaultSorting = dataTableOptions?.aaSorting || [[6, "desc"]];

		dataTableOptions = {
			tableSelector: '#MemberSearchTable',
			formSelector: "[name='searchForm']",//optional
			sAjaxSource: '/manager/member/searchMember',
			aaSorting: defaultSorting,
			excludeColVis: PageConfig.excludeMemberCol,
			iDisplayLength: PageConfig.pageSize,
			showAllColumn: true, // 20201216 新增欄位需要show出全部欄位

			aoColumns: [
				{
					"mData": null,
					"bSortable": false,
				},
				{
					"mData": "email",
					"mRender": function (data, type, full) {
						if (type === "display" && PageConfig.enableShowEmail) {
							if (!data || !data.email) {
								return "-";
							}
							let icon = data.verified == BinaryStatusType.ACTIVE.value
								? "<i class='icon-ok' style='color: green;'></i>"
								: "";

							return "<a style='text-decoration: underline;' id='email' onclick='SearchHandler.openProfile(this);' href='javascript:void(0);'>"
								+ escapeHtml(data.email) + "</a>"
								+ "<input type='hidden' id='userId' class='user-id' value='" + escapeAttr(full.userId) + "'>"
								+ "<input type='hidden' id='currency' class ='currency' value='" + escapeAttr(full.currency) + "'>"
								+ icon;
						}
						return data?.email || "-";

					},
					bVisible: PageConfig.enableProfileContactView && PageConfig.enableShowEmail,
					"bSortable": false
				},
				{
					"mData": "fullName",
					"mRender": function (data, type, full) {
						if (type === 'display' && data) {
							return data;
						}
						return "-";
					}

				},

				{
					"mData": "birthday",
					"mRender": function (data, type, full) {
						if (type === 'display' && data) {
							return data;
						}
						return "-";
					}
				},
				{
					"mData": "status",
					"mRender": function (data, type, full) {
						if (type === 'display') {
							if (!data && data !== 0) {
								return "-";
							}
							var accountStatusType = PageConfig.AccountStatusType[data];
							if (accountStatusType) {
								const labelHtml = `<span class="label ${accountStatusType.css}">${I18N.get(accountStatusType.fullName)}</span>`;
								return `<a href="javascript:void(0);" onclick="SearchHandler.openUpdateStatusModal('${escapeJs(full.userId)}', '${full.status}')" style="text-decoration: none;">${labelHtml}</a>`
									;
							}
						}
						return data || "-";
					}
				},
				{
					"mData": "kycDocumentStatus",
					"mRender": function (data, type, full) {
						if (type === 'display') {
							if (!data && data !== 0) {
								return "-";
							}
							var kycDocumentStatuses = PageConfig.DocumentStatusType[data];
							if (kycDocumentStatuses) {
								const labelHtml = `<span class="label ${kycDocumentStatuses.css}">${I18N.get(kycDocumentStatuses.columnName)}</span>`;
								return `<a href="javascript:void(0);" onclick="SearchHandler.openUpdateKycStatusModal('${escapeJs(full.userId)}', '${full.kycDocumentStatus}', '${escapeJs(full.currency)}')" style="text-decoration: none;">${labelHtml}</a>`
									;
							}
						}
						return data || "-";
					}
				},
				{
					"mData": "loginTime",
					"mRender": function (data, type, full) {
						if (type === 'display') {
							return data || "-";
						}
						return data;
					}
				},
				{
					"mData": "totalBalance",
					"mRender": function (data, type, full) {
						if (type === 'display') {
							if (data === null || data === undefined) {
								return "-";
							}
							return CurrencyUtil.formatter(data);
						}
						return data;
					},
					"bSortable": false,
				},

			],
			aoColumnDefs: [{"sClass": "dt-body-right", "aTargets": [7]}]
		};


		var searchForm = $("[name='searchForm']");


		if (!searchForm.data("validator")) {
			searchForm.validate();

			// searchForm.find("[name=email]").rules("add", {maxlength: 50});
			searchForm.find("[name=phoneNumber]").rules("add", {maxlength: 15});
			searchForm.find("[name=noLoginSince]").rules("add", {maxlength: 19, dateTime: true});

			searchForm.find("[name=loginIp]").rules("add", {maxlength: 15});

			searchForm.find("[name=registeredDateStart]").rules("add", {maxlength: 19, dateTime: true});
			searchForm.find("[name=registeredDateEnd]").rules("add", {maxlength: 19, dateTime: true});

			searchForm.find("[name=birthOfDateStart]").rules("add", {maxlength: 10, date2: true});
			searchForm.find("[name=birthOfDateEnd]").rules("add", {maxlength: 10, date2: true});

		}

		var callBack = function () {
			// if (PageConfig.enableExportExcel) {
			// 	DataTableHandler.addToolBar(dataTableOptions.tableSelector, $('#exportButton').clone().html().trim());
			// }
		}

		var isEnableSequence = true;
		dataTableOptions.dataTableRef = DataTableHandler.create(dataTableOptions, isEnableSequence, PageConfig.lang, callBack);

		$(dataTableOptions.tableSelector).on('order.dt', function () {
			const order = dataTableOptions.dataTableRef.order();
			dataTableOptions.aaSorting = order;
		});

		$('#resultTable').show();
	};


	SearchHandler.create = function () {

		let createAccountForm = $("[name='createAccountForm']");

		if (!createAccountForm.data("validator")) {

			createAccountForm.validate({
				ignore: [],
				errorPlacement: function (error, element) {
					if (element.attr("name") === "createPassword" || element.attr("name") === "createConfirmPassword") {
						element.closest('.form-group')
							.find('.error-msg-block')
							.html(error); // 插入 error HTML
					} else {
						error.insertAfter(element);
					}
				}
			});


			createAccountForm.find("[name='createPassword']").rules('remove');
			createAccountForm.find("[name='createPassword']").rules('add', {
				required: true,
				passwordContainsSpace: true,
				passwordContainsUnicode: true,
				playerLoginPassword: true,
				messages: {
					'playerLoginPassword': I18N.get('msg.error.password.isNotValidated.v2')
				}
			});

			createAccountForm.find("[name='createConfirmPassword']").rules('remove');
			createAccountForm.find("[name='createConfirmPassword']").rules('add', {
				required: true,
				equalTo: "[name='createPassword']",
				messages: {
					'playerLoginPassword': I18N.get('msg.error.password.isNotValidated.v2'),
					equalTo: I18N.get("msg.error.validation.passwordNotMatch")
				}
			});


			createAccountForm.find("[name='createEmail']").rules('add', {
				required: true,
				maxlength: 50,
				email: true,
			});
		}

		createAccountForm.get(0).reset();
		createAccountForm.validate().resetForm();
		$.uniform.update();

		$('#createAccountModal').modal('show');
		if ($('#creatEmailLebel').find('span').length == 0) {
			$('#creatEmailLebel').append('<span class="required">*</span>');
		}
	};

	SearchHandler.resetAccount = function () {
		// var createAccountForm = $("[name='createAccountForm']");
		// $('#passwordRequirements').hide();
		// createAccountForm.get(0).reset();
		// createAccountForm.validate().resetForm();
		// if (passwordReqHandler) {
		// 	passwordReqHandler.reset();
		// }
		resetPasswordForm();

		$.uniform.update();
	};


	SearchHandler.createAccount = function () {

		// 在提交前確保 email 轉換為小寫
		const $emailInput = $('#createEmail');
		const emailValue = $emailInput.val();
		if (emailValue) {
			$emailInput.val(emailValue.toLowerCase());
		}

		const createAccountForm = $("[name='createAccountForm']");

		if (!createAccountForm.valid()) {
			return;
		}
		SearchHandler.creatMemberAccount(createAccountForm.serializeArray())
	};

	SearchHandler.creatMemberAccount = function (data, element) {
		App.blockUI($("body"));
		$.ajax({
			type: "POST",
			url: "/manager/member/createMemberAccount",
			data: data,
			success: function (data) {
				if (data.error) {
					NotifyHandler.errorMsg(data.error);
					return;
				}
				NotifyHandler.successMsg(data.message);
				SearchHandler.search();
				$('#createAccountModal').modal('hide');
			},
			error: function (error) {
				alert(error);
			}, complete: function () {
				App.unblockUI($("body"));
			}
		});
	}

	SearchHandler.exportExcel = function () {
		const visibleColumns = ColVisHandler.getVisibleColumns(dataTableOptions.dataTableRef);

		const searchForm = $("[name='searchForm']");
		if (!searchForm.valid()) {
			return;
		}

		searchForm.find('[name=visibleColumns]').val(visibleColumns);

		checkDefaultCondition(defaultConditions, $("#searchForm").serializeArray());
		ExcelUtils.schedule(ReportExportType.MEMBER, '/manager/member/exportMember', searchForm.serialize(), $('[name=export]'));
		// ExcelUtils.exportExcelBinary('/manager/member/exportMember?' + searchForm.serialize() + "&visibleColumns=" + visibleColumns, 'MemberListReport.xlsx');
	};


	SearchHandler.openProfile = function (pElement) {
		var element = $(pElement).closest('tr');
		const userId = element.find('.user-id').val();
		const currency = element.find('.currency').val();
		var panel = window.open("/page/manager/member/profile.jsp?userId=" + encodeURIComponent(userId) + "&currency=" + encodeURIComponent(currency), "_winPROFILE", "width=1200,height=640,top=5,left=5," +
			"toolbar=no,menubar=no,resizable=no,scrollbars=yes,status=no,location=no,directories=no");
		if (panel) {
			panel.focus();
		}
	};

	function checkDefaultCondition(defaultConditions, currentConditions) {
		if (!!defaultConditions) {

			let isDefaultConditions = true;

			defaultConditions.forEach(condition => {
				let name = condition.name;
				let value = condition.value;

				if (name !== 'defaultConditionFlag' && name !== 'visibleColumns') {
					let formData = currentConditions.find(data => {
						return data.name === name;
					});

					if (formData.value !== value) {
						isDefaultConditions = false;
					}
				}
			});

			$("#defaultConditionFlag").val(isDefaultConditions);
		}
	}

	SearchHandler.toggleVisibility = function (inputId, iconElement) {
		const input = document.getElementById(inputId);
		const isHidden = input.type === "password";

		input.type = isHidden ? "text" : "password";
		iconElement.classList.toggle("icon-eye-close", !isHidden);
		iconElement.classList.toggle("icon-eye-open", isHidden);
	}

	SearchHandler.updateStatus = function () {
		let updateStatusForm = $("[name='updateStatusForm']");

		updateStatusForm.find('[name=save]').addClass('disabled');
		updateStatusForm.find('[name=resetButton]').addClass('disabled');


		$.ajax({
			type: "POST",
			url: '/manager/member/updateStatus',
			dataType: 'JSON',
			data: updateStatusForm.serialize(),
			success: function (data) {
				if (data.error) {
					NotifyHandler.errorMsg(data.error);
					return;
				}
				NotifyHandler.successMsg(data.message);
				$('#updateStatusModal').modal('hide');
				SearchHandler.search();
			},
			complete: function () {
				updateStatusForm.find('[name=save]').removeClass('disabled');
				updateStatusForm.find('[name=resetButton]').removeClass('disabled');
			}
		});
	};

	let loadStatus = function () {
		let updateStatusModal = $('#updateStatusModal');
		let element = updateStatusModal.children().detach();
		let originalStatus = updateStatusModal.data("originalStatus");
		element.find("[name='status']").val(originalStatus);
		$.uniform.update();//Chrome、IE要這樣
		updateStatusModal.append(element);
	}


	SearchHandler.openUpdateKycStatusModal = function (userId, status, currency) {

		let updateKycStatusModal = $('#updateKycStatusModal');
		let element = updateKycStatusModal.children().detach();
		updateKycStatusModal.data("originalKycStatus", status);
		element.find("[name='kycStatus']").val(status);
		element.find("[name='kycUserId']").val(userId);
		element.find("[name='currencyTypeId']").val(currency)
		$.uniform.update();//Chrome、IE要這樣
		updateKycStatusModal.append(element);
		$('#updateKycStatusModal').modal('show');


	}

	SearchHandler.resetKycStatus = function () {

		let updateKycStatusForm = $("[name='updateKycStatusForm']");
		updateKycStatusForm.get(0).reset();
		loadKycStatus();
	};

	let loadKycStatus = function () {
		let updateKycStatusModal = $('#updateKycStatusModal');
		let element = updateKycStatusModal.children().detach();
		let originalStatus = updateKycStatusModal.data("originalKycStatus");

		element.find("[name='kycStatus']").val(originalStatus);
		$.uniform.update();//Chrome、IE要這樣
		updateKycStatusModal.append(element);
	}


	SearchHandler.updateKycStatus = function () {
		let updateKycStatusForm = $("[name='updateKycStatusForm']");

		updateKycStatusForm.find('[name=saveKycButton]').addClass('disabled');
		updateKycStatusForm.find('[name=resetKycButton]').addClass('disabled');


		$.ajax({
			type: "POST",
			url: '/manager/member/updateKycStatus',
			dataType: 'JSON',
			data: updateKycStatusForm.serialize(),
			success: function (data) {
				if (data.error) {
					NotifyHandler.errorMsg(data.error);
					return;
				}
				NotifyHandler.successMsg(data.message);
				$('#updateKycStatusModal').modal('hide');
				SearchHandler.search();

				updateKycStatusForm.find('[name=saveKycButton]').removeClass('disabled');
				updateKycStatusForm.find('[name=resetKycButton]').removeClass('disabled');
			},
			complete: function () {
				updateKycStatusForm.find('[name=saveKycButton]').removeClass('disabled');
				updateKycStatusForm.find('[name=resetKycButton]').removeClass('disabled');
			}
		});
	};


	function createPasswordRequirementsHandler() {
		var state = {
			userInteracted: false,
			allRequirementsMet: false
		};

		var $passwordInput = $('#createPassword');
		var $requirementsBox = $('#passwordRequirements');

		// 移除舊的事件監聽器，防止重複綁定
		$passwordInput.off('.passwordReq');

		function validateRequirement(id, isValid) {
			var $element = $('#' + id);
			var $icon = $element.find('i');

			if (isValid) {
				$element.addClass('valid').removeClass('invalid');
				$icon.removeClass('icon-circle-blank icon-remove')
					.addClass('icon-ok-circle')
					.css('color', '#5cb85c');
			} else {
				$element.addClass('invalid').removeClass('valid');
				$icon.removeClass('icon-ok-circle icon-remove')
					.addClass('icon-circle-blank')
					.css('color', '#666');
			}
		}

		// 綁定 focus 事件 - 只在用戶已互動且未滿足需求時顯示
		$passwordInput.on('focus.passwordReq', function () {
			if (!state.allRequirementsMet && state.userInteracted) {
				$requirementsBox.slideDown(200);
			}
		});

		// 綁定 keydown 事件 - 追蹤用戶實際輸入行為
		$passwordInput.on('keydown.passwordReq', function () {
			state.userInteracted = true;
		});

		// 綁定 input 事件 - 執行密碼驗證邏輯
		$passwordInput.on('input.passwordReq', function () {
			var password = $(this).val();

			// 標記用戶已互動
			if (password.length > 0) {
				state.userInteracted = true;
			}

			// 驗證所有密碼需求
			var lengthValid = password.length >= 6 && password.length <= 20;
			var uppercaseValid = /[A-Z]/.test(password);
			var lowercaseValid = /[a-z]/.test(password);
			var digitValid = /[0-9]/.test(password);
			var symbolValid = /[@$!%*#]/.test(password);

			validateRequirement('req-length', lengthValid);
			validateRequirement('req-uppercase', uppercaseValid);
			validateRequirement('req-lowercase', lowercaseValid);
			validateRequirement('req-digit', digitValid);
			validateRequirement('req-symbol', symbolValid);

			// 更新總體需求滿足狀態
			state.allRequirementsMet = lengthValid && uppercaseValid &&
				lowercaseValid && digitValid && symbolValid;

			// 檢查其他驗證條件（空格和 Unicode）
			var noSpaceAllow = !/\s/.test(password);
			var noUniCodeAllow = !/[^\x00-\x7F]/.test(password);

			// 根據驗證結果顯示/隱藏需求框
			if (state.allRequirementsMet || !noSpaceAllow || !noUniCodeAllow) {
				$requirementsBox.slideUp(200);
			} else if (state.userInteracted) {
				$requirementsBox.slideDown(200);
			}
		});

		// 返回公開介面
		return {
			/**
			 * 重置處理器狀態和 UI
			 */
			reset: function () {
				state.userInteracted = false;
				state.allRequirementsMet = false;
				$requirementsBox.hide();

				// 重置所有驗證狀態
				$('#passwordRequirements li').removeClass('valid invalid');
				$('#passwordRequirements li i')
					.removeClass('icon-ok-circle')
					.addClass('icon-circle-blank')
					.removeAttr('style');
			},

			/**
			 * 銷毀處理器，清理事件監聽器
			 */
			destroy: function () {
				$passwordInput.off('.passwordReq');
				this.reset();
			}
		};
	}

	// 密碼需求處理器實例
	var passwordReqHandler = null;

	function resetPasswordForm() {
		// 清空所有输入
		$('#createEmail').val('');
		$('#createPassword').val('');
		$('#createConfirmPassword').val('');

		console.log("pass --->", passwordReqHandler)
		// 使用處理器重置狀態
		if (passwordReqHandler) {
			passwordReqHandler.reset();
		 }



		// 清空错误信息
		$('.error-msg-block').text('');
	}

})();
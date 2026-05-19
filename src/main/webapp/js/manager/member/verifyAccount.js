if (typeof (DocumentHandler) == 'undefined') {
	DocumentHandler = {};
}

(function() {

	DocumentHandler.init = function() {
		ReportUtil.setDefPaymentConditionOfTimeRange('createDateStart', 'createDateEnd');
		ReportUtil.validationMaxDate('createDateEnd', PageConfig.date.today);
		ReportUtil.validationMinDate('createDateStart', PageConfig.date.todayHalfYearAgo);
		ReportUtil.validationMaxDate('approvedDateEnd', PageConfig.date.today);
		ReportUtil.validationMinDate('approvedDateStart', PageConfig.date.todayHalfYearAgo);

		bindDate();
		bindCreateDocumentEvent();
		bindUnlockDocumentEvent();
		bindViewDocumentEvent();
		bindFile();

		bindRemarkEvent();

	};

	DocumentHandler.accountCurrency = 0;
	DocumentHandler.allbankForCreate = {};
	const initLocalBank = function() {
		$.ajax({
			type: "GET",
			url: '/manager/member/getAllBankForCreateDocument',
			dataType: 'JSON',
			data: {
				currency: DocumentHandler.accountCurrency
			},
			success: function(data) {
				if (data.error) {
					NotifyHandler.errorMsg(data.error);
					return;
				}
				DocumentHandler.allbankForCreate = data;
				loadCreateBank();
				loadBankProvinceList('createBankProvince');
				loadBankCityList('createBankProvince', 'createBankCity');
			}
		});
	};

	const loadCreateBank = function() {
		var data = DocumentHandler.allbankForCreate;
		var selector = document.getElementById('createBankList');
		while (selector.options.length > 0) {
			selector.remove(0);
		}
		var frag = document.createDocumentFragment();
		for (var i = 0; i < data.length; i++) {
			var option = document.createElement("option");
			option.value = data[i].bankId;
			option.text = data[i].displayName;
			frag.appendChild(option);
		}
		selector.appendChild(frag);
	};

	const loadBankProvinceList = function(bankProvinceKey) {
		if (CurrencyType.CNY.value === DocumentHandler.accountCurrency) {
			var data = WithdrawalProvinceCityData.getAllProvince();
			loadUpdateBankProvinceCityList(data, bankProvinceKey);
		}
	};

	const loadBankCityList = function(bankProvinceKey, bankCityKey) {
		if (CurrencyType.CNY.value === PageConfig.accountCurrency) {
			var selectorProvince = document.getElementById(bankProvinceKey);

			var data = WithdrawalProvinceCityData.getProvinceAllCity(selectorProvince.value);
			loadUpdateBankProvinceCityList(data, bankCityKey);
		}
	};


	const loadUpdateBankProvinceCityList = function(data, selectId) {
		var selector = document.getElementById(selectId);
		while (selector.options.length > 0) {
			selector.remove(0);
		}
		var frag = document.createDocumentFragment();
		for (var i = 0; i < data.length; i++) {
			var option = document.createElement("option");
			option.value = data[i].id;
			option.text = data[i].name;
			frag.appendChild(option);
		}
		selector.appendChild(frag);
	};

	function bindDate() {
		var dateTimePption = {
			"maxDate": PageConfig.date.today,
			"minDate": PageConfig.date.todayHalfYearAgo
		};
		DateRangeHandler.singleDateTimePicker(dateTimePption, PageConfig.lang);
		DateRangeHandler.bindEvent();

		var dateOption = {
			"singleDatePicker": true,
			autoUpdateInput: false,
			showDropdowns: true,
		};
		DateRangeHandler.singleDatePicker(dateOption, PageConfig.lang);
		DateRangeHandler.bindEvent();

		$('.glyphicon-calendar').click(function() {
			$(this).parent().find('input').click();
		});
	};

	var initDocumentType = function() {
		var documentType = document.getElementById('documentType');

		var frag = document.createDocumentFragment();
		for (var i = 0; i < PageConfig.documentType.order.length; i++) {
			var status = PageConfig.documentType.order[i];
			var data = PageConfig.documentType[status];

			var option = document.createElement("option");
			option.value = status;
			option.text = I18N.get(data.displayName);
			frag.appendChild(option);
		}
		documentType.appendChild(frag);
		$("#status option[value='9999']").prop('selected', true);
	};

	var initDocumentStatusType = function() {
		var documentStatusType = document.getElementById('documentStatusType');

		var frag = document.createDocumentFragment();
		for (var i = 0; i < PageConfig.documentStatusType.order.length; i++) {
			var status = PageConfig.documentStatusType.order[i];
			var data = PageConfig.documentStatusType[status];

			var option = document.createElement("option");
			option.value = status;
			option.text = I18N.get(data.fullName);
			frag.appendChild(option);
		}
		documentStatusType.appendChild(frag);
		$("#status option[value='9999']").prop('selected', true);
	};

	var dataTableOptions;
	DocumentHandler.search = function() {
		if (!dataTableOptions) {
			dataTableOptions = {
				tableSelector: '#documentSearchTable',
				formSelector: "[name='searchForm']",//optional
				sAjaxSource: '/manager/member/searchDocument',
				aaSorting: [[3, "desc"]],
				excludeColVis: [0, 8],
				iDisplayLength: PageConfig.pageSize,
				showAllColumn: true,
				aoColumns: [
					{
						"mData": null,
						"bSortable": false,
					},
					{
						"mData": "userId",
						"mRender": function(data, type, full) {
							if (PageConfig.enableProfile && type === 'display') {
								return "<a style='text-decoration: underline;' id='userId' onclick='DocumentHandler.openProfile(this);' href='javascript: void(0);'>" + data + "</a>"
									+ "<input type='hidden' id='currency' value='" + full.currencyTypeId + "'>";
							}
							return data;
						},
					},
					{
						"mData": "currencyTypeId",
						"mRender": function(data, type, full) {
							if (type === 'display' && data) {
								return PageConfig.currencyFullName[data];
							}
							return data;
						}
					},
					{
						"mData": "documentType",
						"mRender": function(data, type, full) {
							if (type === 'display') {
								var documentType = PageConfig.documentType[data];
								if (documentType) {
									return I18N.get(documentType.displayName);
								}
								return data;
							}
							return data;
						},
					},
					{"mData": "createTimeStr"},
					{
						"mData": "documentStatusType",
						"mRender": function(data, type, full) {
							if (type === 'display') {
								var documentStatusType = PageConfig.documentStatusType[data];
								if (documentStatusType) {
									return '<span class="label ' + documentStatusType.css + '">' + I18N.get(documentStatusType.fullName) + '</span>';
								}
								return data;
							}
							return data;
						},
					},
					{"mData": "approvedTimeStr"},
					{"mData": "approvedUserid"},
					{"mData": "executor"},
					{
						"mData": null,
						"mRender": function(data, type, full) {
							if (type === 'display') {
								let html = '';
								if (full.documentType === DocumentType.BANK_STATEMENT.unique() && !full.bankIsExists) {
									return html;
								}

								if (DocumentType.CPF.unique() === full.documentType) {
									return html;
								}

								if (PageConfig.enableApproveAction && (full.documentStatusType == DocumentStatusType.PENDING.value
									|| full.documentStatusType == DocumentStatusType.ON_HOLD.value)) {
									html += "<a style='text-decoration: underline;' id='action' data-currency='" + full.currencyTypeId + "' data-id='" + full.id + "' data-documentstatustype='" + full.documentStatusType + "' data-executor='" + full.executor + "' onclick='DocumentHandler.gotoApprove(this)' style='display:none'>" + I18N.get('form.text.backOffice.payment.approve') + "</a>";
								}
									// else if (full.documentStatusType == DocumentStatusType.REJECTED.value
									// 	&& full.showUpdateBtn) {
									// 	return "<a style='text-decoration: underline;' id='action' data-id='" + full.id + "' onclick='DocumentHandler.gotoUpdate(this)' style='display:none'>" + I18N.get('form.text.backOffice.report.update') + "</a>";
								// }
								else if (full.documentStatusType == DocumentStatusType.REJECTED.value
									|| full.documentStatusType == DocumentStatusType.APPROVED.value) {
									html += "<a style='text-decoration: underline;' id='action' data-id='" + full.id + "' onclick='DocumentHandler.gotoView(this)' style='display:none'>" + I18N.get('form.text.backOffice.report.view') + "</a>";
								}
								if (full.executor !== null && PageConfig.enableUnlockRole) {
									html += "<p></p><a style='text-decoration: underline;' id='action' data-id='" + full.id + "' data-executor='" + full.executor + "' onclick='DocumentHandler.gotoUnlockDocument(this)' style='display:none'>Unlock</a>";
								}
								return html;
							}
							return data;
						},
						"bSortable": false,
					}
				]
			};
		}

		var searchForm = $("[name='searchForm']");
		if (!searchForm.data("validator")) {
			searchForm.validate();

			searchForm.find("[name=userId]").rules("add", {maxlength: 15, userId: true});
			searchForm.find("[name=createDateStart]").rules("add", {maxlength: 19, dateTime: true});
			searchForm.find("[name=createDateEnd]").rules("add", {maxlength: 19, dateTime: true});
			searchForm.find("[name=approvedDateStart]").rules("add", {maxlength: 19, dateTime: true});
			searchForm.find("[name=approvedDateEnd]").rules("add", {maxlength: 19, dateTime: true});
		}

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

		var callBack = function() {

			if (PageConfig.enableExportExcel) {
				DataTableHandler.addToolBar(dataTableOptions.tableSelector, $('#exportButton').clone().html().trim());
			}
		}

		var isEnableSequence = true;
		dataTableOptions.dataTableRef = DataTableHandler.create(dataTableOptions, isEnableSequence, PageConfig.lang, callBack);
		$('#resultTable').show();
	};

	DocumentHandler.exportExcel = function() {
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

		ExcelUtils.schedule(ReportExportType.DOCUMENT, '/manager/member/exportDocument', searchForm.serialize(), $('[name=export]'));
	};

	DocumentHandler.openProfile = function(pElement) {
		var element = $(pElement).closest('tr');
		var panel = window.open("/page/manager/member/profile.jsp?userId=" + element.find('#userId').text() + "&currency=" + element.find('#currency').val(), "_winPROFILE", "width=1200,height=640,top=5,left=5," +
			"toolbar=no,menubar=no,resizable=no,scrollbars=yes,status=no,location=no,directories=no");
		if (panel) {
			panel.focus();
		}
	};

	DocumentHandler.gotoApprove = function(element) {
		let documentId = $(element).data('id');
		let executor = $(element).data('executor');
		let currencyTypeId = $(element).data('currency');

		if (executor && executor != PageConfig.userId) {
			NotifyHandler.errorMsg(I18N.get('msg.error.info.distinctExecutor', [executor]));
			return;
		}

		var approveForm = $("[name='approveForm']");
		approveForm.validate();
		approveForm.get(0).reset();
		approveForm.data("validator").resetForm();
		approveForm.data("currency", currencyTypeId);

		$.ajax({
			type: "GET",
			url: '/manager/member/getApproveDocument',
			data: {
				documentId: documentId
			},
			dataType: 'JSON',
			success: function(data) {
				if (data.error) {
					NotifyHandler.errorMsg(data.error);
					return;
				}
				var approveModal = $('#approveModal');
				approveModal.find('#documentId').val(data.documentId);

				var documentType = DocumentType.getInstanceOf(data.documentTypeId);
				var element = approveModal.children().detach();

				element.find('[name=bankDetail]').find('#userId').text('');
				element.find('[name=bankDetail]').hide();
				element.find('[name=documentDetail]').find('#userId').text('');
				element.find('[name=documentDetail]').hide();
				element.find('[name=onlineBankDetail]').find('#userId').text('');
				element.find('[name=onlineBankDetail]').hide();

				let target;
				if (documentType == DocumentType.BANK_STATEMENT) {
					target = element.find('[name=bankDetail]');
					target.find('#userId').text(data.userId);

					target.find('[name=documentTypeStr]').text(I18N.get(documentType.getFullName()));
					target.find('[name=bankName]').text(data.bankName);
					target.find('[name=bankBranch]').text(data.bankBranch);
					target.find('[name=bankAccName]').text(data.bankAccName);
					target.find('[name=bankAccNumber]').text(data.bankAccNumber);
					target.find('[name=financeCode]').text(data.financeCode);

					if (data.currency === CurrencyType.CNY.value) {
						target.find('[name=bankExtraData]').show();
						target.find('[name=bankProvince]').text(data.bankProvince);
						target.find('[name=bankCity]').text(data.bankCity);
					}

					const approveBankStoredRemark = element.find('#approveBankStoredRemark');
					var selector = approveBankStoredRemark[0];

					while (selector.options.length > 0) {
						selector.remove(0);
					}
					var frag = document.createDocumentFragment();

					var option = document.createElement("option");
					option.value = -1;
					option.text = '-- ' + I18N.get('ui.text.deposit.select_remark') + ' --';
					frag.appendChild(option);

					if (Object.keys(PageConfig.remark).length > 0) {
						const remark = PageConfig.remark[data.currency];
						if (remark !== undefined) {
							const approveRemark = remark[PageConfig.accessRight.approveVerifyAccount];
							if (approveRemark !== undefined) {
								$.each(approveRemark, function(i, el) {
									var option = document.createElement("option");
									option.value = el.template;
									option.text = el.title;
									frag.appendChild(option);
								});
							}
						}
					}
					selector.appendChild(frag);
					approveBankStoredRemark.val(-1);
				} else {
					if (documentType.getGroupType() === DocumentGroupType.EWALLET.value) {
						target = element.find('[name=onlineBankDetail]');
						target.find('[name=documentNoStr]').text(I18N.get("form.text.backOffice.payment.account"));
						target.find('#documentNo').text(data.bankAccNumber);
					} else if (documentType.getGroupType() === DocumentGroupType.UPI.value) {
						target = element.find('[name=onlineBankDetail]');
						target.find('[name=documentNoStr]').text(I18N.get("fe.text.upiCode"));
						target.find('#documentNo').text(data.bankAccNumber);
					} else {
						target = element.find('[name=documentDetail]');
						target.find('[name=documentNoStr]').text(I18N.get("form.text.backOffice.documentNo"));
						target.find('#expiredDate').text(data.expiredDate);
						target.find('#documentNo').text(data.documentNo);
					}

					target.find('#userId').text(data.userId);

					target.find('[name=documentTypeStr]').text(I18N.get(documentType.getFullName()));
					const approveDocumentStoredRemark = target.find('#approveDocumentStoredRemark');
					var selector = approveDocumentStoredRemark[0];

					while (selector.options.length > 0) {
						selector.remove(0);
					}
					var frag = document.createDocumentFragment();

					var option = document.createElement("option");
					option.value = -1;
					option.text = '-- ' + I18N.get('ui.text.deposit.select_remark') + ' --';
					frag.appendChild(option);

					if (Object.keys(PageConfig.remark).length > 0) {
						const remark = PageConfig.remark[data.currency];
						if (remark !== undefined) {
							const approveRemark = remark[PageConfig.accessRight.approveVerifyAccount];
							if (approveRemark !== undefined) {
								$.each(approveRemark, function(i, el) {
									var option = document.createElement("option");
									option.value = el.template;
									option.text = el.title;
									frag.appendChild(option);
								});
							}
						}
					}
					selector.appendChild(frag);
					approveDocumentStoredRemark.val(-1);
				}

				target.find('#frontPhotoDiv').hide();
				target.find('[name=' + DocumentType.getFrontFieldName() + ']').hide();
				target.find('[name=' + DocumentType.getFrontFieldName() + ']').attr('src', '');
				if (data.frontImageData !== null) {
					target.find('#frontPhotoDiv').show();
					target.find('[name=' + DocumentType.getFrontFieldName() + ']').show();
					target.find('[name=' + DocumentType.getFrontFieldName() + ']').attr('src', 'data:image/png;base64,' + data.frontImageData);
				}

				if (data.frontImageCrash) {
					target.find('[name=' + DocumentType.getFrontFieldName() + ']').parents('.form-group').addClass('has-error');
				}

				target.find('#backPhotoDiv').hide();
				target.find('[name=' + DocumentType.getBackFieldName() + ']').hide();
				target.find('[name=' + DocumentType.getBackFieldName() + ']').attr('src', '');
				if (data.backImageData !== null) {
					target.find('#backPhotoDiv').show();
					target.find('[name=' + DocumentType.getBackFieldName() + ']').show();
					target.find('[name=' + DocumentType.getBackFieldName() + ']').attr('src', 'data:image/png;base64,' + data.backImageData);
				}
				if (data.backImageCrash) {
					target.find('[name=' + DocumentType.getBackFieldName() + ']').parents('.form-group').addClass('has-error');
				}

				// past cases;
				if (DocumentStatusType.PENDING.value === data.status) {
					element.find('#documentContainer').empty();
					var frag = document.createDocumentFragment();
					element.find('#pastCasesDiv').show();
					element.find('#historyDiv').hide();
					target.find('[name=originalRemarkDiv]').hide();
					element.find("[name=hold]").show();

					if (data.documentHistoryList && data.documentHistoryList.length > 0) {
						for (var i = 0; i < data.documentHistoryList.length; i++) {
							var documentData = data.documentHistoryList[i];
							var template = element.find('#documentTemplate').children().clone();
							var status = PageConfig.documentStatusType[documentData.status];

							template.find('#status').text(I18N.get(status.fullName));
							template.find('#createTime').text(documentData.createTime);
							template.find('#approvedTime').text(documentData.approvedTime);
							template.find('#remark').text(documentData.remark);

							frag.appendChild(template[0]);
						}
					}
					element.find('#documentContainer').append(frag);
				} else {
					element.find('#detailContainer').empty();
					var frag = document.createDocumentFragment();

					element.find('#pastCasesDiv').hide();
					element.find('#historyDiv').show();

					if (DocumentStatusType.ON_HOLD.value === data.status) {
						if (data.remark) {
							target.find("[name='originalRemarkSpan']").text(data.remark);
							target.find("[name='originalRemark']").val(data.remark);
						}
						target.find('[name=originalRemarkDiv]').show();
						element.find("[name=hold]").hide();
					}


					var template = element.find('#detailTemplate').children().clone();
					template.find("#time").text(data.createTimeStr);
					var status = PageConfig.documentStatusType.PENDING;
					template.find("#action").text(PageConfig.documentStatusType[status].action);
					template.find("#actionBy").text(data.creator);
					frag.appendChild(template.get(0));

					if (data.approvedTimeStr) {
						var template = element.find('#detailTemplate').children().clone();
						template.find("#time").text(data.approvedTimeStr);
						template.find("#action").text(PageConfig.documentStatusType[data.status].action);
						template.find("#actionBy").text(data.approvedUserid);
						template.find("#remark").html(data.remark ? data.remark : '');
						frag.appendChild(template.get(0));
					}
					element.find('#detailContainer').append(frag);
				}

				target.show();

				approveModal.append(element);

				approveModal.modal('show');
			},
			"beforeSend": function() {
				App.blockUI($("body"));
			},
			"complete": function() {
				App.unblockUI($("body"));
			},
		});

	};

	DocumentHandler.gotoView = function(element) {
		var documentId = $(element).data('id');

		$.ajax({
			type: "GET",
			url: '/manager/member/viewApproveDocument',
			data: {
				documentId: documentId
			},
			dataType: 'JSON',
			success: function(data) {
				if (data.error) {
					NotifyHandler.errorMsg(data.error);
					return;
				}
				var viewModal = $('#viewModal');
				viewModal.find('#documentId').val(data.documentId);

				var documentType = DocumentType.getInstanceOf(data.documentTypeId);
				var element = viewModal.children().detach();
				element.find('[name=bankDetail]').find('#userId').text('');
				element.find('[name=bankDetail]').hide();

				element.find('[name=documentDetail]').find('#userId').text('');
				element.find('[name=documentDetail]').hide();

				element.find('[name=onlineBankDetail]').find('#userId').text('');
				element.find('[name=onlineBankDetail]').hide();

				let target;
				if (documentType == DocumentType.BANK_STATEMENT) {
					target = element.find('[name=bankDetail]');
					target.find('[name=documentTypeStr]').text(I18N.get(documentType.getFullName()));
					target.find('#userId').text(data.userId);
					target.find('[name=bankName]').text(data.bankName);
					target.find('[name=bankBranch]').text(data.bankBranch);
					target.find('[name=bankAccName]').text(data.bankAccName);
					target.find('[name=bankAccNumber]').text(data.bankAccNumber);
					target.find('[name=financeCode]').text(data.financeCode);

					if (data.currency === CurrencyType.CNY.value) {
						target.find('[name=bankExtraData]').show();
						target.find('[name=bankProvince]').text(data.bankProvince);
						target.find('[name=bankCity]').text(data.bankCity);
					}
				} else {
					if (documentType.getGroupType() === DocumentGroupType.EWALLET.value) {
						target = element.find('[name=onlineBankDetail]');
						target.find('[name=documentNoStr]').text(I18N.get("form.text.backOffice.payment.account"));
						target.find('#documentNo').text(data.bankAccNumber);
					} else if (documentType.getGroupType() === DocumentGroupType.UPI.value) {
						target = element.find('[name=onlineBankDetail]');
						target.find('[name=documentNoStr]').text(I18N.get("fe.text.upiCode"));
						target.find('#documentNo').text(data.bankAccNumber);
					} else {
						target = element.find('[name=documentDetail]');
						target.find('[name=documentNoStr]').text(I18N.get("form.text.backOffice.documentNo"));
						target.find('#expiredDate').text(data.expiredDate);
						target.find('#documentNo').text(data.documentNo);
					}
					target.find('#userId').text(data.userId);
					target.find('[name=documentTypeStr]').text(I18N.get(documentType.getFullName()));
				}

				target.find('#frontPhotoDiv').hide();
				target.find('[name=' + DocumentType.getFrontFieldName() + ']').hide();
				target.find('[name=' + DocumentType.getFrontFieldName() + ']').attr('src', '');
				if (data.frontImageData !== null) {
					target.find('#frontPhotoDiv').show();
					target.find('[name=' + DocumentType.getFrontFieldName() + ']').show();
					target.find('[name=' + DocumentType.getFrontFieldName() + ']').attr('src', 'data:image/png;base64,' + data.frontImageData);
				}

				if (data.frontImageCrash) {
					target.find('[name=' + DocumentType.getFrontFieldName() + ']').parents('.form-group').addClass('has-error');
				}

				target.find('#backPhotoDiv').hide();
				target.find('[name=' + DocumentType.getBackFieldName() + ']').hide();
				target.find('[name=' + DocumentType.getBackFieldName() + ']').attr('src', '');
				if (data.backImageData !== null) {
					target.find('#backPhotoDiv').show();
					target.find('[name=' + DocumentType.getBackFieldName() + ']').show();
					target.find('[name=' + DocumentType.getBackFieldName() + ']').attr('src', 'data:image/png;base64,' + data.backImageData);
				}
				if (data.backImageCrash) {
					target.find('[name=' + DocumentType.getBackFieldName() + ']').parents('.form-group').addClass('has-error');
				}

				element.find('#detailContainer').empty();
				var frag = document.createDocumentFragment();

				element.find('#pastCasesDiv').hide();
				element.find('#historyDiv').show();
				{
					var template = element.find('#detailTemplate').children().clone();
					template.find("#time").text(data.createTimeStr);
					var mockStatus = PageConfig.documentStatusType.PENDING;
					template.find("#action").text(PageConfig.documentStatusType[mockStatus].action);
					template.find("#actionBy").text(data.creator);
					frag.appendChild(template.get(0));
				}
				if (data.approvedTimeStr) {
					var template = element.find('#detailTemplate').children().clone();
					template.find("#time").text(data.approvedTimeStr);
					template.find("#action").text(PageConfig.documentStatusType[data.status].action);
					template.find("#actionBy").text(data.approvedUserid);
					template.find("#remark").html(data.remark ? data.remark.replace('\r\n', '<br/>') : '');
					frag.appendChild(template.get(0));
				}
				element.find('#detailContainer').append(frag);

				target.show();

				viewModal.append(element);
				viewModal.modal('show');
			},
			"beforeSend": function() {
				App.blockUI($("body"));
			},
			"complete": function() {
				App.unblockUI($("body"));
			},
		});

	};

	DocumentHandler.reset = function() {
		var approveForm = $("[name='approveForm']");
		approveForm.get(0).reset();
		approveForm.data("validator").resetForm();
	};

	var bindCreateDocumentEvent = function() {
		$('#createDocumentModal').on('hidden.bs.modal', function(e) {
			$(this).find('#documentTypeDiv').hide();
			$(this).find('#searchHistoryDiv').hide();
			$(this).find('[id$=GeneralDiv]').hide();
			DocumentHandler.resetCreateDocument();
		});

		var createForm = $("[name='createForm']");
		createForm.find('#documentType').on('change', function() {
			createForm.find('#newBankGeneralDiv').hide();
			createForm.find('[name=bankExtraData]').hide();
			createForm.find('#newDocumentGeneralDiv').hide();
			createForm.find('#newEWalletGeneralDiv').hide();
			createForm.find('#newUPICodeGeneralDiv').hide();
			createForm.find('#newImageGeneralDiv').show();
			createForm.find('#backPhotoDiv').show();
			if ($(this).val() == DocumentType.BANK_STATEMENT.value) {
				initLocalBank();
				createForm.find('#newBankGeneralDiv').show();
				createForm.find("[name=" + DocumentType.getFrontFieldName() + "]").rules("add", {required: true});
				return;
			} else {
				createForm.find("[name=" + DocumentType.getFrontFieldName() + "]").rules("add", {required: true});

				if (DocumentType.getInstanceOf($(this).val()).getGroupType() === DocumentGroupType.EWALLET.value) {
					createForm.find('#newEWalletGeneralDiv').show();
					createForm.find('#backPhotoDiv').hide();
					return;
				} else if (DocumentType.getInstanceOf($(this).val()).getGroupType() === DocumentGroupType.UPI.value) {
					createForm.find('#newUPICodeGeneralDiv').show();
					createForm.find('#backPhotoDiv').hide();
					return;
				} else if ($(this).val() == DocumentType.HEADSHOT.value) {
					createForm.find('#backPhotoDiv').hide();
					return;
				}
			}
			createForm.find('#newDocumentGeneralDiv').show();
		});

		createForm.on('keyup', '[name=documentNo], [name=bankAccNumber], [name=financeCode]', function(event) {
			const keyCode = event.keyCode ? event.keyCode : event.which;
			this.value = this.value.replace(/\s+|\s+$/g, '');
			if (keyCode === 32 || keyCode === 229) return false;
		});
	};

	var bindUnlockDocumentEvent = function() {
		$('#approveModal').on('hidden.bs.modal', function(e) {
			var approveForm = $("[name='approveForm']");
			approveForm.find(".remove").trigger('click');
			var documentId = approveForm.find("[name='documentId']").val();
			$.ajax({
				type: "POST",
				url: '/manager/member/unlockDocument',
				dataType: 'JSON',
				data: {
					documentId: documentId
				},
				success: function(data) {
					if (data.error) {
						NotifyHandler.errorMsg(data.error);
						return;
					}
					dataTableOptions.dataTableRef.fnDraw(dataTableOptions.dataTableRef.fnSettings());
				}
			});
		});

	};

	DocumentHandler.lockContinuousClick = false;
	var bindViewDocumentEvent = function() {

		$('#viewDocumentModal').on('hidden.bs.modal', function(e) {
			cleanViewDocument();
		});

		$('[name=viewOriginalPhoto]').on('click', function(event) {

			event.preventDefault();
			event.stopPropagation();


			if (DocumentHandler.lockContinuousClick === true) {
				return;
			}

			DocumentHandler.lockContinuousClick = true;

			let element = $(event.target);
			if (element[0].className === 'icon-cloud-download') {
				element = element.closest('button');
			}
			// element[0].setAttribute('disabled', 'disabled');
			element.addClass('loading');

			cleanViewDocument();

			const form = element.closest('form');
			const documentId = form.find('[name=documentId]').val();
			const userId = form.find('#userId').text();
			const photoType = element.attr('data-name');

			$.ajax({
				type: "POST",
				url: '/manager/member/viewOriginalPhoto',
				data: {
					"documentId": documentId,
					"userId": userId,
					"photoType": photoType
				},
				success: function(data) {
					if (data.error) {
						NotifyHandler.errorMsg(data.error);
						return;
					}

					const $viewDocumentModal = $('#viewDocumentModal');

					let dataImage = data.image;
					if (dataImage) {
						const originalImage = new Image();
						originalImage.src = 'data:image/png;base64,' + dataImage;
						$viewDocumentModal.find('#image').append(originalImage);
					} else {
						$viewDocumentModal.find('#image').append(`<span>${I18N.get("msg.info.profile.noDocumentImage")}</span>`);
					}
					$viewDocumentModal.find('#forOriginalPhoto').show();

					$viewDocumentModal.modal('show');
				}, complete: function() {
					// element[0].removeAttribute("disabled");
					element.removeClass('loading');

					setTimeout(function() {
						DocumentHandler.lockContinuousClick = false;
					}, 1000);
				}
			});
		});
	};

	DocumentHandler.gotoUnlockDocument = function(element) {
		var documentId = $(element).data('id');
		var executor = $(element).data('executor');
		$.ajax({
			type: "POST",
			url: '/manager/member/removeDocumentExecutor',
			dataType: 'JSON',
			data: {
				documentId: documentId,
				executor: executor
			},
			success: function(data) {
				if (data.error) {
					NotifyHandler.errorMsg(data.error);
				}
				dataTableOptions.dataTableRef.fnDraw(dataTableOptions.dataTableRef.fnSettings());
			}
		});
	};

	DocumentHandler.resetCreateDocument = function() {
		var createForm = $("[name='createForm']");
		createForm.get(0).reset();
		createForm.data("validator").resetForm();
		createForm.find('.remove').trigger('click');
		// createForm.find('.fileinput-preview').text('No file selected...');

		loadCreateBank();
	};

	DocumentHandler.create = function() {
		var createForm = $("[name='createForm']");
		if (!createForm.valid()) {
			return;
		}

		var searchProfileDocumentForm = $("[name='searchProfileDocumentForm']");

		var imgData = new FormData();
		imgData.append('userId', searchProfileDocumentForm.find("[name='userId']").val());
		imgData.append('currencyTypeId', DocumentHandler.accountCurrency);
		imgData.append(DocumentType.getFrontFieldName(), createForm.find('[name=' + DocumentType.getFrontFieldName() + ']').prop('files')[0]);
		if (createForm.find('[name=' + DocumentType.getBackFieldName() + ']').prop('files')[0] !== undefined) {
			imgData.append(DocumentType.getBackFieldName(), createForm.find('[name=' + DocumentType.getBackFieldName() + ']').prop('files')[0]);
		}
		imgData.append('documentType', createForm.find("[name='documentType']").val());
		imgData.append('expiredDate', createForm.find("[name='expiredDate']").val());
		// for bank card
		if (createForm.find("[name='documentType']").val() == DocumentType.BANK_STATEMENT.unique()) {
			imgData.append('bankId', createForm.find("[name='bankId']").val());
			imgData.append('bankBranch', createForm.find("[name='bankBranch']").val());
			imgData.append('bankAccName', createForm.find("[name='bankAccName']").val());
			imgData.append('bankAccNumber', createForm.find("[name='bankAccNumber']").val());
			imgData.append('financeCode', createForm.find("[name='financeCode']").val());
			imgData.append('remark', createForm.find("[name='remark']").val());
			if (DocumentHandler.accountCurrency === CurrencyType.CNY.value) {
				imgData.append('createBankProvince', createForm.find("[name='createBankProvince']").val());
				imgData.append('createBankProvinceName', createForm.find("[name='createBankProvinceName']").val());
				imgData.append('createBankCity', createForm.find("[name='createBankCity']").val());
				imgData.append('createBankCityName', createForm.find("[name='createBankCityName']").val());
			}
		} else {
			imgData.append('documentNo', $('[id^="new"]:visible').find('[name=documentNo]').val());
			if (createForm.find("[name='documentType']").val() == DocumentType.UPI.unique()) {
				imgData.append('upiCode', $('[id^="new"]:visible').find('[name=upiCode]').val());
			}
		}

		const $modal = $('#createDocumentModal');

		$.ajax({
			type: "POST",
			url: '/manager/member/createDocument',
			dataType: 'JSON',
			data: imgData,
			processData: false,
			contentType: false,
			cache: false,
			enctype: "multipart/form-data",
			success: function(data) {
				if (data.error) {
					NotifyHandler.errorMsg(data.error);
					return;
				}

				NotifyHandler.successMsg(data.message);

				DocumentHandler.resetCreateDocument();
				$modal.modal('hide');
				if (dataTableOptions === undefined) {
					DocumentHandler.search();
					return;
				}
				dataTableOptions.dataTableRef.fnDraw(dataTableOptions.dataTableRef.fnSettings());
			},
			beforeSend: function() {
				$modal.find('[name=save]').addClass('loading');
				$modal.find('[name=save]').removeAttr('onclick');
				$modal.find('[name=resetButton]').addClass('loading');
				$modal.find('[name=resetButton]').removeAttr('onclick');
			},
			complete: function() {
				$modal.find('[name=save]').removeClass('loading');
				$modal.find('[name=save]').attr('onclick', 'DocumentHandler.create()');
				$modal.find('[name=resetButton]').removeClass('loading');
				$modal.find('[name=resetButton]').attr('onclick', 'DocumentHandler.resetCreateDocument()');
			}
		});
	};

	DocumentHandler.resetUpdateDocument = function() {
		var viewDocumentForm = $("[name='viewDocumentForm']");
		viewDocumentForm.get(0).reset();
		viewDocumentForm.data("validator").resetForm();
	};

	DocumentHandler.update = function() {
		var viewDocumentForm = $("[name='viewDocumentForm']");
		if (!viewDocumentForm.valid()) {
			return;
		}

		var imgData = new FormData();
		imgData.append('id', viewDocumentForm.find("[name='documentId']").val());
		imgData.append('newImage', viewDocumentForm.find('[name=newAttachment]').prop('files')[0]);
		imgData.append('documentNo', viewDocumentForm.find("[name='documentNo']").val());
		imgData.append('remark', viewDocumentForm.find("[name='remark']").val());

		$('#viewModal').find("[name='approve']").attr('disabled', true);

		$.ajax({
			type: "POST",
			url: '/manager/member/updateDocument',
			dataType: 'JSON',
			data: imgData,
			processData: false,
			contentType: false,
			cache: false,
			enctype: "multipart/form-data",
			success: function(data) {
				if (data.error) {
					NotifyHandler.errorMsg(data.error);
					return;
				}
				if (data.status === '500') {
					NotifyHandler.errorMsg(data.message);
					return;
				}
				NotifyHandler.successMsg(data.message);
				DocumentHandler.resetUpdateDocument();
				$('#viewModal').modal('hide');
				if (dataTableOptions === undefined) {
					DocumentHandler.search();
					return;
				}
				dataTableOptions.dataTableRef.fnDraw(dataTableOptions.dataTableRef.fnSettings());
			}, complete: function() {
				$('#viewModal').find("[name='approve']").attr('disabled', false);
			}
		});
	};

	var bindFile = function() {
		var createForm = $("[name='createForm']");

		createForm.find("input[type=file]").change(function() {
			readImageURL(this);
		});

		createForm.find(".remove").click(function() {
			let file = $(this).parent().closest('.input-group-btn').find("input[type=file]");
			createForm.find('[name=' + file[0].name + ']').val('');
			createForm.find('[name=' + file[0].name + '_preview]').attr('src', '').hide();
		});

	};

	const bindRemarkEvent = function() {
		$('#approveDocumentStoredRemark').on('change', function() {
			if (parseInt($(this).val()) === -1) {
				$('#approveModal').find('[name=documentDetail]').find('[name=remark]').val('');
			} else {
				$('#approveModal').find('[name=documentDetail]').find('[name=remark]').val($(this).val());
			}
		});

		$('#approveBankStoredRemark').on('change', function() {
			if (parseInt($(this).val()) === -1) {
				$('#approveModal').find('[name=bankDetail]').find('[name=remark]').val('');
			} else {
				$('#approveModal').find('[name=bankDetail]').find('[name=remark]').val($(this).val());
			}
		});
	};

	function readImageURL(input) {
		if (input.files && input.files[0]) {

			var createForm = $("[name='createForm']");

			if (input.files[0].size > (5 * 1024 * 1024)) { // 500K
				NotifyHandler.errorMsg(I18N.get("msg.error.info.image.sizeIsLarge"));
				return;
			}

			var reader = new FileReader();

			reader.onload = function(e) {
				const image = createForm.find('[name=' + input.name + '_preview]');
				image.attr('src', e.target.result);
				image.show();
			}

			reader.readAsDataURL(input.files[0]);
		}
	};

	const cleanViewDocument = function() {
		const $viewDocumentModal = $('#viewDocumentModal');
		$viewDocumentModal.find('#front').empty();
		$viewDocumentModal.find('#back').empty();
		$viewDocumentModal.find('#image').empty();

		$viewDocumentModal.find('#forOriginalPhoto').hide();
		$viewDocumentModal.find('#forCreateDocument').hide();
	};

})();



if (typeof (AdjustmentHandler) == 'undefined') {
	AdjustmentHandler = {};
}

(function () {

	AdjustmentHandler.init = function () {
		CurrencyUtil.updateSetting({
			"precision": 2,
			"trailingZeros": true,
			"roundingMode": CurrencyUtil.roundingMode.HALF_UP
		});

		bindDate();
		onChangeCurrency();
		initSelect2SearchUserId();
	};

	var onChangeCurrency = function () {
		$("#currencyType").bind("change", function () {
			var vipDom = $("#vipLevel")[0];
			vipDom.length = 0;
			vipDom.options.add(new Option(I18N.get('ui.text.report.all'), "-1"));

			var selectedCurrency = $(this).val();
			var vips = PageConfig.vipNameByCurrency[selectedCurrency];
			if (vips) {
				vips.sort(function (a, b) {
					return a.id - b.id
				});
				vips.forEach(function (vip) {
					vipDom.options.add(new Option(vip.name, vip.id));
				});
			}
		});
	};

	let bindDate = function () {
		$('.singleDateTimePicker').each2(function () {
			var $input = $(this);
			var id = $input.attr('id');

			var dateOptions = {
				"maxDate": PageConfig.date.today,
				"autoUpdateInput": false
			}

			if (id === "createDateStart") {
				dateOptions.startDate = PageConfig.date.todayOneMonthAgo;
			}

			DateRangeHandler.singleDateTimePicker(dateOptions, PageConfig.lang);

			if (id === "createDateStart") {
				$input.val(PageConfig.date.todayOneMonthAgo);
			}

			DateRangeHandler.bindEvent();
		});
	}

	let initSelect2SearchUserId = function () {
		var options = {
			minimumInputLength: 3,
			multiple: false,
			ajaxPath: '/manager/payment/queryAllUserId',
			ajaxParam: {
				currency: function () {
					return $("[name='searchForm'] #currencyType").val();
				},
				selectUserId: function () {
					return $("[name='searchUserIdForm'] #searchUserId").val();
				},
			},
			initSelection: function (element, callback) {
				callback($.map(element.val().split(','), function (id) {
					return {id: id, text: id};
				}));
			}
		};

		Select2Handler.search(options, JsCache.get('#searchUserId'), true);

		$('#searchUserId').change(function () {
			if ($(this).val() === '') {
				return;
			}
			AdjustmentHandler.getAdjustmentInfoForCreate();
		});
	};

	var dataTableOptions;
	AdjustmentHandler.search = function () {
		if (!dataTableOptions) {
			dataTableOptions = {
				tableSelector: '#adjustmentSearchTable',
				formSelector: "[name='searchForm']", //optional
				sAjaxSource: '/manager/payment/searchAdjustment',
				aaSorting: [[5, "desc"]],
				excludeColVis: [0],
				iDisplayLength: PageConfig.pageSize,
				aoColumns: [
					{
						"mData": null,
						"bSortable": false,
					},
					{
						"mData": "userId",
						"mRender": function (data, type, full) {
							if (PageConfig.enableProfile && type === 'display') {
								return "<a style='text-decoration: underline;' id='userId' onclick='AdjustmentHandler.openProfile(this);' href='javascript: void(0);'>" + data + "</a>"
									+ "<input type='hidden' id='currency' value='" + full.currencyTypeId + "'>";
							}
							return data;
						},
					},
					{
						"mData": "id",
						"mRender": function (data, type, full) {
							if (PageConfig.enableProfile && type === 'display') {
								return `<a style='text-decoration: underline;' data-id='${data}' data-userid='${full.userId}' data-amount='${full.amount}' data-createdtime='${full.createTimeStr}' onclick='AdjustmentHandler.gotoDetail(this);' href='javascript: void(0);'>${data}</a>`;
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
						"mData": "creator"
					},
					{
						"mData": "createTimeStr"
					},
				],
				aoColumnDefs: [{"sClass": "dt-body-right", "aTargets": [3]}]
			};
		}

		var callBack = function () {
			if (PageConfig.enableExportExcel) {
				DataTableHandler.addToolBar(dataTableOptions.tableSelector, $('#exportButton').clone().html().trim());
			}
		}

		var isEnableSequence = true;
		dataTableOptions.dataTableRef = DataTableHandler.create(dataTableOptions, isEnableSequence, PageConfig.lang, callBack);

		var resultTable = $('#resultTable');
		resultTable.show();
	};

	AdjustmentHandler.exportExcel = function () {
		const visibleColumns = ColVisHandler.getVisibleColumns(dataTableOptions.dataTableRef);

		const searchForm = $("[name='searchForm']");

		var createDateIsRequire = false;
		if (searchForm.find("[name=createDateStart]").val() || searchForm.find("[name=createDateEnd]").val()) {
			createDateIsRequire = true;
		}
		searchForm.find("[name=createDateStart]").rules("add", {required: createDateIsRequire});
		searchForm.find("[name=createDateEnd]").rules("add", {required: createDateIsRequire});

		if (!searchForm.valid()) {
			return;
		}

		searchForm.find('[name=visibleColumns]').val(visibleColumns);

		ExcelUtils.schedule(ReportExportType.ADJUSTMENT, '/manager/payment/exportAdjustment', searchForm.serialize(), $('[name=export]'));

		// ExcelUtils.exportExcelBinary('/manager/payment/exportAdjustment?' + searchForm.serialize() + "&visibleColumns=" + visibleColumns, 'PaymentAdjustmentReport.xlsx');
	};

	AdjustmentHandler.openProfile = function (pElement) {
		var element = $(pElement).closest('tr');
		var panel = window.open("/page/manager/member/profile.jsp?userId=" + element.find('#userId').text() + "&currency=" + element.find('#currency').val(), "_winPROFILE", "width=1200,height=640,top=5,left=5," +
			"toolbar=no,menubar=no,resizable=no,scrollbars=yes,status=no,location=no,directories=no");
		if (panel) {
			panel.focus();
		}
	};

	AdjustmentHandler.gotoCreateAdjustment = function () {
		var searchUserIdForm = $('[name=searchUserIdForm]');
		if (!searchUserIdForm.data("validator")) {
			searchUserIdForm.validate({
				ignore: [] // Don't ignore hidden fields (Select2)
			});

			searchUserIdForm.find("[name='searchUserId']").rules("add", {
				required: true,
				maxlength: 50,

			});
		}
		searchUserIdForm.get(0).reset();
		searchUserIdForm.data("validator").resetForm();
		searchUserIdForm.find('#searchUserId').val(null).trigger('change'); // this will trigger $('#searchUserId').change

		var createForm = $("[name='createForm']");
		if (!createForm.data("validator")) {
			createForm.validate();

			//notZero 不能0，但可以負
			createForm.find("[name='amount']").rules("add", {
				required: true,
				DBNumber: [20, 2],
				notZero: I18N.get('msg.error.validation.notZero.adjustmentAmount')
			});
		}
		createForm.get(0).reset();
		createForm.data("validator").resetForm();
		createForm.find("[name='userId']").val('');
		createForm.find("[name='amount']").val('');
		createForm.find("#balance").text('');
		createForm.find('#generalDiv').hide();

		$('#createAdjustmentModal').modal('show');
	};

	AdjustmentHandler.resetCreateAdjustment = function () {
		var searchUserIdForm = $("[name='searchUserIdForm']");
		searchUserIdForm.get(0).reset();
		searchUserIdForm.data("validator").resetForm();
		searchUserIdForm.find('#searchUserId').val(null).trigger('change'); // this will trigger $('#searchUserId').change

		var createForm = $("[name='createForm']");
		createForm.get(0).reset();
		createForm.find("[name='userId']").val('');
		createForm.find("[name='amount']").val('');
		createForm.find("#balance").text('');
		createForm.find('#generalDiv').hide();
	};

	AdjustmentHandler.getAdjustmentInfoForCreate = function () {
		var searchUserIdForm = $("[name='searchUserIdForm']");
		if (!searchUserIdForm.data("validator")) {
			searchUserIdForm.validate({
				ignore: [] // Don't ignore hidden fields (Select2)
			});
			searchUserIdForm.find("[name='searchUserId']").rules("add", {
				required: true,
				maxlength: 50,
			});
		}
		if (!searchUserIdForm.valid()) {
			return;
		}

		var userId = searchUserIdForm.find("[name='searchUserId']").val();
		$.ajax({
			type: "POST",
			url: '/manager/payment/getAdjustmentInfoForCreate',
			dataType: 'JSON',
			data: {
				userId: userId,
			},
			success: function (data) {
				if (data.error) {
					NotifyHandler.errorMsg(data.error);
					return;
				}

				var generalDiv = $('#generalDiv');
				var element = generalDiv.children().detach();
				PageConfig.minAmount = data.balance * -1;
				element.find("[name='userId']").val(userId);
				element.find("[name='amount']").val('');
				element.find("#balance").html(CurrencyUtil.formatter(data.balance));
				generalDiv.append(element).show();
			}
		});
	};

	AdjustmentHandler.create = function () {
		var searchUserIdForm = $("[name='searchUserIdForm']");
		if (!searchUserIdForm.data("validator")) {
			searchUserIdForm.validate({
				ignore: [] // Don't ignore hidden fields (Select2)
			});
			searchUserIdForm.find("[name='searchUserId']").rules("add", {
				required: true,
				maxlength: 50,
			});
		}
		if (!searchUserIdForm.valid()) {
			return;
		}

		var createForm = $("[name='createForm']");
		if (!createForm.valid()) {
			return;
		}

		var userId = searchUserIdForm.find("[name='searchUserId']").val();
		var amount = createForm.find("[name='amount']").val();
		$.ajax({
			type: "POST",
			url: '/manager/payment/createAdjustment',
			dataType: 'JSON',
			data: {
				userId,
				amount
			},
			success: function (data) {
				if (data.error) {
					NotifyHandler.errorMsg(data.error);
					return;
				}
				NotifyHandler.successMsg(data.message);
				$('#createAdjustmentModal').modal('hide');
				dataTableOptions.dataTableRef.fnDraw(dataTableOptions.dataTableRef.fnSettings());
			}
		});
	};

	AdjustmentHandler.gotoDetail = function (element) {
		var id = $(element).data('id');
		var userId = $(element).data('userid');
		var amount = $(element).data('amount');
		var amountFormatted = CurrencyUtil.formatterWithCurrency(amount, PageConfig.managerCurrencySymbol);
		var createdTime = $(element).data('createdtime');
		var detailModal = $('#detailModal');
		var detailElement = detailModal.children().detach();
		detailElement.find("[name='transactionId']").text(id)
		detailElement.find("[name='userId']").text(userId);
		detailElement.find("[name='amount']").html(amountFormatted);
		detailElement.find("[name='createdTime']").text(createdTime);
		detailModal.append(detailElement);

		detailModal.modal('show');
	};

	AdjustmentHandler.validateAmount = function (element, minAmount, maxAmount) {
		const inputValue = parseFloat(element.value);
		if (isNaN(inputValue)) {
			element.value = 0;
			return;
		}

		let finalValue = inputValue;

		if (inputValue < minAmount) {
			finalValue = minAmount;
		} else if (inputValue > maxAmount) {
			finalValue = maxAmount;
		}

		element.value = parseFloat(finalValue.toFixed(2));
	}
})();

if (typeof (CMSReportHandler) == 'undefined') {
	CMSReportHandler = {};
}


(function() {
	var tabs = {
		'UpdateLog': 0
	};

	CMSReportHandler.init = function() {
		dateRangeInitial();
		CMSReportHandler.toggleTab(tabs['UpdateLog']);

	}


	CMSReportHandler.toggleTab = function(tabIdx) {
		var tabbable = $('.tabbable');

		tabbable.find("[id^='tab']").removeClass('active');
		tabbable.find('#tab' + tabIdx).addClass('active');

		tabbable.find("[id^='box_tab']").removeClass('active');
		tabbable.find('#box_tab' + tabIdx).addClass('active');

		CMSReportHandler.searchProviderUpdateLog();


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


		$("#searchProviderUpdateLogDaterange").daterangepicker(dateOption);
		$("#searchProviderUpdateLogDaterange").val(PageConfig.date.todayOneMonthAgo + " - " + PageConfig.date.today);


		$("#searchProviderUpdateLogDaterange").on('apply.daterangepicker', function(e, picker) {

			var startDate = moment(picker.startDate);
			startDate.set({second: 0});
			var endDate = moment(picker.endDate);
			endDate.set({second: 59});
			picker.setEndDate(endDate);
			// $(this).parent().find('[name="searchProviderUpdateLogDaterange"]').val(String(startDate.format('DD/MM/YYYY HH:mm:ss')) + '-' + String(endDate.format('DD/MM/YYYY HH:mm:ss')));
			this.val(String(startDate.format('DD/MM/YYYY HH:mm:ss')) + '-' + String(endDate.format('DD/MM/YYYY HH:mm:ss')))
		});
		$('#searchProviderUpdateLogDaterange').on('cancel.daterangepicker', function(e, picker) {
			$(this).val('');
			// $(this).parent().find('[name="searchProviderUpdateLogDaterange"]').val('');
			this.val("");
		});

		$("input[id^=searchProfile]").keydown(false);

		$('.fa-calendar').hide();
	};


	var dataTableOptions = [];
	CMSReportHandler.searchProviderUpdateLog = function() {
		var tabId = tabs['UpdateLog'];
		if (dataTableOptions[tabId]?.dataTableRef) {
			const table = $(dataTableOptions[tabId].tableSelector).DataTable();
			table.fnDestroy();

		}
		dataTableOptions[tabId] = {
			tableSelector: '#searchProviderUpdateLogTable',
			formSelector: "[name='searchProviderUpdateLogForm']",//optional
			sAjaxSource: '/manager/ContentManageController/getAllProvidersUpdateLogs',
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
					"mRender": function(data, type, full) {
						if (type === 'display') {
							var providerUpdateType = PageConfig.ProviderUpdateType[data];
							return I18N.get(providerUpdateType.getFullName);
						}
						return data;
					}
				},
				{
					"mData": "beforeUpdate",
					"bSortable": false,
					"mRender": function(data, type, full) {
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
					"mRender": function(data, type, full) {
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
		$(dataTableOptions[tabId].tableSelector).on('order.dt', function() {
			const order = dataTableOptions[tabId].dataTableRef.order();
			dataTableOptions[tabId].aaSorting = order;
		});

		$('#profileUpdateLogTable').show();
	}


	function getDisplayName(logType, data) {
		var displayName = '-';
		switch (logType) {
			case 1: // status
				if (data) {
					var providerStatusType = ProviderStatusType.getInstanceOf(data);
					displayName = '<span class="label ' + providerStatusType.getClassName() + '">' + I18N.get(providerStatusType.getDisplayName()) + '</span>';
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

})();
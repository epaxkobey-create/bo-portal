if (typeof (CMSGameReportHandler) == 'undefined') {
	CMSGameReportHandler = {};
}


(function() {
	var tabs = {
		'UpdateLog': 0
	};

	CMSGameReportHandler.init = function() {
		dateRangeInitial();
		CMSGameReportHandler.toggleTab(tabs['UpdateLog']);

	}


	CMSGameReportHandler.toggleTab = function(tabIdx) {
		var tabbable = $('.tabbable');

		tabbable.find("[id^='tab']").removeClass('active');
		tabbable.find('#tab' + tabIdx).addClass('active');

		tabbable.find("[id^='box_tab']").removeClass('active');
		tabbable.find('#box_tab' + tabIdx).addClass('active');

		CMSGameReportHandler.searchGameUpdateLog();


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


		$('[name$=Daterange]').daterangepicker(dateOption);
		$('[name$=Daterange]').val(PageConfig.date.todayOneMonthAgo + " - " + PageConfig.date.today);


		$('[name$=Daterange]').on('apply.daterangepicker', function(e, picker) {

			var startDate = moment(picker.startDate);
			startDate.set({second: 0});
			var endDate = moment(picker.endDate);
			endDate.set({second: 59});
			picker.setEndDate(endDate);
			$(this).parent().find('[name="searchDateRange"]').val(String(startDate.format('DD/MM/YYYY HH:mm:ss')) + '-' + String(endDate.format('DD/MM/YYYY HH:mm:ss')));

		});
		$('[name$=Daterange]').on('cancel.daterangepicker', function(e, picker) {
			$(this).val('');
			$(this).parent().find('[name="searchDateRange"]').val('');
		});

		$("input[id^=searchProfile]").keydown(false);

		$('.fa-calendar').hide();
	};


	var dataTableOptions = [];
	CMSGameReportHandler.searchGameUpdateLog = function() {
		var tabId = tabs['UpdateLog'];
		if (dataTableOptions[tabId]?.dataTableRef) {
			const table = $(dataTableOptions[tabId].tableSelector).DataTable();
			table.fnDestroy();

		}
		dataTableOptions[tabId] = {
			tableSelector: '#searchGameUpdateLogTable',
			formSelector: "[name='searchGameUpdateLogForm']",//optional
			sAjaxSource: '/manager/ContentManageController/getAllGameUpdateLogs',
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
							var GameUpdateType = PageConfig.GameUpdateType[data];
							return I18N.get(GameUpdateType.getFullName);
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

				},
				{
					"mData": "updaterIp",
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
					var gameStatusType = GameStatusType.getInstanceOf(data);
					displayName = '<span class="label ' + gameStatusType.getClassName() + '">' + I18N.get(gameStatusType.getName()) + '</span>';
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
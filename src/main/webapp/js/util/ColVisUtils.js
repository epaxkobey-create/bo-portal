if (typeof (ColVisHandler) == 'undefined') {
	ColVisHandler = {};
}

(function() {

	ColVisHandler.getVisibleColumns = function(dataTableRef) {
		var oSettings = dataTableRef.fnSettings();

		return dataTableRef.oApi._fnGetColumns(oSettings, 'bVisible');
	};

	ColVisHandler.setVisibleColumns = function(dataTableRef, i, showHide) {
		dataTableRef.fnSetColumnVis(i, showHide);
	};

	ColVisHandler.saveVisibleColumns = function(selector, dataTableRef) {
		var key;
		var index = selector.indexOf("_");
		if (index == -1) {
			key = selector.replace('#', '');
		} else {
			key = selector.slice(index + 1, selector.length);
		}
		//
		var index = ColVisHandler.getVisibleColumns(dataTableRef);
		var columnObj = {lastTimestamp: new Date().getTime(), visibleColumns: index};
		localStorage.setItem(key, JSON.stringify(columnObj));
		if (NotifyHandler) {
			NotifyHandler.successMsg("Save Success!");
			return;
		}
	};

	ColVisHandler.loadVisibleColumns = function(options) {

		var key;
		var index = options.tableSelector.indexOf("_");
		if (index == -1) {
			key = options.tableSelector.replace('#', '');
		} else {
			key = options.tableSelector.slice(index + 1, options.tableSelector.length);
		}
		var column = options.aoColumns ? options.aoColumns : options.aoColumnDefs;
		var columnObj = JSON.parse(localStorage.getItem(key));
		if (columnObj) {
			var visibleColumnsJson = columnObj.visibleColumns;
			if (!visibleColumnsJson) {
				visibleColumnsJson = columnObj;
			}
			Object.keys(column).forEach(function(idx) {
				if (!visibleColumnsJson.some(value => value == idx)) {
					column[idx].bVisible = false;
				}
			});

			var lastUpdateTime = columnObj.lastTimestamp;
			if (options.showAllColumn) {
				var lastVersionTime = DateUtil.convert(parseInt(PageConfig.version));

				if (!lastUpdateTime
					|| DateUtil.compare(lastVersionTime, DateUtil.convert(lastUpdateTime)) >= 0) {
					visibleColumnsJson = [];

					Object.keys(column).forEach(function(idx) {
						column[idx].bVisible = true;
						visibleColumnsJson.push(parseInt(idx));
					});
				}
			}

			if (!lastUpdateTime) {
				var columnObj = {lastTimestamp: new Date().getTime(), visibleColumns: visibleColumnsJson};
				localStorage.setItem(key, JSON.stringify(columnObj));
			}
		}
	};

})();
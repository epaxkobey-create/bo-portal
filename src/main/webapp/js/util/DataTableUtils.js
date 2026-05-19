if (typeof (DataTableHandler) == 'undefined') {
	//"aoColumnDefs": null，針對aTargets做設定，可對單一、多欄、或搭配th的class name的自定義類型做設定
	//注:用這個來簡化需要flat source

	//儲存每一欄要用的資訊，數量需和table欄位數相同
//	DataTable.defaults的"aoColumns": null,
//	DataTable.models.oSettings的"aoColumns": []，和上面一樣，上面只是default時給個名字而以
//	1.10.x叫做columns，搭配data，舊版的叫aoColumns，搭配mData

	//"mData": function ( source, type, val )，分別客制sort、filter、display使用的值
	//mData後面接int，等於row index?要的話再試
	//mData後面接null，會搭配sDefaultContent使用
	//mData的source
	//mData的type是set或 'filter', 'display', 'type', 'sort' or undefined，最後這個undefined是DataTable一開始取出這個值來用的時候，DataTable內部程式使用的
	//mData的val，對應index的資料吧，看不懂
	//"mRender": function ( data, type, full )，和mData相比沒有set，但比較簡單
	//mRender的data是mData改過的值
	//mRender的type是 'filter', 'display', 'type' or 'sort'
	//mRender的full是source

//	部分render流程，這個type應該是get的意思
//	1.mData undefined
//	2.mData set
//	3.mData type
//	4.mRender type val
//	5.mData display
//	6.mRender display d+val
//	[
//		7.mData display
//		8.mRender display d+val
//		9.fnCreatedCell
//	]

	//向下箭頭，由大到小、desc
	//向上箭頭，由小到大、asc

//	原始程式會做的部分
//	"aaSorting": [[0,'asc']],
//	iDisplayLength : 10,
	DataTableHandler = {};
}

(function() {

	//資料經由AJax取值之後渲染畫面
	DataTableHandler.create = function(pOptions, isEnableSequence, lang, callback, hideHeader, stateChangeCallback) {
		//loadVisibleColumns from localStorage
		ColVisHandler.loadVisibleColumns(pOptions);

		//把共用的放這裡，然後直接呼叫原生的
		var options = {
			"sDom": '<"row-fluid"<"datatableToolbar"><"saveColumnsBtn">C<"span6"l><"span6"f>r>t<"row-fluid"<"span6"i><"span6"p>>',
			"oColVis": {
//				"buttonText": setColVisButtonText(),
				"iOverlayFade": 0,
				"aiExclude": pOptions.excludeColVis, // 右邊Columns下拉選單會忽略的欄位
			},
//			"oLanguage": language(lang, pOptions.tableSelector),
			"bRetrieve": true,
			"bServerSide": true,
//			"bLengthChange": false,
			"bFilter": false,
			"fnServerData": function(sSource, aoData, fnCallback, oSettings) {
				var aaSettings = this.fnSettings();
				var aaSorting = aaSettings.aaSorting;//[0][2]回傳值的定義，0:asc，1:desc

				var formData = [];
				if (pOptions.formSelector) {
					var form = $(pOptions.formSelector);
					if (form.data("validator") && !form.valid()) {
						return;
					}

					//force reinit page
					//1.add text input reinitPage to form
					//2.set true when trigger caller event
					//3.fnPageChange when start, then set flag to false
					//4.return to avoid call backend twice
					if (form.find("[name='reinitPage']").val() == 'true') {
						form.find("[name='reinitPage']").val('false');
						this.fnPageChange('first');
						return;
					}

					formData = form.serializeArray();
				}
				if (options.dropdownOptions && options.dropdownOptions.length > 0) {
					var dropdownId = options.tableSelector.replace('#', '') + '_dropdown';
					var dropdownValue = $('#' + dropdownId).val();

					formData.push({name: 'filterBy', value: dropdownValue ?? options.dropdownOptions[0].value});

				}


				//分頁
				formData.push({name: 'pageNumber', value: (this.fnPagingInfo().iPage + 1)});
				formData.push({name: 'pageSize', value: aaSettings._iDisplayLength});

				//排序
				formData.push({name: 'sortCondition', value: aaSorting[0][0]});
				formData.push({name: 'sortName', value: aaSettings.aoColumns[aaSorting[0][0]].mData});
				formData.push({name: 'sortOrder', value: aaSorting[0][2]});
				formData.push({name: 'searchText', value: oSettings.oPreviousSearch.sSearch});

				oSettings.jqXHR = $.ajax({
					"dataType": 'json',
					"type": "GET",
					url: sSource,
					data: formData,
					"success": function(data) {
						if (data.error) {
							if (NotifyHandler) {
								NotifyHandler.errorMsg(data.error);
								App.unblockUI($("body"));
								return;
							}
						}
						fnCallback(data);
						if (callback) {
							callback(data);
						}
					},
					"beforeSend": function() {
						App.blockUI($("body"));
					},
					"complete": function() {
						App.unblockUI($("body"));
					},
					"error": function(data) {
						console.log(data);
					}
				});
			},

		};


		if (pOptions) {
			if (pOptions.hideColVis === true) {
				options.sDom = '<"row-fluid"<"datatableToolbar"><"span6"l><"span6"f>r>t<"row-fluid"<"span6"i><"span6"p>>';
			}
			$.extend(options, pOptions);
			setLanguage(options, lang);
		}

		//自定義部分，這樣寫沒辦法覆蓋fnRowCallback
		if (!pOptions.fnRowCallback) {
			options.fnRowCallback = function(nRow, aData, iDisplayIndex, iDisplayIndexFull) {
				if (isEnableSequence) {
					var numStart = this.fnPagingInfo().iStart;
					var index = numStart + iDisplayIndexFull + 1;
					$('td:eq(0)', nRow).html(index);
				}
				if (hideHeader) {
					$(options.tableSelector + ' thead').remove();
				}
			};
		}

		options.fnInitComplete = function(oSettings, json) {
			var $input = $("div.dataTables_filter input");
//			$input.unbind(); // 自動filter移除 (bServerSide = true 才有作用)
			$input.on('keyup keypress change', function() {
				result.fnFilter($input.val());
			});
			$('div.dataTables_filter .input-group-addon').css('cursor', 'pointer').click(function(e) {
				result.fnFilter($input.val());
			});
			$input.unbind('keydown').keydown(function(k) {
				var keyCode = k.which;
				if (KeyEventUtils.isEnterKey(keyCode)) {
					WindowEventUtil.stopEvent(k, false, true);
					result.fnFilter($(this).val());
				}
			});
		};

		//Columns下拉選單 Change Callback
		if (stateChangeCallback) {
			options.oColVis.fnStateChange = function(i, showHide, that) {
				stateChangeCallback(i, showHide);
			}
		}

		var oldFnDrawCallback = $.fn.dataTable.defaults.fnDrawCallback;
		$.extend(true, options, {
			fnDrawCallback: function(oSettings) {

				if (pOptions.dropdownOptions) {
					console.log("Adding dropdown with options:", pOptions.dropdownOptions);
					DataTableHandler.addDropdown(options.tableSelector, pOptions.dropdownOptions);
				}
				// DataTableHandler.addSaveColumnsBtn(options.tableSelector);
				if (hideHeader === true || pOptions.hideColVis === true) {
					return;
				}

				if (oldFnDrawCallback) {
					// Extending function
					oldFnDrawCallback.apply(this, oSettings);
				}
			}
		});

		if (options.dataTableRef !== undefined) {
			options.dataTableRef.fnClearTable();
		}

		result = $(options.tableSelector).DataTable(options);

		return result;
	};

	//資料自己丟進來渲染畫面 , 有ExcelButton, 無下拉隱藏欄位選單
	DataTableHandler.createByData = function(pOptions, isEnableSequence, datatableToolbar, lang) {
		if (pOptions.dataTableRef !== undefined) {
			DataTableHandler.refreshByData(pOptions.dataTableRef, pOptions.aaData);
			return pOptions.dataTableRef;
		}
		let options = {
			bRetrieve: true,
			bFilter: false,
			bPaginate: false,
			bInfo: false,
			bDestroy: true,
			oLanguage: language(lang, pOptions.tableSelector),
		};
		if (pOptions) {
			$.extend(options, pOptions);
		}
		if (datatableToolbar) {
			$.extend(options, {
				"sDom": '<"row-fluid"<"datatableToolbar"><"span6"l><"span6"f>r>t<"row-fluid"<"span6"i><"span6"p>>'
			});
		}

		if (isEnableSequence) {
			options.fnRowCallback = function(nRow, aData, iDisplayIndex, iDisplayIndexFull) {
				var numStart = this.fnPagingInfo().iStart;
				var index = numStart + iDisplayIndexFull + 1;
				$('td:eq(0)', nRow).html(index);
			};
		}

		var oldFnDrawCallback = $.fn.dataTable.defaults.fnDrawCallback;
		$.extend(true, options, {
			fnDrawCallback: function(oSettings) {
				if (datatableToolbar) {
					DataTableHandler.addToolBar(options.tableSelector, datatableToolbar);
				}
				if (oldFnDrawCallback) {
					// Extending function
					oldFnDrawCallback.apply(this, oSettings);
				}
			}
		});

		return $(options.tableSelector).DataTable(options);
	};

	//資料自己丟進來渲染畫面 , 有下拉隱藏欄位選單
	DataTableHandler.createByDataWithColumns = function(pOptions, isEnableSequence, datatableToolbar, lang, stateChangeCallback, detroy) {
		if (pOptions.dataTableRef !== undefined) {
			DataTableHandler.refreshByData(pOptions.dataTableRef, pOptions.aaData);
			return pOptions.dataTableRef;
		}
		//loadVisibleColumns from localStorage
		ColVisHandler.loadVisibleColumns(pOptions);

		var options = {
			//有沒有C，影響有沒有Colums下拉選單
			"sDom": '<"row-fluid"<"datatableToolbar"><"saveColumnsBtn">C<"span6"l><"span6"f>r>t<"row"<"dataTables_footer clearfix"<"col-md-6"i><"col-md-6"p>>>',
			"bRetrieve": true,
			"bFilter": false,
			"oColVis": {
				"buttonText": setColVisButtonText(),
				"iOverlayFade": 0,
				"aiExclude": pOptions.excludeColVis, // 右邊Columns下拉選單會忽略的欄位
//				"activate" : pOptions.activate
			},
			bPaginate: false,
			bInfo: false,
			bDestory: true,
			oLanguage: language(lang, pOptions.tableSelector),
		};

		if (pOptions) {
			$.extend(true, options, pOptions);
		}

		if (isEnableSequence) {
			options.fnRowCallback = function(nRow, aData, iDisplayIndex, iDisplayIndexFull) {
				var numStart = this.fnPagingInfo().iStart;
				var index = numStart + iDisplayIndexFull + 1;
				$('td:eq(0)', nRow).html(index);
			};
		}

		//Columns下拉選單 Change Callback
		if (stateChangeCallback) {
			options.oColVis.fnStateChange = function(i, showHide, that) {
				stateChangeCallback(i, showHide, pOptions.dataTableRef);
			}
		}

		var oldFnDrawCallback = $.fn.dataTable.defaults.fnDrawCallback;
		$.extend(true, options, {
			fnDrawCallback: function(oSettings) {
				DataTableHandler.addSaveColumnsBtn(options.tableSelector);
				if (datatableToolbar) {
					DataTableHandler.addToolBar(options.tableSelector, datatableToolbar);
				}
				if (oldFnDrawCallback) {
					// Extending function
					oldFnDrawCallback.apply(this, oSettings);
				}
			}
		});

		return $(options.tableSelector).DataTable(options);
	};

	//FraudTool by User 專用, 從資料第二列開始塞
	DataTableHandler.createByDataSkipFirst = function(pOptions, isEnableSequence, datatableToolbar, lang, stateChangeCallback) {
//		if (pOptions.dataTableRef !== undefined) {
//			DataTableHandler.refreshByData(pOptions.dataTableRef, pOptions.aaData);
//			return pOptions.dataTableRef;
//		}
		//loadVisibleColumns from localStorage
		var visibleColumns;
		ColVisHandler.loadVisibleColumns(pOptions);

		if (pOptions.dataTableRef !== undefined) {
			visibleColumns = ColVisHandler.getVisibleColumns(pOptions.dataTableRef);
		}
		var options = {
			"sDom": '<"row-fluid"<"datatableToolbar"><"saveColumnsBtn">C<"col-md-6"l><"col-md-6"f>r>t<"row"<"dataTables_footer clearfix"<"col-md-6"i><"col-md-6"p>>>',
			"bRetrieve": true,
			"bFilter": false,
			"oColVis": {
				"buttonText": setColVisButtonText(),
				"iOverlayFade": 0,
				"aiExclude": pOptions.excludeColVis, // 要隱藏的欄位, 右邊Columns下拉選單使用
			},
			bPaginate: false,
			bInfo: false,
			bDestory: true,
			oLanguage: language(lang, pOptions.tableSelector),
		};
		if (pOptions) {
			if (pOptions.hideColVis === true) {
				options.sDom = '<"row-fluid"<"datatableToolbar"><"span6"l><"span6"f>r>t<"row-fluid"<"span6"i><"span6"p>>';
			}
			$.extend(options, pOptions);
		}

		if (isEnableSequence) {
			options.fnRowCallback = function(nRow, aData, iDisplayIndex, iDisplayIndexFull) {
				$('td:eq(0)', nRow).html(iDisplayIndexFull + 2);
			};
		}

		//Columns下拉選單 Change Callback
		if (stateChangeCallback) {
			options.oColVis.fnStateChange = function(i, showHide, that) {
				stateChangeCallback(i, showHide);
			}
		}

		var oldFnDrawCallback = $.fn.dataTable.defaults.fnDrawCallback;
		$.extend(true, options, {
			fnDrawCallback: function(oSettings) {
				DataTableHandler.addSaveColumnsBtn(options.tableSelector);
				if (datatableToolbar) {
					DataTableHandler.addToolBar(options.tableSelector, datatableToolbar);
				}
				if (oldFnDrawCallback) {
					// Extending function
					oldFnDrawCallback.apply(this, oSettings);
				}
			}
		});

		if (options.dataTableRef !== undefined) {
			options.dataTableRef.fnClearTable();
			options.dataTableRef.fnDestroy();
		}
		let result = $(options.tableSelector).DataTable(options);

		//Load visibleColumns
		if (pOptions.dataTableRef !== undefined) {
			var column = options.aoColumns ? options.aoColumns : options.aoColumnDefs;
			Object.keys(column).forEach(function(idx) {
				if (!visibleColumns.some(value => value == idx)) {
					ColVisHandler.setVisibleColumns(result, idx, false);
				}
			});
		}

		return result;
	};

	DataTableHandler.addToolBar = function(selector, datatableToolbar) {
		var btnId = selector.replace('#', '') + '_btn';
		$(selector + "_wrapper").find('div.datatableToolbar:not([id])').attr('id', btnId);
		$('#' + btnId).css({
			'float': 'left',
			'margin-right': '10px',
			'margin-bottom': '7px'
		}).html(datatableToolbar);
	}

	DataTableHandler.addMultiToolBar = function(selector, dataTableToolbars) {
		let btnId = selector.replace('#', '') + '_btn';
		$(selector + "_wrapper").find('div.datatableToolbar:not([id])').attr('id', btnId);
		let html = dataTableToolbars.map(eachToolbar => {
			eachToolbar.children().css({
				'float': 'left',
				'margin-right': '10px',
				'margin-bottom': '7px'
			});
			return eachToolbar.html().trim();
		}).reduce((a, b) => a + b);
		$('#' + btnId).html(html);
	}

	DataTableHandler.addSaveColumnsBtn = function(selector) {
		var btnId = selector.replace('#', '') + '_saveColumnsBtn';
		var btnValue;
		if (I18N.get("form.text.backOffice.datatable.save") == "form.text.backOffice.datatable.save") {
			btnValue = "Save";  // 預設 EN
		} else {
			btnValue = I18N.get("form.text.backOffice.datatable.save");
		}
		var saveColumnsBtn = '<input type="button" value="' + btnValue + '" name="saveVisibleColumns" onclick="ColVisHandler.saveVisibleColumns(\'' + selector + '\',$(\'' + selector + '\').DataTable())" class="btn btn-primary">';

		$(selector + "_wrapper").find('div.saveColumnsBtn:not([id])').attr('id', btnId);
		$('#' + btnId).css({
			'float': 'right',
			'margin-left': '10px',
			'margin-bottom': '7px'
		}).html(saveColumnsBtn);
	};
	DataTableHandler.addDropdown = function(selector, dropdownOptions) {
		var dropdownId = selector.replace('#', '') + '_dropdown';

		if (!dropdownOptions || dropdownOptions.length <= 0) {
			console.error("Invalid dropdownOptions provided");
			return;
		}

		var existingDropdown = $('#' + dropdownId);

		// 如果下拉框已存在，只更新选项
		if (existingDropdown.length > 0) {
			console.log("Updating existing dropdown options");

			// 保存当前选中的值
			var currentValue = existingDropdown.val();

			// 检查选项是否真的需要更新
			var needsUpdate = false;
			var currentOptions = existingDropdown.find('option');

			if (currentOptions.length !== dropdownOptions.length) {
				needsUpdate = true;
			} else {
				dropdownOptions.forEach(function(option, index) {
					var currentOption = currentOptions.eq(index);
					if (currentOption.val() !== option.value || currentOption.text() !== option.text) {
						needsUpdate = true;
					}
				});
			}

			// 如果选项没有变化，直接返回
			if (!needsUpdate) {
				console.log("Options unchanged, skipping update");
				return dropdownId;
			}

			// 设置标志位，防止触发change事件
			existingDropdown.data('updating', true);

			// 清空并重新添加选项
			existingDropdown.empty();
			dropdownOptions.forEach(function(option) {
				var optionElement = $('<option></option>')
					.attr('value', option.value)
					.text(option.text);

				if (option.value === currentValue || option.selected) {
					optionElement.prop('selected', true);
				}

				existingDropdown.append(optionElement);
			});

			// 如果之前的选中值不在新选项中，选择第一个
			if (!existingDropdown.find('option[value="' + currentValue + '"]').length && dropdownOptions.length > 0) {
				existingDropdown.val(dropdownOptions[0].value);
			}

			// 延迟移除标志位
			setTimeout(function() {
				existingDropdown.data('updating', false);
			}, 100);

			return dropdownId;
		}

		// 创建新的dropdown
		var dropdownHtml = '<div class="dropdown-wrapper" style="display: inline-block;">' +
			'<select id="' + dropdownId + '" class="form-control datatable-dropdown">';

		dropdownOptions.forEach(function(option) {
			var selected = option.selected ? ' selected="selected"' : '';
			dropdownHtml += '<option value="' + option.value + '"' + selected + '>' + option.text + '</option>';
		});

		dropdownHtml += '</select></div>';

		var wrapper = $(selector + "_wrapper");
		if (wrapper.length === 0) {
			console.error("DataTable wrapper not found!");
			return;
		}

		var toolbar = wrapper.find('div.datatableToolbar');
		if (toolbar.length === 0) {
			console.error("datatableToolbar not found!");
			return;
		}

		toolbar.css({
			'position': 'relative'
		}).append(dropdownHtml);

		$('#' + dropdownId).parent().css({
			'float': 'right',
			'margin-left': '10px',
			'margin-bottom': '7px',
			'margin-right': '10px'
		});

		// 绑定change事件 - 只在用户改变时触发
		$('#' + dropdownId).off('change.dropdown').on('change.dropdown', function() {
			// 如果是程序更新，不执行任何操作
			if ($(this).data('updating')) {
				console.log("Skip action during programmatic update");
				return;
			}

			var selectedValue = $(this).val();
			var selectedText = $(this).find('option:selected').text();

			$(this).attr('data-selected', selectedValue);
			$(this).attr('data-selected-text', selectedText);

			console.log("User changed dropdown - Value:", selectedValue, "Text:", selectedText);

			// 触发表格重新加载
			// 因为 fnServerData 已经会读取dropdown的值，所以只需要重新draw即可
			try {
				var dataTable = $(selector).DataTable();
				if (dataTable) {
					// 重置到第一页并重新加载数据
					dataTable.fnDraw();
				}
			} catch (error) {
				console.error("Error refreshing DataTable:", error);
			}
		});

		console.log("Dropdown created and change event bound");
		return dropdownId;
	};

	DataTableHandler.refreshByData = function(dataTableRef, aaData) {

		let oSettings;

		if (dataTableRef.fnSettings) {

			oSettings = dataTableRef.fnSettings();

			dataTableRef.fnClearTable(this);

			for (var i = 0; i < aaData.length; i++) {
				dataTableRef.oApi._fnAddData(oSettings, aaData[i]);
			}

			oSettings.aiDisplay = oSettings.aiDisplayMaster.slice();

			dataTableRef.fnDraw();

		} else {
			// new API for dataTables 1.10.*
			/*
				oSettings = dataTableRef.settings();
				dataTableRef.clear().draw();
				dataTableRef.rows.add(aaData);
				dataTableRef.columns.adjust().draw();
			 */
		}

	};

	var setLanguage = function(option, lang) {

		var tableSelector = option.tableSelector;

		var locale;

		if (option.sLengthMenu) {
			let lengthMenu = ``;
			option.sLengthMenu.forEach(count => lengthMenu += `<option value="${count}">${count}</option>`);

			locale = {
				"sLengthMenu": `<select onchange="setTimeout(function(){$('${tableSelector}').DataTable().fnPageChange(0);}, 10);">
								${lengthMenu}</select>`
			};
		} else {
			locale = {
				"sLengthMenu": `<select onchange="setTimeout(function(){$('${tableSelector}').DataTable().fnPageChange(0);}, 10);"><option value="10">10</option><option value="25">25</option><option value="50">50</option><option value="100">100</option></select>`
			};
		}

		if (lang === 'cn') {
			locale.sUrl = "/js/plugins/datatables/I18N/cn.json";
			locale.sLengthMenu = `显示 ${locale.sLengthMenu}  项结果`;
		} else if (lang === 'VN') {
			locale.sUrl = "/js/plugins/datatables/I18N/vn.json";
			locale.sLengthMenu = `Hiển thị ${locale.sLengthMenu} kết quả'`;
		} else if (lang === 'BENGALI') {
			locale.sLengthMenu = `${locale.sLengthMenu} রেকর্ড পার পেইজ`;
			locale.sInfo = `_START_ থেকে _END_ দেখানো হচ্ছে _TOTAL_ এন্ট্রির মধ্যে`;
			locale.sInfoEmpty = `0 থেকে 0 দেখানো হচ্ছে 0 এন্ট্রির মধ্যে`;
		} else {
			locale.sLengthMenu = `${locale.sLengthMenu} records per page`;
		}

		if (option.oColVis) {
			option.oColVis.buttonText = "Columns <i class='icon-angle-down'></i>";
			if (lang === 'cn') {
				option.oColVis.buttonText = "栏位 <i class='icon-angle-down'></i>";
			} else if (lang === 'VN') {
				option.oColVis.buttonText = "Cột <i class='icon-angle-down'></i>";
			}
		}

		option.oLanguage = locale;
	};

	function language(lang, tableSelector) {

		var locale = {
			// hotfix
			"sLengthMenu": '<select onchange="setTimeout(function(){ if(!$(\'' + tableSelector + '\').DataTable().fnPageChange) { return; } $(\'' + tableSelector + '\').DataTable().fnPageChange(0);}, 10);"><option value="10">10</option><option value="25">25</option><option value="50">50</option><option value="100">100</option></select> records per page'
		};

		if (lang === 'cn') {
			locale.sUrl = "/js/plugins/datatables/I18N/cn.json";
			locale.sLengthMenu = '显示 <select onchange="setTimeout(function(){$(\'' + tableSelector + '\').DataTable().fnPageChange(0);}, 10);"><option value="10">10</option><option value="25">25</option><option value="50">50</option><option value="100">100</option></select> 项结果';
		} else if (lang === 'VN') {
			locale.sUrl = "/js/plugins/datatables/I18N/vn.json";
			locale.sLengthMenu = 'Hiển thị <select onchange="setTimeout(function(){$(\'' + tableSelector + '\').DataTable().fnPageChange(0);}, 10);"><option value="10">10</option><option value="25">25</option><option value="50">50</option><option value="100">100</option></select> kết quả';
		} else if (lang === 'BENGALI') {
			locale.sLengthMenu = '<select onchange="setTimeout(function(){ if(!$(\'' + tableSelector + '\').DataTable().fnPageChange) { return; } $(\'' + tableSelector + '\').DataTable().fnPageChange(0);}, 10);"><option value="10">10</option><option value="25">25</option><option value="50">50</option><option value="100">100</option></select> রেকর্ড পার পেইজ';
			locale.sInfo = `_START_ থেকে _END_ দেখানো হচ্ছে _TOTAL_ এন্ট্রির মধ্যে`;
			locale.sInfoEmpty = `0 থেকে 0 দেখানো হচ্ছে 0 এন্ট্রির মধ্যে`;
		}

		return locale;
	}

	function setColVisButtonText() {
		if (I18N.get("form.text.backOffice.datatable.columns") == "form.text.backOffice.datatable.columns") {
			return "Columns <i class='icon-angle-down'></i>";  // 預設 EN
		} else {
			return I18N.get("form.text.backOffice.datatable.columns");
		}
	}





	// 简单的数组数据方法 - 接受 [{}] 格式，无分页
	DataTableHandler.createSimpleArray = function(pOptions, isEnableSequence, lang, callback, hideHeader, stateChangeCallback) {
		// loadVisibleColumns from localStorage
		ColVisHandler.loadVisibleColumns(pOptions);

		var options = {
			"sDom": '<"row-fluid"<"datatableToolbar"><"saveColumnsBtn">C<"span6"l><"span6"f>r>t<"row-fluid"<"span6"i><"span6"p>>',
			"oColVis": {
				"iOverlayFade": 0,
				"aiExclude": pOptions.excludeColVis,
			},
			"bRetrieve": true,
			"bServerSide": true,
			"bPaginate": false,  // 禁用分页
			"bFilter": false,
			"bInfo": false,      // 禁用信息显示
			"fnServerData": function(sSource, aoData, fnCallback, oSettings) {
				var aaSettings = this.fnSettings();
				var aaSorting = aaSettings.aaSorting; // 获取排序信息

				var formData = [];

				// 处理表单数据
				if (pOptions.formSelector) {
					var form = $(pOptions.formSelector);
					if (form.data("validator") && !form.valid()) {
						return;
					}
					formData = form.serializeArray();
				}

				// 处理下拉框数据
				if (options.dropdownOptions && options.dropdownOptions.length > 0) {
					var dropdownId = options.tableSelector.replace('#', '') + '_dropdown';
					var dropdownValue = $('#' + dropdownId).val();
					formData.push({name: 'filterBy', value: dropdownValue ?? options.dropdownOptions[0].value});
				}

				// 添加排序参数
				if (aaSorting && aaSorting[0] && aaSettings.aoColumns[aaSorting[0][0]]) {
					formData.push({name: 'sortColumn', value: aaSorting[0][0]});
					formData.push({name: 'sortField', value: aaSettings.aoColumns[aaSorting[0][0]].mData});
					formData.push({name: 'sortOrder', value: aaSorting[0][1]});
				}

				oSettings.jqXHR = $.ajax({
					"dataType": 'json',
					"type": "GET",
					url: sSource,
					data: formData,
					"success": function(data) {
						if (data.error) {
							if (NotifyHandler) {
								NotifyHandler.errorMsg(data.error);
								App.unblockUI($("body"));
								return;
							}
						}

						// 转换数组格式并添加客户端排序功能
						var datatableData;
						if (Array.isArray(data?.data)) {
							var processedData = data?.data;

							// 客户端排序（如果后端不支持排序）
							if (pOptions.enableClientSort !== false) {
								var sortColumn = formData.find(f => f.name === 'sortColumn');
								var sortField = formData.find(f => f.name === 'sortField');
								var sortOrder = formData.find(f => f.name === 'sortOrder');

								if (sortField && sortOrder) {
									var fieldName = sortField.value;
									var order = sortOrder.value; // 'asc' or 'desc'

									processedData = data?.data.slice().sort(function(a, b) {
										var aVal = a[fieldName];
										var bVal = b[fieldName];

										// 处理 null/undefined 值
										if (aVal == null && bVal == null) return 0;
										if (aVal == null) return order === 'asc' ? -1 : 1;
										if (bVal == null) return order === 'asc' ? 1 : -1;

										// 数字排序
										if (typeof aVal === 'number' && typeof bVal === 'number') {
											return order === 'asc' ? (aVal - bVal) : (bVal - aVal);
										}

										// 字符串排序
										var aStr = String(aVal).toLowerCase();
										var bStr = String(bVal).toLowerCase();

										if (order === 'asc') {
											return aStr < bStr ? -1 : (aStr > bStr ? 1 : 0);
										} else {
											return aStr > bStr ? -1 : (aStr < bStr ? 1 : 0);
										}
									});
								}
							}

							datatableData = {
								aaData: processedData,
								iTotalRecords: data.length,
								iTotalDisplayRecords: processedData.length
							};
						} else {
							// 如果已经是标准格式，直接使用
							datatableData = data;
						}

						fnCallback(datatableData);
						if (callback) {
							callback(data);
						}
					},
					"beforeSend": function() {
						App.blockUI($("body"));
					},
					"complete": function() {
						App.unblockUI($("body"));
					},
					"error": function(data) {
						console.log(data);
					}
				});
			},
		};

		// 应用自定义配置
		if (pOptions) {
			if (pOptions.hideColVis === true) {
				options.sDom = '<"row-fluid"<"datatableToolbar"><"span6"l><"span6"f>r>t<"row-fluid"<"span6"i><"span6"p>>';
			}
			$.extend(options, pOptions);
			setLanguage(options, lang);
		}

		// 行回调函数
		if (!pOptions.fnRowCallback) {
			options.fnRowCallback = function(nRow, aData, iDisplayIndex, iDisplayIndexFull) {
				if (isEnableSequence) {
					var index = iDisplayIndexFull + 1;
					$('td:eq(0)', nRow).html(index);
				}
				if (hideHeader) {
					$(options.tableSelector + ' thead').remove();
				}
			};
		}

		// 初始化完成回调
		options.fnInitComplete = function(oSettings, json) {
			var $input = $("div.dataTables_filter input");
			$input.on('keyup keypress change', function() {
				result.fnFilter($input.val());
			});
			$('div.dataTables_filter .input-group-addon').css('cursor', 'pointer').click(function(e) {
				result.fnFilter($input.val());
			});
			$input.unbind('keydown').keydown(function(k) {
				var keyCode = k.which;
				if (KeyEventUtils.isEnterKey(keyCode)) {
					WindowEventUtil.stopEvent(k, false, true);
					result.fnFilter($(this).val());
				}
			});
		};

		// 列可见性状态变化回调
		if (stateChangeCallback) {
			options.oColVis.fnStateChange = function(i, showHide, that) {
				stateChangeCallback(i, showHide);
			}
		}

		// 绘制回调
		var oldFnDrawCallback = $.fn.dataTable.defaults.fnDrawCallback;
		$.extend(true, options, {
			fnDrawCallback: function(oSettings) {
				if (pOptions.dropdownOptions) {
					DataTableHandler.addDropdown(options.tableSelector, pOptions.dropdownOptions);
				}
				if (hideHeader === true || pOptions.hideColVis === true) {
					return;
				}
				if (oldFnDrawCallback) {
					oldFnDrawCallback.apply(this, oSettings);
				}
			}
		});

		if (options.dataTableRef !== undefined) {
			options.dataTableRef.fnClearTable();
		}

		var result = $(options.tableSelector).DataTable(options);
		return result;
	};
})();

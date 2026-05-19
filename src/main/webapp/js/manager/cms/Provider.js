if (typeof (ProviderHandler) == 'undefined') {
	ProviderHandler = {};
}

(function () {
		ProviderHandler.init = async function () {
			await ProviderHandler.queryAllProviders();
			initValidate();
			DateRangeHandler.init(PageConfig.lang);
			$('#updateProviderData').on('hidden.bs.modal', function () {
				console.log('Modal closed - resetting provider forms');

				// Use array iteration for cleaner code when resetting multiple forms
				['updateProviderForm', 'updateProviderProfileForm'].forEach(formId => {
					ProviderHandler.resetUpdateProviderForm(formId);
				});
			});
		};

		var excludeColumn = [0];
		var dataTableOptions;
		var initDataTableOptions = function () {

			dataTableOptions = {
				hideColVis: false,
				tableSelector: '#providerTable',
				sAjaxSource: '/manager/ContentManageController/queryAllProviders',
				aaSorting: [[1, "asc"]],
				iDisplayLength: PageConfig.pageSize,
				excludeColVis: excludeColumn,
				bSort: true,
				aoColumns: [
					{
						"mData": null,
						"bSortable": false,
						"sWidth": "80px"
					},
					{
						"mData": "displayName",
						"mRender": function (data, type, full) {
							return `<a style='text-decoration: underline;' id='displayName' onclick='ProviderHandler.openProviderProfile(this,${full?.id})' href='javascript:void(0);'> ${data} </a>`

						}
					},
					{
						"mData": "status",
						"mRender": function (data, type, full) {
							if (type === 'display') {
								var providerStatusType = ProviderStatusType.getInstanceOf(data);
								console.log(data);
								console.log(providerStatusType)
								const labelHtml = '<span class="label ' + providerStatusType.getClassName() + '">' + I18N.get(providerStatusType.getDisplayName()) + '</span>'

								return `<a href="javascript:void(0);" onclick="ProviderHandler.editProvider(${full?.id}, 'updateProviderForm')" 
										style="text-decoration: none;">${labelHtml}</a>`;
							}
							return data;
						}
					},


				]
			};
		};

		ProviderHandler.bindEvent = function () {
			var $form = $('#updateProviderForm');

			$form.find("[name='providerStatus']").change(function () {
				$('#providerDataRangeDiv').hide();

				if ($(this).val() == ProviderStatusType.MAINTENANCE.unique()) {
					$('#providerDataRangeDiv').show();
					dateRangeInitial(moment().format("DD/MM/YYYY HH:mm:ss"), moment().endOf("day").format("DD/MM/YYYY HH:mm:ss"));
				} else {
					$('#providerDataRangeDiv').hide();
					$form.find('#maintenanceDaterange').val("")
				}
			});
			PageConfig.providerValidator = $form.validate({
				debug: true
			});
		}


		ProviderHandler.bindProviderProfileEvent = function () {
			var $form = $('#updateProviderProfileForm');
			$form.find("[name='providerStatus']").change(function () {
				console.log("here bind")
				$('#providerDataRangeDiv').hide();

				console.log("here sec >>>", $(this).val() === ProviderStatusType.MAINTENANCE.unique().toString())

				if ($(this).val() === ProviderStatusType.MAINTENANCE.unique().toString()) {
					$('#providerDataRangeDiv').show();
					dateRangeInitial(moment().format("DD/MM/YYYY HH:mm:ss"), moment().endOf("day").format("DD/MM/YYYY HH:mm:ss"));
				} else {
					$('#providerDataRangeDiv').hide();
					$form.find('#maintenanceDaterange').val("")
				}
			});
		}

		function initValidate() {
			var $updateProviderForm = $('#updateProviderForm');
			$updateProviderForm.find('#updateProviderName').rules("add", {required: true, maxByteLength: 50});
			$updateProviderForm.find('#updateDisplayOrder').rules("add", {required: true});
			$updateProviderForm.find("#providerMainDateStart").rules("add", {
				required: true,
				endDate: ['#providerMainDateEnd'],
				dateTime: true
			});
			$updateProviderForm.find("#providerMainDateEnd").rules("add", {
				required: true,
				startDate: ['#providerMainDateStart'],
				dateTime: true
			});
		}

		ProviderHandler.queryAllProviders = function () {
			var templateCallBack = function (response) {
				var data = response.aaData;

				if (data.length > 0) {
					DataBase.provider.clean();
					$.each(data, function (i, element) {
						DataBase.provider.insert(element);
					});
				}


				$('#providerTable tr').on('click', 'button', function (e) {
					e.stopPropagation();
				});
			}
			if (!dataTableOptions) {
				initDataTableOptions();
			}
			dataTableOptions.dataTableRef = DataTableHandler.create(dataTableOptions, true, PageConfig.lang, templateCallBack);
		}

		var updateVendor = function ($providerRow, vendorData) {
			var $tempTable = JsCache.get("#vendorTemplateTable").clone().show();
			$tempTable.removeAttr('id');
			var colspan = 4;
			if (!PageConfig.editButton) {
				$tempTable.find('#action').attr('style', 'display: none');
				colspan = 3;
			}
			$.each(vendorData, function (i, el) {
				if (DataBase.vendor.queryByID(el.id)) {
					DataBase.vendor.remove(el.id);
				}
				DataBase.vendor.insert(el);
				var $tempRow = JsCache.get("#vendorTemplateLayer").clone().show();
				$tempRow.find('#seqId').removeAttr('id').text(i + 1);
				$tempRow.find('#vendorName').removeAttr('id').text(el.displayName);
				$tempRow.find('#vendorGameType').removeAttr('id').text(el.gameType);
				var vendorStatusType = VendorStatusType.getInstanceOf(el.status);
				$tempRow.find('#vendorStatus').removeAttr('id').html('<span class="label ' + vendorStatusType.getClassName() + '">' + I18N.get(vendorStatusType.getDisplayName()) + '</span>');
				if (PageConfig.editButton) {
					$tempRow.find('#action').removeAttr('id').html('<ul class="table-controls">'
						+ '<li><button class="btn btn-xs" onclick="VendorHandler.editVendor(' + el.id + ')"><i class="icon-cog"></i></button></li>'
						+ '</ul>');
				} else {
					$tempRow.find('#action').attr('style', 'display: none');
				}
				$tempTable.find("tbody").append($tempRow);
			});
			var $vendorTr = $('<tr id="' + $providerRow.find('#queryName').val() + '_sub">').append($('<td colspan="' + colspan + '">').append($tempTable));
			$providerRow.after($vendorTr);
		}

		ProviderHandler.editProvider = function (id, formName) {
			$('#providerDataRangeDiv').hide();
			var provider;

			if (PageConfig.provider) {
				provider = PageConfig.provider;
			} else {
				provider = DataBase.provider.queryByID(id);
			}

			var $updateProviderForm = $(`#${formName}`);
			$updateProviderForm.find('#providerId').val(provider.id);
			$updateProviderForm.find('#updateProviderName').val(provider.providerName);
			$updateProviderForm.find("[name='providerStatus']").val(provider.status);
			$.uniform.update();

			if ($updateProviderForm.find("[name='providerStatus']").val() === ProviderStatusType.MAINTENANCE.unique().toString()) {
				$('#providerDataRangeDiv').show();
				dateRangeInitial(provider.maintenanceStart, provider.maintenanceEnd);
			} else {
				$updateProviderForm.find('#maintenanceDaterange').val('');
			}

			$('#updateProviderData').modal('show');
		}

		ProviderHandler.resetUpdateProviderForm = function (formName) {
			var $updateProviderForm = $(`#${formName}`);
			const id = $updateProviderForm.find('#providerId').val();
			console.log("formName ", formName)

			var originalProvider;

			if (PageConfig.provider) {
				originalProvider = PageConfig.provider;
			} else {
				originalProvider = DataBase.provider.queryByID(id);
			}

			$updateProviderForm.find('#updateProviderName').val(originalProvider.providerName);
			$updateProviderForm.find("[name='providerStatus']").val(originalProvider.status);
			$.uniform.update();

			if ($updateProviderForm.find("[name='providerStatus']").val() === ProviderStatusType.MAINTENANCE.unique().toString()) {
				$('#providerDataRangeDiv').show();
				dateRangeInitial(originalProvider.maintenanceStart, originalProvider.maintenanceEnd);
			} else {
				$('#providerDataRangeDiv').hide();
				$updateProviderForm.find('#maintenanceDaterange').val("")
			}

			// Clear validation errors
			var validator = $updateProviderForm.data('validator');
			console.log("validator " , validator)
			if (validator) {
				// Reset validator state
				validator.resetForm();

				// Clear custom styles
				$updateProviderForm.find('input, select, textarea').each(function () {
					$(this).css({
						'border-color': '',
						'color': '',
						'background-color': ''
					});
				});

				// Remove all error labels
				$updateProviderForm.find('label.error').remove();

				// Clear error classes
				$updateProviderForm.find('.error').removeClass('error');
				$updateProviderForm.find('.valid').removeClass('valid');
			}
		}

		ProviderHandler.closeWindow = function () {
			var $updateProviderForm = $('#updateProviderForm');
			$updateProviderForm.find('#providerId').val('');
			$updateProviderForm.find('#updateProviderName').val('');
			$updateProviderForm.find('#updateDisplayOrder').val('');
			$updateProviderForm.find('#updateStatus').val(ProviderStatusType.ACTIVE.unique());
			$updateProviderForm.find('#providerMainDateStart').val('');
			$updateProviderForm.find('#providerMainDateEnd').val('');
			$updateProviderForm.find('#providerDataRangeDiv').hide();
			$('#updateProviderData').modal('hide');
			PageConfig.providerValidator.resetForm();
		}

		ProviderHandler.cancelChange = function () {
			var $updateProviderForm = $('#updateProviderForm');
			ProviderHandler.editProvider($updateProviderForm.find('#providerId').val());
			PageConfig.providerValidator.resetForm();
		}


		ProviderHandler.updateProvider = function (formName) {
			var $updateProviderForm = $(`#${formName}`);

			console.log("formName ", formName);
			if (!$updateProviderForm.data('validator')) {
				$updateProviderForm.validate();
			}

			if ("updateProviderNameForm" === formName) {
				$updateProviderForm.find("[name='providerName']").rules('add', {
					required: true
				})
			} else {
				$updateProviderForm.find("#maintenanceDaterange").rules('add', {
					required: true
				})
			}


			if (!$updateProviderForm.valid()) {
				return;
			}


			console.log("$updateProviderForm.serialize() >>", $updateProviderForm.serialize());
			$.ajax({
				type: "POST",
				url: '/manager/ContentManageController/updateProviderProfile',
				data: $updateProviderForm.serialize(),
				success: function (responseText) {
					if (responseText.error) {
						NotifyHandler.errorMsg(responseText.error);
						return;
					} else {
						NotifyHandler.successMsg(I18N.get('msg.manager.update.success'));


						if (formName === "updateProviderForm") {
							ProviderHandler.queryAllProviders();

						} else {
							ProviderHandler.loadProviderProfile();
							$('#updateProviderNameModal').modal('hide');
						}
						$('#updateProviderData').modal('hide')
					}
				},
				error: function (error) {
					alert(error);
				}
			});
		}

		var integerKeydownOnly = function (k) {
			var keyCode = k.which;
			if (KeyEventUtils.isNumberKey(k) || KeyEventUtils.isBackspaceKey(keyCode) || KeyEventUtils.isDeleteKey(keyCode) || KeyEventUtils.isArrowKey(keyCode) || KeyEventUtils.isTabKey(keyCode)) {
				return;
			}
			WindowEventUtil.stopEvent(k, false, true);
		}
		var dateRangeInitial = function (originalStartDate, originalEndDate) {

			var dateOption = {
				"singleDatePicker": false,
				"timePicker": true,
				timePicker24Hour: false,
				autoUpdateInput: true,
				autoApply: false,
				locale: DateRangeHandler.changeLanguage('DD/MM/YYYY HH:mm:ss', PageConfig.lang),
				startDate: originalStartDate,
				endDate: originalEndDate

			};
			$('[name$=Daterange]').daterangepicker(dateOption);

			if (originalStartDate && originalEndDate) {
				$('[name$=Daterange]').val(originalStartDate + " - " + originalEndDate);
			}

			$('[name$=Daterange]').on('apply.daterangepicker', function (e, picker) {

				var startDate = moment(picker.startDate);
				startDate.set({second: 0});
				var endDate = moment(picker.endDate);
				endDate.set({second: 59});
				picker.setEndDate(endDate);

				$(this).parent().find('[name="searchDateRange"]').val(String(startDate.format('DD/MM/YYYY HH:mm:ss')) + '-' + String(endDate.format('DD/MM/YYYY HH:mm:ss')));

				$(this).val(String(startDate.format('DD/MM/YYYY HH:mm:ss')) + ' - ' + String(endDate.format('DD/MM/YYYY HH:mm:ss')))
			});
			$('[name$=Daterange]').on('cancel.daterangepicker', function (e, picker) {
				$(this).val('');
				$(this).parent().find('[name="searchDateRange"]').val('');
			});

			$("input[id^=searchProfile]").keydown(false);

			$('.fa-calendar').hide();
		};

		ProviderHandler.openProviderProfile = function (pElement, id) {
			var element = $(pElement).closest('tr');
			console.log("element >>>>", element);


			if (!id) {
				console.error("Provider ID not found");
				return;
			}

			var panel = window.open(
				"/page/manager/cms/providerProfile.jsp?providerId=" + id,
				"_winPROFILE",
				"width=1200,height=640,top=5,left=5,toolbar=no,menubar=no,resizable=no,scrollbars=yes,status=no,location=no,directories=no"
			);

			if (panel) {
				panel.focus();
			}
		};

		ProviderHandler.loadProviderProfile = function () {

			$.ajax({
				type: "POST",
				url: '/manager/ContentManageController/getProviderById',
				data: {
					providerId: PageConfig.providerId
				},
				success: function (responseText) {
					if (responseText.error) {
						NotifyHandler.errorMsg(responseText.error);
						return;
					} else {
						PageConfig.provider = responseText;
						ProviderHandler.setProviderProfile();

					}
				},
				error: function (error) {
					alert(error);
				}
			});

		}

		ProviderHandler.setProviderProfile = function () {

			const provider = PageConfig.provider;
			var profileContainer = $('#profileContainer');
			var element = profileContainer.children().detach();

			element.find("#displayName").text(provider.providerName);

			console.log("provider >>>>", provider)

			var providerStatusType = ProviderStatusType.getInstanceOf(provider.status);
			const buttonHtml = `<input type="button" value="Edit" onclick="ProviderHandler.editProvider(${provider?.id}, 'updateProviderProfileForm')" class="btn btn-primary btn-xs btn-unified" style="float: right; margin-right: 15px;">`
			const labelHtml = `<span class="label ${providerStatusType.getClassName()}">${I18N.get(providerStatusType.getDisplayName())}</span>`;

			element.find("#status").html(labelHtml);
			element.find("#editStatus").html(buttonHtml);

			profileContainer.append(element);
		}

		ProviderHandler.gotoEditProviderName = function () {
			loadProviderName();
			$('#updateProviderNameModal').modal('show');
		}

		ProviderHandler.resetProviderName = function () {
			var $form = $("[name='updateProviderNameForm']");
			$form.get(0).reset();

			loadProviderName();
		};

		var loadProviderName = function () {
			const provider = PageConfig.provider;
			removeValidation();
			var element = $('#updateProviderNameModal').children().detach();
			element.find('#providerName').val(provider.providerName);
			element.find("#providerStatus").val(provider.status);
			$('#updateProviderNameModal').append(element);
		}

		var removeValidation = function () {
			var $form = $("[name='updateProviderNameForm']");
			var validator = $form.data('validator');
			$form.get(0).reset();
			if (validator) {
				// 重置验证器状态
				validator.resetForm();

				// 清除自定义样式
				$form.find('input, select, textarea').each(function () {
					$(this).css({
						'border-color': '',
						'color': '',
						'background-color': ''
					});
				});

				// 移除所有错误信息
				$form.find('label.error').remove();

				// 清除错误类
				$form.find('.error').removeClass('error');
				$form.find('.valid').removeClass('valid');
			}
		}
	}
)();


if (typeof (VendorHandler) == 'undefined') {
	VendorHandler = {};
}

(function () {
	VendorHandler.init = function () {
		bindEvent();

		// queryGameCategory();

		initValidate();
	};

	function bindEvent() {
//		$('#updateVendorStatus').on('switch-change', function (e, state) {
//			setVendorStatus(state.value);
//		});

		var $form = $('#updateVendorForm');

		$form.find('.fileinput-holder').removeClass('input-width-xxlarge');


		$form.find('#updateDisplayOrder').unbind('keydown').keydown(function (k) {
			integerKeydownOnly(k);
		}).unbind('keyup').keyup(function (k) {

		});

		PageConfig.vendorValidator = $form.validate({
			debug: true,
			errorPlacement: function (error, element) {
				if (element.attr("name") == 'updateVendorGameType') {
					error.insertAfter($('[name=updateVendorGameType]').last().closest('label'));
				} else {
					error.insertAfter(element);
				}
			}
		});

		var today = new Date();
		var todarStr = DateUtil.format(today, 'yyyy/MM/dd');
		var dateOption = {
			"minDate": todarStr
		};
		DateRangeHandler.singleDateTimePicker(dateOption, PageConfig.lang);
		DateRangeHandler.bindEvent();

		$form.find('#updateVendorStatus').change(function () {
			$('#vendorDataRangeDiv').hide();
			if ($(this).val() == VendorStatusType.MAINTENANCE.unique()) {
				$('#vendorDataRangeDiv').show();
			}
		});

		$form.find('[name=updateVendorGameType]').click(function () {
			const gameType = GameType.getInstanceOf($(this).val());
			let gameTypeWebDiv = $('#' + PlatformType.WEB.getName() + '_' + gameType.getShortName() + 'FileDiv');
			let gameTypeHtDiv = $('#' + PlatformType.HTML5.getName() + '_' + gameType.getShortName() + 'FileDiv');

			if ($(this).is(":checked")) {
				gameTypeWebDiv.show();
				gameTypeHtDiv.show();
			} else {
				gameTypeWebDiv.hide();
				gameTypeHtDiv.hide();
			}
		});

		ImageUtils.bindPreview(370 * 1024);
	}

	VendorHandler.gameCategorySelector;

	function queryGameCategory() {
		$.ajax({
			type: "GET",
			url: '/manager/ContentManageController/queryAllGameCategoryByProvider',
			dataType: 'JSON',
			success: function (data) {
				if (data.error) {
					NotifyHandler.errorMsg(data.error);
					return;
				}

				Object.keys(data).forEach(function (key) {
					var fragment = $(document.createDocumentFragment());
					var groupName = I18N.get(key.split('-'));
					var value = data[key];
					var $group = JsCache.get('#categoryGroupTemplate').clone().show();
					$group.attr('id', '').attr('label', groupName[1]);
					$.each(value, function (i, el) {
						var $option = JsCache.get('#categoryTemplate').clone().show();
						$option.attr('id', '').val(el.id).text(el.name);
						$group.append($option);
					});
					fragment.append($group);
					JsCache.get('#updateGameCategory').append(fragment);
				});

				setTimeout(function () {
					VendorHandler.gameCategorySelector = Select2Handler.init(JsCache.get('#updateGameCategory'));
				}, 0);

			}
		});
	}

	function initValidate() {
		var $updateVendorForm = $('#updateVendorForm');
		$updateVendorForm.find('#updateVendorName').rules("add", {required: true, maxByteLength: 50});
		$updateVendorForm.find('#updateVendorDisplayOrder').rules("add", {required: true});
		$updateVendorForm.find("#vendorMainDateStart").rules("add", {
			required: true,
			endDate: ['#vendorMainDateEnd'],
			dateTime: true
		});
		$updateVendorForm.find("#vendorMainDateEnd").rules("add", {
			required: true,
			startDate: ['#vendorMainDateStart'],
			dateTime: true
		});
		$updateVendorForm.find("[name=updateVendorGameType]").rules("add", {
			required: function (element) {
				if (!$('[name=updateVendorGameType]').parent().hasClass('checked')) {
					return true;
				} else {
					return false;
				}
			}
		});

//		$updateVendorForm.find('#updateGameCategory').rules("add", { required: true });
	}

	VendorHandler.editVendor = function (id) {
		var vendor = DataBase.vendor.queryByID(id);
		if (!vendor) {
			return;
		}
		$('.modal-title').text(I18N.get('form.text.backOffice.provider.editVendor') + ' - ' + vendor.displayName);
		var $updateVendorForm = $('#updateVendorForm');
//		$updateVendorForm.find('#updateVendorProviderID').val(id);
		$updateVendorForm.find('#updateVendorID').val(vendor.id);
		$updateVendorForm.find('#updateVendorName').val(vendor.displayName);
		$updateVendorForm.find('div[id$="FileDiv"]').hide();
		$.each($('[name=updateVendorGameType]:checked'), function () {
			$(this).prop('checked', false);
		});


		$.each(JsCache.get('[name=updateVendorGameType]'), function (key, value) {
			if ((vendor.gameTypeNum & $(this).val()) == $(this).val()) {
				$(this).prop('checked', true);

				const gameType = GameType.getInstanceOf($(this).val());
				$("#" + PlatformType.WEB.getName() + "_" + gameType.getShortName() + "FileDiv").show();
				$("#" + PlatformType.HTML5.getName() + "_" + gameType.getShortName() + "FileDiv").show();
			}
		});

		$updateVendorForm.find('#updateVendorStatus').val(vendor.status).trigger('change');
		$updateVendorForm.find('#vendorMainDateStart').val('');
		if (vendor.startDate) {
			var startDate = DateUtil.format(DateUtil.convert(vendor.startDate), 'yyyy/MM/dd HH:mm:ss');
			$updateVendorForm.find('#vendorMainDateStart').val(startDate);
		}
		$updateVendorForm.find('#vendorMainDateEnd').val('');
		if (vendor.endDate) {
			var endDate = DateUtil.format(DateUtil.convert(vendor.endDate), 'yyyy/MM/dd HH:mm:ss');
			$updateVendorForm.find('#vendorMainDateEnd').val(endDate);
		}
		if (vendor.categories) {
			var categories = vendor.categories.split(",");
			setTimeout(function () {
				VendorHandler.gameCategorySelector.select2().select2('val', categories);
			}, 0);
		} else {
			VendorHandler.gameCategorySelector.select2().select2('val', '');
		}
		$updateVendorForm.find('[id$=DisplayTitle]').val('');
		if (vendor.title) {
			const displayName = JSON.parse(vendor.title);
			$.each(PageConfig.langJson, function (i, el) {
				if (!displayName[el]) {
					return;
				}
				$updateVendorForm.find('#' + el + 'DisplayTitle').val(displayName[el]);
			});
		}
		$updateVendorForm.find('[id$=DisplayDesc]').val('');
		if (vendor.description) {
			const displayName = JSON.parse(vendor.description);
			$.each(PageConfig.langJson, function (i, el) {
				if (!displayName[el]) {
					return;
				}
				$updateVendorForm.find('#' + el + 'DisplayDesc').val(displayName[el]);
			});
		}

		ImageUtils.clearPreview();
		if (vendor.webIconList !== undefined) {
			vendor.webIconList.forEach(function (icon) {
				ImageUtils.loadPreview(PlatformType.WEB.getName() + '_' + icon.gameType + 'File', icon.webIcon, icon.webIconPath);
			});
		}
		if (vendor.h5IconList !== undefined) {
			vendor.h5IconList.forEach(function (icon) {
				ImageUtils.loadPreview(PlatformType.HTML5.getName() + '_' + icon.gameType + 'File', icon.h5Icon, icon.h5IconPath);
			});
		}
		$('#updateVendorData').modal('show');
		$.uniform.update();
	}


	VendorHandler.cancelChange = function () {
		var $updateVendorForm = $('#updateVendorForm');
		VendorHandler.editVendor($updateVendorForm.find('#updateVendorID').val());
		PageConfig.vendorValidator.resetForm();
	}

	VendorHandler.closeWindow = function () {
		var $updateVendorForm = $('#updateVendorForm');
		$updateVendorForm.find('#updateVendorID').val('');
		$updateVendorForm.find('#updateVendorName').val('');
		$.each($('[name="updateVendorGameType"]:checked'), function (i, el) {
			$(this).prop('checked', false);
		});
		$.uniform.update();
		$updateVendorForm.find('#updateVendorDisplayOrder').val('');
		$updateVendorForm.find('#updateVendorStatus').val(VendorStatusType.ACTIVE.unique());
		$updateVendorForm.find('#vendorMainDateStart').val('');
		$updateVendorForm.find('#vendorMainDateEnd').val('');
		$updateVendorForm.find('[id$=DisplayTitle]').val('');
		$updateVendorForm.find('[id$=DisplayDesc]').val('');
		$updateVendorForm.find('#' + PlatformType.WEB.getName() + 'File').parent().siblings('.remove').trigger('click');
		$updateVendorForm.find('#' + PlatformType.HTML5.getName() + 'File').parent().siblings('.remove').trigger('click');
		VendorHandler.gameCategorySelector.select2().select2('val', '');
		$('#updateVendorData').modal('hide');
		PageConfig.vendorValidator.resetForm();
	}

	VendorHandler.updateVendor = function () {
		const $updateVendorForm = $('#updateVendorForm');
		if (!$updateVendorForm.valid()) {
			return;
		}

		const imgData = new FormData();

		const formDataSerialize = $updateVendorForm.serializeArray();
		for (var i = 0; i < formDataSerialize.length; i++) {
			const fieldName = formDataSerialize[i]['name'];
			const value = formDataSerialize[i]['value'];

			imgData.append(fieldName, value);
		}

		$.each($('[name="updateVendorGameType"]:checked'), function (i, el) {
			const gameType = GameType.getInstanceOf($(this).val());
			const gameWeb = PlatformType.WEB.getName() + '_' + gameType.getShortName();
			const gameH5 = PlatformType.HTML5.getName() + '_' + gameType.getShortName();

			const $webFile = $('#' + gameWeb + "File");
			const $h5File = $('#' + gameH5 + "File");

			let web;
			const $webFileName = $webFile.parent().parent().siblings('.fileinput-preview ').html();
			if ($webFile.prop('files')[0]) {
				web = $webFile.prop('files')[0];
			} else if (!($webFileName == PageConfig.noFileText)) {
				web = $webFileName;
			}
			if (typeof web !== 'undefined') {
				imgData.append(gameWeb, web);
			}

			let h5;
			const $h5FileName = $h5File.parent().parent().siblings('.fileinput-preview ').html();
			if ($h5File.prop('files')[0]) {
				h5 = $h5File.prop('files')[0];
			} else if (!($h5FileName == PageConfig.noFileText)) {
				h5 = $h5FileName;
			}
			if (typeof h5 !== 'undefined') {
				imgData.append(gameH5, h5);
			}
		});


		$.ajax({
			type: "POST",
			url: '/manager/ContentManageController/changeVendor',
			data: imgData,
			processData: false,
			contentType: false,
			cache: false,
			enctype: "multipart/form-data",
			success: function (responseText) {
				if (responseText.error) {
					NotifyHandler.errorMsg(responseText.error);
					return;
				}
				NotifyHandler.successMsg(I18N.get('msg.manager.update.success'));
				VendorHandler.closeWindow();
				ProviderHandler.queryAllProviders();
			},
			error: function (error) {
				alert(error);
			}
		});
	}

	var integerKeydownOnly = function (k) {
		var keyCode = k.which;
		if (KeyEventUtils.isNumberKey(k) || KeyEventUtils.isBackspaceKey(keyCode) || KeyEventUtils.isDeleteKey(keyCode) || KeyEventUtils.isArrowKey(keyCode) || KeyEventUtils.isTabKey(keyCode)) {
			return;
		}
		WindowEventUtil.stopEvent(k, false, true);
	}


})();

if (typeof (DataBase) == 'undefined') {
	DataBase = {};
}

(function () {

	/** ***************  Provider DataBase  ********************* */
	DataBase.provider = {};
	var providers = new HashMap();
	DataBase.provider.insert = function (entity) {
		providers.put(entity.id, entity);
	};
	DataBase.provider.remove = function (key) {
		providers.remove(key);
	};
	DataBase.provider.queryByID = function (key) {
		return providers.get(key);
	};
	DataBase.provider.queryAll = function () {
		return providers.values();
	};
	DataBase.provider.clean = function () {
		providers.clear();
	};

	/** ***************  Vendor DataBase  ********************* */
	DataBase.vendor = {};
	var vendors = new HashMap();
	DataBase.vendor.insert = function (entity) {
		vendors.put(entity.id, entity);
	};
	DataBase.vendor.remove = function (key) {
		vendors.remove(key);
	};
	DataBase.vendor.queryByID = function (key) {
		return vendors.get(key);
	};
	DataBase.vendor.queryAll = function () {
		return vendors.values();
	};
	DataBase.vendor.clean = function () {
		vendors.clear();
	};


})();
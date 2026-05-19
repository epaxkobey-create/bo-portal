if (typeof (ExcelUtils) == 'undefined') {
	ExcelUtils = {};
}

(function() {
	ExcelUtils.exportExcel = function(output, filename, url) {
		$.ajax({
			type: "GET",
			url: url,
			dataType: 'JSON',
			success: function(response) {
				if (response.error) {
					alert(response.error);
					return;
				}
				var html = '<meta http-equiv="content-type" content="application/vnd.ms-excel; charset=UTF-8" /><title>Excel</title>';
				html += output;

				var uri = 'data:application/vnd.ms-excel,' + encodeURIComponent(html);

				var link = document.createElement("a");
				link.href = uri;

				//set the visibility hidden so it will not effect on your web-layout
				link.style = "display:none";
				link.download = filename;

				//this part will append the anchor tag and remove it after automatic click
				document.body.appendChild(link);
				link.click();
				document.body.removeChild(link);
			}
		});
	};

	function timeout(ms, promise) {
		return new Promise(function(resolve, reject) {
			setTimeout(function() {
				reject(new Error("Production report timeout, please try again after sometime"))
			}, ms);
			promise.then(resolve, reject)
		})
	};

	ExcelUtils.blockModal = {};
	if (typeof (AF) !== 'undefined') {
		ExcelUtils.blockModal = AF.blockUIHandler;
	} else {
		ExcelUtils.blockModal = App;
	}

	var exportReport = function(url, fileName, generateDate, fromAdmin) {

		if (fromAdmin === 'undefined') {
			if (typeof ExcelUtils.blockModal.block !== 'undefined') {
				ExcelUtils.blockModal.block($("body"));
			} else if (typeof ExcelUtils.blockModal.blockUI !== 'undefined') {
				ExcelUtils.blockModal.blockUI($("body"));
			}
		}

		// cloudfare maximum 100 seconds
		timeout(99000, fetch(url)).then(res => {
			const contentType = res.headers.get("content-type");
			if (contentType && contentType.indexOf("application/json") !== -1) {
				return res.json().then(json => {
					if (json.error) {
						NotifyHandler.errorMsg(json.error);
						return;
					}
					NotifyHandler.successMsg(json.message);
				});
			} else {
				return res.blob().then(blob => {
					var a = document.createElement('a');
					var url = window.URL.createObjectURL(blob);

					var fileNameArray = fileName.split('.');

					// _yyyyMMdd_hhmmss
					a.href = url;
					if (generateDate) {
						a.download = fileNameArray[0] + DateUtil.format(new Date(), "_yyyyMMdd_hhmmss") + '.' + fileNameArray[1];
					} else {
						a.download = fileName;
					}
					a.click();
					window.URL.revokeObjectURL(url);
				})
			}
		}).catch(error => {
			alert(error.message);
		}).finally(() => {
			if (fromAdmin === 'undefined') {
				if (typeof ExcelUtils.blockModal.unblock !== 'undefined') {
					ExcelUtils.blockModal.unblock($("body"));
				} else if (typeof ExcelUtils.blockModal.unblockUI !== 'undefined') {
					ExcelUtils.blockModal.unblockUI($("body"));
				}
			}
		});


		// timeout(99000, fetch(url)).then(res => res.blob().then(blob => {
		//     var a = document.createElement('a');
		//     var url = window.URL.createObjectURL(blob);
		//
		//     var fileNameArray = fileName.split('.');
		//
		//     // _yyyyMMdd_hhmmss
		//     a.href = url;
		//     if (generateDate) {
		//     	a.download = fileNameArray[0] + DateUtil.format(new Date(), "_yyyyMMdd_hhmmss") + '.' + fileNameArray[1];
		//     } else {
		//     	a.download = fileName;
		//     }
		//     a.click();
		//     window.URL.revokeObjectURL(url);
		// })).catch(error => {
		//     alert(error.message);
		// }).finally(() => {
		// 	if (typeof ExcelUtils.blockModal.unblock !== 'undefined') {
		// 		ExcelUtils.blockModal.unblock($("body"));
		// 	} else {
		// 		ExcelUtils.blockModal.unblockUI($("body"));
		// 	}
		// });
	};

	ExcelUtils.exportOriginalNameExcelBinary = function(url, fileName) {
		exportReport(url, fileName, false);
	};

	ExcelUtils.exportExcelBinary = function(url, fileName, generateDate, fromAdmin) {
		exportReport(url, fileName, (generateDate === 'undefined') ? true : generateDate, fromAdmin);
	};

	ExcelUtils.exportBinary = function(url, fileName, extension, generateDate, fromAdmin) {
		fileName = fileName + extension;
		exportReport(url, fileName, (generateDate === 'undefined') ? true : generateDate, fromAdmin);
	};

	ExcelUtils.exportExcelPostClear = function() {
		if (document.getElementById("exportExcelPostDiv") != null) {
			document.getElementById("exportExcelPostDiv").remove();
		}
	};

	ExcelUtils.exportExcelPost = function(url, map) {
		ExcelUtils.exportExcelPostClear();

		var divElement = document.createElement("div");
		divElement.id = "exportExcelPostDiv";
		var iframe = document.createElement("iframe");
		iframe.style = "visibility:hidden";
		iframe.name = "iframeExcel";
		iframe.onload = function() {
			try {
				if (this.contentWindow.document.body.textContent) {
					var data = JSON.parse(this.contentWindow.document.body.textContent);
					if (data.error) {
						if (NotifyHandler) {
							NotifyHandler.errorMsg(data.error);
						} else {
							alert(data.error);
						}
					}
				}
			} catch (e) {
			}
		};

		var formDiv = document.createElement("form");
		formDiv.id = "iframeExcel";
		formDiv.action = url;
		formDiv.target = "iframeExcel";
		formDiv.method = "post";

		map.forEach(function(values, key) {
				var input = document.createElement("input");
				input.name = key;
				input.type = "text";
				input.value = values;
				input.style = "display:none";
				formDiv.appendChild(input);
			}
		);

		divElement.appendChild(formDiv);
		divElement.appendChild(iframe);

		document.body.appendChild(divElement);
		$('#iframeExcel').submit();
	};

	ExcelUtils.schedule = function(type, path, params, button) {
		button.addClass('disabled');
		$.ajax({
			type: "GET",
			url: '/manager/managerController/checkProcessingReport',
			data: {
				type: type.unique()
			},
			dataType: 'JSON',
			success: function(data) {
				if (parseInt(data.count) > 0) {
					NotifyHandler.confirmMsg(I18N.get('msg.info.backOffice.reportIsProduction', [I18N.get(type.getDisplayName())]), false,
						function(callback) {
							if (callback) {
								$.ajax({
									type: "POST",
									url: path,
									dataType: 'JSON',
									data: params,
									success: function(data) {
										if (data.error) {
											NotifyHandler.errorMsg(data.error);
											return;
										}
										NotifyHandler.successMsg(data.message);
									},
									complete: function() {
										button.removeClass('disabled');
										if (document.getElementById('exportModal') !== null) {
											$('#exportModal').modal('hide');
										}
									}
								});
							} else {
								button.removeClass('disabled');
							}
						});
				} else {
					$.ajax({
						type: "POST",
						url: path,
						dataType: 'JSON',
						data: params,
						success: function(data) {
							if (data.error) {
								NotifyHandler.errorMsg(data.error);
								return;
							}
							NotifyHandler.successMsg(data.message);
						},
						complete: function() {
							button.removeClass('disabled');
							if (document.getElementById('exportModal') !== null) {
								$('#exportModal').modal('hide');
							}
						}
					});
				}
			}
		});
	};

	ExcelUtils.scheduleUpload = function(type, path, params, button) {
		button.addClass('disabled');
		$.ajax({
			type: "GET",
			url: '/manager/managerController/checkProcessingReport',
			data: {
				type: type.unique()
			},
			dataType: 'JSON',
			success: function(data) {
				if (parseInt(data.count) > 0) {
					NotifyHandler.confirmMsg(I18N.get('msg.info.backOffice.reportIsProduction', [I18N.get(type.getDisplayName())]), false,
						function(callback) {
							if (callback) {
								$.ajax({
									type: "POST",
									url: path,
									dataType: 'JSON',
									data: params,
									success: function(data) {
										if (data.error) {
											NotifyHandler.errorMsg(data.error);
											return;
										}
										NotifyHandler.successMsg(data.message);
									},
									complete: function() {
										button.removeClass('disabled');
										if (document.getElementById('exportModal') !== null) {
											$('#exportModal').modal('hide');
										}
									}
								});
							} else {
								button.removeClass('disabled');
							}
						});
				} else {
					$.ajax({
						type: "POST",
						url: path,
						dataType: 'JSON',
						data: params,
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
						},
						complete: function() {
							button.removeClass('disabled');
							if (document.getElementById('exportModal') !== null) {
								$('#exportModal').modal('hide');
							}
						}
					});
				}
			}
		});
	};

	ExcelUtils.openExportType = function(callback) {

		if (document.getElementById('exportModal') === null) {
			const container = document.getElementsByClassName('container')[1];
			container.insertAdjacentHTML('beforeend',
				`<div class="modal fade" id="exportModal" role="dialog">
						<div class="modal-dialog">
							<div class="modal-content">
								<div class="modal-header">
									<button type="button" class="close" data-dismiss="modal">&times;</button>
									<h4 class="modal-title">${I18N.get("form.text.backOffice.exportType.title")}</h4>
								</div>
								<div class="modal-body">
									<div class="form-group">
										<label class="col-md-5 control-label">${I18N.get("form.text.backOffice.needSelectExportType")}</label>
										<div class="col-md-7">
											<label class="radio-inline"><input type="radio" class="uniform" name="exportType" value="xlsx" checked/>XLSX</label>
											<label class="radio-inline"><input type="radio" class="uniform" name="exportType" value="csv"/>CSV</label>
										</div>
									</div>
								</div>
								<div class="modal-footer">
									<input type="button" value="${I18N.get("ui.text.reset")}" name="resetButton" onclick="ExcelUtils.resetExport()" class="btn btn-primary">
									<input type="button" value="${I18N.get("ui.text.confirm")}" name="confirmButton" class="btn btn-primary">
								</div>
							</div>
						</div>
				  </div>`);

			$('#exportModal').on('hidden.bs.modal', function(e) {
				ExcelUtils.resetExport();
			});

			$('#exportModal').find('[name=confirmButton]').bind('click', function(e) {
				callback();
			});
		}

		$('#exportModal').modal('show');
	};

	ExcelUtils.resetExport = function() {
		$.each($('[name=exportType]'), function() {
			if ($(this).val() == 'xlsx') {
				$(this).prop('checked', true).trigger("change");
			} else {
				$(this).prop('checked', false);
			}
		});

		$('[name=exportType]').uniform();
	};
})();



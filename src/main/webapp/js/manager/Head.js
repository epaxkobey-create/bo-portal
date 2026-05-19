if (typeof (HeadHandler) == 'undefined') {
	HeadHandler = {};
}

(function() {
	HeadHandler.init = function() {
		setBreadCrumbs();
		bindEvent();
	};

	function setBreadCrumbs() {

		JsCache.get("#currencycrumbs").hide();

		var urlName = window.location.pathname.replace("/page/manager", "");
		var path = urlName.split(/\//g);
		for (var i = 0; i < path.length; i++) {
			var pathTemplate = JsCache.get("#headTemplate").clone().show();
			if (i == 0) { // ""
				pathTemplate.find("i").addClass("icon-home");
				pathTemplate.find("a").attr("href", "/page/manager/dashboard.jsp").text(I18N.get("form.text.backOffice.breadcrumbs.home"));
			} else {
				pathTemplate.find("i").remove();
				var pathName = path[i].replace(".jsp", "");
				var displayPathName = I18N.get("form.text.backOffice.breadcrumbs." + pathName);
				if (i == (path.length - 1)) {
					setHeader(displayPathName);
					pathTemplate.addClass("current");
					pathTemplate.find("a").attr("href", path[i]).text(displayPathName);
				} else {
					pathTemplate.find("a").attr("href", "#").text(displayPathName);
				}
			}
			JsCache.get("#breadcrumbs").append(pathTemplate);
			if (pathName == 'dashboard' || pathName == 'overview') {
				JsCache.get("#currencycrumbs").show();
			}
		}

	}

	function firstCharUpperCase(pathName) {
		var temp = pathName.charAt(0).toUpperCase();
		return temp + pathName.substring(1);
	}

	function setHeader(pathName) {
		JsCache.get(".page-title").find("h3").text(pathName);
		// TODO
	}

	function bindEvent() {

		$('#updateForm').find('#oldPassword').keydown(function(k) {
			var keyCode = k.which;
			if (KeyEventUtils.isEnterKey(keyCode)) {
				WindowEventUtil.stopEvent(k, false, true);
				LogUtil.changePassword();
			}
		});

		$('#updateForm').find('#roleNewPwd').keydown(function(k) {
			var keyCode = k.which;
			if (KeyEventUtils.isEnterKey(keyCode)) {
				WindowEventUtil.stopEvent(k, false, true);
				LogUtil.changePassword();
			}
		});

		$('#updateForm').find('#roleConfirmPwd').keydown(function(k) {
			var keyCode = k.which;
			if (KeyEventUtils.isEnterKey(keyCode)) {
				WindowEventUtil.stopEvent(k, false, true);
				LogUtil.changePassword();
			}
		});

	}

})();

if (typeof (LogUtil) == 'undefined') {
	LogUtil = {};
}

(function() {

	LogUtil.init = function() {
		var updateForm = $('#updateForm');
		PageConfig.roleValidator = $('#updateForm').validate({
			debug: true
		});

		updateForm.find("[name=oldPassword]").rules("add", {required: true});
		updateForm.find("[name=roleNewPwd]").rules("add", {required: true, password2: true});
		updateForm.find("[name=roleConfirmPwd]").rules("add", {required: true, equalTo: '#roleNewPwd'});

	};

	var winOTHER = {};

	LogUtil.getWindows = function() {
		return winOTHER;
	};

	if (opener != null && !opener.closed && opener.LogUtil && opener.LogUtil.getWindows()) {

		winOTHER = opener.LogUtil.getWindows();
		winOTHER[window.name] = window;
	} else {
		if (window.name != top.window.name) {
			winOTHER = top.LogUtil.getWindows();
		} else {
			winOTHER[window.name] = window;
		}
	}

	//same as citi code
	function sync() {
		var winNEW = {};
		for (var i in winOTHER) {
			try {
				if (winOTHER[i] && winOTHER[i].closed) {
					winOTHER[i] = null;
				}
				if (winOTHER[i]) {
					winNEW[i] = winOTHER[i];
					for (var j in winOTHER) {
						if (winOTHER[j]) {
							winOTHER[j].winOTHER[i] = winOTHER[i];
						}
					}
				}
			} catch (e) {
			}
		}
		winOTHER = winNEW;
	}

	//now the requirement is apply admin only
	LogUtil.logout = function() {
		window.onbeforeunload = null;

		sync();

		window.location.replace('/page/logout/logout.jsp');

		for (var i in winOTHER) {
			try {
				if (winOTHER[i] != null && winOTHER[i] != top.window) {
					winOTHER[i].close();
				}
			} catch (e) {
			}
		}

	};

	LogUtil.changePassword = function() {
		var $updateForm = $('#updateForm');
		if (!$updateForm.valid()) {
			return;
		}
		var password = $updateForm.find('#roleNewPwd').val();
		var oldPassword = $updateForm.find('#oldPassword').val();
		$.ajax({
			type: "POST",
			url: '/manager/managerController/changeSelfPassword',
			data: {
				"oldPassword": EncryptUtil.mask(oldPassword),
				"password": EncryptUtil.mask(password)
			},
			success: function(responseText) {
				if (responseText.errors != null) {
					var errors = '';
					for (var i = 0; i < responseText.errors.length; i++) {
						errors += responseText.errors[i] + '<br>';
					}
					NotifyHandler.errorMsg(errors);
				} else if (responseText.error) {
					NotifyHandler.errorMsg(responseText.error);
				} else {
					NotifyHandler.successMsg("Update Success");
					$('#updatePassword').modal('hide');
					LogUtil.clearData();
				}
			},
			error: function(error) {
				alert(error);
			}
		});
	};

	LogUtil.clearData = function() {
		var $updateForm = $('#updateForm');
		$updateForm.find('#oldPassword').val('');
		$updateForm.find('#roleNewPwd').val('');
		$updateForm.find('#roleConfirmPwd').val('');
		PageConfig.roleValidator.resetForm();
	};

})();
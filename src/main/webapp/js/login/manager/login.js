if (typeof (ManagerLoginHandler) == 'undefined') {
	ManagerLoginHandler = {};
}

(function() {

	ManagerLoginHandler.init = function() {
		$("#username").focus();
		ManagerLoginHandler.bind();

		// default language
		JsCache.get('#selectLang').find('option[value="' + PageConfig.lang + '"]').attr("selected", true);

		JsCache.get("#selectLang").change(function() {
			var lang = $(this).val();
			ManagerLoginHandler.changeLang(this, lang);
		});

		var loginForm = $('#loginForm');
		loginForm.validate({
			errorContainer: '#errorMsg',
			errorLabelContainer: '#errorMsg'
		});

		loginForm.find("[name=username]").rules("add", {
			required: true,
			messages: {required: I18N.get('msg.manager.login.inputName')}
		});
		loginForm.find("[name=password]").rules("add", {
			required: true,
			messages: {required: I18N.get('msg.manager.login.inputPassword')}
		});
	};

	ManagerLoginHandler.bind = function() {
		// limit : not allow input chinese
		$('input:text').attr("style", "ime-mode:disabled");
		$('input:password').attr("style", "ime-mode:disabled");
		// force lower case
		$('#username').blur(function() {
			var userID = $(this).val();
			userID = userID.toLowerCase();
			$(this).val(userID);
		});
		$("#login").bind('click', function() {
			ManagerLoginHandler.login();
		})
	};

	ManagerLoginHandler.changeLang = function() {
		$.ajax({
			type: "POST",
			url: '/login/manager/managerController/changeLanguage',
			data: {
				"lang": $('#selectLang').val()
			},
			success: function(data) {
				if (data.error) {
					NotifyHandler.errorMsg(data.error);
					return;
				}
				window.location.reload();
			}
		});
	}


	ManagerLoginHandler.clearPassword = function() {
		$("#password").val('');
	};

	ManagerLoginHandler.login = function() {

		if (!$('#loginForm').valid()) {
			return false;
		}

		var username = $("#username").val();
		var password = $("#password").val();
		var randomCode = $("#randomCode").val();

		$.ajax({
			type: "POST",
			url: '/login/manager/managerController/login',
			data: {
				'username': username,
				'password': EncryptUtil.mask(password),
				'randomCode': randomCode
			},
			success: function(resp) {
				var $errorMsg = $('#errorMsg');
				if (resp.error) {
					$errorMsg.empty();
					$errorMsg.append(resp.error).show();
					ManagerLoginHandler.clearPassword();
				} else if (resp.errors) {
					$errorMsg.empty();
					var errors = '';
					for (var i = 0; i < resp.errors.length; i++) {
						errors += resp.errors[i] + '<br>';
					}
					$errorMsg.append(errors).show();
					ManagerLoginHandler.clearPassword();

				} else {
					removeLocalStorage();

					setLocalStorage();

					window.location.replace(resp.page);
				}
			}

		})
	};

	var removeLocalStorage = function() {
		localStorage.removeItem('deposit');
		localStorage.removeItem('withdrawal');
		localStorage.removeItem('lastRingDepositTime');
		localStorage.removeItem('lastRingWithdrawalTime');
		localStorage.removeItem('searchHistory');
		localStorage.removeItem('backSearch');
	};

	var setLocalStorage = function() {
		var object = {timestamp: ''};
		localStorage.setItem('lastUpdateTime', JSON.stringify(object));
		localStorage.setItem('lastReadTime', JSON.stringify(object));
		localStorage.setItem('isReading', false);
		localStorage.setItem('unreadCount', false);
		localStorage.setItem('notificationCount', 0);
	};

})();
if (typeof (LoginHandlerUtil) == 'undefined') {
	LoginHandlerUtil = {};
}

(function() {

	LoginHandlerUtil.guestLogin = function(userId, password, loginCaptchaInput, callback, isBioLogin = false) {

		var fingerprint = (new TrafficStatistics()).get();

		var fingerprintCanvas = (new TrafficStatistics({
			canvas: true
		})).get();
		var fingerprintActiveX = (new TrafficStatistics({
			ie_activex: true
		})).get();
		var fingerprintResolution = (new TrafficStatistics({
			screen_resolution: true
		})).get();

		var delay = new Promise(function(resolve, reject) {
			(new TrafficStatistics2()).get(function(result) {
				resolve(result);
			});
		});

		var fingerprint3 = new Promise(function(resolve, reject) {
			Fingerprint3.load()
				.then(fp => fp.get())
				.then(result => {
					const visitorId = result.visitorId;
					resolve(visitorId);
				});
		});

		if (typeof (AdElementHandler) !== 'undefined') {
			AdElementHandler.init(AdEventType.LOGIN_EVENT, PageConfig.country, PageConfig.lang, {user_id: userId});
		}

		Promise.all([fingerprint3, delay, Fingerprint4Wrapper.getFp4BrowserDeviceHash()])
			.then(([fingerprint3, fingerprint2,
					   fp4BrowserDeviceHash]) => {
				let request = {
					'userId': userId,
					'password': password,
					'fingerprint': fingerprint,
					'fingerprint2': fingerprint2,
					'fingerprintCanvas': fingerprintCanvas,
					'fingerprintActiveX': fingerprint3,
					'fingerprintResolution': fingerprintResolution,
					'isBioLogin': isBioLogin,
					'fingerprint4': fp4BrowserDeviceHash.fingerprint4,
					'browserHash': fp4BrowserDeviceHash.browserHash,
					'deviceHash': fp4BrowserDeviceHash.deviceHash
				};

				if (loginCaptchaInput) {
					request.captcha = loginCaptchaInput;
				}

				$.ajax({
					url: '/guest/login',
					type: 'POST',
					dataType: 'json',
					data: request,
					success: function(data) {
						callback(data);
					},
					error: function(data) {
						// Trace.error(data);
					}
				})
			});
	};

	LoginHandlerUtil.affiliateLogin = function(userId, password, options) {

		var fingerprint = (new TrafficStatistics()).get();

		var fingerprintCanvas = (new TrafficStatistics({
			canvas: true
		})).get();
		var fingerprintActiveX = (new TrafficStatistics({
			ie_activex: true
		})).get();
		var fingerprintResolution = (new TrafficStatistics({
			screen_resolution: true
		})).get();

		var delay = new Promise(function(resolve, reject) {
			(new TrafficStatistics2()).get(function(result) {
				resolve(result);
			});
		});

		delay.then((fingerprint2) => {
			var request = {
				'userId': userId,
				'password': password,
				'fingerprint': fingerprint,
				'fingerprint2': fingerprint2,
				'fingerprintCanvas': fingerprintCanvas,
				'fingerprintActiveX': fingerprintActiveX,
				'fingerprintResolution': fingerprintResolution
			};

			HttpUtil.post('/affiliate/main/login', request, options);
		});
	};
})();
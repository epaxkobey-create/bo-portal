if (typeof (TopHandler) == 'undefined') {
	TopHandler = {};
}

(function() {

	let depositTimer = null;
	let withdrawalTimer = null;

	TopHandler.init = function() {
		CurrencyUtil.updateSetting({
			"precision": 2,
			"trailingZeros": false,
			"roundingMode": CurrencyUtil.roundingMode.HALF_UP
		});

		$("#searchTime").val($("#searchTime option:first").val());

		if (performance.navigation.type == 1) { // user可能開著通知視窗就refresh畫面
			localStorage.setItem('isReading', false);
		}

		// const $notificationDiv = $("#notificationDiv");
		// const observer = new MutationObserver(function(mutations) {
		// 	mutations.forEach(function(mutation) {
		// 		if (mutation.attributeName === "class") {
		// 			var attributeValue = $(mutation.target).prop(mutation.attributeName);
		// 			if (attributeValue.includes('open')) {
		// 				localStorage.setItem('isReading', true);
		// 				localStorage.setItem('unreadCount', false);
		// 				$('#unreadCount').hide();
		// 				localStorage.setItem('lastReadTime', localStorage.getItem('lastUpdateTime'));
		// 			} else {
		// 				$('#notificationCount').text(0);
		// 				localStorage.setItem('notificationCount', 0);
		// 				localStorage.setItem('isReading', false);
		// 			}
		// 		}
		// 	});
		// });
		// if ($notificationDiv[0]) {
		// 	observer.observe($notificationDiv[0], {
		// 		attributes: true
		// 	});
		// }

		// // if (PageConfig.enableNotification) {
		// 	TopHandler.updateNotificationTask.execute();
		// }
		TopHandler.updatePaymentTask.execute();
		TopHandler.updatePendingReportTask.execute();


		$('#reportConditionModal').on('hidden.bs.modal', function(e) {
			$('#reportListModal').modal('show');
		});

		$('#reportConditionModal').on('shown.bs.modal', function(e) {
			$('#reportListModal').modal('hide');
		});

		initHeaderInnerTime();
	};

	const initHeaderInnerTime = function() {

		if (document.getElementById('header_system_inner_time') === null
			&& document.getElementById('header_currency_inner_time') === null) {
			return;
		}

		moment.suppressDeprecationWarnings = true;

		var nf = function(num, size) {
			var s = "000000000" + num;
			return s.substr(s.length - size);
		}

		setInterval(function() {

			const systemTime = moment(new Date().toUTCString()).utcOffset(parseInt(PageConfig.systemTime));

			JsCache.get('#header_system_inner_time').html('<b>System Time Zone<br>' +
				systemTime.year() + '/' + nf(systemTime.month() + 1, 2) + '/' + nf(systemTime.date(), 2) +
				" " + nf(systemTime.hours(), 2) + ':' + nf(systemTime.minutes(), 2) + ':' + nf(systemTime.seconds(), 2) + '</b>');

			const currencyTime = moment(new Date().toUTCString()).utcOffset(parseInt(PageConfig.currencyTime));

			JsCache.get('#header_currency_inner_time').html('<b>Player Time Zone<br>' +
				currencyTime.year() + '/' + nf(currencyTime.month() + 1, 2) + '/' + nf(currencyTime.date(), 2) +
				" " + nf(currencyTime.hours(), 2) + ':' + nf(currencyTime.minutes(), 2) + ':' + nf(currencyTime.seconds(), 2) + '</b>');
		}, 1000);
	};

	TopHandler.togglePopUpStatus = function() {
		$.ajax({
			type: "GET",
			url: "/manager/managerController/changeEnablePopUp",
			dataType: 'JSON',
			success: function(data) {
				if (data.error) {
					NotifyHandler.errorMsg(data.error);
					return;
				}
				$('#enableBtn').text(data.btnName);
			},
			error: function(response) {

			}
		});
	}

	// TopHandler.updateNotificationTask = TaskHelper.createTask(5, // cycleTime
	// 	0, // cycleTick
	// 	null, function() {
	// 		const that = this;
	// 		updateNotificationExecute(that);
	// 	});

	/*
	const updateNotificationExecute = function(task) {
		$.ajax({
			type: "POST",
			url: "/manager/managerController/getPendingData",
			dataType: 'JSON',
			success: function(response) {
				try {
					if (!response.notification) {
						return;
					}
					// Menu Notification
					renderMenuNotification(response);
					//  Notifications
					renderNotification(response.notification, response.enablePopup);

					// if (response.reportMsg !== undefined) {
					// 	NotifyHandler.successMsg(response.reportMsg, 5000, 'topRight');
					// }

				} catch (err) {
					console.error(err);
				} finally {
					task.check();
					TaskExecuter.execute();
				}
			},
			error: function(response) {
				task.check();
				TaskExecuter.execute();
			}
		});
	}
	*/

	const renderNotification = function(notification, enablePopup) {
		const now = new Date();
		const lastUpdateTimeObj = JSON.parse(localStorage.getItem('lastUpdateTime'));
		const isReading = (localStorage.getItem('isReading') === 'true');
		const lastReadTimeObj = JSON.parse(localStorage.getItem('lastReadTime'));
		const cacheUnreadCount = (localStorage.getItem('unreadCount') === 'true');
		let notificationCount = localStorage.getItem('notificationCount');

		JsCache.get('.notification').find('li:not(.title, .footer)').remove();

		$.each(notification, function(i, el) {
			const createTime = DateUtil.convert(el.time);
			let $tempRow;
			if (el.type === NotificationSettingType.DEPOSIT.unique()) {
				$tempRow = JsCache.get("#depositTemplate").attr('id', '').clone().show();
			} else if (el.type === NotificationSettingType.WITHDRAWAL.unique()) {
				$tempRow = JsCache.get("#withdrawalTemplate").attr('id', '').clone().show();
			} else if (el.type === NotificationSettingType.PLAYER.unique()) {
				$tempRow = JsCache.get("#registerTemplate").attr('id', '').clone().show();
			}

			$tempRow.find('.message').text(el.msg);

			// time
			$tempRow.find('.time').text('');
			const difference = (now - createTime);
			const hours = Math.floor((difference % (60 * 60 * 1000 * 24)) / (60 * 60 * 1000));
			const mins = Math.floor(((difference % (60 * 60 * 1000 * 24)) % (60 * 60 * 1000)) / (60 * 1000));

			if (hours > 0) {
				$tempRow.find('.time').text(hours + ' hours');
			} else {
				$tempRow.find('.time').text(mins <= 0 ? 'Just Now' : (mins + ' mins'));
			}

			JsCache.get('.notification').find('.title').after($tempRow);

			if (lastUpdateTimeObj.timestamp) {
				const lastUpdateTime = DateUtil.convert(lastUpdateTimeObj.timestamp);
				if (DateUtil.compare(lastUpdateTime, createTime) >= 0) {
					return;
				}
			}

			if (enablePopup === BinaryStatusType.ACTIVE.unique()) {
				if (el.type === NotificationSettingType.DEPOSIT.unique()) {
					NotifyHandler.warningMsg(el.msg, 5000, 'topRight');
				} else if (el.type === NotificationSettingType.WITHDRAWAL.unique()) {
					NotifyHandler.errorMsg(el.msg, 5000, 'topRight');
				} else if (el.type === NotificationSettingType.PLAYER.unique()) {
					NotifyHandler.successMsg(el.msg, 5000, 'topRight');
				}
			}


			if (!isReading) {
				if (!lastReadTimeObj.timestamp) {
					notificationCount++;
				} else {
					const lastReadTime = DateUtil.convert(lastReadTimeObj.timestamp);
					if (DateUtil.compare(createTime, lastReadTime) === 1) {
						notificationCount++;
					}
				}
			}

			if (i === (notification.length - 1)) { // time desc
				localStorage.setItem('lastUpdateTime', JSON.stringify({timestamp: el.time}));
			}

		});

		let unreadCount = false;
		if (!isReading && notificationCount > 0) {
			localStorage.setItem('unreadCount', true);
			unreadCount = true;
		}

		if (unreadCount || cacheUnreadCount) {
			$('#unreadCount').show().text(notificationCount >= 99 ? "99+" : notificationCount);
		} else {
			$('#unreadCount').hide();
		}

		$('#notificationCount').text(notificationCount);
		localStorage.setItem('notificationCount', notificationCount);

	};

	const renderMenuNotification = function(data) {
		const promotionVerificationCount = data.promotionVerificationCount;
		const forceServeCount = data.forceServeCount;
		const applicationCount = data.applicationCount;

		const $marketingMenu = $('#count33');
		$marketingMenu.hide();
		// Promotion Verification
		const $subPromotion = $('#menu159');
		$subPromotion.find('span').remove();
		if (promotionVerificationCount > 0) {
			$subPromotion.find('i').after(`<span id="notifyTemplate" class="label label-danger pull-right">${promotionVerificationCount}</span>`);
		}

		// Manual Force Serve
		const $subForceServe = $('#menu126');
		$subForceServe.find('span').remove();
		if (forceServeCount > 0) {
			$subForceServe.find('i').after(`<span id="notifyTemplate" class="label label-danger pull-right">${forceServeCount}</span>`);
		}

		if (promotionVerificationCount > 0 || forceServeCount > 0) { // 14
			let count = 0;
			if (promotionVerificationCount > 0) {
				count = count + promotionVerificationCount;
			}
			if (forceServeCount > 0) {
				count = count + forceServeCount;
			}
			$marketingMenu.text(count);
			$marketingMenu.show();
		}

		const $affiliateMenu = $('#count140');
		$affiliateMenu.hide();

		const $subApplication = $('#menu144');
		$subApplication.find('span').remove();

		if (applicationCount > 0) {
			$subApplication.find('i').after(`<span id="notifyTemplate" class="label label-danger pull-right">${applicationCount}</span>`);

			$affiliateMenu.text(applicationCount);
			$affiliateMenu.show();
		}
	};

	TopHandler.updatePaymentTask = TaskHelper.createTask(5, // cycleTime
		0, // cycleTick
		null, function() {
			var that = this;
			paymentNotification(that);
		});

	TopHandler.changeCurrency = function() {
		$.ajax({
			type: "GET",
			url: "/manager/managerController/changeCurrency",
			data: {
				'currencyType': $('#dashboardCurrencyType').val()
			},
			dataType: 'JSON',
			success: function(data) {
				if (data.error) {
					NotifyHandler.errorMsg(data.error);
					return;
				}
				PageConfig.managerCurrency = data.currency;
				PageConfig.managerCurrencySymbol = data.currencySymbol;
				PageConfig.currencyTime = data.currencyTime;
				DashboardHandler.updateTaskExecute();
				paymentNotification();
			},
			error: function(response) {

			}
		});
	};

	const paymentNotification = function(task) {
		$.ajax({
			type: "POST",
			url: "/manager/managerController/queryPaymentPendingData",
			dataType: 'JSON',
			success: function(response) {
				try {
					if (response == null) {
						return;
					}
					if (response.error) {
						alert(response.error);
						return;
					}

					renderPaymentNotification(response);
				} finally {
					if (task) {
						task.check();
					}
					TaskExecuter.execute();
				}
			},
			error: function(response) {

			}
		});
	};

	const renderPaymentNotification = function(data) {
		const pending = data.pending;

		const $dashboardPage = $('#dashboardPage');

		if ($dashboardPage.length > 0) {
			$dashboardPage.find('#totalDepositAmount').empty();
			$dashboardPage.find('#totalDepositCount').empty();
			$dashboardPage.find('#totalWithdrawlAmount').empty();
			$dashboardPage.find('#totalWithdrawlCount').empty();
		}

		const depositCount = pending.depositCount;
		const withdrawalCount = pending.withdrawalCount;

		const $paymentMenu = $('#count14');
		$paymentMenu.hide();

		const $subDeposit = $('#menu15');
		$subDeposit.find('span').remove();
		if (depositCount > 0) {
			$subDeposit.find('i').after(`<span id="notifyTemplate" class="label label-danger pull-right">${depositCount}</span>`);

			if (pending.isPendingDeposit) {
				const now = new Date().getTime();
				let lastRingDepositTime = localStorage.getItem('lastRingDepositTime');
				if (lastRingDepositTime == null || (now - parseInt(lastRingDepositTime) >= 30000)) {
					ringDepositAudio();
					localStorage.setItem('lastRingDepositTime', now + '');
				}
			} else {
				let depositInCache = localStorage.getItem('deposit');
				if (depositInCache == null || depositInCache < depositCount) {
					ringDepositAudio();
				}
			}
		} else {
			localStorage.removeItem('lastRingDepositTime');
		}

		localStorage.setItem('deposit', depositCount);

		const $subWithdrawal = $('#menu20');
		$subWithdrawal.find('span').remove();
		if (withdrawalCount > 0) {
			$subWithdrawal.find('i').after(`<span id="notifyTemplate" class="label label-danger pull-right">${withdrawalCount}</span>`);

			if (pending.isPendingWithdrawal) {
				const now = new Date().getTime();
				let lastRingWithdrawalTime = localStorage.getItem('lastRingWithdrawalTime');
				if (lastRingWithdrawalTime == null || (now - parseInt(lastRingWithdrawalTime) >= 30000)) {
					ringWithdrawalAudio();
					localStorage.setItem('lastRingWithdrawalTime', now + '');
				}
			} else {
				let withdrawalInCache = localStorage.getItem('withdrawal');
				if (withdrawalInCache == null || withdrawalInCache < withdrawalCount) {
					ringWithdrawalAudio();
				}
			}
		} else {
			localStorage.removeItem('lastRingWithdrawalTime');
		}
		localStorage.setItem('withdrawal', withdrawalCount);

		if (depositCount > 0 || withdrawalCount > 0) { // 14
			let count = 0;
			if (depositCount > 0) {
				count = count + depositCount;
			}
			if (withdrawalCount > 0) {
				count = count + withdrawalCount;
			}
			$paymentMenu.text(count);
			$paymentMenu.show();
		}

		if ($dashboardPage.length > 0) {
			$dashboardPage.find('#totalDepositAmount').append(`<i id="tempSign" class="no-italics">${PageConfig.managerCurrencySymbol}</i>`)
				.append(CurrencyUtil.formatter(pending.currencyDepositAmount));
			$dashboardPage.find('#totalDepositCount').html(CurrencyUtil.thousandComma(pending.currencyDepositCount));
			$dashboardPage.find('#totalPendingDeposit').unbind('click').on('click', function(e) {
				DashboardHandler.openDetail('deposit', I18N.get('ui.text.backOffice.dashboard.totalPending'), '', e);
			});

			$dashboardPage.find('#totalWithdrawlAmount').append(`<i id="tempSign" class="no-italics">${PageConfig.managerCurrencySymbol}</i>`)
				.append(CurrencyUtil.formatter(pending.currencyWithdrawalAmount));
			$dashboardPage.find('#totalWithdrawlCount').html(CurrencyUtil.thousandComma(pending.currencyWithdrawalCount));
			$dashboardPage.find('#totalPendingWithdrawal').unbind('click').on('click', function(e) {
				DashboardHandler.openDetail('withdrawal', I18N.get('ui.text.backOffice.dashboard.totalPending'), '', e);
			});
		}
	};

	let reportDataTableOptions;
	const initReportDataTableOptions = function() {
		reportDataTableOptions = {
			tableSelector: '#reportListTable',
			sAjaxSource: '/manager/managerController/searchReportList',
			aaSorting: [[4, "desc"]],
			hideColVis: true,
			iDisplayLength: PageConfig.pageSize,
			aoColumnDefs: [{
				"mData": "exportType",
				"aTargets": [0]
			}, {
				"mData": "id",
				"aTargets": [1],
				"mRender": function(data, type, full) {
					if (type === 'display') {
						if (full.showCondition) {
							return `<ul class="table-controls">
									<li><button class="btn btn-xs" onclick="TopHandler.openReportDetail(${data})"><i class="icon-search"></i></button></li>
								</ul>`;
						}
						return '';
					}
					return data;
				}
			}, {
				"mData": "status",
				"aTargets": [2],
				"mRender": function(data, type, full) {
					if (type === 'display') {
						return `<span class="label ${full.cssName}">${full.statusName}</span>`;
					}
					return data;
				}
			}, {
				"mData": "reportName",
				"aTargets": [3],
				"mRender": function(data, type, full) {
					if (type === 'display') {
						if (full.fileExisted) {
							return `<a href="${full.path}?id=${full.id}">${data}</a>`;
						}
					}
					return data;
				},
				"bSortable": false
			}, {
				"mData": "createTime",
				"aTargets": [4],
			}, {
				"mData": "completedTime",
				"aTargets": [5],
			}, {
				"mData": "processedTime",
				"aTargets": [6],
			}]
		};
	};

	TopHandler.openReportModal = function() {

		const callBack = function(response) {
			$('#reportListModal').modal('show');
		};

		if (!reportDataTableOptions) {
			initReportDataTableOptions();
		}

		const isEnableSequence = false;
		reportDataTableOptions.dataTableRef = DataTableHandler.create(reportDataTableOptions, isEnableSequence, PageConfig.lang, callBack);
	};

	TopHandler.openReportDetail = function(id) {

		$.ajax({
			type: "POST",
			url: "/manager/managerController/getReportConditionDetail",
			data: {
				reportId: id,
			},
			dataType: 'JSON',
			success: function(data) {

				if (data.error) {
					NotifyHandler.errorMsg(data.error);
					return;
				}
				const target = document.querySelector('#conditionBody');
				target.insertAdjacentHTML("beforeend", `<table class="table table-striped table-bordered" style="table-layout:fixed;word-break:break-all;" cellspacing="0" width="100%">
						<tbody>
							<tr><th>Column</th><th>Value</th></tr>
									${Object.keys(data).map(function(key) {
					return `<tr><td>${key}</td><td>${data[key]}</td></tr>`
				}).join("")}
						</tbody>
					</table>`);
			},
			beforeSend: function() {
				$('#conditionBody').empty();
			},
			complete: function() {
				$('#reportConditionModal').modal('show');
			}
		});

	};

	TopHandler.updatePendingReportTask = TaskHelper.createTask(15, // cycleTime
		0, // cycleTick
		null, function() {
			const that = this;
			updatePendingReportExecute(that);
		});

	const updatePendingReportExecute = function(task) {
		$.ajax({
			type: "POST",
			url: "/manager/managerController/getPendingReport",
			dataType: 'JSON',
			success: function(response) {
				try {

					if (response.msg !== undefined) {
						NotifyHandler.successMsg(response.msg, 5000, 'topRight');
					}

				} catch (err) {
					console.error(err);
				} finally {
					task.check();
					TaskExecuter.execute();
				}
			},
			error: function(response) {
				task.check();
				TaskExecuter.execute();
			}
		});
	}

	const ringDepositAudio = function() {
		const depositAudio = document.getElementById('depositAudio');
		depositAudio.pause();
		depositAudio.currentTime = 0;
		depositAudio.play();
	};

	const ringWithdrawalAudio = function() {
		const withdrawalAudio = document.getElementById('withdrawalAudio');
		withdrawalAudio.pause();
		withdrawalAudio.currentTime = 0;
		withdrawalAudio.play();
	};

})();
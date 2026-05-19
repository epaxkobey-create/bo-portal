if (typeof (SettingHandler) == 'undefined') {
	SettingHandler = {};
}

(function () {

	SettingHandler.cacheData = {};

	SettingHandler.init = function () {
		bindEvents();
		CurrencyUtil.updateSetting({
			"precision": 2,
			"trailingZeros": true,
			"roundingMode": CurrencyUtil.roundingMode.HALF_UP
		});

		SettingHandler.getPlayerResponsibilities();
	};

	var bindEvents = function () {
		$('[name=allowForceServeType]').on('switch-change', function (event, state) {
			if (state.value == true) {
				$(this).siblings('[name=allowForceServeStatus]').val("1");
			} else {
				$(this).siblings('[name=allowForceServeStatus]').val("0");
			}
		});
	}

	SettingHandler.getPlayerResponsibilities = function () {
		$.ajax({
			type: "GET",
			url: '/manager/member/playerResponsibilities',
			dataType: 'JSON',
			data: {
				userId: PageConfig.userId
			},
			success: function (data) {
				if (data.error) {
					NotifyHandler.errorMsg(data.error);
					return;
				}
				SettingHandler.cacheData = data;
				renderPlayerResponsibilities();
			}
		});
	}

	SettingHandler.clearText = function (id) {

		const element = document.getElementById(id);
		const defaultValues = {
			'updateType': 0,
			'selfExclusionType': 0,
			'newMonthlyLossLimit': '0',
			'newDailyLossLimit': '0',
			'newWeeklyLossLimit': '0',
			'newMonthlyWagerLimit':'0',
			'newDailyWagerLimit':'0',
			'newWeeklyWagerLimit':'0',
			'newDailyDepositLimits': "0",
			'newWeeklyDepositLimits': "0",
			'newMonthlyDepositLimits': "0",
			'timeSpentLimitType': 0
		};

		element.value = defaultValues[id] ?? '';
	}

	const renderPlayerResponsibilities = () => {
		// Initialize currency symbol

		const data = SettingHandler.cacheData;
		if (!data) return;

		renderWagerLimits(data.wagerLimits);
		renderLossLimit(data.lossLimits);
		// renderRealityCheck(data.realityCheck);
		// renderDepositLimit(data.depositLimits);
		renderSelfExclusion(data?.selfExclusion)
		renderTimeSpentLimit(data?.timeSpentLimit);
		// renderAccountReviewReminder(data.accountReviewReminder);
	};

	const renderAccountReviewReminder = (accountReviewReminder) => {
		if (!accountReviewReminder) return;


		updateSelectValue('accountReviewReminderType',	accountReviewReminder.newValue);
		$("#currentAccountReviewReminder").html(PageConfig.accountReviewReminderTypes[accountReviewReminder.currentValue].name);

	};


	const renderRealityCheck = (realityCheck) => {
		if (!realityCheck) return;


		updateSelectValue('realityCheckType',	realityCheck.newValue);
		$("#currentRealityCheck").html(PageConfig.realityCheckTypes[realityCheck.currentValue].name);

		handleMessage(
			realityCheck.message,
			"#realityCheckMessage",
			"updateRealityCheckForm",
			realityCheck.effectiveTime,
			()=> $("#realityCheckType").val(realityCheck?.currentValue)
		)

	};

	const renderWagerLimits = (wagerLimits) => {
		if (!wagerLimits) return;

		const limitTypes = [
			{key: 'daily', prefix: 'Daily', messageSelector: '#dailyWagerLimitMessage'},
			{key: 'weekly', prefix: 'Weekly', messageSelector: '#weeklyWagerLimitMessage'},
			{key: 'monthly', prefix: 'Monthly', messageSelector: '#monthlyWagerLimitMessage'}
		]

		limitTypes.forEach(({key, prefix, messageSelector}) => {
			const limit = wagerLimits[key];
			console.log("limit : ", limit)
			if (!limit) return;

			console.log("limit current value: ", limit?.currentValue)

			const formattedValue = Number(limit?.currentValue).toLocaleString();
			const currentLimitId = `current${prefix}WagerLimits`;
			const newLimitId = `new${prefix}WagerLimit`;

			updateCurrentLimit(currentLimitId, formattedValue);
			updateLimitInput(newLimitId, limit.newValue, limit?.currentValue);
			handleMessage(
				limit.message,
				messageSelector,
				'updateWagerLimitForm',
				limit?.effectiveTime,
				() => $(`#${newLimitId}`).val(formattedValue)
			);
		});
	};


	const renderLossLimit = (lossLimit) => {
		if (!lossLimit) return;

		const limitTypes = [
			{key: 'daily', prefix: 'Daily', messageSelector: '#dailyLossLimitMessage'},
			{key: 'weekly', prefix: 'Weekly', messageSelector: '#weeklyLossLimitMessage'},
			{key: 'monthly', prefix: 'Monthly', messageSelector: '#monthlyLossLimitMessage'}
		]


		limitTypes.forEach(({key, prefix, messageSelector}) => {
			const limit = lossLimit[key];
			if (!limit) return;


			const formattedValue = Number(limit?.currentValue).toLocaleString();
			const currentLimitId = `current${prefix}LossLimit`;
			const newLimitId = `new${prefix}LossLimit`;

			updateCurrentLimit(currentLimitId, formattedValue);
			updateLimitInput(newLimitId, limit.newValue, limit?.currentValue);
			handleMessage(
				limit.message,
				messageSelector,
				'updateLossLimitForm',
				limit?.effectiveTime,
				() => $(`#${newLimitId}`).val(formattedValue)
			);
		});
	};


	const renderDepositLimit = (depositLimit) => {
		if (!depositLimit) return;

		const limitTypes = [
			{key: 'daily', prefix: 'Daily', messageSelector: '#dailyDepositLimitMessage'},
			{key: 'weekly', prefix: 'Weekly', messageSelector: '#weeklyDepositLimitMessage'},
			{key: 'monthly', prefix: 'Monthly', messageSelector: '#monthlyDepositLimitMessage'}
		];

		limitTypes.forEach(({key, prefix, messageSelector}) => {
			const limit = depositLimit[key];

			if (!limit) return;

			const formattedValue = Number(limit.currentValue).toLocaleString();
			const currentLimitId = `current${prefix}DepositLimits`;
			const newLimitId = `new${prefix}DepositLimits`;

			updateCurrentLimit(currentLimitId, formattedValue);
			updateLimitInput(newLimitId, limit.newValue, limit.currentValue);
			handleMessage(
				limit.message,
				messageSelector,
				'updateDepositLimitForm',
				limit.effectiveTime,
				() => $(`#${newLimitId}`).val(formattedValue)
			);
		});
	};

	const renderSelfExclusion = (selfExclusion) => {
		if (!selfExclusion) return;

		updateCurrentExclusion(selfExclusion.currentValue, selfExclusion.effectiveEndTime);

		updateSelectValue('selfExclusionType', selfExclusion.newValue);

		handleMessage(
			selfExclusion.message,
			'#exclusionMessage',
			'updateSelfExclusionForm',
			selfExclusion.effectiveTime,
			() => $('#selfExclusionType').val(selfExclusion.currentValue ?? '')
		);
	};

	const renderTimeSpentLimit = (timeSpentLimit) =>{
		if(!timeSpentLimit) return;

		updateCurrentTimeSpentLimit( timeSpentLimit.currentValue);
		updateSelectValue('timeSpentLimitType', timeSpentLimit.newValue);
		handleMessage(
			timeSpentLimit.message,
			'#timeSpentLimitMessage',
			'updateTimeSpentLimitForm',
			timeSpentLimit.effectiveTime,
			() => $('#timeSpentLimitType').val(timeSpentLimit.currentValue ?? '')
		);
	}

	const updateCurrentTimeSpentLimit = (currentValue) => {
		const timeSpentLimitType = PageConfig.timeSpentLimitTypes[currentValue];
		const displayText = timeSpentLimitType?.name === 'None' ? '-' : timeSpentLimitType?.name;
		$('#currentTimeSpentLimit').text(displayText);
	};

	const updateCurrentSession = (currentValue) => {
		const sessionType = PageConfig.SessionExpiryTypes[currentValue];
		const displayText = sessionType?.name === 'None' ? '-' : sessionType?.name;
		$('#currentSession').text(displayText);
	};

	const updateCurrentExclusion = (currentValue, effectiveEndTime) => {
		const exclusionType = PageConfig.selfExclusionType[currentValue];

		if (!exclusionType?.name || exclusionType.name === 'No Exclusion') {
			$('#currentSelfExclusion').html('-');
		} else if (exclusionType.name === 'Indefinite') {
			$('#currentSelfExclusion').html(exclusionType.name);
		} else {
			const formattedDate = DateUtil.format(effectiveEndTime, 'dd/MM/yyyy, HH:mm');
			$('#currentSelfExclusion').html(`
            Definite: ${exclusionType.name} <br/>
            <p style='color: red;'>Until ${formattedDate}</p>
        `);
		}
	};


	const updateCurrentLimit = (id, currentValue) => {
		if (currentValue !== '0') {
			$(`#${id}`).html(PageConfig.managerCurrencySymbol + " " + currentValue);
		}
	};

	const updateLimitInput = (id, newValue, currentValue) => {
		if (currentValue !== '0') {
			const formattedNewValue = Number(newValue).toLocaleString();
			$(`#${id}`).val(formattedNewValue);
		}
	};

	const updateSelectValue = (elementId, newValue) => {
		if (newValue) {
			const selectElement = document.getElementById(elementId);
			selectElement.value = parseInt(newValue);
		}
	};

	const handleMessage = (message, containerSelector, formName, effectiveTime, resetCallback) => {
		let trimmedMessage = message?.trim() ?? '';
		const messageContainer = $(containerSelector);

		console.log("trimmedMessage :" , trimmedMessage)
		if (trimmedMessage && effectiveTime != null) {
			trimmedMessage = trimmedMessage.replace("{effectiveTime}", DateUtil.format(effectiveTime, "dd/MM/YYYY at hh:mm"));
			showMessage(messageContainer, trimmedMessage, resetCallback);
		} else {
			hideMessage(messageContainer);
			resetCallback();
		}
	};

	const showMessage = (container, message, resetCallBack) => {
		const messageHtml = `
        <p style='color: #e67e22; display: inline;'>
            ${message} 
            <a class = "cancel-link" 
               style='color: #e67e22; text-decoration: underline;'>
                Cancel Request
            </a>
        </p>
    `;
		container.html(messageHtml).show();
		container.find('.cancel-link').on('click', function (e) {
			e.preventDefault();
			resetCallBack();
			hideMessage(container);

		});
	};

	const hideMessage = (container) => {
		container.empty().hide();
	};

	SettingHandler.onChangeSeparator = function (input) {
		let num = parseFloat(input.value.replace(/,/g, '')) || 0;
		if (num <= 0) input.value = '0';
		else if (num > 1000000) input.value = '1,000,000';
		else input.value = num.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ',')
	}


	const PlayerResponsibilityType = {
		WAGER_LIMIT: 1,
		LOSS_LIMIT: 2,
		SESSION_EXPIRY: 3,
		SELF_EXCLUSION: 4,
		REALITY_CHECK: 5,
		DEPOSIT_LIMIT: 6,
		ACCOUNT_REVIEW_REMINDER:8,
		TIME_SPENT_LIMIT:9
	};

	const PeriodType = {
		DAILY: 1,
		WEEKLY: 2,
		MONTHLY: 3
	};

	const FORM_CONFIGS = {
		updateSessionExpiryForm: {
			responsibilityType: PlayerResponsibilityType.SESSION_EXPIRY,
			handler: handleSessionExpiryForm
		},
		updateSelfExclusionForm: {
			responsibilityType: PlayerResponsibilityType.SELF_EXCLUSION,
			handler: handleSelfExclusionForm
		},
		updateWagerLimitForm: {
			responsibilityType: PlayerResponsibilityType.WAGER_LIMIT,
			handler: handleWagerLimitForm
		},
		updateLossLimitForm: {
			responsibilityType: PlayerResponsibilityType.LOSS_LIMIT,
			handler: handleLossLimitForm
		},
		updateRealityCheckForm: {
			responsibilityType: PlayerResponsibilityType.REALITY_CHECK,
			handler: handleRealityCheckForm
		},
		updateDepositLimitForm: {
			responsibilityType: PlayerResponsibilityType.DEPOSIT_LIMIT,
			handler: handleDepositLimitForm
		},
		updateAccountReviewReminderForm:{
			responsibilityType: PlayerResponsibilityType.ACCOUNT_REVIEW_REMINDER,
			handler: handleAccountReviewReminderForm
		},
		updateTimeSpentLimitForm:{
			responsibilityType: PlayerResponsibilityType.TIME_SPENT_LIMIT,
			handler: handleTimeSpentLimitForm
		}
	};

	const parseNumericValue = (value) => {
		if (!value) return null;
		const cleaned = String(value).replace(/,/g, "");
		const parsed = parseInt(cleaned, 10);
		return isNaN(parsed) ? null : parsed;
	};

	const createResponsibleItem = (newValue, periodType = PeriodType.DAILY) => ({
		newValue: String(newValue),
		periodType: periodType
	});


	function handleSessionExpiryForm() {
		const form = $("[name='updateSessionExpiryForm']");
		const updateType = form.find("[name='updateType']").val();

		if (!updateType) {
			throw new Error("Please select a session expiry type");
		}

		return [createResponsibleItem(updateType)];
	}

	function handleSelfExclusionForm() {
		const form = $("[name='updateSelfExclusionForm']");
		const exclusionType = form.find("[name='selfExclusionType']").val();

		if (!exclusionType) {
			throw new Error("Please select an exclusion type");
		}

		return [createResponsibleItem(exclusionType)];
	}

	function handleTimeSpentLimitForm(){
		const form = $("[name='updateTimeSpentLimitForm']");
		const timeSpentLimitValue = form.find("[name ='timeSpentLimitType']").val();

		if (!timeSpentLimitValue) {
			throw new Error("Please select an a new value");
		}

		return [createResponsibleItem(timeSpentLimitValue)];
	}

	function handleWagerLimitForm() {
		const form = $("[name='updateWagerLimitForm']");

		if(!form.data("validator")){
			console.log("entry 1");
			initializeWagerLimitValidator(form);
		}

		if(!form.valid()){
			return;
		}

		const dailyLimit = parseNumericValue(form.find("#newDailyWagerLimit").val());
		// const weeklyLimit = parseNumericValue(form.find("#newWeeklyWagerLimit").val());
		// const monthlyLimit = parseNumericValue(form.find("#newMonthlyWagerLimit").val());

		if (dailyLimit === null
		//|| weeklyLimit === null || monthlyLimit === null
		) {
			throw new Error("All limits must be valid numbers");
		}

		return [
			createResponsibleItem(dailyLimit, PeriodType.DAILY),
			// createResponsibleItem(weeklyLimit, PeriodType.WEEKLY),
			// createResponsibleItem(monthlyLimit, PeriodType.MONTHLY)
		];
	}

	function handleLossLimitForm() {
		const form = $("[name='updateLossLimitForm']");

		if(!form.data("validator")){
			initializeLossLimitValidator(form);
		}
		if(!form.valid()){
			return;
		}
		const dailyLimit = parseNumericValue(form.find("#newDailyLossLimit").val());
		// const weeklyLimit = parseNumericValue(form.find("#newWeeklyLossLimit").val());
		// const monthlyLimit = parseNumericValue(form.find("#newMonthlyLossLimit").val());

		if (dailyLimit === null
			// || weeklyLimit === null || monthlyLimit === null
		) {
			throw new Error("All limits must be valid numbers");
		}

		return [
			createResponsibleItem(dailyLimit, PeriodType.DAILY),
			// createResponsibleItem(weeklyLimit, PeriodType.WEEKLY),
			// createResponsibleItem(monthlyLimit, PeriodType.MONTHLY)
		];
	}

	function handleAccountReviewReminderForm() {
		const form = $("[name='updateAccountReviewReminderForm']");
		const accountReviewReminderValue = form.find("[name='accountReviewReminderType']").val();

		if (!accountReviewReminderValue) {
			throw new Error("Please select a account review reminder type");
		}

		return [createResponsibleItem(accountReviewReminderValue)];
	}

	function handleRealityCheckForm() {
		const form = $("[name='updateRealityCheckForm']");
		const realityCheckValue = form.find("[name='realityCheckType']").val();

		if (!realityCheckValue) {
			throw new Error("Please select a reality check type");
		}

		return [createResponsibleItem(realityCheckValue)];
	}

	function handleDepositLimitForm() {
		const form = $("[name='updateDepositLimitForm']");

		// 只初始化一次验证器
		if (!form.data("validator")) {
			initializeDepositLimitValidator(form);
		}

		if (!form.valid()) {
			return;
		}

		// 验证表单
		const dailyLimit = parseNumericValue(form.find("#newDailyDepositLimits").val());

		// 验证数值
		if (dailyLimit === null) {
			throw new Error("All limits must be valid numbers");
		}

		return [
			createResponsibleItem(dailyLimit, PeriodType.DAILY),
			// createResponsibleItem(weeklyLimit, PeriodType.WEEKLY),
			// createResponsibleItem(monthlyLimit, PeriodType.MONTHLY)
		];

	}

	function initializeDepositLimitValidator(form) {
		form.validate({
			rules: {
				newDailyDepositLimits: {
					required: true,
				},
				// newWeeklyDepositLimits: {
				// 	required: true,
					// validWeeklyLimit: "#newDailyDepositLimits"
				// },
				// newMonthlyDepositLimits: {
				// 	required: true,
					// validMonthlyLimit: {
					// 	daily: "#newDailyDepositLimits",
					// 	weekly: "#newWeeklyDepositLimits"
					// }
				// }
			},
			errorPlacement: function (error, element) {
				const errorMap = {
					'newDailyDepositLimits': '#dailyDepositLimitErrorMessage',
					// 'newWeeklyDepositLimits': '#weeklyDepositLimitErrorMessage',
					// 'newMonthlyDepositLimits': '#monthlyDepositLimitErrorMessage'
				};

				const errorElement = errorMap[element.attr("name")];
				if (errorElement) {
					element.closest('.form-group').find(errorElement).html(error);
				} else {
					error.insertAfter(element);
				}
			},
			success: function (label, element) {
				const errorMap = {
					'newDailyDepositLimits': '#dailyDepositLimitErrorMessage',
					// 'newWeeklyDepositLimits': '#weeklyDepositLimitErrorMessage',
					// 'newMonthlyDepositLimits': '#monthlyDepositLimitErrorMessage'
				};

				const errorElement = errorMap[$(element).attr("name")];
				if (errorElement) {
					$(errorElement).empty();
				}
			},
			ignore: []
		});
	}


	function initializeWagerLimitValidator(form) {
		form.validate({
			rules: {
				newDailyWagerLimit: {
					required: true,
				},
				// newWeeklyWagerLimit: {
				// 	required: true,
				// 	// validWeeklyLimit: "#newDailyWagerLimit"
				// },
				// newMonthlyWagerLimit: {
				// 	required: true,
				// 	validMonthlyLimit: {
				// 		daily: "#newDailyWagerLimit",
				// 		weekly: "#newWeeklyWagerLimit"
				// 	}
				// }
			},
			errorPlacement: function (error, element) {
				const errorMap = {
					'newDailyWagerLimit': '#dailyWagerLimitErrorMessage',
					// 'newWeeklyWagerLimit': '#weeklyWagerLimitErrorMessage',
					// 'newMonthlyWagerLimit': '#monthlyWagerLimitErrorMessage'
				};

				const errorElement = errorMap[element.attr("name")];
				if (errorElement) {
					element.closest('.form-group').find(errorElement).html(error);
				} else {
					error.insertAfter(element);
				}
			},
			success: function (label, element) {
				const errorMap = {
					'newDailyWagerLimits': '#dailyWagerLimitErrorMessage',
					// 'newWeeklyWagerLimits': '#weeklyWagerLimitErrorMessage',
					// 'newMonthlyWagerLimits': '#monthlyWagerLimitErrorMessage'
				};

				const errorElement = errorMap[$(element).attr("name")];
				if (errorElement) {
					$(errorElement).empty();
				}
			},
			ignore: []
		});
	}

	function initializeLossLimitValidator(form) {
		form.validate({
			rules: {
				newDailyLossLimit: {
					required: true,
				},
				// newWeeklyLossLimit: {
				// 	required: true,
				// 	validWeeklyLimit: "#newDailyLossLimit"
				// },
				// newMonthlyLossLimit: {
				// 	required: true,
				// 	validMonthlyLimit: {
				// 		daily: "#newDailyLossLimit",
				// 		weekly: "#newWeeklyLossLimit"
				// 	}
				// }
			},
			errorPlacement: function (error, element) {
				const errorMap = {
					'newDailyLossLimit': '#dailyLossLimitErrorMessage',
					// 'newWeeklyLossLimit': '#weeklyLossLimitErrorMessage',
					// 'newMonthlyLossLimit': '#monthlyLossLimitErrorMessage'
				};

				const errorElement = errorMap[element.attr("name")];
				if (errorElement) {
					element.closest('.form-group').find(errorElement).html(error);
				} else {
					error.insertAfter(element);
				}
			},
			success: function (label, element) {
				const errorMap = {
					'newDailyLossLimit': '#dailyLossLimitErrorMessage',
					// 'newWeeklyLossLimit': '#weeklyLossLimitErrorMessage',
					// 'newMonthlyLossLimit': '#monthlyLossLimitErrorMessage'
				};

				const errorElement = errorMap[$(element).attr("name")];
				if (errorElement) {
					$(errorElement).empty();
				}
			},
			ignore: []
		});
	}

	SettingHandler.saveAction = function (formName) {


		if (!formName) {
			NotifyHandler.errorMsg("Form name is required");
			return;
		}

		console.log("form name : ", formName);
		console.log("config : ",  FORM_CONFIGS[formName])


		const config = FORM_CONFIGS[formName];

		if (!config) {
			NotifyHandler.errorMsg("Unknown form type");
			return;
		}

		try {
			// 调用对应的表单处理函数
			const responsibleList = config.handler();
			if (!responsibleList) {
				return;
			}

			// 构建请求数据
			const data = {
				userId: PageConfig.userId,
				currencyTypeId: PageConfig.currencyTypeId,
				playerResponsibilityType: config.responsibilityType,
				responsibleList: JSON.stringify(responsibleList)
			};

			// 发送请求
			SettingHandler.submitResponsibility(data);

		} catch (error) {
			console.log("error: ", error)
		}
	};

	SettingHandler.submitResponsibility = function (data) {
		$.ajax({
			type: "POST",
			url: '/manager/member/updatePlayerResponsibilities',
			dataType: 'JSON',
			data: data,
			success: function (response) {
				if (response.error) {
					NotifyHandler.errorMsg(response.error);
					return;
				}
				NotifyHandler.successMsg(response.message);
				SettingHandler.getPlayerResponsibilities();
			},
			error: function (xhr, status, error) {
				NotifyHandler.errorMsg("Failed to update: " + error);
			}
		});
	};


})();




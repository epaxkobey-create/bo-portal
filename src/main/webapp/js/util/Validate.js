$(document).ready(function() {
	//$.extend($.validator.messages, validateProperties_EN);
	let errorShown = false;
	$.extend($.validator.defaults, {
		invalidHandler: function(form, validator) {
			const errors = validator.numberOfInvalids();
			if (errors && !errorShown) {
				errorShown = true;
				const message = $.validator.format($.validator.messages.missHighlightedFields, errors);
				NotifyHandler.errorMsg(message, 2000);
				setTimeout(() => errorShown = false, 2500); // 防止过快连发
			}
		},
		errorPlacement: function(error, element) {
			if (element.attr('type') === "file" && element.data('style') === "fileinput") {
				error.appendTo(element.closest("div.fileinput-holder").parent('div'));
			} else {
				error.insertAfter(element)
			}
		}
	});

	$.validator.addMethod("endDate", function(value, element, params) { // must be yyyy/MM/dd
		const endDate = $(params[0]).val();
		if (!endDate) {
			return true;
		}
		return (DateUtil.compare(endDate, value) >= 0);
	});

	$.validator.addMethod("startDate", function(value, element, params) { // must be yyyy/MM/dd
		const startDate = $(params[0]).val();
		if (!startDate) {
			return true;
		}
		return (DateUtil.compare(value, startDate) >= 0);
	});

	$.validator.addMethod("maxNumber", function(value, element, params) {
		let maxNumber = params[0];
		if (!maxNumber) {
			return true;
		}

		if (!value) {
			return true;
		}

		const fieldValue = Number(value.replace(/\,/g, ""));
		maxNumber = Number(maxNumber.replace(/\,/g, ""));
		return fieldValue <= maxNumber;
	}, jQuery.validator.messages.maxNumber);

	$.validator.addMethod("minNumber", function(value, element, params) {
		let minNumber = params[0];
		if (!minNumber) {
			return true;
		}

		if (!value) {
			return true;
		}

		const fieldValue = Number(value.replace(/\,/g, ""));
		minNumber = Number(minNumber.replace(/\,/g, ""));
		return fieldValue >= minNumber;
	}, jQuery.validator.messages.minNumber);

	$.validator.addMethod("biggerThanNumber", function(value, element, params) {
		let minNumber = params[0];
		if (!minNumber) {
			return true;
		}
		if (!value) {
			return true;
		}

		const fieldValue = Number(value.replace(/\,/g, ""));
		minNumber = Number(minNumber.replace(/\,/g, ""));
		return fieldValue > minNumber;
	}, jQuery.validator.messages.biggerThanNumber);

	$.validator.addMethod("rangeNumber", function(value, element, params) {
		const fieldValue = Number(value.replace(/\,/g, ""));
		const minNumber = Number(params[0].replace(/\,/g, ""));
		const maxNumber = Number(params[1].replace(/\,/g, ""));
		return (fieldValue >= minNumber && fieldValue <= maxNumber);
	}, jQuery.validator.messages.rangeNumber);

	$.validator.addMethod("require_from_group", function(value, element, params) {
		const validator = this;
		const selector = params[1];
		const validOrNot = $(selector, element.form).filter(function() {
			return validator.elementValue(this);
		}).length >= params[0];

		if (!$(element).data('being_validated')) {
			const fields = $(selector, element.form);
			fields.data('being_validated', true);
			fields.valid();
			fields.data('being_validated', false);
		}
		return validOrNot;
	});

	$.validator.addMethod("alphanumeric2", function(value, element, params) {
		return this.optional(element) || new RegExp("^[0-9a-zA-Z]+$").test(value);
	});
	$.validator.addMethod("alphanumeric2WithSpace", function(value, element, params) {
		return this.optional(element) || new RegExp("^[0-9a-zA-Z\\-_ ]+$").test(value);
	});
	$.validator.addMethod("date2", function(value, element, params) {
		return this.optional(element) || new RegExp("^(?:19|20)[0-9]{2}/(?:0[1-9]|1[012])/(?:0[1-9]|[12][0-9]|3[01])$").test(value);
	});
	$.validator.addMethod("dateTime", function(value, element, params) {
		return this.optional(element) || new RegExp("^(?:19|20)[0-9]{2}/(?:0[1-9]|1[012])/(?:0[1-9]|[12][0-9]|3[01]) (?:[01][0-9]|2[0-3])(?::[0-5][0-9]){2}$").test(value);
	});
	$.validator.addMethod("cellPhone", function(value, element, params) {
		const currency = parseInt(params[0]);
		switch (currency) {
			case CurrencyType.CAD.value:
				return this.optional(element) || new RegExp("^\\d{10}$").test(value);
			default:
				// return this.optional(element) || new RegExp("^\\d{8,11}$").test(value);
				return false;
		}
	});

	$.validator.addMethod("byCallingCode", function(value, element, params) {
		const callingCode = parseInt(params[0]);
		switch (callingCode) {
			case CallingCodeType.China.value:
				return this.optional(element) || new RegExp("^[1]\\d{10}$").test(value);
			case CallingCodeType.Singapore.value:
				return this.optional(element) || new RegExp("^[89]\\d{7}$").test(value);
			case CallingCodeType.Malaysia.value:
				return this.optional(element) || new RegExp("^0?[1-9]\\d{8,9}$").test(value);
			case CallingCodeType.Vietnam.value:
				return this.optional(element) || new RegExp("^\\d{9,10}$").test(value);
			case CallingCodeType.SouthKorea.value:
				return this.optional(element) || new RegExp("^\\d{10,11}$").test(value);
			case CallingCodeType.Thailand.value:
				return this.optional(element) || new RegExp("^\\d{9}$").test(value);
			case CallingCodeType.Indonesia.value:
				return this.optional(element) || new RegExp("^0?[8]\\d{7,13}$").test(value);
			case CallingCodeType.India.value:
				return this.optional(element) || new RegExp("^\\d{10}$").test(value);
			case CallingCodeType.Bangladesh.value:
				return this.optional(element) || new RegExp("^\\d{10,11}$").test(value);
			case CallingCodeType.Philippines.value:
				return this.optional(element) || new RegExp("^[9]\\d{9}$").test(value);
			case CallingCodeType.Pakistan.value:
				return this.optional(element) || new RegExp("^\\d{10}$").test(value);
			case CallingCodeType.Mexico.value:
				return this.optional(element) || new RegExp("^\\d{10}$").test(value);
			case CallingCodeType.US.value:
				return this.optional(element) || new RegExp("^\\d{10}$").test(value);
			case CallingCodeType.Cambodia.value:
				return this.optional(element) || new RegExp("^\\d{8,9}$").test(value);
			case CallingCodeType.Brazil.value:
				return this.optional(element) || new RegExp("^\\d{10,11}$").test(value);
			case CallingCodeType.Nigeria.value:
				return this.optional(element) || new RegExp("^\\d{10}$").test(value);
			case CallingCodeType.ZA.value:
				return this.optional(element) || new RegExp("^\\d{9}$").test(value);
			case CallingCodeType.GH.value:
				return this.optional(element) || new RegExp("^\\d{9}$").test(value);
			case CallingCodeType.SriLanka.value:
				return this.optional(element) || new RegExp("^\\d{9,10}$").test(value);
			case CallingCodeType.Nepal.value:
				return this.optional(element) || new RegExp("^\\d{10}$").test(value);
			case CallingCodeType.Australia.value:
				return this.optional(element) || new RegExp("^\\d{9}$").test(value);
			case CallingCodeType.NewZealand.value:
				return this.optional(element) || new RegExp("^\\d{8,10}$").test(value);
			case CallingCodeType.Afghanistan.value:
				return this.optional(element) || new RegExp("^\\d{9,10}$").test(value);
			case CallingCodeType.Bhutan.value:
				return this.optional(element) || new RegExp("^\\d{8,9}$").test(value);
			case CallingCodeType.Maldives.value:
				return this.optional(element) || new RegExp("^\\d{7}$").test(value);
			case CallingCodeType.Iran.value:
				return this.optional(element) || new RegExp("^\\d{10}$").test(value);
			case CallingCodeType.Canada.value:
				return this.optional(element) || new RegExp("^\\d{10}$").test(value);
			case CallingCodeType.HongKong.value:
				return this.optional(element) || new RegExp("^[569]\\d{8}$").test(value);
			default:
				return this.optional(element) || new RegExp("^\\d{8,11}$").test(value);
		}
	});

	$.validator.addMethod("custom", function(value, element, params) {
		return this.optional(element) || params;
	});

	$.validator.addMethod("qqId", function(value, element, params) {
		return this.optional(element) || new RegExp("^\\d{5,11}$").test(value);
	});
	$.validator.addMethod("weChatId", function(value, element, params) {
		return this.optional(element) || new RegExp("^[a-zA-Z]{1}[a-zA-Z0-9_-]{6,20}$").test(value);
	});
	$.validator.addMethod("skypeId", function(value, element, params) {
		return this.optional(element) || new RegExp("^[a-zA-Z]{1}[a-zA-Z0-9\\.,_-]{6,32}$").test(value);
	});
	$.validator.addMethod("telegramId", function(value, element, params) {
		return this.optional(element) || new RegExp("^[a-zA-Z]{1}[a-zA-Z0-9]{5,32}$").test(value);
	});
	$.validator.addMethod("lineId", function(value, element, params) {
		return this.optional(element) || new RegExp("^[a-zA-Z0-9\\._-]{4,48}$").test(value);
	});
	$.validator.addMethod("moneyAmount", function(value, element, params) {
		const scale = $(element).attr('data-scale');
		if (scale != null && scale == '0') {
			// CSHMPS-156 IM-MWG special case : this vendor only allow integer
			return this.optional(element) || new RegExp("^\\d{1,20}$").test(value);
		}
		return this.optional(element) || new RegExp("^\\d{1,20}(\\.\\d{1,2})?$").test(value);
	});
	$.validator.addMethod("decimal", function(value, element, params) {
		const scale = $(element).attr('data-scale');
		if (scale != null && scale == '0') {
			// CSHMPS-156 IM-MWG special case : this vendor only allow integer
			return this.optional(element) || new RegExp("^-?\\d{1,20}$").test(value);
		}
		return this.optional(element) || new RegExp("^-?\\d{1,20}(\\.\\d{1," + params + "})?$").test(value);
	});
	$.validator.addMethod("number", function(value, element, params) {
		const scale = $(element).attr('data-scale');
		if (scale != null && scale == '0') {
			// CSHMPS-156 IM-MWG special case : this vendor only allow integer
			return this.optional(element) || new RegExp("^-?\\d{1,20}$").test(value);
		}
		return this.optional(element) || new RegExp("^-?\\d{1,20}(\\.\\d{1,2})?$").test(value);
	});
	$.validator.addMethod("cnBankAccNumber", function(value, element, params) {
		return this.optional(element) || new RegExp("^\\d{1,19}$").test(value);
	});
	$.validator.addMethod("userId", function(value, element, params) {
		return this.optional(element) || new RegExp("^[0-9a-z]{4,15}$").test(value);
	}, jQuery.validator.messages.invalidUserIdFormat);
	$.validator.addMethod("userName", function(value, element, params) {
		return this.optional(element) || new RegExp("^[a-zA-Z. -]{1,13}$").test(value);
	});
	$.validator.addMethod("managerId", function(value, element, params) {
		return this.optional(element) || new RegExp("^[0-9a-z]{6,12}$").test(value);
	}, jQuery.validator.messages.invalidFormat);
	$.validator.addMethod("affiliateUserId", function(value, element, params) {
		return this.optional(element) || new RegExp("^[0-9a-z]{6,18}$").test(value);
	}, jQuery.validator.messages.invalidFormat);
	$.validator.addMethod("password2", function(value, element, params) {
		let regExp = new RegExp("^[0-9a-zA-Z]{6,20}$");
		let regExpAllNum = new RegExp("^[0-9]{6,20}$");
		let regExpAllLetter = new RegExp("^[a-zA-Z]{6,20}$");
		return this.optional(element) ||
			(regExp.test(value) && !regExpAllNum.test(value) && !regExpAllLetter.test(value));
	});

	$.validator.addMethod("playerLoginPassword", function(value, element, params) {
		let regExp = new RegExp("^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*#])[A-Za-z\\d@$!%*#]{6,20}$")
		return this.optional(element) || (regExp.test(value));
	});

	$.validator.addMethod("playerPasswordEqualToAccount", function(value, element, params) {
		let target = $(params);
		return this.optional(element) || value.toUpperCase() !== target.val().toUpperCase();
	});

	$.validator.addMethod("password918kiss", function(value, element, params) {
		let regExp = new RegExp("^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z]).{6,15}$");
		return this.optional(element) || regExp.test(value);
	}, jQuery.validator.messages.password918Kiss);
	$.validator.addMethod("pattern", function(value, element, param) {
		if (this.optional(element)) {
			return true;
		}
		if (typeof param === 'string') {
			param = new RegExp('^(?:' + param + ')$');
		}
		return param.test(value);
	}, jQuery.validator.messages.invalidFormat);
	$.validator.addMethod("DBNumber", function(value, element, params) {
		return this.optional(element) || new RegExp('^-?\\d{0,' + (params[0] - params[1]) + '}(\\.\\d{0,' + params[1] + '})?$').test(value);
	}, jQuery.validator.messages.number);
	$.validator.addMethod('minStrict', function(value, element, param) {
		return this.optional(element) || value > param;
	});
	$.validator.addMethod('notEqual', function(value, element, param) {
		return this.optional(element) || value != 0;
	});

	$.validator.addMethod('noUrlCharacter', function(value, element, param) {
		return this.optional(element) || !new RegExp("[#%&=/.?:]").test(value);
	});

	$.validator.addMethod("checkAgeOver18", function(value, element, params) {

		const birthday = new Date(element.value);

		const currentDate = new Date();
		//Be careful! January is 0 not 1
		const month = currentDate.getMonth() + 1;
		const eighteenYearsAgo = new Date((currentDate.getFullYear() - 18) + '/' + month + '/' + currentDate.getDate());

		//Trace.info(eighteenYearsAgo);
		//Trace.info(birthday);

		return this.optional(element) || birthday <= eighteenYearsAgo;
	});

	$.validator.addMethod("checkAgeOver21", function(value, element, params) {

		const birthday = new Date(element.value);

		const currentDate = new Date();
		//Be careful! January is 0 not 1
		const month = currentDate.getMonth() + 1;
		const eighteenYearsAgo = new Date((currentDate.getFullYear() - 21) + '/' + month + '/' + currentDate.getDate());

		//Trace.info(eighteenYearsAgo);
		//Trace.info(birthday);

		return this.optional(element) || birthday <= eighteenYearsAgo;
	});
	$.validator.addMethod("maxByteLength", function(value, element, param) {
		if (this.optional(element)) {
			return true;
		}
		return encodeURIComponent(value).replace(/%[A-F\d]{2}/g, 'U').length <= param;
	}, jQuery.validator.messages.maxByteLength);
	$.validator.addMethod("tagsinput", function(value, element, params) {
		const required = params[0];
		const decimal = params[2];
		const integer = params[1] - decimal;
		const minStrict = params[3];
		const tmp = [];
		let invalid = true;
		$('#' + element.id + '_tagsinput span span').each(function(idx, item) {
			var tmpVal = $(item).text().trim();
			if (new RegExp('^-?\\d{0,' + (integer) + '}(\\.\\d{0,' + decimal + '})?$').test(tmpVal) && tmpVal > minStrict) {
				tmp.push(tmpVal);
			} else {
				invalid = false;
				return false;
//				$('#'+element.id).removeTag(tmpVal);
			}
		});
		return invalid;
//		if (!required) {
//			return 0 == tmp.length;
//		}
//		return tmp.length > 0;
	});

	$.validator.addMethod("gender", function(value, element, params) {
		return parseInt(value) >= 0;

	});

	$.validator.addMethod("marital", function(value, element, params) {
		return parseInt(value) >= 0;
	});

	$.validator.addMethod("exactLength", function(value, element, param) {
		return this.optional(element) || value.length === param;
	}, jQuery.validator.messages.exactLength);


	$.validator.addMethod("maxDigitalLength", function(value, element, param) {
		let formatValue = value.replace(/\,/g, "");
		if (formatValue.match(/\./)) {
			formatValue = formatValue.replace(/\.?0*$/, '');
		}
		return this.optional(element) || formatValue.length <= param;
	}, jQuery.validator.messages.maxDigitslength);

	$.validator.addMethod("creditNumber", function(value, element, params) {
		return this.optional(element) || new RegExp("^\\d{15,16}$").test(value);
	});

	$.validator.addMethod("cvvNumber", function(value, element, params) {
		return this.optional(element) || new RegExp("^\\d{3,4}$").test(value);
	});
	$.validator.addMethod("expDate", function(value, element, params) {
		return this.optional(element) || new RegExp("^\\d{4}$").test(value);
	});

	$.validator.addMethod("upiCode", function(value, element, params) {
		return this.optional(element) || new RegExp("^(?=.{1,50}$)(?=.*@)[.\\-_A-Za-z0-9]+@+[.\\-_A-Za-z0-9]+$").test(value);
	});

	$.validator.addMethod("referenceNo", function(value, element, params) {
		const currency = params[2];
		if (currency !== CurrencyType.INR.unique()) {
			return true;
		}
		const companyBankPaymentType = CompanyBankPaymentType.getInstanceOf(params[0]);
		if (companyBankPaymentType === null) {
			return true;
		}
		if (companyBankPaymentType === CompanyBankPaymentType.PHONEPE
			|| companyBankPaymentType === CompanyBankPaymentType.GPAY
			|| companyBankPaymentType === CompanyBankPaymentType.PAYTM
			|| companyBankPaymentType === CompanyBankPaymentType.UPI) {

			if (companyBankPaymentType === CompanyBankPaymentType.PHONEPE
				|| companyBankPaymentType === CompanyBankPaymentType.GPAY
				|| companyBankPaymentType === CompanyBankPaymentType.UPI) {
				return new RegExp("^\\d{12}$").test(value);
			}
			if (companyBankPaymentType === CompanyBankPaymentType.PAYTM) {
				const validator1 = new RegExp("^\\d{12}$");
				const validator2 = new RegExp("^\\d{18,19}$");
				return validator1.test(value) || validator2.test(value);
			}
		}
		if (companyBankPaymentType === CompanyBankPaymentType.REAL_BANK) {
			const bankTransferType = BankTransferType.getInstanceOf(params[1]);
			if (bankTransferType === BankTransferType.IMPS
				|| bankTransferType === BankTransferType.INTERNAL_ICCI) {
				return new RegExp("^\\d{12}$").test(value);
			}
			if (bankTransferType === BankTransferType.RTGS) {
				return new RegExp("^[0-9A-Z]{22}$").test(value);
			}
			if (bankTransferType === BankTransferType.NEFT) {
				return new RegExp("^[0-9A-Z]{16}$").test(value);
			}
			if (bankTransferType === BankTransferType.INTERNAL_SBIN) {
				return new RegExp("^[0-9A-Z]{10}$").test(value);
			}
		}
		return true;

	}, jQuery.validator.messages.referenceNo);

	$.validator.addMethod("notZero", function (value, element, param) {
		return parseFloat(value) !== 0;
	}, function (params, element) {
		return params; // use the passed string as the error message
	});

});

if (typeof (ValidatorUtil) == 'undefined') {
	ValidatorUtil = {};
}

(function() {
	//
	/*
	ValidatorUtil.setLanguage = function(lang) {
		if(lang == 'cn'){
			$.extend($.validator.messages, validateProperties_CH);
		}else{
			$.extend($.validator.messages, validateProperties_EN);
		}
	};
	*/

	ValidatorUtil.applyMultiInputNamingRules = function(element, index, rules) {
		if (element.prop('alt')) {
			return;
		}
		element.attr('alt', element.attr('name'));
		element.attr('name', element.attr('name') + '-' + index);
		element.rules('add', rules);
	};
	ValidatorUtil.removeMultiInputNamingRules = function(element) {
		element.attr('name', element.attr('alt'));
		element.removeAttr('alt');
	};

	$.validator.addMethod("yearDateFormat", function(value, element) {
		if (!value) return true;
		return moment($.trim(value), "DD/MM/YYYY", true).isValid();
	}, "Invalid date format");

	$.validator.addMethod("passwordContainsSpaceOrUnicode", function(value, element) {
		return !/[\s\u0080-\uFFFF]/.test(value);
	}, "Password must not contain spaces or non-ASCII characters.");

	$.validator.addMethod("passwordContainsSpace", function(value, element) {
		return !/\s/.test(value);
	}, "Password cannot contain spaces.");

	$.validator.addMethod("passwordContainsUnicode", function(value, element) {
		return !/[^\x00-\x7F]/.test(value);
	}, "Only standard english letters, numbers, and symbols are allowed.");

	$.validator.addMethod("requiredFileWithPreview", function(value, element, params) {
		const hasFile = element.files && element.files.length > 0;
		const previewId = params;
		const hasPreview = document.getElementById(previewId).querySelector("img") !== null;
		return hasFile || hasPreview;
	});


	$.validator.addMethod("fileTypeChecking", function(value, element) {
		if (!value) return true;

		const extensionValid = /\.(jpe?g|png|heic|webp|pdf)$/i.test(value);
		if (element.files && element.files[0]) {
			const mimeType = element.files[0].type;
			const mimeTypeValid = /^(image\/(jpeg|png|heic|webp)|application\/pdf)$/.test(mimeType);
			return extensionValid && mimeTypeValid;
		}
		return extensionValid;
	}, "Unsupported file type!");

	$.validator.addMethod("minMaxAmountChecking", function(value, element) {
		if (value === "") return true;

		var currentFieldName = $(element).attr('name');
		var otherFieldName = currentFieldName === 'minAmount' ? 'maxAmount' : 'minAmount';


		var otherElement = $(element).closest('form').find(`[name="${otherFieldName}"]`);
		if (otherElement.length === 0) {
			otherElement = $(`[name="${otherFieldName}"]`);
		}

		var otherValue = otherElement.val();
		if (otherValue === "") return true;

		var currentVal = parseFloat(value.replace(/,/g, ''));
		var otherVal = parseFloat(otherValue.replace(/,/g, ''));

		if (isNaN(currentVal) || isNaN(otherVal)) return true;
		if (currentFieldName === 'minAmount') {
			return currentVal <= otherVal;
		} else {
			return currentVal >= otherVal;
		}
	}, "The Min amount must not be greater than the Max amount.");


	$.validator.addMethod("passwordRequirements", function(value, element) {
		var lengthValid = value.length >= 6 && value.length <= 20;
		var uppercaseValid = /[A-Z]/.test(value);
		var lowercaseValid = /[a-z]/.test(value);
		var digitValid = /[0-9]/.test(value);
		var symbolValid = /[@$!%*#]/.test(value);

		return lengthValid && uppercaseValid && lowercaseValid && digitValid && symbolValid;
	}, "Password must meet all requirements");


	$.validator.addMethod("validWeeklyLimit", function(value, element, params) {
		// params: selector for daily field, e.g., "#newDailyDepositLimits" or "[name='dailyLimit']"
		const dailySelector = params || "[name='dailyLimit']";
		const dailyVal = $(dailySelector).val();

		const dailyValue = parseFloat(String(dailyVal).replace(/,/g, '')) || 0;
		const weeklyValue = parseFloat(String(value).replace(/,/g, '')) || 0;

		if (dailyValue === 0) {
			return weeklyValue === 0;
		}

		if (weeklyValue === 0) {
			return true;
		}

		return weeklyValue >= dailyValue;
	}, "Weekly Limit cannot be lesser than Daily Limit");

	$.validator.addMethod("validMonthlyLimit", function(value, element, params) {
		// params: { daily: selector, weekly: selector }
		const dailySelector = params?.daily || "[name='dailyLimit']";
		const weeklySelector = params?.weekly || "[name='weeklyLimit']";

		const dailyVal = $(dailySelector).val();
		const dailyValue = parseFloat(String(dailyVal).replace(/,/g, '')) || 0;

		const weeklyVal = $(weeklySelector).val();
		const weeklyValue = parseFloat(String(weeklyVal).replace(/,/g, '')) || 0;

		const monthlyValue = parseFloat(String(value).replace(/,/g, '')) || 0;

		if (dailyValue === 0 || weeklyValue === 0) {
			return monthlyValue === 0;
		}
		if (monthlyValue === 0) {
			return true;
		}
		return monthlyValue >= weeklyValue;
	}, "Monthly Limit cannot be lesser than Daily Limit & Weekly Limit");

	$.validator.addMethod("cleanedDigits", function(value, element) {
		if (!value || value.trim() === '') {
			return true;
		}
		const cleaned = value.replace(/\s/g, '').trim();

		return cleaned.length > 0 && /^\d+$/.test(cleaned);
	}, "Account number must contain only digits");


	$.validator.addMethod("cleanedMinLength", function(value, element, param) {
		const cleaned = value.replace(/\s/g, '');
		return this.optional(element) || cleaned.length >= param;
	}, "Account number must be 8 ~ 34 digits");

	$.validator.addMethod("cleanedMaxLength", function(value, element, param) {
		const cleaned = value.replace(/\s/g, '');
		return this.optional(element) || cleaned.length <= param;
	}, "Account number must be 8 ~ 34 digits");
})();

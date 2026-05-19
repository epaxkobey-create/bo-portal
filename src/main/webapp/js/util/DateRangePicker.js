if (typeof (DateRangeHandler) == 'undefined') {
	DateRangeHandler = {};
}

(function() {
	DateRangeHandler.singleDateTimePicker = function(pOptions, lang) {
		var options = {
			"singleDatePicker": true,
			"timePicker": true,
			"timePickerSeconds": true,
			"timePicker24Hour": true,
			autoUpdateInput: false,
			locale: DateRangeHandler.changeLanguage('DD/MM/YYYY HH:mm:ss', lang)
		};
		if (pOptions) {
			$.extend(options, pOptions);
		}
		$('.singleDateTimePicker').daterangepicker(options, function(start, end) {
			$(this).val(start.format(options.locale.format));
		});

		$('.singleDateTimePicker').on('cancel.daterangepicker', function(ev, picker) {
			$(this).val('');
		});
		$('.singleDateTimePicker').on('keydown paste input beforeinput', function(e) {
			if (e.key !== 'Tab') {
				e.preventDefault();
			}
		});
	};
	DateRangeHandler.singleDatePicker = function(pOptions, lang) {
		var options = {
			"singleDatePicker": true,
			autoUpdateInput: false,
			locale: DateRangeHandler.changeLanguage('DD/MM/YYYY', lang)
		};
		if (pOptions) {
			$.extend(options, pOptions);
		}
		$('.singleDatePicker').daterangepicker(options, function(start, end) {
			$(this).val(start.format(options.locale.format));
		});

		$('.singleDatePicker').on('cancel.daterangepicker', function(ev, picker) {
			$(this).val('');
		});
		$('.singleDatePicker').on('keydown paste input beforeinput', function(e) {
			if (e.key !== 'Tab') {
				e.preventDefault();
			}
		});
	};

	DateRangeHandler.init = function(lang) {
		DateRangeHandler.singleDateTimePicker(null, lang);
		DateRangeHandler.singleDatePicker(null, lang);
		DateRangeHandler.bindEvent();
	}

	DateRangeHandler.bindEvent = function() {
		$('input:data(daterangepicker)').on('apply.daterangepicker', function(e, picker) {
			$(this).val(picker.startDate.format(picker.locale.format));
		});
		$('.calendar-icon').click(function() {
			$(this).parent().find('input').click();
		});
	}

	DateRangeHandler.changeLanguage = function(customerFormat, lang) {

		var locale = {
			format: customerFormat
		};

		if (lang === 'cn') {
			locale.applyLabel = '确定';
			locale.cancelLabel = '取消';
			locale.weekLabel = '星期';
			locale.customRangeLabel = '自选范围';
			locale.daysOfWeek = ["日", "一", "二", "三", "四", "五", "六"];
			locale.monthNames = ["一月", "二月", "三月", "四月", "五月", "六月", "七月", "八月", "九月", "十月", "十一月", "十二月"];
		}

		return locale;
	}




	DateRangeHandler.singleDatePicker2 = function($input, pOptions, lang) {
		var options = {
			singleDatePicker: true,
			autoUpdateInput: false,
			locale: DateRangeHandler.changeLanguage('DD/MM/YYYY', lang)
		};

		if (pOptions) {
			options = {
				...options,
				...pOptions
			};
		}

		$input.daterangepicker(options, function(start) {
			$input.val(start.format(options.locale.format));
		});

		$input.on('cancel.daterangepicker', function() {
			$input.val('');
		});

		$input.on('keydown paste input beforeinput', function(e) {
			if (e.key !== 'Tab') {
				e.preventDefault();
			}
		});
	};
})();
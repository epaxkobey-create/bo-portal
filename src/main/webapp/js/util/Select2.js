if (typeof (Select2Handler) == 'undefined') {
	Select2Handler = {};
}

(function() {
	Select2Handler.create = function(data, selector) {
		reloadSelector(data, selector);
		return $(selector).select2({
			minimumInputLength: 3,
//			width: '100%'
		});
	};

	var reloadSelector = function(data, idSelector) {
		var selector = $(idSelector)[0];

		while (selector.options.length > 0) {
			selector.remove(0);
		}
		var frag = document.createDocumentFragment();
		for (var i = 0; i < data.length; i++) {
			var option = document.createElement("option");
			option.value = data[i].value;
			option.text = data[i].text;
			frag.appendChild(option);
		}
		selector.appendChild(frag);
	};

	Select2Handler.format = function(collections, key, value) {
		return $.map(collections, function(item) {
			if (!key) {
				return {value: item, text: item};
			}
			if (!value) {
				return {value: item[key], text: item[key]};
			}
			return {value: item[key], text: item[value]};
		});
	};

	Select2Handler.init = function(selector, pOptions) {
		if (pOptions) {
			return $(selector).select2(pOptions);
		}
		return $(selector).select2();
	}

	Select2Handler.search = function(pOptions, selector, toLowerCase) {
		var callback = function(data, params) {
			return {
				results: $.map(data, function(item) {
					return {text: item, id: item}
				})
			}
		};

		if (pOptions.ajaxResult) {
			callback = pOptions.ajaxResult;
		}

		var options = {
			width: '100%',
			multiple: true,
			minimumInputLength: 1,
			ajax: {
				type: "GET",
				url: pOptions.ajaxPath,
				data: function(term) {
					var parameter = {
						search: (toLowerCase ? term.toLowerCase() : term)
					};
					if (pOptions.ajaxParam) {
						Object.assign(parameter, pOptions.ajaxParam);
					}
					return parameter
				},
				dataType: 'json',
				results: callback
			}
		};

		if (pOptions) {
			$.extend(options, pOptions);
		}

		return $(selector).select2(options);
	};

})();
if (typeof (DualListBoxHandler) == 'undefined') {
	DualListBoxHandler = {};
}

(function() {
	DualListBoxHandler.init = function(pOptions) {
		var options = {
			useFilters: false,
			useCounters: false,
			useSorting: false,
			selectOnSubmit: false,
		};
		if (pOptions) {
			$.extend(options, pOptions);
		}
		$.configureBoxes(options);
	};

	DualListBoxHandler.autoSelectTarget = function(options) {
		$('#' + (options && options.box2View ? options.box2View : 'box2View') + ' option').prop('selected', true);
	};

	DualListBoxHandler.format = function(collections, key, value) {
		return $.map(collections, function(item) {
			if (!value) {
				return {value: item[key], text: item[key]};
			}
			return {value: item[key], text: item[value]};
		});
	};

	DualListBoxHandler.reloadSelector = function(data, id) {
		var selector = document.getElementById(id);
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

	DualListBoxHandler.getComplementOfEventTarget = function(source, target) {
		return $.grep(
			source,
			function(item) {
				for (var i = 0; i < target.length; i++) {
					if (target[i].value == item.value) {
						return false;
					}
				}
				return true;
			}
		);
	};

	DualListBoxHandler.clearFilterValue = function(id) {
		$('#' + id + 'box1Clear').trigger('click');
		$('#' + id + 'box2Clear').trigger('click');
	};

})();
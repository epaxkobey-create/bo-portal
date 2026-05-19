EligibilityHandler = function() {

	let containExclude = false;
	const memberGroupOption = {
		box1View: 'memberGroupbox1View',
		box1Storage: 'memberGroupbox1Storage',
		box1Filter: 'memberGroupbox1Filter',
		box1Clear: 'memberGroupbox1Clear',
		box1Counter: 'memberGroupbox1Counter',
		box2View: 'memberGroupbox2View',
		box2Storage: 'memberGroupbox2Storage',
		box2Filter: 'memberGroupbox2Filter',
		box2Clear: 'memberGroupbox2Clear',
		box2Counter: 'memberGroupbox2Counter',
		to1: 'memberGroupto1',
		allTo1: 'memberGroupallTo1',
		to2: 'memberGroupto2',
		allTo2: 'memberGroupallTo2',
		useFilters: true,
		useCounters: false,
		useSorting: false,
		selectOnSubmit: false
	};
	const excludeMemberGroupOption = {
		box1View: 'excludeMemberGroupbox1View',
		box1Storage: 'excludeMemberGroupbox1Storage',
		box1Filter: 'excludeMemberGroupbox1Filter',
		box1Clear: 'excludeMemberGroupbox1Clear',
		box1Counter: 'excludeMemberGroupbox1Counter',
		box2View: 'excludeMemberGroupbox2View',
		box2Storage: 'excludeMemberGroupbox2Storage',
		box2Filter: 'excludeMemberGroupbox2Filter',
		box2Clear: 'excludeMemberGroupbox2Clear',
		box2Counter: 'excludeMemberGroupbox2Counter',
		to1: 'excludeMemberGroupto1',
		allTo1: 'excludeMemberGroupallTo1',
		to2: 'excludeMemberGroupto2',
		allTo2: 'excludeMemberGroupallTo2',
		useFilters: true,
		useCounters: false,
		useSorting: false,
		selectOnSubmit: false
	};

	const $affiliates = $('[name=affiliates]');
	const $excludeAffiliates = $('[name=excludeAffiliates]');

	const $players = $('[name=players]');
	const $excludePlayers = $('[name=excludePlayers]');

	const $excludeSwitch = $('#excludeSwitch');
	const $allPlayers = $('#allPlayers');

	const initGroup = function() {
		DualListBoxHandler.init(memberGroupOption);

		if (containExclude) {
			DualListBoxHandler.init(excludeMemberGroupOption);
		}
	};

	const bindExcludeSwitch = function() {
		$excludeSwitch.on('switch-change', function(event, state) { // true: YES, false: NO

			$('[name=excludePlayers]').select2("val", "");
			$('[name=excludeAffiliates]').select2("val", "");

			$.each($('[name=excludeVipLevel]'), function() {
				$(this).prop('checked', false);
			});
			$('[name=excludeVipLevel]').uniform();

			let currency = $('[name=currency]').val();
			if (!currency) {
				currency = $('[name=currencyType]').val();
			}

			var source = EligibilityHandler.getGroupData(currency);
			if (!source) {
				source = [];
			}

			EligibilityHandler.renderTable([], source, false);

			if (state.value === true) {
				$('#excludePlayerDiv').show();
				$('#excludeAffiliateDiv').show();
				$('#excludeVIPDiv').show();
				$('#excludeGroupDiv').show();

				$(this).siblings('[name=excludeStatus]').val(BinaryStatusType.ACTIVE.unique());
			} else {
				$('#excludePlayerDiv').hide();
				$('#excludeAffiliateDiv').hide();
				$('#excludeVIPDiv').hide();
				$('#excludeGroupDiv').hide();

				$(this).siblings('[name=excludeStatus]').val(BinaryStatusType.INACTIVE.unique());
			}
		});
	};

	const queryAllAffiliateUserId = function(path) {
		const options = {
			minimumInputLength: 2,
			multiple: true,
			ajaxPath: path,
			initSelection: function(element, callback) {
				callback($.map(element.val().split(','), function(id) {
					return {id: id, text: id};
				}));
			}
		};

		Select2Handler.search(options, $affiliates);

		if (containExclude) {
			Select2Handler.search(options, $excludeAffiliates);
		}
	};

	const bindAllPlayers = function() {
		$allPlayers.click(function() {
			if ($(this).prop('checked')) {
				$players.select2("enable", false);
				$players.select2("val", "");

				$affiliates.select2("enable", false);
				$affiliates.select2("val", "");

				$.each($('[name=vipLevel]'), function() {
					$(this).prop('disabled', true);
					$(this).prop('checked', false);
				});
				$('[name=vipLevel]').uniform();

				$('[id^=memberGroup]').attr("disabled", true);

				let currency = $('[name=currency]').val();
				if (!currency) {
					currency = $('[name=currencyType]').val();
				}

				var source = EligibilityHandler.getGroupData(currency);
				if (!source) {
					source = [];
				}

				EligibilityHandler.renderTable([], source, true);

			} else {
				$players.select2("enable", true);
				$affiliates.select2("enable", true);
				$.each($('[name=vipLevel]'), function() {
					$(this).prop('disabled', false);
				});
				$('[name=vipLevel]').uniform();
				$('[id^=memberGroup]').removeAttr("disabled");
			}
		});
	};

	const queryAllUserId = function(path, byCurrency) {
		const options = {
			minimumInputLength: 3,
			multiple: true,
			ajaxPath: path,
			initSelection: function(element, callback) {
				callback($.map(element.val().split(','), function(id) {
					return {id: id, text: id};
				}));
			}
		};

		if (byCurrency) {
			options.ajaxParam = {
				currency: function() {
					const $form = $players.closest("form");
					let currency = $form.find('[name=currency]').val();
					if (!currency) {
						currency = $form.find('[name=currencyType]').val();
					}
					return currency;
				}
			}
		}

		options.ajaxParam.selectUserId = function() {
			return $players.closest("form").find('[name="players"]').val();
		}

		Select2Handler.search(options, $players);

		if (containExclude) {
			Select2Handler.search(options, $excludePlayers);
		}
	};

	const setAffiliateData = function(data, isInclude) {
		if (isInclude) {
			$affiliates.select2('val', data.split(","), false);
		} else {
			$excludeAffiliates.select2('val', data.split(","), false);
		}
	};

	const setPlayerData = function(data, isInclude) {
		if (isInclude) {
			$players.select2('val', data.split(","), false);
		} else {
			$excludePlayers.select2('val', data.split(","), false);
		}
	};

	const clearPlayer = function() {
		$players.select2("val", "");
		if (containExclude) {
			$excludePlayers.select2("val", "");
		}
	};

	const appendVIP = function(vips) {

		$('#vipLevelGroup').empty();
		if (containExclude) {
			$('#excludeVipLevelGroup').empty();
		}

		if (!vips) {
			return;
		}

		vips.sort(function(a, b) {
			return a.id - b.id
		});

		$.each(vips, function(i, el) {
			$('#vipLevelGroup').append('<label class="checkbox-inline"><input type="checkbox" class="uniform" name="vipLevel" value="' + el.id + '" />'
				+ el.name + '</label>');
		});

		$('[name=vipLevel]').prop("disabled", $("#allPlayers").prop("checked")).uniform();

		if (containExclude) {
			$.each(vips, function(i, el) {
				$('#excludeVipLevelGroup').append('<label class="checkbox-inline"><input type="checkbox" class="uniform" name="excludeVipLevel" value="' + el.id + '" />'
					+ el.name + '</label>');
			});

			$('[name=excludeVipLevel]').uniform();
		}
	};

	const setVIPData = function(data, isInclude) {
		if (isInclude) {
			$.each(data.split(","), function(i, element) {
				$('[name=vipLevel][value=' + element + ']').prop('checked', true);
			});
			$('[name=vipLevel]').uniform();
		} else {
			$.each(data.split(","), function(i, element) {
				$('[name=excludeVipLevel][value=' + element + ']').prop('checked', true);
			});
			$('[name=excludeVipLevel]').uniform();
		}
	};

	const insertGroupData = function(group) {
		if (group.length > 0) {
			DataBase.group.clean();

			$.each(group, function(i, element) {
				DataBase.group.insert(element);
			});
		}
	};

	const getGroupData = function(currency) {
		return DataBase.group.queryByID(currency);
	};

	const renderTable = function(targetData, sourceData, isInclude) {

		if (isInclude) {
			$('#memberGroupbox1View').empty();
			$('#memberGroupbox2View').empty();
			var target = DualListBoxHandler.format(targetData, 'id', 'name');
			var source = DualListBoxHandler.getComplementOfEventTarget(DualListBoxHandler.format(sourceData, 'id', 'name'), target);

			DualListBoxHandler.reloadSelector(source, 'memberGroupbox1View');
			DualListBoxHandler.reloadSelector(target, 'memberGroupbox2View');
		} else {
			$('#excludeMemberGroupbox1View').empty();
			$('#excludeMemberGroupbox2View').empty();

			var target = DualListBoxHandler.format(targetData, 'id', 'name');
			var source = DualListBoxHandler.getComplementOfEventTarget(DualListBoxHandler.format(sourceData, 'id', 'name'), target);

			DualListBoxHandler.reloadSelector(source, 'excludeMemberGroupbox1View');
			DualListBoxHandler.reloadSelector(target, 'excludeMemberGroupbox2View');
		}
	};

	const setGroupData = function(groupData, currency, isInclude) {
		var source = [];
		var target = [];
		var currencyGroup = EligibilityHandler.getGroupData(currency);
		if (groupData) {
			var data = groupData.split(',');
			$.each(currencyGroup, function(i, element) {
				var index = $.inArray(String(element.id), data);
				if (index !== -1) {
					target.push(element);
				} else {
					source.push(element);
				}
			});
		} else if (currencyGroup) {
			source = currencyGroup;
		}
		EligibilityHandler.renderTable(target, source, isInclude);
	};

	const selectedGroup = function() {
		DualListBoxHandler.autoSelectTarget(memberGroupOption);

		if (containExclude) {
			DualListBoxHandler.autoSelectTarget(excludeMemberGroupOption);
		}
	};

	const bindRule = function(playerRule) {
		$players.rules("add", playerRule);
		$('[name=vipLevel]').rules("add", playerRule);
		$("[name=memberGroup]").rules("add", playerRule);
	};

	const toggleEnabled = function(enable) {
		if (!$allPlayers.prop('checked')) {
			$('[name=players]').select2("enable", enable);
			$('[name=affiliates]').select2("enable", enable);
			$.each($('[name=vipLevel]'), function() {
				$(this).prop('disabled', !enable);
			});
			$('[id^=memberGroup]').attr("disabled", !enable);
		}

		if (containExclude) {
			$excludePlayers.select2("enable", enable);
			$excludeAffiliates.select2("enable", enable);
			$.each($('[name=excludeVipLevel]'), function() {
				$(this).prop('disabled', !enable);
			});
			$('[id^=excludeMemberGroup]').attr("disabled", !enable);
			$excludeSwitch.bootstrapSwitch('setActive', enable);
		}
	};

	const checkAllPlayer = function() {
		if (!$allPlayers.prop('checked')) {
			$allPlayers.trigger("click");
		}
		$allPlayers.uniform();
	};

	const uncheckAllPlayer = function(needDisabled) {
		if ($allPlayers.prop('checked')) {
			if (needDisabled) {
				$allPlayers.prop('checked', false);
			} else {
				$allPlayers.trigger("click");
			}
		}
		$allPlayers.uniform();
	};

	const closeExclude = function() {
		$excludeSwitch.bootstrapSwitch('setState', false);
	};

	const openExclude = function() {
		$excludeSwitch.bootstrapSwitch('setState', true);
	};

	const enableAllPlayer = function() {
		$allPlayers.prop('disabled', false);
	};

	const disableAllPlayer = function() {
		$allPlayers.prop('disabled', true);
	};

	// EligibilityHandler.hideExcludeSwitch = function() {
	// 	$('#excludeSwitch').hide();
	// };
	//
	// EligibilityHandler.showExcludeSwitch = function() {
	// 	$('#excludeSwitch').show();
	// };

	const downloadTemplate = function(element) {
		const $form = $(element).closest("form");
		$form.data("validator").cancelSubmit = true;
		ExcelUtils.exportExcelBinary('/manager/MarketingController/downloadMassTemplate', 'BonusTemplate.xlsx');
	};

	const checkPromoCodeExist = function(element) {
		const $form = $(element).closest("form");
		$form.data("validator").cancelSubmit = true;
		const promoCode = $form.find('#promoCode').val();
		$.ajax({
			url: "/manager/MarketingController/checkPromoCodeExist",
			type: 'POST', //data type
			dataType: "json",
			data: {
				'promoCode': promoCode,
				'bonusId': PageConfig.queryID
			},
			success: function(data) {
				if (data) {
					$('#used').css('display', 'flex');
					$('#available').hide();
				} else {
					$('#available').css('display', 'flex');
					$('#used').hide();
				}
			},
			error: function(data) {
			}
		});
	};

	return {
		init: function() {
			containExclude = $('#exclude').val() === 'true';
			bindExcludeSwitch();
			$excludeSwitch.bootstrapSwitch('setState', false);
			initGroup();
			bindAllPlayers();
		},
		// AFFILIATE
		queryAllAffiliateUserId,
		setAffiliateData,
		// PLAYER
		queryAllUserId,
		setPlayerData,
		clearPlayer,
		// VIP
		appendVIP,
		setVIPData,
		// GROUP
		initGroup,
		insertGroupData,
		getGroupData,
		renderTable,
		setGroupData,
		selectedGroup,

		bindRule,
		toggleEnabled,
		checkAllPlayer,
		uncheckAllPlayer,

		closeExclude,
		openExclude,
		enableAllPlayer,
		disableAllPlayer,

		downloadTemplate,
		checkPromoCodeExist
	};

}();

if (typeof (DataBase) == 'undefined') {
	DataBase = {};
}

(function() {

	/** ***************  AccountGroup DataBase  ********************* */
	DataBase.group = {};
	var groups = new HashMap();
	DataBase.group.insert = function(entity) {
		var key = entity.currencyTypeId;
		var groupArray = groups.get(key);
		if (groupArray == null) {
			groupArray = [];
		}
		groupArray.push(entity);

		groups.put(key, groupArray);
	};
	DataBase.group.remove = function(key) {
		groups.remove(key);
	};
	DataBase.group.queryByID = function(key) {
		return groups.get(key);
	};
	DataBase.group.queryAll = function() {
		return groups.values();
	};
	DataBase.group.clean = function() {
		groups.clear();
	};

})();
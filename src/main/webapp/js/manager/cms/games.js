if (typeof (GamesHandler) == 'undefined') {
	GamesHandler = {};
}

(function() {
	GamesHandler.init = function() {
		bindEvent();
		initValidate();

		GamesHandler.search();
	};

	GamesHandler.deviceTypeSelector;
	GamesHandler.gameCategorySelector;

	function bindEvent() {
		JsCache.get('#gameType').val($("#gameType option:first").val());
		JsCache.get('#vendor').val($("#vendor option:first").val());
		JsCache.get('#gameStatus').val($("#gameStatus option:first").val());
		JsCache.get('#platformType').val($("#platformType option:first").val());
		JsCache.get('#sort').val($("#sort option:first").val());
		JsCache.get('#sorting').on('switch-change', function(event, state) {
			sortingTypeChange(state.value);
		});
		JsCache.get('#sorting').bootstrapSwitch('setState', true);
		sortingTypeChange(true);

		GamesHandler.deviceTypeSelector = Select2Handler.init(JsCache.get('#updateDeviceType'));

		JsCache.get('#updateDisplayOrder').unbind('keydown').keydown(function(k) {
			integerKeydownOnly(k);
		}).unbind('keyup').keyup(function(k) {

		});

		PageConfig.validator = JsCache.get('#updateGameForm').validate({
			ignore: [],
			debug: true,
			errorPlacement: function(error, element) {
				if (element.attr("name") === 'updateCategory') {
					error.insertAfter($('[name=updateCategory]').last().closest('label'));
				} else if (element.attr('type') === "file" && element.data('style') === "fileinput") {
					error.appendTo(element.closest("div.fileinput-holder").parent('div'));
				} else {
					error.insertAfter(element);
				}
			}
		});

		JsCache.get('#updateType').change(function() {
			$('#iconRequired').hide();
			if ($(this).val() == PageConfig.slotUnique) {
				$('#iconRequired').show();
			}
			$('#updateTypeHidden').val($('#updateType').val())
		});

		$('#vendor, #updateVendor').change(function() {
			PageConfig.validator.resetForm();
			catgoryVisible($(this).val());
		});

		categoryAllVisible();

		$('.input-width-xxlarge').attr('style', 'width: 400px !important');

		$('input[id^=customizedGameIcon]').on('change', function(element) {
			let $this = $(this);
			let widthLimit = $this.data('width');
			let heightLimit = $this.data('height');
			const file = element.target.files[0];
			let fileReader = new FileReader();
			let preview = $this.parent().parent().parent().siblings('img');

			fileReader.onload = function(e) {
				let fileContent = e.target.result;
				let image = new Image();
				image.src = fileContent;
				if (image.width !== widthLimit || image.height !== heightLimit) {
					$this.val('');
					alert(`Upload file format is not matched (${widthLimit}x${heightLimit})`)
					return;
				}

				preview.attr('src', e.target.result);
			}

			$this.parent().siblings('.remove').on('click', function() {
				preview.attr('src', '');
			});
			fileReader.readAsDataURL(file);
		});
	}

	function initValidate() {
		var $updateGameForm = $('#updateGameForm');
		$updateGameForm.find('#updateDisplayOrder').rules("add", {required: true});
	}

	GamesHandler.imgError = function(e) {
		var image = $(e).attr({
			"data-src": "holder.js/300x200"
		});
		Holder.run({
			images: image[0]
		});
	}

	var gameList = [];
	for (var i = 1; i <= 5; i++) {
		var item = {
			"mData": "record" + i,
			"className": "col-sm-6 col-md-4",
			"mRender": function(data, type, full) {
				if (type === 'display') {
					if (data) {
						var content = gameContent(data);
						return content;
					}
					// else {
					// 	return PageConfig.emptyGame
					// }
				}
				return data;
			}
		};
		gameList.push(item);
	}

	var dataTableOptions;
	var initDataTableOptions = function() {
		dataTableOptions = {
			tableSelector: '#gamesTable',
			formSelector: "[name='searchForm']",//optional
			hideColVis: true,
			sAjaxSource: '/manager/ContentManageController/searchGames',
			iDisplayLength: PageConfig.pageSize,
			aoColumns: gameList,
			dropdownOptions: [
				{value: 'recommended', text: 'Recommended'},
				{value: 'latest', text: 'Latest'},
				{value: 'az', text: 'A-Z'},
			]
		};
	};

	function isActiveStatus(status) {
		try {
			if (window.GameStatusType && typeof window.GameStatusType.ACTIVE?.unique === 'function') {
				return String(status) === String(window.GameStatusType.ACTIVE.unique());
			}
		} catch (e) {
		}
		if (typeof status === 'number') return status === 1;
		if (typeof status === 'boolean') return status === true;
		if (typeof status === 'string') {
			var s = status.toLowerCase();
			return s === 'active' || s === '1' || s === 'true';
		}
		return false;
	}

	// 眼睛 SVG（使用 currentColor，由父層 color 控制顏色）
	function svgEye() {
		return '' +
			'<svg width="20" height="20" viewBox="0 0 24 24" aria-hidden="true">' +
			'  <path d="M12 5C5 5 1 12 1 12s4 7 11 7 11-7 11-7-4-7-11-7Z" fill="none" stroke="currentColor" stroke-width="2"/>' +
			'  <circle cx="12" cy="12" r="3" fill="currentColor"/>' +
			'</svg>';
	}

	function svgEyeSlash() {
		return '' +
			'<svg width="20" height="20" viewBox="0 0 24 24" aria-hidden="true">' +
			'  <path d="M12 5C5 5 1 12 1 12s4 7 11 7 11-7 11-7-4-7-11-7Z" fill="none" stroke="currentColor" stroke-width="2"/>' +
			'  <circle cx="12" cy="12" r="3" fill="currentColor"/>' +
			'  <line x1="3" y1="3" x2="21" y2="21" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>' +
			'</svg>';
	}

	function statusIconHTML(status, gameId) {
		var active = isActiveStatus(status);

		var color = active ? '#28a745' : '#dc3545';
		return '' +
			'<button type="button" class="status-icon" ' +
			'        onclick="GamesHandler.changeStatus(' + gameId + ',' + (!active) + ', this)" ' +
			'        aria-label="Toggle status" ' +
			'        style="position:absolute;top:8px;right:8px;z-index:2;line-height:1;' +
			'color:' + color + ';background:white;border:1px;cursor:pointer;">' +
			(active ? svgEye() : svgEyeSlash()) +
			'</button>';
	}

	function updateStatusIcon($switch, isOn) {
		var $thumb = $switch.closest('.thumbnail');
		$thumb.find('.status-icon')
			.html(isOn ? svgEye() : svgEyeSlash())
			.css('color', isOn ? '#28a745' : '#dc3545');
	}

	function gameContent(data) {

		if (typeof data === 'string') {
			try {
				data = JSON.parse(data);
			} catch (e) {
				console.error('Invalid data', e);
				data = {};
			}
		}

		let vendorName = data['vendorName'];
		let gameEnName = data['gameEnName'];
		let gameCode = data['gameCode'];
		let gameType = data['gameType'];
		let display = data['display'];
		let gameId = data['id'];
		let deviceType = data['deviceType'];
		let vendorCode = data['vendorCode'];
		let gameTypeId = data['gameTypeUnique'];

		var imageHTML = (!data.path || String(data.path) === 'path')
			? '<img id="emptyData" style="width:300px;height:200px;" alt="No image" onclick="GamesHandler.openGameProfile(' + gameId + ')' + '" >'
			: '<img src="' + data.path + '" style="width:300px;height:200px;" loading="lazy" ' +
			'alt="' + (gameEnName || vendorName) + '" ' +
			'onerror="GamesHandler.imgError && GamesHandler.imgError(this)" onclick="GamesHandler.openGameProfile(' + gameId + ')' + '" >';

		var gameUrlBtnHTML =
			'<button onclick="javascript:GamesHandler.getGameUrl && GamesHandler.getGameUrl(this)" ' +
			'        data-vendorcode="' + vendorCode + '" data-gamecode="' + gameCode + '" ' +
			'        data-gametype="' + gameTypeId + '" data-devicetype="' + deviceType + '" ' +
			'        type="button" class="btn btn-primary" style="width: 100%; margin: 5px 0px; padding: 10px;">Game URL</button>';

		return '' +
			'<td class="sorting_1">' +
			'  <div class="thumbnail" style="position:relative;">' +
			statusIconHTML(data.status, gameId) +
			imageHTML +
			'    <div class="caption">' +
			'      <h3>' + vendorName + '</h3>' +
			'      <p>' + gameEnName + '</p>' +
			'      <p>' + gameCode + '</p>' +
			'      <p>' + gameType + '</p>' +
			'      <p>' + display + '</p>' +
			'      <p id="gameId" style="display:none;">' + gameId + '</p>' +
			gameUrlBtnHTML +
			'    </div>' +
			'  </div>' +
			'</td>';
	}

	GamesHandler.bindStatusSwitch = function(root) {
		var $root = root ? $(root) : $(document);
		// 只負責同步 icon，後端切換/確認仍走你原本的流程
		$root.find('[name=status]').off('switch-change.eye').on('switch-change.eye', function(e, state) {
			updateStatusIcon($(this), state.value);
		});
	}

	GamesHandler.changeStatus = function(gameId) {

		if (PageConfig.showDialog) return;

		$('[name=edit]').addClass('disabled');

		var game = DataBase.game.queryByID(gameId);
		var originalStatus = game.status;
		var nextValue = !isActiveStatus(originalStatus);

		var statusMsg = I18N.get('form.text.backOffice.notification.statusPromptMessage', [
			game.gameEnName,
			(nextValue ? I18N.get('form.text.backOffice.status.active') : I18N.get('form.text.backOffice.status.inactive'))
		]);

		NotifyHandler.confirmMsg(statusMsg, false, function(ok) {
			if (ok) {
				$.ajax({
					type: "POST",
					url: '/manager/ContentManageController/updateGameStatus',
					data: {
						gameId: gameId,
						status: nextValue ? GameStatusType.ACTIVE.unique() : GameStatusType.INACTIVE.unique()
					},
					success: function(resp) {
						if (resp.error) {
							NotifyHandler.errorMsg(resp.error);
							return;
						}
						NotifyHandler.successMsg(I18N.get('msg.manager.update.success'));
						// 同步 Switch 與 Icon（會觸發 switch-change → 也會更新 icon）
						$('#game_' + gameId).bootstrapSwitch('setState', nextValue, true);
						// 若你的 switch-change 只做 icon，同步保險再手動一次：
						updateStatusIcon($('#game_' + gameId), nextValue);
						GamesHandler.search(); // 保險起見重新查一次
					},
					error: function(err) {
						alert(err);
					},
					complete: function() {
						$('[name=edit]').removeClass('disabled');
						PageConfig.showDialog = false;
					}
				});
			} else {
				// 取消：還原 switch/icon
				var backToOn = (game.status == GameStatusType.ACTIVE.unique());
				$('#game_' + gameId).bootstrapSwitch('setState', backToOn, true);
				updateStatusIcon($('#game_' + gameId), backToOn);
				$('[name=edit]').removeClass('disabled');
				PageConfig.showDialog = false;
			}
		});

		PageConfig.showDialog = true;
	}

	GamesHandler.search = function() {

		var callBack = function(response) {

			var data = response.aaData;

			$('.gamesTable').show();
			DataBase.game.clean();

			$.each(data, function(i, el) {
				var arr = Object.values(el);
				$.each(arr, function(j, record) {
					if (record) {
						var recordJson = JSON.parse(record);
						DataBase.game.insert(recordJson);
						$('#game_' + recordJson.id).bootstrapSwitch('setState', recordJson.status == GameStatusType.ACTIVE.unique() ? true : false, true);
					}
				});
			});

			$('[id^=emptyData]').each(function() {
				var image = $(this).attr({
					"data-src": "holder.js/300x200"
				});
				Holder.run({
					images: image[0]
				});
			});
		};

		if (!dataTableOptions) {
			initDataTableOptions();
		}
//		var syncButton = PageConfig.enableUpdateStatus ? $('.syncButton').html().trim() : null;


		dataTableOptions.dataTableRef = DataTableHandler.create(dataTableOptions, false, PageConfig.lang, callBack, true);
	}

	function sortingTypeChange(status) {
		if (status == true) {
			$('#sortingType').val(DBOrderType.DESC.unique());
		} else {
			$('#sortingType').val(DBOrderType.ASC.unique());
		}
	}

	function catgoryVisible(vendorId) {
		if (vendorId != -1) { // not all
			// show option
			JsCache.get('#updateGameCategory').empty();

			var vendor = DataBase.vendor.queryByID(vendorId);

			if (vendor.categories) {
				var categories = vendor.categories.split(",");
				var groupId = 0;
				$.each(categories, function(i, el) {
					var category = DataBase.category.queryByID(el);
					if (category == null) {
						return;
					}
					if (groupId != category.groupId) {
						groupId = category.groupId;
						var groupName = DataBase.group.queryByID(groupId);
						var $group = JsCache.get('#categoryGroupTemplate').clone().show();
						$group.attr('id', 'group_' + groupId).attr('label', groupName);
						JsCache.get('#updateGameCategory').append($group);
					}
					var $option = JsCache.get('#elementTemplate').clone().show();
					$option.attr('id', '').val(category.id).text(category.name);
					$('#group_' + groupId).append($option);
				});
			}

			setTimeout(function() {
				GamesHandler.gameCategorySelector = Select2Handler.init(JsCache.get('#updateGameCategory'));
			}, 0);

		}
	}

	function categoryAllVisible() {
		$('[id^=gameCategory]').show();
	}

	var integerKeydownOnly = function(k) {
		var keyCode = k.which;
		if (KeyEventUtils.isNumberKey(k) || KeyEventUtils.isBackspaceKey(keyCode) || KeyEventUtils.isDeleteKey(keyCode) || KeyEventUtils.isArrowKey(keyCode) || KeyEventUtils.isTabKey(keyCode)) {
			return;
		}
		WindowEventUtil.stopEvent(k, false, true);
	}

	GamesHandler.getGameUrl = function(target) {
		let vendorCode = $(target).data('vendorcode');
		let gameType = $(target).data('gametype');
		let gameCode = $(target).data('gamecode') ? $(target).data('gamecode') : '';
		let deviceType = $(target).data('devicetype');
		let showWeb = deviceType.includes("WEB");
		let showH5 = deviceType.includes("HTML5");

		let webUrl = '/player/playGame?t=' + GameType.getInstanceOf(gameType).getShortName() + '&v=' + vendorCode + '&g=' + gameCode;
		let h5GameCode = gameCode ? gameCode : 'null';
		let h5Url = '/open-game-link/' + gameType + '/' + vendorCode + '/' + h5GameCode;

		let $gameUrlModal = $('#gameUrlModal');

		if (showWeb) {
			$gameUrlModal.find('#webGameUrl').val(webUrl);
			$gameUrlModal.find('#webGameUrlDiv').show();
		} else {
			$gameUrlModal.find('#webGameUrlDiv').hide();
		}

		if (showH5) {
			$gameUrlModal.find('#h5GameUrl').val(h5Url);
			$gameUrlModal.find('#h5GameUrlDiv').show();
		} else {
			$gameUrlModal.find('#h5GameUrlDiv').hide();
		}

		$gameUrlModal.modal('show');
	}

	GamesHandler.copy = function(target) {

		let textareaId = $(target).closest('[id$="GameUrlDiv"]').attr('id').replace('Div', '');
		let textarea = document.getElementById(textareaId);

		if (!textarea) {
			console.error('Textarea not found');
			return;
		}

		textarea.select();
		document.execCommand("copy");
		document.getSelection().removeAllRanges();
		alert('Copy Success');
	}

	GamesHandler.openGameProfile = function(gameId) {
		var panel = window.open("/page/manager/cms/gamesProfile.jsp?gameid=" + gameId
			, "_winPROFILE", "width=1200,height=640,top=5,left=5," +
			"toolbar=no,menubar=no,resizable=no,scrollbars=yes,status=no,location=no,directories=no");

		if (panel) {
			panel.focus();
		}
	};

})();

if (typeof (DataBase) == 'undefined') {
	DataBase = {};
}

(function() {

	/** ***************  Game DataBase  ********************* */
	DataBase.game = {};
	var games = new HashMap();
	DataBase.game.insert = function(entity) {
		games.put(entity.id, entity);
	};
	DataBase.game.remove = function(key) {
		games.remove(key);
	};
	DataBase.game.queryByID = function(key) {
		return games.get(key);
	};
	DataBase.game.queryAll = function() {
		return games.values();
	};
	DataBase.game.clean = function() {
		games.clear();
	};

	/** ***************  Vendor DataBase  ********************* */
	DataBase.vendor = {};
	var vendors = new HashMap();
	DataBase.vendor.insert = function(entity) {
		vendors.put(entity.id, entity);
	};
	DataBase.vendor.remove = function(key) {
		vendors.remove(key);
	};
	DataBase.vendor.queryByID = function(key) {
		return vendors.get(key);
	};
	DataBase.vendor.queryAll = function() {
		return vendors.values();
	};
	DataBase.vendor.clean = function() {
		vendors.clear();
	};

	/** ***************  Group DataBase  ********************* */
	DataBase.group = {};
	var groups = new HashMap();
	DataBase.group.insert = function(id, entity) {
		groups.put(id, entity);
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

	/** ***************  Category DataBase  ********************* */
	DataBase.category = {};
	var categories = new HashMap();
	DataBase.category.insert = function(entity) {
		categories.put(entity.id, entity);
	};
	DataBase.category.remove = function(key) {
		categories.remove(key);
	};
	DataBase.category.queryByID = function(key) {
		return categories.get(key);
	};
	DataBase.category.queryAll = function() {
		return categories.values();
	};
	DataBase.category.clean = function() {
		categories.clear();
	};

})();
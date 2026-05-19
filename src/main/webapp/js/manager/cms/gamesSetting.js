if (typeof (GamesSettingHandler) == 'undefined') {
	GamesSettingHandler = {};
}

(function() {

	GamesSettingHandler.cacheData = {};

	GamesSettingHandler.init = function() {
		GamesSettingHandler.searchGames();

		document.getElementById('displayOrder').addEventListener('input', function() {
			// 移除非數字
			this.value = this.value.replace(/\D/g, '');

			// 若不為空才轉數字檢查
			if (this.value !== '') {
				let num = parseInt(this.value, 10);

				// 大於等於 100 -> 自動設為 99
				if (num >= 100) {
					num = 99;
				}
				if (num < 0) {
					num = 0;
				}

				this.value = num;
			}else {
					this.value  =0;
			}
		});
	}

	GamesSettingHandler.searchGames = function() {
		const urlParams = new URLSearchParams(window.location.search);
		const gameId = urlParams.get('gameid');

		$.ajax({
			type: "GET",
			url: '/manager/ContentManageController/searchGameById',
			dataType: 'JSON',
			data: {
				gameId: gameId
			},
			success: function(data) {
				if (data.error) {
					NotifyHandler.errorMsg(data.error);
					return;
				}

				GamesSettingHandler.cacheData = data;

				document.getElementById('displayOrder').value = `${data['displayOrder']}`;
			}
		});
	}

	GamesSettingHandler.updateGameDisplayOrder = function() {

		const displayOrder = document.getElementById('displayOrder').value;

		if (!/^\d+$/.test(displayOrder) || Number(displayOrder) < 0 || Number(displayOrder) > 100) {
			NotifyHandler.errorMsg("Please enter a Display Order between 0 and 100.");
			return;
		}

		const updateGameDisplayOrderForm = $("[name='updateGameDisplayOrderForm']");

		updateGameDisplayOrderForm.find('[name=save]').addClass('disabled');
		updateGameDisplayOrderForm.find('[name=resetButton]').addClass('disabled');

		if (GamesSettingHandler.cacheData) {
			const gameId = GamesSettingHandler.cacheData['id'];

			$.ajax({
				type: "POST",
				url: '/manager/ContentManageController/updateGameDisplayOrder',
				dataType: 'JSON',
				data: {
					gameId: gameId,
					displayOrder: displayOrder
				},
				success: function(data) {
					if (data.error) {
						NotifyHandler.errorMsg(data.error);
						return;
					}
					NotifyHandler.successMsg(data.message);
					$('#updateStatusModal').modal('hide');

					GamesSettingHandler.searchGames();
				},
				complete: function() {
					updateGameDisplayOrderForm.find('[name=save]').removeClass('disabled');
					updateGameDisplayOrderForm.find('[name=resetButton]').removeClass('disabled');
				}
			});

		}
	}

	GamesSettingHandler.resetGameDisplayOrder = function() {
		document.getElementById('displayOrder').value = `${GamesSettingHandler.cacheData['displayOrder']}`;
	};

})();
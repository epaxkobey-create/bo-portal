if (typeof (GamesProfileHandler) == 'undefined') {
	GamesProfileHandler = {};
}

(function() {

	GamesProfileHandler.cacheData = {};

	GamesProfileHandler.init = function() {
		GamesProfileHandler.searchGames();
	}

	GamesProfileHandler.searchGames = function() {
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

				GamesProfileHandler.cacheData = data;

				document.getElementById('gameName').innerHTML = `${data['nameEn']}`;

				const gameType = GameType.getInstanceOf(data['gameType']);

				document.getElementById('gameType').innerHTML = gameType ? `${gameType.name}` : ``;

				document.getElementById('gameCode').innerHTML = `${data['code']}`;

				const gameStatusType = PageConfig.GameStatusType[data['status']];

				document.getElementById('status').innerHTML = `
					<span class="label ${gameStatusType === `Active` ? `label-success` : `label-default`}" id='status'>${gameStatusType === `Active` ? `Active` : `Inactive`}</span>
					
				`;

			}
		});
	}

	GamesProfileHandler.viewGamePhoto = function(e) {

		if (GamesProfileHandler.cacheData) {
			const iconPath = GamesProfileHandler.cacheData['iconPath'];

			const $viewGamePhoto = $('#viewGamePhoto');
			const $photo = $viewGamePhoto.find('[name=photo]');

			$photo.empty();
			const image = new Image();
			image.src = `/upload/game/${iconPath}`;
			image.style.minWidth = '300px';
			image.style.minHeight = '200px';
			$photo.append(image);

			$viewGamePhoto.modal('show');
		}

	}

	GamesProfileHandler.resetStatus = function() {
		let updateStatusForm = $("[name='updateStatusForm']");
		updateStatusForm.get(0).reset();
		loadStatus();
	};

	GamesProfileHandler.getEditStatus = function() {
		let updateStatusForm = $("[name='updateStatusForm']");

		updateStatusForm.find('[name="gameId"]').val(`${GamesProfileHandler.cacheData.id}`);

		updateStatusForm.validate({
			onfocusout: false
		});
		updateStatusForm.get(0).reset();
		loadStatus();
		$('#updateStatusModal').modal('show');
	}

	GamesProfileHandler.updateStatus = function() {
		let updateStatusForm = $("[name='updateStatusForm']");

		updateStatusForm.find('[name=save]').addClass('disabled');
		updateStatusForm.find('[name=resetButton]').addClass('disabled');

		$.ajax({
			type: "POST",
			url: '/manager/ContentManageController/updateGameStatus',
			dataType: 'JSON',
			data: updateStatusForm.serialize(),
			success: function(data) {
				if (data.error) {
					NotifyHandler.errorMsg(data.error);
					return;
				}
				NotifyHandler.successMsg(data.message);
				$('#updateStatusModal').modal('hide');

				GamesProfileHandler.searchGames();
			},
			complete: function() {
				updateStatusForm.find('[name=save]').removeClass('disabled');
				updateStatusForm.find('[name=resetButton]').removeClass('disabled');
			}
		});
	};

	let loadStatus = function(status) {
		let updateStatusModal = $('#updateStatusModal');
		let element = updateStatusModal.children().detach();

		const gameStatusType = GamesProfileHandler.cacheData['status'];

		element.find("[name='status']").val(gameStatusType);
		$.uniform.update();//Chrome、IE要這樣
		updateStatusModal.append(element);
	}

})();
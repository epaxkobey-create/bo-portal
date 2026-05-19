if (typeof (ImageUtils) == 'undefined') {
	ImageUtils = {};
}

(function() {

	ImageUtils.bindPreview = function(sizeLimit) {
		$("[id$=File], [id$=attachment]").each(function() {

			const $fileInput = $(this);

			if ($fileInput.data('type') !== 'image') {
				return;
			}

			const targetId = $fileInput.attr('id');
			$fileInput.closest('.fileinput-holder').after(`<br>
						<img id="${targetId}Preview" src="" style="max-height:300px; max-width:300px; overflow: hidden; display:none;">
						<p id="${targetId}OriginalSize" style="margin-top: 3px; color: #888888; font-size: 14px; display:none;"></p>`);

			$fileInput.change(function(e) {
				const realFile = $fileInput[0];

				if (!realFile.files || !realFile.files[0]) {
					return;
				}
				if (realFile.files[0].size > sizeLimit) {
					NotifyHandler.errorMsg(I18N.get("msg.error.info.image.sizeIsLarge"));
					return;
				}

				const reader = new FileReader();

				reader.onload = function(e) {
					loadPicture(targetId, e.target.result);
				}

				reader.readAsDataURL(realFile.files[0]);
			});
		});

		$(".remove").click(function(e) {
			const fileId = $(e.target).closest('div').find('input').attr('id');
			$('#' + fileId).val('');
			$('#' + fileId + 'Preview').attr('src', '').hide();
			$('#' + fileId + 'OriginalSize').empty().hide();
		});
	};

	ImageUtils.loadPreview = function(targetId, fileName, source) {
		$('#' + targetId).closest('.fileinput-holder').find('.fileinput-preview').text(fileName);
		loadPicture(targetId, source);
	};

	ImageUtils.clearPreview = function() {
		$(".remove").each(function() {
			$(this).click();
		});
	};

	const loadPicture = function(targetId, source) {
		const $image = $('#' + targetId + 'Preview');
		$image.attr("src", source).load(function() {
			$('#' + targetId + 'OriginalSize').text(I18N.get('form.text.originalSize', [this.naturalWidth, this.naturalHeight])).show();
		});
		$image.show();
	};

})();



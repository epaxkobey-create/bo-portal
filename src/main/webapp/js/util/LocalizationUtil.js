/**
 *
 */
//
if (typeof (LocalizationUtil) == 'undefined') {
	LocalizationUtil = {};
}
(function() {

	let toggleDiv =
		'<div id="localizeToggle"' +
		' style="position: fixed; z-index: 9999; margin: 10px; color: white; font-weight: bold; cursor: pointer">Localize</div>';

	let localizeBoard =
		'<div id="localizeBoard" style="position: fixed; z-index: 9999; margin: 10px 25px; display: none; ' +
		'bottom: 0; right: 0; background-color: skyblue; width: 800px; min-height: 300px;">localization Board</div>';

	/*
	 * public method
	 */
	LocalizationUtil.init = function() {

		$('body')
			.prepend(toggleDiv)
			.append(localizeBoard);

		$('#localizeToggle').on('click', function() {

			$('#localizeBoard').show();

			$('div,span,h1,h2,h3,h4,h5,th,td,a,button').filter(function() {
					return $(this).children().size() === 0
						&& $(this).text().trim().toLowerCase().indexOf('form.text.') !== -1;
				})
				.sort(function(a, b) {
					return $(a).text().localeCompare($(b).text());
				})
				.each(function(i, e) {
					let elem = $(e)
					elem.css('background-color', '#FFFF00')

					elem.text().trim().split(' ').forEach(function(i18nStr, idx) {

						$('#localizeBoard').append('<div>' +
							'<label style="width: 350px; display: inline-block;">' + i18nStr.trim() + '</label> = ' +
							'<input style="width: 350px;"/>' +
							'</div>')
					});
				});
		});
	};


}());

$(function() {
	LocalizationUtil.init();
});
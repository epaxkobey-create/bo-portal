CKEDITOR.editorConfig = function(config) {
	config.language = 'en';
	config.height = 300;
	config.toolbarCanCollapse = false;

	config.toolbarGroups = [
		{name: 'clipboard', groups: ['clipboard', 'undo']},
		{name: 'editing', groups: ['find', 'selection', 'spellchecker']},
		{name: 'links'},
		{name: 'insert'},
		{name: 'forms'},
		{name: 'tools'},
		{name: 'document', groups: ['mode', 'document', 'doctools']},
		{name: 'others'},
		'/',
		{name: 'basicstyles', groups: ['basicstyles', 'cleanup']},
		{name: 'paragraph', groups: ['list', 'indent', 'blocks', 'align', 'bidi']},
		{name: 'styles', groups: ['styles']},
		{name: 'colors'},
		{name: 'about'}
	];

	config.removeButtons = 'Underline,Subscript,Superscript,Image,Flash,Iframe';
	config.allowedContent = true;
	// Set the most common block elements.
	config.format_tags = 'p;h1;h2;h3;pre';

	// Simplify the dialog windows.
	config.removeDialogTabs = 'image:advanced;link:advanced';
	config.pasteFilter = null;

	config.extraPlugins = 'font,colorbutton';
};
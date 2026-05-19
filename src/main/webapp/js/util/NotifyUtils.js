if (typeof (NotifyHandler) == 'undefined') {
	NotifyHandler = {};
//	  layout:顯示的位置(預設是top),
//    theme:'defaultTheme',
//    type:'alert',
//    text:'',
//    dismissQueue:true,
//    template:'<div class="noty_message"><span class="noty_text"></span><div class="noty_close"></div></div>',
//    animation:{
//        open:{height:'toggle'},
//        close:{height:'toggle'},
//        easing:'swing',
//        speed:500
//    },
//    timeout:false, 訊息持續多久(單位:毫秒)
//    force:false,
//    modal:false, 背景是否反灰
//    maxVisible:5,一次最多顯示幾則訊息
//    closeWith:['click'],關閉方式
//    callback:{
//        onShow:function () {
//        },
//        afterShow:function () {
//        },
//        onClose:function () {
//        },
//        afterClose:function () {
//        },
//        onCloseClick:function () {
//        }
//    },
//    buttons:false 新增按鈕
}

NotifyHandler = function() {
	// msg : message
	// msgType : alert, success, warning, error, info/information
	// sticky : false or milliseconds
	// onConfirm : callback function
	var create = function(msg, msgType, sticky, callback, layout) {
		var options = {
			type: msgType,
			text: msg,
			template: '<div class="noty_message"><span style="font-size: 12pt" class="noty_text"></span><div class="noty_close"></div></div>',
			timeout: 3000,
			buttons: false
		};
		if (typeof sticky != "undefined") {
			$.extend(options, {
				timeout: sticky
			});
		}
		if (typeof layout != "undefined") {
			$.extend(options, {
				layout: layout
			});
		}
		if (callback) {
			$.extend(options, {
				buttons: [{
					addClass: 'btn btn-primary',
					text: I18N.get('ui.text.confirm'),
					onClick: function($noty) {
						$noty.close();
						callback(true);
					}
				}, {
					addClass: 'btn btn-danger',
					text: I18N.get('ui.text.cancel'),
					onClick: function($noty) {
						$noty.close();
						callback(false);
					}
				}]
			});
		}
		noty(options);
	}

	return {
		create: function(msg, msgType, sticky, callback, layout) {
			create(msg, msgType, sticky, callback, layout);
		},
		alertMsg: function(msg, sticky, layout) {
			create(msg, 'alert', sticky, null, layout);
		},
		successMsg: function(msg, sticky, layout) {
			create(msg, 'success', sticky, null, layout);
		},
		errorMsg: function(msg, sticky, layout) {
			create(msg, 'error', sticky, null, layout);
		},
		warningMsg: function(msg, sticky, layout) {
			create(msg, 'warning', sticky, null, layout);
		},
		infoMsg: function(msg, sticky, layout) {
			create(msg, 'info', sticky, null, layout);
		},
		confirmMsg: function(msg, sticky, onConfirm, layout) {
			create(msg, 'info', sticky, onConfirm, layout);
		}
	}
}();
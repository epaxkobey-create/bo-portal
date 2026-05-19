/**
 * TemplateUtil.autoBindListData(dataList, templateSelector, containerSelector)
 *
 * 這個 Util Method 是用來將 js data object List, 自動填入到 template HTML中, 然後自動 append 到 container HTML上.
 *
 * 註: dataList 會是多筆 js object, 例如 [{id:1,value:'foo'},{id:2,value:'bar'}]
 *
 * 註: templateSelector 跟 containerSelector 都是 jquery selecotr, 例如 '#xx_id', '.xx_class', 最好都是 id selector
 *
 * demo: https://jsfiddle.net/7mh3Ljw4/165/
 */
//// 以下是範例程式
//
//	var dataList = [ {
//		name : 'aapple',
//		value : 10,
//		details : [ {
//			detailName : 'detail1',
//			innerDetails : [ {
//				innername : 'inner1'
//			} ]
//		} ]
//	}, {
//		name : 'banana',
//		value : 20,
//		details : [ {
//			detailName : 'detail2',
//			innerDetails : [ {
//				innername : 'inner2'
//			} ]
//		} ]
//	}, {
//		name : 'kiwi',
//		value : 30,
//		details : [ {
//			detailName : 'detail3',
//			innerDetails : [ {
//				innername : 'inner3'
//			} ]
//		} ]
//	}, {
//		name : 'mango',
//		value : 40,
//		details : [ {
//			detailName : 'detail4',
//			innerDetails : [ {
//				innername : 'inner4'
//			} ]
//		} ]
//	}, ];
//
//	TemplateUtil.autoBindListData(dataList, '#template', '#fruit-list');
//
// // -----------------------------
// // 以下是 HTML的結果
//
//	aapple $10
//		detail1
//			inner1
//	banana $20
//		detail2
//			inner2
//	kiwi $30
//		detail3
//			inner3
//	mango $40
//		detail4
//			inner4
//
// // -----------------------------
//	<div>
//		<ul id="fruit-list">
//		</ul>
//	</div>
//
// <!-- 以下是 html template -->
//	<div style="display: none">
//		<ul>
//			<li id="template">
//				<div>
//					<span ui-key="name">-</span> $<span ui-key="value">0</span>
//					<div>
//						<ul ui-key="details">
//							<li>
//								<span ui-key="detailName">-</span>
//								<div>
//									<ul ui-key="innerDetails">
//										<li data-innername>
//											<span ui-key="innername" data-innername>-</span>
//										</li>
//									</ul>
//								</div>
//							</li>
//						</ul>
//					</div>
//				</div>
//			</li>
//		</ul>
//	</div>
//
//
if (typeof (TemplateUtil) == 'undefined') {
	TemplateUtil = {};
}
(function() {

	/*
	 * public method
	 */
	TemplateUtil.autoBindListData = function(list, templateSelector, containerSelector, options) {

		if (list == null || !(list instanceof Array) || list.length === 0) {
			return;
		}
		if (!JsCache.get(templateSelector) || !$(containerSelector)) {
			return;
		}

		if (options && options.append) {

		} else {
			$(containerSelector).empty();
		}

		let templateElement = $(templateSelector).clone();

		let templateElementMap = {};
		templateElementMap[templateSelector] = templateElement;

		_parseTemplateMap(list, templateElement, templateElementMap);

		let containerElement = $(containerSelector);

		_bindListData(list, templateSelector, containerElement, templateElementMap);
	};

	/*
	 * 
	 */
	const _parseTemplateMap = function(list, templateElement, templateElementMap) {

		let item = list[0];

		for (let property in item) {

			if (item.hasOwnProperty(property)) {

				let dataValue = item[property];

				if (dataValue instanceof Array) {

					let firstChild = templateElement.find('[ui-key="' + property + '"]').children()
						.get(0);

					let containerElement = $(firstChild).clone();

					templateElementMap['#' + property] = containerElement;

					_parseTemplateMap(dataValue, containerElement, templateElementMap);

					// NOTE: need to clear children after parse done
					templateElement.find('[ui-key="' + property + '"]').empty();
				}
			}
		}
		//		// for debug
		//		for (var property in templateElementMap) {
		//			if (templateElementMap.hasOwnProperty(property)) {
		//				var value = templateElementMap[property];
		//				console.info(property);
		//				console.info(value.get(0));
		//			}
		//		}
	};

	/*
	 * private method, handle list data
	 */
	const _bindListData = function(list, templateSelector, containerElement, templateElementMap) {

		if (list == null || !(list instanceof Array) || list.length === 0
			|| templateElementMap[templateSelector] == null || containerElement == null) {
			return;
		}

		let topTemplateElemen = templateElementMap[templateSelector];
		// MEMO: gather "ui-key" attribute
		let uiKeyArr = $.map(topTemplateElemen.find('[ui-key]'), function(el) {
			return Array($(el).attr('ui-key'));
		});
		if (topTemplateElemen.attr('ui-key')) {
			uiKeyArr.push(topTemplateElemen.attr('ui-key'));
		}
		// console.info(uiKeyArr);

		// MEMO: for all "data-" attributes
		let dataAttrArr = [];

		let allTargetElement = topTemplateElemen.add(topTemplateElemen.find('*'));
		// MEMO: gather "data-" attribute
		allTargetElement.each(function() {

			let attributes = $(this)[0].attributes;

			for (let i = 0; i < attributes.length; i++) {
				// only prefix 'data-'
				if (attributes[i].name.indexOf('data-') === 0
					// put if not exist
					&& dataAttrArr.indexOf(attributes[i].name) === -1) {

					dataAttrArr.push(attributes[i].name);
				}
			}
		});
		// console.info(dataAttrArr);

		/**
		 * loop js data list
		 */
		for (let j = 0; j < list.length; j++) {

			let item = list[j];

			let templateElement = templateElementMap[templateSelector].clone();

			if (item['id'] != null) {
				templateElement.attr('id', item['id']);
			} else {
				templateElement.removeAttr('id');
			}
			// SEO title
			if (item['title'] != null) {
				templateElement.attr('title', item['title']);
			} else {
				templateElement.removeAttr('title');
			}

			/**
			 * loop item 所有的 properties
			 */
			for (let property in item) {

				if (item.hasOwnProperty(property)) {

					let dataValue = item[property];

					// 如果 uiKeyArr 有符合 property 的 uiKey, 就把 dataValue 填入對應的 HTML位置
					if (uiKeyArr.indexOf(property) !== -1) {

						let uiKey = property + '';

						_bindUiKeyData(uiKey, dataValue, templateElement, templateElementMap);
					}
					// 如果 dataAttrArr 有符合 property 的 dataAttr, 就把 dataValue 填入對應的 "data-" attribute
					if (dataAttrArr.indexOf('data-' + property.toLowerCase()) !== -1) {

						let dataAttr = 'data-' + property.toLowerCase();

						_bindDataAttrData(dataAttr, dataValue, templateElement);
					}
					/**
					 * 如果需要擴充, 只要新增 _bindXXXData(xxxKey, dataValue, templateElement);
					 */
				}
			}

			containerElement.append(templateElement);
		}
	};

	/**
	 * handle single item
	 */
	const _bindUiKeyData = function(uiKey, dataValue, templateElement, templateElementMap) {

		// console.info(uiKey + ': ' + dataValue);

		// 如果 js data item 的 value 是 Array 就遞迴呼叫 _bindListData()
		if (dataValue instanceof Array) {

			let list = dataValue;

			let containerElement = templateElement.find('[ui-key="' + uiKey + '"]');
			// 遞迴
			_bindListData(list, '#' + uiKey, containerElement, templateElementMap);

		} else {
			let targetTag;

			if (templateElement.attr('ui-key') === uiKey) {
				// 如果 ui-key 是在 template HTML 的最上層 tag
				targetTag = templateElement;
			} else {
				// 找出 ui-key 所在的 HTML tag
				targetTag = templateElement.find('[ui-key="' + uiKey + '"]');
			}

			if (targetTag.prop('tagName').toUpperCase() === 'IMG') {
				// 如果 ui-key 所在的 tag 是 img, 就把 value 填入 img src
				targetTag.attr('src', dataValue);

			} else {
				// 把 value 填入 HTML
				targetTag.html(dataValue);
			}
		}
	};

	/**
	 * handle single item
	 */
	const _bindDataAttrData = function(dataAttr, dataValue, templateElement) {

		// console.info(dataAttr + ': ' + dataValue);

		// force toString. special handling?
		dataValue = dataValue + '';

		// handle "data-id"
		if (dataAttr === 'data-id' && dataValue.indexOf('-') !== -1) {
			// TODO: temp fix
			if (dataValue.indexOf('bonus') === 0
				|| dataValue.indexOf('promotion') === 0) {
				// pass
			} else {
				dataValue = dataValue.split('-')[1];
			}
		}

		// 如果 js data item 的 value 是 Array 就 ignore
		if (dataValue instanceof Array) {

			Trace.error('"data-" attribute not allow Array data');

		} else {
			let targetTag;

			let attr = templateElement.attr(dataAttr);

			if (attr != null && (attr === '' || attr !== false)) {
				// 如果 dataAttr 是在 template HTML 的最上層 tag
				targetTag = templateElement;
			} else {
				// 找出 dataAttr 所在的 HTML tag
				targetTag = templateElement.find('[' + dataAttr + ']');
			}

			targetTag.attr(dataAttr, dataValue);
		}
	}

}());

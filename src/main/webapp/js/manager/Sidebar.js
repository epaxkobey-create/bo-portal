/************************ Task Creater ******************************/
if (typeof (TaskHelper) == 'undefined') {
	TaskHelper = {};
}
(function() {
	TaskHelper.createTask = function(iCycleTime, iCycleTick, iWinType, executeMethod) {
		var task = {
			cycleTime: iCycleTime,
			cycleTick: iCycleTick,
			winType: iWinType,
			execute: executeMethod,
			run: function() {
				this.cycleTick = this.cycleTime;
				TaskExecuter.addTask(this);
			},
			check: function() {
				if (this.cycleTick < 0) {
					return;
				}
				var that = this;
				if (this.cycleTick == 0) {
					this.cycleTick = -1;
					setTimeout(function() {
						that.run();
					}, 100);
					return;
				}
				if (this.cycleTick > 0) {
					this.cycleTick--;
					setTimeout(function() {
						that.check();
					}, 1000);
					return;
				}
			},
			refresh: function() {
				this.cycleTick = 0;
			}

		};
		return task;
	};
})();

if (typeof (MenuHandler) == 'undefined') {
	MenuHandler = {};
}

(function() {
	MenuHandler.init = function() {
		MenuHandler.initMenu();
		setTimeout(function() {
			MenuHandler.updateMenuTask.execute();
		}, 0);
	};

	MenuHandler.initMenu = function() {
		$.ajax({
			type: "GET",
			cache: false,
			url: '/manager/managerController/initSideBarElement',
			success: function(data) {
				writeSideBar(data.menus);
				setTimeout(function() {
					handleSidebarMenu();
					bindMenuEvent(data);
				}, 0);
			}
		});
	}

	var notifyPermission = [2, 14, 33, 140]; // Member, Payment, Marketing, Affiliate

	function writeSideBar(menus) {
		$('#nav').empty();

		var parentMenu = [];
		var subMenu = [];
		$.each(menus, function(i, element) {
			if (element.parent === 0) {
				parentMenu.push(element);
			} else {
				subMenu.push(element);
			}
		});

		$.each(parentMenu, function(i, element) {
			var subTemplate = JsCache.get('#subTemplate').clone().show();
			subTemplate.attr('id', 'menu' + element.id);
			subTemplate.attr('name', element.name);
			subTemplate.attr('display-id', element.displayID);
			subTemplate.attr('parent', '#');
			subTemplate.attr('level', element.level);
			subTemplate.find('a').attr('href', element.url);
			if (element.icon != null) {
				subTemplate.find('i').removeClass();
				subTemplate.find('i').addClass(element.icon);
			}
			subTemplate.find('i').after(element.name);

			$.each(notifyPermission, function(j, el) {
				if (el == element.id) {
					subTemplate.find('i').after(`<span id="count${element.id}" class="label label-danger pull-right"></span>`);
				}
			});
			JsCache.get('#nav').append(subTemplate);
		});
		// sub
		$.each(subMenu, function(i, element) {
			if ($('#menu' + element.parent).find('ul').length == 0) {
				var menuTemplate = JsCache.get('#menuTemplate').clone();
				menuTemplate.attr('id', element.parent);
				menuTemplate.attr('name', $('#menu' + element.parent).attr('name') + '_sub');
				$('#menu' + element.parent).find('a').after(menuTemplate);
			}
			var subTemplate = $('#' + element.parent).find('#subTemplate').clone().show();
			subTemplate.attr('id', 'menu' + element.id);
			subTemplate.attr('name', element.name);
			subTemplate.attr('display-id', element.displayID);
			subTemplate.attr('parent', element.parent);
			subTemplate.attr('level', element.level);
			subTemplate.find('a').attr('href', element.url);
			if (element.icon != null) {
				subTemplate.find('i').removeClass();
				subTemplate.find('i').addClass(element.icon);
			}
			subTemplate.find('i').after(element.name);

			$('[name=' + $('#menu' + element.parent).attr('name') + '_sub' + ']').append(subTemplate);
		});
	}

	var handleSidebarMenu = function() {
		var arrow_class_open = 'icon-angle-down',
			arrow_class_closed = 'icon-angle-left';

		$('li:has(ul)', '#sidebar-content ul').each(function() {
			if ($(this).hasClass('current') || $(this).hasClass('open-default')) {
				$('>a', this).append("<i class='arrow " + arrow_class_open + "'></i>");
			} else {
				$('>a', this).append("<i class='arrow " + arrow_class_closed + "'></i>");
			}
		});

		if ($('#sidebar').hasClass('sidebar-fixed')) {
			$('#sidebar-content').append('<div class="fill-nav-space"></div>');
		}

		$('#sidebar-content ul > li > a').on('click', function(e) {

			if ($(this).next().hasClass('sub-menu') == false) {
				return;
			}

			// Toggle on small devices instead of accordion
			if ($(window).width() > 767) {
				var parent = $(this).parent().parent();

				parent.children('li.open').children('a').children('i.arrow').removeClass(arrow_class_open).addClass(arrow_class_closed);
				parent.children('li.open').children('.sub-menu').slideUp(200);
				parent.children('li.open-default').children('.sub-menu').slideUp(200);
				parent.children('li.open').removeClass('open').removeClass('open-default');
			}

			var sub = $(this).next();
			if (sub.is(":visible")) {
				$('i.arrow', $(this)).removeClass(arrow_class_open).addClass(arrow_class_closed);
				$(this).parent().removeClass('open');
				sub.slideUp(200, function() {
					$(this).parent().removeClass('open-fixed').removeClass('open-default');
					calculateHeight();
				});
			} else {
				$('i.arrow', $(this)).removeClass(arrow_class_closed).addClass(arrow_class_open);
				$(this).parent().addClass('open');
				sub.slideDown(200, function() {
					calculateHeight();
				});
				// prevent dom element hide
				$.each(sub.children(), function() {
					var id = $(this).attr('id');
					if (StringUtil.startsWith(id, 'menu')) {
						if (!$(this).is(':visible')) {
							$(this).attr('style', 'display: list-item;');
						}
					}
				});

			}

			e.preventDefault();
		});

		var calculateHeight = function() {
			$('body').height('100%');

			var $header = $('.header');
			var header_height = $header.outerHeight();

			var document_height = $(document).height();
			var window_height = $(window).height();

			var doc_win_diff = document_height - window_height;

			if (doc_win_diff <= header_height) {
				var new_height = document_height - doc_win_diff;
			} else {
				var new_height = document_height;
			}

			new_height = new_height - header_height;

			var document_height = $(document).height();

			$('body').height(new_height);
		}

		var _handleResizeable = function() {
			$('#divider.resizeable').mousedown(function(e) {
				e.preventDefault();

				var divider_width = $('#divider').width();
				$(document).mousemove(function(e) {
					var sidebar_width = e.pageX + divider_width;
					if (sidebar_width <= 300 && sidebar_width >= (divider_width * 2 - 3)) {
						if (sidebar_width >= 240 && sidebar_width <= 260) {
							$('#sidebar').css("width", 250);
							$('#sidebar-content').css("width", 250);
							$('#content').css("margin-left", 250);
							$('#divider').css("margin-left", 250);
						} else {
							$('#sidebar').css("width", sidebar_width);
							$('#sidebar-content').css("width", sidebar_width);
							$('#content').css("margin-left", sidebar_width);
							$('#divider').css("margin-left", sidebar_width);
						}

					}

				})
			});
			$(document).mouseup(function(e) {
				$(document).unbind('mousemove');
			});
		}

		_handleResizeable();
	}

//	var initAccessData = function() {
//		$.ajax({
//			type: "GET",
//            cache:false,
//			url: '/manager/managerController/updateMenuAccess',
//			success: function (responseText) {
//				accessMenu(responseText);
//			}
//		});
//	};


//	var accessMenu = function(data) {
//		$('li[id^=menu][parent=#]').hide(); // close
//		$('li[id^=menu][level=2]').hide(); // hide
//		// 權限控制
//		DataBase.access.clean();
//		var accessRight = data.access.split(',');
//		$.each(accessRight, function(i, access){
//			DataBase.access.insert('menu' + access);
//			var menu = $("#menu" + access);
//			menu.show();
//			// parent show too
//			var $rowParent = menu.parents();
//			try {
//				var pathName = $rowParent.attr('name').split(/\_/g); // xxx_sub
//				$('li[name=' + pathName[0] +']').show();
//			} catch(err) {
//				
//			}
//		});
//		$('#logoLink').attr('href', data.index);
//	};

	var bindMenuEvent = function(data) {

		$('#sidebar-content').find('a').each(function() {
			if ($(this).attr('href') == window.location.pathname) {
				// current
				var $rowParent = $(this).closest('li');
				$rowParent.addClass('current');
				if (!$rowParent.is(':visible')) {
					$rowParent.attr('style', 'display: list-item;');
				}
				// parent open
				var parent = $rowParent.parents();
				parent.addClass('current').show();
				try {
					var pathName = parent.attr('name').split(/\_/g); // xxx_sub
					$('li[name=' + pathName[0] + ']').addClass('open');
					$('li[name=' + pathName[0] + ']').find('i.arrow').removeClass('icon-angle-left').addClass('icon-angle-down');
					if ($('li[name=' + pathName[0] + ']').has('span')) {
						$('li[name=' + pathName[0] + ']').find('span').first().attr('style', 'margin-right:10px');
					}
				} catch (err) {

				}
			}
		});

		if (data.isAllowCustomerFee) {

			const $subWithdrawal = $('#menu20');

			if (!$subWithdrawal) {
				return;
			}

			$subWithdrawal.hover(function() {
				var e = $(this);
				e.off('hover');

				$.ajax({
					type: "GET",
					url: '/manager/managerController/getPendingWithdrawal',
					dataType: 'JSON',
					success: function(data) {
						if (data.error) {
							NotifyHandler.errorMsg(data.error);
							return;
						}
						const feeType = data.feeType;
						let pendingContent = data.name;
						if (feeType !== null) {
							pendingContent = `<table id="pendingWithdraw" class="table table-hover">
													<tbody>
														${feeType.map(data => {
								return `<tr href="/page/manager/payment/withdrawal.jsp?minutes=${data.key}">
																		<td>${data.name}</td>
																		<td><span>${CurrencyUtil.thousandComma(data.count)}</span></td>
																	</tr>`
							}).join("")}
													</tbody>
												</table>`;
						}

						let popover = e.popover({
							title: (feeType != null) ? data.name : '',
							placement: 'auto right',
							container: 'body',
							html: true,
							animation: false,
							template: `<div class="popover pendingwithdrawal" role="tooltip"><div class="arrow"></div><h3 class="popover-title"></h3><div class="popover-content"></div></div>`
						}).on("mouseenter", function() {
							var _this = this;
							popover.attr('data-content', pendingContent);
							e.popover("show");
							$(".popover").on("mouseleave", function() {
								$(_this).popover('hide');
							});
						}).on("mouseleave", function() {
							var _this = this;
							setTimeout(function() {
								if (!$(".popover:hover").length) {
									$(_this).popover("hide");
								}
							}, 300);
						}).on('shown.bs.popover', function() {
							const pendingWithdraw = document.getElementById("pendingWithdraw");
							if (pendingWithdraw) {
								const tr = pendingWithdraw.getElementsByTagName('tr');
								for (let i = 0; i < tr.length; i++) {
									tr[i].onclick = function() {
										window.location = tr[i].getAttribute('href');
										return false;
									};
								}
							}
						});
					},
					error: function(e) {
						console.log(e);
					}
				});
			});
		}
	};

	MenuHandler.updateMenuTask = TaskHelper.createTask(5, // cycleTime
		0, // cycleTick
		null, function() {
			var that = this;
			updateMenuExecute(that);
		});

	var updateMenuExecute = function(task) {
		$.ajax({
			type: "GET",
			cache: false,
			url: '/manager/managerController/getSideBarElement',
			success: function(response) {
				try {
					if (!response) {
						return;
					}
					updateMenuBar(response);
				} finally {
					task.check();
					TaskExecuter.execute();
				}
			},
			error: function() {
				task.check();
				TaskExecuter.execute();
			}
		});
	}


	var updateMenuBar = function(data) {

		setTimeout(function() {
			writeSideBar(data.menus);
			handleSidebarMenu();
			bindMenuEvent(data);
		}, 0);

		$('#logoLink').attr('href', data.index);

	}

//	var updateMenuList = function(data) {
//		var isUpdated = false;
//		
//		var access = data.access.split(',')
//		
//		if (DataBase.access.queryAll().length > 0) {
//			$.each(access, function(i, element) {
//				if (DataBase.access.queryByID('menu' + element.trim()) < 0) { // 查無此ID
//					isUpdated = true;
//					return true;
//				}
//			});
//			
//			if (DataBase.access.queryAll().length != access.length) {
//				isUpdated = true;
//			}
//		}
//		
//		if(isUpdated){
//			setTimeout(function(){
//				writeSideBar(responseText.menus);
//			}, 0);
//		}
//	}

})();
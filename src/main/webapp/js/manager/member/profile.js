if (typeof (ProfileHandler) == 'undefined') {
	ProfileHandler = {};
}

(function () {

	ProfileHandler.cacheData = {};

	ProfileHandler.init = function () {

		// prevent form submission on "Enter" pressed
		$('form').on('keydown', 'input', function (e) {
			if (e.key === 'Enter') {
				e.preventDefault();
			}
		});

		CurrencyUtil.updateSetting({
			"precision": 2,
			"trailingZeros": true,
			"roundingMode": CurrencyUtil.roundingMode.HALF_UP
		});
		ProfileHandler.getProfileOverview();

		var updateGeneralForm = $("[name='updateGeneralForm']");
		updateGeneralForm.find('[name=password]').attr("data-random", Math.random());
		updateGeneralForm.find('[name=userBirthday]').attr("data-random", Math.random());

		$("[name='takeBackFundsForm']").find('[name=transferType]').change(function () {
			initProviderSelector();
		});


		$('#updateDocumentModal').on('hidden.bs.modal', function () {
			const form = $('#updateDocumentForm');
			const validator = form.data('validator');

			if (validator) {
				validator.resetForm();
			}

			form[0].reset();

			form.find(".error").removeClass("error");
			form.find("label.error").remove();
			form.find('.error-msg-pictureUpload-block-frontPhotoFile').html("");
			form.find('.error-msg-pictureUpload-block-backPhotoFile').empty();
			form.find('.error-msg-pictureUpload-block-addressPhotoFile').empty();

			// Reset file cache when modal closes
			ProfileHandler.cachedFiles = {
				frontPhotoFile: null,
				backPhotoFile: null,
				addressPhotoFile: null
			};

			// Reset upload status indicators when modal closes
			ProfileHandler.uploadStatus = {
				frontPhotoFile: false,
				backPhotoFile: false,
				addressPhotoFile: false
			};

			// Reset original file state when modal closes
			ProfileHandler.originalFileState = {
				frontPhotoFile: false,
				backPhotoFile: false,
				addressPhotoFile: false
			};

			console.log("Modal closed - reset uploadStatus, cachedFiles, and originalFileState");
		});
		requirementBoxOnChange();
		$('#updatePasswordModal').on("hidden.bs.modal", function () {
			resetPasswordForm();
		});

	};


	ProfileHandler.initCalendar = function (dob, expiryDate) {
		var dateOption = {
			singleDatePicker: true,
			showDropdowns: true,
			locale: DateRangeHandler.changeLanguage('DD/MM/YYYY', PageConfig.lang),
		};

		$('.singleDatePicker').each(function () {
			var $input = $(this);
			var id = $input.attr('id');
			var currentDateOption = Object.assign({}, dateOption);

			if (id === "dob") {
				const date18YearsAgo = moment().subtract(18, 'years');
				currentDateOption.maxDate = date18YearsAgo;
			}

			DateRangeHandler.singleDatePicker2($input, currentDateOption, PageConfig.lang);

			let valueToSet = undefined;
			let targetName = '';

			if (id === "dob") {
				if (dob !== undefined) {
					valueToSet = moment.utc(dob).utcOffset(8).format("DD/MM/YYYY")
					targetName = 'dob';

					const picker = $input.data("daterangepicker");
					if (picker) {
						const momentDate = moment.utc(dob).utcOffset(8).format("DD/MM/YYYY");// Convert string to moment object
						// const date18YearsAgo = moment().subtract(18, 'years');
						picker.setStartDate(momentDate);
						picker.setEndDate(momentDate);

					}
				} else {
					const picker = $input.data("daterangepicker");
					if (picker) {
						const momentDate = moment().subtract(18, 'years').format("DD/MM/YYYY");// Convert string to moment object
						// const date18YearsAgo = moment().subtract(18, 'years');
						picker.setStartDate(momentDate);
						picker.setEndDate(momentDate);

					}
				}


			} else if (id === 'expiryDate' && expiryDate !== undefined) {
				valueToSet = moment.utc(expiryDate).utcOffset(8).format("DD/MM/YYYY");
				targetName = 'expiryDate';
				const picker = $input.data("daterangepicker");
				if (picker) {
					const momentDate = moment.utc(expiryDate).utcOffset(8).format("DD/MM/YYYY");
					picker.setStartDate(momentDate);
					picker.setEndDate(momentDate);
				}
			}
			if (valueToSet !== undefined) {
				$input.val(valueToSet);
				$input.parent().find(`[name="${targetName}"]`).val(valueToSet);

			}
			DateRangeHandler.bindEvent();
		});
	}


	ProfileHandler.getProfileOverview = function () {
		$.ajax({
			type: "GET",
			url: '/manager/member/getProfileOverview',
			dataType: 'JSON',
			data: {
				userId: PageConfig.userId,
				currency: PageConfig.currency
			},
			success: function (data) {
				if (data.error) {
					NotifyHandler.errorMsg(data.error);
					return;
				}
				ProfileHandler.cacheData = data;
				renderTable();
				ProfileHandler.getAccountContactVerify();
			}
		});
	};


	ProfileHandler.getAccountContactVerify = function () {
		$.ajax({
			type: "GET",
			url: '/manager/member/getAccountContactVerify',
			dataType: 'JSON',
			data: {
				userId: PageConfig.userId
			},
			success: function (data) {
				if (data.error) {
					NotifyHandler.errorMsg(data.error);
					return;
				}

				$('[id$=_verifyButton]').hide();
				$('[id$=_unlinkThe3PartButton]').hide();

				let verifyMark = '<i class="icon-ok" style="color: green;"></i>';


				if (data.verifiedList) {
					let container = $('#profileContainer');
					for (let i = 0; i < data.verifiedList.length; i++) {
						let record = data.verifiedList[i];

						let contactType = record.cotactType;

						let target = container.find('#' + PageConfig.AccountUpdateType[record.updateType].name + 'flag');

						// if (contactType == ContactType.Phone.value) {
						// 	if (record.contentNo == 2) {
						// 		// target = container.find('#' + PageConfig.AccountUpdateType[PageConfig.UpdateType.PhoneNumber2].name + 'flag');
						// 	}
						// 	if (record.contentNo == 3) {
						// 		// target = container.find('#' + PageConfig.AccountUpdateType[PageConfig.UpdateType.PhoneNumber3].name + 'flag');
						// 	}
						// }

						target.find("[name='verifier']").text('');
						target.find("[name='verifiedTime']").text('');

						let unlinkThe3PartButton = target.find('#' + record.cotactType + "-" + record.contentNo + "_unlinkThe3PartButton");
						if (record.verified === 0) { // unverified
							let verifyButton = target.find('#' + record.cotactType + "-" + record.contentNo + "_verifyButton");
							if (verifyButton) {
								verifyButton.show();
								let setupLoginButton = target.find('#' + record.cotactType + "-" + record.contentNo + "_setupLoginButton");
								if (setupLoginButton) {
									setupLoginButton.find('.btn').attr('disabled', true);
									setupLoginButton.hide();
								}
							}

							if (unlinkThe3PartButton) {
								unlinkThe3PartButton.hide();
							}
						} else if (record.verified == 1) { // verified
							let typeNameId = '#' + record.cotactTypeName;
							if (record.contentNo > 1) {
								typeNameId = typeNameId + record.contentNo;
							}
							target.find(typeNameId).html(
								container.find(typeNameId).text() +
								' ' + verifyMark);
							target.find("[name='verifier']").text(record.updater);
//							target.find("[name='verifiedTime']").text(I18N.get('form.text.backOffice.verifiedBy') + ' ' +
//									DateUtil.format(record.updateTime, PageConfig.DateHourMinuteSecondPattern));
							target.find("[name='verifiedTime']").text(I18N.get('form.text.backOffice.verifiedBy') + ' ' + record.updateTime);

							let setupLoginButton = target.find('#' + record.cotactType + "-" + record.contentNo + "_setupLoginButton");
							if (setupLoginButton) {
								setupLoginButton.show();
								if (record.allowLogin == 1) {
									setupLoginButton.find('.btn').attr('disabled', true);
								} else {
									setupLoginButton.find('.btn').attr('disabled', false);
								}
							}

							if (unlinkThe3PartButton) {
								unlinkThe3PartButton.show();
							}
						}
					}
				}
			}
		});
	};

	ProfileHandler.currentLevel = 0;
	ProfileHandler.cacheVipLevel = null;


	ProfileHandler.resetPassword = function () {
		const updateForm = $("[name='updatePasswordForm']");
		updateForm[0].reset();
		updateForm.validate().resetForm();
		updateForm.find("[name='encodePassword']").val('');
		$.uniform.update();

		// Reset password requirements display
		resetPasswordForm();
	};


	let loadStatus = function (status) {
		let updateStatusModal = $('#updateStatusModal');
		let element = updateStatusModal.children().detach();
		element.find("[name='status']").val(status);
		$.uniform.update();//Chrome、IE要這樣
		updateStatusModal.append(element);
	}

	var loadRemark = function () {
		var data = ProfileHandler.cacheData;
		var element = $('#updateUserRemarkModal').children().detach();
		const userRemark = data?.userRemark;
		element.find('#userRemark').val(userRemark);
		$('#updateUserRemarkModal').append(element);
	};

	ProfileHandler.resetUserRemark = function () {
		var updateUserRemarkForm = $("[name='updateUserRemarkForm']");
		updateUserRemarkForm.get(0).reset();
		loadRemark();
	};

	ProfileHandler.updateUserRemark = function () {
		var updateUserRemarkForm = $("[name='updateUserRemarkForm']");

		$.ajax({
			type: "POST",
			url: '/manager/member/updateUserRemark',
			dataType: 'JSON',
			data: updateUserRemarkForm.serialize(),
			success: function (data) {
				if (data.error) {
					NotifyHandler.errorMsg(data.error);
					return;
				}
				NotifyHandler.successMsg(data.message);
				$('#updateUserRemarkModal').modal('hide');
				ProfileHandler.getProfileOverview();
			},
			beforeSend: function () {
				updateUserRemarkForm.find('[name=resetButton]').addClass('loading');
				updateUserRemarkForm.find('[name=resetButton]').removeAttr('onclick');

				updateUserRemarkForm.find('[name=save]').addClass('loading');
				updateUserRemarkForm.find('[name=save]').removeAttr('onclick');
			},
			complete: function () {
				updateUserRemarkForm.find('[name=resetButton]').removeClass('loading');
				updateUserRemarkForm.find('[name=resetButton]').attr('onclick', 'ProfileHandler.resetUserRemark()');

				updateUserRemarkForm.find('[name=save]').removeClass('loading');
				updateUserRemarkForm.find('[name=save]').attr('onclick', 'ProfileHandler.updateUserRemark()');

			}
		});
	};

	ProfileHandler.goToEditUserRemark = function () {
		loadRemark();
		$('#updateUserRemarkModal').modal('show');
	};

	ProfileHandler.getEditStatus = function () {

		let updateStatusForm = $("[name='updateStatusForm']");
		updateStatusForm.validate({
			onfocusout: false
		});
		updateStatusForm.get(0).reset();
		loadStatus(ProfileHandler.cacheData.status);
		$('#updateStatusModal').modal('show');
	}


	/**
	 * update password
	 * */

	ProfileHandler.updatePassword = function () {
		// let updatePasswordForm = $("[name='updatePasswordForm']");
		$('#updatePasswordModal').modal("show");

	}

	ProfileHandler.resetStatus = function () {
		let updateStatusForm = $("[name='updateStatusForm']");
		updateStatusForm.get(0).reset();
		loadStatus(ProfileHandler.cacheData.status);
	};

	ProfileHandler.updatePasswordSubmit = function () {

		var updatePasswordForm = $("[name = 'updatePasswordForm']");

		updatePasswordForm.find("[name='userId']").remove();
		$("#updatePasswordForm").append(
			$("<input>", {
				type: "hidden",
				name: "userId",
				value: PageConfig.userId
			})
		);
		$.ajax({
			type: "POST",
			url: '/manager/member/updatePassword',
			dataType: 'JSON',
			data: updatePasswordForm.serialize(),
			success: function (data) {
				if (data.error) {
					NotifyHandler.errorMsg(data.error);
					return;
				}
				NotifyHandler.successMsg(data.message);
				updatePasswordForm.get(0).reset();
				updatePasswordForm.data("validator").resetForm();
				$('#updatePasswordModal').modal('hide');
				ProfileHandler.getProfileOverview();
			},
			complete: function () {
				updatePasswordForm.find('[name=save]').removeClass('disabled');
				updatePasswordForm.find('[name=resetButton]').removeClass('disabled');
			}
		});
	}
	ProfileHandler.updatePasswordValidation = function () {

		const updatePasswordForm = $("[name='updatePasswordForm']");
		if (!updatePasswordForm.data("validator")) {
			updatePasswordForm.validate({
				ignore: [],
				errorPlacement: function (error, element) {
					if (element.attr("id") === "editPassword" || element.attr("name") === "confirmPassword") {
						element.closest('.form-group')
							.find('.error-msg-block')
							.html(error);
					} else {
						error.insertAfter(element);
					}
				}
			});
			updatePasswordForm.find('[name=save]').removeClass('disabled');
			updatePasswordForm.find('[name=resetButton]').removeClass('disabled');
			updatePasswordForm.find("[name=password]").rules('remove');


			updatePasswordForm.find("[name=password]").rules('add', {
				required: true,
				passwordContainsSpace: true,
				passwordContainsUnicode: true,
				playerLoginPassword: true,
				messages: {
					playerLoginPassword: I18N.get('msg.error.password.isNotValidated.v2')
				}
			});

			updatePasswordForm.find("[name=confirmPassword]").rules('remove');
			updatePasswordForm.find("[name=confirmPassword]").rules('add', {
				required: true,
				equalTo: "[name='password']",
				messages: {
					equalTo: I18N.get("msg.error.validation.passwordNotMatch")
				}
			});
		}


		if (!updatePasswordForm.valid()) {
			updatePasswordForm.find('[name=save]').removeClass('disabled');
			updatePasswordForm.find('[name=resetButton]').removeClass('disabled');
			return;
		}

		ProfileHandler.updatePasswordSubmit();

	}


	ProfileHandler.updateStatus = function () {
		let updateStatusForm = $("[name='updateStatusForm']");

		updateStatusForm.find('[name=save]').addClass('disabled');
		updateStatusForm.find('[name=resetButton]').addClass('disabled');

		$.ajax({
			type: "POST",
			url: '/manager/member/updateStatus',
			dataType: 'JSON',
			data: updateStatusForm.serialize(),
			success: function (data) {
				if (data.error) {
					NotifyHandler.errorMsg(data.error);
					return;
				}
				NotifyHandler.successMsg(data.message);
				$('#updateStatusModal').modal('hide');
				ProfileHandler.getProfileOverview();
			},
			complete: function () {
				updateStatusForm.find('[name=save]').removeClass('disabled');
				updateStatusForm.find('[name=resetButton]').removeClass('disabled');
			}
		});
	};

	// Helper function to display '-' for null/undefined values
	var safeText = function (value) {
		return (value !== null && value !== undefined && value !== '') ? value : '-';
	};

	// Helper function to format currency with € symbol
	var formatCurrency = function (value) {
		if (value !== null && value !== undefined && value !== '') {
			return CurrencyUtil.formatterWithCurrency(value, PageConfig.managerCurrencySymbol);
		}
		return '-';
	};


	var accountProviderSortCompare = function (a, b) {
		if (a.provider.systemCode < b.provider.systemCode) {
			return -1;
		}
		if (a.provider.systemCode > b.provider.systemCode) {
			return 1;
		}
		return 0;
	}

	var renderTable = function () {
		var data = ProfileHandler.cacheData;
		var stats = ProfileHandler.cacheData.accountStats;
		var profileContainer = $('#profileContainer');

		var otherWalletBalance = 0;
		profileContainer.find('#accountProviderContainer').empty();
		if (data.accountProviderList && data.accountProviderList.length > 0) {
			data.accountProviderList.sort(accountProviderSortCompare);

			var frag = document.createDocumentFragment();
			for (var i = 0; i < data.accountProviderList.length; i++) {
				var accountProvider = data.accountProviderList[i];
				var template = $('#accountProviderTemplate').children().clone();
				var provider = accountProvider.provider;
//				template.find('#systemCode').text(provider.systemCode);
				//for a8 inplay provider(63)
				template.find('#providerName').html(provider.providerName);

				template.find('#providerAccount').text(safeText(accountProvider.providerBOAccount));
				if (accountProvider.providerExtraData) {
					template.find('#providerExtraData').text(accountProvider.providerExtraData.replaceAll("{", "").replaceAll("}", ""));
				} else {
					template.find('#providerExtraData').text('-');
				}
				// //TODO adjust if condition
				// if (accountProvider.provider.systemCode === "CRICKET" ||  accountProvider.provider.systemCode === "CRICKETV2") {
				// 	template.find('#providerExtraData').text("exposure:" + accountProvider.exposure);
				// }

				if (PageConfig.exposureProviderList?.indexOf(accountProvider.provider.systemCode) && accountProvider.exposure) {
					template.find('#providerExtraData').text("exposure:" + accountProvider.exposure);
				}

				const createTimeStr = (accountProvider.providerCreateTime) ? DateUtil.format(accountProvider.providerCreateTime, PageConfig.DateHourMinuteSecondPattern) : '-';
				template.find('#providerCreateTime').html(createTimeStr);

				otherWalletBalance += accountProvider.providerBalance;
				template.find('#providerBalance').html(formatCurrency(accountProvider.providerBalance));
				frag.appendChild(template[0]);
			}
			profileContainer.find('#accountProviderContainer').append(frag);
		}
		profileContainer.find('#accountCardContainer').empty();
		if (data.accountCard != null) {
			var accountCardFrag = document.createDocumentFragment();
			var accountCard = data.accountCard;
			var accountCardTemplate = $('#accountCardTemplate').children().clone();
			accountCardTemplate.find('#cardBank').text(accountCard.bankName);
			accountCardTemplate.find('#cardBrand').text(accountCard.cardBrand);
			accountCardTemplate.find('#cardNumber').text(formatCardNumber(accountCard.cardNumber));
			accountCardTemplate.find('#cardExpiryDate').text(accountCard.expiryDate);
			accountCardTemplate.find('#cardholderName').text(accountCard.cardholderName);
			accountCardTemplate.find('#btnRemoveAccountCard')[0].addEventListener('click', function () {
				ProfileHandler.openRemoveAccountCardModal(accountCard);
			});
			accountCardFrag.appendChild(accountCardTemplate[0]);
			profileContainer.find('#accountCardContainer').append(accountCardFrag);
		}


		// profileContainer.find('#accountBankContainer').empty();
		// if (data?.accountBank != null) {
		// 	var accountCardFrag = document.createDocumentFragment();
		// 	var accountBank = data.accountBank;
		//
		// 	// 控制 Add 按钮的显示/隐藏
		// 	if (accountBank.length >= 3) {
		// 		$('#gotoAddAccountBankId').hide();
		// 	} else {
		// 		$('#gotoAddAccountBankId').show();
		// 	}
		// 	accountBank.forEach(function (accountBank) {
		// 		var accountBankTemplate = $('#accountBankTemplate').children().clone();
		// 		accountBankTemplate.find('#bankName').text(accountBank.bankName || 'Bank ' + accountBank.bankId);
		// 		accountBankTemplate.find('#bankAccountNumber').text(formatAccountNumber(accountBank.accountNumber));
		//
		// 		accountBankTemplate.find('#btnRemoveAccountCard').on('click', function () {
		// 			ProfileHandler.openRemoveAccountBankModal(accountBank.accountNumber, accountBank.id, accountBank.bankName);
		// 		});
		//
		// 		accountCardFrag.appendChild(accountBankTemplate[0]);
		// 	});
		// 	profileContainer.find('#accountBankContainer').append(accountCardFrag);
		// }

		var element = profileContainer.children().detach();
		element.find('#createTime').text(safeText(data.signUpTimeStr));
		element.find('#userId').text(safeText(data.userId));
		element.find('#legalFirstName').text(safeText(data.legalFirstName));
		element.find('#legalLastName').text(safeText(data.legalLastName));

		// element.find('#email').html('');

		element.find('#email').html(safeText(data.userId));


		const sumsubAccountDocument = data['sumsubAccountDocument'];
		if (sumsubAccountDocument) {
			element.find('#documentType').html(safeText(sumsubAccountDocument?.['documentType']));
			element.find('#documentNo').html(safeText(sumsubAccountDocument?.['documentNo']));
			element.find('#fullName').html(safeText(sumsubAccountDocument?.['fullName']));
			if (sumsubAccountDocument?.['dob'] !== undefined) {

				element.find('#dob').html(moment.utc(sumsubAccountDocument?.['dob']).utcOffset(8).format("DD/MM/YYYY"));
			} else {
				element.find('#dob').html('-');
			}
			if (sumsubAccountDocument?.['expiryDate'] !== undefined) {
				element.find('#expiryDate').html(moment.utc(sumsubAccountDocument?.['expiryDate']).utcOffset(8).format("DD/MM/YYYY"));
			} else {
				element.find('#expiryDate').html('-');
			}

			const streetText = safeText(sumsubAccountDocument?.['street']);
			element.find('#streetDisplay').text(streetText);
			element.find('#streetDisplay').closest('.remark-cell').attr('data-fulltext', streetText);


			const citySafeText = safeText(safeText(sumsubAccountDocument?.['city']));
			element.find('#cityDisplay').text(citySafeText);
			element.find('#cityDisplay').closest('.remark-cell').attr('data-fulltext', citySafeText);


			element.find('#postalCode').html(safeText(sumsubAccountDocument?.['postalCode']));

			const accountDocumentVerifyStatusType = PageConfig.KycDocumentStatusType[sumsubAccountDocument?.['accountDocumentStatusType']] ?? PageConfig.KycDocumentStatusType["0"];

			if (accountDocumentVerifyStatusType) {
				element.find('#verificationStatus').text(I18N.get(accountDocumentVerifyStatusType.columnName)).removeClass().addClass('label ' + accountDocumentVerifyStatusType.css);
			}

			const fullText = safeText(sumsubAccountDocument?.['approveRemark']);
			element.find('#verificationRemark').text(fullText);
			element.find('#verificationRemark').closest('.remark-cell').attr('data-fulltext', fullText);

			element.find('[name=documentId]').attr('data-id', sumsubAccountDocument?.['id']);
		} else {
			element.find('#documentType').html('-');
			element.find('#documentNo').html('-');
			element.find('#fullName').html('-');
			element.find('#dob').html('-');
			element.find('#expiryDate').html('-');
			element.find('#streetDisplay').html('-');
			element.find('#cityDisplay').html('-');
			element.find('#postalCode').html('-');
			element.find('#verificationStatus').text(I18N.get(PageConfig.KycDocumentStatusType["0"].columnName)).removeClass().addClass('label ' + PageConfig.KycDocumentStatusType["0"].css);
			element.find('#verificationRemark').text('-');
		}

		element.find('#birthday').text(safeText(data.birthdayStr));
		element.find('#vipLevel').text(safeText(data.vipLevelName));
		ProfileHandler.currentLevel = data.vipLevel;

		element.find('#vipExperience').text(safeText(data.summaryConvertionPoint));
		element.find('#vipPoint').text(safeText(data.accountVipPoint));
		element.find('#affiliateUrl').text(safeText(data.affiliate));
		element.find('#friendReferCode').text(safeText(data.friendReferCode));

		let userChannelTypeObj = UserChannelType.getInstanceOf(data.userChannelType);
		if (userChannelTypeObj) {
			element.find('#userChannelType').text(userChannelTypeObj.displayName);
		} else {
			element.find('#userChannelType').text('-');
		}

		if (data.userChannelType === UserChannelType.DIRECT.value
			|| data.userChannelType === UserChannelType.AFFILIATE.value) {

			// element.find('#affiliateName').text(data.affiliateName);
			element.find('#userChannelName').text(safeText(data.affiliateName));

		} else if (data.userChannelType === UserChannelType.REFER_A_FRIEND.value) {
			element.find('#userChannelName').text(safeText(data.friendReferrer));
		} else {
			element.find('#userChannelName').text('-');
		}

		if (data.gender >= 0) {
			const genderType = AccountGenderType.getInstanceOf(data.gender);
			element.find('#genderType').text(I18N.get(genderType.getFullName()));
		} else {
			element.find('#genderType').text('-');
		}

		if (data.marital >= 0) {
			const maritalType = AccountMaritalType.getInstanceOf(data.marital);
			element.find('#maritalType').text(I18N.get(maritalType.getFullName()));
		} else {
			element.find('#maritalType').text('-');
		}

		var accountStatusType = PageConfig.AccountStatusType[data.status];
		element.find('#status').text(I18N.get(accountStatusType.fullName)).removeClass().addClass('label ' + accountStatusType.css);
		var accountGroup = '';
		for (var i = 0; i < data.accountGroupList.length; i++) {
			if (data.accountGroupList[i]) {
				accountGroup += ',&nbsp;' + data.accountGroupList[i].name;
			}
		}
		if (accountGroup.length > 0) {
			accountGroup = accountGroup.substring(7);//',&nbsp;'.length
		}
		element.find("[name='accountGroups']").html(accountGroup);


		const $cell = element.find('.userRemark-cell');
		const userRemarkFullText = safeText(data.userRemark)
		$cell.find('#userRemarkDisplay').text(userRemarkFullText);

		$cell.attr('data-fulltext', userRemarkFullText);
		element.find('#userRemarkDisplay').html(data.userRemark ? userRemarkFullText : '-');
		// element.find("[name='userRemarkUpdateTime']").text(data.userRemarkUpdateTime ? (I18N.get('form.text.backOffice.updatedBy') + ' ' + data.userRemarkUpdateTime) : '');
		// element.find("[name='userRemarkUpdater']").text(data.userRemarkUpdater ? data.userRemarkUpdater : '');

		element.find('#riskRemark').html(data.riskRemark ? data.riskRemark.replace(/<br\s*[\/]?>/gi, '\r\n') : '-');
		element.find("[name='riskRemarkUpdateTime']").text(data.riskRemarkUpdateTime ? (I18N.get('form.text.backOffice.updatedBy') + ' ' + data.riskRemarkUpdateTime) : '-');
		element.find("[name='riskRemarkUpdater']").text(data.riskRemarkUpdater ? data.riskRemarkUpdater : '-');

		element.find('#mainBalance').html(formatCurrency(data.balance));
		element.find('#otherWallet').html(formatCurrency(otherWalletBalance));
		// for bonus wallet
		element.find('[name^=wallet]').hide();

		element.find('#signUpIp').text(safeText(data.signUpIp));
		element.find('#loginIp').text(safeText(data.loginIp));
		element.find('#loginTime').text(safeText(data.loginTimeStr));
		element.find('#firstDepositTime').text(safeText(data.firstDepositTimeStr));
		element.find('#lastDepositTime').text(safeText(data.lastDepositTimeStr));
		element.find('#firstWithdrawalTime').text(safeText(data.firstWithdrawalTimeStr));
		element.find('#lastWithdrawalTime').text(safeText(data.lastWithdrawalTimeStr));
		element.find('#firstAdjustmentTime').text(safeText(data.firstAdjustmentTimeStr));
		element.find('#lastAdjustmentTime').text(safeText(data.lastAdjustmentTimeStr));
		element.find('#firstBonusTime').text(safeText(data.firstBonusTimeStr));
		element.find('#lastBonusTime').text(safeText(data.lastBonusTimeStr));
		element.find('#firstBetTime').text(safeText(data.firstBetTimeStr));
		element.find('#lastBetTime').text(safeText(data.lastBetTimeStr));

//		element.find('#depositCount').html(CurrencyUtil.thousandComma(stats.depositCount));
		element.find('#depositAmount').html(formatCurrency(stats.depositAmount));
//		element.find('#withdrawalCount').html(CurrencyUtil.thousandComma(stats.withdrawalCount));
		element.find('#withdrawalAmount').html(formatCurrency(stats.withdrawalAmount));
		// element.find('#netDepositAmount').html(formatCurrency(stats.netDepositAmount));
//		element.find('#adjustmentCount').html(CurrencyUtil.thousandComma(stats.adjustmentCount));
		element.find('#adjustmentAmount').html(formatCurrency(stats.adjustmentAmount));
//		element.find('#bonusCount').html(CurrencyUtil.thousandComma(stats.bonusCount));
		element.find('#bonusAmount').html(formatCurrency(stats.bonusAmount));
		element.find('#turnover').html(formatCurrency(stats.turnover));
		element.find('#profitLoss').html(formatCurrency(stats.profitLoss));
		element.find('#pointToBalance').html(formatCurrency(stats.pointToBalance));
		element.find('#recycleBalance').html(formatCurrency(stats.recycleBalance));

		profileContainer.append(element);
	};

	ProfileHandler.openRemoveAccountCardModal = function(accountCard) {
		var modal = $('#removeAccountCardModal');
		modal.find('#title').text(I18N.get("form.text.account.accountBank.remove_?", [accountCard.cardBrand +" "+ accountCard.bankName,formatCardNumber(accountCard.cardNumber)]));
		modal.modal({backdrop: 'static', keyboard: false, show: true});
	}


	ProfileHandler.gotoVerifyContact = function (verifycontactType, verifyContentNo) {
		$.ajax({
			type: "POST",
			url: '/manager/member/verifyProfileContact',
			dataType: 'JSON',
			data: {
				userId: PageConfig.userId,
				contactType: verifycontactType,
				contentNo: verifyContentNo
			},
			success: function (data) {
				if (data.error) {
					NotifyHandler.errorMsg(data.error);
					return;
				}
				NotifyHandler.successMsg(data.message);
				$('#takeBackFundsModal').modal('hide');
				ProfileHandler.getProfileOverview();
			}
		});
	}

	ProfileHandler.toggleVisibility = function (inputId, iconElement) {
		const input = document.getElementById(inputId);
		const isHidden = input.type === "password";

		input.type = isHidden ? "text" : "password";
		iconElement.classList.toggle("icon-eye-close", !isHidden);
		iconElement.classList.toggle("icon-eye-open", isHidden);
	}

	ProfileHandler.goToEditDocument = function () {
		loadDocument();
		$('#updateDocumentModal').modal('show');
	}

	var loadDocument = function () {

		var data = ProfileHandler.cacheData;

		const kycInfo = data?.sumsubAccountDocument
		const street = kycInfo?.['street'];
		const city = kycInfo?.['city'];
		const postalCode = kycInfo?.['postalCode'];
		const documentNo = kycInfo?.['documentNo'];
		const fullName = kycInfo?.['fullName'];
		const dob = kycInfo?.['dob'];
		const expiryDate = kycInfo?.["expiryDate"];
		const verificationRemark = kycInfo?.["approveRemark"];
		ProfileHandler.initCalendar(dob, expiryDate);

		var element = $('#updateDocumentModal').children().detach();


		ProfileHandler.viewDocumentAtEditDocument(element)
		element.find('#street').val(street);
		element.find('#city').val(city);
		element.find('#postalCode').val(postalCode);
		element.find('#editDocumentNo').val(documentNo);
		element.find('#editFullName').val(fullName);

		element.find('#verificationRemarkInput').val(verificationRemark);
		$('#updateDocumentModal').append(element);

	};

	ProfileHandler.previewImage = function (input, previewId) {

		const file = input.files[0];
		if (!file) {
			// User canceled - restore previous file if it exists
			if (ProfileHandler.cachedFiles[input.id]) {
				const dataTransfer = new DataTransfer();
				dataTransfer.items.add(ProfileHandler.cachedFiles[input.id]);
				input.files = dataTransfer.files;
			}
			return;
		}

		if (file.size > 50 * 1024 * 1024) {
			alert("File exceeds 50MB limit.");
			input.value = "";
			return;
		}

		const preview = document.getElementById(previewId);
		const reader = new FileReader();

		reader.onload = function (e) {
			let content = file.type.startsWith("image/")
				? `<img src="${e.target.result}"  style="max-width: 100%; max-height: 250px;">`
				: `<p>📄 ${file.name}</p>`;

			content += `<span class="clear-btn" onclick="event.stopPropagation(); ProfileHandler.clearImage('${input.id}', '${previewId}')">×</span>`;
			preview.innerHTML = content;
		};

		// Cache the file for potential restore on cancel
		ProfileHandler.cachedFiles[input.id] = file;
		ProfileHandler.uploadStatus[input.id] = true;
		reader.readAsDataURL(file);
		$(input).valid();
	}

	ProfileHandler.handleDrop = function (event, inputId, previewId) {
		event.preventDefault();
		const file = event.dataTransfer.files[0];
		const input = document.getElementById(inputId);


		input.files = event.dataTransfer.files;
		ProfileHandler.previewImage(input, previewId);
	}

	ProfileHandler.clearImage = function (inputId, previewId) {
		const input = document.getElementById(inputId);
		const preview = document.getElementById(previewId);

		// Properly clear the file input - set to empty DataTransfer
		input.value = "";
		try {
			const dataTransfer = new DataTransfer();
			input.files = dataTransfer.files; // Set to empty FileList
		} catch (e) {
			// Fallback for older browsers
			input.value = "";
		}

		// Determine if this is a change from original state:
		// - If originally had image → clearing is a change (delete) → uploadStatus = true
		// - If originally no image → clearing returns to original → uploadStatus = false
		if (ProfileHandler.originalFileState[inputId]) {
			// Originally had image, now clearing it = change
			ProfileHandler.uploadStatus[inputId] = true;
		} else {
			// Originally no image, clearing means back to original state = no change
			ProfileHandler.uploadStatus[inputId] = false;
		}

		// Clear cache to prevent restoring this file if user cancels next browse
		ProfileHandler.cachedFiles[inputId] = null;

		// Reset preview to empty state
		preview.innerHTML = `
		 <div style ="text-align: center">
			<i class="icon-picture" style="font-size: 50px;"></i>
		 	<p style="font-weight: bold">Drop image here or click to upload</p>
		 	<br/>
      		<p>File Type: JPG, PNG, HEIC, WEBP or PDF</p>
     	 	<p>Max:50MB</p>
		</div>

		 `;

		// Trigger validation to clear any error messages
		$(input).valid();

		console.log(`Cleared ${inputId}: originalState=${ProfileHandler.originalFileState[inputId]}, uploadStatus=${ProfileHandler.uploadStatus[inputId]}, files.length=${input.files.length}`);
	}

	ProfileHandler.uploadStatus = {
		frontPhotoFile: false,
		backPhotoFile: false,
		addressPhotoFile: false
	};

	// Cache to store previous files before opening file dialog
	ProfileHandler.cachedFiles = {
		frontPhotoFile: null,
		backPhotoFile: null,
		addressPhotoFile: null
	};

	// Track original state when modal opens (true = had image, false = no image)
	ProfileHandler.originalFileState = {
		frontPhotoFile: false,
		backPhotoFile: false,
		addressPhotoFile: false
	};

	function base64ToFile(base64, filename) {
		const arr = base64.split(',');
		const mime = arr[0].match(/:(.*?);/)[1];
		const bstr = atob(arr[1]);
		let n = bstr.length;
		const u8arr = new Uint8Array(n);
		while (n--) {
			u8arr[n] = bstr.charCodeAt(n);
		}
		return new File([u8arr], filename, {type: mime});
	}

	ProfileHandler.updateDocument = function () {

		console.log("Start: ", new Date().toLocaleString());

		const updateDocumentForm = $("[name='updateDocumentForm']");


		updateDocumentForm.removeData("validator")


		if (!updateDocumentForm.data("validator")) {
			updateDocumentForm.validate({
				rules: {
					frontPhotoFile: {
						required: true,
						fileTypeChecking: true
					},
					addressPhotoFile: {
						required: true,
						fileTypeChecking: true
					}
				},
				messages: {
					frontPhotoFile: {
						required: "This field is required.",
						fileTypeChecking: "Upload failed: Only JPG, PNG, HEIC, WEBP or PDF files under 50 MB are allowed."
					},
					addressPhotoFile: {
						required: "This field is required.",
						fileTypeChecking: "Upload failed: Only JPG, PNG, HEIC, WEBP or PDF files under 50 MB are allowed."
					}
				},
				ignore: [],
				errorPlacement: function (error, element) {
					if (element.attr("id") === "dob" || element.attr("id") === "expiryDate") {
						element.closest('.form-group')
							.find('.error-msg-calander-block')
							.html(error);
					} else if (element.attr("id") === "addressPhotoFile" || element.attr("id") === 'frontPhotoFile' || element.attr('id') === 'backPhotoFile') {
						element.closest('.form-group')
							.find('.error-msg-pictureUpload-block-' + element.attr("id"))
							.html(error);
					} else {
						error.insertAfter(element);
					}
				}

			});
			updateDocumentForm.find("[name='street']").rules('add', {
				required: true
			})
			updateDocumentForm.find("[name='city']").rules('add', {
				required: true
			})
			updateDocumentForm.find("[name='postalCode']").rules('add', {
				required: true
			})
			updateDocumentForm.find("[name='documentNo']").rules('add', {
				required: true
			})
			updateDocumentForm.find("[name='fullName']").rules('add', {
				required: true
			})
			updateDocumentForm.find("[name='dob']").rules('add', {
				required: true,
			})
			updateDocumentForm.find("[name='expiryDate']").rules('add', {
				required: true,
			})
		}


		console.log("end validating form: ", new Date().toLocaleString());
		if (!updateDocumentForm.valid()) {
			return;
		}


		console.log("converting to formData ", new Date().toLocaleString());

		var formData = new FormData(updateDocumentForm.get(0));
		const backInput = document.getElementById("backPhotoFile");

		console.log("Before cleanup - backPhotoFile:", {
			uploadStatus: ProfileHandler.uploadStatus.backPhotoFile,
			filesLength: backInput.files ? backInput.files.length : 0,
			hasFile: backInput.files && backInput.files.length > 0
		});

		// Remove empty file fields from FormData (when changed but no file)
		if ((!backInput.files || backInput.files.length === 0)) {
			console.log("Deleting backPhotoFile from FormData (cleared by user)");
			formData.delete("backPhotoFile");
		}

		// Always append change flags (regardless of file deletion)
		formData.append("frontPhotoChanged", ProfileHandler.uploadStatus.frontPhotoFile)
		formData.append("backPhotoChanged", ProfileHandler.uploadStatus.backPhotoFile)
		formData.append("addressImageChanged", ProfileHandler.uploadStatus.addressPhotoFile)


		console.log("call api:  ", new Date().toLocaleString());
		$.ajax({
			type: "POST",
			url: '/manager/member/editIdDocument',
			data: formData,
			processData: false,
			contentType: false,
			cache: false,
			enctype: "multipart/form-data",
			success: function (responseText) {
				console.log("return success api:  ", new Date().toLocaleString());

				if (responseText.errors != null) {
					var errors = '';
					for (var i = 0; i < responseText.errors.length; i++) {
						errors += responseText.errors[i] + '<br>';
					}
					NotifyHandler.errorMsg(errors);

				} else if (responseText.error) {
					NotifyHandler.errorMsg(responseText.error);
				} else {
					console.log("running success else block: ", new Date().toLocaleString());
					NotifyHandler.successMsg("Update Success");
					$('#updateDocumentModal').modal('hide');

					ProfileHandler.getProfileOverview();
					ProfileHandler.uploadStatus = {
						frontPhotoFile: false,
						backPhotoFile: false,
						addressPhotoFile: false
					}

					const validator = updateDocumentForm.data("validator");
					if (validator) validator.resetForm();
					updateDocumentForm.removeData("validator");
					updateDocumentForm[0].reset();
					LogUtil.clearData();
				}

				console.log("end succuss api:  ", new Date().toLocaleString());
			},
			error: function (jqXHR, textStatus, errorThrown) {
				var errorMsg = '';
				if (jqXHR.status === 413) {
					errorMsg = "File cannot exceed 50MB";
				} else {
					try {
						var response = JSON.parse(jqXHR.responseText);
						errorMsg = response.error || response.message || errorThrown;
					} catch (e) {
						errorMsg = jqXHR.responseText || errorThrown || textStatus || "An error occurred";
					}
				}

				NotifyHandler.errorMsg(errorMsg);
			}
		});

		console.log("end:  ", new Date().toLocaleString());
	}


	ProfileHandler.loadBase64Preview = function (dataImages, element = $(document)) {
		const keys = {
			front: "frontPreview",
			back: "backPreview",
			residence: "addressPreview"
		};

		const previewIds = {
			frontPreview: "frontPhotoFile",
			backPreview: "backPhotoFile",
			addressPreview: "addressPhotoFile",
		}

		for (const key in keys) {
			const base64 = dataImages?.[key];
			const previewId = keys[key];
			const inputId = previewIds[previewId]
			if (base64) {
				element.find(`#${previewId}`).html(`
                <img src="data:image/png;base64,${base64}" style="max-width: 100%; max-height: 250px;">
                <span class="clear-btn" onclick="event.stopPropagation(); ProfileHandler.clearImage('${inputId}', '${previewId}')">×</span>`);

				const file = base64ToFile(`data:image/png;base64,${base64}`, `${key}.png`);
				const dataTransfer = new DataTransfer();
				dataTransfer.items.add(file);
				document.getElementById(inputId).files = dataTransfer.files;

				// Cache the file so it can be restored if user cancels
				ProfileHandler.cachedFiles[inputId] = file;
				// Mark that this field originally had an image
				ProfileHandler.originalFileState[inputId] = true;
			} else {
				ProfileHandler.clearImage(inputId, previewId);
				// Mark that this field originally had NO image
				ProfileHandler.originalFileState[inputId] = false;
			}
		}
	};


	ProfileHandler.viewDocumentAtEditDocument = function (element) {

		$.ajax({
			type: "POST",
			url: '/manager/member/viewSumsubDocument',
			data: {
				userId: PageConfig.userId,
				// documentId: documentId,
			},
			success: function (data) {

				if (data.error) {
					NotifyHandler.errorMsg(data.error);
					return;
				}
				const dataImages = data.image;
				ProfileHandler.loadBase64Preview(dataImages, element);
			}
		});
	}

	ProfileHandler.viewSumsubDocument = function (e, photoType) {
		const $viewDocumentPhoto = $('#viewDocumentPhoto');
		const $photo = $viewDocumentPhoto.find('[name=photo]');


		const documentId = $("[name='documentId']").attr("data-id");

		$.ajax({
			type: "POST",
			url: '/manager/member/viewSumsubDocument',
			data: {
				userId: PageConfig.userId,
				documentId: documentId,
			},
			success: function (data) {

				if (data.error) {
					NotifyHandler.errorMsg(data.error);
					return;
				}

				const dataImages = data.image;

				let imageUrl = null;

				if (photoType === 'front') {
					imageUrl = dataImages?.['front'];
				} else if (photoType === 'back') {
					imageUrl = dataImages?.['back'];
				} else if (photoType === 'residence') {
					imageUrl = dataImages?.['residence'];
				}

				if (imageUrl) {
					$photo.empty();
					const image = new Image();
					image.src = 'data:image/png;base64,' + imageUrl;
					$photo.append(image);
				} else {
					$photo.empty();
					$photo.append(`<span style="display: flex; justify-content: center">${I18N.get("msg.info.profile.noDocumentImage")}</span>`);
				}

				$viewDocumentPhoto.modal('show');
			}
		});
	};


	ProfileHandler.resetEditDocument = function () {
		var updateDocumentForm = $("[name='updateDocumentForm']");
		updateDocumentForm.get(0).reset();
		// updateGeneralForm.data("validator").resetForm();
		loadDocument();
	};


	ProfileHandler.openUpdateKycStatusModal = function () {

		let updateKycStatusModal = $('#updateKycStatusModal');
		updateKycStatusModal.validate({
			onfocusout: false
		});
		loadKycStatus();
		$('#updateKycStatusModal').modal('show');


	}

	ProfileHandler.resetKycStatus = function () {

		let updateKycStatusForm = $("[name='updateKycStatusForm']");
		updateKycStatusForm.get(0).reset();
		loadKycStatus();
	};

	let loadKycStatus = function () {
		let updateKycStatusModal = $('#updateKycStatusModal');
		let element = updateKycStatusModal.children().detach();

		let kycStatus = ProfileHandler.cacheData?.sumsubAccountDocument?.accountDocumentStatusType ?? "0"
		element.find("[name='kycStatus']").val(kycStatus);
		$.uniform.update();//Chrome、IE要這樣
		updateKycStatusModal.append(element);
	}


	ProfileHandler.updateKycStatus = function () {
		let updateKycStatusForm = $("[name='updateKycStatusForm']");

		updateKycStatusForm.find('[name=save]').addClass('disabled');
		updateKycStatusForm.find('[name=resetButton]').addClass('disabled');


		$.ajax({
			type: "POST",
			url: '/manager/member/updateKycStatus',
			dataType: 'JSON',
			data: updateKycStatusForm.serialize(),
			success: function (data) {
				if (data.error) {
					NotifyHandler.errorMsg(data.error);
					return;
				}
				NotifyHandler.successMsg(data.message);
				$('#updateKycStatusModal').modal('hide');
				ProfileHandler.getProfileOverview();
			},
			complete: function () {
				updateKycStatusForm.find('[name=save]').removeClass('disabled');
				updateKycStatusForm.find('[name=resetButton]').removeClass('disabled');
			}
		});
	};

	ProfileHandler.openRemoveAccountBankModal = function (cardNumber, bankId, bankName) {
		var modal = $('#removeAccountBankModal');
		modal.find('#title').text(I18N.get("form.text.account.accountBank.remove_?", [bankName, formatAccountNumber(cardNumber)]));
		modal.find("[name='bankId']").val(bankId);
		modal.modal('show');
	}

	ProfileHandler.removeAccountBank = function () {
		var modal = $('#removeAccountBankModal');
		const bankId = modal.find("[name='bankId']").val()


		$.ajax({
			type: "POST",
			url: '/manager/payment/deleteAccountBank',
			data: {
				bankId,
				"userId": PageConfig.userId
			},
			success: function (data) {

				if (data.error) {
					NotifyHandler.errorMsg(data.error);
					return;
				}

				NotifyHandler.successMsg(data.message);
				$('#removeAccountBankModal').modal('hide');
				ProfileHandler.getProfileOverview();
			}
		});
	};

	let formatCardNumber = function (cardNumber, chunkSize = 4, separator = ' ') {
		return cardNumber.replace(new RegExp(`(.{${chunkSize}})`, 'g'), `$1${separator}`).trim();
	}


	function validateRequirement(id, isValid) {
		var $element = $('#' + id);
		var $icon = $element.find('i');

		console.log(id, isValid)

		if (isValid) {
			$element.addClass('valid').removeClass('invalid');
			$icon.removeClass('icon-circle-blank icon-remove').addClass('icon-ok-circle').css('color', '#5cb85c');
			;
		} else {
			$element.addClass('invalid').removeClass('valid');
			$icon.removeClass('icon-ok-circle icon-remove').addClass('icon-circle-blank').css('color', "#666");
		}
	}

	function requirementBoxOnChange() {
		var $passwordInput = $('#editPassword');
		var $requirementsBox = $('#passwordRequirements');
		var allRequirementsMet = false;

		// 点击 input 时，如果不满足条件则显示 requirements box
		$passwordInput.on('focus', function () {
			if (!allRequirementsMet) {
				$requirementsBox.slideDown(200);
			}
		});

		$passwordInput.on('input', function () {
			var password = $(this).val();

			var lengthValid = password.length >= 6 && password.length <= 20;
			var uppercaseValid = /[A-Z]/.test(password);
			var lowercaseValid = /[a-z]/.test(password);
			var digitValid = /[0-9]/.test(password);
			var symbolValid = /[@$!%*#]/.test(password);
			// var spaceValid = /\s/.test(password);

			validateRequirement('req-length', lengthValid);
			validateRequirement('req-uppercase', uppercaseValid);
			validateRequirement('req-lowercase', lowercaseValid);
			validateRequirement('req-digit', digitValid);
			validateRequirement('req-symbol', symbolValid);

			// 检查是否所有条件都满足
			allRequirementsMet = lengthValid && uppercaseValid && lowercaseValid && digitValid && symbolValid;

			var noSpaceAllow = !/\s/.test(password);
			var noUniCodeAllow = !/[^\x00-\x7F]/.test(password)

			console.log("all Requirement met", allRequirementsMet);
			if (allRequirementsMet || !noSpaceAllow || !noUniCodeAllow) {
				$requirementsBox.slideUp(200);
			} else {
				$requirementsBox.slideDown(200);
			}
		});
	}

	function resetPasswordForm() {
		$('#editPassword').val('');
		$('#confirmPassword').val('');

		// 隐藏 requirements box
		$('#passwordRequirements').hide();

		// 重置所有验证状态
		$('#passwordRequirements li').removeClass('valid invalid');
		$('#passwordRequirements li i')
			.removeClass('icon-ok-circle')
			.addClass('icon-circle-blank')
			.removeAttr('style'); // 移除内联样式

		// 清空错误信息
		$('.error-msg-block').text('');
	}


	function formatAccountNumber(accountNumber) {
		return accountNumber
			.replace(/\s/g, '')
			.replace(/(.{4})/g, '$1 ')
			.trim();
	}

	ProfileHandler.openAddBankModal = function () {
		var modal = $('#createBankModal');
		ProfileHandler.reset();
		modal.modal('show');
	}

	ProfileHandler.reset = function () {
		var form = $('#createBankModalForm');
		form[0].reset();

		// Reset validation if exists
		var validator = form.data('validator');
		if (validator) {
			validator.resetForm();
		}

		// Reset uniform elements if used
		if (typeof $.uniform !== 'undefined') {
			$.uniform.update();
		}
	};

	ProfileHandler.close = function () {
		$('#createBankModal').modal('hide');
	};

	ProfileHandler.save = function () {
		var form = $('#createBankModalForm');

		const bankId = form.find('[name=bankId]').val();
		const accountNumber = form.find('[name=accountNumber]').val();


		if (!form.data("validator")) {
			form.validate({
				rules: {
					bankId: {
						required: true
					},
					accountNumber: {
						required: true,
						cleanedDigits: true,
						cleanedMinLength: 8,
						cleanedMaxLength: 34
					}
				},
			});
		}

		// Validate form
		if (!form.valid()) {
			return;
		}


		// Validate required fields
		if (!bankId) {
			NotifyHandler.errorMsg("Bank is required");
			return;
		}

		if (!accountNumber) {
			NotifyHandler.errorMsg("Account Number is required");
			return;
		}

		$.ajax({
			type: "POST",
			url: '/manager/payment/addAccountBank',
			dataType: 'JSON',
			data: {
				userId: PageConfig.userId,
				bankId: parseInt(bankId),
				bankAccountNumber: accountNumber.replace(/\s/g, '').toString()
			},
			success: function (data) {
				if (data.error) {
					NotifyHandler.errorMsg(data.error);
					return;
				}

				NotifyHandler.successMsg(data.message);
				ProfileHandler.close();
				if (typeof ProfileHandler !== 'undefined' && ProfileHandler.getProfileOverview) {
					ProfileHandler.getProfileOverview();
				}
			},
			error: function (xhr, status, error) {
				NotifyHandler.errorMsg("Failed to create bank account: " + error);
			},
			beforeSend: function () {
				form.find('[name=save]').addClass('disabled');
				form.find('[name=resetButton]').addClass('disabled');
			},
			complete: function () {
				form.find('[name=save]').removeClass('disabled');
				form.find('[name=resetButton]').removeClass('disabled');
			}
		});
	};


	ProfileHandler.onInputAccountNumberFormat = function (event) {
		const input = event.target;
		const cleaned = input.value.replace(/\s/g, '');
		const limited = cleaned.substring(0, 34);
		input.value = limited.replace(/(.{4})/g, '$1 ').trim();
	}
	ProfileHandler.removeAccountCard = function() {

		$.ajax({
			type: "POST",
			url: '/manager/member/removeAccountCard',
			data: {
				userId: PageConfig.userId,
				currencyTypeId: PageConfig.currency,
			},
			success: function(data) {

				if (data.error) {
					NotifyHandler.errorMsg(data.error);
					return;
				}

				NotifyHandler.successMsg(data.message);
				$('#removeAccountCardModal').modal('hide');
				ProfileHandler.getProfileOverview();
			}
		});
	};

})();




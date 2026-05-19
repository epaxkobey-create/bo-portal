<%@ page import="com.nv.commons.utils.FrontendUtils" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<!-- App -->
<script type="text/javascript" src="/js/manager/app.js?v=<%=FrontendUtils.getJsFileVersion()%>"></script>
<script type="text/javascript" src="/js/manager/plugins.js?v=<%=FrontendUtils.getJsFileVersion()%>"></script>
<script type="text/javascript"
	src="/js/manager/plugins.form-components.js?v=<%=FrontendUtils.getJsFileVersion()%>"></script>
<!-- FormComponents -->
<script type="text/javascript">
	// Fix for class_selector in BS3 plugins.form-components.js
	if ($.validator) {
		var _base_resetForm = $.validator.prototype.resetForm;
		$.extend($.validator.prototype, {
			resetForm: function() {
				var resetForm_this = this;
				_base_resetForm.call(this);

				var currentForm = $(this.currentForm);
				var class_selector = ".form-group";
				if (currentForm.hasClass('form-vertical')) {
					class_selector = "*[class^=col-]";
				}

				currentForm.find(class_selector).each(function() {
					$(this).removeClass(resetForm_this.settings.errorClass + ' ' + resetForm_this.settings.validClass);
				});
				currentForm.find('.select2-container').removeClass(resetForm_this.settings.errorClass + ' ' + resetForm_this.settings.validClass);

				currentForm.find('label[generated="true"]').html('');
			}
		});
	}
</script>

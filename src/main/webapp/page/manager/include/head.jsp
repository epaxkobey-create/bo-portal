<%@ page contentType="text/html;charset=UTF-8" language="java"
	import="com.nv.commons.constants.CurrencyType"
%>
<!-- Breadcrumbs line -->
<div class="crumbs">
	<ul id="breadcrumbs" class="breadcrumb">
	</ul>
	<ul id="currencycrumbs" class="crumb-buttons">
		<li class="range"><a href="javascript:void(0)">
			<span style="margin-right:5px; display:inline-block;"><%=commonLangMessage.get(
				"form.text.backOffice.currencyType")%></span>
			<span style="display:inline-block;">
      			<select class="form-control" id="dashboardCurrencyType" name="dashboardCurrencyType"
					onchange="TopHandler.changeCurrency()">
					<%
						for (CurrencyType type : CurrencyType.VALUES) {
					%>
					<option value="<%=type.unique()%>" <%=(commonCurrencyType != null
						&&commonCurrencyType.unique() == type.unique()) ? "selected" : ""%>><%=type.getFullName(
						commonLangMessage)%></option>
					<%
						}
					%>
				</select>
			</span>
		</a>
		</li>
	</ul>
</div>
<!-- /Breadcrumbs line -->

<!--=== Page Header ===-->
<div class="page-header">
	<div class="page-title">
		<h3></h3>
		<span></span>
	</div>
</div>
<!-- /Page Header -->
<ul style="display:none">
	<li id="headTemplate">
		<i></i>
		<a></a>
	</li>
</ul>
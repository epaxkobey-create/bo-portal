<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
	// 接收從父頁面傳入的標題參數
	String documentPhotoTitle = (String) request.getAttribute("documentPhotoTitle");
	if (documentPhotoTitle == null || documentPhotoTitle.isEmpty()) {
		documentPhotoTitle = "Document"; // 預設值
	}
%>
<div class="modal fade" id="viewDocumentPhoto" role="dialog">
	<div class="modal-dialog" style="width: 70%;text-align: center;">
		<div class="modal-content">
			<div class="modal-header">
				<button type="button" class="close" data-dismiss="modal">&times;</button>
				<h4 class="modal-title" id="modalTitle" style="text-align: start">
					<%=documentPhotoTitle%>
				</h4>
			</div>
			<div class="modal-body">
				<div class="row" style="display: flex; justify-content: center;">
					<div class="col-md-10" name="photo"></div>
				</div>
			</div>
		</div>
	</div>
</div>

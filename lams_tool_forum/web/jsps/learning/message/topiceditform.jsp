<table cellpadding="0">
	<tr>
		<td class="field-name">
			<bean:message key="message.label.subject" />
			*
		</td>
		<td>
			<html:text size="30" tabindex="1" property="message.subject" />
			<html:errors property="message.subject" />
		</td>
	</tr>
	<tr>
		<td class="field-name">
			<bean:message key="message.label.body" />
			*
		</td>
		<td>
			<%@include file="bodyarea.jsp"%>
		</td>
	</tr>
	<c:if test="${topic.hasAttachment || allowUpload}">
		<tr>
			<td class="field-name">
				<bean:message key="message.label.attachment" />
			</td>
			<td>
				<c:if test="${topic.hasAttachment}">
					<c:forEach var="file" items="${topic.message.attachments}">
						<c:set var="downloadURL">
							<html:rewrite page="/download/?uuid=${file.fileUuid}&versionID=${file.fileVersionId}&preferDownload=true" />
						</c:set>
						<a href="<c:out value='${downloadURL}' escapeXml='false'/>"> <c:out value="${file.fileName}" /> </a>
						<c:set var="deleteURL">
							<html:rewrite page="/learning/deleteAttachment.do?uuid=${file.fileUuid}&versionID=${file.fileVersionId}" />
						</c:set>
						&nbsp;
						<a href="<c:out value='${deleteURL}'/>"> <fmt:message key="label.delete" /> </a>
					</c:forEach>
				</c:if>
				<c:if test="${not topic.hasAttachment && allowUpload}">
					<html:file tabindex="3" property="attachmentFile" />
					<html:errors property="message.attachment" />
				</c:if>
			</td>
		</tr>
	</c:if>
	<tr>
		<td></td>
		<td>
			<html:submit styleClass="button">
				<bean:message key="button.submit" />
			</html:submit>
			<c:set var="backToTopic">
				<html:rewrite page="/learning/viewTopic.do?topicId=${rootUid}&create=${topic.message.created.time}" />
			</c:set>
			<html:button property="goback" onclick="javascript:location.href='${backToTopic}';" styleClass="button">
				<bean:message key="button.cancel" />
			</html:button>
		</td>
	</tr>
</table>

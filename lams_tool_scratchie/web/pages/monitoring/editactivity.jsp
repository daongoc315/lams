<%@ include file="/common/taglibs.jsp"%>
<c:set var="sessionMap" value="${sessionScope[sessionMapID]}"/>
<c:set var="scratchie" value="${sessionMap.scratchie}"/>

<table class="table table-condensed">
	<tr>
		<td width="10%" nowrap>
			<fmt:message key="label.authoring.basic.title" />
			:
		</td>
		<td>
			<c:out value="${scratchie.title}" escapeXml="true" />
		</td>
	</tr>

	<tr>
		<td width="10%" valign="true" nowrap>
			<fmt:message key="label.authoring.basic.instruction" />
			:
		</td>
		<td>
			<c:out value="${scratchie.instructions}" escapeXml="false" />
		</td>
	</tr>
</table>

<c:url  var="authoringUrl" value="/definelater.do">
	<c:param name="toolContentID" value="${sessionMap.toolContentID}" />
	<c:param name="contentFolderID" value="${sessionMap.contentFolderID}" />
</c:url>
<html:link href="javascript:;" onclick="launchPopup('${authoringUrl}','definelater')" styleClass="btn btn-default">
	<fmt:message key="label.monitoring.edit.activity.edit" />
</html:link>

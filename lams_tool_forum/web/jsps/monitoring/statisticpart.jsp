<%@ include file="/common/taglibs.jsp"%>

<%-- If you change this file, remember to update the copy made for CNG-12 --%>

<c:forEach var="element" items="${topicList}">
	<c:set var="toolSessionDto" value="${element.key}" />
	<c:set var="sessionTopicList" value="${element.value}" />
	<c:forEach var="totalMsg" items="${totalMessage}">
		<c:if test="${totalMsg.key eq toolSessionDto.sessionID}">
			<c:set var="sessionTotalMessage" value="${totalMsg.value}" />
		</c:if>
	</c:forEach>
	<c:forEach var="avaMark" items="${markAverage}">
		<c:if test="${avaMark.key eq toolSessionDto.sessionID}">
			<c:set var="sessionMarkAverage" value="${avaMark.value}" />
		</c:if>
	</c:forEach>

	<h2>
		<fmt:message key="message.session.name" />: <c:out value="${toolSessionDto.sessionName}" />
	</h2>
	
	<table cellpadding="0" class="alternative-color">
		<tr>
			<th scope="col" width="50%">
				<fmt:message key="lable.topic.title.subject" />
			</th>
			<th scope="col" width="25%">
				<fmt:message key="lable.topic.title.message.number" />
			</th>
			<th scope="col" width="25%">
				<fmt:message key="lable.topic.title.average.mark" />
			</th>
		</tr>
		<c:forEach items="${sessionTopicList}" var="topic">
			<tr>
				<td valign="MIDDLE" width="48%">
					<c:set var="viewtopic">
						<html:rewrite page="/monitoring/viewTopicTree.do?topicID=${topic.message.uid}&create=${topic.message.created.time}" />
					</c:set>
					<html:link href="javascript:launchPopup('${viewtopic}');">
						<c:out value="${topic.message.subject}" />
					</html:link>
				</td>
				<td>
					<c:out value="${topic.message.replyNumber+1}" />
				</td>
				<td>
					<fmt:formatNumber value="${topic.mark}"  maxFractionDigits="2"/>
				</td>
			</tr>
		</c:forEach>
	</table>

	<table cellpadding="0" style="padding-left: 20px;">
		<tr>
			<td class="field-name" width="30%">
				<fmt:message key="lable.monitoring.statistic.total.message" />
			</td>
			<td>
				<c:out value="${sessionTotalMessage}" />
			</td>
			
		</tr>
		<tr>
			<td class="field-name" width="30%">
				<fmt:message key="label.monitoring.statistic.average.mark" />
			</td>
			
			<td>
				<fmt:formatNumber value="${sessionMarkAverage}"  maxFractionDigits="2"/>
			</td>
		</tr>
	</table>
</c:forEach>

<%@ include file="/common/taglibs.jsp"%>

<c:if
	test="${chatUserDTO.finishedActivity and chatDTO.reflectOnActivity}">
	<div class="space-top">
		<h4>
			${chatDTO.reflectInstructions}
		</h4>
		<p>
			${chatUserDTO.notebookEntry}
		</p>
	</div>
</c:if>

<html:form action="/learning" method="post">
	<html:hidden property="chatUserUID" value="${chatUserDTO.uid}" />
	<div class="space-bottom-top align-right">
		<c:choose>
			<c:when
				test="${!chatUserDTO.finishedActivity and chatDTO.reflectOnActivity}">
				<html:hidden property="dispatch" value="openNotebook" />

				<html:submit styleClass="button">
					<fmt:message key="button.continue" />
				</html:submit>

			</c:when>
			<c:otherwise>
				<html:hidden property="dispatch" value="finishActivity" />
				<html:submit styleClass="button">
					<fmt:message key="button.finish" />
				</html:submit>
			</c:otherwise>
		</c:choose>
	</div>
</html:form>

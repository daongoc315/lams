<%@ include file="/common/taglibs.jsp" %>
<html>
<head>
    <%@ include file="/common/header.jsp" %>	
    <%-- user for  rsrcresourceitem.js --%>
	<script type="text/javascript">
	   var removeInstructionUrl = "<c:url value='/authoring/removeInstruction.do'/>";
       var addInstructionUrl = "<c:url value='/authoring/newInstruction.do'/>";
	</script>
	<script type="text/javascript" src="<html:rewrite page='/includes/javascript/rsrcresourceitem.js'/>"></script>

</head>
<body class="tabpart">
	<table class="forms">
		<!-- Basic Info Form-->
		<tr>
			<td>
			<%@ include file="/common/messages.jsp" %>
			<html:form action="/authoring/saveOrUpdateItem" method="post" styleId="resourceItemForm">
				<input type="hidden" name="instructionList" id="instructionList"/>
				<input type="hidden" name="itemType" id="itemType" value="1"/>
				<html:hidden property="itemIndex"/>
				<table class="innerforms">
					<tr>
						<td colspan="2"><h2><fmt:message key="label.authoring.basic.add.url"/></h2></td>
					</tr>
					<tr>
						<td width="130px"><fmt:message key="label.authoring.basic.resource.title.input"/></td>
						<td><html:text property="title" size="55"/></td>
					</tr>
					<tr>
						<td width="130px"><fmt:message key="label.authoring.basic.resource.description.input"/></td>
						<td><lams:STRUTS-textarea rows="5" cols="55" property="description"/> </td>
					</tr>						
					<tr>
						<td width="130px"><fmt:message key="label.authoring.basic.resource.url.input"/></td>
						<td><html:text property="url" size="55"/>
							<html:checkbox property="openUrlNewWindow"><fmt:message key="open.in.new.window"/></html:checkbox>
						</td>
					</tr>	
				</table>
			</html:form>
			</td>
		</tr>
		<tr>
			<td><hr></td>
		</tr>
		<tr>
			
			<!-- Instructions -->
			<td>
				<table class="innerforms">
					<tr>
						<td>
							<%@ include file="instructions.jsp" %>
						</td>
						<td width="100px" align="right" valign="bottom">
							<input onclick="cancelResourceItem()" type="button" name="cancel" value="<fmt:message key="label.cancel"/>" class="buttonStyle">
						</td>						
						<td width="150px" align="right" valign="bottom">
							<input onclick="submitResourceItem()" type="button" name="add" value="<fmt:message key="label.authoring.basic.add.url"/>" class="buttonStyle">
						</td>
					</tr>
				</table>
			</td>
		</tr>
	</table>

</body>
</html>
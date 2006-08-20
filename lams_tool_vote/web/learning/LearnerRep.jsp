<%-- 
Copyright (C) 2005 LAMS Foundation (http://lamsfoundation.org)
License Information: http://lamsfoundation.org/licensing/lams/2.0/

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License version 2 as 
  published by the Free Software Foundation.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
  USA

  http://www.gnu.org/licenses/gpl.txt
--%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN"
        "http://www.w3.org/TR/html4/strict.dtd">

<%@ include file="/common/taglibs.jsp"%>

<c:set scope="request" var="lams">
	<lams:LAMSURL />
</c:set>
<c:set scope="request" var="tool">
	<lams:WebAppURL />
</c:set>

<html:html>
<head>
	<html:base />
	<lams:headItems />
	<title><bean:message key="activity.title" /></title>

	<script language="JavaScript" type="text/JavaScript">

		function submitMonitoringMethod(actionMethod) 
		{
			document.VoteMonitoringForm.method.value=actionMethod; 
			document.VoteMonitoringForm.submit();
		}
		
		function submitMethod(actionMethod) 
		{
			submitMonitoringMethod(actionMethod);
		}

	</script>		
</head>

<body>
	<div id="page-learner">
	
	<h1 class="no-tabs-below">
		<c:out value="${voteGeneralLearnerFlowDTO.activityTitle}" escapeXml="false" />
	</h1>

	<div id="header-no-tabs-learner"></div>
	

	<div id="content-learner">


		<table>
				<tr>
			 		<th NOWRAP>  <bean:message key="label.learner.progress"/>   </th>
				</tr>
		</table>

		<c:set scope="request" var="monitoringURL">
			<html:rewrite page="/monitoring.do" />
		</c:set>

	  <html:form  action="/monitoring?validate=false" enctype="multipart/form-data" method="POST" target="_self">		
		<html:hidden property="method"/>	 

				<table>
					<tr>
						<td NOWRAP valign=top align=left>
			 			<table align=left>
			 			
						<c:forEach var="currentDto" items="${voteGeneralLearnerFlowDTO.listMonitoredAnswersContainerDto}">
				  	 		<c:set var="currentQuestionId" scope="request" value="${currentDto.questionUid}"/>
				  	 		<tr>
				  	 			<td> &nbsp</td>
				  	 		</tr>
							<tr>			
								<td NOWRAP valign=top align=left><b>  <bean:message key="label.nomination"/>: </b>
									<c:out value="${currentDto.question}" escapeXml="false"/>
								 </td>
							</tr>	
							
							<tr> 
								<td NOWRAP valign=top>
									<table align=center>
										<tr> 
											 <td NOWRAP valign=top> <b>  <bean:message key="label.user"/>  </b> </td>  
					  						 <td NOWRAP valign=top> <b>  <bean:message key="label.attemptTime"/> </b></td>
							  			</tr>				 
			  							<c:forEach var="questionAttemptData" items="${currentDto.questionAttempts}">
											<c:forEach var="sData" items="${questionAttemptData.value}">
									  	 		<c:set var="userData" scope="request" value="${sData.value}"/>
									  	 		<c:set var="responseUid" scope="request" value="${userData.uid}"/>
	
		  	 									<c:if test="${currentQuestionId == userData.questionUid}">
			  	 									<c:if test="${sessionScope.currentMonitoredToolSession == 'All'}"> 			
														<tr> 
																 <td NOWRAP valign=top>   <c:out value="${userData.userName}"/>   </td>  
																 <td NOWRAP valign=top>   <c:out value="${userData.attemptTime}"/>  </td>
														</tr>		
													</c:if>														  					 									  			
													
			  	 									<c:if test="${sessionScope.currentMonitoredToolSession != 'All'}"> 			
			  	 										<c:if test="${sessionScope.currentMonitoredToolSession == userData.sessionId}"> 			
															<tr> 
																	 <td NOWRAP valign=top>   <c:out value="${userData.userName}"/>   </td>  
																	 <td NOWRAP valign=top>   <c:out value="${userData.attemptTime}"/>  </td>
															</tr>		
														</c:if>														  					 									  													  			
													</c:if>														  					 									  													  			
												</c:if>														  					 
		 									</c:forEach>		  	
										</c:forEach>		  	
									</table>
								</td>  
				  			</tr>
						</c:forEach>		  
							
						
					<c:forEach var="currentDto" items="${listUserEntries.listUserEntries}">
				  	 		<c:set var="currentQuestionId" scope="request" value="${currentDto.questionUid}"/>
				  	 		<tr>
				  	 			<td> &nbsp&nbsp</td>
				  	 		</tr>
							<tr>			
								<td NOWRAP valign=top align=left><b>  <bean:message key="label.nomination"/>: </b>
									<c:out value="${currentDto.question}" escapeXml="false"/>
								 </td>
							</tr>	
							
							<tr> 
								<td NOWRAP  valign=top>
									<table align=center>
										<tr> 
											 <td NOWRAP valign=top> <b>  <bean:message key="label.user"/>  </b> </td>  
					  						 <td NOWRAP valign=top> <b>  <bean:message key="label.attemptTime"/> </b></td>
							  			</tr>				 
	
			  							<c:forEach var="questionAttemptData" items="${currentDto.questionAttempts}">
									  	 		<c:set var="userData" scope="request" value="${questionAttemptData.value}"/>
												<tr> 
														 <td NOWRAP valign=top>   <c:out value="${userData.userName}"/>   </td>  
														 <td NOWRAP valign=top>   <c:out value="${userData.attemptTime}"/>  </td>
												</tr>		
										</c:forEach>		  	
							  			
									</table>
								</td>  
				  			</tr>
					</c:forEach>		  	

					</table>
			 		</td>
				</tr>
			</table>
	</html:form>

	</div>
	

<div id="footer-learner"></div>	

</div>
</body>
</html:html>









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


<%@ taglib uri="tags-bean" prefix="bean"%> 
<%@ taglib uri="tags-html" prefix="html"%>
<%@ taglib uri="tags-logic" prefix="logic" %>
<%@ taglib uri="tags-logic-el" prefix="logic-el" %>
<%@ taglib uri="tags-core" prefix="c"%>
<%@ taglib uri="tags-fmt" prefix="fmt" %>
<%@ taglib uri="fck-editor" prefix="FCK" %>
<%@ taglib uri="tags-lams" prefix="lams" %>

<c:set scope="request" var="lams"><lams:LAMSURL/></c:set>
<c:set scope="request" var="tool"><lams:WebAppURL/></c:set>

				<table>
				<tr> 
					<td NOWRAP colspan=4 valign=top>
						<lams:SetEditor id="richTextOnlineInstructions" text="${voteGeneralAuthoringDTO.richTextOnlineInstructions}" small="true" key="label.onlineInstructions.col"/>					
					</td> 
				</tr>
				
				<tr>
          			
					<td NOWRAP colspan=4 align=left valign=top width="100%">
						<table align="left" >
									<c:forEach var='file' items='${voteGeneralAuthoringDTO.listOnlineFilesMetadata}'>
											<tr>
												<td NOWRAP valign=top>
													 <c:out value="${file.filename}"/> 
													</td>
													<td NOWRAP valign=top>												
													<c:set scope="request" var="viewURL">
														<html:rewrite page="/download/?uuid=${file.uuid}&preferDownload=false"/>
													</c:set>
													<a href="javascript:launchInstructionsPopup('<c:out value='${viewURL}' escapeXml='false'/>')">
														 	<bean:message key="label.view"/> 
													</a>
													</td>
													<td NOWRAP valign=top>													
													<c:set scope="request" var="downloadURL">
															<html:rewrite page="/download/?uuid=${file.uuid}&preferDownload=true"/>
													</c:set>
													<a href="<c:out value='${downloadURL}' escapeXml='false'/>">
														 <bean:message key="label.download"/> 
													</a>
													</td>
													<td NOWRAP valign=top>

													 <img src="<c:out value="${tool}"/>images/delete.gif" align=left onclick="javascript:submitDeleteFile('<c:out value="${file.uuid}"/>','deleteOnlineFile');"> 	
												</td>
											</tr>
				         			</c:forEach>
	         			</table>
					</td> 
				</tr>

				<tr> 
					<td>
    	      				<b> <bean:message key="label.onlineFiles" />  </b>
          			</td>
          			<td colspan=3 NOWRAP valign=top> 
							<html:file  property="theOnlineFile"></html:file>
						 	<html:submit onclick="javascript:submitMethod('submitOnlineFiles');" styleClass="buttonLeft">
								 <bean:message key="button.upload"/> 
							</html:submit>
					</td> 
				</tr>

				
				<tr> 
					<td NOWRAP colspan=4 valign=top>
						<lams:SetEditor id="richTextOfflineInstructions" text="${voteGeneralAuthoringDTO.richTextOfflineInstructions}" small="true" key="label.offlineInstructions.col"/>																			
					</td> 
				</tr>
				

				<tr> 
					<td NOWRAP colspan=4 align=left valign=top width="100%">
						<table align="left">
									<c:forEach var='file' items='${voteGeneralAuthoringDTO.listOfflineFilesMetadata}'>
											<tr>
												<td NOWRAP valign=top>
													 <c:out value="${file.filename}"/> 
													</td>
													<td NOWRAP valign=top>												
													<c:set scope="request" var="viewURL">
														<html:rewrite page="/download/?uuid=${file.uuid}&preferDownload=false"/>
													</c:set>
													<a href="javascript:launchInstructionsPopup('<c:out value='${viewURL}' escapeXml='false'/>')">
														 	<bean:message key="label.view"/> 
													</a>
													</td>
													<td NOWRAP valign=top>													
													<c:set scope="request" var="downloadURL">
															<html:rewrite page="/download/?uuid=${file.uuid}&preferDownload=true"/>
													</c:set>
													<a href="<c:out value='${downloadURL}' escapeXml='false'/>">
														 <bean:message key="label.download"/> 
													</a>
													</td>
													<td NOWRAP valign=top>

													 <img src="<c:out value="${tool}"/>images/delete.gif" align=left onclick="javascript:submitDeleteFile('<c:out value="${file.uuid}"/>','deleteOfflineFile');"> 	
												</td>
											</tr>
				         			</c:forEach>
	         			</table>
					</td> 
				</tr>

				<tr> 
					<td>
							<b> <bean:message key="label.offlineFiles" /> </b>
	      			</td>
          			<td colspan=3 NOWRAP valign=top> 
							<html:file  property="theOfflineFile"></html:file>
						 	<html:submit onclick="javascript:submitMethod('submitOfflineFiles');" styleClass="buttonLeft">
								 <bean:message key="button.upload"/> 
							</html:submit>
					</td> 

				</tr>


				<html:hidden property="fileItem"/>
				<html:hidden property="offlineFile"/>				
				<html:hidden property="uuid"/>				
				
		  		<tr>
 				 	<td NOWRAP colspan=4 align=center valign=top>								
						&nbsp
				  	</td>
				</tr>
 				 
			</table>	  	

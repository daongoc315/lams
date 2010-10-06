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
  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA

  http://www.gnu.org/licenses/gpl.txt
--%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN"
        "http://www.w3.org/TR/html4/strict.dtd">

<%@ include file="/common/taglibs.jsp"%>
<lams:html>
	<lams:head>
		<%@ include file="/common/header.jsp"%>
		
		<script language="JavaScript" type="text/JavaScript">

			function submitMethod() {
				document.VoteAuthoringForm.submit();
			}
			
			function submitMethod(actionMethod) {
				document.VoteAuthoringForm.dispatch.value=actionMethod; 
				document.VoteAuthoringForm.submit();
			}
			
		</script>		
	</lams:head>
	
	<body class="stripes">
	
		<div id="content">
		
		<table cellpadding="0">

			<html:form  action="/monitoring?validate=false" styleId="newNominationForm" enctype="multipart/form-data" method="POST">			
			<html:hidden property="dispatch" value="saveSingleNomination"/>
			<html:hidden property="toolContentID"/>
			<html:hidden property="currentTab" styleId="currentTab" />
			<html:hidden property="activeModule"/>
			<html:hidden property="httpSessionID"/>								
			<html:hidden property="defaultContentIdStr"/>								
			<html:hidden property="defineLaterInEditMode"/>										
			<html:hidden property="contentFolderID"/>														
			<html:hidden property="editableNominationIndex"/>														
			<html:hidden property="editNominationBoxRequest" value="true"/>																	
			
			
			<tr>
			<td>
			<table class="innerforms">
		
				<tr>
					<td>
						<div class="field-name">
							<fmt:message key="label.edit.nomination"></fmt:message>
						</div>
						
						<lams:CKEditor id="newNomination"
							value="${voteGeneralAuthoringDTO.editableNominationText}"
							contentFolderID="${voteGeneralAuthoringDTO.contentFolderID}"></lams:CKEditor>
					</td>
				</tr>
			</table>				
			</td>
			</tr>
			</table>
		
			<lams:ImgButtonWrapper>
				<a href="#" onclick="getElementById('newNominationForm').submit();" class="button-add-item">
					<fmt:message key="label.save.nomination" />
				</a>
				<a href="#" onclick="javascript:window.parent.hideMessage()" class="button space-left">
					<fmt:message key="label.cancel" />
				</a>
			</lams:ImgButtonWrapper>	
			</html:form>
			

		</div>
		
		<div id="footer"></div>
	</body>
</lams:html>

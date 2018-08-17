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
  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301
  USA

  http://www.gnu.org/licenses/gpl.txt
--%>

<!DOCTYPE html>

<%@ include file="/common/taglibs.jsp"%>

<lams:html>

<lams:head>
	<title><fmt:message key="label.view.groups.title" />
	</title>

	<lams:css />
	<c:set var="lams">
		<lams:LAMSURL />
	</c:set>

	<script type="text/javascript" src="${lams}includes/javascript/jquery.js"></script>
	<script type="text/javascript"
		src="${lams}includes/javascript/common.js"></script>
</lams:head>
<body class="stripes">
	<style type="text/css">
	.user-container {
		padding: 2px;
	}
	</style>
	
	<script type="text/javascript" src="/lams/includes/javascript/bootstrap.min.js"></script>
	
	<!-- Modal -->
	<div class="modal fade" id="confirmationModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel">
	  <div class="modal-dialog modal-sm" role="popup">
	    <div class="modal-content">
	      <div class="modal-header bg-warning">
	        <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
	        <h4 class="modal-title"><i class="fa fa-sm fa-users"></i> <fmt:message key="label.group.confirm.header"/></h4>
	      </div>
	      <div class="modal-body text-center" style="min-height: 60px;">
	      </div>
	      <div class="modal-footer" style="padding: 8px">
	        	<button type="button" class="btn  btn-sm btn-default" data-dismiss="modal"><fmt:message key="label.cancel.button" /></button>
						<button id="submitter" onclick="" class="btn btn-sm btn-primary"><fmt:message key="label.group.confirm.button"/></button>
	      </div>
	    </div>
	  </div>
	</div>
	
	<lams:Page type="learner" title="${title}">
	
		<div class="panel">
			<fmt:message key="label.learner.choice.group.message" />
		</div>
	
		<div class="table-responsive">
			<table class="table table-condensed table-hover">
				<tr>
					<th width="25%" class="first"><fmt:message key="label.view.groups.title" /></th>
					<th><c:if test="${viewStudentsBeforeSelection}">
							<fmt:message key="label.view.groups.learners" />
						</c:if></th>
					<th></th>
				</tr>
				<c:forEach var="group" items="${groups}">
					<tr>
						<td width="25%" class="first"><strong><c:out value="${group.groupName}" /></strong></td>
						<td width="60%">
						<c:if test="${viewStudentsBeforeSelection}">
								<c:forEach items="${group.userList}" var="user">
									<div name="u-${user.userID}" class="user-container">
									<lams:Portrait userId="${user.userID}"/>&nbsp;<c:out value="${user.firstName}" />&nbsp;<c:out value="${user.lastName}" />
									</div>
								</c:forEach>
						</c:if></td>
						<td><c:choose>
								<c:when test="${not empty maxLearnersPerGroup and fn:length(group.userList)>=maxLearnersPerGroup}">
									<fmt:message key="label.learner.choice.group.full" />
								</c:when>
								<c:otherwise>
									<form:form
										action="/grouping/learnerChooseGroup.do?userId=${user.userID}&activityID=${activityID}&groupId=${group.groupID}"
										modelAttribute="form${user.userID}${activityID}${group.groupID}" id="form${user.userID}${activityID}${group.groupID}" target="_self">
									</form:form>							
									<button type="button" class="btn btn-sm btn-primary" 
										data-toggle="modal" data-target="#confirmationModal" 
										data-u="form${user.userID}${activityID}${group.groupID}" 
										data-gn="<c:out value="${group.groupName}" />"><fmt:message key="label.choose.group.button" />
									</button>							
									
								</c:otherwise>							
							</c:choose></td>
					</tr>
				</c:forEach>
			</table>
		</div>
	</lams:Page>
	<script>
		$('#confirmationModal').on('show.bs.modal', function (event) {
			  var button = $(event.relatedTarget) 
			  var url = button.data('u') 
			  var groupName = button.data('gn')
	
			  var modal = $(this)
			  modal.find('.modal-body').html('<fmt:message key="label.group.confirm.areyoujoining"/>:&nbsp;<strong>' + groupName + '</strong>?')
			  modal.find('#submitter').attr('onclick', "javascript:document.getElementById('" +url+ "').submit();")
			})
	</script>

</body>
</lams:html>

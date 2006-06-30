/****************************************************************
 * Copyright (C) 2005 LAMS Foundation (http://lamsfoundation.org)
 * =============================================================
 * License Information: http://lamsfoundation.org/licensing/lams/2.0/
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2.0 
 * as published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 * 
 * http://www.gnu.org/licenses/gpl.txt
 * ****************************************************************
 */

/* $Id$ */
package org.lamsfoundation.lams.admin.web;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.action.DynaActionForm;
import org.lamsfoundation.lams.usermanagement.Organisation;
import org.lamsfoundation.lams.usermanagement.OrganisationType;
import org.lamsfoundation.lams.usermanagement.Role;
import org.lamsfoundation.lams.usermanagement.User;
import org.lamsfoundation.lams.usermanagement.service.IUserManagementService;
import org.lamsfoundation.lams.util.WebUtil;
import org.lamsfoundation.lams.web.util.HttpSessionManager;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * @author Jun-Dir Liew
 *
 */

/**
 * struts doclets
 * 
 * @struts:action path="/userorg"
 *                name="UserOrgForm"
 *                scope="request"
 *                validate="false"
 *
 * @struts:action-forward name="userorg"
 *                        path=".userorg"
 */
public class UserOrgAction extends Action {
	
	private static final Logger log = Logger.getLogger(UserOrgAction.class);
	private static WebApplicationContext ctx = WebApplicationContextUtils
	    .getWebApplicationContext(HttpSessionManager.getInstance()
	    		.getServletContext());
	private static IUserManagementService service = (IUserManagementService)ctx
	    .getBean("userManagementServiceTarget");
	
	public ActionForward execute(ActionMapping mapping,
            ActionForm form,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {
		
		ActionMessages errors = new ActionMessages();
		Integer orgId = WebUtil.readIntParam(request,"orgId",true);
		log.debug("orgId: "+orgId);
        // get org name
		Organisation organisation = (Organisation)service.findById(Organisation.class,orgId);

		if((orgId==null)||(orgId<=0)||organisation==null){
			errors.add("orgId",new ActionMessage("error.org.invalid"));
			saveErrors(request,errors);
			return mapping.findForward("error");
		}
		
		String orgName = organisation.getName();
		log.debug("orgName: "+orgName);
		Organisation parentOrg = organisation.getParentOrganisation();
		if(parentOrg!=null){
			request.setAttribute("pOrgId",parentOrg.getOrganisationId());
			request.setAttribute("pOrgName",parentOrg.getName());
		}
		Integer orgType = organisation.getOrganisationType().getOrganisationTypeId();
		request.setAttribute("orgType",orgType);
				
		// get list of users in org
		User user = (User)service.getUserByLogin(request.getRemoteUser());
		List<User> users = new ArrayList<User>();
		if(request.isUserInRole(Role.SYSADMIN)){
			users = service.findAll(User.class);
		}else if(service.isUserInRole(user.getUserId(),orgId,Role.COURSE_ADMIN)){
			if(organisation.getCourseAdminCanAddNewUsers()){
				if(organisation.getCourseAdminCanBrowseAllUsers()){
					users = service.findAll(User.class);
				}else if(orgType.equals(new Integer(OrganisationType.CLASS_TYPE))){
					users = service.getUsersFromOrganisation(parentOrg.getOrganisationId());
				}else if(orgType.equals(new Integer(OrganisationType.COURSE_TYPE))){
					users = service.getUsersFromOrganisation(orgId);
				}
			}else{
				errors.add("authorisation",new ActionMessage("error.authorisation"));
				saveErrors(request,errors);
				return mapping.findForward("error");
			}
		}
		request.setAttribute("userlist",users);
		
		// create form object
		DynaActionForm userOrgForm = (DynaActionForm)form;
		userOrgForm.set("orgId",orgId);
		userOrgForm.set("orgName",orgName);
				
		// create list of userids, members of this org
		List<User> memberUsers = service.getUsersFromOrganisation(orgId);
		String[] userIds = new String[memberUsers.size()];
		for(int i=0; i<userIds.length; i++){
			userIds[i] = memberUsers.get(i).getUserId().toString();
		}
		userOrgForm.set("userIds",userIds);

		return mapping.findForward("userorg");
	}
	
}

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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301
 * USA
 *
 * http://www.gnu.org/licenses/gpl.txt
 * ****************************************************************
 */

package org.lamsfoundation.lams.admin.web.action;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.DynaActionForm;
import org.lamsfoundation.lams.admin.service.AdminServiceProxy;
import org.lamsfoundation.lams.admin.web.dto.UserOrgRoleDTO;
import org.lamsfoundation.lams.logevent.LogEvent;
import org.lamsfoundation.lams.logevent.service.ILogEventService;
import org.lamsfoundation.lams.themes.Theme;
import org.lamsfoundation.lams.themes.service.IThemeService;
import org.lamsfoundation.lams.timezone.Timezone;
import org.lamsfoundation.lams.timezone.dto.TimezoneDTO;
import org.lamsfoundation.lams.timezone.service.ITimezoneService;
import org.lamsfoundation.lams.timezone.util.TimezoneDTOComparator;
import org.lamsfoundation.lams.usermanagement.AuthenticationMethod;
import org.lamsfoundation.lams.usermanagement.Organisation;
import org.lamsfoundation.lams.usermanagement.OrganisationState;
import org.lamsfoundation.lams.usermanagement.OrganisationType;
import org.lamsfoundation.lams.usermanagement.Role;
import org.lamsfoundation.lams.usermanagement.SupportedLocale;
import org.lamsfoundation.lams.usermanagement.User;
import org.lamsfoundation.lams.usermanagement.UserOrganisation;
import org.lamsfoundation.lams.usermanagement.UserOrganisationRole;
import org.lamsfoundation.lams.usermanagement.dto.UserDTO;
import org.lamsfoundation.lams.usermanagement.service.IUserManagementService;
import org.lamsfoundation.lams.util.LanguageUtil;
import org.lamsfoundation.lams.util.MessageService;
import org.lamsfoundation.lams.util.WebUtil;
import org.lamsfoundation.lams.web.action.LamsDispatchAction;
import org.lamsfoundation.lams.web.session.SessionManager;
import org.lamsfoundation.lams.web.util.AttributeNames;

/**
 * @author Jun-Dir Liew
 */
public class UserAction extends LamsDispatchAction {

    private static Logger log = Logger.getLogger(UserAction.class);
    private IUserManagementService service;
    private MessageService messageService;
    private static IThemeService themeService;
    private static ITimezoneService timezoneService;
    private static List<SupportedLocale> locales;
    private static List<AuthenticationMethod> authenticationMethods;

    private void initServices() {
	if (service == null) {
	    service = AdminServiceProxy.getService(getServlet().getServletContext());
	}
	if (messageService == null) {
	    messageService = AdminServiceProxy.getMessageService(getServlet().getServletContext());
	}
	if (themeService == null) {
	    themeService = AdminServiceProxy.getThemeService(getServlet().getServletContext());
	}
	if (timezoneService == null) {
	    timezoneService = AdminServiceProxy.getTimezoneService(getServlet().getServletContext());
	}
    }

    @SuppressWarnings("unchecked")
    public ActionForward edit(ActionMapping mapping, ActionForm form, HttpServletRequest request,
	    HttpServletResponse response) throws Exception {

	initServices();
	if (locales == null) {
	    locales = service.findAll(SupportedLocale.class);
	    Collections.sort(locales);
	}
	if (authenticationMethods == null) {
	    authenticationMethods = service.findAll(AuthenticationMethod.class);
	}

	DynaActionForm userForm = (DynaActionForm) form;
	Integer orgId = WebUtil.readIntParam(request, "orgId", true);
	Integer userId = WebUtil.readIntParam(request, "userId", true);

	// Get all the css themess
	List<Theme> themes = themeService.getAllThemes();
	request.setAttribute("themes", themes);

	// Select the default themes by default
	Theme defaultTheme = themeService.getDefaultTheme();
	for (Theme theme : themes) {
	    if (theme.getThemeId().equals(defaultTheme.getThemeId())) {
		userForm.set("userTheme", theme.getThemeId());
		break;
	    }
	}

	// test requestor's permission
	Organisation org = null;
	Boolean canEdit = service.isUserGlobalGroupAdmin();
	if (orgId != null) {
	    org = (Organisation) service.findById(Organisation.class, orgId);
	    if (!canEdit) {
		OrganisationType orgType = org.getOrganisationType();
		Integer orgIdOfCourse = orgType.getOrganisationTypeId().equals(OrganisationType.CLASS_TYPE)
			? org.getParentOrganisation().getOrganisationId()
			: orgId;
		User requestor = service.getUserByLogin(request.getRemoteUser());
		if (service.isUserInRole(requestor.getUserId(), orgIdOfCourse, Role.GROUP_ADMIN)
			|| service.isUserInRole(requestor.getUserId(), orgIdOfCourse, Role.GROUP_MANAGER)) {
		    Organisation course = (Organisation) service.findById(Organisation.class, orgIdOfCourse);
		    canEdit = course.getCourseAdminCanAddNewUsers();
		}
	    }
	}

	if (!(canEdit || request.isUserInRole(Role.SYSADMIN))) {
	    request.setAttribute("errorName", "UserAction");
	    request.setAttribute("errorMessage", messageService.getMessage("error.authorisation"));
	    return mapping.findForward("error");
	}

	// editing a user
	if ((userId != null) && (userId != 0)) {
	    User user = (User) service.findById(User.class, userId);
	    log.debug("got userid to edit: " + userId);
	    BeanUtils.copyProperties(userForm, user);
	    userForm.set("password", null);
	    SupportedLocale locale = user.getLocale();
	    userForm.set("localeId", locale.getLocaleId());

	    AuthenticationMethod authenticationMethod = user.getAuthenticationMethod();
	    userForm.set("authenticationMethodId", authenticationMethod.getAuthenticationMethodId());
	    // set user's organisations to display
	    request.setAttribute("userOrgRoles", getUserOrgRoles(user));
	    request.setAttribute("globalRoles", getGlobalRoles(user));

	    // Check the user css theme is still installed
	    Long userSelectedTheme = null;
	    if (user.getTheme() != null) {
		for (Theme theme : themes) {
		    if (theme.getThemeId() == user.getTheme().getThemeId()) {
			userSelectedTheme = theme.getThemeId();
			break;
		    }
		}
	    }
	    // if still null, use the default
	    if (userSelectedTheme == null) {
		userSelectedTheme = themeService.getDefaultTheme().getThemeId();
	    }
	    userForm.set("userTheme", userSelectedTheme);
	    userForm.set("initialPortraitId", user.getPortraitUuid());

	    //property available for modification only to sysadmins
	    userForm.set("twoFactorAuthenticationEnabled", user.isTwoFactorAuthenticationEnabled());
	    // can not make a part of form as in JSP it can only be accessed by <bean:write>
	    // and we need the Date object to put into <lams:Date>
	    request.setAttribute("createDate", user.getCreateDate());
	} else { // create a user
	    try {
		SupportedLocale locale = LanguageUtil.getDefaultLocale();
		userForm.set("localeId", locale.getLocaleId());
	    } catch (Exception e) {
		log.debug(e);
	    }
	}
	userForm.set("orgId", (org == null ? null : org.getOrganisationId()));

	// sysadmins can mark users as required to use two-factor authentication
	if (request.isUserInRole(Role.SYSADMIN)) {
	    request.setAttribute("isSysadmin", true);
	}

	// Get all available time zones
	List<Timezone> availableTimeZones = timezoneService.getDefaultTimezones();
	TreeSet<TimezoneDTO> timezoneDtos = new TreeSet<TimezoneDTO>(new TimezoneDTOComparator());
	for (Timezone availableTimeZone : availableTimeZones) {
	    String timezoneId = availableTimeZone.getTimezoneId();
	    TimezoneDTO timezoneDto = new TimezoneDTO();
	    timezoneDto.setTimeZoneId(timezoneId);
	    timezoneDto.setDisplayName(TimeZone.getTimeZone(timezoneId).getDisplayName());
	    timezoneDtos.add(timezoneDto);
	}
	request.setAttribute("timezoneDtos", timezoneDtos);

	// for breadcrumb links
	if (org != null) {
	    request.setAttribute("orgName", org.getName());
	    Organisation parentOrg = org.getParentOrganisation();
	    if ((parentOrg != null) && !parentOrg.equals(service.getRootOrganisation())) {
		request.setAttribute("pOrgId", parentOrg.getOrganisationId());
		request.setAttribute("parentName", parentOrg.getName());
	    }
	}

	request.setAttribute("locales", locales);
	request.setAttribute("countryCodes", LanguageUtil.getCountryCodes(false));
	request.setAttribute("authenticationMethods", authenticationMethods);

	return mapping.findForward("user");
    }

    // display user's global roles, if any
    private UserOrgRoleDTO getGlobalRoles(User user) {
	initServices();
	UserOrganisation uo = service.getUserOrganisation(user.getUserId(),
		service.getRootOrganisation().getOrganisationId());
	if (uo == null) {
	    return null;
	}
	UserOrgRoleDTO uorDTO = new UserOrgRoleDTO();
	List<String> roles = new ArrayList<String>();
	for (Object uor : uo.getUserOrganisationRoles()) {
	    roles.add(((UserOrganisationRole) uor).getRole().getName());
	}
	Collections.sort(roles);
	uorDTO.setOrgName(uo.getOrganisation().getName());
	uorDTO.setRoles(roles);
	return uorDTO;
    }

    // display user's organisations and roles in them
    @SuppressWarnings("unchecked")
    private List<UserOrgRoleDTO> getUserOrgRoles(User user) {

	initServices();
	List<UserOrgRoleDTO> uorDTOs = new ArrayList<UserOrgRoleDTO>();
	List<UserOrganisation> uos = service.getUserOrganisationsForUserByTypeAndStatus(user.getLogin(),
		OrganisationType.COURSE_TYPE, OrganisationState.ACTIVE);
	for (UserOrganisation uo : uos) {
	    UserOrgRoleDTO uorDTO = new UserOrgRoleDTO();
	    List<String> roles = new ArrayList<String>();
	    for (Object uor : uo.getUserOrganisationRoles()) {
		roles.add(((UserOrganisationRole) uor).getRole().getName());
	    }
	    Collections.sort(roles);
	    uorDTO.setOrgName(uo.getOrganisation().getName());
	    uorDTO.setRoles(roles);
	    List<UserOrgRoleDTO> childDTOs = new ArrayList<UserOrgRoleDTO>();
	    List<UserOrganisation> childuos = service.getUserOrganisationsForUserByTypeAndStatusAndParent(
		    user.getLogin(), OrganisationType.CLASS_TYPE, OrganisationState.ACTIVE,
		    uo.getOrganisation().getOrganisationId());
	    for (UserOrganisation childuo : childuos) {
		UserOrgRoleDTO childDTO = new UserOrgRoleDTO();
		List<String> childroles = new ArrayList<String>();
		for (Object uor : childuo.getUserOrganisationRoles()) {
		    childroles.add(((UserOrganisationRole) uor).getRole().getName());
		}
		Collections.sort(childroles);
		childDTO.setOrgName(childuo.getOrganisation().getName());
		childDTO.setRoles(childroles);
		childDTOs.add(childDTO);
	    }
	    uorDTO.setChildDTOs(childDTOs);
	    uorDTOs.add(uorDTO);
	}

	return uorDTOs;
    }

    // determine whether to disable or delete user based on their lams data
    public ActionForward remove(ActionMapping mapping, ActionForm form, HttpServletRequest request,
	    HttpServletResponse response) throws Exception {

	initServices();

	if (!(request.isUserInRole(Role.SYSADMIN) || service.isUserGlobalGroupAdmin())) {
	    request.setAttribute("errorName", "UserAction");
	    request.setAttribute("errorMessage", messageService.getMessage("error.authorisation"));
	    return mapping.findForward("error");
	}

	Integer orgId = WebUtil.readIntParam(request, "orgId", true);
	Integer userId = WebUtil.readIntParam(request, "userId");
	User user = (User) service.findById(User.class, userId);

	Boolean hasData = service.userHasData(user);

	request.setAttribute("method", (hasData ? "disable" : "delete"));
	request.setAttribute("orgId", orgId);
	request.setAttribute("userId", userId);
	return mapping.findForward("remove");
    }

    public ActionForward disable(ActionMapping mapping, ActionForm form, HttpServletRequest request,
	    HttpServletResponse response) throws Exception {

	initServices();

	if (!(request.isUserInRole(Role.SYSADMIN) || service.isUserGlobalGroupAdmin())) {
	    request.setAttribute("errorName", "UserAction");
	    request.setAttribute("errorMessage", messageService.getMessage("error.authorisation"));
	    return mapping.findForward("error");
	}
	UserDTO sysadmin = (UserDTO) SessionManager.getSession().getAttribute(AttributeNames.USER);

	Integer orgId = WebUtil.readIntParam(request, "orgId", true);
	Integer userId = WebUtil.readIntParam(request, "userId");
	service.disableUser(userId);
	String[] args = new String[1];
	args[0] = userId.toString();
	String message = messageService.getMessage("audit.user.disable", args);
	ILogEventService logEventService = AdminServiceProxy.getLogEventService(getServlet().getServletContext());
	logEventService.logEvent(LogEvent.TYPE_USER_ORG_ADMIN, sysadmin != null ? sysadmin.getUserID() : null, userId,
		null, null, message);
	if ((orgId == null) || (orgId == 0)) {
	    return mapping.findForward("usersearch");
	} else {
	    request.setAttribute("org", orgId);
	    return mapping.findForward("userlist");
	}
    }

    public ActionForward delete(ActionMapping mapping, ActionForm form, HttpServletRequest request,
	    HttpServletResponse response) throws Exception {

	initServices();

	if (!(request.isUserInRole(Role.SYSADMIN) || service.isUserGlobalGroupAdmin())) {
	    request.setAttribute("errorName", "UserAction");
	    request.setAttribute("errorMessage", messageService.getMessage("error.authorisation"));
	    return mapping.findForward("error");
	}
	UserDTO sysadmin = (UserDTO) SessionManager.getSession().getAttribute(AttributeNames.USER);

	Integer orgId = WebUtil.readIntParam(request, "orgId", true);
	Integer userId = WebUtil.readIntParam(request, "userId");
	try {
	    service.removeUser(userId);
	} catch (Exception e) {
	    request.setAttribute("errorName", "UserAction");
	    request.setAttribute("errorMessage", e.getMessage());
	    return mapping.findForward("error");
	}
	String[] args = new String[1];
	args[0] = userId.toString();
	String message = messageService.getMessage("audit.user.delete", args);
	ILogEventService logEventService = AdminServiceProxy.getLogEventService(getServlet().getServletContext());
	logEventService.logEvent(LogEvent.TYPE_USER_ORG_ADMIN, sysadmin != null ? sysadmin.getUserID() : null, userId,
		null, null, message);
	if ((orgId == null) || (orgId == 0)) {
	    return mapping.findForward("usersearch");
	} else {
	    request.setAttribute("org", orgId);
	    return mapping.findForward("userlist");
	}
    }

    // called from disabled users screen
    public ActionForward enable(ActionMapping mapping, ActionForm form, HttpServletRequest request,
	    HttpServletResponse response) throws Exception {

	initServices();

	if (!(request.isUserInRole(Role.SYSADMIN) || service.isUserGlobalGroupAdmin())) {
	    request.setAttribute("errorName", "UserAction");
	    request.setAttribute("errorMessage", messageService.getMessage("error.authorisation"));
	    return mapping.findForward("error");
	}

	Integer userId = WebUtil.readIntParam(request, "userId", true);
	User user = (User) service.findById(User.class, userId);

	log.debug("enabling user: " + userId);
	user.setDisabledFlag(false);
	service.saveUser(user);

	return mapping.findForward("disabledlist");
    }

}
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

package org.lamsfoundation.lams.tool.noticeboard.web.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.lamsfoundation.lams.authoring.web.AuthoringConstants;
import org.lamsfoundation.lams.tool.ToolAccessMode;
import org.lamsfoundation.lams.tool.noticeboard.NoticeboardConstants;
import org.lamsfoundation.lams.tool.noticeboard.NoticeboardContent;
import org.lamsfoundation.lams.tool.noticeboard.service.INoticeboardService;
import org.lamsfoundation.lams.tool.noticeboard.util.NbApplicationException;
import org.lamsfoundation.lams.tool.noticeboard.util.NbWebUtil;
import org.lamsfoundation.lams.tool.noticeboard.web.form.NbAuthoringForm;
import org.lamsfoundation.lams.usermanagement.dto.UserDTO;
import org.lamsfoundation.lams.util.MessageService;
import org.lamsfoundation.lams.util.WebUtil;
import org.lamsfoundation.lams.web.session.SessionManager;
import org.lamsfoundation.lams.web.util.AttributeNames;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * <p>
 * This class is a simple combination of NbAuthoringStarterAction and NbAuthoringAction. It has been created for the
 * purpose of supporting the new authoring page which is done using DHTML.
 * </p>
 *
 * <p>
 * The unspecified method, is the same as the execute method for NbAuthoringStarterAction. It will get called when the
 * method parameter is not specified (that is on first entry into the authoring environment).
 * </p>
 *
 * <p>
 * The save, upload and delete method is the same as that of NbAuthoringAction, to see its explanation, please see
 * org.lamsfoundation.lams.tool.noticeboard.web.NbAuthoringAction
 * </p>
 *
 * @author mtruong
 */
@Controller
public class NbAuthoringController {

    private static Logger logger = Logger.getLogger(NbAuthoringController.class.getName());
    public final static String FORM = "NbAuthoringForm";

    @Autowired
    @Qualifier("nbService")
    private INoticeboardService nbService;

    @Autowired
    @Qualifier("nbMessageService")
    private MessageService messageService;

    /** Get the user from the shared session */
    public UserDTO getUser(HttpServletRequest request) {
	// set up the user details
	HttpSession ss = SessionManager.getSession();
	UserDTO user = (UserDTO) ss.getAttribute(AttributeNames.USER);
	if (user == null) {
	    String error = messageService.getMessage(NoticeboardConstants.ERR_MISSING_PARAM, "User");
	    logger.error(error);
	    throw new NbApplicationException(error);
	}
	return user;
    }

    @RequestMapping("/authoring")
    public String unspecified(@ModelAttribute NbAuthoringForm nbAuthoringForm, HttpServletRequest request,
	    HttpServletResponse response) {
	/*
	 * Retrieve the Service
	 */
	String contentIdString = nbAuthoringForm.getToolContentID();
//	Long contentId = NbWebUtil.convertToLong(nbAuthoringForm.getToolContentID());
//	String contentFolderId = nbAuthoringForm.getContentFolderID();
	
	Long contentId = WebUtil.readLongParam(request, NoticeboardConstants.TOOL_CONTENT_ID);
	String contentFolderId = WebUtil.readStrParam(request, NoticeboardConstants.CONTENT_FOLDER_ID);

	nbAuthoringForm.setToolContentID(contentIdString);

	/*
	 * DefineLater is used in the basic screen. If defineLater is set, then in the authoring page,
	 * the two tabs {Advanced, Instructions} are not visible.
	 */
	nbAuthoringForm.setDefineLater(request.getParameter(NoticeboardConstants.DEFINE_LATER));

	if (!contentExists(nbService, contentId)) {
	    //	Pre-fill the form with the default content
	    //NoticeboardContent nb =	nbService.retrieveNoticeboard(NoticeboardConstants.DEFAULT_CONTENT_ID);
	    Long defaultToolContentId = nbService
		    .getToolDefaultContentIdBySignature(NoticeboardConstants.TOOL_SIGNATURE);
	    //  logger.debug("Default tool content id is " + defaultToolContentId);
	    NoticeboardContent nb = nbService.retrieveNoticeboard(defaultToolContentId);

	    if (nb == null) {
		String error = "There is data missing in the database";
		logger.error(error);
		throw new NbApplicationException(error);
	    }

	    //initialise the values in the form, so the values will be shown in the jsp
	    nbAuthoringForm.setToolContentID(contentId.toString());
	    nbAuthoringForm.setContentFolderID(contentFolderId);
	    nbAuthoringForm.setTitle(nb.getTitle());
	    nbAuthoringForm.setBasicContent(nb.getContent());

	    //content already exists on the database
	} else {
	    //get the values from the database
	    NoticeboardContent nb = nbService.retrieveNoticeboard(contentId);

	    /*
	     * If retrieving existing content, check whether the contentInUse flag is set, if set, the
	     * author is not allowed to edit content
	     */

	    /*
	     * Define later set to true when the edit activity tab is brought up
	     * So that users cannot start using the content while the staff member is editing the content
	     */
	    nbAuthoringForm.populateFormWithNbContentValues(nb);
	    nbAuthoringForm.setContentFolderID(contentFolderId);
	    boolean isDefineLater = Boolean.parseBoolean(nbAuthoringForm.getDefineLater());
	    nb.setDefineLater(isDefineLater);
	    nbService.saveNoticeboard(nb);

	    if (isDefineLater) {
		request.setAttribute(AttributeNames.ATTR_MODE, ToolAccessMode.TEACHER.toString());

		// audit log the teacher has started editing activity in monitor
		nbService.auditLogStartEditingActivityInMonitor(contentId);
	    }
	}

	request.setAttribute(FORM, nbAuthoringForm);
	
	return "authoring/authoring";
    }

    /**
     * Checks the session to see if the title and content session variables exist or not.
     *
     * @param session
     *            The HttpSession to check.
     * @return true if the parameters title and content exists in the session, false otherwise
     */
    private boolean contentExists(INoticeboardService service, Long id) {
	NoticeboardContent nb = service.retrieveNoticeboard(id);
	if (nb == null) {
	    return false;
	} else {
	    return true;
	}

    }

    @RequestMapping(value = "/authoring/save", method = RequestMethod.POST)
    public String save(@ModelAttribute NbAuthoringForm nbAuthoringForm, HttpServletRequest request) {

	//copyAuthoringFormValuesIntoFormBean(request, nbForm);
	String contentId = WebUtil.readStrParam(request, NoticeboardConstants.TOOL_CONTENT_ID);
	Long content_id = WebUtil.readLongParam(request,NoticeboardConstants.TOOL_CONTENT_ID);

	if (contentId == null) {
	    String error = messageService.getMessage(NoticeboardConstants.ERR_MISSING_PARAM, "Tool Content Id");
	    logger.error(error);
	    throw new NbApplicationException(error);
	}

	//throws exception if the content id does not exist
	checkContentId(content_id);

	NoticeboardContent nbContent = nbService.retrieveNoticeboard(content_id);
	if (nbContent == null) {
	    //create a new noticeboard object
	    nbContent = new NoticeboardContent();
	    nbContent.setNbContentId(content_id);
	}

	nbAuthoringForm.copyValuesIntoNbContent(nbContent);
	if (nbContent.getDateCreated() == null) {
	    nbContent.setDateCreated(nbContent.getDateUpdated());
	}

	UserDTO user = getUser(request);
	nbContent.setCreatorUserId(new Long(user.getUserID().longValue()));

	// Author has finished editing the content and mark the defineLater flag to false
	nbContent.setDefineLater(false);
	nbService.saveNoticeboard(nbContent);

	request.setAttribute(AuthoringConstants.LAMS_AUTHORING_SUCCESS_FLAG, Boolean.TRUE);
	return "authoring/authoring";
    }

    /**
     * It is assumed that the contentId is passed as a http parameter
     * if the contentId is null, an exception is thrown, otherwise proceed as normal
     *
     * @param contentId
     *            the <code>toolContentId</code> to check
     */
    private void checkContentId(Long contentId) {
	if (contentId == null) {
	    String error = "Unable to continue. Tool content id missing.";

	    throw new NbApplicationException(error);
	}
    }

}

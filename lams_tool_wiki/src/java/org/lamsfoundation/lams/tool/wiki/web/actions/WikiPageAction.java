/****************************************************************
 * Copyright (C) 2005 LAMS Foundation (http://lamsfoundation.org)
 * =============================================================
 * License Information: http://lamsfoundation.org/licensing/lams/2.0/
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation.
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
/* $$Id$$ */

package org.lamsfoundation.lams.tool.wiki.web.actions;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.lamsfoundation.lams.tool.ToolAccessMode;
import org.lamsfoundation.lams.tool.wiki.dto.WikiPageContentDTO;
import org.lamsfoundation.lams.tool.wiki.dto.WikiPageDTO;
import org.lamsfoundation.lams.tool.wiki.model.Wiki;
import org.lamsfoundation.lams.tool.wiki.model.WikiPage;
import org.lamsfoundation.lams.tool.wiki.model.WikiPageContent;
import org.lamsfoundation.lams.tool.wiki.model.WikiSession;
import org.lamsfoundation.lams.tool.wiki.model.WikiUser;
import org.lamsfoundation.lams.tool.wiki.service.IWikiService;
import org.lamsfoundation.lams.tool.wiki.service.WikiServiceProxy;
import org.lamsfoundation.lams.tool.wiki.util.WikiConstants;
import org.lamsfoundation.lams.tool.wiki.web.forms.WikiPageForm;
import org.lamsfoundation.lams.util.WebUtil;
import org.lamsfoundation.lams.web.action.LamsDispatchAction;
import org.lamsfoundation.lams.web.util.AttributeNames;

/**
 * An abstract class used for wiki actions common to monitor, learner and author
 * 
 * @author lfoxton
 * 
 */
public abstract class WikiPageAction extends LamsDispatchAction {

    private static Logger logger = Logger.getLogger(AuthoringAction.class);

    public IWikiService wikiService;

    /**
     * Default method when no dispatch parameter is specified.
     */
    protected abstract ActionForward unspecified(ActionMapping mapping, ActionForm form, HttpServletRequest request,
	    HttpServletResponse response) throws Exception;

    /**
     * This action returns to the current wiki by updating the form accordingly
     * 
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return
     */
    protected abstract ActionForward returnToWiki(ActionMapping mapping, ActionForm form, HttpServletRequest request,
	    HttpServletResponse response, Long currentWikiPageId) throws Exception;

    protected abstract WikiUser getCurrentUser(Long toolSessionId);

    /**
     * Edit a page and make a new page content entry
     * 
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return
     */
    public ActionForward editPage(ActionMapping mapping, ActionForm form, HttpServletRequest request,
	    HttpServletResponse response) throws Exception {
	Long currentPageUid = WebUtil.readLongParam(request, WikiConstants.ATTR_CURRENT_WIKI);

	// set up wikiService
	if (wikiService == null) {
	    wikiService = WikiServiceProxy.getWikiService(this.getServlet().getServletContext());
	}

	// Set up the wiki form
	WikiPageForm wikiForm = (WikiPageForm) form;

	// Get the current wiki page
	WikiPage currentPage = wikiService.getWikiPageByUid(currentPageUid);

	// Set up the wiki user if this is a tool session (learner)
	// Also set the editable flag here
	Long toolSessionID = WebUtil.readLongParam(request, AttributeNames.PARAM_TOOL_SESSION_ID, true);
	WikiUser user = null;
	if (toolSessionID != null) {
	    user = this.getCurrentUser(toolSessionID);
	} 

	// Setting the editable flag based on call origin
	ToolAccessMode mode = WebUtil.readToolAccessModeParam(request, AttributeNames.PARAM_MODE, true);
	if (mode == null || mode==ToolAccessMode.TEACHER)
	{
	    // Author/Monitor/Live edit 
	    // If no editable flag came in the form (as in learner), set false
	    if (wikiForm.getIsEditable() == null) {
		wikiForm.setIsEditable(false);
	    }
	}
	else
	{
	    // Learner or preview
	    // If no editable flag came in the form (as in learner), set true
	    if (wikiForm.getIsEditable() == null) {
		wikiForm.setIsEditable(true);
	    }
	}
	// Updating the wikiPage, setting a null user which indicated this
	// change was made in author
	wikiService.updateWikiPage(wikiForm, currentPage, user);

	// Make sure the current page is set correctly then return to the wiki
	return returnToWiki(mapping, wikiForm, request, response, currentPageUid);
    }

    /**
     * Revert to a previous page content in the page history
     * 
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return
     */
    public ActionForward revertPage(ActionMapping mapping, ActionForm form, HttpServletRequest request,
	    HttpServletResponse response) throws Exception {
	Long revertPageContentVersion = new Long(WebUtil.readLongParam(request,
		WikiConstants.ATTR_HISTORY_PAGE_CONTENT_ID));
	Long currentPageUid = WebUtil.readLongParam(request, WikiConstants.ATTR_CURRENT_WIKI);

	// set up wikiService
	if (wikiService == null) {
	    wikiService = WikiServiceProxy.getWikiService(this.getServlet().getServletContext());
	}

	// Get the current wiki page
	WikiPage currentPage = wikiService.getWikiPageByUid(currentPageUid);

	// Set up the wiki user if this is a tool session (learner)
	Long toolSessionID = WebUtil.readLongParam(request, AttributeNames.PARAM_TOOL_SESSION_ID, true);
	WikiUser user = null;
	if (toolSessionID != null) {
	    user = this.getCurrentUser(toolSessionID);
	}

	WikiPageContent content = wikiService.getWikiPageContent(revertPageContentVersion);

	// Set up the authoring form
	WikiPageForm wikiForm = (WikiPageForm) form;

	// Set the wiki body in the authform
	wikiForm.setWikiBody(content.getBody());
	wikiForm.setIsEditable(currentPage.getEditable());

	// Updating the wikiPage, setting a null user which indicated this
	// change was made in author
	wikiService.updateWikiPage(wikiForm, currentPage, user);

	return unspecified(mapping, wikiForm, request, response);
    }

    /**
     * Compare two page content history items and return the result
     * 
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return
     */
    public ActionForward comparePage(ActionMapping mapping, ActionForm form, HttpServletRequest request,
	    HttpServletResponse response) throws Exception {
	Long revertPageContentVersion = new Long(WebUtil.readLongParam(request,
		WikiConstants.ATTR_HISTORY_PAGE_CONTENT_ID));
	Long currentPageUid = WebUtil.readLongParam(request, WikiConstants.ATTR_CURRENT_WIKI);

	// set up wikiService
	if (wikiService == null) {
	    wikiService = WikiServiceProxy.getWikiService(this.getServlet().getServletContext());
	}

	// Get the current wiki page content
	WikiPage currentWikiPage = wikiService.getWikiPageByUid(currentPageUid);
	WikiPageContent currentContent = currentWikiPage.getCurrentWikiContent();

	// Get the old wiki content to compare
	WikiPageContent compareContent = wikiService.getWikiPageContent(revertPageContentVersion);

	// Do the compariason
	String diff = wikiService.comparePages(compareContent.getBody(), currentContent.getBody());

	request.setAttribute(WikiConstants.ATTR_COMPARE_VERSIONS, compareContent.getVersion().toString() + "-"
		+ currentContent.getVersion().toString());
	request.setAttribute(WikiConstants.ATTR_COMPARE_TITLE, currentWikiPage.getTitle());
	request.setAttribute(WikiConstants.ATTR_COMPARE_STRING, diff);

	return mapping.findForward("compareWiki");
    }

    /**
     * View a page content from a wiki page's history
     * 
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return
     */
    public ActionForward viewPage(ActionMapping mapping, ActionForm form, HttpServletRequest request,
	    HttpServletResponse response) throws Exception {
	Long revertPageContentVersion = new Long(WebUtil.readLongParam(request,
		WikiConstants.ATTR_HISTORY_PAGE_CONTENT_ID));
	Long currentPageUid = WebUtil.readLongParam(request, WikiConstants.ATTR_CURRENT_WIKI);

	// set up wikiService
	if (wikiService == null) {
	    wikiService = WikiServiceProxy.getWikiService(this.getServlet().getServletContext());
	}

	// Get the current wiki page content
	WikiPage currentWikiPage = wikiService.getWikiPageByUid(currentPageUid);

	// Get the old wiki content
	WikiPageContent oldContent = wikiService.getWikiPageContent(revertPageContentVersion);

	// Set up the dto, only need to set title and content, as this is a view
	WikiPageDTO pageDTO = new WikiPageDTO();
	pageDTO.setTitle(currentWikiPage.getTitle());
	pageDTO.setCurrentWikiContentDTO(new WikiPageContentDTO(oldContent));

	request.setAttribute(WikiConstants.ATTR_CURRENT_WIKI, pageDTO);

	return mapping.findForward("viewWiki");
    }

    /**
     * Change the active page of the wiki form
     * 
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return
     */
    public ActionForward changePage(ActionMapping mapping, ActionForm form, HttpServletRequest request,
	    HttpServletResponse response) throws Exception {
	// set up wikiService
	if (wikiService == null) {
	    wikiService = WikiServiceProxy.getWikiService(this.getServlet().getServletContext());
	}

	Wiki wiki = null;
	WikiSession session = null;
	WikiPage wikiPage = null;

	String newPageName = WebUtil.readStrParam(request, WikiConstants.ATTR_NEW_PAGE_NAME);

	Long toolSessionID = WebUtil.readLongParam(request, AttributeNames.PARAM_TOOL_SESSION_ID, true);

	// get the wiki by either toolContentId or tool session
	if (toolSessionID == null) {
	    Long toolContentID = WebUtil.readLongParam(request, AttributeNames.PARAM_TOOL_CONTENT_ID);
	    wiki = wikiService.getWikiByContentId(toolContentID);

	    // Get the page to change to
	    wikiPage = wikiService.getWikiPageByWikiAndTitle(wiki, newPageName);
	} else {
	    session = wikiService.getSessionBySessionId(toolSessionID);
	    wiki = session.getWiki();

	    // Get the page to change to
	    wikiPage = wikiService.getWikiBySessionAndTitle(session, newPageName);
	}

	// go through unspecified to display the author screen, using wrapper
	// method to set the current page
	return this.returnToWiki(mapping, form, request, response, wikiPage.getUid());
    }

    /**
     * Add a new wiki page to this wiki instance
     * 
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return
     */
    public ActionForward addPage(ActionMapping mapping, ActionForm form, HttpServletRequest request,
	    HttpServletResponse response) throws Exception {
	// set up wikiService
	if (wikiService == null) {
	    wikiService = WikiServiceProxy.getWikiService(this.getServlet().getServletContext());
	}

	Wiki wiki = null;
	WikiSession session = null;
	WikiUser user = null;

	Long toolSessionID = WebUtil.readLongParam(request, AttributeNames.PARAM_TOOL_SESSION_ID, true);

	// Set up the authoring form
	WikiPageForm wikiForm = (WikiPageForm) form;

	// get the wiki by either toolContentId or tool session
	if (toolSessionID == null) {
	    Long toolContentID = WebUtil.readLongParam(request, AttributeNames.PARAM_TOOL_CONTENT_ID);
	    wiki = wikiService.getWikiByContentId(toolContentID);
	    // If no editable flag came in the form (as in learner), set true
	    if (wikiForm.getNewPageIsEditable() == null) {
		wikiForm.setNewPageIsEditable(false);
	    }
	} else {
	    session = wikiService.getSessionBySessionId(toolSessionID);
	    wiki = session.getWiki();
	    user = getCurrentUser(toolSessionID);
	   
	    
	}
	
	// Setting the editable flag based on call origin
	ToolAccessMode mode = WebUtil.readToolAccessModeParam(request, AttributeNames.PARAM_MODE, true);
	if (mode == null || mode==ToolAccessMode.TEACHER)
	{
	    // Author/Monitor/Live edit 
	    // If no editable flag came in the form (as in learner), set false
	    if (wikiForm.getNewPageIsEditable() == null) {
		wikiForm.setNewPageIsEditable(false);
	    }
	}
	else
	{
	    // Learner or preview
	    // If no editable flag came in the form (as in learner), set true
	    if (wikiForm.getNewPageIsEditable() == null) {
		wikiForm.setNewPageIsEditable(true);
	    }
	}

	// inserting the wiki page, null user and session indicates that this
	// page was saved in author
	Long currentWikiPageUid = wikiService.insertWikiPage(wikiForm, wiki, user, session);

	// go to the new wiki page
	return returnToWiki(mapping, wikiForm, request, response, currentWikiPageUid);
    }

    /**
     * Remove a wiki page from the wiki instance
     * 
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return
     */
    public ActionForward removePage(ActionMapping mapping, ActionForm form, HttpServletRequest request,
	    HttpServletResponse response) throws Exception {
	// The page to be removed
	Long currentPageUid = WebUtil.readLongParam(request, WikiConstants.ATTR_CURRENT_WIKI);

	// set up wikiService
	if (wikiService == null) {
	    wikiService = WikiServiceProxy.getWikiService(this.getServlet().getServletContext());
	}

	WikiPage wikiPage = wikiService.getWikiPageByUid(currentPageUid);

	// Updating the wikiPage, setting a null user which indicated this
	// change was made in author
	wikiService.deleteWikiPage(wikiPage);

	// return to the main page, by setting the current page to null
	return this.returnToWiki(mapping, form, request, response, null);

    }
}

/*******************************************************************************
 * Copyright (C) 2005 LAMS Foundation (http://lamsfoundation.org)
 * ============================================================= License
 * Information: http://lamsfoundation.org/licensing/lams/2.0/
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License version 2.0 as published by the
 * Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 * 
 * http://www.gnu.org/licenses/gpl.txt
 * ****************************************************************
 */
/* $$Id$$ */
package org.lamsfoundation.lams.authoring;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.lamsfoundation.lams.dao.IBaseDAO;
import org.lamsfoundation.lams.learningdesign.Activity;
import org.lamsfoundation.lams.learningdesign.ActivityOrderComparator;
import org.lamsfoundation.lams.learningdesign.BranchActivityEntry;
import org.lamsfoundation.lams.learningdesign.BranchCondition;
import org.lamsfoundation.lams.learningdesign.BranchingActivity;
import org.lamsfoundation.lams.learningdesign.ChosenGrouping;
import org.lamsfoundation.lams.learningdesign.Competence;
import org.lamsfoundation.lams.learningdesign.CompetenceMapping;
import org.lamsfoundation.lams.learningdesign.ComplexActivity;
import org.lamsfoundation.lams.learningdesign.ConditionGateActivity;
import org.lamsfoundation.lams.learningdesign.GateActivity;
import org.lamsfoundation.lams.learningdesign.Group;
import org.lamsfoundation.lams.learningdesign.Grouping;
import org.lamsfoundation.lams.learningdesign.GroupingActivity;
import org.lamsfoundation.lams.learningdesign.LearnerChoiceGrouping;
import org.lamsfoundation.lams.learningdesign.LearningDesign;
import org.lamsfoundation.lams.learningdesign.LearningLibrary;
import org.lamsfoundation.lams.learningdesign.License;
import org.lamsfoundation.lams.learningdesign.OptionsActivity;
import org.lamsfoundation.lams.learningdesign.ParallelActivity;
import org.lamsfoundation.lams.learningdesign.PermissionGateActivity;
import org.lamsfoundation.lams.learningdesign.RandomGrouping;
import org.lamsfoundation.lams.learningdesign.ScheduleGateActivity;
import org.lamsfoundation.lams.learningdesign.SequenceActivity;
import org.lamsfoundation.lams.learningdesign.SynchGateActivity;
import org.lamsfoundation.lams.learningdesign.SystemGateActivity;
import org.lamsfoundation.lams.learningdesign.ToolActivity;
import org.lamsfoundation.lams.learningdesign.Transition;
import org.lamsfoundation.lams.learningdesign.dao.IActivityDAO;
import org.lamsfoundation.lams.learningdesign.dao.IBranchActivityEntryDAO;
import org.lamsfoundation.lams.learningdesign.dao.ICompetenceDAO;
import org.lamsfoundation.lams.learningdesign.dao.ICompetenceMappingDAO;
import org.lamsfoundation.lams.learningdesign.dao.IGroupDAO;
import org.lamsfoundation.lams.learningdesign.dao.IGroupingDAO;
import org.lamsfoundation.lams.learningdesign.dao.ILearningDesignDAO;
import org.lamsfoundation.lams.learningdesign.dao.ILearningLibraryDAO;
import org.lamsfoundation.lams.learningdesign.dao.ILicenseDAO;
import org.lamsfoundation.lams.learningdesign.dao.ITransitionDAO;
import org.lamsfoundation.lams.lesson.LessonClass;
import org.lamsfoundation.lams.tool.SystemTool;
import org.lamsfoundation.lams.tool.Tool;
import org.lamsfoundation.lams.tool.ToolSession;
import org.lamsfoundation.lams.tool.dao.ISystemToolDAO;
import org.lamsfoundation.lams.tool.dao.IToolDAO;
import org.lamsfoundation.lams.tool.dao.IToolSessionDAO;
import org.lamsfoundation.lams.usermanagement.User;
import org.lamsfoundation.lams.usermanagement.WorkspaceFolder;
import org.lamsfoundation.lams.util.Configuration;
import org.lamsfoundation.lams.util.ConfigurationKeys;
import org.lamsfoundation.lams.util.wddx.WDDXProcessor;
import org.lamsfoundation.lams.util.wddx.WDDXProcessorConversionException;
import org.lamsfoundation.lams.util.wddx.WDDXTAGS;

/**
 * @author Manpreet Minhas
 * @author Mailing Truong
 * 
 * This is a utility class for extracting the information from the WDDX packet sent by the FLASH.
 * 
 * The following rules are applied: The client sends a subset of all possible data. If a field is included, then the
 * value associated with this field should be persisted (the value maybe a new value or an unchanged value) If a field
 * is not included then the server should assume that the value is unchanged. If the value of a field is one of the
 * special null values, then null should be persisted.
 * 
 * Object extractor has member data, so it should not be used as a singleton.
 * 
 */
public class ObjectExtractor implements IObjectExtractor {

    private static final Integer DEFAULT_COORD = new Integer(10); // default
    // coordinate
    // used if
    // the entry
    // from
    // Flash is
    // 0 or
    // less.

    protected IBaseDAO baseDAO = null;
    protected ILearningDesignDAO learningDesignDAO = null;
    protected IActivityDAO activityDAO = null;
    protected ICompetenceDAO competenceDAO = null;
    protected ICompetenceMappingDAO competenceMappingDAO = null;
    protected ITransitionDAO transitionDAO = null;
    protected ILearningLibraryDAO learningLibraryDAO = null;
    protected ILicenseDAO licenseDAO = null;
    protected IGroupingDAO groupingDAO = null;
    protected IToolDAO toolDAO = null;
    protected ISystemToolDAO systemToolDAO = null;
    protected IGroupDAO groupDAO = null;
    protected IToolSessionDAO toolSessionDAO = null;
    protected IBranchActivityEntryDAO branchActivityEntryDAO = null;

    private Integer mode = null;

    /*
     * The newActivityMap is a local copy of all the current activities. This will include the "top level" activities
     * and subactivities. It is used to "crossreference" activities as we go, without having to repull them from the
     * database. The keys are the UIIDs of the activities, not the IDs. It is important that the values in this map are
     * the Activity objects related to the Hibernate session as they are updated by the parseTransitions code.
     */
    protected HashMap<Integer, Activity> newActivityMap = new HashMap<Integer, Activity>();
    /*
     * Record the tool sessions and activities as we go for edit on fly. This is needed in case we delete any. Cannot
     * get them at the end as Hibernate tries to store the activities before getting the tool sessions, and this fails
     * due to a foriegn key error. Its the foriegn key error that we are trying to avoid!
     */
    protected HashMap<Integer, List<ToolSession>> toolSessionMap = new HashMap<Integer, List<ToolSession>>(); // Activity
    // UIID
    // ->
    // ToolSession
    /*
     * Used for easy access (rather than going back to the db) and makes it easier to clean them up at the end - they
     * can't be deleted easily via the cascades as we don't get a list of deleted entries sent back from the client! We
     * would end up with mappings still attached to a sequence activity that shouldn't be there! Not done as a map as we
     * sometimes will check via UIID, sometimes by ID
     */
    protected List<BranchActivityEntry> oldbranchActivityEntryList = new ArrayList<BranchActivityEntry>();

    protected HashMap<Integer, Activity> oldActivityMap = new HashMap<Integer, Activity>(); // Activity
    // UIID
    // ->
    // Activity
    /* cache of groupings - too hard to get them from the db */
    protected HashMap<Integer, Grouping> groupings = new HashMap<Integer, Grouping>();
    protected HashMap<Integer, Group> groups = new HashMap<Integer, Group>();
    protected HashMap<Integer, BranchActivityEntry> branchEntries = new HashMap<Integer, BranchActivityEntry>();
    protected HashMap<Integer, ComplexActivity> defaultActivityMap = new HashMap<Integer, ComplexActivity>();
    /*
     * can't delete as we go as they are linked to other items and have no way of knowing from the packet which ones
     * will need to be deleted, so start off assuming all need to be deleted and remove the ones we want to keep.
     */
    protected HashMap<Integer, Grouping> groupingsToDelete = new HashMap<Integer, Grouping>();
    protected LearningDesign learningDesign = null;
    protected Date modificationDate = null;
    /* cache of system tools so we aren't going back to the db all the time */
    protected HashMap<Long, SystemTool> systemTools = new HashMap<Long, SystemTool>();

    protected Logger log = Logger.getLogger(ObjectExtractor.class);

    /** Constructor to be used if Spring method injection is used */
    public ObjectExtractor() {
	modificationDate = new Date();
    }

    /** Constructor to be used if Spring method injection is not used */
    public ObjectExtractor(IBaseDAO baseDAO, ILearningDesignDAO learningDesignDAO, IActivityDAO activityDAO,
	    ILearningLibraryDAO learningLibraryDAO, ILicenseDAO licenseDAO, IGroupingDAO groupingDAO, IToolDAO toolDAO,
	    ISystemToolDAO systemToolDAO, IGroupDAO groupDAO, ITransitionDAO transitionDAO,
	    IToolSessionDAO toolSessionDAO) {
	this.baseDAO = baseDAO;
	this.learningDesignDAO = learningDesignDAO;
	this.activityDAO = activityDAO;
	this.learningLibraryDAO = learningLibraryDAO;
	this.licenseDAO = licenseDAO;
	this.groupingDAO = groupingDAO;
	this.toolDAO = toolDAO;
	this.systemToolDAO = systemToolDAO;
	this.groupDAO = groupDAO;
	this.transitionDAO = transitionDAO;
	this.toolSessionDAO = toolSessionDAO;
	modificationDate = new Date();
    }

    /** Spring injection methods */
    public IActivityDAO getActivityDAO() {
	return activityDAO;
    }

    public void setActivityDAO(IActivityDAO activityDAO) {
	this.activityDAO = activityDAO;
    }

    public ICompetenceDAO getCompetenceDAO() {
	return competenceDAO;
    }

    public void setCompetenceDAO(ICompetenceDAO competenceDAO) {
	this.competenceDAO = competenceDAO;
    }

    public ICompetenceMappingDAO getCompetenceMappingDAO() {
	return competenceMappingDAO;
    }

    public void setCompetenceMappingDAO(ICompetenceMappingDAO competenceMappingDAO) {
	this.competenceMappingDAO = competenceMappingDAO;
    }

    public IGroupDAO getGroupDAO() {
	return groupDAO;
    }

    public void setGroupDAO(IGroupDAO groupDAO) {
	this.groupDAO = groupDAO;
    }

    public IGroupingDAO getGroupingDAO() {
	return groupingDAO;
    }

    public void setGroupingDAO(IGroupingDAO groupingDAO) {
	this.groupingDAO = groupingDAO;
    }

    public ILearningDesignDAO getLearningDesignDAO() {
	return learningDesignDAO;
    }

    public void setLearningDesignDAO(ILearningDesignDAO learningDesignDAO) {
	this.learningDesignDAO = learningDesignDAO;
    }

    public ILearningLibraryDAO getLearningLibraryDAO() {
	return learningLibraryDAO;
    }

    public void setLearningLibraryDAO(ILearningLibraryDAO learningLibraryDAO) {
	this.learningLibraryDAO = learningLibraryDAO;
    }

    public ILicenseDAO getLicenseDAO() {
	return licenseDAO;
    }

    public void setLicenseDAO(ILicenseDAO licenseDAO) {
	this.licenseDAO = licenseDAO;
    }

    public IToolSessionDAO getToolSessionDAODAO() {
	return toolSessionDAO;
    }

    public void setToolSessionDAO(IToolSessionDAO toolSessionDAO) {
	this.toolSessionDAO = toolSessionDAO;
    }

    public HashMap<Integer, Activity> getNewActivityMap() {
	return newActivityMap;
    }

    public void setNewActivityMap(HashMap<Integer, Activity> newActivityMap) {
	this.newActivityMap = newActivityMap;
    }

    public IToolDAO getToolDAO() {
	return toolDAO;
    }

    public void setToolDAO(IToolDAO toolDAO) {
	this.toolDAO = toolDAO;
    }

    public ISystemToolDAO getSystemToolDAO() {
	return systemToolDAO;
    }

    public void setSystemToolDAO(ISystemToolDAO systemToolDAO) {
	this.systemToolDAO = systemToolDAO;
    }

    public ITransitionDAO getTransitionDAO() {
	return transitionDAO;
    }

    public void setTransitionDAO(ITransitionDAO transitionDAO) {
	this.transitionDAO = transitionDAO;
    }

    public IBaseDAO getBaseDAO() {
	return baseDAO;
    }

    public void setBaseDAO(IBaseDAO baseDAO) {
	this.baseDAO = baseDAO;
    }

    public IBranchActivityEntryDAO getBranchActivityEntryDAO() {
	return branchActivityEntryDAO;
    }

    public void setBranchActivityEntryDAO(IBranchActivityEntryDAO branchActivityEntryDAO) {
	this.branchActivityEntryDAO = branchActivityEntryDAO;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.lamsfoundation.lams.authoring.IObjectExtractor#extractSaveLearningDesign(java.util.Hashtable)
     */
    public LearningDesign extractSaveLearningDesign(Hashtable table, LearningDesign existingLearningDesign,
	    WorkspaceFolder workspaceFolder, User user) throws WDDXProcessorConversionException,
	    ObjectExtractorException {

	// if the learningDesign already exists, update it, otherwise create a
	// new one.
	learningDesign = existingLearningDesign != null ? existingLearningDesign : new LearningDesign();

	// Check the copy type. Can only update it if it is COPY_TYPE_NONE (ie
	// authoring copy)
	Integer copyTypeID = WDDXProcessor.convertToInteger(table, WDDXTAGS.COPY_TYPE);
	if (copyTypeID == null) {
	    copyTypeID = LearningDesign.COPY_TYPE_NONE;
	}
	if (learningDesign != null && learningDesign.getCopyTypeID() != null
		&& !learningDesign.getCopyTypeID().equals(copyTypeID) && !learningDesign.getEditOverrideLock()) {
	    throw new ObjectExtractorException(
		    "Unable to save learning design.  Cannot change copy type on existing design.");
	}
	if (!copyTypeID.equals(LearningDesign.COPY_TYPE_NONE) && !learningDesign.getEditOverrideLock()) {
	    throw new ObjectExtractorException("Unable to save learning design.  Learning design is read-only");
	}
	learningDesign.setCopyTypeID(copyTypeID);

	learningDesign.setWorkspaceFolder(workspaceFolder);
	learningDesign.setUser(user);

	// Pull out all the existing groups. there isn't an easy way to pull
	// them out of the db requires an outer join across
	// three objects (learning design -> grouping activity -> grouping) so
	// put both the existing ones and the new ones
	// here for reference later.
	initialiseGroupings();

	// Branch activity mappings are indirectly accessible via sequence
	// activities or groupings. Have a separate list
	// to make them easier to delete unwanted ones later.
	intialiseBranchActivityMappings();

	// get a mapping of all the existing activities and their tool sessions,
	// in case we need to delete some tool sessions later.
	initialiseToolSessionMap(learningDesign);

	// get the core learning design stuff - default to invalid
	learningDesign.setValidDesign(Boolean.FALSE);
	if (keyExists(table, WDDXTAGS.LEARNING_DESIGN_UIID)) {
	    learningDesign.setLearningDesignUIID(WDDXProcessor.convertToInteger(table, WDDXTAGS.LEARNING_DESIGN_UIID));
	}
	if (keyExists(table, WDDXTAGS.DESCRIPTION)) {
	    learningDesign.setDescription(WDDXProcessor.convertToString(table, WDDXTAGS.DESCRIPTION));
	}
	if (keyExists(table, WDDXTAGS.TITLE)) {
	    learningDesign.setTitle(WDDXProcessor.convertToString(table, WDDXTAGS.TITLE));
	}
	if (keyExists(table, WDDXTAGS.MAX_ID)) {
	    learningDesign.setMaxID(WDDXProcessor.convertToInteger(table, WDDXTAGS.MAX_ID));
	}
	if (keyExists(table, WDDXTAGS.VALID_DESIGN)) {
	    learningDesign.setValidDesign(WDDXProcessor.convertToBoolean(table, WDDXTAGS.VALID_DESIGN));
	}
	if (keyExists(table, WDDXTAGS.READ_ONLY)) {
	    learningDesign.setReadOnly(WDDXProcessor.convertToBoolean(table, WDDXTAGS.READ_ONLY));
	}
	if (keyExists(table, WDDXTAGS.EDIT_OVERRIDE_LOCK)) {
	    learningDesign.setEditOverrideLock(WDDXProcessor.convertToBoolean(table, WDDXTAGS.EDIT_OVERRIDE_LOCK));
	}
	if (keyExists(table, WDDXTAGS.DATE_READ_ONLY)) {
	    learningDesign.setDateReadOnly(WDDXProcessor.convertToDate(table, WDDXTAGS.DATE_READ_ONLY));
	}
	if (keyExists(table, WDDXTAGS.OFFLINE_INSTRUCTIONS)) {
	    learningDesign.setOfflineInstructions(WDDXProcessor.convertToString(table, WDDXTAGS.OFFLINE_INSTRUCTIONS));
	}
	if (keyExists(table, WDDXTAGS.ONLINE_INSTRUCTIONS)) {
	    learningDesign.setOnlineInstructions(WDDXProcessor.convertToString(table, WDDXTAGS.ONLINE_INSTRUCTIONS));
	}
	if (keyExists(table, WDDXTAGS.HELP_TEXT)) {
	    learningDesign.setHelpText(WDDXProcessor.convertToString(table, WDDXTAGS.HELP_TEXT));
	}
	// don't receive version from flash anymore(it was hardcoded). Get it
	// from lams configuration database.
	learningDesign.setVersion(Configuration.get(ConfigurationKeys.SERVER_VERSION_NUMBER));

	if (keyExists(table, WDDXTAGS.DURATION)) {
	    learningDesign.setDuration(WDDXProcessor.convertToLong(table, WDDXTAGS.DURATION));
	}

	if (keyExists(table, WDDXTAGS.DURATION)) {
	    learningDesign.setDuration(WDDXProcessor.convertToLong(table, WDDXTAGS.DURATION));
	}

	if (keyExists(table, WDDXTAGS.CONTENT_FOLDER_ID)) {
	    learningDesign.setContentFolderID(WDDXProcessor.convertToString(table, WDDXTAGS.CONTENT_FOLDER_ID));
	}

	if (keyExists(table, WDDXTAGS.SAVE_MODE)) {
	    mode = WDDXProcessor.convertToInteger(table, WDDXTAGS.SAVE_MODE);
	}

	// Set creation date and modification date based on the server timezone,
	// not the client.
	if (learningDesign.getCreateDateTime() == null) {
	    learningDesign.setCreateDateTime(modificationDate);
	}
	learningDesign.setLastModifiedDateTime(modificationDate);

	if (keyExists(table, WDDXTAGS.LICENCE_ID)) {
	    Long licenseID = WDDXProcessor.convertToLong(table, WDDXTAGS.LICENCE_ID);
	    if (licenseID != null) {
		License license = licenseDAO.getLicenseByID(licenseID);
		learningDesign.setLicense(license);
	    } else {
		learningDesign.setLicense(null); // special null value set
	    }
	}
	if (keyExists(table, WDDXTAGS.LICENSE_TEXT)) {
	    learningDesign.setLicenseText(WDDXProcessor.convertToString(table, WDDXTAGS.LICENSE_TEXT));
	}

	if (keyExists(table, WDDXTAGS.ORIGINAL_DESIGN_ID)) {
	    Long parentLearningDesignID = WDDXProcessor.convertToLong(table, WDDXTAGS.ORIGINAL_DESIGN_ID);
	    if (parentLearningDesignID != null) {
		LearningDesign parent = learningDesignDAO.getLearningDesignById(parentLearningDesignID);
		learningDesign.setOriginalLearningDesign(parent);
	    } else {
		learningDesign.setOriginalLearningDesign(null);
	    }
	}

	learningDesignDAO.insertOrUpdate(learningDesign);

	// now process the "parts" of the learning design
	parseCompetences((Vector) table.get(WDDXTAGS.COMPETENCES), learningDesign);
	parseGroupings((Vector) table.get(WDDXTAGS.GROUPINGS));
	parseActivities((Vector) table.get(WDDXTAGS.ACTIVITIES));
	parseActivitiesToMatchUpParentandInputActivities((Vector) table.get(WDDXTAGS.ACTIVITIES));
	parseTransitions((Vector) table.get(WDDXTAGS.TRANSITIONS));
	parseBranchMappings((Vector) table.get(WDDXTAGS.BRANCH_MAPPINGS));
	parseCompetenceMappings((Vector) table.get(WDDXTAGS.ACTIVITIES));
	progressDefaultChildActivities();

	learningDesign.setFirstActivity(learningDesign.calculateFirstActivity());
	learningDesignDAO.insertOrUpdate(learningDesign);

	deleteUnwantedGroupings();
	deleteUnwantedToolSessions(learningDesign);

	return learningDesign;
    }

    /**
     * Link SequenceActivities up with their firstActivity entries and BranchingActivities with their default branch.
     * Also tidy up the order ids for sequence activities so that they are in the same order as the transitions - needed
     * for the IMSLD export conversion to work. Not all the transitions may be drawn yet, so number off the others best
     * we can.
     * 
     * @throws WDDXProcessorConversionException
     */
    private void progressDefaultChildActivities() throws WDDXProcessorConversionException {

	if (defaultActivityMap.size() > 0) {
	    for (Integer defaultChildUIID : defaultActivityMap.keySet()) {
		ComplexActivity complex = defaultActivityMap.get(defaultChildUIID);
		Activity defaultActivity = newActivityMap.get(defaultChildUIID);
		if (defaultActivity == null) {
		    String msg = "Unable to find the default child activity (" + defaultChildUIID
			    + ") for the activity (" + complex + ") referred to in First Child to Sequence map.";
		    throw new WDDXProcessorConversionException(msg);
		} else {
		    complex.setDefaultActivity(defaultActivity);

		    // fix up the order ids for SequenceActivities
		    if (complex.isSequenceActivity()) {
			Set unprocessedChildren = new TreeSet(new ActivityOrderComparator());
			unprocessedChildren.addAll(complex.getActivities());

			unprocessedChildren.remove(defaultActivity);
			defaultActivity.setOrderId(1);

			int nextOrderId = 2;
			Activity nextActivity = defaultActivity.getTransitionFrom() != null ? defaultActivity
				.getTransitionFrom().getToActivity() : null;
			while (nextActivity != null) {
			    boolean removed = unprocessedChildren.remove(nextActivity);
			    if (!removed) {
				log
					.error("Next activity should be a child of the current sequence, but it isn't. Could we have a loop in the transitions? Aborting the ordering of ids based on transitions. Sequence activity "
						+ complex + " next activity " + nextActivity);
				break;
			    }
			    nextActivity.setOrderId(nextOrderId++);
			    nextActivity = nextActivity.getTransitionFrom() != null ? nextActivity.getTransitionFrom()
				    .getToActivity() : null;
			}

			if (unprocessedChildren.size() > 0) {
			    Iterator iter = unprocessedChildren.iterator();
			    while (iter.hasNext()) {
				nextActivity = (Activity) iter.next();
				nextActivity.setOrderId(nextOrderId++);
			    }
			}
		    }
		}
	    }

	}
    }

    /**
     * Initialise the map of groupings with those in the db from a previous save. This must be called as soon as the
     * learning design is read from the db and before it is changed.
     */
    private void initialiseGroupings() {
	List dbGroupings = groupingDAO.getGroupingsByLearningDesign(learningDesign.getLearningDesignId());
	Iterator iter = dbGroupings.iterator();
	while (iter.hasNext()) {
	    Grouping grouping = (Grouping) iter.next();
	    groupings.put(grouping.getGroupingUIID(), grouping);
	    groupingsToDelete.put(grouping.getGroupingUIID(), grouping);
	}
    }

    private void intialiseBranchActivityMappings() {
	oldbranchActivityEntryList = branchActivityEntryDAO.getEntriesByLearningDesign(learningDesign
		.getLearningDesignId());
    }

    /**
     * Initialise the map of tool sessions already in the database. Used to work out what will be deleted by Hibernate
     * later - useful to clean up any unwanted tool sessions for edit on the fly.
     */
    @SuppressWarnings("unchecked")
    private void initialiseToolSessionMap(LearningDesign learningDesign) {
	if (learningDesign.getEditOverrideLock() && learningDesign.getEditOverrideUser() != null) {
	    Iterator iter = learningDesign.getActivities().iterator();
	    while (iter.hasNext()) {
		Activity activity = (Activity) iter.next();
		oldActivityMap.put(activity.getActivityUIID(), activity);
		List<ToolSession> toolSessions = toolSessionDAO.getToolSessionByActivity(activity);
		if (toolSessions != null && toolSessions.size() > 0) {
		    toolSessionMap.put(activity.getActivityUIID(), toolSessions);
		}
	    }
	}
    }

    /** Delete the old unneeded groupings. Won't be done via a cascade */
    private void deleteUnwantedGroupings() {
	for (Grouping grouping : groupingsToDelete.values()) {
	    groupingDAO.delete(grouping);
	}
    }

    /**
     * Delete the old tool session. Won't be done via Hibernate cascades as we only want to do it for edit on fly. The
     * progress engine pre-generates the tool sessions for class level activities, so if we edit the design, we need to
     * delete the tool sessions. If we encounter evidence that this is a grouped activity - either more than one tool
     * session exists or the activity is grouped, then abort.
     */
    private void deleteUnwantedToolSessions(LearningDesign learningDesign) throws ObjectExtractorException {
	if (learningDesign.getEditOverrideLock() && learningDesign.getEditOverrideUser() != null) {

	    for (Integer uiid : toolSessionMap.keySet()) {
		if (!newActivityMap.containsKey(uiid)) {
		    List toolSessions = toolSessionMap.get(uiid);
		    if (toolSessions != null) {

			Activity activity = oldActivityMap.get(uiid);
			if (toolSessions.size() > 1) {
			    throw new ObjectExtractorException(
				    "More than one tool session exists for activity "
					    + activity.getTitle()
					    + " ("
					    + uiid
					    + ") but this shouldn't be possible. Cannot delete this tool session so editing is not allowed!");
			} else if (toolSessions.size() == 1) {

			    ToolSession toolSession = (ToolSession) toolSessions.get(0);
			    if (activity.isGroupingActivity()) {
				throw new ObjectExtractorException(
					"Activity "
						+ activity.getTitle()
						+ " ("
						+ activity.getActivityUIID()
						+ ") has a tool session but it is grouped. Cannot delete this tool session so editing is not allowed!");
			    }

			    // all okay, do ahead and delete the tool session
			    if (log.isDebugEnabled()) {
				log.debug("Removing tool session for activity " + activity.getTitle() + " ("
					+ activity.getActivityUIID() + ")");
			    }

			    toolSessionDAO.removeToolSession(toolSession);
			}
		    }
		}
	    }

	}
    }

    /**
     * Parses the groupings array sent from the WDDX packet. It will create the groupings object (ChosenGrouping,
     * RandomGrouping) so that when the GroupingActivity is processed, it can link to the grouping object that has been
     * created by this method.
     * 
     * @param groupingsList
     * @throws WDDXProcessorConversionException
     */

    private void parseGroupings(List groupingsList) throws WDDXProcessorConversionException {
	// iterate through the list of groupings objects
	// each object should contain information groupingUUID, groupingID,
	// groupingTypeID
	if (groupingsList != null) {
	    Iterator iterator = groupingsList.iterator();
	    while (iterator.hasNext()) {
		Hashtable groupingDetails = (Hashtable) iterator.next();
		if (groupingDetails != null) {
		    Grouping grouping = extractGroupingObject(groupingDetails);
		    groupingDAO.insertOrUpdate(grouping);
		    groupings.put(grouping.getGroupingUIID(), grouping);
		}
	    }

	}
    }

    public Grouping extractGroupingObject(Hashtable groupingDetails) throws WDDXProcessorConversionException {

	Integer groupingUUID = WDDXProcessor.convertToInteger(groupingDetails, WDDXTAGS.GROUPING_UIID);
	Integer groupingTypeID = WDDXProcessor.convertToInteger(groupingDetails, WDDXTAGS.GROUPING_TYPE_ID);
	if (groupingTypeID == null) {
	    throw new WDDXProcessorConversionException("groupingTypeID is missing");
	}

	Grouping grouping = groupings.get(groupingUUID);
	// check that the grouping type is still okay - if it is we keep the
	// grouping otherwise
	// we get rid of the old hibernate object.
	if (grouping != null) {
	    if (grouping.getGroupingTypeId().equals(groupingTypeID)) {
		groupingsToDelete.remove(groupingUUID);
	    } else {
		groupings.remove(grouping.getGroupingUIID());
		grouping = null;
	    }
	}

	if (grouping == null) {
	    Object object = Grouping.getGroupingInstance(groupingTypeID);
	    grouping = (Grouping) object;

	    if (keyExists(groupingDetails, WDDXTAGS.GROUPING_UIID)) {
		grouping.setGroupingUIID(WDDXProcessor.convertToInteger(groupingDetails, WDDXTAGS.GROUPING_UIID));
	    }
	}

	if (grouping.isRandomGrouping()) {
	    createRandomGrouping((RandomGrouping) grouping, groupingDetails);
	} else if (grouping.isChosenGrouping()) {
	    createChosenGrouping((ChosenGrouping) grouping, groupingDetails);
	}

	else if (grouping.isLearnerChoiceGrouping()) {
	    createLearnerChoiceGrouping((LearnerChoiceGrouping) grouping, groupingDetails);
	} else {
	    createLessonClass((LessonClass) grouping, groupingDetails);
	}

	if (keyExists(groupingDetails, WDDXTAGS.MAX_NUMBER_OF_GROUPS)) {
	    grouping.setMaxNumberOfGroups(WDDXProcessor
		    .convertToInteger(groupingDetails, WDDXTAGS.MAX_NUMBER_OF_GROUPS));
	}

	Set<Group> groupsToDelete = new HashSet<Group>(grouping.getGroups());

	List groupsList = (Vector) groupingDetails.get(WDDXTAGS.GROUPS);
	if (groupsList != null && groupsList.size() > 0) {
	    Iterator iter = groupsList.iterator();
	    while (iter.hasNext()) {
		Hashtable groupDetails = (Hashtable) iter.next();
		Group group = extractGroupObject(groupDetails, grouping);
		groups.put(group.getGroupUIID(), group);
		groupsToDelete.remove(group);
	    }
	}

	if (groupsToDelete.size() > 0) {
	    Iterator iter = groupsToDelete.iterator();
	    while (iter.hasNext()) {
		Group group = (Group) iter.next();
		if (group.getBranchActivities() != null) {
		    Iterator branchIter = group.getBranchActivities().iterator();
		    while (branchIter.hasNext()) {
			BranchActivityEntry entry = (BranchActivityEntry) branchIter.next();
			entry.setGroup(null);
		    }
		    group.getBranchActivities().clear();
		}
		grouping.getGroups().remove(group);
	    }
	}
	return grouping;
    }

    @SuppressWarnings("unchecked")
    private Group extractGroupObject(Hashtable groupDetails, Grouping grouping) throws WDDXProcessorConversionException {

	Group group = null;
	Integer groupUIID = WDDXProcessor.convertToInteger(groupDetails, WDDXTAGS.GROUP_UIID);
	if (groupUIID == null) {
	    throw new WDDXProcessorConversionException("Group is missing its UUID. Group " + groupDetails
		    + " grouping " + grouping);
	}
	Long groupID = WDDXProcessor.convertToLong(groupDetails, WDDXTAGS.GROUP_ID);

	// Does it exist already? If the group was created at runtime, there
	// will be an ID but no IU ID field.
	// If it was created in authoring, will have a UI ID and may or may not
	// have an ID.
	// So try to match up on UI ID first, failing that match on ID. Then the
	// worst case, which is the group
	// is created at runtime but then modified in authoring (and has has a
	// new UI ID added) is handled.
	if (grouping.getGroups() != null && grouping.getGroups().size() > 0) {
	    Group uiid_match = null;
	    Group id_match = null;
	    Iterator iter = grouping.getGroups().iterator();
	    while (uiid_match == null && iter.hasNext()) {
		Group possibleGroup = (Group) iter.next();
		if (groupUIID.equals(possibleGroup.getGroupUIID())) {
		    uiid_match = possibleGroup;
		}
		if (groupID != null && groupID.equals(possibleGroup.getGroupId())) {
		    id_match = possibleGroup;
		}
	    }
	    group = uiid_match != null ? uiid_match : id_match;
	}

	if (group == null) {
	    group = new Group();
	    grouping.getGroups().add(group);
	}

	group.setGroupName(WDDXProcessor.convertToString(groupDetails, WDDXTAGS.GROUP_NAME));
	group.setGrouping(grouping);
	group.setGroupUIID(groupUIID);

	if (keyExists(groupDetails, WDDXTAGS.ORDER_ID)) {
	    group.setOrderId(WDDXProcessor.convertToInteger(groupDetails, WDDXTAGS.ORDER_ID));
	} else {
	    group.setOrderId(0);
	}

	return group;
    }

    private void createRandomGrouping(RandomGrouping randomGrouping, Hashtable groupingDetails)
	    throws WDDXProcessorConversionException {
	// the two settings are mutually exclusive. Flash takes care of this,
	// but we'll code it here too just to make sure.
	Integer numLearnersPerGroup = WDDXProcessor.convertToInteger(groupingDetails, WDDXTAGS.LEARNERS_PER_GROUP);
	if (numLearnersPerGroup != null && numLearnersPerGroup.intValue() > 0) {
	    randomGrouping.setLearnersPerGroup(numLearnersPerGroup);
	    randomGrouping.setNumberOfGroups(null);
	} else {
	    Integer numGroups = WDDXProcessor.convertToInteger(groupingDetails, WDDXTAGS.NUMBER_OF_GROUPS);
	    if (numGroups != null && numGroups.intValue() > 0) {
		randomGrouping.setNumberOfGroups(numGroups);
	    } else {
		randomGrouping.setNumberOfGroups(null);
	    }
	    randomGrouping.setLearnersPerGroup(null);
	}
    }

    private void createChosenGrouping(ChosenGrouping chosenGrouping, Hashtable groupingDetails)
	    throws WDDXProcessorConversionException {
	// no extra properties as yet
    }

    /**
     * Parses the list of activities sent from the WDDX packet. The current activities that belong to this learning
     * design will be compared with the new list of activities. Any new activities will be added to the database,
     * existing activities will be updated, and any activities that are not present in the list of activities from the
     * wddx packet (but appear in the list of current activities) are deleted.
     * 
     * @param activitiesList
     *                The list of activities from the WDDX packet.
     * @throws WDDXProcessorConversionException
     * @throws ObjectExtractorException
     */
    @SuppressWarnings("unchecked")
    private void parseActivities(List activitiesList) throws WDDXProcessorConversionException, ObjectExtractorException {

	if (activitiesList != null) {
	    Iterator iterator = activitiesList.iterator();
	    while (iterator.hasNext()) {
		Hashtable activityDetails = (Hashtable) iterator.next();
		Activity activity = extractActivityObject(activityDetails);
		activityDAO.insertOrUpdate(activity);
		newActivityMap.put(activity.getActivityUIID(), activity);
	    }
	}

	// clear the transitions.
	// clear the old set and reset up the activities set. Done this way to
	// keep the Hibernate cascading happy.
	// this means we don't have to manually remove the transition object.
	// Note: This will leave orphan content in the tool tables. It can be
	// removed by the tool content cleaning job,
	// which may be run from the admin screen or via a cron job.

	learningDesign.getActivities().clear();
	learningDesign.getActivities().addAll(newActivityMap.values());

	// TODO: Need to double check that the toolID/toolContentID combinations
	// match entries in lams_tool_content table, or put FK on table.
	learningDesignDAO.insertOrUpdate(learningDesign);
    }

    /**
     * Parses the list of activities sent from the WDDX packet for competence mappings. Each activity's new set of
     * competenceMapping is compared against the old set and the db is updated accordingly
     * 
     * @param activitiesList
     *                The list of activities from the WDDX packet.
     * @throws WDDXProcessorConversionException
     * @throws ObjectExtractorException
     */
    @SuppressWarnings("unchecked")
    private void parseCompetenceMappings(List activitiesList) throws WDDXProcessorConversionException,
	    ObjectExtractorException {

	if (activitiesList != null) {
	    Iterator iterator = activitiesList.iterator();
	    while (iterator.hasNext()) {
		Hashtable activityDetails = (Hashtable) iterator.next();

		// Check that this is a tool activity, otherwise continue to the
		// next iteration
		if (keyExists(activityDetails, WDDXTAGS.ACTIVITY_TYPE_ID)) {
		    int actType = WDDXProcessor.convertToInteger(activityDetails, WDDXTAGS.ACTIVITY_TYPE_ID);
		    if (actType != Activity.TOOL_ACTIVITY_TYPE) {
			continue;
		    }
		}

		// Get the tool activity using the UIID and the learningDesignID
		ToolActivity toolActivity = null;
		if (keyExists(activityDetails, WDDXTAGS.ACTIVITY_UIID)) {
		    toolActivity = (ToolActivity) activityDAO.getActivityByUIID(WDDXProcessor.convertToInteger(
			    activityDetails, WDDXTAGS.ACTIVITY_UIID), learningDesign);
		} else {
		    continue;
		}

		if (keyExists(activityDetails, WDDXTAGS.COMPETENCE_MAPPINGS)) {
		    List<String> competenceMappingsList = (Vector) activityDetails.get(WDDXTAGS.COMPETENCE_MAPPINGS);

		    for (String competenceMappingEntry : competenceMappingsList) {

			if (toolActivity.getActivityId() != null) {

			    // First get the competence from the competence
			    // mapping entry in the hashtable
			    Competence competence = competenceDAO.getCompetence(toolActivity.getLearningDesign(),
				    competenceMappingEntry);
			    if (competence == null) {
				continue;
			    }

			    // Now get the competence mapping using the tool
			    // activity and the competence as reference
			    CompetenceMapping competenceMapping = competenceMappingDAO.getCompetenceMapping(
				    toolActivity, competence);

			    // Only save new competence mappings, no need to
			    // update existing
			    if (competenceMapping == null) {
				CompetenceMapping newMapping = new CompetenceMapping();
				newMapping.setCompetence(competence);
				newMapping.setToolActivity(toolActivity);

				// newMappings.add(newMapping);
				competenceMappingDAO.saveOrUpdate(newMapping);
				// toolActivity.getCompetenceMappings().add(newMapping);
			    }
			} else {
			    Competence competence = competenceDAO.getCompetence(learningDesign, competenceMappingEntry);
			    CompetenceMapping newMapping = new CompetenceMapping();
			    newMapping.setCompetence(competence);
			    newMapping.setToolActivity(toolActivity);
			    competenceMappingDAO.saveOrUpdate(newMapping);
			    // toolActivity.getCompetenceMappings().add(newMapping);
			}
		    }

		    // delete any pre-existing mappings that have been deleted
		    Set<CompetenceMapping> existingMappings = toolActivity.getCompetenceMappings();
		    if (existingMappings != null) {
			Set<CompetenceMapping> removeCompetenceMappings = new HashSet();
			if (competenceMappingsList != null && competenceMappingsList.size() > 0) {
			    for (CompetenceMapping competenceMapping : existingMappings) {
				boolean remove = true;

				for (String competenceMappingEntry : competenceMappingsList) {
				    if (competenceMappingEntry.equals(competenceMapping.getCompetence().getTitle())) {
					remove = false;
					break;
				    }
				}

				if (remove) {
				    removeCompetenceMappings.add(competenceMapping);
				}
			    }
			} else {
			    removeCompetenceMappings.addAll(existingMappings);
			}
			competenceMappingDAO.deleteAll(removeCompetenceMappings);
			toolActivity.getCompetenceMappings().removeAll(removeCompetenceMappings);
		    }
		}
	    }
	}
    }

    /**
     * Parses the list of competences sent from the WDDX packet. The current competences that belong to this learning
     * design will be compared with the new list of competences. Any new competences will be added to the database,
     * existing competences will be updated, and any competences that are not present in the list of competences from
     * the wddx packet (but appear in the list of current competences) are deleted.
     * 
     * @param activitiesList
     *                The list of activities from the WDDX packet.
     * @throws WDDXProcessorConversionException
     * @throws ObjectExtractorException
     */
    @SuppressWarnings("unchecked")
    private void parseCompetences(List<Hashtable> competenceList, LearningDesign learningDesign)
	    throws WDDXProcessorConversionException, ObjectExtractorException {

	Set<Competence> existingCompetences = learningDesign.getCompetences();
	if (competenceList != null) {
	    for (Hashtable competenceTable : competenceList) {
		String title = (String) competenceTable.get("title");
		String description = (String) competenceTable.get("description");

		if (getComptenceFromSet(existingCompetences, title) != null) {
		    Competence updateCompetence = getComptenceFromSet(existingCompetences, title);
		    updateCompetence.setDescription(description);
		    competenceDAO.saveOrUpdate(updateCompetence);
		} else {
		    Competence newCompetence = new Competence();
		    newCompetence.setTitle(title);
		    newCompetence.setDescription(description);
		    newCompetence.setLearningDesign(learningDesign);
		    competenceDAO.saveOrUpdate(newCompetence);
		}
	    }

	    // now go through and delete any competences from the old list,
	    // that dont exist in the new list.
	    Set<Competence> removeCompetences = new HashSet<Competence>();
	    if (existingCompetences != null) {
		if (competenceList != null && competenceList.size() > 0) {
		    for (Competence existingCompetence : existingCompetences) {
			boolean remove = true;
			for (Hashtable competenceTable : competenceList) {
			    if (existingCompetence.getTitle().equals(competenceTable.get("title"))) {
				remove = false;
				break;
			    }
			}

			if (remove) {
			    removeCompetences.add(existingCompetence);
			}
		    }
		} else {
		    removeCompetences.addAll(existingCompetences);
		}
		// competenceDAO.deleteAll(removeCompetences);
		learningDesign.getCompetences().removeAll(removeCompetences);
	    }
	}
    }

    private Competence getComptenceFromSet(Set<Competence> competences, String title) {
	if (competences != null) {
	    for (Competence competence : competences) {
		if (competence.getTitle().equals(title)) {
		    return competence;
		}
	    }

	}
	return null;
    }

    /**
     * Because the activities list was processed before by the method parseActivities, it is assumed that all activities
     * have already been saved into the database. So now we can go through and find the any parent activity or input
     * activities for an activity.
     * 
     * @param activitiesList
     * @param learningDesign
     * @throws WDDXProcessorConversionException
     * @throws ObjectExtractorException
     */
    private void parseActivitiesToMatchUpParentandInputActivities(List activitiesList)
	    throws WDDXProcessorConversionException, ObjectExtractorException {
	if (activitiesList != null) {
	    Iterator iterator = activitiesList.iterator();
	    while (iterator.hasNext()) {

		Hashtable activityDetails = (Hashtable) iterator.next();

		Integer activityUUID = WDDXProcessor.convertToInteger(activityDetails, WDDXTAGS.ACTIVITY_UIID);
		Activity existingActivity = newActivityMap.get(activityUUID);

		// match up activity to parent based on UIID
		if (keyExists(activityDetails, WDDXTAGS.PARENT_UIID)) {
		    Integer parentUIID = WDDXProcessor.convertToInteger(activityDetails, WDDXTAGS.PARENT_UIID);
		    if (parentUIID != null) {
			Activity parentActivity = newActivityMap.get(parentUIID);
			if (parentActivity == null) {
			    throw new ObjectExtractorException("Parent activity " + parentUIID
				    + " missing for activity " + existingActivity.getTitle() + ": "
				    + existingActivity.getActivityUIID());
			}
			existingActivity.setParentActivity(parentActivity);
			existingActivity.setParentUIID(parentUIID);
			if (parentActivity.isComplexActivity()) {
			    ((ComplexActivity) parentActivity).addActivity(existingActivity);
			    activityDAO.update(parentActivity);
			}

		    } else {
			existingActivity.setParentActivity(null);
			existingActivity.setParentUIID(null);
			existingActivity.setOrderId(null); // top level
			// activities don't
			// have order ids.
		    }
		}

		// match up activity to input activities based on UIID. At
		// present there will be only one input activity
		existingActivity.getInputActivities().clear();
		Integer inputActivityUIID = WDDXProcessor.convertToInteger(activityDetails,
			WDDXTAGS.INPUT_TOOL_ACTIVITY_UIID);
		if (inputActivityUIID != null) {
		    Activity inputActivity = newActivityMap.get(inputActivityUIID);
		    if (inputActivity == null) {
			throw new ObjectExtractorException("Input activity " + inputActivityUIID
				+ " missing for activity " + existingActivity.getTitle() + ": "
				+ existingActivity.getActivityUIID());
		    }
		    existingActivity.getInputActivities().add(inputActivity);
		}

		activityDAO.update(existingActivity);

	    }
	}
    }

    /**
     * Like parseActivities, parseTransitions parses the list of transitions from the wddx packet. New transitions will
     * be added, existing transitions updated and any transitions that are no longer needed are deleted.
     * 
     * @param transitionsList
     *                The list of transitions from the wddx packet
     * @param learningDesign
     * @throws WDDXProcessorConversionException
     */
    @SuppressWarnings("unchecked")
    private void parseTransitions(List transitionsList) throws WDDXProcessorConversionException {

	HashMap<Integer, Transition> newMap = new HashMap<Integer, Transition>();

	if (transitionsList != null) {
	    Iterator iterator = transitionsList.iterator();
	    while (iterator.hasNext()) {
		Hashtable transitionDetails = (Hashtable) iterator.next();
		Transition transition = extractTransitionObject(transitionDetails);
		// Check if transition actually exists. extractTransitionObject
		// returns null
		// if neither the to/from activity exists.
		if (transition != null) {
		    transitionDAO.insertOrUpdate(transition);
		    newMap.put(transition.getTransitionUIID(), transition);
		}
	    }
	}

	// clean up the links for any old transitions.
	Iterator iter = learningDesign.getTransitions().iterator();
	while (iter.hasNext()) {
	    Transition element = (Transition) iter.next();
	    Integer uiID = element.getTransitionUIID();
	    Transition match = newMap.get(uiID);
	    if (match == null) {
		// transition is no more, clean up the old activity links
		cleanupTransition(element);
	    }
	}
	// clear the old set and reset up the transition set. Done this way to
	// keep the Hibernate cascading happy.
	// this means we don't have to manually remove the transition object.
	// Note: This will leave orphan content in the tool tables. It can be
	// removed by the tool content cleaning job,
	// which may be run from the admin screen or via a cron job.
	learningDesign.getTransitions().clear();
	learningDesign.getTransitions().addAll(newMap.values());

	learningDesignDAO.insertOrUpdate(learningDesign);

    }

    public Activity extractActivityObject(Hashtable activityDetails) throws WDDXProcessorConversionException,
	    ObjectExtractorException {

	// it is assumed that the activityUUID will always be sent by flash.
	Integer activityUUID = WDDXProcessor.convertToInteger(activityDetails, WDDXTAGS.ACTIVITY_UIID);
	Activity activity = null;
	Integer activityTypeID = WDDXProcessor.convertToInteger(activityDetails, WDDXTAGS.ACTIVITY_TYPE_ID);
	if (activityTypeID == null) {
	    throw new ObjectExtractorException("activityTypeID missing");
	}

	// get the activity with the particular activity uuid, if null, then new
	// object needs to be created.
	Activity existingActivity = activityDAO.getActivityByUIID(activityUUID, learningDesign);
	if (existingActivity != null && !existingActivity.getActivityTypeId().equals(activityTypeID)) {
	    existingActivity = null;
	}

	if (existingActivity != null) {
	    activity = existingActivity;
	} else {
	    activity = Activity.getActivityInstance(activityTypeID.intValue());
	}
	processActivityType(activity, activityDetails);

	if (keyExists(activityDetails, WDDXTAGS.ACTIVITY_TYPE_ID)) {
	    activity.setActivityTypeId(WDDXProcessor.convertToInteger(activityDetails, WDDXTAGS.ACTIVITY_TYPE_ID));
	}
	if (keyExists(activityDetails, WDDXTAGS.ACTIVITY_UIID)) {
	    activity.setActivityUIID(WDDXProcessor.convertToInteger(activityDetails, WDDXTAGS.ACTIVITY_UIID));
	}
	if (keyExists(activityDetails, WDDXTAGS.DESCRIPTION)) {
	    activity.setDescription(WDDXProcessor.convertToString(activityDetails, WDDXTAGS.DESCRIPTION));
	}
	if (keyExists(activityDetails, WDDXTAGS.ACTIVITY_TITLE)) {
	    activity.setTitle(WDDXProcessor.convertToString(activityDetails, WDDXTAGS.ACTIVITY_TITLE));
	}
	if (keyExists(activityDetails, WDDXTAGS.HELP_TEXT)) {
	    activity.setHelpText(WDDXProcessor.convertToString(activityDetails, WDDXTAGS.HELP_TEXT));
	}

	activity.setXcoord(getCoord(activityDetails, WDDXTAGS.XCOORD));
	activity.setYcoord(getCoord(activityDetails, WDDXTAGS.YCOORD));

	if (keyExists(activityDetails, WDDXTAGS.GROUPING_UIID)) {
	    Integer groupingUIID = WDDXProcessor.convertToInteger(activityDetails, WDDXTAGS.GROUPING_UIID);
	    if (groupingUIID != null) {
		Grouping grouping = groupings.get(groupingUIID);
		if (grouping != null) {
		    setGrouping(activity, grouping, groupingUIID);
		} else {
		    log.warn("Unable to find matching grouping for groupingUIID" + groupingUIID + ". Activity UUID"
			    + activityUUID + " will not be grouped.");
		    clearGrouping(activity);
		}
	    } else {
		clearGrouping(activity);
	    }
	} else {
	    clearGrouping(activity);
	}

	if (keyExists(activityDetails, WDDXTAGS.ORDER_ID)) {
	    activity.setOrderId(WDDXProcessor.convertToInteger(activityDetails, WDDXTAGS.ORDER_ID));
	}
	if (keyExists(activityDetails, WDDXTAGS.DEFINE_LATER)) {
	    activity.setDefineLater(WDDXProcessor.convertToBoolean(activityDetails, WDDXTAGS.DEFINE_LATER));
	}

	activity.setLearningDesign(learningDesign);

	if (keyExists(activityDetails, WDDXTAGS.LEARNING_LIBRARY_ID)) {
	    Long learningLibraryID = WDDXProcessor.convertToLong(activityDetails, WDDXTAGS.LEARNING_LIBRARY_ID);
	    if (learningLibraryID != null) {
		LearningLibrary library = learningLibraryDAO.getLearningLibraryById(learningLibraryID);
		activity.setLearningLibrary(library);
	    } else {
		activity.setLearningLibrary(null);
	    }
	}

	// Set creation date based on the server timezone, not the client.
	if (activity.getCreateDateTime() == null) {
	    activity.setCreateDateTime(modificationDate);
	}

	if (keyExists(activityDetails, WDDXTAGS.RUN_OFFLINE)) {
	    activity.setRunOffline(WDDXProcessor.convertToBoolean(activityDetails, WDDXTAGS.RUN_OFFLINE));
	}
	if (keyExists(activityDetails, WDDXTAGS.ACTIVITY_CATEGORY_ID)) {
	    activity.setActivityCategoryID(WDDXProcessor.convertToInteger(activityDetails,
		    WDDXTAGS.ACTIVITY_CATEGORY_ID));
	}
	if (keyExists(activityDetails, WDDXTAGS.LIBRARY_IMAGE)) {
	    activity.setLibraryActivityUiImage(WDDXProcessor.convertToString(activityDetails, WDDXTAGS.LIBRARY_IMAGE));
	}

	if (keyExists(activityDetails, WDDXTAGS.GROUPING_SUPPORT_TYPE)) {
	    activity.setGroupingSupportType(WDDXProcessor.convertToInteger(activityDetails,
		    WDDXTAGS.GROUPING_SUPPORT_TYPE));
	}

	if (keyExists(activityDetails, WDDXTAGS.STOP_AFTER_ACTIVITY)) {
	    activity
		    .setStopAfterActivity(WDDXProcessor.convertToBoolean(activityDetails, WDDXTAGS.STOP_AFTER_ACTIVITY));
	}

	return activity;
    }

    private Integer getCoord(Hashtable details, String wddxtag) throws WDDXProcessorConversionException {
	Integer coord = null;
	if (keyExists(details, wddxtag)) {
	    coord = WDDXProcessor.convertToInteger(details, wddxtag);
	}
	return coord == null || coord >= 0 ? coord : ObjectExtractor.DEFAULT_COORD;
    }

    private void clearGrouping(Activity activity) {
	activity.setGrouping(null);
	activity.setGroupingUIID(null);
	activity.setApplyGrouping(false);
    }

    private void setGrouping(Activity activity, Grouping grouping, Integer groupingUIID) {
	activity.setGrouping(grouping);
	activity.setGroupingUIID(groupingUIID);
	activity.setApplyGrouping(true);
    }

    private void processActivityType(Activity activity, Hashtable activityDetails)
	    throws WDDXProcessorConversionException, ObjectExtractorException {
	if (activity.isGroupingActivity()) {
	    buildGroupingActivity((GroupingActivity) activity, activityDetails);
	} else if (activity.isToolActivity()) {
	    buildToolActivity((ToolActivity) activity, activityDetails);
	} else if (activity.isGateActivity()) {
	    buildGateActivity(activity, activityDetails);
	} else {
	    buildComplexActivity((ComplexActivity) activity, activityDetails);
	}
    }

    private void buildComplexActivity(ComplexActivity activity, Hashtable activityDetails)
	    throws WDDXProcessorConversionException, ObjectExtractorException {
	// clear all the children - will be built up again by
	// parseActivitiesToMatchUpParentActivityByParentUIID
	// we don't use all-delete-orphan on the activities relationship so we
	// can do this clear.
	activity.getActivities().clear();

	activity.setDefaultActivity(null);
	Integer defaultActivityMapUIID = WDDXProcessor
		.convertToInteger(activityDetails, WDDXTAGS.DEFAULT_ACTIVITY_UIID);
	if (defaultActivityMapUIID != null) {
	    defaultActivityMap.put(defaultActivityMapUIID, activity);
	}

	if (activity instanceof OptionsActivity) {
	    buildOptionsActivity((OptionsActivity) activity, activityDetails);
	} else if (activity instanceof ParallelActivity) {
	    buildParallelActivity((ParallelActivity) activity, activityDetails);
	} else if (activity instanceof BranchingActivity) {
	    buildBranchingActivity((BranchingActivity) activity, activityDetails);
	} else {
	    buildSequenceActivity((SequenceActivity) activity, activityDetails);
	}
    }

    private void buildBranchingActivity(BranchingActivity branchingActivity, Hashtable activityDetails)
	    throws WDDXProcessorConversionException, ObjectExtractorException {
	if (branchingActivity.isChosenBranchingActivity()) {
	    branchingActivity.setSystemTool(getSystemTool(SystemTool.TEACHER_CHOSEN_BRANCHING));
	} else if (branchingActivity.isGroupBranchingActivity()) {
	    branchingActivity.setSystemTool(getSystemTool(SystemTool.GROUP_BASED_BRANCHING));
	} else if (branchingActivity.isToolBranchingActivity()) {
	    branchingActivity.setSystemTool(getSystemTool(SystemTool.TOOL_BASED_BRANCHING));
	}

	branchingActivity.setStartXcoord(getCoord(activityDetails, WDDXTAGS.START_XCOORD));
	branchingActivity.setStartYcoord(getCoord(activityDetails, WDDXTAGS.START_YCOORD));
	branchingActivity.setEndXcoord(getCoord(activityDetails, WDDXTAGS.END_XCOORD));
	branchingActivity.setEndYcoord(getCoord(activityDetails, WDDXTAGS.END_YCOORD));
    }

    private void buildGroupingActivity(GroupingActivity groupingActivity, Hashtable activityDetails)
	    throws WDDXProcessorConversionException, ObjectExtractorException {
	/**
	 * read the createGroupingUUID, get the Grouping Object, and set CreateGrouping to that object
	 */
	Integer createGroupingUIID = WDDXProcessor.convertToInteger(activityDetails, WDDXTAGS.CREATE_GROUPING_UIID);
	Grouping grouping = groupings.get(createGroupingUIID);
	if (grouping != null) {
	    groupingActivity.setCreateGrouping(grouping);
	    groupingActivity.setCreateGroupingUIID(createGroupingUIID);
	}

	SystemTool systemTool = getSystemTool(SystemTool.GROUPING);
	groupingActivity.setSystemTool(systemTool);

	/*
	 * Hashtable groupingDetails = (Hashtable) activityDetails.get(WDDXTAGS.GROUPING_DTO); if( groupingDetails !=
	 * null ){ Grouping grouping = extractGroupingObject(groupingDetails);
	 * groupingActivity.setCreateGrouping(grouping);
	 * groupingActivity.setCreateGroupingUIID(grouping.getGroupingUIID()); } else {
	 * groupingActivity.setCreateGrouping(null); groupingActivity.setCreateGroupingUIID(null); }
	 */
    }

    private void buildOptionsActivity(OptionsActivity optionsActivity, Hashtable activityDetails)
	    throws WDDXProcessorConversionException {
	if (keyExists(activityDetails, WDDXTAGS.MAX_OPTIONS)) {
	    optionsActivity
		    .setMaxNumberOfOptions(WDDXProcessor.convertToInteger(activityDetails, WDDXTAGS.MAX_OPTIONS));
	}
	if (keyExists(activityDetails, WDDXTAGS.MIN_OPTIONS)) {
	    optionsActivity
		    .setMinNumberOfOptions(WDDXProcessor.convertToInteger(activityDetails, WDDXTAGS.MIN_OPTIONS));
	}
	if (keyExists(activityDetails, WDDXTAGS.OPTIONS_INSTRUCTIONS)) {
	    optionsActivity.setOptionsInstructions(WDDXProcessor.convertToString(activityDetails,
		    WDDXTAGS.OPTIONS_INSTRUCTIONS));
	}
    }

    private void buildParallelActivity(ParallelActivity activity, Hashtable activityDetails)
	    throws WDDXProcessorConversionException {
    }

    private void buildSequenceActivity(SequenceActivity activity, Hashtable activityDetails)
	    throws WDDXProcessorConversionException {
	activity.setSystemTool(getSystemTool(SystemTool.SEQUENCE));
    }

    private void buildToolActivity(ToolActivity toolActivity, Hashtable activityDetails)
	    throws WDDXProcessorConversionException {
	if (log.isDebugEnabled()) {
	    log.debug("In tool activity UUID" + activityDetails.get(WDDXTAGS.ACTIVITY_UIID) + " tool content id="
		    + activityDetails.get(WDDXTAGS.TOOL_CONTENT_ID));
	}
	if (keyExists(activityDetails, WDDXTAGS.TOOL_CONTENT_ID)) {
	    toolActivity.setToolContentId(WDDXProcessor.convertToLong(activityDetails, WDDXTAGS.TOOL_CONTENT_ID));
	}

	if (keyExists(activityDetails, WDDXTAGS.TOOL_ID)) {
	    Tool tool = toolDAO.getToolByID(WDDXProcessor.convertToLong(activityDetails, WDDXTAGS.TOOL_ID));
	    toolActivity.setTool(tool);
	}
    }

    private void buildGateActivity(Object activity, Hashtable activityDetails) throws WDDXProcessorConversionException {
	if (activity instanceof SynchGateActivity) {
	    buildSynchGateActivity((SynchGateActivity) activity, activityDetails);
	} else if (activity instanceof PermissionGateActivity) {
	    buildPermissionGateActivity((PermissionGateActivity) activity, activityDetails);
	} else if (activity instanceof SystemGateActivity) {
	    buildSystemGateActivity((SystemGateActivity) activity, activityDetails);
	} else if (activity instanceof ConditionGateActivity) {
	    buildConditionGateActivity((ConditionGateActivity) activity, activityDetails);
	} else {
	    buildScheduleGateActivity((ScheduleGateActivity) activity, activityDetails);
	}
	GateActivity gateActivity = (GateActivity) activity;
	gateActivity.setGateActivityLevelId(WDDXProcessor.convertToInteger(activityDetails,
		WDDXTAGS.GATE_ACTIVITY_LEVEL_ID));
	gateActivity.setGateOpen(WDDXProcessor.convertToBoolean(activityDetails, WDDXTAGS.GATE_OPEN));

    }

    private void buildSynchGateActivity(SynchGateActivity activity, Hashtable activityDetails)
	    throws WDDXProcessorConversionException {
	activity.setSystemTool(getSystemTool(SystemTool.SYNC_GATE));
    }

    private void buildPermissionGateActivity(PermissionGateActivity activity, Hashtable activityDetails)
	    throws WDDXProcessorConversionException {
	activity.setSystemTool(getSystemTool(SystemTool.PERMISSION_GATE));
    }

    private void buildSystemGateActivity(SystemGateActivity activity, Hashtable activityDetails)
	    throws WDDXProcessorConversionException {
	activity.setSystemTool(getSystemTool(SystemTool.SYSTEM_GATE));
    }

    private void buildScheduleGateActivity(ScheduleGateActivity activity, Hashtable activityDetails)
	    throws WDDXProcessorConversionException {
	// activity.setGateStartDateTime(WDDXProcessor.convertToDate(activityDetails,WDDXTAGS.GATE_START_DATE));
	// activity.setGateEndDateTime(WDDXProcessor.convertToDate(activityDetails,WDDXTAGS.GATE_END_DATE));
	activity.setGateStartTimeOffset(WDDXProcessor.convertToLong(activityDetails, WDDXTAGS.GATE_START_OFFSET));
	activity.setGateEndTimeOffset(WDDXProcessor.convertToLong(activityDetails, WDDXTAGS.GATE_END_OFFSET));
	SystemTool systemTool = getSystemTool(SystemTool.SCHEDULE_GATE);
	activity.setSystemTool(systemTool);
    }

    private void createLessonClass(LessonClass lessonClass, Hashtable groupingDetails)
	    throws WDDXProcessorConversionException {
	if (keyExists(groupingDetails, WDDXTAGS.STAFF_GROUP_ID)) {
	    Group group = groupDAO.getGroupById(WDDXProcessor.convertToLong(groupingDetails, WDDXTAGS.STAFF_GROUP_ID));
	    if (group != null) {
		lessonClass.setStaffGroup(group);
	    }
	}
    }

    /**
     * Create the transition from a WDDX based hashtable. It is easier to go straight to the data object rather than
     * going via the DTO, as the DTO returns the special null values from the getter methods. This makes it hard to set
     * up the transaction object from the transitionDTO.
     * <p>
     * Assumes that all the activities have been read and are in the newActivityMap. The toActivity and fromActivity are
     * only set if the activity exists in the newActivityMap. If this leaves the transition with no to/from activities
     * then null is returned.
     * 
     * @param transitionDetails
     * @throws WDDXProcessorConversionException
     */
    private Transition extractTransitionObject(Hashtable transitionDetails) throws WDDXProcessorConversionException {

	Integer transitionUUID = WDDXProcessor.convertToInteger(transitionDetails, WDDXTAGS.TRANSITION_UIID);
	if (transitionUUID == null) {
	    throw new WDDXProcessorConversionException("Transition is missing its UUID " + transitionDetails);
	}

	Integer toUIID = WDDXProcessor.convertToInteger(transitionDetails, WDDXTAGS.TO_ACTIVITY_UIID);
	if (toUIID == null) {
	    throw new WDDXProcessorConversionException("Transition is missing its toUUID " + transitionDetails);
	}

	Integer fromUIID = WDDXProcessor.convertToInteger(transitionDetails, WDDXTAGS.FROM_ACTIVITY_UIID);
	if (fromUIID == null) {
	    throw new WDDXProcessorConversionException("Transition is missing its fromUUID " + transitionDetails);
	}

	Transition transition = null;
	Transition existingTransition = findTransition(transitionUUID, toUIID, fromUIID);

	if (existingTransition == null) {
	    transition = new Transition();
	} else {
	    transition = existingTransition;
	}

	transition.setTransitionUIID(transitionUUID);

	Activity toActivity = newActivityMap.get(toUIID);
	if (toActivity != null) {
	    transition.setToActivity(toActivity);
	    transition.setToUIID(toUIID);
	    // update the transitionTo property for the activity
	    toActivity.setTransitionTo(transition);
	} else {
	    transition.setToActivity(null);
	    transition.setToUIID(null);
	}

	Activity fromActivity = newActivityMap.get(fromUIID);
	if (fromActivity != null) {
	    transition.setFromActivity(fromActivity);
	    transition.setFromUIID(fromUIID);
	    // update the transitionFrom property for the activity
	    fromActivity.setTransitionFrom(transition);
	} else {
	    transition.setFromActivity(null);
	    transition.setFromUIID(null);
	}

	transition.setDescription(WDDXProcessor.convertToString(transitionDetails, WDDXTAGS.DESCRIPTION));
	transition.setTitle(WDDXProcessor.convertToString(transitionDetails, WDDXTAGS.TITLE));

	// Set creation date based on the server timezone, not the client.
	if (transition.getCreateDateTime() == null) {
	    transition.setCreateDateTime(modificationDate);
	}

	if (transition.getToActivity() != null && transition.getFromActivity() != null) {
	    transition.setLearningDesign(learningDesign);
	    return transition;
	} else {
	    // One of the to/from is missing, can't store this transition. Make
	    // sure we clean up the related activities
	    cleanupTransition(transition);
	    transition.setLearningDesign(null);
	    return null;
	}
    }

    /**
     * Wipe out any links fromany activities that may be linked to it (e.g. the case where a transition has an from
     * activity but not a too activity. These cases should be picked up by Flash, but just in case.
     */
    private void cleanupTransition(Transition transition) {
	if (transition.getFromActivity().getTransitionFrom().equals(transition)) {
	    transition.getFromActivity().setTransitionFrom(null);
	}
	if (transition.getToActivity().getTransitionTo().equals(transition)) {
	    transition.getToActivity().setTransitionTo(null);
	}
    }

    /**
     * Search in learning design for existing object. Can't go to database as that will trigger a Flush, and we haven't
     * updated the rest of the design, so this would trigger a "deleted object would be re-saved by cascade" error.
     * 
     * Check both the UUID for a match, and the to and from for a match. If the user deletes a transition then redraws
     * it between the same activities, then inserting a new one in the db will trigger a duplicate key exception. So we
     * need to reuse any that have the same to/from.
     */
    private Transition findTransition(Integer transitionUUID, Integer toUIID, Integer fromUIID) {
	Transition existingTransition = null;
	Set transitions = learningDesign.getTransitions();
	Iterator iter = transitions.iterator();
	while (existingTransition == null && iter.hasNext()) {
	    Transition element = (Transition) iter.next();
	    if (transitionUUID != null && transitionUUID.equals(element.getTransitionUIID())) {
		existingTransition = element;
	    } else if (toUIID != null && toUIID.equals(element.getToUIID()) && fromUIID != null
		    && fromUIID.equals(element.getFromUIID())) {
		existingTransition = element;
	    }
	}
	return existingTransition;
    }

    /**
     * Checks whether the hashtable contains the key specified by <code>key</code> If the key exists, returns true,
     * otherwise return false.
     * 
     * @param table
     *                The hashtable to check
     * @param key
     *                The key to find
     * @return
     */
    private boolean keyExists(Hashtable table, String key) {
	if (table.containsKey(key)) {
	    return true;
	} else {
	    return false;
	}
    }

    public void setMode(Integer mode) {
	this.mode = mode;
    }

    public Integer getMode() {
	return mode;
    }

    /**
     * Parses the mappings used for branching. They map groups to the sequence activities that form a branch within a
     * branching activity.
     * 
     * Must be done after all the other parsing as we need to match up activities and groups.
     * 
     * Will also delete any old (now unused) mappings
     * 
     * @param branchMappingsList
     * @throws WDDXProcessorConversionException
     */

    private void parseBranchMappings(List branchMappingsList) throws WDDXProcessorConversionException {
	if (branchMappingsList != null) {
	    Iterator iterator = branchMappingsList.iterator();
	    while (iterator.hasNext()) {
		extractBranchActivityEntry((Hashtable) iterator.next());
	    }
	}

	// now go through and delete any old branch mappings that are no longer
	// used.
	// need to remove them from their collections to make sure it isn't
	// accidently re-added.
	Iterator iter = oldbranchActivityEntryList.iterator();
	while (iter.hasNext()) {
	    BranchActivityEntry oldEntry = (BranchActivityEntry) iter.next();
	    SequenceActivity sequenceActivity = oldEntry.getBranchSequenceActivity();
	    if (sequenceActivity != null) {
		sequenceActivity.getBranchEntries().remove(oldEntry);
	    }
	    Group group = oldEntry.getGroup();
	    if (group != null) {
		group.getBranchActivities().remove(oldEntry);
	    }
	    activityDAO.delete(oldEntry);
	}
    }

    @SuppressWarnings("unchecked")
    /**
     * Get the BranchActivityEntry details. This may be either for group based branching, or it may be for tool output
     * based branching
     */
    private BranchActivityEntry extractBranchActivityEntry(Hashtable details) throws WDDXProcessorConversionException {

	Long entryId = WDDXProcessor.convertToLong(details, WDDXTAGS.BRANCH_ACTIVITY_ENTRY_ID);
	Integer entryUIID = WDDXProcessor.convertToInteger(details, WDDXTAGS.BRANCH_ACTIVITY_ENTRY_UIID);
	if (entryUIID == null) {
	    throw new WDDXProcessorConversionException("Group based branch mapping entry is missing its UUID. Entry "
		    + details);
	}

	Integer sequenceActivityUIID = WDDXProcessor.convertToInteger(details, WDDXTAGS.BRANCH_SEQUENCE_ACTIVITY_UIID);
	Integer branchingActivityUIID = WDDXProcessor.convertToInteger(details, WDDXTAGS.BRANCH_ACTIVITY_UIID);

	Activity branchingActivity = newActivityMap.get(branchingActivityUIID);
	if (branchingActivity == null) {
	    throw new WDDXProcessorConversionException(
		    "Branching Activity listed in the branch mapping list is missing. Mapping entry UUID " + entryUIID
			    + " branchingActivityUIID " + branchingActivityUIID);
	}
	if (!branchingActivity.isBranchingActivity() && !branchingActivity.isConditionGate()) {
	    throw new WDDXProcessorConversionException(
		    "Activity listed in the branch mapping list is not a branching activity nor a condition gate. Mapping entry UUID "
			    + entryUIID + " branchingActivityUIID " + branchingActivityUIID);
	}
	// sequence activity is null for a condition gate
	SequenceActivity sequenceActivity = null;
	if (!branchingActivity.isConditionGate()) {
	    Activity activity = newActivityMap.get(sequenceActivityUIID);
	    if (activity == null) {
		throw new WDDXProcessorConversionException(
			"Sequence Activity listed in the branch mapping list is missing. Mapping entry UUID "
				+ entryUIID + " sequenceActivityUIID " + sequenceActivityUIID);
	    } else if (!activity.isSequenceActivity()) {
		throw new WDDXProcessorConversionException(
			"Activity listed in the branch mapping list is not a sequence activity. Mapping entry UUID "
				+ entryUIID + " sequenceActivityUIID " + sequenceActivityUIID);
	    } else {
		sequenceActivity = (SequenceActivity) activity;
	    }
	}

	// If the mapping was created at runtime, there will be an ID but no IU
	// ID field.
	// If it was created in authoring, will have a UI ID and may or may not
	// have an ID.
	// So try to match up on UI ID first, failing that match on ID. Then the
	// worst case, which is the mapping
	// is created at runtime but then modified in authoring (and has has a
	// new UI ID added) is handled.
	BranchActivityEntry uiid_match = null;
	BranchActivityEntry id_match = null;
	Iterator iter = null;
	if (sequenceActivity != null) {
	    if (sequenceActivity.getBranchEntries() != null) {
		iter = sequenceActivity.getBranchEntries().iterator();
	    }
	} else {
	    iter = ((ConditionGateActivity) branchingActivity).getOpeningGateBranchEntries().iterator();
	}

	if (iter != null) {
	    while (uiid_match == null && iter.hasNext()) {
		BranchActivityEntry possibleEntry = (BranchActivityEntry) iter.next();
		if (entryUIID.equals(possibleEntry.getEntryUIID())) {
		    uiid_match = possibleEntry;
		}
		if (entryId != null && entryId.equals(possibleEntry.getEntryId())) {
		    id_match = possibleEntry;
		}
	    }
	}

	BranchActivityEntry entry = uiid_match != null ? uiid_match : id_match;

	// Does it exist already? If it does, remove it from the "old" set
	// (which is used for doing deletions later).
	oldbranchActivityEntryList.remove(entry);

	BranchCondition condition = extractCondition((Hashtable) details.get(WDDXTAGS.BRANCH_CONDITION), entry);

	Integer groupUIID = WDDXProcessor.convertToInteger(details, WDDXTAGS.GROUP_UIID);
	Group group = null;
	if (groupUIID != null) {
	    group = groups.get(groupUIID);
	    if (group == null) {
		throw new WDDXProcessorConversionException(
			"Group listed in the branch mapping list is missing. Mapping entry UUID " + entryUIID
				+ " groupUIID " + groupUIID);
	    }
	}

	if (condition == null && group == null) {
	    throw new WDDXProcessorConversionException(
		    "Branch mapping has neither a group or a condition. Not a valid mapping. " + details);
	}

	if (entry == null) {
	    if (condition != null) {
		entry = condition.allocateBranchToCondition(entryUIID, sequenceActivity, branchingActivity);
	    } else {
		entry = group.allocateBranchToGroup(entryUIID, sequenceActivity, (BranchingActivity) branchingActivity);
	    }
	} else {
	    entry.setEntryUIID(entryUIID);
	    entry.setBranchSequenceActivity(sequenceActivity);
	    entry.setBranchingActivity(branchingActivity);
	}

	entry.setGroup(group);
	entry.setCondition(condition);
	if (sequenceActivity != null) {
	    if (sequenceActivity.getBranchEntries() == null) {
		sequenceActivity.setBranchEntries(new HashSet());
	    }
	    sequenceActivity.getBranchEntries().add(entry);
	    activityDAO.update(sequenceActivity);
	}

	if (group != null) {
	    groupingDAO.update(group);
	}

	return entry;
    }

    /**
     * @param details
     * @param entry
     * @return
     * @throws WDDXProcessorConversionException
     */
    private BranchCondition extractCondition(Hashtable conditionTable, BranchActivityEntry entry)
	    throws WDDXProcessorConversionException {

	BranchCondition condition = null;

	if (conditionTable != null && conditionTable.size() > 0) {

	    Long conditionID = WDDXProcessor.convertToLong(conditionTable, WDDXTAGS.CONDITION_ID);
	    if (entry != null) {
		condition = entry.getCondition();
	    }
	    if (condition != null && conditionID != null && !condition.getConditionId().equals(conditionID)) {
		log
			.warn("Unexpected behaviour: condition supplied in WDDX packet has a different ID to matching branch activity entry in the database. Dropping old database condition."
				+ " Old db condition " + condition + " new entry in WDDX " + conditionTable);
		condition = null; // Hibernate should dispose of it
		// automatically via the cascade
	    }

	    Integer conditionUIID = WDDXProcessor.convertToInteger(conditionTable, WDDXTAGS.CONDITION_UIID);
	    if (conditionUIID == null) {
		throw new WDDXProcessorConversionException("Condition is missing its UUID: " + conditionTable);
	    }
	    String conditionType = WDDXProcessor.convertToString(conditionTable, WDDXTAGS.CONDITION_TYPE);

	    if (BranchCondition.OUTPUT_TYPE_COMPLEX.equals(conditionType)
		    || BranchCondition.OUTPUT_TYPE_STRING.equals(conditionType)) {
		if (condition == null) {
		    // This is different than "conditionID" !!!
		    Long newConditionID = WDDXProcessor.convertToLong(conditionTable, "conditionId");
		    BranchCondition originalCondition = branchActivityEntryDAO.getConditionByID(newConditionID);
		    if (originalCondition == null) {
			log.error("Could not find condition with given ID: " + conditionID);
		    } else {
			condition = (BranchCondition) originalCondition.clone();
			condition.setConditionUIID(conditionUIID);
		    }
		}
		else {
		    condition.setConditionUIID(conditionUIID);
		}
		
	    } else if (condition == null) {
		condition = new BranchCondition(null, conditionUIID, WDDXProcessor.convertToInteger(conditionTable,
			WDDXTAGS.ORDER_ID), WDDXProcessor.convertToString(conditionTable, WDDXTAGS.CONDITION_NAME),
			WDDXProcessor.convertToString(conditionTable, WDDXTAGS.CONDITION_DISPLAY_NAME), WDDXProcessor
				.convertToString(conditionTable, WDDXTAGS.CONDITION_TYPE), WDDXProcessor
				.convertToString(conditionTable, WDDXTAGS.CONDITION_START_VALUE), WDDXProcessor
				.convertToString(conditionTable, WDDXTAGS.CONDITION_END_VALUE), WDDXProcessor
				.convertToString(conditionTable, WDDXTAGS.CONDITION_EXACT_MATCH_VALUE));
	    } else {
		condition.setConditionUIID(conditionUIID);
		condition
			.setDisplayName(WDDXProcessor.convertToString(conditionTable, WDDXTAGS.CONDITION_DISPLAY_NAME));
		condition.setEndValue(WDDXProcessor.convertToString(conditionTable, WDDXTAGS.CONDITION_END_VALUE));
		condition.setExactMatchValue(WDDXProcessor.convertToString(conditionTable,
			WDDXTAGS.CONDITION_EXACT_MATCH_VALUE));
		condition.setName(WDDXProcessor.convertToString(conditionTable, WDDXTAGS.CONDITION_NAME));
		condition.setOrderId(WDDXProcessor.convertToInteger(conditionTable, WDDXTAGS.ORDER_ID));
		condition.setStartValue(WDDXProcessor.convertToString(conditionTable, WDDXTAGS.CONDITION_START_VALUE));
		condition.setType(WDDXProcessor.convertToString(conditionTable, WDDXTAGS.CONDITION_TYPE));
	    }
	}
	return condition;
    }

    private SystemTool getSystemTool(Long systemToolId) {
	SystemTool tool = systemTools.get(systemToolId);
	if (tool == null) {
	    tool = systemToolDAO.getSystemToolByID(systemToolId);
	    if (tool != null) {
		systemTools.put(systemToolId, tool);
	    } else {
		log.error("ObjectExtractor: Unable to find matching system tool for id " + systemToolId);
	    }
	}
	return tool;
    }

    private void createLearnerChoiceGrouping(LearnerChoiceGrouping learnerChoiceGrouping, Hashtable groupingDetails)
	    throws WDDXProcessorConversionException {

	Integer numLearnersPerGroup = WDDXProcessor.convertToInteger(groupingDetails, WDDXTAGS.LEARNERS_PER_GROUP);
	if (numLearnersPerGroup != null && numLearnersPerGroup.intValue() > 0) {
	    learnerChoiceGrouping.setLearnersPerGroup(numLearnersPerGroup);
	    learnerChoiceGrouping.setNumberOfGroups(null);
	    learnerChoiceGrouping.setEqualNumberOfLearnersPerGroup(null);
	} else {
	    Integer numGroups = WDDXProcessor.convertToInteger(groupingDetails, WDDXTAGS.NUMBER_OF_GROUPS);
	    if (numGroups != null && numGroups.intValue() > 0) {
		learnerChoiceGrouping.setNumberOfGroups(numGroups);
	    } else {
		learnerChoiceGrouping.setNumberOfGroups(null);
	    }
	    learnerChoiceGrouping.setLearnersPerGroup(null);
	    Boolean equalNumberOfLearnersPerGroup = WDDXProcessor.convertToBoolean(groupingDetails,
		    WDDXTAGS.EQUAL_NUMBER_OF_LEARNERS_PER_GROUP);
	    if (equalNumberOfLearnersPerGroup != null) {
		learnerChoiceGrouping.setEqualNumberOfLearnersPerGroup(equalNumberOfLearnersPerGroup);
	    }
	}
    }

    private void buildConditionGateActivity(ConditionGateActivity activity, Hashtable activityDetails)
	    throws WDDXProcessorConversionException {
	activity.setSystemTool(getSystemTool(SystemTool.CONDITION_GATE));
    }
}
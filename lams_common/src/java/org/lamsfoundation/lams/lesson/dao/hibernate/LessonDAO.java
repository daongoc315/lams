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
/* $$Id$$ */
package org.lamsfoundation.lams.lesson.dao.hibernate;

import java.util.Date;
import java.util.List;

import org.hibernate.FetchMode;
import org.hibernate.Query;
import org.hibernate.criterion.Restrictions;
import org.lamsfoundation.lams.dao.hibernate.LAMSBaseDAO;
import org.lamsfoundation.lams.learningdesign.Activity;
import org.lamsfoundation.lams.learningdesign.LearningDesign;
import org.lamsfoundation.lams.lesson.LearnerProgress;
import org.lamsfoundation.lams.lesson.Lesson;
import org.lamsfoundation.lams.lesson.dao.ILessonDAO;
import org.lamsfoundation.lams.usermanagement.Role;
import org.lamsfoundation.lams.usermanagement.User;
import org.springframework.stereotype.Repository;

/**
 * Hibernate implementation of ILessonDAO
 * 
 * @author chris
 */
@Repository
public class LessonDAO extends LAMSBaseDAO implements ILessonDAO {
    private final static String FIND_LESSON_BY_CREATOR = "from " + Lesson.class.getName()
	    + " lesson where lesson.user.userId=? and lesson.lessonStateId <= 6 and "
	    + " lesson.learningDesign.copyTypeID=" + LearningDesign.COPY_TYPE_LESSON;
    private final static String FIND_PREVIEW_BEFORE_START_DATE = "from " + Lesson.class.getName()
	    + " lesson where lesson.learningDesign.copyTypeID=" + LearningDesign.COPY_TYPE_PREVIEW
	    + "and lesson.startDateTime is not null and lesson.startDateTime < ?";
    private final static String COUNT_ACTIVE_LEARNERS = "select count(distinct progress.user.id)" + " from "
	    + LearnerProgress.class.getName() + " progress" + " where progress.lesson.id = :lessonId";
    private final static String FIND_LESSON_FOR_ACTIVITY = "select lesson from " + Lesson.class.getName() + " lesson, "
	    + Activity.class.getName() + " activity "
	    + " where activity.activityId=:activityId and activity.learningDesign=lesson.learningDesign";
    private final static String LESSONS_WITH_ORIGINAL_LEARNING_DESIGN = "select l from " + Lesson.class.getName()
	    + " l " + "where l.learningDesign.originalLearningDesign.learningDesignId = ? "
	    + "and l.learningDesign.copyTypeID != " + LearningDesign.COPY_TYPE_PREVIEW + " " + "and l.lessonStateId = "
	    + Lesson.STARTED_STATE + " " + "and l.organisation.organisationId = ? " + " order by l.lessonName";
    private final static String LESSONS_BY_GROUP = "from " + Lesson.class.getName()
	    + " where organisation.organisationId=? and lessonStateId <= 6";
    private final static String LESSON_BY_SESSION_ID = "select lesson from Lesson lesson, ToolSession session where "
	    + "session.lesson=lesson and session.toolSessionId=:toolSessionID";
    private final static String COUNT_LEARNERS_CLASS = "SELECT COUNT(*) FROM lams_lesson AS lesson "
	    + "JOIN lams_grouping AS grouping ON lesson.class_grouping_id = grouping.grouping_id "
	    + "JOIN lams_group AS gr USING (grouping_id) JOIN lams_user_group AS ug USING (group_id) "
	    + "WHERE lesson_id = :lessonId";

    /**
     * Retrieves the Lesson. Used in instances where it cannot be lazy loaded so it forces an initialize.
     * 
     * @param lessonId
     *            identifies the lesson to get
     * @return the lesson
     */
    @Override
    public Lesson getLesson(Long lessonId) {
	Lesson lesson = (Lesson) getSession().get(Lesson.class, lessonId);
	return lesson;
    }

    @Override
    public Lesson getLessonWithJoinFetchedProgress(final Long lessonId) {

	return (Lesson) getSession().createCriteria(Lesson.class).add(Restrictions.like("lessonId", lessonId))
		.setFetchMode("learnerProgresses", FetchMode.JOIN).uniqueResult();
    }

    /** Get all the lessons in the database. This includes the disabled lessons. */
    @Override
    public List getAllLessons() {
	return loadAll(Lesson.class);
    }

    /**
     * Gets all lessons that are active for a learner.
     * 
     * @param learner
     *            a User that identifies the learner.
     * @return a List with all active lessons in it.
     */
    @Override
    public List getActiveLessonsForLearner(final User learner) {

	Query query = getSession().getNamedQuery("activeLessonsAllOrganisations");
	query.setInteger("userId", learner.getUserId().intValue());
	List result = query.list();
	return result;
    }

    /**
     * Gets all lessons that are active for a learner, in a given organisation
     * 
     * @param learnerId
     *            a User that identifies the learner.
     * @param organisationId
     *            the desired organisation.
     * @return a List with all active lessons in it.
     */
    @Override
    public List<Lesson> getActiveLessonsForLearner(final Integer learnerId, final Integer organisationId) {

	Query query = getSession().getNamedQuery("activeLessons");
	query.setInteger("userId", learnerId);
	query.setInteger("organisationId", organisationId);
	List result = query.list();
	return result;
    }

    /**
     * @see org.lamsfoundation.lams.lesson.dao.ILessonDAO#getActiveLearnerByLesson(long)
     */
    @Override
    public List getActiveLearnerByLesson(final long lessonId) {

	Query query = getSession().getNamedQuery("activeLearners");
	query.setLong("lessonId", lessonId);
	List result = query.list();
	return result;
    }

    /**
     * @see org.lamsfoundation.lams.lesson.dao.ILessonDAO#getActiveLearnerByLessonAndGroup(long, long)
     */
    @Override
    public List getActiveLearnerByLessonAndGroup(final long lessonId, final long groupId) {
	Query query = getSession().getNamedQuery("activeLearnersByGroup");
	query.setLong("lessonId", lessonId);
	query.setLong("groupId", groupId);
	List result = query.list();
	return result;
    }

    /**
     * @see org.lamsfoundation.lams.lesson.dao.ILessonDAO#getActiveLearnerByLesson(long) Note: Hibernate 3.1
     *      query.uniqueResult() returns Integer, Hibernate 3.2 query.uniqueResult() returns Long
     */
    @Override
    public Integer getCountActiveLearnerByLesson(final long lessonId) {
	Query query = getSession().createQuery(LessonDAO.COUNT_ACTIVE_LEARNERS);
	query.setLong("lessonId", lessonId);
	Object value = query.uniqueResult();
	return new Integer(((Number) value).intValue());
    }

    @Override
    public Integer getCountLearnerByLesson(final long lessonId) {
	Query query = getSession().createSQLQuery(LessonDAO.COUNT_LEARNERS_CLASS);
	query.setLong("lessonId", lessonId);
	Object value = query.uniqueResult();
	return ((Number) value).intValue();
    }

    /**
     * Saves or Updates a Lesson.
     * 
     * @param lesson
     */
    @Override
    public void saveLesson(Lesson lesson) {
	getSession().save(lesson);
    }

    /**
     * Deletes a Lesson <b>permanently</b>.
     * 
     * @param lesson
     */
    @Override
    public void deleteLesson(Lesson lesson) {
	getSession().delete(lesson);
    }

    /**
     * Update the lesson data
     * 
     * @see org.lamsfoundation.lams.lesson.dao.ILessonDAO#updateLesson(org.lamsfoundation.lams.lesson.Lesson)
     */
    @Override
    public void updateLesson(Lesson lesson) {
	getSession().update(lesson);
    }

    /**
     * Returns the list of available Lessons created by a given user. Does not return disabled lessons or preview
     * lessons.
     * 
     * @param userID
     *            The user_id of the user
     * @return List The list of Lessons for the given user
     */
    @Override
    public List getLessonsCreatedByUser(Integer userID) {
	List lessons = this.doFind(LessonDAO.FIND_LESSON_BY_CREATOR, userID);
	return lessons;
    }

    /**
     * Gets all lessons in the given organisation, for which this user is in the staff group. Does not return disabled
     * lessons or preview lessons. This is the list of lessons that a user may monitor/moderate/manage.
     * 
     * @param user
     *            a User that identifies the teacher/staff member.
     * @return a List with all appropriate lessons in it.
     */
    @Override
    public List getLessonsForMonitoring(final int userID, final int organisationID) {
	Query query = getSession().getNamedQuery("lessonsForMonitoring");
	query.setInteger("userId", userID);
	query.setInteger("organisationId", organisationID);
	List result = query.list();
	return result;
    }

    /**
     * Get all the preview lessons more with the creation date before the given date.
     * 
     * @param startDate
     *            UTC date
     * @return the list of Lessons
     */
    @Override
    public List getPreviewLessonsBeforeDate(final Date startDate) {
	List lessons = this.doFind(LessonDAO.FIND_PREVIEW_BEFORE_START_DATE, startDate);
	return lessons;
    }

    /**
     * Get the lesson that applies to this activity. Not all activities have an attached lesson.
     */
    @Override
    public Lesson getLessonForActivity(final long activityId) {
	Query query = getSession().createQuery(LessonDAO.FIND_LESSON_FOR_ACTIVITY);
	query.setLong("activityId", activityId);
	return (Lesson) query.uniqueResult();
    }

    /**
     * @see org.lamsfoundation.lams.lesson.dao.ILessonDAO#getLessonsByOrgAndUserWithCompletedFlag(Integer, Integer,
     *      boolean)
     */
    @Override
    public List getLessonsByOrgAndUserWithCompletedFlag(final Integer userId, final Integer orgId,
	    final Integer userRole) {

	String queryName;
	if (Role.ROLE_MONITOR.equals(userRole)) {
	    queryName = "staffLessonsByOrgAndUserWithCompletedFlag";
	} else if (Role.ROLE_LEARNER.equals(userRole)) {
	    queryName = "learnerLessonsByOrgAndUserWithCompletedFlag";
	} else {
	    // in case of Role.ROLE_GROUP_MANAGER
	    queryName = "allLessonsByOrgAndUserWithCompletedFlag";
	}

	Query query = getSession().getNamedQuery(queryName);
	query.setInteger("userId", userId.intValue());
	query.setInteger("orgId", orgId.intValue());
	List result = query.list();
	return result;
    }

    /**
     * @see org.lamsfoundation.lams.lesson.dao.ILessonDAO#getLessonsByOrgAndUserWithCompletedFlag(Integer, Integer,
     *      boolean)
     */
    @Override
    public List getLessonsByGroupAndUser(final Integer userId, final Integer orgId) {
	Query query = getSession().getNamedQuery("lessonsByOrgAndUserWithChildOrgs");
	query.setInteger("userId", userId.intValue());
	query.setInteger("orgId", orgId.intValue());
	List result = query.list();
	return result;
    }

    @Override
    public List getLessonsByGroup(final Integer orgId) {
	return this.doFind(LessonDAO.LESSONS_BY_GROUP, orgId);
    }

    /**
     * @see org.lamsfoundation.lams.lesson.dao.ILessonDAO#getLessonsByOriginalLearningDesign(Integer)
     */
    @Override
    public List getLessonsByOriginalLearningDesign(final Long ldId, final Integer orgId) {
	Object[] args = { ldId.longValue(), orgId.intValue() };
	List lessons = this.doFind(LessonDAO.LESSONS_WITH_ORIGINAL_LEARNING_DESIGN, args);
	return lessons;
    }

    /**
     * @see org.lamsfoundation.lams.lesson.dao.ILessonDAO#getMonitorsByToolSessionId(Long)
     */
    @Override
    @SuppressWarnings("unchecked")
    public List<User> getMonitorsByToolSessionId(Long sessionId) {
	return (List<User>) this.doFindByNamedQueryAndNamedParam("monitorsByToolSessionId", "sessionId", sessionId);
    }

    /**
     * @see org.lamsfoundation.lams.lesson.dao.ILessonDAO#getLessonDetailsFromSessionID(java.lang.Long)
     */
    @Override
    public Lesson getLessonFromSessionID(final Long toolSessionID) {
	Query query = getSession().createQuery(LessonDAO.LESSON_BY_SESSION_ID);
	query.setLong("toolSessionID", toolSessionID);
	return (Lesson) query.uniqueResult();
    }
}

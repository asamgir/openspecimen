
package com.krishagni.catissueplus.core.administrative.repository.impl;

import static com.krishagni.catissueplus.core.common.util.Utility.numberToLong;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import com.krishagni.catissueplus.core.administrative.domain.ForgotPasswordToken;
import com.krishagni.catissueplus.core.administrative.domain.User;
import com.krishagni.catissueplus.core.administrative.repository.UserDao;
import com.krishagni.catissueplus.core.administrative.repository.UserListCriteria;
import com.krishagni.catissueplus.core.common.events.DependentEntityDetail;
import com.krishagni.catissueplus.core.common.events.UserSummary;
import com.krishagni.catissueplus.core.common.repository.AbstractDao;

public class UserDaoImpl extends AbstractDao<User> implements UserDao {
	
	@Override
	public Class<?> getType() {
		return User.class;
	}
	
	@SuppressWarnings("unchecked")
	public List<UserSummary> getUsers(UserListCriteria listCrit) {
		Criteria criteria = sessionFactory.getCurrentSession()
				.createCriteria(User.class, "u")
				.setProjection(Projections.countDistinct("u.id"))
				.setFirstResult(listCrit.startAt())
				.setMaxResults(listCrit.maxResults())
				.addOrder(Order.asc("u.lastName"))
				.addOrder(Order.asc("u.firstName"));
		
		addSearchConditions(criteria, listCrit);
		addProjectionFields(criteria);
		
		return getUsers(criteria.list());
	}
	
	@SuppressWarnings("unchecked")
	public List<User> getUsersByIds(List<Long> userIds) {
		return sessionFactory.getCurrentSession()
				.getNamedQuery(GET_USERS_BY_IDS)
				.setParameterList("userIds", userIds)
				.list();
	}

	@SuppressWarnings("unchecked")
	public User getUser(String loginName, String domainName) {
		List<User> users = sessionFactory.getCurrentSession()
				.getNamedQuery(GET_USER_BY_LOGIN_NAME)
				.setString("loginName", loginName)
				.setString("domainName", domainName)
				.list();
		
		return users.isEmpty() ? null : users.get(0);
	}
	
	@Override
	public User getSystemUser() {
		return getUser(User.SYS_USER, "openspecimen");
	}
	
	@SuppressWarnings("unchecked")
	public User getUserByEmailAddress(String emailAddress) {
		List<User> users = sessionFactory.getCurrentSession()
				.getNamedQuery(GET_USER_BY_EMAIL_ADDRESS)
				.setString("emailAddress", emailAddress)
				.list();
		
		return users.isEmpty() ? null : users.get(0);
	}
	
	@SuppressWarnings("unchecked")
	public List<DependentEntityDetail> getDependentEntities(Long userId) {
		List<Object[]> rows = sessionFactory.getCurrentSession()
				.getNamedQuery(GET_DEPENDENT_ENTITIES)
				.setLong("userId", userId)
				.list();
		
		return getDependentEntities(rows);
	}
	
	@SuppressWarnings("unchecked")
	public ForgotPasswordToken getFpToken(String token) {
		List<ForgotPasswordToken> result = sessionFactory.getCurrentSession()
				.getNamedQuery(GET_FP_TOKEN)
				.setString("token", token)
				.list();
		
		return result.isEmpty() ? null : result.get(0);
	}
	
	@SuppressWarnings("unchecked")
	public ForgotPasswordToken getFpTokenByUser(Long userId) {
		List<ForgotPasswordToken> result = sessionFactory.getCurrentSession()
				.getNamedQuery(GET_FP_TOKEN_BY_USER)
				.setLong("userId", userId)
				.list();
		
		return result.isEmpty() ? null : result.get(0);
	}
	
	@Override
	public void saveFpToken(ForgotPasswordToken token) {
		sessionFactory.getCurrentSession().saveOrUpdate(token);
	};
	
	@Override
	public void deleteFpToken(ForgotPasswordToken token) {
		sessionFactory.getCurrentSession().delete(token);
	}
	
	private List<UserSummary> getUsers(List<Object[]> rows) {
		List<UserSummary> result = new ArrayList<UserSummary>();
		for (Object[] row : rows) {			
			result.add(getUserSummary(row));			
		}
		
		return result;		
	}

	private UserSummary getUserSummary(Object[] row) {
		UserSummary userSummary = new UserSummary();
		userSummary.setId(numberToLong(row[0]));
		userSummary.setFirstName((String)row[1]);
		userSummary.setLastName((String)row[2]);
		userSummary.setLoginName((String)row[3]);
		return userSummary;		
	}
	
	private void addSearchConditions(Criteria criteria, UserListCriteria listCrit) {
		String searchString = listCrit.query();
		
		if (StringUtils.isBlank(searchString)) {
			addNameRestriction(criteria, listCrit.name());
			addLoginNameRestriction(criteria, listCrit.loginName());
		} else {
			Criterion srchCond = Restrictions.disjunction()
					.add(Restrictions.ilike("u.firstName", searchString, MatchMode.ANYWHERE))
					.add(Restrictions.ilike("u.lastName", searchString, MatchMode.ANYWHERE))
					.add(Restrictions.ilike("u.loginName", searchString, MatchMode.ANYWHERE));
			
			criteria.add(srchCond);
		}
		
		addActivityStatusRestriction(criteria, listCrit.activityStatus());
		addInstituteRestriction(criteria, listCrit.instituteId());
	}

	private void addNameRestriction(Criteria criteria, String name) {
		if (StringUtils.isBlank(name)) {
			return;
		}
		
		Criterion nameCond = Restrictions.disjunction()
				.add(Restrictions.ilike("u.firstName", name, MatchMode.ANYWHERE))
				.add(Restrictions.ilike("u.lastName", name, MatchMode.ANYWHERE));
		criteria.add(nameCond);
	}
	
	private void addLoginNameRestriction(Criteria criteria, String loginName) {
		if (StringUtils.isBlank(loginName)) {
			return;
		}
		
		Criterion loginNameCond = Restrictions.ilike("u.loginName", loginName, MatchMode.ANYWHERE);
		criteria.add(loginNameCond);
	}
	
	private void addActivityStatusRestriction(Criteria criteria, String activityStatus) {
		if (StringUtils.isBlank(activityStatus)) {
			return;
		}
		
		Criterion activityStatusCond = Restrictions.eq("u.activityStatus", activityStatus);
		criteria.add(activityStatusCond);
	}
	
	private void addInstituteRestriction(Criteria criteria, Long instituteId) {
		if (instituteId == null) {
			return;
		}
		
		criteria.createAlias("u.department", "dept")
			.createAlias("dept.institute", "institute")
			.add(Restrictions.eq("institute.id", instituteId));
	}

	private void addProjectionFields(Criteria criteria) {
		criteria.setProjection(Projections.distinct(
				Projections.projectionList()
					.add(Projections.property("u.id"), "id")
					.add(Projections.property("u.firstName"), "firstName")
					.add(Projections.property("u.lastName"), "lastName")
					.add(Projections.property("u.loginName"), "loginName")
		));
	}
	
	private List<DependentEntityDetail> getDependentEntities(List<Object[]> rows) {
		List<DependentEntityDetail> dependentEntities = new ArrayList<DependentEntityDetail>();
		
		for (Object[] row: rows) {
			String name = (String)row[0];
			int count = ((Number)row[1]).intValue();
			dependentEntities.add(DependentEntityDetail.from(name, count));
		}
		
		return dependentEntities;
 	}
	
	private static final String FQN = User.class.getName();

	private static final String GET_USERS_BY_IDS = FQN + ".getUsersByIds";
	
	private static final String GET_USER_BY_LOGIN_NAME = FQN + ".getUserByLoginName";

	private static final String GET_USER_BY_EMAIL_ADDRESS = FQN + ".getUserByEmailAddress";
	
	private static final String GET_DEPENDENT_ENTITIES = FQN + ".getDependentEntities"; 
	
	private static final String TOKEN_FQN = ForgotPasswordToken.class.getName();
	
	private static final String GET_FP_TOKEN_BY_USER = TOKEN_FQN + ".getFpTokenByUser";
	
	private static final String GET_FP_TOKEN = TOKEN_FQN + ".getFpToken";

}


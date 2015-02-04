
package com.krishagni.catissueplus.core.administrative.events;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.krishagni.catissueplus.core.administrative.domain.DistributionProtocol;
import com.krishagni.catissueplus.core.administrative.domain.User;
import com.krishagni.catissueplus.core.common.events.UserSummary;

public class DistributionProtocolDetails {
	private Long id;

	private UserSummary principalInvestigator;

	private String title;

	private String shortTitle;

	private String irbId;

	private Date startDate;

	private String activityStatus;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public UserSummary getPrincipalInvestigator() {
		return principalInvestigator;
	}

	public void setPrincipalInvestigator(UserSummary principalInvestigator) {
		this.principalInvestigator = principalInvestigator;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getShortTitle() {
		return shortTitle;
	}

	public void setShortTitle(String shortTitle) {
		this.shortTitle = shortTitle;
	}

	public String getIrbId() {
		return irbId;
	}

	public void setIrbId(String irbId) {
		this.irbId = irbId;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public String getActivityStatus() {
		return activityStatus;
	}

	public void setActivityStatus(String activityStatus) {
		this.activityStatus = activityStatus;
	}

	public static DistributionProtocolDetails from(DistributionProtocol distributionProtocol) {

		DistributionProtocolDetails details = new DistributionProtocolDetails();
		details.setShortTitle(distributionProtocol.getShortTitle());
		details.setId(distributionProtocol.getId());
		details.setTitle(distributionProtocol.getTitle());
		details.setIrbId(distributionProtocol.getIrbId());
		details.setStartDate(distributionProtocol.getStartDate());
		details.setPrincipalInvestigator(getPrincipleInvestigatorInfo(distributionProtocol.getPrincipalInvestigator()));
		details.setActivityStatus(distributionProtocol.getActivityStatus());
		return details;
	}

	private static UserSummary getPrincipleInvestigatorInfo(User principleInvestigator) {
		UserSummary pi = new UserSummary();
		pi.setLoginName(principleInvestigator.getLoginName());
		if (principleInvestigator.getAuthDomain() != null) {
			pi = UserSummary.from(principleInvestigator);
		}

		return pi;
	}
	
	public static List<DistributionProtocolDetails> from(List<DistributionProtocol> distributionProtocols) {
		List<DistributionProtocolDetails> list = new ArrayList<DistributionProtocolDetails>();
		
		for (DistributionProtocol dp : distributionProtocols) {
			list.add(from(dp));
		}
		
		return list;
	}

}

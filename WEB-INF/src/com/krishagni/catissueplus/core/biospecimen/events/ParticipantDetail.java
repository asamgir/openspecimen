
package com.krishagni.catissueplus.core.biospecimen.events;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.krishagni.catissueplus.core.biospecimen.domain.CollectionProtocolRegistration;
import com.krishagni.catissueplus.core.biospecimen.domain.Participant;
import com.krishagni.catissueplus.core.common.AttributeModifiedSupport;
import com.krishagni.catissueplus.core.common.ListenAttributeChanges;

@ListenAttributeChanges
public class ParticipantDetail extends AttributeModifiedSupport {
	private Long id;
	
	private String firstName;

	private String lastName;
	
	private String middleName;

	private Date birthDate;

	private Date deathDate;

	private String gender;

	private Set<String> races;

	private String vitalStatus;

	private List<PmiDetail> pmis;

	private String sexGenotype;

	private String ethnicity;

	private String ssn;

	private String activityStatus;
	
	private String empi;
	
	private boolean phiAccess;
	
	private Set<String> registeredCps;
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getMiddleName() {
		return middleName;
	}

	public void setMiddleName(String middleName) {
		this.middleName = middleName;
	}

	public Date getBirthDate() {
		return birthDate;
	}

	public void setBirthDate(Date birthDate) {
		this.birthDate = birthDate;
	}

	public Date getDeathDate() {
		return deathDate;
	}

	public void setDeathDate(Date deathDate) {
		this.deathDate = deathDate;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public Set<String> getRaces() {
		return races;
	}

	public void setRaces(Set<String> races) {
		this.races = races;
	}

	public String getVitalStatus() {
		return vitalStatus;
	}

	public void setVitalStatus(String vitalStatus) {
		this.vitalStatus = vitalStatus;
	}

	public List<PmiDetail> getPmis() {
		return pmis;
	}

	public void setPmis(List<PmiDetail> pmis) {
		this.pmis = pmis;
	}

	public String getSexGenotype() {
		return sexGenotype;
	}

	public void setSexGenotype(String sexGenotype) {
		this.sexGenotype = sexGenotype;
	}

	public String getEthnicity() {
		return ethnicity;
	}

	public void setEthnicity(String ethnicity) {
		this.ethnicity = ethnicity;
	}

	public String getSsn() {
		return ssn;
	}

	public void setSsn(String ssn) {
		this.ssn = ssn;
	}

	public String getActivityStatus() {
		return activityStatus;
	}

	public void setActivityStatus(String activityStatus) {
		this.activityStatus = activityStatus;
	}

	public String getEmpi() {
		return empi;
	}

	public void setEmpi(String empi) {
		this.empi = empi;
	}
	
	public boolean getPhiAccess() {
		return phiAccess;
	}

	public void setPhiAccess(boolean phiAccess) {
		this.phiAccess = phiAccess;
	}

	public Set<String> getRegisteredCps() {
		return registeredCps;
	}

	public void setRegisteredCps(Set<String> registeredCps) {
		this.registeredCps = registeredCps;
	}

	public static ParticipantDetail from(Participant participant, boolean excludePhi) {
		ParticipantDetail result = new ParticipantDetail();
		result.setId(participant.getId());
		result.setFirstName(excludePhi ? "###" : participant.getFirstName());
		result.setLastName(excludePhi ? "###" : participant.getLastName());
		result.setMiddleName(excludePhi ? "###" : participant.getMiddleName());
		result.setActivityStatus(participant.getActivityStatus());
		result.setBirthDate(excludePhi ? null : participant.getBirthDate());
		result.setDeathDate(excludePhi ? null : participant.getDeathDate());
		result.setEthnicity(participant.getEthnicity());
		result.setGender(participant.getGender());
		result.setEmpi(excludePhi ? "###" : participant.getEmpi());				
		result.setPmis(PmiDetail.from(participant.getPmis(), excludePhi)); 
		result.setRaces(new HashSet<String>(participant.getRaces()));
		result.setSexGenotype(participant.getSexGenotype());
		result.setSsn(excludePhi ? "###" : participant.getSocialSecurityNumber());
		result.setVitalStatus(participant.getVitalStatus());
		result.setPhiAccess(!excludePhi);
		
		Set<String> cps = new HashSet<String>();
		for (CollectionProtocolRegistration cpr : participant.getCprs()) {
			cps.add(cpr.getCollectionProtocol().getShortTitle());
		}
		result.setRegisteredCps(cps);
		return result;
	}
	
	public static List<ParticipantDetail> from(List<Participant> participants, boolean excludePhi) {
		List<ParticipantDetail> result = new ArrayList<ParticipantDetail>();
		for (Participant participant : participants) {
			result.add(ParticipantDetail.from(participant, excludePhi));
		}
		
		return result;
	}
}

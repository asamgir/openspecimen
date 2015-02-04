
package com.krishagni.catissueplus.core.administrative.events;

import com.krishagni.catissueplus.core.common.events.EventStatus;
import com.krishagni.catissueplus.core.common.events.ResponseEvent;

public class DistributionProtocolDetailEvent extends ResponseEvent {

	private Long id;

	private String title;

	private DistributionProtocolDetails details;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public DistributionProtocolDetails getDetails() {
		return details;
	}

	public void setDetails(DistributionProtocolDetails details) {
		this.details = details;
	}

	public static DistributionProtocolDetailEvent notFound(Long id) {
		DistributionProtocolDetailEvent resp = new DistributionProtocolDetailEvent();
		resp.setId(id);
		resp.setStatus(EventStatus.NOT_FOUND);
		return resp;
	}

	public static DistributionProtocolDetailEvent notFound(String title) {
		DistributionProtocolDetailEvent resp = new DistributionProtocolDetailEvent();
		resp.setTitle(title);
		resp.setStatus(EventStatus.NOT_FOUND);
		return resp;
	}

	public static DistributionProtocolDetailEvent ok(DistributionProtocolDetails details) {
		DistributionProtocolDetailEvent event = new DistributionProtocolDetailEvent();
		event.setDetails(details);
		event.setStatus(EventStatus.OK);
		return event;
	}

}

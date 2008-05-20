/**
 * <p>Title: DisposalEventParametersAction Class>
 * <p>Description:	This class initializes the fields in the DisposalEventParameters Add/Edit webpage.</p>
 * Copyright:    Copyright (c) year
 * Company: Washington University, School of Medicine, St. Louis.
 * @author Mandar Deshmukh
 * @version 1.00
 * Created on Aug 05, 2005
 */

package edu.wustl.catissuecore.action;

import javax.servlet.http.HttpServletRequest;

import edu.wustl.catissuecore.actionForm.DisposalEventParametersForm;
import edu.wustl.catissuecore.actionForm.EventParametersForm;
import edu.wustl.catissuecore.util.global.Constants;

/**
 * @author vaishali_khandelwal
 *  * This class initializes the fields in the DisposalEventParameters Add/Edit webpage.
 */
public class DisposalEventParametersAction extends SpecimenEventParametersAction
{

	/**
	 * @param request object of HttpServletRequest
	 * @throws Exception generic exception
	 */
	protected void setRequestParameters(HttpServletRequest request, EventParametersForm eventParametersForm) throws Exception
	{
		String formName=null;
	    boolean readOnlyValue;
	    DisposalEventParametersForm disposalEventParametersForm=(DisposalEventParametersForm)eventParametersForm;
	    if (disposalEventParametersForm.getOperation().equals(Constants.EDIT))
	    {
	        formName = Constants.DISPOSAL_EVENT_PARAMETERS_EDIT_ACTION;
	        readOnlyValue = true;
	    }
	    else
	    {
	        formName = Constants.DISPOSAL_EVENT_PARAMETERS_ADD_ACTION;
	        readOnlyValue = false;
	    }
	   // String changeAction = "setFormAction('" + formName + "');";
	    String deleteAction="deleteObject('" + formName+"?disposal=true" +"','" + Constants.BIO_SPECIMEN + "')";
		request.setAttribute("formName", formName);
		request.setAttribute("readOnlyValue", readOnlyValue);
		//request.setAttribute("changeAction", changeAction);
		request.setAttribute("deleteAction", deleteAction);
		/*DisposalEventParametersForm form = (DisposalEventParametersForm) request
				.getAttribute("disposalEventParametersForm");*/
		
//		request.setAttribute(Constants.ACTIVITYSTATUSLIST,
//				Constants.DISPOSAL_EVENT_ACTIVITY_STATUS_VALUES);
		request.setAttribute("activityStatusDisabled",Constants.ACTIVITY_STATUS_DISABLED);
		request.setAttribute("activityStatusClosed",Constants.ACTIVITY_STATUS_CLOSED);
		request.setAttribute("activityStatusList",Constants.DISPOSAL_EVENT_ACTIVITY_STATUS_VALUES);
		request.setAttribute("disposalEventParametersAddAction",Constants.DISPOSAL_EVENT_PARAMETERS_ADD_ACTION);

	}
	
}

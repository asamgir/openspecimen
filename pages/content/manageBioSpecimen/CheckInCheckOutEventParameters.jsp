<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/nlevelcombo.tld" prefix="ncombo" %>
<%@ page import="edu.wustl.catissuecore.util.global.Constants"%>
<%@ include file="/pages/content/common/AutocompleterCommon.jsp" %> 
<%@ page language="java" isELIgnored="false" %>

<head>
<!-- Mandar : 434 : for tooltip -->
<script language="JavaScript" type="text/javascript" src="jss/javaScript.js"></script>
<!-- Mandar 21-Aug-06 : For calendar changes -->
<script src="jss/calendarComponent.js"></script>
<SCRIPT>var imgsrc="images/";</SCRIPT>
<LINK href="css/calanderComponent.css" type=text/css rel=stylesheet>
<!-- Mandar 21-Aug-06 : calendar changes end -->
	<script language="javascript">
		
	</script>
</head>
	

			
<html:errors/>
    
<table summary="" cellpadding="0" cellspacing="0" border="0" class="contentPage" width="600">

<html:form action='${requestScope.formName}'>


	<!-- NEW CHECKIN_CHECKOUTEventParameter REGISTRATION BEGINS-->
	<tr>
	<td>
	
	<table summary="" cellpadding="3" cellspacing="0" border="0">
		<tr>
			<td><html:hidden property="operation" /></td>
		</tr>
		
		<tr>
			<td><html:hidden property="id" /></td>
		</tr>

		<tr>
			<td>
				<html:hidden property="specimenId" value='${requestScope.specimenId}'/>
			</td>
		</tr>
		
		
		<tr>
			 <td class="formMessage" colspan="3">*indicates a required field</td>
		</tr>

		<tr>
			<td class="formTitle" height="20" colspan="3">
				<logic:equal name="operation" value='${requestScope.addForJSP}'>
					<bean:message key="checkincheckouteventparameter.title"/>
				</logic:equal>
				<logic:equal name="operation" value='${requestScope.editForJSP}'>
					<bean:message key="checkincheckouteventparameter.edittitle"/>
				</logic:equal>
			</td>
		</tr>

		<!-- Name of the checkincheckouteventparameter -->
<!-- User -->		
		<tr>
			<td class="formRequiredNotice" width="5">*</td>
			<td class="formRequiredLabel">
				<label for="type">
					<bean:message key="eventparameters.user"/> 
				</label>
			</td>
			<td class="formField">
<!-- Mandar : 434 : for tooltip -->
				<html:select property="userId" styleClass="formFieldSized" styleId="userId" size="1"
				 onmouseover="showTip(this.id)" onmouseout="hideTip(this.id)">
					<html:options collection='${requestScope.userListforJSP}' labelProperty="name" property="value"/>
				</html:select>
			</td>
		</tr>

<!-- date -->		
		<tr>
			<td class="formRequiredNotice" width="5">*</td>
			<td class="formRequiredLabel">
				<label for="type">
					<bean:message key="eventparameters.dateofevent"/> 
				</label>
			</td>
			<td class="formField">
				
	<logic:notEmpty name="currentEventParametersDate">
<ncombo:DateTimeComponent name="dateOfEvent"
			  id="dateOfEvent"
			  formName="checkInCheckOutEventParametersForm"
			                  month='${requestScope.eventParametersMonth}'
							  year='${requestScope.eventParametersYear}'
							  day='${requestScope.eventParametersDay}'
							  value='${requestScope.currentEventParametersDate}'
			  styleClass="formDateSized10"
					/>
</logic:notEmpty>
<logic:empty name="currentEventParametersDate">
<ncombo:DateTimeComponent name="dateOfEvent"
			  id="dateOfEvent"
			  formName="checkInCheckOutEventParametersForm"
			  styleClass="formDateSized10"
					/>
</logic:empty>
				
				
				<bean:message key="page.dateFormat" />&nbsp;

			</td>
		</tr>
		
		
		
		<!-- hours & minutes -->	
		<!-- 
			 Bug ID:  AutocompleteBugID
			 Patch ID: AutocompleteBugID_2
			 Description:html:select tag is replaced by Autocomplete
	   -->	
		<tr>
			<td class="formRequiredNotice" width="5">*</td>
			<td class="formRequiredLabel">
				<label for="eventparameters.time">
					<bean:message key="eventparameters.time"/>
				</label>
			</td>
			<td class="formField">
				 <autocomplete:AutoCompleteTag property="timeInHours"
					   optionsList = '${requestScope.hourList}'
					   initialValue='${checkInCheckOutEventParametersForm.timeInHours}'
					  styleClass="formFieldSized5"
					  staticField="false"
			    />	
				&nbsp;
				<label for="eventparameters.timeinhours">
					<bean:message key="eventparameters.timeinhours"/>&nbsp; 
				</label>
                   <autocomplete:AutoCompleteTag property="timeInMinutes"
						  optionsList = '${requestScope.minutesList}'
						  initialValue='${checkInCheckOutEventParametersForm.timeInMinutes}'
						  styleClass="formFieldSized5"
						  staticField="false"
				    />	
				<label for="eventparameters.timeinminutes">
					&nbsp;<bean:message key="eventparameters.timeinminutes"/> 
				</label>
			</td>
		</tr>

<!-- checkincheckouteventparameter.storagestatus -->	
	
		<tr>
			<td class="formRequiredNotice" width="5">*</td>
			<td class="formRequiredLabel">
				<label for="type">
					<bean:message key="checkincheckouteventparameter.storagestatus"/> 
				</label>
			</td>
			<td class="formField">
				<autocomplete:AutoCompleteTag property="storageStatus"
					  optionsList ='${requestScope.storageStatusList}'
					  initialValue='${checkInCheckOutEventParametersForm.storageStatus}'
			    />	
			</td>
		</tr>

<!-- comments -->		
		<tr>
			<td class="formRequiredNotice" width="5">&nbsp;</td>
			<td class="formLabel">
				<label for="type">
					<bean:message key="eventparameters.comments"/> 
				</label>
			</td>
			<td class="formField">
				<html:textarea styleClass="formFieldSized"  styleId="comments" property="comments" />
			</td>
		</tr>

<!-- buttons -->
		<tr>
		  <td align="right" colspan="3">
			<!-- action buttons begins -->
			
			<table cellpadding="4" cellspacing="0" border="0">
				<tr>
					<td>
						<html:submit styleClass="actionButton" value="Submit" onclick='${requestScope.changeAction}'/>
					</td>
					<%-- td><html:reset styleClass="actionButton"/></td --%> 
				</tr>
			</table>
			<!-- action buttons end -->
			</td>
		</tr>

		</table>
		
	  </td>
	 </tr>

	 <!-- NEW CheckInCheckOutEventParameters ends-->
	 
	 </html:form>
 </table>
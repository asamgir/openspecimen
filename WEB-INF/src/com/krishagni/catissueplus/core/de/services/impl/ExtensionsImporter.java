package com.krishagni.catissueplus.core.de.services.impl;

import java.util.HashMap;
import java.util.Map;

import com.krishagni.catissueplus.core.biospecimen.domain.CollectionProtocol;
import com.krishagni.catissueplus.core.biospecimen.domain.CollectionProtocolRegistration;
import com.krishagni.catissueplus.core.biospecimen.domain.Specimen;
import com.krishagni.catissueplus.core.biospecimen.domain.Visit;
import com.krishagni.catissueplus.core.biospecimen.domain.factory.CprErrorCode;
import com.krishagni.catissueplus.core.biospecimen.domain.factory.SpecimenErrorCode;
import com.krishagni.catissueplus.core.biospecimen.domain.factory.VisitErrorCode;
import com.krishagni.catissueplus.core.biospecimen.repository.DaoFactory;
import com.krishagni.catissueplus.core.common.errors.OpenSpecimenException;
import com.krishagni.catissueplus.core.common.events.RequestEvent;
import com.krishagni.catissueplus.core.common.events.ResponseEvent;
import com.krishagni.catissueplus.core.de.domain.FormErrorCode;
import com.krishagni.catissueplus.core.de.events.FormDataDetail;
import com.krishagni.catissueplus.core.de.repository.FormDao;
import com.krishagni.catissueplus.core.de.services.FormService;
import com.krishagni.catissueplus.core.importer.events.ImportObjectDetail;
import com.krishagni.catissueplus.core.importer.services.ObjectImporter;

import edu.common.dynamicextensions.domain.nui.Container;
import edu.common.dynamicextensions.napi.FormData;

public class ExtensionsImporter implements ObjectImporter<Map<String, Object>> {
	
	private FormService formSvc;
	
	private FormDao formDao;
	
	private DaoFactory daoFactory;
	
	public void setFormSvc(FormService formSvc) {
		this.formSvc = formSvc;
	}
	
	public void setFormDao(FormDao formDao) {
		this.formDao = formDao;
	}
	
	public void setDaoFactory(DaoFactory daoFactory) {
		this.daoFactory = daoFactory;
	}

	@SuppressWarnings("unchecked")
	@Override
	public ResponseEvent<Map<String, Object>> importObject(RequestEvent<ImportObjectDetail<Map<String, Object>>> req) {
		try {
			ImportObjectDetail<Map<String, Object>> importDetail = req.getPayload();
						
			Map<String, Object> params = importDetail.getParams();			
			String formName = (String)params.get("formName");
			Container form = Container.getContainer(formName);
			if (form == null) {
				return ResponseEvent.userError(FormErrorCode.NOT_FOUND);
			}
			
			Map<String, Object> extnObj = importDetail.getObject();
			String entityType = (String)params.get("entityType");
			Long objectId = null;
			CollectionProtocol cp = null;
			
			if (entityType.equals("Participant")) {
				String ppid = (String)extnObj.get("ppid");
				String cpShortTitle = (String)extnObj.get("cpShortTitle");
				CollectionProtocolRegistration cpr = daoFactory.getCprDao().getCprByCpShortTitleAndPpid(cpShortTitle, ppid);
				if (cpr == null) {
					return ResponseEvent.userError(CprErrorCode.NOT_FOUND);
				}
				
				objectId = cpr.getId();
				cp = cpr.getCollectionProtocol();				
			} else if (entityType.equals("SpecimenCollectionGroup")) {
				String visitName = (String)extnObj.get("visitName");
				Visit visit = daoFactory.getVisitsDao().getByName(visitName);
				if (visit == null) {
					return ResponseEvent.userError(VisitErrorCode.NOT_FOUND);
				}
				
				objectId = visit.getId();
				cp = visit.getCollectionProtocol();
			} else if (entityType.equals("Specimen") || entityType.equals("SpecimenEvent")) {
				String label = (String)extnObj.get("specimenLabel");
				Specimen specimen = daoFactory.getSpecimenDao().getByLabel(label);
				if (specimen == null) {
					return ResponseEvent.userError(SpecimenErrorCode.NOT_FOUND, label);
				}
				
				objectId = specimen.getId();
				cp = specimen.getCollectionProtocol();
			}
			
			Long formCtxId = formDao.getFormCtxtId(form.getId(), entityType, cp.getId());
			if (formCtxId == null) {
				return ResponseEvent.userError(FormErrorCode.NO_ASSOCIATION, cp.getShortTitle(), form.getCaption());
			}
			
			Map<String, Object> appData = new HashMap<String, Object>();
			appData.put("formCtxtId", formCtxId);
			appData.put("objectId", objectId);
			
			Map<String, Object> formValueMap = (Map<String, Object>)extnObj.get("formValueMap");
			formValueMap.put("appData", appData);
			
			String recordId = (String)extnObj.get("recordId");
			if (recordId != null) {
				formValueMap.put("id", Long.parseLong(recordId));
			}
									
			FormData formData = FormData.getFormData(form, formValueMap, true, null);
			
			FormDataDetail formDataDetail = new FormDataDetail();
			formDataDetail.setFormId(form.getId());
			formDataDetail.setRecordId(formData.getRecordId());			
			formDataDetail.setFormData(formData);			
			ResponseEvent<FormDataDetail> resp = formSvc.saveFormData(new RequestEvent<FormDataDetail>(formDataDetail));
			resp.throwErrorIfUnsuccessful();
			
			return ResponseEvent.response(resp.getPayload().getFormData().getFieldNameValueMap(true));
		} catch (OpenSpecimenException ose) {
			return ResponseEvent.error(ose);
		} catch (Exception e) {
			return ResponseEvent.serverError(e);
		}
	}
}

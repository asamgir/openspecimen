package edu.wustl.catissuecore.action;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import edu.wustl.catissuecore.actionForm.SpecimenCollectionGroupForm;
import edu.wustl.catissuecore.actionForm.ViewSpecimenSummaryForm;
import edu.wustl.catissuecore.bean.CollectionProtocolEventBean;
import edu.wustl.catissuecore.bean.GenericSpecimen;
import edu.wustl.catissuecore.bean.GenericSpecimenVO;
import edu.wustl.catissuecore.bizlogic.BizLogicFactory;
import edu.wustl.catissuecore.bizlogic.StorageContainerBizLogic;
import edu.wustl.catissuecore.domain.MolecularSpecimen;
import edu.wustl.catissuecore.domain.Specimen;
import edu.wustl.catissuecore.domain.SpecimenCharacteristics;
import edu.wustl.catissuecore.domain.SpecimenCollectionGroup;
import edu.wustl.catissuecore.domain.StorageContainer;
import edu.wustl.catissuecore.util.CollectionProtocolUtil;
import edu.wustl.catissuecore.util.SpecimenAutoStorageContainer;
import edu.wustl.catissuecore.util.global.Constants;
import edu.wustl.common.action.BaseAction;
import edu.wustl.common.beans.NameValueBean;
import edu.wustl.common.beans.SessionDataBean;
import edu.wustl.common.dao.AbstractDAO;
import edu.wustl.common.dao.DAO;
import edu.wustl.common.dao.DAOFactory;
import edu.wustl.common.util.dbManager.DAOException;
import edu.wustl.common.util.logger.Logger;

public class AnticipatorySpecimenViewAction extends BaseAction {

	private String globalSpecimenId = null;
	private SessionDataBean bean;
	protected LinkedHashMap<String, ArrayList<GenericSpecimenVO>> autoStorageSpecimenMap =new LinkedHashMap<String, ArrayList<GenericSpecimenVO>> ();
	Long cpId = null;
	protected HashSet<String> storageContainerIds = new HashSet<String>();
	String target = null;
	SpecimenAutoStorageContainer autoStorageContainer; 
	public ActionForward executeAction(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		target=Constants.SUCCESS;
		SpecimenCollectionGroupForm specimenCollectionGroupForm=
			(SpecimenCollectionGroupForm)form;
		HttpSession session = request.getSession();
		Long id = specimenCollectionGroupForm.getId();
		DAO dao = DAOFactory.getInstance().getDAO(Constants.HIBERNATE_DAO);
		bean = (SessionDataBean) session.getAttribute(Constants.SESSION_DATA);
		((AbstractDAO)dao).openSession(bean);
		try{
			target=Constants.SUCCESS;
			session.setAttribute("SCGFORM", specimenCollectionGroupForm.getId());
			List cpList = dao.retrieve(SpecimenCollectionGroup.class.getName(), "id", id);
			if(cpList != null && !cpList.isEmpty())
			{
				autoStorageContainer = new SpecimenAutoStorageContainer ();
				SpecimenCollectionGroup specimencollectionGroup = (SpecimenCollectionGroup) cpList.get(0);
				if(specimencollectionGroup.getActivityStatus().equalsIgnoreCase(Constants.ACTIVITY_STATUS_DISABLED))
				{
					target=Constants.ACTIVITY_STATUS_DISABLED;
				}
				LinkedHashMap<String, CollectionProtocolEventBean> cpEventMap = new LinkedHashMap<String, CollectionProtocolEventBean> ();
				cpId = specimencollectionGroup.getCollectionProtocolRegistration().getCollectionProtocol().getId();
				CollectionProtocolEventBean eventBean = new CollectionProtocolEventBean();
	
				eventBean.setUniqueIdentifier(String.valueOf(specimencollectionGroup.getId().longValue()));

				eventBean.setSpecimenRequirementbeanMap(getSpecimensMap(
						specimencollectionGroup.getSpecimenCollection(),cpId ));
				globalSpecimenId = "E"+eventBean.getUniqueIdentifier() + "_";
				cpEventMap.put(globalSpecimenId, eventBean);			
				session.removeAttribute(Constants.COLLECTION_PROTOCOL_EVENT_SESSION_MAP);
				session
				.setAttribute(Constants.COLLECTION_PROTOCOL_EVENT_SESSION_MAP, cpEventMap);			
			
				request.setAttribute("RequestType",ViewSpecimenSummaryForm.REQUEST_TYPE_ANTICIPAT_SPECIMENS);
				((AbstractDAO)dao).closeSession();
				autoStorageContainer.setCollectionProtocol(cpId);
				autoStorageContainer.setSpecimenStoragePositions(bean);				
				return mapping.findForward(target);
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			((AbstractDAO)dao).closeSession();
		}
		return null;
	}

	protected LinkedHashMap<String, GenericSpecimen> getSpecimensMap(
			Collection specimenCollection, long collectionProtocolId)
				throws DAOException
	{
		LinkedHashMap<String, GenericSpecimen> specimenMap = 
						new LinkedHashMap<String, GenericSpecimen>();
		autoStorageSpecimenMap.clear();
		Iterator specimenIterator = specimenCollection.iterator();
		while(specimenIterator.hasNext())
		{
			Specimen specimen = (Specimen)specimenIterator.next();
			if (specimen.getParentSpecimen() == null)
			{
				GenericSpecimenVO specBean =getSpecimenBean(specimen, null);
				specBean.setUniqueIdentifier("S_"+specimen.getId());
				specBean.setCollectionProtocolId(collectionProtocolId);
				specimenMap.put("S_"+specimen.getId(), specBean);				
			}
			
		}
		return specimenMap;
	}
	
	protected LinkedHashMap<String, GenericSpecimen> getChildAliquots(Specimen specimen) throws DAOException
	{
		Collection specimenChildren = specimen.getChildrenSpecimen();
		Iterator iterator = specimenChildren.iterator();
		LinkedHashMap<String, GenericSpecimen>  aliquotMap = new
			LinkedHashMap<String, GenericSpecimen> ();
		while(iterator.hasNext())
		{
			Specimen childSpecimen = (Specimen) iterator.next();
			if(Constants.ALIQUOT.equals(childSpecimen.getLineage()))
			{
				GenericSpecimenVO specimenBean = getSpecimenBean(childSpecimen, specimen.getLabel());
				specimenBean.setUniqueIdentifier("al_" + specimen.getId() +"_"+ childSpecimen.getId());
				aliquotMap.put("al_" + specimen.getId() +"_"+ childSpecimen.getId(), specimenBean);
			}
		}
		return aliquotMap;
	}

	protected LinkedHashMap<String, GenericSpecimen> getChildDerived(Specimen specimen) throws DAOException
	{
		Collection specimenChildren = specimen.getChildrenSpecimen();
		Iterator iterator = specimenChildren.iterator();
		LinkedHashMap<String, GenericSpecimen>  derivedMap = new
			LinkedHashMap<String, GenericSpecimen> ();
		while(iterator.hasNext())
		{
			Specimen childSpecimen = (Specimen) iterator.next();
			if(Constants.DERIVED_SPECIMEN.equals(childSpecimen.getLineage()))
			{
				GenericSpecimenVO specimenBean = getSpecimenBean(childSpecimen, specimen.getLabel());
				specimenBean.setUniqueIdentifier("dr_" + specimen.getId() +"_"+ childSpecimen.getId());
				derivedMap.put("dr_" + specimen.getId() +"_"+ childSpecimen.getId(), specimenBean);
			}
		}
		return derivedMap;
	}	
	
	protected GenericSpecimenVO getSpecimenBean(Specimen specimen, String parentName) throws DAOException
	{
		GenericSpecimenVO specimenDataBean = new GenericSpecimenVO();
		specimenDataBean.setBarCode(specimen.getBarcode());
		specimenDataBean.setClassName(specimen.getClassName());
//		specimenDataBean.setCreatedOn(specimen.getCreatedOn());
		specimenDataBean.setDisplayName(specimen.getLabel());
		specimenDataBean.setPathologicalStatus(specimen.getPathologicalStatus());
		specimenDataBean.setId(specimen.getId().longValue());
		specimenDataBean.setParentName(parentName);
		if(specimen.getInitialQuantity()!=null)
		{	
			specimenDataBean.setQuantity(specimen.getInitialQuantity().getValue().toString());
		}
//		specimenDataBean.setAvailable(Boolean.TRUE);
//		specimenDataBean.setAvailableQuantity(availableQuantity);
//		specimenDataBean.setInitialQuantity(availableQuantity);
		//specimenDataBean.setConcentration(specimen.get);
	
		specimenDataBean.setCheckedSpecimen(true);
		if (Constants.SPECIMEN_COLLECTED.equals(specimen.getCollectionStatus()))
		{
			specimenDataBean.setReadOnly(true);
		}
		specimenDataBean.setType(specimen.getType());
//		specimenDataBean.setStorageContainerForSpecimen("Virtual");
		SpecimenCharacteristics characteristics = specimen.getSpecimenCharacteristics();
		if (characteristics != null)
		{
			specimenDataBean.setTissueSide(characteristics.getTissueSide());
			specimenDataBean.setTissueSite(characteristics.getTissueSite());
		}
		//specimenDataBean.setExternalIdentifierCollection(specimen.getExternalIdentifierCollection());
		//specimenDataBean.setBiohazardCollection(specimen.getBiohazardCollection());
		//specimenDataBean.setSpecimenEventCollection(specimen.getSpecimenEventCollection());
		
//		specimenDataBean.setSpecimenCollectionGroup(specimen.getSpecimenCollectionGroup());
				
//		specimenDataBean.setStorageContainer(null);
		String concentration ="";
		if ("Molecular".equals(specimen.getClassName()))
		{
			concentration =String.valueOf(
					((MolecularSpecimen) specimen).getConcentrationInMicrogramPerMicroliter()
					);
		}
		specimenDataBean.setConcentration(concentration);
		StorageContainer container = specimen.getStorageContainer();
		Logger.out.info("-----------Container while getting from domain--:"+container);
		String storageType = null;
		if (container != null)
		{
			specimenDataBean.setContainerId( String.valueOf(container.getId()));
			specimenDataBean.setSelectedContainerName(container.getName());
			specimenDataBean.setPositionDimensionOne(String.valueOf(specimen.getPositionDimensionOne()));
			specimenDataBean.setPositionDimensionTwo(String.valueOf(specimen.getPositionDimensionTwo()));

			specimenDataBean.setStorageContainerForSpecimen("Auto");

		}
		else
		{
			//TODO:After model change 
			if(specimen.getPositionDimensionOne() == null)
			{
				specimenDataBean.setStorageContainerForSpecimen("Virtual");
			}
			else
			{
				storageType = 
					CollectionProtocolUtil.getStorageTypeValue(specimen.getPositionDimensionOne());
				specimenDataBean.setStorageContainerForSpecimen(storageType);
			}
		}
		if ("Auto".equals(storageType))
		{

			autoStorageContainer.addSpecimen(specimenDataBean, specimenDataBean.getClassName());
			
		}
		specimenDataBean.setAliquotSpecimenCollection(getChildAliquots(specimen));
		specimenDataBean.setDeriveSpecimenCollection(getChildDerived(specimen));
		return specimenDataBean;
	}
		
}

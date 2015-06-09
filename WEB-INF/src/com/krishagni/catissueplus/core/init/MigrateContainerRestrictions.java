
package com.krishagni.catissueplus.core.init;

import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import com.krishagni.catissueplus.core.administrative.domain.StorageContainer;
import com.krishagni.catissueplus.core.biospecimen.domain.CollectionProtocol;
import com.krishagni.catissueplus.core.biospecimen.repository.DaoFactory;
import com.krishagni.catissueplus.core.common.errors.OpenSpecimenException;
import com.krishagni.catissueplus.core.common.errors.ParameterizedError;

public class MigrateContainerRestrictions implements InitializingBean {

	private PlatformTransactionManager txnMgr;

	private DaoFactory daoFactory;

	private JdbcTemplate jdbcTemplate;

	private static Set<String> tissueTypes;

	private static Set<String> fluidTypes;

	private static Set<String> molecularTypes;

	private static Set<String> cellTypes;

	public void setTxnMgr(PlatformTransactionManager txnMgr) {
		this.txnMgr = txnMgr;
	}

	public void setDaoFactory(DaoFactory daoFactory) {
		this.daoFactory = daoFactory;
	}

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		try {
			migrate();
		}
		finally {
			cleanup();
		}
	}

	public void migrate() throws Exception {
		final List<Long> parentCntIds = getParentCntrIds();

		TransactionTemplate txnTmpl = new TransactionTemplate(txnMgr);
		txnTmpl.setPropagationBehavior(TransactionTemplate.PROPAGATION_REQUIRED);
		txnTmpl.execute(new TransactionCallback<Void>() {

			@Override
			public Void doInTransaction(TransactionStatus status) {
				try {
					tissueTypes = new HashSet<String>(getTypes("Tissue"));
					fluidTypes = new HashSet<String>(getTypes("Fluid"));
					cellTypes = new HashSet<String>(getTypes("Cell"));
					molecularTypes = new HashSet<String>(getTypes("Molecular"));
					migrateRestrictions(parentCntIds);
					return null;
				}
				catch (Exception e) {
					status.setRollbackOnly();
					throw new RuntimeException(e);
				}
				finally {
					SecurityContextHolder.clearContext();
				}
			}
		});
	}

	protected void cleanup() {

	}

	private void migrateRestrictions(List<Long> contnrIds) throws IllegalAccessException, InvocationTargetException {
		for (Long contId : contnrIds) {
			System.out.println("Processing container with  ID: " + contId);
			StorageContainer existingContainer = daoFactory.getStorageContainerDao().getById(contId);
			migrateRestrictions(existingContainer);
		}
	}

	private void migrateRestrictions(StorageContainer existingContainer) throws IllegalAccessException,
			InvocationTargetException {
		System.out.println("Processing container with  ID: " + existingContainer.getId());
		StorageContainer newContainer = new StorageContainer();
		BeanUtils.copyProperties(newContainer, existingContainer);

		Set<String> specimenTypeRestrictions = getTypeRestrictions(existingContainer.getId());
		Set<String> specimenClassRestrictions = getClassRestrictions(specimenTypeRestrictions);

		Set<String> allowedCps = getCpRestrictions(existingContainer.getId());
		List<CollectionProtocol> cps = new ArrayList<CollectionProtocol>();

		if (!CollectionUtils.isEmpty(allowedCps)) {
			cps = daoFactory.getCollectionProtocolDao().getCpsByShortTitle(allowedCps);
		}

		newContainer.setAllowedCps(new HashSet<CollectionProtocol>(cps));

		newContainer.setAllowedSpecimenClasses(specimenClassRestrictions);
		newContainer.setAllowedSpecimenTypes(specimenTypeRestrictions);

		if (existingContainer.getParentContainer() != null) {

			if (!existingContainer.getParentContainer().getAllowedCps().containsAll(newContainer.getAllowedCps())) {
				existingContainer.getParentContainer().getAllowedCps().addAll(newContainer.getAllowedCps());
			}
			if (!existingContainer.getParentContainer().getAllowedSpecimenClasses()
					.containsAll(newContainer.getAllowedSpecimenClasses())) {
				existingContainer.getParentContainer().getAllowedSpecimenClasses()
						.addAll(newContainer.getAllowedSpecimenClasses());
			}
			List<String> allAllowedTypes = daoFactory.getPermissibleValueDao().getSpecimenTypes(
					existingContainer.getParentContainer().getAllowedSpecimenClasses());
			if (!(existingContainer.getParentContainer().getAllowedSpecimenTypes().isEmpty()
					&& !existingContainer.getParentContainer().getAllowedSpecimenTypes()
							.containsAll(newContainer.getAllowedSpecimenTypes()))
					|| !allAllowedTypes.containsAll(newContainer.getAllowedSpecimenTypes())) {
				existingContainer.getParentContainer().getAllowedSpecimenTypes().addAll(newContainer.getAllowedSpecimenTypes());
			}
			if (CollectionUtils.isEqualCollection(newContainer.getAllowedCps(), existingContainer.getParentContainer()
					.getAllowedCps())) {
				newContainer.getAllowedCps().clear();
			}
			if (CollectionUtils.isEqualCollection(newContainer.getAllowedSpecimenClasses(), existingContainer
					.getParentContainer().getAllowedSpecimenClasses())) {
				newContainer.getAllowedSpecimenClasses().clear();
			}
			if (CollectionUtils.isEqualCollection(newContainer.getAllowedSpecimenTypes(), existingContainer
					.getParentContainer().getAllowedSpecimenTypes())) {
				newContainer.getAllowedSpecimenTypes().clear();
			}
		}

		try {
			existingContainer.update(newContainer);
		}
		catch (OpenSpecimenException e) {
			System.out.println("Error for container with ID: " + existingContainer.getId());
			System.out.println("Error for container with Name: " + existingContainer.getName());
			List<ParameterizedError> errors = e.getErrors();
			for (ParameterizedError error : errors) {
				System.out.println("Error: " + error.error());
				System.out.println("params: " + error.params());
			}
			throw new RuntimeException(e);
		}

		daoFactory.getStorageContainerDao().saveOrUpdate(existingContainer, true);
		for (StorageContainer childContainer : existingContainer.getChildContainers()) {
			migrateRestrictions(childContainer);
		}
	}

	private List<Long> getParentCntrIds() {
		List<Long> result = jdbcTemplate
				.query(
						"select identifier from os_storage_containers where parent_container_id is null and activity_status <> 'Disabled'",
						new RowMapper<Long>() {

							public Long mapRow(ResultSet result, int rowNum) throws SQLException {
								return result.getLong("identifier");
							}

						});
		return result;
	}

	private Set<String> getTypeRestrictions(Long contId) {
		String sql = "select specimen_type as specimen_type from catissue_stor_cont_spec_type where storage_container_id = ?";

		List<String> result = jdbcTemplate.query(sql, new Object[]{contId}, new RowMapper<String>() {

			public String mapRow(ResultSet result, int rowNum) throws SQLException {
				return result.getString("specimen_type");
			}

		});
		return new HashSet<String>(result);
	}

	private Set<String> getClassRestrictions(Set<String> specimenTypeRestrictions) {
		Set<String> classrestrictions = new HashSet<String>();

		if (specimenTypeRestrictions.containsAll(tissueTypes)) {
			classrestrictions.add("Tissue");
			specimenTypeRestrictions.removeAll(tissueTypes);
		}

		if (specimenTypeRestrictions.containsAll(fluidTypes)) {
			classrestrictions.add("Fluid");
			specimenTypeRestrictions.removeAll(fluidTypes);
		}

		if (specimenTypeRestrictions.containsAll(cellTypes)) {
			classrestrictions.add("Cell");
			specimenTypeRestrictions.removeAll(cellTypes);
		}

		if (specimenTypeRestrictions.containsAll(molecularTypes)) {
			classrestrictions.add("Molecular");
			specimenTypeRestrictions.removeAll(molecularTypes);
		}

		return classrestrictions;
	}

	private Set<String> getCpRestrictions(Long contId) {
		String sql = "select cp.short_title as short_title from catissue_st_cont_coll_prot_rel cpRel "
				+ " join catissue_collection_protocol cp on cpRel.collection_protocol_id = cp.identifier"
				+ " where storage_container_id = ?";
		List<String> result = jdbcTemplate.query(sql, new Object[]{contId}, new RowMapper<String>() {

			public String mapRow(ResultSet result, int rowNum) throws SQLException {
				return result.getString("short_title");
			}

		});
		return new HashSet<String>(result);
	}

	private List<String> getTypes(String specimenClass) {
		return daoFactory.getPermissibleValueDao().getSpecimenTypes(java.util.Arrays.asList(specimenClass));
	}

}

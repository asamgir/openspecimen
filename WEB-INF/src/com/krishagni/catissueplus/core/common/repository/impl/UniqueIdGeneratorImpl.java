package com.krishagni.catissueplus.core.common.repository.impl;

import java.util.List;

import org.hibernate.LockMode;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.krishagni.catissueplus.core.common.domain.KeySequence;
import com.krishagni.catissueplus.core.common.repository.AbstractDao;
import com.krishagni.catissueplus.core.common.repository.UniqueIdGenerator;

public class UniqueIdGeneratorImpl extends AbstractDao<KeySequence> implements UniqueIdGenerator {

	@SuppressWarnings("unchecked")
	@Override
	public Long getUniqueId(String type, String id) {
		Transaction tx = null;
		Session session = null; 
		
		try {
			session = sessionFactory.openSession();
			tx = session.beginTransaction();
			List<KeySequence> seqs = session
					.getNamedQuery(GET_BY_TYPE_AND_TYPE_ID)
					.setLockMode("ks", LockMode.PESSIMISTIC_WRITE)
					.setString("type", type)
					.setString("typeId", id)
					.list();
			
			KeySequence seq = null;
			if (seqs.isEmpty()) {
				seq = new KeySequence();
				seq.setType(type);
				seq.setTypeId(id);			
			} else {
				seq = seqs.iterator().next();
			}
			
			Long uniqueId = seq.increment();
			
			session.saveOrUpdate(seq);
			tx.commit();
			return uniqueId;
		} catch (Exception e) {
			if (tx != null) {
				tx.rollback();
			}
			throw new RuntimeException(e);
		} finally {
			if (session != null) {
				session.close();
			}
		}
	}
	
	private static final String FQN = KeySequence.class.getName();
	
	private String GET_BY_TYPE_AND_TYPE_ID = FQN + ".getByTypeAndTypeId";
}
package ch.redhat.app.xa.service.impl;


import ch.redhat.app.xa.entity.Pair;
import ch.redhat.app.xa.service.PairService;

import javax.ejb.*;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Stateless
@TransactionManagement(TransactionManagementType.CONTAINER)
public class DefaultPairService implements PairService {

    @PersistenceContext
    private EntityManager em;

    @TransactionAttribute(value = TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public Pair find(String key) {
        return em.find(Pair.class, key);
    }

    // force existing transaction (for testing), this make sure the mdb is starting a transaction
    @TransactionAttribute(value = TransactionAttributeType.MANDATORY)
    @Override
    public void set(Pair pair) {
        em.persist(pair);
        em.flush();
        em.clear();
    }

    @Override
    @TransactionAttribute(value = TransactionAttributeType.MANDATORY)
    public void setWithRollback(Pair pair) {
        throw new RuntimeException("Something wrong for: " + pair);
    }

    @TransactionAttribute(value = TransactionAttributeType.NOT_SUPPORTED)
    @Override
    @SuppressWarnings("unchecked")
    public List<Pair> findAll() {
        return em.createQuery("Select p from " + Pair.class.getSimpleName() + " p").getResultList();
    }

    @Override
    @TransactionAttribute(value = TransactionAttributeType.REQUIRED)
    public Pair delete(String key) {
        Pair entity = this.find(key);
        if (entity != null) {
            em.remove(entity);
            em.flush();
            em.clear();
        }
        return entity;
    }

    @Override
    @TransactionAttribute(value = TransactionAttributeType.REQUIRED)
    public Integer deleteAll() {
        return em.createNativeQuery("delete from " + Pair.class.getSimpleName()).executeUpdate();
    }
}

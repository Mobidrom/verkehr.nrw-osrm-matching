package nrw.mobidrom.verkehr.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import jakarta.transaction.Transactional;
import nrw.mobidrom.verkehr.entity.cache.OpenLRCache;
import nrw.mobidrom.verkehr.entity.cache.OpenLRCache_;
import nrw.mobidrom.verkehr.service.api.openlr.OpenLRObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;

@ApplicationScoped
public class OpenLRCacheDao implements PanacheRepository<OpenLRCache> {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpenLRCacheDao.class);

    @PersistenceContext
    protected EntityManager entityManager;

    public OpenLRCache getFromCache(String openLRBase64, int realLength) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<OpenLRCache> cq = cb.createQuery(OpenLRCache.class);
        Root<OpenLRCache> root = cq.from(OpenLRCache.class);
        cq.select(root);

        cq.where(cb.and(
                cb.equal(root.get(OpenLRCache_.OPENLR_BASE64), openLRBase64),
                cb.equal(root.get(OpenLRCache_.REAL_LENGTH), realLength)));
        try {
            return entityManager.createQuery(cq).getSingleResult();
        } catch (Exception e) {
            // no cache available
            return null;
        }
    }

    @Transactional
    public void persist(String openLRBase64, int realLength, OpenLRObject openLRObject, OpenLRCache fromCache) {
        try {
            OpenLRCache openLRCache;
            if (fromCache != null) {
                openLRCache = findById(fromCache.getId());
            } else {
                openLRCache = new OpenLRCache();
            }
            openLRCache.setOpenlrBase64(openLRBase64);
            openLRCache.setRealLength(realLength);
            openLRCache.setJsonResult(openLRObject);
            openLRCache.setLastDecoded(Calendar.getInstance());
            persist(openLRCache);
        } catch (Exception e) {
            LOGGER.warn(e.getMessage(), e);
        }
    }
}

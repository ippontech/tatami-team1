package fr.ippon.tatami.repository.cassandra;

import static fr.ippon.tatami.config.ColumnFamilyKeys.ATTACHMENT_CF;

import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import me.prettyprint.cassandra.serializers.StringSerializer;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.factory.HFactory;
import me.prettyprint.hector.api.mutation.Mutator;
import me.prettyprint.hom.EntityManagerImpl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import fr.ippon.tatami.domain.Attachment;
import fr.ippon.tatami.domain.Status;
import fr.ippon.tatami.domain.validation.ContraintsAttachmentCreation;
import fr.ippon.tatami.repository.AttachmentRepository;
import fr.ippon.tatami.repository.CounterRepository;

@Repository
public class CassandraAttachmentRepository implements AttachmentRepository {

    private final Log log = LogFactory.getLog(CassandraAttachmentRepository.class);

    @Inject
    private EntityManagerImpl em;

    @Inject
    private Keyspace keyspaceOperator;

    @Inject
    private CounterRepository counterRepository;
    

    private static ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    private static Validator validator = factory.getValidator();
    
    
	@Override
    @CacheEvict(value = "attachment-cache", key = "#attachment.attachmentId")
    public void createAttachment(Attachment attach) {
        if (log.isDebugEnabled()) {
            log.debug("Creating attachment : " + attach);
        }
        Set<ConstraintViolation<Attachment>> constraintViolations = validator.validate(attach, ContraintsAttachmentCreation.class);
        if (!constraintViolations.isEmpty()) {
            throw new ConstraintViolationException(new HashSet<ConstraintViolation<?>>(constraintViolations));
        }
        em.persist(attach);
    }

    @Override
    @CacheEvict(value = "attachment-cache", key = "#attachment.attachmentId")
    public void deleteAttachment(Attachment attach) {
        if (log.isDebugEnabled()) {
            log.debug("Deleting user : " + attach);
        }
        Mutator<String> mutator = HFactory.createMutator(keyspaceOperator, StringSerializer.get());
        mutator.addDeletion(attach.getAttachmentId(), ATTACHMENT_CF);
        mutator.execute();
    }

    @Override
    @Cacheable("user-cache")
    public Attachment findAttachmentByFilename(String filename) {
    	Attachment attach = null;
        try {
            attach = em.find(Attachment.class, filename);
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("Exception while looking for attachment " + filename + " : " + e.toString());
            }
            return null;
        }
        return attach;
    }
    
    @Override
    @Cacheable("attachment-cache")
    public Attachment findAttachmentByStatusId(String statusId) {
        if (statusId == null || statusId.equals("")) {
            return null;
        }
        if (log.isTraceEnabled()) {
            log.trace("Finding attachment : " + statusId);
        }
        Attachment attach = em.find(Attachment.class, statusId);
        if (attach != null) {
            return Boolean.TRUE.equals(attach.getRemoved()) ? null : attach;
        } else {
            return null;
        }
    }
    
    @Override
    @CacheEvict(value = "attachment-cache", key = "#attachment.statusId")
    public void removeAttachment(Attachment attach) {
        attach.setRemoved(true);
        if (log.isDebugEnabled()) {
            log.debug("Updating Status : " + attach);
        }
        em.persist(attach);
    }
	
}

package fr.ippon.tatami.repository;

import fr.ippon.tatami.domain.Attachment;

public interface AttachmentRepository {

	void createAttachment(Attachment attach);

    void deleteAttachment(Attachment attach);

    Attachment findAttachmentByFilename(String filename);
    
    Attachment findAttachmentByStatusId(String statusId);
    
    void removeAttachment(Attachment attach);
	
}

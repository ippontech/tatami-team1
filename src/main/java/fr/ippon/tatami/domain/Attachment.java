package fr.ippon.tatami.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "Attachment")
public class Attachment {

    @Id
    private String attachmentId;


    public String getAttachmentId() {
		return attachmentId;
	}

	public void setAttachmentId(String attachmentId) {
		this.attachmentId = attachmentId;
	}

	@NotNull
    @Column(name = "filename")
    private String filename;

    @Column(name = "extension")
    private String extension;

    @Column(name = "type")
    private String type;

    @Column(name = "content")
    private String content;

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getExtension() {
		return extension;
	}

	public void setExtension(String extension) {
		this.extension = extension;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	@Override
	public String toString() {
		return "Attachment [attachmentId=" + attachmentId + ", filename="
				+ filename + ", extension=" + extension + ", type=" + type
				+ ", content=" + content + "]";
	}
    
    
}
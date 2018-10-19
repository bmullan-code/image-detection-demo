package com.example.image.detection.queue;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name ="queuemessages")
public final class QueueMessage implements Serializable {

	@Id @GeneratedValue long guid;
	
	private long id;
    private String text;
    private int priority;
    private boolean secret;
    @Column(columnDefinition = "TEXT")
    private String encodedFile;
    @Column(columnDefinition = "TEXT")
    private String detectedFile;

	public long getGuid() {
		return guid;
	}

	public void setGuid(long guid) {
		this.guid = guid;
	}

	public String getDetectedFile() {
		return detectedFile;
	}

	public void setDetectedFile(String detectedFile) {
		this.detectedFile = detectedFile;
	}

    // Default constructor is needed to deserialize JSON
    public QueueMessage() {
    }

    public QueueMessage(long id, String text, int priority, boolean secret, String encodedFile) {
    		this.id = id;
        this.text = text;
        this.priority = priority;
        this.secret = secret;
        this.encodedFile = encodedFile;
        this.detectedFile = null;
    }

    public String getText() {
        return text;
    }

    public int getPriority() {
        return priority;
    }

    public boolean isSecret() {
        return secret;
    }

    @Override
    public String toString() {
        return "CustomMessage{" +
        			"guid='" + guid + "' " +  
        			"id='" + id + "' " +
                "text='" + text + '\'' +
                ", priority=" + priority +
                ", secret=" + secret +
                '}';
    }

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getEncodedFile() {
		return encodedFile;
	}

	public void setEncodedFile(String encodedFile) {
		this.encodedFile = encodedFile;
	}
}

package vh.activeobject;

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class MMSDeliverRequest implements Serializable {
	private String transactionID;
	private String messageType = "Delivery.req";
	private String senderAddress;
	private Recipient recipient = new Recipient();
	private String subject;
	private Attachment attachment=new Attachment();

	public long getExpiry() {
		return expiry;
	}

	private long expiry;
	private Date timeStamp;

	public MMSDeliverRequest() {

	}

	public void setExpiry(long expiry) {
		this.expiry = expiry;
	}

	public Date getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(Date timeStamp) {
		this.timeStamp = timeStamp;
	}

	public String getTransactionID() {
		return transactionID;
	}

	public void setTransactionID(String transactionID) {
		this.transactionID = transactionID;
	}

	public String getMessageType() {
		return messageType;
	}

	public void setMessageType(String messageType) {
		this.messageType = messageType;
	}

	public String getSenderAddress() {
		return senderAddress;
	}

	public void setSenderAddress(String senderAddress) {
		this.senderAddress = senderAddress;
	}

	public Recipient getRecipient() {
		return recipient;
	}

	public void setRecipient(Recipient recipient) {
		this.recipient = recipient;
	}

	public Attachment getAttachment() {
		return attachment;
	}

	public void setAttachment(Attachment attachment) {
		this.attachment = attachment;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	@Override
	public String toString() {
		return "MM7DeliverRequest [transactionID=" + transactionID
				+ ", messageType=" + messageType + ", senderAddress="
				+ senderAddress + ", recipient=" + recipient + ", subject="
				+ subject + ", attachment=" + attachment + "]";
	}

	private static final long serialVersionUID = 302185079311891797L;

}

class Recipient implements Serializable{

	private static final long serialVersionUID = -5427696559429827584L;
	private Set<String> to = new HashSet<String>();

	public void addTo(String msisdn) {
		to.add(msisdn);
	}

	public Set<String> getToList() {
		return (Set<String>) Collections.unmodifiableCollection(to);
	}

}

class Attachment implements Serializable{
	private static final long serialVersionUID = -313285270497968496L;
	private String contentType;
	private byte[] content=new byte[0];

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public byte[] getContent() {
		return content;
	}

	public void setContent(byte[] content) {
		this.content = content;
	}

	@Override
	public String toString() {
		return "Attachment [contentType=" + contentType + ", content="
				+ content.length + "]";
	}

}

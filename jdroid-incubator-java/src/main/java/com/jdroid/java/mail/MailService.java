package com.jdroid.java.mail;

public interface MailService {
	
	public void sendMail(String subject, String body, String sender, String recipient) throws MailException;
	
}

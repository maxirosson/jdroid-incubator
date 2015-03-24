package com.jdroid.java.mail;

import com.jdroid.java.utils.LoggerUtils;
import com.jdroid.java.utils.PropertiesUtils;

import org.slf4j.Logger;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class MailServiceImpl implements MailService {
	
	protected static final Logger LOGGER = LoggerUtils.getLogger(MailServiceImpl.class);
	
	private static final String MAIL_USER = PropertiesUtils.getStringProperty("mail.user");
	private static final String MAIL_PASSWORD = PropertiesUtils.getStringProperty("mail.password");
	private static final String MAIL_HOST = PropertiesUtils.getStringProperty("mail.host");
	private static final String MAIL_PORT = PropertiesUtils.getStringProperty("mail.port");
	private static final String SMTP = "smtp";
	
	private Session session = Session.getDefaultInstance(new Properties(), null);
	
	/**
	 * @see com.jdroid.java.mail.MailService#sendMail(java.lang.String, java.lang.String, java.lang.String,
	 *      java.lang.String)
	 */
	@Override
	public void sendMail(String subject, String body, String sender, String recipient) throws MailException {
		
		Transport transport = null;
		try {
			Message message = this.makeMessage(subject, body, sender, recipient);
			transport = this.makeTransport();
			transport.sendMessage(message, message.getAllRecipients());
		} catch (MessagingException e) {
			throw new MailException(e);
		} finally {
			if (transport != null) {
				try {
					transport.close();
				} catch (MessagingException e) {
					LOGGER.error("", e);
				}
			}
		}
	}
	
	private Message makeMessage(String subject, String body, String sender, String recipient) throws MessagingException {
		MimeMessage message = new MimeMessage(session);
		message.setFrom(new InternetAddress(sender));
		message.setSubject(subject);
		message.setText(body);
		message.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
		message.saveChanges();
		
		return message;
	}
	
	private Transport makeTransport() throws MessagingException {
		Transport transport = session.getTransport(SMTP);
		transport.connect(MAIL_HOST, Integer.valueOf(MAIL_PORT), MAIL_USER, MAIL_PASSWORD);
		return transport;
	}
}

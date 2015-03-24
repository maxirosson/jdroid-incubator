package com.jdroid.java.mail;

import com.jdroid.java.exception.AbstractException;

public class MailException extends AbstractException {
	
	private static final long serialVersionUID = 7343781404856218086L;
	
	public MailException(Throwable throwable) {
		super(throwable);
	}
	
}

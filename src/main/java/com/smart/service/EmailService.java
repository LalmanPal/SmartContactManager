package com.smart.service;

import java.util.Properties;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

//import org.springframework.boot.web.servlet.server.Session;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
 
	public boolean sendEmail(String subject,String message,String to) {
		boolean f=false;
		String from="lalmanpal004@gmail.com";
		//variable for gmail
		String host="smtp.gmail.com";
		
		//get the system properties
		Properties properties=System.getProperties();
		System.out.println("Properties====="+properties);
		
		//Settings implortant information to properties
		
		//host set
		properties.put("mail.smtp.host", host);
		properties.put("mail.smtp.port", "465");
		properties.put("mail.smtp.ssl.enable", "true");
		properties.put("mail.smtp.auth", "true");
		
		
		//step:1 to get the session object...
		Session session=Session.getInstance(properties,new Authenticator() {

			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				// TODO Auto-generated method stub
				return new PasswordAuthentication("lalmanpal004@gmail.com","lalmanpal004");
			}			
		});
		session.setDebug(true);
		
		//step:2 compose the message[text,multimedia]
		MimeMessage mime=new MimeMessage(session);
		try {
			//email address from
			mime.setFrom(from);
			//for reciepitance
			mime.addRecipient(Message.RecipientType.TO, new InternetAddress(to));			
			//adding message to subject
			mime.setSubject(subject);
			//Adding text to mesage
			//mime.setText(message);
			mime.setContent(message,"text/html");
			
			//send
			//step:3 send the message using transport class
			Transport.send(mime);
			f=true;
			System.out.println("Mail sended successfully.......");
			
		} catch (MessagingException e) {
			
			e.printStackTrace();
		}
		
		return f;
		
	}
}

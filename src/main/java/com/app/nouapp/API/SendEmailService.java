package com.app.nouapp.API;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class SendEmailService {
	
	@Autowired
	private JavaMailSender mailSender;
	
	public void SendEmail(String name,String email)
	{
		String subject = "Welcome to NOU e-Gyan Portal";
		String message ="Hello Dear,"+name+"\nYour Registration is successful on NOU e-Gyan Portal.\nNow you can login through your creadentials.\n\nThank You 😊\nAdmin Deepak Kumar👍";
		SimpleMailMessage msg = new SimpleMailMessage();
		msg.setTo(email);
		msg.setSubject(subject);
		msg.setText(message);
		mailSender.send(msg);
	}

}

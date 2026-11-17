package com.bohakka.renthub.service.impl;

import com.bohakka.renthub.dto.EmailMessage;
import com.bohakka.renthub.service.MailGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.mail.autoconfigure.MailProperties;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/** SMTP implementation; the relay is configured by the SMTP_* variables (Gmail by default), sender = SMTP user. */
@Component
@RequiredArgsConstructor
public class SmtpMailGateway implements MailGateway {

	private final JavaMailSender mailSender;
	private final MailProperties mailProperties;

	@Override
	public void send(EmailMessage message) {
		SimpleMailMessage mail = new SimpleMailMessage();
		mail.setFrom(mailProperties.getUsername());
		mail.setTo(message.to());
		mail.setSubject(message.subject());
		mail.setText(message.body());
		mailSender.send(mail);
	}
}

package com.bohakka.renthub.mail;

import com.bohakka.renthub.config.AppProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/** SMTP implementation (Mailpit in dev, any transactional provider in prod via SMTP_* variables). */
@Component
@RequiredArgsConstructor
public class SmtpMailGateway implements MailGateway {

	private final JavaMailSender mailSender;
	private final AppProperties properties;

	@Override
	public void send(EmailMessage message) {
		SimpleMailMessage mail = new SimpleMailMessage();
		mail.setFrom(properties.mailFrom());
		mail.setTo(message.to());
		mail.setSubject(message.subject());
		mail.setText(message.body());
		mailSender.send(mail);
	}
}

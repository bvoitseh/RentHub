package com.bohakka.renthub.mail;

/** A plain-text e-mail to one recipient; subject and body are Ukrainian. */
public record EmailMessage(String to, String subject, String body) {
}

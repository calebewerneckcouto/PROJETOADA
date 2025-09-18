package br.com.adacommerce.ecommerce.service;

import java.util.Properties;
import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class EmailService {

	private String userName = System.getenv("EMAIL_USER");
	private String senha = System.getenv("EMAIL_PASSWORD");

	private String listaDestinatarios = "";
	private String nomeRemetente = "";
	private String assuntoEmail = "";
	private String textoEmail = "";

	public EmailService(String listaDestinatario, String nomeRemetente, String assuntoEmail, String textoEmail) {
		this.listaDestinatarios = listaDestinatario;
		this.nomeRemetente = nomeRemetente;
		this.assuntoEmail = assuntoEmail;
		this.textoEmail = textoEmail;
	}

	/* Envio simples (com texto alternativo e HTML) */
	public void enviarEmail() throws Exception {
		Properties properties = new Properties();

		properties.put("mail.smtp.auth", "true");
		properties.put("mail.smtp.starttls.enable", "true");
		properties.put("mail.smtp.host", "smtp.gmail.com");
		properties.put("mail.smtp.port", "587");
		properties.put("mail.smtp.ssl.trust", "smtp.gmail.com");

		Session session = Session.getInstance(properties, new Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(userName, senha);
			}
		});

		Address[] toUser = InternetAddress.parse(listaDestinatarios);

		Message message = new MimeMessage(session);
		message.setFrom(new InternetAddress(userName, nomeRemetente));
		message.setRecipients(Message.RecipientType.TO, toUser);
		message.setSubject(assuntoEmail);
		message.setReplyTo(InternetAddress.parse(userName));

		/* Corpo do e-mail com alternativa (texto simples + HTML) */
		MimeBodyPart textoPlano = new MimeBodyPart();
		textoPlano.setText(textoEmail.replaceAll("\\<.*?\\>", ""), "utf-8");

		MimeBodyPart textoHtml = new MimeBodyPart();
		textoHtml.setContent(textoEmail, "text/html; charset=utf-8");

		Multipart alternativa = new MimeMultipart("alternative");
		alternativa.addBodyPart(textoPlano);
		alternativa.addBodyPart(textoHtml);

		message.setContent(alternativa);

		/* Cabeçalhos para melhorar reputação */
		message.addHeader("X-Priority", "1");
		message.addHeader("Importance", "High");

		Transport.send(message);
	}
}

package br.com.adacommerce.ecommerce.service;

import java.util.Date;
import java.util.Properties;
import java.util.UUID;
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

    public void enviarEmail() throws Exception {
        Properties properties = new Properties();

        
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "587");
        properties.put("mail.smtp.ssl.trust", "smtp.gmail.com");
        
       
        properties.put("mail.smtp.connectiontimeout", "10000");
        properties.put("mail.smtp.timeout", "10000");
        properties.put("mail.smtp.writetimeout", "10000");
        properties.put("mail.smtp.ssl.protocols", "TLSv1.2");
        properties.put("mail.smtp.starttls.required", "true");
        properties.put("mail.smtp.ssl.checkserveridentity", "true");

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
        
       
        message.setSentDate(new Date());

       
        MimeBodyPart textoPlano = new MimeBodyPart();
        textoPlano.setText(textoEmail.replaceAll("\\<.*?\\>", ""), "utf-8");

        MimeBodyPart textoHtml = new MimeBodyPart();
        textoHtml.setContent(textoEmail, "text/html; charset=utf-8");

        Multipart alternativa = new MimeMultipart("alternative");
        alternativa.addBodyPart(textoPlano);
        alternativa.addBodyPart(textoHtml);

        message.setContent(alternativa);

      
        message.addHeader("Message-ID", "<" + UUID.randomUUID().toString() + "@" + "adacommerce.com.br" + ">");
        message.addHeader("Date", new Date().toString());
        message.addHeader("Return-Path", userName);
        message.addHeader("Delivered-To", listaDestinatarios);
        message.addHeader("X-Mailer", "AdaCommerce Mailer 1.0");
        message.addHeader("X-Originating-IP", "[" + getLocalIP() + "]");
        message.addHeader("Content-Language", "pt-BR");
        message.addHeader("List-Unsubscribe", "<mailto:" + userName + "?subject=unsubscribe>");
        message.addHeader("Precedence", "bulk");
        message.addHeader("Auto-Submitted", "auto-generated");
        
       
        message.addHeader("X-Priority", "3"); 
        message.addHeader("X-MSMail-Priority", "Normal");
        message.addHeader("Importance", "Normal"); 
        
       
        message.addHeader("X-Spam-Status", "No");
        message.addHeader("X-Spam-Flag", "NO");
        message.addHeader("Authentication-Results", "pass");
        
        
        message.addHeader("X-Company", "AdaCommerce");
        message.addHeader("Organization", "AdaCommerce E-commerce");

        Transport.send(message);
    }
    
    private String getLocalIP() {
        try {
            return java.net.InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            return "127.0.0.1";
        }
    }
}
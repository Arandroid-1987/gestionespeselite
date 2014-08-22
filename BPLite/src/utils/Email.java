package utils;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.URLDataSource;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class Email {
	
	public static void sendEmailBug(String schermata, String testo) {
		String email = "arandroid.developers@gmail.com";

		try {
			// Creazione di una mail session
			Properties props = new Properties();
			props.put("mail.smtp.host", "smtp.gmail.com");
			props.put("mail.user", "arandroid.developers@gmail.com");
			props.put("mail.smtp.auth", "true");
			props.put("mail.smtp.port", "465");
			props.put("mail.smtp.socketFactory.port", "465");
			props.put("mail.smtp.socketFactory.class",
					"javax.net.ssl.SSLSocketFactory");
			props.put("mail.smtp.socketFactory.fallback", "false");
			props.setProperty("mail.smtp.quitwait", "false");

			Authenticator a = new MyAuthenticator();

			Session session = Session.getInstance(props, a);

			// Creazione del messaggio da inviare
			final MimeMessage message = new MimeMessage(session);
			message.setSubject("Gestione Spese - Segnalazione Bug");
			
			Date date = new Date();
			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm", Locale.ITALIAN);
			
			message.setText("Bug segnalato il "+sdf.format(date)+" - relativo alla schermata "+schermata+" con descrizione: "+testo+".");

			// Aggiunta degli indirizzi del mittente e del
			// destinatario
			InternetAddress fromAddress = new InternetAddress(
					"arandroid.developers@gmail.com");
			InternetAddress toAddress = new InternetAddress(email);
			message.setFrom(fromAddress);
			message.setRecipient(Message.RecipientType.TO, toAddress);

			// Invio del messaggio
			Thread t = new Thread(){
				
				@Override
				public void run() {
					try {
						Transport.send(message);
					} catch (MessagingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			};
			t.start();
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static void sendEmail(String email, String password) {

		try {
			// Creazione di una mail session
			Properties props = new Properties();
			props.put("mail.smtp.host", "smtp.gmail.com");
			props.put("mail.user", "arandroid.developers@gmail.com");
			props.put("mail.smtp.auth", "true");
			props.put("mail.smtp.port", "465");
			props.put("mail.smtp.socketFactory.port", "465");
			props.put("mail.smtp.socketFactory.class",
					"javax.net.ssl.SSLSocketFactory");
			props.put("mail.smtp.socketFactory.fallback", "false");
			props.setProperty("mail.smtp.quitwait", "false");

			Authenticator a = new MyAuthenticator();

			Session session = Session.getInstance(props, a);

			// Creazione del messaggio da inviare
			MimeMessage message = new MimeMessage(session);
			message.setSubject("Recupero Password GestioneSpese by ARANDROID");

			Multipart multipart = new MimeMultipart("related");

			BodyPart htmlPart = new MimeBodyPart();
			htmlPart.setContent(
					"<html><body><p>Gentile utente "
							+ email
							+ ", come da lei richiesto le inviamo la password da lei smarrita.</p>Password: "
							+ password
							+ "<br/>"
							+ "----------------------------------------------------------------------------<br/>"
							+ "<img src=\"cid:the-img-1\"/><br/>E-mail: <a href='mailto:arandroid@libero.it'>arandroid@libero.it</a><br/>Questa email e' stata inviata in modo automatico, si prega quindi"
							+ " di non rispondere.<br/>Se hai ricevuto questa email per errore, ti preghiamo di ignorarla.</body></html>",
					"text/html");

			multipart.addBodyPart(htmlPart);

			BodyPart imgPart = new MimeBodyPart();

			// Loading the image
			DataSource ds = new URLDataSource(
					new URL(
							"https://dl.dropboxusercontent.com/u/81288197/arandroid.png"));
			imgPart.setDataHandler(new DataHandler(ds));

			// Setting the header
			imgPart.setHeader("Content-ID", "the-img-1");

			multipart.addBodyPart(imgPart);

			// attaching the multi-part to the message
			message.setContent(multipart);

			// Aggiunta degli indirizzi del mittente e del
			// destinatario
			InternetAddress fromAddress = new InternetAddress(
					"arandroid.developers@gmail.com");
			InternetAddress toAddress = new InternetAddress(email);
			message.setFrom(fromAddress);
			message.setRecipient(Message.RecipientType.TO, toAddress);

			// Invio del messaggio
			Transport.send(message);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}

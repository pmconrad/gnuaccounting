package dataLayer;

import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.Properties;

import javax.activation.DataHandler;
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
import javax.mail.util.ByteArrayDataSource;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;

import GUILayer.Messages;
import appLayer.application;
import appLayer.configs;

import com.sun.mail.smtp.SMTPSSLTransport;

class MailAuthenticator extends Authenticator {
	/**
	 * Ein String, der den Usernamen nach der Erzeugung eines Objektes<br>
	 * dieser Klasse enthalten wird.
	 */
	private final String user;
	/**
	 * Ein String, der das Passwort nach der Erzeugung eines Objektes<br>
	 * dieser Klasse enthalten wird.
	 */

	private final String password;

	/**
	 * Der Konstruktor erzeugt ein MailAuthenticator Objekt<br>
	 * aus den beiden Parametern user und passwort.
	 * 
	 * @param user
	 *            String, der Username fuer den Mailaccount.
	 * @param password
	 *            String, das Passwort fuer den Mailaccount.
	 */

	public MailAuthenticator(String user, String password) {
		this.user = user;
		this.password = password;
	}

	/**
	 * Diese Methode gibt ein neues PasswortAuthentication Objekt zurueck.
	 * 
	 * @see javax.mail.Authenticator#getPasswordAuthentication()
	 */

	protected PasswordAuthentication getPasswordAuthentication() {
		return new PasswordAuthentication(this.user, this.password);
	}

}

public class mailThread extends Thread implements IRunnableWithProgress {
	private String recipientsAddress;
	private String subject;
	private String text;
	private String filename1;
	private String filename2;
	private String filename3;
	private String SMTPPassword;
	private String SMTPUsername;

	private String memoryFileName = null;
	private String memoryFileContentType = null;
	private String memoryFileData = null;

	/**
	 * will send a mail with two file attachments
	 * */
	public mailThread(String SMTPUsername, String SMTPPassword,
			String recipientsAddress, String subject, String text,
			String filename1, String filename2, String filename3) {
		this.recipientsAddress = recipientsAddress;
		this.subject = subject;
		this.text = text;
		this.filename1 = filename1;
		this.filename2 = filename2;
		this.filename3 = filename3;
		this.SMTPPassword = SMTPPassword;
		this.SMTPUsername = SMTPUsername;

	}

	public void attachMemoryFile(String memoryFileName,
			String memoryFileContentType, String memoryFileData) {
		this.memoryFileName = memoryFileName;
		this.memoryFileContentType = memoryFileContentType;
		this.memoryFileData = memoryFileData;
	}

	public void run(IProgressMonitor ipm) throws InvocationTargetException,
			InterruptedException {

		ipm.beginTask(
				Messages.getString("newTransactionWizard.sendingMailTo") + recipientsAddress, 100); //$NON-NLS-1$
		// run function and authenticator class based on Java Mail API
		// sendfile example and
		// http://www.tutorials.de/forum/java/255387-email-mit-javamail-versenden.html
		/*
		 * Security.addProvider( new com.sun.net.ssl.internal.ssl.Provider());
		 * final String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";
		 * Properties props = System.getProperties(); // IMAP provider
		 * props.setProperty( "mail.imap.socketFactory.class", SSL_FACTORY);
		 */

		MailAuthenticator auth = new MailAuthenticator(SMTPUsername,
				SMTPPassword);
		Properties properties = new Properties();
		// the server address is added to the properties
		if (!configs.shallUseSMTPSSL()) {
			properties.put("mail.smtp.host", configs.getSMTPServer()); //$NON-NLS-1$
		} else {
			properties.put("mail.smtps.host", configs.getSMTPServer()); //$NON-NLS-1$
		}

		// properties.put("mail.smtps.port", "25");
		// !!Important!! If the SMTP Server requires authentication
		// this property must be set to true

		if (configs.shallUseSMTPAuth()) {
			properties.put("mail.smtps.auth", "true"); //$NON-NLS-1$ //$NON-NLS-2$
			properties.put("mail.smtp.auth", "true"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		if (configs.shallUseSMTPSSL()) {
			properties.setProperty("mail.smtps.socketFactory.class", //$NON-NLS-1$
					"dataLayer.overridableSSLSocketFactory"); //$NON-NLS-1$
			properties
					.setProperty("mail.smtps.socketFactory.fallback", "false"); //$NON-NLS-1$ //$NON-NLS-2$
		}

		// Here a session gets constructed with the properties and the Mail
		// Authenticator
		// create session

		Session session = Session.getDefaultInstance(properties, auth);
		// session.setDebug(true);
		try {
			// Create new message
			Message msg = new MimeMessage(session);
			// Set sender and recipient addresses
			msg.setFrom(new InternetAddress(configs.getSenderEmail()));
			msg.setRecipients(Message.RecipientType.TO,
					InternetAddress.parse(recipientsAddress, false));
			msg.setRecipients(Message.RecipientType.BCC,
					InternetAddress.parse(configs.getSenderEmail(), false));// put
																			// sender
			// on blind
			// carbon
			// copy so
			// that
			// (s)he
			// receives
			// a copy as
			// well

			// Set subject and message body
			msg.setSubject(subject);

			// create and fill the first message part
			MimeBodyPart mbp1 = new MimeBodyPart();
			mbp1.setText(text);

			// create the Multipart and add its parts to it
			Multipart mp = new MimeMultipart();
			mp.addBodyPart(mbp1);

			if (filename1 != null) {
				MimeBodyPart mbp = new MimeBodyPart();
				// attach the file to the message
				mbp.attachFile(filename1);
				mp.addBodyPart(mbp);
			}
			if (filename2 != null) {
				MimeBodyPart mbp = new MimeBodyPart();
				// attach the file to the message
				mbp.attachFile(filename2);
				mp.addBodyPart(mbp);
			}
			if (filename3 != null) {
				MimeBodyPart mbp = new MimeBodyPart();
				// attach the file to the message
				mbp.attachFile(filename3);
				mp.addBodyPart(mbp);
			}

			// Attach file generated in memory
			if (this.memoryFileName != null
					&& this.memoryFileContentType != null
					&& this.memoryFileData != null) {
				MimeBodyPart mbp = new MimeBodyPart();
				mbp.setDataHandler(new DataHandler(new ByteArrayDataSource(
						this.memoryFileData, this.memoryFileContentType)));
				mbp.setFileName(this.memoryFileName);
				mp.addBodyPart(mbp);
			}

			/*
			 * Use the following approach instead of the above line if you want
			 * to control the MIME type of the attached file. Normally you
			 * should never need to do this.
			 * 
			 * FileDataSource fds = new FileDataSource(filename) { public String
			 * getContentType() { return "application/octet-stream"; } };
			 * mbp2.setDataHandler(new DataHandler(fds));
			 * mbp2.setFileName(fds.getName());
			 */

			// add the Multipart to the message
			msg.setContent(mp);

			// here you can add additional header info
			// msg.setHeader("Test", "Test");
			msg.setHeader("X-Mailer", application.getAppName()); //$NON-NLS-1$

			msg.setSentDate(new Date());
			ipm.worked(10);

			// transport the message
			if (configs.shallUseSMTPSSL()) {
				SMTPSSLTransport transport = (SMTPSSLTransport) session
						.getTransport("smtps"); //$NON-NLS-1$
				// connect to server
				// send the message
				transport.connect(configs.getSMTPServer(), SMTPUsername,
						SMTPPassword);
				ipm.worked(20);

				// now send the message
				transport.sendMessage(msg, msg.getAllRecipients()); // used
				// close the connection
				transport.close();
			} else {
				Transport.send(msg);
			}
		} catch (Exception e) {
			e.printStackTrace();

			return;
		}

		ipm.worked(100);
	}

}
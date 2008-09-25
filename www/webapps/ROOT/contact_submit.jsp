<%@ page import="edu.sfsu.netbeams.web.*" %>
<%@ page import="javax.mail.*" %>
<%@ page import="javax.mail.internet.*" %>
<%@ page import="java.util.*" %>
<%@ page errorPage="error.jsp" %>


<% out.println(WebTemplate.getHeader(WebTemplate.CONTACT_US_PAGE)); %>


<%!
	class MyEmailAuthenticator extends Authenticator {
		private String user;
		private String password;

		public MyEmailAuthenticator(String user, String password) {
			this.user = user;
			this.password = password;
		}

		public PasswordAuthentication getPasswordAuthentication() {
			return new PasswordAuthentication(user, password);
		}
	}

%>




	<table border="0" width="100%" id="table2" cellspacing="0" cellpadding="0">
		<tr>
			<td>
				<p class="title"><a name='resources' />Contact Us</a></p>

				<%
					String name = request.getParameter("userName");
					String email = request.getParameter("email");
					String subject = request.getParameter("subject");
					String sendTo = request.getParameter("sendTo");
					String message = request.getParameter("message");

					if (email == null || email.trim().length() == 0)
						message = "You have a message from: " + name.toUpperCase() + "\n\n" + message;
					else
						message = "You have a message from: " + name.toUpperCase() + " (" + email + ")\n\n" + message;


/*
						<option selected value="0">Everyone on the NetBEAMS' team</option>
						<option value="1">Toby Garfield</option>
						<option value="2">Todor Cooklev</option>
						<option value="3">Arno Puder</option>
						<option value="4">Dragutin Petkovic</option>
						<option value="5">Gary Thompson</option>
						<option value="6">James Wright</option>
						<option value="7">James C. Liu</option>
						<option value="8">James Todd</option>
						<option value="9">Tai-Wei Lin</option>
						<option value="10">Jay Warrior</option>
						<option value="11">Jerry Liu</option>
						<option value="12">Glen Purdy</option>
						<option value="13">Glenn Engel</option>
						<option value="14">Brian Zambra</option>
						<option value="15">Bill Huynh</option>
*/


					Hashtable emailTable = new Hashtable();
					emailTable.put("0", "dev@netbeams.dev.java.net");
					emailTable.put("1", "tgarfield@dev.java.net");
					emailTable.put("2", "tcooklev@dev.java.net");
					emailTable.put("3", "apuder@dev.java.net");
					emailTable.put("4", "dpetkovic@dev.java.net");
					emailTable.put("5", "gthomps@dev.java.net");
					emailTable.put("6", "semesa2@dev.java.net");
					emailTable.put("7", "gyozadude@dev.java.net");
					emailTable.put("8", "james.todd@sun.com");
					emailTable.put("9", "vegdave@gmail.com");
					emailTable.put("10", "lowlylowlypeon@dev.java.net");
					emailTable.put("11", "jliu@dev.java.net");
					emailTable.put("12", "glenp@dev.java.net");
					emailTable.put("13", "glennengel@dev.java.net");
					emailTable.put("14", "brianz@dev.java.net");
					emailTable.put("15", "bill_huynh@sbcglobal.net");


					String sendToAddress = (String)emailTable.get(sendTo);


					Properties mailProps = System.getProperties();
					mailProps.put("mail.smtp.host", NetBEAMSConstants.MAIL_SERVER);
					mailProps.put("mail.smtp.auth", "true");
					mailProps.put("mail.transport.protocol", "smtp");
					Session mailSession = Session.getInstance(mailProps, new MyEmailAuthenticator(NetBEAMSConstants.SENDER_ID, NetBEAMSConstants.SENDER_PASSWORD));


					// Create and Initialize Message
					Message mimeMessage = new MimeMessage(mailSession);

					String from = email;
					if (email == null || email.trim().length() == 0) from = NetBEAMSConstants.SENDER_NAME;
					mimeMessage.setFrom(new InternetAddress(from));
					mimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(sendToAddress));

					if (email != null && email.trim().length() > 0)
						mimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(email));

					mimeMessage.setSubject("NETBEAMS FEEDBACK: " + subject);
					mimeMessage.setContent(message, "text/plain");

					Transport.send(mimeMessage);

				%>

				<p align='center' class='message'>Your message has been sent successfully.</p>
			</td>
		</tr>
	</table>


<%	out.println(WebTemplate.getFooter(WebTemplate.CONTACT_US_PAGE)); %>




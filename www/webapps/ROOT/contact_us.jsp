<%@ page import="edu.sfsu.netbeams.web.*" %>
<%@ page errorPage="error.jsp" %>


<% out.println(WebTemplate.getHeader(WebTemplate.CONTACT_US_PAGE)); %>

<script type="text/javascript">

	function validateInput () {
		var errorMessage = "";

		if (trim(document.contactForm.userName.value).length == 0)
			errorMessage = errorMessage + "- your name<br>";
		if (trim(document.contactForm.subject.value).length == 0)
			errorMessage = errorMessage + "- subject<br>";
		if (trim(document.contactForm.message.value).length == 0)
			errorMessage = errorMessage + "- message text<br>";


		if (errorMessage.length > 0) {
			document.getElementById("error").innerHTML = "Please provide the followng information<br>" + errorMessage;
			return false;
		}

		return true;
	}


	function trim(s)
	{
		var re=/^(\s*)([\w\W]*[^\s])(\s*)$/;
		var rs=re.exec(String(s));
		return rs != null ? rs[2] : "";
	}

</script>


	<table border="0" width="100%" id="table2" cellspacing="0" cellpadding="0">
		<tr>
			<td>
			<p class="title"><a name='resources' />Contact Us</a></p>
			<form method="POST" action="contact_submit.jsp" name="contactForm" id="contactForm" onsubmit="return validateInput()">
				<table border="0" width="100%" id="table4" cellspacing="0" cellpadding="5">
					<tr><td></td><td><div class='error_message' name='error' id='error' style='text-align:left'></div></tr></td>
					<tr><td>&nbsp;</td><td align='left'>If you have any comments or questions, please send us a message. We appreciate your input and will try to response to your comments or questions as soon as possible.<br><br><b>Please provide your email if you want us to contact you.</b></td></tr>
					<tr>
						<td width="16%" align="right" valign="top"><b>Your Name:</b></td>
						<td align="left" valign="top" width="82%">
						<input type="text" name="userName" id="userName" size="34"></td>
					</tr>
					<tr>
						<td width="16%" align="right" valign="top"><b>Email: 
						(optional)</b></td>
						<td align="left" valign="top" width="82%">
						<input type="text" name="email" id="email" size="34"></td>
					</tr>
					<tr>
						<td width="16%" align="right" valign="top"><b>Send To:</b></td>
						<td align="left" valign="top" width="82%">
						<select size="1" name="sendTo" id="sendTo">
						<option selected value="0">Everyone on the NetBEAMS' team</option>
						<option value="2">Todor Cooklev</option>
						<option value="13">Glenn Engel</option>
						<option value="1">Toby Garfield</option>
						<option value="15">Bill Huynh</option>
						<option value="9">Tai-Wei Lin</option>
						<option value="7">James C. Liu</option>
						<option value="11">Jerry Liu</option>
						<option value="4">Dragutin Petkovic</option>
						<option value="3">Arno Puder</option>
						<option value="12">Glen Purdy</option>
						<option value="5">Gary Thompson</option>
						<option value="8">James Todd</option>
						<option value="10">Jay Warrior</option>
						<option value="6">James Wright</option>
						<option value="14">Brian Zambrano</option>
						</select></td>
					</tr>
					<tr>
						<td width="16%" align="right" valign="top"><b>Subject:</b></td>
						<td align="left" valign="top" width="82%">
						<input type="text" name="subject" id="subject" size="66"></td>
					</tr>
					<tr>
						<td width="16%" align="right" valign="top"><b>Message:</b></td>
						<td align="left" valign="top" width="82%">
						<textarea rows="10" name="message" id="message" cols="50"></textarea><br></td>
					</tr>
				</table>
				<p class="title">
				<input type="submit" value="Send" name="submit"></p>
			</form>
			</td>
		</tr>
	</table>


<%	out.println(WebTemplate.getFooter(WebTemplate.CONTACT_US_PAGE)); %>



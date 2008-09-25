<?xml version="1.0"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	
	<xsl:template match="/">
		<link href='resources/netbeams.css' rel='stylesheet' type='text/css' />	
		
		<xsl:apply-templates select="location"/>
	</xsl:template>

	<xsl:template match="location">
		<div style="width:250px; text-align:center; padding:0px;">
			<xsl:apply-templates select="info"/>
		</div>
	</xsl:template>
	<xsl:template match="info">
		<table width='250px' border='0' cellspacing='0' cellpadding='0' align='center'>
			<tr>
				<td class='title' valign='top' style='padding-bottom:10px; line-height:1.5'>
					Current Condition at<br /><span class='important_info'><xsl:value-of select='locationDesc' /></span>
				</td>
			</tr>
			<xsl:for-each select="measurement">
			
			<tr>
				<td>
					<table width='100%' border='0px' class='measurement_row' bgcolor='#ffffff' cellspacing='0' cellpadding='0'>
						<tr>
							<td align='left' valign='top'>
								<span class='measurement_name'>
									<xsl:value-of select='name' />
								</span>
							</td>
							<td align='right' valign='top'>
								<span class='measurement_value'>
									<xsl:value-of select='value' />
								</span>
								<span class='measurement_unit'>
									<xsl:value-of select='unit' />
								</span>
							</td>
						</tr>
					</table>
				</td>
			</tr>

			</xsl:for-each>
			
			<tr>
				<td>
					<table width='100%' border='0px' class='measurement_row' bgcolor='#ffffff' cellspacing='0' cellpadding='0' style='background-color:#EEEEEE'>
						<tr>
							<td align='left' valign='top'>
								<span class='measurement_name'>
									<span class='measurement_name'><xsl:value-of select="time/name"/></span>
								</span>
							</td>
							<td align='right' valign='top'>
								<span class='measurement_value'><xsl:value-of select='time/age' /></span>
							</td>
						</tr>
						<tr>
							<td colspan='2' align='right' style='line-height:1.5;' valign='top'>
								<span><xsl:value-of select="time/value"/></span>
							</td>
						</tr>
					</table>
				</td>
			</tr>

			<tr class='measurement_row'>
				<td align='right' valign='bottom' style='padding-top:10px'>
					<a>
						<xsl:attribute name="href">
							<xsl:value-of select="link"/>
						</xsl:attribute>
						<b>
							See detail
						</b>
					</a>
				</td>
			</tr>
		</table>
	</xsl:template>
</xsl:stylesheet>




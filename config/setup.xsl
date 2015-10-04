<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:h="homecan">
<xsl:template match="h:setup">
  <html>
  <head>
	  <title>Übersicht der HomeCAN Geräte</title>
  </head>
  <body>
  <xsl:for-each select="h:segment">
	<h1>Segment IP:<xsl:value-of select="@ip" /></h1>
	Folgende Geräte sind in diesem Segment verbaut:
 	<table border="1" bgcolor="lightgrey">            
      <thead>
      	<tr>
        	<th>Geräte ID</th>
        	<th>Verbauort</th>
        	<th>Hardware</th>
      	</tr>
      </thead>
      <tbody>
      <xsl:for-each select="h:device">
      	<tr>
        	<td>
	        	0x<xsl:call-template name="ConvertDecToHex">
    				<xsl:with-param name="index">
						<xsl:value-of select="@address" />
					</xsl:with-param>
				</xsl:call-template>
        	</td>
        	<td><xsl:value-of select="h:location" /></td>
        	<td><xsl:value-of select="h:hardware" /></td>
     	 </tr>
      </xsl:for-each>
      </tbody>
   	</table>
    	
	<h2>Details zu den Geräten im Segment:</h2>		    
	  <xsl:for-each select="h:device">
    	<hr/>
    	<h3>
    		<xsl:value-of select="h:hardware" /> (Gerät 0x<xsl:call-template name="ConvertDecToHex">
	    		<xsl:with-param name="index">
	    			<xsl:value-of select="@address" />
				</xsl:with-param>
			</xsl:call-template>)<br/>
			<xsl:value-of select="h:location" />			
		</h3>
		<table border="1" bgcolor="lightgrey">
			<thead>
	      		<tr>
		        	<th>Kanal</th>
		        	<th>Funktion</th>
	      		</tr>
      		</thead>
      		<tbody>
	      		<xsl:for-each select="h:channel">
    	  			<tr>
        				<td><xsl:value-of select="@number" /></td>
        				<td><xsl:value-of select="h:description" /></td>
						<td><xsl:value-of select="@function" /></td>
					</tr>	
				</xsl:for-each>
			</tbody>		
		</table>
		<br/>		
      </xsl:for-each>
      <hr/><hr/>
    </xsl:for-each>
  </body>
  </html>
</xsl:template>

<xsl:template name="ConvertDecToHex">
    <xsl:param name="index" />
    <xsl:if test="$index > 0">
      <xsl:call-template name="ConvertDecToHex">
        <xsl:with-param name="index" select="floor($index div 16)" />
      </xsl:call-template>
      <xsl:choose>
        <xsl:when test="$index mod 16 &lt; 10">
          <xsl:value-of select="$index mod 16" />
        </xsl:when>
        <xsl:otherwise>
          <xsl:choose>
            <xsl:when test="$index mod 16 = 10">A</xsl:when>
            <xsl:when test="$index mod 16 = 11">B</xsl:when>
            <xsl:when test="$index mod 16 = 12">C</xsl:when>
            <xsl:when test="$index mod 16 = 13">D</xsl:when>
            <xsl:when test="$index mod 16 = 14">E</xsl:when>
            <xsl:when test="$index mod 16 = 15">F</xsl:when>
            <xsl:otherwise>A</xsl:otherwise>
          </xsl:choose>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:if>
  </xsl:template>

</xsl:stylesheet>
<?xml version="1.0" encoding="UTF-8"?>
<!--
  FindBugs - Find bugs in Java programs
  Copyright (C) 2004,2005 University of Maryland
  Copyright (C) 2005, Chris Nappin
  
  This library is free software; you can redistribute it and/or
  modify it under the terms of the GNU Lesser General Public
  License as published by the Free Software Foundation; either
  version 2.1 of the License, or (at your option) any later version.
  
  This library is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  Lesser General Public License for more details.
  
  You should have received a copy of the GNU Lesser General Public
  License along with this library; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
-->
<xsl:stylesheet version="1.0"
	xmlns="http://www.w3.org/1999/xhtml"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output
	method="html"
	omit-xml-declaration="yes"
	standalone="yes"
	doctype-public="-//W3C//DTD XHTML 1.0 Transitional//EN"
	doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"
	indent="yes"
	encoding="UTF-8"/>

<xsl:variable name="bugTableHeader">
	<tr class="tableheader">
		<th align="left">Warning</th>
		<th align="left">Details</th>
	</tr>
</xsl:variable>

<xsl:template match="/">
	<html>
	<head>
		<title>FindBugs Report</title>
		<style type="text/css">
		.tablerow0 {
			background: #EEEEEE;
		}

		.tablerow1 {
			background: white;
		}

		.detailrow0 {
			background: #EEEEEE;
		}

		.detailrow1 {
			background: white;
		}

		.tableheader {
			background: #b9b9fe;
			font-size: larger;
		}
		</style>
	</head>
	<body>

	<h1>FindBugs Report</h1>

	<h2>Summary</h2>
	<table width="500" cellpadding="5" cellspacing="2">
	    <tr class="tableheader">
			<th align="left">Warning Type</th>
			<th align="right">Number</th>
		</tr>
		<tr class="tablerow0">
		    <td><a href="#Warnings_CORRECTNESS">Correctness Warnings</a></td>
		    <td align="right"><xsl:value-of select="count(/BugCollection/BugInstance[@category='CORRECTNESS'])"/></td>
		</tr>
		<tr class="tablerow1">
		    <td><a href="#Warnings_I18N">Internationalization Warnings</a></td>
		    <td align="right"><xsl:value-of select="count(/BugCollection/BugInstance[@category='I18N'])"/></td>
		</tr>
		<tr class="tablerow0">
		    <td><a href="#Warnings_MT_CORRECTNESS">Multithreaded Correctness Warnings</a></td>
		    <td align="right"><xsl:value-of select="count(/BugCollection/BugInstance[@category='MT_CORRECTNESS'])"/></td>
		</tr>
		<tr class="tablerow1">
		    <td><a href="#Warnings_MALICIOUS_CODE">Malicious Code Vulnerability Warnings</a></td>
		    <td align="right"><xsl:value-of select="count(/BugCollection/BugInstance[@category='MALICIOUS_CODE'])"/></td>
		</tr>
		<tr class="tablerow0">
		    <td><a href="#Warnings_PERFORMANCE">Performance Warnings</a></td>
		    <td align="right"><xsl:value-of select="count(/BugCollection/BugInstance[@category='PERFORMANCE'])"/></td>
		</tr>
		<tr class="tablerow1">
		    <td><a href="#Warnings_STYLE">Style Warnings</a></td>
		    <td align="right"><xsl:value-of select="count(/BugCollection/BugInstance[@category='STYLE'])"/></td>
		</tr>
		<tr class="tablerow0">
		    <td><b>Total</b></td>
		    <td align="right"><b><xsl:value-of select="count(/BugCollection/BugInstance)"/></b></td>
		</tr>
	</table>
	<p><br/><br/></p>
	
	<h1>Warnings</h1>

	<p>Click on each warning link to see a full description of the issue, and
	    details of how to resolve it.</p>

	<xsl:call-template name="generateWarningTable">
		<xsl:with-param name="warningSet" select="/BugCollection/BugInstance[@category='CORRECTNESS']"/>
		<xsl:with-param name="sectionTitle">Correctness Warnings</xsl:with-param>
		<xsl:with-param name="sectionId">Warnings_CORRECTNESS</xsl:with-param>
	</xsl:call-template>

	<xsl:call-template name="generateWarningTable">
		<xsl:with-param name="warningSet" select="/BugCollection/BugInstance[@category='I18N']"/>
		<xsl:with-param name="sectionTitle">Internationalization Warnings</xsl:with-param>
		<xsl:with-param name="sectionId">Warnings_I18N</xsl:with-param>
	</xsl:call-template>

	<xsl:call-template name="generateWarningTable">
		<xsl:with-param name="warningSet" select="/BugCollection/BugInstance[@category='MT_CORRECTNESS']"/>
		<xsl:with-param name="sectionTitle">Multithreaded Correctness Warnings</xsl:with-param>
		<xsl:with-param name="sectionId">Warnings_MT_CORRECTNESS</xsl:with-param>
	</xsl:call-template>

	<xsl:call-template name="generateWarningTable">
		<xsl:with-param name="warningSet" select="/BugCollection/BugInstance[@category='MALICIOUS_CODE']"/>
		<xsl:with-param name="sectionTitle">Malicious Code Vulnerability Warnings</xsl:with-param>
		<xsl:with-param name="sectionId">Warnings_MALICIOUS_CODE</xsl:with-param>
	</xsl:call-template>

	<xsl:call-template name="generateWarningTable">
		<xsl:with-param name="warningSet" select="/BugCollection/BugInstance[@category='PERFORMANCE']"/>
		<xsl:with-param name="sectionTitle">Performance Warnings</xsl:with-param>
		<xsl:with-param name="sectionId">Warnings_PERFORMANCE</xsl:with-param>
	</xsl:call-template>

	<xsl:call-template name="generateWarningTable">
		<xsl:with-param name="warningSet" select="/BugCollection/BugInstance[@category='STYLE']"/>
		<xsl:with-param name="sectionTitle">Style Warnings</xsl:with-param>
		<xsl:with-param name="sectionId">Warnings_STYLE</xsl:with-param>
	</xsl:call-template>

    <p><br/><br/></p>
	<h1><a name="Details">Warning Types</a></h1>

	<xsl:apply-templates select="/BugCollection/BugPattern">
		<xsl:sort select="@abbrev"/>
		<xsl:sort select="ShortDescription"/>
	</xsl:apply-templates>

	</body>
	</html>
</xsl:template>

<xsl:template match="BugInstance">
	<xsl:variable name="warningId"><xsl:value-of select="generate-id()"/></xsl:variable>

	<tr class="tablerow{position() mod 2}">
		<td width="20%" valign="top">
			<a href="#{@type}"><xsl:value-of select="ShortMessage"/></a>
		</td>
		<td width="80%">
		    <p><xsl:value-of select="LongMessage"/><br/><br/>
		    
		    	<!--  add source filename and line number(s), if any -->
				<xsl:if test="SourceLine">
					<br/>In file <xsl:value-of select="SourceLine/@sourcefile"/>,
					<xsl:choose>
						<xsl:when test="SourceLine/@start = SourceLine/@end">
						line <xsl:value-of select="SourceLine/@start"/>
						</xsl:when>
						<xsl:otherwise>
						lines <xsl:value-of select="SourceLine/@start"/>
						    to <xsl:value-of select="SourceLine/@end"/>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:if>
				
				<xsl:for-each select="./*/Message">
					<br/><xsl:value-of select="text()"/>
				</xsl:for-each>
		    </p>
		</td>
	</tr>
</xsl:template>

<xsl:template match="BugPattern">
	<h2><a name="{@type}"><xsl:value-of select="ShortDescription"/></a></h2>
	<xsl:value-of select="Details" disable-output-escaping="yes"/>
	<p><br/><br/></p>
</xsl:template>

<xsl:template name="generateWarningTable">
	<xsl:param name="warningSet"/>
	<xsl:param name="sectionTitle"/>
	<xsl:param name="sectionId"/>

	<h2><a name="{$sectionId}"><xsl:value-of select="$sectionTitle"/></a></h2>
	<table class="warningtable" width="100%" cellspacing="2" cellpadding="5">
		<xsl:copy-of select="$bugTableHeader"/>
		<xsl:choose>
		    <xsl:when test="count($warningSet) &gt; 0">
				<xsl:apply-templates select="$warningSet">
					<xsl:sort select="@abbrev"/>
					<xsl:sort select="Class/@classname"/>
				</xsl:apply-templates>
		    </xsl:when>
		    <xsl:otherwise>
		        <tr><td colspan="2"><p><i>None</i></p></td></tr>
		    </xsl:otherwise>
		</xsl:choose>
	</table>
	<p><br/><br/></p>
</xsl:template>

</xsl:stylesheet>
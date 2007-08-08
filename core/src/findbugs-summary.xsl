<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0" >

<xsl:output method="html"/>

<xsl:param name="PAGE.TITLE" select="'Findbugs Summary Statistics'" />
<xsl:param name="PAGE.FONT" select="'Arial'" />
<xsl:param name="SUMMARY.HEADER" select="'Findbugs Summary Report'" />
<xsl:param name="SUMMARY.LABEL" select="'Summary Analysis Generated at: '" />
<xsl:param name="PACKAGE.HEADER" select="'Bugs By Package'" />
<xsl:param name="PACKAGE.SORT.LABEL" select="'Sorted by Total Bugs'" />
<xsl:param name="PACKAGE.LABEL" select="'Analysis of Package: '" />
<xsl:param name="DEFAULT.PACKAGE.NAME" select="'default package'" />
<xsl:param name="PACKAGE.BUGCLASS.LABEL" select="'Most Buggy Class in Package with #1 $1:'" />
<xsl:param name="TOTAL.PACKAGES.LABEL" select="'#1 $1 Analyzed'" />

<xsl:param name="BUGS.SINGLE.LABEL" select="'Bug'" />
<xsl:param name="BUGS.PULURAL.LABEL" select="'Bugs'" />
<xsl:param name="PACKAGE.SINGLE.LABEL" select="'Package'" />
<xsl:param name="PACKAGE.PULURAL.LABEL" select="'Packages'" />


<xsl:param name="TABLE.HEADING.TYPE" select="'Type Checked'" />
<xsl:param name="TABLE.HEADING.COUNT" select="'Count'" />
<xsl:param name="TABLE.HEADING.BUGS" select="'Bugs'" />
<xsl:param name="TABLE.HEADING.PERCENT" select="'Percentage'" />
<xsl:param name="TABLE.ROW.OUTER" select="'Outer Classes'" />
<xsl:param name="TABLE.ROW.INNER" select="'Inner Classes'" />
<xsl:param name="TABLE.ROW.INTERFACE" select="'Interfaces'" />
<xsl:param name="TABLE.ROW.TOTAL" select="'Total'" />
<xsl:param name="TABLE.WIDTH" select="'90%'" />

<xsl:param name="PERCENTAGE.FORMAT" select="'#0.00%'" />

<!-- This template drives the rest of the output -->
<xsl:template match="/" >
  <html>
   <!-- JEditorPane gets really angry if it sees this -->
   <!--<head><title><xsl:value-of select="$PAGE.TITLE" /></title></head> -->
  <body>
  <h1><center><xsl:value-of select="$SUMMARY.HEADER" /></center></h1>
  <h2><center><xsl:value-of select="$SUMMARY.LABEL" /> 
      <i><xsl:value-of select="//FindBugsSummary/@timestamp" /></i></center></h2>
  <xsl:apply-templates select="//FindBugsSummary" />
  <br/>
  <center>
  <font face="{$PAGE.FONT}" size="6"><xsl:value-of select="$PACKAGE.HEADER" /></font>
    <br/><font face="{$PAGE.FONT}" size="4"><i>(<xsl:value-of select="$PACKAGE.SORT.LABEL"/>)</i></font>
  </center>
  <xsl:for-each select="//FindBugsSummary/PackageStats">
  <xsl:sort select="@total_bugs" data-type="number" order="descending" />
  <xsl:apply-templates select="." />
  </xsl:for-each>
  </body>
  </html>
</xsl:template>

<xsl:template name="status_table_row" >
  <xsl:param name="LABEL" select="''" />
  <xsl:param name="COUNT" select="1" />
  <xsl:param name="BUGS" select="0" />
  <xsl:param name="FONT_SIZE" select="4" />
  <tr>
   <td align="left"><font face="{$PAGE.FONT}" size="{$FONT_SIZE}"><xsl:value-of select="$LABEL" /></font></td>
   <td align="center"><font face="{$PAGE.FONT}" color="green" size="{$FONT_SIZE}"><xsl:value-of select="$COUNT" /></font></td>
   <td align="center"><font face="{$PAGE.FONT}" color="red" size="{$FONT_SIZE}"><xsl:value-of select="$BUGS" /></font></td>
   <td align="center"><font face="{$PAGE.FONT}" color="blue" size="{$FONT_SIZE}">
      <xsl:choose>
      <xsl:when test="$COUNT &gt; 0">
       <xsl:value-of select="format-number(number($BUGS div $COUNT), $PERCENTAGE.FORMAT)"/>
      </xsl:when>
      <xsl:otherwise>
       <xsl:value-of select="format-number(0, $PERCENTAGE.FORMAT)"/>
      </xsl:otherwise>
      </xsl:choose>
     </font>
   </td>
  </tr>
</xsl:template>

<xsl:template name="table_header" >
  <tr>
  <th><font face="{$PAGE.FONT}" size="4"><xsl:value-of select="$TABLE.HEADING.TYPE"/></font></th>
  <th><font face="{$PAGE.FONT}" size="4"><xsl:value-of select="$TABLE.HEADING.COUNT"/></font></th>
  <th><font face="{$PAGE.FONT}" size="4"><xsl:value-of select="$TABLE.HEADING.BUGS"/></font></th>
  <th><font face="{$PAGE.FONT}" size="4"><xsl:value-of select="$TABLE.HEADING.PERCENT"/></font></th>
  </tr>
</xsl:template>

<xsl:template match="FindBugsSummary" >
  <center><table width="{$TABLE.WIDTH}" border="1">
   <xsl:call-template name="table_header" />

   <xsl:call-template name="status_table_row">
     <xsl:with-param name="LABEL" select="$TABLE.ROW.OUTER" />
     <xsl:with-param name="COUNT" select="count(PackageStats/ClassStats[@interface='false' and substring-after(@class,'$')=''])" />
     <xsl:with-param name="BUGS" select="sum(PackageStats/ClassStats[@interface='false' and substring-after(@class,'$')='']/@bugs)" />
   </xsl:call-template>

   <xsl:call-template name="status_table_row">
     <xsl:with-param name="LABEL" select="$TABLE.ROW.INNER" />
     <xsl:with-param name="COUNT" select="count(PackageStats/ClassStats[@interface='false' and substring-after(@class,'$')!=''])" />
     <xsl:with-param name="BUGS" select="sum(PackageStats/ClassStats[@interface='false' and substring-after(@class,'$')!='']/@bugs)" />
   </xsl:call-template>

   <xsl:call-template name="status_table_row">
     <xsl:with-param name="LABEL" select="$TABLE.ROW.INTERFACE" />
     <xsl:with-param name="COUNT" select="count(PackageStats/ClassStats[@interface='true'])" />
     <xsl:with-param name="BUGS" select="sum(PackageStats/ClassStats[@interface='true']/@bugs)" />
   </xsl:call-template>

   <xsl:call-template name="status_table_row">
     <xsl:with-param name="LABEL" select="$TABLE.ROW.TOTAL" />
     <xsl:with-param name="COUNT" select="@total_classes" />
     <xsl:with-param name="BUGS" select="@total_bugs"/>
     <xsl:with-param name="FONT_SIZE" select="5"/>
   </xsl:call-template>
   <xsl:variable name="num_packages" select="count(PackageStats)" />
   <tr><td align="center" colspan="4"><font face="{$PAGE.FONT}" size="4">
     <xsl:call-template name='string_format'>
     <xsl:with-param name="COUNT" select="$num_packages"/>
     <xsl:with-param name="STRING" select="$TOTAL.PACKAGES.LABEL"/>
     <xsl:with-param name="SINGLE" select="$PACKAGE.SINGLE.LABEL"/>
     <xsl:with-param name="PULURAL" select="$PACKAGE.PULURAL.LABEL"/>
     </xsl:call-template>
     </font></td>
   </tr>
  </table></center>
</xsl:template>


<xsl:template name='string_format'>
  <xsl:param name="COUNT" select="1"/>
  <xsl:param name="STRING" select="''"/>
  <xsl:param name="SINGLE" select="''"/>
  <xsl:param name="PULURAL" select="''"/>
  <xsl:variable name="count_str" select="concat(substring-before($STRING,'#1'), $COUNT, substring-after($STRING,'#1'))" />

  <xsl:choose>
    <xsl:when test="$COUNT &gt; 1">
      <xsl:value-of select="concat(substring-before($count_str,'$1'), $PULURAL, substring-after($count_str,'$1'))" />
    </xsl:when>
    <xsl:otherwise>
    <xsl:value-of select="concat(substring-before($count_str,'$1'), $SINGLE, substring-after($count_str,'$1'))" />
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>


<xsl:template match="PackageStats" >
  <xsl:variable name="package-name">
    <xsl:choose>
      <xsl:when test="@package = ''">
        <xsl:value-of select="$DEFAULT.PACKAGE.NAME"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="@package"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>
  <xsl:variable name="package-prefix">
    <xsl:choose>
      <xsl:when test="@package = ''">
        <xsl:text></xsl:text>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="concat(@package,'.')"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>
  <center><h2><xsl:value-of select="$PACKAGE.LABEL"/><i><font color='green'><xsl:value-of select="$package-name" /></font></i></h2></center>
  <center><table width="{$TABLE.WIDTH}" border="1">
   <xsl:call-template name="table_header" />

   <xsl:call-template name="status_table_row">
     <xsl:with-param name="LABEL" select="$TABLE.ROW.OUTER" />
     <xsl:with-param name="COUNT" select="count(ClassStats[@interface='false' and substring-after(@class,'$')=''])" />
     <xsl:with-param name="BUGS" select="sum(ClassStats[@interface='false' and substring-after(@class,'$')='']/@bugs)" />
   </xsl:call-template>

   <xsl:call-template name="status_table_row">
     <xsl:with-param name="LABEL" select="$TABLE.ROW.INNER" />
     <xsl:with-param name="COUNT" select="count(ClassStats[@interface='false' and substring-after(@class,'$')!=''])" />
     <xsl:with-param name="BUGS" select="sum(ClassStats[@interface='false' and substring-after(@class,'$')!='']/@bugs)" />
   </xsl:call-template>

   <xsl:call-template name="status_table_row">
     <xsl:with-param name="LABEL" select="$TABLE.ROW.INTERFACE" />
     <xsl:with-param name="COUNT" select="count(ClassStats[@interface='true'])" />
     <xsl:with-param name="BUGS" select="sum(ClassStats[@interface='true']/@bugs)" />
   </xsl:call-template>

   <xsl:call-template name="status_table_row">
     <xsl:with-param name="LABEL" select="$TABLE.ROW.TOTAL" />
     <xsl:with-param name="COUNT" select="@total_types" />
     <xsl:with-param name="BUGS" select="@total_bugs" />
     <xsl:with-param name="FONT_SIZE" select="5"/>
   </xsl:call-template>

  </table>
  <xsl:if test="@total_bugs &gt; 0">
  <table width="{$TABLE.WIDTH}" border="0">
     <xsl:variable name="max_bugs">
       <xsl:for-each select="ClassStats">
         <xsl:sort select="@bugs" data-type="number" order="descending"/>
         <xsl:if test="position()=1">
           <xsl:value-of select="@bugs"/>
         </xsl:if>
       </xsl:for-each>
     </xsl:variable>

     <tr>
       <td align="left" colspan="2">
         <font face="{$PAGE.FONT}" size="4">
     <xsl:call-template name='string_format'>
     <xsl:with-param name="COUNT" select="$max_bugs"/>
     <xsl:with-param name="STRING" select="$PACKAGE.BUGCLASS.LABEL"/>
     <xsl:with-param name="SINGLE" select="$BUGS.SINGLE.LABEL"/>
     <xsl:with-param name="PULURAL" select="$BUGS.PULURAL.LABEL"/>
     </xsl:call-template>
         </font>
       </td>
     </tr>

     <xsl:for-each select="ClassStats">
       <xsl:if test="@bugs = $max_bugs">
       <tr>
          <td>&#160;&#160;&#160;&#160;&#160;&#160;&#160;</td>
          <td align="left"><font face="{$PAGE.FONT}" color="red" size="4"><i><xsl:value-of select="$package-prefix"/><xsl:value-of select="@class" /></i></font></td>
       </tr>
       </xsl:if>
     </xsl:for-each>

   </table>
  </xsl:if>
  </center>
  <br/>
</xsl:template>

</xsl:stylesheet>

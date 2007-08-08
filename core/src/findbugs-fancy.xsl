<?xml version="1.0" encoding="UTF-8" ?>
<!--
  Copyright (C) 2005, Etienne Giraudy, InStranet Inc

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

<xsl:transform xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0" >
   <xsl:output
         method="xml" indent="yes"
         doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd"
         doctype-public="-//W3C//DTD XHTML 1.0 Strict//EN"
         encoding="UTF-8"/>

   <xsl:key name="lbc-category-key"    match="/BugCollection/BugInstance" use="@category" />
   <xsl:key name="lbc-code-key"        match="/BugCollection/BugInstance" use="concat(@category,@abbrev)" />
   <xsl:key name="lbc-bug-key"         match="/BugCollection/BugInstance" use="concat(@category,@abbrev,@type)" />

   <xsl:key name="lbp-class-bug-type"  match="/BugCollection/BugInstance" use="concat(Class/@classname,@type)" />
    <xsl:param name="buildLabel"/>

<xsl:template match="/" >
<html>
   <head>
      <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
      <title>FindBugs (<xsl:value-of select="/BugCollection/@version" />) Analysis for <xsl:value-of select="$buildLabel" /></title>
      <script type="text/javascript">
         function show(foo) {
            document.getElementById(foo).style.display="block";
         }
         function hide(foo) {
            document.getElementById(foo).style.display="none";
         }
         function toggle(foo) {
            if( document.getElementById(foo).style.display == "none") {
               show(foo);
            } else {
               if( document.getElementById(foo).style.display == "block") {
                  hide(foo);
               } else {
                  show(foo);
               }
            }
         }
         function showmenu(foo) {
            if( document.getElementById(foo).style.display == "none") {
               hide("bug-summary");
               document.getElementById("bug-summary-tab").className="menu-tab";
               hide("analysis-data");
               document.getElementById("analysis-data-tab").className="menu-tab";
               //hide("list-by-bug-type");
               //document.getElementById("list-by-bug-type-tab").className="menu-tab";
               hide("list-by-package");
               document.getElementById("list-by-package-tab").className="menu-tab";
               hide("list-by-category");
               document.getElementById("list-by-category-tab").className="menu-tab";
               document.getElementById(foo+"-tab").className="menu-tab-selected";
               show(foo);
            }
            // else menu already selected!
         }
      </script>
      <style type='text/css'>
         html, body {
            background-color: #ffffff;
         }
         a, a:link , a:active, a:visited, a:hover {
            text-decoration: none; color: black;
         }
         div, span {
            vertical-align: top;
         }
         p {
            margin: 0px;
         }
         #header {
            width: 100%;
            text-align: center;
            margin-bottom: 5px;
            font-size: 14pt;
            color: red;
         }
         #menu {
            margin-bottom: 10px;
         }
         #menu ul {
         margin-left: 0;
         padding-left: 0;
         display: inline;
         }
         #menu ul li {
         margin-left: 0;
         margin-bottom: 0;
         padding: 2px 15px 5px;
         border: 1px solid #000;
         list-style: none;
         display: inline;
         }
         #menu ul li.here {
         border-bottom: 1px solid #ffc;
         list-style: none;
         display: inline;
         }
         .menu-tab {
            background: white;
         }
         .menu-tab:hover {
            background: grey;
         }
         .menu-tab-selected {
            background: #aaaaaa;
         }
         #analysis-data ul {
            margin-left: 15px;
         }
         #analyzed-files, #used-libraries, #analysis-error {
           float: left;
           margin: 2px;
           border: 1px black solid;
           padding: 2px;
         }
         #analyzed-files {
           width: 25%;
         }
         #used-libraries {
           width: 25%;
         }
         #analysis-error {
           width: 40%;
         }
         div.summary {
            width:100%;
            text-align:center;
         }
         .summary table {
            border:1px solid black;
         }
         .summary th {
            background: #aaaaaa;
            color: white;
         }
         .summary th, .summary td {
            padding: 2px 4px 2px 4px;
         }
         .summary-name {
            background: #eeeeee;
            text-align:left;
         }
         .summary-size {
            background: #eeeeee;
            text-align:center;
         }
         .summary-ratio {
            background: #eeeeee;
            text-align:center;
         }
         .summary-priority-all {
            background: #dddddd;
            text-align:center;
         }
         .summary-priority-1 {
            background: red;
            text-align:center;
         }
         .summary-priority-2 {
            background: orange;
            text-align:center;
         }
         .summary-priority-3 {
            background: green;
            text-align:center;
         }
         .summary-priority-4 {
            background: blue;
            text-align:center;
         }
         .outerbox {
            border: 1px solid black;
            margin: 10px;
         }
         .outerbox-title {
            border-bottom: 1px solid #000000; font-size: 12pt; font-weight: bold;
            background: #cccccc; margin: 0; padding: 0 5px 0 5px;
         }
         .title-help {
            font-weight: normal;
         }
         .innerbox-1, .innerbox-2 {
            margin: 0 0 0 10px;
         }
         .innerbox-1-title, .innerbox-2-title {
            border-bottom: 1px solid #000000; border-left: 1px solid #000000;
            margin: 0; padding: 0 5px 0 5px;
            font-size: 12pt; font-weight: bold; background: #cccccc;
         }
         .bug-box {
            border-bottom: 1px solid #000000; border-left: 1px solid #000000;
         }
         .bug-priority-1 {
            background: red; height: 0.5em; width: 1em;
            margin-right: 0.5em;
         }
         .bug-priority-2 {
            background: orange; height: 0.5em; width: 1em;
            margin-right: 0.5em;
         }
         .bug-priority-3 {
            background: green; height: 0.5em; width: 1em;
            margin-right: 0.5em;
         }
         .bug-priority-4 {
            background: blue; height: 0.5em; width: 1em;
            margin-right: 0.5em;
         }
         .bug-type {
         }
         .bug-ref {
            font-size: 10pt; font-weight: bold; padding: 0 0 0 60px;
         }
         .bug-descr {
            font-weight: normal; background: #eeeee0;
            padding: 0 5px 0 5px; border-bottom: 1px dashed black; margin: 0px;
         }
         .bug-details {
            font-weight: normal; background: #eeeee0;
            padding: 0 5px 0 5px; margin: 0px;
         }
      </style>
   </head>
   <body>
      <div id="header">
         FindBugs (<xsl:value-of select="/BugCollection/@version" />) Analysis for <xsl:value-of select="$buildLabel" />
      </div>
      <div id="menu">
         <ul>
            <li id='bug-summary-tab' class='menu-tab-selected'>
               <xsl:attribute name="onclick">showmenu('bug-summary');return false;</xsl:attribute>
               <a href='' onclick='return false;'>Bug Summary</a>
            </li>
            <li id='analysis-data-tab' class='menu-tab'>
               <xsl:attribute name="onclick">showmenu('analysis-data');return false;</xsl:attribute>
               <a href='' onclick='return false;'>Analysis Informations</a>
            </li>
            <li id='list-by-category-tab' class='menu-tab'>
               <xsl:attribute name="onclick">showmenu('list-by-category');return false;</xsl:attribute>
               <a href='' onclick='return false;'>List bugs by bug category</a>
            </li>
            <li id='list-by-package-tab' class='menu-tab'>
               <xsl:attribute name="onclick">showmenu('list-by-package');return false;</xsl:attribute>
               <a href='' onclick='return false;'>List bugs by package</a>
            </li>
         </ul>
      </div>
      <xsl:call-template name="generateSummary" />
      <xsl:call-template name="analysis-data" />
      <xsl:call-template name="list-by-category" />
      <xsl:call-template name="list-by-package" />
   </body>
</html>
</xsl:template>

<!-- ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ -->
<!-- generate summary report from stats -->
<xsl:template name="generateSummary" >
<div class='summary' id='bug-summary'>
   <h2>FindBugs Analysis generated at: <xsl:value-of select="/BugCollection/FindBugsSummary/@timestamp" /></h2>
   <table>
      <tr>
         <th>Package</th>
         <th>Code Size</th>
         <th>Bugs</th>
         <th>Bugs p1</th>
         <th>Bugs p2</th>
         <th>Bugs p3</th>
         <th>Bugs Exp.</th>
         <th>Ratio</th>
      </tr>
      <tr>
         <td class='summary-name'>
            Overall
            (<xsl:value-of select="/BugCollection/FindBugsSummary/@num_packages" /> packages),
            (<xsl:value-of select="/BugCollection/FindBugsSummary/@total_classes" /> classes)
         </td>
         <td class='summary-size'><xsl:value-of select="/BugCollection/FindBugsSummary/@total_size" /></td>
         <td class='summary-priority-all'><xsl:value-of select="/BugCollection/FindBugsSummary/@total_bugs" /></td>
         <td class='summary-priority-1'><xsl:value-of select="/BugCollection/FindBugsSummary/@priority_1" /></td>
         <td class='summary-priority-2'><xsl:value-of select="/BugCollection/FindBugsSummary/@priority_2" /></td>
         <td class='summary-priority-3'><xsl:value-of select="/BugCollection/FindBugsSummary/@priority_3" /></td>
         <td class='summary-priority-4'><xsl:value-of select="/BugCollection/FindBugsSummary/@priority_4" /></td>
         <td class='summary-ratio'></td>
      </tr>
      <xsl:for-each select="/BugCollection/FindBugsSummary/PackageStats">
         <xsl:sort select="@package" />
         <xsl:if test="@total_bugs!='0'" >
            <tr>
               <td class='summary-name'><xsl:value-of select="@package" /></td>
               <td class='summary-size'><xsl:value-of select="@total_size" /></td>
               <td class='summary-priority-all'><xsl:value-of select="@total_bugs" /></td>
               <td class='summary-priority-1'><xsl:value-of select="@priority_1" /></td>
               <td class='summary-priority-2'><xsl:value-of select="@priority_2" /></td>
               <td class='summary-priority-3'><xsl:value-of select="@priority_3" /></td>
               <td class='summary-priority-4'><xsl:value-of select="@priority_4" /></td>
               <td class='summary-ratio'></td>
<!--
               <xsl:for-each select="ClassStats">
                  <xsl:if test="@bugs!='0'" >
                  <li>
                     <xsl:value-of select="@class" /> - total: <xsl:value-of select="@bugs" />
                  </li>
                  </xsl:if>
               </xsl:for-each>
-->
            </tr>
         </xsl:if>
      </xsl:for-each>
   </table>
</div>
</xsl:template>

<!-- ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ -->
<!-- display analysis info -->
<xsl:template name="analysis-data">
      <div id='analysis-data' style='display:none;'>
         <div id='analyzed-files'>
            Analyzed Files:
            <ul>
               <xsl:for-each select="/BugCollection/Project/Jar">
                  <li><xsl:apply-templates /></li>
               </xsl:for-each>
            </ul>
         </div>
         <div id='used-libraries'>
            Used Libraries:
            <ul>
               <xsl:for-each select="/BugCollection/Project/AuxClasspathEntry">
                  <li><xsl:apply-templates /></li>
               </xsl:for-each>
            </ul>
         </div>
         <div id='analysis-error'>
            Analysis Errors:
            <ul>
               <xsl:variable name="error-count"
                             select="count(/BugCollection/Errors/MissingClass)" />
               <xsl:if test="$error-count=0" >
                  <li>None</li>
               </xsl:if>
               <xsl:if test="$error-count>0" >
                  <li>Missing ref classes for analysis:
                     <ul>
                        <xsl:for-each select="/BugCollection/Errors/MissingClass">
                           <li><xsl:apply-templates /></li>
                        </xsl:for-each>
                     </ul>
                  </li>
               </xsl:if>
            </ul>
         </div>
      </div>
</xsl:template>

<!-- ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ -->
<!-- show priorities helper -->
<xsl:template name="helpPriorities">
   <span>
      <xsl:attribute name="class">bug-priority-1</xsl:attribute>
      &#160;&#160;
   </span> P1
   <span>
      <xsl:attribute name="class">bug-priority-2</xsl:attribute>
      &#160;&#160;
   </span> P2
   <span>
      <xsl:attribute name="class">bug-priority-3</xsl:attribute>
      &#160;&#160;
   </span> P3
   <span>
      <xsl:attribute name="class">bug-priority-4</xsl:attribute>
      &#160;&#160;
   </span> Exp.
</xsl:template>

<!-- ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ -->
<!-- display the details of a bug -->
<xsl:template name="display-bug" >
   <xsl:param name="bug-type"    select="''" />
   <xsl:param name="bug-id"      select="''" />
   <xsl:param name="which-list"  select="''" />
   <div class="bug-box">
      <a>
         <xsl:attribute name="href"></xsl:attribute>
         <xsl:attribute name="onclick">toggle('<xsl:value-of select="$which-list" />-<xsl:value-of select="@uid" />');return false;</xsl:attribute>
         <span>
            <xsl:attribute name="class">bug-priority-<xsl:value-of select="@priority"/></xsl:attribute>
            &#160;&#160;
         </span>
         <span class="bug-type"><xsl:value-of select="@abbrev" />: </span> <xsl:value-of select="Class/Message" />
      </a>
      <div style="display:none;">
         <xsl:attribute name="id"><xsl:value-of select="$which-list" />-<xsl:value-of select="@uid" /></xsl:attribute>
         <xsl:for-each select="*/Message">
            <div class="bug-ref"><xsl:apply-templates /></div>
         </xsl:for-each>
         <div class="bug-descr">
            <xsl:value-of select="LongMessage" disable-output-escaping="no" />
         </div>
         <div class="bug-details"><xsl:value-of select="/BugCollection/BugPattern[@type=$bug-type]/Details" disable-output-escaping="yes" /></div>
      </div>
   </div>
</xsl:template>

<!-- ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ -->
<!-- main template for the list by category -->
<xsl:template name="list-by-category" >
   <div id='list-by-category' class='data-box' style='display:none;'>
      <xsl:call-template name="helpPriorities" />
      <xsl:variable name="unique-category" select="/BugCollection/BugInstance[generate-id() = generate-id(key('lbc-category-key',@category))]/@category" />
      <xsl:for-each select="$unique-category">
         <xsl:sort select="." order="ascending" />
            <xsl:call-template name="categories">
               <xsl:with-param name="category" select="." />
            </xsl:call-template>
      </xsl:for-each>
   </div>
</xsl:template>

<xsl:template name="categories" >
   <xsl:param name="category" select="''" />
   <xsl:variable name="category-count"
                       select="count(/BugCollection/BugInstance[@category=$category])" />
   <xsl:variable name="category-count-p1"
                       select="count(/BugCollection/BugInstance[@category=$category and @priority='1'])" />
   <xsl:variable name="category-count-p2"
                       select="count(/BugCollection/BugInstance[@category=$category and @priority='2'])" />
   <xsl:variable name="category-count-p3"
                       select="count(/BugCollection/BugInstance[@category=$category and @priority='3'])" />
   <xsl:variable name="category-count-p4"
                       select="count(/BugCollection/BugInstance[@category=$category and @priority='4'])" />
   <div class='outerbox'>
      <div class='outerbox-title'>
         <a>
            <xsl:attribute name="href"></xsl:attribute>
            <xsl:attribute name="onclick">toggle('category-<xsl:value-of select="$category" />');return false;</xsl:attribute>
            <xsl:value-of select="/BugCollection/BugCategory[@category=$category]/Description" />
            (<xsl:value-of select="$category-count" />:
            <span class='title-help'><xsl:value-of select="$category-count-p1" />/<xsl:value-of select="$category-count-p2" />/<xsl:value-of select="$category-count-p3" />/<xsl:value-of select="$category-count-p4" /></span>)
         </a>
      </div>
      <div style="display:none;">
         <xsl:attribute name="id">category-<xsl:value-of select="$category" /></xsl:attribute>
         <xsl:call-template name="list-by-category-and-code">
            <xsl:with-param name="category" select="$category" />
         </xsl:call-template>
      </div>
   </div>
</xsl:template>

<xsl:template name="list-by-category-and-code" >
   <xsl:param name="category" select="''" />
   <xsl:variable name="unique-code" select="/BugCollection/BugInstance[@category=$category and generate-id()= generate-id(key('lbc-code-key',concat(@category,@abbrev)))]/@abbrev" />
   <xsl:for-each select="$unique-code">
      <xsl:sort select="." order="ascending" />
         <xsl:call-template name="codes">
            <xsl:with-param name="category" select="$category" />
            <xsl:with-param name="code" select="." />
         </xsl:call-template>
   </xsl:for-each>
</xsl:template>

<xsl:template name="codes" >
   <xsl:param name="category" select="''" />
   <xsl:param name="code"     select="''" />
   <xsl:variable name="code-count"
                       select="count(/BugCollection/BugInstance[@category=$category and @abbrev=$code])" />
   <xsl:variable name="code-count-p1"
                       select="count(/BugCollection/BugInstance[@category=$category and @abbrev=$code and @priority='1'])" />
   <xsl:variable name="code-count-p2"
                       select="count(/BugCollection/BugInstance[@category=$category and @abbrev=$code and @priority='2'])" />
   <xsl:variable name="code-count-p3"
                       select="count(/BugCollection/BugInstance[@category=$category and @abbrev=$code and @priority='3'])" />
   <xsl:variable name="code-count-p4"
                       select="count(/BugCollection/BugInstance[@category=$category and @abbrev=$code and @priority='4'])" />
   <div class='innerbox-1'>
      <div class="innerbox-1-title">
         <a>
            <xsl:attribute name="href"></xsl:attribute>
            <xsl:attribute name="onclick">toggle('category-<xsl:value-of select="$category" />-and-code-<xsl:value-of select="$code" />');return false;</xsl:attribute>
            <xsl:value-of select="$code" />: <xsl:value-of select="/BugCollection/BugCode[@abbrev=$code]/Description" />
            (<xsl:value-of select="$code-count" />:
            <span class='title-help'><xsl:value-of select="$code-count-p1" />/<xsl:value-of select="$code-count-p2" />/<xsl:value-of select="$code-count-p3" />/<xsl:value-of select="$code-count-p4" /></span>)
         </a>
      </div>
      <div style="display:none;">
         <xsl:attribute name="id">category-<xsl:value-of select="$category" />-and-code-<xsl:value-of select="$code" /></xsl:attribute>
         <xsl:call-template name="list-by-category-and-code-and-bug">
            <xsl:with-param name="category" select="$category" />
            <xsl:with-param name="code" select="$code" />
         </xsl:call-template>
      </div>
   </div>
</xsl:template>

<xsl:template name="list-by-category-and-code-and-bug" >
   <xsl:param name="category" select="''" />
   <xsl:param name="code" select="''" />
   <xsl:variable name="unique-bug" select="/BugCollection/BugInstance[@category=$category and @abbrev=$code and generate-id()= generate-id(key('lbc-bug-key',concat(@category,@abbrev,@type)))]/@type" />
   <xsl:for-each select="$unique-bug">
      <xsl:sort select="." order="ascending" />
         <xsl:call-template name="bugs">
            <xsl:with-param name="category" select="$category" />
            <xsl:with-param name="code" select="$code" />
            <xsl:with-param name="bug" select="." />
         </xsl:call-template>
   </xsl:for-each>
</xsl:template>

<xsl:template name="bugs" >
   <xsl:param name="category" select="''" />
   <xsl:param name="code"     select="''" />
   <xsl:param name="bug"      select="''" />
   <xsl:variable name="bug-count"
                       select="count(/BugCollection/BugInstance[@category=$category and @abbrev=$code and @type=$bug])" />
   <xsl:variable name="bug-count-p1"
                       select="count(/BugCollection/BugInstance[@category=$category and @abbrev=$code and @type=$bug and @priority='1'])" />
   <xsl:variable name="bug-count-p2"
                       select="count(/BugCollection/BugInstance[@category=$category and @abbrev=$code and @type=$bug and @priority='2'])" />
   <xsl:variable name="bug-count-p3"
                       select="count(/BugCollection/BugInstance[@category=$category and @abbrev=$code and @type=$bug and @priority='3'])" />
   <xsl:variable name="bug-count-p4"
                       select="count(/BugCollection/BugInstance[@category=$category and @abbrev=$code and @type=$bug and @priority='4'])" />
   <div class='innerbox-2'>
      <div class='innerbox-2-title'>
         <a>
            <xsl:attribute name="href"></xsl:attribute>
            <xsl:attribute name="onclick">toggle('category-<xsl:value-of select="$category" />-and-code-<xsl:value-of select="$code" />-and-bug-<xsl:value-of select="$bug" />');return false;</xsl:attribute>
            <xsl:attribute name="title"><xsl:value-of select="$bug" /></xsl:attribute>
            <xsl:value-of select="/BugCollection/BugPattern[@category=$category and @abbrev=$code and @type=$bug]/ShortDescription" />&#160;&#160;
            (<xsl:value-of select="$bug-count" />:
            <span class='title-help'><xsl:value-of select="$bug-count-p1" />/<xsl:value-of select="$bug-count-p2" />/<xsl:value-of select="$bug-count-p3" />/<xsl:value-of select="$bug-count-p4" /></span>)
         </a>
      </div>
      <div style="display:none;">
         <xsl:attribute name="id">category-<xsl:value-of select="$category" />-and-code-<xsl:value-of select="$code" />-and-bug-<xsl:value-of select="$bug" /></xsl:attribute>
         <xsl:variable name="cat-code-type">category-<xsl:value-of select="$category" />-and-code-<xsl:value-of select="$code" />-and-bug-<xsl:value-of select="$bug" /></xsl:variable>
         <xsl:for-each select="/BugCollection/BugInstance[@category=$category and @abbrev=$code and @type=$bug]">
            <xsl:call-template name="display-bug">
               <xsl:with-param name="bug-type"     select="@type" />
               <xsl:with-param name="bug-id"       select="@uid" />
               <xsl:with-param name="which-list"   select="$cat-code-type" />
            </xsl:call-template>
         </xsl:for-each>
      </div>
   </div>
</xsl:template>

<!-- ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ -->
<!-- main template for the list by package -->
<xsl:template name="list-by-package" >
   <div id='list-by-package' class='data-box' style='display:none;'>
      <xsl:call-template name="helpPriorities" />
      <xsl:for-each select="/BugCollection/FindBugsSummary/PackageStats[@total_bugs != '0']/@package">
         <xsl:sort select="." order="ascending" />
            <xsl:call-template name="packages">
               <xsl:with-param name="package" select="." />
            </xsl:call-template>
      </xsl:for-each>
   </div>
</xsl:template>

<xsl:template name="packages" >
   <xsl:param name="package" select="''" />
   <xsl:variable name="package-count-p1">
      <xsl:if test="not(/BugCollection/FindBugsSummary/PackageStats[@package=$package]/@priority_1 != '')">0</xsl:if>
      <xsl:if test="/BugCollection/FindBugsSummary/PackageStats[@package=$package]/@priority_1 != ''">
         <xsl:value-of select="/BugCollection/FindBugsSummary/PackageStats[@package=$package]/@priority_1" />
      </xsl:if>
   </xsl:variable>
   <xsl:variable name="package-count-p2">
      <xsl:if test="not(/BugCollection/FindBugsSummary/PackageStats[@package=$package]/@priority_2 != '')">0</xsl:if>
      <xsl:if test="/BugCollection/FindBugsSummary/PackageStats[@package=$package]/@priority_2 != ''">
         <xsl:value-of select="/BugCollection/FindBugsSummary/PackageStats[@package=$package]/@priority_2" />
      </xsl:if>
   </xsl:variable>
   <xsl:variable name="package-count-p3">
      <xsl:if test="not(/BugCollection/FindBugsSummary/PackageStats[@package=$package]/@priority_3 != '')">0</xsl:if>
      <xsl:if test="/BugCollection/FindBugsSummary/PackageStats[@package=$package]/@priority_3 != ''">
         <xsl:value-of select="/BugCollection/FindBugsSummary/PackageStats[@package=$package]/@priority_3" />
      </xsl:if>
   </xsl:variable>
   <xsl:variable name="package-count-p4">
      <xsl:if test="not(/BugCollection/FindBugsSummary/PackageStats[@package=$package]/@priority_4 != '')">0</xsl:if>
      <xsl:if test="/BugCollection/FindBugsSummary/PackageStats[@package=$package]/@priority_4 != ''">
         <xsl:value-of select="/BugCollection/FindBugsSummary/PackageStats[@package=$package]/@priority_4" />
      </xsl:if>
   </xsl:variable>

   <div class='outerbox'>
      <div class='outerbox-title'>
         <a>
            <xsl:attribute name="href"></xsl:attribute>
            <xsl:attribute name="onclick">toggle('package-<xsl:value-of select="$package" />');return false;</xsl:attribute>
            <xsl:value-of select="$package" />
            (<xsl:value-of select="/BugCollection/FindBugsSummary/PackageStats[@package=$package]/@total_bugs" />:
            <span class='title-help'><xsl:value-of select="$package-count-p1" />/<xsl:value-of select="$package-count-p2" />/<xsl:value-of select="$package-count-p3" />/<xsl:value-of select="$package-count-p4" /></span>)
         </a>
      </div>
      <div style="display:none;">
         <xsl:attribute name="id">package-<xsl:value-of select="$package" /></xsl:attribute>
         <xsl:call-template name="list-by-package-and-class">
            <xsl:with-param name="package" select="$package" />
         </xsl:call-template>
      </div>
   </div>
</xsl:template>

<xsl:template name="list-by-package-and-class" >
   <xsl:param name="package" select="''" />
   <xsl:for-each select="/BugCollection/FindBugsSummary/PackageStats[@package=$package]/ClassStats[@bugs != '0']/@class">
      <xsl:sort select="." order="ascending" />
         <xsl:call-template name="classes">
            <xsl:with-param name="package" select="$package" />
            <xsl:with-param name="class" select="." />
         </xsl:call-template>
   </xsl:for-each>
</xsl:template>

<xsl:template name="classes" >
   <xsl:param name="package" select="''" />
   <xsl:param name="class"     select="''" />
   <xsl:variable name="class-count"
                       select="/BugCollection/FindBugsSummary/PackageStats[@package=$package]/ClassStats[@class=$class and @bugs != '0']/@bugs" />

   <xsl:variable name="class-count-p1">
      <xsl:if test="not(/BugCollection/FindBugsSummary/PackageStats[@package=$package]/ClassStats[@class=$class and @bugs != '0']/@priority_1 != '')">0</xsl:if>
      <xsl:if test="/BugCollection/FindBugsSummary/PackageStats[@package=$package]/ClassStats[@class=$class and @bugs != '0']/@priority_1 != ''">
         <xsl:value-of select="/BugCollection/FindBugsSummary/PackageStats[@package=$package]/ClassStats[@class=$class and @bugs != '0']/@priority_1" />
      </xsl:if>
   </xsl:variable>
   <xsl:variable name="class-count-p2">
      <xsl:if test="not(/BugCollection/FindBugsSummary/PackageStats[@package=$package]/ClassStats[@class=$class and @bugs != '0']/@priority_2 != '')">0</xsl:if>
      <xsl:if test="/BugCollection/FindBugsSummary/PackageStats[@package=$package]/ClassStats[@class=$class and @bugs != '0']/@priority_2 != ''">
         <xsl:value-of select="/BugCollection/FindBugsSummary/PackageStats[@package=$package]/ClassStats[@class=$class and @bugs != '0']/@priority_2" />
      </xsl:if>
   </xsl:variable>
   <xsl:variable name="class-count-p3">
      <xsl:if test="not(/BugCollection/FindBugsSummary/PackageStats[@package=$package]/ClassStats[@class=$class and @bugs != '0']/@priority_3 != '')">0</xsl:if>
      <xsl:if test="/BugCollection/FindBugsSummary/PackageStats[@package=$package]/ClassStats[@class=$class and @bugs != '0']/@priority_3 != ''">
         <xsl:value-of select="/BugCollection/FindBugsSummary/PackageStats[@package=$package]/ClassStats[@class=$class and @bugs != '0']/@priority_3" />
      </xsl:if>
   </xsl:variable>
   <xsl:variable name="class-count-p4">
      <xsl:if test="not(/BugCollection/FindBugsSummary/PackageStats[@package=$package]/ClassStats[@class=$class and @bugs != '0']/@priority_4 != '')">0</xsl:if>
      <xsl:if test="/BugCollection/FindBugsSummary/PackageStats[@package=$package]/ClassStats[@class=$class and @bugs != '0']/@priority_4 != ''">
         <xsl:value-of select="/BugCollection/FindBugsSummary/PackageStats[@package=$package]/ClassStats[@class=$class and @bugs != '0']/@priority_4" />
      </xsl:if>
   </xsl:variable>


   <div class='innerbox-1'>
      <div class="innerbox-1-title">
         <a>
            <xsl:attribute name="href"></xsl:attribute>
            <xsl:attribute name="onclick">toggle('package-<xsl:value-of select="$package" />-and-class-<xsl:value-of select="$class" />');return false;</xsl:attribute>
            <xsl:value-of select="$class" />  (<xsl:value-of select="$class-count" />:
            <span class='title-help'><xsl:value-of select="$class-count-p1" />/<xsl:value-of select="$class-count-p2" />/<xsl:value-of select="$class-count-p3" />/<xsl:value-of select="$class-count-p4" /></span>)
         </a>
      </div>
      <div style="display:none;">
         <xsl:attribute name="id">package-<xsl:value-of select="$package" />-and-class-<xsl:value-of select="$class" /></xsl:attribute>
         <xsl:call-template name="list-by-package-and-class-and-bug">
            <xsl:with-param name="package" select="$package" />
            <xsl:with-param name="class" select="$class" />
         </xsl:call-template>
      </div>
   </div>
</xsl:template>


<xsl:template name="list-by-package-and-class-and-bug" >
   <xsl:param name="package" select="''" />
   <xsl:param name="class" select="''" />
   <xsl:variable name="unique-class-bugs" select="/BugCollection/BugInstance[Class[position()=1 and @classname=$class] and generate-id() = generate-id(key('lbp-class-bug-type',concat(Class/@classname,@type)))]/@type" />

   <xsl:for-each select="$unique-class-bugs">
      <xsl:sort select="." order="ascending" />
         <xsl:call-template name="class-bugs">
            <xsl:with-param name="package" select="$package" />
            <xsl:with-param name="class" select="$class" />
            <xsl:with-param name="type" select="." />
         </xsl:call-template>
   </xsl:for-each>
</xsl:template>

<xsl:template name="class-bugs" >
   <xsl:param name="package" select="''" />
   <xsl:param name="class"     select="''" />
   <xsl:param name="type"      select="''" />
   <xsl:variable name="bug-count"
                       select="count(/BugCollection/BugInstance[@type=$type and Class[position()=1 and @classname=$class]])" />
   <div class='innerbox-2'>
      <div class='innerbox-2-title'>
         <a>
            <xsl:attribute name="href"></xsl:attribute>
            <xsl:attribute name="onclick">toggle('package-<xsl:value-of select="$package" />-and-class-<xsl:value-of select="$class" />-and-type-<xsl:value-of select="$type" />');return false;</xsl:attribute>
            <xsl:attribute name="title"><xsl:value-of select="$type" /></xsl:attribute>
            <xsl:value-of select="/BugCollection/BugPattern[@type=$type]/ShortDescription" />&#160;&#160;
            (<xsl:value-of select="$bug-count" />)
         </a>
      </div>
      <div style="display:none;">
         <xsl:attribute name="id">package-<xsl:value-of select="$package" />-and-class-<xsl:value-of select="$class" />-and-type-<xsl:value-of select="$type" /></xsl:attribute>
         <xsl:variable name="package-class-type">package-<xsl:value-of select="$package" />-and-class-<xsl:value-of select="$class" />-and-type-<xsl:value-of select="$type" /></xsl:variable>
         <xsl:for-each select="/BugCollection/BugInstance[@type=$type and Class[position()=1 and @classname=$class]]">
            <xsl:call-template name="display-bug">
               <xsl:with-param name="bug-type"     select="@type" />
               <xsl:with-param name="bug-id"       select="@uid" />
               <xsl:with-param name="which-list"   select="$package-class-type" />
            </xsl:call-template>
         </xsl:for-each>
      </div>
   </div>
</xsl:template>



</xsl:transform>
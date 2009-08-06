<?xml version="1.0"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:param name="buildLabel"/>
    <xsl:param name="cvsModule"/>

    <xsl:template match="/checkstyle">
        <html>
            <head>
                <title>Checkstyle Report for
                    <xsl:value-of select="$buildLabel"/>
                </title>
            </head>
            <body>
                <h1>Checkstyle Report for
                    <xsl:value-of select="$buildLabel"/>
                </h1>
                <table cellspacing="5">
                    <tr>
                        <td>Files:</td>
                        <td>
                            <xsl:value-of select="count(file)"/>
                        </td>
                    </tr>
                    <tr>
                        <td>"Warning Priority" Violations:</td>
                        <td>
                            <xsl:value-of select="count(file/error[@severity='warning'])"/>
                        </td>
                    </tr>
                    <tr>
                        <td>"Error" Violations</td>
                        <td>
                            <font color="RED">
                                <xsl:value-of select="count(file/error[@severity='error'])"/>
                            </font>
                        </td>
                    </tr>
                    <tr>
                        <td>Total Violations</td>
                        <td>
                            <xsl:value-of select="count(file/error)"/>
                        </td>
                    </tr>
                </table>
                <p/>
                <h1>Files</h1>
                <table border="1" width="100%">
                    <tr>
                        <th>File</th>
                        <th>"warning" Errors</th>
                        <th>"Error" Errors</th>
                        <th>Total Errors</th>
                    </tr>
                    <xsl:for-each select="file">
                        <tr>
                            <td>
                                <a>
                                    <xsl:attribute name="href">#<xsl:value-of select="@name"/></xsl:attribute>
                                    <xsl:value-of select="@name"/>
                                </a>
                            </td>
                            <td>
                                <xsl:value-of select="count(error[@severity='warning'])"/>
                            </td>
                            <td>
                                <xsl:if test="count(error[@severity='error']) > 0">
                                    <xsl:attribute name="bgcolor">RED</xsl:attribute>
                                </xsl:if>
                                <xsl:value-of select="count(error[@severity='error'])"/>
                            </td>
                            <td>
                                <xsl:value-of select="count(error)"/>
                            </td>
                        </tr>
                    </xsl:for-each>
                </table>
                <p/>
                <ul>
                    <xsl:for-each select="file">
                        <a>
                            <xsl:attribute name="name">
                                <xsl:value-of select="@name"></xsl:value-of>
                            </xsl:attribute>
                        </a>
                        <li>
                            <a>
                                <xsl:attribute name="href">../../src/<xsl:value-of select="translate(substring-after(@name, '\src\java\'), '\','/')"/></xsl:attribute>
                                <font size="+1">
                                    <b>
                                        <xsl:value-of select="@name"/>
                                    </b>
                                </font>
                            </a>
                            <ul>
                                <xsl:for-each select="error">
                                    <li>
                                        <font>
                                            <xsl:if test="@severity='error'">
                                                <xsl:attribute name="color">RED</xsl:attribute>
                                            </xsl:if>
                                            Line: <xsl:value-of select="@line"/> - <xsl:value-of select="@message"/>
                                        </font>
                                    </li>
                                </xsl:for-each>
                            </ul>
                        </li>
                    </xsl:for-each>
                </ul>
            </body>
        </html>
    </xsl:template>

</xsl:stylesheet>

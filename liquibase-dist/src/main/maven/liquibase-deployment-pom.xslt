<xsl:stylesheet version="1.0"
                exclude-result-prefixes="maven "
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:maven="http://maven.apache.org/POM/4.0.0">
    <!-- Identity transform -->
    <xsl:template match="@* | node()">
        <xsl:copy>
            <xsl:apply-templates select="@* | node()"/>
        </xsl:copy>
    </xsl:template>

    <!-- remove all profiles -->
    <xsl:template match="maven:profiles"/>

    <!-- remove all org.liquibase sub-module dependencies -->
    <xsl:template match="maven:dependencies/maven:dependency[maven:groupId = 'org.liquibase']"/>

    <!--
    Set module <name> as "Liquibase".
    We do not set it in the original pom.xml since it makes build output more confusing - nice to keep the capitalization consistent
    -->
    <xsl:template match="maven:project/maven:description">
        <name xmlns="http://maven.apache.org/POM/4.0.0">Liquibase</name>
        <xsl:copy-of select="."/>
    </xsl:template>

    <!--
    Set artifactId as "liquibase".
    We do not set it in the original pom.xml since it makes build output more confusing - nice to keep the module names matching directory names
    -->
    <xsl:template match="maven:artifactId">
        <artifactId xmlns="http://maven.apache.org/POM/4.0.0">liquibase</artifactId>
    </xsl:template>

</xsl:stylesheet>

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

    <!-- remove all org.liquibase sub-module dependencies except liquibase-core -->
    <xsl:template match="maven:dependencies/maven:dependency[maven:groupId = 'org.liquibase'  and maven:artifactId != 'liquibase-core']"/>

    <!--
    Set module <name> as "Liquibase".
    We do not set it in the original pom.xml since it makes build output more confusing - nice to keep the capitalization consistent
    -->
    <xsl:template match="maven:project/maven:description">
        <name xmlns="http://maven.apache.org/POM/4.0.0">Liquibase Maven Plugin</name>
        <xsl:copy-of select="."/>
    </xsl:template>

</xsl:stylesheet>

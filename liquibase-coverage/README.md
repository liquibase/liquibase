# liquibase-coverage

This module imports JaCoCo's aggregate XML reports to send unit and integration test coverage across modules to Sonar.

It provides several execution configurarations to be invoked by `jacococli` to manage and merge reports:

```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>${jacoco-maven-plugin.version}</version>
    <executions>
    <execution>
        <id>report-aggregate</id>
        <phase>verify</phase>
        <goals>
        <goal>report-aggregate</goal>
        </goals>
    </execution>
    <execution>
        <id>merge-results</id>
        <phase>verify</phase>
        <goals>
        <goal>merge</goal>
        </goals>
        <configuration>
        <fileSets>
            <fileSet>
            <directory>${code.coverage.project.folder}</directory>
            <includes>
                <include>*.exec</include>
            </includes>
            </fileSet>
        </fileSets>
        <destFile>${code.coverage.overall.data.folder}/aggregate.exec</destFile>
        </configuration>
    </execution>
    <execution>
        <id>cli-merge-results</id>
        <phase>test</phase>
        <goals>
        <goal>merge</goal>
        </goals>
        <configuration>
        <fileSets>
            <fileSet>
            <directory>${code.coverage.overall.data.folder}</directory>
            <includes>
                <include>*.exec</include>
            </includes>
            </fileSet>
        </fileSets>
        <destFile>${code.coverage.overall.data.folder}/aggregate.exec</destFile>
        </configuration>
    </execution>
    </executions>
</plugin>
```

Check out the following reusable workflow to know more about how aggregated reports are generated: [sonar-test-scan.yml](https://github.com/liquibase/build-logic/blob/main/.github/workflows/sonar-test-scan.yml)
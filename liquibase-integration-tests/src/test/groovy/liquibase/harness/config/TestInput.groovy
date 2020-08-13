package liquibase.harness.config

import groovy.transform.builder.Builder;

@Builder
class TestInput {
     String databaseName
     String url
     String dbSchema
     String username
     String password
     String version
     String context
     String changeObject
}
#!/bin/bash
set -e  # Exit on any error

echo "🏗️  STEP 1: Building Liquibase Snowflake Extension"
echo "Command: ./mvnw clean install -DskipTests -pl '!liquibase-maven-plugin,!liquibase-dist'"
./mvnw clean install -DskipTests -pl '!liquibase-maven-plugin,!liquibase-dist'

echo ""
echo "🧪 STEP 2: Running Unit Tests"  
echo "Command: ./mvnw test -pl liquibase-snowflake"
./mvnw test -pl liquibase-snowflake

echo ""
echo "🔗 STEP 3: Running Integration Tests"
echo "Command: ./mvnw test -pl liquibase-integration-tests -Dtest=\"*Snowflake*\" -Dliquibase.sdk.testSystem.test=snowflake"
./mvnw test -pl liquibase-integration-tests -Dtest="*Snowflake*" -Dliquibase.sdk.testSystem.test=snowflake

echo ""
echo "✅ ALL TESTS PASSED - READY FOR SNOWFLAKE TESTING"
echo "💰 Remember to set cost limits and define test scope!"
echo "📋 Use the operational checklist before proceeding!"
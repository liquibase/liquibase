# DevOps Engineer Role Context & Learning

## Role Definition
**Primary Focus**: Build systems, deployment automation, environment management, CI/CD pipelines

## Current Maturity Assessment
- **Strengths**: Build discipline, environment consistency, deployment verification
- **Weaknesses**: Cache management, dependency tracking, deployment validation
- **Experience Level**: Learning Liquibase build ecosystem, establishing deployment practices

## Key Learnings from Projects

### Sequence ORDER Implementation:
- **JAR Deployment**: Manual copy to test harness lib/ directory required
- **Cache Issues**: JAR updates not taking effect, suggesting classpath caching problems
- **Build Verification**: Need to verify JAR contents match source code changes
- **Dependency Management**: Test harness depends on extension JAR being current

## Build & Deployment Principles
1. **Verification First**: Always verify deployment before concluding failure
2. **Reproducible Builds**: Same source should produce same artifacts
3. **Environment Consistency**: Test environment should match production deployment
4. **Automation Over Manual**: Reduce manual deployment steps where possible

## Build Standards
- **Clean Builds**: Always use `clean` before `package` for reliable builds
- **Dependency Management**: Understand and document all JAR dependencies
- **Artifact Verification**: Check JAR contents after build to verify changes included
- **Version Control**: Track which JAR version is deployed in each environment

## Deployment Process
```bash
# Standard Build & Deploy
cd /path/to/liquibase
./mvnw clean package -DskipTests -pl liquibase-snowflake
cp liquibase-snowflake/target/liquibase-snowflake-0-SNAPSHOT.jar /path/to/test-harness/lib/liquibase-snowflake.jar

# Verification
jar -tf lib/liquibase-snowflake.jar | grep -i xsd  # Check XSD included
ls -la lib/liquibase-snowflake.jar  # Check timestamp
```

## Environment Management
- **Test Harness Setup**: Snowflake connection configured in liquibase.sdk.local.yaml
- **Database Environment**: Real Snowflake instance for integration testing
- **JAR Management**: Extension JARs must be in test harness classpath
- **Configuration**: Environment-specific configs for different test scenarios

## Common Issues & Solutions
- **JAR Caching**: Clear classpath cache or restart test process
- **Stale Artifacts**: Always verify JAR timestamp matches build time
- **Dependency Conflicts**: Check for multiple versions of same JAR
- **Environment Drift**: Regularly verify test environment matches expected state

## Monitoring & Verification
- **Build Success**: Verify compilation and packaging success
- **Deployment Success**: Verify JAR copied and accessible
- **Runtime Success**: Verify extension loaded and functioning
- **Test Environment**: Verify database connectivity and permissions

## CI/CD Considerations
- **Automated Testing**: Integration tests should run after every build
- **Artifact Management**: Proper versioning and storage of JAR artifacts
- **Environment Promotion**: Clear path from dev → test → prod
- **Rollback Strategy**: Quick rollback to previous working version

## Infrastructure as Code
- **Test Environment**: Document Snowflake setup and configuration
- **Build Environment**: Document Maven setup and dependencies
- **Deployment Scripts**: Automate JAR deployment and verification
- **Configuration Management**: Version control for all configuration files

## Retrospective Input Style
**Focus on**: Build reliability, deployment effectiveness, environment stability, automation opportunities

**Perspective**: "From a build/deployment standpoint, how well did our infrastructure support development velocity..."

## Next Learning Goals
- Better cache management and classpath understanding
- Automated deployment verification procedures
- Improved build artifact management and versioning
# DevOps Engineer Role Context

## Content Standards (v1.0 - Created 2025-01-26)
1. **Only include what we've actually done** - No theoretical knowledge
2. **Evidence required** - Every claim needs proof from our work
3. **Confidence from attempts** - Not self-assessment
4. **Update after retrospectives** - This is a living document
5. **Keep it concise** - If it's not actionable, remove it

*Standards Review: During retrospectives, ask "Are these content standards working?"*

---

## Role Definition
**Primary Responsibility**: Build systems, deployment, environment management, JAR verification

## Validated Learnings

### Sequence ORDER Implementation:
- **Learning**: JAR deployment to test harness requires manual copy
- **Evidence**: Tests failed until JAR was copied to lib/ directory
- **Application**: Always verify JAR deployment before debugging code

- **Learning**: JAR caching causes stale code to run
- **Evidence**: Same errors persisted after rebuild until cache cleared
- **Application**: Check JAR timestamp and contents after every build

- **Learning**: Build verification prevents wasted debugging time
- **Evidence**: Spent 2 hours debugging code when JAR wasn't updated
- **Application**: Created verification checklist for builds

## Proven Patterns

### Build Verification Pattern
- **What Works**: Check JAR timestamp and contents after build
- **Why It Works**: Catches deployment issues immediately
- **When to Use**: After every build before testing
- **Success Rate**: 3/3 times prevented confusion

### Clean Build Pattern
- **What Works**: Always use `./mvnw clean package -DskipTests`
- **Why It Works**: Ensures no stale compiled classes
- **When to Use**: Every build cycle
- **Success Rate**: 100% reliable builds

## Anti-Patterns (What Failed)

### Assuming Deployment Success
- **What We Tried**: Build and immediately test without verification
- **Why It Failed**: JAR wasn't actually updated in test environment
- **What to Do Instead**: Always verify JAR timestamp and location
- **Failure Rate**: 2 failures before learning

## Confidence Levels

### High Confidence Areas (85%+):
- Maven build commands: 95% - Used successfully many times
- JAR verification process: 90% - Proven pattern
- Environment configuration: 88% - Snowflake setup working

### Low Confidence Areas (<70%):
- Automated deployment: 40% - Still manual process
- Cache management: 60% - Don't fully understand caching
- CI/CD integration: 30% - Haven't implemented yet

## Retrospective Contribution
**Focus**: Build reliability, deployment verification, environment issues
**Perspective**: "From a DevOps standpoint, the JAR caching issue could be prevented by..."
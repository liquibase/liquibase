# Perfection Trap Rule

## Rule Metadata
- **Version**: 1.0
- **Type**: global
- **Severity**: medium
- **Enforcement**: manual (will be automatic)
- **Status**: active
- **Last Updated**: 2025-01-26
- **Validated Through**: To be validated (starting at 50% confidence)

## Purpose
Prevents endless polishing of working code by enforcing "good enough" principle. Ships at 85% perfect to maintain velocity while ensuring quality.

## Rule Statement
**WHEN** code works AND tests pass AND follows patterns
**THEN** SHIP IT after maximum 3 iterations
**EVEN IF** it could be 5-15% cleaner/better
**ELSE** you're wasting time on diminishing returns

## Scope
- **Applies To**: All implementation work
- **Exceptions**: Security-critical or user-facing APIs

## Detailed Specification

### The 85% Perfect Checklist
- ✅ Core functionality works? → Ship it
- ✅ Tests pass? → Ship it  
- ✅ Follows patterns? → Ship it
- ✅ No security issues? → Ship it
- ❌ Could be 5% cleaner? → Note for later
- ❌ Better variable names? → Note for later
- ❌ Minor optimization possible? → Note for later

### Iteration Limits
1. **First Pass**: Get it working (functionality)
2. **Second Pass**: Make it clean (readability)
3. **Third Pass**: Final polish (minor improvements)
4. **Fourth Pass**: STOP! Ship it now

### Perfection Trap Indicators
- Renaming variables for the 3rd time
- Restructuring working code "just because"
- Optimizing non-bottleneck code
- Adding features not requested
- Refactoring without clear benefit

### When to Note Instead of Fix
```markdown
// TODO: Could extract this to a method (low priority)
// NOTE: Consider caching if performance issue
// LATER: Might benefit from builder pattern
// NICE-TO-HAVE: Add convenience overload
```

## Examples

### ✅ Good: Ship at 85%
```java
// Works, tested, follows patterns - SHIP IT!
public void createDatabase(Database database, String dbName) {
    // Functional, clear, good enough
    String sql = "CREATE DATABASE " + database.escapeObjectName(dbName);
    if (database.supportsCatalogs()) {
        execute(sql);
    }
    // TODO: Could add more options later if needed
}
```

### ❌ Bad: Perfection Paralysis
```java
// Iteration 1: Works
// Iteration 2: Cleaner
// Iteration 3: "Better" names
// Iteration 4: Unnecessary abstraction
// Iteration 5: Over-engineered
// 2 hours later... still "improving"
```

### Real Example
```markdown
Task: SQL Generator for CreateDatabase
Iteration 1 (30 min): Basic SQL generation ✅
Iteration 2 (20 min): Added escaping ✅
Iteration 3 (15 min): WITH clause support ✅
Iteration 4 starting... STOPPED by rule
Result: Shipped working code, saved 30+ min
```

## The Cost of Perfection

### Time Mathematics
- 0-85% perfect: 1 hour
- 85-95% perfect: +1 hour (2x time)
- 95-99% perfect: +2 hours (3x time)
- 99-100% perfect: +4 hours (5x time)

### Velocity Impact
- Ship at 85%: 5 features/week
- Ship at 95%: 2-3 features/week
- Ship at 99%: 1 feature/week

## Enforcement

### Detection
- Track iteration count on same code
- Monitor time on "working" code
- Watch for repeated refactoring

### Response
- **Warning**: After 2nd iteration on working code
- **Alert**: After 3rd iteration
- **Block**: Prevent 4th iteration

### Override Only For
- Security vulnerabilities
- Data corruption risks
- Public API contracts
- Performance bottlenecks (proven)

## What To Do Instead

### Capture Improvements
```markdown
## Improvement Ideas - [Component]
- [ ] Extract common pattern
- [ ] Add builder for complex cases
- [ ] Cache expensive operations
Priority: Low (code works fine)
```

### Schedule Enhancement Time
- Weekly: 2-hour improvement block
- Focus: High-value refactoring only
- Rule: Must improve multiple components

## Anti-Patterns

### The "While I'm Here" Trap
```
Fixing bug (10 min) →
"While I'm here" cleanup (20 min) →
"Might as well" refactor (40 min) →
"Should also" optimize (60 min) →
Total: 2+ hours for 10-min bug
```

### The "Just One More" Loop
```
"Just one more" rename →
"Just one more" structure change →
"Just one more" abstraction →
Never ships
```

## Metrics
- **Initial Confidence**: 50% (needs validation)
- **Success Metric**: Features shipped on time
- **Value Metric**: Velocity maintained/increased

## Related Documents
- Rules: THREE_STRIKE_META_RULE (limits iterations)
- Processes: Code review (quality gates)
- Philosophy: Pragmatic > Perfect

## Learning History
| Date | Learning | Impact |
|------|----------|--------|
| 2025-01-26 | Rule created from LBCF | To be validated |

## Change Log
| Version | Date | Change | Reason |
|---------|------|--------|--------|
| 1.0 | 2025-01-26 | Initial version | Prevent perfection paralysis |
# LBCF Confidence Scoring Guide

## Why Precise Confidence Scores Matter

Confidence scores (0-100%) provide **quantifiable certainty** that enables:
- **Risk Assessment**: Know exactly where uncertainty lies
- **Resource Allocation**: Focus help where confidence is low
- **Progress Tracking**: Watch confidence grow over time
- **Decision Making**: Proceed confidently or seek help based on scores
- **Trust Building**: Honest assessment builds credibility

## Confidence Score Definitions

### Score Ranges

| Score | Level | Meaning | Action |
|-------|-------|---------|--------|
| 95-100% | **Certain** | Proven pattern, multiple successes | Execute without hesitation |
| 85-94% | **High** | Strong pattern, few variations | Proceed with standard caution |
| 70-84% | **Moderate** | General understanding, some gaps | Proceed but verify carefully |
| 50-69% | **Low** | Basic concept, many unknowns | Proceed with help nearby |
| 25-49% | **Very Low** | Mostly guessing | Stop and seek help |
| 0-24% | **None** | No knowledge | Don't attempt alone |

### Calculating Confidence Scores

```markdown
Base Score = (Successes / Total Attempts) × 100

Modifiers:
+ 10% for documented pattern
+ 10% for recent success (< 1 week)
+ 5% for similar context
- 10% for different context
- 20% for no documentation
- 15% for recent failure
```

## Confidence Scoring in Practice

### Pattern Confidence Scoring

```markdown
## Pattern: Mutable Statement Classes
**Confidence Score: 98%**
**Evidence**: 
- Attempts: 15
- Successes: 15
- Failures: 0
- Last used: Yesterday
- Documentation: Complete

**Breakdown**:
- Base: 100% (15/15)
- Documented pattern: +10%
- Recent success: +10%
- Maximum confidence: 98% (never 100% - always room for edge cases)
```

### Task Confidence Scoring

```markdown
## Task: Implement CreateWarehouseChange
**Overall Confidence: 87%**

**Component Breakdown**:
- Change class structure: 95% (proven pattern)
- Validation logic: 90% (clear examples)
- SQL generation: 75% (complex WITH clause)
- Testing approach: 92% (standard pattern)
- Integration: 80% (environment dependent)

**Risk Areas** (< 80%):
- SQL generation needs careful attention
- Integration might have surprises
```

### Implementation Planning with Confidence

```markdown
| Task | Estimated Time | Confidence | Risk Mitigation |
|------|----------------|------------|-----------------|
| CreateDomainChange | 45 min | 92% | Use proven pattern |
| Domain SQL Generator | 60 min | 78% | Review PG docs first |
| Integration Tests | 45 min | 85% | Standard approach |
| Rollback Support | 30 min | 45% | Need help - DROP DOMAIN complex |
```

## Confidence in Status Tracking

### Enhanced Status Table

| Stage | Component | Status | Time | Success | Confidence Before | Confidence After | Learning |
|-------|-----------|--------|------|---------|------------------|-----------------|----------|
| 3.1 | CreateTableChange | 🟢 Done | 40m | ✅ | 85% | 95% | Pattern solid |
| 3.2 | Complex Validation | 🟢 Done | 65m | 🔄 | 60% | 82% | Needed examples |
| 3.3 | Multi-statement SQL | 🟡 Active | 30m | - | 55% | - | Struggling |

## Building Confidence Systematically

### Confidence Growth Tracking

```markdown
## Week-over-Week Confidence Growth

### Week 1
- Statement Classes: 45% → 90% (+45%)
- SQL Generation: 40% → 75% (+35%)
- Testing Patterns: 60% → 85% (+25%)

### Week 2
- Statement Classes: 90% → 98% (+8%)
- SQL Generation: 75% → 88% (+13%)
- Testing Patterns: 85% → 94% (+9%)

**Insight**: Steep initial learning, then refinement
```

### Confidence by Category

```markdown
## Current Confidence Levels

### High Confidence (90%+)
- Basic change implementation: 95%
- Service registration: 98%
- Unit test patterns: 92%
- Maven setup: 96%

### Moderate Confidence (70-89%)
- Complex SQL generation: 78%
- Integration testing: 82%
- XSD schema design: 75%
- Error handling: 80%

### Low Confidence (< 70%)
- Rollback implementation: 55%
- Performance optimization: 45%
- Multi-database support: 40%
- Custom types: 35%
```

## Using Confidence Scores for Decision Making

### When to Proceed vs Ask for Help

```markdown
if (confidenceScore >= 85) {
    // Proceed with normal caution
    implement();
    test();
} else if (confidenceScore >= 70) {
    // Proceed but with extra validation
    implement();
    validateAssumptions();
    testThoroughly();
} else if (confidenceScore >= 50) {
    // Proceed only with safety net
    documentUncertainties();
    implement();
    askForReview();
} else {
    // Don't proceed alone
    askForHelp();
}
```

### Confidence-Based Time Estimates

```markdown
Adjusted Time = Base Time × (2 - Confidence/100)

Examples:
- 90% confidence: 45 min × 1.1 = 50 min
- 70% confidence: 45 min × 1.3 = 59 min
- 50% confidence: 45 min × 1.5 = 68 min
- 30% confidence: 45 min × 1.7 = 77 min
```

## Confidence Score Documentation

### In Code Comments

```java
/**
 * Creates a domain in PostgreSQL.
 * 
 * Implementation Confidence: 92%
 * - Pattern: 98% (well-established)
 * - Validation: 95% (clear rules)
 * - SQL Generation: 88% (some edge cases)
 * - Testing: 90% (standard approach)
 */
public class CreateDomainChange extends AbstractChange {
```

### In Status Updates

```markdown
## Status Update - 3:15pm
**Completed**: CreateDatabaseChange implementation
**Confidence Level**: 89% (would be 95%+ with second attempt)
**Time**: 55 min (estimated 45 min at 85% confidence)
**Confidence Breakdown**:
- Structure: 95% (followed pattern exactly)
- Validation: 92% (comprehensive)
- SQL Gen: 78% (WITH clause was tricky)
- Overall: 89% (weighted average)
**Next**: Tests at 94% confidence (15 min)
```

### In Help Requests

```markdown
## Help Needed - Rollback Implementation

**Task**: Implement rollback for CREATE DOMAIN
**Current Confidence**: 35%
**Why Low**:
- No clear pattern found (0% - no examples)
- CASCADE implications unclear (20%)
- Dependency checking complex (30%)
- Some PG knowledge (60%)

**Specific Help Needed**:
- Example of safe domain rollback
- How to check dependencies
- Best practice for CASCADE handling
```

## Confidence Calibration

### Weekly Confidence Review

```markdown
## Confidence Calibration Check

**Overconfident Areas** (predicted > actual):
- Multi-statement SQL: Predicted 75%, Actual 55%
- Learning: Need to factor in complexity

**Underconfident Areas** (predicted < actual):
- Basic patterns: Predicted 70%, Actual 95%
- Learning: Patterns more reliable than expected

**Well-Calibrated** (within 5%):
- Service registration: Predicted 95%, Actual 98%
- Unit testing: Predicted 90%, Actual 92%
```

## Remember

**Accurate confidence scores enable optimal decision-making**

- **High confidence** → Move fast with safety
- **Low confidence** → Seek help early
- **Growing confidence** → Learning is working
- **Honest scoring** → Builds trust and prevents failures

The goal isn't 100% confidence in everything - it's knowing exactly where you stand so you can make intelligent decisions about when to proceed and when to ask for help.
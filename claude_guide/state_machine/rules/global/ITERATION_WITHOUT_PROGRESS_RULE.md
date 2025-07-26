# Iteration Without Progress Rule

## Rule Metadata
- **Version**: 1.0
- **Type**: global
- **Severity**: high
- **Enforcement**: manual (will be automatic)
- **Status**: active
- **Last Updated**: 2025-01-26
- **Validated Through**: 30 years of development experience (starting at 50% confidence)

## Purpose
Detects when repeated iterations indicate a fundamental problem (wrong assumptions or missing information) rather than just needing more attempts. Forces systematic problem-solving instead of trial-and-error.

## Rule Statement
**WHEN** 3 iterations on the same code produce no meaningful progress
**THEN** you MUST:
1. STOP iterating
2. REVALIDATE all assumptions
3. RESEARCH the specific problem
4. BUILD an explicit plan based on findings
5. FOLLOW the plan exactly
**ELSE** you're just rearranging deck chairs on the Titanic

## Scope
- **Applies To**: All implementation, debugging, and optimization tasks
- **Exceptions**: None - this indicates a fundamental issue

## Detailed Specification

### Trigger Conditions
- [ ] First iteration: some progress
- [ ] Second iteration: minimal progress
- [ ] Third iteration: no meaningful progress
- [ ] OR: Same error/issue persists across iterations
- [ ] OR: "Polishing" without addressing core issue

### Required Actions

#### Step 1: Stop and Analyze
1. **Immediate**: Stop all code changes
2. **Document**: 
   - What you've tried (all 3 approaches)
   - What hasn't improved
   - Current assumptions

#### Step 2: Revalidate Assumptions
1. **List all assumptions** you're making
2. **Test each assumption** explicitly
3. **Document** which were wrong
4. **Update** your mental model

#### Step 3: Research the Problem
1. **Search** for similar issues/solutions
2. **Read** relevant documentation
3. **Understand** the root cause
4. **Document** findings

#### Step 4: Build Explicit Plan
1. **Based on** validated assumptions + research
2. **Write out** specific steps
3. **Include** success criteria
4. **Get review** if confidence <70%

#### Step 5: Execute Plan
1. **Follow exactly** as written
2. **Document results** at each step
3. **If fails**: Use results to revise plan
4. **If stuck**: Ask for help

### Examples

#### ✅ Correct Application
```
Situation: SQL escaping keeps failing after 3 tries
Iteration 1-3: Different escaping approaches, all fail

Action: STOPPED iterating
- Assumption check: "I need to escape at SQL level" → WRONG
- Research: Found Liquibase handles escaping in Database class
- Plan: Use database.escapeObjectName() instead
- Result: Worked immediately

Time saved: 2+ hours of continued iteration
```

#### ❌ Violation Example
```
Situation: Validation logic not working right
Iterations 1-10: Keep tweaking validation conditions

Action: Continued iterating without stopping
Result: 3 hours wasted, problem was wrong method signature
Should have: Checked assumptions after iteration 3
```

## Enforcement

### Detection
- **Automatic**: Git commits show repeated changes to same code
- **Manual**: Developer recognizes lack of progress

### Response
- **Warning**: After 2nd iteration without progress
- **Action**: Force assumption validation after 3rd
- **Escalation**: Require help if plan fails

## The Iteration Cycle

```
Initial Attempt
    ↓
Iteration 1: Some progress
    ↓
Iteration 2: Less progress  
    ↓
Iteration 3: No progress ← STOP HERE
    ↓
Validate Assumptions
    ↓
Research Problem
    ↓
Build Plan
    ↓
Execute Plan
    ↓
Success OR Revise with new info OR Get help
```

## Metrics
- **Initial Confidence**: 50% (based on experience, needs validation)
- **Expected Success Rate**: 80%+ when followed
- **Time Saved**: 1-3 hours per incident

## Related Documents
- Rules: THREE_STRIKE_RULE (for attempts, not iterations)
- Processes: RESEARCH process, HELP_SEEKING process

## Learning History
| Date | Learning | Impact |
|------|----------|--------|
| 2025-01-26 | Rule created from 30 years experience | To be validated |

## Change Log
| Version | Date | Change | Reason |
|---------|------|--------|--------|
| 1.0 | 2025-01-26 | Initial version | Better than perfection trap concept |
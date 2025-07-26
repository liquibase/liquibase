# LBCF Role Switching Protocol

## How to Use Role-Based Development

### The Protocol

When working on any task, explicitly cycle through relevant roles to ensure comprehensive quality:

```markdown
## Task: Implement CreateDatabaseChange

### 👨‍💻 Developer Mode (First Pass)
**Confidence**: 91%
**Focus**: Clean implementation following patterns
**Questions**:
- ✓ Is this following the established pattern?
- ✓ Are all attributes properly mapped?
- ✓ Is validation comprehensive?
**Result**: Basic implementation complete

### 🧪 QA Engineer Mode (Review)
**Confidence**: 85%
**Focus**: What could break?
**Questions**:
- ⚠️ What if databaseName is null?
- ⚠️ What if name contains special characters?
- ⚠️ What about SQL injection?
- ❌ Missing test for cascading behavior
**Result**: Found 3 potential issues

### 📊 Project Manager Mode (Status Check)
**Confidence**: 78%
**Focus**: Progress and risks
**Questions**:
- ✓ On track? Yes, 45 min vs 60 min estimate
- ⚠️ Any blockers? Uncertain about rollback
- ✓ Status updated? Yes, in tracker
**Result**: Identified rollback as risk

### 🔧 DevOps Mode (Deployment View)
**Confidence**: 70%
**Focus**: Will this integrate smoothly?
**Questions**:
- ✓ Service registration correct?
- ✓ Dependencies declared?
- ⚠️ Any version conflicts?
**Result**: Need to verify jar packaging

### 👨‍🏫 Subject Matter Expert Mode
**Confidence**: 65%
**Focus**: Does this match database behavior?
**Questions**:
- ❓ Is IF NOT EXISTS supported?
- ❓ What's the CASCADE behavior?
- ⚠️ Any version-specific features?
**Result**: Need to research database docs
```

## Role Switching Triggers

### Automatic Role Switches

Switch roles when:

1. **After implementation** → Switch to QA
2. **After testing** → Switch to PM for status
3. **Before committing** → Switch to DevOps
4. **When uncertain** → Switch to SME
5. **Every 30 minutes** → PM check-in

### Quick Role Checks

#### 🚀 Developer Quick Check
```markdown
□ Pattern followed?
□ Code clean?
□ Edge cases handled?
□ Performance considered?
```

#### 🧪 QA Quick Check
```markdown
□ Happy path tested?
□ Error cases tested?
□ Edge cases tested?
□ Integration tested?
```

#### 📊 PM Quick Check
```markdown
□ Status updated?
□ Time tracked?
□ Risks identified?
□ Next steps clear?
```

#### 🔧 DevOps Quick Check
```markdown
□ Builds successfully?
□ Deploys cleanly?
□ Dependencies managed?
□ Monitoring considered?
```

## Role-Based Review Checklist

### Before Marking "Complete"

Run through each role's checklist:

```markdown
## Completion Checklist - CreateDatabaseChange

### Developer Review ✅
- [x] Follows established patterns (98% confidence)
- [x] All requirements implemented
- [x] Code is clean and documented
- [x] No known technical debt

### QA Review ✅
- [x] Unit tests comprehensive (90% coverage)
- [x] Integration tests pass
- [x] Edge cases identified and tested
- [x] Performance acceptable

### PM Review ✅
- [x] Delivered on time (45 min < 60 min estimate)
- [x] All risks documented
- [x] Status tracking updated
- [x] Stakeholder communication complete

### DevOps Review ⚠️
- [x] Builds in isolation
- [x] Service registration verified
- [ ] CI/CD pipeline updated (TODO)
- [x] No deployment blockers

### SME Review ✅
- [x] Matches database documentation
- [x] User-friendly API
- [x] Follows domain conventions
- [x] No surprising behavior

**Overall Confidence**: 87%
**Ready for Production**: Yes, with CI/CD TODO
```

## Improving Through Role Analysis

### Weekly Role Effectiveness Review

```markdown
## Role Performance - Week Ending [Date]

### Developer Role
- Tasks completed: 8
- First-try success: 6/8 (75%)
- Patterns missed: 2
- **Action**: Review SQL generation patterns

### QA Role
- Bugs caught before production: 12
- Bugs missed: 2
- Test coverage: 87%
- **Action**: Add edge case checklist

### PM Role
- Estimates accuracy: 78%
- Status updates: 100%
- Risks identified early: 60%
- **Action**: Improve risk assessment

### DevOps Role
- Build failures: 3
- Deploy issues: 1
- Automation added: 2
- **Action**: Study CI/CD patterns

### SME Role
- Requirements clarified: 5
- Misunderstandings: 2
- Research tasks: 8
- **Action**: Build domain knowledge base
```

## Role Switching in Communication

### How to Communicate from Each Role

#### As Developer
"I've implemented the change following the mutable statement pattern. The code handles all 15 attributes with validation. Confidence: 91%"

#### As QA
"I'm concerned about the edge case where both OR REPLACE and IF NOT EXISTS are specified. We need a test for this conflict."

#### As PM
"We're at 75% completion, 2 hours into a 3-hour estimate. One blocker identified: rollback complexity. Overall on track."

#### As DevOps
"The build passes but I notice we're not testing against multiple Liquibase versions. This could cause integration issues."

#### As SME
"Per PostgreSQL docs, CASCADE is required when dependents exist. Our implementation should warn users about this."

## Benefits of Role Switching

1. **Catches more issues early** - Each role sees different problems
2. **Improves quality** - Multiple perspectives ensure completeness
3. **Builds expertise** - Practice improves each role
4. **Reduces blind spots** - What developer misses, QA catches
5. **Increases confidence** - Systematic review builds trust

## Remember

**Excellent developers think like QA engineers**
**Excellent QA engineers think like users**
**Excellent PMs think like stakeholders**
**Excellent DevOps think like operations**
**Excellent SMEs think like customers**

By explicitly switching roles, we embody all these perspectives and deliver exceptional results.
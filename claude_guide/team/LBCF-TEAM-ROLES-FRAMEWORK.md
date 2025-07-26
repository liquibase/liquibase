# LBCF Team Roles Framework

## Why This Matters

You're right - this IS like onboarding a new development team! By explicitly defining roles and tracking effectiveness in each role, we can:
- **Identify weaknesses** before they impact delivery
- **Build expertise** systematically in each role
- **Ensure quality** through multiple perspectives
- **Improve continuously** by strengthening weak areas

## The Virtual Team

### Current Team Composition

| Role | Responsibility | Current Effectiveness | Confidence | Growth Path |
|------|----------------|---------------------|------------|-------------|
| **Developer** | Write code, implement features | 88% | 91% | Strong patterns, some SQL complexity |
| **QA Engineer** | Test thoroughly, find edge cases | 82% | 87% | Good unit tests, integration needs work |
| **Project Manager** | Plan, track, communicate | 75% | 78% | Getting better at visibility |
| **DevOps Engineer** | Environment, build, deploy | 68% | 70% | Limited real environment experience |
| **Scrum Master** | Process improvement, efficiency | 70% | 75% | Learning from each sprint |
| **Subject Matter Expert** | Domain knowledge, requirements | 65% | 65% | Varies by database |
| **Tech Lead** | Architecture, patterns, standards | 85% | 88% | Good pattern recognition |
| **Documentation Specialist** | Clear docs, examples | 80% | 85% | Improving with templates |

### Role Definitions and Expectations

## 1. Developer Role
**Effectiveness: 88%**

### Responsibilities
- Write clean, working code
- Follow established patterns
- Implement features completely
- Handle edge cases

### Current Strengths
- Pattern recognition (95%)
- Code structure (92%)
- Basic implementation (90%)

### Areas for Growth
- Complex SQL generation (78%)
- Performance optimization (70%)
- Database-specific quirks (75%)

### Effectiveness Tracking
```markdown
## Developer Effectiveness Log
Week 1: 75% - Learning patterns
Week 2: 82% - Applying patterns
Week 3: 86% - Confident in basics
Week 4: 88% - Handling complexity better
```

## 2. QA Engineer Role
**Effectiveness: 82%**

### Responsibilities
- Write comprehensive tests
- Find edge cases
- Verify all scenarios
- Ensure quality

### Current Strengths
- Unit test patterns (90%)
- Basic test coverage (88%)
- Test structure (85%)

### Areas for Growth
- Integration test complexity (75%)
- Performance testing (65%)
- Edge case identification (78%)

### Quality Metrics
```markdown
| Test Type | Coverage | Effectiveness | Confidence |
|-----------|----------|---------------|------------|
| Unit Tests | 92% | 90% | High |
| Integration | 78% | 75% | Medium |
| Edge Cases | 70% | 72% | Growing |
| Performance | 45% | 50% | Low |
```

## 3. Project Manager Role
**Effectiveness: 75%**

### Responsibilities
- Create clear plans
- Track progress accurately
- Communicate status
- Manage risks

### Current Strengths
- Planning templates (85%)
- Status tracking (80%)
- Communication (78%)

### Areas for Growth
- Risk anticipation (68%)
- Timeline accuracy (70%)
- Stakeholder management (72%)

### PM Effectiveness Indicators
- Plans created before starting: ✅ 100%
- Status updates frequency: ✅ 95%
- Timeline accuracy: ⚠️ 70%
- Risk identification: ⚠️ 65%

## 4. DevOps Engineer Role
**Effectiveness: 68%**

### Responsibilities
- Environment setup
- Build automation
- Deployment process
- Monitoring

### Current Strengths
- Maven configuration (85%)
- Basic builds (80%)
- Package structure (82%)

### Areas for Growth
- CI/CD pipelines (55%)
- Environment troubleshooting (60%)
- Performance monitoring (45%)

### DevOps Maturity
```markdown
Level 1 ✅: Manual builds work
Level 2 ✅: Automated builds work
Level 3 ⚠️: Basic CI/CD (partial)
Level 4 ❌: Full automation
Level 5 ❌: Self-healing systems
```

## 5. Scrum Master / Process Role
**Effectiveness: 70%**

### Responsibilities
- Improve process efficiency
- Remove blockers
- Facilitate improvement
- Track metrics

### Current Strengths
- Retrospectives (80%)
- Process documentation (82%)
- Metric tracking (75%)

### Areas for Growth
- Blocker anticipation (65%)
- Process optimization (68%)
- Team velocity improvement (70%)

## Team Effectiveness Dashboard

### Overall Team Score: 76%

```markdown
## Team Health Metrics

### Strong Areas (>80%)
- Code Quality: 88%
- Documentation: 85%
- Pattern Usage: 92%

### Adequate Areas (70-80%)
- Testing: 78%
- Planning: 75%
- Process: 72%

### Weak Areas (<70%)
- DevOps: 68%
- Domain Expertise: 65%
- Performance: 60%
```

## Role Switching and Context

### When I Switch Roles

During development, I explicitly switch between roles:

```markdown
## Current Role: Developer
**Focus**: Writing clean implementation
**Confidence**: 91%
**Asking**: "Is this the right pattern?"

[Switching to QA Role]

## Current Role: QA Engineer
**Focus**: Will this break? Edge cases?
**Confidence**: 85%
**Asking**: "What could go wrong?"

[Switching to PM Role]

## Current Role: Project Manager
**Focus**: Are we on track? Risks?
**Confidence**: 78%
**Asking**: "What's the status?"
```

## Improving Role Effectiveness

### For Each Role, Track:

1. **Successes** - What worked well?
2. **Failures** - What went wrong?
3. **Learning** - What improved?
4. **Confidence** - Current level?
5. **Next Steps** - How to improve?

### Role-Specific Improvement Plans

#### Developer Improvement Plan
- Study complex SQL patterns: +10% effectiveness
- Practice performance optimization: +8%
- Learn database quirks: +7%
- **Target**: 95% effectiveness in 4 weeks

#### QA Improvement Plan
- Expand integration test scenarios: +8%
- Add performance benchmarks: +10%
- Study edge case patterns: +7%
- **Target**: 90% effectiveness in 3 weeks

## Team Retrospectives

### Weekly Team Performance Review

```markdown
## Sprint Retrospective - Week Ending [Date]

### What Went Well
- Developer: Clean code, patterns worked (91%)
- QA: Caught 3 edge cases (85%)
- PM: Status tracking accurate (80%)

### What Needs Improvement
- DevOps: Environment setup took 2x time (65%)
- SME: Missed database-specific requirement (60%)

### Action Items
1. DevOps to study CI/CD patterns
2. SME to research database specifics
3. QA to expand test scenarios

### Team Effectiveness Trend
Week 1: 68% → Week 2: 72% → Week 3: 74% → Week 4: 76%
```

## When to Consult Each Role

### Ask the Developer When:
- Choosing implementation patterns
- Dealing with code structure
- Optimizing algorithms

### Ask the QA Engineer When:
- Identifying test scenarios
- Finding edge cases
- Ensuring coverage

### Ask the PM When:
- Planning work
- Tracking progress
- Communicating status

### Ask the DevOps Engineer When:
- Setting up environments
- Automating builds
- Troubleshooting deployments

### Ask the SME When:
- Understanding requirements
- Database-specific features
- User needs

## Success Metrics

### High-Performing Team Indicators

1. **Role Effectiveness**: All roles >85%
2. **Communication**: Clear handoffs between roles
3. **Quality**: <5% rework rate
4. **Velocity**: Predictable, improving
5. **Learning**: Each sprint better than last

### Current Team Maturity

```markdown
Stage 1: Forming ✅ (Roles defined)
Stage 2: Storming ✅ (Finding weaknesses)
Stage 3: Norming ← We are here (Establishing patterns)
Stage 4: Performing ⏳ (High effectiveness)
Stage 5: Optimizing ⏳ (Continuous excellence)
```

## The Power of Multiple Perspectives

By explicitly switching roles, we get:

1. **Developer View**: "Will this code work?"
2. **QA View**: "Will this code break?"
3. **PM View**: "Will this deliver on time?"
4. **DevOps View**: "Will this deploy smoothly?"
5. **SME View**: "Will users want this?"

Each perspective catches different issues BEFORE they become problems.

## Remember

**A chain is only as strong as its weakest link**

By tracking effectiveness in each role and systematically improving weak areas, we build a stronger, more capable team over time. This isn't complicated - it's smart team development!
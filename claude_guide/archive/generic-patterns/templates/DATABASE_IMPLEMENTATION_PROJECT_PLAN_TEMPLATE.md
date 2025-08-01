# <DATABASE> Implementation Project Plan Template

## Project Overview
Systematic implementation of all <Database>-specific change types for the Liquibase <Database> Extension.

**Total Scope**: [Count] actual change types across [N] object types (not counting attribute variations)

## Implementation Status Dashboard

### 📊 Overall Progress
- **Completed**: 0/[Total] (0%)
- **In Progress**: 0/[Total] (0%)
- **Remaining**: [Total]/[Total] (100%)

### 🗂️ By Object Type

#### [OBJECT] Object ([N] change types) - 0% Complete
| Change Type | Requirements | Implementation | Unit Tests | Test Harness | Status |
|------------|--------------|----------------|------------|--------------|---------|
| create<Object> | ❌ TODO | ❌ TODO | ❌ TODO | ❌ TODO | 🔴 PENDING |
| alter<Object> | ❌ TODO | ❌ TODO | ❌ TODO | ❌ TODO | 🔴 PENDING |
| drop<Object> | ❌ TODO | ❌ TODO | ❌ TODO | ❌ TODO | 🔴 PENDING |

**Note**: [Any special attributes or considerations]

## Implementation Priority Order

### Phase 1: [First Priority Object]
Priority: [High/Medium/Low] ([Reason])
1. create<Object>
2. drop<Object>
3. alter<Object>

### Phase 2: [Second Priority Object]
Priority: [High/Medium/Low] ([Reason])
1. create<Object>
2. drop<Object>
3. alter<Object>

## Work Tracking Template

For each change type, track:

```markdown
### <ChangeType> Implementation

**Started**: YYYY-MM-DD HH:MM
**Completed**: YYYY-MM-DD HH:MM
**Developer**: Claude/Human

#### Checklist:
- [ ] Requirements document created/verified
- [ ] Change class implemented
- [ ] Statement class implemented
- [ ] SQL Generator implemented
- [ ] Service registration complete
- [ ] XSD schema updated
- [ ] Unit tests passing
- [ ] Test harness test created
- [ ] Test harness test passing
- [ ] Documentation updated

#### Notes:
- Any special considerations
- Issues encountered
- Decisions made
```

## Implementation Guidelines

### Key Principles
1. **SQL Command Mapping**: Only create change types for actual SQL commands
2. **Attributes over Change Types**: OR REPLACE, IF NOT EXISTS are attributes
3. **Follow Database Terminology**: Use <Database>'s exact terms
4. **Test Everything**: Unit tests at each step, test harness for integration

### Required Guides
1. **Requirements**: Use DETAILED_REQUIREMENTS_CREATION_GUIDE.md
2. **New Change Types**: Use NEW_CHANGETYPE_PATTERN_2.md
3. **Enhance Existing**: Use EXISTING_CHANGETYPE_EXTENSION_PATTERN.md
4. **Test Harness**: Use TEST_HARNESS_IMPLEMENTATION_GUIDE_3.md

### Success Metrics
- All unit tests passing
- Test harness tests passing
- No manual workarounds
- Clean, maintainable code
- Comprehensive documentation

## Next Steps

1. [First concrete step]
2. [Second concrete step]
3. [Third concrete step]

## Notes

- [Database-specific considerations]
- [Common pitfalls to avoid]
- [Testing requirements]

## Current Work Log

### [Date] - [Time]
**Working on**: [Specific task]
**Progress**: [What was completed]
**Next**: [What to do next]
**Blockers**: [Any issues]

---

<!-- Add new work log entries above this line -->
# State Machine Accountability System

## Purpose
Ensure deterministic, auditable behavior through enforced rules and comprehensive logging.

## Core Principle
**No action without permission. No permission without verification. No violation without learning.**

## System Components

### 1. Action Audit Trail
Every action taken must be logged with:
- Timestamp
- Current state
- Action type (read, write, delete, execute, etc.)
- Target (file path, command, etc.)
- Permission status (granted, denied, not_requested)
- Result (success, failure, violation)
- Rule applied

### 2. Permission System

#### State-Based Permissions
Each state explicitly defines:
```yaml
allowed_actions:
  - Actions that can be taken without asking
  - Example: read_file, analyze_content
  
requires_consent:
  - Actions that need explicit user approval
  - Example: modify_file, delete_file
  
forbidden_actions:
  - Actions that are never allowed in this state
  - Example: delete_file in review states
```

#### Consent Protocol
Before any action requiring consent:
1. **ASK**: "May I [specific action] on [specific target]?"
2. **WAIT**: For explicit approval ("yes", "proceed", etc.)
3. **VERIFY**: Confirm understanding of approval
4. **ACT**: Only after verification
5. **LOG**: Record consent and action

### 3. Rule Enforcement Engine

#### Pre-Action Checks
```python
def can_perform_action(action, target, current_state):
    # 1. Is action allowed in current state?
    if action in current_state.forbidden_actions:
        return False, "Action forbidden in this state"
    
    # 2. Does action require consent?
    if action in current_state.requires_consent:
        if not has_valid_consent(action, target):
            return False, "No consent for this action"
    
    # 3. Check global rules
    if violates_global_rules(action, target):
        return False, "Violates global rules"
    
    return True, "Action permitted"
```

### 4. Violation Tracking

#### Violation Log Format
```yaml
violations:
  - timestamp: ISO-8601
    state: current_state_name
    action_attempted: what_was_tried
    rule_violated: specific_rule
    severity: critical|high|medium|low
    learning: what_to_change
    prevention: how_to_prevent
```

#### Violation Severity
- **CRITICAL**: Acting without consent (deleting files)
- **HIGH**: Exceeding state permissions
- **MEDIUM**: Not following process order
- **LOW**: Minor process variations

### 5. Verification System

#### Audit Commands
User can verify compliance at any time:
- `show_action_log` - Display recent actions
- `show_violations` - List rule violations
- `verify_state` - Confirm current state is valid
- `check_permissions` - Show what's allowed now

#### Compliance Metrics
- Actions with consent: X%
- Violations per session: Y
- Rules followed: Z%
- Trust score: (tracked over time)

## Implementation in State Machine

### Enhanced State Definition
```yaml
states:
  document_review:
    description: "Review documents for compliance"
    allowed_actions:
      - read: ".*"
      - analyze: ".*"
      - recommend: "changes"
    requires_consent:
      - write: ".*"
      - delete: ".*"
      - move: ".*"
      - execute: "code"
    forbidden_actions:
      - commit: "changes"
      - push: "to_remote"
    audit_level: "high"
```

### Global Rules Enhancement
```yaml
global_rules:
  explicit_consent:
    description: "All file modifications require explicit consent"
    applies_to: all_states
    enforcement: pre_action_check
    
  audit_all_actions:
    description: "Every action must be logged"
    applies_to: all_states
    enforcement: automatic
    
  three_strikes:
    description: "Three violations trigger help state"
    applies_to: all_states
    enforcement: post_action_check
```

## Trust Building Through Accountability

### Trust Metrics
1. **Consent Compliance**: % of actions with proper consent
2. **Rule Adherence**: % of actions following state rules
3. **Violation Frequency**: Violations per 100 actions
4. **Recovery Time**: How quickly violations are corrected

### Trust Levels
- **Full Trust** (>95% compliance): Normal operations
- **Probation** (85-95%): Enhanced audit logging
- **Restricted** (70-85%): Require consent for more actions
- **Supervised** (<70%): All actions need approval

## Learning from Violations

Each violation triggers:
1. **Immediate Stop**: Halt current action
2. **Log Entry**: Record what happened
3. **Analysis**: Why did violation occur?
4. **Rule Update Proposal**: Should rules change?
5. **Process Improvement**: Update state machine

## Verification Examples

### User Verification Commands
```
User: Show me your last 5 actions
Assistant: [Displays action log with permissions]

User: Why did you delete those files?
Assistant: [Shows violation log entry]

User: What can you do in this state?
Assistant: [Shows current permissions]
```

This accountability system ensures that every action is:
- **Predictable** (follows state rules)
- **Auditable** (logged with detail)
- **Verifiable** (you can check anytime)
- **Learning-enabled** (violations improve system)

The goal: Build trust through transparent, deterministic behavior.
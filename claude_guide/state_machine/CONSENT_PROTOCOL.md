# Consent Protocol

## The Rule
**No file changes without explicit consent. Period.**

## How It Works

### 1. Before ANY File Change
I must:
```
ACTION REQUEST: [delete/modify/move/create]
TARGET: [specific file(s)]
REASON: [why this change is needed]
IMPACT: [what will change]

May I proceed with this action?
```

### 2. Valid Consent
You must explicitly say one of:
- "yes"
- "proceed" 
- "approved"
- "go ahead"
- "yes, [specific action] those files"

### 3. Invalid Consent
These do NOT count as consent:
- Silence
- "I think so"
- "maybe"
- "sure" (too ambiguous)
- Any unclear response

### 4. Verification
After consent, I will verify:
```
I understand you're approving me to:
[specific action] on [specific files]
Is this correct?
```

## Example Interactions

### ✅ Correct:
```
Assistant: ACTION REQUEST: delete
TARGET: /team/LBCF-*.md (8 files)  
REASON: These are theoretical documents without validation
IMPACT: Remove 2,108 lines of theoretical content

May I proceed with this action?

User: yes, delete those LBCF files
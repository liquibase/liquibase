#!/usr/bin/env python3
"""
State Machine Executor - Runs our development process state machine
This is a prototype showing how we could make the state machine truly executable
"""

import json
import datetime
from pathlib import Path
from dataclasses import dataclass, asdict
from typing import Dict, List, Optional, Tuple

@dataclass
class StateTransition:
    from_state: str
    to_state: str
    timestamp: str
    duration_min: int
    confidence_before: int
    confidence_after: int
    outcome: str

@dataclass
class CurrentState:
    state: str
    entry_time: str
    role: str
    confidence: int
    attempts: int
    context: Dict

class DevelopmentStateMachine:
    def __init__(self, state_file='PROJECT_STATE.json', machine_file='development_machine.json'):
        self.state_file = Path(state_file)
        self.machine_file = Path(machine_file)
        self.load_state()
        self.load_machine_definition()
        
    def load_state(self):
        """Load current state from file or initialize"""
        if self.state_file.exists():
            with open(self.state_file) as f:
                data = json.load(f)
                self.current_state = CurrentState(**data['current_state'])
                self.history = [StateTransition(**t) for t in data['history']]
        else:
            # Initialize at start state
            self.current_state = CurrentState(
                state='requirements_product_owner',
                entry_time=datetime.datetime.now().isoformat(),
                role='product_owner',
                confidence=70,
                attempts=0,
                context={}
            )
            self.history = []
    
    def load_machine_definition(self):
        """Load the state machine definition"""
        # For now, hardcode key rules
        self.states = {
            'requirements_product_owner': {
                'role': 'product_owner',
                'process': '/roles/product_owner/GOAL_DEFINE_REQUIREMENTS.md',
                'transitions': [
                    {'to': 'implementation_developer', 'condition': 'requirements_clear', 'min_confidence': 70},
                    {'to': 'requirements_product_owner', 'condition': 'needs_clarification'}
                ]
            },
            'implementation_developer': {
                'role': 'developer',
                'process': '/roles/developer/GOAL_PROVE_CODE_WORKS.md',
                'transitions': [
                    {'to': 'test_qa', 'condition': 'code_complete', 'min_confidence': 85},
                    {'to': 'help_architect', 'condition': 'three_strikes'},
                    {'to': 'implementation_developer', 'condition': 'low_confidence', 'max_confidence': 70}
                ]
            },
            # ... more states
        }
        
        self.global_rules = {
            'three_strikes': {'max_attempts': 3, 'help_state': 'help_architect'},
            'time_box': {'multiplier': 3, 'action': 'warn_then_help'},
            'confidence_threshold': {'min_progress': 70, 'min_complete': 85}
        }
    
    def get_current_process(self) -> str:
        """Get the process document for current state"""
        state_info = self.states.get(self.current_state.state, {})
        return state_info.get('process', 'No process defined')
    
    def get_valid_transitions(self) -> List[Dict]:
        """Get valid transitions from current state"""
        state_info = self.states.get(self.current_state.state, {})
        transitions = state_info.get('transitions', [])
        
        valid = []
        for t in transitions:
            # Check confidence requirements
            if 'min_confidence' in t and self.current_state.confidence < t['min_confidence']:
                continue
            if 'max_confidence' in t and self.current_state.confidence > t['max_confidence']:
                continue
            valid.append(t)
        
        return valid
    
    def check_global_rules(self) -> Optional[str]:
        """Check if any global rules trigger a transition"""
        # Three strikes rule
        if self.current_state.attempts >= 3:
            return 'help_architect'
        
        # Low confidence rule
        if self.current_state.confidence < 70 and self.current_state.state != 'help_architect':
            return 'help_architect'
        
        return None
    
    def transition_to(self, new_state: str, outcome: str, confidence_change: int = 0):
        """Execute a state transition"""
        # Calculate duration
        duration = (datetime.datetime.now() - 
                   datetime.datetime.fromisoformat(self.current_state.entry_time))
        duration_min = int(duration.total_seconds() / 60)
        
        # Record transition
        transition = StateTransition(
            from_state=self.current_state.state,
            to_state=new_state,
            timestamp=datetime.datetime.now().isoformat(),
            duration_min=duration_min,
            confidence_before=self.current_state.confidence,
            confidence_after=self.current_state.confidence + confidence_change,
            outcome=outcome
        )
        self.history.append(transition)
        
        # Update current state
        self.current_state = CurrentState(
            state=new_state,
            entry_time=datetime.datetime.now().isoformat(),
            role=self.states[new_state]['role'],
            confidence=self.current_state.confidence + confidence_change,
            attempts=0,  # Reset attempts on transition
            context={'previous': self.current_state.state, 'outcome': outcome}
        )
        
        # Save state
        self.save_state()
    
    def save_state(self):
        """Persist current state to file"""
        data = {
            'current_state': asdict(self.current_state),
            'history': [asdict(t) for t in self.history],
            'last_updated': datetime.datetime.now().isoformat()
        }
        with open(self.state_file, 'w') as f:
            json.dump(data, f, indent=2)
    
    def predict_completion(self) -> Tuple[List[str], int]:
        """Predict path to completion and time estimate"""
        # Simple prediction based on average times
        avg_times = {
            'implementation_developer': 75,
            'test_qa': 45,
            'document_technical_writer': 30,
            'retrospective_scrum_master': 30
        }
        
        # Simplified happy path from current state
        path_map = {
            'requirements_product_owner': ['implementation_developer', 'test_qa', 'document_technical_writer', 'retrospective_scrum_master'],
            'implementation_developer': ['test_qa', 'document_technical_writer', 'retrospective_scrum_master'],
            'test_qa': ['document_technical_writer', 'retrospective_scrum_master'],
            'document_technical_writer': ['retrospective_scrum_master'],
            'retrospective_scrum_master': []
        }
        
        remaining_path = path_map.get(self.current_state.state, [])
        total_time = sum(avg_times.get(state, 30) for state in remaining_path)
        
        return remaining_path, total_time
    
    def generate_status_update(self) -> str:
        """Generate automatic status update"""
        path, time_remaining = self.predict_completion()
        
        # Calculate progress
        total_states = 5  # requirements -> implementation -> test -> document -> retrospective
        completed = len([h for h in self.history if 'help' not in h.to_state])
        progress = (completed / total_states) * 100
        
        update = f"""## Status Update - {datetime.datetime.now().strftime('%Y-%m-%d %H:%M')}

**Current State**: {self.current_state.state.replace('_', ' ').title()}
**Role**: {self.current_state.role}
**Progress**: {progress:.0f}% complete
**Confidence**: {self.current_state.confidence}%

**Path to Completion**: {' → '.join(path)}
**Estimated Time Remaining**: {time_remaining} minutes

**Recent Activity**:
"""
        
        # Add last 3 transitions
        for t in self.history[-3:]:
            update += f"- {t.from_state} → {t.to_state} ({t.duration_min} min, {t.outcome})\n"
        
        return update
    
    def learn_from_history(self) -> Dict:
        """Analyze history to find patterns and improvements"""
        if not self.history:
            return {}
        
        insights = {
            'avg_state_duration': {},
            'success_rate': {},
            'confidence_growth': {},
            'common_help_triggers': []
        }
        
        # Calculate average durations
        state_durations = {}
        for t in self.history:
            if t.from_state not in state_durations:
                state_durations[t.from_state] = []
            state_durations[t.from_state].append(t.duration_min)
        
        for state, durations in state_durations.items():
            insights['avg_state_duration'][state] = sum(durations) / len(durations)
        
        # Track confidence changes
        for t in self.history:
            conf_change = t.confidence_after - t.confidence_before
            if conf_change > 0:
                insights['confidence_growth'][t.to_state] = insights.get('confidence_growth', {}).get(t.to_state, [])
                insights['confidence_growth'][t.to_state] = conf_change
        
        return insights

# Example usage
if __name__ == '__main__':
    machine = DevelopmentStateMachine()
    
    print(f"Current State: {machine.current_state.state}")
    print(f"Current Process: {machine.get_current_process()}")
    print(f"Valid Transitions: {machine.get_valid_transitions()}")
    print("\n" + machine.generate_status_update())
    
    # Example transition
    # machine.transition_to('test_qa', 'code complete', confidence_change=10)
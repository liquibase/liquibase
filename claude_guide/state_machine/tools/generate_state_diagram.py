#!/usr/bin/env python3
"""
Generate Mermaid state diagram from development_machine.yaml
"""

import yaml
from pathlib import Path

def load_state_machine(yaml_file):
    """Load the state machine definition from YAML"""
    with open(yaml_file, 'r') as f:
        return yaml.safe_load(f)

def generate_mermaid(machine):
    """Generate Mermaid diagram from state machine definition"""
    lines = ["```mermaid", "stateDiagram-v2"]
    
    # Add states with descriptions
    for state_id, state_info in machine['states'].items():
        state_name = state_id.replace('_', ' ').title()
        lines.append(f"    {state_id} : {state_name}")
        lines.append(f"    {state_id} : {state_info['description']}")
    
    # Add start state
    lines.append("    [*] --> requirements_product_owner : New Feature Request")
    
    # Add transitions
    for state_id, state_info in machine['states'].items():
        for transition in state_info.get('transitions', []):
            to_state = transition['to']
            condition = transition['when'].replace('_', ' ')
            
            # Add confidence requirement if specified
            if 'confidence_required' in transition:
                condition += f" (>{transition['confidence_required']}%)"
            
            # Special handling for 'complete' state
            if to_state == 'complete':
                lines.append(f"    {state_id} --> [*] : {condition}")
            else:
                lines.append(f"    {state_id} --> {to_state} : {condition}")
    
    # Add color coding for different role types
    lines.extend([
        "",
        "    classDef product fill:#f9f,stroke:#333,stroke-width:2px",
        "    classDef developer fill:#bbf,stroke:#333,stroke-width:2px", 
        "    classDef qa fill:#bfb,stroke:#333,stroke-width:2px",
        "    classDef writer fill:#fbf,stroke:#333,stroke-width:2px",
        "    classDef scrum fill:#ffb,stroke:#333,stroke-width:2px",
        "    classDef help fill:#fbb,stroke:#333,stroke-width:2px",
        "",
        "    class requirements_product_owner product",
        "    class implementation_developer developer",
        "    class test_qa qa",
        "    class document_technical_writer writer",
        "    class retrospective_scrum_master,structural_evolution_scrum_master scrum",
        "    class help_architect,help_devops help"
    ])
    
    lines.append("```")
    
    return '\n'.join(lines)

def generate_process_flow(machine):
    """Generate a simplified process flow diagram"""
    lines = ["", "## Simplified Process Flow", "", "```mermaid", "graph LR"]
    
    lines.extend([
        "    A[Requirements] --> B[Implementation]",
        "    B --> C[Testing]",
        "    C --> D[Documentation]", 
        "    D --> E[Retrospective]",
        "    E --> F[Complete]",
        "",
        "    B -.->|Help Needed| G[Architect]",
        "    C -.->|Environment Issues| H[DevOps]",
        "    E -.->|Friction| I[Evolution]",
        "",
        "    G --> B",
        "    H --> C",
        "    I --> E",
        "",
        "    style A fill:#f9f",
        "    style B fill:#bbf",
        "    style C fill:#bfb",
        "    style D fill:#fbf",
        "    style E fill:#ffb",
        "    style G,H,I fill:#fbb"
    ])
    
    lines.append("```")
    return '\n'.join(lines)

def main():
    # Load the state machine
    yaml_file = Path(__file__).parent / 'development_machine.yaml'
    machine = load_state_machine(yaml_file)
    
    # Generate the documentation
    output = [
        "# Development State Machine Visualization",
        "",
        "Generated from `development_machine.yaml`",
        "",
        "## Full State Diagram",
        ""
    ]
    
    # Add the full state diagram
    output.append(generate_mermaid(machine))
    
    # Add simplified flow
    output.append(generate_process_flow(machine))
    
    # Add state descriptions
    output.extend([
        "",
        "## State Descriptions",
        ""
    ])
    
    for state_id, state_info in machine['states'].items():
        state_name = state_id.replace('_', ' ').title()
        output.extend([
            f"### {state_name}",
            f"- **Description**: {state_info['description']}",
            f"- **Process Doc**: `{state_info['process_doc']}`",
            f"- **Entry Criteria**: {', '.join(state_info.get('entry_criteria', []))}",
            f"- **Exit Criteria**: {', '.join(state_info.get('exit_criteria', []))}",
            ""
        ])
    
    # Write output
    output_file = Path(__file__).parent / 'DEVELOPMENT_STATE_MACHINE.md'
    with open(output_file, 'w') as f:
        f.write('\n'.join(output))
    
    print(f"Generated {output_file}")

if __name__ == '__main__':
    main()
@Grab('org.codehaus.groovy:groovy-all:3.0.9')

import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import java.nio.file.Files
import java.nio.file.Paths

/**
 * Self-contained Groovy TDD Workflow - No external Groovy installation required
 * Uses @Grab to pull in dependencies automatically
 */

// TDD State Management
class TDDState {
    String objectType
    String scenario
    String currentPhase
    int microCycleCount = 0
    List<String> completedCheckpoints = []
    Map<String, Object> metadata = [:]
    
    private static final String STATE_FILE = '.process_state/tdd_state.json'
    
    void save() {
        new File('.process_state').mkdirs()
        def json = new JsonBuilder()
        json {
            objectType this.objectType
            scenario this.scenario
            currentPhase this.currentPhase
            microCycleCount this.microCycleCount
            completedCheckpoints this.completedCheckpoints
            metadata this.metadata
            lastUpdated new Date().toString()
        }
        new File(STATE_FILE).text = json.toPrettyString()
    }
    
    static TDDState load() {
        def stateFile = new File(STATE_FILE)
        if (!stateFile.exists()) return new TDDState()
        
        def data = new JsonSlurper().parse(stateFile)
        def state = new TDDState()
        state.objectType = data.objectType
        state.scenario = data.scenario
        state.currentPhase = data.currentPhase
        state.microCycleCount = data.microCycleCount ?: 0
        state.completedCheckpoints = data.completedCheckpoints ?: []
        state.metadata = data.metadata ?: [:]
        return state
    }
    
    void initialize(String objectType, String scenario) {
        this.objectType = objectType
        this.scenario = scenario
        this.currentPhase = 'requirements_research'
        this.microCycleCount = 0
        this.completedCheckpoints.clear()
        this.metadata.clear()
        save()
    }
    
    String getStatusSummary() {
        return """
=== CURRENT WORKFLOW STATE ===
Object Type: ${objectType ?: 'Not set'}
Scenario: ${scenario ?: 'Not set'}
Current Phase: ${currentPhase ?: 'Not set'}
Micro-Cycle Count: ${microCycleCount}

=== COMPLETED CHECKPOINTS ===
${completedCheckpoints.isEmpty() ? '  (none)' : completedCheckpoints.collect { "  ✅ $it" }.join('\n')}
""".trim()
    }
}

// Validation Service
class ValidationService {
    static boolean validateTemplateCompilation() {
        println "🔧 Validating template compilation..."
        def process = "mvn compile -q".execute()
        process.waitFor()
        
        if (process.exitValue() == 0) {
            println "✅ Template compilation PASSED"
            return true
        } else {
            println "❌ Template compilation FAILED"
            return false
        }
    }
    
    static boolean validateFrameworkIntegration(String objectType) {
        println "🔧 Validating framework integration for: ${objectType}"
        
        def objectFile = new File("src/main/java/liquibase/database/object/${objectType}.java")
        if (!objectFile.exists()) {
            println "❌ Object file not found: ${objectFile.path}"
            return false
        }
        
        def content = objectFile.text
        def checks = [
            'extends AbstractDatabaseObject',
            'getContainingObjects()',
            'getName()',
            'setName(',
            'equals(Object obj)',
            'hashCode()'
        ]
        
        def failedChecks = checks.findAll { !(content.contains(it)) }
        
        if (failedChecks.isEmpty()) {
            println "✅ Framework integration compliance PASSED"
            return true
        } else {
            println "❌ Framework integration compliance FAILED"
            println "Missing: ${failedChecks.join(', ')}"
            return false
        }
    }
}

// Main Workflow
class TDDWorkflow {
    private TDDState state = TDDState.load()
    
    boolean init(String objectType, String scenario = 'NEW_OBJECT') {
        println "🚀 INITIALIZING TDD WORKFLOW"
        println "Object Type: ${objectType}"
        println "Scenario: ${scenario}"
        
        state.initialize(objectType, scenario)
        println "✅ Workflow initialized successfully"
        
        // Validate existing object model
        def objectFile = new File("src/main/java/liquibase/database/object/${objectType}.java")
        if (objectFile.exists()) {
            println "Found existing object model, validating..."
            if (ValidationService.validateTemplateCompilation() && 
                ValidationService.validateFrameworkIntegration(objectType)) {
                println "✅ Existing object model validation PASSED"
                println "Ready for TDD development phases"
                return true
            } else {
                println "❌ Existing object model has issues"
                return false
            }
        } else {
            println "❌ Object model not found: ${objectFile.path}"
            println "Please ensure object model exists before running TDD workflow"
            return false
        }
    }
    
    void status() {
        println state.statusSummary
        
        if (state.currentPhase == 'requirements_research') {
            println "\n=== CURRENT PHASE REQUIREMENTS ==="
            println "  1. WebFetch CREATE ${state.objectType} documentation"
            println "  2. WebFetch SHOW ${state.objectType}S documentation"
            println "  3. WebFetch DESCRIBE ${state.objectType} documentation"
            println "  4. Create requirements document with 15+ properties"
            println "  5. Validate requirements completeness"
            println "→ Creates checkpoint: requirements_complete"
        }
    }
    
    boolean next() {
        println "🔄 EXECUTING NEXT PHASE"
        println "Current: ${state.currentPhase}"
        println "Object: ${state.objectType}"
        
        if (state.currentPhase == 'requirements_research') {
            println "\n📋 REQUIREMENTS RESEARCH PHASE"
            println "Object: ${state.objectType}"
            println "\nREQUIRED ACTIONS:"
            println "1. Use WebFetch to research CREATE ${state.objectType} documentation"
            println "2. Use WebFetch to research SHOW ${state.objectType}S documentation"
            println "3. Use WebFetch to research DESCRIBE ${state.objectType} documentation"
            println "4. Update requirements document with 15+ properties"
            println "5. Run: .scripts/tdd complete"
        }
        
        status()
        return true
    }
    
    boolean complete() {
        println "🏁 COMPLETING CURRENT PHASE"
        println "Attempting to complete phase: ${state.currentPhase}"
        
        if (state.currentPhase == 'requirements_research') {
            // For now, just advance - can add validation later
            state.currentPhase = 'tdd_object_model'
            state.completedCheckpoints << 'requirements_complete'
            state.save()
            println "✅ Phase completed successfully"
            println "Advanced to: ${state.currentPhase}"
            return true
        }
        
        return true
    }
    
    boolean reset() {
        println "⚠️  RESETTING WORKFLOW"
        state = new TDDState()
        state.save()
        
        // Clean up directories
        ['process_state', '.checkpoints'].each { dir ->
            def file = new File(dir)
            if (file.exists()) file.deleteDir()
        }
        
        println "✅ Workflow reset complete"
        return true
    }
}

// Main execution
def args = this.args
if (!args) {
    println """
TDD Workflow Orchestration System (Groovy Implementation)

COMMANDS:
  init <ObjectType> [scenario]  - Initialize workflow
  status                        - Show current state
  next                          - Execute next phase  
  complete                      - Complete current phase
  reset                         - Reset workflow

EXAMPLES:
  groovy tdd_workflow_java.groovy init FileFormat NEW_OBJECT
  groovy tdd_workflow_java.groovy status
"""
    return
}

def workflow = new TDDWorkflow()
def command = args[0]

try {
    switch (command) {
        case 'init':
            if (args.length < 2) {
                println "❌ Usage: init <ObjectType> [scenario]"
                System.exit(1)
            }
            def success = workflow.init(args[1], args.length > 2 ? args[2] : 'NEW_OBJECT')
            if (!success) System.exit(1)
            break
            
        case 'status':
            workflow.status()
            break
            
        case 'next':
            if (!workflow.next()) System.exit(1)
            break
            
        case 'complete':
            if (!workflow.complete()) System.exit(1)
            break
            
        case 'reset':
            if (!workflow.reset()) System.exit(1)
            break
            
        default:
            println "❌ Unknown command: ${command}"
            System.exit(1)
    }
} catch (Exception e) {
    println "❌ Error: ${e.message}"
    e.printStackTrace()
    System.exit(1)
}
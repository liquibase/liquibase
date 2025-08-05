#!/usr/bin/env groovy

/**
 * Self-contained Groovy TDD Workflow - All classes in one file
 */

import groovy.json.JsonBuilder
import groovy.json.JsonSlurper

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
        
        // Validate state integrity on load
        if (!state.validateStateIntegrity()) {
            println "⚠️  State integrity validation failed - resetting to clean state"
            return new TDDState()
        }
        
        return state
    }
    
    boolean validateStateIntegrity() {
        // Check if we have orphaned checkpoints without proper validation
        if (completedCheckpoints.contains('requirements_research_complete')) {
            if (!validateRequirementsArtifacts()) {
                println "❌ Found requirements_research_complete but missing artifacts"
                return false
            }
        }
        
        if (completedCheckpoints.contains('tdd_object_model_complete')) {
            if (!validateObjectModelArtifacts()) {
                println "❌ Found tdd_object_model_complete but missing object model"
                return false
            }
        }
        
        return true
    }
    
    private boolean validateRequirementsArtifacts() {
        // Check for requirements document or evidence of WebFetch research
        def reqsFile = new File("claude_guide/snowflake_requirements/snapshot_diff_requirements/${objectType}_requirements.md")
        def altReqsFile = new File("${objectType}_requirements.md")
        
        return reqsFile.exists() || altReqsFile.exists() || 
               (metadata.containsKey('webfetch_calls') && metadata.webfetch_calls > 0)
    }
    
    private boolean validateObjectModelArtifacts() {
        if (!objectType) return false
        def objectFile = new File("src/main/java/liquibase/database/object/${objectType}.java")
        return objectFile.exists()
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
    
    void completePhase() {
        if (currentPhase) {
            completedCheckpoints << "${currentPhase}_complete"
            advancePhase()
            save()
        }
    }
    
    private void advancePhase() {
        def phases = ['requirements_research', 'tdd_object_model', 'snapshot_generator', 'diff_comparator', 'integration']
        def currentIndex = phases.indexOf(currentPhase)
        if (currentIndex >= 0 && currentIndex < phases.size() - 1) {
            currentPhase = phases[currentIndex + 1]
        }
    }
    
    void reset() {
        this.objectType = null
        this.scenario = null
        this.currentPhase = null
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
            // Get error details
            def errorProcess = "mvn compile".execute()
            errorProcess.waitFor()
            def errors = errorProcess.err.text
            if (errors) {
                println "Compilation errors:"
                errors.split('\n').findAll { it.contains('[ERROR]') }.take(5).each { println it }
            }
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
            [pattern: 'extends AbstractDatabaseObject', desc: 'extends AbstractDatabaseObject'],
            [pattern: 'getContainingObjects()', desc: 'implements getContainingObjects()'],
            [pattern: 'getName()', desc: 'implements getName()'],
            [pattern: 'setName(', desc: 'implements setName()'],
            [pattern: 'equals(Object obj)', desc: 'implements equals()'],
            [pattern: 'hashCode()', desc: 'implements hashCode()']
        ]
        
        def failed = checks.findAll { !content.contains(it.pattern) }
        
        if (failed.isEmpty()) {
            println "✅ Framework integration compliance PASSED"
            return true
        } else {
            println "❌ Framework integration compliance FAILED"
            println "Missing patterns:"
            failed.each { println "  - ${it.desc}" }
            return false
        }
    }
    
    static boolean validatePropertyPatterns(String objectType) {
        println "🔧 Validating property patterns for: ${objectType}"
        
        def objectFile = new File("src/main/java/liquibase/database/object/${objectType}.java")
        if (!objectFile.exists()) {
            println "❌ Object file not found: ${objectFile.path}"
            return false
        }
        
        def content = objectFile.text
        def getterCount = (content =~ /public \w+ get\w+\(\)/).size()
        def setterCount = (content =~ /public \w+ set\w+\(.*?\)/).size()
        
        println "  - Found ${getterCount} getters, ${setterCount} setters"
        
        if (getterCount >= 2 && setterCount >= 1) {
            println "✅ Property patterns validation PASSED"
            return true
        } else {
            println "❌ Property patterns validation FAILED"
            return false
        }
    }
    
    static boolean validateCompleteTemplateGeneration(String objectType) {
        println "🔧 ENHANCED TEMPLATE GENERATION VALIDATION"
        println "Object Type: ${objectType}"
        
        def allPassed = true
        
        if (!validateTemplateCompilation()) allPassed = false
        if (!validateFrameworkIntegration(objectType)) allPassed = false
        if (!validatePropertyPatterns(objectType)) allPassed = false
        
        if (allPassed) {
            println "✅ Template generation validation PASSED"
        } else {
            println "❌ Template generation validation FAILED"
        }
        
        return allPassed
    }
    
    static boolean validateRequirementsComplete(String objectType, TDDState state) {
        println "🔧 VALIDATING REQUIREMENTS RESEARCH COMPLETION"
        println "Object Type: ${objectType}"
        
        def validations = []
        
        // Check for WebFetch calls
        def webfetchCount = state.metadata.webfetch_calls ?: 0
        if (webfetchCount >= 3) {
            validations << "✅ WebFetch research (${webfetchCount} calls)"
        } else {
            validations << "❌ WebFetch research (${webfetchCount}/3 minimum calls)"
        }
        
        // Check for requirements document
        def reqsFile = new File("claude_guide/snowflake_requirements/snapshot_diff_requirements/${objectType}_requirements.md")
        def altReqsFile = new File("${objectType}_requirements.md")
        if (reqsFile.exists() || altReqsFile.exists()) {
            validations << "✅ Requirements document exists"
        } else {
            validations << "❌ Requirements document missing"
        }
        
        // Check for property count metadata
        def propertyCount = state.metadata.documented_properties ?: 0
        if (propertyCount >= 15) {
            validations << "✅ Property documentation (${propertyCount} properties)"
        } else {
            validations << "❌ Property documentation (${propertyCount}/15 minimum properties)"
        }
        
        validations.each { println "  ${it}" }
        
        def passed = validations.every { it.startsWith('✅') }
        
        if (passed) {
            println "✅ Requirements research validation PASSED"
        } else {
            println "❌ Requirements research validation FAILED"
            println ""
            println "TO COMPLETE REQUIREMENTS PHASE:"
            if (webfetchCount < 3) {
                println "1. Use WebFetch for CREATE ${objectType}, SHOW ${objectType}S, DESCRIBE ${objectType}"
                println "2. Record each WebFetch: groovy .scripts/tdd.groovy webfetch <url> <description>"  
            }
            if (!reqsFile.exists() && !altReqsFile.exists()) {
                println "3. Create requirements document with findings"
            }
            if (propertyCount < 15) {
                println "4. Document 15+ properties: groovy .scripts/tdd.groovy properties <count>"
            }
        }
        
        return passed
    }
}

// Main Workflow
class TDDWorkflow {
    private TDDState state = TDDState.load()
    
    boolean init(String objectType, String scenario = 'NEW_OBJECT') {
        println "🚀 INITIALIZING TDD WORKFLOW"
        println "Object Type: ${objectType}"
        println "Scenario: ${scenario}"
        println ""
        
        state.initialize(objectType, scenario)
        println "✅ Workflow initialized successfully"
        
        // Validate existing object model
        def objectFile = new File("src/main/java/liquibase/database/object/${objectType}.java")
        if (objectFile.exists()) {
            println "Found existing object model, validating..."
            if (ValidationService.validateCompleteTemplateGeneration(objectType)) {
                println "✅ Object model validation PASSED"
                println "🎉 Ready for TDD development phases"
                return true
            } else {
                println "❌ Object model validation FAILED"
                return false
            }
        } else {
            println "❌ Object model not found: ${objectFile.path}"
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
        } else if (state.currentPhase == 'tdd_object_model') {
            println "\n=== CURRENT PHASE REQUIREMENTS ==="
            println "  1. Execute TDD micro-cycles for object model"
            println "  2. Identity cycle (equals/hashCode)"
            println "  3. Property cycles (one per property from requirements)"
            println "  4. Framework integration (getName/setName/getSchema)"
            println "→ Creates checkpoint: object_model_complete"
        }
    }
    
    boolean next() {
        println "🔄 EXECUTING NEXT PHASE"
        println "Current: ${state.currentPhase}"
        println "Object: ${state.objectType}"
        println ""
        
        if (state.currentPhase == 'requirements_research') {
            println "📋 REQUIREMENTS RESEARCH PHASE"
            println "Object: ${state.objectType}"
            println "\nREQUIRED ACTIONS:"
            println "1. Use WebFetch to research CREATE ${state.objectType} documentation"
            println "2. Use WebFetch to research SHOW ${state.objectType}S documentation"
            println "3. Use WebFetch to research DESCRIBE ${state.objectType} documentation"
            println "4. Update requirements document with 15+ properties"
            println "5. Run: groovy .scripts/tdd.groovy complete"
        } else if (state.currentPhase == 'tdd_object_model') {
            println "🧪 TDD OBJECT MODEL PHASE"
            println "Object: ${state.objectType}"
            println "\nREQUIRED ACTIONS:"
            println "1. Execute TDD micro-cycles for object model properties"
            println "2. Test identity methods (equals/hashCode)"
            println "3. Test framework integration methods"
            println "4. Run: groovy .scripts/tdd.groovy complete"
        }
        
        println ""
        status()
        return true
    }
    
    boolean complete() {
        println "🏁 COMPLETING CURRENT PHASE"
        println "Attempting to complete phase: ${state.currentPhase}"
        
        if (state.currentPhase == 'requirements_research') {
            if (ValidationService.validateRequirementsComplete(state.objectType, state)) {
                println "✅ Requirements phase validation passed"
                state.completePhase()
                println "Advanced to: ${state.currentPhase}"
                return true
            } else {
                println "❌ Requirements validation failed - cannot advance"
                return false
            }
        } else if (state.currentPhase == 'tdd_object_model') {
            if (ValidationService.validateFrameworkIntegration(state.objectType)) {
                println "✅ Object model phase validation passed"
                state.completePhase()
                println "Advanced to: ${state.currentPhase}"
                return true
            } else {
                println "❌ Object model validation failed"
                return false
            }
        }
        
        return true
    }
    
    boolean recordWebFetch(String url, String description) {
        println "📚 RECORDING WEBFETCH RESEARCH"
        println "URL: ${url}"
        println "Description: ${description}"
        
        state.metadata.webfetch_calls = (state.metadata.webfetch_calls ?: 0) + 1
        if (!state.metadata.webfetch_history) state.metadata.webfetch_history = []
        state.metadata.webfetch_history << [
            url: url,
            description: description,
            timestamp: new Date().toString()
        ]
        state.save()
        
        println "✅ WebFetch recorded (${state.metadata.webfetch_calls} total calls)"
        return true
    }
    
    boolean cycle(String cycleName, String testClass, String testMethod) {
        println "🔄 EXECUTING TDD MICRO-CYCLE"
        println "Cycle: ${cycleName}"
        println "Test: ${testClass}#${testMethod}"
        
        if (state.currentPhase != 'tdd_object_model') {
            println "❌ TDD cycles only available in 'tdd_object_model' phase"
            return false
        }
        
        state.microCycleCount++
        state.save()
        
        // Run the test
        def testResult = runTest("${testClass}#${testMethod}")
        if (testResult) {
            println "✅ Micro-cycle ${state.microCycleCount} completed successfully"
        } else {
            println "⚠️  Continue TDD cycle (RED-GREEN-REFACTOR)"
        }
        
        return true
    }
    
    boolean reset() {
        println "⚠️  RESETTING WORKFLOW"
        state.reset()
        
        ['process_state', '.checkpoints'].each { dir ->
            def file = new File(dir)
            if (file.exists()) {
                if (file.isDirectory()) file.deleteDir()
                else file.delete()
            }
        }
        
        println "✅ Workflow reset complete"
        return true
    }
    
    private boolean runTest(String testSpec) {
        try {
            def process = "mvn test -Dtest=${testSpec} -q".execute()
            process.waitFor()
            return process.exitValue() == 0
        } catch (Exception e) {
            return false
        }
    }
}

// Main execution
if (args.length == 0) {
    println """
TDD Workflow Orchestration System (Groovy Implementation)

COMMANDS:
  init <ObjectType> [scenario]  - Initialize workflow
  status                        - Show current state
  next                          - Execute next phase
  cycle <name> <class> <method> - Execute TDD micro-cycle
  complete                      - Complete current phase
  reset                         - Reset workflow
  webfetch <url> <description>  - Record WebFetch research call
  properties <count>            - Record documented properties count

EXAMPLES:
  groovy .scripts/tdd.groovy init FileFormat NEW_OBJECT
  groovy .scripts/tdd.groovy status
  groovy .scripts/tdd.groovy webfetch "docs.snowflake.com/create-file-format" "CREATE FILE FORMAT research"
  groovy .scripts/tdd.groovy properties 18
  groovy .scripts/tdd.groovy cycle identity FileFormatTest testEqualsContract
"""
    System.exit(0)
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
            if (!workflow.init(args[1], args.length > 2 ? args[2] : 'NEW_OBJECT')) {
                System.exit(1)
            }
            break
            
        case 'status':
            workflow.status()
            break
            
        case 'next':
            if (!workflow.next()) System.exit(1)
            break
            
        case 'cycle':
            if (args.length < 4) {
                println "❌ Usage: cycle <name> <class> <method>"
                System.exit(1)
            }
            if (!workflow.cycle(args[1], args[2], args[3])) System.exit(1)
            break
            
        case 'complete':
            if (!workflow.complete()) System.exit(1)
            break
            
        case 'reset':
            workflow.reset()
            break
            
        case 'webfetch':
            if (args.length < 3) {
                println "❌ Usage: webfetch <url> <description>"
                System.exit(1)
            }
            if (!workflow.recordWebFetch(args[1], args[2])) System.exit(1)
            break
            
        case 'properties':
            if (args.length < 2) {
                println "❌ Usage: properties <count>"
                System.exit(1)
            }
            workflow.state.metadata.documented_properties = Integer.parseInt(args[1])
            workflow.state.save()
            println "✅ Recorded ${args[1]} documented properties"
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
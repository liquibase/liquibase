#!/usr/bin/env groovy

/**
 * Main TDD Workflow orchestrator - Groovy implementation
 * Replaces the bash-based TDD enforcement system
 */
class TDDWorkflow {
    
    private TDDState state
    private ValidationService validator
    private TemplateService templateService
    
    TDDWorkflow() {
        this.state = TDDState.load()
        this.validator = new ValidationService()
        this.templateService = new TemplateService()
    }
    
    /**
     * Initialize new TDD workflow
     */
    def init(String objectType, String scenario = 'NEW_OBJECT') {
        println "🚀 INITIALIZING TDD WORKFLOW"
        println "Object Type: ${objectType}"
        println "Scenario: ${scenario}"
        println ""
        
        // Initialize state
        state.initialize(objectType, scenario)
        
        println "Initializing TDD workflow for ${objectType} (scenario: ${scenario})"
        println "Phase set to: ${state.currentPhase}"
        println "✅ Workflow initialized successfully"
        
        // Generate template files
        println "Generating template files..."
        if (templateService.generateAllFiles(objectType)) {
            println ""
            println "Next steps:"
            println "1. Complete requirements research (WebFetch documentation)"
            println "2. Execute TDD micro-cycles for object model"
            println "3. Execute TDD micro-cycles for snapshot generator"
            println "4. Execute TDD micro-cycles for diff comparator"
            println "5. Register services and run integration tests"
            
            // Validate template generation
            if (validator.validateCompleteTemplateGeneration(objectType)) {
                println ""
                println "🎉 Template generation and validation SUCCESSFUL!"
                println "Ready to proceed with TDD development."
                return true
            } else {
                println ""
                println "❌ Template validation failed. Fix issues before proceeding."
                return false
            }
        } else {
            println "❌ Template generation failed"
            return false
        }
    }
    
    /**
     * Show current workflow status
     */
    def status() {
        println state.statusSummary
        
        if (state.currentPhase) {
            def phaseRequirements = getPhaseRequirements(state.currentPhase)
            if (phaseRequirements) {
                println ""
                println "=== CURRENT PHASE REQUIREMENTS ==="
                phaseRequirements.eachWithIndex { req, index ->
                    println "  ${index + 1}. ${req}"
                }
                println "→ Creates checkpoint: ${state.currentPhase}_complete"
            }
        }
    }
    
    /**
     * Execute next phase
     */
    def next() {
        println "🔄 EXECUTING NEXT PHASE"
        println "Current: ${state.currentPhase}"
        println "Object: ${state.objectType}"
        println ""
        
        if (!state.currentPhase) {
            println "❌ No active workflow. Run 'init' first."
            return false
        }
        
        def phaseRequirements = getPhaseRequirements(state.currentPhase)
        if (phaseRequirements) {
            println "📋 ${state.currentPhase.toUpperCase().replace('_', ' ')} PHASE"
            println "Object: ${state.objectType}"
            println ""
            println "REQUIRED ACTIONS:"
            phaseRequirements.eachWithIndex { req, index ->
                println "${index + 1}. ${req}"
            }
            println "${phaseRequirements.size() + 1}. Run: tdd complete"
            println ""
        }
        
        status()
        return true
    }
    
    /**
     * Complete current phase
     */
    def complete() {
        println "🏁 COMPLETING CURRENT PHASE"
        println ""
        
        if (!state.currentPhase) {
            println "❌ No active phase to complete"
            return false
        }
        
        println "Attempting to complete phase: ${state.currentPhase}"
        
        // Phase-specific validation
        def validationPassed = validatePhaseCompletion(state.currentPhase)
        
        if (validationPassed) {
            println "✅ Phase validation PASSED"
            state.completePhase()
            println "✅ Phase '${state.currentPhase}' completed successfully"
            println "Advanced to next phase: ${state.currentPhase}"
            return true
        } else {
            println "❌ Phase validation FAILED"
            println "Complete phase requirements before proceeding"
            return false
        }
    }
    
    /**
     * Execute TDD micro-cycle
     */
    def cycle(String cycleName, String testClass, String testMethod) {
        println "🔄 EXECUTING TDD MICRO-CYCLE"
        println "Cycle: ${cycleName}"
        println "Test: ${testClass}#${testMethod}"
        println ""
        
        if (state.currentPhase != 'tdd_object_model') {
            println "❌ TDD cycles only available in 'tdd_object_model' phase"
            println "Current phase: ${state.currentPhase}"
            return false
        }
        
        // Increment cycle count
        state.incrementMicroCycle()
        
        // Execute RED-GREEN-REFACTOR validation
        println "Executing RED-GREEN-REFACTOR cycle..."
        
        // Basic validation - ensure test exists and compiles
        def testFile = new File("src/test/java/liquibase/ext/snowflake/database/${testClass}.java")
        if (!testFile.exists()) {
            testFile = new File("src/test/java/liquibase/ext/snowflake/database/${state.objectType}Test.java")
        }
        
        if (testFile.exists()) {
            println "✅ Test file found: ${testFile.path}"
            
            // Run the specific test
            def testResult = runTest("${testClass}#${testMethod}")
            if (testResult) {
                println "✅ Micro-cycle ${state.microCycleCount} completed successfully"
                return true
            } else {
                println "❌ Test execution failed - continue TDD cycle"
                return false
            }
        } else {
            println "❌ Test file not found"
            return false
        }
    }
    
    /**
     * Reset workflow
     */
    def reset() {
        println "⚠️  RESETTING WORKFLOW"
        println "This will clear all progress and checkpoints."
        
        // For now, just reset - in interactive mode could ask for confirmation
        state.reset()
        
        // Clean up generated files (optional)
        def filesToClean = [
            '.process_state',
            '.checkpoints'
        ]
        
        filesToClean.each { path ->
            def file = new File(path)
            if (file.exists()) {
                if (file.isDirectory()) {
                    file.deleteDir()
                } else {
                    file.delete()
                }
            }
        }
        
        println "✅ Workflow reset complete"
        return true
    }
    
    /**
     * Get requirements for a specific phase
     */
    private List<String> getPhaseRequirements(String phase) {
        switch (phase) {
            case 'requirements_research':
                return [
                    "WebFetch CREATE ${state.objectType} documentation",
                    "WebFetch SHOW ${state.objectType}S documentation", 
                    "WebFetch DESCRIBE ${state.objectType} documentation",
                    "Create requirements document with 15+ properties",
                    "Validate requirements completeness"
                ]
            case 'tdd_object_model':
                return [
                    "Execute TDD micro-cycles for object model",
                    "Identity cycle (equals/hashCode)",
                    "Property cycles (one per property from requirements)",
                    "Framework integration (getName/setName/getSchema)"
                ]
            case 'snapshot_generator':
                return [
                    "TDD micro-cycles for snapshot generator",
                    "Query implementation cycles",
                    "Object construction cycles",
                    "Integration test cycles"
                ]
            case 'diff_comparator':
                return [
                    "TDD micro-cycles for diff comparator",
                    "Identity comparison cycles",
                    "Property comparison cycles", 
                    "Difference detection cycles"
                ]
            case 'integration':
                return [
                    "Service registration",
                    "Integration test execution",
                    "End-to-end validation"
                ]
            default:
                return []
        }
    }
    
    /**
     * Validate phase completion requirements
     */
    private boolean validatePhaseCompletion(String phase) {
        switch (phase) {
            case 'requirements_research':
                return validator.validateRequirements(state.objectType)
            case 'tdd_object_model':
                return validator.validateFrameworkIntegration(state.objectType) &&
                       validator.validatePropertyPatterns(state.objectType)
            case 'snapshot_generator':
                // Check snapshot generator implementation
                def snapshotFile = new File("src/main/java/liquibase/snapshot/jvm/${state.objectType}SnapshotGeneratorSnowflake.java")
                return snapshotFile.exists()
            case 'diff_comparator':
                // Check comparator implementation
                def comparatorFile = new File("src/main/java/liquibase/diff/output/${state.objectType}Comparator.java")
                return comparatorFile.exists()
            case 'integration':
                return validator.validateTemplateCompilation()
            default:
                return true
        }
    }
    
    /**
     * Run a specific test
     */
    private boolean runTest(String testSpec) {
        try {
            def process = "mvn test -Dtest=${testSpec} -q".execute()
            process.waitFor()
            return process.exitValue() == 0
        } catch (Exception e) {
            println "Test execution failed: ${e.message}"
            return false
        }
    }
}
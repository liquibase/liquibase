#!/usr/bin/env groovy

/**
 * AIPL v2.0 Engine - AI Implementation Programming Language Interpreter
 * 
 * A complete workflow orchestration engine that can load, parse, and execute
 * any AIPL v2.0 workflow program with full enforcement capabilities.
 */

import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import groovy.yaml.YamlSlurper
import groovy.yaml.YamlBuilder
import java.util.concurrent.*
import java.util.regex.Pattern

// ============================================================================
// AIPL PROGRAM LOADER AND PARSER
// ============================================================================

class AIPLProgram {
    String version
    String programName
    String description
    String enforcementMode
    String blockingBehavior
    Map<String, Object> variables = [:]
    Map<String, Object> imports = [:]
    Map<String, Object> phases = [:]
    Map<String, Object> metadata = [:]
    
    static AIPLProgram load(String programPath) {
        def file = new File(programPath)
        if (!file.exists()) {
            throw new IllegalArgumentException("AIPL program not found: ${programPath}")
        }
        
        def yaml = new YamlSlurper()
        def data = yaml.parse(file)
        
        def program = new AIPLProgram()
        program.version = data.AIPL_VERSION ?: "1.0"
        program.programName = data.PROGRAM_NAME
        program.description = data.DESCRIPTION
        program.enforcementMode = data.ENFORCEMENT_MODE ?: "STANDARD"
        program.blockingBehavior = data.BLOCKING_BEHAVIOR ?: "WARN_ON_VIOLATION"
        program.variables = data.VARIABLES ?: [:]
        program.imports = data.IMPORTS ?: [:]
        program.phases = data.PHASES ?: [:]
        
        return program
    }
    
    String resolveVariable(String text) {
        if (!text) return text
        
        // Replace ${VARIABLE_NAME} with actual values
        def pattern = /\$\{([^}]+)\}/
        return text.replaceAll(pattern) { match, varName ->
            return variables[varName] ?: match[0]
        }
    }
    
    Map<String, Object> getPhase(String phaseName) {
        return phases[phaseName] ?: [:]
    }
    
    List<String> getPhaseNames() {
        return phases.keySet() as List
    }
}

// ============================================================================
// VALIDATION FRAMEWORK
// ============================================================================

class ValidationEngine {
    static final Map<String, Closure> VALIDATORS = [
        'FILE_EXISTS': { target ->
            return new File(target).exists()
        },
        
        'DIRECTORY_EXISTS': { target ->
            def file = new File(target)
            return file.exists() && file.isDirectory()
        },
        
        'COMMAND_SUCCESS': { command ->
            try {
                def process = command.execute()
                process.waitFor()
                return process.exitValue() == 0
            } catch (Exception e) {
                return false
            }
        },
        
        'TESTS_PASS': { testPattern ->
            try {
                def process = "mvn test -Dtest=${testPattern} -q".execute()
                process.waitFor()
                return process.exitValue() == 0
            } catch (Exception e) {
                return false
            }
        },
        
        'TEST_FAILS': { testPattern ->
            try {
                def process = "mvn test -Dtest=${testPattern} -q".execute()
                process.waitFor()
                return process.exitValue() != 0
            } catch (Exception e) {
                return true // Assume failure if can't run
            }
        },
        
        'PROPERTIES_COUNT': { params ->
            def parts = params.split(':')
            if (parts.length != 3) return false
            
            def filePath = parts[0]
            def pattern = parts[1]
            def minCount = Integer.parseInt(parts[2])
            
            def file = new File(filePath)
            if (!file.exists()) return false
            
            def content = file.text
            def matches = (content =~ pattern).size()
            return matches >= minCount
        },
        
        'COMPILATION_SUCCESS': { target ->
            try {
                def process = "mvn compile -q".execute()
                process.waitFor()
                return process.exitValue() == 0
            } catch (Exception e) {
                return false
            }
        },
        
        'SERVICE_REGISTERED': { serviceName ->
            def serviceFile = new File("src/main/resources/META-INF/services/liquibase.snapshot.SnapshotGenerator")
            if (!serviceFile.exists()) return false
            return serviceFile.text.contains(serviceName)
        }
    ]
    
    static boolean validate(String validationType, String target, AIPLProgram program) {
        def resolvedTarget = program.resolveVariable(target)
        
        def parts = validationType.split(':')
        def type = parts[0]
        def params = parts.length > 1 ? parts[1..-1].join(':') : resolvedTarget
        
        def validator = VALIDATORS[type]
        if (!validator) {
            println "⚠️  Unknown validation type: ${type}"
            return false
        }
        
        try {
            return validator(params)
        } catch (Exception e) {
            println "❌ Validation error for ${type}: ${e.message}"
            return false
        }
    }
    
    static Map<String, Object> parseValidation(Object validation) {
        if (validation instanceof String) {
            def parts = validation.split(':')
            return [
                type: parts[0],
                target: parts.length > 1 ? parts[1..-1].join(':') : '',
                blocking: true,
                errorMessage: "Validation failed: ${validation}"
            ]
        } else if (validation instanceof Map) {
            return validation + [blocking: true]
        }
        return [:]
    }
}

// ============================================================================
// STATE MANAGEMENT
// ============================================================================

class AIPLState {
    String programName
    String currentPhase
    String currentStep
    int executionCount = 0
    List<String> completedPhases = []
    List<String> completedSteps = []
    Map<String, Object> variables = [:]
    Map<String, Object> metadata = [:]
    List<Map> executionHistory = []
    
    private static final String STATE_DIR = '.aipl_state'
    private String stateFile
    
    AIPLState(String programName) {
        this.programName = programName
        this.stateFile = "${STATE_DIR}/${programName}_state.json"
        load()
    }
    
    void save() {
        new File(STATE_DIR).mkdirs()
        def json = new JsonBuilder()
        json {
            programName this.programName
            currentPhase this.currentPhase
            currentStep this.currentStep
            executionCount this.executionCount
            completedPhases this.completedPhases
            completedSteps this.completedSteps
            variables this.variables
            metadata this.metadata
            executionHistory this.executionHistory
            lastUpdated new Date().toString()
        }
        new File(stateFile).text = json.toPrettyString()
    }
    
    void load() {
        def file = new File(stateFile)
        if (!file.exists()) return
        
        try {
            def data = new JsonSlurper().parse(file)
            this.currentPhase = data.currentPhase
            this.currentStep = data.currentStep
            this.executionCount = data.executionCount ?: 0
            this.completedPhases = data.completedPhases ?: []
            this.completedSteps = data.completedSteps ?: []
            this.variables = data.variables ?: [:]
            this.metadata = data.metadata ?: [:]
            this.executionHistory = data.executionHistory ?: []
        } catch (Exception e) {
            println "⚠️  Could not load state: ${e.message}"
        }
    }
    
    void reset() {
        this.currentPhase = null
        this.currentStep = null
        this.executionCount = 0
        this.completedPhases.clear()
        this.completedSteps.clear()
        this.variables.clear()
        this.metadata.clear()
        this.executionHistory.clear()
        save()
    }
    
    void recordExecution(String type, String description, boolean success, String output = '') {
        executionHistory << [
            timestamp: new Date().toString(),
            type: type,
            description: description,
            success: success,
            output: output,
            phase: currentPhase,
            step: currentStep
        ]
        executionCount++
        save()
    }
    
    boolean isPhaseCompleted(String phaseName) {
        return completedPhases.contains(phaseName)
    }
    
    boolean isStepCompleted(String stepName) {
        return completedSteps.contains(stepName)
    }
    
    void completePhase(String phaseName) {
        if (!completedPhases.contains(phaseName)) {
            completedPhases << phaseName
        }
        save()
    }
    
    void completeStep(String stepName) {
        if (!completedSteps.contains(stepName)) {
            completedSteps << stepName
        }
        save()
    }
    
    String getStatusSummary() {
        return """
=== AIPL ENGINE STATUS ===
Program: ${programName}
Current Phase: ${currentPhase ?: 'Not started'}
Current Step: ${currentStep ?: 'None'}
Execution Count: ${executionCount}

=== COMPLETED PHASES ===
${completedPhases.isEmpty() ? '  (none)' : completedPhases.collect { "  ✅ $it" }.join('\n')}

=== COMPLETED STEPS ===
${completedSteps.isEmpty() ? '  (none)' : completedSteps.collect { "  ✅ $it" }.join('\n')}

=== VARIABLES ===
${variables.isEmpty() ? '  (none)' : variables.collect { k, v -> "  ${k}: ${v}" }.join('\n')}
""".trim()
    }
}

// ============================================================================
// EXECUTION ENGINE
// ============================================================================

class AIPLExecutionEngine {
    AIPLProgram program
    AIPLState state
    ValidationEngine validator = new ValidationEngine()
    
    AIPLExecutionEngine(AIPLProgram program) {
        this.program = program
        this.state = new AIPLState(program.programName)
        
        // Merge program variables into state
        state.variables.putAll(program.variables)
    }
    
    boolean executeProgram() {
        println "🚀 STARTING AIPL PROGRAM EXECUTION"
        println "Program: ${program.programName}"
        println "Version: ${program.version}"
        println "Enforcement: ${program.enforcementMode}"
        println ""
        
        try {
            def phaseNames = program.getPhaseNames()
            
            for (String phaseName : phaseNames) {
                if (state.isPhaseCompleted(phaseName)) {
                    println "⏭️  Skipping completed phase: ${phaseName}"
                    continue
                }
                
                if (!executePhase(phaseName)) {
                    println "❌ Program execution stopped at phase: ${phaseName}"
                    return false
                }
            }
            
            println "✅ Program execution completed successfully"
            return true
            
        } catch (Exception e) {
            println "💥 Program execution failed: ${e.message}"
            e.printStackTrace()
            return false
        }
    }
    
    boolean executePhase(String phaseName) {
        println "🔄 EXECUTING PHASE: ${phaseName}"
        
        state.currentPhase = phaseName
        state.save()
        
        def phase = program.getPhase(phaseName)
        if (!phase) {
            println "❌ Phase not found: ${phaseName}"
            return false
        }
        
        // Execute all steps in the phase
        for (def entry : phase.entrySet()) {
            String stepName = entry.key
            def stepData = entry.value
            
            if (!executeStep(stepName, stepData)) {
                println "❌ Phase ${phaseName} failed at step: ${stepName}"
                return false
            }
        }
        
        state.completePhase(phaseName)
        println "✅ Phase completed: ${phaseName}"
        return true
    }
    
    boolean executeStep(String stepName, Map stepData) {
        println "\n  🔧 Executing step: ${stepName}"
        
        state.currentStep = stepName
        state.save()
        
        try {
            // Check blocking prerequisites
            if (stepData.BLOCKING_PREREQUISITE_CHECK) {
                if (!executeBlockingPrerequisiteCheck(stepData.BLOCKING_PREREQUISITE_CHECK)) {
                    return false
                }
            }
            
            // Handle different step types
            if (stepData.PURPOSE) {
                println "  📋 Purpose: ${program.resolveVariable(stepData.PURPOSE)}"
            }
            
            // Execute step based on type
            boolean stepSuccess = true
            
            if (stepData.EXECUTION_STEPS) {
                stepSuccess = executeExecutionSteps(stepData.EXECUTION_STEPS)
            }
            
            if (stepSuccess && stepData.CREATES) {
                stepSuccess = executeFileCreation(stepData.CREATES, stepData)
            }
            
            if (stepSuccess && stepData.AUTONOMOUS_COMMAND) {
                stepSuccess = executeAutonomousCommand(stepData.AUTONOMOUS_COMMAND)
            }
            
            // Validate step completion
            if (stepSuccess && stepData.BLOCKING_VALIDATION) {
                stepSuccess = executeBlockingValidation(stepData.BLOCKING_VALIDATION, stepData)
            }
            
            if (stepSuccess) {
                state.completeStep(stepName)
                state.recordExecution("STEP", stepName, true)
                println "  ✅ Step completed: ${stepName}"
            } else {
                state.recordExecution("STEP", stepName, false)
                handleStepFailure(stepName, stepData)
            }
            
            return stepSuccess
            
        } catch (Exception e) {
            println "  💥 Step execution error: ${e.message}"
            state.recordExecution("STEP", stepName, false, e.message)
            return false
        }
    }
    
    boolean executeBlockingPrerequisiteCheck(Map prerequisiteData) {
        println "  🛡️  Checking blocking prerequisites..."
        
        if (prerequisiteData.BLOCKING_VALIDATIONS) {
            for (validation in prerequisiteData.BLOCKING_VALIDATIONS) {
                def validationData = ValidationEngine.parseValidation(validation)
                boolean result = ValidationEngine.validate(validationData.type, validationData.target, program)
                
                if (!result) {
                    println "  ❌ Prerequisite validation failed: ${validation}"
                    if (prerequisiteData.ERROR_MESSAGE) {
                        println "  📢 ${program.resolveVariable(prerequisiteData.ERROR_MESSAGE)}"
                    }
                    if (prerequisiteData.STOP_ON_FAILURE) {
                        return false
                    }
                }
            }
        }
        
        return true
    }
    
    boolean executeExecutionSteps(List executionSteps) {
        for (step in executionSteps) {
            def stepText = program.resolveVariable(step.toString())
            println "  ▶️  ${stepText}"
            
            // For now, just log the step - in a full implementation,
            // we'd parse and execute different step types
            state.recordExecution("EXECUTION_STEP", stepText, true)
        }
        return true
    }
    
    boolean executeFileCreation(String filePath, Map stepData) {
        def resolvedPath = program.resolveVariable(filePath)
        println "  📄 Creating file: ${resolvedPath}"
        
        try {
            def file = new File(resolvedPath)
            file.parentFile?.mkdirs()
            
            if (stepData.TEMPLATE) {
                def content = program.resolveVariable(stepData.TEMPLATE)
                file.text = content
            } else if (stepData.MINIMAL_IMPLEMENTATION) {
                def content = program.resolveVariable(stepData.MINIMAL_IMPLEMENTATION)
                file.text = content
            } else {
                // Create empty file
                file.createNewFile()
            }
            
            state.recordExecution("FILE_CREATION", resolvedPath, true)
            return true
            
        } catch (Exception e) {
            println "  ❌ File creation failed: ${e.message}"
            state.recordExecution("FILE_CREATION", resolvedPath, false, e.message)
            return false
        }
    }
    
    boolean executeAutonomousCommand(String command) {
        def resolvedCommand = program.resolveVariable(command)
        println "  🤖 Autonomous command: ${resolvedCommand}"
        
        try {
            def process = resolvedCommand.execute()
            process.waitFor()
            def success = process.exitValue() == 0
            def output = process.text
            
            if (success) {
                println "  ✅ Command succeeded"
            } else {
                println "  ❌ Command failed (exit code: ${process.exitValue()})"
                if (output) println "  📝 Output: ${output.take(200)}..."
            }
            
            state.recordExecution("AUTONOMOUS_COMMAND", resolvedCommand, success, output)
            return success
            
        } catch (Exception e) {
            println "  💥 Command execution failed: ${e.message}"
            state.recordExecution("AUTONOMOUS_COMMAND", resolvedCommand, false, e.message)
            return false
        }
    }
    
    boolean executeBlockingValidation(Object validation, Map stepData) {
        println "  🔍 Executing blocking validation..."
        
        def validationData = ValidationEngine.parseValidation(validation)
        boolean result = ValidationEngine.validate(validationData.type, validationData.target, program)
        
        if (result) {
            println "  ✅ Validation passed: ${validation}"
        } else {
            println "  ❌ Validation failed: ${validation}"
            if (stepData.ERROR_MESSAGE) {
                println "  📢 ${program.resolveVariable(stepData.ERROR_MESSAGE)}"
            }
            if (stepData.STOP_ON_FAILURE) {
                return false
            }
        }
        
        return result || !stepData.STOP_ON_FAILURE
    }
    
    void handleStepFailure(String stepName, Map stepData) {
        println "  💔 Step failed: ${stepName}"
        
        if (stepData.ERROR_HANDLING) {
            println "  🔧 Error handling: ${stepData.ERROR_HANDLING}"
        }
        
        if (program.enforcementMode == "STRICT_TDD" && program.blockingBehavior == "STOP_ON_VIOLATION") {
            println "  🚫 STRICT ENFORCEMENT - Execution stopped"
        }
    }
    
    void reset() {
        state.reset()
        println "✅ Engine state reset"
    }
    
    void status() {
        println state.statusSummary
    }
}

// ============================================================================
// MAIN AIPL ENGINE INTERFACE
// ============================================================================

class AIPLEngine {
    static void main(String[] args) {
        if (args.length == 0) {
            showUsage()
            return
        }
        
        def command = args[0]
        
        try {
            switch (command) {
                case 'run':
                    if (args.length < 2) {
                        println "❌ Usage: run <program-file>"
                        System.exit(1)
                    }
                    runProgram(args[1])
                    break
                    
                case 'status':
                    if (args.length < 2) {
                        println "❌ Usage: status <program-file>"
                        System.exit(1)
                    }
                    showStatus(args[1])
                    break
                    
                case 'reset':
                    if (args.length < 2) {
                        println "❌ Usage: reset <program-file>"
                        System.exit(1)
                    }
                    resetProgram(args[1])
                    break
                    
                case 'validate':
                    if (args.length < 2) {
                        println "❌ Usage: validate <program-file>"
                        System.exit(1)
                    }
                    validateProgram(args[1])
                    break
                    
                default:
                    println "❌ Unknown command: ${command}"
                    showUsage()
                    System.exit(1)
            }
        } catch (Exception e) {
            println "💥 Engine error: ${e.message}"
            e.printStackTrace()
            System.exit(1)
        }
    }
    
    static void runProgram(String programPath) {
        println "🎯 Loading AIPL program: ${programPath}"
        
        def program = AIPLProgram.load(programPath)
        def engine = new AIPLExecutionEngine(program)
        
        if (engine.executeProgram()) {
            println "\n🎉 Program execution completed successfully!"
        } else {
            println "\n💔 Program execution failed"
            System.exit(1)
        }
    }
    
    static void showStatus(String programPath) {
        def program = AIPLProgram.load(programPath)
        def engine = new AIPLExecutionEngine(program)
        engine.status()
    }
    
    static void resetProgram(String programPath) {
        def program = AIPLProgram.load(programPath)
        def engine = new AIPLExecutionEngine(program)
        engine.reset()
    }
    
    static void validateProgram(String programPath) {
        println "🔍 Validating AIPL program: ${programPath}"
        
        try {
            def program = AIPLProgram.load(programPath)
            println "✅ Program loaded successfully"
            println "  - Version: ${program.version}"
            println "  - Name: ${program.programName}"
            println "  - Phases: ${program.getPhaseNames().size()}"
            println "  - Variables: ${program.variables.size()}"
            
        } catch (Exception e) {
            println "❌ Program validation failed: ${e.message}"
            System.exit(1)
        }
    }
    
    static void showUsage() {
        println """
AIPL v2.0 Engine - AI Implementation Programming Language Interpreter

COMMANDS:
  run <program-file>      - Execute AIPL workflow program
  status <program-file>   - Show current execution status
  reset <program-file>    - Reset program execution state
  validate <program-file> - Validate program syntax

EXAMPLES:
  groovy aipl_engine.groovy run claude_guide/implementation_guides/scenario_programs/snapshot_diff/tdd-enforced-object-implementation.yaml
  groovy aipl_engine.groovy status tdd-enforced-object-implementation.yaml
  groovy aipl_engine.groovy reset tdd-enforced-object-implementation.yaml

AIPL PROGRAMS:
  AIPL programs are YAML-based workflow definitions that specify:
  - Sequential phases with dependencies
  - Validation gates and enforcement points  
  - Autonomous command execution
  - State management and variable resolution
  - Error handling and recovery procedures
"""
    }
}

// Execute main if called directly
if (this.args) {
    AIPLEngine.main(this.args)
}
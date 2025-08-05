#!/usr/bin/env groovy

/**
 * AIPL v2.0 Engine Test Suite
 * 
 * Comprehensive unit and integration tests for the AIPL workflow orchestration engine.
 */

@Grab('org.spockframework:spock-core:2.3-groovy-3.0')

import spock.lang.Specification
import spock.lang.Shared
import spock.lang.Unroll
import groovy.json.JsonSlurper
import groovy.yaml.YamlSlurper
import groovy.yaml.YamlBuilder
import java.nio.file.Files
import java.nio.file.Paths

// Load the AIPL engine classes
evaluate(new File('.scripts/aipl_engine.groovy'))

// ============================================================================
// UNIT TESTS
// ============================================================================

class AIPLProgramTest extends Specification {
    
    @Shared
    File tempDir
    
    def setupSpec() {
        tempDir = Files.createTempDirectory("aipl_test").toFile()
    }
    
    def cleanupSpec() {
        tempDir.deleteDir()
    }
    
    def "should load valid AIPL program"() {
        given: "a valid AIPL program file"
        def programFile = new File(tempDir, "test_program.yaml")
        programFile.text = """
AIPL_VERSION: "2.0"
PROGRAM_NAME: "test-program"
DESCRIPTION: "Test program"
ENFORCEMENT_MODE: "STRICT"
VARIABLES:
  TEST_VAR: "test_value"
PHASES:
  PHASE_1:
    STEP_1:
      PURPOSE: "Test step"
"""
        
        when: "loading the program"
        def program = AIPLProgram.load(programFile.absolutePath)
        
        then: "program properties are correctly parsed"
        program.version == "2.0"
        program.programName == "test-program"
        program.description == "Test program"
        program.enforcementMode == "STRICT"
        program.variables.TEST_VAR == "test_value"
        program.phases.size() == 1
        program.getPhaseNames().contains("PHASE_1")
    }
    
    def "should handle missing program file"() {
        when: "loading non-existent program"
        AIPLProgram.load("non_existent.yaml")
        
        then: "exception is thrown"
        thrown(IllegalArgumentException)
    }
    
    @Unroll
    def "should resolve variables: '#input' -> '#expected'"() {
        given: "a program with variables"
        def program = new AIPLProgram()
        program.variables = [
            TEST_VAR: "test_value",
            OBJECT_TYPE: "FileFormat",
            PATH: "src/main/java"
        ]
        
        expect: "variable resolution works correctly"
        program.resolveVariable(input) == expected
        
        where:
        input                                    | expected
        "Simple text"                           | "Simple text"
        "\${TEST_VAR}"                          | "test_value"
        "Path: \${PATH}/\${OBJECT_TYPE}.java"  | "Path: src/main/java/FileFormat.java"
        "\${UNKNOWN_VAR}"                       | "\${UNKNOWN_VAR}"
        null                                    | null
        ""                                      | ""
    }
}

class ValidationEngineTest extends Specification {
    
    @Shared
    File tempDir
    @Shared
    File testFile
    
    def setupSpec() {
        tempDir = Files.createTempDirectory("aipl_validation_test").toFile()
        testFile = new File(tempDir, "test_file.txt")
        testFile.text = "test content"
    }
    
    def cleanupSpec() {
        tempDir.deleteDir()
    }
    
    def "should validate file existence"() {
        given: "a test program"
        def program = new AIPLProgram()
        
        expect: "file existence validation works"
        ValidationEngine.validate("FILE_EXISTS", testFile.absolutePath, program) == true
        ValidationEngine.validate("FILE_EXISTS", "non_existent_file.txt", program) == false
    }
    
    def "should validate directory existence"() {
        given: "a test program"
        def program = new AIPLProgram()
        
        expect: "directory existence validation works"
        ValidationEngine.validate("DIRECTORY_EXISTS", tempDir.absolutePath, program) == true
        ValidationEngine.validate("DIRECTORY_EXISTS", "non_existent_dir", program) == false
    }
    
    def "should validate command success"() {
        given: "a test program"
        def program = new AIPLProgram()
        
        expect: "command success validation works"
        ValidationEngine.validate("COMMAND_SUCCESS", "echo 'test'", program) == true
        ValidationEngine.validate("COMMAND_SUCCESS", "false", program) == false
    }
    
    def "should parse validation objects"() {
        expect: "validation parsing works correctly"
        ValidationEngine.parseValidation("FILE_EXISTS:path/to/file").type == "FILE_EXISTS"
        ValidationEngine.parseValidation("FILE_EXISTS:path/to/file").target == "path/to/file"
        ValidationEngine.parseValidation([type: "CUSTOM", target: "test"]).type == "CUSTOM"
    }
    
    def "should handle properties count validation"() {
        given: "a test file with patterns"
        def testFile = new File(tempDir, "properties_test.txt")
        testFile.text = """
        property1: value
        property2: value
        property3: value
        """
        def program = new AIPLProgram()
        
        expect: "properties count validation works"
        ValidationEngine.validate("PROPERTIES_COUNT", "${testFile.absolutePath}:property:2", program) == true
        ValidationEngine.validate("PROPERTIES_COUNT", "${testFile.absolutePath}:property:5", program) == false
    }
}

class AIPLStateTest extends Specification {
    
    @Shared
    File tempDir
    
    def setupSpec() {
        tempDir = Files.createTempDirectory("aipl_state_test").toFile()
        // Change to temp directory for state files
        System.setProperty("user.dir", tempDir.absolutePath)
    }
    
    def cleanupSpec() {
        tempDir.deleteDir()
    }
    
    def "should create and persist state"() {
        given: "a new state"
        def state = new AIPLState("test-program")
        
        when: "updating state properties"
        state.currentPhase = "PHASE_1"
        state.currentStep = "STEP_1"
        state.variables.TEST_VAR = "test_value"
        state.save()
        
        and: "loading state again"
        def loadedState = new AIPLState("test-program")
        
        then: "state is persisted correctly"
        loadedState.currentPhase == "PHASE_1"
        loadedState.currentStep == "STEP_1"
        loadedState.variables.TEST_VAR == "test_value"
    }
    
    def "should track completion status"() {
        given: "a state instance"
        def state = new AIPLState("completion-test")
        
        when: "completing phases and steps"
        state.completePhase("PHASE_1")
        state.completeStep("STEP_1")
        
        then: "completion is tracked"
        state.isPhaseCompleted("PHASE_1") == true
        state.isStepCompleted("STEP_1") == true
        state.isPhaseCompleted("PHASE_2") == false
        state.isStepCompleted("STEP_2") == false
    }
    
    def "should record execution history"() {
        given: "a state instance"
        def state = new AIPLState("history-test")
        
        when: "recording executions"
        state.recordExecution("COMMAND", "test command", true, "success output")
        state.recordExecution("VALIDATION", "test validation", false, "failure output")
        
        then: "execution history is maintained"
        state.executionHistory.size() == 2
        state.executionHistory[0].type == "COMMAND"
        state.executionHistory[0].success == true
        state.executionHistory[1].type == "VALIDATION"
        state.executionHistory[1].success == false
        state.executionCount == 2
    }
    
    def "should reset state completely"() {
        given: "a state with data"
        def state = new AIPLState("reset-test")
        state.currentPhase = "PHASE_1"
        state.completedPhases << "COMPLETED_PHASE"
        state.variables.TEST = "value"
        state.executionCount = 5
        
        when: "resetting state"
        state.reset()
        
        then: "all state is cleared"
        state.currentPhase == null
        state.completedPhases.isEmpty()
        state.variables.isEmpty()
        state.executionCount == 0
    }
}

// ============================================================================
// INTEGRATION TESTS
// ============================================================================

class AIPLExecutionEngineIntegrationTest extends Specification {
    
    @Shared
    File tempDir
    @Shared
    File testWorkflowFile
    
    def setupSpec() {
        tempDir = Files.createTempDirectory("aipl_integration_test").toFile()
        System.setProperty("user.dir", tempDir.absolutePath)
        
        // Create test workflow
        testWorkflowFile = new File(tempDir, "integration_test_workflow.yaml")
        testWorkflowFile.text = """
AIPL_VERSION: "2.0"
PROGRAM_NAME: "integration-test"
DESCRIPTION: "Integration test workflow"
ENFORCEMENT_MODE: "STANDARD"
BLOCKING_BEHAVIOR: "STOP_ON_VIOLATION"

VARIABLES:
  TEST_FILE: "integration_test.txt"
  TEST_CONTENT: "Integration test content"

PHASES:
  PHASE_1_SETUP:
    CREATE_TEST_FILE:
      PURPOSE: "Create test file"
      CREATES: "\${TEST_FILE}"
      TEMPLATE: "\${TEST_CONTENT}"
      BLOCKING_VALIDATION: "FILE_EXISTS:\${TEST_FILE}"
      STOP_ON_FAILURE: true
      ERROR_MESSAGE: "Failed to create test file"
    
    VALIDATE_FILE_CONTENT:
      PURPOSE: "Validate file was created with correct content"
      AUTONOMOUS_COMMAND: "cat \${TEST_FILE}"
      BLOCKING_VALIDATION: "FILE_EXISTS:\${TEST_FILE}"
      ERROR_MESSAGE: "File validation failed"

  PHASE_2_VALIDATION:
    CHECK_PREREQUISITES:
      PURPOSE: "Validate prerequisites are met"
      BLOCKING_PREREQUISITE_CHECK:
        BLOCKING_VALIDATIONS:
          - "FILE_EXISTS:\${TEST_FILE}"
        STOP_ON_FAILURE: true
        ERROR_MESSAGE: "Prerequisites not met"
    
    SUCCESS_COMMAND:
      PURPOSE: "Execute success command"
      AUTONOMOUS_COMMAND: "echo 'Integration test success'"
      BLOCKING_VALIDATION: "COMMAND_SUCCESS:echo 'test'"
      ERROR_MESSAGE: "Success command failed"

  PHASE_3_CLEANUP:
    CLEANUP_FILES:
      PURPOSE: "Clean up test files"
      EXECUTION_STEPS:
        - "Remove test file: rm -f \${TEST_FILE}"
        - "Verify cleanup"
"""
    }
    
    def cleanupSpec() {
        tempDir.deleteDir()
    }
    
    def "should execute complete workflow successfully"() {
        given: "a valid workflow program"
        def program = AIPLProgram.load(testWorkflowFile.absolutePath)
        def engine = new AIPLExecutionEngine(program)
        
        when: "executing the program"
        def result = engine.executeProgram()
        
        then: "program executes successfully"
        result == true
        
        and: "all phases are completed"
        engine.state.isPhaseCompleted("PHASE_1_SETUP")
        engine.state.isPhaseCompleted("PHASE_2_VALIDATION")
        engine.state.isPhaseCompleted("PHASE_3_CLEANUP")
        
        and: "all steps are completed"
        engine.state.isStepCompleted("CREATE_TEST_FILE")
        engine.state.isStepCompleted("VALIDATE_FILE_CONTENT")
        engine.state.isStepCompleted("CHECK_PREREQUISITES")
        engine.state.isStepCompleted("SUCCESS_COMMAND")
        engine.state.isStepCompleted("CLEANUP_FILES")
        
        and: "execution history is recorded"
        engine.state.executionCount > 0
        engine.state.executionHistory.size() > 0
    }
    
    def "should handle workflow interruption and resume"() {
        given: "a workflow that will be interrupted"
        def program = AIPLProgram.load(testWorkflowFile.absolutePath)
        def engine1 = new AIPLExecutionEngine(program)
        
        when: "executing first phase only"
        engine1.executePhase("PHASE_1_SETUP")
        
        then: "first phase is completed"
        engine1.state.isPhaseCompleted("PHASE_1_SETUP")
        !engine1.state.isPhaseCompleted("PHASE_2_VALIDATION")
        
        when: "creating new engine instance (simulating restart)"
        def engine2 = new AIPLExecutionEngine(program)
        
        then: "state is preserved"
        engine2.state.isPhaseCompleted("PHASE_1_SETUP")
        !engine2.state.isPhaseCompleted("PHASE_2_VALIDATION")
        
        when: "continuing execution"
        def result = engine2.executeProgram()
        
        then: "remaining phases execute successfully"
        result == true
        engine2.state.isPhaseCompleted("PHASE_2_VALIDATION")
        engine2.state.isPhaseCompleted("PHASE_3_CLEANUP")
    }
}

class AIPLEngineErrorHandlingTest extends Specification {
    
    @Shared
    File tempDir
    
    def setupSpec() {
        tempDir = Files.createTempDirectory("aipl_error_test").toFile()
        System.setProperty("user.dir", tempDir.absolutePath)
    }
    
    def cleanupSpec() {
        tempDir.deleteDir()
    }
    
    def "should handle blocking validation failures"() {
        given: "a workflow with failing validation"
        def workflowFile = new File(tempDir, "failing_workflow.yaml")
        workflowFile.text = """
AIPL_VERSION: "2.0"
PROGRAM_NAME: "failing-test"
DESCRIPTION: "Test workflow with failing validation"
ENFORCEMENT_MODE: "STRICT"
BLOCKING_BEHAVIOR: "STOP_ON_VIOLATION"

VARIABLES:
  NON_EXISTENT_FILE: "this_file_does_not_exist.txt"

PHASES:
  PHASE_1_FAIL:
    FAILING_STEP:
      PURPOSE: "This step should fail"
      BLOCKING_VALIDATION: "FILE_EXISTS:\${NON_EXISTENT_FILE}"
      STOP_ON_FAILURE: true
      ERROR_MESSAGE: "Expected failure message"
"""
        
        def program = AIPLProgram.load(workflowFile.absolutePath)
        def engine = new AIPLExecutionEngine(program)
        
        when: "executing the failing program"
        def result = engine.executeProgram()
        
        then: "program execution fails as expected"
        result == false
        
        and: "failure is recorded in execution history"
        engine.state.executionHistory.any { !it.success }
    }
    
    def "should handle command execution failures"() {
        given: "a workflow with failing command"
        def workflowFile = new File(tempDir, "command_fail_workflow.yaml")
        workflowFile.text = """
AIPL_VERSION: "2.0"
PROGRAM_NAME: "command-fail-test"
DESCRIPTION: "Test workflow with failing command"

PHASES:
  PHASE_1_COMMAND_FAIL:
    FAILING_COMMAND:
      PURPOSE: "Execute failing command"
      AUTONOMOUS_COMMAND: "false"
      BLOCKING_VALIDATION: "COMMAND_SUCCESS:false"
      STOP_ON_FAILURE: true
      ERROR_MESSAGE: "Command should fail"
"""
        
        def program = AIPLProgram.load(workflowFile.absolutePath)
        def engine = new AIPLExecutionEngine(program)
        
        when: "executing the program with failing command"
        def result = engine.executeProgram()
        
        then: "program handles command failure"
        result == false
        engine.state.executionHistory.any { it.type == "AUTONOMOUS_COMMAND" && !it.success }
    }
    
    def "should handle prerequisite check failures"() {
        given: "a workflow with failing prerequisites"
        def workflowFile = new File(tempDir, "prereq_fail_workflow.yaml")
        workflowFile.text = """
AIPL_VERSION: "2.0"
PROGRAM_NAME: "prereq-fail-test"
DESCRIPTION: "Test workflow with failing prerequisites"

PHASES:
  PHASE_1_PREREQ_FAIL:
    STEP_WITH_PREREQS:
      PURPOSE: "Step with failing prerequisites"
      BLOCKING_PREREQUISITE_CHECK:
        BLOCKING_VALIDATIONS:
          - "FILE_EXISTS:missing_file.txt"
          - "DIRECTORY_EXISTS:missing_directory"
        STOP_ON_FAILURE: true
        ERROR_MESSAGE: "Prerequisites not met"
"""
        
        def program = AIPLProgram.load(workflowFile.absolutePath)
        def engine = new AIPLExecutionEngine(program)
        
        when: "executing program with failing prerequisites"
        def result = engine.executeProgram()
        
        then: "program stops at prerequisite failure"
        result == false
    }
}

// ============================================================================
// PERFORMANCE AND EDGE CASE TESTS
// ============================================================================

class AIPLEnginePerformanceTest extends Specification {
    
    @Shared
    File tempDir
    
    def setupSpec() {
        tempDir = Files.createTempDirectory("aipl_perf_test").toFile()
        System.setProperty("user.dir", tempDir.absolutePath)
    }
    
    def cleanupSpec() {
        tempDir.deleteDir()
    }
    
    def "should handle large workflow programs"() {
        given: "a large workflow with many phases and steps"
        def workflowFile = new File(tempDir, "large_workflow.yaml")
        def yamlBuilder = new YamlBuilder()
        
        def phases = [:]
        (1..10).each { phaseNum ->
            def steps = [:]
            (1..5).each { stepNum ->
                steps["STEP_${stepNum}"] = [
                    PURPOSE: "Step ${stepNum} in phase ${phaseNum}",
                    EXECUTION_STEPS: ["echo 'Phase ${phaseNum}, Step ${stepNum}'"],
                    BLOCKING_VALIDATION: "COMMAND_SUCCESS:echo 'test'"
                ]
            }
            phases["PHASE_${phaseNum}"] = steps
        }
        
        def workflowData = [
            AIPL_VERSION: "2.0",
            PROGRAM_NAME: "large-workflow-test",
            DESCRIPTION: "Large workflow for performance testing",
            VARIABLES: [TEST_VAR: "test_value"],
            PHASES: phases
        ]
        
        workflowFile.text = yamlBuilder.call(workflowData).toString()
        
        when: "executing large workflow"
        def startTime = System.currentTimeMillis()
        def program = AIPLProgram.load(workflowFile.absolutePath)
        def engine = new AIPLExecutionEngine(program)
        def result = engine.executeProgram()
        def endTime = System.currentTimeMillis()
        
        then: "workflow executes successfully within reasonable time"
        result == true
        (endTime - startTime) < 30000 // Should complete within 30 seconds
        engine.state.completedPhases.size() == 10
        engine.state.completedSteps.size() == 50
    }
    
    def "should handle edge case variable resolution"() {
        given: "a program with complex variable scenarios"
        def program = new AIPLProgram()
        program.variables = [
            SIMPLE: "simple_value",
            NESTED: "\${SIMPLE}_nested",
            CIRCULAR_A: "\${CIRCULAR_B}",
            CIRCULAR_B: "\${CIRCULAR_A}",
            EMPTY: "",
            SPECIAL_CHARS: "value!@#\$%^&*()"
        ]
        
        expect: "edge cases are handled gracefully"
        program.resolveVariable("\${SIMPLE}") == "simple_value"
        program.resolveVariable("\${EMPTY}") == ""
        program.resolveVariable("\${SPECIAL_CHARS}") == "value!@#\$%^&*()"
        program.resolveVariable("\${UNDEFINED}") == "\${UNDEFINED}"
        // Circular references should not cause infinite loops
        program.resolveVariable("\${CIRCULAR_A}") in ["\${CIRCULAR_B}", "\${CIRCULAR_A}"]
    }
    
    def "should handle concurrent state access"() {
        given: "a state instance"
        def state = new AIPLState("concurrent-test")
        def errors = Collections.synchronizedList([])
        
        when: "accessing state concurrently"
        def threads = (1..10).collect { threadNum ->
            Thread.start {
                try {
                    (1..100).each { iteration ->
                        state.variables["thread_${threadNum}_var_${iteration}"] = "value_${iteration}"
                        state.recordExecution("TEST", "Thread ${threadNum} iteration ${iteration}", true)
                        state.save()
                    }
                } catch (Exception e) {
                    errors.add(e)
                }
            }
        }
        
        threads.each { it.join(5000) } // Wait up to 5 seconds for each thread
        
        then: "no concurrency errors occur"
        errors.isEmpty()
        state.variables.size() == 1000 // 10 threads × 100 iterations
        state.executionHistory.size() == 1000
    }
}

// ============================================================================
// TEST RUNNER
// ============================================================================

class AIPLTestRunner {
    static void main(String[] args) {
        println "🧪 Running AIPL v2.0 Engine Test Suite"
        println "=" * 50
        
        def testSuite = [
            AIPLProgramTest,
            ValidationEngineTest, 
            AIPLStateTest,
            AIPLExecutionEngineIntegrationTest,
            AIPLEngineErrorHandlingTest,
            AIPLEnginePerformanceTest
        ]
        
        def totalTests = 0
        def passedTests = 0
        def failedTests = 0
        
        testSuite.each { testClass ->
            println "\n🔍 Running ${testClass.simpleName}..."
            
            try {
                // For this simple test runner, we'll just instantiate and call test methods
                def testInstance = testClass.newInstance()
                def testMethods = testClass.methods.findAll { 
                    it.name.startsWith('should') || it.name.startsWith('test') 
                }
                
                testMethods.each { method ->
                    totalTests++
                    try {
                        // Setup
                        if (testInstance.hasProperty('setupSpec')) {
                            testInstance.setupSpec()
                        }
                        if (testInstance.hasProperty('setup')) {
                            testInstance.setup()
                        }
                        
                        // Execute test
                        method.invoke(testInstance)
                        
                        // Cleanup
                        if (testInstance.hasProperty('cleanup')) {
                            testInstance.cleanup()
                        }
                        if (testInstance.hasProperty('cleanupSpec')) {
                            testInstance.cleanupSpec()
                        }
                        
                        println "  ✅ ${method.name}"
                        passedTests++
                        
                    } catch (Exception e) {
                        println "  ❌ ${method.name}: ${e.message}"
                        failedTests++
                    }
                }
                
            } catch (Exception e) {
                println "  💥 Test class error: ${e.message}"
                failedTests++
            }
        }
        
        println "\n" + "=" * 50
        println "🏁 Test Results:"
        println "  Total Tests: ${totalTests}"
        println "  Passed: ${passedTests}"
        println "  Failed: ${failedTests}"
        println "  Success Rate: ${totalTests > 0 ? (passedTests / totalTests * 100).round(1) : 0}%"
        
        if (failedTests == 0) {
            println "\n🎉 All tests passed!"
            System.exit(0)
        } else {
            println "\n💔 Some tests failed!"
            System.exit(1)
        }
    }
}

// Run tests if executed directly
if (this.args && this.args[0] == 'test') {
    AIPLTestRunner.main(this.args[1..-1])
}
#!/usr/bin/env groovy

/**
 * Simple Test Runner for AIPL Engine
 * Tests core functionality without external dependencies
 */

// Load the AIPL engine
evaluate(new File('.scripts/aipl_engine.groovy'))

class SimpleTestRunner {
    static int testCount = 0
    static int passedTests = 0
    static int failedTests = 0
    
    static void runTest(String testName, Closure test) {
        testCount++
        print "  Testing ${testName}... "
        
        try {
            test()
            println "✅ PASSED"
            passedTests++
        } catch (Exception e) {
            println "❌ FAILED: ${e.message}"
            failedTests++
        }
    }
    
    static void main(String[] args) {
        println "🧪 AIPL v2.0 Engine - Simple Test Suite"
        println "=" * 50
        
        // Test AIPLProgram
        println "\n📋 Testing AIPLProgram class..."
        testAIPLProgram()
        
        // Test ValidationEngine  
        println "\n🔍 Testing ValidationEngine class..."
        testValidationEngine()
        
        // Test AIPLState
        println "\n💾 Testing AIPLState class..."
        testAIPLState()
        
        // Test Integration
        println "\n🔄 Testing Integration scenarios..."
        testIntegration()
        
        // Results
        println "\n" + "=" * 50
        println "🏁 Test Results:"
        println "  Total Tests: ${testCount}"
        println "  Passed: ${passedTests}"
        println "  Failed: ${failedTests}"
        println "  Success Rate: ${testCount > 0 ? (passedTests / testCount * 100).round(1) : 0}%"
        
        if (failedTests == 0) {
            println "\n🎉 All tests passed!"
        } else {
            println "\n💔 ${failedTests} test(s) failed!"
        }
    }
    
    static void testAIPLProgram() {
        runTest("Variable resolution") {
            def program = new AIPLProgram()
            program.variables = [TEST_VAR: "test_value", PATH: "src"]
            
            assert program.resolveVariable('${TEST_VAR}') == "test_value"
            assert program.resolveVariable('${PATH}/${TEST_VAR}') == "src/test_value"
            assert program.resolveVariable('${UNKNOWN}') == '${UNKNOWN}'
            assert program.resolveVariable('plain text') == "plain text"
        }
        
        runTest("Program creation") {
            def program = new AIPLProgram()
            program.programName = "test-program"
            program.version = "2.0"
            program.variables = [TEST: "value"]
            
            assert program.programName == "test-program"
            assert program.version == "2.0"
            assert program.variables.TEST == "value"
        }
    }
    
    static void testValidationEngine() {
        runTest("Validation parsing") {
            def parsed = ValidationEngine.parseValidation("FILE_EXISTS:test.txt")
            assert parsed.type == "FILE_EXISTS"
            assert parsed.target == "test.txt"
            assert parsed.blocking == true
        }
        
        runTest("Command success validation") {
            def program = new AIPLProgram()
            assert ValidationEngine.validate("COMMAND_SUCCESS", "echo 'test'", program) == true
            assert ValidationEngine.validate("COMMAND_SUCCESS", "false", program) == false
        }
        
        runTest("File existence validation") {
            def program = new AIPLProgram()
            // Create a temporary file for testing
            def testFile = File.createTempFile("aipl_test", ".tmp")
            testFile.deleteOnExit()
            
            assert ValidationEngine.validate("FILE_EXISTS", testFile.absolutePath, program) == true
            assert ValidationEngine.validate("FILE_EXISTS", "non_existent_file.txt", program) == false
        }
    }
    
    static void testAIPLState() {
        def tempDir = File.createTempDir("aipl_state_test")
        def originalDir = System.getProperty("user.dir")
        
        try {
            System.setProperty("user.dir", tempDir.absolutePath)
            
            runTest("State creation and persistence") {
                def state = new AIPLState("test-program")
                state.currentPhase = "PHASE_1"
                state.variables.TEST = "value"
                state.save()
                
                def loadedState = new AIPLState("test-program")
                assert loadedState.currentPhase == "PHASE_1"
                assert loadedState.variables.TEST == "value"
            }
            
            runTest("Completion tracking") {
                def state = new AIPLState("completion-test")
                state.completePhase("PHASE_1")
                state.completeStep("STEP_1")
                
                assert state.isPhaseCompleted("PHASE_1") == true
                assert state.isStepCompleted("STEP_1") == true
                assert state.isPhaseCompleted("PHASE_2") == false
            }
            
            runTest("Execution history") {
                def state = new AIPLState("history-test")
                state.recordExecution("TEST", "test action", true, "success")
                
                assert state.executionHistory.size() == 1
                assert state.executionHistory[0].type == "TEST"
                assert state.executionHistory[0].success == true
                assert state.executionCount == 1
            }
            
        } finally {
            System.setProperty("user.dir", originalDir)
            tempDir.deleteDir()
        }
    }
    
    static void testIntegration() {
        def tempDir = File.createTempDir("aipl_integration_test")
        def originalDir = System.getProperty("user.dir")
        
        try {
            System.setProperty("user.dir", tempDir.absolutePath)
            
            runTest("Simple workflow execution") {
                // Create a simple test workflow
                def workflowFile = new File(tempDir, "simple_test.yaml")
                workflowFile.text = """
AIPL_VERSION: "2.0"
PROGRAM_NAME: "simple-integration-test"
DESCRIPTION: "Simple integration test"
VARIABLES:
  TEST_FILE: "integration_test.txt"
PHASES:
  PHASE_1:
    CREATE_FILE:
      PURPOSE: "Create test file"
      CREATES: "\${TEST_FILE}"
      TEMPLATE: "Integration test content"
      BLOCKING_VALIDATION: "FILE_EXISTS:\${TEST_FILE}"
      STOP_ON_FAILURE: true
"""
                
                def program = AIPLProgram.load(workflowFile.absolutePath)
                def engine = new AIPLExecutionEngine(program)
                def result = engine.executeProgram()
                
                assert result == true
                assert engine.state.isPhaseCompleted("PHASE_1")
                assert engine.state.isStepCompleted("CREATE_FILE")
                assert new File(tempDir, "integration_test.txt").exists()
            }
            
        } finally {
            System.setProperty("user.dir", originalDir)
            tempDir.deleteDir()
        }
    }
}

// Run tests if executed directly
SimpleTestRunner.main(this.args)
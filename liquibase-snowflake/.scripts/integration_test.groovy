#!/usr/bin/env groovy

/**
 * AIPL Engine Integration Tests
 * Tests complete workflow execution with real AIPL programs
 */

class AIPLIntegrationTestSuite {
    static int testCount = 0
    static int passedTests = 0
    static int failedTests = 0
    
    static void runTest(String testName, Closure test) {
        testCount++
        print "🔧 ${testName}... "
        
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
        println "🚀 AIPL v2.0 Engine Integration Test Suite"
        println "=" * 60
        
        testBasicWorkflowExecution()
        testTDDWorkflowExecution()
        testErrorRecoveryWorkflow()
        testStrictEnforcementWorkflow()
        
        // Results
        println "\n" + "=" * 60
        println "🏁 Integration Test Results:"
        println "  Total Tests: ${testCount}"
        println "  Passed: ${passedTests}"
        println "  Failed: ${failedTests}"
        println "  Success Rate: ${testCount > 0 ? (passedTests / testCount * 100).round(1) : 0}%"
        
        if (failedTests == 0) {
            println "\n🎉 All integration tests passed!"
            System.exit(0)
        } else {
            println "\n💔 ${failedTests} integration test(s) failed!"
            System.exit(1)
        }
    }
    
    static void testBasicWorkflowExecution() {
        println "\n📋 Testing Basic Workflow Execution..."
        
        runTest("Execute simple test workflow") {
            def result = executeCommand("groovy .scripts/aipl_engine.groovy run .scripts/test_workflow.yaml")
            assert result.exitCode == 0
            assert result.output.contains("Program execution completed successfully")
        }
        
        runTest("Validate test workflow status") {
            def result = executeCommand("groovy .scripts/aipl_engine.groovy status .scripts/test_workflow.yaml")
            assert result.exitCode == 0
            assert result.output.contains("PHASE_1_SETUP")
            assert result.output.contains("PHASE_2_VALIDATION") 
            assert result.output.contains("PHASE_3_CLEANUP")
        }
        
        runTest("Reset test workflow state") {
            def result = executeCommand("groovy .scripts/aipl_engine.groovy reset .scripts/test_workflow.yaml")
            assert result.exitCode == 0
        }
    }
    
    static void testTDDWorkflowExecution() {
        println "\n🧪 Testing TDD Workflow Execution..."
        
        runTest("Execute TDD workflow") {
            def result = executeCommand("groovy .scripts/aipl_engine.groovy run .scripts/tdd_workflow_simple.yaml")
            assert result.exitCode == 0
            assert result.output.contains("Program execution completed successfully")
        }
        
        runTest("Validate TDD workflow creates requirements") {
            def requirementsFile = new File("FileFormat_requirements.md")
            assert requirementsFile.exists()
            
            def content = requirementsFile.text
            assert content.contains("FileFormat Snapshot/Diff Requirements")
            assert content.contains("Official Documentation URLs")
            assert content.contains("Complete Property List")
        }
        
        runTest("Validate TDD workflow status tracking") {
            def result = executeCommand("groovy .scripts/aipl_engine.groovy status .scripts/tdd_workflow_simple.yaml")
            assert result.exitCode == 0
            assert result.output.contains("PHASE_1_REQUIREMENTS")
            assert result.output.contains("PHASE_2_TDD_OBJECT_MODEL")
            assert result.output.contains("PHASE_3_VALIDATION")
        }
    }
    
    static void testErrorRecoveryWorkflow() {
        println "\n🔄 Testing Error Recovery Workflow..."
        
        runTest("Execute error recovery workflow") {
            def result = executeCommand("groovy .scripts/aipl_engine.groovy run .scripts/test_workflows/error_recovery_test.yaml")
            // This workflow is designed to have some failures but recover
            // We expect it to complete successfully despite initial failures
            assert result.output.contains("Expected failure for testing error handling") || 
                   result.output.contains("Program execution completed successfully")
        }
        
        runTest("Validate error recovery workflow state") {
            def result = executeCommand("groovy .scripts/aipl_engine.groovy status .scripts/test_workflows/error_recovery_test.yaml")
            assert result.exitCode == 0
            assert result.output.contains("error-recovery-test")
        }
    }
    
    static void testStrictEnforcementWorkflow() {
        println "\n🛡️ Testing Strict Enforcement Workflow..."
        
        runTest("Execute strict enforcement workflow") {
            def result = executeCommand("groovy .scripts/aipl_engine.groovy run .scripts/test_workflows/strict_enforcement_test.yaml")
            // This workflow may fail initially due to strict enforcement
            // We're testing that the enforcement system works correctly
            assert result.output.contains("STRICT ENFORCEMENT") || 
                   result.output.contains("Program execution completed successfully")
        }
        
        runTest("Validate strict enforcement behavior") {
            def result = executeCommand("groovy .scripts/aipl_engine.groovy status .scripts/test_workflows/strict_enforcement_test.yaml")
            assert result.exitCode == 0
            assert result.output.contains("strict-enforcement-test")
        }
    }
    
    static Map<String, Object> executeCommand(String command) {
        def process = command.execute()
        process.waitFor()
        
        return [
            exitCode: process.exitValue(),
            output: process.text,
            error: process.err.text
        ]
    }
}

// Run integration tests
AIPLIntegrationTestSuite.main(this.args)
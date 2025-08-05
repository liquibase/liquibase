#!/usr/bin/env groovy

/**
 * TDD Workflow Main Script - Groovy Implementation
 * 
 * Usage:
 *   groovy tdd_workflow.groovy init <ObjectType> [scenario]
 *   groovy tdd_workflow.groovy status
 *   groovy tdd_workflow.groovy next
 *   groovy tdd_workflow.groovy cycle <name> <class> <method>
 *   groovy tdd_workflow.groovy complete
 *   groovy tdd_workflow.groovy reset
 */

// Add the groovy directory to classpath so we can import our classes
def scriptDir = new File(getClass().protectionDomain.codeSource.location.path).parent
def groovyDir = new File(scriptDir, 'groovy')
this.class.classLoader.rootLoader.addURL(groovyDir.toURI().toURL())

// Import our classes
evaluate(new File(groovyDir, 'TDDState.groovy'))
evaluate(new File(groovyDir, 'ValidationService.groovy'))  
evaluate(new File(groovyDir, 'TemplateService.groovy'))
evaluate(new File(groovyDir, 'TDDWorkflow.groovy'))

// Main execution
def main(String[] args) {
    def workflow = new TDDWorkflow()
    
    if (args.length == 0) {
        showUsage()
        return
    }
    
    def command = args[0]
    def success = false
    
    try {
        switch (command) {
            case 'init':
                if (args.length < 2) {
                    println "❌ Usage: init <ObjectType> [scenario]"
                    return
                }
                def objectType = args[1]
                def scenario = args.length > 2 ? args[2] : 'NEW_OBJECT'
                success = workflow.init(objectType, scenario)
                break
                
            case 'status':
                workflow.status()
                success = true
                break
                
            case 'next':
                success = workflow.next()
                break
                
            case 'cycle':
                if (args.length < 4) {
                    println "❌ Usage: cycle <name> <class> <method>"
                    return
                }
                def cycleName = args[1]
                def testClass = args[2] 
                def testMethod = args[3]
                success = workflow.cycle(cycleName, testClass, testMethod)
                break
                
            case 'complete':
                success = workflow.complete()
                break
                
            case 'reset':
                success = workflow.reset()
                break
                
            default:
                println "❌ Unknown command: ${command}"
                showUsage()
                return
        }
        
        // Exit with appropriate code
        if (!success) {
            System.exit(1)
        }
        
    } catch (Exception e) {
        println "❌ Error executing command: ${e.message}"
        e.printStackTrace()
        System.exit(1)
    }
}

def showUsage() {
    println """
TDD Workflow Orchestration System (Groovy Implementation)

COMMANDS:
  init <ObjectType> [scenario]  - Initialize workflow for object type
  status                        - Show current workflow state
  next                          - Execute next phase
  cycle <name> <class> <method> - Execute TDD micro-cycle
  complete                      - Complete current phase
  reset                         - Reset workflow (clears all progress)

EXAMPLES:
  groovy tdd_workflow.groovy init FileFormat NEW_OBJECT
  groovy tdd_workflow.groovy status
  groovy tdd_workflow.groovy next
  groovy tdd_workflow.groovy cycle identity_check FileFormatTest testEqualsContract
  groovy tdd_workflow.groovy complete

WORKFLOW PHASES:
  1. requirements_research  - WebFetch documentation and create requirements
  2. tdd_object_model      - TDD micro-cycles for object model
  3. snapshot_generator    - TDD micro-cycles for snapshot generator
  4. diff_comparator       - TDD micro-cycles for diff comparator
  5. integration           - Service registration and integration tests

SCENARIOS:
  NEW_OBJECT              - Complete new object implementation
  ENHANCE_EXISTING        - Add properties to existing object
  COMPLETE_INCOMPLETE     - Finish partial implementation
  FIX_BUGS               - Systematic bug fixing
  OPTIMIZE_PERFORMANCE   - Performance improvements
"""
}

// Execute if running as script
if (args) {
    main(args)
}
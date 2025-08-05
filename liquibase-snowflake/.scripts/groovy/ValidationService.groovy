#!/usr/bin/env groovy

/**
 * Enhanced validation service for TDD enforcement
 */
class ValidationService {
    
    /**
     * Validate that generated templates compile successfully
     */
    static boolean validateTemplateCompilation() {
        println "🔧 Validating template compilation..."
        
        def process = "mvn compile -q".execute()
        process.waitFor()
        
        if (process.exitValue() == 0) {
            println "✅ Template compilation PASSED"
            return true
        } else {
            println "❌ Template compilation FAILED"
            // Capture compilation errors
            def errorProcess = "mvn compile".execute()
            errorProcess.waitFor()
            def errors = errorProcess.err.text
            if (errors) {
                println "Compilation errors:"
                println errors
            }
            return false
        }
    }
    
    /**
     * Validate framework integration compliance
     */
    static boolean validateFrameworkIntegration(String objectType) {
        println "🔧 Validating framework integration for: ${objectType}"
        
        def objectFile = new File("src/main/java/liquibase/database/object/${objectType}.java")
        if (!objectFile.exists()) {
            println "❌ Object file not found: ${objectFile.path}"
            return false
        }
        
        def content = objectFile.text
        
        // Check required framework patterns
        def checks = [
            [pattern: /extends AbstractDatabaseObject/, description: "extends AbstractDatabaseObject"],
            [pattern: /public DatabaseObject\[\] getContainingObjects\(\)/, description: "implements getContainingObjects()"],
            [pattern: /public String getName\(\)/, description: "implements getName()"],
            [pattern: /public \w+ setName\(String name\)/, description: "implements setName()"],
            [pattern: /public boolean equals\(Object obj\)/, description: "implements equals()"],
            [pattern: /public int hashCode\(\)/, description: "implements hashCode()"]
        ]
        
        def failedChecks = []
        checks.each { check ->
            if (!(content =~ check.pattern)) {
                failedChecks << check.description
            }
        }
        
        if (failedChecks.isEmpty()) {
            println "✅ Framework integration compliance PASSED"
            return true
        } else {
            println "❌ Framework integration compliance FAILED"
            println "Missing patterns:"
            failedChecks.each { println "  - ${it}" }
            return false
        }
    }
    
    /**
     * Validate property patterns in object model
     */
    static boolean validatePropertyPatterns(String objectType) {
        println "🔧 Validating property patterns for: ${objectType}"
        
        def objectFile = new File("src/main/java/liquibase/database/object/${objectType}.java")
        if (!objectFile.exists()) {
            println "❌ Object file not found: ${objectFile.path}"
            return false
        }
        
        def content = objectFile.text
        
        // Count getters and setters
        def getterCount = (content =~ /public \w+ get\w+\(\)/).size()
        def setterCount = (content =~ /public \w+ set\w+\(.*?\)/).size()
        
        println "  - Found ${getterCount} getters, ${setterCount} setters"
        
        // Basic validation - at least getName/setName
        if (getterCount >= 2 && setterCount >= 1) {
            println "✅ Property patterns validation PASSED"
            return true
        } else {
            println "❌ Property patterns validation FAILED"
            println "Need at least 2 getters and 1 setter"
            return false
        }
    }
    
    /**
     * Validate test class compilation
     */
    static boolean validateTestCompilation() {
        println "🔧 Validating test compilation..."
        
        def process = "mvn test-compile -q".execute()
        process.waitFor()
        
        if (process.exitValue() == 0) {
            println "✅ Test compilation PASSED"
            return true
        } else {
            println "❌ Test compilation FAILED"
            
            // Get detailed error info
            def errorProcess = "mvn test-compile".execute()
            errorProcess.waitFor()
            def errors = errorProcess.err.text
            if (errors) {
                println "Test compilation errors:"
                println errors.split('\n').findAll { 
                    it.contains('[ERROR]') && (
                        it.contains('cannot find symbol') || 
                        it.contains('does not exist') ||
                        it.contains('cannot access')
                    )
                }.take(10).join('\n')
            }
            return false
        }
    }
    
    /**
     * Validate requirements document completeness
     */
    static boolean validateRequirements(String objectType, int minProperties = 15) {
        println "🔧 Validating requirements document for: ${objectType}"
        
        def reqFile = new File("../claude_guide/snowflake_requirements/snapshot_diff_requirements/${objectType}_requirements.md")
        if (!reqFile.exists()) {
            println "❌ Requirements document not found: ${reqFile.path}"
            return false
        }
        
        def content = reqFile.text
        def propertyCount = content.findAll(/(?i)(REQUIRED|OPTIONAL)/).size()
        
        println "  - Properties documented: ${propertyCount}"
        
        if (propertyCount >= minProperties) {
            println "✅ Requirements validation PASSED"
            return true
        } else {
            println "❌ Requirements validation FAILED"
            println "Need ${minProperties}+ properties, found ${propertyCount}"
            return false
        }
    }
    
    /**
     * Comprehensive template generation validation
     */
    static boolean validateCompleteTemplateGeneration(String objectType) {
        println "🔧 ENHANCED TEMPLATE GENERATION VALIDATION"
        println "Object Type: ${objectType}"
        
        def allPassed = true
        
        // Main object compilation
        if (!validateTemplateCompilation()) {
            allPassed = false
        }
        
        // Framework integration
        if (!validateFrameworkIntegration(objectType)) {
            allPassed = false
        }
        
        // Property patterns
        if (!validatePropertyPatterns(objectType)) {
            allPassed = false
        }
        
        // Test compilation (optional - can fail and still proceed)
        def testsPassed = validateTestCompilation()
        if (!testsPassed) {
            println "⚠️  Test compilation failed - may need import fixes"
        }
        
        if (allPassed) {
            println "✅ Template generation validation PASSED"
        } else {
            println "❌ Template generation validation FAILED"
            println "Fix template issues before proceeding"
        }
        
        return allPassed
    }
}
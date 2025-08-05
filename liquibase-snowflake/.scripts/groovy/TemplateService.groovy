#!/usr/bin/env groovy

import groovy.text.SimpleTemplateEngine

/**
 * Template generation and substitution service
 */
class TemplateService {
    
    private static final Map<String, String> TEMPLATE_MAPPINGS = [
        'object_model': 'src/main/java/liquibase/database/object/${objectType}.java',
        'object_test': 'src/test/java/liquibase/ext/snowflake/database/${objectType}Test.java',
        'snapshot_generator': 'src/main/java/liquibase/snapshot/jvm/${objectType}SnapshotGeneratorSnowflake.java',
        'snapshot_test': 'src/test/java/liquibase/ext/snowflake/snapshot/${objectType}SnapshotGeneratorTest.java',
        'comparator': 'src/main/java/liquibase/diff/output/${objectType}Comparator.java',
        'comparator_test': 'src/test/java/liquibase/ext/snowflake/diff/compare/${objectType}ComparatorTest.java',
        'requirements': '../claude_guide/snowflake_requirements/snapshot_diff_requirements/${objectType}_requirements.md'
    ]
    
    /**
     * Generate all template files for an object type
     */
    static boolean generateAllFiles(String objectType) {
        println "Generating complete TDD file set for: ${objectType}"
        
        try {
            // Create required directories
            createDirectories()
            
            // Generate each template
            def templateDir = new File('.templates')
            def allSuccess = true
            
            TEMPLATE_MAPPINGS.each { templateName, outputPath ->
                def templateFile = new File(templateDir, "${templateName}_template.java")
                if (!templateFile.exists()) {
                    templateFile = new File(templateDir, "${templateName}_template.md")
                }
                
                if (templateFile.exists()) {
                    def resolvedPath = outputPath.replace('${objectType}', objectType)
                    if (generateFromTemplate(templateFile, resolvedPath, objectType)) {
                        println "✅ Template substitution completed: ${resolvedPath}"
                    } else {
                        println "❌ Template substitution failed: ${resolvedPath}"
                        allSuccess = false
                    }
                } else {
                    println "⚠️  Template not found: ${templateFile.path}"
                }
            }
            
            if (allSuccess) {
                println "✅ All files generated for ${objectType}"
                println "✅ Ready for TDD micro-cycle implementation"
            }
            
            return allSuccess
            
        } catch (Exception e) {
            println "❌ Template generation failed: ${e.message}"
            e.printStackTrace()
            return false
        }
    }
    
    /**
     * Generate file from template with substitutions
     */
    static boolean generateFromTemplate(File templateFile, String outputPath, String objectType) {
        try {
            // Ensure output directory exists
            def outputFile = new File(outputPath)
            outputFile.parentFile.mkdirs()
            
            // Read template content
            def templateContent = templateFile.text
            
            // Perform substitutions
            def substitutions = [
                'ObjectType': objectType,
                'objectType': objectType.toLowerCase(),
                'OBJECT_TYPE_UPPER': objectType.toUpperCase()
            ]
            
            def result = templateContent
            substitutions.each { key, value ->
                result = result.replaceAll(/\$\{${key}\}/, value)
            }
            
            // Handle property placeholders with proper syntax
            result = result.replaceAll(/\$\{PropertyDeclarations\}/, '// Properties added via TDD micro-cycles')
            result = result.replaceAll(/\$\{PropertyMethods\}/, '// Property methods added via TDD micro-cycles')
            result = result.replaceAll(/\$\{PropertyEqualsChecks\}/, ';\n               // Property equals checks added via TDD micro-cycles')
            result = result.replaceAll(/\$\{PropertyHashFields\}/, ';\n        // Property hash fields added via TDD micro-cycles')
            result = result.replaceAll(/\$\{PropertyToStringFields\}/, '// Property toString fields added via TDD micro-cycles')
            
            // Handle test placeholders
            result = result.replaceAll(/\$\{PropertyPositiveTests\}/, '// Property positive tests added via TDD micro-cycles')
            result = result.replaceAll(/\$\{PropertyNegativeTests\}/, '// Property negative tests added via TDD micro-cycles')
            result = result.replaceAll(/\$\{PropertyBoundaryTests\}/, '// Property boundary tests added via TDD micro-cycles')
            result = result.replaceAll(/\$\{PropertyEdgeTests\}/, '// Property edge tests added via TDD micro-cycles')
            
            // Handle snapshot/comparator placeholders
            result = result.replaceAll(/\$\{SnapshotQueryImplementation\}/, '// Query implementation added via TDD micro-cycles')
            result = result.replaceAll(/\$\{AddToSnapshotImplementation\}/, '// Add to snapshot logic added via TDD micro-cycles')
            result = result.replaceAll(/\$\{SnapshotHelperMethods\}/, '// Helper methods added via TDD micro-cycles')
            result = result.replaceAll(/\$\{IdentityComparisonImplementation\}/, '// Identity comparison added via TDD micro-cycles')
            result = result.replaceAll(/\$\{PropertyComparisonImplementation\}/, '// Property comparison added via TDD micro-cycles')
            result = result.replaceAll(/\$\{ComparisonHelperMethods\}/, '// Comparison helper methods added via TDD micro-cycles')
            
            // Write result
            outputFile.text = result
            
            return true
            
        } catch (Exception e) {
            println "❌ Failed to generate ${outputPath}: ${e.message}"
            return false
        }
    }
    
    /**
     * Create required directory structure
     */
    private static void createDirectories() {
        def directories = [
            'src/main/java/liquibase/database/object',
            'src/test/java/liquibase/ext/snowflake/database', 
            'src/main/java/liquibase/snapshot/jvm',
            'src/test/java/liquibase/ext/snowflake/snapshot',
            'src/main/java/liquibase/diff/output',
            'src/test/java/liquibase/ext/snowflake/diff/compare',
            '../claude_guide/snowflake_requirements/snapshot_diff_requirements'
        ]
        
        directories.each { dir ->
            new File(dir).mkdirs()
        }
    }
    
    /**
     * Check if object model already exists and is working
     */
    static boolean objectModelExists(String objectType) {
        def objectFile = new File("src/main/java/liquibase/database/object/${objectType}.java")
        if (!objectFile.exists()) {
            return false
        }
        
        // Quick compilation check
        def process = "mvn compile -q".execute()
        process.waitFor()
        return process.exitValue() == 0
    }
}
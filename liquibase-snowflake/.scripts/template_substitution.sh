#!/bin/bash
# Universal template substitution system for TDD enforcement
# Generates all files for any Snowflake object type

set -e

# Template substitution function
substitute_template() {
    local template_file="$1"
    local output_file="$2"
    local object_type="$3"
    
    echo "Generating $output_file from template"
    
    # Convert ObjectType to various formats
    local object_type_lower=$(echo "$object_type" | tr '[:upper:]' '[:lower:]')
    local object_type_upper=$(echo "$object_type" | tr '[:lower:]' '[:upper:]')
    
    # Copy template and perform substitutions
    cp "$template_file" "$output_file"
    
    # Basic substitutions
    sed -i '' "s/\${ObjectType}/$object_type/g" "$output_file"
    sed -i '' "s/\${objectType}/$object_type_lower/g" "$output_file"
    sed -i '' "s/\${OBJECT_TYPE_UPPER}/$object_type_upper/g" "$output_file"
    
    # Property placeholders (filled during TDD micro-cycles) - fix syntax
    sed -i '' "s/\${PropertyDeclarations}/\/\/ Properties added via TDD micro-cycles/g" "$output_file"
    sed -i '' "s/\${PropertyMethods}/\/\/ Property methods added via TDD micro-cycles/g" "$output_file"
    sed -i '' "s/\${PropertyEqualsChecks}/;\n               \/\/ Property equals checks added via TDD micro-cycles/g" "$output_file"
    sed -i '' "s/\${PropertyHashFields}/\);\n        \/\/ Property hash fields added via TDD micro-cycles/g" "$output_file"
    sed -i '' "s/\${PropertyToStringFields}/\/\/ Property toString fields added via TDD micro-cycles/g" "$output_file"
    
    # Test placeholders  
    sed -i '' "s/\${PropertyPositiveTests}/\/\/ Property positive tests added via TDD micro-cycles/g" "$output_file"
    sed -i '' "s/\${PropertyNegativeTests}/\/\/ Property negative tests added via TDD micro-cycles/g" "$output_file"
    sed -i '' "s/\${PropertyBoundaryTests}/\/\/ Property boundary tests added via TDD micro-cycles/g" "$output_file"
    sed -i '' "s/\${PropertyEdgeTests}/\/\/ Property edge tests added via TDD micro-cycles/g" "$output_file"
    
    # Snapshot generator placeholders
    sed -i '' "s/\${SnapshotQueryImplementation}/\/\/ Query implementation added via TDD micro-cycles/g" "$output_file"
    sed -i '' "s/\${AddToSnapshotImplementation}/\/\/ Add to snapshot logic added via TDD micro-cycles/g" "$output_file"
    sed -i '' "s/\${SnapshotHelperMethods}/\/\/ Helper methods added via TDD micro-cycles/g" "$output_file"
    sed -i '' "s/\${SnapshotPositiveTests}/\/\/ Snapshot positive tests added via TDD micro-cycles/g" "$output_file"
    sed -i '' "s/\${SnapshotNegativeTests}/\/\/ Snapshot negative tests added via TDD micro-cycles/g" "$output_file"
    sed -i '' "s/\${SnapshotBoundaryTests}/\/\/ Snapshot boundary tests added via TDD micro-cycles/g" "$output_file"
    sed -i '' "s/\${SnapshotEdgeTests}/\/\/ Snapshot edge tests added via TDD micro-cycles/g" "$output_file"
    sed -i '' "s/\${SnapshotTestHelpers}/\/\/ Test helper methods added via TDD micro-cycles/g" "$output_file"
    
    # Comparator placeholders
    sed -i '' "s/\${IdentityComparisonImplementation}/\/\/ Identity comparison added via TDD micro-cycles/g" "$output_file"
    sed -i '' "s/\${PropertyComparisonImplementation}/\/\/ Property comparison added via TDD micro-cycles/g" "$output_file"
    sed -i '' "s/\${ComparisonHelperMethods}/\/\/ Comparison helper methods added via TDD micro-cycles/g" "$output_file"
    sed -i '' "s/\${ComparisonPositiveTests}/\/\/ Comparison positive tests added via TDD micro-cycles/g" "$output_file"
    sed -i '' "s/\${ComparisonNegativeTests}/\/\/ Comparison negative tests added via TDD micro-cycles/g" "$output_file"
    sed -i '' "s/\${ComparisonBoundaryTests}/\/\/ Comparison boundary tests added via TDD micro-cycles/g" "$output_file"
    sed -i '' "s/\${ComparisonEdgeTests}/\/\/ Comparison edge tests added via TDD micro-cycles/g" "$output_file"
    sed -i '' "s/\${ComparisonTestHelpers}/\/\/ Comparison test helpers added via TDD micro-cycles/g" "$output_file"
    
    echo "✅ Template substitution completed: $output_file"
}

# Generate all files for object type
generate_all_files() {
    local object_type="$1"
    
    echo "Generating complete TDD file set for: $object_type"
    
    # Create directories if needed - following established Snowflake patterns
    mkdir -p "src/main/java/liquibase/database/object"
    mkdir -p "src/test/java/liquibase/ext/snowflake/database"
    mkdir -p "src/main/java/liquibase/snapshot/jvm"
    mkdir -p "src/test/java/liquibase/ext/snowflake/snapshot"
    mkdir -p "src/main/java/liquibase/diff/output"
    mkdir -p "src/test/java/liquibase/ext/snowflake/diff/compare"
    mkdir -p "../claude_guide/snowflake_requirements/snapshot_diff_requirements"
    
    # Generate object model - following established pattern
    substitute_template \
        ".templates/object_model_template.java" \
        "src/main/java/liquibase/database/object/${object_type}.java" \
        "$object_type"
    
    # Generate object tests
    substitute_template \
        ".templates/object_test_template.java" \
        "src/test/java/liquibase/ext/snowflake/database/${object_type}Test.java" \
        "$object_type"
    
    # Generate snapshot generator - following established pattern
    substitute_template \
        ".templates/snapshot_generator_template.java" \
        "src/main/java/liquibase/snapshot/jvm/${object_type}SnapshotGeneratorSnowflake.java" \
        "$object_type"
    
    # Generate snapshot generator tests
    substitute_template \
        ".templates/snapshot_generator_test_template.java" \
        "src/test/java/liquibase/ext/snowflake/snapshot/${object_type}SnapshotGeneratorTest.java" \
        "$object_type"
    
    # Generate diff comparator - following established pattern
    substitute_template \
        ".templates/diff_comparator_template.java" \
        "src/main/java/liquibase/diff/output/${object_type}Comparator.java" \
        "$object_type"
    
    # Generate diff comparator tests
    substitute_template \
        ".templates/diff_comparator_test_template.java" \
        "src/test/java/liquibase/ext/snowflake/diff/compare/${object_type}ComparatorTest.java" \
        "$object_type"
    
    # Generate requirements document
    substitute_template \
        ".templates/requirements_template.md" \
        "../claude_guide/snowflake_requirements/snapshot_diff_requirements/${object_type}_requirements.md" \
        "$object_type"
    
    echo ""
    echo "✅ All files generated for $object_type"
    echo "✅ Ready for TDD micro-cycle implementation"
    echo ""
    echo "Next steps:"
    echo "1. Complete requirements research (WebFetch documentation)"
    echo "2. Execute TDD micro-cycles for object model"
    echo "3. Execute TDD micro-cycles for snapshot generator"
    echo "4. Execute TDD micro-cycles for diff comparator"
    echo "5. Register services and run integration tests"
}

# Command line interface
case "${1:-help}" in
    "generate")
        if [ -z "$2" ]; then
            echo "Usage: $0 generate <ObjectType>"
            echo "Example: $0 generate FileFormat"
            exit 1
        fi
        generate_all_files "$2"
        ;;
    "substitute")
        if [ $# -ne 4 ]; then
            echo "Usage: $0 substitute <template_file> <output_file> <ObjectType>"
            exit 1
        fi
        substitute_template "$2" "$3" "$4"
        ;;
    *)
        echo "Universal Template Substitution System"
        echo ""
        echo "Usage:"
        echo "  $0 generate <ObjectType>     - Generate all files for object type"
        echo "  $0 substitute <template> <output> <ObjectType> - Single template substitution"
        echo ""
        echo "Examples:"
        echo "  $0 generate FileFormat"
        echo "  $0 generate Warehouse"
        echo "  $0 generate Stream"
        echo ""
        echo "Generated files:"
        echo "  - Object model and tests"
        echo "  - Snapshot generator and tests"
        echo "  - Diff comparator and tests"
        echo "  - Requirements document"
        ;;
esac
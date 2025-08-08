#!/bin/bash

# Script to fix common compilation errors in test files

TEST_DIR="/Users/kevinchappell/Documents/GitHub/liquibase/liquibase-snowflake/src/test"

echo "Fixing compilation errors in test files..."

# Find all Java test files and fix common patterns
find "$TEST_DIR" -name "*.java" | while read file; do
    echo "Processing: $file"
    
    # Fix pattern: assertTrue(..., \n    }
    # Add default error message before closing
    sed -i '' '/assertTrue.*,$/,/^[ ]*}$/{
        s/assertTrue(\(.*\),$/assertTrue(\1,/
        /^[ ]*}$/{
            i\
                     "Assertion should pass");
        }
    }' "$file"
    
    # Fix pattern: assertEquals(..., \n    }
    # Add default error message before closing
    sed -i '' '/assertEquals.*,$/,/^[ ]*}$/{
        s/assertEquals(\(.*\),$/assertEquals(\1,/
        /^[ ]*}$/{
            i\
                     "Values should be equal");
        }
    }' "$file"
    
    # Fix pattern: assertNotNull(..., \n    }
    sed -i '' '/assertNotNull.*,$/,/^[ ]*}$/{
        s/assertNotNull(\(.*\),$/assertNotNull(\1,/
        /^[ ]*}$/{
            i\
                     "Value should not be null");
        }
    }' "$file"
    
    # Fix pattern: assertFalse(..., \n    }
    sed -i '' '/assertFalse.*,$/,/^[ ]*}$/{
        s/assertFalse(\(.*\),$/assertFalse(\1,/
        /^[ ]*}$/{
            i\
                     "Assertion should be false");
        }
    }' "$file"
    
done

echo "Compilation error fixes completed!"
#!/bin/bash

# Bulk fix for common compilation error patterns
TEST_DIR="/Users/kevinchappell/Documents/GitHub/liquibase/liquibase-snowflake/src/test"

echo "Bulk fixing common compilation error patterns..."

# Find files with broken assertions and fix them
grep -r -l "assertEquals.*,[ ]*$" "$TEST_DIR" | while read file; do
    echo "Fixing assertEquals in: $file"
    perl -i -pe 's/(assertEquals\([^,]+,\s*[^,]+),\s*$/$1, "Values should be equal");/g' "$file"
done

grep -r -l "assertTrue.*,[ ]*$" "$TEST_DIR" | while read file; do
    echo "Fixing assertTrue in: $file"
    perl -i -pe 's/(assertTrue\([^,]+),\s*$/$1, "Assertion should be true");/g' "$file"
done

grep -r -l "assertFalse.*,[ ]*$" "$TEST_DIR" | while read file; do
    echo "Fixing assertFalse in: $file"
    perl -i -pe 's/(assertFalse\([^,]+),\s*$/$1, "Assertion should be false");/g' "$file"
done

grep -r -l "assertNotNull.*,[ ]*$" "$TEST_DIR" | while read file; do
    echo "Fixing assertNotNull in: $file"
    perl -i -pe 's/(assertNotNull\([^,]+),\s*$/$1, "Value should not be null");/g' "$file"
done

grep -r -l "assertNull.*,[ ]*$" "$TEST_DIR" | while read file; do
    echo "Fixing assertNull in: $file"
    perl -i -pe 's/(assertNull\([^,]+),\s*$/$1, "Value should be null");/g' "$file"
done

echo "Bulk fixes completed!"
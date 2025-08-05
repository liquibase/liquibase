#!/usr/bin/env groovy

import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import java.nio.file.Files
import java.nio.file.Paths

/**
 * Manages TDD workflow state with JSON persistence
 */
class TDDState {
    String objectType
    String scenario
    String currentPhase
    int microCycleCount = 0
    List<String> completedCheckpoints = []
    Map<String, Object> metadata = [:]
    
    private static final String STATE_FILE = '.process_state/tdd_state.json'
    private static final String STATE_DIR = '.process_state'
    
    /**
     * Save current state to JSON file
     */
    void save() {
        // Ensure directory exists
        def stateDir = new File(STATE_DIR)
        if (!stateDir.exists()) {
            stateDir.mkdirs()
        }
        
        def json = new JsonBuilder()
        json {
            objectType this.objectType
            scenario this.scenario
            currentPhase this.currentPhase
            microCycleCount this.microCycleCount
            completedCheckpoints this.completedCheckpoints
            metadata this.metadata
            lastUpdated new Date().toString()
        }
        
        new File(STATE_FILE).text = json.toPrettyString()
    }
    
    /**
     * Load state from JSON file
     */
    static TDDState load() {
        def stateFile = new File(STATE_FILE)
        if (!stateFile.exists()) {
            return new TDDState()
        }
        
        def jsonSlurper = new JsonSlurper()
        def data = jsonSlurper.parse(stateFile)
        
        def state = new TDDState()
        state.objectType = data.objectType
        state.scenario = data.scenario
        state.currentPhase = data.currentPhase
        state.microCycleCount = data.microCycleCount ?: 0
        state.completedCheckpoints = data.completedCheckpoints ?: []
        state.metadata = data.metadata ?: [:]
        
        return state
    }
    
    /**
     * Reset state for new workflow
     */
    void reset() {
        this.objectType = null
        this.scenario = null
        this.currentPhase = null
        this.microCycleCount = 0
        this.completedCheckpoints.clear()
        this.metadata.clear()
        save()
    }
    
    /**
     * Initialize new workflow
     */
    void initialize(String objectType, String scenario) {
        this.objectType = objectType
        this.scenario = scenario
        this.currentPhase = 'requirements_research'
        this.microCycleCount = 0
        this.completedCheckpoints.clear()
        this.metadata.clear()
        save()
    }
    
    /**
     * Complete current phase and advance to next
     */
    void completePhase() {
        if (currentPhase) {
            completedCheckpoints << "${currentPhase}_complete"
            advancePhase()
            save()
        }
    }
    
    /**
     * Advance to next phase based on scenario
     */
    private void advancePhase() {
        def phaseSequence = [
            'requirements_research',
            'tdd_object_model', 
            'snapshot_generator',
            'diff_comparator',
            'integration'
        ]
        
        def currentIndex = phaseSequence.indexOf(currentPhase)
        if (currentIndex >= 0 && currentIndex < phaseSequence.size() - 1) {
            currentPhase = phaseSequence[currentIndex + 1]
        }
    }
    
    /**
     * Increment micro-cycle count
     */
    void incrementMicroCycle() {
        microCycleCount++
        save()
    }
    
    /**
     * Get current status summary
     */
    String getStatusSummary() {
        return """
=== CURRENT WORKFLOW STATE ===
Object Type: ${objectType ?: 'Not set'}
Scenario: ${scenario ?: 'Not set'}
Current Phase: ${currentPhase ?: 'Not set'}
Micro-Cycle Count: ${microCycleCount}

=== COMPLETED CHECKPOINTS ===
${completedCheckpoints.isEmpty() ? '  (none)' : completedCheckpoints.collect { "  ✅ $it" }.join('\n')}
""".trim()
    }
}
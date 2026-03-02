package com.dhis2sync.model;

/**
 * Communicates the per-item outcome from {@code processRole()} / {@code processUser()}
 * private methods back to the calling loop for counter updates.
 *
 * <p>Usage example:
 * <pre>{@code
 * SyncItemOutcome outcome = processRole(role);
 * switch (outcome) {
 *     case SUCCESS -> success++;
 *     case SKIPPED -> skipped++;
 *     case FAILED  -> failed++;
 * }
 * }</pre>
 */
public enum SyncItemOutcome {
    /** Item was successfully created in DHIS2. */
    SUCCESS,
    /** Item already exists in DHIS2; no action taken. */
    SKIPPED,
    /** An error occurred during processing. */
    FAILED
}
package com.dhis2sync.model;

/**
 * Immutable summary of a sync operation's outcome.
 *
 * <p>Invariant: {@code total == success + skipped + failed} must hold
 * for any correctly constructed instance.</p>
 *
 * <p>Usage:
 * <ul>
 *   <li>{@code RoleSyncService.syncRoles()} and {@code UserSyncService.syncUsers()}
 *       each return a {@code SyncResult}.</li>
 *   <li>{@code SyncOrchestrator.executeFullSync()} calls {@link #merge(SyncResult, SyncResult)}
 *       to combine role and user results into an overall summary.</li>
 * </ul>
 */
public class SyncResult {

    private final int total;
    private final int success;
    private final int skipped;
    private final int failed;

    /**
     * All-args constructor.
     *
     * @param total   total number of items processed
     * @param success number of items successfully created in DHIS2
     * @param skipped number of items already existing in DHIS2 (deduplicated)
     * @param failed  number of items that encountered errors
     */
    public SyncResult(int total, int success, int skipped, int failed) {
        this.total = total;
        this.success = success;
        this.skipped = skipped;
        this.failed = failed;
    }

    public int getTotal() {
        return total;
    }

    public int getSuccess() {
        return success;
    }

    public int getSkipped() {
        return skipped;
    }

    public int getFailed() {
        return failed;
    }

    /**
     * Merges two {@code SyncResult} instances by summing each corresponding field.
     *
     * @param a first result (guaranteed non-null by caller)
     * @param b second result (guaranteed non-null by caller)
     * @return a new {@code SyncResult} with summed totals
     */
    public static SyncResult merge(SyncResult a, SyncResult b) {
        return new SyncResult(
                a.total + b.total,
                a.success + b.success,
                a.skipped + b.skipped,
                a.failed + b.failed
        );
    }

    @Override
    public String toString() {
        return "SyncResult{total=" + total + ", success=" + success
                + ", skipped=" + skipped + ", failed=" + failed + "}";
    }
}
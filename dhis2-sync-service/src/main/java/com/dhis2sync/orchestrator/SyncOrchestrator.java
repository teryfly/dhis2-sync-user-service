package com.dhis2sync.orchestrator;

import com.dhis2sync.model.SyncResult;
import com.dhis2sync.service.RoleSyncService;
import com.dhis2sync.service.UserSyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Orchestrates the complete synchronization workflow.
 * Executes role sync first (to ensure roles exist in DHIS2 for user references),
 * then user sync, aggregates results, and provides comprehensive logging.
 */
@Service
public class SyncOrchestrator {

    private static final Logger logger = LoggerFactory.getLogger(SyncOrchestrator.class);

    private final RoleSyncService roleSyncService;
    private final UserSyncService userSyncService;

    /**
     * Constructor injection of sync service dependencies.
     *
     * @param roleSyncService service for role synchronization
     * @param userSyncService service for user synchronization
     */
    public SyncOrchestrator(RoleSyncService roleSyncService, UserSyncService userSyncService) {
        this.roleSyncService = roleSyncService;
        this.userSyncService = userSyncService;
    }

    /**
     * Executes the complete synchronization workflow in sequence:
     * 1. Sync all roles from database to DHIS2
     * 2. Sync all users from database to DHIS2
     * 3. Aggregate results and log comprehensive summary
     *
     * <p>Sequential execution is critical: roles must be created before users
     * to ensure role UID resolution succeeds during user sync.</p>
     *
     * <p>Both sync services handle all exceptions internally and return
     * SyncResult objects. This method assumes no exceptions are thrown.</p>
     *
     * @return aggregated sync result combining role and user sync outcomes
     */
    public SyncResult executeFullSync() {
        long startTime = System.currentTimeMillis();

        logger.info("========================================");
        logger.info("开始执行完整同步任务...");
        logger.info("========================================");

        // Execute role sync first
        SyncResult roleResult = roleSyncService.syncRoles();
        logger.info("角色同步结果: {}", roleResult);

        logger.info("----------------------------------------");

        // Execute user sync second (depends on roles existing in DHIS2)
        SyncResult userResult = userSyncService.syncUsers();
        logger.info("用户同步结果: {}", userResult);

        // Aggregate results
        SyncResult totalResult = SyncResult.merge(roleResult, userResult);

        // Calculate elapsed time
        long elapsedTime = System.currentTimeMillis() - startTime;

        // Log final summary
        logger.info("========================================");
        logger.info("完整同步任务完成");
        logger.info("总体结果: {}", totalResult);
        logger.info("总耗时: {} 毫秒 ({} 秒)", elapsedTime, elapsedTime / 1000.0);
        logger.info("========================================");

        return totalResult;
    }
}
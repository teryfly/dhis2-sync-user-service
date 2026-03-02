package com.dhis2sync.scheduler;

import com.dhis2sync.model.SyncResult;
import com.dhis2sync.orchestrator.SyncOrchestrator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Scheduled trigger for the synchronization workflow.
 * Executes at the time specified by the {@code sync.cron} configuration property.
 * Provides top-level exception handling to protect the scheduling thread.
 */
@Component
public class SyncScheduler {

    private static final Logger logger = LoggerFactory.getLogger(SyncScheduler.class);

    private final SyncOrchestrator syncOrchestrator;

    /**
     * Constructor injection of the orchestrator dependency.
     *
     * @param syncOrchestrator the orchestrator that coordinates the sync workflow
     */
    public SyncScheduler(SyncOrchestrator syncOrchestrator) {
        this.syncOrchestrator = syncOrchestrator;
    }

    /**
     * Scheduled method that triggers the full synchronization workflow.
     * Executes according to the cron expression defined in {@code application.yml}
     * under the {@code sync.cron} property (default: daily at 2:00 AM).
     *
     * <p>The try-catch block protects the Spring scheduling thread from
     * unhandled exceptions. If an unexpected error occurs, it is logged
     * and the scheduler remains active for the next trigger.</p>
     */
    @Scheduled(cron = "${sync.cron}")
    public void scheduledSync() {
        logger.info("定时同步任务触发,时间: {}", LocalDateTime.now());

        try {
            SyncResult result = syncOrchestrator.executeFullSync();
            logger.info("定时同步任务执行完成,结果: {}", result);

        } catch (Exception e) {
            logger.error("定时同步任务执行失败: {}", e.getMessage(), e);
            logger.info("调度器将在下次定时触发时重试");
        }
    }
}
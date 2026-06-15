package space.controlnet.mineagent.ae.common.part;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;
import space.controlnet.mineagent.ae.core.terminal.AiTerminalData;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public final class AiTerminalPartOperationsJobLifecycleRegressionTest {
    @Test
    void task7_resolveKey_knownInvalidBlankAndUnknownItemIds_areDeterministic() {
        ResourceLocation known = AiTerminalPartOperationsJobLifecycle.resolveKey(
                "ae2:controller",
                id -> "ae2:controller".equals(id.toString())
        );
        ResourceLocation blank = AiTerminalPartOperationsJobLifecycle.resolveKey("   ", id -> true);
        ResourceLocation invalid = AiTerminalPartOperationsJobLifecycle.resolveKey("not a key", id -> true);
        ResourceLocation unknown = AiTerminalPartOperationsJobLifecycle.resolveKey("ae2:missing", id -> false);

        assertEquals("task7/resolve-key/known", ResourceLocation.tryParse("ae2:controller"), known);
        assertEquals("task7/resolve-key/blank", null, blank);
        assertEquals("task7/resolve-key/invalid", null, invalid);
        assertEquals("task7/resolve-key/unknown", null, unknown);
    }

    @Test
    void task7_selectCpu_caseInsensitiveSubstringAndUnavailableMatching_areDeterministic() {
        FakeCpu unnamed = new FakeCpu(null);
        FakeCpu alpha = new FakeCpu("Alpha CPU");
        FakeCpu beta = new FakeCpu("Builder Node");
        Set<FakeCpu> cpus = new LinkedHashSet<>(List.of(unnamed, alpha, beta));

        FakeCpu selectedAlpha = AiTerminalPartOperationsJobLifecycle.selectCpu(cpus, FakeCpu::name, "alpha");
        FakeCpu selectedBeta = AiTerminalPartOperationsJobLifecycle.selectCpu(cpus, FakeCpu::name, "builder");
        FakeCpu blank = AiTerminalPartOperationsJobLifecycle.selectCpu(cpus, FakeCpu::name, " ");
        FakeCpu unavailable = AiTerminalPartOperationsJobLifecycle.selectCpu(cpus, FakeCpu::name, "missing cpu");

        assertEquals("task7/select-cpu/alpha", alpha, selectedAlpha);
        assertEquals("task7/select-cpu/beta", beta, selectedBeta);
        assertEquals("task7/select-cpu/blank", null, blank);
        assertEquals("task7/select-cpu/unavailable", null, unavailable);
    }

    @Test
    void task7_jobStatus_refreshJob_andCancelJob_lockUnknownSubmittedDoneAndCanceledOutputs() {
        AiTerminalPartOperationsJobLifecycle<FakeLink> lifecycle = newLifecycle();

        AiTerminalData.AeJobStatus unknownStatus = lifecycle.jobStatus("job-missing");
        AiTerminalData.AeJobStatus unknownCancel = lifecycle.cancelJob("job-missing");

        assertJobStatus(
                "task7/job-status/unknown",
                unknownStatus,
                "job-missing",
                "unknown",
                List.of(),
                Optional.of("Job not found")
        );
        assertJobStatus(
                "task7/cancel-job/unknown",
                unknownCancel,
                "job-missing",
                "unknown",
                List.of(),
                Optional.of("Job not found")
        );

        assertJobStatus(
                "task7/job-status/calculating",
                lifecycle.jobStatus(lifecycle.startCalculatingJob("job-calc").jobId()),
                "job-calc",
                "calculating",
                List.of(),
                Optional.empty()
        );

        FakeLink doneLink = new FakeLink("done-link");
        List<AiTerminalData.AePlanItem> submittedMissing = List.of(new AiTerminalData.AePlanItem("ae2:controller", 2L));
        lifecycle.putJob(
                "job-done",
                lifecycle.startCalculatingJob("job-done")
                        .withPlan(false, submittedMissing)
                        .withLink(doneLink)
        );

        assertJobStatus(
                "task7/job-status/submitted",
                lifecycle.jobStatus("job-done"),
                "job-done",
                "submitted",
                submittedMissing,
                Optional.empty()
        );

        doneLink.done = true;
        assertJobStatus(
                "task7/job-status/done-refresh",
                lifecycle.jobStatus("job-done"),
                "job-done",
                "done",
                submittedMissing,
                Optional.empty()
        );

        FakeLink cancelLink = new FakeLink("cancel-link");
        lifecycle.putJob(
                "job-cancel",
                lifecycle.startCalculatingJob("job-cancel")
                        .withPlan(false, List.of())
                        .withLink(cancelLink)
        );

        AiTerminalData.AeJobStatus canceled = lifecycle.cancelJob("job-cancel");

        assertEquals("task7/cancel-job/cancel-count", 1, cancelLink.cancelInvocations);
        assertJobStatus(
                "task7/cancel-job/status",
                canceled,
                "job-cancel",
                "canceled",
                List.of(),
                Optional.empty()
        );
        assertJobStatus(
                "task7/cancel-job/status-after-refresh",
                lifecycle.jobStatus("job-cancel"),
                "job-cancel",
                "canceled",
                List.of(),
                Optional.empty()
        );
    }

    @Test
    void task7_jobStateChange_clearJobs_andMissingItemFailureMessages_areDeterministic() {
        AiTerminalPartOperationsJobLifecycle<FakeLink> lifecycle = newLifecycle();
        List<AiTerminalData.AePlanItem> missing = List.of(
                new AiTerminalData.AePlanItem("ae2:logic_processor", 1L),
                new AiTerminalData.AePlanItem("minecraft:redstone", 4L)
        );

        lifecycle.putJob("job-failed", lifecycle.startCalculatingJob("job-failed").withPlan(true, missing));

        assertJobStatus(
                "task7/job-status/failed-missing-items",
                lifecycle.jobStatus("job-failed"),
                "job-failed",
                "failed",
                missing,
                Optional.of("Missing items: ae2:logic_processor, minecraft:redstone")
        );

        FakeLink doneLink = new FakeLink("state-done");
        FakeLink canceledLink = new FakeLink("state-canceled");
        FakeLink liveLink = new FakeLink("state-live");
        lifecycle.putJob(
                "job-state-done",
                lifecycle.startCalculatingJob("job-state-done")
                        .withPlan(false, List.of())
                        .withLink(doneLink)
        );
        lifecycle.putJob(
                "job-state-canceled",
                lifecycle.startCalculatingJob("job-state-canceled")
                        .withPlan(false, List.of())
                        .withLink(canceledLink)
        );
        lifecycle.putJob(
                "job-state-live",
                lifecycle.startCalculatingJob("job-state-live")
                        .withPlan(false, List.of())
                        .withLink(liveLink)
        );

        doneLink.done = true;
        canceledLink.canceled = true;
        lifecycle.jobStateChange(doneLink);
        lifecycle.jobStateChange(canceledLink);

        assertJobStatus(
                "task7/job-state-change/done",
                lifecycle.jobStatus("job-state-done"),
                "job-state-done",
                "done",
                List.of(),
                Optional.empty()
        );
        assertJobStatus(
                "task7/job-state-change/canceled",
                lifecycle.jobStatus("job-state-canceled"),
                "job-state-canceled",
                "canceled",
                List.of(),
                Optional.empty()
        );
        assertEquals(
                "task7/requested-jobs/live-count-before-clear",
                Set.of(liveLink),
                lifecycle.getRequestedJobs()
        );

        lifecycle.clearJobs();

        assertJobStatus(
                "task7/clear-jobs/failed-job",
                lifecycle.jobStatus("job-failed"),
                "job-failed",
                "unknown",
                List.of(),
                Optional.of("Job not found")
        );
        assertJobStatus(
                "task7/clear-jobs/done-job",
                lifecycle.jobStatus("job-state-done"),
                "job-state-done",
                "unknown",
                List.of(),
                Optional.of("Job not found")
        );
        assertJobStatus(
                "task7/clear-jobs/live-job",
                lifecycle.jobStatus("job-state-live"),
                "job-state-live",
                "unknown",
                List.of(),
                Optional.of("Job not found")
        );
        assertEquals("task7/clear-jobs/requested-jobs", Set.of(), lifecycle.getRequestedJobs());
    }

    private static AiTerminalPartOperationsJobLifecycle<FakeLink> newLifecycle() {
        return new AiTerminalPartOperationsJobLifecycle<>(FakeLink::isCanceled, FakeLink::isDone, FakeLink::cancel);
    }

    private static void assertJobStatus(
            String assertionName,
            AiTerminalData.AeJobStatus actual,
            String expectedJobId,
            String expectedStatus,
            List<AiTerminalData.AePlanItem> expectedMissingItems,
            Optional<String> expectedError
    ) {
        assertEquals(assertionName + "/job-id", expectedJobId, actual.jobId());
        assertEquals(assertionName + "/status", expectedStatus, actual.status());
        assertEquals(assertionName + "/missing-items", expectedMissingItems, actual.missingItems());
        assertEquals(assertionName + "/error", expectedError, actual.error());
    }

    private static void assertEquals(String assertionName, Object expected, Object actual) {
        if ((expected == null && actual == null) || (expected != null && expected.equals(actual))) {
            return;
        }
        throw new AssertionError(assertionName + " -> expected: " + expected + ", actual: " + actual);
    }

    private record FakeCpu(String name) {
    }

    private static final class FakeLink {
        private final String id;
        private boolean canceled;
        private boolean done;
        private int cancelInvocations;

        private FakeLink(String id) {
            this.id = id;
        }

        private boolean isCanceled() {
            return canceled;
        }

        private boolean isDone() {
            return done;
        }

        private void cancel() {
            cancelInvocations++;
            canceled = true;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof FakeLink that)) {
                return false;
            }
            return id.equals(that.id);
        }

        @Override
        public int hashCode() {
            return id.hashCode();
        }
    }
}

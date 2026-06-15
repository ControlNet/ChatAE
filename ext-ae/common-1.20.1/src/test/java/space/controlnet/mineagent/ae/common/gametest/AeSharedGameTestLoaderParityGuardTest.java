package space.controlnet.mineagent.ae.common.gametest;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class AeSharedGameTestLoaderParityGuardTest {
    private static final Path FABRIC_ENTRYPOINT = Path.of(
            "ext-ae",
            "fabric-1.20.1",
            "src",
            "main",
            "java",
            "space",
            "controlnet",
            "mineagent",
            "ae",
            "fabric",
            "gametest",
            "MineAgentAeFabricGameTestEntrypoint.java"
    );
    private static final Path FORGE_BOOTSTRAP = Path.of(
            "ext-ae",
            "forge-1.20.1",
            "src",
            "main",
            "java",
            "space",
            "controlnet",
            "mineagent",
            "ae",
            "forge",
            "gametest",
            "MineAgentAeGameTestBootstrap.java"
    );
    private static final Path FORGE_GAME_TEST_DIRECTORY = Path.of(
            "ext-ae",
            "forge-1.20.1",
            "src",
            "main",
            "java",
            "space",
            "controlnet",
            "mineagent",
            "ae",
            "forge",
            "gametest"
    );
    private static final Path CRAFT_SCENARIOS = Path.of(
            "ext-ae",
            "common-1.20.1",
            "src",
            "main",
            "java",
            "space",
            "controlnet",
            "mineagent",
            "ae",
            "common",
            "gametest",
            "AeCraftLifecycleIsolationGameTestScenarios.java"
    );
    private static final Path BINDING_SCENARIOS = Path.of(
            "ext-ae",
            "common-1.20.1",
            "src",
            "main",
            "java",
            "space",
            "controlnet",
            "mineagent",
            "ae",
            "common",
            "gametest",
            "AeBindingFailureGameTestScenarios.java"
    );
    private static final Pattern RUNNABLE_SCENARIO_METHOD = Pattern.compile(
            "public\\s+static\\s+void\\s+(\\w+)\\s*\\(\\s*GameTestHelper\\s+helper\\s*,\\s*GameTestPlayerFactory\\s+playerFactory",
            Pattern.MULTILINE
    );
    private static final List<ScenarioExposure> EXPECTED_SCENARIOS = List.of(
            new ScenarioExposure(
                    "AeCraftLifecycleIsolationGameTestScenarios",
                    "craftLifecycleIsolation",
                    "craftLifecycleIsolation",
                    "MineAgentAeFabricRuntimeGameTests.craftLifecycleIsolation(helper)",
                    "AeCraftLifecycleIsolationGameTest",
                    "craftLifecycleIsolation_beginSuccessFailure_withoutCrossTerminalLeakage",
                    "AeCraftLifecycleIsolationGameTest.class",
                    "AeCraftLifecycleIsolationGameTestScenarios.craftLifecycleIsolation("
            ),
            new ScenarioExposure(
                    "AeBindingFailureGameTestScenarios",
                    "boundTerminalApprovalSuccessHandoff",
                    "aeBoundTerminalApprovalSuccessHandoff",
                    "MineAgentAeFabricRuntimeGameTests.boundTerminalApprovalSuccessHandoff(helper)",
                    "AeBoundTerminalApprovalSuccessGameTest",
                    "aeBoundTerminalApprovalSuccessHandoff",
                    "AeBoundTerminalApprovalSuccessGameTest.class",
                    "AeBindingFailureGameTestScenarios.boundTerminalApprovalSuccessHandoff("
            ),
            new ScenarioExposure(
                    "AeBindingFailureGameTestScenarios",
                    "boundTerminalApprovalFailsWhenAeBindingUnavailable",
                    "aeBoundTerminalApprovalFailsWhenAeBindingUnavailable",
                    "MineAgentAeFabricRuntimeGameTests.boundTerminalApprovalFailsWhenAeBindingUnavailable(helper)",
                    "AeBoundTerminalApprovalBindingUnavailableGameTest",
                    "aeBoundTerminalApprovalFailsWhenAeBindingUnavailable",
                    "AeBoundTerminalApprovalBindingUnavailableGameTest.class",
                    "AeBindingFailureGameTestScenarios.boundTerminalApprovalFailsWhenAeBindingUnavailable("
            ),
            new ScenarioExposure(
                    "AeCraftLifecycleIsolationGameTestScenarios",
                    "terminalTeardownClearsLiveJobs",
                    "aeTerminalTeardownClearsLiveJobs",
                    "MineAgentAeFabricRuntimeGameTests.terminalTeardownClearsLiveJobs(helper)",
                    "AeTerminalTeardownLiveJobsGameTest",
                    "aeTerminalTeardownClearsLiveJobs",
                    "AeTerminalTeardownLiveJobsGameTest.class",
                    "AeCraftLifecycleIsolationGameTestScenarios.terminalTeardownClearsLiveJobs("
            ),
            new ScenarioExposure(
                    "AeBindingFailureGameTestScenarios",
                    "bindingInvalidationAfterTerminalRemovalOrWrongSide",
                    "aeBindingInvalidationAfterTerminalRemovalOrWrongSide",
                    "MineAgentAeFabricRuntimeGameTests.bindingInvalidationAfterTerminalRemovalOrWrongSide(helper)",
                    "AeBindingInvalidationGameTest",
                    "aeBindingInvalidationAfterTerminalRemovalOrWrongSide",
                    "AeBindingInvalidationGameTest.class",
                    "AeBindingFailureGameTestScenarios.bindingInvalidationAfterTerminalRemovalOrWrongSide("
            ),
            new ScenarioExposure(
                    "AeBindingFailureGameTestScenarios",
                    "bindingBasedContextReresolutionSucceedsUntilBindingBecomesStale",
                    "aeBindingBasedContextReresolutionSucceedsUntilBindingBecomesStale",
                    "MineAgentAeFabricRuntimeGameTests.bindingBasedContextReresolutionSucceedsUntilBindingBecomesStale(helper)",
                    "AeBindingReresolutionGameTest",
                    "aeBindingBasedContextReresolutionSucceedsUntilBindingBecomesStale",
                    "AeBindingReresolutionGameTest.class",
                    "AeBindingFailureGameTestScenarios.bindingBasedContextReresolutionSucceedsUntilBindingBecomesStale("
            ),
            new ScenarioExposure(
                    "AeCraftLifecycleIsolationGameTestScenarios",
                    "cpuTargetedUnavailableCpuBranch",
                    "aeCpuTargetedUnavailableCpuBranch",
                    "MineAgentAeFabricRuntimeGameTests.cpuTargetedUnavailableCpuBranch(helper)",
                    "AeCpuUnavailableGameTest",
                    "aeCpuTargetedUnavailableCpuBranch",
                    "AeCpuUnavailableGameTest.class",
                    "AeCraftLifecycleIsolationGameTestScenarios.cpuTargetedUnavailableCpuBranch("
            ),
            new ScenarioExposure(
                    "AeCraftLifecycleIsolationGameTestScenarios",
                    "terminalRemovalInvalidatesMenuContextAndClearsJobs",
                    "aeTerminalRemovalInvalidatesMenuContextAndClearsJobs",
                    "MineAgentAeFabricRuntimeGameTests.terminalRemovalInvalidatesMenuContextAndClearsJobs(helper)",
                    "AeTerminalRemovalInvalidationGameTest",
                    "aeTerminalRemovalInvalidatesMenuContextAndClearsJobs",
                    "AeTerminalRemovalInvalidationGameTest.class",
                    "AeCraftLifecycleIsolationGameTestScenarios.terminalRemovalInvalidatesMenuContextAndClearsJobs("
            ),
            new ScenarioExposure(
                    "AeCraftLifecycleIsolationGameTestScenarios",
                    "cancelAndClearStayTerminalLocalAfterSubmittedRequest",
                    "aeCancelAndClearStayTerminalLocalAfterSubmittedRequest",
                    "MineAgentAeFabricRuntimeGameTests.cancelAndClearStayTerminalLocalAfterSubmittedRequest(helper)",
                    "AeCancelClearIsolationGameTest",
                    "aeCancelAndClearStayTerminalLocalAfterSubmittedRequest",
                    "AeCancelClearIsolationGameTest.class",
                    "AeCraftLifecycleIsolationGameTestScenarios.cancelAndClearStayTerminalLocalAfterSubmittedRequest("
            )
    );

    @Test
    void task4_sharedAeScenarioInventory_isFrozenToNineRunnableMethods() {
        Path repositoryRoot = repositoryRoot();
        Set<String> actualInventory = new TreeSet<>();
        actualInventory.addAll(discoverRunnableScenarioMethods(
                repositoryRoot.resolve(CRAFT_SCENARIOS),
                "AeCraftLifecycleIsolationGameTestScenarios"
        ));
        actualInventory.addAll(discoverRunnableScenarioMethods(
                repositoryRoot.resolve(BINDING_SCENARIOS),
                "AeBindingFailureGameTestScenarios"
        ));

        Set<String> expectedInventory = new TreeSet<>();
        for (ScenarioExposure exposure : EXPECTED_SCENARIOS) {
            expectedInventory.add(exposure.scenarioKey());
        }

        assertEquals("task4/ae-shared-scenarios/inventory", expectedInventory, actualInventory);
    }

    @Test
    void task4_sharedAeScenarioInventory_isExposedThroughFabricEntrypoint() {
        String fabricSource = readRequired(repositoryRoot().resolve(FABRIC_ENTRYPOINT),
                "task4/ae-shared-scenarios/fabric-entrypoint");

        for (ScenarioExposure exposure : EXPECTED_SCENARIOS) {
            assertContains(
                    "task4/ae-shared-scenarios/fabric-method/" + exposure.commonMethodName(),
                    fabricSource,
                    "public static void " + exposure.fabricEntrypointMethodName() + "(GameTestHelper helper)"
            );
            assertContains(
                    "task4/ae-shared-scenarios/fabric-delegate/" + exposure.commonMethodName(),
                    fabricSource,
                    exposure.fabricDelegateCall()
            );
        }
    }

    @Test
    void task4_sharedAeScenarioInventory_isExposedThroughForgeBootstrapAndWrappers() {
        Path repositoryRoot = repositoryRoot();
        String forgeBootstrapSource = readRequired(repositoryRoot.resolve(FORGE_BOOTSTRAP),
                "task4/ae-shared-scenarios/forge-bootstrap");

        for (ScenarioExposure exposure : EXPECTED_SCENARIOS) {
            assertContains(
                    "task4/ae-shared-scenarios/forge-bootstrap-registration/" + exposure.commonMethodName(),
                    forgeBootstrapSource,
                    exposure.forgeBootstrapRegistration()
            );

            Path forgeWrapperPath = repositoryRoot
                    .resolve(FORGE_GAME_TEST_DIRECTORY)
                    .resolve(exposure.forgeWrapperClassName() + ".java");
            String forgeWrapperSource = readRequired(forgeWrapperPath,
                    "task4/ae-shared-scenarios/forge-wrapper/" + exposure.commonMethodName());

            assertContains(
                    "task4/ae-shared-scenarios/forge-wrapper-method/" + exposure.commonMethodName(),
                    forgeWrapperSource,
                    "public static void " + exposure.forgeWrapperMethodName() + "(GameTestHelper helper)"
            );
            assertContains(
                    "task4/ae-shared-scenarios/forge-wrapper-call/" + exposure.commonMethodName(),
                    forgeWrapperSource,
                    exposure.forgeScenarioCall()
            );
        }
    }

    private static Set<String> discoverRunnableScenarioMethods(Path scenarioSourcePath, String scenarioClassName) {
        String scenarioSource = readRequired(scenarioSourcePath, "task4/ae-shared-scenarios/common-source/" + scenarioClassName);
        Set<String> methods = new TreeSet<>();
        Matcher matcher = RUNNABLE_SCENARIO_METHOD.matcher(scenarioSource);
        while (matcher.find()) {
            methods.add(scenarioClassName + "#" + matcher.group(1));
        }
        return methods;
    }

    private static Path repositoryRoot() {
        Path current = Path.of("").toAbsolutePath().normalize();
        while (current != null) {
            if (Files.exists(current.resolve("settings.gradle")) && Files.isDirectory(current.resolve("ext-ae"))) {
                return current;
            }
            current = current.getParent();
        }
        throw new AssertionError("task4/ae-shared-scenarios/repository-root -> unable to locate repository root from working directory");
    }

    private static String readRequired(Path path, String assertionName) {
        try {
            return Files.readString(path);
        } catch (Exception exception) {
            throw new AssertionError(assertionName + " -> unable to read: " + path, exception);
        }
    }

    private static void assertContains(String assertionName, String haystack, String needle) {
        if (haystack.contains(needle)) {
            return;
        }
        throw new AssertionError(assertionName + " -> missing expected text: " + needle);
    }

    private static void assertEquals(String assertionName, Object expected, Object actual) {
        if (expected == null ? actual == null : expected.equals(actual)) {
            return;
        }
        throw new AssertionError(assertionName + " -> expected: " + expected + " but was: " + actual);
    }

    private record ScenarioExposure(
            String commonScenarioClassName,
            String commonMethodName,
            String fabricEntrypointMethodName,
            String fabricDelegateCall,
            String forgeWrapperClassName,
            String forgeWrapperMethodName,
            String forgeBootstrapRegistration,
            String forgeScenarioCall
    ) {
        private String scenarioKey() {
            return commonScenarioClassName + "#" + commonMethodName;
        }
    }
}

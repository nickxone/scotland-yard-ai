package model;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Includes all test for the actual AI model
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
		GraphHelperTest.class,
		PickMoveAITest.class,
		SimulationTest.class
})
public class AllTest {}

package model;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Includes all test for the actual AI model
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
		GraphHelperTest.class,
		SimulationTest.class,
		PickMoveAITest.class
})
public class AllTest {}

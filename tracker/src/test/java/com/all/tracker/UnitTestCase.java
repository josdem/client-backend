package com.all.tracker;

import org.junit.Before;
import org.mockito.MockitoAnnotations;

public abstract class UnitTestCase {

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
	}

}

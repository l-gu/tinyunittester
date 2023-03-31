package org.demo.pojo;

import org.junit.jupiter.api.Test;

import tinyunittester.PojoUnitTester;


public class PojoClassesTest {
	
	static final boolean TRACE = true;

	@Test
	public void testEmployeeDTO() {
		new PojoUnitTester(TRACE).testAll(Employee.class);
	}

	@Test
	public void testImbalance() {
		new PojoUnitTester(TRACE).testAll(Imbalance.class);
	}

	@Test
	public void testInvalid() {
		// ERROR in this POJO
		new PojoUnitTester(TRACE).testAll(Invalid.class);
	}
}

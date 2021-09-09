package com.alessiodp.core.common;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.assertEquals;

@RunWith(PowerMockRunner.class)
public class ADPPluginTest {
	
	@PrepareForTest(ADPPlugin.class)
	@Test
	public void testJavaVersion() {
		assertEquals(8, ADPPlugin.getJavaVersion("1.8"));
		assertEquals(11, ADPPlugin.getJavaVersion("11"));
		assertEquals(16, ADPPlugin.getJavaVersion("16"));
		assertEquals(16, ADPPlugin.getJavaVersion("16-ea"));
	}
}

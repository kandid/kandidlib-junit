/*
 * (C) Copyright 2009, by Dominikus Diesch.
 *
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation;
 * either version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * library; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307, USA.
 */

package de.kandid.junit;

import java.io.File;
import java.io.IOException;

import junit.framework.TestResult;

public class TestCaseTest extends TestCase {

	public TestCaseTest(String name) {
		super(name);
	}

	public void testTempDirCreation() throws Exception {
		File tmp = createTempDir();
		assertNotNull(tmp);
		assertTrue(tmp.toString().startsWith(_tempRoot.toString()));
		File otherTmp = createTempDir();
		assertFalse(tmp.getCanonicalPath().equals(otherTmp.getCanonicalPath()));
	}

	public void testTempDirDeletion() throws Exception {
		File tmp = createTempDir();
		assertTrue(tmp.exists());
		File inner = new File(tmp, "inner");
		inner.mkdir();
		assertTrue(inner.exists());
		deleteTempFile(tmp);
		assertFalse(tmp.exists());
	}

	public void testDontDeleteOutsideDirectories() throws Exception {
		try {
			deleteTempFile(new File("out"));
			fail();
		} catch (NotAUnitTestTempDirException e) {
		}
	}

	public static class LocalTest extends TestCase {
		public LocalTest() {
			super("testFoo");
		}
		public void testFoo() throws IOException {
			_files[0] = createTempFile();
			_files[1] = createTempDir();
			for (File f : _files)
				assertTrue(f.exists());
		}
		public File[] _files = new File[2];
	};

	public void testCleanupAtEnd() {
		final LocalTest tc = new LocalTest();
      tc._tmpDirDeletePolicy = TmpDirDeletePolicy.always;
		TestResult tr = tc.run();
		assertEquals(0, tr.errorCount());
		for (File f : tc._files)
			assertFalse(f.exists());
	}
}
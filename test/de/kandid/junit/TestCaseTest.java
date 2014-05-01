/*
 * (C) Copyright 2009-2014, by Dominikus Diesch.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
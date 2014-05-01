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
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Properties;

import junit.framework.TestResult;

/**
 *
 *
 * @version $Rev:$
 */
public abstract class TestCase extends junit.framework.TestCase {

	public static class NotAUnitTestTempDirException extends RuntimeException {
		public NotAUnitTestTempDirException(File dir) {
			super("Not a unit test temporary directory: " + dir);
		}
	}

	public enum TmpDirDeletePolicy {always, passed, never};

	public TestCase(String name) {
		super(name);
		Properties props = new Properties(System.getProperties());
		if (_properties.exists())
		try {
		   props.load(new FileInputStream(_properties));
		} catch (IOException e) {
		}
		_tempRoot = new File(props.getProperty("de.kandid.junit.tmp.dir", "out/unittest"));
		_tmpDirDeletePolicy = TmpDirDeletePolicy.valueOf(props.getProperty("de.kandid.junit.tmpdir.remove", "passed"));
	}

	@Override
	public void run(TestResult result) {
		super.run(result);
		if (_tmpDirDeletePolicy == TmpDirDeletePolicy.always
		      || (_tmpDirDeletePolicy == TmpDirDeletePolicy.passed && result.errorCount() == 0)) {
			for (File f : _tempFiles.toArray(new File[_tempFiles.size()]))
				deleteTempFile(f);
		}
	}

	public File createTempDir() throws IOException {
		ensureTempRoot();
		File dir = Files.createTempDirectory(_tempRoot.toPath(), getName()).toFile();
		_tempFiles.add(dir);
		return dir;
	}

	public File createTempFile() throws IOException {
		ensureTempRoot();
		File file = File.createTempFile(getName(), null, _tempRoot);
		_tempFiles.add(file);
		return file;
	}


	public void deleteTempFile(File file) {
		if (!file.toString().startsWith(_tempRoot.toString()))
			throw new NotAUnitTestTempDirException(file);
		if (file.isDirectory()) {
			File[] files = file.listFiles();
			for (File f : files)
				deleteTempFile(f);
		}
		file.delete();
		_tempFiles.remove(file);
	}

	private void ensureTempRoot() throws IOException {
		if (!_tempRoot.exists()) {
			if (!_tempRoot.mkdir())
				throw new IOException("Could not create temp root in " + new File(".").getAbsolutePath());
		}
	}

	public static final File _properties = new File(System.getProperty("user.home") + "/.config/kandid.de/general.properties");
	public final File _tempRoot;// = new File(System.getProperty("junit.tmp.dir", "out/unittest"));
	public TmpDirDeletePolicy _tmpDirDeletePolicy = TmpDirDeletePolicy.passed;
	private final HashSet<File> _tempFiles = new HashSet<>();
}
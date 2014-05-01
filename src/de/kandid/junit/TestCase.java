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
 * A TestCase that adds some logic for the handling of temporary directories. Its main purpose
 * is to control where and how long temporary files and directories live. The properties will be
 * read from a file located at <tt>$HOME/.config/de.kandid/general.properties</tt>.<p/>
 * The following keys are recognized:
 * <table><thead><tr>
 * 	<th>Property</th><th>Description</th><th>Default</th>
 * </tr></thead><tbody><tr>
 * 	<td>{@code de.kandid.junit.tmp.dir}</td>
 * 	<td>the location where to create all temporary files and and directories</td>
 * 	<td>{@code build/tmp/unittest}</td>
 * </tr><tr>
 * 	<td>{@code de.kandid.junit.tmp.dir.remove}</td>
 * 	<td> a {@link TmpDirDeletePolicy} as a String controlling when to delete the files created for this TestCase</td>
 * 	<td>{@code passed}</td>
 * </tr></tbody></table>
 * Both values are global to all TestCases descending from this class.
 */
public abstract class TestCase extends junit.framework.TestCase {

	/**
	 * This exception will be thrown if an attempt is made to delete a file outside
	 * of the designated unit test zone.
	 */
	public static class NotAUnitTestTempDirException extends RuntimeException {
		private NotAUnitTestTempDirException(File dir) {
			super("Not a unit test temporary directory: " + dir);
		}
	}

	public enum TmpDirDeletePolicy {
		/** Always delete the files after the test has finished */
		always,
		/** Only delete the temporary files when the test has finished successfully */
		passed,
		/** Never delete the test files */
		never
	};

	/**
	 * Constructs a test case with the given name.
	 * @param name the name of this test case
	 */
	public TestCase(String name) {
		super(name);
		Properties props = new Properties(System.getProperties());
		if (_properties.exists())
		try {
		   props.load(new FileInputStream(_properties));
		} catch (IOException e) {
		}
		_tempRoot = new File(props.getProperty("de.kandid.junit.tmp.dir", "build/tmp/unittest"));
		_tmpDirDeletePolicy = TmpDirDeletePolicy.valueOf(props.getProperty("de.kandid.junit.tmp.dir.remove", "passed"));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run(TestResult result) {
		super.run(result);
		if (_tmpDirDeletePolicy == TmpDirDeletePolicy.always
		      || (_tmpDirDeletePolicy == TmpDirDeletePolicy.passed && result.errorCount() == 0)) {
			for (File f : _tempFiles.toArray(new File[_tempFiles.size()]))
				deleteTempFile(f);
		}
	}

	/**
	 * Creates a unique directory inside the unit test root. Its name starts with the name
	 * of the test and will be deleted at the end of the test according the policy given
	 * in the properties.
	 * @return the newly created directory
	 * @throws IOException if the directory could not be created
	 */
	public File createTempDir() throws IOException {
		ensureTempRoot();
		File dir = Files.createTempDirectory(_tempRoot.toPath(), getName()).toFile();
		_tempFiles.add(dir);
		return dir;
	}

	/**
	 * Creates a unique file inside the unit test root. Its name starts with the name
	 * of the test and will be deleted at the end of the test according the policy given
	 * in the properties.
	 * @return the newly created file
	 * @throws IOException if the directory could not be created
	 */
	public File createTempFile() throws IOException {
		ensureTempRoot();
		File file = File.createTempFile(getName(), null, _tempRoot);
		_tempFiles.add(file);
		return file;
	}

	/**
	 * Deletes the file or directory (recursively) denoted by {@code file}. An exception
	 * will be thrown if the the file is not inside the unit test directory.
	 * <em>Note</em>: the test for containment is a little sloppy. Paths with <tt>../</tt>
	 * will fool the safety check!
	 * @param file the file to delete
	 * @throws NotAUnitTestTempDirException if the file is outside of the unit test dir
	 */
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

	/**
	 * The path of the properties controlling the behaviour of this class
	 */
	public static final File _properties = new File(System.getProperty("user.home") + "/.config/de.kandid/general.properties");

	/**
	 * The (configurable) root of the created test files.
	 */
	public final File _tempRoot;// = new File(System.getProperty("junit.tmp.dir", "out/unittest"));

	/**
	 * The policy how to handle the created files and directories after the test has finished.
	 */
	public TmpDirDeletePolicy _tmpDirDeletePolicy = TmpDirDeletePolicy.passed;
	private final HashSet<File> _tempFiles = new HashSet<>();
}
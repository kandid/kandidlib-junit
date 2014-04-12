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
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Properties;

import junit.framework.TestResult;
import de.kandid.util.IOUtil;
import de.kandid.util.KandidException;

public abstract class TestCase extends junit.framework.TestCase {

	public static class NotAUnitTestTempDirException extends KandidException {
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
		File dir = IOUtil.createTempDir(getName(), _tempRoot);
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
	private final HashSet<File> _tempFiles = new HashSet<File>();
}
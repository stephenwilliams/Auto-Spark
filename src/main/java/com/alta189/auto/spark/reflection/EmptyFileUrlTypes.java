package com.alta189.auto.spark.reflection;

import org.reflections.vfs.Vfs;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class EmptyFileUrlTypes implements Vfs.UrlType {
	private final List<String> fileTypes = new ArrayList<>();

	public EmptyFileUrlTypes() {
	}
	public EmptyFileUrlTypes(List<String> fileTypes) {
		fileTypes.forEach(this::addFileType);
	}

	public EmptyFileUrlTypes(String... fileTypes) {
		this(Arrays.asList(fileTypes));
	}

	public void addFileType(String fileType) {
		fileTypes.add(fileType.toLowerCase());
	}

	@Override
	public boolean matches(URL url) throws Exception {
		if (!url.getProtocol().equalsIgnoreCase("file")) {
			return false;
		}
		String file = url.toExternalForm().toLowerCase();
		for (String fileType : fileTypes) {
			if (file.endsWith(fileType)) {
				return true;
			}
		}

		return false;
	}

	@Override
	public Vfs.Dir createDir(URL url) throws Exception {
		return new Vfs.Dir() {

			@Override
			public String getPath() {
				return url.toExternalForm();
			}

			@Override
			public Iterable<Vfs.File> getFiles() {
				return Collections.emptyList();
			}

			@Override
			public void close() {
			}
		};
	}
}

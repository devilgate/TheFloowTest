package org.devilgate.floowtest.file;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipFile;

import org.devilgate.floowtest.mongodb.Connection;

public class FileProcess {

	private final String fileName;
	private final Connection conn;
	private Path file;
	private BufferedReader bufferedReader;

	public FileProcess(final String fileName, final Connection conn) {
		this.fileName = fileName;
		this.conn = conn;
	}

	public void fileToQueue() throws IOException {

		prepareFile();
		sendLinesToQueue();
	}

	public void createWordsCollection() {

	}

	private void sendLinesToQueue() throws IOException {

		String line = "";
		while (line != null){

			line = bufferedReader.readLine();
			conn.writeToQueue(line);
		}

		// When we're done we write a suitable entry and close everything.
		conn.finishedWithQueue();
		bufferedReader.close();
	}

	private void prepareFile() throws IOException {

		file = FileSystems.getDefault().getPath(fileName);
		if (!file.toFile().exists()) {

			throw new FileNotFoundException(fileName);
		}

		Reader reader;
		if (isZipped()) {
			ZipFile zip = new ZipFile(file.toFile());
			InputStream input = zip.getInputStream(zip.entries().nextElement()); // Assume it only has one
			reader = new InputStreamReader(input);
		} else {
			reader = new FileReader(file.toFile());
		}

		bufferedReader = new BufferedReader(reader);
	}

	private boolean isZipped() throws IOException {
		return Files.probeContentType(file).contains("compress");
	}
}

package org.devilgate.floowtest;

import java.io.IOException;
import java.time.Instant;

import org.devilgate.floowtest.file.FileProcess;
import org.devilgate.floowtest.line.LineParseAndSave;
import org.devilgate.floowtest.mongodb.Connection;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

@SpringBootApplication
public class FloowTestApplication {

	public static final String DATABASE_NAME = "WordCount";
	public static final String WORDS_COLLECTION_NAME = "Words";

	private Connection connection;

	public static void main(String[] args) throws IOException {

		FloowTestApplication app = new FloowTestApplication();
		app.launch(args);

		SpringApplication.run(FloowTestApplication.class, args);
	}

	private void launch(String[] args) throws IOException {

		Args arguments = new Args();
		JCommander commander = new JCommander(arguments);
		commander.parse(args);
		connection = new Connection(arguments.mongoUrl);

		// If we have a source file, we are the startup instance, so we write the lines from the
		// file to our queue.
		if (arguments.source != null) {
			populateQueue(arguments);
		}

		processQueue(arguments);
	}

	private void processQueue(final Args arguments) {

		System.out.println("Started processing queue at " + Instant.now());
		var parser = new LineParseAndSave(connection);
		var line = connection.readQueueAndRemove();
		while (line != null && !line.equals("###Done###")) {

			parser.processLine(line);
			line = connection.readQueueAndRemove();
		}

		System.out.println("Finished processing queue at " + Instant.now());

		// If we're not the startup instance, we shut down here.
		if (arguments.source == null) {
			System.exit(0);
		}

	}

	private void populateQueue(final Args arguments) throws IOException {

		FileProcess process = new FileProcess(arguments.source, connection);
		process.fileToQueue();
	}

	/**
	 * The command-line arguments for running the application.
	 */
	public static class Args {

		@Parameter(
				names = "–source",
				description = "The file to process.")
		private String source;

		@Parameter(
				names = "–mongo",
				description = "The host and port for the MongoDB instance, in the form "
				              + "'host:port'. Default is localhost:27017.")
		private String mongoUrl = "localhost:27017";
	}
}

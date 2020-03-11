package org.devilgate.floowtest;

import java.io.IOException;
import java.time.Instant;

import org.devilgate.floowtest.file.FileProcess;
import org.devilgate.floowtest.line.LineParseAndSave;
import org.devilgate.floowtest.mongodb.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

@SpringBootApplication
public class FloowTestApplication {

	public static final String DATABASE_NAME = "WordCount";
	public static final String WORDS_COLLECTION_NAME = "Words";
	private static final Logger log = LoggerFactory.getLogger(FloowTestApplication.class);

	private Connection connection;

	public static void main(String[] args) throws IOException {

		FloowTestApplication app = new FloowTestApplication();
		app.launch(args);

		// Only the startup instance (the one with the -source argument set) should get here, so
		// there will only be one web app.
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
			connection.clearQueue();
			connection.clearWords();
			populateQueue(arguments);
		}

		// In either case we read the queue and process whatever we find.
		processQueue(arguments);

		// If we've finished processing the queue, and we are not the startup instance, then we
		// shut down.
		if (arguments.source == null) {
			System.exit(0);
		}
	}

	private void processQueue(final Args arguments) {

		log.debug("Started processing queue at {}", Instant.now());
		var parser = new LineParseAndSave(connection);
		var line = connection.readQueueAndRemove();
		while (line != null && !line.equals("###Done###")) {

			parser.processLine(line);
			line = connection.readQueueAndRemove();
		}

		log.debug("Finished processing queue at {}", Instant.now());
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

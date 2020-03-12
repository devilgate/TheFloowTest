package org.devilgate.floowtest;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.devilgate.floowtest.file.FileProcess;
import org.devilgate.floowtest.line.LineParseAndSave;
import org.devilgate.floowtest.mongodb.Connection;
import org.devilgate.floowtest.mongodb.MongoRead;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.util.Pair;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

public class FloowTestApplication {

	private static final Logger log = LoggerFactory.getLogger(FloowTestApplication.class);
	private static final int STARTUP_QUEUE_READ_SLEEP_TIME = 1;
	private static final int STARTUP_QUEUE_READ_RETRIES = 5;

	private Connection connection;

	public static void main(String[] args) throws IOException {

		log.info("FloowTest Application launching...");
		FloowTestApplication app = new FloowTestApplication();
		app.launch(args);
	}

	/**
	 * We launch in one of three modes, depending on the command-line arguments. If -source is set,
	 * we are the primary instance. We read the file and write each line to the queue. After that we
	 * behave as a secondary instance.
	 *
	 * If neither source nor top/bottom is set, we are a secondary instance. We read the queue
	 * and process each line.
	 *
	 * If top and/or bottom is set, we display the specified number of entries from the
	 * appropriate part of the results.
	 *
	 */
	private void launch(String[] args) throws IOException {

		log.info("Parsing command-line arguments...");
		Args arguments = new Args();
		JCommander commander = new JCommander(arguments);
		commander.parse(args);
		connection = new Connection(arguments.mongoUrl);

		// If we have a source file, we are the startup instance, so we write the lines from the
		// file to our queue.
		if (arguments.source != null) {
			populateQueue(arguments);
		}

		// If neither source nor top/bottom is set, we read the queue and process whatever we find.
		if (shouldProcessQueue(arguments)) {
			processQueue(arguments);

			// If we've finished processing the queue, and we are not the startup instance, then we
			// shut down.
			if (arguments.source == null) {
				System.exit(0);
			}
		}

		// If top or bottom is set, we want to display the appropriate results.
		if (arguments.top > 0 || arguments.bottom > 0) {

			printList(arguments.top, "Most");
			printList(arguments.bottom, "Least");
		}

		if (arguments.printMoreStats) {
			printMoreStats();
		}
	}

	private void printMoreStats() {

		MongoRead read = new MongoRead(connection);
		long unique = read.howManyUniqueWords();
		long oncelers = read.howManyOccurOnlyOnce();

		System.out.printf("%n%nThere are %d unique words. %d of them occur only once.%n%n",
		                  unique, oncelers);
	}

	private void printList(final int howMany, String direction) {

		if (howMany > 0) {
			MongoRead read = new MongoRead(connection);
			System.out.printf("%n--- %s frequently-occurring words in set ---%n%n", direction);

			List<Pair<String, Integer>> words = new ArrayList<>();
			switch (direction) {
				case "Most":
					words = read.getTop(howMany);
					break;
				case "Least":
					words = read.getBottom(howMany);
					break;
			}

			System.out.printf("|____ Word ________________________________|_ Occurrences _|%n");
			String LineFormat = "| %-40s | %13d |%n";
			for (Pair<String, Integer> word : words) {
				System.out.printf(LineFormat, word.getFirst(), word.getSecond());
			}
			System.out.printf("|__________________________________________|_______________|%n");
		}
	}

	private boolean shouldProcessQueue(Args arguments) {

		// The only time we don't process the queue is when any of the reporting options are set,
		// but source is not.
		return arguments.source != null
		       || (arguments.top == 0
		           && arguments.bottom == 0
		           && !arguments.printMoreStats);
	}

	private void processQueue(final Args arguments) {

		log.info("Started processing queue at {}", Instant.now());
		var parser = new LineParseAndSave(connection);
		var line = readWithRetries();
		while (line.isPresent() && !line.get().equals(Connection.DONE_SPECIAL_VALUE)) {

			parser.processLine(line.get(), arguments.excludeStopWords);
			line = connection.readQueueAndRemove();
		}

		log.info("Finished processing queue at {}", Instant.now());
	}

	/**
	 * This is intended to give processes a few seconds on startup for the queue to become
	 * populated.
	 * @return an optional string
	 */
	private Optional<String> readWithRetries() {

		var line = connection.readQueueAndRemove();
		int retries = 0;
		while (line.isEmpty() && retries < STARTUP_QUEUE_READ_RETRIES) {
			try {
				TimeUnit.SECONDS.sleep(STARTUP_QUEUE_READ_SLEEP_TIME);
			} catch (InterruptedException e) {
				// Should never happen.
				throw new RuntimeException("Unexpected interrupt", e);
			}
			retries++;
			line = connection.readQueueAndRemove();
		}
		return line;
	}

	private void populateQueue(final Args arguments) throws IOException {

		connection.clearQueue();
		connection.clearWords();
		log.info("Sending file to queue");
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
				names = "-mongo",
				description = "The host and port for the MongoDB instance, in the form "
				              + "'host:port'. Default is localhost:27017.")
		private String mongoUrl = "localhost:27017";

		@Parameter(
				names = "-top",
				description = "The number of most-frequently-used words to display."
		)
		private int top;

		@Parameter(
				names = "-bottom",
				description = "The number of least-frequently-used words to display."
		)
		private int bottom;

		@Parameter(
				names = "-more",
				description = "If present, additional statistics will be printed"
		)
		private boolean printMoreStats;

		@Parameter(
				names = "-excludeStopWords",
				description = "If provided, the word count will exclude a standard set of English"
				              + " stop words."
		)
		private boolean excludeStopWords;
	}
}

package org.devilgate.floowtest;

import java.io.IOException;

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

	public static void main(String[] args) throws IOException {

		FloowTestApplication app = new FloowTestApplication();
		app.launch(args);

		SpringApplication.run(FloowTestApplication.class, args);
	}

	private void launch(String[] args) throws IOException {

		// Check the arguments: if no -mongo specified, use the default
		Args arguments = new Args();
		JCommander commander = new JCommander(arguments);
		commander.parse(args);

		if (arguments.source != null) {
			populateQueue(arguments);
		}

		processQueue(arguments);
	}

	private void processQueue(final Args arguments) {

		Connection connection = new Connection(arguments.mongoUrl);

		// TODO: Move all that code into the Connection class.
		LineParseAndSave parser = new LineParseAndSave(connection.getClient());
		//

		String line = connection.readQueueAndRemove();
		while (line != null && !line.equals("###Done###")) {

			parser.processLine(line);
			line = connection.readQueueAndRemove();
		}


		System.out.println("Finished processing");
		System.exit(0);

	}

	private void populateQueue(final Args arguments) throws IOException {

		FileProcess process = new FileProcess(arguments.source, arguments.mongoUrl);
		process.fileToQueue();
	}

	/**
	 * The command-line arguments for running the application.
	 */
	public static class Args {

		@Parameter(
				names = "–source",
				description = "The file to process. Default is dump.xml.")
		private String source = "dump.xml";

		@Parameter(
				names = "–mongo",
				description = "The host and port for the MongoDB instance, in the form "
				              + "'host:port'. Default is localhost:27017.")
		private String mongoUrl = "localhost:27017";
	}
}

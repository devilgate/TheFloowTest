package org.devilgate.floowtest.line;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.devilgate.floowtest.mongodb.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LineParseAndSave {

	private static final Logger log = LoggerFactory.getLogger(LineParseAndSave.class);
	private final Connection conn;

	public LineParseAndSave(final Connection conn) {

		this.conn = conn;
	}

	public void processLine(String textLine, boolean excludeStop) {

		Line line = new Line(textLine);
		save(line.parse(excludeStop));
	}

	private void save(List<String> words) {

		for (String word : words) {
			log.debug("Word: {}", word);

			conn.saveWord(word);
		}

	}
}

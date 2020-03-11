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
	private Map<String, Long> wordCounts = new HashMap<>();

	public LineParseAndSave(final Connection conn) {

		this.conn = conn;
	}

	public boolean processLine(String textLine, boolean excludeStop) {

		Line line = new Line(textLine);
		return save(line.parse(excludeStop));
	}

	private boolean save(List<String> words) {

		for (String word : words) {
			log.debug("Word: {}}", word);

			conn.saveWord(word);
		}

		return true;
	}

	public Map<String, Long> getWordCounts() {

		return wordCounts;
	}
}

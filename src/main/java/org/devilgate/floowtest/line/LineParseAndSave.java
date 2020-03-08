package org.devilgate.floowtest.line;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

@Service
public class LineParseAndSave {

	private Map<String, Long> wordCounts = new HashMap<>();

	public boolean processLine(String lineOfText) {

		Line line = new Line(lineOfText);
		return save(line.parse());
	}

	private boolean save(List<String> words) {

		// For now, we just log and store in-memory.
		for (String word : words) {
			System.out.printf("Word: %s%n", word);

			long currentCount = 1;
			if (wordCounts.containsKey(word)) {
				currentCount = wordCounts.get(word) + 1;
			}
			wordCounts.put(word, currentCount);
		}

		// Send to the MongoDB server
		return false;
	}

	public Map<String, Long> getWordCounts() {

		return wordCounts;
	}
}

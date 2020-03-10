package org.devilgate.floowtest.line;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.devilgate.floowtest.FloowTestApplication;
import org.devilgate.floowtest.mongodb.Connection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;

// @Service
public class LineParseAndSave {

	// private MongoCollection<Document> wordStore;

	private final Connection conn;
	private Map<String, Long> wordCounts = new HashMap<>();

	// @Autowired
	public LineParseAndSave(final Connection conn) {

		this.conn = conn;
	}

	public boolean processLine(String textLine) {

		Line line = new Line(textLine);
		return save(line.parse());
	}

	private boolean save(List<String> words) {

		for (String word : words) {
			System.out.printf("Word: %s%n", word);

			conn.saveWord(word);
		}

		return true;
	}

	public Map<String, Long> getWordCounts() {

		return wordCounts;
	}
}

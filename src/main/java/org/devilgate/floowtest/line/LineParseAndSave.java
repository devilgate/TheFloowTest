package org.devilgate.floowtest.line;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

@Service
public class LineParseAndSave {

	private MongoCollection<Document> wordStore;

	private Map<String, Long> wordCounts = new HashMap<>();

	@Autowired
	public LineParseAndSave(final MongoClient client) {
		final MongoDatabase db = client.getDatabase("WordCount");
		wordStore = db.getCollection("Words");
	}

	public boolean processLine(String textLine) {

		Line line = new Line(textLine);
		return save(line.parse());
	}

	private boolean save(List<String> words) {

		for (String word : words) {
			System.out.printf("Word: %s%n", word);


			// wordStore.findOneAndUpdate()
			long currentCount = 1;
			wordStore.insertOne(new Document(word, currentCount));
			// if (wordCounts.containsKey(word)) {
			// 	currentCount = wordCounts.get(word) + 1;
			// }
			// wordCounts.put(word, currentCount);
		}

		// Send to the MongoDB server
		return false;
	}

	public Map<String, Long> getWordCounts() {

		return wordCounts;
	}
}

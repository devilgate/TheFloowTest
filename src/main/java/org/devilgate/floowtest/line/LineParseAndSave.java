package org.devilgate.floowtest.line;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.devilgate.floowtest.FloowTestApplication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;

@Service
public class LineParseAndSave {

	private MongoCollection<Document> wordStore;

	private Map<String, Long> wordCounts = new HashMap<>();

	@Autowired
	public LineParseAndSave(final MongoClient client) {
		final MongoDatabase db = client.getDatabase(FloowTestApplication.DATABASE_NAME);
		wordStore = db.getCollection("Words");
	}

	public boolean processLine(String textLine) {

		Line line = new Line(textLine);
		return save(line.parse());
	}

	private boolean save(List<String> words) {

		for (String word : words) {
			System.out.printf("Word: %s%n", word);

			Document findWord = new Document();
			findWord.append("Word", word);
			Document updatedWord = new Document();
			updatedWord.append("$inc", new Document().append("Count", 1));
			UpdateOptions options = new UpdateOptions().upsert(true);
			wordStore.updateOne(findWord, updatedWord, options);
		}

		return true;
	}

	public Map<String, Long> getWordCounts() {

		return wordCounts;
	}
}

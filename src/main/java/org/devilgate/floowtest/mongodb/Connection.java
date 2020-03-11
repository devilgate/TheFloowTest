package org.devilgate.floowtest.mongodb;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.devilgate.floowtest.FloowTestApplication;
import org.springframework.data.util.Pair;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.UpdateOptions;

public class Connection {

	private final String mongo;
	private MongoCollection<Document> queue;
	private MongoCollection<Document> wordStore;

	public Connection(final String mongo) {
		this.mongo = mongo;
		init();
	}

	public void writeToQueue(String line) {

		var lineDoc = new Document();
		lineDoc.append("Line", line);
		queue.insertOne(lineDoc);
	}

	public String readQueueAndRemove() {

		Document document = queue.findOneAndDelete(new Document());
		String line = null;
		if (document != null) {

			if (document.containsKey("Done")) {
				return "###Done###";
			}

			line = document.getString("Line");
		}
		return line;
	}

	public void finishedWithQueue() {

		Document done = new Document().append("Done", true);
		queue.insertOne(done);
	}

	private void init() {

		final MongoClient client = new MongoClient(mongo);
		final MongoDatabase db = client.getDatabase(FloowTestApplication.DATABASE_NAME);
		queue = db.getCollection("Queue");
		wordStore = db.getCollection(FloowTestApplication.WORDS_COLLECTION_NAME);
		wordStore.createIndex(Indexes.ascending("Word"));
		wordStore.createIndex(Indexes.ascending("Count"));
		wordStore.createIndex(Indexes.descending("Count"));
	}

	public void clearWords() {

		wordStore.deleteMany(new Document());
	}

	public void clearQueue() {

		queue.deleteMany(new Document());
	}

	public void saveWord(final String word) {

		Document findWord = new Document();
		findWord.append("Word", word);
		Document updatedWord = new Document();
		updatedWord.append("$inc", new Document().append("Count", 1));
		UpdateOptions options = new UpdateOptions().upsert(true);
		wordStore.updateOne(findWord, updatedWord, options);
	}

	List<Pair<String, Integer>> getEntriesWithFewestOccurrences(int howMany) {

		return getTopOrBottomList(howMany, 1);
	}

	List<Pair<String, Integer>> getEntriesWithMostOccurrences(int howMany) {

		return getTopOrBottomList(howMany, -1);
	}

	long getNumberOfUniqueWords() {
		return wordStore.countDocuments();
	}

	private List<Pair<String, Integer>> getTopOrBottomList(int howMany, int direction) {

		var words = new ArrayList<Pair<String, Integer>>();
		var sorter = new Document();
		sorter.append("Count", direction);
		var cursor = wordStore.find().sort(sorter).limit(howMany);
		for (Document document : cursor) {
			words.add(Pair.of(document.getString("Word"), document.getInteger("Count")));
		}
		return words;
	}

	public long howManyOccurOnlyOnce() {

		Document query = new Document("Count", new Document("$eq", 1));
		return wordStore.countDocuments(query);
	}
}

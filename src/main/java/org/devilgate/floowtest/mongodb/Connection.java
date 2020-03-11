package org.devilgate.floowtest.mongodb;

import org.bson.Document;
import org.devilgate.floowtest.FloowTestApplication;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.UpdateOptions;

public class Connection {

	private final String mongo;
	private MongoClient client;
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

	public void finishedWithClient() {

		client.close();
	}

	private void init() {

		client = new MongoClient(mongo);
		MongoDatabase db = client.getDatabase(FloowTestApplication.DATABASE_NAME);
		queue = db.getCollection("Queue");
		wordStore = db.getCollection(FloowTestApplication.WORDS_COLLECTION_NAME);
		wordStore.createIndex(Indexes.ascending("Word"));
	}

	public void clearWords() {

		wordStore.deleteMany(new Document());
	}

	public void clearQueue() {

		queue.deleteMany(new Document());
	}

	public MongoClient getClient() {
		return client;
	}

	public void saveWord(final String word) {

		Document findWord = new Document();
		findWord.append("Word", word);
		Document updatedWord = new Document();
		updatedWord.append("$inc", new Document().append("Count", 1));
		UpdateOptions options = new UpdateOptions().upsert(true);
		wordStore.updateOne(findWord, updatedWord, options);
	}
}

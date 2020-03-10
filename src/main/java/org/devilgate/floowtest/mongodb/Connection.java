package org.devilgate.floowtest.mongodb;

import org.bson.Document;
import org.devilgate.floowtest.FloowTestApplication;

import com.mongodb.DBCursor;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class Connection {

	private final String mongo;
	private MongoClient client;
	private MongoCollection<Document> queue;

	public Connection(final String mongo) {
		this.mongo = mongo;
		connectToDb();
	}

	public void writeToQueue(String line) {

		var lineDoc = new Document();
		lineDoc.append("Line", line);
		queue.insertOne(lineDoc);
	}

	public String readQueueAndRemove() {

		var cursor = queue.find();
		Document document = cursor.first();

		String line = null;
		if (document != null) {

			if (document.containsKey("Done")) {
				return "###Done###";
			}

			line = document.getString("Line");
			queue.deleteOne(document);
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

	private void connectToDb() {

		client = new MongoClient(mongo);
		MongoDatabase db = client.getDatabase(FloowTestApplication.DATABASE_NAME);
		queue = db.getCollection("Queue");
	}

	public MongoClient getClient() {
		return client;
	}
}

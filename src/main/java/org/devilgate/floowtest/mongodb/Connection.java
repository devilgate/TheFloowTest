package org.devilgate.floowtest.mongodb;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.springframework.data.util.Pair;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.UpdateOptions;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Connection {

	public static final String WORDS_COLLECTION_NAME = "Words";
	public static final String DATABASE_NAME = "WordCount";
	public static final String QUEUE_COLLECTION_NAME = "Queue";
	public static final String WORD_FIELD_NAME = "Word";
	public static final String COUNT_FIELD_NAME = "Count";
	public static final String LINE_NAME = "Line";
	public static final String DONE_KEY = "Done";
	public static final String DONE_SPECIAL_VALUE = "###Done###";
	private final String mongo;
	private MongoCollection<Document> queue;
	private MongoCollection<Document> wordStore;
	private MongoDatabase db;

	public Connection(final String mongo) {

		log.info("Connecting to MongoDB");
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

			if (document.containsKey(DONE_KEY)) {
				return DONE_SPECIAL_VALUE;
			}

			line = document.getString(LINE_NAME);
		}
		return line;
	}

	public void finishedWithQueue() {

		Document done = new Document().append(DONE_KEY, true);
		queue.insertOne(done);
	}

	private void init() {

		final MongoClient client = new MongoClient(mongo);
		db = client.getDatabase(DATABASE_NAME);
		log.info("Getting Queue collection");
		queue = db.getCollection(QUEUE_COLLECTION_NAME);
		log.info("Getting Words collection");
		wordStore = db.getCollection(WORDS_COLLECTION_NAME);
		log.info("Creating indexes");
		wordStore.createIndex(Indexes.ascending(WORD_FIELD_NAME));
		wordStore.createIndex(Indexes.ascending(COUNT_FIELD_NAME));
		wordStore.createIndex(Indexes.descending(COUNT_FIELD_NAME));
	}

	public void clearWords() {

		log.info("Clearing words");

		// We drop and recreate, because clearing a large collection can be very
		// slow.
		wordStore.drop();
		db.createCollection(WORDS_COLLECTION_NAME);
	}

	public void clearQueue() {

		log.info("Clearing queue");
		queue.drop();
		db.createCollection(QUEUE_COLLECTION_NAME);
	}

	public void saveWord(final String word) {

		Document findWord = new Document();
		findWord.append(WORD_FIELD_NAME, word);
		Document updatedWord = new Document();
		updatedWord.append("$inc", new Document().append(COUNT_FIELD_NAME, 1));
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
		sorter.append(COUNT_FIELD_NAME, direction);
		var cursor = wordStore.find().sort(sorter).limit(howMany);
		for (Document document : cursor) {
			words.add(Pair.of(document.getString(WORD_FIELD_NAME),
			                  document.getInteger(COUNT_FIELD_NAME)));
		}
		return words;
	}

	public long howManyOccurOnlyOnce() {

		Document query = new Document(COUNT_FIELD_NAME, new Document("$eq", 1));
		return wordStore.countDocuments(query);
	}
}

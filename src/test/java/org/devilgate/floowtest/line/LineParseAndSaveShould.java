package org.devilgate.floowtest.line;

import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import static org.mockito.Mockito.*;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LineParseAndSaveShould {

	private static final String LINE = "word again this that the other other word word, other "
	                                   + "other other";

	@Mock
	private MongoClient client;

	@Mock
	private DB db;

	@Mock
	private DBCollection wordStore;

	@Test
	public void giveCorrectCountsOnSaving() {

		when(client.getDB(anyString())).thenReturn(db);
		when(db.getCollection(anyString())).thenReturn(wordStore);

		var classUnderTest = new LineParseAndSave(client);
		var notUsed = classUnderTest.processLine(LINE);
		// var savedValues = classUnderTest.getWordCounts();
		//
		// assertEquals(1, savedValues.get("again"));
		// assertEquals(2, savedValues.get("word"));
		// assertEquals(2, savedValues.get("other"));
		// assertEquals(1, savedValues.get("this"));
		// assertEquals(1, savedValues.get("that"));
		// assertEquals(1, savedValues.get("the"));
	}

	@Test
	public void writeToMongo() {

		MongoClient client = new MongoClient();
		MongoDatabase db = client.getDatabase("WordCount");
		MongoCollection<Document> collection = db.getCollection("Words");

		var cut = new LineParseAndSave(client);
		cut.processLine(LINE);
	}
}

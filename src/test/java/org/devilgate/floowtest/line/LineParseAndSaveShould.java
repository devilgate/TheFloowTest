package org.devilgate.floowtest.line;

import org.bson.Document;
import org.hibernate.validator.internal.IgnoreForbiddenApisErrors;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;

import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LineParseAndSaveShould {

	private static final String LINE = "word word word,";

	@Mock
	private MongoClient client;

	@Mock
	private MongoDatabase db;

	@Mock
	private MongoCollection<Document> wordStore;

	@Test
	public void giveCorrectCountsOnSaving() {

		when(client.getDatabase(anyString())).thenReturn(db);
		when(db.getCollection(anyString())).thenReturn(wordStore);

		Document findWord = new Document();
		findWord.append("Word", "word");
		Document updatedWord = new Document();
		updatedWord.append("$inc", new Document().append("Count", 1));
		UpdateOptions options = new UpdateOptions().upsert(true);

		var classUnderTest = new LineParseAndSave(client);
		classUnderTest.processLine(LINE);

		verify(wordStore, times(3)).updateOne(findWord, updatedWord, options);
	}

	@Disabled
	@Test
	public void writeToMongo() {

		MongoClient client = new MongoClient();
		MongoDatabase db = client.getDatabase("WordCount");
		MongoCollection<Document> collection = db.getCollection("Words");

		var cut = new LineParseAndSave(client);
		cut.processLine(LINE);
	}
}

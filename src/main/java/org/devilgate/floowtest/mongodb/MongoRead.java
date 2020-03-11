package org.devilgate.floowtest.mongodb;

import java.util.List;

import org.springframework.data.util.Pair;

public class MongoRead {

	private final Connection connection;

	public MongoRead(final Connection connection) {
		this.connection = connection;
	}

	public List<Pair<String, Integer>> getBottom(int howMany) {

		return connection.getEntriesWithFewestOccurrences(howMany);
	}

	public List<Pair<String, Integer>> getTop(int howMany) {

		return connection.getEntriesWithMostOccurrences(howMany);
	}

	public long howManyOccurOnlyOnce() {

		return connection.howManyOccurOnlyOnce();
	}

	public long howManyUniqueWords() {

		return connection.getNumberOfUniqueWords();
	}
}

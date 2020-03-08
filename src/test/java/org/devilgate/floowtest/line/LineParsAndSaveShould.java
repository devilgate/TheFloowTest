package org.devilgate.floowtest.line;

import java.util.Map;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LineParsAndSaveShould {

	private static final String LINE = "word again this that the other other word";

	@Test
	public void giveCorrectCountsOnSaving() {

		var classUnderTest = new LineParseAndSave();
		var notUsed = classUnderTest.processLine(LINE);
		var savedValues = classUnderTest.getWordCounts();

		assertEquals(1, savedValues.get("again"));
		assertEquals(2, savedValues.get("word"));
		assertEquals(2, savedValues.get("other"));
		assertEquals(1, savedValues.get("this"));
		assertEquals(1, savedValues.get("that"));
		assertEquals(1, savedValues.get("the"));
	}
}

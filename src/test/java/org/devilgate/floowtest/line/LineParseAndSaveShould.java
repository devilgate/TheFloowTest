package org.devilgate.floowtest.line;

import org.devilgate.floowtest.mongodb.Connection;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class LineParseAndSaveShould {

	private static final String LINE = "word word word,";

	@Mock
	private Connection conn;

	@Test
	public void giveCorrectCountsOnSaving() {

		var classUnderTest = new LineParseAndSave(conn);
		classUnderTest.processLine(LINE);

		verify(conn, times(3)).saveWord("word");
	}
}

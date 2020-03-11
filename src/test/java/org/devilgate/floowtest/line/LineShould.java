package org.devilgate.floowtest.line;

import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class LineShould {

	private static final String TEST_LINE = "The quick (brown) dog? ==; pie-eyed in the SKY. <xml"
	                                        + " tags>, hello - | O'Reilly ' -- I'm";
	private static final List<String> EXPECTED_WORDS = List.of("The", "quick", "brown", "dog",
	                                                           "pie-eyed", "in", "the","SKY",
	                                                           "xml", "tags", "hello", "O'Reilly",
	                                                           "I'm");

	@Test
	public void returnWordsWhenLineReceived() {

		Line classUnderTest = new Line(TEST_LINE);

		// Could test for equality, but they don't have to be equal, just contain the same words.
		List<String> results = classUnderTest.parse(false);
		assertTrue(results.containsAll(EXPECTED_WORDS) &&
		           EXPECTED_WORDS.containsAll(results));
	}
}



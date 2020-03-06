package org.devilgate.floowtest.line;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.util.StringUtils;

/**
 * A simple parser for a line of text. Strips out punctuation and other non-letter characters,
 * and splits on whitespace. Hyphenated words are treated as one.
 */
public class Line {

	final String line;
	final Pattern junk = Pattern.compile("[^\\w\\s-]");

	public Line(final String testLine) {

		this.line = testLine;
	}

	public List<String> parse() {

		// Remove all non-letter, non-whitespace characters, except hyphens
		String stripped = line.replaceAll(junk.pattern(), "");
		List<String> parsed = new LinkedList<>(Arrays.asList(stripped.split("\\s")));

		// Remove any hyphens on their own or empty/blank entries. Could probably do this by
		// constructing a more complex regex above, but then we'd have n+1 problems.
		parsed.removeIf(s -> StringUtils.isEmpty(s) || s.trim().equals("-"));
		return parsed;
	}
}

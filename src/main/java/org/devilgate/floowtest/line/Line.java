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

	private static final Pattern CHARACTERS_TO_REMOVE = Pattern.compile("[^\\w\\s-'‘]");
	private static final List<String> REMOVE_ON_THEIR_OWN = List.of("-", "'", "‘");
	private final String line;

	public Line(final String testLine) {

		this.line = testLine;
	}

	public List<String> parse() {

		// Remove all non-letter, non-whitespace characters, except hyphens and apostrophes
		var stripped = CHARACTERS_TO_REMOVE.matcher(line).replaceAll("");
		var parsed = new LinkedList<>(Arrays.asList(stripped.split("\\s")));

		// Remove any hyphens or apostrophes on their own, or empty/blank entries. Could probably do
		// this by constructing a more complex regex above, but then we'd have n+1 problems.
		parsed.removeIf(s -> StringUtils.isEmpty(s) || REMOVE_ON_THEIR_OWN.contains(s.trim()));
		return parsed;
	}
}

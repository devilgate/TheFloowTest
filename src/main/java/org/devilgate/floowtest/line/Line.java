package org.devilgate.floowtest.line;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

/**
 * A simple parser for a line of text. Strips out punctuation and other non-letter characters,
 * and splits on whitespace. Hyphenated words are treated as one.
 */
public class Line {

	private static final Pattern CHARACTERS_TO_REMOVE = Pattern.compile("[^\\w\\s-'‘]");
	private static final List<String> REMOVE_ON_THEIR_OWN = List.of("-", "'", "‘", "--");
	private static final Logger log = LoggerFactory.getLogger(Line.class);
	private final String line;

	public Line(final String testLine) {

		log.debug("Received lin: {}", testLine);
		this.line = testLine;
	}

	public List<String> parse() {

		// Remove all non-letter, non-whitespace characters, except hyphens and apostrophes
		var stripped = CHARACTERS_TO_REMOVE.matcher(line).replaceAll("");
		var parsed = new LinkedList<>(Arrays.asList(stripped.split("\\s")));

		// Remove any hyphens or apostrophes on their own, or empty/blank entries. Could probably do
		// this by constructing a more complex regex above, but then we'd have n+1 problems.
		parsed.removeIf(s -> StringUtils.isEmpty(s) || REMOVE_ON_THEIR_OWN.contains(s.trim()));
		log.debug("parsed line: {} ", parsed);
		return parsed;
	}
}

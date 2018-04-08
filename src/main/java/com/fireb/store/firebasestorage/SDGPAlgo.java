package com.fireb.store.firebasestorage;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

/**
 * SECURITY
 * Code within this class is written in order to create as few security loopholes as possible
 * In the algorithm there is no processing that is done in other classes regarding the algorithm
 * There is minimum areas for buffer overflow within this class
 */

public class SDGPAlgo {

	// The total number of words in the text file
	protected static double totalNoOfWords;

	// The number of matches found in total
	private static int numMatches;

	//	public static void main(String[] args) {
	//
	//		// To print the total number of matches found in the text
	//		System.out.println("Number of matches in total: " + numMatches);
	//
	//	}

	// To find the number of times the repeating pattern occurs in the text
	public static List<Integer> match(String pattern, String text) {

		// Using the boyer-moore search algorithm to find repeating patterns
		// To store a list of the indexes of the matches of the pattern in the text
		List<Integer> matches = new ArrayList<Integer>();

		// To store the length of the text and the pattern
		int textLength = text.length();
		int patternLength = pattern.length();

		// A map of the patterns to process via bad character shift to go through the text
		Map<Character, Integer> rightMostIndexes = preprocessForBadCharacterShift(pattern);

		// To get the current index where the pattern identification is currently at
		int alignedAt = 0;

		/*
		 * To check if we are still in bounds of the text when we
		 * compare from the current index with the pattern's length
		 */
		while (alignedAt + (patternLength - 1) < textLength) {

			// Identifies if the pattern occurs again or not in the text from the entire text
			for (int indexInPattern = patternLength - 1; indexInPattern >= 0; indexInPattern--) {
				int indexInText = alignedAt + indexInPattern;
				char x = text.charAt(indexInText);
				char y = pattern.charAt(indexInPattern);
				if (indexInText >= textLength) {
					break;
				}
				if (x != y) {
					Integer r = rightMostIndexes.get(x);
					if (r == null) {
						alignedAt = indexInText + 1;
					} else {
						int shift = indexInText - (alignedAt + r);
						alignedAt += shift > 0 ? shift : 1;
					}
					break;
				} else if (indexInPattern == 0) {
					matches.add(alignedAt);
					alignedAt++;
				}
			}
		}
		return matches;
	}

	// To get the pattern from the text
	public static int getPattern(String text) {

		// Length of the text
		int length = text.length();

		// Pattern to be checked
		String pattern;

		// The range of the values to check for the repeating patterns
		int minToSkip = 0;
		int maxToSkip = 0;

		// Get the words separated into an array
		String[] words = text.split(" ");
		totalNoOfWords = words.length;

		// Removing all the spaces from the text once the words have been added
		text = text.replaceAll("\\s+", "");

		// Current index of the word
		int currentWordIndex = 0;

		// For loop that creates the pattern and checks if it is in the text
		for (int i = 0; i <= length; i += words[currentWordIndex++].length()) {

			pattern = "";

			pattern = getPatternRange(text, pattern, i, i + words[currentWordIndex].length());

			// To define the range to check for the repeating patterns
			minToSkip = i;
			maxToSkip = i + 5;

			// To get the matches and where they occur within a certain range
			getMatches(text, pattern, minToSkip, maxToSkip);

			/*
			 * To check if the current index of words is reached before going to the next iteration
			 * To avoid going out of bounds in the string of words
			 */
			if ((currentWordIndex + 1) >= words.length) {

				break;

			}

		}

		return numMatches;

	}

	// Creates the pattern from the current index to the maximum index
	public static String getPatternRange(String text, String pattern, int current, int max) {

		// Throw's exception if there is a logical error
		if (current > max) {

			throw new UnsupportedOperationException("Current index can not be greater than the maximum index !");

			// Condition to stop the recursive function
		} else if (current == max) {

			return pattern;

			// The recursive function to create a pattern from the range provided
		} else {

			pattern += "" + text.charAt(current);

			current++;

			return getPatternRange(text, pattern, current, max);

		}

	}

	// To get the matches and where they occur within a certain range
	public static void getMatches(String text, String pattern, int minToSkip, int maxToSkip) {

		// List of the matches of the pattern in the text
		List<Integer> matches = match(pattern, text);


		/*
		 * To remove the starting index of the pattern from the matches and to remove
		 * the matches for the pattern that are found before the starting index of the pattern
		 * Therefore only the repeated pattern after the starting index are identified
		 */
		for (int i = 0; i < matches.size(); i++) {

			if (matches.get(i) <= minToSkip | matches.get(i) >= maxToSkip) {

				matches.remove(i);

				i = i - 1;

			}

		}

		// If no matches are found
		if (matches.size() == 0) {

			// Not printing anything currently as only the number of matches is needed
			// To inform the user that there are no matches found for that pattern
			// System.out.println("No matches found for \"" + pattern + "\" in the text \"" + text + "\"");
		} else {

			// Not printing anything currently as only the number of matches is needed
			// To inform the user that there are matches found for that pattern and the index of where they occur
			// System.out.println("Matches found for \"" + pattern + "\" in the text \"" + text + "\":");
			// To add to the number of matches and to show the user where each of patterns occur
			for (Integer integer : matches) {

				// Not printing anything currently as only the number of matches is needed
				// System.out.println("Match at: " + integer);
				// Adding 1 to the number of matches found in total
				numMatches++;

			}

		}

	}

	// To convert the text file to a string
	public static String textToString(File textFile) throws FileNotFoundException {

		String textInString = "";

		StringBuilder fileContents = new StringBuilder((int) textFile.length());

		Scanner scan = new Scanner(textFile);

		try {

			while (scan.hasNextLine()) {

				fileContents.append(scan.nextLine());

			}

			textInString = fileContents.toString();

		} finally {

			scan.close();

		}

		return textInString;

	}

	// To process for bad character shift to traverse through the text faster
	private static Map<Character, Integer> preprocessForBadCharacterShift(String pattern) {
		Map<Character, Integer> map = new HashMap<Character, Integer>();
		for (int i = pattern.length() - 1; i >= 0; i--) {
			char c = pattern.charAt(i);
			if (!map.containsKey(c)) {
				map.put(c, i);
			}
		}
		return map;
	}

}

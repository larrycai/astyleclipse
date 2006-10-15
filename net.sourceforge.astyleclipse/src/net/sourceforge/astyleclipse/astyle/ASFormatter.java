/*
 * :tabSize=8:indentSize=4:noTabs=true:maxLineLen=0:
 *
 * Copyright (c) 1998,1999,2000,2001 Tal Davidson. All rights reserved.
 *
 * ASFormatter.java
 * by Tal Davidson (davidsont@bigfoot.com)
 * This file is a part of "Artistic Style" - an indentater and reformatter
 * of C++, C, and Java source files.
 *
 * Ported from C++ to Java by Dirk Moebius (dmoebius@gmx.net).
 *
 * The "Artistic Style" project, including all files needed to compile it,
 * is free software; you can redistribute it and/or use it and/or modify it
 * under the terms of EITHER the "Artistic License" OR
 * the GNU Library General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 *  version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of EITHER the "Artistic License" or
 * the GNU Library General Public License along with this program.
 */

package net.sourceforge.astyleclipse.astyle;

import java.util.*;
import java.io.*;
import net.sourceforge.astyleclipse.astyle.util.*;

/**
 * A C/C++/Java source code indenter, formatter and beautifier.
 */
public class ASFormatter extends ASBeautifier {

	/**
	 * Constructor of ASFormatter
	 */
	public ASFormatter() {
		staticInit();

		preBracketHeaderStack = null;
		bracketTypeStack = null;
		parenStack = null;

		sourceIterator = null;
		bracketFormatMode = NONE_MODE;
		shouldPadOperators = false;
		shouldPadParenthesies = false;
		shouldBreakOneLineBlocks = true;
		shouldBreakOneLineStatements = true;
		shouldConvertTabs = false;
		shouldBreakBlocks = false;
		shouldBreakClosingHeaderBlocks = false;
		shouldBreakClosingHeaderBrackets = false;
		shouldBreakElseIfs = false;
	}

	/**
	 * initialize the ASFormatter. This method should be called every time a
	 * ASFormatter object is to start formatting a new source file.
	 * <code>init()</code> receives a reference to a ASSourceIterator object
	 * that will be used to iterate through the source code.
	 * 
	 * @param iter
	 *            the ASSourceIterator object.
	 */
	public void init(ASSourceIterator si) {
		super.init(si);
		sourceIterator = si;

		preBracketHeaderStack = new StringStack();
		bracketTypeStack = new IntegerStack(); // = new vector<BracketType>;
		bracketTypeStack.push_back(DEFINITION_TYPE);
		parenStack = new IntegerStack();
		parenStack.push_back(0);

		currentHeader = null;
		currentLine = "";
		formattedLine = new StringBuffer();
		currentChar = ' ';
		previousCommandChar = ' ';
		previousNonWSChar = ' ';
		quoteChar = '"';
		charNum = 0;
		previousOperator = null;

		isVirgin = true;
		isInLineComment = false;
		isInComment = false;
		isInPreprocessor = false;
		doesLineStartComment = false;
		isInQuote = false;
		isSpecialChar = false;
		isNonParenHeader = true;
		foundPreDefinitionHeader = false;
		foundPreCommandHeader = false;
		foundQuestionMark = false;
		isInLineBreak = false;
		endOfCodeReached = false;
		isLineReady = false;
		isPreviousBracketBlockRelated = true;
		isInPotentialCalculation = false;
		// foundOneLineBlock = false;
		shouldReparseCurrentChar = false;
		passedSemicolon = false;
		passedColon = false;
		isInTemplate = false;
		shouldBreakLineAfterComments = false;
		isImmediatelyPostComment = false;
		isImmediatelyPostEmptyBlock = false;

		isPrependPostBlockEmptyLineRequested = false;
		isAppendPostBlockEmptyLineRequested = false;
		prependEmptyLine = false;

		foundClosingHeader = false;
		previousReadyFormattedLineLength = 0;

		isImmediatelyPostHeader = false;
		isInHeader = false;
	}

	/**
	 * get the bracket formatting mode.
	 * 
	 * @see #setBracketFormatMode(int)
	 */
	public int getBracketFormatMode() {
		return bracketFormatMode;
	}

	/**
	 * set the bracket formatting mode. The <code>mode</code> must be one of:
	 * <ul>
	 * <li>ASResource.NONE_MODE: no formatting of brackets.</li>
	 * <li>ASResource.ATTACH_MODE: Java, K&R style bracket placement.</li>
	 * <li>ASResource.BREAK_MODE: ANSI C/C++ style bracket placement.</li>
	 * <li>ASResource.BDAC_MODE: Linux style bracket placement.</li>
	 * </ul>
	 * </p>
	 * 
	 * @param mode
	 *            the bracket formatting mode.
	 * @see astyle.ASResource#NONE_MODE
	 * @see astyle.ASResource#ATTACH_MODE
	 * @see astyle.ASResource#BREAK_MODE
	 * @see astyle.ASResource#BDAC_MODE
	 */
	public void setBracketFormatMode(int mode) {
		bracketFormatMode = mode;
	}

	/**
	 * get closing header bracket breaking mode.
	 * 
	 * @see #setBreakClosingHeaderBracketsMode(boolean)
	 */
	public boolean getBreakClosingHeaderBracketsMode() {
		return shouldBreakClosingHeaderBrackets;
	}

	/**
	 * set closing header bracket breaking mode. Options:
	 * <ul>
	 * <li><code>true</code> brackets just before closing headers (e.g.
	 * 'else', 'catch') will be broken, even if standard brackets are attached.</li>
	 * <li><code>false</code> closing header brackets will be treated as
	 * standard brackets.</li>
	 * </ul>
	 * 
	 * @param mode
	 *            the closing header bracket breaking mode.
	 */
	public void setBreakClosingHeaderBracketsMode(boolean state) {
		shouldBreakClosingHeaderBrackets = state;
	}

	/**
	 * get 'else if()' breaking mode. If true, 'else' headers will be broken
	 * from their succeeding 'if' headers, otherwise 'else' headers will be
	 * attached to their succeeding 'if' headers.
	 */
	public boolean getBreakElseIfsMode() {
		return shouldBreakElseIfs;
	}

	/**
	 * set 'else if()' breaking mode. If true, 'else' headers will be broken
	 * from their succeeding 'if' headers, otherwise 'else' headers will be
	 * attached to their succeeding 'if' headers.
	 * 
	 * @param mode
	 *            the 'else if()' breaking mode.
	 */
	public void setBreakElseIfsMode(boolean state) {
		shouldBreakElseIfs = state;
	}

	/**
	 * get operator padding mode. If true, statement operators will be padded
	 * with spaces around them, otherwise statement operators will not be
	 * padded.
	 */
	public boolean getOperatorPaddingMode() {
		return shouldPadOperators;
	}

	/**
	 * set operator padding mode. If true, statement operators will be padded
	 * with spaces around them, otherwise statement operators will not be
	 * padded.
	 * 
	 * @param mode
	 *            the padding mode.
	 */
	public void setOperatorPaddingMode(boolean state) {
		shouldPadOperators = state;
	}

	/**
	 * get parenthesis padding mode. If true, statement parenthesis will be
	 * padded with spaces around them, otherwise statement parenthesis will not
	 * be padded.
	 */
	public boolean getParenthesisPaddingMode() {
		return shouldPadParenthesies;
	}

	/**
	 * set parenthesis padding mode. If true, statement parenthesis will be
	 * padded with spaces around them, otherwise statement parenthesis will not
	 * be padded.
	 * 
	 * @param mode
	 *            the padding mode.
	 */
	public void setParenthesisPaddingMode(boolean state) {
		shouldPadParenthesies = state;
	}

	/**
	 * get option to break/not break one-line blocks.
	 */
	public boolean getBreakOneLineBlocksMode() {
		return shouldBreakOneLineBlocks;
	}

	/**
	 * set option to break/not break one-line blocks.
	 * 
	 * @param state
	 *            true = break, false = don't break.
	 */
	public void setBreakOneLineBlocksMode(boolean state) {
		shouldBreakOneLineBlocks = state;
	}

	/**
	 * get option to break/not break lines consisting of multiple statements.
	 */
	public boolean getSingleStatementsMode() {
		return shouldBreakOneLineStatements;
	}

	/**
	 * set option to break/not break lines consisting of multiple statements.
	 * 
	 * @param state
	 *            true = break, false = don't break.
	 */
	public void setSingleStatementsMode(boolean state) {
		shouldBreakOneLineStatements = state;
	}

	/**
	 * get option to convert tabs to spaces.
	 */
	public boolean getTabSpaceConversionMode() {
		return shouldConvertTabs;
	}

	/**
	 * set option to convert tabs to spaces.
	 * 
	 * @param state
	 *            true = convert, false = don't convert.
	 */
	public void setTabSpaceConversionMode(boolean state) {
		shouldConvertTabs = state;
	}

	/**
	 * get option to break unrelated blocks of code with empty lines.
	 */
	public boolean getBreakBlocksMode() {
		return shouldBreakBlocks;
	}

	/**
	 * set option to break unrelated blocks of code with empty lines.
	 * 
	 * @param state
	 *            true = convert, false = don't convert.
	 */
	public void setBreakBlocksMode(boolean state) {
		shouldBreakBlocks = state;
	}

	/**
	 * <p>
	 * get option to break closing header blocks of code (such as 'else',
	 * 'catch', ...) with empty lines.
	 * </p>
	 */
	public boolean getBreakClosingHeaderBlocksMode() {
		return shouldBreakClosingHeaderBlocks;
	}

	/**
	 * <p>
	 * set option to break closing header blocks of code (such as 'else',
	 * 'catch', ...) with empty lines.
	 * </p>
	 * 
	 * @param state
	 *            true = convert, false = don't convert.
	 */
	public void setBreakClosingHeaderBlocksMode(boolean state) {
		shouldBreakClosingHeaderBlocks = state;
	}

	/**
	 * check if there are any indented lines ready to be read by nextLine()
	 * 
	 * @return are there any indented lines ready?
	 */
	public boolean hasMoreLines() {
		if (!isFormattingEnabled())
			return super.hasMoreLines();
		else
			return !endOfCodeReached;
	}

	/**
	 * get the next formatted line.
	 * 
	 * @return a formatted line.
	 */
	public String nextLine() {
		String newHeader;
		boolean isCharImmediatelyPostComment = false;
		boolean isPreviousCharPostComment = false;
		boolean isInVirginLine = isVirgin;
		boolean isCharImmediatelyPostOpenBlock = false;
		boolean isCharImmediatelyPostCloseBlock = false;
		boolean isCharImmediatelyPostTemplate = false;

		if (!isFormattingEnabled())
			return super.nextLine();

		while (!isLineReady) {
			if (shouldReparseCurrentChar)
				shouldReparseCurrentChar = false;
			else if (!getNextChar()) {
				breakLine();
				return beautify(readyFormattedLine);
			} else {
				// stuff to do when reading a new character...
				// make sure that a virgin '{' at the begining of the file will
				// be treated as a block...
				if (isInVirginLine && currentChar == '{')
					previousCommandChar = '{';
				isPreviousCharPostComment = isCharImmediatelyPostComment;
				isCharImmediatelyPostComment = false;
				isCharImmediatelyPostTemplate = false;
			}

			if (isInLineComment) {
				appendCurrentChar();

				// explicitely break a line when a line comment's end is found.
				if ( /* bracketFormatMode == ATTACH_MODE && */charNum + 1 == currentLine
						.length()) {
					isInLineBreak = true;
					isInLineComment = false;
					isImmediatelyPostComment = true;
					// /currentChar = '{'; // danson, commented out this line

					// / danson, explanation of above --
					/*
					 * with code like this:
					 * 
					 * if (true) {}
					 *  // some comment int i = 0;
					 * 
					 * after beautifying, it would look like this:
					 * 
					 * if (true) {}
					 *  // some comment
					 * 
					 * int i = 0;
					 * 
					 * with an extra line between the comment and the next line.
					 * Beautifying again would cause another blank line to be
					 * inserted. I narrowed the problem down to this block of
					 * code, and commenting out the above line seems to have
					 * made it work correctly now.
					 * 
					 */
					// / end comments by danson
				}
				continue;
			} else if (isInComment) {
				if (isSequenceReached(AS_CLOSE_COMMENT)) {
					isInComment = false;
					isImmediatelyPostComment = true;
					appendSequence(AS_CLOSE_COMMENT);
					goForward(1);
				} else
					appendCurrentChar();

				continue;
			}
			// not in line comment or comment
			else if (isInQuote) {
				if (isSpecialChar) {
					isSpecialChar = false;
					appendCurrentChar();
				} else if (currentChar == '\\') {
					isSpecialChar = true;
					appendCurrentChar();
				} else if (quoteChar == currentChar) {
					isInQuote = false;
					appendCurrentChar();
				} else
					appendCurrentChar();

				continue;
			}

			// handle white space - needed to simplify the rest.
			if (isWhiteSpace(currentChar) || isInPreprocessor) {
				// DEVEL: if (isLegalNameChar(previousChar) &&
				// isLegalNameChar(peekNextChar()))
				appendCurrentChar();
				continue;
			}

			// not in MIDDLE of quote or comment or white-space of any type...
			if (isSequenceReached(AS_OPEN_LINE_COMMENT)) {
				isInLineComment = true;
				if (shouldPadOperators)
					appendSpacePad();
				appendSequence(AS_OPEN_LINE_COMMENT);
				goForward(1);
				continue;
			} else if (isSequenceReached(AS_OPEN_COMMENT)) {
				isInComment = true;
				if (shouldPadOperators)
					appendSpacePad();
				appendSequence(AS_OPEN_COMMENT);
				goForward(1);
				continue;
			} else if (currentChar == '"' || currentChar == '\'') {
				isInQuote = true;
				quoteChar = currentChar;
				// if (shouldPadOperators) // BUGFIX: these two lines removed.
				// seem to be unneeded, and interfere with L"
				// appendSpacePad(); // BUFFIX: TODO make sure the removal of
				// these lines doesn't reopen old bugs...
				appendCurrentChar();
				continue;
			}

			// not in quote or comment or white-space of any type...

			// check if in preprocessor
			// isInPreprocessor will be automatically reset at the beginning
			// of a new line in getNextChar()
			if (currentChar == '#')
				isInPreprocessor = true;

			if (isInPreprocessor) {
				appendCurrentChar();
				continue;
			}

			// not in preprocessor...
			if (isImmediatelyPostComment) {
				isImmediatelyPostComment = false;
				isCharImmediatelyPostComment = true;
			}

			if (shouldBreakLineAfterComments) {
				shouldBreakLineAfterComments = false;
				shouldReparseCurrentChar = true;
				breakLine();
				continue;
			}

			// reset isImmediatelyPosHeader information
			if (isImmediatelyPostHeader) {
				isImmediatelyPostHeader = false;

				// Make sure headers are broken from their succeeding blocks
				// (e.g.
				// if (isFoo) DoBar();
				// should become
				// if (isFoo)
				// DoBar;
				// )
				// But treat else if() as a special case which should not be
				// broken!
				if (shouldBreakOneLineStatements) {
					// if may break 'else if()'s, then simply break the line
					if (shouldBreakElseIfs)
						isInLineBreak = true;
					else {
						// make sure 'else if()'s are not broken.
						boolean isInElseIf = false;
						String upcomingHeader;

						upcomingHeader = findHeader(headers);
						if (currentHeader == AS_ELSE && upcomingHeader == AS_IF)
							isInElseIf = true;

						if (!isInElseIf)
							isInLineBreak = true; // BUGFIX: SHOULD NOT BE
													// breakLine() !!!
					}
				}
			}

			if (passedSemicolon) {
				passedSemicolon = false;
				if (parenStack.back() == 0) {
					shouldReparseCurrentChar = true;
					isInLineBreak = true;
					continue;
				}
			}

			if (passedColon) {
				passedColon = false;
				if (parenStack.back() == 0) {
					shouldReparseCurrentChar = true;
					isInLineBreak = true;
					continue;
				}
			}

			// Check if in template declaration, e.g. foo<bar> or foo<bar,fig>
			// If so, set isInTemplate to true

			if (!isInTemplate && currentChar == '<') {
				int templateDepth = 0;
				String oper;
				for (int i = charNum; i < currentLine.length(); i += (oper != null ? oper
						.length()
						: 1)) {
					oper = super.findHeader(currentLine, i, operators);

					if (oper == AS_LS)
						templateDepth++;
					else if (oper == AS_GR) {
						templateDepth--;
						if (templateDepth == 0) {
							// this is a template!
							isInTemplate = true;
							break;
						}
					} else if (oper == AS_COMMA // comma, e.g. A<int, char>
							|| oper == AS_BIT_AND // reference, e.g. A<int&>
							|| oper == AS_MULT // pointer, e.g. A<int*>
							|| oper == AS_COLON_COLON) // ::, e.g. std::string
						continue;
					else if (!isLegalNameChar(currentLine.charAt(i))
							&& !isWhiteSpace(currentLine.charAt(i))) {
						// this is not a template -> leave...
						isInTemplate = false;
						break;
					}
				}
			}

			// handle parenthesies

			if (currentChar == '(' || currentChar == '['
					|| (isInTemplate && currentChar == '<'))
				parenStack.incBack();
			else if (currentChar == ')' || currentChar == ']'
					|| (isInTemplate && currentChar == '>')) {
				parenStack.decBack();
				if (isInTemplate && parenStack.back() == 0) {
					isInTemplate = false;
					isCharImmediatelyPostTemplate = true;
				}

				// check if this parenthesis closes a header, e.g. ig (...),
				// while (...)
				if (isInHeader && parenStack.back() == 0) {
					isInHeader = false;
					isImmediatelyPostHeader = true;
				}
			}

			// handle brackets

			int bracketType = 0;

			if (currentChar == '{') {
				bracketType = getBracketType();
				foundPreDefinitionHeader = false;
				foundPreCommandHeader = false;
				bracketTypeStack.push_back(bracketType);
				preBracketHeaderStack.push_back(currentHeader);
				currentHeader = null;
				isPreviousBracketBlockRelated = !IS_A(bracketType, ARRAY_TYPE);
			} else if (currentChar == '}') {
				// if a request has been made to append a post block empty line,
				// but the block exists immediately before a closing bracket,
				// then there is not need for the post block empty line.
				isAppendPostBlockEmptyLineRequested = false;
				if (!bracketTypeStack.empty()) {
					bracketType = bracketTypeStack.back();
					bracketTypeStack.pop_back();
					isPreviousBracketBlockRelated = !IS_A(bracketType,
							ARRAY_TYPE);
				}
				if (!preBracketHeaderStack.empty()) {
					currentHeader = preBracketHeaderStack.back();
					preBracketHeaderStack.pop_back();
				} else
					currentHeader = null;
			}

			if (!IS_A(bracketType, ARRAY_TYPE)) {
				if (currentChar == '{')
					parenStack.push_back(0);
				else if (currentChar == '}')
					if (!parenStack.empty())
						parenStack.pop_back();

				if (bracketFormatMode != NONE_MODE) {
					if (currentChar == '{') {
						if (bracketFormatMode == ATTACH_MODE
								|| (bracketFormatMode == BDAC_MODE
										&& bracketTypeStack.size() >= 2 && IS_A(
										bracketTypeStack.at(bracketTypeStack
												.size() - 2), COMMAND_TYPE)
								/* && isInLineBreak */)) {
							appendSpacePad();
							if (previousCommandChar != '{'
									&& previousCommandChar != '}'
									&& previousCommandChar != ';') // '}', ';'
																	// chars
																	// added for
																	// proper
																	// handling
																	// of '{'
																	// immediately
																	// after a
																	// '}' or
																	// ';'
								appendCurrentChar(false);
							else
								appendCurrentChar(true);
							continue;
						} else if (bracketFormatMode == BREAK_MODE
								|| (bracketFormatMode == BDAC_MODE
										&& bracketTypeStack.size() >= 2 && IS_A(
										bracketTypeStack.at(bracketTypeStack
												.size() - 2), DEFINITION_TYPE))) {
							if (shouldBreakOneLineBlocks
									|| !IS_A(bracketType, SINGLE_LINE_TYPE))
								breakLine();
							appendCurrentChar();
							continue;
						}
					} else if (currentChar == '}') {
						// boolean origLineBreak = isInLineBreak;

						// mark state of immediately after empty block
						// this state will be used for locating brackets that
						// appear immedately AFTER an empty block (e.g. '{}
						// \n}').
						if (previousCommandChar == '{')
							isImmediatelyPostEmptyBlock = true;

						if ((!(previousCommandChar == '{' && isPreviousBracketBlockRelated)) // this
																								// '{'
																								// does
																								// not
																								// close
																								// an
																								// empty
																								// block
								&& (shouldBreakOneLineBlocks || !IS_A(
										bracketType, SINGLE_LINE_TYPE)) // astyle
																		// is
																		// allowed
																		// to
																		// break
																		// on
																		// line
																		// blocks
								&& !isImmediatelyPostEmptyBlock) // this '}'
																	// does not
																	// immediately
																	// follow an
																	// empty
																	// block
						{
							breakLine();
							appendCurrentChar();
						} else {
							if (!isCharImmediatelyPostComment)
								isInLineBreak = false;
							appendCurrentChar();
							if (shouldBreakOneLineBlocks
									|| !IS_A(bracketType, SINGLE_LINE_TYPE))
								shouldBreakLineAfterComments = true;
						}
						if (shouldBreakBlocks)
							isAppendPostBlockEmptyLineRequested = true;

						continue;
					}
				}
			}

			if (((previousCommandChar == '{' && isPreviousBracketBlockRelated) || (previousCommandChar == '}'
					&& !isImmediatelyPostEmptyBlock // <--
					&& isPreviousBracketBlockRelated
					&& !isPreviousCharPostComment // <-- Fixes wrongly
													// appended newlines after
													// '}' immediately after
													// comments...
			&& peekNextChar() != ' '))
					&& (shouldBreakOneLineBlocks || !IS_A(bracketTypeStack
							.back(), SINGLE_LINE_TYPE))) {
				isCharImmediatelyPostOpenBlock = (previousCommandChar == '{');
				isCharImmediatelyPostCloseBlock = (previousCommandChar == '}');
				previousCommandChar = ' ';
				isInLineBreak = true; // <----
			}

			// reset block handling flags
			isImmediatelyPostEmptyBlock = false;

			// look for headers
			if (!isInTemplate) {
				if ((newHeader = findHeader(headers)) != null) {
					foundClosingHeader = false;
					String previousHeader;
					// recognize closing headers of do..while and if..else
					if ((newHeader == AS_ELSE && currentHeader == AS_IF)
							|| (newHeader == AS_WHILE && currentHeader == AS_DO)
							|| (newHeader == AS_CATCH && currentHeader == AS_TRY)
							|| (newHeader == AS_FINALLY && currentHeader == AS_CATCH))
						foundClosingHeader = true;

					previousHeader = currentHeader;
					currentHeader = newHeader;

					// If in ATTACH or LINUX bracket modes, attach closing
					// headers
					// (e.g. 'else', 'catch') to their preceding bracket,
					// but do not perform the attachment if the
					// shouldBreakClosingHeaderBrackets is set!
					if (!shouldBreakClosingHeaderBrackets
							&& foundClosingHeader
							&& (bracketFormatMode == ATTACH_MODE || bracketFormatMode == BDAC_MODE)
							&& previousNonWSChar == '}') {
						isInLineBreak = false;
						appendSpacePad();
						if (shouldBreakBlocks) {
							isAppendPostBlockEmptyLineRequested = false;
						}
					}

					// Check if a template definition as been reached, e.g.
					// template<class A>
					if (newHeader == AS_TEMPLATE)
						isInTemplate = true;

					// check if the found header is non-paren header
					isNonParenHeader = nonParenHeaders.contains(newHeader);
					appendSequence(currentHeader);
					goForward(currentHeader.length() - 1);
					// if padding is on, and a paren-header is found,
					// then add a space pad after it.
					if (shouldPadOperators && !isNonParenHeader)
						appendSpacePad();

					// Signal that a header has been reached
					// *** But treat a closing while() (as in do...while)
					// as if it where NOT a header since a closing while()
					// should never have a block after it!
					if (!(foundClosingHeader && currentHeader == AS_WHILE)) {
						isInHeader = true;
						if (isNonParenHeader) {
							isImmediatelyPostHeader = true;
							isInHeader = false;
						}
					}

					if (currentHeader == AS_IF && previousHeader == AS_ELSE)
						isInLineBreak = false;

					if (shouldBreakBlocks) {
						if (previousHeader == null && !foundClosingHeader
								&& !isCharImmediatelyPostOpenBlock) {
							isPrependPostBlockEmptyLineRequested = true;
						}

						if (currentHeader == AS_ELSE
								|| currentHeader == AS_CATCH
								|| currentHeader == AS_FINALLY
								|| foundClosingHeader) {
							isPrependPostBlockEmptyLineRequested = false;
						}

						if (shouldBreakClosingHeaderBlocks
								&& isCharImmediatelyPostCloseBlock) {
							isPrependPostBlockEmptyLineRequested = true;
						}
					}

					continue;
				} else if ((newHeader = findHeader(preDefinitionHeaders)) != null) {
					foundPreDefinitionHeader = true;
					appendSequence(newHeader);
					goForward(newHeader.length() - 1);
					if (shouldBreakBlocks) {
						isPrependPostBlockEmptyLineRequested = true;
					}
					continue;
				} else if ((newHeader = findHeader(preCommandHeaders)) != null) {
					foundPreCommandHeader = true;
					appendSequence(newHeader);
					goForward(newHeader.length() - 1);
					continue;
				}
			}

			if (previousNonWSChar == '}' || currentChar == ';') {
				if (shouldBreakOneLineStatements
						&& currentChar == ';'
						&& (shouldBreakOneLineBlocks || !IS_A(bracketTypeStack
								.back(), SINGLE_LINE_TYPE))) {
					passedSemicolon = true;
				}

				if (shouldBreakBlocks && currentHeader != null
						&& parenStack.back() == 0) {
					isAppendPostBlockEmptyLineRequested = true;
				}

				if (currentChar != ';')
					currentHeader = null; // DEVEL: is this ok?

				foundQuestionMark = false;
				foundPreDefinitionHeader = false;
				foundPreCommandHeader = false;
				isInPotentialCalculation = false;
			}

			if (currentChar == ':' && shouldBreakOneLineStatements && !isInFor // not
																				// in a
																				// for(...
																				// :
																				// ...)
																				// sequence
					&& !foundQuestionMark // not in a ... ? ... : ... sequence
					&& !foundPreDefinitionHeader // not in a definition block
													// (e.g. class foo : public
													// bar
					&& previousCommandChar != ')' // not immediately after
													// closing paren of a method
													// header, e.g.
													// ASFormatter::ASFormatter(...)
													// : ASBeautifier(...)
					&& previousChar != ':' // not part of '::'
					&& peekNextChar() != ':') // not part of '::'
			{
				passedColon = true;
				if (shouldBreakBlocks) {
					isPrependPostBlockEmptyLineRequested = true;
				}
			}

			if (currentChar == '?') {
				foundQuestionMark = true;
			}

			if (shouldPadOperators) {
				if ((newHeader = findHeader(operators)) != null) {
					boolean shouldPad = (newHeader != AS_COLON_COLON
							&& newHeader != AS_PAREN_PAREN
							&& newHeader != AS_BLPAREN_BLPAREN
							&& newHeader != AS_PLUS_PLUS
							&& newHeader != AS_MINUS_MINUS
							&& newHeader != AS_NOT
							&& newHeader != AS_BIT_NOT
							&& newHeader != AS_ARROW
							&& newHeader != AS_OPERATOR
							&& !(newHeader == AS_MINUS && isInExponent())
							&& !(newHeader == AS_PLUS && isInExponent())
							&& previousOperator != AS_OPERATOR
							&& !((newHeader == AS_MULT || newHeader == AS_BIT_AND) && isPointerOrReference()) && !((isInTemplate || isCharImmediatelyPostTemplate) && (newHeader == AS_LS || newHeader == AS_GR)));

					if (!isInPotentialCalculation)
						if (assignmentOperators.contains(newHeader))
							isInPotentialCalculation = true;

					// pad before operator
					if (shouldPad
							&& !(newHeader == AS_COLON && !foundQuestionMark)
							&& newHeader != AS_SEMICOLON
							&& newHeader != AS_COMMA) {
						appendSpacePad();
					}
					appendSequence(newHeader);
					goForward(newHeader.length() - 1);

					// since this block handles '()' and '[]',
					// the parenStack must be updated here accordingly!
					if (newHeader == AS_PAREN_PAREN
							|| newHeader == AS_BLPAREN_BLPAREN)
						parenStack.decBack();

					currentChar = newHeader.charAt(newHeader.length() - 1);

					// pad after operator
					// but do not pad after a '-' that is a urinary-minus.
					if (shouldPad
							&& !(newHeader == AS_MINUS && isUrinaryMinus()))
						appendSpacePad();

					previousOperator = newHeader;
					continue;
				}
			}

			if (shouldPadParenthesies) {
				if (currentChar == '(' || currentChar == '[') {
					char peekedChar = peekNextChar();
					isInPotentialCalculation = true;

					appendCurrentChar();

					if (!(currentChar == '(' && peekedChar == ')')
							&& !(currentChar == '[' && peekedChar == ']'))
						appendSpacePad();
					continue;
				} else if (currentChar == ')' || currentChar == ']') {
					char peekedChar = peekNextChar();
					if (!(previousChar == '(' && currentChar == ')')
							&& !(previousChar == '[' && currentChar == ']'))
						appendSpacePad();

					appendCurrentChar();

					if (peekedChar != ';' && peekedChar != ','
							&& peekedChar != '.'
							&& !(currentChar == ']' && peekedChar == '['))
						appendSpacePad();
					continue;
				}
			}

			appendCurrentChar();
		}

		// return a beautified (i.e. correctly indented) line.
		String beautifiedLine;
		int readyFormattedLineLength = readyFormattedLine.trim().length();

		if (prependEmptyLine && readyFormattedLineLength > 0
				&& previousReadyFormattedLineLength > 0) {
			isLineReady = true; // signal that a readyFormattedLine is still
								// waiting
			beautifiedLine = beautify("");
		} else {
			isLineReady = false;
			beautifiedLine = beautify(readyFormattedLine);
		}

		prependEmptyLine = false;
		previousReadyFormattedLineLength = readyFormattedLineLength;
		return beautifiedLine;
	}

	private static final boolean IS_A(int a, int b) {
		return ((a & b) == b);
	}

	/**
	 * check if formatting options are enabled, in addition to indentation.
	 * 
	 * @return are formatting options enabled?
	 */
	private boolean isFormattingEnabled() {
		return bracketFormatMode != NONE_MODE || shouldPadOperators
				|| shouldConvertTabs;
	}

	/**
	 * jump over several characters.
	 * 
	 * @param i
	 *            the number of characters to jump over.
	 */
	private void goForward(int i) {
		while (--i >= 0) {
			getNextChar();
		}
	}

	/**
	 * get the next character, increasing the current placement in the process.
	 * the new character is inserted into the variable currentChar.
	 * 
	 * @return whether succeded to recieve the new character.
	 */
	private boolean getNextChar() {
		isInLineBreak = false;
		boolean isAfterFormattedWhiteSpace = false;

		if (shouldPadOperators && !isInComment && !isInLineComment
				&& !isInQuote && !doesLineStartComment && !isInPreprocessor
				&& !isBeforeComment()) {
			int len = formattedLine.length();
			if (len > 0 && isWhiteSpace(formattedLine.charAt(len - 1)))
				isAfterFormattedWhiteSpace = true;
		}

		previousChar = currentChar;
		if (!isWhiteSpace(currentChar)) {
			previousNonWSChar = currentChar;
			if (!isInComment && !isInLineComment && !isInQuote
					&& !isSequenceReached(AS_OPEN_COMMENT)
					&& !isSequenceReached(AS_OPEN_LINE_COMMENT)) {
				previousCommandChar = previousNonWSChar;
			}
		}

		int currentLineLength = currentLine.length();

		if (charNum + 1 < currentLineLength
				&& (!isWhiteSpace(peekNextChar()) || isInComment || isInLineComment)) {
			currentChar = currentLine.charAt(++charNum);
			if (isAfterFormattedWhiteSpace) {
				while (isWhiteSpace(currentChar)
						&& charNum + 1 < currentLineLength) {
					currentChar = currentLine.charAt(++charNum);
				}
			}

			if (shouldConvertTabs && currentChar == '\t') {
				currentChar = ' ';
			}

			return true;
		} else {
			if (sourceIterator.hasMoreLines()) {
				currentLine = sourceIterator.nextLine();
				// FIXME: once currentLine could get null
				if (currentLine.length() == 0) {
					// FIXME: think about it
					currentLine = " ";
				}

				// unless reading in the first line of the file,
				// break a new line.
				if (!isVirgin) {
					isInLineBreak = true;
				} else {
					isVirgin = false;
				}

				isInLineComment = false;

				trimNewLine();
				currentChar = currentLine.charAt(charNum);

				// check if is in preprocessor right after the line break and
				// line trimming
				if (previousNonWSChar != '\\') {
					isInPreprocessor = false;
				}

				if (shouldConvertTabs && currentChar == '\t') {
					currentChar = ' ';
				}

				return true;
			} else {
				endOfCodeReached = true;
				return false;
			}
		}
	}

	/**
	 * peek at the next unread character.
	 * 
	 * @return the next unread character.
	 */
	private char peekNextChar() {
		int peekNum = charNum + 1;
		int len = currentLine.length();
		char ch = ' ';

		while (peekNum < len) {
			ch = currentLine.charAt(peekNum++);
			if (!isWhiteSpace(ch)) {
				return ch;
			}
		}

		if (shouldConvertTabs && ch == '\t') {
			ch = ' ';
		}

		return ch;
	}

	/**
	 * check if current placement is before a comment or line-comment
	 * 
	 * @return is before a comment or line-comment.
	 */
	private boolean isBeforeComment() {
		int peekNum;
		int len = currentLine.length();
		boolean foundComment = false;

		for (peekNum = charNum + 1; peekNum < len
				&& isWhiteSpace(currentLine.charAt(peekNum)); ++peekNum)
			;

		if (peekNum < len) {
			foundComment = currentLine.regionMatches(peekNum, AS_OPEN_COMMENT,
					0, 2)
					|| currentLine.regionMatches(peekNum, AS_OPEN_LINE_COMMENT,
							0, 2);
		}
		return foundComment;
	}

	/**
	 * jump over the leading white space in the current line, IF the line does
	 * not begin a comment or is in a preprocessor definition.
	 */
	private void trimNewLine() {
		int len = currentLine.length();
		charNum = 0;

		if (isInComment || isInPreprocessor)
			return;

		while (isWhiteSpace(currentLine.charAt(charNum)) && charNum + 1 < len)
			++charNum;

		doesLineStartComment = false;
		if (isSequenceReached("/*")) {
			charNum = 0;
			doesLineStartComment = true;
		}
	}

	/**
	 * check if the currently reached open-bracket (i.e. '{') opens a definition
	 * type block (such as a class or namespace), a command block (such as a
	 * method block) or a static array.
	 * 
	 * This method takes for granted that the current character is an opening
	 * bracket.
	 * 
	 * @return the type of the opened block.
	 */
	private int getBracketType() {
		int returnVal = 0;
		if (foundPreDefinitionHeader) {
			returnVal = DEFINITION_TYPE;
		} else {
			boolean isCommandType;
			isCommandType = foundPreCommandHeader
					|| (currentHeader != null && isNonParenHeader)
					|| (previousCommandChar == ')')
					|| (previousCommandChar == ':' && !foundQuestionMark)
					|| (previousCommandChar == ';')
					|| ((previousCommandChar == '{' || previousCommandChar == '}') && isPreviousBracketBlockRelated);
			returnVal = isCommandType ? COMMAND_TYPE : ARRAY_TYPE;
		}
		if (isOneLineBlockReached()) {
			returnVal |= SINGLE_LINE_TYPE;
		}
		return returnVal;
	}

	/**
	 * check if the currently reached '*' or '&' character is a pointer- or
	 * reference symbol, or another operator. This method takes for granted that
	 * the current character is either a '*' or '&'.
	 * 
	 * @return whether current character is a pointer- or reference symbol.
	 */
	private boolean isPointerOrReference() {
		boolean isPR = !isInPotentialCalculation
				|| IS_A(bracketTypeStack.back(), DEFINITION_TYPE)
				|| (!isLegalNameChar(previousNonWSChar)
						&& previousNonWSChar != ')' && previousNonWSChar != ']');
		if (!isPR) {
			char nextChar = peekNextChar();
			isPR |= !isWhiteSpace(nextChar) && nextChar != '-'
					&& nextChar != '(' && nextChar != '['
					&& !isLegalNameChar(nextChar);
		}
		return isPR;
	}

	/**
	 * check if the currently reached '-' character is a urinary minus. This
	 * method takes for granted that the current character is a '-'.
	 * 
	 * @return whether the current '-' is a urinary minus.
	 */
	private boolean isUrinaryMinus() {
		return (previousOperator.equals(AS_RETURN) || !Character
				.isLetterOrDigit(previousCommandChar))
				&& previousCommandChar != '.'
				&& previousCommandChar != ')'
				&& previousCommandChar != ']';
	}

	/**
	 * <p>
	 * check if the currently reached '-' or '+' character is part of an
	 * exponent, i.e. 0.2E-5.
	 * </p>
	 * 
	 * This method takes for granted that the current character is a '-' or '+'.
	 * 
	 * @return whether the current char is in an exponent.
	 */
	private boolean isInExponent() {
		int formattedLineLength = formattedLine.length();
		if (formattedLineLength >= 2) {
			char prevPrevFormattedChar = formattedLine
					.charAt(formattedLineLength - 2);
			char prevFormattedChar = formattedLine
					.charAt(formattedLineLength - 1);
			return ((prevFormattedChar == 'e' || prevFormattedChar == 'E'))
					&& (prevPrevFormattedChar == '.' || Character
							.isDigit(prevPrevFormattedChar));
		} else {
			return false;
		}
	}

	/**
	 * <p>
	 * check if a one-line bracket has been reached, i.e. if the currently
	 * reached '{' character is closed with a complimentry '}' elsewhere on the
	 * current line.
	 * </p>
	 * 
	 * @return has a one-line bracket been reached?
	 */
	private boolean isOneLineBlockReached() {
		boolean isInComment = false;
		boolean isInQuote = false;
		int bracketCount = 1;
		int currentLineLength = currentLine.length();
		int i = 0;
		char ch = ' ';
		char quoteChar = ' ';

		for (i = charNum + 1; i < currentLineLength; ++i) {
			ch = currentLine.charAt(i);
			if (isInComment) {
				if (currentLine.startsWith("*/", i)) {
					isInComment = false;
					++i;
				}
				continue;
			}
			if (ch == '\\') {
				++i;
				continue;
			}
			if (isInQuote) {
				if (ch == quoteChar) {
					isInQuote = false;
				}
				continue;
			}
			if (ch == '"' || ch == '\'') {
				isInQuote = true;
				quoteChar = ch;
				continue;
			}
			if (currentLine.startsWith("//", i)) {
				break;
			}
			if (currentLine.startsWith("/*", i)) {
				isInComment = true;
				++i;
				continue;
			}
			if (ch == '{') {
				++bracketCount;
			} else if (ch == '}') {
				--bracketCount;
			}
			if (bracketCount == 0) {
				return true;
			}
		} // for
		return false;
	}

	private void appendChar(char ch) {
		appendChar(ch, true);
	}

	private void appendCurrentChar() {
		appendCurrentChar(true);
	}

	private void appendSequence(String sequence) {
		appendSequence(sequence, true);
	}

	/**
	 * append a character to the current formatted line. Unless disabled (via
	 * canBreakLine == false), first check if a line-break has been registered,
	 * and if so break the formatted line, and only then append the character
	 * into the next formatted line.
	 * 
	 * @param ch
	 *            the character to append.
	 * @param canBreakLine
	 *            if true, a registered line-break
	 */
	private void appendChar(char ch, boolean canBreakLine) {
		if (canBreakLine && isInLineBreak) {
			breakLine();
		}
		formattedLine.append(ch);
	}

	/**
	 * append the CURRENT character (curentChar) to the current formatted line.
	 * 
	 * @param canBreakLine
	 *            if true, a registered line-break
	 */
	private void appendCurrentChar(boolean canBreakLine) {
		appendChar(currentChar, canBreakLine);
	}

	/**
	 * append a String sequence to the current formatted line. Unless disabled
	 * (via canBreakLine == false), first check if a line-break has been
	 * registered, and if so break the formatted line, and only then append the
	 * sequence into the next formatted line.
	 * 
	 * @param sequence
	 *            the sequence to append.
	 * @param canBreakLine
	 *            if true, a registered line-break
	 */
	private void appendSequence(String sequence, boolean canBreakLine) {
		if (canBreakLine && isInLineBreak) {
			breakLine();
		}
		formattedLine.append(sequence);
	}

	/**
	 * append a space to the current formattedline, UNLESS the last character is
	 * already a white-space character.
	 */
	private void appendSpacePad() {
		int len = formattedLine.length();
		if (len == 0 || !isWhiteSpace(formattedLine.charAt(len - 1))) {
			formattedLine.append(' ');
		}
	}

	/**
	 * register a line break for the formatted line.
	 */
	private void breakLine() {
		isLineReady = true;
		isInLineBreak = false;
		// queue an empty line prepend request if one exists
		prependEmptyLine = isPrependPostBlockEmptyLineRequested;
		readyFormattedLine = formattedLine.toString();
		if (isAppendPostBlockEmptyLineRequested) {
			isAppendPostBlockEmptyLineRequested = false;
			isPrependPostBlockEmptyLineRequested = true;
		} else {
			isPrependPostBlockEmptyLineRequested = false;
		}
		formattedLine = new StringBuffer();
	}

	/**
	 * check if a specific sequence exists in the current placement of the
	 * current line.
	 * 
	 * @param sequence
	 *            the sequence to be checked
	 * @return whether sequence has been reached.
	 */
	private final boolean isSequenceReached(String sequence) {
		return currentLine.regionMatches(charNum, sequence, 0, sequence
				.length());
	}

	private String findHeader(StringStack headers) {
		return findHeader(headers, true);
	}

	/**
	 * check if one of a set of headers has been reached in the current position
	 * of the current line.
	 * 
	 * @param headers
	 *            a vector of headers
	 * @param checkBoundry
	 * @return a pointer to the found header, or a null if no header has been
	 *         reached.
	 */
	private String findHeader(StringStack headers, boolean checkBoundry) {
		return super.findHeader(currentLine, charNum, headers, checkBoundry);
	}

	/**
	 * initialization of static data of ASFormatter.
	 */
	private void staticInit() {
		if (calledInitStatic) {
			return;
		}

		calledInitStatic = true;

		headers.push_back(AS_IF);
		headers.push_back(AS_ELSE);
		headers.push_back(AS_DO);
		headers.push_back(AS_WHILE);
		headers.push_back(AS_FOR);
		headers.push_back(AS_SYNCHRONIZED);
		headers.push_back(AS_TRY);
		headers.push_back(AS_CATCH);
		headers.push_back(AS_FINALLY);
		headers.push_back(AS_SWITCH);
		headers.push_back(AS_TEMPLATE);

		nonParenHeaders.push_back(AS_ELSE);
		nonParenHeaders.push_back(AS_DO);
		nonParenHeaders.push_back(AS_TRY);
		nonParenHeaders.push_back(AS_FINALLY);
		// nonParenHeaders.push_back(AS_TEMPLATE);

		preDefinitionHeaders.push_back(AS_CLASS);
		preDefinitionHeaders.push_back(AS_INTERFACE);
		preDefinitionHeaders.push_back(AS_NAMESPACE);
		preDefinitionHeaders.push_back(AS_STRUCT);

		preCommandHeaders.push_back(AS_EXTERN);
		preCommandHeaders.push_back(AS_THROWS);
		preCommandHeaders.push_back(AS_CONST);

		preprocessorHeaders.push_back(AS_BAR_DEFINE);
		// DEVEL: removed the following lines
		// preprocessorHeaders.push_back(AS_BAR_INCLUDE);
		// preprocessorHeaders.push_back(AS_BAR_IF); // #if or #ifdef
		// preprocessorHeaders.push_back(AS_BAR_EL); // #else or #elif
		// preprocessorHeaders.push_back(AS_BAR_ENDIF);

		operators.push_back(AS_PLUS_ASSIGN);
		operators.push_back(AS_MINUS_ASSIGN);
		operators.push_back(AS_MULT_ASSIGN);
		operators.push_back(AS_DIV_ASSIGN);
		operators.push_back(AS_MOD_ASSIGN);
		operators.push_back(AS_OR_ASSIGN);
		operators.push_back(AS_AND_ASSIGN);
		operators.push_back(AS_XOR_ASSIGN);
		operators.push_back(AS_EQUAL);
		operators.push_back(AS_PLUS_PLUS);
		operators.push_back(AS_MINUS_MINUS);
		operators.push_back(AS_NOT_EQUAL);
		operators.push_back(AS_GR_EQUAL);
		operators.push_back(AS_GR_GR_GR_ASSIGN);
		operators.push_back(AS_GR_GR_ASSIGN);
		operators.push_back(AS_GR_GR_GR);
		operators.push_back(AS_GR_GR);
		operators.push_back(AS_LS_EQUAL);
		operators.push_back(AS_LS_LS_LS_ASSIGN);
		operators.push_back(AS_LS_LS_ASSIGN);
		operators.push_back(AS_LS_LS_LS);
		operators.push_back(AS_LS_LS);
		operators.push_back(AS_ARROW);
		operators.push_back(AS_AND);
		operators.push_back(AS_OR);
		operators.push_back(AS_COLON_COLON);

		// BUGFIX: removed the following lines:
		// operators.push_back(AS_PAREN_PAREN);
		// operators.push_back(AS_BLPAREN_BLPAREN);

		operators.push_back(AS_PLUS);
		operators.push_back(AS_MINUS);
		operators.push_back(AS_MULT);
		operators.push_back(AS_DIV);
		operators.push_back(AS_MOD);
		operators.push_back(AS_QUESTION);
		operators.push_back(AS_COLON);
		operators.push_back(AS_ASSIGN);
		operators.push_back(AS_LS);
		operators.push_back(AS_GR);
		operators.push_back(AS_NOT);
		operators.push_back(AS_BIT_OR);
		operators.push_back(AS_BIT_AND);
		operators.push_back(AS_BIT_NOT);
		operators.push_back(AS_BIT_XOR);
		operators.push_back(AS_OPERATOR);
		operators.push_back(AS_COMMA);
		// operators.push_back(AS_SEMICOLON);
		operators.push_back(AS_RETURN);

		assignmentOperators.push_back(AS_PLUS_ASSIGN);
		assignmentOperators.push_back(AS_MINUS_ASSIGN);
		assignmentOperators.push_back(AS_MULT_ASSIGN);
		assignmentOperators.push_back(AS_DIV_ASSIGN);
		assignmentOperators.push_back(AS_MOD_ASSIGN);
		assignmentOperators.push_back(AS_XOR_ASSIGN);
		assignmentOperators.push_back(AS_OR_ASSIGN);
		assignmentOperators.push_back(AS_AND_ASSIGN);
		assignmentOperators.push_back(AS_GR_GR_GR_ASSIGN);
		assignmentOperators.push_back(AS_LS_LS_LS_ASSIGN);
		assignmentOperators.push_back(AS_ASSIGN);
	}

	// STATIC FIELDS

	private static boolean calledInitStatic = false;

	private static StringStack headers = new StringStack();

	private static StringStack nonParenHeaders = new StringStack();

	private static StringStack preprocessorHeaders = new StringStack();

	private static StringStack preDefinitionHeaders = new StringStack();

	private static StringStack preCommandHeaders = new StringStack();

	private static StringStack operators = new StringStack();

	private static StringStack assignmentOperators = new StringStack();

	// MEMBER FIELDS

	private ASSourceIterator sourceIterator;

	private StringStack preBracketHeaderStack;

	private IntegerStack bracketTypeStack;

	private IntegerStack parenStack;

	private String readyFormattedLine;

	private String currentLine;

	private StringBuffer formattedLine;

	private String currentHeader;

	private String previousOperator;

	private char currentChar;

	private char previousChar;

	private char previousNonWSChar;

	private char previousCommandChar;

	private char quoteChar;

	private int charNum;

	private int bracketFormatMode;

	private boolean isVirgin;

	private boolean shouldPadOperators;

	private boolean shouldPadParenthesies;

	private boolean shouldConvertTabs;

	private boolean isInLineComment;

	private boolean isInComment;

	private boolean isInPreprocessor;

	private boolean isInTemplate; // true both in template definitions (e.g.
									// template<class A>) and template usage
									// (e.g. F<int>).

	private boolean doesLineStartComment;

	private boolean isInQuote;

	private boolean isSpecialChar;

	private boolean isNonParenHeader;

	private boolean foundQuestionMark;

	private boolean foundPreDefinitionHeader;

	private boolean foundPreCommandHeader;

	private boolean isInLineBreak;

	private boolean isInClosingBracketLineBreak;

	private boolean endOfCodeReached;

	private boolean isLineReady;

	private boolean isPreviousBracketBlockRelated;

	private boolean isInPotentialCalculation;

	// private boolean foundOneLineBlock;
	private boolean shouldBreakOneLineBlocks;

	private boolean shouldReparseCurrentChar;

	private boolean shouldBreakOneLineStatements;

	private boolean shouldBreakLineAfterComments;

	private boolean shouldBreakClosingHeaderBrackets;

	private boolean shouldBreakElseIfs;

	private boolean passedSemicolon;

	private boolean passedColon;

	private boolean isImmediatelyPostComment;

	private boolean isImmediatelyPostEmptyBlock;

	private boolean shouldBreakBlocks;

	private boolean shouldBreakClosingHeaderBlocks;

	private boolean isPrependPostBlockEmptyLineRequested;

	private boolean isAppendPostBlockEmptyLineRequested;

	private boolean prependEmptyLine;

	private boolean foundClosingHeader;

	private int previousReadyFormattedLineLength;

	private boolean isInHeader;

	private boolean isImmediatelyPostHeader;

	// CONSTANTS

	// bracket types
	private final static int DEFINITION_TYPE = 1;

	private final static int COMMAND_TYPE = 2;

	private final static int ARRAY_TYPE = 4;

	private final static int SINGLE_LINE_TYPE = 8;

}

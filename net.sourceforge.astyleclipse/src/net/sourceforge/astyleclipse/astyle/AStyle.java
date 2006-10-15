/*
 * :tabSize=8:indentSize=4:noTabs=true:maxLineLen=0:
 *
 * Copyright (c) 1998,1999,2000,2001 Tal Davidson. All rights reserved.
 *
 * AStyle.java
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

import java.io.*;
import java.util.*;

import net.sourceforge.astyleclipse.AstyleLog;
import net.sourceforge.astyleclipse.preferences.PreferenceConstants;

/**
 * the <b>AStyle</b> main class.
 */
public class AStyle implements ASResource {

	public static final String VERSION = "1.14.1";

	// changed for use
	public AStyle() {
	}

	public static void main(String[] argv) {
		ASFormatter formatter = new ASFormatter();
		Vector fileNameVector = new Vector();
		Vector optionsVector = new Vector();
		String optionsFileName = "";
		boolean ok = true;
		boolean shouldPrintHelp = false;
		boolean shouldParseOptionsFile = true;

		_err = System.err;
		_suffix = ".orig";
		_modeManuallySet = false;

		// manage flags
		for (int i = 0; i < argv.length; i++) {
			String arg = argv[i];
			if (IS_PARAM_OPTION(arg, "--options=none")) {
				shouldParseOptionsFile = false;
			} else if (IS_PARAM_OPTION(arg, "--options=")) {
				optionsFileName = GET_PARAM(arg, "--options=");
			} else if (IS_OPTION(arg, "-h") || IS_OPTION(arg, "--help")
					|| IS_OPTION(arg, "-?")) {
				shouldPrintHelp = true;
			} else if (arg.charAt(0) == '-') {
				optionsVector.addElement(arg);
			} else {
				// file-name
				fileNameVector.addElement(arg);
			}
		}

		// parse options file
		if (shouldParseOptionsFile) {
			if (optionsFileName.length() == 0) {
				String env = System.getProperty("ASTYLE_OPTIONS");
				if (env != null) {
					optionsFileName = env;
				}
			}

			if (optionsFileName.length() == 0) {
				String env = System.getProperty("user.home");
				if (env != null) {
					optionsFileName = env + "/.astylerc";
					File optFile = new File(optionsFileName);
					if (!optFile.exists() || !optFile.canRead()) {
						optionsFileName = "";
						optFile = null;
					}
				}
			}

			if (optionsFileName.length() != 0)
				try {
					BufferedReader optionsIn = new BufferedReader(
							new FileReader(optionsFileName));
					Vector fileOptionsVector = new Vector();
					try {
						importOptions(optionsIn, fileOptionsVector);
						ok = parseOptions(formatter, fileOptionsVector,
								"Unknown option in default options file: ");
						optionsIn.close();
					} catch (IOException ioex) {
						_err.println("Error reading options file: "
								+ optionsFileName);
						ok = false;
					}
					if (!ok) {
						_err.println("For help on options, type 'astyle -h'.");
					}
				} catch (FileNotFoundException fnfex) {
					error("Could not open astyle options file:",
							optionsFileName);
				}
		}

		// parse options from command line
		ok = parseOptions(formatter, optionsVector,
				"Unknown command line option: ");
		if (!ok) {
			_err.println("For help on options, type 'astyle -h'.");
			System.exit(1);
		}

		if (shouldPrintHelp) {
			printHelp();
			System.exit(1);
		}

		// if no files have been given, use System.in for input and System.out
		// for output
		if (fileNameVector.isEmpty()) {
			formatter.init(new ASStreamIterator(System.in));
			while (formatter.hasMoreLines()) {
				System.out.print(formatter.nextLine());
				if (formatter.hasMoreLines()) {
					System.out.println();
				}
			}
			System.out.flush();
		} else {
			// indent the given files
			for (int i = 0; i < fileNameVector.size(); i++) {
				String origFileName = (String) fileNameVector.elementAt(i);
				String inFileName = origFileName + _suffix;
				File origFile = new File(origFileName);
				File inFile = new File(inFileName);

				if (inFile.exists() && !inFile.delete()) {
					error("Could not delete file", inFile.toString());
				}

				if (!origFile.renameTo(inFile)) {
					error("Could not rename", origFile.toString() + " to "
							+ inFile.toString());
				}

				BufferedReader in = null;
				try {
					in = new BufferedReader(new FileReader(inFile));
				} catch (FileNotFoundException fnfex) {
					error("Could not open input file", inFile.toString());
				}

				BufferedWriter out = null;
				try {
					out = new BufferedWriter(new FileWriter(origFile));
				} catch (IOException ioex) {
					error("Could not open output file", origFile.toString());
				}

				// Unless a specific language mode has been, set the language
				// mode according to the file's suffix.
				if (!_modeManuallySet) {
					if (origFileName.endsWith(".java")) {
						formatter.setCStyle(false);
					} else {
						formatter.setCStyle(true);
					}
				}

				formatter.init(new ASStreamIterator(in));
				try {
					while (formatter.hasMoreLines()) {
						String line = formatter.nextLine();
						out.write(line, 0, line.length());
						if (formatter.hasMoreLines()) {
							out.newLine();
						}
					}
					out.flush();
				} catch (IOException ioex) {
					error("Could not write to output file", origFile.toString());
				}

				try {
					out.close();
				} catch (IOException ioex) {
					error("could not be closed", origFile.toString());
				}
				try {
					in.close();
				} catch (IOException ioex) {
					error("Could not be closed", inFile.toString());
				}
			}
		}
		return;
	}

	private static final boolean IS_OPTION(String arg, String op) {
		return arg.equals(op);
	}

	private static final boolean IS_OPTIONS(String arg, String a, String b) {
		return IS_OPTION(arg, a) || IS_OPTION(arg, b);
	}

	private static final boolean IS_PARAM_OPTION(String arg, String op) {
		return arg.startsWith(op);
	}

	private static final boolean IS_PARAM_OPTIONS(String arg, String a, String b) {
		return IS_PARAM_OPTION(arg, a) || IS_PARAM_OPTION(arg, b);
	}

	private static final String GET_PARAM(String arg, String op) {
		return arg.substring(op.length());
	}

	private static final String GET_PARAMS(String arg, String a, String b) {
		return IS_PARAM_OPTION(arg, a) ? GET_PARAM(arg, a) : GET_PARAM(arg, b);
	}

	private static final int GET_NUM_PARAM(String arg, int defaultValue) {
		int num = defaultValue;
		if (arg.length() > 0)
			try {
				num = Integer.parseInt(arg);
			} catch (NumberFormatException ex) {
			}
		return num;
	}

	private static void error(String why, String what) {
		_err.println(why + ' ' + what);
		System.exit(1);
	}
	/**
	 * @param formatter
	 * @param source
	 * @return
	 */
	public static String formatString(ASFormatter formatter, String source) {
		StringBuffer target = new StringBuffer("");
		BufferedReader in = null;
		in = new BufferedReader(new StringReader(source));
		formatter.init(new ASStreamIterator(in));
		while (formatter.hasMoreLines()) {
			String line = formatter.nextLine();
			// System.out.println(line);
			target.append(line);
			if (formatter.hasMoreLines()) {
				target.append("\n");
			}
		}
		return target.toString();
	}
	public static boolean parseOptions(ASFormatter formatter,String options) 
	{
		Vector<String> optionsVector = new Vector<String>();
		// check whether it is empty string
		if(options.trim().length()==0)
			return true;
		//	split with space
		String [] optionList = options.split("\\s+");
		for(int i=0;i<optionList.length;i++) {
			optionsVector.addElement(optionList[i]);
			// AstyleLog.logInfo("data is [" + optionList[i] +"]");
		}
		// TODO, import file options to vector
		String errorInfo="";
		return parseOptions(formatter,optionsVector,errorInfo);
	}

	public static boolean parseOptionFile(ASFormatter formatter,
			String optionFile) {
		Boolean ok = true;
		// parse file
		BufferedReader optionsIn;
		try {
			optionsIn = new BufferedReader(new FileReader(optionFile));
			Vector <String> optionsVector = new Vector <String>();
			importOptions(optionsIn,optionsVector);
			ok = parseOptions(formatter, optionsVector,
					"Unknown option in default options file: ");
			optionsIn.close();
		} catch (IOException ioex) {
			AstyleLog.logError("Error when handling file", ioex);
			ok = false;
		}

		return ok;
	}

	public static boolean parseOptions(ASFormatter formatter, Vector options,
			String errorInfo) {
		boolean ok = true;
		String subArg = "";
		String arg;

		for (Enumeration e = options.elements(); e.hasMoreElements();) {
			arg = e.nextElement().toString();
			if (arg.startsWith("--")) {
				ok &= parseOption(formatter, arg.substring(2), errorInfo);
			} else if (arg.charAt(0) == '-') {
				for (int i = 1; i < arg.length(); ++i) {
					if (Character.isLetter(arg.charAt(i)) && i > 1) {
						ok &= parseOption(formatter, subArg, errorInfo);
						subArg = "";
					}
					subArg.concat(String.valueOf(arg.charAt(i)));
				}
				ok &= parseOption(formatter, subArg, errorInfo);
				subArg = "";
			} else {
				ok &= parseOption(formatter, arg, errorInfo);
				subArg = "";
			}
		}
		return ok;
	}

	private static boolean parseOption(ASFormatter formatter, String arg,
			String errorInfo) {
		if (IS_PARAM_OPTION(arg, "suffix=")) {
			String suffixParam = GET_PARAM(arg, "suffix=");
			if (suffixParam.length() > 0) {
				_suffix = suffixParam;
			}
		} else if (IS_OPTION(arg, "style=ansi")) {
			formatter.setBracketIndent(false);
			formatter.setSpaceIndentation(4);
			formatter.setBracketFormatMode(BREAK_MODE);
			formatter.setClassIndent(false);
			formatter.setSwitchIndent(false);
			formatter.setNamespaceIndent(false);
		} else if (IS_OPTION(arg, "style=gnu")) {
			formatter.setBlockIndent(true);
			formatter.setSpaceIndentation(2);
			formatter.setBracketFormatMode(BREAK_MODE);
			formatter.setClassIndent(false);
			formatter.setSwitchIndent(false);
			formatter.setNamespaceIndent(false);
		} else if (IS_OPTION(arg, "style=java")) {
			manuallySetJavaStyle(formatter);
			formatter.setBracketIndent(false);
			formatter.setSpaceIndentation(4);
			formatter.setBracketFormatMode(ATTACH_MODE);
			formatter.setSwitchIndent(false);
		} else if (IS_OPTION(arg, "style=kr")) {
			// manuallySetCStyle(formatter);
			formatter.setBracketIndent(false);
			formatter.setSpaceIndentation(4);
			formatter.setBracketFormatMode(ATTACH_MODE);
			formatter.setClassIndent(false);
			formatter.setSwitchIndent(false);
			formatter.setNamespaceIndent(false);
		} else if (IS_OPTION(arg, "style=linux")) {
			formatter.setBracketIndent(false);
			formatter.setSpaceIndentation(8);
			formatter.setBracketFormatMode(BDAC_MODE);
			formatter.setClassIndent(false);
			formatter.setSwitchIndent(false);
			formatter.setNamespaceIndent(false);
		} else if (IS_OPTIONS(arg, "c", "mode=c")) {
			manuallySetCStyle(formatter);
		} else if (IS_OPTIONS(arg, "j", "mode=java")) {
			manuallySetJavaStyle(formatter);
		} else if (IS_OPTIONS(arg, "t", "indent=tab=")) {
			String spaceNumParam = GET_PARAMS(arg, "t", "indent=tab=");
			formatter.setTabIndentation(GET_NUM_PARAM(spaceNumParam, 4));
			formatter.setForceTabs(false);
		} else if (IS_OPTIONS(arg, "T", "force-indent=tab=")) {
			String spaceNumParam = GET_PARAMS(arg, "T", "force-indent=tab=");
			formatter.setTabIndentation(GET_NUM_PARAM(spaceNumParam, 4));
			formatter.setForceTabs(true);
		} else if (IS_PARAM_OPTION(arg, "indent=tab")) {
			formatter.setTabIndentation(4);
			formatter.setForceTabs(false);
		} else if (IS_PARAM_OPTIONS(arg, "s", "indent=spaces=")) {
			String spaceNumParam = GET_PARAMS(arg, "s", "indent=spaces=");
			formatter.setSpaceIndentation(GET_NUM_PARAM(spaceNumParam, 4));
		} else if (IS_PARAM_OPTION(arg, "indent=spaces")) {
			formatter.setSpaceIndentation(4);
		} else if (IS_PARAM_OPTIONS(arg, "m", "min-conditional-indent=")) {
			String minIndentParam = GET_PARAMS(arg, "m",
					"min-conditional-indent=");
			formatter.setMinConditionalIndentLength(GET_NUM_PARAM(
					minIndentParam, 0));
		} else if (IS_PARAM_OPTIONS(arg, "M", "max-instatement-indent=")) {
			String maxIndentParam = GET_PARAMS(arg, "M",
					"max-instatement-indent=");
			formatter.setMaxInStatementIndentLength(GET_NUM_PARAM(
					maxIndentParam, 40));
		} else if (IS_OPTIONS(arg, "B", "indent-brackets")) {
			formatter.setBracketIndent(true);
		} else if (IS_OPTIONS(arg, "G", "indent-blocks")) {
			formatter.setBlockIndent(true);
		} else if (IS_OPTIONS(arg, "N", "indent-namespaces")) {
			formatter.setNamespaceIndent(true);
		} else if (IS_OPTIONS(arg, "C", "indent-classes")) {
			formatter.setClassIndent(true);
		} else if (IS_OPTIONS(arg, "S", "indent-switches")) {
			formatter.setSwitchIndent(true);
		} else if (IS_OPTIONS(arg, "K", "indent-cases")) {
			formatter.setCaseIndent(true);
		} else if (IS_OPTIONS(arg, "L", "indent-labels")) {
			formatter.setLabelIndent(true);
		} else if (IS_OPTION(arg, "brackets=break-closing-headers")) {
			formatter.setBreakClosingHeaderBracketsMode(true);
		} else if (IS_OPTIONS(arg, "b", "brackets=break")) {
			formatter.setBracketFormatMode(BREAK_MODE);
		} else if (IS_OPTIONS(arg, "a", "brackets=attach")) {
			formatter.setBracketFormatMode(ATTACH_MODE);
		} else if (IS_OPTIONS(arg, "l", "brackets=linux")) {
			formatter.setBracketFormatMode(BDAC_MODE);
		} else if (IS_OPTIONS(arg, "O", "one-line=keep-blocks")) {
			formatter.setBreakOneLineBlocksMode(false);
		} else if (IS_OPTIONS(arg, "o", "one-line=keep-statements")) {
			formatter.setSingleStatementsMode(false);
		} else if (IS_OPTION(arg, "pad=paren")) {
			formatter.setParenthesisPaddingMode(true);
		} else if (IS_OPTIONS(arg, "P", "pad=all")) {
			formatter.setOperatorPaddingMode(true);
			formatter.setParenthesisPaddingMode(true);
		} else if (IS_OPTIONS(arg, "p", "pad=oper")) {
			formatter.setOperatorPaddingMode(true);
		} else if (IS_OPTIONS(arg, "E", "fill-empty-lines")) {
			formatter.setEmptyLineFill(true);
		} else if (IS_OPTION(arg, "indent-preprocessor")) {
			formatter.setPreprocessorIndent(true);
		} else if (IS_OPTION(arg, "convert-tabs")) {
			formatter.setTabSpaceConversionMode(true);
		} else if (IS_OPTION(arg, "break-blocks=all")) {
			formatter.setBreakBlocksMode(true);
			formatter.setBreakClosingHeaderBlocksMode(true);
		} else if (IS_OPTION(arg, "break-blocks")) {
			formatter.setBreakBlocksMode(true);
		} else if (IS_OPTION(arg, "break-elseifs")) {
			formatter.setBreakElseIfsMode(true);
		} else if (IS_OPTIONS(arg, "X", "errors-to-standard-output")) {
			_err = System.out;
		} else if (IS_OPTIONS(arg, "v", "version")) {
			System.out.println("AStyle " + VERSION + " (Java)");
			System.exit(1);
		} else {
			_err.println(errorInfo + arg);
			return false; // unknown option
		}
		return true;
	}

	public static void importOptions(Reader in, Vector<String> optionsVector)
			throws IOException {
		StringBuffer currentTokenBuf;
		boolean eof = false;

		while (!eof) {
			currentTokenBuf = new StringBuffer();
			do {
				int c = in.read();
				if (c == -1) {
					eof = true;
					break;
				}
				char ch = (char) c;
				if (ch == '#') { // treat '#' as line comments
					do {
						c = in.read();
						if (c == -1) {
							eof = true;
							break;
						}
						ch = (char) c;
					} while (ch != '\n' && ch != '\r');
				}
				if (!eof) {
					// break options on spaces, tabs or new-lines:
					if (ch == ' ' || ch == '\t' || ch == '\n' || ch == '\r') {
						break;
					} else {
						currentTokenBuf.append(ch);
					}
				}
			} while (!eof);
			String currentToken = currentTokenBuf.toString().trim();
			if (currentToken.length() != 0) {
				optionsVector.addElement(currentToken);
			}
		}
	}

	private static void manuallySetJavaStyle(ASFormatter formatter) {
		formatter.setCStyle(true);
		_modeManuallySet = true;
	}

	private static void manuallySetCStyle(ASFormatter formatter) {
		formatter.setCStyle(false);
		_modeManuallySet = true;
	}

	private boolean stringEndsWith(String str, String suffix) {
		return str.toLowerCase().endsWith(suffix.toLowerCase());
	}

	private static final String HELP = "\n"
			+ "AStyle "
			+ VERSION
			+ " (Java)\n"
			+ "        (http://astyle.sourceforge.net)\n"
			+ "        (created by Tal Davidson, davidsont@bigfoot.com)\n"
			+ "        (ported to Java by Dirk Moebius, dmoebius@gmx.net)\n"
			+ "\n"
			+ "Usage:  astyle [options] < Original > Beautified\n"
			+ "        astyle [options] Foo.cpp Bar.cpp  [...]\n"
			+ "\n"
			+ "When indenting a specific file, the resulting indented file RETAINS the\n"
			+ "original file-name. The original pre-indented file is renamed, with a suffix\n"
			+ "of \".orig\" added to the original filename.\n"
			+ "\n"
			+ "By default, astyle is set up to indent C/C++ files, with 4 spaces per indent,\n"
			+ "a maximal indentation of 40 spaces inside continuous statements and\n"
			+ "NO formatting.\n"
			+ "\n"
			+ "Option's Format:\n"
			+ "----------------\n"
			+ "    Long options (starting with '--') must be written one at a time.\n"
			+ "    Short options (starting with '-') may be appended together.\n"
			+ "    Thus, -bps4 is the same as -b -p -s4.\n"
			+ "\n"
			+ "Predefined Styling options:\n"
			+ "---------------------------\n"
			+ "    --style=ansi\n"
			+ "    ANSI style formatting/indenting.\n"
			+ "\n"
			+ "    --style=kr\n"
			+ "    Kernighan&Ritchie style formatting/indenting.\n"
			+ "\n"
			+ "    --style=gnu\n"
			+ "    GNU style formatting/indenting.\n"
			+ "\n"
			+ "    --style=java\n"
			+ "    Java mode, with standard java style formatting/indenting.\n"
			+ "\n"
			+ "    --style=linux\n"
			+ "    Linux mode (i.e. 8 spaces per indent, break definition-block brackets,\n"
			+ "    but attach command-block brackets).\n"
			+ "\n"
			+ "Indentation options:\n"
			+ "--------------------\n"
			+ "    -c  OR  --mode=c\n"
			+ "    Indent a C or C++ source file (default)\n"
			+ "\n"
			+ "    -j  OR  --mode=java\n"
			+ "    Indent a Java(TM) source file\n"
			+ "\n"
			+ "    -s  OR  -s#  OR  --indent=spaces=#\n"
			+ "    Indent using # spaces per indent. The default is 4 spaces per indent.\n"
			+ "\n"
			+ "    -t  OR  -t#  OR  --indent=tab=#\n"
			+ "    Indent using tab characters, assuming that each tab is # spaces long.\n"
			+ "    The default is 4 spaces per tab.\n"
			+ "\n"
			+ "    -T  OR  -T#  OR  --force-indent=tab=#\n"
			+ "    Indent using tab characters, assuming that each tab is # spaces long.\n"
			+ "    Force tabs to be used in areas, where AStyle would prefer to use spaces.\n"
			+ "\n"
			+ "    -C  OR  --indent-classes\n"
			+ "    Indent 'class' blocks, so that the inner 'public:', 'protected:' and\n"
			+ "    'private:' headers are indented in relation to the class block.\n"
			+ "\n"
			+ "    -S  OR  --indent-switches\n"
			+ "    Indent 'switch' blocks, so that the inner 'case XXX:' headers are\n"
			+ "    indented in relation to the switch block.\n"
			+ "\n"
			+ "    -K  OR  --indent-cases\n"
			+ "    Indent 'case XXX:' lines, so that they are flush with their bodies.\n"
			+ "\n"
			+ "    -N  OR  --indent-namespaces\n"
			+ "    Indent the contents of namespace blocks.\n"
			+ "\n"
			+ "    -B  OR  --indent-brackets\n"
			+ "    Add extra indentation to '{' and '}' block brackets.\n"
			+ "\n"
			+ "    -G  OR  --indent-blocks\n"
			+ "    Add extra indentation entire blocks (including brackets).\n"
			+ "\n"
			+ "    -L  OR  --indent-labels\n"
			+ "    Indent labels so that they appear one indent less than the current\n"
			+ "    indentation level, rather than being flushed completely to the left\n"
			+ "    (which is the default).\n"
			+ "\n"
			+ "    -m#  OR  --min-conditional-indent=#\n"
			+ "    Indent a minimal # spaces in a continuous conditional belonging to a\n"
			+ "    conditional header.\n"
			+ "\n"
			+ "    -M#  OR  --max-instatement-indent=#\n"
			+ "    Indent a maximal # spaces in a continuous statement, relatively to the\n"
			+ "    previous line.\n"
			+ "\n"
			+ "    -E  OR  --fill-empty-lines\n"
			+ "    Fill empty lines with the white space of their previous lines.\n"
			+ "\n"
			+ "    --indent-preprocessor\n"
			+ "    Indent multi-line #define statements.\n"
			+ "\n"
			+ "Formatting options:\n"
			+ "-------------------\n"
			+ "    -b  OR  --brackets=break\n"
			+ "    Break brackets from pre-block code (i.e. ANSI C/C++ style).\n"
			+ "\n"
			+ "    -a  OR  --brackets=attach\n"
			+ "    Attach brackets to pre-block code (i.e. Java/K&R style).\n"
			+ "\n"
			+ "    -l  OR  --brackets=linux\n"
			+ "    Break definition-block brackets and attach command-block brackets.\n"
			+ "\n"
			+ "    --brackets=break-closing-headers\n"
			+ "    Break brackets before closing headers (e.g. 'else', 'catch', ...).\n"
			+ "    Should be appended to --brackets=attach or --brackets=linux.\n"
			+ "\n"
			+ "    -o  OR  --one-line=keep-statements\n"
			+ "    Don't break lines containing multiple statements into multiple\n"
			+ "    single-statement lines.\n"
			+ "\n"
			+ "    -O  OR  --one-line=keep-blocks\n"
			+ "    Don't break blocks residing completely on one line.\n"
			+ "\n"
			+ "    -p  OR  --pad=oper\n"
			+ "    Insert space paddings around operators only.\n"
			+ "\n"
			+ "    --pad=paren\n"
			+ "    Insert space paddings around parenthesies only.\n"
			+ "\n"
			+ "    -P  OR  --pad=all\n"
			+ "    Insert space paddings around operators AND parenthesies.\n"
			+ "\n"
			+ "    --convert-tabs\n"
			+ "    Convert tabs to spaces.\n"
			+ "\n"
			+ "    --break-blocks\n"
			+ "    Insert empty lines around unrelated blocks, labels, classes, ...\n"
			+ "\n"
			+ "    --break-blocks=all\n"
			+ "    Like --break-blocks, except also insert empty lines around closing\n"
			+ "    headers (e.g. 'else', 'catch', ...).\n"
			+ "\n"
			+ "    --break-elseifs\n"
			+ "    Break 'else if()' statements into two different lines.\n"
			+ "\n"
			+ "Other options:\n"
			+ "--------------\n"
			+ "    --suffix=####\n"
			+ "    Append the suffix #### instead of '.orig' to original filename.\n"
			+ "\n"
			+ "    -X  OR  --errors-to-standard-output\n"
			+ "    Print errors and help information to standard-output rather than to\n"
			+ "    standard-error.\n"
			+ "\n"
			+ "    -v  OR  --version\n"
			+ "    Print version number.\n"
			+ "\n"
			+ "    -h  OR  -?  OR  --help\n"
			+ "    Print this help message.\n"
			+ "\n"
			+ "Default options file:\n"
			+ "---------------------\n"
			+ "    AStyle looks for a default options file in the following order:\n"
			+ "    1. The contents of the ASTYLE_OPTIONS property, if it has been set\n"
			+ "       on start of the Java VM with the -D option.\n"
			+ "    2. The file called .astylerc in the home directory. The location of the\n"
			+ "       home directory depends on the operating system and the Java VM. On this\n"
			+ "       system it is: "
			+ System.getProperty("user.home")
			+ "\n"
			+ "    If a default options file is found, the options in this file will be\n"
			+ "    parsed BEFORE the command-line options.\n"
			+ "    Options within the default option file may be written without the\n"
			+ "    preliminary '-' or '--'.\n" + "\n";

	private static void printHelp() {
		_err.print(HELP);
	}

	private static PrintStream _err = System.err;

	private static String _suffix = ".orig";

	private static boolean _modeManuallySet = false;

}

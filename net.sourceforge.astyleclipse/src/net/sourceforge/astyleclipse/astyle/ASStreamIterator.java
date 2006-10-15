/*
 * :tabSize=8:indentSize=4:noTabs=true:maxLineLen=0:
 *
 * Copyright (c) 1998,1999,2000,2001 Tal Davidson. All rights reserved.
 *
 * ASStreamIterator.java
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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.Reader;

/**
 * A default implementation of a <code>ASSourceIterator</code> that uses an
 * <code>InputStream</code> or a <code>Reader</code> to read source code
 * from.
 */
public class ASStreamIterator implements ASSourceIterator {

	public ASStreamIterator(InputStream in) {
		inStream = new BufferedReader(new InputStreamReader(in));
	}

	public ASStreamIterator(Reader in) {
		inStream = new BufferedReader(in);
	}

	public boolean hasMoreLines() {
		try {
			if(inStream != null) {
				if(inStream.ready()) {
					return true;
				}
			}
			return false;
		} catch (IOException ioex) {
			return false;
		}
	}

	public String nextLine() {
		String line = null;
		try {
			line = inStream.readLine();
		} catch (IOException ioex) {
			// FIXME: handle exception, or set line=""
			;
		}
		if (line == null) {
			try {
				inStream.close();
			} catch (IOException ioex) {
				//FIXME : handle exception, or set line="";
			}
			inStream = null;
			line = ""; // FIXME : return null will cause exception
		}
		return line;
	}

	private BufferedReader inStream;

}

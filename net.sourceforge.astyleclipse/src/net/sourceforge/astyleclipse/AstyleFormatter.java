/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package net.sourceforge.astyleclipse;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import net.sourceforge.astyleclipse.astyle.ASFormatter;
import net.sourceforge.astyleclipse.astyle.ASStreamIterator;
import net.sourceforge.astyleclipse.preferences.PreferenceConstants;
import net.sourceforge.astyleclipse.astyle.ASResource;
import net.sourceforge.astyleclipse.astyle.AStyle;

import org.eclipse.cdt.core.formatter.CodeFormatter;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.Status;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;

/**
 */
public class AstyleFormatter extends CodeFormatter {
	Map astyleOptions = new HashMap();
	Map fOptions = new HashMap();

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.org.eclipse.cdt.indent.CodeFormatter#format(int,
	 *      org.eclipse.jface.text.IDocument, int, int, int, java.lang.String)
	 */
	public TextEdit format(int kind, String source, int offset, int length,
			int indentationLevel, String lineSeparator) {
		try {
			// AstyleLog.logInfo(source);
			// kind,offset,length,indentationLevel,lineSeparator);
			ASFormatter formatter = new ASFormatter();

			// TODO : check suffix to set C or Java Style
			// filename = getSourcefileName();
			// if (filename.endsWith(".java")) {
			// formatter.setCStyle(false);
			// } else {
			// formatter.setCStyle(true);
			// }
			// 
			if(!setOptions(formatter))
				throw new Exception("parse option error");

			String target = AStyle.formatString(formatter, source);
			// AstyleLog.logInfo("result is " + target.toString());
			// TODO: format the who file in iteration 0 ;-)
		
			int textOffset = 0;
			int textLength = source.length();
			MultiTextEdit textEdit = new MultiTextEdit(textOffset, textLength);
			textEdit
					.addChild(new ReplaceEdit(textOffset, textLength, target));
			//			
			// File tempFile = File.createTempFile("indent", null);
			// //$NON-NLS-1$
			// FileOutputStream ostream = new FileOutputStream(tempFile);
			// Writer writer = new OutputStreamWriter(ostream);
			// writer.write(source.substring(offset, offset +
			// length).toCharArray());
			// writer.close(); // close file, otherwise astyle fails to open it
			// transform(tempFile.getCanonicalPath());
			// FileInputStream istream = new FileInputStream(tempFile);
			// Reader reader = new InputStreamReader(istream);
			// BufferedReader br = new BufferedReader(reader);
			// StringBuffer buffer = new StringBuffer();
			// String line;
			// while ((line = br.readLine()) != null) {
			// buffer.append(line).append(lineSeparator);
			// }
			// int bLen = buffer.length();
			// MultiTextEdit textEdit = new MultiTextEdit(offset, length);
			// textEdit.addChild(new ReplaceEdit(offset, length,
			// buffer.toString()));
			// br.close();
			return textEdit;
		} catch (Throwable e) {
			Status status = new Status(IStatus.ERROR, Activator.PLUGIN_ID,
					IStatus.ERROR, "Error", e); //$NON-NLS-1$
			Activator.getDefault().getLog().log(status);
		}
		return null;
	}

	private boolean setOptions(ASFormatter formatter) {
		Preferences prefs = Activator.getDefault().getPluginPreferences();

		String style = prefs.getString(PreferenceConstants.STYLE_CHOICE);
		String options = prefs.getString(PreferenceConstants.OTHER_OPTIONS);
		String optionFile = prefs.getString(PreferenceConstants.OPTION_FILE);	
		if(!style.equalsIgnoreCase("none")) {
			// style: style=ansi 
			if(!AStyle.parseOptions(formatter,"style="+style))
				return false;
		}
		// options: -t10
		if(!AStyle.parseOptions(formatter,options)) {
			return false;
		}
		// parse file
		if(prefs.getBoolean(PreferenceConstants.ENABLE_OPTION_FILE)) {
			return AStyle.parseOptionFile(formatter, optionFile);
		}
		return true;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.org.eclipse.cdt.indent.CodeFormatter#setOptions(java.util.Map)
	 */	
	public void setOptions(Map options) {
		fOptions.putAll(options);
	}
	
	public void setOptions(ASFormatter formatter, Map options) {
		Preferences prefs = Activator.getDefault().getPluginPreferences();

		String style = prefs.getString(PreferenceConstants.STYLE_CHOICE);
		AstyleLog.logInfo("style is " + style);
		formatter.setCStyle(true);
		if (style.equalsIgnoreCase("ansi")) {
			formatter.setBracketIndent(false);
			formatter.setSpaceIndentation(4);
			formatter.setBracketFormatMode(ASResource.BREAK_MODE);
			formatter.setClassIndent(false);
			formatter.setSwitchIndent(false);
			formatter.setNamespaceIndent(false);
		}
		if (style.equalsIgnoreCase("gnu")) {
			formatter.setBlockIndent(true);
			formatter.setSpaceIndentation(2);
			formatter.setBracketFormatMode(ASResource.BREAK_MODE);
			formatter.setClassIndent(false);
			formatter.setSwitchIndent(false);
			formatter.setNamespaceIndent(false);
		}
		if (style.equalsIgnoreCase("java")) {
			formatter.setCStyle(false);
			formatter.setBracketIndent(false);
			formatter.setSpaceIndentation(4);
			formatter.setBracketFormatMode(ASResource.ATTACH_MODE);
			formatter.setSwitchIndent(false);
		}
		if (style.equalsIgnoreCase("kr")) {
			// manuallySetCStyle(formatter);
			formatter.setBracketIndent(false);
			formatter.setSpaceIndentation(4);
			formatter.setBracketFormatMode(ASResource.ATTACH_MODE);
			formatter.setClassIndent(false);
			formatter.setSwitchIndent(false);
			formatter.setNamespaceIndent(false);
		}
		if (style.equalsIgnoreCase("linux")) {
			formatter.setBracketIndent(false);
			formatter.setSpaceIndentation(8);
			formatter.setBracketFormatMode(ASResource.BDAC_MODE);
			formatter.setClassIndent(false);
			formatter.setSwitchIndent(false);
			formatter.setNamespaceIndent(false);
		}
	}

	public boolean supportProperty(String propertyID) {
		// TODO Auto-generated method stub
		return false;
	}

	public String[][] getEnumerationProperty(String propertyID) {
		// TODO Auto-generated method stub
		String[][] empty = {{""}};
		// FINDBUGS suggest to return empty instead of null
		return empty;
	}
}

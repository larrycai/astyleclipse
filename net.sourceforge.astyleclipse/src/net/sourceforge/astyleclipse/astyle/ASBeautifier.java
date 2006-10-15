/*
 * :tabSize=8:indentSize=4:noTabs=true:maxLineLen=0:
 *
 * Copyright (c) 1998,1999,2000,2001 Tal Davidson. All rights reserved.
 *
 * ASBeautifier.java
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

// import org.gjt.sp.util.Log;

/**
 * A C/C++/Java source code indenter.
 */
public class ASBeautifier implements ASResource {

    public ASBeautifier() {
        initStatic();

        waitingBeautifierStack = null;
        activeBeautifierStack = null;
        waitingBeautifierStackLengthStack = null;
        activeBeautifierStackLengthStack = null;

        headerStack  = null;
        tempStacks = null;
        blockParenDepthStack = null;
        blockStatementStack = null;
        parenStatementStack = null;
        bracketBlockStateStack = null;
        inStatementIndentStack = null;
        inStatementIndentStackSizeStack = null;
        parenIndentStack = null;
        sourceIterator = null;

        isMinimalConditinalIndentSet = false;
        shouldForceTabIndentation = false;

        setSpaceIndentation(4);
        setMaxInStatementIndentLength(40);
        setClassIndent(false);
        setSwitchIndent(false);
        setCaseIndent(false);
        setBlockIndent(false);
        setBracketIndent(false);
        setNamespaceIndent(false);
        setLabelIndent(false);
        setEmptyLineFill(false);
        setCStyle(true);
        setPreprocessorIndent(false);
    }


    /** copy constructor */
    public ASBeautifier(ASBeautifier other) {
        this();

        headerStack  = other.headerStack.getClone();

        tempStacks = new StackStack();
        for (int i = 0; i < other.tempStacks.size(); i++) {
            StringStack s = (StringStack) other.tempStacks.at(i);
            tempStacks.push_back(s.getClone());
        }

        blockParenDepthStack = other.blockParenDepthStack.getClone();
        blockStatementStack = other.blockStatementStack.getClone();
        parenStatementStack =  other.parenStatementStack.getClone();
        bracketBlockStateStack = other.bracketBlockStateStack.getClone();
        inStatementIndentStack = other.inStatementIndentStack.getClone();
        inStatementIndentStackSizeStack = other.inStatementIndentStackSizeStack.getClone();
        parenIndentStack = other.parenIndentStack.getClone();

        sourceIterator = other.sourceIterator;

        indentString = other.indentString;
        currentHeader = other.currentHeader;
        previousLastLineHeader = other.previousLastLineHeader;
        immediatelyPreviousAssignmentOp = other.immediatelyPreviousAssignmentOp;
        isInQuote = other.isInQuote;
        isInComment = other.isInComment;
        isInCase = other.isInCase;
        isInQuestion = other.isInQuestion;
        isInStatement =other. isInStatement;
        isInHeader = other.isInHeader;
        isCStyle = other.isCStyle;
        isInFor = other.isInFor;        /// danson
        isInOperator = other.isInOperator;
        isInTemplate = other.isInTemplate;
        classIndent = other.classIndent;
        isInClassHeader = other.isInClassHeader;
        isInClassHeaderTab = other.isInClassHeaderTab;
        switchIndent = other.switchIndent;
        caseIndent = other.caseIndent;
        namespaceIndent = other.namespaceIndent;
        bracketIndent = other.bracketIndent;
        blockIndent = other.blockIndent;
        labelIndent = other.labelIndent;
        preprocessorIndent = other.preprocessorIndent;
        parenDepth = other.parenDepth;
        indentLength = other.indentLength;
        blockTabCount = other.blockTabCount;
        leadingWhiteSpaces = other.leadingWhiteSpaces;
        maxInStatementIndent = other.maxInStatementIndent;
        templateDepth = other.templateDepth;
        quoteChar = other.quoteChar;
        prevNonSpaceCh = other.prevNonSpaceCh;
        currentNonSpaceCh = other.currentNonSpaceCh;
        currentNonLegalCh = other.currentNonLegalCh;
        prevNonLegalCh = other.prevNonLegalCh;
        isInConditional = other.isInConditional;
        minConditionalIndent = other.minConditionalIndent;
        prevFinalLineSpaceTabCount = other.prevFinalLineSpaceTabCount;
        prevFinalLineTabCount = other.prevFinalLineTabCount;
        emptyLineFill = other.emptyLineFill;
        probationHeader = other.probationHeader;
        isInDefine = other.isInDefine;
        isInDefineDefinition = other.isInDefineDefinition;
        backslashEndsPrevLine = other.backslashEndsPrevLine;
        defineTabCount = other.defineTabCount;
    }


    /**
     * initialize the ASBeautifier.
     * This method should be called every time a ASBeautifier object is to
     * start beautifying a new source file.
     * <code>init()</code> receives an ASSourceIterator object that will be
     * used to iterate through the source code.
     *
     * @param  iter  a reference to the ASSourceIterator object
     */
    public void init(ASSourceIterator iter) {
        sourceIterator = iter;
        waitingBeautifierStack = new ASBeautifierStack();
        activeBeautifierStack = new ASBeautifierStack();
        waitingBeautifierStackLengthStack = new IntegerStack();
        activeBeautifierStackLengthStack = new IntegerStack();
        headerStack = new StringStack();
        tempStacks = new StackStack();
        tempStacks.push_back(new StringStack());
        blockParenDepthStack = new IntegerStack();
        blockStatementStack = new BooleanStack();
        parenStatementStack = new BooleanStack();
        bracketBlockStateStack = new BooleanStack();
        bracketBlockStateStack.push_back(true);
        inStatementIndentStack = new IntegerStack();
        inStatementIndentStackSizeStack = new IntegerStack();
        inStatementIndentStackSizeStack.push_back(0);
        parenIndentStack = new IntegerStack();

        immediatelyPreviousAssignmentOp = null;
        previousLastLineHeader = null;

        isInQuote = false;
        isInComment = false;
        isInStatement = false;
        isInCase = false;
        isInQuestion = false;
        isInClassHeader = false;
        isInClassHeaderTab = false;
        isInHeader = false;
        isInOperator = false;
        isInFor = false;
        isInTemplate = false;
        isInConditional = false;
        templateDepth = 0;
        parenDepth=0;
        blockTabCount = 0;
        leadingWhiteSpaces = 0;
        prevNonSpaceCh = '{';
        currentNonSpaceCh = '{';
        prevNonLegalCh = '{';
        currentNonLegalCh = '{';
        prevFinalLineSpaceTabCount = 0;
        prevFinalLineTabCount = 0;
        probationHeader = null;
        backslashEndsPrevLine = false;
        isInDefine = false;
        isInDefineDefinition = false;
        defineTabCount = 0;
    }


    /**
     * get indent mode.
     * @return  true, if ASBeautifier indents using tab.
     */
    public final boolean getUseTabs() {
        return indentString.equals("\t");
    }


    /**
     * set indent mode.
     * @param  useTabs  true if ASBeautifier should indent using tabs,
     *                  otherwise indent using spaces.
     */
    public final void setUseTabs(boolean useTabs) {
        if (useTabs)
            setTabIndentation(indentLength);
        else
            setSpaceIndentation(indentLength);
    }


    /**
     * get tab indentation.
     * @return  number of spaces to assume for one tab.
     */
    public final int getTabIndentation() {
        return indentLength;
    }


    /**
     * indent using one tab per indentation.
     * @param  length  assume each tab is this spaces long.
     */
    public final void setTabIndentation(int length) {
        indentString = "\t";
        indentLength = length;

        if (!isMinimalConditinalIndentSet)
            minConditionalIndent = indentLength * 2;
    }


    /**
     * get value of 'forceTabs' property.
     * @return  the value of the <code>forceTabs</code> property.
     */
    public final boolean getForceTabs() {
        return shouldForceTabIndentation;
    }


    /**
     * enforce usage of tabs.
     * @param  forceTabs  if true, the beautifier uses tabs in areas where
     *                    it otherwise would use spaces.
     */
    public final void setForceTabs(boolean forceTabs) {
        shouldForceTabIndentation = forceTabs;
    }


    /**
     * get space indentation.
     * @return  number of spaces per indent.
     */
    public final int getSpaceIndentation() {
        return indentLength;
    }


    /**
     * indent using a number of spaces per indentation.
     * @param  length  number of spaces per indent. The default is 4.
     */
    public final void setSpaceIndentation(int length) {
        StringBuffer indentBuf = new StringBuffer();

        for (int i = 0; i < length; i++)
            indentBuf.append(' ');

        indentString = indentBuf.toString();
        indentLength = length;

        if (!isMinimalConditinalIndentSet)
            minConditionalIndent = indentLength * 2;
    }


    /**
     * get maximum indentation between two lines in a multi-line statement.
     * @return  maximum indentation length.
     */
    public final int getMaxInStatementIndentLength() {
        return maxInStatementIndent;
    }


    /**
     * set the maximum indentation between two lines in a multi-line statement.
     * @param  max  maximum indentation length.
     */
    public final void setMaxInStatementIndentLength(int max) {
        maxInStatementIndent = max;
    }


    /**
     * get the minimum indentation between two lines in a multi-line condition.
     * @return  minimal indentation length. The default is twice the indent level.
     * @see #setSpaceIndentation(int)
     */
    public final int getMinConditionalIndentLength() {
        if (!isMinimalConditinalIndentSet)
            setMinConditionalIndentLength(indentLength * 2);
        return minConditionalIndent;
    }


    /**
     * set the minimum indentation between two lines in a multi-line condition.
     * @param  min  minimal indentation length.
     */
    public final void setMinConditionalIndentLength(int min) {
        minConditionalIndent = min;
        isMinimalConditinalIndentSet = true;
    }


    /**
     * get the state of the class indentation option. If true, C++ class
     * definitions will be indented one additional indent.
     * @return  state of option.
     */
    public final boolean getClassIndent() {
        return classIndent;
    }


    /**
     * set the state of the class indentation option. If true, C++ class
     * definitions will be indented one additional indent.
     * @param  state  state of option.
     */
    public final void setClassIndent(boolean state) {
        classIndent = state;
    }


    /**
     * get the state of the switch indentation option. If true, blocks of
     * 'switch' statements will be indented one additional indent.
     * @return  state of option.
     */
    public final boolean getSwitchIndent() {
        return switchIndent;
    }


    /**
     * set the state of the switch indentation option. If true, blocks of
     * 'switch' statements will be indented one additional indent.
     * @param  state  state of option.
     */
    public final void setSwitchIndent(boolean state) {
        switchIndent = state;
    }


    /**
     * get the state of the case indentation option. If true, lines of 'case'
     * statements will be indented one additional indent.
     * @return  state of option.
     */
    public final boolean getCaseIndent() {
        return caseIndent;
    }


    /**
     * set the state of the case indentation option. If true, lines of 'case'
     * statements will be indented one additional indent.
     * @param  state  state of option.
     */
    public final void setCaseIndent(boolean state) {
        caseIndent = state;
    }


    /**
     * get the state of the bracket indentation option. If true, brackets will
     * be indented one additional indent.
     * @return  state of option.
     */
    public final boolean getBracketIndent() {
        return bracketIndent;
    }


    /**
     * set the state of the bracket indentation option. If true, brackets will
     * be indented one additional indent.
     * @param  state  state of option.
     */
    public final void setBracketIndent(boolean state) {
        if (state)
            setBlockIndent(false); // so that we don't have both bracket and block indent
        bracketIndent = state;
    }


    /**
     * get the state of the block indentation option. If true, entire blocks
     * will be indented one additional indent, similar to the GNU indent style.
     * @param  state  state of option.
     */
    public final boolean getBlockIndent() {
        return blockIndent;
    }


    /**
     * set the state of the block indentation option. If true, entire blocks
     * will be indented one additional indent, similar to the GNU indent style.
     * @param  state  state of option.
     */
    public final void setBlockIndent(boolean state) {
        if (state)
            setBracketIndent(false); // so that we don't have both bracket and block indent
        blockIndent = state;
    }


    /**
     * get the state of the namespace indentation option.
     * If true, blocks of 'namespace' statements will be indented one
     * additional indent. Otherwise, NO indentation will be added.
     * @return  state of option.
     */
    public final boolean getNamespaceIndent() {
        return namespaceIndent;
    }


    /**
     * set the state of the namespace indentation option.
     * If true, blocks of 'namespace' statements will be indented one
     * additional indent. Otherwise, NO indentation will be added.
     * @param  state  state of option.
     */
    public final void setNamespaceIndent(boolean state) {
        namespaceIndent = state;
    }


    /**
     * get the state of the label indentation option.
     * If true, labels will be indented one indent LESS than the
     * current indentation level.
     * If false, labels will be flushed to the left with NO
     * indent at all.
     * @return  state of option.
     */
    public final boolean getLabelIndent() {
        return labelIndent;
    }


    /**
     * set the state of the label indentation option.
     * If true, labels will be indented one indent LESS than the
     * current indentation level.
     * If false, labels will be flushed to the left with NO
     * indent at all.
     * @param  state  state of option.
     */
    public final void setLabelIndent(boolean state) {
        labelIndent = state;
    }


    /**
     * return true, if C formatting style is on.
     * @return  true, if C formatting style is on, otherwise false.
     */
    public final boolean isCStyle() {
        return isCStyle;
    }


    /**
     * set C formatting style. Set this to true, if you want to beautify a
     * C/C++ file. If true, the beautifier performs additional indenting
     * on templates and precompiler instructions, among other things.
     * Corresponds to the options "--mode=c" and "--mode=java".
     * @param  state  if true, C formatting style is on, otherwise off.
     */
    public final void setCStyle(boolean state) {
        isCStyle = state;
    }


    /**
     * get the state of the empty line fill option.
     * If true, empty lines will be filled with the whitespace.
     * of their previous lines.
     * If false, these lines will remain empty.
     * @return  state of option.
     */
    public final boolean getEmptyLineFill() {
        return emptyLineFill;
    }


    /**
     * set the state of the empty line fill option.
     * If true, empty lines will be filled with the whitespace.
     * of their previous lines.
     * If false, these lines will remain empty.
     * @param  state  state of option.
     */
    public final void setEmptyLineFill(boolean state) {
        emptyLineFill = state;
    }


    /**
     * get the state of the preprocessor indentation option.
     * If true, multiline #define statements will be indented.
     * @return state of option.
     */
    public final boolean getPreprocessorIndent() {
        return preprocessorIndent;
    }


    /**
     * set the state of the preprocessor indentation option.
     * If true, multiline #define statements will be indented.
     * @param  state  state of option.
     */
    public final void setPreprocessorIndent(boolean state) {
        preprocessorIndent = state;
    }


    /**
     * check if there are any indented lines ready to be read by nextLine()
     * @return  are there any indented lines ready?
     */
    public boolean hasMoreLines() {
        return sourceIterator.hasMoreLines();
    }


    /**
    * get the next indented line.
    * @return  indented line.
    */
    public String nextLine() {
        return beautify(sourceIterator.nextLine());
    }


    /**
     * beautify a line of source code.
     * every line of source code in a source code file should be sent
     * one after the other to the beautify method.
     * @return  the indented line.
     * @param  originalLine  the original unindented line.
     */
    protected String beautify(String originalLine) {
        String line;
        boolean isInLineComment = false;
        boolean isInClass = false;
        boolean isInSwitch = false;
        boolean isImmediatelyAfterConst = false;
        boolean isSpecialChar = false;
        char ch = ' ';
        char prevCh;
        // TODO: how to handle eof
        if(null==originalLine){
        	return "";
        }
        StringBuffer outBuffer = new StringBuffer(); // the newly idented line is buffered here
        int tabCount = 0;
        String lastLineHeader = null;
        boolean closingBracketReached = false;
        int spaceTabCount = 0;
        char tempCh;
        int headerStackSize = headerStack.size();
        //boolean isLineInStatement = isInStatement;
        boolean shouldIndentBrackettedLine = true;
        int lineOpeningBlocksNum = 0;
        int lineClosingBlocksNum = 0;
        boolean previousLineProbation = (probationHeader != null);
        int i;

        currentHeader = null;

        // handle and remove white spaces around the line:
        // If not in comment, first find out size of white space before line,
        // so that possible comments starting in the line continue in
        // relation to the preliminary white-space.
        if (!isInComment) {
            leadingWhiteSpaces = 0;
            while (leadingWhiteSpaces < originalLine.length() && originalLine.charAt(leadingWhiteSpaces) <= 0x20)
                leadingWhiteSpaces++;

            line = originalLine.trim();
        } else {
            int trimSize;
            for (trimSize = 0;
                 trimSize < originalLine.length() && trimSize < leadingWhiteSpaces && originalLine.charAt(trimSize) <= 0x20;
                 trimSize++)
            ;
            line = originalLine.substring(trimSize);
        }

        if (line.length() == 0)
            if (emptyLineFill)
                return preLineWS(prevFinalLineSpaceTabCount, prevFinalLineTabCount);
            else
                return line;

        // handle preprocessor commands
        if (isCStyle && !isInComment && (line.charAt(0) == '#' || backslashEndsPrevLine)) {
            if (line.charAt(0) == '#') {
                String preproc = line.substring(1).trim();
                // When finding a multi-lined #define statement, the original beautifier
                // 1. sets its isInDefineDefinition flag
                // 2. clones a new beautifier that will be used for the actual indentation
                //    of the #define. This clone is put into the activeBeautifierStack in order
                //    to be called for the actual indentation.
                // The original beautifier will have isInDefineDefinition = true, isInDefine = false.
                // The cloned beautifier will have   isInDefineDefinition = true, isInDefine = true.
                if (preprocessorIndent && preproc.equals("define") && line.endsWith("\\")) {
                    if (!isInDefineDefinition) {
                        // this is the original beautifier
                        isInDefineDefinition = true;
                        // push a new beautifier into the active stack
                        // this breautifier will be used for the indentation of this define
                        ASBeautifier defineBeautifier = new ASBeautifier(this);
                        //defineBeautifier.init();
                        //defineBeautifier.isInDefineDefinition = true;
                        //defineBeautifier.beautify("");
                        activeBeautifierStack.push_back(defineBeautifier);
                    } else {
                        // this is the cloned beautifier that is in charge of indenting the #define.
                        isInDefine = true;
                    }
                }
                else if (preproc.equals("if")) {
                    // push a new beautifier into the stack
                    waitingBeautifierStackLengthStack.push_back(waitingBeautifierStack.size());
                    activeBeautifierStackLengthStack.push_back(activeBeautifierStack.size());
                    waitingBeautifierStack.push_back(new ASBeautifier(this));
                }
                else if (preproc.equals("else")) {
                    if (!waitingBeautifierStack.empty()) {
                        // MOVE current waiting beautifier to active stack.
                        activeBeautifierStack.push_back(waitingBeautifierStack.back());
                        waitingBeautifierStack.pop_back();
                    }
                }
                else if (preproc.equals("elif")) {
                    if (!waitingBeautifierStack.empty()) {
                        // append a COPY current waiting beautifier to active stack, WITHOUT deleting the original.
                        activeBeautifierStack.push_back(new ASBeautifier(waitingBeautifierStack.back()));
                    }
                }
                else if (preproc.equals("endif")) {
                    int stackLength;
                    ASBeautifier beautifier;

                    if (!waitingBeautifierStackLengthStack.empty()) {
                        stackLength = waitingBeautifierStackLengthStack.back();
                        waitingBeautifierStackLengthStack.pop_back();
                        while (waitingBeautifierStack.size() > stackLength) {
                            beautifier = waitingBeautifierStack.back();
                            waitingBeautifierStack.pop_back();
                            beautifier = null;
                        }
                    }

                    if (!activeBeautifierStackLengthStack.empty()) {
                        stackLength = activeBeautifierStackLengthStack.back();
                        activeBeautifierStackLengthStack.pop_back();
                        while (activeBeautifierStack.size() > stackLength) {
                            beautifier = activeBeautifierStack.back();
                            activeBeautifierStack.pop_back();
                            beautifier = null;
                        }
                    }
                }
            } // if (line.charAt(0) == '#')

            // check if the last char is a backslash
            if (line.length() > 0)
                backslashEndsPrevLine = line.endsWith("\\");
            else
                backslashEndsPrevLine = false;

            // check if this line ends a multi-line #define.
            // if so, use the #define's cloned beautifier for the line's indentation
            // and then remove it from the active beautifier stack and delete it.
            if (!backslashEndsPrevLine && isInDefineDefinition && !isInDefine) {
                String beautifiedLine;
                ASBeautifier defineBeautifier;

                isInDefineDefinition = false;
                defineBeautifier = activeBeautifierStack.back();
                activeBeautifierStack.pop_back();

                beautifiedLine = defineBeautifier.beautify(line);
                defineBeautifier = null;
                return beautifiedLine;
            }

            // unless this is a multi-line #define, return this precompiler line as is.
            if (!isInDefine && !isInDefineDefinition)
                return originalLine;
        } // if preprocessor command

        // if there exists any worker beautifier in the activeBeautifierStack,
        // then use it instead of me to indent the current line.
        if (!isInDefine && activeBeautifierStack != null && !activeBeautifierStack.empty())
            return activeBeautifierStack.back().beautify(line);

        // calculate preliminary indentation based on data from past lines
        if (!inStatementIndentStack.empty())
            spaceTabCount = inStatementIndentStack.back();

        for (i = 0; i < headerStackSize; i++) {
            isInClass = false;

            if (blockIndent || (!(i > 0 && headerStack.at(i-1) != AS_OPEN_BRACKET
                   && headerStack.at(i) == AS_OPEN_BRACKET)))
                ++tabCount;

            if (isCStyle && !namespaceIndent && i >= 1
                    && headerStack.at(i-1) == AS_NAMESPACE
                    && headerStack.at(i) == AS_OPEN_BRACKET)
                --tabCount;

            if (isCStyle && i >= 1
                    && headerStack.at(i-1) == AS_CLASS
                    && headerStack.at(i) == AS_OPEN_BRACKET)
            {
                if (classIndent)
                    ++tabCount;
                isInClass = true;
            }
            else if (switchIndent && i > 1
                     && headerStack.at(i-1) == AS_SWITCH
                     && headerStack.at(i) == AS_OPEN_BRACKET)
            {
                // is the switchIndent option is on, indent switch statements an additional indent.
                ++tabCount;
                isInSwitch = true;
            }
        } // for

        if (isCStyle && isInClass && classIndent && headerStackSize >= 2
                && headerStack.at(headerStackSize-2) == AS_CLASS
                && headerStack.at(headerStackSize-1) == AS_OPEN_BRACKET
                && line.charAt(0) == '}')
            --tabCount;
        else if (isInSwitch && switchIndent && headerStackSize >= 2
                 && headerStack.at(headerStackSize-2) == AS_SWITCH
                 && headerStack.at(headerStackSize-1) == AS_OPEN_BRACKET
                 && line.charAt(0) == '}')
            --tabCount;

        if (isInClassHeader) {
            isInClassHeaderTab = true;
            tabCount += 2;
        }

        if (isInConditional)
            --tabCount;

        // parse characters in the current line.
        for (i = 0; i < line.length(); i++) {
            tempCh = line.charAt(i);
            prevCh = ch;
            ch = tempCh;
            outBuffer.append(ch);

            if (isWhiteSpace(ch))
                continue;

            // handle special characters (i.e. backslash and characters such as \n, \t, ...)
            if (isSpecialChar) {
                isSpecialChar = false;
                continue;
            }
            if (!(isInComment || isInLineComment) && line.regionMatches(i, "\\\\", 0, 2)) {
                outBuffer.append('\\');
                i++;
                continue;
            }
            if (!(isInComment || isInLineComment) && ch=='\\') {
                isSpecialChar = true;
                continue;
            }

            // handle quotes (such as 'x' and "Hello Dolly")
            if (!(isInComment || isInLineComment) && (ch=='"' || ch=='\'')) {
                if (!isInQuote) {
                    quoteChar = ch;
                    isInQuote = true;
                } else if (quoteChar == ch) {
                    isInQuote = false;
                    isInStatement = true;
                    continue;
                }
            }
            if (isInQuote)
                continue;

            // handle comments
            if (!(isInComment || isInLineComment) && line.regionMatches(i, AS_OPEN_LINE_COMMENT, 0, 2)) {
                isInLineComment = true;
                outBuffer.append('/');
                i++;
                continue;
            }
            else if (!(isInComment || isInLineComment) && line.regionMatches(i, AS_OPEN_COMMENT, 0, 2)) {
                isInComment = true;
                outBuffer.append('*');
                i++;
                continue;
            }
            else if ((isInComment || isInLineComment) && line.regionMatches(i, AS_CLOSE_COMMENT, 0, 2)) {
                isInComment = false;
                outBuffer.append('/');
                i++;
                continue;
            }

            if (isInComment || isInLineComment) {
                continue;
            }

            // if we have reached this far then we are NOT in a comment or string of special character...
            if (probationHeader != null) {
                if (((probationHeader == AS_STATIC || probationHeader == AS_CONST) && ch == '{')
                        || (probationHeader == AS_SYNCHRONIZED && ch == '('))
                {
                    // insert the probation header as a new header
                    isInHeader = true;
                    headerStack.push_back(probationHeader);

                    // handle the specific probation header
                    isInConditional = (probationHeader == AS_SYNCHRONIZED);
                    if (probationHeader == AS_CONST)
                        isImmediatelyAfterConst = true;
                    //  isInConst = true;
                    // TODO:
                    // There is actually no more need for the global isInConst variable.
                    // The only reason for checking const is to see if there is a const
                    // immediately before an open-bracket.
                    // Since CONST is now put into probation and is checked during itspost-char,
                    // isImmediatelyAfterConst can be set by its own...

                    isInStatement = false;
                    // if the probation comes from the previous line, then indent by 1 tab count.
                    if (previousLineProbation && ch == '{')
                        tabCount++;
                    previousLineProbation = false;
                }

                // dismiss the probation header
                probationHeader = null;
            }

            prevNonSpaceCh = currentNonSpaceCh;
            currentNonSpaceCh = ch;
            if (!isLegalNameChar(ch) && ch != ',' && ch != ';') {
                prevNonLegalCh = currentNonLegalCh;
                currentNonLegalCh = ch;
            }

            //if (isInConst)
            //{
            //    isInConst = false;
            //    isImmediatelyAfterConst = true;
            //}

            if (isInHeader) {
                isInHeader = false;
                currentHeader = headerStack.back();
            }
            else
                currentHeader = null;

            // handle templates
            if (isCStyle && isInTemplate
                    && (ch == '<' || ch == '>')
                    && findHeader(line, i, nonAssignmentOperators) == null)
            {
                if (ch == '<')
                    ++templateDepth;
                else if (ch == '>') {
                    if (--templateDepth <= 0) {
                        if (isInTemplate)
                            ch = ';';
                        else
                            ch = 't';
                        isInTemplate = false;
                        templateDepth = 0;
                    }
                }
            }

            // handle parenthesies
            if (ch == '(' || ch == '[' || ch == ')' || ch == ']') {
                if (ch == '(' || ch == '[') {
                    if (parenDepth == 0) {
                        parenStatementStack.push_back(isInStatement);
                        isInStatement = true;
                    }
                    parenDepth++;
                    inStatementIndentStackSizeStack.push_back(inStatementIndentStack.size());

                    if (currentHeader != null)
                        registerInStatementIndent(line, i, spaceTabCount, minConditionalIndent /*indentLength*2 */, true);
                    else
                        registerInStatementIndent(line, i, spaceTabCount, 0, true);
                }
                else if (ch == ')' || ch == ']') {
                    parenDepth--;
                    if (parenDepth == 0) {
                        isInStatement = parenStatementStack.back();
                        parenStatementStack.pop_back();
                        ch = ' ';
                        isInConditional = false;
                    }

                    if (!inStatementIndentStackSizeStack.empty()) {
                        int previousIndentStackSize = inStatementIndentStackSizeStack.back();
                        inStatementIndentStackSizeStack.pop_back();
                        while (previousIndentStackSize < inStatementIndentStack.size())
                            inStatementIndentStack.pop_back();

                        if (!parenIndentStack.empty()) {
                            int poppedIndent = parenIndentStack.back();
                            parenIndentStack.pop_back();
                            if (i == 0)
                                spaceTabCount = poppedIndent;
                        }
                    }
                }
                continue;
            } // if handle parenthesis

            // handle block start
            if (ch == '{') {
                boolean isBlockOpener = false;

                // first, check if '{' is a block-opener or an static-array opener
                isBlockOpener = ((prevNonSpaceCh == '{' && bracketBlockStateStack.back())
                                || prevNonSpaceCh == '}'
                                || prevNonSpaceCh == ')'
                                || prevNonSpaceCh == ';'
                                || isInClassHeader
                                || isBlockOpener
                                || isImmediatelyAfterConst
                                || (isInDefine &&
                                    (prevNonSpaceCh == '('
                                     || prevNonSpaceCh == '_'
                                     || Character.isLetterOrDigit(prevNonSpaceCh))));
                isInClassHeader = false;
                if (!isBlockOpener && currentHeader != null)
                    if (nonParenHeaders.contains(currentHeader))
                        isBlockOpener = true;
                bracketBlockStateStack.push_back(isBlockOpener);
                if (!isBlockOpener) {
                    inStatementIndentStackSizeStack.push_back(inStatementIndentStack.size());
                    registerInStatementIndent(line, i, spaceTabCount, 0, true);
                    parenDepth++;
                    if (i == 0)
                        shouldIndentBrackettedLine = false;
                    continue;
                }

                // this bracket is a block opener..

                ++lineOpeningBlocksNum;

                if (isInClassHeader)
                    isInClassHeader = false;
                if (isInClassHeaderTab) {
                    isInClassHeaderTab = false;
                    tabCount -= 2;
                }
                
                /// danson
                if (isInFor) {
                    isInFor = false;
                }
                
                blockParenDepthStack.push_back(parenDepth);
                blockStatementStack.push_back(isInStatement);
                inStatementIndentStackSizeStack.push_back(inStatementIndentStack.size());

                blockTabCount += isInStatement ? 1 : 0;
                parenDepth = 0;
                isInStatement = false;

                tempStacks.push_back(new StringStack());
                headerStack.push_back(AS_OPEN_BRACKET);
                lastLineHeader = AS_OPEN_BRACKET; // <------

                continue;
            }

            // check if a header has been reached
            if (prevCh == ' ') {
                boolean isIndentableHeader = true;
                String newHeader = findHeader(line, i, headers);
                if (newHeader != null) {
                    // if we reached here, then this is a header...
                    isInHeader = true;
                    StringStack lastTempStack;
                    if (tempStacks.empty())
                        lastTempStack = null;
                    else
                        lastTempStack = (StringStack) tempStacks.back();

                    // if a new block is opened, push a new stack into tempStacks to hold the
                    // future list of headers in the new block.

                    // take care of the special case: 'else if (...)'
                    if (newHeader == AS_IF && lastLineHeader == AS_ELSE) {
                        //spaceTabCount += indentLength; // to counter the opposite addition that occurs when the 'if' is registered below...
                        headerStack.pop_back();
                    }
                    // take care of 'else'
                    else if (newHeader == AS_ELSE) {
                        if (lastTempStack != null) {
                            int indexOfIf = lastTempStack.indexOf(AS_IF); // <---
                            if (indexOfIf != -1) {
                                // recreate the header list in headerStack up to the previous 'if'
                                // from the temporary snapshot stored in lastTempStack.
                                int restackSize = lastTempStack.size() - indexOfIf - 1;
                                for (int r = 0; r < restackSize; r++) {
                                    headerStack.push_back(lastTempStack.back());
                                    lastTempStack.pop_back();
                                }
                                if (!closingBracketReached)
                                    tabCount += restackSize;
                            }
                            // If the above if is not true, i.e. no 'if' before the 'else',
                            // then nothing beautiful will come out of this...
                            // I should think about inserting an Exception here to notify the caller of this...
                        }
                    }
                    // check if 'while' closes a previous 'do'
                    else if (newHeader == AS_WHILE) {
                        if (lastTempStack != null) {
                            int indexOfDo = lastTempStack.indexOf(AS_DO); // <---
                            if (indexOfDo != -1) {
                                // recreate the header list in headerStack up to the previous 'do'
                                // from the temporary snapshot stored in lastTempStack.
                                int restackSize = lastTempStack.size() - indexOfDo - 1;
                                for (int r = 0; r < restackSize; r++) {
                                    headerStack.push_back(lastTempStack.back());
                                    lastTempStack.pop_back();
                                }
                                if (!closingBracketReached)
                                    tabCount += restackSize;
                            }
                        }
                    }
                    /// danson, added check for 'for' for Java 1.5's "for(... : ...)" construct
                    else if (newHeader == AS_FOR) {
                        isInFor = true;    
                    }
                    
                    // check if 'catch' closes a previous 'try' or 'catch'
                    else if (newHeader == AS_CATCH || newHeader == AS_FINALLY) {
                        if (lastTempStack != null) {
                            int indexOfTry = lastTempStack.indexOf(AS_TRY);
                            if (indexOfTry == -1)
                                indexOfTry = lastTempStack.indexOf(AS_CATCH);
                            if (indexOfTry != -1) {
                                // recreate the header list in headerStack up to the previous 'try'
                                // from the temporary snapshot stored in lastTempStack.
                                int restackSize = lastTempStack.size() - indexOfTry - 1;
                                for (int r = 0; r < restackSize; r++) {
                                    headerStack.push_back(lastTempStack.back());
                                    lastTempStack.pop_back();
                                }
                                if (!closingBracketReached)
                                    tabCount += restackSize;
                            }
                        }
                    }
                    else if (newHeader == AS_CASE) {
                        isInCase = true;
                        if (!caseIndent)
                            --tabCount;
                    }
                    else if (newHeader == AS_DEFAULT) {
                        isInCase = true;
                        if (!caseIndent)
                            --tabCount;
                    }
                    else if (newHeader == AS_PUBLIC || newHeader == AS_PROTECTED || newHeader == AS_PRIVATE) {
                        if (isCStyle && !isInClassHeader)
                            --tabCount;
                        isIndentableHeader = false;
                    }
                    //else if ((newHeader == AS_STATIC || newHeader == AS_SYNCHRONIZED) &&
                    //         !headerStack.empty() &&
                    //         (headerStack.back() == AS_STATIC || headerStack.back() == AS_SYNCHRONIZED))
                    //{
                    //    isIndentableHeader = false;
                    //}
                    else if (newHeader == AS_STATIC
                             || newHeader == AS_SYNCHRONIZED
                             || (newHeader == AS_CONST && isCStyle))
                    {
                        if (!headerStack.empty()
                            && (headerStack.back() == AS_STATIC
                                || headerStack.back() == AS_SYNCHRONIZED
                                || headerStack.back() == AS_CONST))
                        {
                            isIndentableHeader = false;
                        } else {
                            isIndentableHeader = false;
                            probationHeader = newHeader;
                        }
                    }
                    else if (newHeader == AS_CONST) {
                        // this will be entered only if NOT in C style
                        // since otherwise the CONST would be found to be a probation header...
                        isIndentableHeader = false;
                    }
                    /*
                    else if (newHeader == AS_OPERATOR) {
                        if (isCStyle) {
                            isInOperator = true;
                        }
                        isIndentableHeader = false;
                    }
                    */
                    else if (newHeader == AS_TEMPLATE) {
                        if (isCStyle)
                            isInTemplate = true;
                        isIndentableHeader = false;
                    }

                    if (isIndentableHeader) {
                        //spaceTabCount -= indentLength;
                        headerStack.push_back(newHeader);
                        isInStatement = false;
                        if (nonParenHeaders.indexOf(newHeader) == -1)
                            isInConditional = true;
                        lastLineHeader = newHeader;
                    }
                    else
                        isInHeader = false;

                    //lastLineHeader = newHeader;

                    outBuffer.append(newHeader.substring(1));
                    i += newHeader.length() - 1;

                    continue;
                } // if (newHeader != null)
            } // if (prevCh == ' ')

            // handle operators
            if (isCStyle &&
                !Character.isLetter(prevCh) &&
                line.regionMatches(i, AS_OPERATOR, 0, 8) &&
                !Character.isLetterOrDigit(line.charAt(i+8)))
            {
                isInOperator = true;
                outBuffer.append(AS_OPERATOR.substring(1));
                i += 7;
                continue;
            }

            if (ch == '?')
                isInQuestion = true;

            // special handling of 'case' statements
            if (ch == ':') {
                if (line.length() > i+1 && line.charAt(i+1) == ':') {
                    // this is '::'
                    ++i;
                    outBuffer.append(':');
                    ch = ' ';
                    continue;
                }
                else if (isCStyle && isInClass && prevNonSpaceCh != ')') {
                    --tabCount;
                    // found a 'private:' or 'public:' inside a class definition,
                    // so do nothing special
                }
                else if (isCStyle && isInClassHeader) {
                    // found a 'class A : public B' definition
                    // so do nothing special
                }
                else if (isInQuestion) {
                    isInQuestion = false;
                }
                else if (isInFor) {     /// danson
                    continue;
                }
                else if (isCStyle && prevNonSpaceCh == ')') {
                    isInClassHeader = true;
                    if (i==0)
                        tabCount += 2;
                }
                else {
                    currentNonSpaceCh = ';'; // so that brackets after the ':' will appear as block-openers
                    if (isInCase) {
                        isInCase = false;
                        ch = ';'; // from here on, treat char as ';'
                    } else {
                        // is in a label (e.g. 'label1:')
                        if (labelIndent)
                            --tabCount; // unindent label by one indent
                        else
                            tabCount = 0; // completely flush indent to left
                    }
                }
            }

            if ((ch == ';'  || (parenDepth > 0 && ch == ',')) && !inStatementIndentStackSizeStack.empty())
                while (inStatementIndentStackSizeStack.back() + (parenDepth > 0 ? 1 : 0) < inStatementIndentStack.size())
                    inStatementIndentStack.pop_back();

            // handle ends of statements
            if ((ch == ';' && parenDepth == 0) || ch == '}') {
                if (ch == '}') {
                    // first check if this '}' closes a previous block, or a static array...
                    if (!bracketBlockStateStack.empty()) {
                        boolean bracketBlockState = bracketBlockStateStack.back();
                        bracketBlockStateStack.pop_back();
                        if (!bracketBlockState) {
                            if (!inStatementIndentStackSizeStack.empty()) {
                                // this bracket is a static array
                                int previousIndentStackSize = inStatementIndentStackSizeStack.back();
                                inStatementIndentStackSizeStack.pop_back();
                                while (previousIndentStackSize < inStatementIndentStack.size()) {
                                    inStatementIndentStack.pop_back();
                                }
                                parenDepth--;
                                if (i == 0)
                                    shouldIndentBrackettedLine = false;

                                if (!parenIndentStack.empty()) {
                                    int poppedIndent = parenIndentStack.back();
                                    parenIndentStack.pop_back();
                                    if (i == 0)
                                        spaceTabCount = poppedIndent;
                                }
                            }
                            continue;
                        }
                    }

                    // this bracket is block closer...

                    ++lineClosingBlocksNum;

                    if(!inStatementIndentStackSizeStack.empty())
                        inStatementIndentStackSizeStack.pop_back();

                    if (!blockParenDepthStack.empty()) {
                        parenDepth = blockParenDepthStack.back();
                        blockParenDepthStack.pop_back();
                        isInStatement = blockStatementStack.back();
                        blockStatementStack.pop_back();

                        if (isInStatement)
                            blockTabCount--;
                    }

                    closingBracketReached = true;
                    int headerPlace = headerStack.indexOf(AS_OPEN_BRACKET); // <---
                    if (headerPlace != -1) {
                        String popped = headerStack.back();
                        while (popped != AS_OPEN_BRACKET) {
                            headerStack.pop_back();
                            popped = headerStack.back();
                        }
                        headerStack.pop_back();

                        if (!tempStacks.empty()) {
                            StringStack temp = (StringStack) tempStacks.back();
                            tempStacks.pop_back();
                            temp = null;
                        }
                    }

                    ch = ' ';
                    // needed due to cases such as '}else{', so that headers ('else' in this case) will be identified...
                } // if (ch == '}')

                // Create a temporary snapshot of the current block's
                // header-list in the uppermost inner stack in tempStacks,
                // and clear the headerStack up to the beginning of the block.
                // Thus, the next future statement will think it comes one
                // indent past the block's '{' unless it specifically checks
                // for a companion-header (such as a previous 'if' for an
                // 'else' header) within the tempStacks, and recreates the
                // temporary snapshot by manipulating the tempStacks.
                if (!tempStacks.back().empty())
                    while (!tempStacks.back().empty()) {
                        StringStack s = (StringStack) tempStacks.back();
                        s.pop_back();
                    }
                while (!headerStack.empty() && headerStack.back() != AS_OPEN_BRACKET) {
                    StringStack s = (StringStack) tempStacks.back();
                    s.push_back(headerStack.back());
                    headerStack.pop_back();
                }

                if (parenDepth == 0 && ch == ';')
                    isInStatement=false;

                isInClassHeader = false;

                continue;
            }

            // check for preBlockStatements ONLY if not within parenthesies
            // (otherwise 'struct XXX' statements would be wrongly
            // interpreted...)
            if (prevCh == ' ' && !isInTemplate && parenDepth == 0) {
                String newHeader = findHeader(line, i, preBlockStatements);
                if (newHeader != null) {
                    isInClassHeader = true;
                    outBuffer.append(newHeader.substring(1));
                    i += newHeader.length() - 1;
                    //if (isCStyle)
                    headerStack.push_back(newHeader);
                }
            }

            // Handle operators

            // PRECHECK if a '==' or '--' or '++' operator was reached.
            // If not, then register an indent IF an assignment operator was
            // reached. The precheck is important, so that statements such
            // as 'i--==2' are not recognized to have assignment operators
            // (here, '-=') in them . . .

            String foundAssignmentOp = null;
            String foundNonAssignmentOp = null;
            immediatelyPreviousAssignmentOp = null;

            // Check if an operator has been reached.
            foundAssignmentOp = findHeader(line, i, assignmentOperators, false);
            foundNonAssignmentOp = findHeader(line, i, nonAssignmentOperators, false);

            // Since findHeader's boundary checking was not used above, it is possible
            // that both an assignment-op and a non-assignment-op where found,
            // e.g. '>>' and '>>='. If this is the case, treat the LONGER one as the
            // found operator.
            if (foundAssignmentOp != null && foundNonAssignmentOp != null)
                if (foundAssignmentOp.length() < foundNonAssignmentOp.length())
                    foundAssignmentOp = null;
                else
                    foundNonAssignmentOp = null;

            if (foundNonAssignmentOp != null) {
                if (foundNonAssignmentOp.length() > 1) {
                    outBuffer.append(foundNonAssignmentOp.substring(1));
                    i += foundNonAssignmentOp.length() - 1;
                }
            }
            else if (foundAssignmentOp != null) {
                if (foundAssignmentOp.length() > 1) {
                    outBuffer.append(foundAssignmentOp.substring(1));
                    i += foundAssignmentOp.length() - 1;
                }
                if (!isInOperator && !isInTemplate) {
                    registerInStatementIndent(line, i, spaceTabCount, 0, false);
                    immediatelyPreviousAssignmentOp = foundAssignmentOp;
                    isInStatement = true;
                }
            }

            /*
            immediatelyPreviousAssignmentOp = null;
            boolean isNonAssingmentOperator = false;
            for (int n = 0; n < nonAssignmentOperators.size(); n++) {
                String op = nonAssignmentOperators.at(n);
                if (line.regionMatches(i, op, 0, op.length())) {
                    if (op.length() > 1) {
                        outBuffer.append(op.substring(1));
                        i += op.length() - 1;
                    }
                    isNonAssingmentOperator = true;
                    break;
                }
            }

            if (!isNonAssingmentOperator) {
                for (int a = 0; a < assignmentOperators.size(); a++) {
                    String op = assignmentOperators.at(a);
                    if (line.regionMatches(i, op, 0, op.length())) {
                        if (op.length() > 1) {
                            outBuffer.append(op.substring(1));
                            i += op.length() - 1;
                        }
                        if (!isInOperator && !isInTemplate) {
                            registerInStatementIndent(line, i, spaceTabCount, 0, false);
                            immediatelyPreviousAssignmentOp = op;
                            isInStatement = true;
                        }
                        break;
                    }
                }
            }
            */

            if (isInOperator)
                isInOperator = false;
        } // for

        // handle special cases of unindentation:

        // if '{' doesn't follow an immediately previous '{' in the
        // headerStack (but rather another header such as "for" or "if"),
        // then unindent it by one indentation relative to its block.

        // System.err.println("\n" + lineOpeningBlocksNum + " "  +
        //     lineClosingBlocksNum + " " + previousLastLineHeader);

        // indent #define lines with one less tab:
        //if (isInDefine)
        //    tabCount -= defineTabCount - 1;

        if (!blockIndent && outBuffer.length() > 0
                && outBuffer.charAt(0) == '{'
                && !(lineOpeningBlocksNum > 0 && lineOpeningBlocksNum == lineClosingBlocksNum)
                && !(headerStack.size() > 1 && headerStack.at(headerStack.size() - 2) == AS_OPEN_BRACKET)
                && shouldIndentBrackettedLine)
            --tabCount;
        else if (outBuffer.length() > 0 && outBuffer.charAt(0) == '}' && shouldIndentBrackettedLine)
            --tabCount;
        // correctly indent one-line-blocks...
        else if (outBuffer.length() > 0
                 && lineOpeningBlocksNum > 0
                 && lineOpeningBlocksNum == lineClosingBlocksNum
                 && previousLastLineHeader != null
                 && previousLastLineHeader != AS_OPEN_BRACKET)
            tabCount -= 1; //lineOpeningBlocksNum - (blockIndent ? 1 : 0);

        if (tabCount < 0)
            tabCount = 0;

        // take care of extra bracket indentation option...
        if (bracketIndent && outBuffer.length() > 0 && shouldIndentBrackettedLine)
            if (outBuffer.charAt(0) == '{' || outBuffer.charAt(0) == '}')
                tabCount++;

        if (isInDefine) {
            if (outBuffer.charAt(0) == '#') {
                String preproc = outBuffer.toString().substring(1).trim();
                if (preproc.equals("define")) {
                    if (!inStatementIndentStack.empty()
                            && inStatementIndentStack.back() > 0)
                    {
                        defineTabCount = tabCount;
                    } else {
                        defineTabCount = tabCount - 1;
                        tabCount--;
                    }
                }
            }
            tabCount -= defineTabCount;
        }

        if (tabCount < 0)
            tabCount = 0;

        // finally, insert indentations into beginning of line

        prevFinalLineSpaceTabCount = spaceTabCount;
        prevFinalLineTabCount = tabCount;

        if (shouldForceTabIndentation) {
                tabCount += spaceTabCount / indentLength;
                spaceTabCount = spaceTabCount % indentLength;
        }

        String ret = preLineWS(spaceTabCount, tabCount) + outBuffer.toString();

        if (lastLineHeader != null) {
            previousLastLineHeader = lastLineHeader;
        }

        return ret;
    }


    /**
     * get distance to the next non-white space, non-comment character in
     * the line. If no such character exists, return the length remaining to
     * the end of the line.
     */
    protected int getNextProgramCharDistance(String line, int i) {
        boolean inComment = false;
        int remainingCharNum = line.length() - i;
        int charDistance = 1;
        char ch;

        for (charDistance = 1; charDistance < remainingCharNum; charDistance++) {
            ch = line.charAt(i + charDistance);
            if (inComment) {
                if (line.regionMatches(i + charDistance, AS_CLOSE_COMMENT, 0, 2)) {
                    charDistance++;
                    inComment = false;
                }
                continue;
            }
            else if (isWhiteSpace(ch)) {
                continue;
            }
            else if (ch == '/') {
                if (line.regionMatches(i + charDistance, AS_OPEN_LINE_COMMENT, 0, 2))
                    return remainingCharNum;
                else if (line.regionMatches(i + charDistance, AS_OPEN_COMMENT, 0, 2)) {
                    charDistance++;
                    inComment = true;
                }
            }
            else
                return charDistance;
        } // for

        return charDistance;
    }


    /**
     * check if a specific character can be used in a legal
     * variable/method/class name
     * @param ch        the character to be checked.
     * @return          legality of the char.
     */
     protected boolean isLegalNameChar(char ch) {
         return
            Character.isLetterOrDigit(ch)
            //(ch>='a' && ch<='z') || (ch>='A' && ch<='Z') || (ch>='0' && ch<='9') ||
            || ch=='.'
            || ch=='_'
            || (!isCStyle && ch=='$')
            || (isCStyle && ch=='~');
     }


     /**
      * check if a specific character is white space.
      */
     protected boolean isWhiteSpace(char ch) {
         return Character.isWhitespace(ch);
         //return ch == ' ' || ch == '\t';
     }


    /**
     * check if a specific line position contains a header, out of several
     * possible headers.
     * @param  possibleHeaders a vector with header strings.
     * @return  a pointer to the found header, or null if no header was found.
     */
    protected String findHeader(String line, int i, Vector possibleHeaders) {
        return findHeader(line, i, possibleHeaders, true);
    }


    /**
     * check if a specific line position contains a header, out of several
     * possible headers.
     * @param  possibleHeaders a vector with header strings.
     * @return  a pointer to the found header, or null if no header was found.
     */
    protected String findHeader(String line, int i,
                                Vector possibleHeaders,
                                boolean checkBoundry)
    {
        int maxHeaders = possibleHeaders.size();
        String header = null;
        int p;

        for (p = 0; p < maxHeaders; p++) {
            header = (String) possibleHeaders.elementAt(p);

            if (line.regionMatches(i, header, 0, header.length())) {
                // check that this is a header and not a part of a longer word
                // (e.g. not at its begining, not at its middle...)
                int lineLength = line.length();
                int headerEnd = i + header.length();
                char startCh = header.charAt(0); // first char of header
                char endCh = '\0'; // char just after header
                char prevCh = '\0'; // char just before header

                if (headerEnd < lineLength)
                    endCh = line.charAt(headerEnd);

                if (i > 0)
                    prevCh = line.charAt(i - 1);

                if (!checkBoundry)
                    return header;
                else if (prevCh != 0
                         && isLegalNameChar(startCh)
                         && isLegalNameChar(prevCh))
                    return null;
                else if (headerEnd >= lineLength
                         || !isLegalNameChar(startCh)
                         || !isLegalNameChar(endCh))
                    return header;
                else
                    return null;
            }
        }
        return null;
    }


    /**
     * register an in-statement indent.
     */
    private void registerInStatementIndent(String line,
                                           int i,
                                           int spaceTabCount,
                                           int minIndent,
                                           boolean updateParenStack) {
        int inStatementIndent;
        int remainingCharNum = line.length() - i;
        int nextNonWSChar = 1;

        nextNonWSChar = getNextProgramCharDistance(line, i);

        // if indent is around the last char in the line, indent instead
        // 2 spaces from the previous indent
        if (nextNonWSChar == remainingCharNum) {
            int previousIndent = spaceTabCount;
            if (!inStatementIndentStack.empty())
                previousIndent = inStatementIndentStack.back();

            inStatementIndentStack.push_back(/*2*/ indentLength + previousIndent);
            if (updateParenStack)
                parenIndentStack.push_back(previousIndent);
            return;
        }

        if (updateParenStack)
            parenIndentStack.push_back(i + spaceTabCount);

        inStatementIndent = i + nextNonWSChar + spaceTabCount;

        if (i + nextNonWSChar < minIndent)
            inStatementIndent = minIndent + spaceTabCount;

        if (i + nextNonWSChar > maxInStatementIndent)
            inStatementIndent =  indentLength * 2 + spaceTabCount;

        if (!inStatementIndentStack.empty()
                && inStatementIndent < inStatementIndentStack.back())
            inStatementIndent = inStatementIndentStack.back();

        inStatementIndentStack.push_back(inStatementIndent);
    }


    private String preLineWS(int spaceTabCount, int tabCount) {
        StringBuffer ws = new StringBuffer();

        for (int i = 0; i < tabCount; i++)
            ws.append(indentString);

        while ((spaceTabCount--) > 0)
            ws.append(' ');

        return ws.toString();
    }


    /**
     * initialize the static vars
     */
    private void initStatic() {
        if (calledInitStatic)
            return;

        calledInitStatic = true;

        headers.push_back(AS_IF);
        headers.push_back(AS_ELSE);
        headers.push_back(AS_FOR);
        headers.push_back(AS_WHILE);
        headers.push_back(AS_DO);
        headers.push_back(AS_TRY);
        headers.push_back(AS_CATCH);
        headers.push_back(AS_FINALLY);
        headers.push_back(AS_SYNCHRONIZED);
        headers.push_back(AS_SWITCH);
        headers.push_back(AS_CASE);
        headers.push_back(AS_DEFAULT);
        //headers.push_back(AS_PUBLIC);
        //headers.push_back(AS_PRIVATE);
        //headers.push_back(AS_PROTECTED);
        //headers.push_back(AS_OPERATOR);
        headers.push_back(AS_TEMPLATE);
        headers.push_back(AS_CONST);
        headers.push_back(AS_STATIC);
        headers.push_back(AS_EXTERN);

        nonParenHeaders.push_back(AS_ELSE);
        nonParenHeaders.push_back(AS_DO);
        nonParenHeaders.push_back(AS_TRY);
        nonParenHeaders.push_back(AS_FINALLY);
        nonParenHeaders.push_back(AS_STATIC);
        nonParenHeaders.push_back(AS_CONST);
        nonParenHeaders.push_back(AS_EXTERN);
        nonParenHeaders.push_back(AS_CASE);
        nonParenHeaders.push_back(AS_DEFAULT);

        nonParenHeaders.push_back(AS_PUBLIC);
        nonParenHeaders.push_back(AS_PRIVATE);
        nonParenHeaders.push_back(AS_PROTECTED);
        nonParenHeaders.push_back(AS_TEMPLATE);
        nonParenHeaders.push_back(AS_CONST);

        preBlockStatements.push_back(AS_CLASS);
        preBlockStatements.push_back(AS_STRUCT);
        preBlockStatements.push_back(AS_UNION);
        preBlockStatements.push_back(AS_INTERFACE);
        preBlockStatements.push_back(AS_NAMESPACE);
        preBlockStatements.push_back(AS_THROWS);
        preBlockStatements.push_back(AS_EXTERN);

        assignmentOperators.push_back(AS_ASSIGN);
        assignmentOperators.push_back(AS_PLUS_ASSIGN);
        assignmentOperators.push_back(AS_MINUS_ASSIGN);
        assignmentOperators.push_back(AS_MULT_ASSIGN);
        assignmentOperators.push_back(AS_DIV_ASSIGN);
        assignmentOperators.push_back(AS_MOD_ASSIGN);
        assignmentOperators.push_back(AS_OR_ASSIGN);
        assignmentOperators.push_back(AS_AND_ASSIGN);
        assignmentOperators.push_back(AS_XOR_ASSIGN);
        assignmentOperators.push_back(AS_GR_GR_GR_ASSIGN);
        assignmentOperators.push_back(AS_GR_GR_ASSIGN);
        assignmentOperators.push_back(AS_LS_LS_LS_ASSIGN);
        assignmentOperators.push_back(AS_LS_LS_ASSIGN);
        assignmentOperators.push_back(AS_RETURN);

        nonAssignmentOperators.push_back(AS_EQUAL);
        nonAssignmentOperators.push_back(AS_PLUS_PLUS);
        nonAssignmentOperators.push_back(AS_MINUS_MINUS);
        nonAssignmentOperators.push_back(AS_NOT_EQUAL);
        nonAssignmentOperators.push_back(AS_GR_EQUAL);
        nonAssignmentOperators.push_back(AS_GR_GR_GR);
        nonAssignmentOperators.push_back(AS_GR_GR);
        nonAssignmentOperators.push_back(AS_LS_EQUAL);
        nonAssignmentOperators.push_back(AS_LS_LS_LS);
        nonAssignmentOperators.push_back(AS_LS_LS);
        nonAssignmentOperators.push_back(AS_ARROW);
        nonAssignmentOperators.push_back(AS_AND);
        nonAssignmentOperators.push_back(AS_OR);
    }


    // STATIC FIELDS

    private static boolean calledInitStatic = false;

    private static StringStack headers = new StringStack();
    private static StringStack nonParenHeaders = new StringStack();
    private static StringStack preprocessorHeaders = new StringStack();
    private static StringStack preBlockStatements = new StringStack();
    private static StringStack assignmentOperators = new StringStack();
    private static StringStack nonAssignmentOperators = new StringStack();


    // MEMBER FIELDS

    private ASSourceIterator sourceIterator;

    private ASBeautifierStack waitingBeautifierStack;
    private ASBeautifierStack activeBeautifierStack;
    private IntegerStack waitingBeautifierStackLengthStack;
    private IntegerStack activeBeautifierStackLengthStack;
    private StringStack headerStack;
    private StackStack tempStacks;
    private IntegerStack blockParenDepthStack;
    private BooleanStack blockStatementStack;
    private BooleanStack parenStatementStack;
    private BooleanStack bracketBlockStateStack;
    private IntegerStack inStatementIndentStack;
    private IntegerStack inStatementIndentStackSizeStack;
    private IntegerStack parenIndentStack;

    private String indentString = " ";
    private String currentHeader;
    private String previousLastLineHeader;
    private String immediatelyPreviousAssignmentOp;
    private String probationHeader;

    private boolean isInQuote;
    private boolean isInComment;
    private boolean isInCase;
    private boolean isInQuestion;
    private boolean isInStatement;
    private boolean isInHeader;
    private boolean isCStyle;
    protected boolean isInFor;    /// danson
    private boolean isInOperator;
    private boolean isInTemplate;
    private boolean isInDefine;
    private boolean isInDefineDefinition;
    private boolean classIndent;
    private boolean isInClassHeader;
    private boolean isInClassHeaderTab;
    private boolean switchIndent;
    private boolean caseIndent;
    private boolean namespaceIndent;
    private boolean bracketIndent;
    private boolean blockIndent;
    private boolean labelIndent;
    private boolean preprocessorIndent;
    private boolean isInConditional;
    private boolean isMinimalConditinalIndentSet;
    private boolean shouldForceTabIndentation;

    private int indentLength = 4;
    private int minConditionalIndent = 8;
    private int parenDepth;
    private int blockTabCount;
    private int leadingWhiteSpaces;
    private int maxInStatementIndent;
    private int templateDepth;
    private char quoteChar;
    private char prevNonSpaceCh;
    private char currentNonSpaceCh;
    private char currentNonLegalCh;
    private char prevNonLegalCh;
    private int prevFinalLineSpaceTabCount;
    private int prevFinalLineTabCount;
    private boolean emptyLineFill;
    private boolean backslashEndsPrevLine;
    private int defineTabCount;

}

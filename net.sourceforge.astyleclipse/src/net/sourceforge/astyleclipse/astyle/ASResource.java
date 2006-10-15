/*
 * :tabSize=8:indentSize=4:noTabs=true:maxLineLen=0:
 *
 * Copyright (c) 1998,1999,2000,2001 Tal Davidson. All rights reserved.
 *
 * ASResource.java
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


/**
 * Constants and definitions used in AStyle.
 */
public interface ASResource {

    /**
     * bracket mode NONE (default):
     * disable bracket formatting
     */
    public final static int NONE_MODE = 0;

    /**
     * bracket mode ATTACH (option "--brackets=attach" or
     * "--style=java" or "--style=kr")::
     * Java / Kernighan &amp; Richie style formatting.
     */
    public final static int ATTACH_MODE = 1;

    /**
     * bracket mode BREAK (option "--brackets=break" or
     * "--style=ansi" or "--style=gnu"):
     * Gnu/Ansi C/C++ style formatting.
     */
    public final static int BREAK_MODE = 2;

    /**
     * bracket mode BDAC (option "--brackets=linux" or "--style=linux"):
     * Linux style formatting.
     */
    public final static int BDAC_MODE = 3;

    public final static String AS_IF = "if";
    public final static String AS_ELSE = "else";
    public final static String AS_FOR = "for";
    public final static String AS_DO = "do";
    public final static String AS_WHILE = "while";
    public final static String AS_SWITCH = "switch";
    public final static String AS_CASE = "case";
    public final static String AS_DEFAULT = "default";
    public final static String AS_CLASS = "class";
    public final static String AS_STRUCT = "struct";
    public final static String AS_UNION = "union";
    public final static String AS_INTERFACE = "interface";
    public final static String AS_NAMESPACE = "namespace";
    public final static String AS_EXTERN = "extern";
    public final static String AS_PUBLIC = "public";
    public final static String AS_PROTECTED = "protected";
    public final static String AS_PRIVATE = "private";
    public final static String AS_STATIC = "static";
    public final static String AS_SYNCHRONIZED = "synchronized";
    public final static String AS_OPERATOR = "operator";
    public final static String AS_TEMPLATE = "template";
    public final static String AS_TRY = "try";
    public final static String AS_CATCH = "catch";
    public final static String AS_FINALLY = "finally";
    public final static String AS_THROWS = "throws";
    public final static String AS_CONST = "const";

    public final static String AS_ASM = "asm";

    public final static String AS_BAR_DEFINE = "#define";
    public final static String AS_BAR_INCLUDE = "#include";
    public final static String AS_BAR_IF = "#if";
    public final static String AS_BAR_EL = "#el";
    public final static String AS_BAR_ENDIF = "#endif";

    public final static String AS_OPEN_BRACKET = "{";
    public final static String AS_CLOSE_BRACKET = "}";
    public final static String AS_OPEN_LINE_COMMENT = "//";
    public final static String AS_OPEN_COMMENT = "/*";
    public final static String AS_CLOSE_COMMENT = "*/";

    public final static String AS_ASSIGN = "=";
    public final static String AS_PLUS_ASSIGN = "+=";
    public final static String AS_MINUS_ASSIGN = "-=";
    public final static String AS_MULT_ASSIGN = "*=";
    public final static String AS_DIV_ASSIGN = "/=";
    public final static String AS_MOD_ASSIGN = "%=";
    public final static String AS_OR_ASSIGN = "|=";
    public final static String AS_AND_ASSIGN = "&=";
    public final static String AS_XOR_ASSIGN = "^=";
    public final static String AS_GR_GR_ASSIGN = ">>=";
    public final static String AS_LS_LS_ASSIGN = "<<=";
    public final static String AS_GR_GR_GR_ASSIGN = ">>>=";
    public final static String AS_LS_LS_LS_ASSIGN = "<<<=";
    public final static String AS_RETURN = "return";

    public final static String AS_EQUAL = "==";
    public final static String AS_PLUS_PLUS = "++";
    public final static String AS_MINUS_MINUS = "--";
    public final static String AS_NOT_EQUAL = "!=";
    public final static String AS_GR_EQUAL = ">=";
    public final static String AS_GR_GR = ">>";
    public final static String AS_GR_GR_GR = ">>>";
    public final static String AS_LS_EQUAL = "<=";
    public final static String AS_LS_LS = "<<";
    public final static String AS_LS_LS_LS = "<<<";
    public final static String AS_ARROW = "->";
    public final static String AS_AND = "&&";
    public final static String AS_OR = "||";
    public final static String AS_COLON_COLON = "::";
    public final static String AS_PAREN_PAREN = "(";
    public final static String AS_BLPAREN_BLPAREN = "[]";

    public final static String AS_PLUS = "+";
    public final static String AS_MINUS = "-";
    public final static String AS_MULT = "*";
    public final static String AS_DIV = "/";
    public final static String AS_MOD = "%";
    public final static String AS_GR = ">";
    public final static String AS_LS = "<";
    public final static String AS_NOT = "!";
    public final static String AS_BIT_OR = "|";
    public final static String AS_BIT_AND = "&";
    public final static String AS_BIT_NOT = "~";
    public final static String AS_BIT_XOR = "^";
    public final static String AS_QUESTION = "?";
    public final static String AS_COLON = ":";
    public final static String AS_COMMA = ",";
    public final static String AS_SEMICOLON = ";";

}

/*
 * :tabSize=8:indentSize=4:noTabs=true:maxLineLen=0:
 *
 * Copyright (c) 2001 Dirk Moebius.
 *
 * BooleanStack.java - a wrapper class for a typesafe stack.
 *
 * Part of the C++ to Java port of AStyle, maintained by
 * Dirk Moebius (dmoebius@gmx.net).
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


package net.sourceforge.astyleclipse.astyle.util;


import java.util.Stack;


// FIXME: this class could more efficiently be implemented as array of boolean
// or bitset

/**
 * This is a stack of booleans.
 */
public class BooleanStack extends Stack {

    public BooleanStack() {
        super();
    }


    /**
     * same as push()
     * @see java.util.Stack#push
     */
    public void push_back(boolean b) {
        super.push(new Boolean(b));
    }


    /**
     * same as pop()
     * @see java.util.Stack#pop
     */
    public boolean pop_back() {
        if (empty())
            throw new Error("empty stack");
        return ((Boolean)super.pop()).booleanValue();
    }


    /**
     * same as peek()
     * @see java.util.Stack#peek
     */
    public boolean back() {
        if (empty())
            throw new Error("empty stack");
        return ((Boolean)super.peek()).booleanValue();
    }


    public BooleanStack getClone() {
        return (BooleanStack) clone();
    }

}


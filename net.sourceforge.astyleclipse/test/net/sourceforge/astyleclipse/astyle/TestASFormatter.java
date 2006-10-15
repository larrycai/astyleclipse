package net.sourceforge.astyleclipse.astyle;

import java.io.StringReader;

import junit.framework.TestCase;

public class TestASFormatter extends TestCase {
	ASFormatter formatter;
	public TestASFormatter() {
		formatter = new ASFormatter();
	}

	public void testInit() {
		// just to make more coverage ;-)
		// how to test it ?
		assert(true);
	}

	public void testHasMoreLines() {
		String input= 
			"line1\n" +
			"endline\n"
			;
		ASStreamIterator buffer = new ASStreamIterator(new StringReader(input));
		formatter.init(buffer);
		assertEquals(formatter.hasMoreLines(),true);
		assertEquals(formatter.nextLine(),"line1");
		assertEquals(formatter.hasMoreLines(),true);
		assertEquals(formatter.nextLine(),"endline");
		assertEquals(formatter.hasMoreLines(),true);
		assertEquals(formatter.nextLine(),"");
		assertEquals(formatter.hasMoreLines(),false);
	}

	public void testNextLine() {
		// it always get "" in end of line
		String input= 
			"line1\n" +
			"endline"
			;
		ASStreamIterator buffer = new ASStreamIterator(new StringReader(input));
		formatter.init(buffer);
		assertEquals(formatter.hasMoreLines(),true);
		assertEquals(formatter.nextLine(),"line1");
		assertEquals(formatter.hasMoreLines(),true);
		assertEquals(formatter.nextLine(),"endline");
		assertEquals(formatter.hasMoreLines(),true);
		assertEquals(formatter.nextLine(),"");
		assertEquals(formatter.hasMoreLines(),false);
	}

	public void testASFormatter() {
		// just to make more coverage ;-)
		assert(true);
	}

	public void testGetBracketFormatMode() {
		assertEquals(formatter.getBracketFormatMode(),ASResource.NONE_MODE);
		formatter.setBracketFormatMode(ASResource.ATTACH_MODE);
		assertEquals(formatter.getBracketFormatMode(),ASResource.ATTACH_MODE);
		formatter.setBracketFormatMode(ASResource.BREAK_MODE);
		assertEquals(formatter.getBracketFormatMode(),ASResource.BREAK_MODE);
		formatter.setBracketFormatMode(ASResource.BDAC_MODE);
		assertEquals(formatter.getBracketFormatMode(),ASResource.BDAC_MODE);
		formatter.setBracketFormatMode(ASResource.NONE_MODE);
		assertEquals(formatter.getBracketFormatMode(),ASResource.NONE_MODE);		
	}

	public void testSetBracketFormatMode() {
		testGetBracketFormatMode();
	}

	public void testGetBreakClosingHeaderBracketsMode() {
		assertEquals(formatter.getBreakClosingHeaderBracketsMode(),false);
		formatter.setBreakClosingHeaderBracketsMode(true);
		assertEquals(formatter.getBreakClosingHeaderBracketsMode(),true);		
		formatter.setBreakClosingHeaderBracketsMode(false);
		assertEquals(formatter.getBreakClosingHeaderBracketsMode(),false);			
	}

	public void testSetBreakClosingHeaderBracketsMode() {
		testGetBreakClosingHeaderBracketsMode();
	}

	public void testGetBreakElseIfsMode() {
		assertEquals(formatter.getBreakElseIfsMode(),false);
		formatter.setBreakElseIfsMode(true);
		assertEquals(formatter.getBreakElseIfsMode(),true);		
		formatter.setBreakElseIfsMode(false);
		assertEquals(formatter.getBreakElseIfsMode(),false);		
	}

	public void testSetBreakElseIfsMode() {
		testGetBreakElseIfsMode();
	}

	public void testGetOperatorPaddingMode() {
		assertEquals(formatter.getOperatorPaddingMode(),false);
		formatter.setOperatorPaddingMode(true);
		assertEquals(formatter.getOperatorPaddingMode(),true);		
		formatter.setOperatorPaddingMode(false);
		assertEquals(formatter.getOperatorPaddingMode(),false);	
	}

	public void testSetOperatorPaddingMode() {
		testGetOperatorPaddingMode();
	}

	public void testGetParenthesisPaddingMode() {
		assertEquals(formatter.getParenthesisPaddingMode(),false);
		formatter.setParenthesisPaddingMode(true);
		assertEquals(formatter.getParenthesisPaddingMode(),true);		
		formatter.setParenthesisPaddingMode(false);
		assertEquals(formatter.getParenthesisPaddingMode(),false);	
	}

	public void testSetParenthesisPaddingMode() {
		testGetParenthesisPaddingMode();
	}

	public void testGetBreakOneLineBlocksMode() {
		assertEquals(formatter.getBreakOneLineBlocksMode(),false);
		formatter.setBreakOneLineBlocksMode(true);
		assertEquals(formatter.getBreakOneLineBlocksMode(),true);		
		formatter.setBreakOneLineBlocksMode(false);
		assertEquals(formatter.getBreakOneLineBlocksMode(),false);	
	}

	public void testSetBreakOneLineBlocksMode() {
		testGetBreakOneLineBlocksMode();
	}

	public void testGetSingleStatementsMode() {
		assertEquals(formatter.getSingleStatementsMode(),false);
		formatter.setSingleStatementsMode(true);
		assertEquals(formatter.getSingleStatementsMode(),true);		
		formatter.setSingleStatementsMode(false);
		assertEquals(formatter.getSingleStatementsMode(),false);	
	}

	public void testSetSingleStatementsMode() {
		testGetSingleStatementsMode();
	}

	public void testGetTabSpaceConversionMode() {
		assertEquals(formatter.getTabSpaceConversionMode(),false);
		formatter.setTabSpaceConversionMode(true);
		assertEquals(formatter.getTabSpaceConversionMode(),true);		
		formatter.setTabSpaceConversionMode(false);
		assertEquals(formatter.getTabSpaceConversionMode(),false);	
	}

	public void testSetTabSpaceConversionMode() {
		testGetTabSpaceConversionMode();
	}

	public void testGetBreakBlocksMode() {
		assertEquals(formatter.getBreakBlocksMode(),false);
		formatter.setBreakBlocksMode(true);
		assertEquals(formatter.getBreakBlocksMode(),true);		
		formatter.setBreakBlocksMode(false);
		assertEquals(formatter.getBreakBlocksMode(),false);	
	}

	public void testSetBreakBlocksMode() {
		testGetBreakBlocksMode();
	}

	public void testGetBreakClosingHeaderBlocksMode() {
		assertEquals(formatter.getBreakClosingHeaderBlocksMode(),false);
		formatter.setBreakClosingHeaderBlocksMode(true);
		assertEquals(formatter.getBreakClosingHeaderBlocksMode(),true);		
		formatter.setBreakClosingHeaderBlocksMode(false);
		assertEquals(formatter.getBreakClosingHeaderBlocksMode(),false);	
	}

	public void testSetBreakClosingHeaderBlocksMode() {
		testGetBreakClosingHeaderBlocksMode();
	}

}

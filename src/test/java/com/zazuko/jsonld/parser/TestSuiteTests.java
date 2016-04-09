/*
 * The MIT License
 *
 * Copyright 2016 user.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.zazuko.jsonld.parser;

import static com.zazuko.jsonld.parser.ParserTest.testFromResource;
import java.io.IOException;
import java.io.InputStream;
import org.apache.clerezza.commons.rdf.IRI;
import org.apache.clerezza.commons.rdf.ImmutableGraph;
import org.apache.clerezza.rdf.core.serializedform.Parser;
import org.apache.clerezza.rdf.core.serializedform.SupportedFormat;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.wymiwyg.commons.util.dirbrowser.PathNameFilter;
import org.wymiwyg.commons.util.dirbrowser.PathNode;
import org.wymiwyg.commons.util.dirbrowser.PathNodeFactory;

/**
 *
 * @author user
 */
public class TestSuiteTests {
    
    public TestSuiteTests() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void testSuiteTests() throws Exception {
        PathNode testsuite = PathNodeFactory.getPathNode(getClass().getResource("testsuite"));
        for (String li : testsuite.list((PathNode pn, String string) -> string.endsWith("in.jsonld"))) {
            testFromResource(li.substring(0, li.length()-10));
        }
    }
    
    static void testFromResource(String baseName) throws Exception {
        final Parser parser = Parser.getInstance();
        final InputStream inTurtle = ParserTest.class.getResourceAsStream("testsuite/"+baseName+"-out.nq");
        final ImmutableGraph expected = parser.parse(inTurtle, SupportedFormat.TURTLE);
        final String jsonldFileName = baseName+"-in.jsonld";
        IRI base = new IRI("http://json-ld.org/test-suite/tests/"+jsonldFileName);
        ParserTest.testFromResource("testsuite/"+jsonldFileName, expected, base);
    }
}

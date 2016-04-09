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

import com.zazuko.jsonld.parser.JsonLdParser;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import org.apache.clerezza.commons.rdf.Graph;
import org.apache.clerezza.commons.rdf.IRI;
import org.apache.clerezza.commons.rdf.ImmutableGraph;
import org.apache.clerezza.commons.rdf.impl.utils.simple.SimpleGraph;
import org.apache.clerezza.rdf.core.serializedform.Parser;
import org.apache.clerezza.rdf.core.serializedform.SupportedFormat;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author user
 */
public class ParserTest {

    public ParserTest() {
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

    static void testFromResource(String baseName) throws Exception {
        final Parser parser = Parser.getInstance();
        final InputStream inTurtle = ParserTest.class.getResourceAsStream(baseName+".ttl");
        final ImmutableGraph expected = parser.parse(inTurtle, SupportedFormat.TURTLE);
        testFromResource(baseName+".json", expected, new IRI("http://base/"));
    }
    static void testFromResource(String fileName, ImmutableGraph expected, IRI base) throws Exception {
        final InputStream inJsonLd = ParserTest.class.getResourceAsStream(fileName);
        final Graph graph = new SimpleGraph();
        JsonLdParser.parse(inJsonLd, graph, base);
        final ImmutableGraph result = graph.getImmutableGraph();
        Assert.assertEquals(expected, result);
        //And parse to stream
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final InputStream inJsonLd2 = ParserTest.class.getResourceAsStream(fileName);
        JsonLdParser.parse(inJsonLd2, baos, base);
        final InputStream inN3Output = new ByteArrayInputStream(baos.toByteArray());
        final Parser parser = Parser.getInstance();
        final ImmutableGraph n3output = parser.parse(inN3Output, SupportedFormat.N_TRIPLE);
        Assert.assertEquals(expected, n3output);
    }
    
    @Test
    public void simple() throws Exception {
        testFromResource("simple");
    }
    
    @Test
    public void simpleNamed() throws Exception {
        testFromResource("simple-named");
    }
    
    @Test
    public void nestedWithoutId() throws Exception {
        testFromResource("nested-without-id");
    }
    
    @Test
    public void typedLiteral() throws Exception {
        testFromResource("typed-literal");
    }
    
    @Test
    public void interlis() throws Exception {
        testFromResource("interlis");
    }
    
    @Test
    public void knowsCircle() throws Exception {
        testFromResource("knows-circle");
    }
    
    @Test
    public void disconnected() throws Exception {
        testFromResource("disconnected");
    }
    
    @Test
    public void languageLiteral() throws Exception {
        testFromResource("language-literal");
    }
    
    @Test
    public void list() throws Exception {
        testFromResource("list");
    }
    
    @Test
    public void relative() throws Exception {
        testFromResource("relative");
    }
    
}

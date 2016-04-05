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

import com.sun.javafx.scene.control.skin.VirtualFlow;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import javax.json.Json;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;
import javax.json.stream.JsonParserFactory;
import org.apache.clerezza.commons.rdf.BlankNode;
import org.apache.clerezza.commons.rdf.BlankNodeOrIRI;
import org.apache.clerezza.commons.rdf.Graph;
import org.apache.clerezza.commons.rdf.IRI;
import org.apache.clerezza.commons.rdf.Language;
import org.apache.clerezza.commons.rdf.Literal;
import org.apache.clerezza.commons.rdf.RDFTerm;
import org.apache.clerezza.commons.rdf.Triple;
import org.apache.clerezza.commons.rdf.impl.utils.PlainLiteralImpl;
import org.apache.clerezza.commons.rdf.impl.utils.TripleImpl;
import org.apache.clerezza.commons.rdf.impl.utils.TypedLiteralImpl;
import org.apache.clerezza.rdf.ontologies.RDF;

/**
 *
 * @author user
 */
public class JsonLdParser {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws FileNotFoundException {
        final String fileName = args[0];
        final File file = new File(fileName);
        if (!file.exists()) {
            System.err.println("File " + file + " does not exist");
            System.exit(-1);
        }
        final FileInputStream in = new FileInputStream(file);
        parse(in, System.out);
    }

    static void parse(final InputStream in, final OutputStream out) {
        final PrintWriter printWriter;
        try {
            printWriter = new PrintWriter(new OutputStreamWriter(out, "utf-8"), true);
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        }
        parse(in, new TripleSink() {
            
            final WeakHashMap<BlankNode, String> node2IdMap = new WeakHashMap<>();
            int idCounter = 1;
            
            @Override
            public void add(Triple triple) {
                printWriter.println(toNT(triple));
            }

            private String toNT(Triple triple) {
                return toNT(triple.getSubject()) + " " + toNT(triple.getPredicate()) + " " + toNT(triple.getObject()) + ".";
            }

            private String toNT(Literal literal) {
                //TODO real impl
                return literal.toString();
            }

            private String toNT(IRI iri) {
                //TODO real impl
                return iri.toString();
            }

            private String toNT(BlankNode node) {
                String id = node2IdMap.get(node);
                if (id == null) {
                    id = Integer.toString(idCounter);
                    idCounter++;
                    node2IdMap.put(node, id);
                }
                return "_:"+id;
            }
            
            private String toNT(RDFTerm node) {
                if (node instanceof Literal) {
                    return toNT((Literal)node);
                } else {
                    return toNT((BlankNodeOrIRI)node);
                }
            }
            
            private String toNT(BlankNodeOrIRI node) {
                if (node instanceof IRI) {
                    return toNT((IRI)node);
                } else {
                    return toNT((BlankNode)node);
                }
            }
        });
    }

    static void parse(final InputStream in, final Graph graph) {
        parse(in, new TripleSink() {
            @Override
            public void add(Triple triple) {
                graph.add(triple);
            }

        });
    }

    static void parse(InputStream in, TripleSink sink) {
        final JsonParserFactory factory = Json.createParserFactory(null);
        final JsonParser jsonParser = factory.createParser(in, Charset.forName("utf-8"));
        JsonLdParser jsonLdParser = new JsonLdParser(jsonParser, sink);
        jsonLdParser.parse();
    }

    private final JsonParser jsonParser;
    private final TripleSink sink;
    private final Map<String, BlankNode> label2bnodeMap = new HashMap<>();

    private JsonLdParser(JsonParser jsonParser, TripleSink sink) {
        this.jsonParser = jsonParser;
        this.sink = sink;
    }

    private void parse() {
        final Event firstEvent = jsonParser.next();
        switch (firstEvent) {
            case START_OBJECT: {
                parseJsonObject();
                break;
            }
            default: {
                throw new RuntimeException("Currently only documents staring with resorce are supported, got: " + firstEvent);
            }
        }
    }

    private void parseJsonObject() {
        SubjectParser subjectParser = new SubjectParser();
        subjectParser.parse();
    }

    private BlankNode getBlankNode(String identifier) {
        BlankNode result = label2bnodeMap.get(identifier);
        if (result == null) {
            result = new BlankNode();
            label2bnodeMap.put(identifier, result);
        }
        return result;
    }

    class SubjectParser {

        private IRI ambiguousTypeIRI = null;
        private String value = null;
        private Language language = null;
        private RDFTerm node;

        public void parse() {
            JsonParser.Event first = jsonParser.next();
            if (!first.equals(JsonParser.Event.KEY_NAME)) {
                throw new RuntimeException("Sorry");
            }
            String firstKey = jsonParser.getString();
            if (firstKey.equals("@id")) {
                node = parseId();
                if (jsonParser.next().equals(JsonParser.Event.END_OBJECT)) {
                    return;
                }
            }
            handleKey();
            while (jsonParser.hasNext()) {
                final Event next = jsonParser.next();
                switch (next) {
                    case KEY_NAME: {
                        handleKey();
                        break;
                    }
                    case END_OBJECT: {
                        if (value != null) {
                            if (node != null) {
                                throw new RuntimeException("@value combined with incompatible key");
                            }
                            if (language != null) {
                                node = new PlainLiteralImpl(value, language);
                            } else {
                                node = new TypedLiteralImpl(value, ambiguousTypeIRI);
                            }
                            return;
                        }
                        if (ambiguousTypeIRI != null) {
                            sink.add(new TripleImpl(getSubject(), RDF.type, ambiguousTypeIRI));
                        }
                        return;
                    }
                    default: {
                        throw new RuntimeException("Not supported here: " + next);
                    }
                }

            }
        }

        //called when the resource represented by this node is used as subject
        private BlankNodeOrIRI getSubject() {
            if (node == null) {
                node = new BlankNode();
            }
            return (BlankNodeOrIRI) node;
        }

        private BlankNodeOrIRI parseId() {
            jsonParser.next();
            final String identifier = jsonParser.getString();
            if (identifier.startsWith("_:")) {
                return getBlankNode(identifier);
            } else {
                return new IRI(identifier);
            }
        }

        private IRI getIRI(String keyName) {
            return new IRI(keyName);
        }

        private void handleKey() {
            final String keyName = jsonParser.getString();
            if (keyName.equals("@type")) {
                //either datatype or rdf type
                //@type value must a string, an array of strings (, or an empty object?)
                IRI[] types;
                final Event next = jsonParser.next();
                switch (next) {
                    case VALUE_STRING: {
                        types = new IRI[]{new IRI(jsonParser.getString())};
                        break;
                    }
                    case START_ARRAY: {
                        types = readTypes();
                        break;
                    }
                    default: {
                        throw new RuntimeException("Not supported here: " + next);
                    }
                }
                //if a single type is defined it could also be a datype for a literal
                //otherwise they are rdf types
                if (types.length == 1) {
                    ambiguousTypeIRI = types[0];
                } else {
                    for (IRI type : types) {
                        sink.add(new TripleImpl(getSubject(), RDF.type, type));
                    }
                }
                return;
            }
            if (keyName.equals("@language")) {
                final Event next = jsonParser.next();
                switch (next) {
                    case VALUE_STRING: {
                        language = new Language(jsonParser.getString());
                        break;
                    }
                    default: {
                        throw new RuntimeException("Language must be a string" + next);
                    }
                }
                return;
            }
            if (keyName.equals("@value")) {
                final Event next = jsonParser.next();
                switch (next) {
                    case VALUE_STRING: {
                        value = jsonParser.getString();
                        break;
                    }
                    default: {
                        throw new RuntimeException("Value must be a string" + next);
                    }
                }
                return;
            }
            final IRI property = getIRI(keyName);
            final ObjectParser subjectPredicateParser = new ObjectParser(getSubject(), property);
            subjectPredicateParser.parse();

        }

        private IRI[] readTypes() {
            final List<IRI> types = new VirtualFlow.ArrayLinkedList<>();
            while (jsonParser.hasNext()) {
                final Event next = jsonParser.next();
                switch (next) {
                    case VALUE_STRING: {
                        types.add(new IRI(jsonParser.getString()));
                        break;
                    }
                    case END_ARRAY: {
                        return types.toArray(new IRI[types.size()]);
                    }
                    default: {
                        throw new RuntimeException("Not supported here: " + next);
                    }
                }
            }
            throw new RuntimeException("Unterminated Array");
        }

    }

    class ObjectParser {

        private final BlankNodeOrIRI subject;
        private final IRI predicate;

        public ObjectParser(BlankNodeOrIRI subject, IRI predicate) {
            this.subject = subject;
            this.predicate = predicate;
        }

        void parse() {
            final Event firstEvent = jsonParser.next();
            switch (firstEvent) {
                case START_OBJECT: {
                    parseSingleObject();
                    break;
                }
                case START_ARRAY: {
                    parseArray();
                    break;
                }
                case VALUE_STRING: {
                    sink.add(new TripleImpl(subject, predicate, new PlainLiteralImpl(jsonParser.getString())));
                    break;
                }
                default: {
                    throw new RuntimeException("Currently only documents staring with resorce are supported, got: " + firstEvent);
                }
            }
        }

        private void parseSingleObject() {
            final SubjectParser subjectParser = new SubjectParser();
            subjectParser.parse();
            sink.add(new TripleImpl(subject, predicate, subjectParser.node));
        }

        private void parseArray() {
            while (jsonParser.hasNext()) {
                final Event next = jsonParser.next();
                switch (next) {
                    case START_OBJECT: {
                        parseSingleObject();
                        break;
                    }
                    case VALUE_STRING: {
                        sink.add(new TripleImpl(subject, predicate, new PlainLiteralImpl(jsonParser.getString())));
                        break;
                    }
                    case END_ARRAY: {
                        return;
                    }
                    default: {
                        throw new RuntimeException("Not supported here: " + next);
                    }
                }
            }
        }

    }
}

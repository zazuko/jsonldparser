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

import java.util.HashMap;
import java.util.Map;
import javax.json.stream.JsonParser;
import org.apache.clerezza.commons.rdf.BlankNodeOrIRI;
import org.apache.clerezza.commons.rdf.IRI;

/**
 *
 * @author user
 */
public class ContextParser {
    
    private JsonParser jsonParser;
    private Context parent;

    public ContextParser(JsonParser jsonParser, Context parent) {
        this.jsonParser = jsonParser;
        this.parent = parent;
    }
    
    Context parse() {
        final Context result = new Context();
            JsonParser.Event firstKey = jsonParser.next();
            /*its value MUST be null, an absolute IRI, a relative IRI, a context 
            definition, or an array composed of any of these.*/
            switch (firstKey) {
                case START_OBJECT: {
                    //its a context definition
                    break;
                }
                default: throw new RuntimeException("Sorry we currently supoort only inline context definitions");
            }
            while (jsonParser.hasNext()) {
                final JsonParser.Event next = jsonParser.next();
                switch (next) {
                    case KEY_NAME: {
                        handleKey(result);
                        break;
                    }
                    case END_OBJECT: {
                        return result;
                    }
                    default: {
                        throw new RuntimeException("Not supported here: " + next);
                    }
                }
            }
            throw new RuntimeException("Unexpected end of JSON data");
    }

    private void handleKey(Context target) {
        final String term = jsonParser.getString();
        JsonParser.Event valueEvent = jsonParser.next();
        switch (valueEvent) {
            case VALUE_STRING: {
                //TODO handle null
                //TODO apparently terms can be defined using terms from the same context as long as its not circular
                //TODO support blank nodes
                BlankNodeOrIRI value = parent.resolve(jsonParser.getString());
                target.register(term, value);
            }
            case START_OBJECT: {
                throw new RuntimeException("Expanded term definition not yet supported");
            }
        }
    }
    
    static class Context {

        private final Map<String, BlankNodeOrIRI> termMap = new HashMap<>();
        public Context() {
        }

        private BlankNodeOrIRI resolve(String key) {
            final BlankNodeOrIRI value = termMap.get(key);
            if (value != null) {
                return value;
            }
            final int colonPos = key.indexOf(':');
            if (colonPos > -1) {
                final String prefix = key.substring(0, colonPos);
                final IRI expanded = (IRI) termMap.get(prefix);
                if (expanded != null) {
                    return new IRI(expanded.getUnicodeString()+key.substring(colonPos+1));
                }
            }
            return new IRI(key);
        }

        private void register(String term, BlankNodeOrIRI value) {
            termMap.put(term, value);
        }
    }
    
}

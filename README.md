# jsonldparser

Efficient [JSON-LD](http://json-ld.org/) parsing.

Cuttently supporty only a tiny subset of the JSON-LD format. Most notably it 
doesn't support contexts (@Context).

Other limitations:
- The `@id` is onli allowed as the first key of an object
 

## So what can it do

- Parse rather large files
- See the files in [src/test/resources/com/zazuko/jsonld/parser](src/test/resources/com/zazuko/jsonld/parser) for example of files that it can parse.
 
## How to use it

The following lines from the test illustarte the programmatic usage:

```
final InputStream inJsonLd = getClass().getResourceAsStream(baseName+".json");
final Graph graph = new SimpleGraph();
JsonLdParser.parse(inJsonLd, graph);
```

Nota that you don't need to pass a graph but you can pass an instance of [TripleSink](com/zazuko/jsonld/parser/TripleSink.java) for efficient streaming parsing.

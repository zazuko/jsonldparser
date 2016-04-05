# jsonldparser

Efficient [JSON-LD](http://json-ld.org/) parsing.

Cuttently supporty only a tiny subset of the JSON-LD format. Most notably it 
doesn't support contexts (@Context).

Other limitations:
- The `@id` is onli allowed as the first key of an object

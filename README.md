# scufl2-info

[![Build Status](https://travis-ci.org/stain/scufl2-info.svg?branch=0.4.0)](https://travis-ci.org/stain/scufl2-info)

Returns Linked Data information about a [SCUFL2](http://dev.mygrid.org.uk/wiki/display/developer/SCUFL2) 
resource.

The idea is that say

    http://ns.taverna.org.uk/2010/workflowBundle/74674670-04d0-4832-9651-bfc3a2ec3a19/workflow/HelloWorld/processor/Hello/out/fred"

will redirect to this service to "guess" the partial workflow structure
expressed in the URI - e.g. that we're talking about an output port 
`fred` in processor `Hello` in workflow `HelloWorld` in workflow bundle
with uuid `746....a19`.

## Example

    http://localhost:3000/workflowBundle/74674670-04d0-4832-9651-bfc3a2ec3a19/workflow/HelloWorld/processor/Hello%20there/out/fred

returns [JSON-LD](http://json-ld.org/)):

    
    {
      "workflow": {
        "processor": {
          "inputProcessorPort": {
            "@id": "workflow/HelloWorld/processor/Hello/in/hello",
            "@type": "InputProcessorPort",
            "name": "hello"
          },
          "@id": "workflow/HelloWorld/processor/Hello/",
          "@type": "Processor",
          "name": "Hello"
        },
        "@id": "workflow/HelloWorld/",
        "@type": "Workflow",
        "name": "HelloWorld"
      },
      "@context": {
        "@base": "http://ns.taverna.org.uk/2010/workflowBundle/74674670-04d0-4832-9651-bfc3a2ec3a19/",
        "@vocab": "http://ns.taverna.org.uk/2010/scufl2#"
      },
      "@id": "http://ns.taverna.org.uk/2010/workflowBundle/74674670-04d0-4832-9651-bfc3a2ec3a19/",
      "@type": "WorkflowBundle"
    }
    


Which is equivalent to the RDF triples:

    @base <http://ns.taverna.org.uk/2010/workflowBundle/74674670-04d0-4832-9651-bfc3a2ec3a19/> .
    <> <http://ns.taverna.org.uk/2010/scufl2#workflow> <workflow/HelloWorld/> .
    <> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ns.taverna.org.uk/2010/scufl2#WorkflowBundle> .
    <workflow/HelloWorld/> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ns.taverna.org.uk/2010/scufl2#Workflow> .
    <workflow/HelloWorld/> <http://ns.taverna.org.uk/2010/scufl2#name> "HelloWorld" .
    <workflow/HelloWorld/> <http://ns.taverna.org.uk/2010/scufl2#processor> <workflow/HelloWorld/processor/Hello/> .
    <workflow/HelloWorld/processor/Hello/> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ns.taverna.org.uk/2010/scufl2#Processor> .
    <workflow/HelloWorld/processor/Hello/> <http://ns.taverna.org.uk/2010/scufl2#inputProcessorPort> <workflow/HelloWorld/processor/Hello/in/hello> .
    <workflow/HelloWorld/processor/Hello/> <http://ns.taverna.org.uk/2010/scufl2#name> "Hello" .
    <workflow/HelloWorld/processor/Hello/in/hello> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ns.taverna.org.uk/2010/scufl2#InputProcessorPort> .
    <workflow/HelloWorld/processor/Hello/in/hello> <http://ns.taverna.org.uk/2010/scufl2#name> "hello" .
    
This corresponds to what is defined inside the scufl2 workflow bundle.

## TODO

- control links, activities, profiles, configurations, dispatch layer
- Other RDF formats: RDF/XML and Turtle
- Search myExperiment by UUID to add rdfs:seeAlso 
- wfdesc


## Prerequisites

You will need [Leiningen][1] 1.7.0 or above installed.

[1]: https://github.com/technomancy/leiningen

## Running

To start a developer web server for the application, run:

    lein ring server

To generate a WAR that can be installed into a servlet container like Apache Tomcat:

    lein ring uberwar
    

## Docker image

This service is also available as a Docker image 
[stain/scufl2-info](https://registry.hub.docker.com/u/stain/scufl2-info/), exposing the ports `8080` (HTTP) and `8009` (AJP)
through [tomcat](https://registry.hub.docker.com/_/tomcat/).

    docker run -p 8080:8080 stain/scufl2-info


## License

Copyright © 2014 University of Manchester

This software is licensed under the [MIT license](LICENSE.txt).


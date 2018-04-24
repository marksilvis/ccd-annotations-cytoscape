# CCD Annotations for Cytoscape
This app aims to bring CCD annotations to Cytoscape.

## Requirements
- Cytoscape 3.5 or greater
- Java 1.8
- Maven 3

## Build
`mvn clean package` (add option `-DskipTests` to skip unit tests)

## Install
### Cytoscape Apps Directory (Recommended)
1. Build the app
1. Locate the jar file **ccd-annotations-cytoscape-0.5.0.jar** in the `target/` directory
1. Locate your Cytscape installation directory
1. Copy the jar file to the `apps/` subdirectory of your Cytoscape installation
1. If Cytoscape is open, you must restart it for the changes to take effect
### Cytoscape App Manager
1. Build the app
1. Open Cytoscape
1. Open the App Manager via **Apps -> App Manager...**
1. Click **Install from File...**
1. Locate the build directory and select the jar file **ccd-annotations-cytoscape-0.5.0.jar** from the `target/` subdirectory
1. Restart Cytoscape for the changes to take effect

## CCD Annotations Guide

The goal of this plugin is to extend Cytoscape's annotating capabilities in order to
be able to connect annotations with network components and support annotation
searching and filtering. To this end, CCD-annotation-plugin follows a dual level
view on annotations: the __Visualization__ and the __Semantic__ view.

The __Visualization__ view is responsible for rendering an annotation on a network
and is mostly handled by Cytoscape's visualization engine.
The __Semantic__ view is responsible for annotation, search, filter actions, in
agreement with Cytoscape's OSGi specifications.

The aforementioned views appear in the storage engine of the plugin and on Import/Export
functionality of Cytoscape.

### Annotation Semantic View

The __CCD Annotations for Cytoscape__ plugin (__ccd_annot__) is designed to support two types of
CCD annotations:
 1. __holistic__ : An annotation that refers to the whole network.
 1. __specific__ : An annotation that exists by referencing one or more network components (i.e., nodes, edges).
 __Specific__ annotations can have values associated with each one of their associated
 network components, and the value types supported are: `boolean`, `char`, `int`,
 `float`, and `string`.

Both __holistic__ and __specific__ CCD annotations are declared through an *annotation class*
that contains the following fields:
* `UUID` __id__: unique identifier
* `string` __name__: a 32 character name
* `string` __type__: a description for the value type, which can take a value of
`bool, char, int, float, string` (optional for __holistic__ annotations)
* `string` __description__

By the time an __annotation class__ is registered, multiple __annotation instances__
can be instantiated for different network components. An annotation __instance__ requires some
additional information:
* `int` __suid__: unique identifier of a network component (i.e., node or edge id)
* `UUID` __a_id__: the __id__ of the __annotation class__ it is an instance of
* `UUID` __cy_a_id__: a Cytoscape generated `UUID` for visualization purposes
* `binary` __value__: the value for this particular instance

### Database Engine

The plugin makes use of [HSQLDB](http://hsqldb.org/) (version 2.4) to accommodate main-memory
storage of network components and CCD annotations. All data are stored in main-memory for
improved performance, and for each loaded Network, a different database is created. By the time a
 network session is stopped, HSQLDB drops the main memory database. Therefore, it is the user's
 responsibility to make changes persistent by exporting a CCD-annotated network to a .cyjs file.

### CCD-Database Schema

The database schema consists of the following tables that represent Strong entities in the schema
 (additional details can be found in `edu.pitt.cs.admt.cytoscape.annotations.db.AnnotationSchema`):

- __Node__ (suid INTEGER)
- __Edge__ (suid INTEGER, source INTEGER, destination INTEGER)
- __Annotation__ (id UUID, name VARCHAR(32), type ENUM(bool, char, int, float, string), description
VARCHAR(64))

Each network strong entity (i.e., Node, Edge, Annotation) has an identifying attribute: for nodes
 and edges it is the *suid* field (as defined in the cyjs input file), and for annotations is the
  *id*. For annotations' *id* a UUID data type is selected to preserve the uniqueness of
  annotations generated by multiple collaborators.

In addition, there exist two relation tables between Strong entities:

- __ANNOT_TO_NODE__ (a_id UUID, cy_a_id UUID, suid INTEGER, value LONGVARBINARY)
- __ANNOT_TO_EDGE__ (a_id UUID, cy_a_id UUID, suid INTEGER, value LONGVARBINARY)

The *a_id* field is a foreign key to the `Annotation.id` field and the `cy_a_id` is the UUID
generated by Cytoscape (used for visualization purposes). The `suid` is a foreign key to the
corresponding entity's id (either `Node` or `Edge`) and `value` holds a value for that particular
 annotation (it is optional).

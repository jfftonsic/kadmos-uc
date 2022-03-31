Command to generate a puml file from savings-a:
java -jar documentation/plantuml-dependency/plantuml-dependency-cli-1.4.0-jar-with-dependencies.jar -b savings-a/src/main/java -o test.puml

It will print "severe" exceptions, but it will output the file. It will not have anything that is post-java8, such as records.
And it will not go beyond what it can parse from the source files themselves.

Command line Usage
====================

Usage:
	java -jar plantuml-dependency-cli-1.4.0.jar [OPTIONS]


where optional options are:

	-about, --author, --authors
		To display information about PlantUML Dependency, its license and its authors.

	-b, --basedir DIR
		The base directory where to look for source files. If not specified, the default pattern is "." i.e. the directory where the program is launched from.
		DIR specifies a valid and existing directory path, not a single file. It can be absolute or relative.

	-dn, --display-name DISPLAY_NAME_PATTERN
		To specify class diagram objects to display following their name. If not specified, the default is ".*". Note : native calls which are represented by the "NativeCall" name can also be matched by this regular expression even if it is a fictive dependency.
		DISPLAY_NAME_PATTERN specifies display name pattern when generating the plantUML output file, it is a regular expression following the Java pattern (see http://docs.oracle.com/javase/7/docs/api/java/util/regex/Pattern.html for description).

	-dp, --display-package-name DISPLAY_PACKAGE_NAME_PATTERN
		To specify class diagram objects to display following their package name. If not specified, the default is ".*". Note : native calls which are represented by the "javax.native" package name can also be matched by this regular expression even if it is a fictive dependency.
		DISPLAY_PACKAGE_NAME_PATTERN specifies display package name pattern when generating the plantUML output file, it is a regular expression following the Java pattern (see http://docs.oracle.com/javase/7/docs/api/java/util/regex/Pattern.html for description).

	-dt, --display-type DISPLAY_TYPES_OPTIONS
		To specify class diagram objects to display following their type. If not specified, the default is [abstract_classes,annotations,classes,enums,extensions,implementations,imports,interfaces,native_methods,static_imports]
		DISPLAY_TYPES_OPTIONS specifies display types options when generating the plantUML output file, it is a separated comma list with these possible values : [abstract_classes,annotations,classes,enums,extensions,implementations,imports,interfaces,native_methods,static_imports]. "abstract_classes" : displays parsed source files which are abstract classes and relations to abstract classes, "annotations" : displays parsed source files which are annotations, annotations (upon classes and methods) of all parsed source files and relations to annotations, "classes" : displays parsed source files which are classes (not abstract), dependencies which are considered as classes (because they are imported or extended but not parsed) and relations to classes, "enums" : displays parsed source files which are enums and relations to enums, "extensions" : displays relations between dependencies which are extended by parsed source files (i.e. classes or interfaces) if their type is displayed, "implementations" : displays relations between dependencies which are implemented by parsed source files (i.e. interfaces) if their type is displayed, "imports" : displays relations from parsed source files to import dependencies (not static) if their type is displayed, "interfaces" : displays parsed source files which are interfaces, dependencies which are considered as interfaces (because they are implemented but not parsed) and relations to interfaces, "native_methods" : displays relations from parsed source files to the native dependency if they use native methods, "static_imports" : displays relations from parsed source files to import dependencies (only static) if their type is displayed.

	-e, --exclude FILE_PATTERN
		To exclude files that match the provided pattern. If not specified, the default pattern is "**/package-info.java".
		FILE_PATTERN specifies a file pattern, with the same syntax as ANT patterns. It means that "**", "*" or "?" special characters can be used. For more information, please consult http://ant.apache.org/manual/dirtasks.html.

	-h, --help, -?
		To display this help message.

	-i, --include FILE_PATTERN
		To include files that match the provided pattern. If not specified, the default pattern is "**/*.java".
		FILE_PATTERN specifies a file pattern, with the same syntax as ANT patterns. It means that "**", "*" or "?" special characters can be used. For more information, please consult http://ant.apache.org/manual/dirtasks.html.

	-o, --output FILE
		To specify the output file path where to generate the PlantUML description.
		FILE specifies a valid file path, where the file can exist or not and is not a directory. It can be absolute or relative. If the file already exists, it overrides it.

	-v, --verbose [VERBOSE_LEVEL]
		To display log information.
		VERBOSE_LEVEL specifies the verbose level. The argument may consist of either a level name or an integer value. Classical values are : "SEVERE":1000, "WARNING":900, "INFO":800, "CONFIG":700, "FINE":500, "FINER":400, "FINEST":300. By default, if the verbose option is specified but the level is not set, the value "INFO":800 is taken. If not specified, the default value is "WARNING":900.

	-version
		To display versions information about PlantUML Dependency and Java.

Examples:

	java -jar plantuml-dependency-cli-1.4.0.jar -h
	java -jar plantuml-dependency-cli-1.4.0.jar -o /home/test/plantuml.txt -b . -dp ^(?!net.sourceforge.plantumldependency)(.+)$ -v
	java -jar plantuml-dependency-cli-1.4.0.jar -o /home/test/plantuml.txt -b . -i **/*.java -e **/*Test*.java -dn .*Test.* -v
	java -jar plantuml-dependency-cli-1.4.0.jar -o /home/test/plantuml.txt -b . -i **/*.java -e **/*Test*.java -dt implementations,interfaces,extensions,imports,static_imports
	java -jar plantuml-dependency-cli-1.4.0.jar -o myoutput.txt -b "C:\Users\PlantUML test" -i **/*Test.java
	java -jar plantuml-dependency-cli-1.4.0.jar -version -v

Known bugs or program limitations:

	- As PlantUML Dependency reads source files imports, it will generate object - imports relation even if the imports are not used within the object (usually, this raises a warning within the IDE)
	- Be careful, in order to correctly parse source files, it is better to have source code which compiles without any errors
	- Generated annotations (if used) are only supported by PlantUML 7972 and above
	- Import instructions "import package_name.*" are ignored because the dependencies are not explicitly defined, use precise imports instead
	- Links between dependencies are found out by parsing "import" instructions, so PlantUML Dependency won't display dependencies which are called using their full names in the source code
	- PlantUML Dependency can be run by JRE 1.6+
	- PlantUML Dependency is fully compliant up to Java 1.6 source files (and may work with Java 1.7 source files)
	- The generated output file is better when PlantUML Dependency is launched with a JRE matching the parsed source files

# Need to link to a PRISM distribution
PRISM_DIR = ../prism

# For compilation, just need access to classes/jars in the PRISM distribution
# We look in both the top-level and the prism sub-directory
# (currently svn/git repos and downloaded distributions differ in structure)
PRISM_CLASSPATH = "$(PRISM_DIR)/classes:$(PRISM_DIR)/lib/*:$(PRISM_DIR)/prism/classes:$(PRISM_DIR)/prism/lib/*"

# This Makefile just builds all java files in src and puts the class files in classes

JAVA_FILES := $(shell cd src && find ./ctmc -name '*.java')
CLASS_FILES = $(JAVA_FILES:%.java=classes/%.class)

default: all

all: init $(CLASS_FILES)

init:
	@mkdir -p classes

classes/%.class: src/%.java
	(javac -sourcepath src -classpath $(PRISM_CLASSPATH) -d classes $<)

# Clean up

clean:
	@rm -f $(CLASS_FILES)

celan: clean

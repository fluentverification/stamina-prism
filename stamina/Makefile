# Need to link to a PRISM distribution
PRISM_HOME = ""

# environment setup file
STAMINA_RC = ~/.staminarc

# stamina home dir
STAMINA_HOME = "$(PWD)"

# stamina class path 
STAMINA_CLASSPATH = classes

# For compilation, just need access to classes/jars in the PRISM distribution
# We look in both the top-level and the prism sub-directory
# (currently svn/git repos and downloaded distributions differ in structure)
PRISM_CLASSPATH = "$(PRISM_HOME)/classes:$(PRISM_HOME)/lib/*:$(PRISM_HOME)/prism/classes:$(PRISM_HOME)/prism/lib/*"

# This Makefile just builds all java files in src and puts the class files in classes

STAMINA_JAVA_FILES := $(shell cd src && find . -name '*.java')
STAMINA_CLASS_FILES = $(STAMINA_JAVA_FILES:%.java=classes/%.class)

default: all

all: init $(STAMINA_CLASS_FILES)

init:
	@mkdir -p $(STAMINA_CLASSPATH)


classes/%.class: src/%.java
	(javac -sourcepath src -classpath $(STAMINA_CLASSPATH):$(PRISM_CLASSPATH) -d $(STAMINA_CLASSPATH) $<)

	# Export environment variables
	@echo "export PRISM_HOME=$(PRISM_HOME)" > $(STAMINA_RC)
	@echo "export STAMINA_HOME=$(PWD)" >> $(STAMINA_RC)

# Clean up
clean:
	@rm -f $(STAMINA_CLASS_FILES)
	@rm -f $(STAMINA_RC)

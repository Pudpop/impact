JAVAC=/usr/bin/javac
.SUFFIXES: .java .class

BIN=bin
NRS=numberrangesummarizer
SOURCE=src
RES=res

$(BIN)/$(NRS)/%.class: $(SOURCE)/$(NRS)/%.java
	$(JAVAC) -d $(BIN)/ -cp $(BIN) $<

NRSCLASSES=NumberRangeSummarizer.class SummarizeIntegerList.class Summarizer.class Main.class

CLASS_FILES=$(NRSCLASSES:%.class=$(BIN)/$(NRS)/%.class)

default: $(CLASS_FILES)



docs:
	javadoc -d doc $(SOURCE)/numberrangesummarizer/*.java

clean:
	rm -r $(BIN)/*

run:
	java -cp bin numberrangesummarizer/Main

build:
	make
	make run

reb:
	make clean
	make

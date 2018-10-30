CC = g++
DEPS = parlor.h
OBJ = parlordriver.o parlor.o

%.o: %.cpp $(DEPS)
        $(CC) -c -o $@ $<

parlor: $(OBJ)
        $(CC) -o $@ $^

JFLAGS = -g
JC = javac
.SUFFIXES: .java .class
.java.class:
        $(JC) $(JFLAGS) $*.java

CLASSES = \
        DanceDriver.java \
        DanceFloorBarrier.java \
        ParlorDriver.java \
        ParlorMonitor.java \
        ParlorSemaphore.java \
        Parlor.java \
        DanceFloor.java

default: classes

classes: $(CLASSES:.java=.class)

clean:
        $(RM) *.class
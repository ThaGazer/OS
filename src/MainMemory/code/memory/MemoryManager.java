/*
 * MainMemory:MemoryManager
 * Created on 3/22/2018
 *
 * Author(s):
 * -Justin Ritter
 */
package memory;

import java.util.*;

public class MemoryManager {

  private static final String errCommandLine =
      "Usage: <size of memory>";
  private static final String errUsage = "Usage: a(dd) <id> <size> <method>" +
      ", d(isplay), f(ree) <item>, q(uite)";
  private static final String errAddUsage = "Usage: a(dd) <id> <size> " +
      "<method=(f,b,w)>";

  private static ArrayList<MyInt> stack = null;

  public static void main(String[] args) {
    if(args.length != 1) {
      throw new IllegalArgumentException(errCommandLine);
    }
    int memorySize = Integer.parseInt(args[0]);
    stack = new ArrayList<>();
    for(int i = 0; i < memorySize; i++) {
      stack.add(null);
    }

    Scanner scn = new Scanner(System.in);
    Commands command;
    do {
      String[] userCommand = scn.nextLine().split(" ");
      if((command = Commands.commandBy(userCommand[0])) != null) {
        switch(command) {
          case ADD:
            if(userCommand.length != 4) {
              throw new IllegalArgumentException(errUsage);
            }

            if(!add(Integer.parseInt(userCommand[1]),
                Integer.parseInt(userCommand[2]),
                userCommand[3])) {
              System.out.println("Failed to add");
            } else {
              System.out.println("Successful add");
            }
            break;
          case DISPLAY:
            if(userCommand.length != 1) {
              throw new IllegalArgumentException(errUsage);
            }
            display();
            break;
          case FREE:
            if(userCommand.length != 2) {
              throw new IllegalArgumentException(errUsage);
            }
            if(free(Integer.parseInt(userCommand[1]))) {
              System.out.println("free success");
            } else {
              System.out.println("free failed");
            }
            break;
        }
      } else {
        System.err.println(errUsage);
      }
    } while(command != Commands.QUIT);
  }

  private static boolean add(int id, int size, String method) {
    AddCommands command;
    if((command = AddCommands.commandBy(method)) != null) {
      switch(command) {
        case FIRST:
          return firstFit(id, size);
        case BEST:
          return bestFit(id, size);
        case WORST:
          return worstFit(id, size);
        default:
          System.err.println(errAddUsage);
          return false;
      }
    } else {
      System.err.println(errAddUsage);
      return false;
    }
  }

  private static void display() {
    System.out.println("Bytes:");
    for(int i = 0; i < stack.size(); i++) {
      MyInt val = stack.get(i);

      System.out.print(i + 1 + " ");
      if(val == null) {
        System.out.println("Free");
      } else {
        System.out.println(val);
      }
    }
  }

  private static boolean free(int id) {
    if(stack.contains(new MyInt(id))) {
      for(int i = 0; i < stack.size(); i++) {
        MyInt val = stack.get(i);
        if(val != null && val.getInt() == id) {
          stack.set(i, null);
        }
      }
      return true;
    } else {
      return false;
    }
  }

  private static boolean firstFit(int id, int size) {
    int openCount = 0, index = -1;
    boolean breakOut = false;
    for(int i = 0; i < stack.size() && !breakOut; i++) {
      MyInt val = stack.get(i);

      if(val == null) {
        openCount++;
      } else {
        index = -1;
        openCount = 0;
      }

      if(openCount == size) {
        index = i + 1;
        breakOut = true;
      }
    }

    return setStack(index - openCount, id, size);
  }

  private static boolean bestFit(int id, int size) {
    TreeMap<Integer, Integer> fits = new TreeMap<>(new minSort());
    fits.putAll(findHoles(size));

    return fits.size() != 0 &&
        setStack(fits.firstEntry().getValue(), id, size);
  }

  private static boolean worstFit(int id, int size) {
    TreeMap<Integer, Integer> fits = new TreeMap<>(new maxSort());
    fits.putAll(findHoles(size));

    return fits.size() != 0 &&
        setStack(fits.firstEntry().getValue(), id, size);
  }

  private static Map<Integer, Integer> findHoles(int minSize) {
    Map<Integer, Integer> holes = new HashMap<>();

    int openCount = 0;
    int i;
    for(i = 0; i < stack.size(); i++) {
      MyInt val = stack.get(i);
      if(val == null) {
        openCount++;
      } else {
        if(openCount != 0 && openCount >= minSize) {
          holes.put(openCount, i - openCount);
        }
        openCount = 0;
      }
    }

    if(openCount >= minSize) {
      holes.put(openCount, i - openCount);
    }
    return holes;
  }

  private static boolean setStack(int index, int id, int size) {
    if(index < 0 || index + size > stack.size()) {
      return false;
    }

    for(int i = index; i < index + size; i++) {
      stack.set(i, new MyInt(id));
    }
    return true;
  }

  protected enum Commands {
    ADD("a"), DISPLAY("d"), FREE("f"), QUIT("q");

    private String literal;

    Commands(String name) {
      literal = name;
    }

    public static Commands commandBy(String name) {
      switch(name) {
        case "a":
          return ADD;
        case "d":
          return DISPLAY;
        case "f":
          return FREE;
        case "q":
          return QUIT;
        default:
          return null;
      }
    }

    public String getLiteral() {
      return literal;
    }
  }

  protected enum AddCommands {
    FIRST("f"), BEST("b"), WORST("w");

    private String literal;

    AddCommands(String name) {
      literal = name;
    }

    public static AddCommands commandBy(String name) {
      switch(name) {
        case "f":
          return FIRST;
        case "b":
          return BEST;
        case "w":
          return WORST;
        default:
          return null;
      }
    }

    public String getLiteral() {
      return literal;
    }
  }

  protected static class MyInt {

    private int value;

    MyInt(int val) {
      value = val;
    }

    int getInt() {
      return value;
    }

    public String toString() {
      return "Item " + value;
    }

    @Override
    public int hashCode() {
      return Integer.hashCode(value);
    }

    @Override
    public boolean equals(Object obj) {
      if(obj == this) {
        return true;
      }
      if(obj == null || !this.getClass().equals(obj.getClass())) {
        return false;
      }
      MyInt that = (MyInt)obj;
      return this.getInt() == that.getInt();
    }
  }

  static class minSort implements Comparator<Integer> {

    @Override
    public int compare(Integer o1, Integer o2) {
      return o1 - o2;
    }
  }

  static class maxSort implements Comparator<Integer> {

    @Override
    public int compare(Integer o1, Integer o2) {
      return o2 - o1;
    }
  }
}

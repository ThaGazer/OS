/*
 * mmio:MMIO_Reader
 * Created on 4/15/2018
 *
 * Author(s):
 * -Justin Ritter
 */
package mmio;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.Semaphore;

public class MMIO_Reader {

    private static final int SIZEOF_NODE = 16;
    private static final int SIZEOF_SLEEP = 1000;
    private static final int SIZEOF_READERWRITER = 100;

    //mapped memory space
    private Node tree;

    //random number generator
    private Random random = new Random();

    //synchronization
    private int rCount = 0;
    private int wCount = 0;
    private Semaphore rCntLock = new Semaphore(1);
    private Semaphore wCntLock = new Semaphore(1);
    private Semaphore read = new Semaphore(1);
    private Semaphore treeLock = new Semaphore(1);

    private class Node {

        private final String errOB = "index out of bounds";
        private final String errStrOB = "improperly formatted string";

        private MappedByteBuffer tree;

        /**
         * creates a new Node using a mapped byte buffer
         *
         * @param buff mapped byt ebuffer
         */
        Node(MappedByteBuffer buff) {
            setTree(buff);
        }

        /**
         * checks bounds of index to be with in the buffer
         *
         * @param i index to check
         */
        private void boundsCheck(int i) {
            if(i < 0 || i >= getSize()) {
                throw new IllegalArgumentException(errOB);
            }
        }

        /**
         * sets the tree
         */
        private void setTree(MappedByteBuffer buff) {
            tree = (MappedByteBuffer) buff.slice();
        }

        /**
         * returns number of Nodes in the buffer
         *
         * @return number of Nodes
         */
        private int getSize() {
            return tree.capacity() / SIZEOF_NODE;
        }

        /**
         * converts little endian to bif endian
         *
         * @param i integer to convert
         * @return big endian representation of i
         */
        private int little2Big(int i) {
            return Integer.reverseBytes(i);
        }

        /**
         * returns the Location of Node at index
         *
         * @param index index of the Node
         * @return Location of Node in tree
         */
        int getLoc(int index) {
            boundsCheck(index);
            index *= SIZEOF_NODE;
            return little2Big(tree.getInt(index));
        }

        /**
         * returns the Left pointer of Node at index
         *
         * @param index index of Node
         * @return Left pointer of Node
         */
        int getLeft(int index) {
            boundsCheck(index);
            index *= SIZEOF_NODE;
            return little2Big(tree.getInt(index + 8));
        }

        /**
         * returns the Right pointer of Node at index
         *
         * @param index index of Node
         * @return Right pointer of Node
         */
        int getRight(int index) {
            boundsCheck(index);
            index *= SIZEOF_NODE;
            return little2Big(tree.getInt(index + 12));
        }

        /**
         * returns the Value of the Node at index
         *
         * @param index index of Node
         * @return Value of Node
         */
        String getValue(int index) {
            boundsCheck(index);
            index *= SIZEOF_NODE;
            return "" + (char) tree.get(index + 4) + (char) tree.get(index + 5);
        }

        /**
         * changes the Value of a Node at index to str
         *
         * @param index index of Node
         * @param str   string to change Value to
         */
        void setValue(int index, String str) {
            boundsCheck(index);
            if(str.length() != 2) {
                throw new IllegalArgumentException(errStrOB);
            }

            index *= SIZEOF_NODE;
            tree.put(index + 4, (byte) str.charAt(0));
            tree.put(index + 5, (byte) str.charAt(1));
        }

        /**
         * prints the left side of the tree at index
         *
         * @param index index of Node
         * @return the tree represented as a string
         */
        String printLeftTree(int index) {
            return printTree(getLeft(index));
        }

        /**
         * prints the whole tree
         *
         * @return the tree representation as a string
         */
        String printTree() {
            return printTree(0);
        }

        /**
         * prints the tree starting at index. If index is -1 print nothing
         *
         * @param index index of Node to start reading
         * @return the tree representation as a string
         */
        String printTree(int index) {
            if(index != -1) {
                boundsCheck(index);

                StringBuilder buildTree = new StringBuilder();
                for(int i = index; i < getSize(); i++) {
                    buildTree.append(toString(i));
                }
                return buildTree.toString();
            }
            return "";
        }

        /**
         * string representation of a Node at index
         *
         * @param index index of Node
         * @return string representation of aNode
         */
        String toString(int index) {
            boundsCheck(index);

            return "Loc: " + getLoc(index) +
                    " Value: " + getValue(index) +
                    " Left:" + getLeft(index) +
                    " Right:" + getRight(index) + "\n";
        }
    }

    class Reader implements Runnable {

        @Override
        public void run() {
            while(true) {
                int nodeIndex = random.nextInt(tree.getSize());

                try {
                    read.acquire();
                    rCntLock.acquire();
                    rCount++;
                    if(rCount == 1) {
                        treeLock.acquire();
                    }
                    rCntLock.release();
                    read.release();

                    //critical section
                    System.out.println(Thread.currentThread().getName() +
                            " is printing " + nodeIndex + "\n" +
                            tree.printLeftTree(nodeIndex));

                    rCntLock.acquire();
                    rCount--;
                    if(rCount == 0) {
                        treeLock.release();
                    }
                    rCntLock.release();

                    //thread sleeps for SIZEOF_SLEEP
                    Thread.sleep(SIZEOF_SLEEP);
                } catch(InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class Writer implements Runnable {

        @Override
        public void run() {
            while(true) {
                int nodeIndex = random.nextInt(tree.getSize() - 1);
                try {

                    wCntLock.acquire();
                    wCount++;
                    if(wCount == 1) {
                        read.release();
                    }
                    wCntLock.release();

                    //critical section
                    treeLock.acquire();
                    tree.setValue(nodeIndex, randomChar() + randomChar());
                    System.out.println(Thread.currentThread().getName() +
                            " changed node " + nodeIndex + " to " +
                            tree.toString(nodeIndex));
                    treeLock.release();

                    wCntLock.acquire();
                    wCount--;
                    if(wCount == 0) {
                        read.release();
                    }
                    wCntLock.release();

                    //thread sleeps for SIZEOF_SLEEP
                    Thread.sleep(SIZEOF_SLEEP);
                } catch(InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        /**
         * creates a random character from A-Z
         *
         * @return two chars as a string
         */
        private String randomChar() {
            char a = 'A';
            return "" + (char) (a + random.nextInt(26));
        }
    }

    public static void main(String[] args) throws IOException {
        new MMIO_Reader().run(args);
    }

    /**
     * seed for main
     *
     * @param args parameters from user
     * @throws IOException if i/o problems
     */
    private void run(String[] args) throws IOException {

        if(args.length != 1) {
            throw new IllegalArgumentException("Usage: <filename>");
        }

        File fileName = new File(args[0]);
        if(!fileName.isFile()) {
            throw new FileNotFoundException("could not read file");
        }

        //creates a new MappedByteBuffer and stores it in the Node class
        tree = new Node(new RandomAccessFile(fileName, "rw").getChannel().map
                (FileChannel.MapMode.READ_WRITE, 0, fileName.length()));

        //System.out.println(tree.printTree());

        //spawns SIZEOF_READWRITERS number of threads
        ArrayList<Thread> threads = new ArrayList<>();
        for(int i = 0; i < SIZEOF_READERWRITER; i++) {
            Thread read = new Thread(new Reader(), "Reader " + i++);
            Thread write = new Thread(new Writer(), "Writer " + i++);

            read.start();
            write.start();

            threads.add(read);
            threads.add(write);
        }

        //join all threads spawned
        for(Thread t : threads) {
            try {
                t.join();
            } catch(InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

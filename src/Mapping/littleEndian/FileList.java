/*
 * FileList.java
 *
 * reads a file and maps it into memory
 * Date Created: 10/26/2017
 * Author:
 *   -Justin Ritter
 */
package Mapping.littleEndian;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import static java.lang.System.exit;

/**
 * Created by Justin Ritter on 10/26/2017.
 */
public class FileList {
    public static class Node2 {
        private int value;
        private int next;

        public Node2() {
            value = 0;
            next = -1;
        }

        public Node2(int v, int n) {
            value = v;
            next = n;
        }

        public int getValue() {
            return value;
        }

        public int getNext() {
            return next;
        }

        public void setValue(int val) {
            value = val;
        }

        public void setNext(int val) {
            next = val;
        }
    }

    public static void main(String[] args) {
        if(args.length != 1) {
            System.err.println("Usage: <filename>");
            exit(-1);
        }

        File filename = new File(args[0]);

        List<Node2> list = new ArrayList<>();
        MappedByteBuffer fMapped;
        try {
            fMapped = new RandomAccessFile(filename, "r").getChannel().
                    map(FileChannel.MapMode.READ_ONLY, 0, filename.getTotalSpace());

            list.add(new Node2(readInt(fMapped), readInt(fMapped)));
            while(list.get(0).getNext() != -1) {
                list.add(new Node2(readInt(fMapped), readInt(fMapped)));
            }

            for(int i = list.get(0).getNext(); i < list.size();
                i = list.get(i).next) {
                System.out.println(list.get(i).getValue());
            }
        } catch(FileNotFoundException e) {
            System.err.println(e.getMessage());
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    private static int btoi(byte[] b) {
        return ((b[3] & 0xFF) << 24) | ((b[2] & 0xFF) << 16) |
                ((b[1] & 0xFF) << 8) | (b[0] & 0xFF);
    }

    private static int little2big(int i) {
        return (i & 0xff) << 24 | (i & 0xff00) << 8 | (i & 0xff0000) >> 8 | (i >> 24) & 0xff;
    }

    private static int readInt(MappedByteBuffer b) {
        byte[] bytes = new byte[4];
        for(int i = 0; i < 4; i++) {
            bytes[i] = b.get();
        }

        return little2big(btoi(bytes));
    }
}

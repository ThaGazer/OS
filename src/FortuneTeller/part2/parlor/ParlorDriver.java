/*
 * Author: Justin Ritter
 * File: null.java
 * Date: 10/10/2018
 */
package FortuneTeller.part2.parlor;

import java.util.Random;

public class ParlorDriver {

    private static final String errParams = "Usage: <[Semaphore:0|Monitor:1]> <capacity>";
    private static final String errParam1 = "improperly formatted class option";
    private static final String errParam2 = "improperly formatted capacity";

    private static final String msgClosed = "shop is close for the day";
    private static final String msgFortune = "Fortune teller telling ";
    private static final String msgPatronsHandled = "Teller helped: ";
    private static final String msgTookaSeat = " took a seat in the parlor";
    private static final String msgNextStore= " continued on to find a new store";

    private Parlor parlor;

    public static void main(String[] args) {
        new ParlorDriver().go(args);
    }

    public void go(String[] args) {
        if(args.length != 2) {
            throw new IllegalArgumentException(errParams);
        }
        setInterface(args);

        //create a new teller
        new Thread(new Teller()).start();

        //spawn patron thread
        new Thread(()->{
            for(int i = 0; i < Integer.MAX_VALUE; i++) {
                new Thread(new Patron(String.valueOf(i))).start();
                try {
                    Thread.sleep(new Random().nextInt(150));
                } catch(InterruptedException e) {
                }
            }
        }).start();

        try {
            Thread.sleep(new Random().nextInt(100000));
        } catch(InterruptedException e) {
            e.printStackTrace();
        }
        parlor.close();
    }

    private void setInterface(String[] args) {
        try {
            switch(Integer.parseInt(args[0])) {
                case 0:
                    parlor = new ParlorSemaphore(Integer.parseInt(args[1]));
                    break;
                case 1:
                    parlor = new ParlorMonitor(Integer.parseInt(args[1]));
                    break;
                default:
                    throw new IllegalArgumentException(errParam1);
            }
        } catch(NumberFormatException e) {
            System.err.println(errParam2);
            System.exit(1);
        }
    }

    private class Teller implements Runnable {

        private int patronsHandled = 0;

        @Override
        public void run() {
            while(true) {
                //attempts to tell someones fortune
                String patron = parlor.tellFortune();

                /*if tellFortune return a null reference then the shop is closed
                else print the patrons name and try again*/
                if(patron == null) {
                    System.out.println(msgClosed);
                    break;
                } else {
                    System.out.println(msgFortune + patron);
                    patronsHandled++;
                }
                try {
                    Thread.sleep(new Random().nextInt(500));
                } catch(InterruptedException e) {
                }
            }

            //if shop closes print the number of patrons helped
            System.out.println(msgPatronsHandled + patronsHandled);
            System.exit(0);
        }
    }

    private class Patron implements Runnable {

        private String name; /*name of the patron*/

        public Patron(String name) {
            this.name = name;
        }

        @Override
        public void run() {
            //tries to take a seat in the parlor
            if(parlor.newPatron(name)) {
                System.out.println(name + msgTookaSeat);
            } else {
                System.out.println(name + msgNextStore);
            }
        }
    }
}

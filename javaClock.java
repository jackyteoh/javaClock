package javaclock;
/*
Displaying an analog clock that displays the current time (in EDT) with hours, minutes, and seconds.
*/

import java.util.ArrayList;
import java.awt.*;
import java.awt.event.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.swing.*;
import java.io.IOException;
import java.io.PrintStream;
import java.time.*;

public class javaClock {
    static int i;
    static long bigNumber;
    static int timeOfDay, timeH, timeM, timeS;
    static String morn = "AM";
    static String eve = "PM";
//    static ArrayList<Point> al;

    public static void main(String[] args) {
        i = 0;
        JFrame jf = new JFrame("javaClock");
//        al = new ArrayList<Point>();
        jf.setSize(400, 400);
        jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        CirclePanel cp = new CirclePanel();
        jf.add(cp);
        new Thread() {
            public void run() {
                try {
                    SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
                    SSLSocket socket = (SSLSocket) factory.createSocket("nist.time.gov", 443);
                    socket.startHandshake();
                    PrintStream sout = new PrintStream(socket.getOutputStream());
                    Scanner sin = new Scanner(socket.getInputStream());
                    sout.print("GET /actualtime.cgi HTTP/1.0\r\nHOST: nist.time.gov\r\n\r\n");
                    String gotLine = "";
                    while (sin.hasNext()) {
                        gotLine = (sin.nextLine());
//                        System.out.println(gotLine);
                    }
                    bigNumber = (Long.parseLong(gotLine.substring(17, 30)));
//                    System.out.println(bigNumber); // The epoch number thingy

                    Date d = new Date(bigNumber);
//                    System.out.println(d);

                    String timeString = d.toString();
                    System.out.println(timeString);
                    timeOfDay = Integer.parseInt((timeString.substring(11, 13)));
                    timeH = timeOfDay % 12;
                    timeM = Integer.parseInt((timeString.substring(14, 16)));
                    timeS = Integer.parseInt((timeString.substring(17, 19)));
                    System.out.println(timeH + " " + timeM + " " + timeS);

                    // sleep for 1 minute, only want to fetch at most once per min
                    sleep(60000);

                }catch(Exception e){
                    System.out.println("OOpsie: " + e.toString());
                }
            }
        }.start();

        new Thread(){
            public void run() {
                while(true) {
                    if (timeH >= 13) {
                        timeH = 1;
                    }

                    if (timeH == 0) {
                        timeH = 12;
                    }

                    if (timeM >= 59) {
                        timeH += 1;
                        timeM = 0;
                    }

                    if (timeS >= 59) {
                        timeM += 1;
                        timeS = 0;
                    }else {
                        timeS+=1;
                    }

                    cp.repaint();

                    try {
                        sleep(1000); // We want to repaint every second
                    }catch(InterruptedException ie) {
                        System.out.println("InterruptedException: " + ie.toString());
                    }
                }
            }
        }.start();

        jf.setVisible(true);
    }
}

class CirclePanel extends JPanel{

    int hourHandLength, minuteHandLength, secondHandLength;

    CirclePanel(){
        super();
        javaClock.timeH = 0;
        javaClock.timeM = 0;
        javaClock.timeS = 0;
        javaClock.timeOfDay = 0;
        javaClock.morn = "AM";
        javaClock.eve = "PM";
    }
    Point calcLocationForHour(){
        Point p = new Point();
        p.x = (int)(Math.sin(Math.toRadians(javaClock.timeH * 30))* hourHandLength);
        p.y = (int)(Math.cos(Math.toRadians(javaClock.timeH * 30))* hourHandLength);
        return p;
    }
    Point calcLocationForMinute(){
        Point p = new Point();
        p.x = (int)(Math.sin(Math.toRadians(javaClock.timeM * 6))* minuteHandLength);
        p.y = (int)(Math.cos(Math.toRadians(javaClock.timeM * 6))* minuteHandLength);
        return p;
    }
    Point calcLocationForSecond(){
        Point p = new Point();
        p.x = (int)(Math.sin(Math.toRadians(javaClock.timeS * 6))* secondHandLength);
        p.y = (int)(Math.cos(Math.toRadians(javaClock.timeS * 6))* secondHandLength);
        return p;
    }
    protected void paintComponent(Graphics g){
        int height = this.getSize().height;
        int width = this.getSize().width;
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);

        int centerX = width/2;
        int centerY = height/2;

        hourHandLength = height * 1/8;
        minuteHandLength = height * 1/3;
        secondHandLength = height * 1/3;

        String displayTime = "";

        Point hours = calcLocationForHour();
        g.setColor(Color.BLUE); // hour
        g.drawLine(centerX, centerY, centerX + hours.x, centerY - hours.y);
        if(javaClock.timeH < 10) {
            displayTime += "0" + javaClock.timeH + ":";
        }else{
            displayTime += javaClock.timeH + ":";
        }

        Point minutes = calcLocationForMinute();
        g.setColor(Color.BLACK); // Minute
        g.drawLine(centerX, centerY, centerX + minutes.x, centerY - minutes.y);
        if (javaClock.timeM < 10) {
            displayTime += "0" + javaClock.timeM + ":";
        }else {
            displayTime += javaClock.timeM + ":";
        }

        Point seconds = calcLocationForSecond();
        g.setColor(Color.RED); // Second
        g.drawLine(centerX, centerY, centerX + seconds.x, centerY - seconds.y);
        if (javaClock.timeS < 10) {
            displayTime += "0" + javaClock.timeS + " ";
        }else {
            displayTime += javaClock.timeS + " ";
        }

        if (javaClock.timeOfDay >= 12) {
            displayTime += "PM";
        }else{
            displayTime += "AM";
        }

        g.setColor(Color.BLACK);
        g.drawString(displayTime, centerX - 40, centerY - 150);
    }
}

class DrawingPanel extends JPanel {
    DrawingPanel() {
        super();
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int height = this.getSize().height;
        int width = this.getSize().width;
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);
        g.setColor(Color.RED);
        int size = 6;
//        for (Point p: javaClock.al)
//            g.fillOval(p.x-size/2,p.y-size/2,size,size);
//    }
    }
}

class Point{
    int x;
    int y;
    Point(int newx, int newy){x = newx; y = newy;}
    Point(){this(0,0);}
}
package com.geniusgithub.mediarender.proxy;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;


import com.geniusgithub.mediarender.util.CommonLog;
import com.geniusgithub.mediarender.util.CrashHelper;
import com.geniusgithub.mediarender.util.LogFactory;
import com.geniusgithub.mediarender.proxy.IUdpRecvDelegate;

public class UdpProxy{

    private static final CommonLog log = LogFactory.createLog();
    private DatagramSocket socket = null;
    private UdpProxyThread socketRecvThread = null;

    private static final int UDP_PORT = 2000;

    IUdpRecvDelegate recvDelegate = null;

    InetAddress clientAddress = null;
    int clientPort = 0;

    public UdpProxy(IUdpRecvDelegate delegate){

        try {
            if(socket==null){
                socket = new DatagramSocket(null);
                socket.setReuseAddress(true);
                socket.setBroadcast(true);
                socket.bind(new InetSocketAddress(UDP_PORT));
            }
            recvDelegate = delegate;
        }catch (Exception e) {
            e.printStackTrace();
            CrashHelper.postException(e);
            log.e("request time failed:  " + e.toString());
        } finally {
            if (socket != null) {
            //    socket.close();
            }
        }



    }

    private class UdpProxyThread extends Thread {
        private volatile boolean keepAlive = true;

        public UdpProxyThread(String name) {
            super(name);
        }


        @Override
        public void run() {

            byte[] message = new byte[256+2];
            DatagramPacket packet = new DatagramPacket(message, message.length);

            int realDataLen = 0;
            if(null == socket){
                log.i("socket is null");
                return;
            }
            while(keepAlive){
               try {
                   socket.receive(packet);

                    clientAddress = packet.getAddress();
                    clientPort = packet.getPort();


                 //  String text = new String(message, 0, packet.getLength());
                   int cmd = (message[0]<<8 | message[1]&0xff);

                 //  log.i("Received text " + cmd + ", index :" + index + ",len:" +  realDataLen);



                   if(recvDelegate!=null){
                       switch (cmd){
                           case 1:
                               int offset =2;

                               long  index = (long) ( ((message[offset] & 0xFF)<<24)
                                       |((message[offset+1] & 0xFF)<<16)
                                       |((message[offset+2] & 0xFF)<<8)
                                       |(message[offset+3] & 0xFF));


                               realDataLen = packet.getLength() -6;

                               byte[] realData = new byte[realDataLen];
                               System.arraycopy(message, 6, realData, 0, realDataLen);
                               recvDelegate.processUdpRecvData(realData, index);
                               break;

                           case 2:
                               log.i("recv collect log command");
                               recvDelegate.collectLog();
                               break;

                           default:
                               log.e("receive error cmd");
                               break;

                       }

                   }
               }
               catch (Exception e) {
                   log.i("socket error ->"+e.getMessage());
                   e.printStackTrace();
                   CrashHelper.postException(e);
               }
               finally {

               }
            }

        }

        public void joinThread() {
            keepAlive = false;
            while (isAlive()) {
                try {
                    join();
                } catch (InterruptedException e) {
                    // Ignore.
                }
            }
        }
    }


    public boolean stopRecv() {
        log.i("stop udp Recv ");
        //assertTrue(audioThread != null);
        if (socketRecvThread != null) {
            socketRecvThread.joinThread();
            socketRecvThread = null;
            return true;
        } else {
            return false;
        }
    }

    public boolean startRecv() {
        log.i("start udp recv ");
        //assertTrue(audioRecord != null);
        //assertTrue(audioThread == null);
        if (socketRecvThread == null) {

            socketRecvThread = new UdpProxyThread("upd recv thread ");
            socketRecvThread.start();
            return true;
        } else {

            return false;
        }
    }

    public void sendData(int ids[]){
        int dataLen = ids.length*4 + 4;
        byte[] message = new byte[dataLen];



        if(clientAddress==null || clientPort==0){
            return;
        }
        DatagramPacket packet = null;
        try {

            packet = new DatagramPacket(message, message.length, clientAddress, clientPort);
        }catch (Exception e) {
            e.printStackTrace();
        }
        finally {

        }

        int cmd = 2;
        message[0] = (byte)(cmd >>8);
        message[1] = (byte)(cmd);

        int len = ids.length;
        message[2] = (byte)(len >>8);
        message[3] = (byte)(len);



        for (int index =0; index < ids.length; index++) {
            byte[] bytes = intToBytes(ids[index]);
            System.arraycopy(bytes, 0, message, 4+4*index, 4);
        }

        try{
            socket.send(packet);
        }catch (Exception e) {
            e.printStackTrace();
            CrashHelper.postException(e);
        }
        finally {

        }

    }

    public static byte[] intToBytes( int value )
    {
        byte[] src = new byte[4];
        src[0] =  (byte) ((value>>24) & 0xFF);
        src[1] =  (byte) ((value>>16) & 0xFF);
        src[2] =  (byte) ((value>>8) & 0xFF);
        src[3] =  (byte) (value & 0xFF);
        return src;
    }

}

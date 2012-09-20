package com.client;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

import android.util.Log;

public class ClientThread{
		
    protected	int 				CONNECT_TO_PORT =9990;
    protected	Socket				client_socket;
    protected	PrintWriter			out;
    
    protected ClientThread(){}
	protected void startClient(){
		try{
		client_socket = new Socket("10.0.2.2", CONNECT_TO_PORT);                                                                                                            	
    	out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(client_socket.getOutputStream())), true);
        Log.d("DroidCommActivity", "C: Connected to Server");
		}
		catch(Exception e){e.printStackTrace();}
	}
	
	protected void sendData(String tempData){
		out.println(tempData);        
        Log.d("DroidCommActivity", "C: Sent Data: "+tempData);
    
	}
}

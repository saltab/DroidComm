package com.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import android.util.Log;

public class ServerThread{

	protected static 	int					LISTEN_TO_PORT =7770;
    protected static 	ServerSocket 		listen_socket;
    protected static	Socket				sckt;
    protected static	InputStreamReader 	in;
    protected static	BufferedReader    	bufReader;    
    protected static	String 				recvData = "";
    
    protected static void startServer(){
    	try{
    		listen_socket = new ServerSocket(LISTEN_TO_PORT);
    		Log.d("DroidCommActivity","S: Server listening on port "+LISTEN_TO_PORT);
    	}
    	catch(Exception e){
    		e.printStackTrace();
    	}
    }
    
	protected static String pollData() {		
		try {															
			sckt = listen_socket.accept();
			Log.d("DroidCommActibity","S: Client Connection Accepted");			
			in = new InputStreamReader(sckt.getInputStream());
			bufReader = new BufferedReader(in);			
			recvData=bufReader.readLine();			
			sckt.close();		
		} catch (IOException e) {
			e.printStackTrace();
		}
		return recvData;	
	}
	
	protected static void stopServer(){
		try{
		in.close();
		listen_socket.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}		
	}
	
}

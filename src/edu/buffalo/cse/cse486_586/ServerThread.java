package edu.buffalo.cse.cse486_586;

import java.io.BufferedReader;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

import edu.buffalo.cse.cse486_586.DroidGrupChatActivity.MessageWrapper;

import android.util.Log;
/*
 * Purpose of this class: To provide supporting methods to Server Thread
 * #startServer(): goes by the name
 * #pollData(): polls for incoming data
 * # stopServer()
 */
public class ServerThread{

    protected static 	ServerSocket 		listenSocket;
    protected static	Socket				sckt;
    protected static	ObjectInputStream 	in;
    protected static	BufferedReader    	bufReader;    
    protected static	MessageWrapper 		recvData;
    
    protected static void startServer(){
    	try{
    		listenSocket = new ServerSocket(GlobalData.serverPort);
    		Log.d("DroidChatActivity","Server: Server listening on port "+GlobalData.serverPort);
    	}
    	catch(Exception e){
    		e.printStackTrace();
    	}
    }
    
	protected static MessageWrapper pollData() {		
		try {															
			sckt = listenSocket.accept();
			Log.d("DroidChatActivity","Server-Client: Client Connection Accepted");			
			in = new ObjectInputStream(sckt.getInputStream());			
			recvData=(MessageWrapper) in.readObject();						
			sckt.close();		
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return recvData;	
	}
	
	protected static void stopServer(){
		try{
		in.close();
		listenSocket.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}		
	}
	
	protected static void insertToMessageProvider(){
		
	}
}

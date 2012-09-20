package com.client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

@SuppressWarnings("unused")
public class DroidCommActivity extends Activity {
	
	private 	EditText 	textIn;
	private 	EditText 	textPort;
    private 	Button 		sendData;
    private 	Button 		sendExit;
	private		TextView	textOut;

    // Variables for ClientThread
    protected 	String 		tempData = "", port="";
    protected 	int 		CONNECT_TO_PORT;
    protected	Socket		client_socket;    
    
    // Variables used by ServerThread
    protected	String		recvData = "";
    protected	String		newLine = System.getProperty("line.separator");
    private 	Handler 	handler = new Handler();        
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        // XML Tags used for UI
        textIn   = (EditText) findViewById(R.id.textInput);
        textPort = (EditText) findViewById(R.id.textPort);
        textOut  = (TextView) findViewById(R.id.textOutput);
        sendData = (Button)   findViewById(R.id.buttonSend);
        sendExit = (Button)   findViewById(R.id.buttonExit);
        //Call the ServerThread Method
        ServerThread();              
        //Wait for User Response                
        sendData.setOnClickListener(inputListener);
        sendExit.setOnClickListener(exitListener);
    }
	
	private OnClickListener inputListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
        	try{
        	port = textPort.getText().toString();
        	        	
        	if(port.isEmpty()){
        		Context context = getApplicationContext();
        		CharSequence text = "Please Enter Port Number";
        		int duration = Toast.LENGTH_SHORT;
        		Toast toast = Toast.makeText(context, text, duration);
        		toast.show();
        	}else{
        		CONNECT_TO_PORT = Integer.parseInt(textPort.getText().toString());
        		tempData = textIn.getText().toString();
        		ClientThread();
        	}
        	}
        	catch(Exception e){
        		
        		e.printStackTrace();
        	}        		
        }
    };
    
    private OnClickListener exitListener = new OnClickListener() {
        @Override
        public void onClick(View v) {   
        	//ServerThread.stopServer();
        	Log.d("DroidCommActivity", "S: Server Stopped");
        	System.exit(0);
        }
    };
   
    protected void ClientThread() {  
        Runnable runnable = new Runnable(){
        	public void run(){    	
        		try {
        			// Establish Connection With Server
        			client_socket = new Socket("10.0.2.2", CONNECT_TO_PORT);
        			if(client_socket.isConnected())
        			Log.d("DroidCommActivity", "C: Connection Establised");
        			PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(client_socket.getOutputStream())), true);
        			//Send Data To Server
        			out.println(tempData);
        			Log.d("DroidCommActivity", "C: Sent Data: "+tempData);
        			//Close The Connection
        			client_socket.close();                
        		}	
        		catch(RuntimeException i){
        			Context context = getApplicationContext();
            		CharSequence text = "Problem Connecting to Server";
            		int duration = Toast.LENGTH_SHORT;
            		Toast toast = Toast.makeText(context, text, duration);
            		toast.show();
        		}
        		catch (Exception e) {                           
        			e.printStackTrace();
        		}
        	}
    	};
    	//Start The ClientThread
    	new Thread(runnable).start();    
		}
    
    protected void ServerThread(){
    	Runnable runServ = new Runnable(){    		
			@Override
			public void run() {
				// TODO Auto-generated method stub
				//Start The Server
				ServerThread.startServer();
				while(true){
					// Poll For Incoming Data
					recvData=ServerThread.pollData();
					// Push Received Data to UI Thread
					handler.post(new Runnable(){
						@Override
						public void run(){
							//append data to TextView
							textOut.append(recvData);
							textOut.append(newLine);
							textIn.setText("");
						}});}
			}    			    		
    	};
    	//Start The ServerThread
    	new Thread(runServ).start();
    }
}
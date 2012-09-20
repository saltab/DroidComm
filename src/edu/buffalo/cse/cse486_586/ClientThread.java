package edu.buffalo.cse.cse486_586;

import java.io.ObjectOutputStream;
import java.net.Socket;

import edu.buffalo.cse.cse486_586.DroidGrupChatActivity.MessageWrapper;

import android.util.Log;

/*
 * Purpose of this Class: To provide supporting methods to Client Thread
 * Methods here:
 * # sendToSequencer(): Send the data to sequencer
 * # B_Multicast(): multicast data to all
 * 
 *  
 */

public class ClientThread {
	
	private static 	Socket				sendToSeqSocket;
	private	static	Socket				multicastSocket;
	private	static 	ObjectOutputStream 	out;
	private	static 	ObjectOutputStream 	out2;
	
	protected static void sendToSequencer(MessageWrapper message){
		try {
			sendToSeqSocket = new Socket("10.0.2.2",GlobalData.seqSendPort);
			if(sendToSeqSocket.isConnected())
    			Log.d("DroidChatActivity", "Client-Seq: Connection Establised with Sequencer");
    			out = new ObjectOutputStream(sendToSeqSocket.getOutputStream());
    			//Send Data To Sequencer
    			out.writeObject(message);
    			Log.d("DroidChatActivity", "Client-Seq: Sent Data to Sequencer: "+message);
    			sendToSeqSocket.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	
	
	protected static void B_Multicast(MessageWrapper message){
		try{
			for(int port : GlobalData.portArray){
				multicastSocket = new Socket("10.0.2.2",port);
				if(multicastSocket.isConnected())
	    			Log.d("DroidChatActivity", "Client-Server: Connection Establised with Emulator:"+(port/2));
				out2 = new ObjectOutputStream(multicastSocket.getOutputStream());
    			//Send Data To All peers
    			out2.writeObject(message);
    			Log.d("DroidChatActivity", "Client-Server: Sent Data"+message.data+" to Emulator: "+(port/2));
	    		multicastSocket.close();
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}	
	
}

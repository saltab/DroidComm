package edu.buffalo.cse.cse486_586;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import edu.buffalo.cse.cse486_586.DroidGrupChatActivity.MessageWrapper;
import android.util.Log;
/*
 * Purpose of this class: To provide supporting methods to sequencer thread
 * Following are the methods in this class:
 * # startSequencer(): Starts the sequencer thread on port 15000 and redirection is expected from port 16000
 * # recvData(): Polls for incoming data
 * # replyAck(): Replies back to client/peer which sent messageType= "REQ"
 * # orderMessages(): Runs the total-causal algorithm
 * # stopSequencer(): Stops the sequencer, though I have not used it 
 */

public class SequencerThread {
	// variables required by SequencerThread
	private		static 	ServerSocket		seqListenSocket;
	private		static 	Socket				seqSendSocket;
	private		static	Socket				seqRecvSocket;
    private 	static	ObjectInputStream 	in;
    private 	static	ObjectOutputStream 	out;
   
    protected 	static	MessageWrapper		recvMessage;    
    protected	static	List<Integer>[] 	perProcessQ ;
    protected	static	Integer[] 			backupArr ;
    protected	static	int					processID;
    protected	static	int					clientPort;
    
    
    @SuppressWarnings("unchecked")
	protected static void startSequencer(){
    	try{
    		seqListenSocket = new ServerSocket(GlobalData.seqListenPort);
    		if(seqListenSocket.isBound()){
    			Log.d("DroidChatActivity","Seq: Sequencer listening on port "+GlobalData.seqListenPort);
    			
    			perProcessQ = (LinkedList<Integer>[]) new LinkedList[5];    			
    			for(int count = 0  ; count < GlobalData.portArray.length; count ++)	//Define the queue
    			{
    				perProcessQ[count] = new LinkedList<Integer>();
    				//perProcessQ[count].add(0);
    			}
    			backupArr = new Integer[5];
    			for(int count = 0  ; count < GlobalData.portArray.length; count ++)	//Initialize the Array
    				backupArr[count] = 0;
    				
    			//backupArr = perProcessQ.clone();
    		}
    	}
    	catch(Exception e){
    		e.printStackTrace();
    	}
    }
	
	protected static MessageWrapper recvData() {		
		try {						
			seqRecvSocket = seqListenSocket.accept();
			Log.d("DroidChatActivity","Seq-Client: Client Connection Accepted");			
			in = new ObjectInputStream(seqRecvSocket.getInputStream());			
			recvMessage=(MessageWrapper) in.readObject();			
			seqRecvSocket.close();		
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return recvMessage;
	}

	protected static synchronized void replyAck(MessageWrapper ackMessage){
		try{
			seqSendSocket = new Socket("10.0.2.2", recvMessage.devID*2);
			if(seqSendSocket.isConnected()){    			
				out = new ObjectOutputStream(seqSendSocket.getOutputStream());    			
    			out.writeObject(ackMessage);
    			Log.d("DroidChatActivity", "Seq-Client: Sent Seq Number To client ");
        		seqSendSocket.close();
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	protected static void stopSequencer(){
		try{
		in.close();
		seqListenSocket.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}		
	}
	
	protected static synchronized void orderMessages(MessageWrapper message){
		
		switch(message.devID){
			case 5554:
				processID = 0; break;
				
			case 5556:
				processID = 1; break;
				
			case 5558:
				processID = 2; break;
			
			case 5560:
				processID = 3; break;
			
			case 5562:
				processID = 4; break;
		}
		//holdBack = true; think about this condition
		//perProcessQ[processID].add(message.messageID);
		
		perProcessQ[processID].add(backupArr[processID]);
		
		while(!perProcessQ[processID].isEmpty()){
			Collections.sort(perProcessQ[processID]);
			
			if((backupArr[processID]+1)==message.messageID){
				//if(!perProcessQ[processID].isEmpty())
					//perProcessQ[processID].add(message.messageID);
				backupArr[processID]++;
				perProcessQ[processID].remove(0);				
				GlobalData.SequenceNo++;
				
				MessageWrapper ackMessage = new MessageWrapper();
    			ackMessage.messageType = "ACK";
    			ackMessage.devID = message.devID;
    			ackMessage.messageID = backupArr[processID];
    			ackMessage.seqNo = GlobalData.SequenceNo;
    			ackMessage.data = null;
    			
    			if(message.isTest2True == true && message.count < 6){
    				ackMessage.isTest2True = message.isTest2True;
    				ackMessage.count = message.count;
    			}
    			SequencerThread.replyAck(ackMessage);
			}
			else{								
				perProcessQ[processID].add(message.messageID);
				break;
			}
		}
		
	}

}

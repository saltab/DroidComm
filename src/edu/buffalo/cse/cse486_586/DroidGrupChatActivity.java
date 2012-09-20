package edu.buffalo.cse.cse486_586;

import java.io.Serializable;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import edu.buffalo.cse.cse486_586.provider.MessageProvider;

/* ©Saurabh Talbar
 * PROJECT NAME : GROUP MESSENGER FOR ANDROID
 * PROJECT TEAM : PENTADROID
 * COURSE		: DISTRIBUTED SYSTEMS - CSE 586
 * VERSION		: 0.1
 * ------------------------------------------------
 * 
 * Purpose of this Activity: To start the main UI thread and manage all other threads(server, client and sequencer)
 * MessageWrapper is the base class for all the different message formats I require
 * Method in this class: 
 *  # insertCV(): inserting content values into the SQLite Database using content resolver
 * 
 * Threads in this class:
 * 
 * UI Thread ----> kicks ---> ServerThread (onCreate()) ---> Listens for incoming data (of type "ACK" or "DATA")
 *    |   |
 *    |   \---> kicks ClientThread(if user hits TEST1, TEST2, DISPLAY or EXIT button) --> Sends data (of type "REQ" or "DATA") on demand
 *    V
 *  kicks
 *    |
 *    |
 *    V
 * if(emulator== 5554)
 *  Sequencer Thread;  --> orders the messages with messageType = "REQ" and replies with type = "ACK" (with a global seqNo)
 *    
 * 
 *  
 * 
 */

public class DroidGrupChatActivity extends Activity {

	// variables used by XML layout
	private Button sendExit;
	private Button test1;
	private Button test2;
	private Button display;
	private TextView textOut;

	// Variables for ClientThread	
	private String portStr;
	private int localPort;
	private int messageStart;

	// Variables used by ServerThread
	private MessageWrapper recvData;
	private int prevProc;
	private int currProc;
	private String newLine = System.getProperty("line.separator");
	private Handler handler = new Handler();

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// extract the local emulator's id/port
		TelephonyManager tel = (TelephonyManager) this
				.getSystemService(Context.TELEPHONY_SERVICE);
		portStr = tel.getLine1Number().substring(
				tel.getLine1Number().length() - 4);
		
		localPort = Integer.parseInt(portStr); // e.g local port = 5554...5562
		
		switch(localPort){
		case 5554:
			currProc = 1; break;
			
		case 5556:
			currProc = 2; break;
			
		case 5558:
			currProc = 3; break;
		
		case 5560:
			currProc = 4; break;
		
		case 5562:
			currProc = 5; break;
		}
		
		// Initialize local message number for this process
		messageStart = 0;

		
		// textIn = (EditText) findViewById(R.id.textInput);
		
		// Text Output Tag
		textOut = (TextView) findViewById(R.id.textOutput);
		
		
		// Button tags
		test1 = (Button) findViewById(R.id.buttonSend);		
		test2 = (Button) findViewById(R.id.buttonTest2);
		sendExit = (Button) findViewById(R.id.buttonExit);
		display = (Button) findViewById(R.id.buttonDisp);

		// Call the ServerThread Method
		ServerThread();

		// Start the Sequencer Thread on port: 15000	
		if (portStr.contentEquals("5554"))
			SequencerThread();

		// Wait for User Response
		test1.setOnClickListener(test1Case);
		test2.setOnClickListener(test2Case);
		display.setOnClickListener(displayData);
		sendExit.setOnClickListener(exitListener);
	}

	private OnClickListener test1Case = new OnClickListener() {
		@Override
		public void onClick(View v) {
			try {
				
				// Loop for TEST-CASE-1
				for (int count = 0; count < 5; count++)
					ClientThread("REQ", null,1);			// Invoke the ClientThread() which is a runnable method
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};

	private OnClickListener test2Case = new OnClickListener() {
		@Override
		public void onClick(View v) {
			try {
				ClientThread("REQ", null,2);				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};

	
	private OnClickListener displayData = new OnClickListener() {
		@Override
		public void onClick(View v) {
			try {
				Uri providerUri = Uri.parse("content://"
						+ MessageProvider.AUTHORITY + "/MessageT");

				Cursor cur = getContentResolver().query(providerUri, null,
						null, null, Message.provider_key + " ASC");

				if (cur.getCount() > 0) {
					StringBuffer buff = new StringBuffer();
					cur.moveToFirst();
					int msgID = cur.getColumnIndex(Message.MSG_ID);
					int keyColumn = cur.getColumnIndex(Message.provider_key);
					int valueColumn = cur
							.getColumnIndex(Message.provider_value);
					buff.append("RowID         " + Message.provider_key
							+ "         " + Message.provider_value + "\n");
					boolean flag = true;
					int iterator = 0, key;

					while (!cur.isLast()) {
						key = cur.getInt(keyColumn);
						if ((iterator + 1) != key) {
							flag = false;
							break;
						}
						buff.append(cur.getInt(msgID) + "         "
								+ cur.getString(keyColumn) + "         "
								+ cur.getString(valueColumn) + "\n");
						cur.moveToNext();
						iterator++;
					}
					if (flag)
						buff.append(cur.getInt(msgID) + "         "
								+ cur.getString(keyColumn) + "         "
								+ cur.getString(valueColumn) + "\n");

					Log.d("Display buffer size",
							Integer.toString(buff.length()));
					textOut.setText(buff.toString());

				} else {
					Log.d("Activity: Display Button", "Empty");
					textOut.setText("Empty Table");
				}
				cur.close();
			}

			catch (Exception e) {
				e.printStackTrace();
			}
		}
	};
	private OnClickListener exitListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			// ServerThread.stopServer();
			Log.d("DroidChatActivity", "S: Server Stopped");
			System.exit(0);
		}
	};

	static class MessageWrapper implements Serializable {
		/**
		 * MESSAGE FORMATS USED FOR MY GROUP MESSENGER
		 */
		private static final long serialVersionUID = 1L;
		String messageType; 					// messageType = REQ / ACK / DATA / TEST-CASE-2
		int devID; 								// devID = 5554 / 5556 / 5558 / 5560 / 5562
		int messageID; 							// messageID = 1...n or -1 for NOT_USED
		int seqNo; 								// sequence number for this message ; -1 for REQ
		String data; 							// data = null - (Type = REQ) ; user input - (Type = DATA); null - (Type = ACK)
		//-----------
		int count;								// added for test-case-2
		boolean isTest2True;					// added for test-case-2
		
	}

	
	// Definition of the insertCV method
	public void insertCV(int key, String message) {
		try {
			
			// generate appropriate Uri
			Uri uri = Uri.parse("content://" + MessageProvider.AUTHORITY
					+ "/MessageT");
			if (uri == null)
				throw new IllegalArgumentException();
			ContentValues valuesCV = new ContentValues();
			valuesCV.put(Message.provider_key, key);
			valuesCV.put(Message.provider_value, message);

			// insert into ContentProvider i.e MessageProvider for my app
			getContentResolver().insert(uri, valuesCV);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Definition of the Client Thread
	protected void ClientThread(final String messageType,
			final MessageWrapper _message, final int _case) {
		Runnable runnable = new Runnable() {
			public void run() {
				try {

					MessageWrapper message = new MessageWrapper();

					if (messageType.contentEquals("REQ")) {
						// Generate the message of type - REQ
						message.messageType = "REQ";
						message.devID = localPort;
						message.messageID = messageStart + 1;
						message.seqNo = -1;
						message.data = null;
						
						
						// set variables for TEST CASE 2
						if(_case == 2){
							message.count = 0;
							message.isTest2True = true;
						}
						else if(_case == 1){
							message.count = -1;
							message.isTest2True = false;
						}

						// send the data to sequencer
						ClientThread.sendToSequencer(message);
						// increase the message count
						messageStart = messageStart + 1;
					}
					if (messageType.contentEquals("DATA")) {
						message.messageType = "DATA";
						message.devID = _message.devID;
						message.messageID = messageStart;
						message.seqNo = _message.seqNo;
						message.data = null;
						if(_message.isTest2True == true && _message.count < 4){
		    				message.isTest2True = _message.isTest2True;
		    				message.count = _message.count;						
						}
						ClientThread.B_Multicast(message);
						Log.d("Multicast - DATA",""+message.devID+":"+message.count);
					}

				} catch (RuntimeException i) {
					Context context = getApplicationContext();
					CharSequence text = "Problem Connecting to Server";
					int duration = Toast.LENGTH_SHORT;
					Toast toast = Toast.makeText(context, text, duration);
					toast.show();
				}
			}
		};
		// Start The ClientThread
		new Thread(runnable).start();		
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	// Definition of the Server Thread
	protected void ServerThread() {
		Runnable runServ = new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				// Start The Server
				ServerThread.startServer();
				while (true) {
					
					// Poll For Incoming Data
					recvData = ServerThread.pollData();
					Log.d("Server - POLL",""+recvData.devID+":"+recvData.count);
					if (recvData.messageType.contentEquals("ACK")) {
						if(recvData.isTest2True == true && recvData.count < 6)
							ClientThread("DATA", recvData,0);
						else
							ClientThread("DATA", recvData,1);
					}
					if (recvData.messageType.contentEquals("DATA")) {

						insertCV(recvData.seqNo, "" + recvData.devID + ":"
								+ recvData.messageID);
						Log.d("DroidChatActivity: INSERT To CV", "" + recvData.devID
								+ " , " + recvData.seqNo);
						
						if(recvData.isTest2True == true){
							Log.d("Server-O/P : TEST-2",""+recvData.devID+":"+recvData.count);
						switch(recvData.devID){
							case 5554:
								prevProc = 1; break;
								
							case 5556:
								prevProc = 2; break;
								
							case 5558:
								prevProc = 3; break;
							
							case 5560:
								prevProc = 4; break;
							
							case 5562:
								prevProc = 5; break;
							}
						if (recvData.count < 6){							
							handler.post(new Runnable() {						  
								 @Override 
								 public void run() { // append data to
								 textOut.append(newLine);
								 textOut.append("" +recvData.devID + ":" + recvData.count);
								  } });
						}
						if((recvData.count <= 5 && (prevProc == ((currProc-1)%5))) || (prevProc == 5 && currProc == 1 )){
							recvData.devID = localPort;							
							recvData.count++;
							ClientThread("DATA", recvData,0);
						}																			
						else if(recvData.count>6)
							recvData.isTest2True = false;													
						}
					}
				}
			}
		};
		// Start The ServerThread
		new Thread(runServ).start();
	}

	// Definition of the Sequencer Thread
	
	protected void SequencerThread() {
		Runnable sequencer = new Runnable() {
			public void run() {
				try {
					MessageWrapper recvMessage = new MessageWrapper();
					SequencerThread.startSequencer();
					while (true) {
						// Poll For Incoming Data
						recvMessage = SequencerThread.recvData();
						// Run the total-causal algorithm
						if (recvMessage.messageType.contentEquals("REQ"))
							SequencerThread.orderMessages(recvMessage);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		new Thread(sequencer).start();
	}
}
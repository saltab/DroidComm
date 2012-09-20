package edu.buffalo.cse.cse486_586;
/*
 * This is the class of data that can be accessed by all, though, sequence No is accessible to Sequencer Thread
 * 
 */
public class GlobalData {	    
    
	protected	static 	final 	int[] 	portArray 		= new int[] {11108, 11112, 11116, 11120, 11124};
    protected	static	final 	int 	serverPort  	= 10000;	// Data sent to portArray ==> 10000 
    protected	static	final	int		seqListenPort	= 15000;	// Sequencer listens at this port
    protected	static	final	int		seqSendPort		= 16000;	// Data Sent to 16000 ==> 15000
    protected	static	boolean			flag			= false;
    protected	static	int				SequenceNo		= 0;		//global sequence number
    
}

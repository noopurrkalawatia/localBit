/*
 * Class name	:	PeerHandler.java
 * Description	:	This class defines the peer handler for the peer in the protocol
 * Institution	:	University of Florida
 */
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.logging.Level;


public class PeerHandler extends Thread {

	final ObjectInputStream peerObjectInStream;
	final ObjectOutputStream peerObjectOutStream;
	final Socket peerSocket;
	int peerID;
	boolean flag=false;
	PeerParameters peerParameters;
	private MessageHandler messageHandler = null;
	private volatile static boolean exitPeerHandler = false;
	
	/*
	 * Function name : PeerHandler()
	 * Parameters    : socket, ObjectInputStream, ObjectOutputStream, peerID
	 * Description   : Constructor
	 */
	PeerHandler(Socket socket, ObjectInputStream obInStream, ObjectOutputStream obOutStream,int peerID)
	{
		this.peerObjectInStream = obInStream;
		this.peerObjectOutStream = obOutStream;
		this.peerSocket = socket;
		this.peerID = peerID;
		this.peerParameters  = CommonConfigurations.peerConfigMap.get(peerID);
	}

	/*
	 * Function name : PeerHandler()
	 * Parameters    : none
	 * Description   : Constructor
	 */
	PeerHandler(Socket socket, ObjectInputStream obInStream,ObjectOutputStream obOutStream)
	{
		this.peerObjectInStream = obInStream;
		this.peerObjectOutStream = obOutStream;
		this.peerSocket = socket;
		this.peerID = 0;
		this.peerParameters  = null;
	}

	
	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	public void run()
	{
		if(this.peerParameters != null)
		{
			peerParameters.peerHandler = this;
		}

		messageHandler = new MessageHandler(this);
		messageHandler.setName("Message Handler Thread");
		messageHandler.start();	
		try {
			while(!exitPeerHandler)
			{
				Object priMessage = null;
				
					priMessage = peerObjectInStream.readObject();
				
				
				if(priMessage instanceof MessageType)
				{
					handleHandshakeMessage(priMessage);
					sendBitfieldMessage();
				}

				else
				{
					if(priMessage != null)
					{
						messageHandler.messagesQueue.add((ActualMessage)priMessage);
						if(messageHandler.getState().equals(Thread.State.WAITING)) {
							synchronized (messageHandler) {
								messageHandler.notify();
							}	
						}
					}
					else
					{
						System.out.println("Actual Message type not received");
					}
				}
			}
		}
		catch(SocketException s) 
		{
			//s.printStackTrace();
		}

		catch (IOException | ClassNotFoundException e)
		{
			//e.printStackTrace();
		}

		finally
		{
			try
			{
				this.peerObjectInStream.close();
				this.peerObjectOutStream.close();
			}
			catch(SocketException ex)
			{
				LoggingUtil.getLOGGER().info("Peer " + "[" + peerID + "]" + " is no longer available" );
				//System.out.println("Socket connection is lost for this is during closing the object stream " + this.peerID);
			}
			catch(IOException e)
			{
				LoggingUtil.getLOGGER().info("Peer " + "[" + peerID + "]" + " is no longer available" );
				//System.out.println("Socket connection is lost for this is during closing the object stream " + this.peerID);
			}

		}
	}

	/*
	 * Function name : sendBitfieldMessage()
	 * Parameters    : none
	 * Description   : This method is responsible for sending the bitfield message to the peer.
	 */
	private void sendBitfieldMessage() throws IOException 
	{
		if(!CommonConfigurations.peerConfigMap.get(CommonConfigurations.currentPeerID).bitfield.isEmpty())
		{
			PeerParameters peerPara = CommonConfigurations.peerConfigMap.get(CommonConfigurations.currentPeerID);
			byte[] payload = peerPara.bitfield.toByteArray();

			ActualMessage actualMessage = new ActualMessage(message_Type.BITFIELD,payload);
			writeMessage(actualMessage);
		}
	}

	/*
	 * Function name : handleHandshakeMessage()
	 * Parameters    : none
	 * Description   : This method is responsible for handling the handshake message to the peer.
	 */
	private void handleHandshakeMessage(Object priMessage) throws IOException 
	{
		MessageType message = (MessageType) priMessage;
		if(this.peerID == 0)
		{
			this.peerID = message.PeerID;
			this.peerParameters  = CommonConfigurations.peerConfigMap.get(message.PeerID);
			this.peerParameters.peerHandler = this;
		}
		LoggingUtil.getLOGGER().log(Level.INFO,"Peer " + CommonConfigurations.currentPeerID + " makes connection to Peer " + this.peerID );
		
		if(message.PeerID == peerID)
		{
			if(message.getMessageHeader().equals("P2PFILESHARINGPROJ"))
			{
				if(CommonConfigurations.peerConfigMap.get(CommonConfigurations.currentPeerID).stateOfPeer == peerState.HANDSHAKE)
				{
					MessageType toSendMsg = HandleMessage(CommonConfigurations.currentPeerID);
					this.writeMessage(toSendMsg);
					CommonConfigurations.peerConfigMap.get(CommonConfigurations.currentPeerID).stateOfPeer = peerState.BITFIELD;
				}
			}
			else
			{
				System.out.println("Invalid message received");
			}
		}

	}


	/*
	 * Function name : HandleMessage()
	 * Parameters    : peerID
	 * Description   : The message is responsible for handling the present message in the hashmap
	 * 				   and forming the appropriate reply
	 * return type   : MessageType
	 */
	private MessageType HandleMessage(int peerID)
	{
		MessageType msg = new MessageType(peerID);
		msg.setMessageHeader("P2PFILESHARINGPROJ");
		return msg;
	}

	/*
	 * Function name : writeMessage()
	 * Parameters    : messageToSend
	 * Description   : The message is responsible for writing the message to the socket
	 * return type   : void
	 */
	synchronized public void writeMessage(ActualMessage messageToSend)
	{
		try {
			if(this != null)
			{
				this.peerObjectOutStream.writeObject(messageToSend);
				this.peerObjectOutStream.flush();
			}
		}
		catch(SocketException ex)
		{
			//System.out.println("This user is disconnected " + this.peerID);
		}
		catch (IOException e) 
		{
			//System.out.println("This user is disconnected " + this.peerID);
		}
	}

	/*
	 * Function name : writeMessage()
	 * Parameters    : messageToSend : MessageType
	 * Description   : The message is responsible for writing the message to the socket
	 * return type   : void
	 */
	synchronized public void writeMessage(MessageType messageToSend)
	{
		try {
			if(this != null)
			{
				this.peerObjectOutStream.writeObject(messageToSend);
				this.peerObjectOutStream.flush();
			}
		}catch(SocketException s) 
		{
			//System.out.println("This user is disconnected : " + this.peerID);
		} 
		catch (IOException e) 
		{
			//System.out.println("This user is disconnected : " + this.peerID);
		}
	}

	/*
	 * Function name : stopThread()
	 * Parameters    : none
	 * Description   : The message is responsible for terminating the thread
	 * return type   : void
	 */
	public void stopThread()
	{
		//System.out.println("I am stoping the peer handler");
		exitPeerHandler = true;
	}
}

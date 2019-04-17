/*
 * Class name	:	MessageHandler.java
 * Description	:	This class defines the entire logic to handle the type of messages
 * Institution	:	University of Florida
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class MessageHandler extends Thread
{
	private PeerHandler peerHandler = null;
	ConcurrentLinkedQueue<ActualMessage> messagesQueue = new ConcurrentLinkedQueue<ActualMessage>();
	private volatile boolean exitMsgHandler = false;

	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	public void run()
	{
		try
		{
			while(!exitMsgHandler)
			{
				if(!messagesQueue.isEmpty())
				{
					ActualMessage receivedMessage = messagesQueue.poll();

					switch(receivedMessage.type)
					{
					case CHOKE:
						LoggingUtil.getLOGGER().info("Peer " + "[" + CommonConfigurations.currentPeerID + "]" + " is choked by " + "[" + peerHandler.peerID + "]" );
						CommonConfigurations.peerConfigMap.get(CommonConfigurations.currentPeerID).stateOfPeer = peerState.CHOKED;
						break;

					case UNCHOKE:
						LoggingUtil.getLOGGER().log(Level.INFO,"Peer " +  "[" + CommonConfigurations.currentPeerID + "]" + " is unchoked by " + "[" + peerHandler.peerID + "]");
						CommonConfigurations.peerConfigMap.get(CommonConfigurations.currentPeerID).stateOfPeer = peerState.UNCHOKED;
						sendRequestMessage();
						break; 

					case INTERESTED:
						LoggingUtil.getLOGGER().log(Level.INFO,"Peer [" + CommonConfigurations.currentPeerID + "] received the 'interested' message from [" + peerHandler.peerID + "]");
						handleInterestedMessage(receivedMessage);
						break;

					case UNINTERESTED:
						LoggingUtil.getLOGGER().log(Level.INFO,"Peer [" + CommonConfigurations.currentPeerID + "] received the 'not interested' message from [" + peerHandler.peerID + "]");
						handleUninterestedMessage();
						break;

					case HAVE:
						handleHaveMessage(receivedMessage);
						break;

					case BITFIELD:
						handleBitfieldMessage(receivedMessage);
						break;

					case REQUEST:
						handleRequestMessage(receivedMessage);
						break;

					case PIECE:
						handlePieceMessage(receivedMessage);
						break;
					}
				}
				else 
				{
					synchronized (this)
					{
						try 
						{
							wait();
						}
						catch(InterruptedException ie)
						{

						}
					}
				}
			}
		}
		catch(SocketException ex)
		{
			ex.printStackTrace();
			System.out.println("Exception in run(), Message Handler : SocketException");
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			System.out.println("Exception in run(), Message Handler : Exception");
		}
	}

	/*
	 * Function name : handleUninterestedMessage()
	 * Parameters    : none.
	 * Description   : The method handles the uninterested message received from the peers.
	 * Return type   : void
	 */
	private void handleUninterestedMessage() 
	{
		//System.out.println("I have received the un - interested message from " + peerHandler.peerID);
		if(CommonConfigurations.interestedPeers.contains(peerHandler.peerID))
		{
			CommonConfigurations.interestedPeers.remove(peerHandler.peerID);
		}
	}

	/*
	 * Function name : handleHaveMessage()
	 * Parameters    : receivedMessage - The actual message received from the remote peer.
	 * Description   : The method handles the Have message received from the peers.
	 * Return type   : void
	 */
	private void handleHaveMessage(ActualMessage receivedMessage) 
	{
		byte[] payload = receivedMessage.messagePayload;
		ByteArrayInputStream bInput = new ByteArrayInputStream(payload);
		bInput.read(payload,0,4);
		int index = ByteBuffer.wrap(payload).getInt();
		LoggingUtil.getLOGGER().log(Level.INFO,"Peer " + "[" + CommonConfigurations.currentPeerID + "]" +
				" received the 'have' message from [" + peerHandler.peerID + "]" + " for the piece " + index);

		setIndexOfBitfield(peerHandler.peerID,index);

		PeerParameters remotePeer = CommonConfigurations.peerConfigMap.get(peerHandler.peerID);

		BitSet currBitset = new BitSet(CommonConfigurations.noOfSplitParts);
		currBitset = (BitSet) CommonConfigurations.peerConfigMap.get(CommonConfigurations.currentPeerID).bitfield.clone();

		BitSet peerBitset = new BitSet(CommonConfigurations.noOfSplitParts);
		peerBitset = (BitSet) CommonConfigurations.peerBitField.get(remotePeer.peerID).clone();		

		currBitset.flip(0,CommonConfigurations.noOfSplitParts);
		currBitset.and(peerBitset);

		if(!currBitset.isEmpty())
		{
			ActualMessage messageToSend = new ActualMessage(message_Type.INTERESTED,null);
			peerHandler.writeMessage(messageToSend);
		}
	}

	/*
	 * Function name : handleInterestedMessage()
	 * Parameters    : msg - The actual message received from the remote peer.
	 * Description   : The method handles the interested message received from the peers.
	 * Return type   : void
	 */
	private void handleInterestedMessage(ActualMessage receivedMessage) 
	{
		//System.out.println("I have received the interested message from " + peerHandler.peerID);
		CommonConfigurations.interestedPeers.put(peerHandler.peerID, CommonConfigurations.peerConfigMap.get(peerHandler.peerID));
	}

	/*
	 * Function name : handleInterestedMessage()
	 * Parameters    : receivedMessage - The actual message received from the remote peer.
	 * Description   : The method handles the piece message received from the peers.
	 * Return type   : void
	 */
	private void handlePieceMessage(ActualMessage receivedMessage) throws IOException 
	{
		++CommonConfigurations.peerConfigMap.get(CommonConfigurations.currentPeerID).noOfPieces;
		byte[] payload = receivedMessage.messagePayload;
		CommonConfigurations.peerConfigMap.get(peerHandler.peerID).finishTime = System.nanoTime();

		long diff = CommonConfigurations.peerConfigMap.get(peerHandler.peerID).finishTime - CommonConfigurations.peerConfigMap.get(peerHandler.peerID).startTime;
		double elapsedTime = TimeUnit.MILLISECONDS.convert(diff, TimeUnit.NANOSECONDS) / 1000.0;
		double downloadRate = 0;

		if(elapsedTime != 0)
		{
			downloadRate = ((ConfigParameters.PieceSize + 4 + 1) / elapsedTime); 
		}

		//System.out.println("Download Rate : " + downloadRate);
		CommonConfigurations.peerConfigMap.get(peerHandler.peerID).downloadSpeed = downloadRate;

		byte[] indexOfField = new byte[4];
		ByteArrayInputStream bInput = new ByteArrayInputStream(payload);
		bInput.read(indexOfField,0,4);
		int index = ByteBuffer.wrap(indexOfField).getInt();
		setIndexOfBitfield(CommonConfigurations.currentPeerID,index);

		LoggingUtil.getLOGGER().log(Level.INFO,"Peer [" + CommonConfigurations.currentPeerID +
				"] has downloaded the piece [" + index + "] from [" + peerHandler.peerID + "]" + " .Now the number of pieces it has is [" + CommonConfigurations.peerConfigMap.get(CommonConfigurations.currentPeerID).noOfPieces + "]" );

		writeToFile(payload, index);

		sendHaveMessage(index);

		if(CommonConfigurations.peerConfigMap.get(CommonConfigurations.currentPeerID).noOfPieces == CommonConfigurations.noOfSplitParts)
		{
			LoggingUtil.getLOGGER().log(Level.INFO,"Peer [" + CommonConfigurations.currentPeerID + "] has downloaded the complete file");
			CommonConfigurations.mergeFile();
		}

		if(CommonConfigurations.peerConfigMap.get(CommonConfigurations.currentPeerID).noOfPieces != CommonConfigurations.noOfSplitParts)
		{
			sendRequestMessage();
		}
	}

	/*
	 * Function name : writeToFile()
	 * Parameters    : payload - The a received from the remote peer.
	 * Description   : The method handles the piece message received from the peers.
	 * Return type   : void
	 */
	private void writeToFile(byte[] payload, int index) throws IOException 
	{
		byte[] data = Arrays.copyOfRange(payload, 4, payload.length - 4);

		if(data.length >= ConfigParameters.PieceSize || (index == CommonConfigurations.noOfSplitParts - 1))
		{
			FileOutputStream fos;
			try {
				fos = new FileOutputStream(System.getProperty("user.dir") + File.separator + "peer_" + CommonConfigurations.currentPeerID + File.separator + index + ".splitPart");
				fos.write(data);
				fos.close();	
			} catch (FileNotFoundException e) 
			{
				System.out.println("Exception in writeToFile : FileNotFoundException");
			}
		}
		else
		{
			System.out.println("Incomplete piece recieved");
		}
	}

	/*
	 * Function name : sendHaveMessage()
	 * Parameters    : index - The index of the packet
	 * Description   : The method sending the have message
	 * Return type   : void
	 */
	private void sendHaveMessage(int index) 
	{
		//System.out.println("I am sending have message");
		for(int id : CommonConfigurations.peerBitField.keySet())
		{
			PeerParameters remotePeer = CommonConfigurations.peerConfigMap.get(id);
			if(remotePeer.peerHandler != null)
			{
				byte[] payloadToSend = ByteBuffer.allocate(4).putInt(index).array();
				ActualMessage sendMessage = new ActualMessage(message_Type.HAVE,payloadToSend);
				try {
					if(remotePeer.peerHandler != null)
					{
						remotePeer.peerHandler.writeMessage(sendMessage);
					}
				} catch (Exception e) 
				{
					System.out.println("Exception in sendHaveMessage : Exception");
				}
			}
		}

		BitSet currBitset = new BitSet(CommonConfigurations.noOfSplitParts);
		currBitset = (BitSet) CommonConfigurations.peerConfigMap.get(CommonConfigurations.currentPeerID).bitfield.clone();

		for(int peer : CommonConfigurations.interestedPeers.keySet())
		{
			BitSet peerBitset = new BitSet(CommonConfigurations.noOfSplitParts);
			peerBitset = (BitSet) CommonConfigurations.peerBitField.get(peer).clone();		

			currBitset.flip(0,CommonConfigurations.noOfSplitParts);
			currBitset.and(peerBitset);

			if((currBitset.isEmpty()) && (CommonConfigurations.peerConfigMap.get(peer).peerHandler != null))
			{
				ActualMessage messageToSend = new ActualMessage(message_Type.UNINTERESTED,null);
				//System.out.println("I have received the uninterested message");
				if(CommonConfigurations.peerConfigMap.get(peer).peerHandler != null)
				{
					CommonConfigurations.peerConfigMap.get(peer).peerHandler.writeMessage(messageToSend);
				}
			}
		}
	}


	/*
	 * Function name : handleRequestMessage()
	 * Parameters    : receivedMessage - The message read from the input stream
	 * Description   : The method handling the request message
	 * Return type   : void
	 */
	private void handleRequestMessage(ActualMessage receivedMessage) throws IOException 
	{
		int index = ByteBuffer.wrap(receivedMessage.messagePayload).getInt();
		String directoryPath = System.getProperty("user.dir") + File.separator + "peer_" + CommonConfigurations.currentPeerID + File.separator + index + ".splitPart";


		byte[] bytes1 = ByteBuffer.allocate(4).putInt(index).array();
		File file = new File(directoryPath);
		byte[] bytes2 = new byte[(int) file.length() + 4];

		FileInputStream fis;
		try {

			fis = new FileInputStream(file);
			fis.read(bytes2);
			fis.close();

		} catch (FileNotFoundException e) 
		{ 
			System.out.println("Excepiton in handleRequestMessage : FileNotFoundException");
		}

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		baos.write(bytes1);

		baos.write(bytes2);
		byte[] payload = baos.toByteArray();

		ActualMessage sendMessage = new ActualMessage(message_Type.PIECE, payload);


		if(peerHandler != null)
		{
			peerHandler.writeMessage(sendMessage);
		}

	}

	/*
	 * Function name : sendRequestMessage()
	 * Parameters    : none
	 * Description   : The method sending the request message
	 * Return type   : void
	 */
	private void sendRequestMessage() 
	{
		CommonConfigurations.peerConfigMap.get(peerHandler.peerID).startTime = System.nanoTime();
		int split = CommonConfigurations.noOfSplitParts;
		PeerParameters currentPeerParameters = CommonConfigurations.peerConfigMap.get(CommonConfigurations.currentPeerID);

		BitSet currBitset = new BitSet(split);
		currBitset = (BitSet) currentPeerParameters.bitfield.clone();


		BitSet peerBitset = new BitSet(split);
		peerBitset = (BitSet) CommonConfigurations.peerConfigMap.get(peerHandler.peerID).bitfield.clone();		

		currBitset.flip(0,CommonConfigurations.noOfSplitParts);

		currBitset.and(peerBitset);

		Random rand = new Random();

		if(!currBitset.isEmpty())
		{
			while(true)
			{
				int randIndex = rand.nextInt(CommonConfigurations.noOfSplitParts);
				//System.out.println("The piece I am requesting is " + randIndex);
				if(currBitset.get(randIndex) == true && (CommonConfigurations.RequestedPieces.contains(randIndex) == false))
				{
					CommonConfigurations.requestedIndex = randIndex;

					CommonConfigurations.RequestedPieces.add(randIndex);
					byte[] payload = ByteBuffer.allocate(4).putInt(randIndex).array();
					//System.out.println(ByteBuffer.wrap(payload).getInt());

					ActualMessage sendMessage = new ActualMessage(message_Type.REQUEST,payload);
					peerHandler.writeMessage(sendMessage);
					break;
				}
			}
		}

		else
		{
			ActualMessage sendMessage = new ActualMessage(message_Type.UNINTERESTED,null);
			peerHandler.writeMessage(sendMessage);
		}
	}

	/*
	 * Function name : handleBitfieldMessage()
	 * Parameters    : none
	 * Description   : The method handling the bitfeild message
	 * Return type   : void
	 */
	private void handleBitfieldMessage(ActualMessage msg) 
	{	
		if(msg.messagePayload != null)
		{
			CommonConfigurations.peerConfigMap.get(peerHandler.peerID).bitfield = (BitSet) BitSet.valueOf(msg.messagePayload).clone();
			CommonConfigurations.peerConfigMap.put(peerHandler.peerID,CommonConfigurations.peerConfigMap.get(peerHandler.peerID));
			CommonConfigurations.peerBitField.put(peerHandler.peerID,(BitSet) BitSet.valueOf(msg.messagePayload).clone());
		}
		else
		{
			System.out.println("The payload is null");
		}
		processForInterestedMessage();
	}

	/*
	 * Function name : processForInterestedMessage()
	 * Parameters    : none
	 * Description   : The method processing the condition to send interested message
	 * Return type   : void
	 */
	private void processForInterestedMessage() 
	{
		PeerParameters remotePeer = CommonConfigurations.peerConfigMap.get(peerHandler.peerID);

		BitSet currBitset = new BitSet(CommonConfigurations.noOfSplitParts);
		currBitset = (BitSet) CommonConfigurations.peerConfigMap.get(CommonConfigurations.currentPeerID).bitfield.clone();

		BitSet peerBitset = new BitSet(CommonConfigurations.noOfSplitParts);
		peerBitset = (BitSet) CommonConfigurations.peerBitField.get(remotePeer.peerID).clone();		

		currBitset.flip(0,CommonConfigurations.noOfSplitParts);
		currBitset.and(peerBitset);

		if(!currBitset.isEmpty())
		{
			ActualMessage messageToSend = new ActualMessage(message_Type.INTERESTED,null);
			peerHandler.writeMessage(messageToSend);
		}

		else
		{
			ActualMessage messageToSend = new ActualMessage(message_Type.UNINTERESTED,null);
			peerHandler.writeMessage(messageToSend);
		}
	}

	/*
	 * Function Name: constructor for MessageHandler
	 */
	MessageHandler(PeerHandler peerHandler)
	{
		this.peerHandler = peerHandler;
	}

	/*
	 * Function name : setIndexOfBitfield()
	 * Parameters    : peerID, Index
	 * Description   : The method setting the index of the bitfeild
	 * Return type   : void
	 */
	public synchronized void setIndexOfBitfield(int peerID,int index)
	{
		CommonConfigurations.peerConfigMap.get(peerID).bitfield.set(index);
		CommonConfigurations.peerBitField.get(peerID).set(index);
	}

	/*
	 * Function name : getIndexOfBitfield()
	 * Parameters    : peerID, Index
	 * Description   : The method getting the index of the bitfeild
	 * Return type   : void
	 */
	public synchronized boolean getIndexOfBitfield(int peerID,int index)
	{
		return CommonConfigurations.peerBitField.get(peerID).get(index);
	}

	/*
	 * Function name : stopThread()
	 * Parameters    : None
	 * Description   : The method for stopping the thread
	 * Return type   : void
	 */
	public void stopThread()
	{
		exitMsgHandler = true;
	}
}

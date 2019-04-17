/*
 * Class name	:	ConfigParameters.java
 * Description	:	This class defines the peer process, the class responsible for the main method of the protocol.
 * 					This class will deploy the server thread and the respective peers for the protocol
 * Institution	:	University of Florida
 */
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;


public class peerProcess {
	static ScheduledExecutorService scheduler;
	public volatile static boolean shutdown;

	public static void main(String[] args)
	{
		int PeerID = Integer.parseInt(args[0]);
		CommonConfigurations commonConfig = new CommonConfigurations();
		commonConfig.load();
		P2PPeer p2pPeer = commonConfig.loadPeer(PeerID);

		CommonConfigurations.currentPeerID = PeerID;

		LoggingUtil.initializeLogging(PeerID);
		try 
		{
			String directoryPath = System.getProperty("user.dir") + File.separator + "peer_" + PeerID;
			createDirectory(directoryPath);
		} 
		catch (IOException e) 
		{
			//System.out.println("Exception in creating the directory");
		}


		if(p2pPeer.peerParameters.completeFile == 1)
		{
			try {
				FileSplitter.splitFile(ConfigParameters.FileName,
						ConfigParameters.PieceSize, PeerID);
			} catch (IOException e) 
			{
				e.printStackTrace();
			}
		}

		setNumberOfSplits();
		//System.out.println("Number of split parts" + CommonConfigurations.noOfSplitParts);

		for(int id: CommonConfigurations.peerConfigMap.keySet())
		{
			CommonConfigurations.peerConfigMap.get(id).initialiseBitfield(id);
			CommonConfigurations.peerBitField.put(id, CommonConfigurations.peerConfigMap.get(id).bitfield);
		}

		Thread serverThread = new Server(p2pPeer.peerParameters);
		serverThread.setName("Server Thread");
		serverThread.start();


		for(int peerId : p2pPeer.PeerMap.keySet())
		{
			PeerParameters peerPara = CommonConfigurations.peerConfigMap.get(peerId);
			createPeerThread(peerPara,peerId);
		}

		scheduler = Executors.newScheduledThreadPool(3);
		StartShutdownProcess();
		DeterminekpreferredNeighbors(ConfigParameters.UnchokingInterval);
		DetermineOptimisticallyUnchoked(ConfigParameters.OptimisticUnchokingInterval);
	}

	/*
	 * Function name : setNumberOfSplits()
	 * Parameters    : None
	 * Description   : The method sets the number of split parts for the file.
	 * return type   : void
	 */
	private static void setNumberOfSplits() 
	{
		long numSplits = CommonConfigurations.configParameters.FileSize / ConfigParameters.PieceSize;

		long remainingBytes = CommonConfigurations.configParameters.FileSize % ConfigParameters.PieceSize;

		if(remainingBytes > 0)
		{
			CommonConfigurations.noOfSplitParts = (int) (numSplits + 1);
		}
	}

	/*
	 * Function name : DetermineOptimisticallyUnchoked()
	 * Parameters    : optimisticUnchokingInterval
	 * Description   : The thread is going to calculate the optimistically unchoked neighbor
	 * return type   : void
	 */
	private static void DetermineOptimisticallyUnchoked(int optimisticUnchokingInterval)
	{
		final Runnable DetermineOptimisticallyUnchokedPeer = new Runnable()
		{
			@Override
			public void run()
			{
				try 
				{
					int k = ConfigParameters.NumberOfPreferredNeighbors;
					CopyOnWriteArrayList<PeerParameters> interestedPeers = new CopyOnWriteArrayList<>();


					for(int id : CommonConfigurations.interestedPeers.keySet())
					{
						//System.out.println("I am adding this to copyWriteArrayList, interested peers -->> " + id);
						interestedPeers.add(CommonConfigurations.peerConfigMap.get(id));
					}
					Random rand = new Random();

					interestedPeers.sort(new Comparator<PeerParameters>() 
					{
						@Override
						public int compare (PeerParameters o1, PeerParameters o2)
						{
							//System.out.println("The download speed is : " + o1.downloadSpeed + "for " + o1.peerID);
							//System.out.println("The download speed is : " + o2.downloadSpeed + "for " + o2.peerID);
							if(o1.downloadSpeed == o2.downloadSpeed) {
								return rand.nextInt(2); 
							}
							return (int) (o2.downloadSpeed - o1.downloadSpeed);    //sort in decreasing order
						}
					});

					if(interestedPeers.size() > 0)
					{
						if(interestedPeers.size() == 1)
						{
							PeerParameters randomPeer = interestedPeers.get(0);
							LoggingUtil.getLOGGER().info("Peer " + "[" +CommonConfigurations.currentPeerID + "]"
									+ " has the optimistically unchoked neighbor [" + randomPeer.peerID + "]");
							sendUnchokeMessage(randomPeer);
						}

						else if(interestedPeers.size() == k + 1 )
						{
							PeerParameters randomPeer = interestedPeers.get(interestedPeers.size() - 1);
							LoggingUtil.getLOGGER().info("Peer " + "[" +CommonConfigurations.currentPeerID + "]"
									+ " has the optimistically unchoked neighbor [" + randomPeer.peerID + "]");
							sendUnchokeMessage(randomPeer);
						}

						else if(interestedPeers.size() > k + 1) 
						{
							int randomNum = ThreadLocalRandom.current().nextInt(k + 1, interestedPeers.size());
							PeerParameters peerPara = interestedPeers.get(randomNum);
							LoggingUtil.getLOGGER().info("Peer " + "[" +CommonConfigurations.currentPeerID + "]"
									+ " has the optimistically unchoked neighbor [" + peerPara.peerID + "]");
							sendUnchokeMessage(peerPara);
						}

						else
						{
							PeerParameters peerPara = interestedPeers.get(0);
							LoggingUtil.getLOGGER().info("Peer " + "[" +CommonConfigurations.currentPeerID + "]"
									+ " has the optimistically unchoked neighbor [" + peerPara.peerID + "]");
							sendUnchokeMessage(peerPara);
						}
					} 
					else
					{
						//System.out.println("I dont have enough peers to choose an optimistically unchoked peer");
					}
				}
				catch (Exception e) 
				{
					e.printStackTrace();
				}
			}


		};
		scheduler.scheduleAtFixedRate(DetermineOptimisticallyUnchokedPeer,optimisticUnchokingInterval,optimisticUnchokingInterval,TimeUnit.SECONDS);
	}

	/*
	 * Function name : sendUnchokeMessage()
	 * Parameters    : peerPara
	 * Description   : The method sends unchoke message 
	 * return type   : void
	 */
	private static void sendUnchokeMessage(PeerParameters peerPara) 
	{
		if(peerPara.stateOfPeer != peerState.UNCHOKED)
		{
			//System.out.println("I am evaluating this peer to send the unchoke message" + peerPara.peerID);
			ActualMessage sendMessage = new ActualMessage(message_Type.UNCHOKE,null);
			
			if(peerPara.peerHandler != null)
			{
				peerPara.peerHandler.writeMessage(sendMessage);
				CommonConfigurations.peerConfigMap.get(peerPara.peerID).stateOfPeer = peerState.UNCHOKED; 
			}
		}
	}

	/*
	 * Function name : DeterminekpreferredNeighbors()
	 * Parameters    : unchokingInterval
	 * Description   : The thread to monitor to calculate the k - preferred neighbors.
	 * return type   : void
	 */
	private static void DeterminekpreferredNeighbors(int unchokingInterval) 
	{
		final Runnable kPreferredNeighbors = new Runnable()
		{
			@Override
			public void run()
			{
				try 
				{
					int kPreferredNeighbors = ConfigParameters.getNumberOfPreferredNeighbors();
					int i = 0;
					Random rand = new Random();


					CopyOnWriteArrayList<PeerParameters> interestedPeers = new CopyOnWriteArrayList<PeerParameters>();

					String preferredNeighbors = "";

					for(int id : CommonConfigurations.interestedPeers.keySet())
					{
						preferredNeighbors = preferredNeighbors + " " + Integer.toString(CommonConfigurations.peerConfigMap.get(id).peerID) + ",";
						interestedPeers.add(CommonConfigurations.peerConfigMap.get(id));
					}

					if(preferredNeighbors != "")
					{
						LoggingUtil.getLOGGER().info("Peer " + CommonConfigurations.currentPeerID + " as the preferred neighbors " + "[" +preferredNeighbors + "]");
					}

					Iterator<PeerParameters> it = interestedPeers.iterator();
					ArrayList<Integer> kPreferredIndexes = new ArrayList<Integer>();

					if(interestedPeers.size() != 0 && interestedPeers.size() >= kPreferredNeighbors)
					{

						if((CommonConfigurations.peerConfigMap.get(CommonConfigurations.currentPeerID).completeFile == 1) 
								&& (CommonConfigurations.peerConfigMap.get(CommonConfigurations.currentPeerID).firstChoking == true))//check for complete file
						{
							for(int index = 0 ; index < kPreferredNeighbors; ++index)
							{
								int rnd = rand.nextInt(kPreferredNeighbors);
								kPreferredIndexes.add(rnd);
								PeerParameters sendPeer = interestedPeers.get(rnd);
								sendUnchokeMessage(sendPeer);
							}
							sendChokeMessage();
							CommonConfigurations.peerConfigMap.get(CommonConfigurations.currentPeerID).firstChoking = false;
						}

						else
						{
							interestedPeers.sort(new Comparator<PeerParameters>() {
								@Override
								public int compare (PeerParameters o1, PeerParameters o2){
									if(o1.downloadSpeed == o2.downloadSpeed) {
										return rand.nextInt(2); 
									}
									return (int) (o2.downloadSpeed - o1.downloadSpeed);    //sort in decreasing order
								}
							});
							while(interestedPeers.size() >= kPreferredNeighbors && i < kPreferredNeighbors)
							{
								PeerParameters peerSelected = it.next();
								i++;
								sendUnchokeMessage(peerSelected);
							}
							sendChokeMessage();
						}
					}
					else
					{
						//System.out.println("Insufficient peers for calculation");
					}
				}

				catch (Exception e) {
					e.printStackTrace();
				}
			}
		};

		try {
			final ScheduledFuture<?> kPreferredNeighborDeterminer = 
					scheduler.scheduleAtFixedRate(kPreferredNeighbors,unchokingInterval,unchokingInterval,TimeUnit.SECONDS);
		} catch (Exception e) 
		{
			//System.out.println("Exception in determining k-preferred neighbors");
		}

	}

	/*
	 * Function name : sendChokeMessage()
	 * Parameters    : None
	 * Description   : The method sends the choke message.
	 * return type   : void
	 */
	private static void sendChokeMessage() 
	{
		for(int id: CommonConfigurations.interestedPeers.keySet())
		{
			//System.out.println("I am evaluating this peer to send the choke message" + id);
			if(CommonConfigurations.interestedPeers.get(id).stateOfPeer != peerState.UNCHOKED)
			{
				ActualMessage ChokeMessage = new ActualMessage(message_Type.CHOKE,null);
				try
				{
					if(CommonConfigurations.interestedPeers.get(id).peerHandler != null)
					{
						CommonConfigurations.interestedPeers.get(id).peerHandler.writeMessage(ChokeMessage);
						CommonConfigurations.interestedPeers.get(id).stateOfPeer = peerState.CHOKED;
					}
				}
				catch(Exception ex)
				{
					ex.printStackTrace();
				}
			}
		}
	}

	/*
	 * Function name : StartShutdownProcess()
	 * Parameters    : None
	 * Description   : The thread to monitor for the shutdown process
	 * return type   : void
	 */
	private static void StartShutdownProcess() 
	{
		final Runnable shutdownProcess = new Runnable()
		{
			@Override
			public void run()
			{
				try 
				{
					shutdown = true;
					for(int id : CommonConfigurations.peerConfigMap.keySet())
					{
						int value = CommonConfigurations.peerConfigMap.get(id).bitfield.cardinality();
						if(CommonConfigurations.peerConfigMap.get(id).peerHandler != null || id == CommonConfigurations.currentPeerID)
						{
							if(value != CommonConfigurations.noOfSplitParts)
							{
								shutdown = false;
								break;
							}	
						}
					}

					if(shutdown == true)
					{
						for (PeerHandler peer : CommonConfigurations.myPeerHandlerThreads)
						{
							peer.stopThread();
							peer = null;
						}
						try 
						{
							System.out.println("That is all folks :)");
							scheduler.shutdown();
							scheduler.awaitTermination(5, TimeUnit.SECONDS);
							LoggingUtil.getLOGGER().info("Peer " + "[" + CommonConfigurations.currentPeerID + "]" + " is shuting down.");
							System.exit(0);
						}
						catch(SecurityException ex)
						{
							//ex.printStackTrace(); 
						} 

					}
				} 
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		};

		try
		{
			scheduler.scheduleAtFixedRate(shutdownProcess, 25, 8, TimeUnit.SECONDS);
		} catch (Exception e) 
		{
			e.printStackTrace();
		}

	}

	/*
	 * Function name : createPeerThread()
	 * Parameters    : PeerParameters - the parameters of the peer to which the connect request will be sent
	 * 				   PeerID - The peerID of the peer sending the connect request to the peers
	 * Description   : Display peer parameters.
	 * return type   : void
	 */
	public static void createPeerThread(PeerParameters peerPara,int PeerID)
	{
		InetAddress IPAddress = null;
		try {
			IPAddress = InetAddress.getByName(peerPara.peerName);
			Socket socket = new Socket(IPAddress,peerPara.portNumber);
			ObjectOutputStream ObjectOutStream = new ObjectOutputStream(socket.getOutputStream());
			ObjectInputStream ObjectInStream = new ObjectInputStream(socket.getInputStream());

			CommonConfigurations.peerConfigMap.get(peerPara.peerID).stateOfPeer = peerState.HANDSHAKE;
			MessageType message = new MessageType(CommonConfigurations.currentPeerID);
			message.setMessageHeader("P2PFILESHARINGPROJ");
			try {
				ObjectOutStream.writeObject(message);
				ObjectOutStream.flush();
				LoggingUtil.getLOGGER().log(Level.INFO,"Peer " + CommonConfigurations.currentPeerID + " makes connection to Peer " + peerPara.peerID );
				
			} catch (Exception e) 
			{
			}
			PeerHandler peerhandlerThread = new PeerHandler(socket,ObjectInStream,ObjectOutStream,peerPara.peerID);
			CommonConfigurations.myPeerHandlerThreads.add(peerhandlerThread);
			peerhandlerThread.start();

		} 
		catch (SocketException e) 
		{
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			//To be added
		}
	}

	/*
	 * Function name : createDirectory()
	 * Parameters    : directoryPath - the path at which the directory is supposed to be connected.
	 * Description   : The method creates a directory for the peer 
	 * return type   : File
	 */
	public static File createDirectory(String directoryPath) throws IOException 
	{
		File dir = new File(directoryPath);
		if (dir.exists()) {
			return dir;
		}
		if (dir.mkdirs()) {
			return dir;
		}
		throw new IOException("Failed to create directory '" + dir.getAbsolutePath() + "' for an unknown reason.");
	}
}

/*
 * Class name	:	CommonConfigurations.java
 * Description	:	This class defines the common parameters for the entire framework
 * Institution	:	University of Florida
 */
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class CommonConfigurations
{
	public static int currentPeerID;
	public static final DateFormat dateFormat = new SimpleDateFormat("dd/MM/YYYY HH:mm:ss");
	public static ConfigParameters configParameters;
	public static ConcurrentHashMap<Integer,PeerParameters> peerConfigMap = new ConcurrentHashMap<Integer,PeerParameters>();
	public static ConcurrentHashMap<Integer,PeerParameters> interestedPeers = new ConcurrentHashMap<Integer,PeerParameters>();
	public static ArrayList<PeerParameters> kpreferredNeighbours = new ArrayList<PeerParameters>();
	public static ConcurrentHashMap<Integer,BitSet> peerBitField = new ConcurrentHashMap<Integer,BitSet>();
	public static HashSet<Integer> RequestedPieces = new HashSet<Integer>();
	public static CopyOnWriteArrayList<PeerHandler> myPeerHandlerThreads = new CopyOnWriteArrayList<PeerHandler>();
	public static int noOfSplitParts;
	public static int requestedIndex;
	
	/*
	 * Function name : load()
	 * Parameters    : none
	 * Description   : The function is responsible to load the configurations for the protocol.
	 * Return type   : Void
	 */
	public void load()
	{
		ConfigReader configReader = new ConfigReader();
		String commonPath = "Common.cfg";
		String peerPath = "PeerInfo.cfg";

		configParameters = configReader.readCommonConfigFromFile(commonPath);

		ArrayList<P2PPeer> p2pPeers = configReader.readPeerConfiguration(peerPath);
		for(P2PPeer p1 : p2pPeers)
		{
			peerConfigMap.put(p1.getPeerParameters().peerID,p1.getPeerParameters());
		}
	}

	/*
	 * Function name : loadPeer()
	 * Parameters    : peer ID - The ID of the peer whose configuration will be read.
	 * Description   : The function is responsible to load the peer configurations for the protocol.
	 * Return type   : P2PPeer - The peer information
	 */
	public P2PPeer loadPeer(int peerID)
	{
		P2PPeer p2pPeer = new P2PPeer();

		for(int peerId : peerConfigMap.keySet())
		{
			if(peerId != peerID)
			{
				PeerParameters peerConfig = peerConfigMap.get(peerId);
				p2pPeer.PeerMap.put(peerId, peerConfig);
			}
			else
			{
				p2pPeer.peerParameters = peerConfigMap.get(peerID);
				break;
			}
		}
		return p2pPeer;
	}

	/*
	 * Function name : mergeFile()
	 * Parameters    : none
	 * Description   : The functions is going to merge the file splitparts into the main output file.
	 * Return        : void
	 */
	public static void mergeFile() throws IOException 
	{
		String directoryPath;
		ArrayList<byte[]> byteArray = new ArrayList<byte[]>(100);
		String fileName = CommonConfigurations.configParameters.getFileName();
		//System.out.println(fileName);

		String[] tokens = fileName.split("\\.");
		String extension = tokens[1];
		try {
			for(int index = 0; index < CommonConfigurations.noOfSplitParts; ++index)
			{
				directoryPath = System.getProperty("user.dir") + File.separator + "peer_" + CommonConfigurations.currentPeerID + File.separator + index + ".splitPart";
				File file = new File(directoryPath);
				FileInputStream fis = null;
				byte[] bArray = new byte[(int) file.length()];

				try{
					fis = new FileInputStream(file);
					fis.read(bArray);
					byteArray.add(index, bArray);
					fis.close();           
				}
				catch(IOException ioExp){
					System.out.println("IO exception in Merge file - 1");
				}
			}

			OutputStream os = new FileOutputStream(System.getProperty("user.dir") + File.separator + "peer_" + CommonConfigurations.currentPeerID + File.separator + "output." + extension); 
			for(int index = 0; index < CommonConfigurations.noOfSplitParts; ++index)
			{
				os.write(byteArray.get(index));
			}
			try {
				os.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				System.out.println("Problem in closing file");
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("There was an exception in mergeFile - 2");
		}
	}
}

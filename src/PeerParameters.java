import java.util.BitSet;

/*
 * Class name	:	PeerParameters.java
 * Description	:	This class defines the peer parameters for the protocol
 * Institution	:	University of Florida
 */
public class PeerParameters
{
	int peerID;
	String peerName;
	int portNumber;
	int completeFile;
	double downloadSpeed;
	peerState stateOfPeer = peerState.INITIAL;
	public BitSet bitfield;
	PeerHandler peerHandler = null;
	int noOfPieces;
	boolean firstChoking;
	long startTime;
	long finishTime;

	PeerParameters()
	{
		peerID = 0;
		portNumber = 0;
		completeFile = 0;
		stateOfPeer = peerState.INITIAL;
		noOfPieces = 0;
		bitfield = new BitSet(CommonConfigurations.noOfSplitParts);
		downloadSpeed = 0;
		firstChoking = true;
	}

	/**
	 * @return the peerID
	 */
	public int getPeerID() {
		return peerID;
	}
	/**
	 * @param peerID the peerID to set
	 */
	public void setPeerID(int peerID) {
		this.peerID = peerID;
	}
	/**
	 * @return the peerName
	 */
	public String getPeerName() {
		return peerName;
	}
	/**
	 * @param peerName the peerName to set
	 */
	public void setPeerName(String peerName) {
		this.peerName = peerName;
	}
	/**
	 * @return the portNumber
	 */
	public int getPortNumber() {
		return portNumber;
	}
	/**
	 * @param portNumber the portNumber to set
	 */
	public void setPortNumber(int portNumber) {
		this.portNumber = portNumber;
	}
	/**
	 * @return the completeFile
	 */
	public int isCompleteFile() {
		return completeFile;
	}
	/**
	 * @param completeFile the completeFile to set
	 */
	public void setCompleteFile(int completeFile) {
		this.completeFile = completeFile;
	}

	/**
	 *
	 * @param downloadSpeed
	 */
	public void setDownloadSpeed(long downloadSpeed)
	{
		this.downloadSpeed=downloadSpeed;
	}

	public double getDownloadSpeed()
	{
		return downloadSpeed;
	}
	
	public void initialiseBitfield(int peerID)
	{
		//System.out.println("The peer I am initializing is " + peerID);
		if(CommonConfigurations.peerConfigMap.get(peerID).completeFile == 1)
		{
			//System.out.println("Complete file is present here");
			int noOfSplitParts = CommonConfigurations.noOfSplitParts;
			
			for(int index = 0; index < noOfSplitParts ; index++) 
			{
				CommonConfigurations.peerConfigMap.get(peerID).bitfield.set(index);    
		    }
		}
	}

	/*
	 * Function name : displayParameters()
	 * Parameters    : none
	 * Description   : Display peer parameters.
	 * return type   : void
	 */
	public void displayParameters()
	{
		System.out.println("The peer ID is : " + peerID);
		System.out.println("The peer Name : " + peerName);
		System.out.println("The port Number is : " + portNumber);
		System.out.println("Is the complete file present " + completeFile);
	}

	/**
	 * @return the bitfield
	 */
	public BitSet getBitfield() {
		return bitfield;
	}

	
}
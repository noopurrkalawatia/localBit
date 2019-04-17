/*
 * Class name	:	P2PPeer.java
 * Description	:	This class defines the peer for the protocol
 * Institution	:	University of Florida
 */
import java.util.HashMap;

public class P2PPeer
{
	public PeerParameters peerParameters;
	public HashMap<Integer,PeerParameters> PeerMap;
	
	/*
	 * Function name : P2PPeer()
	 * Parameters    : none
	 * Description   : Constructor 
	 */
	public P2PPeer()
	{
		peerParameters = new PeerParameters();
		PeerMap = new HashMap<Integer,PeerParameters>();
	}

	/**
	 * @return the peerParameters
	 */
	public PeerParameters getPeerParameters() {
		return peerParameters;
	}

	/**
	 * @param peerParameters the peerParameters to set
	 */
	public void setPeerParameters(PeerParameters peerParameters) {
		this.peerParameters = peerParameters;
	}
}

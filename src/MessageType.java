/*
 * Class name	:	MessageType.java
 * Description	:	This class defines the basic type of message format for the protocol.
 * Institution	:	University of Florida
 */
import java.io.Serializable;
import java.util.BitSet;

public class MessageType implements Serializable
{

	private static final long serialVersionUID = 1L;
	public String messageHeader;

	MessageType(int peerID)
	{
		PeerID = peerID;
	}
	
	/*
	 * The getter and the setter functions for the class members.
	 */
	public String getMessageHeader() {
		return messageHeader;
	}

	public void setMessageHeader(String messageHeader) {
		this.messageHeader = messageHeader;
	}

	BitSet bitSet = new BitSet(80);
	public BitSet getBitSet() {
		return bitSet;
	}

	public void setBitSet(BitSet bitSet) {
		this.bitSet = bitSet;
	}

	public int getPeerID() {
		return PeerID;
	}

	public void setPeerID(int peerID) {
		PeerID = peerID;
	}

	int PeerID;
	

}

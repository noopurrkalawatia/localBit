/*
 * Class name	:	ActualMessage.java
 * Description	:	This class is responsible for structure of the actual message and the functionalities of the same.
 * Institution	:	University of Florida
 */
import java.io.Serializable;

public class ActualMessage implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7556123042923046061L;
	/**
	 * 
	 */

	int messageLen;
	message_Type type;
	byte[] messagePayload;

	/*
	 * Function name : ActualMessage()
	 * Parameters    : message type and the payload for the message.
	 * Description   : Constructor
	 */
	public ActualMessage(message_Type msgType , byte[] messPayload)
	{
		this.type = msgType;
		if(messPayload != null)
		{
			if(messPayload.length != 0)
			{
				this.messagePayload = messPayload;
				this.messageLen = messagePayload.length + 1;
			}
		}
		else
		{
			this.messagePayload = null;
			this.messageLen = 1;
		}
	}


	public void setMessageLen(int messageLen)
	{
		this.messageLen=messageLen;
	}

	/**
	 * @return
	 */
	public  int getMessageLen()
	{
		return messageLen;
	}

	/**
	 * @param messagePayload
	 */
	public void setmessagePayload(byte[] messagePayload)
	{
		this.messagePayload=messagePayload;
	}

	public byte[] getmessagePayload()
	{
		return messagePayload;
	}

	public String getMessageType()
	{
		String c="";
		return c;
	}
}
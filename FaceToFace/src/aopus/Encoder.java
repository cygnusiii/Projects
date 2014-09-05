package aopus;

public class Encoder
{
    private long state;
	
	public Encoder(int clockRate, int channels, int packetTime)
	{
		try
		{
	        state = OpusLibrary.encoderCreate(clockRate, channels, packetTime);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	public byte[] encode(byte[] data, int index, int length)
	{
	    return OpusLibrary.encoderEncode(state, data, index, length);
	}

    public void destroy()
	{
		try
		{
	        OpusLibrary.encoderDestroy(state);
		}
		catch (Exception ex) { }
	}
}
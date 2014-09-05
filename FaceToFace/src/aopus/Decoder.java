package aopus;

public class Decoder
{
    private long state;

	public Decoder(int clockRate, int channels, int packetTime)
	{
		try
		{
		    state = OpusLibrary.decoderCreate(clockRate, channels, packetTime);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

    public byte[] decode(byte[] encodedData)
    {
        return OpusLibrary.decoderDecode(state, encodedData);
    }
	
	public void destroy()
	{
        try
        {
            OpusLibrary.decoderDestroy(state);
        }
        catch (Exception ex) { }
	}
}
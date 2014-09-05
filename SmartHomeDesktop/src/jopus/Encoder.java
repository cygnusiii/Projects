package jopus;

import org.bridj.*;

import opus.*;
import opus.OpusLibrary.*;

public class Encoder
{
    private Pointer<OpusEncoder> enc;
    private int frameSizePerChannel;
    private int maxBufferSize;
    private Pointer<Byte> buffer;
	
	public boolean debugMode;
	
	public Encoder(int clockRate, int channels, int packetTime)
	{
		try
		{
			// make sure opus is loaded
			BridJ.getNativeLibrary("opus");
			
            frameSizePerChannel = (clockRate * packetTime) / 1000;
            maxBufferSize = 4000;
            buffer = Pointer.allocateBytes(maxBufferSize);

            // initialize encoder
            Pointer<Integer> error = Pointer.allocateInt();
            try
            {
                enc = OpusLibrary.opus_encoder_create(clockRate, channels, OpusLibrary.OPUS_APPLICATION_VOIP, error);
                if (error.getInt() != OpusLibrary.OPUS_OK)
                {
                    throw new Exception("Could not initialize encoder. " + OpusLibrary.opus_strerror(error.getInt()));
                }
            }
            finally
            {
                Pointer.release(error);
            }
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	public byte[] encode(byte[] data, int index, int length)
	{
        // wrap managed
        Pointer<Short> dataShorts = Pointer.allocateShorts(length / 2);
        dataShorts.setBytesAtOffset(0, data, index, length);
        try
        {
            int encodedLength = OpusLibrary.opus_encode(enc, dataShorts, frameSizePerChannel, buffer, maxBufferSize);
            if (encodedLength > 0)
            {
                // unwrap unmanaged
                return buffer.getBytes(encodedLength);
            }
            
            if (debugMode)
            {
                System.out.println("Could not encode frame.");
            }
            return null;
        }
        finally
        {
            Pointer.release(dataShorts);
        }
	}

    public void destroy()
	{
		try
		{
            if (enc != null)
            {
                OpusLibrary.opus_encoder_destroy(enc);
                enc = null;
            }

            if (buffer != null)
            {
                Pointer.release(buffer);
                buffer = null;
            }
		}
		catch (Exception ex) { }
	}
}
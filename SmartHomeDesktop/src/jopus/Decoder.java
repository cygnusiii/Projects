package jopus;

import java.nio.ByteBuffer;

import org.bridj.*;

import opus.*;
import opus.OpusLibrary.*;

public class Decoder
{
    private Pointer<OpusDecoder> dec;
    private int frameSizePerChannel;
    private int maxBufferSize;
    private Pointer<Short> buffer;
    private int channels;
    private boolean previousPacketInvalid;
	
	public boolean debugMode;

	public Decoder(int clockRate, int channels, int packetTime)
	{
		try
		{
			// make sure opus is loaded
			BridJ.getNativeLibrary("opus");

            frameSizePerChannel = (clockRate * packetTime) / 1000;
            maxBufferSize = clockRate * channels * packetTime * 2 / 1000;
            buffer = Pointer.allocateShorts(maxBufferSize / 2);

            this.channels = channels;
            previousPacketInvalid = false;

            // initialize decoder
            Pointer<Integer> error = Pointer.allocateInt();
            try
            {
                dec = OpusLibrary.opus_decoder_create(clockRate, channels, error);
                if (error.getInt() != OpusLibrary.OPUS_OK)
                {
                    throw new Exception("Could not initialize decoder. " + OpusLibrary.opus_strerror(error.getInt()));
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

    public byte[] decode(byte[] encodedData)
    {
        // wrap managed
        Pointer<Byte> encodedDataBytes = Pointer.pointerToBytes(ByteBuffer.wrap(encodedData));
        try
        {
            int numSamplesDecoded;
            int bandwidth = OpusLibrary.opus_packet_get_bandwidth(encodedDataBytes);
            if (bandwidth == OpusLibrary.OPUS_INVALID_PACKET)
            {
                numSamplesDecoded = OpusLibrary.opus_decode(dec, null, 0, buffer, frameSizePerChannel, 0);
                previousPacketInvalid = true;
            }
            else
            {
                numSamplesDecoded = OpusLibrary.opus_decode(dec, encodedDataBytes, encodedData.length, buffer, frameSizePerChannel, previousPacketInvalid ? 1 : 0);
                previousPacketInvalid = false;
            }
    
            if (numSamplesDecoded > 0)
            {
                // unwrap unmanaged
                return buffer.getBytes(numSamplesDecoded * channels * 2);
            }
    
            if (debugMode)
            {
                System.out.println("Could not decode frame.");
            }
            return null;
        }
        finally
        {
            Pointer.release(encodedDataBytes);
        }
    }
	
	public void destroy()
	{
        try
        {
            if (dec != null)
            {
                OpusLibrary.opus_decoder_destroy(dec);
                dec = null;
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
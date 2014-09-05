package jopus;

import fm.icelink.*;
import fm.icelink.webrtc.*;

public class OpusCodec extends AudioCodec {

    private BasicAudioPadep padep;
    private Encoder encoder;
    private Decoder decoder;
    
    public OpusCodec()
    {
        super(20);
        padep = new BasicAudioPadep();
    }

    /// <summary>
    /// Encodes a frame.
    /// </summary>
    /// <param name="frame">The frame.</param>
    /// <returns></returns>
    public byte[] encode(AudioBuffer frame)
    {
        if (encoder == null)
        {
            encoder = new Encoder(getClockRate(), getChannels(), getPacketTime());
        }
        
        return encoder.encode(frame.getData(), frame.getIndex(), frame.getLength());
    }

    /// <summary>
    /// Decodes an encoded frame.
    /// </summary>
    /// <param name="encodedFrame">The encoded frame.</param>
    /// <returns></returns>
    public AudioBuffer decode(byte[] encodedFrame)
    {
        if (decoder == null)
        {
            decoder = new Decoder(getClockRate(), getChannels(), getPacketTime());
        }

        byte[] data = decoder.decode(encodedFrame);
        if (data == null)
        {
            return null;
        }
        return new AudioBuffer(data, 0, data.length);
    }

    /// <summary>
    /// Packetizes an encoded frame.
    /// </summary>
    /// <param name="encodedFrame">The encoded frame.</param>
    /// <returns></returns>
    public RTPPacket[] packetize(byte[] encodedFrame)
    {
        return padep.packetize(encodedFrame, getClockRate());
    }

    /// <summary>
    /// Depacketizes a packet.
    /// </summary>
    /// <param name="packet">The packet.</param>
    /// <returns></returns>
    public byte[] depacketize(RTPPacket packet)
    {
        return padep.depacketize(packet);
    }

    /// <summary>
    /// Processes RTCP packets.
    /// </summary>
    /// <param name="packets">The packets to process.</param>
    public void processRTCP(RTCPPacket[] packets)
    { }

    /// <summary>
    /// Destroys the codec.
    /// </summary>
    public void destroy()
    {
        if (encoder != null)
        {
            encoder.destroy();
            encoder = null;
        }

        if (decoder != null)
        {
            decoder.destroy();
            decoder = null;
        }
    }
}

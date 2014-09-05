package aopus;

public class OpusLibrary
{
    static
    {
        System.loadLibrary("opus");
        System.loadLibrary("opusJNI");
    }
    
    public static native long   encoderCreate(int clockRate, int channels, int packetTime);
    public static native void   encoderDestroy(long state);
    public static native byte[] encoderEncode(long state, byte[] data, int index, int length);
    
    public static native long   decoderCreate(int clockRate, int channels, int packetTime);
    public static native void   decoderDestroy(long state);
    public static native byte[] decoderDecode(long state, byte[] encodedData);
}

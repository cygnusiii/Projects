package jvp8;

import java.nio.*;

import java.awt.image.*;

import org.bridj.*;

import vpx.*;
import vpx.VpxLibrary.*;

public class Decoder
{
	private vpx_codec_ctx_t codec;
	
	private static Pointer<vpx_codec_iface> vp8dx = VpxLibrary.vpx_codec_vp8_dx();
	
	public boolean debugMode;
    public boolean needsKeyFrame;
	
    @SuppressWarnings("deprecation")
	public Decoder()
	{
		try
		{
			// make sure vpx is loaded
			BridJ.getNativeLibrary("vpx");

			// initialize decoder
            codec = new vpx_codec_ctx_t();
			IntValuedEnum<vpx_codec_err_t> res = VpxLibrary.vpx_codec_dec_init_ver(Pointer.pointerTo(codec), vp8dx, null, 0, VpxLibrary.VPX_DECODER_ABI_VERSION);
			if (res.value() != vpx_codec_err_t.VPX_CODEC_OK.value())
			{
				throw new Exception("Could not initialize decoder. " + VpxLibrary.vpx_codec_err_to_string(res));
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

    @SuppressWarnings("deprecation")
    public BufferedImage decode(byte[] encodedData)
    {
        // copy managed buffer to unmanaged buffer
        ByteBuffer frame = ByteBuffer.allocateDirect(encodedData.length);
        for (byte b : encodedData)
        {
        	frame.put(b);
        }

        // decode
        IntValuedEnum<vpx_codec_err_t> res = VpxLibrary.vpx_codec_decode(Pointer.pointerTo(codec), Pointer.pointerToBytes(frame), encodedData.length, null, 0);
        if (res.value() != vpx_codec_err_t.VPX_CODEC_OK.value())
        {
            if (res.value() == vpx_codec_err_t.VPX_CODEC_UNSUP_BITSTREAM.value())
            {
                needsKeyFrame = true;
            }
        	if (debugMode)
        	{
        		System.out.println("Could not decode frame. " + VpxLibrary.vpx_codec_err_to_string(res));
        	}
        	return null;
        }

        needsKeyFrame = false;

        // get frame
		Pointer<Pointer<?>> iter = Pointer.allocatePointer();
        try
        {
            Pointer<vpx_image_t> img;
            while ((img = VpxLibrary.vpx_codec_get_frame(Pointer.pointerTo(codec), iter)) != null)
            {
                // convert I420 to RGB
                vpx_image_t _img = img.get();
                BufferedImage bufferedImage = new BufferedImage(_img.d_w(), _img.d_h(), BufferedImage.TYPE_3BYTE_BGR);
                Converter.convertI420ToRGB24(img, bufferedImage);
                return bufferedImage;
            }
        }
        finally
        {
            Pointer.release(iter);
        }

    	if (debugMode)
    	{
    		System.out.println("Could not decode frame.");
    	}
        return null;
    }
	
    @SuppressWarnings("deprecation")
	public void destroy()
	{
		try
		{
            VpxLibrary.vpx_codec_destroy(Pointer.pointerTo(codec));
            codec = null;
		}
		catch (Exception ex) { }
	}
}
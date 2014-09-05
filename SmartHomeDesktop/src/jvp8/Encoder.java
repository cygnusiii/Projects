package jvp8;

import org.bridj.*;

import vpx.*;
import vpx.VpxLibrary.*;

import java.awt.image.*;

public class Encoder
{
	private vpx_codec_ctx_t codec;
	private vpx_image_t img;
	private int frame_cnt;
	private boolean sendKeyFrame;
	
	private static Pointer<vpx_codec_iface> vp8cx = VpxLibrary.vpx_codec_vp8_cx();
	
	public boolean debugMode;
	
    @SuppressWarnings("deprecation")
	public Encoder(int width, int height, int fps)
	{
		try
		{
			// make sure vpx is loaded
			BridJ.getNativeLibrary("vpx");

	        // allocate image
            img = new vpx_image_t();
            VpxLibrary.vpx_img_alloc(Pointer.pointerTo(img), vpx_img_fmt_t.VPX_IMG_FMT_I420, width, height, 0);

	        // define configuration options
			frame_cnt = 0;
            vpx_codec_enc_cfg_t cfg = new vpx_codec_enc_cfg_t();
			IntValuedEnum<vpx_codec_err_t> res = VpxLibrary.vpx_codec_enc_config_default(vp8cx, Pointer.pointerTo(cfg), 0);
			if (res.value() != vpx_codec_err_t.VPX_CODEC_OK.value())
			{
				throw new Exception("Could not get encoder config. " + VpxLibrary.vpx_codec_err_to_string(res));
			}
			cfg.g_timebase().num(1);
			cfg.g_timebase().den(fps);
			cfg.rc_target_bitrate(width * height * 256 / 320 / 240);
			cfg.rc_end_usage(vpx_rc_mode.VPX_CBR);
			cfg.g_w(width);
			cfg.g_h(height);
			cfg.kf_mode(vpx_kf_mode.VPX_KF_AUTO);
			cfg.kf_min_dist(fps); // keyframe 1x / second
			cfg.kf_max_dist(fps);
			cfg.g_error_resilient(1);
			cfg.g_lag_in_frames(0);
			cfg.g_pass(vpx_enc_pass.VPX_RC_ONE_PASS);
			cfg.rc_min_quantizer(0);
			cfg.rc_max_quantizer(63);
			cfg.g_profile(0);

	        // initialize encoder
            codec = new vpx_codec_ctx_t();
			res = VpxLibrary.vpx_codec_enc_init_ver(Pointer.pointerTo(codec), vp8cx, Pointer.pointerTo(cfg), 0, VpxLibrary.VPX_ENCODER_ABI_VERSION);
			if (res.value() != vpx_codec_err_t.VPX_CODEC_OK.value())
			{
				throw new Exception("Could not initialize encoder. " + VpxLibrary.vpx_codec_err_to_string(res));
		    }
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
    @SuppressWarnings("deprecation")
	public byte[] encode(BufferedImage bufferedImage)
	{
		// convert RGB to I420
		Converter.convertRGB24ToI420(bufferedImage, Pointer.pointerTo(img));
	    
	    // set flags
	    long flag = 0;
	    if (sendKeyFrame)
	    {
	        if (debugMode)
	        {
	        	System.out.println("Forcing keyframe.");
	        }
	        
	        flag |= VpxLibrary.VPX_EFLAG_FORCE_KF;
	        sendKeyFrame = false;
	    }

		// encode
		IntValuedEnum<vpx_codec_err_t> res = VpxLibrary.vpx_codec_encode(Pointer.pointerTo(codec), Pointer.pointerTo(img), frame_cnt, 1, flag, VpxLibrary.VPX_DL_REALTIME);
		if (res.value() != vpx_codec_err_t.VPX_CODEC_OK.value())
		{
        	if (debugMode)
        	{
        		System.out.println("Could not encode frame. " + VpxLibrary.vpx_codec_err_to_string(res));
        	}
			return null;
		}

		frame_cnt++;

        Pointer<Pointer<?>> iter = Pointer.allocatePointer();
        try
        {
            // get frame
            Pointer<vpx_codec_cx_pkt_t> pktPtr;
            while ((pktPtr = VpxLibrary.vpx_codec_get_cx_data(Pointer.pointerTo(codec), iter)) != null)
            {
                vpx_codec_cx_pkt_t pkt = pktPtr.get();
                if (pkt.kind().value() == vpx_codec_cx_pkt_kind.VPX_CODEC_CX_FRAME_PKT.value())
                {
                    // copy unmanaged buffer to managed buffer
                    vpx_codec_cx_pkt_t.data_union.frame_struct frame = pkt.data().frame();
                    return frame.buf().getBytes((int)frame.sz());
                }
            }
        }
        finally
        {
            Pointer.release(iter);
        }

        if (debugMode)
        {
            System.out.println("Could not encode frame.");
        }
        return null;
	}
	
	public void forceKeyframe()
	{
		sendKeyFrame = true;
	}

    @SuppressWarnings("deprecation")
    public void destroy()
	{
		try
		{
            VpxLibrary.vpx_img_free(Pointer.pointerTo(img));
            VpxLibrary.vpx_codec_destroy(Pointer.pointerTo(codec));
            img = null;
            codec = null;
		}
		catch (Exception ex) { }
	}
}
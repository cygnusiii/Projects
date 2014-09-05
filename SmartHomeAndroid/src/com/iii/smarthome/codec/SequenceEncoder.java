package com.iii.smarthome.codec;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.YuvImage;

public class SequenceEncoder extends org.jcodec.api.SequenceEncoder {

	public SequenceEncoder(File out) throws IOException {
		super(out);
	}

	public void encodeImage(Bitmap bi) throws IOException {
        encodeNativeFrame(BitmapUtil.fromBitmap(bi));
    }
	public void encodeImage(YuvImage yuv) throws IOException{
		encodeImage(getBitmapFromYUV(yuv));
	}
	public  Bitmap getBitmapFromYUV(YuvImage yImage) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        yImage.compressToJpeg(new Rect(0, 0, yImage.getWidth(), yImage.getHeight()), 100, baos);
        byte[] jdata = baos.toByteArray();
        BitmapFactory.Options bitmapFatoryOptions = new BitmapFactory.Options();
        bitmapFatoryOptions.inPreferredConfig = Bitmap.Config.RGB_565;
        Bitmap bmp = BitmapFactory.decodeByteArray(jdata, 0, jdata.length, bitmapFatoryOptions);
        return bmp;
	}

}
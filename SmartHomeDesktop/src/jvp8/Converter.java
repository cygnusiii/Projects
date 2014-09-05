package jvp8;

import org.bridj.*;

import vpx.*;

import java.awt.image.*;

public class Converter
{
	// Converts RGB24 data to a VPX YUV I420 image
	public static void convertRGB24ToI420(BufferedImage bufferedImage, Pointer<vpx_image_t> img)
	{
		DataBuffer rgbData = bufferedImage.getRaster().getDataBuffer();

		vpx_image_t _img = img.get();
		int width = _img.d_w();
		int height = _img.d_h();
		
		int rgb24Stride = width * 3;
		
		Pointer<Pointer<Byte>> planes = _img.planes();
		Pointer<Byte> imgY = planes.get(VpxLibrary.VPX_PLANE_Y);
		Pointer<Byte> imgU = planes.get(VpxLibrary.VPX_PLANE_U);
		Pointer<Byte> imgV = planes.get(VpxLibrary.VPX_PLANE_V);

		int rgb1 = 0;
		int yi1 = 0;
		int uvi = 0;
        for (int ypos = 0; ypos < height; ypos += 2)
        {
        	for (int xpos = 0; xpos < width; xpos += 2)
        	{
				int rgb2 = rgb1 + 3;
				int rgb3 = rgb24Stride + rgb1;
				int rgb4 = rgb24Stride + rgb2;
				
				int ro, go, bo;
				if (java.nio.ByteOrder.nativeOrder() == java.nio.ByteOrder.LITTLE_ENDIAN)
				{
					ro = 0;
					go = 1;
					bo = 2;
				}
				else
				{
					bo = 0;
					go = 1;
					ro = 2;
				}

				int r1 = rgbData.getElem(rgb1+ro);
				int g1 = rgbData.getElem(rgb1+go);
				int b1 = rgbData.getElem(rgb1+bo);
				int r2 = rgbData.getElem(rgb2+ro);
				int g2 = rgbData.getElem(rgb2+go);
				int b2 = rgbData.getElem(rgb2+bo);
				int r3 = rgbData.getElem(rgb3+ro);
				int g3 = rgbData.getElem(rgb3+go);
				int b3 = rgbData.getElem(rgb3+bo);
				int r4 = rgbData.getElem(rgb4+ro);
				int g4 = rgbData.getElem(rgb4+go);
				int b4 = rgbData.getElem(rgb4+bo);

				int yi2 = yi1 + 1;
				int yi3 = width + yi1;
				int yi4 = width + yi2;

				imgY.set(yi1, (byte) getY(r1, g1, b1));
				imgY.set(yi2, (byte) getY(r2, g2, b2));
				imgY.set(yi3, (byte) getY(r3, g3, b3));
				imgY.set(yi4, (byte) getY(r4, g4, b4));

				int u1 = getU(r1, g1, b1);
				int u2 = getU(r2, g2, b2);
				int u3 = getU(r3, g3, b3);
				int u4 = getU(r4, g4, b4);

				int v1 = getV(r1, g1, b1);
				int v2 = getV(r2, g2, b2);
				int v3 = getV(r3, g3, b3);
				int v4 = getV(r4, g4, b4);
            
				imgU.set(uvi, (byte) ((u1 + u2 + u3 + u4) / 4));
				imgV.set(uvi, (byte) ((v1 + v2 + v3 + v4) / 4));

				rgb1 += 6;
				yi1 += 2;
				uvi++;
			}

			rgb1 += rgb24Stride;
			yi1 += width;
        }
	}

	// Converts a VPX YUV I420 image to RGB24 data
	public static void convertI420ToRGB24(Pointer<vpx_image_t> img, BufferedImage bufferedImage)
	{
		DataBuffer rgbData = bufferedImage.getRaster().getDataBuffer();

		vpx_image_t _img = img.get();
		int width = _img.d_w();
		int height = _img.d_h();

		Pointer<Pointer<Byte>> planes = _img.planes();
		Pointer<Byte> imgY = planes.get(VpxLibrary.VPX_PLANE_Y);
		Pointer<Byte> imgU = planes.get(VpxLibrary.VPX_PLANE_U);
		Pointer<Byte> imgV = planes.get(VpxLibrary.VPX_PLANE_V);

		Pointer<Integer> stride = _img.stride();
        int yPadding = stride.get(VpxLibrary.VPX_PLANE_Y) - width;
        int uPadding = stride.get(VpxLibrary.VPX_PLANE_U) - (width / 2);
        int vPadding = stride.get(VpxLibrary.VPX_PLANE_V) - (width / 2);

        int yi = 0;
        int ui = 0;
        int vi = 0;
        int rgbi = 0;
        for (int ypos = 0; ypos < height; ypos++)
        {
        	for (int xpos = 0; xpos < width; xpos++)
        	{
				int y = imgY.get(yi) & 0xFF;
				int u = imgU.get(ui) & 0xFF;
				int v = imgV.get(vi) & 0xFF;
        
				int r = getR(y, u, v);
				int g = getG(y, u, v);
				int b = getB(y, u, v);

				if (java.nio.ByteOrder.nativeOrder() == java.nio.ByteOrder.LITTLE_ENDIAN)
				{
					rgbData.setElem(rgbi++, (byte) b);
					rgbData.setElem(rgbi++, (byte) g);
					rgbData.setElem(rgbi++, (byte) r);
				}
				else
				{
					rgbData.setElem(rgbi++, (byte) r);
					rgbData.setElem(rgbi++, (byte) g);
					rgbData.setElem(rgbi++, (byte) b);
				}

				yi++;
				if (xpos % 2 == 1)
				{
					ui++;
					vi++;
				}
			}
    
			yi += yPadding;
    
			if (ypos % 2 == 0)
			{
				ui -= (width / 2);
				vi -= (width / 2);
			}
			else
			{
				ui += uPadding;
				vi += vPadding;
			}
        }
	}
    
    private static int getR(int y, int u, int v)
    {
        return clamp(y + (int) (1.402f * (v - 128)));
    }
    
    private static int getG(int y, int u, int v)
    {
        return clamp(y - (int) (0.344f * (u - 128) + 0.714f * (v - 128)));
    }
    
    private static int getB(int y, int u, int v)
    {
        return clamp(y + (int) (1.772f * (u - 128)));
    }
    
    private static int getY(int r, int g, int b)
    {
    	return (int) (0.299f * r + 0.587f * g + 0.114f * b);
    }
    
    private static int getU(int r, int g, int b)
    {
    	return (int) (-0.169f * r - 0.331f * g + 0.499f * b + 128);
    }
    
    private static int getV(int r, int g, int b)
    {
    	return (int) (0.499f * r - 0.418f * g - 0.0813f * b + 128);
    }
    
    private static int clamp(int value)
    {
        return Math.min(Math.max(value, 0), 255);
    }
}
package sm.java.conference;

import fm.*;
import fm.icelink.webrtc.*;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.*;
import java.util.*;

import com.github.sarxos.webcam.*;

/*
 * Webcam capture library from http://webcam-capture.sarxos.pl/
 * 
 * Copyright (C) 2012 - 2013 Bartosz Firyn
 * 
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 */

public class SarXosVideoCaptureProvider extends VideoCaptureProvider
{
    private Webcam captureDevice = null;
    private String label = null;

    private WebcamPanel previewInternal = null;
    private PanelVideoRenderProvider preview = null;

    /// <summary>
    /// Initializes a new instance of the <see cref="SarXosVideoCaptureProvider"/> class.
    /// </summary>
    public SarXosVideoCaptureProvider()
    { 
        this(new PanelVideoRenderProvider());
    }

    /// <summary>
    /// Initializes a new instance of the <see cref="SarXosVideoCaptureProvider"/> class.
    /// </summary>
    /// <param name="previewScale">The scaling algorithm to use for the preview.</param>
    public SarXosVideoCaptureProvider(LayoutScale previewScale)
    { 
        this(new PanelVideoRenderProvider(previewScale));
    }

    /// <summary>
    /// Initializes a new instance of the <see cref="SarXosVideoCaptureProvider"/> class.
    /// </summary>
    /// <param name="disablePreviewContextMenu">Whether or not to disable the preview context menu.</param>
    public SarXosVideoCaptureProvider(boolean disableContextMenu)
    { 
        this(new PanelVideoRenderProvider(disableContextMenu));
    }

    /// <summary>
    /// Initializes a new instance of the <see cref="SarXosVideoCaptureProvider"/> class.
    /// </summary>
    /// <param name="disablePreviewContextMenu">Whether or not to disable the preview context menu.</param>
    /// <param name="previewScale">The scaling algorithm to use for the preview.</param>
    public SarXosVideoCaptureProvider(boolean disableContextMenu, LayoutScale previewScale)
    { 
        this(new PanelVideoRenderProvider(disableContextMenu, previewScale));
    }

    /// <summary>
    /// Initializes a new instance of the <see cref="SarXosVideoCaptureProvider"/> class.
    /// </summary>
    /// <param name="preview">The preview video render provider.</param>
    public SarXosVideoCaptureProvider(PanelVideoRenderProvider preview)
    {
        this.preview = preview;
    }
    
    public boolean getMirrorPreview()
    {
        return mirrorPreview;
    }
    public void setMirrorPreview(boolean mirrorPreview)
    {
        this.mirrorPreview = mirrorPreview;
    }
    private boolean mirrorPreview = false;
    
    /// <summary>
    /// Initializes the video capture provider.
    /// </summary>
    /// <param name="captureArgs">The arguments.</param>
    public void initialize(final VideoCaptureInitializeArgs captureArgs) throws Exception
    {
        preview.initialize(new VideoRenderInitializeArgs()
        {{
            setClockRate(captureArgs.getClockRate());
            setLocalStream(captureArgs.getLocalStream());
        }});
    }

    private boolean initializeCamera() throws Exception
    {
        Webcam webcam = getWebcam();
        if (webcam == null)
        {
            return false;
        }

        try
        {
	        captureDevice = webcam;
            captureDevice.setViewSize(new Dimension(getDesiredWidth(), getDesiredHeight()));
	        
	        previewInternal = new WebcamPanel(captureDevice, false);
            previewInternal.setFPSLimit(getDesiredFrameRate());
	        previewInternal.setFillArea(true);
	        
	        label = captureDevice.getName();
	        
        	captureDevice.addWebcamListener(new WebcamListener()
        	{
				public void webcamOpen(WebcamEvent e) { }
				public void webcamClosed(WebcamEvent e) { }
				public void webcamDisposed(WebcamEvent e) { }
				public void webcamImageObtained(WebcamEvent e)
				{
                    // get image
                    BufferedImage frame = e.getImage();
                    int width = frame.getWidth();
                    int height = frame.getHeight();
                    
                    try
                    {
                        // get buffers
                        VideoBuffer videoBuffer;
                        VideoBuffer videoPreviewBuffer;
                        boolean videoPreviewBufferIsRgb = true;
                        
                        if (getIsMuted() && getIsPreviewMuted())
                        {
                            videoBuffer = getBlackFrame(width, height);
                            videoPreviewBuffer = videoBuffer;
                        }
                        else if (getIsMuted())
                        {
                            videoBuffer = getBlackFrame(width, height);
                            
                            if (mirrorPreview)
                            {
                                videoPreviewBuffer = getMirroredVideoBuffer(frame);
                                videoPreviewBufferIsRgb = false;
                            }
                            else
                            {
                                videoPreviewBuffer = getVideoBuffer(frame);
                            }
                        }
                        else if (getIsPreviewMuted())
                        {
                            videoBuffer = getVideoBuffer(frame);
                            videoPreviewBuffer = getBlackFrame(width, height);
                        }
                        else
                        {
                            videoBuffer = getVideoBuffer(frame);
                            
                            if (mirrorPreview)
                            {
                                videoPreviewBuffer = getMirroredVideoBuffer(frame);
                                videoPreviewBufferIsRgb = false;
                            }
                            else
                            {
                                videoPreviewBuffer = videoBuffer;
                            }
                        }
                        
                        // update preview
                        preview.render(videoPreviewBuffer, videoPreviewBufferIsRgb);

                        // raise event
                        raiseFrame(videoBuffer);
                    }
                    catch (Exception ex)
                    {
                        Log.error("Could not raise frame.", ex);
                    }
				}
        	});
	        return true;
        }
        catch (Exception ex)
        {
        	ex.printStackTrace();
        	return false;
        }
    }
    
    private VideoBuffer getBlackFrame(int width, int height) throws Exception
    {
        return VideoBuffer.createBlack(width, height);
    }
    
    private VideoBuffer getMirroredVideoBuffer(BufferedImage image) throws Exception
    {
        BufferedImage mirrorImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D g = mirrorImage.createGraphics();
        g.drawImage(image, image.getWidth(), 0, 0, image.getHeight(), 0, 0, image.getWidth(), image.getHeight(), null);
        g.dispose();
        return getVideoBuffer(mirrorImage);
    }
    
    private VideoBuffer getVideoBuffer(BufferedImage image) throws Exception
    {
        WritableRaster raster = image.getRaster();
        DataBufferByte rasterData = (DataBufferByte) raster.getDataBuffer();
        byte[] data = rasterData.getData();
        return new VideoBuffer(image.getWidth(), image.getHeight(), new VideoPlane(data));
    }

    private Webcam getWebcam()
    {
        if (getDesiredDeviceNumber() != null && getDesiredDeviceNumber().hasValue())
        {
            // try get to get the desired device first
            int deviceNumber = 0;
            List<Webcam> webcams = Webcam.getWebcams();
            for (Webcam webcam : webcams)
            {
                if (deviceNumber == getDesiredDeviceNumber().getValue())
                {
                    setDeviceNumber(deviceNumber);
                    return webcam;
                }
                deviceNumber++;
            }
        }
        else
        {
            // if that fails, get the default
            Webcam wc = Webcam.getDefault();
            setDeviceNumber(0);
            return wc;
        }
        return null;
    }

    /// <summary>
    /// Starts the video capture.
    /// </summary>
    public boolean start()
    {
        try
        {
            if (!initializeCamera())
            {
                return false;
            }
            else
            {
                Log.debug("Checking if camera is open.");
                if (captureDevice.isOpen())
                {
                    Log.debug("Closing camera.");
                    captureDevice.close();
                }

                for (int i = 0; i < 10; i++)
                {
                    Log.debug("Checking if camera is locked.");
                    if (captureDevice.getLock().isLocked())
                    {
                        Log.debug(String.format("Waiting for camera lock to be released (attempt #%d).", i + 1));
                        try
                        {
                            Thread.sleep(100);
                        }
                        catch (Exception ex) { }
                    }
                    else
                    {
                        break;
                    }
                }
    
                if (captureDevice.getLock().isLocked())
                {
                    String error = "'" + label + "' is locked.";
                    preview.render(ImageUtility.bufferedImageToBuffer(getWaitingImage(new Dimension(getDesiredWidth(), getDesiredHeight()), error)));
                    Log.error(error);
                    return true;
                }
    
                try
                {
                    Log.debug("Opening camera.");
                    captureDevice.open();
                }
                catch (WebcamException wex)
                {
                    String error = "'" + label + "' could not be opened.";
                    preview.render(ImageUtility.bufferedImageToBuffer(getWaitingImage(new Dimension(getDesiredWidth(), getDesiredHeight()), error)));
                    Log.error(error, wex);
                    return true;
                }
    
                Log.debug("Starting camera preview.");
                previewInternal.start();
    
                Log.debug("Camera is ready.");
                return true;
            }
        }
        catch (Exception ex)
        {
            try
            {
                preview.render(ImageUtility.bufferedImageToBuffer(getWaitingImage(new Dimension(getDesiredWidth(), getDesiredHeight()), "Waiting for '" + label + "'...")));
            }
            catch (Exception e) { }
            Log.error("Could not start video capture.", ex);
            return false;
        }
    }

    /// <summary>
    /// Stops the video capture.
    /// </summary>
    public void stop()
    {
        try
        {
            if (previewInternal != null)
            {
                previewInternal.stop();
                previewInternal = null;
            }
            if (captureDevice != null)
            {
                captureDevice.close();
                captureDevice = null;
            }
        }
        catch (Exception ex)
        {
            Log.error("Could not stop video capture.", ex);
        }
    }

    /// <summary>
    /// Destroys this instance.
    /// </summary>
    public void destroy()
    {
        preview.destroy();
    }

    /// <summary>
    /// Gets the label of the video device.
    /// </summary>
    /// <returns></returns>
    public String getLabel()
    {
        return label;
    }

    /// <summary>
    /// Gets the video capture preview control.
    /// </summary>
    /// <returns></returns>
    public Object getPreviewControl()
    {
    	return preview.getControl();
    }
    
    public String[] getDeviceNames()
    {
        int index = 0;
        List<Webcam> webcams = Webcam.getWebcams();
        String[] names = new String[webcams.size()];
        for (Webcam webcam : webcams)
        {
            names[index] = webcam.getName();
            index++;
        }
        return names;
    }

    /// <summary>
    /// Gets the front device number. (Doesn't apply to generic Java.)
    /// </summary>
    /// <returns></returns>
    public int getFrontDeviceNumber()
    {
        return 0;
    }

    /// <summary>
    /// Gets the rear device number. (Doesn't apply to generic Java.)
    /// </summary>
    /// <returns></returns>
    public int getRearDeviceNumber()
    {
        return 0;
    }

    private static DoubleFunction<Dimension, String, BufferedImage> _createWaitingImage;
    
    /// <summary>
    /// Gets or sets the callback that creates the
    /// image shown while waiting for device images.
    /// </summary>
    public static DoubleFunction<Dimension, String, BufferedImage> getCreateWaitingImage()
    {
        return _createWaitingImage;
    }

    /// <summary>
    /// Gets or sets the callback that creates the
    /// image shown while waiting for device images.
    /// </summary>
    public static void setCreateWaitingImage(DoubleFunction<Dimension, String, BufferedImage> createWaitingImage)
    {
        _createWaitingImage = createWaitingImage;
    }

    private BufferedImage defaultCreateWaitingImage(Dimension size, String text)
    {
        BufferedImage bufferedImage = new BufferedImage(size.width, size.height, BufferedImage.TYPE_3BYTE_BGR);
        Graphics g = bufferedImage.getGraphics();
        g.setColor(Color.WHITE);
        g.drawString(text, 4, 4 + g.getFont().getSize());
        return bufferedImage;
    }
    
    private BufferedImage getWaitingImage(Dimension size, String text)
    {
        DoubleFunction<Dimension, String, BufferedImage> createWaitingImage = getCreateWaitingImage();
        if (createWaitingImage != null)
        {
            return createWaitingImage.invoke(size, text);
        }
        return defaultCreateWaitingImage(size, text);
    }
}
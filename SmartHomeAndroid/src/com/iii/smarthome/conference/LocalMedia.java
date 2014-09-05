package com.iii.smarthome.conference;

import java.util.Locale;

import android.view.*;
import fm.*;
import fm.icelink.webrtc.*;

public class LocalMedia
{
    // We're going to need both audio and video
    // for this example. We can constrain the
    // video slightly for performance benefits.
    private boolean audio = true;
    private boolean video = true;
    private int videoWidth     = 320;
    private int videoHeight    = 240;
    private int videoFrameRate = 15;
    
    private LocalMediaStream localMediaStream;
    public  LocalMediaStream getLocalMediaStream() { return localMediaStream; }

    private AndroidLayoutManager layoutManager;
    public  AndroidLayoutManager getLayoutManager() { return layoutManager; }
    
    public void start(final ViewGroup videoContainer, final SingleAction<String> callback) throws Exception
    {
        // WebRTC audio and video streams require us to first get access to
        // the local media (microphone, camera, or both).
        UserMedia.getMedia(new GetMediaArgs(audio, video)
        {{
            setVideoWidth(videoWidth);         // optional
            setVideoHeight(videoHeight);       // optional
            setVideoFrameRate(videoFrameRate); // optional
            setDefaultVideoPreviewScale(LayoutScale.Contain); // optional
            setDefaultVideoScale(LayoutScale.Contain);        // optional
            setOnFailure(new SingleAction<GetMediaFailureArgs>()
            {
                public void invoke(GetMediaFailureArgs e)
                {
                    callback.invoke(String.format(Locale.getDefault(), "Could not get media. %s", e.getException().getMessage()));
                }
            });
            setOnSuccess(new SingleAction<GetMediaSuccessArgs>()
            {
                public void invoke(GetMediaSuccessArgs e)
                {
                    try
                    {
                        // We have successfully acquired access to the local
                        // audio/video device! Grab a reference to the media.
                        // Internally, it maintains access to the local audio
                        // and video feeds coming from the device hardware.
                        localMediaStream = e.getLocalStream();

                        // This is our local video control, a View. It is
                        // constantly updated with our live video feed since
                        // we requested video above. Add it directly to the UI
                        // or use the IceLink layout manager, which we do below.
                        View localVideoControl = (View)e.getLocalVideoControl();

                        // Create an IceLink layout manager, which makes the task
                        // of arranging video controls easy. Give it a reference
                        // to a View that can be filled with video feeds.
                        layoutManager = new AndroidLayoutManager(videoContainer);

                        // Position and display the local video control on-screen
                        // by passing it to the layout manager created above.
                        layoutManager.setLocalVideoControl(localVideoControl);
    
                        callback.invoke(null);
                    }
                    catch (Exception ex)
                    {
                        ex.printStackTrace();
                    }
                }
            });
        }});
    }
    
    public void stop(final SingleAction<String> callback) throws Exception
    {
        // Clear out the layout manager.
        if (layoutManager != null)
        {
            layoutManager.removeRemoteVideoControls();
            layoutManager.unsetLocalVideoControl();
            layoutManager = null;
        }

        // Stop the local media stream.
        if (localMediaStream != null)
        {
            localMediaStream.stop();
            localMediaStream = null;
        }

        callback.invoke(null);
    }
}

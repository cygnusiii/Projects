package sm.java.conference;

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Locale;

import fm.*;
import fm.icelink.webrtc.*;

import javax.swing.*;

import sm.java.main.VideoChat;

public class LocalMedia
{
    // We're going to use both audio and video
    // for this example.
    private boolean audio          = true;
    private boolean video          = true;
    private int     videoWidth     = 320;
    private int     videoHeight    = 240;
    private int     videoFrameRate = 30;

    private LocalMediaStream localMediaStream;
    public  LocalMediaStream getLocalMediaStream() { return localMediaStream; }
    
    private LayoutManager layoutManager;
    public  LayoutManager getLayoutManager() { return layoutManager; }

    private VideoChat videoChat;
    
//    private ItemListener switchAudioDeviceListener;
//    private ItemListener switchVideoDeviceListener;
//    private SingleAction<AudioDeviceNumberChangedArgs> updateSelectedAudioDeviceEvent;
//    private SingleAction<VideoDeviceNumberChangedArgs> updateSelectedVideoDeviceEvent;
    
    public LocalMedia()
    {
//        switchAudioDeviceListener = new ItemListener() { public void itemStateChanged(ItemEvent e) { switchAudioDevice(e); } };
//        switchVideoDeviceListener = new ItemListener() { public void itemStateChanged(ItemEvent e) { switchVideoDevice(e); } };
//        updateSelectedAudioDeviceEvent = new SingleAction<AudioDeviceNumberChangedArgs>() { public void invoke(AudioDeviceNumberChangedArgs e) { updateSelectedAudioDevice(e); } };
//        updateSelectedVideoDeviceEvent = new SingleAction<VideoDeviceNumberChangedArgs>() { public void invoke(VideoDeviceNumberChangedArgs e) { updateSelectedVideoDevice(e); } };
    }
    
    public void start(final VideoChat videoChat, final SingleAction<String> callback) throws Exception
    {
        // WebRTC audio and video streams require us to first get access to
        // the local media (microphone, camera, or both).
        final LocalMedia self = this;
        UserMedia.getMedia(new GetMediaArgs(audio, video)
        {{
            // Specify a video capture provider. A video
            // render provider, audio capture provider,
            // and audio render provider are built into
            // the SDK, but Java does not offer native
            // support for video capture.
        	setVideoCaptureProvider(new SarXosVideoCaptureProvider(LayoutScale.Contain));
            setVideoWidth(videoWidth);         // optional
            setVideoHeight(videoHeight);       // optional
            setVideoFrameRate(videoFrameRate); // optional
            setDefaultVideoScale(LayoutScale.Contain); // optional
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
                        
                        // Wire up the UI.
                        self.videoChat = videoChat;
//                        EventQueue.invokeLater(new Runnable()
//                        {
//                            public void run()
//                            {
//                            	try{
//                                self.videoChat.audioDevices.setModel(new DefaultComboBoxModel<String>(localMediaStream.getAudioDeviceNames()));
//                                self.videoChat.videoDevices.setModel(new DefaultComboBoxModel<String>(localMediaStream.getVideoDeviceNames()));
//                                self.videoChat.audioDevices.addItemListener(switchAudioDeviceListener);
//                                self.videoChat.videoDevices.addItemListener(switchVideoDeviceListener);
//                                self.videoChat.audioDevices.setSelectedIndex(localMediaStream.getAudioDeviceNumber());
//                                self.videoChat.videoDevices.setSelectedIndex(localMediaStream.getVideoDeviceNumber());
//                            	}catch(Exception e){
//                            		System.err.print(e.toString());
//                            	}
//                            }
//                        });
    
                        // Keep the UI updated if devices are switched.
//                        localMediaStream.addOnAudioDeviceNumberChanged(updateSelectedAudioDeviceEvent);
//                        localMediaStream.addOnVideoDeviceNumberChanged(updateSelectedVideoDeviceEvent);
    
                        // This is our local video control, a Java component
                        // that displays video coming from the capture source.
                        Component localVideoControl = (Component)e.getLocalVideoControl();
    
                        // Create an IceLink layout manager, which makes the task
                        // of arranging video controls easy. Give it a reference
                        // to a Java component that can be filled with video feeds.
                        // Use AndroidLayoutManager for Android applications.
                        layoutManager = new LayoutManager(self.videoChat.container);
    
                        // Display the local video control.
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
    
//    private void switchAudioDevice(ItemEvent e)
//    {
//        if (videoChat.audioDevices.getSelectedIndex() >= 0)
//        {
//            localMediaStream.setAudioDeviceNumber(videoChat.audioDevices.getSelectedIndex());
//        }
//    }
//
//    private void switchVideoDevice(ItemEvent e)
//    {
//        if (videoChat.videoDevices.getSelectedIndex() >= 0)
//        {
//            localMediaStream.setVideoDeviceNumber(videoChat.videoDevices.getSelectedIndex());
//        }
//    }
//
//    private void updateSelectedAudioDevice(final AudioDeviceNumberChangedArgs e)
//    {
//        EventQueue.invokeLater(new Runnable()
//        {
//            public void run()
//            {
//                videoChat.audioDevices.setSelectedIndex(e.getDeviceNumber());
//            }
//        });
//    }
//
//    private void updateSelectedVideoDevice(final VideoDeviceNumberChangedArgs e)
//    {
//        EventQueue.invokeLater(new Runnable()
//        {
//            public void run()
//            {
//                videoChat.videoDevices.setSelectedIndex(e.getDeviceNumber());
//            }
//        });
//    }
    
    public void stop(SingleAction<String> callback) throws Exception
    {
        // Clear out the layout manager.
        if (layoutManager != null)
        {
            layoutManager.removeRemoteVideoControls();
            layoutManager.unsetLocalVideoControl();
            layoutManager = null;
        }

//        if (localMediaStream != null)
//        {
//            localMediaStream.removeOnAudioDeviceNumberChanged(updateSelectedAudioDeviceEvent);
//            localMediaStream.removeOnVideoDeviceNumberChanged(updateSelectedVideoDeviceEvent);
//        }
//
//        if (videoChat != null)
//        {
//            videoChat.audioDevices.removeItemListener(switchAudioDeviceListener);
//            videoChat.videoDevices.removeItemListener(switchVideoDeviceListener);
//            videoChat = null;
//        }

        // Stop the local media stream.
        if (localMediaStream != null)
        {
            localMediaStream.stop();
            localMediaStream = null;
        }

        callback.invoke(null);
    }
}

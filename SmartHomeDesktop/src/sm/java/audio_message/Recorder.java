package sm.java.audio_message;
import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.TargetDataLine;

public class Recorder extends Thread {
    private TargetDataLine  m_line;
    private AudioFileFormat.Type m_targetType;
    private AudioInputStream m_audioInputStream;
    private File m_outputFile;

    public Recorder(TargetDataLine line,
                     AudioFileFormat.Type targetType,
                     File file)
    {
        m_line = line;
        m_audioInputStream = new AudioInputStream(line);
        m_targetType = targetType;
        m_outputFile = file;
    }

    /** Starts the recording.
        To accomplish this, (i) the line is started and (ii) the
        thread is started.
    */
    public void start()
    {
        m_line.start();
        super.start();
    }

    /** Stops the recording.
    */
    public void stopRecording()
    {
        m_line.stop();
        m_line.close();
    }

    /** Main working method.
    */
    public void run()
    {
            try
            {
                AudioSystem.write(
                    m_audioInputStream,
                    m_targetType,
                    m_outputFile);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
    }

    private static void closeProgram()
    {
        System.out.println("Program closing.....");
        System.exit(1);
    }

    private static void out(String strMessage)
    {
        System.out.println(strMessage);
    }


}
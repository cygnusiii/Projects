package sm.java.main;

import javax.swing.*;

public class Program
{
	public static void main(String[] args)
	{
        SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				new VideoChat().setVisible(true);
			}
		});
	}
}
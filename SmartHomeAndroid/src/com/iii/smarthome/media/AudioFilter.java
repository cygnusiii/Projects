package com.iii.smarthome.media;

import java.io.File;
import java.io.FileFilter;

public class AudioFilter implements FileFilter {

	  private final String[] okFileExtensions = 
	    new String[] {"mp3"};
	 
	  public boolean accept(File file)
	  {
	    for (String extension : okFileExtensions)
	    {
	      if (file.getName().toLowerCase().endsWith(extension))
	      {
	        return true;
	      }
	    }
	    return false;
	  }
	
}

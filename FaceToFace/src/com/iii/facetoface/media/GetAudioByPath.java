package com.iii.facetoface.media;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;


public class GetAudioByPath {
	private File[] files;
	private ArrayList<HashMap<String, String>> songsList = new ArrayList<HashMap<String, String>>();
	public GetAudioByPath(String path){
		getPlayList(path);
	}
	public ArrayList<HashMap<String, String>> getSongsList(){
		return this.songsList;
	}
	public ArrayList<HashMap<String, String>> getPlayList(String path) {

		File home = new File(path);
		files = home.listFiles();
		
		if (home.listFiles(new AudioFilter()).length > 0) {

			for (File file : home.listFiles(new AudioFilter())) {
				HashMap<String, String> song = new HashMap<String, String>();
				song.put(
						"songTitle",
						file.getName().substring(0,
								(file.getName().length() - 4)));
				song.put("songPath", file.getPath());

				songsList.add(song);
			}

		}

		for (int i = 0; i < files.length; i++) {
			if ((files[i].isDirectory()))
				getPlayList(files[i].getAbsolutePath());
		}

		return songsList;
	}
}

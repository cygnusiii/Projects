package sm.java.ultility;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class FileWriter {

	public FileWriter(String fileName, byte[] datas) {
		FileOutputStream fos = null;
		ObjectOutputStream oos = null;
		try {
			fos = new FileOutputStream(fileName, true);
			oos = new ObjectOutputStream(fos);

			oos.writeObject(datas);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (oos != null) {
				try {
					oos.close();
				} catch (IOException ex) {
				}
			}
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException ex) {
				}
			}
		}

	}
}

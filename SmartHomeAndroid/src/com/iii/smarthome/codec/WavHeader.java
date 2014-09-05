package com.iii.smarthome.codec;

public class WavHeader {
	byte[] header = new byte[44];
	//The canonical WAVE format starts with the RIFF header:
//
//		0         4   ChunkID          Contains the letters "RIFF" in ASCII form
//		                               (0x52494646 big-endian form).
//		4         4   ChunkSize        36 + SubChunk2Size, or more precisely:
//		                               4 + (8 + SubChunk1Size) + (8 + SubChunk2Size)
//		                               This is the size of the rest of the chunk 
//		                               following this number.  This is the size of the 
//		                               entire file in bytes minus 8 bytes for the
//		                               two fields not included in this count:
//		                               ChunkID and ChunkSize.
//		8         4   Format           Contains the letters "WAVE"
//		                               (0x57415645 big-endian form).
//
//		The "WAVE" format consists of two subchunks: "fmt " and "data":
//		The "fmt " subchunk describes the sound data's format:
//
//		12        4   Subchunk1ID      Contains the letters "fmt "
//		                               (0x666d7420 big-endian form).
//		16        4   Subchunk1Size    16 for PCM.  This is the size of the
//		                               rest of the Subchunk which follows this number.
//		20        2   AudioFormat      PCM = 1 (i.e. Linear quantization)
//		                               Values other than 1 indicate some 
//		                               form of compression.
//		22        2   NumChannels      Mono = 1, Stereo = 2, etc.
//		24        4   SampleRate       8000, 44100, etc.
//		28        4   ByteRate         == SampleRate * NumChannels * BitsPerSample/8
//		32        2   BlockAlign       == NumChannels * BitsPerSample/8
//		                               The number of bytes for one sample including
//		                               all channels. I wonder what happens when
//		                               this number isn't an integer?
//		34        2   BitsPerSample    8 bits = 8, 16 bits = 16, etc.
//		          2   ExtraParamSize   if PCM, then doesn't exist
//		          X   ExtraParams      space for extra parameters
//
//		The "data" subchunk contains the size of the data and the actual sound:
//
//		36        4   Subchunk2ID      Contains the letters "data"
//		                               (0x64617461 big-endian form).
//		40        4   Subchunk2Size    == NumSamples * NumChannels * BitsPerSample/8
//		                               This is the number of bytes in the data.
//		                               You can also think of this as the size
//		                               of the read of the subchunk following this 
//		                               number.
//		44        *   Data             The actual sound data.
	// WavHeader(36+NumSamples*2*16/8,48000,48000*2*16/8,NumSaples*2*16/8);
	public WavHeader(int totalDataLen,int longSampleRate,int byteRate,int totalAudioLen) {
		header[0] = 'R'; // RIFF/WAVE header
		header[1] = 'I';
		header[2] = 'F';
		header[3] = 'F';
		header[4] = (byte) (totalDataLen & 0xff);
		header[5] = (byte) ((totalDataLen >> 8) & 0xff);
		header[6] = (byte) ((totalDataLen >> 16) & 0xff);
		header[7] = (byte) ((totalDataLen >> 24) & 0xff);
		header[8] = 'W';
		header[9] = 'A';
		header[10] = 'V';
		header[11] = 'E';
		header[12] = 'f'; // 'fmt ' chunk
		header[13] = 'm';
		header[14] = 't';
		header[15] = ' ';
		header[16] = 16; // 4 bytes: size of 'fmt ' chunk
		header[17] = 0;
		header[18] = 0;
		header[19] = 0;
		header[20] = 1; // format = 1
		header[21] = 0;
		header[22] = 2;//Mono 1, Stereo 2
		header[23] = 0;
		header[24] = (byte) (longSampleRate & 0xff);		//
		header[25] = (byte) ((longSampleRate >> 8) & 0xff);//    48kHz
		header[26] = (byte) ((longSampleRate >> 16) & 0xff);//
		header[27] = (byte) ((longSampleRate >> 24) & 0xff);//
		header[28] = (byte) (byteRate & 0xff);//28->32: 19200d ->4B00h
		header[29] = (byte) ((byteRate >> 8) & 0xff);
		header[30] = (byte) ((byteRate >> 16) & 0xff);
		header[31] = (byte) ((byteRate >> 24) & 0xff);
		header[32] = (byte) (2 * 16 / 8);
		header[33] = 0;
		header[34] = 16;//16 bitsPerSample
		header[35] = 0;
		header[36] = 'd';
		header[37] = 'a';
		header[38] = 't';
		header[39] = 'a';
		header[40] = (byte) (totalAudioLen & 0xff);			//
		header[41] = (byte) ((totalAudioLen >> 8) & 0xff);	//	
		header[42] = (byte) ((totalAudioLen >> 16) & 0xff);	//
		header[43] = (byte) ((totalAudioLen >> 24) & 0xff);	//
	}
	public byte[] getHeader(){
		return this.header;
	}

}

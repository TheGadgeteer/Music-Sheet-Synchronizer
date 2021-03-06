package AudioFile;

import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import javax.sound.sampled.AudioFormat;

public class WAVWriter implements Closeable {
	private String fileName, name, artist;
	private AudioFormat format;
	private RandomAccessFile raf;
	private long fileSizeIdx, dataSizeIdx;
	private int headerSize;
	
	//The Writer has to be open() before calling write()
	public WAVWriter(String fileName, AudioFormat format, String name, String artist) {
		this.fileName = fileName;
		setSongName(name);
		setArtist(artist);
		this.format = format;
	}
	
	public WAVWriter(String fileName, AudioFormat format) {
		this(fileName, format, null, null);
	}

	
	public void open() throws IOException {
		open(fileName);
	}
	
	public void open(String fileName) throws IOException {
		try {
			raf = new RandomAccessFile(fileName, "rw");
		} catch (FileNotFoundException e) {
			System.err.println("File " + fileName + "could not be created!");
			return;
		}
		//Truncate File
		raf.setLength(0);
		//Write Header Data, Format from https://de.wikipedia.org/wiki/RIFF_WAVE
		// and https://sno.phy.queensu.ca/~phil/exiftool/TagNames/RIFF.html#Info
		//One Chunk consists of: The Riff Tag, size of the data segment, the data segment
		writeStringToFile("RIFF", false);
		//FileSize, overwritten when file is closed
		fileSizeIdx = raf.getFilePointer();
		writeIntToFile(0);
		writeStringToFile("WAVE", false);
		//format Chunk
		writeStringToFile("fmt ", false);
		//Length of fmt header
		writeIntToFile(16);
		//Data Format (e.g. PCM = 0x0001, MPEG-1 Layer III (MP3) = 0x0055
		writeShortToFile((short)0x0001);
		writeShortToFile((short)format.getChannels());
		writeIntToFile((int)format.getSampleRate());
		writeIntToFile((int)format.getFrameRate());
		writeShortToFile((short)format.getFrameSize());
		writeShortToFile((short)format.getSampleSizeInBits());
		//Information Chunks
		writeStringToFile("LIST", false);
		//Write the list Size later
		long listSizeIdx = raf.getFilePointer();
		writeIntToFile(0);
		writeStringToFile("INFO", false);
		
		if (artist != null) {
			writeStringToFile("IART", false);
			writeIntToFile(artist.length() + 1);
			writeStringToFile(artist, true);
		}

		String comment = "created with WAVWriter from TheGadgeteer";
		writeStringToFile("ICMT", false);
		writeIntToFile(comment.length() + 1);
		writeStringToFile(comment, true);
		raf.write(0);
		
		if (name != null) {
			writeStringToFile("INAM", false);
			writeIntToFile(name.length() + 1);
			writeStringToFile(name, true);
			raf.write(0);
		}
		//Write the list Size
		int listSize = (int) (raf.getFilePointer() - listSizeIdx - 4);
		raf.seek(listSizeIdx);
		writeIntToFile(listSize);
		raf.seek(raf.length());
		
		//Data Chunk
		writeStringToFile("data", false);
		//dataSize: write when closing
		dataSizeIdx = raf.getFilePointer();
		writeIntToFile(0);
		headerSize = (int)raf.length();
	}
	
	@Override
	public void close() throws IOException {
		if (raf == null)
			return;
		//update FileSize and dataSize
		int fileSize = (int) raf.length();
		raf.seek(fileSizeIdx);
		writeIntToFile(fileSize);
		raf.seek(dataSizeIdx);
		writeIntToFile(fileSize - headerSize);
		raf.close();
	}
	
	public void write(byte[] b, int off, int len) throws IOException {
		raf.write(b, off, len);
	}
	
	private void writeStringToFile(String s, boolean nullTerminated) {
		byte[] b = new byte[s.length()];
		for (int i = 0; i < s.length(); ++i) {
			b[i] = (byte)s.charAt(i);
		}
		try {
			raf.write(b);
			if (nullTerminated)
				raf.write(0);
		} catch (IOException e) {
			System.err.println("Wasn't able to write into File");
		}
	}
	
	//Need own method instead of RandomAcessFile.write(int) because the Integer has to be in little endian format
	private void writeIntToFile(int i) {
		byte[] b = new byte[4];
		for (int x = 0; x < 4; ++x) {
			b[x] = (byte) (i >> (8 * x));
		}
		try {
			raf.write(b);
		} catch (IOException e) {
			System.err.println("Wasn't able to write into File");
		}
	}
	
	private void writeShortToFile(short s) {
		try {
			raf.write((byte)s);
			raf.write((byte)(s >> 8));
		} catch (IOException e) {
			System.err.println("Wasn't able to write into File");
		}
		
	}
	
	public void setFormat(AudioFormat format) {
		this.format = format;
	}
	
	public void setSongName(String name) {
		this.name = name;
	}
	
	public void setArtist(String artist) {
		this.artist = artist;
	}	
	
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
}

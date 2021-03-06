package freqProgram;

import java.awt.*;
import javax.sound.sampled.*;
import java.awt.event.*;

@SuppressWarnings("serial")
public class Frequency_Scrollbar extends Frame implements ActionListener, AdjustmentListener {
	private Button switchButton;
	private Scrollbar frequency, amplitude, height;
	private Label freqLabel, amplLabel, heightLabel;
	private SourceDataLine dataLine;
	private byte[] sampleList;
	private final int sampleRate = 44000;
	private final int sampleSizeinBits = 16;
	private final int playMS = 5000;
	
	public Frequency_Scrollbar() {
		super("Frequenz und Amplitude einstellen");
		AudioFormat af = new AudioFormat(sampleRate, sampleSizeinBits, 1, true, false);
		sampleList = new byte[sampleRate * playMS / 1000 * sampleSizeinBits / 8];
		try {
			dataLine = AudioSystem.getSourceDataLine(af);
			dataLine.open(af, sampleRate);
		} catch (LineUnavailableException e1) {
			System.err.println("Couldn't create SourceDataLine: " + e1.getMessage());
			System.exit(1);
		}
		dataLine.start();
		
		switchButton = new Button("On");
		switchButton.addActionListener(this);
		frequency = new Scrollbar(Scrollbar.HORIZONTAL, 440, 1, 1, 20001);
		amplitude = new Scrollbar(Scrollbar.HORIZONTAL, 50, 1, 0 , 101);
		height = new Scrollbar(Scrollbar.HORIZONTAL, 0, 1, -100, 100);
		frequency.addAdjustmentListener(this);
		amplitude.addAdjustmentListener(this);
		height.addAdjustmentListener(this);
		freqLabel = new Label();
		amplLabel = new Label();
		heightLabel = new Label();
		updateLabels();
		this.setLayout(new GridLayout(8,1, 10, 10));
		this.add(switchButton);
		this.add(amplitude);
		this.add(amplLabel);
		this.add(heightLabel);
		this.add(freqLabel);
		this.add(frequency);
		this.add(amplitude);
		this.add(height);
		this.add(new Label("Note: After changing a value, wait shortly until the signal was fully calculated and sounds clear"));
		
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				dataLine.close();
				dispose();
			}
		});
		
		this.setBackground(new Color(230, 230, 230));
		this.setSize(1000, 200);
		this.setLocation(300, 300);
		this.setVisible(true);
		
	}

	
	private void updateLabels() {
		freqLabel.setText("Frequency: " + frequency.getValue() + " Hz");
		amplLabel.setText("Amplitude: " + amplitude.getValue());
		heightLabel.setText("Height: " + height.getValue());;
	}
	
	private void updateSampleList(int frequency, int amplitude, int height) {
		double period = (double)sampleRate / frequency;
		double amplifier = (double)amplitude / 100d;
		double sin;
		final int valueRange = (int)(Math.pow(2,  sampleSizeinBits - 1) - 1);
		int sinVal;
		final int sampleSizeinBytes = sampleSizeinBits / 8;
		for (int i = 0; i < sampleList.length / sampleSizeinBytes; i += sampleSizeinBytes) {
			sin = Math.sin(2 * Math.PI * i / period);
			sin = sin * amplifier + height / 100;
			sinVal = limitVal((int)(sin * valueRange), -valueRange, valueRange);
			for (int x = 0; x < sampleSizeinBytes; ++x) {
				sampleList[i + x] = (byte)(sinVal >> (8 * x)); //Therefore saved in Little Endian
			}
			
		}
		boolean restart = dataLine.isRunning();
		dataLine.stop();
		dataLine.flush();
		if (restart) {
			dataLine.start();
			new PlaySound().start();
		}
	}
	
	@Override
	public void adjustmentValueChanged(AdjustmentEvent e) {
		updateLabels();
		updateSampleList(frequency.getValue(), amplitude.getValue(), height.getValue());
	}
	
	private void playSound() {
		dataLine.start();
		new PlaySound().start();
	}
	private void stopSound() {
		dataLine.stop();
		dataLine.flush();
	}
	

		
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("On")) {
			updateSampleList(frequency.getValue(), amplitude.getValue(), height.getValue());
			playSound();
			switchButton.setLabel("Off");
		} else if (e.getActionCommand().equals("Off")) {
			stopSound();
			switchButton.setLabel("On");
		}
	}
	
	
	//Threading class
	private class PlaySound extends Thread {
		public void run() {
				dataLine.write(sampleList,  0, sampleList.length);
		}
	}
	
	private int limitVal(int val, int min, int max) {
		if (val < min)
			return min;
		if (val > max)
			return max;
		return val;
	}
	//main Function so it starts itself
	public static void main(String[] args) {
		short value = 31000;
		Integer.toBinaryString(value);
		System.out.println(value + ", " + Integer.toBinaryString(value));
		System.out.println((byte)value + ", " + Integer.toBinaryString((byte)value));
		System.out.println((byte)(value >> 8) + ", " + Integer.toBinaryString((byte)(value >> 8)));
		new Frequency_Scrollbar();
	}
	
}

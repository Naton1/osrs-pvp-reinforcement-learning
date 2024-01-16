package com.runescape.music;


import com.runescape.Client;

import java.io.ByteArrayInputStream;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Receiver;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.ShortMessage;

public class JavaMidiPlayer extends Class56_Sub1 implements Receiver {
    private static Receiver receiver = null;
    private static Sequencer sequencer = null;

	public void method827(int volume, byte[] payload, int i_0_, boolean loop) {
    	if (sequencer != null) {
    		try {
    			Sequence sequence = MidiSystem.getSequence(new ByteArrayInputStream(payload));
    			sequencer.setSequence(sequence);
    			sequencer.setLoopCount(!loop ? 0 : -1);
    			method835(0, volume, -1L);
    			sequencer.start();
    		} catch (Exception exception) {
    			/* empty */
    		}
    	}
    }
    
    public void stop() {
		if (sequencer != null) {
		    sequencer.stop();
		    method838(-1L);
		}
    }
    
    public synchronized void send(MidiMessage midimessage, long l) {
    	byte[] is = midimessage.getMessage();
    	if (is.length < 3 || !method837(is[0], is[1], is[2], l))
    		receiver.send(midimessage, l);
    }

	public JavaMidiPlayer() {
		try {
		    receiver = MidiSystem.getReceiver();
		    sequencer = MidiSystem.getSequencer(false);
		    sequencer.getTransmitter().setReceiver(this);
		    sequencer.open();
		    method838(-1L);
		} catch (Exception exception) {
		    Client.method790();
		}
    }

	public void remove() {
    	if (sequencer != null) {
    		sequencer.close();
    		sequencer = null;
    	}
    	if (receiver != null) {
    		receiver.close();
    		receiver = null;
    	}
    }
    
    public void close() {
	/* empty */
    }
    
    public void method831(int i) {
		if (sequencer != null) {
		    method840(i, -1L);
		}
    }
    
    public synchronized void method830(int i, int i_2_) {
    	if (sequencer != null) {
    		method835(i_2_, i, -1L);
    	}
    }
    
    public void method836(int i, int i_5_, int i_6_, long l) {
    	try {
    		ShortMessage message = new ShortMessage();
    		message.setMessage(i, i_5_, i_6_);
    		receiver.send(message, l);
    	} catch (InvalidMidiDataException invalidmididataexception) {
			invalidmididataexception.printStackTrace();
		}
    }
    
    public void method832(int i) {
	if (i > -90)
	    stop();
    }
}

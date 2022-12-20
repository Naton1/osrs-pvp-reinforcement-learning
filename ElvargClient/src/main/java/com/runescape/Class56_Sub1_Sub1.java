package com.runescape;


import java.io.ByteArrayInputStream;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Receiver;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.ShortMessage;

final class Class56_Sub1_Sub1 extends Class56_Sub1 implements Receiver
{
    private static Receiver aReceiver1850 = null;
    private static Sequencer aSequencer1851 = null;

	public void method827(int i, byte[] is, int i_0_, boolean bool) {
    	if (aSequencer1851 != null) {
    		try {
    			Sequence sequence = MidiSystem.getSequence(new ByteArrayInputStream(is));
    			aSequencer1851.setSequence(sequence);
    			aSequencer1851.setLoopCount(!bool ? 0 : -1);
    			method835(0, i, -1L);
    			aSequencer1851.start();
    		} catch (Exception exception) {
    			/* empty */
    		}
    	}
    }
    
    public void method833() {
		if (aSequencer1851 != null) {
		    aSequencer1851.stop();
		    method838(-1L);
		}
    }
    
    public synchronized void send(MidiMessage midimessage, long l) {
    	byte[] is = midimessage.getMessage();
    	if (is.length < 3 || !method837(is[0], is[1], is[2], l))
    		aReceiver1850.send(midimessage, l);
    }

	public Class56_Sub1_Sub1() {
		try {
		    aReceiver1850 = MidiSystem.getReceiver();
		    aSequencer1851 = MidiSystem.getSequencer(false);
		    aSequencer1851.getTransmitter().setReceiver(this);
		    aSequencer1851.open();
		    method838(-1L);
		} catch (Exception exception) {
		    Client.method790();
		}
    }

	public void method828() {
    	if (aSequencer1851 != null) {
    		aSequencer1851.close();
    		aSequencer1851 = null;
    	}
    	if (aReceiver1850 != null) {
    		aReceiver1850.close();
    		aReceiver1850 = null;
    	}
    }
    
    public void close() {
	/* empty */
    }
    
    public void method831(int i) {
		if (aSequencer1851 != null) {
		    method840(i, -1L);
		}
    }
    
    public synchronized void method830(int i, int i_2_) {
    	if (aSequencer1851 != null) {
    		method835(i_2_, i, -1L);
    	}
    }
    
    public void method836(int i, int i_5_, int i_6_, long l) {
    	try {
    		ShortMessage shortmessage = new ShortMessage();
    		shortmessage.setMessage(i, i_5_, i_6_);
    		aReceiver1850.send(shortmessage, l);
    	} catch (InvalidMidiDataException invalidmididataexception) {
    		/* empty */
		}
    }
    
    public void method832(int i) {
	if (i > -90)
	    method833();
    }
}

package com.runescape.music;/* Class56_Sub1 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

import com.runescape.Client;

abstract class Class56_Sub1 extends Class56 {

	public static int[] anIntArray385 = new int[]{12800, 12800, 12800, 12800, 12800, 12800,
			12800, 12800, 12800, 12800, 12800, 12800,
			12800, 12800, 12800, 12800};

    final void method835(int i, int i_1_, long l) {
    	i_1_ = (int) ((double) i_1_ * Math.pow(0.1, (double) i * 5.0E-4) + 0.5);
    	if (i_1_ != Client.anInt1401) {
			Client.anInt1401 = i_1_;
    		for (int i_2_ = 0; i_2_ < 16; i_2_++) {
    			int i_3_ = method844(i_2_);
    			method836(i_2_ + 176, 7, i_3_ >> 7, l);
    			method836(i_2_ + 176, 39, i_3_ & 0x7f, l);
    		}
    	}
    }
    
    abstract void method836(int i, int i_4_, int i_5_, long l);
    
    final boolean method837(int i, int i_6_, int i_7_, long l) {
    	if ((i & 0xf0) == 176) {
    		if (i_6_ == 121) {
    			method836(i, i_6_, i_7_, l);
    			int i_8_ = i & 0xf;
				anIntArray385[i_8_] = 12800;
    			int i_9_ = method844(i_8_);
    			method836(i, 7, i_9_ >> 7, l);
    			method836(i, 39, i_9_ & 0x7f, l);
    			return true;
    		}
    		if (i_6_ == 7 || i_6_ == 39) {
    			int i_10_ = i & 0xf;
    			if (i_6_ == 7)
					anIntArray385[i_10_] = (anIntArray385[i_10_] & 0x7f) + (i_7_ << 7);
    			else
					anIntArray385[i_10_] = (anIntArray385[i_10_] & 0x3f80) + i_7_;
    			int i_11_ = method844(i_10_);
    			method836(i, 7, i_11_ >> 7, l);
    			method836(i, 39, i_11_ & 0x7f, l);
    			return true;
    		}
    	}
    	return false;
    }
    
    final void method838(long l) {
    	for (int i_12_ = 0; i_12_ < 16; i_12_++)
    		method836(i_12_ + 176, 123, 0, l);
    	for (int i_13_ = 0; i_13_ < 16; i_13_++)
    		method836(i_13_ + 176, 120, 0, l);
    	for (int i_14_ = 0; i_14_ < 16; i_14_++)
    		method836(i_14_ + 176, 121, 0, l);
    	for (int i_15_ = 0; i_15_ < 16; i_15_++)
    		method836(i_15_ + 176, 0, 0, l);
    	for (int i_16_ = 0; i_16_ < 16; i_16_++)
    		method836(i_16_ + 176, 32, 0, l);
    	for (int i_17_ = 0; i_17_ < 16; i_17_++)
    		method836(i_17_ + 192, 0, 0, l);
    }
    
    final void method840(int i, long l) {
		Client.anInt1401 = i;
    	for (int index1 = 0; index1 < 16; index1++)
			anIntArray385[index1] = 12800;
    	for (int index2 = 0; index2 < 16; index2++) {
    		int i_23_ = method844(index2);
    		method836(index2 + 176, 7, i_23_ >> 7, l);
    		method836(index2 + 176, 39, i_23_ & 0x7f, l);
    	}
    }
    
    private static int method844(int i) {
    	int i_32_ = anIntArray385[i];
    	i_32_ = (i_32_ * Client.anInt1401 >> 8) * i_32_;
    	return (int) (Math.sqrt((double) i_32_) + 0.5);
    }
}

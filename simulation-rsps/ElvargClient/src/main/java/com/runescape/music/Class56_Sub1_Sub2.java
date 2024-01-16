package com.runescape.music;

import com.runescape.Client;

final class Class56_Sub1_Sub2 extends Class56_Sub1 implements Runnable {
    private static Runnable_Impl1 aRunnable_Impl1_1852;
    private static boolean aBoolean1853;
    private static boolean aBoolean1854;
    private static int anInt1855;
    private static int anInt1856;
    private MusicDefinition musicdef = new MusicDefinition();
    private static int[] anIntArray1858 = new int[256];

    private static void method845(int i, int i_1_, int i_2_, int i_3_) {
        if (anIntArray1858.length <= anInt1856) {
            aRunnable_Impl1_1852.method10(anIntArray1858, anInt1856);
            anInt1856 = 0;
        }
        anIntArray1858[anInt1856++] = i_1_ - anInt1855;
        anInt1855 = i_1_;
        anIntArray1858[anInt1856++] = i_2_ << 8 | i | i_3_ << 16;
    }

    private static final void method846() {
        if (anInt1856 > 0) {
            aRunnable_Impl1_1852.method10(anIntArray1858, anInt1856);
            anInt1856 = 0;
        }
    }

    public void method836(int i, int i_4_, int i_5_, long l) {
        method845(i, (int) l, i_4_, i_5_);
    }

    public synchronized void method827(int i, byte[] payload, int i_6_, boolean bool) {
        musicdef.decode(payload);
        boolean bool_7_ = true;
        aBoolean1854 = bool;
        anInt1855 = 0;
        aRunnable_Impl1_1852.method12(false);
        method835(i_6_, i, (long) anInt1855);
        int i_8_ = musicdef.method533();
        for (int i_9_ = 0; i_9_ < i_8_; i_9_++) {
            musicdef.method526(i_9_);
            while (!musicdef.method521()) {
                musicdef.method520(i_9_);
                if (musicdef.anIntArray216[i_9_] != 0) {
                    bool_7_ = false;
                    break;
                }
                method847(i_6_ ^ 0x70, 0L, i_9_);
            }
            musicdef.method522(i_9_);
        }
        if (bool_7_) {
            if (aBoolean1854)
                throw new RuntimeException();
            method838((long) anInt1855);
            musicdef.method523();
        }
        method846();
    }

    public synchronized void method831(int i) {
        method840(i, (long) anInt1855);
        aRunnable_Impl1_1852.method10(anIntArray1858, anInt1856);
        anInt1856 = 0;
    }

    public synchronized void method832(int i) {
        if (musicdef.hasPayload()) {
            int i_11_ = anInt1855;
            int i_12_ = -200;
            int i_13_ = aRunnable_Impl1_1852.method14(-29810);
            long l = ((long) (i_11_ - (i_13_ + i_12_))
                    * (long) (musicdef.anInt213 * 1000));
            for (; ; ) {
                int i_14_ = musicdef.method536();
                int i_15_ = musicdef.anIntArray216[i_14_];
                long l_16_ = musicdef.method532(i_15_);
                if (l < l_16_)
                    break;
                while (i_15_
                        == musicdef.anIntArray216[i_14_]) {
                    musicdef.method526(i_14_);
                    method847(126, l_16_, i_14_);
                    if (musicdef.method521()) {
                        musicdef.method522(i_14_);
                        if (musicdef.method531()) {
                            if (aBoolean1854)
                                musicdef.method534(l_16_);
                            else {
                                method838
                                        ((long) (int) (l_16_
                                                / (long) ((musicdef.anInt213)
                                                * 1000)));
                                musicdef.method523();
                                method846();
                                return;
                            }
                        }
                        break;
                    }
                    musicdef.method520(i_14_);
                    musicdef.method522(i_14_);
                }
            }
            if (i > -90)
                aRunnable_Impl1_1852 = null;
            method846();
        }
    }

    public synchronized void stop() {
        aRunnable_Impl1_1852.method12(false);
        method838((long) anInt1855);
        aRunnable_Impl1_1852.method10(anIntArray1858, anInt1856);
        anInt1856 = 0;
        musicdef.method523();
    }

    public void remove() {
        synchronized (this) {
            aBoolean1853 = true;
        }
        for (; ; ) {
            synchronized (this) {
                if (!aBoolean1853)
                    break;
            }
            Client.sleep(20L);
        }
        aRunnable_Impl1_1852.method11(true);
    }

    public void run() {//thread????
        try {
            for (; ; ) {
                synchronized (this) {
                    if (aBoolean1853) {
                        aBoolean1853 = false;
                        break;
                    }
                    method832(-126);
                }
                Client.sleep(100L);
            }
        } catch (Exception exception) {
        }
    }

    private void method847(int i, long l, int i_17_) {
        int i_18_ = musicdef.method529(i_17_);
        if (i_18_ != 1) {
            if ((i_18_ & 0x80) != 0) {
                int i_19_ = (int) (l / (long) (musicdef.anInt213 * 1000));
                int i_20_ = i_18_ & 0xff;
                int i_21_ = (i_18_ & 0xffe7d5) >> 16;
                int i_22_ = (i_18_ & 0xfff6) >> 8;
                if (!method837(i_20_, i_22_, i_21_, (long) i_19_))
                    method845(i_20_, i_19_, i_22_, i_21_);
            }
        } else
            musicdef.method528();
    }

    public Class56_Sub1_Sub2(Runnable_Impl1 runnable) {
        aRunnable_Impl1_1852 = runnable;
        aRunnable_Impl1_1852.method15((byte) 96);
        aRunnable_Impl1_1852.method12(false);
        method838((long) anInt1855);
        aRunnable_Impl1_1852.method10(anIntArray1858, anInt1856);
        anInt1856 = 0;
        Thread thread = new Thread(this);
        thread.setDaemon(true);
        thread.start();
        thread.setPriority(10);
    }

    public synchronized void method830(int i, int i_23_) {
        method835(i_23_, i, (long) anInt1855);
    }
}

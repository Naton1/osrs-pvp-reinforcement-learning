package com.runescape.cache.anim;

import com.runescape.Client;
import com.runescape.io.Buffer;

public final class Frame {


    public static Frame[][] animationlist;
    public int duration;
    public FrameBase base;
    public int transformationCount;
    public int transformationIndices[];
    public int transformX[];
    public int transformY[];
    public int transformZ[];

    public static void load(int file, byte[] array) {
        try {
            final Buffer ay = new Buffer(array);
            final FrameBase b2 = new FrameBase(ay);
            final int n = ay.readUShort();
            ;
            animationlist[file] = new Frame[n * 3];
            final int[] array2 = new int[500];
            final int[] array3 = new int[500];
            final int[] array4 = new int[500];
            final int[] array5 = new int[500];
            for (int j = 0; j < n; ++j) {
                final int k = ay.readUShort();
                ;
                final Frame[] array6 = animationlist[file];
                final int n2 = k;
                final Frame q = new Frame();
                array6[n2] = q;
                final Frame q2 = q;
                q.base = b2;
                final int f = ay.readUnsignedByte();
                int c2 = 0;
                int n3 = -1;
                for (int l = 0; l < f; ++l) {
                    final int f2;
                    if ((f2 = ay.readUnsignedByte()) > 0) {
                        if (b2.transformationType[l] != 0) {
                            for (int n4 = l - 1; n4 > n3; --n4) {
                                if (b2.transformationType[n4] == 0) {
                                    array2[c2] = n4;
                                    array3[c2] = 0;
                                    array5[c2] = (array4[c2] = 0);
                                    ++c2;
                                    break;
                                }
                            }
                        }
                        array2[c2] = l;
                        int n4 = 0;
                        if (b2.transformationType[l] == 3) {
                            n4 = 128;
                        }
                        if ((f2 & 0x1) != 0x0) {
                            array3[c2] = ay.readShort2();
                        } else {
                            array3[c2] = n4;
                        }
                        if ((f2 & 0x2) != 0x0) {
                            array4[c2] = ay.readShort2();
                        } else {
                            array4[c2] = n4;
                        }
                        if ((f2 & 0x4) != 0x0) {
                            array5[c2] = ay.readShort2();
                        } else {
                            array5[c2] = n4;
                        }
                        n3 = l;
                        ++c2;
                    }
                }
                q2.transformationCount = c2;
                q2.transformationIndices = new int[c2];
                q2.transformX = new int[c2];
                q2.transformY = new int[c2];
                q2.transformZ = new int[c2];
                for (int l = 0; l < c2; ++l) {
                    q2.transformationIndices[l] = array2[l];
                    q2.transformX[l] = array3[l];
                    q2.transformY[l] = array4[l];
                    q2.transformZ[l] = array5[l];
                }
            }
        } catch (Exception ex) {

            //ex.printStackTrace();
        }
    }

    public static Frame method531(int frame) {
        try {

            int file = frame >> 16;
            int k = frame & 0xffff;

            if (animationlist[file] == null || animationlist[file].length == 0) {
                Client.instance.resourceProvider.provide(1, file);
                return null;
            }

            return animationlist[file][k];
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
    
    public static boolean noAnimationInProgress(int frame) {
        return frame == -1;
    }

    public static void clear() {
        animationlist = null;
    }
}

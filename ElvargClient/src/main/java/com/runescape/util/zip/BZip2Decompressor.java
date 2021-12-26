package com.runescape.util.zip;

public final class BZip2Decompressor {
    private static final BZip2Header header = new BZip2Header();

    public static int method225(byte abyte0[], int i, byte abyte1[], int j, int k) {
        synchronized (header) {
            header.aByteArray563 = abyte1;
            header.anInt564 = k;
            header.aByteArray568 = abyte0;
            header.anInt569 = 0;
            header.anInt565 = j;
            header.anInt570 = i;
            header.anInt577 = 0;
            header.anInt576 = 0;
            header.anInt566 = 0;
            header.anInt567 = 0;
            header.anInt571 = 0;
            header.anInt572 = 0;
            header.anInt579 = 0;
            method227(header);
            i -= header.anInt570;
            return i;
        }
    }

    private static void method226(BZip2Header header) {
        byte byte4 = header.aByte573;
        int i = header.anInt574;
        int j = header.anInt584;
        int k = header.anInt582;
        int ai[] = BZip2Header.anIntArray587;
        int l = header.anInt581;
        byte abyte0[] = header.aByteArray568;
        int i1 = header.anInt569;
        int j1 = header.anInt570;
        int k1 = j1;
        int l1 = header.anInt601 + 1;
        label0:
        do {
            if (i > 0) {
                do {
                    if (j1 == 0)
                        break label0;
                    if (i == 1)
                        break;
                    abyte0[i1] = byte4;
                    i--;
                    i1++;
                    j1--;
                }
                while (true);
                if (j1 == 0) {
                    i = 1;
                    break;
                }
                abyte0[i1] = byte4;
                i1++;
                j1--;
            }
            boolean flag = true;
            while (flag) {
                flag = false;
                if (j == l1) {
                    i = 0;
                    break label0;
                }
                byte4 = (byte) k;
                l = ai[l];
                byte byte0 = (byte) (l & 0xff);
                l >>= 8;
                j++;
                if (byte0 != k) {
                    k = byte0;
                    if (j1 == 0) {
                        i = 1;
                    } else {
                        abyte0[i1] = byte4;
                        i1++;
                        j1--;
                        flag = true;
                        continue;
                    }
                    break label0;
                }
                if (j != l1)
                    continue;
                if (j1 == 0) {
                    i = 1;
                    break label0;
                }
                abyte0[i1] = byte4;
                i1++;
                j1--;
                flag = true;
            }
            i = 2;
            l = ai[l];
            byte byte1 = (byte) (l & 0xff);
            l >>= 8;
            if (++j != l1)
                if (byte1 != k) {
                    k = byte1;
                } else {
                    i = 3;
                    l = ai[l];
                    byte byte2 = (byte) (l & 0xff);
                    l >>= 8;
                    if (++j != l1)
                        if (byte2 != k) {
                            k = byte2;
                        } else {
                            l = ai[l];
                            byte byte3 = (byte) (l & 0xff);
                            l >>= 8;
                            j++;
                            i = (byte3 & 0xff) + 4;
                            l = ai[l];
                            k = (byte) (l & 0xff);
                            l >>= 8;
                            j++;
                        }
                }
        }
        while (true);
        int i2 = header.anInt571;
        header.anInt571 += k1 - j1;
        if (header.anInt571 < i2)
            header.anInt572++;
        header.aByte573 = byte4;
        header.anInt574 = i;
        header.anInt584 = j;
        header.anInt582 = k;
        BZip2Header.anIntArray587 = ai;
        header.anInt581 = l;
        header.aByteArray568 = abyte0;
        header.anInt569 = i1;
        header.anInt570 = j1;
    }

    private static void method227(BZip2Header class32) {
        int k8 = 0;
        int ai[] = null;
        int ai1[] = null;
        int ai2[] = null;
        class32.anInt578 = 1;
        if (BZip2Header.anIntArray587 == null)
            BZip2Header.anIntArray587 = new int[class32.anInt578 * 0x186a0];
        boolean flag19 = true;
        while (flag19) {
            byte byte0 = method228(class32);
            if (byte0 == 23)
                return;
            byte0 = method228(class32);
            byte0 = method228(class32);
            byte0 = method228(class32);
            byte0 = method228(class32);
            byte0 = method228(class32);
            class32.anInt579++;
            byte0 = method228(class32);
            byte0 = method228(class32);
            byte0 = method228(class32);
            byte0 = method228(class32);
            byte0 = method229(class32);
            class32.aBoolean575 = byte0 != 0;
            if (class32.aBoolean575)
                System.out.println("PANIC! RANDOMISED BLOCK!");
            class32.anInt580 = 0;
            byte0 = method228(class32);
            class32.anInt580 = class32.anInt580 << 8 | byte0 & 0xff;
            byte0 = method228(class32);
            class32.anInt580 = class32.anInt580 << 8 | byte0 & 0xff;
            byte0 = method228(class32);
            class32.anInt580 = class32.anInt580 << 8 | byte0 & 0xff;
            for (int j = 0; j < 16; j++) {
                byte byte1 = method229(class32);
                class32.aBooleanArray590[j] = byte1 == 1;
            }

            for (int k = 0; k < 256; k++)
                class32.aBooleanArray589[k] = false;

            for (int l = 0; l < 16; l++)
                if (class32.aBooleanArray590[l]) {
                    for (int i3 = 0; i3 < 16; i3++) {
                        byte byte2 = method229(class32);
                        if (byte2 == 1)
                            class32.aBooleanArray589[(l << 4) + i3] = true;
                    }
                }

            method231(class32);
            int i4 = class32.anInt588 + 2;
            int j4 = method230(3, class32);
            int k4 = method230(15, class32);
            for (int i1 = 0; i1 < k4; i1++) {
                int j3 = 0;
                do {
                    byte byte3 = method229(class32);
                    if (byte3 == 0)
                        break;
                    j3++;
                }
                while (true);
                class32.aByteArray595[i1] = (byte) j3;
            }

            byte abyte0[] = new byte[6];
            for (byte byte16 = 0; byte16 < j4; byte16++)
                abyte0[byte16] = byte16;

            for (int j1 = 0; j1 < k4; j1++) {
                byte byte17 = class32.aByteArray595[j1];
                byte byte15 = abyte0[byte17];
                for (; byte17 > 0; byte17--)
                    abyte0[byte17] = abyte0[byte17 - 1];

                abyte0[0] = byte15;
                class32.aByteArray594[j1] = byte15;
            }

            for (int k3 = 0; k3 < j4; k3++) {
                int l6 = method230(5, class32);
                for (int k1 = 0; k1 < i4; k1++) {
                    do {
                        byte byte4 = method229(class32);
                        if (byte4 == 0)
                            break;
                        byte4 = method229(class32);
                        if (byte4 == 0)
                            l6++;
                        else
                            l6--;
                    }
                    while (true);
                    class32.aByteArrayArray596[k3][k1] = (byte) l6;
                }
            }

            for (int l3 = 0; l3 < j4; l3++) {
                byte byte8 = 32;
                int i = 0;
                for (int l1 = 0; l1 < i4; l1++) {
                    if (class32.aByteArrayArray596[l3][l1] > i)
                        i = class32.aByteArrayArray596[l3][l1];
                    if (class32.aByteArrayArray596[l3][l1] < byte8)
                        byte8 = class32.aByteArrayArray596[l3][l1];
                }

                method232(class32.anIntArrayArray597[l3], class32.anIntArrayArray598[l3], class32.anIntArrayArray599[l3], class32.aByteArrayArray596[l3], byte8, i, i4);
                class32.anIntArray600[l3] = byte8;
            }

            int l4 = class32.anInt588 + 1;
            int i5 = -1;
            int j5 = 0;
            for (int i2 = 0; i2 <= 255; i2++)
                class32.anIntArray583[i2] = 0;

            int j9 = 4095;
            for (int l8 = 15; l8 >= 0; l8--) {
                for (int i9 = 15; i9 >= 0; i9--) {
                    class32.aByteArray592[j9] = (byte) ((l8 << 4) + i9);
                    j9--;
                }

                class32.anIntArray593[l8] = j9 + 1;
            }

            int i6 = 0;
            if (j5 == 0) {
                i5++;
                j5 = 50;
                byte byte12 = class32.aByteArray594[i5];
                k8 = class32.anIntArray600[byte12];
                ai = class32.anIntArrayArray597[byte12];
                ai2 = class32.anIntArrayArray599[byte12];
                ai1 = class32.anIntArrayArray598[byte12];
            }
            j5--;
            int i7 = k8;
            int l7;
            byte byte9;
            for (l7 = method230(i7, class32); l7 > ai[i7]; l7 = l7 << 1 | byte9) {
                i7++;
                byte9 = method229(class32);
            }

            for (int k5 = ai2[l7 - ai1[i7]]; k5 != l4; )
                if (k5 == 0 || k5 == 1) {
                    int j6 = -1;
                    int k6 = 1;
                    do {
                        if (k5 == 0)
                            j6 += k6;
                        else if (k5 == 1)
                            j6 += k6 << 1;
                        k6 <<= 1;
                        if (j5 == 0) {
                            i5++;
                            j5 = 50;
                            byte byte13 = class32.aByteArray594[i5];
                            k8 = class32.anIntArray600[byte13];
                            ai = class32.anIntArrayArray597[byte13];
                            ai2 = class32.anIntArrayArray599[byte13];
                            ai1 = class32.anIntArrayArray598[byte13];
                        }
                        j5--;
                        int j7 = k8;
                        int i8;
                        byte byte10;
                        for (i8 = method230(j7, class32); i8 > ai[j7]; i8 = i8 << 1 | byte10) {
                            j7++;
                            byte10 = method229(class32);
                        }

                        k5 = ai2[i8 - ai1[j7]];
                    }
                    while (k5 == 0 || k5 == 1);
                    j6++;
                    byte byte5 = class32.aByteArray591[class32.aByteArray592[class32.anIntArray593[0]] & 0xff];
                    class32.anIntArray583[byte5 & 0xff] += j6;
                    for (; j6 > 0; j6--) {
                        BZip2Header.anIntArray587[i6] = byte5 & 0xff;
                        i6++;
                    }
                } else {
                    int j11 = k5 - 1;
                    byte byte6;
                    if (j11 < 16) {
                        int j10 = class32.anIntArray593[0];
                        byte6 = class32.aByteArray592[j10 + j11];
                        for (; j11 > 3; j11 -= 4) {
                            int k11 = j10 + j11;
                            class32.aByteArray592[k11] = class32.aByteArray592[k11 - 1];
                            class32.aByteArray592[k11 - 1] = class32.aByteArray592[k11 - 2];
                            class32.aByteArray592[k11 - 2] = class32.aByteArray592[k11 - 3];
                            class32.aByteArray592[k11 - 3] = class32.aByteArray592[k11 - 4];
                        }

                        for (; j11 > 0; j11--)
                            class32.aByteArray592[j10 + j11] = class32.aByteArray592[(j10 + j11) - 1];

                        class32.aByteArray592[j10] = byte6;
                    } else {
                        int l10 = j11 >> 4;
                        int i11 = j11 & 15;
                        int k10 = class32.anIntArray593[l10] + i11;
                        byte6 = class32.aByteArray592[k10];
                        for (; k10 > class32.anIntArray593[l10]; k10--)
                            class32.aByteArray592[k10] = class32.aByteArray592[k10 - 1];

                        class32.anIntArray593[l10]++;
                        for (; l10 > 0; l10--) {
                            class32.anIntArray593[l10]--;
                            class32.aByteArray592[class32.anIntArray593[l10]] = class32.aByteArray592[(class32.anIntArray593[l10 - 1] + 16) - 1];
                        }

                        class32.anIntArray593[0]--;
                        class32.aByteArray592[class32.anIntArray593[0]] = byte6;
                        if (class32.anIntArray593[0] == 0) {
                            int i10 = 4095;
                            for (int k9 = 15; k9 >= 0; k9--) {
                                for (int l9 = 15; l9 >= 0; l9--) {
                                    class32.aByteArray592[i10] = class32.aByteArray592[class32.anIntArray593[k9] + l9];
                                    i10--;
                                }

                                class32.anIntArray593[k9] = i10 + 1;
                            }
                        }
                    }
                    class32.anIntArray583[class32.aByteArray591[byte6 & 0xff] & 0xff]++;
                    BZip2Header.anIntArray587[i6] = class32.aByteArray591[byte6 & 0xff] & 0xff;
                    i6++;
                    if (j5 == 0) {
                        i5++;
                        j5 = 50;
                        byte byte14 = class32.aByteArray594[i5];
                        k8 = class32.anIntArray600[byte14];
                        ai = class32.anIntArrayArray597[byte14];
                        ai2 = class32.anIntArrayArray599[byte14];
                        ai1 = class32.anIntArrayArray598[byte14];
                    }
                    j5--;
                    int k7 = k8;
                    int j8;
                    byte byte11;
                    for (j8 = method230(k7, class32); j8 > ai[k7]; j8 = j8 << 1 | byte11) {
                        k7++;
                        byte11 = method229(class32);
                    }

                    k5 = ai2[j8 - ai1[k7]];
                }

            class32.anInt574 = 0;
            class32.aByte573 = 0;
            class32.anIntArray585[0] = 0;
            for (int j2 = 1; j2 <= 256; j2++)
                class32.anIntArray585[j2] = class32.anIntArray583[j2 - 1];

            for (int k2 = 1; k2 <= 256; k2++)
                class32.anIntArray585[k2] += class32.anIntArray585[k2 - 1];

            for (int l2 = 0; l2 < i6; l2++) {
                byte byte7 = (byte) (BZip2Header.anIntArray587[l2] & 0xff);
                BZip2Header.anIntArray587[class32.anIntArray585[byte7 & 0xff]] |= l2 << 8;
                class32.anIntArray585[byte7 & 0xff]++;
            }

            class32.anInt581 = BZip2Header.anIntArray587[class32.anInt580] >> 8;
            class32.anInt584 = 0;
            class32.anInt581 = BZip2Header.anIntArray587[class32.anInt581];
            class32.anInt582 = (byte) (class32.anInt581 & 0xff);
            class32.anInt581 >>= 8;
            class32.anInt584++;
            class32.anInt601 = i6;
            method226(class32);
            flag19 = class32.anInt584 == class32.anInt601 + 1 && class32.anInt574 == 0;
        }
    }

    private static byte method228(BZip2Header header) {
        return (byte) method230(8, header);
    }

    private static byte method229(BZip2Header header) {
        return (byte) method230(1, header);
    }

    private static int method230(int i, BZip2Header header) {
        int j;
        do {
            if (header.anInt577 >= i) {
                int k = header.anInt576 >> header.anInt577 - i & (1 << i) - 1;
                header.anInt577 -= i;
                j = k;
                break;
            }
            header.anInt576 = header.anInt576 << 8 | header.aByteArray563[header.anInt564] & 0xff;
            header.anInt577 += 8;
            header.anInt564++;
            header.anInt565--;
            header.anInt566++;
            if (header.anInt566 == 0)
                header.anInt567++;
        }
        while (true);
        return j;
    }

    private static void method231(BZip2Header header) {
        header.anInt588 = 0;
        for (int i = 0; i < 256; i++)
            if (header.aBooleanArray589[i]) {
                header.aByteArray591[header.anInt588] = (byte) i;
                header.anInt588++;
            }
    }

    private static void method232(int ai[], int ai1[], int ai2[], byte abyte0[], int i, int j, int k) {
        int l = 0;
        for (int i1 = i; i1 <= j; i1++) {
            for (int l2 = 0; l2 < k; l2++)
                if (abyte0[l2] == i1) {
                    ai2[l] = l2;
                    l++;
                }
        }

        for (int j1 = 0; j1 < 23; j1++)
            ai1[j1] = 0;

        for (int k1 = 0; k1 < k; k1++)
            ai1[abyte0[k1] + 1]++;

        for (int l1 = 1; l1 < 23; l1++)
            ai1[l1] += ai1[l1 - 1];

        for (int i2 = 0; i2 < 23; i2++)
            ai[i2] = 0;

        int i3 = 0;
        for (int j2 = i; j2 <= j; j2++) {
            i3 += ai1[j2 + 1] - ai1[j2];
            ai[j2] = i3 - 1;
            i3 <<= 1;
        }

        for (int k2 = i + 1; k2 <= j; k2++)
            ai1[k2] = (ai[k2 - 1] + 1 << 1) - ai1[k2];
    }
}

package com.runescape.util;

import com.runescape.cache.FileArchive;
import com.runescape.io.Buffer;

public final class MessageCensor {

    private static final String[] exceptions = {"cook", "cook's", "cooks",
            "seeks", "sheet", "woop", "woops", "faq", "noob", "noobs"};
    private static int[] fragments;
    private static char[][] badWords;
    private static byte[][][] badEncoding;
    private static char[][] domains;
    private static char[][] tldList;
    private static int[] tlds;

    public static void load(FileArchive archive) {
        Buffer fragments = new Buffer(archive.readFile("fragmentsenc.txt"));
        Buffer bad = new Buffer(archive.readFile("badenc.txt"));
        Buffer domain = new Buffer(archive.readFile("domainenc.txt"));
        Buffer tldlist = new Buffer(archive.readFile("tldlist.txt"));
        decode(fragments, bad, domain, tldlist);
    }

    private static void decode(Buffer fragments, Buffer badenc,
                               Buffer domainenc, Buffer tldlist) {
        decodeBadEnc(badenc);
        decodeDomainEnc(domainenc);
        decodeFragmentsEnc(fragments);
        decodeTldList(tldlist);
    }

    private static void decodeTldList(Buffer stream) {
        int i = stream.readInt();
        tldList = new char[i][];
        tlds = new int[i];
        for (int j = 0; j < i; j++) {
            tlds[j] = stream.readUnsignedByte();
            char[] ac = new char[stream.readUnsignedByte()];
            for (int k = 0; k < ac.length; k++)
                ac[k] = (char) stream.readUnsignedByte();

            tldList[j] = ac;
        }

    }

    private static void decodeBadEnc(Buffer stream) {
        int j = stream.readInt();
        badWords = new char[j][];
        badEncoding = new byte[j][][];
        initBad(stream, badWords, badEncoding);
    }

    private static void decodeDomainEnc(Buffer stream) {
        int i = stream.readInt();
        domains = new char[i][];
        initDomains(domains, stream);
    }

    private static void decodeFragmentsEnc(Buffer stream) {
        fragments = new int[stream.readInt()];
        for (int i = 0; i < fragments.length; i++)
            fragments[i] = stream.readUShort();
    }

    private static void initBad(Buffer stream, char[][] words,
                                byte[][][] complexBadEnc) {
        for (int index = 0; index < words.length; index++) {
            char[] word = new char[stream.readUnsignedByte()];
            for (int character = 0; character < word.length; character++)
                word[character] = (char) stream.readUnsignedByte();

            words[index] = word;
            byte[][] complex = new byte[stream.readUnsignedByte()][2];
            for (int l = 0; l < complex.length; l++) {
                complex[l][0] = (byte) stream.readUnsignedByte();
                complex[l][1] = (byte) stream.readUnsignedByte();
            }

            if (complex.length > 0)
                complexBadEnc[index] = complex;
        }

    }

    private static void initDomains(char[][] domains, Buffer stream) {
        for (int index = 0; index < domains.length; index++) {
            char[] domain = new char[stream.readUnsignedByte()];
            for (int k = 0; k < domain.length; k++)
                domain[k] = (char) stream.readUnsignedByte();

            domains[index] = domain;
        }

    }

    private static int method503(char[] ac, char[] ac1, int j) {
        if (j == 0)
            return 2;
        for (int k = j - 1; k >= 0; k--) {
            if (!isNotAlphanumeric(ac[k]))
                break;
            if (ac[k] == '@')
                return 3;
        }

        int l = 0;
        for (int i1 = j - 1; i1 >= 0; i1--) {
            if (!isNotAlphanumeric(ac1[i1]))
                break;
            if (ac1[i1] == '*')
                l++;
        }

        if (l >= 3)
            return 4;
        return !isNotAlphanumeric(ac[j - 1]) ? 0 : 1;
    }

    private static int method504(char[] ac, int i, char[] ac1) {
        if (i + 1 == ac1.length)
            return 2;
        for (int j = i + 1; j < ac1.length; j++) {
            if (!isNotAlphanumeric(ac1[j]))
                break;
            if (ac1[j] == '.' || ac1[j] == ',')
                return 3;
        }
        int k = 0;
        for (int l = i + 1; l < ac1.length; l++) {
            if (!isNotAlphanumeric(ac[l]))
                break;
            if (ac[l] == '*')
                k++;
        }

        if (k >= 3)
            return 4;
        return !isNotAlphanumeric(ac1[i + 1]) ? 0 : 1;
    }

    private static int method508(char[] ac, char[] ac1, int i) {
        if (i + 1 == ac.length)
            return 2;
        for (int j = i + 1; j < ac.length; j++) {
            if (!isNotAlphanumeric(ac[j]))
                break;
            if (ac[j] == '\\' || ac[j] == '/')
                return 3;
        }

        int k = 0;
        for (int l = i + 1; l < ac.length; l++) {
            if (!isNotAlphanumeric(ac1[l]))
                break;
            if (ac1[l] == '*')
                k++;
        }

        if (k >= 5)
            return 4;
        return !isNotAlphanumeric(ac[i + 1]) ? 0 : 1;
    }

    private static void method509(byte[][] abyte0, char[] ac, char[] ac1) {
        if (ac1.length > ac.length)
            return;
        int j;
        for (int k = 0; k <= ac.length - ac1.length; k += j) {
            int l = k;
            int i1 = 0;
            int j1 = 0;
            j = 1;
            boolean flag1 = false;
            boolean flag2 = false;
            boolean flag3 = false;
            while (l < ac.length && (!flag2 || !flag3)) {
                int k1;
                char c = ac[l];
                char c2 = '\0';
                if (l + 1 < ac.length)
                    c2 = ac[l + 1];
                if (i1 < ac1.length && (k1 = method512(c2, c, ac1[i1])) > 0) {
                    if (k1 == 1 && isDigit(c))
                        flag2 = true;
                    if (k1 == 2 && (isDigit(c) || isDigit(c2)))
                        flag2 = true;
                    l += k1;
                    i1++;
                    continue;
                }
                if (i1 == 0)
                    break;
                if ((k1 = method512(c2, c, ac1[i1 - 1])) > 0) {
                    l += k1;
                    if (i1 == 1)
                        j++;
                    continue;
                }
                if (i1 >= ac1.length || !method518(c))
                    break;
                if (isNotAlphanumeric(c) && c != '\'')
                    flag1 = true;
                if (isDigit(c))
                    flag3 = true;
                l++;
                if ((++j1 * 100) / (l - k) > 90)
                    break;
            }
            if (i1 >= ac1.length && (!flag2 || !flag3)) {
                boolean flag4 = true;
                if (!flag1) {
                    char c1 = ' ';
                    if (k - 1 >= 0)
                        c1 = ac[k - 1];
                    char c3 = ' ';
                    if (l < ac.length)
                        c3 = ac[l];
                    byte byte0 = method513(c1);
                    byte byte1 = method513(c3);
                    if (abyte0 != null && method510(byte0, abyte0, byte1))
                        flag4 = false;
                } else {
                    boolean flag5 = false;
                    boolean flag6 = false;
                    if (k - 1 < 0 || isNotAlphanumeric(ac[k - 1])
                            && ac[k - 1] != '\'')
                        flag5 = true;
                    if (l >= ac.length || isNotAlphanumeric(ac[l])
                            && ac[l] != '\'')
                        flag6 = true;
                    if (!flag5 || !flag6) {
                        boolean flag7 = false;
                        int k2 = k - 2;
                        if (flag5)
                            k2 = k;
                        for (; !flag7 && k2 < l; k2++)
                            if (k2 >= 0
                                    && (!isNotAlphanumeric(ac[k2]) || ac[k2] == '\'')) {
                                char[] ac2 = new char[3];
                                int j3;
                                for (j3 = 0; j3 < 3; j3++) {
                                    if (k2 + j3 >= ac.length
                                            || isNotAlphanumeric(ac[k2 + j3])
                                            && ac[k2 + j3] != '\'')
                                        break;
                                    ac2[j3] = ac[k2 + j3];
                                }

                                boolean flag8 = j3 != 0;
                                if (j3 < 3
                                        && k2 - 1 >= 0
                                        && (!isNotAlphanumeric(ac[k2 - 1]) || ac[k2 - 1] == '\''))
                                    flag8 = false;
                                if (flag8 && !containsFragment(ac2))
                                    flag7 = true;
                            }

                        if (!flag7)
                            flag4 = false;
                    }
                }
                if (flag4) {
                    int l1 = 0;
                    int i2 = 0;
                    int j2 = -1;
                    for (int l2 = k; l2 < l; l2++)
                        if (isDigit(ac[l2]))
                            l1++;
                        else if (isLetter(ac[l2])) {
                            i2++;
                            j2 = l2;
                        }

                    if (j2 > -1)
                        l1 -= l - 1 - j2;
                    if (l1 <= i2) {
                        for (int i3 = k; i3 < l; i3++)
                            ac[i3] = '*';

                    } else {
                        j = 1;
                    }
                }
            }
        }

    }

    private static boolean method510(byte byte0, byte[][] abyte0, byte byte2) {
        int i = 0;
        if (abyte0[i][0] == byte0 && abyte0[i][1] == byte2)
            return true;
        int j = abyte0.length - 1;
        if (abyte0[j][0] == byte0 && abyte0[j][1] == byte2)
            return true;
        do {
            int k = (i + j) / 2;
            if (abyte0[k][0] == byte0 && abyte0[k][1] == byte2)
                return true;
            if (byte0 < abyte0[k][0] || byte0 == abyte0[k][0]
                    && byte2 < abyte0[k][1])
                j = k;
            else
                i = k;
        } while (i != j && i + 1 != j);
        return false;
    }

    private static int method511(char c, char c1, char c2) {
        if (c1 == c)
            return 1;
        if (c1 == 'o' && c == '0')
            return 1;
        if (c1 == 'o' && c == '(' && c2 == ')')
            return 2;
        if (c1 == 'c' && (c == '(' || c == '<' || c == '['))
            return 1;
        if (c1 == 'e' && c == '\u20AC')
            return 1;
        if (c1 == 's' && c == '$')
            return 1;
        return c1 != 'l' || c != 'i' ? 0 : 1;
    }

    private static int method512(char c, char c1, char c2) {
        if (c2 == c1)
            return 1;
        if (c2 >= 'a' && c2 <= 'm') {
            if (c2 == 'a') {
                if (c1 == '4' || c1 == '@' || c1 == '^')
                    return 1;
                return c1 != '/' || c != '\\' ? 0 : 2;
            }
            if (c2 == 'b') {
                if (c1 == '6' || c1 == '8')
                    return 1;
                return (c1 != '1' || c != '3') && (c1 != 'i' || c != '3') ? 0
                        : 2;
            }
            if (c2 == 'c')
                return c1 != '(' && c1 != '<' && c1 != '{' && c1 != '[' ? 0 : 1;
            if (c2 == 'd')
                return (c1 != '[' || c != ')') && (c1 != 'i' || c != ')') ? 0
                        : 2;
            if (c2 == 'e')
                return c1 != '3' && c1 != '\u20AC' ? 0 : 1;
            if (c2 == 'f') {
                if (c1 == 'p' && c == 'h')
                    return 2;
                return c1 != '\243' ? 0 : 1;
            }
            if (c2 == 'g')
                return c1 != '9' && c1 != '6' && c1 != 'q' ? 0 : 1;
            if (c2 == 'h')
                return c1 != '#' ? 0 : 1;
            if (c2 == 'i')
                return c1 != 'y' && c1 != 'l' && c1 != 'j' && c1 != '1'
                        && c1 != '!' && c1 != ':' && c1 != ';' && c1 != '|' ? 0
                        : 1;
            if (c2 == 'j')
                return 0;
            if (c2 == 'k')
                return 0;
            if (c2 == 'l')
                return c1 != '1' && c1 != '|' && c1 != 'i' ? 0 : 1;
            if (c2 == 'm')
                return 0;
        }
        if (c2 >= 'n' && c2 <= 'z') {
            if (c2 == 'n')
                return 0;
            if (c2 == 'o') {
                if (c1 == '0' || c1 == '*')
                    return 1;
                return (c1 != '(' || c != ')') && (c1 != '[' || c != ']')
                        && (c1 != '{' || c != '}') && (c1 != '<' || c != '>') ? 0
                        : 2;
            }
            if (c2 == 'p')
                return 0;
            if (c2 == 'q')
                return 0;
            if (c2 == 'r')
                return 0;
            if (c2 == 's')
                return c1 != '5' && c1 != 'z' && c1 != '$' && c1 != '2' ? 0 : 1;
            if (c2 == 't')
                return c1 != '7' && c1 != '+' ? 0 : 1;
            if (c2 == 'u') {
                if (c1 == 'v')
                    return 1;
                return (c1 != '\\' || c != '/') && (c1 != '\\' || c != '|')
                        && (c1 != '|' || c != '/') ? 0 : 2;
            }
            if (c2 == 'v')
                return (c1 != '\\' || c != '/') && (c1 != '\\' || c != '|')
                        && (c1 != '|' || c != '/') ? 0 : 2;
            if (c2 == 'w')
                return c1 != 'v' || c != 'v' ? 0 : 2;
            if (c2 == 'x')
                return (c1 != ')' || c != '(') && (c1 != '}' || c != '{')
                        && (c1 != ']' || c != '[') && (c1 != '>' || c != '<') ? 0
                        : 2;
            if (c2 == 'y')
                return 0;
            if (c2 == 'z')
                return 0;
        }
        if (c2 >= '0' && c2 <= '9') {
            if (c2 == '0') {
                if (c1 == 'o' || c1 == 'O')
                    return 1;
                return (c1 != '(' || c != ')') && (c1 != '{' || c != '}')
                        && (c1 != '[' || c != ']') ? 0 : 2;
            }
            if (c2 == '1')
                return c1 != 'l' ? 0 : 1;
            else
                return 0;
        }
        if (c2 == ',')
            return c1 != '.' ? 0 : 1;
        if (c2 == '.')
            return c1 != ',' ? 0 : 1;
        if (c2 == '!')
            return c1 != 'i' ? 0 : 1;
        else
            return 0;
    }

    private static byte method513(char c) {
        if (c >= 'a' && c <= 'z')
            return (byte) ((c - 97) + 1);
        if (c == '\'')
            return 28;
        if (c >= '0' && c <= '9')
            return (byte) ((c - 48) + 29);
        else
            return 27;
    }

    private static int method515(char[] ac, int i) {
        for (int k = i; k < ac.length && k >= 0; k++)
            if (ac[k] >= '0' && ac[k] <= '9')
                return k;

        return -1;
    }

    private static int method516(char[] ac, int j) {
        for (int k = j; k < ac.length && k >= 0; k++)
            if (ac[k] < '0' || ac[k] > '9')
                return k;
        return ac.length;
    }

    private static boolean containsFragment(char[] chars) {
        boolean onlyDigits = true;
        for (int index = 0; index < chars.length; index++)
            if (!isDigit(chars[index]) && chars[index] != 0)
                onlyDigits = false;

        if (onlyDigits)
            return true;
        int code = hash(chars);
        int index = 0;
        int length = fragments.length - 1;
        if (code == fragments[index] || code == fragments[length])
            return true;
        do {
            int i1 = (index + length) / 2;
            if (code == fragments[i1])
                return true;
            if (code < fragments[i1])
                length = i1;
            else
                index = i1;
        } while (index != length && index + 1 != length);
        return false;
    }

    private static int hash(char[] ac) {
        if (ac.length > 6)
            return 0;
        int k = 0;
        for (int l = 0; l < ac.length; l++) {
            char c = ac[ac.length - l - 1];
            if (c >= 'a' && c <= 'z')
                k = k * 38 + ((c - 97) + 1);
            else if (c == '\'')
                k = k * 38 + 27;
            else if (c >= '0' && c <= '9')
                k = k * 38 + ((c - 48) + 28);
            else if (c != 0)
                return 0;
        }

        return k;
    }

    private static boolean isNotAlphanumeric(char c) {
        return !isLetter(c) && !isDigit(c);
    }

    private static boolean method518(char c) {
        return c < 'a' || c > 'z' || c == 'v' || c == 'x' || c == 'j'
                || c == 'q' || c == 'z';
    }

    private static boolean isLetter(char c) {
        return c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z';
    }

    private static boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private static boolean isLowerCase(char c) {
        return c >= 'a' && c <= 'z';
    }

    private static boolean isUpperCase(char c) {
        return c >= 'A' && c <= 'Z';
    }
}

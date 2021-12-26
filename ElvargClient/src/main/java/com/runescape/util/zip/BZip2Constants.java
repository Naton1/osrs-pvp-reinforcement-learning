package com.runescape.util.zip;


interface BZip2Constants {

    int BASEBLOCKSIZE = 100000;
    int MAX_ALPHA_SIZE = 258;
    int MAX_CODE_LEN = 23;
    int RUNA = 0;
    int RUNB = 1;
    int N_GROUPS = 6;
    int G_SIZE = 50;
    int N_ITERS = 4;
    int MAX_SELECTORS = (2 + (900000 / G_SIZE));
    int NUM_OVERSHOOT_BYTES = 20;

}
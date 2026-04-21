package com.jyw.csp.util.chiper;

public class SM4Context {
    public int mode;
    public long[] sk;
    public boolean isPadding;

    public SM4Context() {
        this.mode = 1;
        this.isPadding = false;
        this.sk = new long[32];
    }
}

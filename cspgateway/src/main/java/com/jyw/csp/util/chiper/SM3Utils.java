package com.jyw.csp.util.chiper;

import com.jyw.csp.util.Utils;

public final class SM3Utils {
    public static String digest(String data) {
        byte[] md = new byte[32];
        byte[] bytes = data.getBytes();

        SM3Digest sm3 = new SM3Digest();
        sm3.update(bytes, 0, bytes.length);
        sm3.doFinal(md, 0);

        return Utils.bytes2Hex(md);
    }
}

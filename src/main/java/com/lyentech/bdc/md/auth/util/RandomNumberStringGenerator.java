package com.lyentech.bdc.md.auth.util;

import java.security.SecureRandom;
import java.util.Random;

/**
 * 随机数生成器，来生成6位手机验证码
 *
 * @author guolanren
 */
public class RandomNumberStringGenerator {

    private static final char[] DEFAULT_CODEC = "1234567890"
            .toCharArray();

    private Random random = new SecureRandom();

    private int length;

    /**
     * Create a generator with the default length (6).
     */
    public RandomNumberStringGenerator() {
        this(6);
    }

    /**
     * Create a generator of random strings of the length provided
     *
     * @param length the length of the strings generated
     */
    public RandomNumberStringGenerator(int length) {
        this.length = length;
    }

    public String generate() {
        byte[] verifierBytes = new byte[length];
        random.nextBytes(verifierBytes);
        return getAuthorizationCodeString(verifierBytes);
    }

    /**
     * Convert these random bytes to a verifier string. The length of the byte array can be
     * {@link #setLength(int) configured}. The default implementation mods the bytes to fit into the
     * ASCII letters 1-9.
     *
     * @param verifierBytes The bytes.
     * @return The string.
     */
    protected String getAuthorizationCodeString(byte[] verifierBytes) {
        char[] chars = new char[verifierBytes.length];
        for (int i = 0; i < verifierBytes.length; i++) {
            chars[i] = DEFAULT_CODEC[((verifierBytes[i] & 0xFF) % DEFAULT_CODEC.length)];
        }
        return new String(chars);
    }

    /**
     * The random value generator used to create token secrets.
     *
     * @param random The random value generator used to create token secrets.
     */
    public void setRandom(Random random) {
        this.random = random;
    }

    /**
     * The length of string to generate.
     *
     * @param length the length to set
     */
    public void setLength(int length) {
        this.length = length;
    }


}

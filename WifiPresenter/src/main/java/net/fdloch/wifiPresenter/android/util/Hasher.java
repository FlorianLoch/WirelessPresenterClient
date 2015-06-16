package net.fdloch.wifiPresenter.android.util;

import org.bouncycastle.util.encoders.Hex;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by florian on 12.03.15.
 */
public class Hasher {

    public static String computeHash(String input) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");

        md.update(input.getBytes("UTF-8"));
        byte[] digest = md.digest();

        return new String(Hex.encode(digest));
    }

}

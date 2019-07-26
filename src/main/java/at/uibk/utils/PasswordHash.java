package at.uibk.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PasswordHash {

    public static String hash(String password){
        StringBuilder passwordHash = new StringBuilder();
        try {
            byte[] passwordBytes = password.getBytes(StandardCharsets.UTF_8);
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] byteHash = messageDigest.digest(passwordBytes);
            for(byte b: byteHash){
                passwordHash.append(String.format("%02x", b));
            }
        } catch (NoSuchAlgorithmException e) { };
        return passwordHash.toString();
    }

    public static void main(String[] args) {
        System.out.println(hash("norabfrus"));
    }
}

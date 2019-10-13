package com.example.KeyGenerator;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;

public class KeyGenerator{



    public boolean generatekey() {
        try {
            KeyPairGenerator keygen = KeyPairGenerator.getInstance("DSA");
            keygen.initialize(512);
            KeyPair keys = keygen.genKeyPair();
            PublicKey pubkey = keys.getPublic();
            PrivateKey prikey = keys.getPrivate();
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("myprikey.dat"));
            out.writeObject(prikey);
            out.close();
            System.out.println("write prikeys ok");
            out = new ObjectOutputStream(new FileOutputStream("mypubkey.dat"));
            out.writeObject(pubkey);
            out.close();
            System.out.println("write pubkeys ok");
            System.out.println("Generated Key Pair successfully");
            return true;
        } catch (java.lang.Exception e) {
            e.printStackTrace();
            System.out.println("Failed to generate Key Pair");
            return false;
        }
    }

    public static void main(String args[]){
        KeyGenerator keyGenerator = new KeyGenerator();
        keyGenerator.generatekey();
    }

}
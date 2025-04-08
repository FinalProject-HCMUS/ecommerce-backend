package com.hcmus.ecommerce_backend.common.utils;

import lombok.experimental.UtilityClass;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.pkcs.PKCS8EncryptedPrivateKeyInfo;

import java.io.IOException;
import java.io.StringReader;
import java.security.PrivateKey;
import java.security.PublicKey;

@UtilityClass
public class KeyConverter {

    public PublicKey convertPublicKey(final String publicPemKey) {
        final StringReader keyReader = new StringReader(publicPemKey);
        try {
            Object pemObject = new PEMParser(keyReader).readObject();
            if (pemObject instanceof SubjectPublicKeyInfo) {
                return new JcaPEMKeyConverter().getPublicKey((SubjectPublicKeyInfo) pemObject);
            } else {
                throw new RuntimeException("Invalid public key format");
            }
        } catch (IOException exception) {
            throw new RuntimeException("Error reading public key", exception);
        }
    }

    public PrivateKey convertPrivateKey(final String privatePemKey) {
        StringReader keyReader = new StringReader(privatePemKey);
        try {
            Object pemObject = new PEMParser(keyReader).readObject();
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
            
            if (pemObject instanceof PrivateKeyInfo) {
                // Plain PKCS#8 private key
                return converter.getPrivateKey((PrivateKeyInfo) pemObject);
            } else if (pemObject instanceof PEMKeyPair) {
                // Traditional format (PKCS#1)
                return converter.getPrivateKey(((PEMKeyPair) pemObject).getPrivateKeyInfo());
            } else if (pemObject instanceof PKCS8EncryptedPrivateKeyInfo) {
                // Encrypted private key - would need password to decrypt
                throw new RuntimeException("Encrypted private keys are not supported");
            } else {
                throw new RuntimeException("Invalid private key format: " + pemObject.getClass().getName());
            }
        } catch (IOException exception) {
            throw new RuntimeException("Error reading private key", exception);
        }
    }
}
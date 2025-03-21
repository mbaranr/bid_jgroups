package util;

import java.io.Serializable;
import java.security.PublicKey;
import java.security.Signature;
import java.util.Base64;

public class SignedResponse implements Serializable{
    private final String message;
    private final String signature;

    public SignedResponse(String message, String signature) {
        this.message = message;
        this.signature = signature;
    }

    public boolean verify(PublicKey publicKey) {
        try {
            Signature rsa = Signature.getInstance("SHA256withRSA");
            rsa.initVerify(publicKey);
            rsa.update(message.getBytes());
            byte[] signatureBytes = Base64.getDecoder().decode(signature);
            return rsa.verify(signatureBytes);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public String getMessage() { return message; }
    public String getSignature() { return signature; }
}

package online.pigeonshouse.moreinterfaces.netty;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import online.pigeonshouse.moreinterfaces.trendsobject.TrendsObject;
import online.pigeonshouse.moreinterfaces.trendsobject.TrendsObjectFactory;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class SerializeFactory {
    public static final TrendsObject<String> AES_KEY = TrendsObjectFactory
            .buildObject(null);
    private final Map<Byte, Serialize> MAP = new HashMap<>();


    public void register(Byte i, Serialize serialize) {
        MAP.put(i, serialize);
    }

    public Serialize get(Byte i) {
        return MAP.get(i);
    }

    public static SerializeFactory getFactory() {
        SerializeFactory factory = new SerializeFactory();

        factory.register(JsonSerialize.JSON, new JsonSerialize());
        factory.register(AESJsonSerialize.AES_JSON, new AESJsonSerialize());

        return factory;
    }

    public static class JsonSerialize implements Serialize {
        private static final Gson json = new GsonBuilder()
                .create();
        public static final Byte JSON = 1;

        @Override
        public byte[] serialize(Object o) {
            return json.toJson(o).getBytes(StandardCharsets.UTF_8);
        }

        @Override
        public <T> T deserialize(Class<T> aClass, byte[] bytes) {
            String s = new String(bytes, StandardCharsets.UTF_8);
            return json.fromJson(s, aClass);
        }
    }

    public static class AESJsonSerialize extends JsonSerialize {
        public static final Byte AES_JSON = 2;
        private static final String KEY_ALGORITHM = "AES";
        private static final String CIPHER_ALGORITHM = "AES/ECB/PKCS5Padding";
        private static final SecureRandom random = new SecureRandom();

        private final Cipher cipher;
        private SecretKeySpec keySpec = null;

        public AESJsonSerialize() {
            AES_KEY.addListener((oldValue, newValue) -> {
                if (newValue != null) {
                    keySpec = new SecretKeySpec(newValue.getBytes(StandardCharsets.UTF_8), KEY_ALGORITHM);
                }
            });

            try {
                cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            }catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public byte[] serialize(Object o) {
            byte[] serialize = super.serialize(o);

            try {
                generateAESKey(false);
                cipher.init(Cipher.ENCRYPT_MODE, keySpec);
                byte[] encrypted = cipher.doFinal(serialize);

                return Base64.getEncoder().encode(encrypted);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public <T> T deserialize(Class<T> aClass, byte[] bytes) {
            try {
                cipher.init(Cipher.DECRYPT_MODE, keySpec);
                byte[] done = cipher.doFinal(Base64.getDecoder().decode(bytes));
                return super.deserialize(aClass, done);
            }catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public static String generateAESKey(boolean isInit) throws Exception {
            if (isInit || AES_KEY.isEmpty()) {
                synchronized (AES_KEY) {
                    if (isInit || AES_KEY.isEmpty()) {
                        KeyGenerator keyGenerator = KeyGenerator.getInstance(KEY_ALGORITHM);
                        keyGenerator.init(128, random);
                        SecretKey secretKey = keyGenerator.generateKey();
                        AES_KEY.set(Base64.getEncoder()
                                .encodeToString(secretKey.getEncoded()));
                    }
                }
            }

            return AES_KEY.get();
        }
    }
}
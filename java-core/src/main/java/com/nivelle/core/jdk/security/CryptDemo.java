package com.nivelle.core.jdk.security;

import org.apache.commons.io.FileUtils;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * 加解密
 *
 * @author fuxinzhong
 * @date 2020/06/26
 */
public class CryptDemo {

    /**
     * 对称性加密算法:
     * DES (Data Encryption Standard):数据加密标准，速度较快，适用于加密大量数据的场合
     * <p>
     * 数据加密标准算法,和BASE64最明显的区别就是有一个工作密钥，该密钥既用于加密、也用于解密，并且要求密钥是一个长度至少大于8位的字符串
     */

    public static final String DES_ALGORITHM = "DES";

    /**
     * 椭圆曲线加密算法：
     */
    public static final String ECC_ALGORITHM = "ECC";


    /**
     * 非对称性算法:
     * <p>
     * RSA:是一个支持变长密钥的公共密钥算法，需要加密的文件块的长度也是可变的
     * <p>
     * 非对称加密算法的典型代表，既能加密、又能解密。和对称加密算法比如DES的明显区别在于用于加密、解密的密钥是不同的。使用RSA算法，只要密钥足够长(一般要求1024bit)，加密的信息是不能被破解的
     */
    public static final String RSA_ALGORITHM = "RSA";


    /**
     * 对称性加密算法:
     * AES (Advanced Encryption Standard):高级加密标准，是下一代的加密算法标准，速度快，安全级别高；AES是一个使用128为分组块的分组加密算法，分组块和128、192或256位的密钥一起作为输入，对4×4的字节数组上进行操作。
     */
    public static final String AES_ALGORITHM = "AES";

    /**
     * 这是默认模式:
     *
     * public static final String transformation = "DES/ECB/PKCS5Padding";
     *
     */

    /**
     * 使用CBC模式, 在初始化Cipher对象时, 需要增加参数, 初始化向量IV : IvParameterSpec iv = new IvParameterSpec(key.getBytes());public static final String transformation = "DES/CBC/PKCS5Padding";
     */

    /**
     * NOPadding: 使用NOPadding模式时, 原文长度必须是8byte的整数倍
     */
    public static final String transformationDES = "DES/ECB/NOPadding";
    public static final String key = "12345678";
    public static final String original = "你好11";


    // 这是默认模式
    //  public static final String transformation = "AES/ECB/PKCS5Padding";
    // 使用CBC模式, 在初始化Cipher对象时, 需要增加参数, 初始化向量IV : IvParameterSpec iv = new IvParameterSpec(key.getBytes());
    //  public static final String transformation = "AES/CBC/PKCS5Padding";
    // NOPadding: 使用NOPadding模式时, 原文长度必须是8byte的整数倍
    public static final String transformationAes = "AES/CBC/NOPadding";
    public static final String keyAes = "1234567812345678";
    public static final String originalAes = "你好你好你1";


    public static void main(String[] args) throws Exception {


        /**
         * DES
         */
        String input = "非对称加密与对称加密相比，其安全性更好：对称加密的通信双方使用相同的秘钥，如果一方的秘钥遭泄露，那么整个通信就会被破解。" +
                "而非对称加密使用一对秘钥，一个用来加密，一个用来解密，而且公钥是公开的，秘钥是自己保存的，不需要像对称加密那样在通信之前要先同步秘钥";
        try {
            generateKeyToFile(DES_ALGORITHM, "a.pub", "a.pri");
            PublicKey publicKey = loadPublicKeyFromFile(DES_ALGORITHM, "a.pub");
            PrivateKey privateKey = loadPrivateKeyFromFile(DES_ALGORITHM, "a.pri");
            String encryptDES = encrypt(DES_ALGORITHM, input, privateKey, 245);
            String decryptDES = decrypt(DES_ALGORITHM, encryptDES, publicKey, 256);
            System.out.println(encryptDES);
            System.out.println(decryptDES);

            String encryptAes = encrypt(AES_ALGORITHM, input, privateKey, 245);
            String decryptAes = decrypt(AES_ALGORITHM, encryptAes, publicKey, 256);
            System.out.println(encryptAes);
            System.out.println(decryptAes);


        } catch (Exception e) {
            e.printStackTrace();
        }

        /**
         * AES
         */
        String encryptByAES = encryptByAES();
        System.out.println(encryptByAES);
        String decryptByAES = decryptByAES(encryptByAES);
        System.out.println(decryptByAES);

        /**
         * base64
         *
         * 通常用作对二进制数据进行加密
         */
        String text = "hello你好";
        System.out.println("压缩前字符长度:" + text.length());
        System.out.println("压缩前字节长度:" + text.getBytes().length);

        String encode = Base64.getEncoder().encodeToString(text.getBytes(StandardCharsets.UTF_8));
        System.out.println("压缩后字符串:" + encode);

        System.out.println("压缩后字符串长度:" + encode.length());
        System.out.println("压缩后字符串字节长度:" + encode.getBytes().length);

        String decode = new String(Base64.getDecoder().decode(encode), StandardCharsets.UTF_8);
        System.out.println("解压后字符串：" + decode);
        System.out.println("原字符串与解压后的字符串比较:" + text.equals(decode));

        System.out.println("base64加密后占字节数:" + encode.length());
        System.out.println("原字符串字节数:" + text.getBytes().length + ";位数:" + text.getBytes().length * 8);
    }


    /**
     * 生成密钥对并保存在本地文件中
     *
     * @param algorithm : 算法
     * @param pubPath   : 公钥保存路径
     * @param priPath   : 私钥保存路径
     * @throws Exception
     */
    private static void generateKeyToFile(String algorithm, String pubPath, String priPath) throws Exception {
        // 获取密钥对生成器
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(algorithm);
        // 获取密钥对
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        // 获取公钥
        PublicKey publicKey = keyPair.getPublic();
        // 获取私钥
        PrivateKey privateKey = keyPair.getPrivate();
        // 获取byte数组
        byte[] publicKeyEncoded = publicKey.getEncoded();
        byte[] privateKeyEncoded = privateKey.getEncoded();
        // 进行Base64编码
        String publicKeyString = new String(Base64.getEncoder().encodeToString(publicKeyEncoded));
        String privateKeyString = Base64.getEncoder().encodeToString(privateKeyEncoded);
        // 保存文件
        FileUtils.writeStringToFile(new File(pubPath), publicKeyString, Charset.forName("UTF-8"));
        FileUtils.writeStringToFile(new File(priPath), privateKeyString, Charset.forName("UTF-8"));


    }

    /**
     * 从文件中加载公钥
     *
     * @param algorithm : 算法
     * @param filePath  : 文件路径
     * @return : 公钥
     * @throws Exception
     */
    private static PublicKey loadPublicKeyFromFile(String algorithm, String filePath) throws Exception {
        // 将文件内容转为字符串
        String keyString = FileUtils.readFileToString(new File(filePath), Charset.forName("UTF-8"));
        return loadPublicKeyFromString(algorithm, keyString);

    }

    /**
     * 从字符串中加载公钥
     *
     * @param algorithm : 算法
     * @param keyString : 公钥字符串
     * @return : 公钥
     * @throws Exception
     */
    private static PublicKey loadPublicKeyFromString(String algorithm, String keyString) throws Exception {
        // 进行Base64解码
        byte[] decode = Base64.getDecoder().decode(keyString);
        // 获取密钥工厂
        KeyFactory keyFactory = KeyFactory.getInstance(algorithm);
        // 构建密钥规范
        X509EncodedKeySpec keyspec = new X509EncodedKeySpec(decode);
        // 获取公钥
        return keyFactory.generatePublic(keyspec);

    }

    /**
     * 从文件中加载私钥
     *
     * @param algorithm : 算法
     * @param filePath  : 文件路径
     * @return : 私钥
     * @throws Exception
     */
    private static PrivateKey loadPrivateKeyFromFile(String algorithm, String filePath) throws Exception {
        // 将文件内容转为字符串
        String keyString = FileUtils.readFileToString(new File(filePath), Charset.forName("UTF-8"));
        return loadPrivateKeyFromString(algorithm, keyString);

    }

    /**
     * 从字符串中加载私钥
     *
     * @param algorithm : 算法
     * @param keyString : 私钥字符串
     * @return : 私钥
     * @throws Exception
     */
    private static PrivateKey loadPrivateKeyFromString(String algorithm, String keyString) throws Exception {
        // 进行Base64解码
        byte[] decode = Base64.getDecoder().decode(keyString);
        // 获取密钥工厂
        KeyFactory keyFactory = KeyFactory.getInstance(algorithm);
        // 构建密钥规范
        PKCS8EncodedKeySpec keyspec = new PKCS8EncodedKeySpec(decode);
        // 生成私钥
        return keyFactory.generatePrivate(keyspec);

    }

    /**
     * 使用密钥加密数据
     *
     * @param algorithm      : 算法
     * @param input          : 原文
     * @param key            : 密钥
     * @param maxEncryptSize : 最大加密长度(需要根据实际情况进行调整)
     * @return : 密文
     * @throws Exception
     */
    private static String encrypt(String algorithm, String input, Key key, int maxEncryptSize) throws Exception {
        // 获取Cipher对象
        Cipher cipher = Cipher.getInstance(algorithm);
        // 初始化模式(加密)和密钥
        cipher.init(Cipher.ENCRYPT_MODE, key);
        // 将原文转为byte数组
        byte[] data = input.getBytes();
        // 总数据长度
        int total = data.length;
        // 输出流
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        decodeByte(maxEncryptSize, cipher, data, total, baos);
        // 对密文进行Base64编码
        return Base64.getEncoder().encodeToString(baos.toByteArray());

    }

    /**
     * 解密数据
     *
     * @param algorithm      : 算法
     * @param encrypted      : 密文
     * @param key            : 密钥
     * @param maxDecryptSize : 最大解密长度(需要根据实际情况进行调整)
     * @return : 原文
     * @throws Exception
     */
    private static String decrypt(String algorithm, String encrypted, Key key, int maxDecryptSize) throws Exception {
        // 获取Cipher对象
        Cipher cipher = Cipher.getInstance(algorithm);
        // 初始化模式(解密)和密钥
        cipher.init(Cipher.DECRYPT_MODE, key);
        // 由于密文进行了Base64编码, 在这里需要进行解码
        byte[] data = Base64.getDecoder().decode(encrypted);
        // 总数据长度
        int total = data.length;
        // 输出流
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        decodeByte(maxDecryptSize, cipher, data, total, baos);
        // 输出原文
        return baos.toString();

    }

    /**
     * 分段处理数据
     *
     * @param maxSize : 最大处理能力
     * @param cipher  : Cipher对象
     * @param data    : 要处理的byte数组
     * @param total   : 总数据长度
     * @param baos    : 输出流
     * @throws Exception
     */
    private static void decodeByte(int maxSize, Cipher cipher, byte[] data, int total, ByteArrayOutputStream baos) throws Exception {
        // 偏移量
        int offset = 0;
        // 缓冲区
        byte[] buffer;
        // 如果数据没有处理完, 就一直继续
        while (total - offset > 0) {
            // 如果剩余的数据 >= 最大处理能力, 就按照最大处理能力来加密数据
            if (total - offset >= maxSize) {
                // 加密数据
                buffer = cipher.doFinal(data, offset, maxSize);
                // 偏移量向右侧偏移最大数据能力个
                offset += maxSize;
            } else {
                // 如果剩余的数据 < 最大处理能力, 就按照剩余的个数来加密数据
                buffer = cipher.doFinal(data, offset, total - offset);
                // 偏移量设置为总数据长度, 这样可以跳出循环
                offset = total;
            }
            // 向输出流写入数据
            baos.write(buffer);
        }
    }


    public static String encryptByAES() throws Exception {

        // 获取Cipher
        Cipher cipher = Cipher.getInstance(transformationAes);
        // 生成密钥
        SecretKeySpec keySpec = new SecretKeySpec(keyAes.getBytes(), AES_ALGORITHM);
        // 指定模式(加密)和密钥
        // 创建初始化向量
        IvParameterSpec iv = new IvParameterSpec(keyAes.getBytes());
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, iv);
        //cipher.init(Cipher.ENCRYPT_MODE, keySpec);
        // 加密
        byte[] bytes = cipher.doFinal(originalAes.getBytes());

        return Base64.getEncoder().encodeToString(bytes);
    }

    public static String decryptByAES(String encrypted) throws Exception {

        // 获取Cipher
        Cipher cipher = Cipher.getInstance(transformationAes);
        // 生成密钥
        SecretKeySpec keySpec = new SecretKeySpec(keyAes.getBytes(), AES_ALGORITHM);
        // 指定模式(解密)和密钥
        // 创建初始化向量
        IvParameterSpec iv = new IvParameterSpec(keyAes.getBytes());
        cipher.init(Cipher.DECRYPT_MODE, keySpec, iv);
        //  cipher.init(Cipher.DECRYPT_MODE, keySpec);
        // 解密
        byte[] bytes = cipher.doFinal(Base64.getDecoder().decode(encrypted));

        return new String(bytes);
    }

    /**
     * SHA加密
     *
     * @param content 待加密内容
     * @return String
     */
    public static String SHAEncrypt(final String content) {
        try {
            MessageDigest sha = MessageDigest.getInstance("SHAE");
            byte[] sha_byte = sha.digest(content.getBytes());
            StringBuffer hexValue = new StringBuffer();
            for (byte b : sha_byte) {
                //将其中的每个字节转成十六进制字符串:byte类型的数据最高位是符号位，通过和0xff进行与操作,转换为int类型的正整数。
                String toHexString = Integer.toHexString(b & 0xff);
                hexValue.append(toHexString.length() == 1 ? "0" + toHexString : toHexString);
            }
            return hexValue.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * HMAC加密
     *
     * @param key     给定秘钥key
     * @param content 待加密内容
     * @return String
     * <p>
     * 使用一个密钥生成一个固定大小的小数据块，即MAC，并将其加入到消息中，然后传输。接收方利用与发送方共享的密钥进行鉴别认证
     */
    public static byte[] HMACEncrypt(final String key, final String content) {
        try {
            SecretKey secretKey = new SecretKeySpec(key.getBytes(), "Mac");
            Mac mac = Mac.getInstance(secretKey.getAlgorithm());
            //初始化mac
            mac.init(secretKey);
            return mac.doFinal(content.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * RSA加密
     *
     * @param content 待加密内容
     * @return byte[]
     */
    public static byte[] RSAEncrypt(final String content) {
        try {
            // 获取密钥对生成器
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(RSA_ALGORITHM);
            // 获取密钥对
            KeyPair keyPair = keyPairGenerator.generateKeyPair();

            return processCipher(content.getBytes(), keyPair.getPrivate(), Cipher.ENCRYPT_MODE, RSA_ALGORITHM);
        } catch (Exception e) {

        }
        return null;
    }

    /**
     * RSA解密
     *
     * @param encoderContent 已加密内容
     * @return byte[]
     */
    public static byte[] RSADecrypt(final byte[] encoderContent) {

        try {
            // 获取密钥对生成器
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(RSA_ALGORITHM);
            // 获取密钥对
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            return processCipher(encoderContent, keyPair.getPublic(), Cipher.DECRYPT_MODE, RSA_ALGORITHM);
        } catch (Exception e) {
        }
        return null;
    }

    private static byte[] processCipher(final byte[] processData, final Key key, final int opsMode, final String algorithm) {

        try {

            Cipher cipher = Cipher.getInstance(algorithm);
            //初始化
            cipher.init(opsMode, key, new SecureRandom());
            return cipher.doFinal(processData);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


}

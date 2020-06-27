package com.nivelle.base.jdk.base;

import org.apache.commons.io.FileUtils;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.charset.Charset;
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
     */

    public static final String algorithm = "DES";

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
    public static final String transformation = "DES/ECB/NOPadding";
    public static final String key = "12345678";
    public static final String original = "你好11";


    /**
     * 对称性加密算法:
     * AES (Advanced Encryption Standard):高级加密标准，是下一代的加密算法标准，速度快，安全级别高；AES是一个使用128为分组块的分组加密算法，分组块和128、192或256位的密钥一起作为输入，对4×4的字节数组上进行操作。
     */
    public static final String algorithmAes = "AES";

    // 这是默认模式
    //  public static final String transformation = "AES/ECB/PKCS5Padding";
    // 使用CBC模式, 在初始化Cipher对象时, 需要增加参数, 初始化向量IV : IvParameterSpec iv = new IvParameterSpec(key.getBytes());
    //  public static final String transformation = "AES/CBC/PKCS5Padding";
    // NOPadding: 使用NOPadding模式时, 原文长度必须是8byte的整数倍
    public static final String transformationAes = "AES/CBC/NOPadding";
    public static final String keyAes = "1234567812345678";
    public static final String originalAes = "你好你好你1";



    public static void main(String[] args) throws Exception {
        String encryptByDES = encryptByDES();
        System.out.println(encryptByDES);
        String decryptByDES = decryptByDES(encryptByDES);
        System.out.println(decryptByDES);

        /**
         * 非对称性算法:
         *
         * RSA:是一个支持变长密钥的公共密钥算法，需要加密的文件块的长度也是可变的
         */
        String algorithm = "RSA";
        String input = "非对称加密与对称加密相比，其安全性更好：对称加密的通信双方使用相同的秘钥，如果一方的秘钥遭泄露，那么整个通信就会被破解。而非对称加密使用一对秘钥，一个用来加密，一个用来解密，而且公钥是公开的，秘钥是自己保存的，不需要像对称加密那样在通信之前要先同步秘钥";
        try {
            generateKeyToFile(algorithm, "a.pub", "a.pri");

            PublicKey publicKey = loadPublicKeyFromFile(algorithm, "a.pub");
            PrivateKey privateKey = loadPrivateKeyFromFile(algorithm, "a.pri");
            String encrypt = encrypt(algorithm, input, privateKey, 245);
            String decrypt = decrypt(algorithm, encrypt, publicKey, 256);
            System.out.println(encrypt);
            System.out.println(decrypt);

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
    }

    public static String encryptByDES() throws Exception {
        // 获取Cipher
        Cipher cipher = Cipher.getInstance(transformation);

        // 指定密钥规则
        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(), algorithm);

        // 指定模式(加密)和密钥
        // 创建初始向量
        IvParameterSpec iv = new IvParameterSpec(key.getBytes());
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
        //  cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, iv);
        // 加密
        byte[] bytes = cipher.doFinal(original.getBytes());
        // 输出加密后的数据
        // com.sun.org.apache.xml.internal.security.utils.Base64
        return new String(Base64.getEncoder().encode(bytes));
    }

    public static String decryptByDES(String encrypted) throws Exception {
        // 获取Cipher
        Cipher cipher = Cipher.getInstance(transformation);

        // 指定密钥规则
        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(), algorithm);

        // 指定模式(解密)和密钥
        // 创建初始向量
        IvParameterSpec iv = new IvParameterSpec(key.getBytes());
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
        //  cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, iv);
        // 解码密文
        // com.sun.org.apache.xml.internal.security.utils.Base64
        byte[] decode = Base64.getDecoder().decode(encrypted);
        // 解密
        byte[] bytes = cipher.doFinal(decode);
        // 输出解密后的数据
        return new String(bytes);
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
        SecretKeySpec keySpec = new SecretKeySpec(keyAes.getBytes(), algorithmAes);
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
        SecretKeySpec keySpec = new SecretKeySpec(keyAes.getBytes(), algorithmAes);
        // 指定模式(解密)和密钥
        // 创建初始化向量
        IvParameterSpec iv = new IvParameterSpec(keyAes.getBytes());
        cipher.init(Cipher.DECRYPT_MODE, keySpec, iv);
        //  cipher.init(Cipher.DECRYPT_MODE, keySpec);
        // 解密
        byte[] bytes = cipher.doFinal(Base64.getDecoder().decode(encrypted));

        return new String(bytes);
    }

}

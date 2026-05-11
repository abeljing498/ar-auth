package com.lyentech.bdc.md.auth.util;

/**
 * @author YuYi
 * @create 2023/7/24
 * @create 15:38
 */

import org.apache.commons.codec.binary.Base64;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by cuiran on 19/1/9.
 */
public class RSAUtil {

    //生成秘钥对
    public static KeyPair getKeyPair() throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(1024);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        return keyPair;
    }
    //获取公钥(Base64编码)
    public static String getPublicKey(KeyPair keyPair) throws UnsupportedEncodingException {
        PublicKey publicKey = keyPair.getPublic();
        byte[] bytes = publicKey.getEncoded();
        return byte2Base64(bytes);
    }
    //获取私钥(Base64编码)
    public static String getPrivateKey(KeyPair keyPair) throws UnsupportedEncodingException {
        PrivateKey privateKey = keyPair.getPrivate();
        byte[] bytes = privateKey.getEncoded();
        return byte2Base64(bytes);
    }
    //将Base64编码后的公钥转换成PublicKey对象
    public static PublicKey string2PublicKey(String pubStr) throws Exception {
        byte[] keyBytes = base642Byte(pubStr);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey publicKey = keyFactory.generatePublic(keySpec);
        return publicKey;
    }
    //将Base64编码后的私钥转换成PrivateKey对象
    public static PrivateKey string2PrivateKey(String priStr) throws Exception {
        byte[] keyBytes = base642Byte(priStr);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
        return privateKey;
    }
    //公钥加密
    public static String publicEncrypt(byte[] content, PublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] bytes = cipher.doFinal(content);
        String outStr = byte2Base64(bytes);
        return outStr;
    }
    //私钥解密
    public static byte[] privateDecrypt(String outStr, PrivateKey privateKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] content = base642Byte(outStr);

        byte[] bytes = cipher.doFinal(content);
        return bytes;
    }
    //字节数组转Base64编码
    public static String byte2Base64(byte[] bytes) throws UnsupportedEncodingException {
        BASE64Encoder encoder = new BASE64Encoder();
        return encoder.encode(bytes);
    }
    //Base64编码转字节数组
    public static byte[] base642Byte(String base64Key) throws IOException, IOException {
        BASE64Decoder decoder = new BASE64Decoder();
        return decoder.decodeBuffer(base64Key);
    }

    public static String encrypt(String str, Key key) throws NoSuchAlgorithmException,
            NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        //RSA加密
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return Base64.encodeBase64String(cipher.doFinal(str.getBytes(StandardCharsets.UTF_8)));
    }

    public static String decrypt(String str, Key key) throws NoSuchAlgorithmException,
            NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        //64位解码加密后的字符串
        byte[] inputByte = Base64.decodeBase64(str.getBytes(StandardCharsets.UTF_8));
        //RSA解密
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, key);
        return new String(cipher.doFinal(inputByte));
    }

    //读取txt文件
    public static String readTxtFile(String filePath){
        try {
            String encoding="GBK";
            File file = new File(filePath);
            if(file.isFile() && file.exists()){
                //判断文件是否存在
                InputStreamReader read = new InputStreamReader(
                        new FileInputStream(file),encoding);
                //考虑到编码格式
                BufferedReader bufferedReader = new BufferedReader(read);
                StringBuilder sb = new StringBuilder();
                String lineTxt = null;
                while((lineTxt = bufferedReader.readLine()) != null){
                    sb.append(lineTxt );
                }
                read.close();
                return sb.toString();
            }else{
                System.out.println("找不到指定的文件");
            }
        } catch (Exception e) {
            System.out.println("读取文件内容出错");
            e.printStackTrace();
        }
        return null;
}


}
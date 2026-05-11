package com.lyentech.bdc.md.auth.util;

import org.lionsoul.ip2region.DataBlock;
import org.lionsoul.ip2region.DbConfig;
import org.lionsoul.ip2region.DbSearcher;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;

public class IpCheckRegionUtil {

    // 1. 静态变量，存放 DB 文件的二进制数据 (这是真正的单例部分)
    private static byte[] dbBinStr = null;

    // 2. 静态配置对象
    private static DbConfig config = null;

    // 3. 静态代码块：类加载时只执行一次，解决 IO 和句柄问题
    static {
        try {

            config = new DbConfig();
            String dbPath = "/opt/ar-auth-backend/ip2region.db";

            // 将文件一次性读取到内存中
            // 方式 A: 纯 Java IO 写法
            File file = new File(dbPath);
            if (file.exists()) {
                try (FileInputStream fis = new FileInputStream(file);
                     ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
                    byte[] buffer = new byte[4096];
                    int len;
                    while ((len = fis.read(buffer)) != -1) {
                        bos.write(buffer, 0, len);
                    }
                    dbBinStr = bos.toByteArray();
                }
            } else {
                System.err.println("错误：IP库文件不存在 " + dbPath);
            }

        } catch (Exception e) {
            e.printStackTrace();
            // 在这里最好记录日志，甚至抛出 Error 阻止服务启动，因为 IP 库坏了
        }
    }
    /**
     * 获取 IP 归属地
     * 此时由内存读取，纳秒级响应，且无文件句柄占用
     */
    public static Boolean getIpRegion(String ip) {
        // 如果数据没加载成功，直接返回 false，避免空指针
        if (dbBinStr == null) {
            return false;
        }

        try {
            // 重点：每次查询 new 一个 Searcher，但是传入的是内存数组
            // 这个操作非常轻量（只是包装了一下数组），且线程安全
            DbSearcher searcher = new DbSearcher(config, dbBinStr);

            // 既然已经改代码了，建议去掉反射，直接调用 memorySearch
            // 如果你必须用反射（比如 jar 包冲突），保留反射也可以，但 target 变成了 searcher
            DataBlock dataBlock = searcher.memorySearch(ip);

            // 这里的 searcher 不需要 close，因为它是基于内存的，没有打开文件流

            // 业务逻辑优化
            String region = dataBlock.getRegion();
            if (region == null) return false;

            // 逻辑简化：不需要 split 再 stream，直接判断字符串包含即可
            // 原始逻辑是 "中国|0|上海|上海市|联通"，判断是否有 "内网IP"
            return !region.contains("内网IP");

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
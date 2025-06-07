package com.wyt;

import java.io.*;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Utils {
    private static String logPath;

    // 读取文件转换为字节数组
    public static byte[] fromFileToString(String path) {
        File file = new File(path);
        // 文件不存在则返回空字符串
        if (file.exists() && file.isFile() && file.canRead()) {
            try (FileInputStream fis = new FileInputStream(path)) {
                return fis.readAllBytes();
            } catch (IOException e) {
                System.out.println("[ERROR] 读取文件异常 " + e.getMessage());
            }
        }
        return new byte[0];
    }

    // 向日志文件中写入日志
    public static void log(String content) {
        if (Utils.logPath == null) {
            String fileName = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss_SSSS")) + ".txt";
            Utils.logPath = Path.of(Config.rootPath, Config.logPath, fileName).toString();
        }
        try (FileWriter fileWriter = new FileWriter(Utils.logPath, true)) {
            String line = "[" + LocalDateTime.now().toString() + "]\t" + content;
            fileWriter.write(line + "\n");
            System.out.println("[log] " + line);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

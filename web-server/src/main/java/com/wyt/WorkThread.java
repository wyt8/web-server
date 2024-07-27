package com.wyt;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class WorkThread implements Runnable {
    // 支持的状态码
    private enum STATUS_CODE {
        CODE_200, // 响应成功
        CODE_400, // 客户端请求语法错误
        CODE_403, // 禁止访问
        CODE_404  // 请求资源不存在
    }

    // 支持的请求方式
    private enum REQUEST_METHOD {
        GET, POST, HEAD
    }

    private final Socket clientSocket;
    private final OutputStream out;
    private REQUEST_METHOD requestMethod;
    private String requestUrl; // 请求URL
    private Map<String, String> requestParams; // 请求参数
    private Map<String, String> requestHeaders; // 请求头
    private byte[] requestBody; // 请求体


    public WorkThread(Socket clientSocket) {
        this.clientSocket = clientSocket;
        try {
            this.out = clientSocket.getOutputStream();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        try (InputStream in = this.clientSocket.getInputStream()) {
            boolean res = requestHandle(in);
            if (res) {
                // CGI程序
                if (requestUrl.startsWith("/" + Config.cgiPath)) {
                    cgiHandle();
                } else { // 非CGI程序
                    switch (requestMethod) {
                        case GET -> {
                            getHandle();
                        }
                        case POST -> {
                            postHandle();
                        }
                        case HEAD -> {
                            headHandle();
                        }
                    }
                }
            } else {
                // 没有正确处理请求，返回400页面
                respond400Page();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            out.close();
            // 关闭客户端连接
            clientSocket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // 处理客户端发送过来的消息，处理成功返回true，失败返回false
    private boolean requestHandle(InputStream in) {
        List<String> firstLineAndHeaders = new ArrayList<>();
        byte[] body = new byte[0];
        boolean encounterBlankLine = false;
        try {
            int lineIndex = 0;
            int lineLength = 0;
            while (!encounterBlankLine) {
                int readLength = in.available();
                byte[] temp = in.readNBytes(readLength);
                for (int i = 0; i < readLength; ) {
                    if (temp[i] == (byte) '\r' && temp[i + 1] == (byte) '\n') { // 遇到了换行
                        lineLength = i - lineIndex;
                        if (lineLength == 0) { // 遇到了空行
                            body = Arrays.copyOfRange(temp, Integer.min(i + 2, readLength), readLength);
                            encounterBlankLine = true;
                            break;
                        } else {
                            firstLineAndHeaders.add(new String(temp, lineIndex, lineLength, StandardCharsets.UTF_8));
                        }
                        lineIndex = i + 2;
                        i += 2;
                    } else {
                        i += 1;
                    }
                }
            }
        } catch (IOException e) {
            return false;
        }

        if (firstLineAndHeaders.isEmpty()) {
            return false;
        }
        // 读取第一行
        String[] firstLineComp = firstLineAndHeaders.getFirst().split(" ");
        if (firstLineComp.length != 3) {
            return false;
        }
        switch (firstLineComp[0]) {
            case "GET":
                this.requestMethod = REQUEST_METHOD.GET;
                break;
            case "POST":
                this.requestMethod = REQUEST_METHOD.POST;
                break;
            case "HEAD":
                this.requestMethod = REQUEST_METHOD.HEAD;
                break;
            default:
                return false;
        }
        // 去除片段标识符，并分开请求地址和参数
        String[] urlComp = firstLineComp[1].split("#")[0].split("\\?");
        this.requestUrl = urlComp[0];
        this.requestParams = new HashMap<>();
        // 解析请求参数
        if (urlComp.length == 2) {
            String[] params = urlComp[1].split("&");
            for (String param : params) {
                String[] keyAndValue = param.split("=");
                requestParams.put(keyAndValue[0], keyAndValue.length == 2 ? keyAndValue[1] : "");
            }
        }

        // 读取请求头
        this.requestHeaders = new HashMap<>();
        for (int i = 1; i < firstLineAndHeaders.size(); i++) {
            String[] headerComp = firstLineAndHeaders.get(i).split(": ");
            if (headerComp.length != 2) {
                return false;
            }
            requestHeaders.put(headerComp[0], headerComp[1]);
        }

        // 读取请求体
        if (requestHeaders.containsKey("Content-Length")) {
            int counter = Integer.parseInt(requestHeaders.get("Content-Length"));
            try {
                this.requestBody = new byte[counter];
                // 将array1复制到requestBody数组
                System.arraycopy(body, 0, this.requestBody, 0, body.length);
                // 将array2复制到requestBody数组的后面
                System.arraycopy(in.readNBytes(counter - body.length), 0, this.requestBody, body.length, counter - body.length);
            } catch (IOException e) {
                return false;
            }
        }
        return true;
    }


    // GET请求处理
    private void getHandle() {
        STATUS_CODE code = STATUS_CODE.CODE_200; // 状态码
        Map<String, String> header = new HashMap<>(); // 请求头
        byte[] body; // 请求体

        // 默认页面替换
        String path = Objects.equals(requestUrl, "/") ? Path.of(Config.rootPath, Config.defaultPage).toString() : Path.of(Config.rootPath, requestUrl).toString();

        File file = new File(path);
        // 文件不存在则返回404页面
        if (!(file.exists() && file.isFile() && file.canRead())) {
            respond404Page();
            return;
        }

        body = Utils.fromFileToString(path);
        try {
            // 获取文件类型，正确标识MIME类型
            header.put("Content-Type", Files.probeContentType(Path.of(path)));
        } catch (IOException e) {
            System.out.println("[ERROR] 解析文件类型失败");
        }
        responseHandle(code, header, body);
    }

    private void postHandle() {

    }

    private void headHandle() {

    }

    private void cgiHandle() {
        File file = new File(Path.of(Config.rootPath, requestUrl).toString());
        // 文件不存在则返回404页面
        if (!(file.exists() && file.isFile() && file.canRead())) {
            respond404Page();
            return;
        }

        ProcessBuilder processBuilder = new ProcessBuilder("python", Path.of(Config.rootPath, requestUrl).toString());
        switch (requestMethod) {
            case GET -> {
                processBuilder.environment().put("REQUEST_METHOD", "GET");
            }
            case POST -> {
                processBuilder.environment().put("REQUEST_METHOD", "POST");
                if (requestHeaders.containsKey("Content-Length")) {
                    processBuilder.environment().put("Content-Length", requestHeaders.get("Content-Length"));
                }
                if (requestHeaders.containsKey("Content-Type")) {
                    processBuilder.environment().put("Content-Type", requestHeaders.get("Content-Type"));
                }
            }
            case HEAD -> {
                processBuilder.environment().put("REQUEST_METHOD", "HEAD");
            }
        }
        StringBuilder query = new StringBuilder();
        int counter = requestParams.size();
        for (Map.Entry<String, String> entry : requestParams.entrySet()) {
            query.append(String.format("%s=%s", entry.getKey(), entry.getValue()));
            if (counter > 1) {
                query.append("&");
                counter--;
            }
        }
        processBuilder.environment().put("QUERY_STRING", query.toString());

        try {
            Process process = processBuilder.start();
            if (requestMethod == REQUEST_METHOD.POST && requestBody != null) {
                process.getOutputStream().write(requestBody);
                process.getOutputStream().flush();
            }
            out.write(String.format("HTTP/1.0 %d %s\r\n", 200, "OK").getBytes(StandardCharsets.UTF_8));
            // 重定向进程的输出到服务器的输出
            out.write(process.getInputStream().readAllBytes());
            process.waitFor();
            log(STATUS_CODE.CODE_200);
        } catch (IOException | InterruptedException e) {
            System.out.println("[ERROR] CGI程序运行错误");
        }
    }

    // 响应处理
    private void responseHandle(STATUS_CODE statusCode, Map<String, String> headers, byte[] body) {
        // 响应第一行
        String firstLine = "";
        switch (statusCode) {
            case CODE_200 -> {
                firstLine = String.format("HTTP/1.0 %d %s\r\n", 200, "OK");
            }
            case CODE_400 -> {
                firstLine = String.format("HTTP/1.0 %d %s\r\n", 400, "Bad Request");
            }
            case CODE_403 -> {
                firstLine = String.format("HTTP/1.0 %d %s\r\n", 403, "Forbidden");
            }
            case CODE_404 -> {
                firstLine = String.format("HTTP/1.0 %d %s\r\n", 404, "Not Found");
            }
        }
        try {
            out.write(firstLine.getBytes(StandardCharsets.UTF_8));
            // 响应头
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                out.write(String.format("%s: %s\r\n", entry.getKey(), entry.getValue()).getBytes(StandardCharsets.UTF_8));
            }
            // 响应体
            if (body.length > 0) {
                out.write("\r\n".getBytes(StandardCharsets.UTF_8));
                out.write(body);
            }

            log(statusCode);
        } catch (IOException e) {
            System.out.println("[ERROR] 返回响应错误");
        }
    }

    // 打印日志
    private void log(STATUS_CODE code) {
        File file = new File(Path.of(Config.rootPath, requestUrl).toString());
        String log = "";
        if (file.exists() && file.isFile() && file.canRead()) {
            log = clientSocket.getRemoteSocketAddress().toString() + " " + requestMethod.toString() + " " + code.toString() + " " + file.getAbsolutePath() + " " + file.length() + " " + requestHeaders.getOrDefault("User-Agent", "-");
        } else {
            log = clientSocket.getRemoteSocketAddress().toString() + " " + requestMethod.toString() + " " + code.toString() +" - - " + requestHeaders.getOrDefault("User-Agent", "-");
        }

        Utils.log(log);
    }

    // 对400页面的封装
    private void respond400Page() {
        String path = Path.of(Config.rootPath, Config.badRequestPage).toString();
        byte[] body = Utils.fromFileToString(path);
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "text/html");
        responseHandle(STATUS_CODE.CODE_400, headers, body);
    }

    // 对403页面的封装
    private void respond403Page() {
        String path = Path.of(Config.rootPath, Config.forbiddenPage).toString();
        byte[] body = Utils.fromFileToString(path);
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "text/html");
        responseHandle(STATUS_CODE.CODE_403, headers, body);
    }

    // 对404页面的封装
    private void respond404Page() {
        String path = Path.of(Config.rootPath, Config.notFoundPage).toString();
        byte[] body = Utils.fromFileToString(path);
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "text/html");
        responseHandle(STATUS_CODE.CODE_404, headers, body);
    }
}

package com.frontend.utils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ISAPI设备操作工具类
 * 用于与支持ISAPI协议的门禁设备进行交互
 */
public class ISAPIDeviceUtil {

    // 常量定义
    private static final int QR_CODE_SIZE = 300;
    private static final int AES_BLOCK_SIZE = 16;
    private static final int RSA_KEY_SIZE = 1024;
    private static final String DEFAULT_SECRET_KEY = "YourSuperSecretKey123";

    // 是否启用详细日志
    private static boolean enableDetailedLogging = true;

    /**
     * 设置是否启用详细日志
     * @param enable 是否启用
     */
    public static void setDetailedLogging(boolean enable) {
        enableDetailedLogging = enable;
    }

    /**
     * 设备配置类
     */
    public static class DeviceConfig {
        private String ip;
        private String port;
        private String username;
        private String password;

        public DeviceConfig(String ip, String port, String username, String password) {
            this.ip = ip;
            this.port = port;
            this.username = username;
            this.password = password;
        }

        // Getters
        public String getIp() { return ip; }
        public String getPort() { return port; }
        public String getUsername() { return username; }
        public String getPassword() { return password; }

        @Override
        public String toString() {
            return String.format("DeviceConfig{ip='%s', port='%s', username='%s'}", ip, port, username);
        }
    }

    /**
     * 用户信息类
     */
    public static class UserInfo {
        private String employeeNo;
        private String name;
        private String password;
        private String userType = "normal";
        private boolean checkUser = false;
        private String userVerifyMode = "QRCode";
        private LocalDateTime beginTime;
        private LocalDateTime endTime;

        public UserInfo(String employeeNo, String name, String password) {
            this.employeeNo = employeeNo;
            this.name = name;
            this.password = password;
            this.beginTime = LocalDateTime.now();
            this.endTime = beginTime.plusYears(10);
        }

        // Setters for optional fields
        public UserInfo setUserType(String userType) { this.userType = userType; return this; }
        public UserInfo setCheckUser(boolean checkUser) { this.checkUser = checkUser; return this; }
        public UserInfo setUserVerifyMode(String userVerifyMode) { this.userVerifyMode = userVerifyMode; return this; }
        public UserInfo setBeginTime(LocalDateTime beginTime) { this.beginTime = beginTime; return this; }
        public UserInfo setEndTime(LocalDateTime endTime) { this.endTime = endTime; return this; }

        // Getters
        public String getEmployeeNo() { return employeeNo; }
        public String getName() { return name; }
        public String getPassword() { return password; }
        public String getUserType() { return userType; }
        public boolean isCheckUser() { return checkUser; }
        public String getUserVerifyMode() { return userVerifyMode; }
        public LocalDateTime getBeginTime() { return beginTime; }
        public LocalDateTime getEndTime() { return endTime; }

        @Override
        public String toString() {
            return String.format("UserInfo{employeeNo='%s', name='%s', userType='%s'}", employeeNo, name, userType);
        }
    }

    /**
     * API响应结果类 - 增强版本，包含详细信息
     */
    public static class ApiResponse {
        private boolean success;
        private int responseCode;
        private String responseBody;
        private String errorMessage;
        private Map<String, List<String>> responseHeaders;
        private String requestDetails;
        private long responseTime;

        public ApiResponse(boolean success, int responseCode, String responseBody, String errorMessage) {
            this.success = success;
            this.responseCode = responseCode;
            this.responseBody = responseBody;
            this.errorMessage = errorMessage;
            this.responseHeaders = new HashMap<>();
            this.responseTime = System.currentTimeMillis();
        }

        // Getters
        public boolean isSuccess() { return success; }
        public int getResponseCode() { return responseCode; }
        public String getResponseBody() { return responseBody; }
        public String getErrorMessage() { return errorMessage; }
        public Map<String, List<String>> getResponseHeaders() { return responseHeaders; }
        public String getRequestDetails() { return requestDetails; }
        public long getResponseTime() { return responseTime; }

        // Setters for additional info
        public ApiResponse setResponseHeaders(Map<String, List<String>> headers) {
            this.responseHeaders = headers;
            return this;
        }
        public ApiResponse setRequestDetails(String details) {
            this.requestDetails = details;
            return this;
        }

        /**
         * 获取完整的响应信息
         */
        public String getFullResponseInfo() {
            StringBuilder info = new StringBuilder();
            info.append("=== API 响应详情 ===\n");
            info.append("成功: ").append(success).append("\n");
            info.append("响应码: ").append(responseCode).append("\n");

            if (requestDetails != null) {
                info.append("请求详情: ").append(requestDetails).append("\n");
            }

            if (responseHeaders != null && !responseHeaders.isEmpty()) {
                info.append("响应头: ").append(responseHeaders).append("\n");
            }

            if (responseBody != null) {
                info.append("响应体: ").append(responseBody).append("\n");
            }

            if (errorMessage != null) {
                info.append("错误信息: ").append(errorMessage).append("\n");
            }

            info.append("==================\n");
            return info.toString();
        }

        @Override
        public String toString() {
            return getFullResponseInfo();
        }
    }

    /**
     * 添加用户到设备
     * @param config 设备配置
     * @param userInfo 用户信息
     * @param secretKey QR码签名密钥（可为null，使用默认密钥）
     * @return API响应结果
     */
    public static ApiResponse addUser(DeviceConfig config, UserInfo userInfo, String secretKey) throws Exception {
        long startTime = System.currentTimeMillis();

            log("开始添加用户: " + userInfo);
            log("设备配置: " + config);

            // 使用默认密钥如果未提供
            String qrSecretKey = secretKey != null ? secretKey : DEFAULT_SECRET_KEY;
            log("使用QR密钥: " + (secretKey != null ? "自定义密钥" : "默认密钥"));

            // 生成安全QR码内容
            String qrContent = generateSecureQRCodeContent(userInfo.getEmployeeNo(), qrSecretKey);
            log("生成QR码内容: " + qrContent.substring(0, Math.min(50, qrContent.length())) + "...");

            // 构建JSON请求体
            String jsonPayload = buildUserJson(userInfo, qrContent);
            log("构建JSON请求体: " + jsonPayload);

            // 发送请求
            ApiResponse response = sendAddUserRequest(config, jsonPayload);

            long endTime = System.currentTimeMillis();
            response.responseTime = endTime - startTime;

            log("请求完成，耗时: " + (endTime - startTime) + "ms");
            log("最终响应: " + response.getFullResponseInfo());

            return response;


    }

    /**
     * 测试设备连接
     * @param config 设备配置
     * @return 测试结果
     */
    public static ApiResponse testDeviceConnection(DeviceConfig config) {
        long startTime = System.currentTimeMillis();

        try {
            log("开始测试设备连接: " + config);

            String url = "http://" + config.getIp() + ":" + config.getPort() + "/ISAPI/AccessControl/UserInfo/Record?format=json";
            log("测试URL: " + url);

            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(10000);

            int responseCode = conn.getResponseCode();
            String wwwAuth = conn.getHeaderField("WWW-Authenticate");
            Map<String, List<String>> headers = conn.getHeaderFields();

            log("响应码: " + responseCode);
            log("WWW-Authenticate: " + wwwAuth);
            log("所有响应头: " + headers);

            String message = String.format("设备连接测试 - 响应码: %d, 认证方式: %s", responseCode, wwwAuth);
            boolean success = responseCode == 401 || responseCode == 200; // 401表示需要认证，这是正常的

            ApiResponse response = new ApiResponse(success, responseCode, message, success ? null : "设备连接失败");
            response.setResponseHeaders(headers);
            response.responseTime = System.currentTimeMillis() - startTime;

            return response;

        } catch (Exception e) {
            log("测试设备连接时发生异常: " + e.getMessage());
            e.printStackTrace();

            ApiResponse response = new ApiResponse(false, -1, null, "测试设备连接时发生错误: " + e.getMessage());
            response.responseTime = System.currentTimeMillis() - startTime;
            return response;
        }
    }

    // ==================== 私有方法 ====================

    private static ApiResponse sendAddUserRequest(DeviceConfig config, String jsonBody) throws Exception {
        String url = "http://" + config.getIp() + ":" + config.getPort() + "/ISAPI/AccessControl/UserInfo/Record?format=json";
        String uriPath = "/ISAPI/AccessControl/UserInfo/Record?format=json";

        log("准备发送添加用户请求到: " + url);
        log("请求体: " + jsonBody);

        // 第一次请求，获取Digest challenge
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        conn.setRequestProperty("Accept", "application/json");
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(30000);

        log("发送第一次请求（获取认证challenge）...");
        int responseCode = conn.getResponseCode();
        Map<String, List<String>> headers = conn.getHeaderFields();
        String firstResponseBody = getResponseBody(conn);

        log("第一次请求响应码: " + responseCode);
        log("第一次请求响应头: " + headers);
        log("第一次请求响应体: " + firstResponseBody);

        if (responseCode == 401) {
            String wwwAuth = conn.getHeaderField("WWW-Authenticate");
            log("收到401认证挑战: " + wwwAuth);

            if (wwwAuth != null && wwwAuth.startsWith("Digest")) {
                log("开始计算Digest认证...");

                // 计算Digest认证
                String digestAuth = calculateDigestAuth(config.getUsername(), config.getPassword(), "POST", uriPath, wwwAuth);

                if (digestAuth == null) {
                    log("❌ 计算Digest认证失败");
                    return new ApiResponse(false, responseCode, firstResponseBody, "计算Digest认证失败")
                            .setResponseHeaders(headers)
                            .setRequestDetails("第一次请求失败，无法计算Digest认证");
                }

                log("计算出的Digest认证: " + digestAuth);

                // 第二次请求，带上Digest认证
                log("发送第二次请求（带认证信息）...");
                conn = (HttpURLConnection) new URL(url).openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setRequestProperty("Accept", "application/json");
                conn.setRequestProperty("Authorization", digestAuth);
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(30000);

                conn.setDoOutput(true);
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
                    os.flush();
                }

                responseCode = conn.getResponseCode();
                headers = conn.getHeaderFields();
                String responseBody = getResponseBody(conn);
                boolean success = responseCode >= 200 && responseCode < 300;

                log("第二次请求响应码: " + responseCode);
                log("第二次请求响应头: " + headers);
                log("第二次请求响应体: " + responseBody);
                log("请求是否成功: " + success);

                String requestDetails = String.format("POST %s with Digest auth, Content-Length: %d",
                        url, jsonBody.getBytes(StandardCharsets.UTF_8).length);

                return new ApiResponse(success, responseCode, responseBody, success ? null : "添加用户失败")
                        .setResponseHeaders(headers)
                        .setRequestDetails(requestDetails);
            } else {
                log("❌ 未收到Digest认证挑战或认证方式不支持");
                return new ApiResponse(false, responseCode, firstResponseBody, "设备不支持Digest认证或认证信息异常")
                        .setResponseHeaders(headers)
                        .setRequestDetails("第一次请求收到401但无有效的Digest认证挑战");
            }
        } else {
            // 如果不是401，可能设备不需要认证或者有其他问题
            log("第一次请求未收到401，响应码: " + responseCode);
            boolean success = responseCode >= 200 && responseCode < 300;

            String requestDetails = String.format("POST %s without auth, Content-Length: %d",
                    url, jsonBody.getBytes(StandardCharsets.UTF_8).length);

            return new ApiResponse(success, responseCode, firstResponseBody,
                    success ? null : "请求失败，响应码: " + responseCode)
                    .setResponseHeaders(headers)
                    .setRequestDetails(requestDetails);
        }
    }

    private static String calculateDigestAuth(String username, String password, String method, String uri, String wwwAuthHeader) {
        try {
            log("计算Digest认证参数:");
            log("- Method: " + method);
            log("- URI: " + uri);
            log("- WWW-Authenticate: " + wwwAuthHeader);

            Map<String, String> digestParams = parseDigestHeader(wwwAuthHeader);

            // 打印解析出的参数
            log("解析出的Digest参数:");
            digestParams.forEach((k, v) -> log("- " + k + ": " + v));

            String realm = digestParams.get("realm");
            String nonce = digestParams.get("nonce");
            String qop = digestParams.get("qop");
            String opaque = digestParams.get("opaque");

            if (realm == null || nonce == null) {
                log("❌ 缺少必要参数 realm 或 nonce");
                return null;
            }

            String ha1 = md5(username + ":" + realm + ":" + password);
            String ha2 = md5(method + ":" + uri);

            log("计算Hash:");
            log("- HA1: " + ha1);
            log("- HA2: " + ha2);

            String response;
            StringBuilder authHeader = new StringBuilder("Digest ");
            authHeader.append("username=\"").append(username).append("\", ");
            authHeader.append("realm=\"").append(realm).append("\", ");
            authHeader.append("nonce=\"").append(nonce).append("\", ");
            authHeader.append("uri=\"").append(uri).append("\"");

            if (qop != null && qop.contains("auth")) {
                String cnonce = generateCnonce();
                String nc = "00000001";
                response = md5(ha1 + ":" + nonce + ":" + nc + ":" + cnonce + ":auth:" + ha2);
                authHeader.append(", qop=auth");
                authHeader.append(", nc=").append(nc);
                authHeader.append(", cnonce=\"").append(cnonce).append("\"");

                log("使用qop=auth模式");
                log("- cnonce: " + cnonce);
                log("- nc: " + nc);
            } else {
                response = md5(ha1 + ":" + nonce + ":" + ha2);
                log("使用简单模式 (无qop)");
            }

            authHeader.append(", response=\"").append(response).append("\"");

            if (opaque != null && !opaque.isEmpty()) {
                authHeader.append(", opaque=\"").append(opaque).append("\"");
            }

            log("最终Response: " + response);
            return authHeader.toString();

        } catch (Exception e) {
            log("❌ 计算Digest认证时发生错误: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private static Map<String, String> parseDigestHeader(String header) {
        Map<String, String> params = new HashMap<>();

        if (header.startsWith("Digest ")) {
            header = header.substring(7);
        }

        Pattern pattern = Pattern.compile("(\\w+)=(?:\"([^\"]*)\"|([^,\\s]+))");
        Matcher matcher = pattern.matcher(header);

        while (matcher.find()) {
            String key = matcher.group(1);
            String value = matcher.group(2) != null ? matcher.group(2) : matcher.group(3);
            params.put(key, value);
            log("解析参数: " + key + " = " + value);
        }

        return params;
    }

    private static String buildUserJson(UserInfo userInfo, String qrContent) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        String beginTimeStr = userInfo.getBeginTime().format(formatter);
        String endTimeStr = userInfo.getEndTime().format(formatter);

        StringBuilder json = new StringBuilder();
        json.append("{\n")
                .append("  \"UserInfo\": {\n")
                .append("    \"employeeNo\": \"").append(userInfo.getEmployeeNo()).append("\",\n")
                .append("    \"name\": \"").append(userInfo.getName()).append("\",\n")
                .append("    \"userType\": \"").append(userInfo.getUserType()).append("\",\n")
                .append("    \"Valid\": {\n")
                .append("      \"enable\": true,\n")
                .append("      \"beginTime\": \"").append(beginTimeStr).append("\",\n")
                .append("      \"endTime\": \"").append(endTimeStr).append("\",\n")
                .append("      \"timeType\": \"local\"\n")
                .append("    },\n")
                .append("    \"doorRight\": \"1\",\n")
                .append("    \"RightPlan\": [\n")
                .append("      {\n")
                .append("        \"doorNo\": 1,\n")
                .append("        \"planTemplateNo\": \"1\"\n")
                .append("      }\n")
                .append("    ],\n")
                .append("    \"password\": \"").append(userInfo.getPassword()).append("\",\n")
                .append("    \"userVerifyMode\": \"\",\n")
                .append("    \"checkUser\": ").append(false).append("\n")
                .append("  }\n")
                .append("}");

        return json.toString();
    }

    private static String generateSecureQRCodeContent(String employeeNo, String secretKey) throws Exception {
        LocalDateTime expireTime = LocalDateTime.now().plusMonths(6);
        String expireAt = expireTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));

        String dataJson = String.format("{\"employeeNo\":\"%s\",\"expireAt\":\"%s\"}", employeeNo, expireAt);

        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(keySpec);
        byte[] hmacBytes = mac.doFinal(dataJson.getBytes(StandardCharsets.UTF_8));
        String signature = Base64.getEncoder().encodeToString(hmacBytes);

        String fullJson = String.format(
                "{\"employeeNo\":\"%s\",\"expireAt\":\"%s\",\"signature\":\"%s\"}",
                employeeNo, expireAt, signature);

        return Base64.getEncoder().encodeToString(fullJson.getBytes(StandardCharsets.UTF_8));
    }

    private static String getResponseBody(HttpURLConnection conn) {
        try (Scanner scanner = new Scanner(
                conn.getResponseCode() >= 200 && conn.getResponseCode() < 300
                        ? conn.getInputStream()
                        : conn.getErrorStream(),
                StandardCharsets.UTF_8)) {

            StringBuilder body = new StringBuilder();
            while (scanner.hasNextLine()) {
                body.append(scanner.nextLine()).append("\n");
            }
            return body.toString().trim();

        } catch (Exception e) {
            return "无法读取响应内容: " + e.getMessage();
        }
    }

    private static String generateCnonce() {
        byte[] bytes = new byte[8];
        new SecureRandom().nextBytes(bytes);
        return bytesToHex(bytes);
    }

    private static String md5(String input) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(hash);
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    /**
     * 日志输出方法
     */
    private static void log(String message) {
        if (enableDetailedLogging) {
            System.out.println("[ISAPIDeviceUtil] " + message);
        }
    }
}
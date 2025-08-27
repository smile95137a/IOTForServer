package com.frontend.ISAPI;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.interfaces.RSAPublicKey;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ISAPIAddPersonTest {

    // ✅ 建议使用环境变量，避免硬编码敏感信息
    private static final String DEVICE_IP = System.getenv("DEVICE_IP") != null ?
            System.getenv("DEVICE_IP") : "192.168.1.111";
    private static final String PORT = System.getenv("DEVICE_PORT") != null ?
            System.getenv("DEVICE_PORT") : "80";
    private static final String USERNAME = System.getenv("DEVICE_USERNAME") != null ?
            System.getenv("DEVICE_USERNAME") : "admin";
    private static final String PASSWORD = System.getenv("DEVICE_PASSWORD") != null ?
            System.getenv("DEVICE_PASSWORD") : "Handsome0202@";

    // ✅ 定义常数
    private static final int QR_CODE_SIZE = 300;
    private static final int AES_BLOCK_SIZE = 16;
    private static final int RSA_KEY_SIZE = 1024;

    public static void main(String[] args) throws Exception {
        // 先测试设备的认证要求
        testDeviceAuth();

        SecurityTokens tokens = generateSecurityTokens(DEVICE_IP, PORT, PASSWORD);
        if (tokens == null) {
            System.err.println("无法取得 security / iv");
            return;
        }

        System.out.println("取得 security 与 iv：");
        System.out.println("security: " + tokens.security);
        System.out.println("iv: " + tokens.iv);
        System.out.println("---");

        // 加密密码（使用 AES key）
        String encryptedPassword = encryptPassword("123456", tokens.aesKey);

        // ✅ 插入：产出安全 QRCode
        String employeeNo = "USER001";
        String secretKey = "YourSuperSecretKey123";

        // 方法2: 使用修正后的 Digest 认证
        System.out.println("尝试方法2: 修正后的 Digest 认证");
        String jsonPayload2 = createTestPersonJson("123456");
        System.out.println("方法2请求JSON: " + jsonPayload2);
        String response2 = addPersonMethod2(DEVICE_IP, PORT, USERNAME, PASSWORD, jsonPayload2);
        System.out.println("方法2回应：\n" + response2);
        System.out.println("---");
    }

    static class SecurityTokens {
        String security;
        String iv;
        byte[] aesKey;

        SecurityTokens(String security, String iv, byte[] aesKey) {
            this.security = security;
            this.iv = iv;
            this.aesKey = aesKey;
        }
    }

    private static void testDeviceAuth() {
        try {
            String url = "http://" + DEVICE_IP + ":" + PORT + "/ISAPI/AccessControl/UserInfo/Record?format=json";
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("GET");

            int responseCode = conn.getResponseCode();
            String wwwAuth = conn.getHeaderField("WWW-Authenticate");

            System.out.println("=== 设备认证测试 ===");
            System.out.println("Response Code: " + responseCode);
            System.out.println("WWW-Authenticate: " + wwwAuth);
            System.out.println("====================");

        } catch (Exception e) {
            System.err.println("测试设备认证时发生错误: " + e.getMessage());
        }
    }

    // ✅ 修正后的 Digest 认证方法
    private static String addPersonMethod2(String ip, String port, String username, String password, String jsonBody) throws Exception {
        String url = "http://" + ip + ":" + port + "/ISAPI/AccessControl/UserInfo/Record?format=json";
        // ✅ 关键修正：使用相对路径作为 URI
        String uriPath = "/ISAPI/AccessControl/UserInfo/Record?format=json";

        // 第一次请求，取得 Digest challenge
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        conn.setRequestProperty("Accept", "application/json");

        int responseCode = conn.getResponseCode();
        System.out.println("第一次请求 Response Code: " + responseCode);

        if (responseCode == 401) {
            String wwwAuth = conn.getHeaderField("WWW-Authenticate");
            System.out.println("收到 WWW-Authenticate: " + wwwAuth);

            if (wwwAuth != null && wwwAuth.startsWith("Digest")) {
                // ✅ 使用修正后的 URI 路径计算 Digest 认证
                String digestAuth = calculateDigestAuth(username, password, "POST", uriPath, wwwAuth);
                System.out.println("计算出的 Digest Auth: " + digestAuth);

                // 第二次请求，带上 Digest 认证
                conn = (HttpURLConnection) new URL(url).openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setRequestProperty("Accept", "application/json");
                conn.setRequestProperty("Authorization", digestAuth);

                conn.setDoOutput(true);
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
                    os.flush();
                }

                return getResponse(conn);
            }
        }

        return getResponse(conn);
    }

    // ✅ 改进的 Digest 认证计算方法
    private static String calculateDigestAuth(String username, String password, String method, String uri, String wwwAuthHeader) {
        try {
            System.out.println("计算 Digest 认证参数:");
            System.out.println("- Method: " + method);
            System.out.println("- URI: " + uri);
            System.out.println("- WWW-Authenticate: " + wwwAuthHeader);

            Map<String, String> digestParams = parseDigestHeader(wwwAuthHeader);

            // 打印解析出的参数
            System.out.println("解析出的 Digest 参数:");
            digestParams.forEach((k, v) -> System.out.println("- " + k + ": " + v));

            String realm = digestParams.get("realm");
            String nonce = digestParams.get("nonce");
            String qop = digestParams.get("qop");
            String opaque = digestParams.get("opaque");

            if (realm == null || nonce == null) {
                System.err.println("❌ 缺少必要参数 realm 或 nonce");
                return null;
            }

            // 计算 digest response
            String ha1 = md5(username + ":" + realm + ":" + password);
            String ha2 = md5(method + ":" + uri);

            System.out.println("计算 Hash:");
            System.out.println("- HA1: " + ha1);
            System.out.println("- HA2: " + ha2);

            String response;
            StringBuilder authHeader = new StringBuilder("Digest ");
            authHeader.append("username=\"").append(username).append("\", ");
            authHeader.append("realm=\"").append(realm).append("\", ");
            authHeader.append("nonce=\"").append(nonce).append("\", ");
            authHeader.append("uri=\"").append(uri).append("\"");

            // ✅ 改进 qop 处理
            if (qop != null && qop.contains("auth")) {
                String cnonce = generateCnonce();
                String nc = "00000001";
                response = md5(ha1 + ":" + nonce + ":" + nc + ":" + cnonce + ":auth:" + ha2);
                authHeader.append(", qop=auth");
                authHeader.append(", nc=").append(nc);
                authHeader.append(", cnonce=\"").append(cnonce).append("\"");

                System.out.println("使用 qop=auth 模式");
                System.out.println("- cnonce: " + cnonce);
                System.out.println("- nc: " + nc);
            } else {
                response = md5(ha1 + ":" + nonce + ":" + ha2);
                System.out.println("使用简单模式 (无 qop)");
            }

            authHeader.append(", response=\"").append(response).append("\"");

            if (opaque != null && !opaque.isEmpty()) {
                authHeader.append(", opaque=\"").append(opaque).append("\"");
            }

            System.out.println("最终 Response: " + response);
            return authHeader.toString();

        } catch (Exception e) {
            System.err.println("❌ 计算 Digest 认证时发生错误: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // ✅ 改进的 Digest 标头解析方法
    private static Map<String, String> parseDigestHeader(String header) {
        Map<String, String> params = new HashMap<>();

        // 移除 "Digest " 前缀
        if (header.startsWith("Digest ")) {
            header = header.substring(7);
        }

        // ✅ 改进的正则表达式，更好地处理引号和逗号
        Pattern pattern = Pattern.compile("(\\w+)=(?:\"([^\"]*)\"|([^,\\s]+))");
        Matcher matcher = pattern.matcher(header);

        while (matcher.find()) {
            String key = matcher.group(1);
            String value = matcher.group(2) != null ? matcher.group(2) : matcher.group(3);
            params.put(key, value);
            System.out.println("解析参数: " + key + " = " + value);
        }

        return params;
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

    // ✅ 改进的回应处理方法，使用 try-with-resources
    private static String getResponse(HttpURLConnection conn) throws Exception {
        StringBuilder resp = new StringBuilder();
        resp.append("Response Code: ").append(conn.getResponseCode()).append("\n");
        resp.append("Response Headers: ").append(conn.getHeaderFields()).append("\n");
        resp.append("Response Body: ");

        try (Scanner scanner = new Scanner(
                conn.getResponseCode() >= 200 && conn.getResponseCode() < 300
                        ? conn.getInputStream()
                        : conn.getErrorStream(),
                StandardCharsets.UTF_8)) {

            while (scanner.hasNextLine()) {
                resp.append(scanner.nextLine()).append("\n");
            }
        }

        return resp.toString();
    }

    // 原有的方法保持不变
    private static SecurityTokens generateSecurityTokens(String ip, String port, String password) throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(RSA_KEY_SIZE);
        KeyPair keyPair = keyGen.generateKeyPair();

        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        PrivateKey privateKey = keyPair.getPrivate();

        byte[] modulus = publicKey.getModulus().toByteArray();
        if (modulus[0] == 0 && modulus.length > 128) {
            modulus = Arrays.copyOfRange(modulus, 1, modulus.length);
        }

        if (modulus.length < 128) {
            byte[] padded = new byte[128];
            System.arraycopy(modulus, 0, padded, 128 - modulus.length, modulus.length);
            modulus = padded;
        }

        String modulusHex = bytesToHex(modulus);
        String pubKeyBase64 = Base64.getEncoder().encodeToString(modulusHex.getBytes(StandardCharsets.UTF_8));

        String publicKeyXml = "<PublicKey><key>" + pubKeyBase64 + "</key></PublicKey>";
        String url = "http://" + ip + ":" + port + "/ISAPI/Security/challenge";
        String response = sendXmlRequest(url, "POST", publicKeyXml, USERNAME, PASSWORD);
        if (response == null) return null;

        String encryptedKey = extractChallenge(response);
        if (encryptedKey == null) return null;

        byte[] encrypted = Base64.getDecoder().decode(encryptedKey);
        byte[] encryptedHex = hexStringToBytes(new String(encrypted, StandardCharsets.UTF_8));

        Cipher rsa = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        rsa.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] random32 = rsa.doFinal(encryptedHex);

        byte[] aesKey = Arrays.copyOfRange(random32, 0, 16);
        byte[] prefix = Arrays.copyOfRange(random32, 0, 16);
        byte[] passwordBytes = password.getBytes(StandardCharsets.UTF_8);

        byte[] plain = new byte[prefix.length + passwordBytes.length];
        System.arraycopy(prefix, 0, plain, 0, prefix.length);
        System.arraycopy(passwordBytes, 0, plain, prefix.length, passwordBytes.length);

        int paddedLength = ((plain.length + 15) / 16) * 16;
        byte[] padded = new byte[paddedLength];
        System.arraycopy(plain, 0, padded, 0, plain.length);

        Cipher aes = Cipher.getInstance("AES/ECB/NoPadding");
        aes.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(aesKey, "AES"));
        byte[] encryptedSecurity = aes.doFinal(padded);

        String security = Base64.getEncoder().encodeToString(encryptedSecurity);
        String iv = Base64.getEncoder().encodeToString(prefix);

        return new SecurityTokens(security, iv, aesKey);
    }

    private static String encryptPassword(String plainPassword, byte[] aesKey) throws Exception {
        byte[] passwordBytes = plainPassword.getBytes(StandardCharsets.UTF_8);
        int paddedLength = ((passwordBytes.length + AES_BLOCK_SIZE - 1) / AES_BLOCK_SIZE) * AES_BLOCK_SIZE;
        byte[] padded = new byte[paddedLength];
        System.arraycopy(passwordBytes, 0, padded, 0, passwordBytes.length);

        Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(aesKey, "AES"));
        byte[] encrypted = cipher.doFinal(padded);
        return Base64.getEncoder().encodeToString(encrypted);
    }

    private static String sendXmlRequest(String urlStr, String method, String body, String user, String pwd) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
        conn.setRequestMethod(method);
        conn.setRequestProperty("Content-Type", "application/xml; charset=UTF-8");
        conn.setRequestProperty("Accept", "application/xml");

        if (user != null && pwd != null) {
            String auth = user + ":" + pwd;
            String encoded = Base64.getEncoder().encodeToString(auth.getBytes());
            conn.setRequestProperty("Authorization", "Basic " + encoded);
        }

        conn.setDoOutput(true);
        try (OutputStream os = conn.getOutputStream()) {
            os.write(body.getBytes(StandardCharsets.UTF_8));
            os.flush();
        }

        try (Scanner scanner = new Scanner(conn.getInputStream(), StandardCharsets.UTF_8)) {
            StringBuilder sb = new StringBuilder();
            while (scanner.hasNextLine()) {
                sb.append(scanner.nextLine()).append("\n");
            }
            return sb.toString();
        }
    }

    private static String extractChallenge(String xml) {
        int start = xml.indexOf("<key>");
        int end = xml.indexOf("</key>");
        if (start != -1 && end != -1) {
            return xml.substring(start + 5, end).trim();
        }
        return null;
    }

    // ✅ 修正后的 JSON 构建方法 - 使用正确的时间格式
    private static String createTestPersonJson(String password) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endTime = now.plusYears(1);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

        String beginTimeStr = now.format(formatter);
        String endTimeStr = endTime.format(formatter);
        String employeeNo = "TEST" + (System.currentTimeMillis() % 10000);

        // ✅ Secure QRCode
        String secretKey = "YourSuperSecretKey123";
        String secureQRContent = null;
        try {
            secureQRContent = generateSecureQRCodeContent(employeeNo, secretKey);
            System.out.println("✅ 已產出安全 QRCode：" + secureQRContent);
        } catch (Exception e) {
            System.err.println("❌ 無法產出安全 QRCode: " + e.getMessage());
        }

        StringBuilder json = new StringBuilder();
        json.append("{\n")
                .append("  \"UserInfo\": {\n")
                .append("    \"employeeNo\": \"").append(employeeNo).append("\",\n")
                .append("    \"name\": \"測試人員\",\n")
                .append("    \"userType\": \"normal\",\n")
                .append("    \"QRCodeInfo\": \"").append(secureQRContent).append("\",\n") // ✅ 關鍵
                .append("    \"Valid\": {\n")
                .append("      \"enable\": true,\n")
                .append("      \"beginTime\": \"").append(beginTimeStr).append("\",\n")
                .append("      \"endTime\": \"").append(endTimeStr).append("\",\n")
                .append("      \"timeType\": \"local\"\n")
                .append("    },\n")
                .append("    \"password\": \"").append(password).append("\",\n")
                .append("    \"userVerifyMode\": \"QRCode\",\n") // ✅ 使用 QRCode 驗證
                .append("    \"checkUser\": false\n")
                .append("  }\n")
                .append("}");
        return json.toString();
    }

    private static String generateSecureQRCodeContent(String employeeNo, String secretKey) throws Exception {
        String expireAt = "2025-06-30T23:59:59";

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

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private static byte[] hexStringToBytes(String hex) {
        int len = hex.length();
        byte[] bytes = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            bytes[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return bytes;
    }
}
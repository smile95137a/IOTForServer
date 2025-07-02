package com.frontend.utils;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DoorControlUtil {
    private static String BASE_URL = "http://192.168.1.113"; // 可改為讀取配置
    private static final String USERNAME = "admin";
    private static final String PASSWORD = "Handsome0202@";

    public static boolean openDoor(String storeIP , String doorId) {
        try {
            BASE_URL = "http://" + storeIP;
            String urlString = BASE_URL + "/ISAPI/AccessControl/RemoteControl/door/" + doorId;
            System.out.println(urlString);
            String requestBody = "<RemoteControlDoor version=\"2.0\" xmlns=\"http://www.isapi.org/ver20/XMLSchema\">" +
                    "<cmd>open</cmd>" +
                    "</RemoteControlDoor>";

            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("PUT");
            connection.setRequestProperty("Content-Type", "application/xml");
            connection.setDoOutput(true);

            try (OutputStream os = connection.getOutputStream()) {
                os.write(requestBody.getBytes(StandardCharsets.UTF_8));
            }

            int responseCode = connection.getResponseCode();

            if (responseCode == 401) {
                String wwwAuth = connection.getHeaderField("WWW-Authenticate");
                if (wwwAuth != null && wwwAuth.startsWith("Digest")) {
                    String digestAuth = calculateDigestAuth(USERNAME, PASSWORD, "PUT", urlString, wwwAuth);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("PUT");
                    connection.setRequestProperty("Content-Type", "application/xml");
                    connection.setRequestProperty("Authorization", digestAuth);
                    connection.setDoOutput(true);

                    try (OutputStream os = connection.getOutputStream()) {
                        os.write(requestBody.getBytes(StandardCharsets.UTF_8));
                    }
                    responseCode = connection.getResponseCode();
                }
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    responseCode >= 200 && responseCode < 300 ?
                            connection.getInputStream() :
                            connection.getErrorStream()
            ));

            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line).append("\n");
            }
            reader.close();

            if (responseCode >= 200 && responseCode < 300 && !response.toString().isBlank()) {
                parseResponseStatus(response.toString());
            }

            return responseCode >= 200 && responseCode < 300;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static void parseResponseStatus(String responseXml) {
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = builder.parse(new ByteArrayInputStream(responseXml.getBytes()));
            NodeList nodeList = document.getDocumentElement().getChildNodes();
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if ("statusString".equals(node.getNodeName())) {
                    System.out.println("OPEN DOOR: " + node.getTextContent());
                }
            }
        } catch (Exception e) {
            System.err.println("XML解析錯誤: " + e.getMessage());
        }
    }

    private static String calculateDigestAuth(String username, String password, String method, String uri, String wwwAuth) throws Exception {
        String realm = extractValue(wwwAuth, "realm");
        String nonce = extractValue(wwwAuth, "nonce");
        String qop = extractValue(wwwAuth, "qop");
        String opaque = extractValue(wwwAuth, "opaque");
        String cnonce = md5(String.valueOf(System.currentTimeMillis()));
        String nc = "00000001";

        String ha1 = md5(username + ":" + realm + ":" + password);
        String ha2 = md5(method + ":" + uri);

        String response = (qop != null && qop.contains("auth")) ?
                md5(ha1 + ":" + nonce + ":" + nc + ":" + cnonce + ":" + qop + ":" + ha2) :
                md5(ha1 + ":" + nonce + ":" + ha2);

        StringBuilder authHeader = new StringBuilder("Digest ");
        authHeader.append("username=\"").append(username).append("\", ");
        authHeader.append("realm=\"").append(realm).append("\", ");
        authHeader.append("nonce=\"").append(nonce).append("\", ");
        authHeader.append("uri=\"").append(uri).append("\", ");
        if (qop != null && qop.contains("auth")) {
            authHeader.append("qop=").append(qop).append(", ");
            authHeader.append("nc=").append(nc).append(", ");
            authHeader.append("cnonce=\"").append(cnonce).append("\", ");
        }
        authHeader.append("response=\"").append(response).append("\"");
        if (opaque != null) {
            authHeader.append(", opaque=\"").append(opaque).append("\"");
        }

        return authHeader.toString();
    }

    private static String extractValue(String header, String key) {
        Matcher matcher = Pattern.compile(key + "=\"([^\"]+)\"").matcher(header);
        return matcher.find() ? matcher.group(1) : null;
    }

    private static String md5(String input) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}

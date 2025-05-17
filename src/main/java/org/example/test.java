package org.example;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class test {

    public static void main(String[] args) {
        try {
            // Bước 1: Load HTML trang sản phẩm
            String pageUrl = "https://cellphones.com.vn/dien-thoai-xiaomi-redmi-a3.html";
            Document doc = Jsoup.connect(pageUrl)
                    .userAgent("Mozilla/5.0")
                    .get();

            // Bước 2: Lấy product-id từ div
            Element commentDiv = doc.select("div#block-comment-cps").first();
            if (commentDiv == null) {
                System.out.println("❌ Không tìm thấy phần bình luận trong HTML.");
                return;
            }

            String productId = commentDiv.attr("product-id");
            System.out.println("➡️ Tìm thấy product-id: " + productId);

            // Bước 3: Gọi API để lấy comment
            String apiUrl = "https://cellphones.com.vn/index.php/rest/V1/cms-comment/product/" + productId;

            HttpURLConnection conn = (HttpURLConnection) new URL(apiUrl).openConnection();
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");
            conn.setRequestProperty("Accept", "application/json");

            int responseCode = conn.getResponseCode();

            if (responseCode == 200) {
                // OK - đọc JSON
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder jsonBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    jsonBuilder.append(line);
                }
                reader.close();

                // Bước 4: Parse JSON comment
                JSONArray comments = new JSONArray(jsonBuilder.toString());

                if (comments.isEmpty()) {
                    System.out.println("⚠️ Sản phẩm không có bình luận nào.");
                } else {
                    for (int i = 0; i < comments.length(); i++) {
                        JSONObject comment = comments.getJSONObject(i);
                        String name = comment.optString("customer_name", "Ẩn danh");
                        String content = comment.optString("question_content", "");
                        String answer = comment.optString("answer_content", "");

                        System.out.println("👤 " + name);
                        System.out.println("❓ " + content);
                        if (!answer.isEmpty()) {
                            System.out.println("✅ Trả lời: " + answer);
                        }
                        System.out.println("-----------------------------");
                    }
                }

            } else if (responseCode == 404) {
                System.out.println("⚠️ API trả về 404 - Không có dữ liệu comment cho sản phẩm.");
            } else {
                System.out.println("⚠️ Lỗi khi gọi API. HTTP status: " + responseCode);
            }

        } catch (Exception e) {
            System.out.println("❌ Đã xảy ra lỗi:");
            e.printStackTrace();
        }
    }
}

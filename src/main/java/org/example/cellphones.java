package org.example;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class cellphones {
    public static void main(String[] args) {
        // URL trang gốc chứa sản phẩm ban đầu
        String baseUrl = "https://cellphones.com.vn/mobile.html";
        // Endpoint GraphQL để load thêm sản phẩm (theo Developer Tools)
        String ajaxUrl = "https://api.cellphones.com.vn/v2/graphql/query";

        // Số lượng sản phẩm cần lấy (ví dụ: 100 sản phẩm)
        int totalNeeded = 100;
        // JSON array chứa toàn bộ sản phẩm
        JSONArray allProducts = new JSONArray();

        try {
            // 1. Lấy sản phẩm ban đầu từ trang gốc
            Document document = Jsoup.connect(baseUrl)
                    .userAgent("Mozilla/5.0")
                    .get();
            // Giả sử các sản phẩm ban đầu nằm trong thẻ có class "product-info-container product-item"
            Elements initialProducts = document.select(".product-info-container.product-item");

            for (Element phone : initialProducts) {
                JSONObject productJson = new JSONObject();
                String name = phone.select("h3").text();
                String price = phone.select(".product__price--show").text();
                String pictureUrl = phone.select("img.product__img").attr("src");

                productJson.put("name", name);
                productJson.put("price", price);
                productJson.put("picture_url", pictureUrl);

                allProducts.put(productJson);
                if (allProducts.length() >= totalNeeded) {
                    break;
                }
            }

            // 2. Nếu chưa đủ sản phẩm, gọi AJAX (mô phỏng "Xem thêm")
            // Ví dụ: sử dụng tham số "page" để phân trang, mỗi request trả về 20 sản phẩm
            int page = 1;
            while (allProducts.length() < totalNeeded) {
                // Xây dựng payload JSON cho request AJAX
                JSONObject requestBodyObj = new JSONObject();
                requestBodyObj.put("operationName", "getProducts");

                JSONObject variables = new JSONObject();
                variables.put("page", page);
                variables.put("limit", 20); // Số sản phẩm mỗi trang (cần điều chỉnh theo thực tế)
                requestBodyObj.put("variables", variables);

                // Query GraphQL để lấy sản phẩm (bạn cần copy chính xác từ Developer Tools)
                requestBodyObj.put("query", "query getProducts($page: Int, $limit: Int){ products(page: $page, limit: $limit){ general { name attributes { image } } filterable { special_price price } } }");

                String requestBody = requestBodyObj.toString();

                // Gửi POST request với Jsoup
                Connection.Response ajaxResponse = Jsoup.connect(ajaxUrl)
                        .method(Connection.Method.POST)
                        .ignoreContentType(true) // Để xử lý JSON
                        .header("Content-Type", "application/json")
                        .userAgent("Mozilla/5.0")
                        .requestBody(requestBody)
                        .execute();

                String ajaxBody = ajaxResponse.body();
                JSONObject ajaxJson = new JSONObject(ajaxBody);
                JSONArray products = ajaxJson.getJSONObject("data").getJSONArray("products");

                // Nếu không có sản phẩm mới, dừng vòng lặp
                if (products.length() == 0) {
                    break;
                }

                // Duyệt qua từng sản phẩm trả về từ AJAX
                for (int i = 0; i < products.length() && allProducts.length() < totalNeeded; i++) {
                    JSONObject prodObj = products.getJSONObject(i);
                    JSONObject general = prodObj.getJSONObject("general");
                    String productName = general.getString("name");

                    // Lấy URL ảnh từ thuộc tính "image" trong "attributes"
                    JSONObject attributes = general.getJSONObject("attributes");
                    String ajaxPictureUrl = attributes.optString("image", "");

                    // Lấy giá: ưu tiên dùng special_price nếu có, nếu không thì dùng price
                    JSONObject filterable = prodObj.getJSONObject("filterable");
                    double productPrice = filterable.optDouble("special_price", 0.0);
                    if (productPrice == 0.0) {
                        productPrice = filterable.optDouble("price", 0.0);
                    }

                    JSONObject ajaxProduct = new JSONObject();
                    ajaxProduct.put("name", productName);
                    ajaxProduct.put("price", productPrice);
                    ajaxProduct.put("picture_url", ajaxPictureUrl);

                    allProducts.put(ajaxProduct);
                }
                page++; // Tăng số trang để load sản phẩm tiếp theo
            }

            // 3. Lưu kết quả ra file JSON với định dạng đẹp (indent 4 khoảng trắng)
            String jsonOutput = allProducts.toString(4);
            try (BufferedWriter writer = new BufferedWriter(new FileWriter("products.json", StandardCharsets.UTF_8))) {
                writer.write(jsonOutput);
            }
            System.out.println("Đã lưu " + allProducts.length() + " sản phẩm vào file products.json");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

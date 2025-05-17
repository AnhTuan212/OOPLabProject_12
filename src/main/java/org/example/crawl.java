package org.example;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class crawl {
    public static void main(String[] args) {
        String url = "https://cellphones.com.vn/mobile.html";
        String jsonFile = "products_cellphones.json"; // Output JSON file

        // Mảng JSON để chứa các sản phẩm
        JSONArray productsArray = new JSONArray();

        try {
            Document document = Jsoup.connect(url).get();
            Elements phones = document.select(".product-info-container.product-item");

            for (Element phone : phones) {
                String name = phone.select("h3").text();
                String price = phone.select(".product__price--show").text(); //text là lấy nội dung giữa 2 thẻ <> ... <>
                String pictureUrl = phone.select("img.product__img").attr("src");

                // Debug: in ra console
                System.out.println(name + " - " + price + " - " + pictureUrl);

                // Tạo JSONObject cho sản phẩm
                JSONObject product = new JSONObject();
                product.put("name", name);
                product.put("price", price);
                product.put("picture_url", pictureUrl);

                // Thêm sản phẩm vào mảng
                productsArray.put(product);
            }

            // Ghi mảng JSON ra file, có format đẹp (indent 4 khoảng trắng)
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(jsonFile))) {
                writer.write(productsArray.toString(4));
            }

            System.out.println("Data successfully saved to " + jsonFile);
        } catch (IOException e) {
            System.out.println("Error writing to file: " + e.getMessage());
        }
    }
}

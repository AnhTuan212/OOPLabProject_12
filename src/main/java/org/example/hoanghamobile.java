package org.example;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class hoanghamobile {

    public static void main(String[] args) throws IOException {
        JSONArray productsArray = new JSONArray();

        for (int i = 1; i <= 10; i++) {
            String url = "https://hoanghamobile.com/laptop?p=" + i;

            Document doc = Jsoup.connect(url).userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .timeout(10000)
                    .get();
            ;
            Elements products = doc.select(".pj16-item-info");

            for (Element product : products) {
                String name = product.select("a").attr("title").trim();
                String pictureUrl = product.select("img").attr("src").split(";")[0].trim();
//                String pictureUrl = product.select("img[alt]").attr("src").split(";")[0].trim();
                // Lấy giá cuối cùng
                String rawPrice = product.select(".price").text();
                String price = extractLastPrice(rawPrice);

                // Lấy link chi tiết
                String detailPath = product.select("a").attr("href");
                String detailUrl = "https://hoanghamobile.com" + detailPath;

                // Lấy specs + đánh giá + hãng
                Map<String, String> specMap = extractSpecs(detailUrl);

                // Lấy hãng
//                String brand = name.split(" ")[0]; for phone
                String brand = brand1(detailUrl);
                //Type
                String type = "Laptop";
                // Lấy content
                StringBuilder content = extractContent(detailUrl);
                // JSON object
                JSONObject json = new JSONObject();
                json.put("Name", name);
                json.put("Price", price);
                json.put("picture_url", pictureUrl);
                json.put("Brand", brand);
                json.put("Main_content",content);
                json.put("Type",type);

                json.put("details", specMap);
                productsArray.put(json);

                System.out.println("✅ " + name + " -> done");
            }
        }

        // Ghi ra file JSON
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("laptop.json"))) {
            writer.write(productsArray.toString(4).replace("\\u20ab", "₫"));
        }

        System.out.println("✅ Data saved to laptop.json");
    }

    // Tách giá cuối cùng
    private static String extractLastPrice(String rawPrice) {
        String[] tokens = rawPrice.trim().split(" ");
        if (tokens.length >= 2) {
            String price = tokens[tokens.length - 2] + " " + tokens[tokens.length - 1];
            return price.replace("\u20ab", "₫").replaceAll("[^\\d₫,\\.]", "").trim();
        }
        return rawPrice;
    }
    private static String brand1(String detailUrl) {
        try {
            Thread.sleep(200);
            Document doc = Jsoup.connect(detailUrl).userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .timeout(10000)
                    .get();
            // Lấy nội dung chính từ mục lục
            Element span = doc.select(".breadcrumb li[itemprop=itemListElement] meta[content=4]")
                    .parents()
                    .select("span[itemprop=name]")
                    .first();
            if (span == null){
                return null;
            }
            else {
                String ten = span.attr("content");
                String[] parts = ten.split(" ");
                if (parts.length >= 2) {
                    String name = parts[1];
                    System.out.println(name);
                    return name;
                } else {
                    System.out.println("⚠️ Không thể tách brand từ: " + ten);
                    return parts[0];  // hoặc "Unknown"
                }
            } }catch (IOException | InterruptedException e) {
            System.out.println("❌ Lỗi truy cập chi tiết: " + detailUrl);
            return null;
        }
    }

    private static Map<String, String> extractSpecs(String detailUrl) {
        Map<String, String> mapped = new HashMap<>();
        // Phone
//        Map<String, String> mapping = new HashMap<>();
//        mapping.put("Vi xử lý", "chipset");
//        mapping.put("Hệ điều hành", "os");
//        mapping.put("Bộ nhớ trong", "internal_storage");
//        mapping.put("Số khe SIM", "sim_slots");
//        mapping.put("Mạng di động", "mobile_network");
//        mapping.put("Công nghệ màn hình", "display_tech");
//        mapping.put("Độ phân giải", "display_resolution");
//        mapping.put("Kích thước màn hình", "screen_size");
//        mapping.put("Độ phân giải camera", "camera_resolution");
//        mapping.put("RAM", "ram");
//        mapping.put("Dung lượng pin sản phẩm", "battery_capacity");

//        Laptop
        Map<String, String> mapping = new HashMap<>();
        mapping.put("Công nghệ CPU", "CPU");
        mapping.put("Số hiệu CPU", "CPU_name");
        mapping.put("Số nhân", "cores");
        mapping.put("Số luồng", "threads");
        mapping.put("Xung nhịp cơ bản", "base_clock");
        mapping.put("Xung nhịp tối đa", "max_clock");
        mapping.put("Bộ nhớ đệm", "cache");

        mapping.put("Card on-board", "gpu_onboard");
        mapping.put("Card đồ hoạ rời", "gpu_discrete");
        mapping.put("Công nghệ âm thanh", "audio_tech");
        mapping.put("Built-in speaker", "speaker");
        mapping.put("Built-in array microphone", "microphone");

        mapping.put("RAM", "ram");
        mapping.put("Loại RAM", "ram_type");
        mapping.put("Số khe RAM trống", "ram_slot_empty");
        mapping.put("Khả năng nâng cấp RAM", "ram_upgrade");
        mapping.put("Ổ cứng mặc định", "internal_storage");

        mapping.put("Số lượng màn hình", "screen_count");
        mapping.put("Kích thước màn hình", "screen_size");
        mapping.put("Độ phân giải", "resolution");
        mapping.put("Loại tấm nền", "panel_type");
        mapping.put("Hỗ trợ cảm ứng", "touch_support");
        mapping.put("Công nghệ màn hình", "screen_tech");
        mapping.put("Tần số quét", "refresh_rate");

        mapping.put("Bàn phím", "keyboard");
        mapping.put("Chuột / Touchpad", "touchpad");

        mapping.put("Các cổng giao tiếp", "port");
        mapping.put("Kết nối không dây", "wireless");

        mapping.put("Hệ điều hành", "os");

        mapping.put("Kích thước", "size");
        mapping.put("Trọng lượng", "weight");

        mapping.put("Dung lượng pin", "battery_capacity");
        mapping.put("Bộ sạc theo máy", "charger");

        mapping.put("Chất liệu", "material");

        mapping.put("Camera", "camera");




        try {
            Thread.sleep(200);
            Document doc = Jsoup.connect(detailUrl).userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .timeout(10000)
                    .get();
            ;

            // Lấy thông số kỹ thuật
            Elements specs = doc.select(".box-technical-specifications .specs-content li");
//            Elements specs = doc.select(".text-align-start d-block box-specs-content .specs-content li");

            for (Element spec : specs) {
                Element keyElement = spec.select("strong").first();
                Element valueElement = spec.select("span").first();

                if (keyElement != null && valueElement != null) {
                    String keyVi = keyElement.text().trim();
                    String value = valueElement.text().replace("\"", "").trim();

                    if (mapping.containsKey(keyVi)) {
                        String keyEn = mapping.get(keyVi);
                        mapped.put(keyEn, value);
                    }
                }
            }

            // Lấy đánh giá TB (nếu có)
            Elements ratingElements = doc.select(".stats span");
            if (!ratingElements.isEmpty()) {
                mapped.put("rating", ratingElements.first().text().trim());
            }

        } catch (IOException | InterruptedException e) {
            System.out.println("❌ Lỗi truy cập chi tiết: " + detailUrl);
        }

        return mapped;
    }

    private static StringBuilder extractContent(String detailUrl) throws IOException {
        try {
            Thread.sleep(200);
            Document doc = Jsoup.connect(detailUrl).userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .timeout(10000)
                    .get();
            // Lấy nội dung chính từ mục lục
            Elements tocItems = doc.select(".toc-container .toc li");
            StringBuilder tocContent = new StringBuilder();
            if (tocItems.size() > 1) {
                Element tocTu = tocItems.get(1);
                Elements subItems = tocTu.select("ul li");
                for (Element item : subItems) {
                    String text = item.text().trim();
                    if (!text.isEmpty()) {
                        tocContent.append("- ").append(text).append("\n");
                    }
                }
            }
            return tocContent;

        }
        catch (IOException | InterruptedException e) {
            System.out.println("❌ Lỗi truy cập chi tiết: " + detailUrl);
            return null;
        }
    }
}

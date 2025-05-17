package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;

public class doc {

    public static void main(String[] args) throws Exception {
        String url = "https://api.cellphones.com.vn/v2/graphql/query";

        String payload = "{\n" +
                "  \"query\": \"\\n            query GetProductsByCateId{\\n                products(\\n                        filter: {\\n                            static: {\\n                                categories: [\\\"3\\\"],\\n                                province_id: 24,\\n                                stock: {\\n                                   from: 0\\n                                },\\n                                stock_available_id: [46, 56, 152, 4920],\\n                               filter_price: {from:0to:54990000}\\n                            },\\n                            dynamic: {\\n                                \\n                                \\n                            }\\n                        },\\n                        page: 2,\\n                        size: 20,\\n                        sort: [{view: desc}]\\n                    )\\n                {\\n                    general{\\n                        product_id\\n                        name\\n                        attributes\\n                        sku\\n                        doc_quyen\\n                        manufacturer\\n                        url_key\\n                        url_path\\n                        categories{\\n                            categoryId\\n                            name\\n                            uri\\n                        }\\n                        review{\\n                            total_count\\n                            average_rating\\n                        }\\n                    },\\n                    filterable{\\n                        is_installment\\n                        stock_available_id\\n                        company_stock_id\\n                        filter {\\n                           id\\n                           Label\\n                        }\\n                        is_parent\\n                        price\\n                        prices\\n                        special_price\\n                        promotion_information\\n                        thumbnail\\n                        promotion_pack\\n                        sticker\\n                        flash_sale_types\\n                    },\\n                }\\n            }\",\n" +
                "  \"variables\": {}\n" +
                "}";

        // Create connection using Jsoup
        Connection.Response response = Jsoup.connect(url)
                .method(Connection.Method.POST)
                .ignoreContentType(true)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Origin", "https://cellphones.com.vn")
                .header("Referer", "https://cellphones.com.vn/")
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                        "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/134.0.0.0 Safari/537.36")
                .requestBody(payload)
                .execute();

        String responseJson = response.body();

        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(responseJson);

        Files.write(Paths.get("graphql_response.json"), mapper.writerWithDefaultPrettyPrinter().writeValueAsString(node).getBytes());

        JsonNode products = node.path("data").path("products");

        // Filtered data array to hold extracted product details
        ArrayNode phonesArray = mapper.createArrayNode();

        for (JsonNode product : products) {
            JsonNode general = product.path("general");
            JsonNode filterable = product.path("filterable");
            ObjectNode phone = mapper.createObjectNode();

            // Extract phone name
            phone.put("Name", general.path("name").asText());

            // Price: prefer special price if available
            double rootValue = filterable.path("prices").path("root").path("value").asDouble();
            String value = new BigDecimal(Double.toString(rootValue)).toPlainString();
            phone.put("Price", value);

            // Manufacturer
            phone.put("Brand", general.path("manufacturer").asText());

            // Category: Find category "Điện thoại"
            String type = "";
            JsonNode categories = general.path("categories");
            for (JsonNode category : categories) {
                if ("Điện thoại".equalsIgnoreCase(category.path("name").asText())) {
                    type = category.path("name").asText();
                    break;
                }
            }
            phone.put("Type", type.isEmpty() ? "Not Specified" : type);

            // Configuration: Extract specific attributes, adding fallback values for missing data
            ObjectNode configuration = mapper.createObjectNode();
            JsonNode attributes = general.path("attributes");
            configuration.put("battery", attributes.path("battery").asText("Unknown"));
            configuration.put("chipset", attributes.path("chipset").asText("Unknown"));
            configuration.put("cpu", attributes.path("cpu").asText("Unknown"));
            configuration.put("display_size", attributes.path("display_size").asText("Unknown"));
            configuration.put("display_resolution", attributes.path("display_resolution").asText("Unknown"));
            configuration.put("camera_primary", attributes.path("camera_primary").asText("Unknown"));
            configuration.put("camera_secondary", attributes.path("camera_secondary").asText("Unknown"));
            configuration.put("memory_internal", attributes.path("memory_internal").asText("Unknown"));
            configuration.put("storage", attributes.path("storage").asText("Unknown"));
            configuration.put("cpu", attributes.path("cpu").asText("Not Available"));
            configuration.put("dimensions", attributes.path("dimensions").asText("Not Available"));
            configuration.put("sim", attributes.path("sim").asText("Not Available"));
            configuration.put("gpu", attributes.path("gpu").asText("Not Available"));
            configuration.put("mobile_cam_bien_van_tay", attributes.path("mobile_cam_bien_van_tay").asText("Not Available"));
            configuration.put("mobile_chat_lieu_khung_vien", attributes.path("mobile_chat_lieu_khung_vien").asText("Not Available"));
            configuration.put("mobile_nhu_cau_sd", attributes.path("mobile_nhu_cau_sd").asText("Not Available"));
            configuration.put("product_weight", attributes.path("product_weight").asText("Not Available"));
            phone.set("details", configuration);

            // Review: Extract review data
            ObjectNode review = mapper.createObjectNode();
            JsonNode reviewNode = general.path("review");
            phone.put("total_count", reviewNode.path("total_count").asInt());
            phone.put("average_rating", reviewNode.path("average_rating").asDouble());

            // Lấy key selling points từ general.attributes (đúng vị trí)
            String encodedSellingPoints = attributes.path("key_selling_points").asText("Not Available");

            String decodedSellingPoints;
            if (!encodedSellingPoints.equals("Not Available")) {
                // Decode unicode escape bằng ObjectMapper
                ObjectMapper mapper2 = new ObjectMapper();
                decodedSellingPoints = mapper2.readValue("\"" + encodedSellingPoints + "\"", String.class);

                // Chuyển HTML sang plain text dễ đọc bằng Jsoup
                decodedSellingPoints = Jsoup.parse(decodedSellingPoints).text();

                // Thêm xuống dòng sau mỗi dấu chấm câu
                decodedSellingPoints = decodedSellingPoints.replaceAll("(?<=[.?!-])\\s*", "\n");
            } else {
                decodedSellingPoints = "Not Available";
            }

            phone.put("Main_content", decodedSellingPoints);


            // Image URL: Ensure correct URL
            String imagePath = attributes.path("ads_base_image").asText();
            String imageUrl = "https://cellphones.com.vn/media/catalog/product" + imagePath;
            phone.put("picture_url", imageUrl);

            // Add phone to array
            // Thêm vào ngay trước khi: phonesArray.add(phone);
            String productUrl = "https://cellphones.com.vn/" + general.path("url_path").asText() ;
            ArrayNode commentsArray = mapper.createArrayNode();
            try {
                // Kết nối tới trang sản phẩm
                org.jsoup.nodes.Document doc = Jsoup.connect(productUrl)
                        .userAgent("Mozilla/5.0")
                        .timeout(10000)
                        .get();

                // Lấy tất cả các bình luận (class item-comment)
                for (org.jsoup.nodes.Element commentElement : doc.select("div.item-comment")) {

                    ObjectNode commentJson = mapper.createObjectNode();

                    // Người bình luận (user hoặc admin)
                    String commenter = commentElement.select(".box-cmt__box-info strong").text();
                    commentJson.put("commenter", commenter);

                    // Nội dung bình luận
                    String commentContent = commentElement.select(".box-cmt__box-question").text();
                    commentJson.put("content", commentContent);

                    // Phản hồi từ admin (nếu có)
                    ArrayNode repliesArray = mapper.createArrayNode();
                    for (org.jsoup.nodes.Element reply : commentElement.select(".list-rep-comment .item-rep-comment")) {
                        ObjectNode replyJson = mapper.createObjectNode();
                        String replier = reply.select(".box-cmt__box-info strong").text();
                        String replyContent = reply.select(".box-cmt__box-question").text();
                        replyJson.put("replier", replier);
                        replyJson.put("reply_content", replyContent);
                        repliesArray.add(replyJson);
                    }

                    // Thêm replies vào comment (nếu có)
                    commentJson.set("replies", repliesArray);
                    commentsArray.add(commentJson);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Lưu comments vào phone JSON
            phone.set("comments", commentsArray);

            phonesArray.add(phone);
        }

        // Write the filtered data to a new JSON file
        String filteredJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(phonesArray);
        Files.write(Paths.get("phones_filtered.json"), filteredJson.getBytes());

        System.out.println("Successfully created 'phones_filtered.json' with filtered phone details.");
    }
}

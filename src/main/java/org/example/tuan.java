package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class tuan {
    public static void main(String[] args) throws Exception {
        long startTime = System.currentTimeMillis(); // Track execution time

        // Set path to chromedriver.exe
        System.setProperty("webdriver.chrome.driver", "D:\\Downloads\\chromedriver-win64\\chromedriver.exe");
        System.out.println("ChromeDriver path: " + System.getProperty("webdriver.chrome.driver"));

        // Enable ChromeDriver logging
        System.setProperty("webdriver.chrome.logfile", "D:\\chromedriver.log");
        System.setProperty("webdriver.chrome.verboseLogging", "true");

        // Thread pool for parallel comment scraping
        ExecutorService executor = Executors.newFixedThreadPool(4); // Adjust based on system capacity
        List<Future<ArrayNode>> commentFutures = new ArrayList<>();

        try {
//             GraphQL API request of phone
            String url = "https://api.cellphones.com.vn/v2/graphql/query";
//          Phone
//            String payload = "{\n" +
//                    "  \"query\": \"\\n            query GetProductsByCateId{\\n                products(\\n                        filter: {\\n                            static: {\\n                                categories: [\\\"3\\\"],\\n                                province_id: 24,\\n                                stock: {\\n                                   from: 0\\n                                },\\n                                stock_available_id: [46, 56, 152, 4920],\\n                               filter_price: {from:0to:54990000}\\n                            },\\n                            dynamic: {\\n                            }\\n                        },\\n                        page: 2,\\n                        size: 100,\\n                        sort: [{view: desc}]\\n                    )\\n                {\\n                    general{\\n                        product_id\\n                        name\\n                        attributes\\n                        sku\\n                        doc_quyen\\n                        manufacturer\\n                        url_key\\n                        url_path\\n                        categories{\\n                            categoryId\\n                            name\\n                            uri\\n                        }\\n                        review{\\n                            total_count\\n                            average_rating\\n                        }\\n                    },\\n                    filterable{\\n                        is_installment\\n                        stock_available_id\\n                        company_stock_id\\n                        filter {\\n                           id\\n                           Label\\n                        }\\n                        is_parent\\n                        price\\n                        prices\\n                        special_price\\n                        promotion_information\\n                        thumbnail\\n                        promotion_pack\\n                        sticker\\n                        flash_sale_types\\n                    },\\n                }\\n            }\",\n" +
//                    "  \"variables\": {}\n" +
//                    "}";

            // Laptop
            String payload = "{\n" +
                    "  \"query\": \"\\n            query GetProductsByCateId{\\n                products(\\n                        filter: {\\n                            static: {\\n                                categories: [\\\"380\\\"],\\n                                province_id: 24,\\n                                stock: {\\n                                   from: 0\\n                                },\\n                                stock_available_id: [46, 56, 152, 4920],\\n                               filter_price: {from: 0, to: 194990000}\\n                            },\\n                            dynamic: {\\n                            }\\n                        },\\n                        page: 2,\\n                        size: 100,\\n                        sort: [{view: desc}]\\n                    )\\n                {\\n                    general{\\n                        product_id\\n                        name\\n                        attributes\\n                        sku\\n                        doc_quyen\\n                        manufacturer\\n                        url_key\\n                        url_path\\n                        categories{\\n                            categoryId\\n                            name\\n                            uri\\n                        }\\n                        review{\\n                            total_count\\n                            average_rating\\n                        }\\n                    },\\n                    filterable{\\n                        is_installment\\n                        stock_available_id\\n                        company_stock_id\\n                        filter {\\n                           id\\n                           Label\\n                        }\\n                        is_parent\\n                        price\\n                        prices\\n                        special_price\\n                        promotion_information\\n                        thumbnail\\n                        promotion_pack\\n                        sticker\\n                        flash_sale_types\\n                    },\\n                }\\n            }\",\n" +
                    "  \"variables\": {}\n" +
                    "}";

            Connection.Response response = Jsoup.connect(url)
                    .method(Connection.Method.POST)
                    .ignoreContentType(true)
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .header("Origin", "https://cellphones.com.vn")
                    .header("Referer", "https://cellphones.com.vn/")
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/135.0.7049.114 Safari/537.36")
                    .requestBody(payload)
                    .execute();

            String responseJson = response.body();
            // Sanitize response to remove control characters
            responseJson = responseJson.replaceAll("[\\p{Cntrl}&&[^\\n\\t]]", "");

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node;
            try {
                node = mapper.readTree(responseJson);
            } catch (Exception e) {
                System.err.println("Failed to parse JSON response: " + e.getMessage());
                Files.write(Paths.get("error_response.json"), responseJson.getBytes());
                throw e;
            }

            Files.write(Paths.get("graphql_response.json"), mapper.writerWithDefaultPrettyPrinter().writeValueAsString(node).getBytes());

            JsonNode products = node.path("data").path("products");

            ArrayNode phonesArray = mapper.createArrayNode();
            List<String> productUrls = new ArrayList<>();
            List<ObjectNode> phoneNodes = new ArrayList<>();

            // Process product data
            for (JsonNode product : products) {
                JsonNode general = product.path("general");
                JsonNode filterable = product.path("filterable");
                ObjectNode phone = mapper.createObjectNode();

                phone.put("name", general.path("name").asText());
                double rootValue = filterable.path("prices").path("root").path("value").asDouble();
                phone.put("price", new BigDecimal(Double.toString(rootValue)).toPlainString() + "đ");
                // Extract Brand from name with fallback to manufacturer
                String brand = "Unknown";
                String productName = general.path("name").asText();

                brand = general.path("manufacturer").asText("Unknown");
                System.out.println("Empty product name for: " + general.path("product_id").asText());

                phone.put("brand", brand);
                String type = "";
                JsonNode categories = general.path("categories");
                for (JsonNode category : categories) {
//                    if ("Điện thoại".equalsIgnoreCase(category.path("name").asText())) {
                        if ("Laptop".equalsIgnoreCase(category.path("name").asText())) {

                    type = category.path("name").asText();
                        break;
                    }
                }
                phone.put("Type", type.isEmpty() ? "Not Specified" : type);

                ObjectNode configuration = mapper.createObjectNode();
                JsonNode attributes = general.path("attributes");
                // This if for phone
//                configuration.put("Battery", attributes.path("battery").asText("Unknown"));
//                configuration.put("Chipset", attributes.path("chipset").asText("Unknown"));
//                configuration.put("Cpu", attributes.path("cpu").asText("Unknown"));
//                configuration.put("Display size", attributes.path("display_size").asText("Unknown"));
//                configuration.put("Display resolution", attributes.path("display_resolution").asText("Unknown"));
//                configuration.put("Camera primary", attributes.path("camera_primary").asText("Unknown"));
//                configuration.put("Camera secondary", attributes.path("camera_secondary").asText("Unknown"));
//                configuration.put("Memory internal", attributes.path("memory_internal").asText("Unknown"));
//                configuration.put("Storage", attributes.path("storage").asText("Unknown"));
//                configuration.put("Dimensions", attributes.path("dimensions").asText("Not Available"));
//                configuration.put("Sim", attributes.path("sim").asText("Not Available"));
//                configuration.put("Gpu", attributes.path("gpu").asText("Not Available"));
//                configuration.put("Mobile fingerprint sensor", attributes.path("mobile_cam_bien_van_tay").asText("Not Available"));
//                configuration.put("Mobile frame material", attributes.path("mobile_chat_lieu_khung_vien").asText("Not Available"));
//                configuration.put("Mobile using", attributes.path("mobile_nhu_cau_sd").asText("Not Available"));
//                configuration.put("Product_weight", attributes.path("product_weight").asText("Not Available"));

                //this is for laptop
                configuration.put("battery", attributes.path("battery").asText("Unknown"));
                configuration.put("cpu", attributes.path("cpu").asText("Unknown"));
                configuration.put("ram", attributes.path("laptop_ram").asText("Unknown"));
                configuration.put("storage", attributes.path("hdd_sdd").asText("Unknown"));
                configuration.put("gpu", attributes.path("vga").asText("Unknown"));
                configuration.put("bluetooth", attributes.path("bluetooth").asText("Unknown"));
                configuration.put("display_size", attributes.path("display_size").asText("Unknown"));
                configuration.put("display_resolution", attributes.path("display_resolution").asText("Unknown"));
                configuration.put("operating_system", attributes.path("os_version").asText("Unknown"));
                configuration.put("special_feature", attributes.path("laptop_special_feature").asText("Unknown"));
                configuration.put("product_weight", attributes.path("product_weight").asText("Not Available"));
                configuration.put("dimensions", attributes.path("dimensions").asText("Not Available"));
                phone.set("details", configuration);

                JsonNode reviewNode = general.path("review");
                phone.put("total_count", reviewNode.path("total_count").asInt());
                phone.put("average_rating", reviewNode.path("average_rating").asDouble());

                String encodedSellingPoints = attributes.path("key_selling_points").asText("Not Available");
                String decodedSellingPoints = "Not Available";
                if (!encodedSellingPoints.equals("Not Available")) {
                    try {
                        decodedSellingPoints = mapper.readValue("\"" + encodedSellingPoints.replaceAll("[\\p{Cntrl}&&[^\\n\\t]]", "") + "\"", String.class);
                        decodedSellingPoints = Jsoup.parse(decodedSellingPoints).text();
                        decodedSellingPoints = decodedSellingPoints.replaceAll("(?<=[.?!-])\\s*", "\n");
                    } catch (Exception e) {
                        System.out.println("Failed to parse key_selling_points for product " + general.path("name").asText() + ": " + e.getMessage());
                    }
                }
                phone.put("main_content", decodedSellingPoints);

                String imagePath = attributes.path("ads_base_image").asText();
                String imageUrl = "https://cellphones.com.vn/media/catalog/product" + imagePath;
                phone.put("picture_url", imageUrl);

                String productUrl = "https://cellphones.com.vn/" + general.path("url_path").asText();
                // Skip comment scraping if no reviews
                if (reviewNode.path("total_count").asInt() == 0) {
                    phone.set("comments", mapper.createArrayNode()); // Create empty ArrayNode
                    phonesArray.add(phone);
                    continue;
                }
                productUrls.add(productUrl);
                phoneNodes.add(phone);
            }

            // Submit parallel comment scraping tasks
            for (String productUrl : productUrls) {
                final String url1 = productUrl;
                commentFutures.add(executor.submit(() -> scrapeComments(url1, mapper)));
            }

            // Collect comments
            for (int i = 0; i < phoneNodes.size(); i++) {
                ObjectNode phone = phoneNodes.get(i);
                ArrayNode comments = commentFutures.get(i).get(30, TimeUnit.SECONDS);
                phone.set("comments", comments);
                phonesArray.add(phone);
            }

            // Write output
            String filteredJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(phonesArray);
//            Files.write(Paths.get("phones_filtered.json"), filteredJson.getBytes());
            Files.write(Paths.get("laptop_filtered.json"), filteredJson.getBytes());


//            System.out.println("Successfully created 'phones_filtered.json' with filtered phone details and comments.");
            System.out.println("Successfully created 'laptop_filtered.json' with filtered phone details and comments.");
            System.out.println("Total time: " + (System.currentTimeMillis() - startTime) / 1000.0 + " seconds");
        } finally {
            executor.shutdown();
            executor.awaitTermination(10, TimeUnit.SECONDS);
        }
    }

    private static ArrayNode scrapeComments(String productUrl, ObjectMapper mapper) {
        ChromeOptions opts = new ChromeOptions();
        opts.addArguments("--remote-debugging-port=0");
        opts.addArguments("user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/135.0.7049.114 Safari/537.36");
        opts.addArguments("--no-sandbox");
        opts.addArguments("--disable-dev-shm-usage");
        opts.addArguments("--disable-gpu");
        // opts.addArguments("--headless=new"); // Uncomment after testing
        WebDriver driver = new ChromeDriver(opts);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
        ArrayNode commentsArray = mapper.createArrayNode();

        try {
            driver.get(productUrl);
            ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight);");
            wait.until(ExpectedConditions.or(
                    ExpectedConditions.presenceOfElementLocated(By.cssSelector(
                            "div.item-comment, div.comment-item, div.review-comment, .item-review-comment")),
                    ExpectedConditions.presenceOfElementLocated(By.cssSelector("body"))
            ));

            List<WebElement> commentContainers = driver.findElements(By.cssSelector(
                    "div.item-comment, div.comment-item, div.review-comment, .item-review-comment"));
            if (commentContainers.isEmpty()) {
                System.out.println("No comments section found for " + productUrl);
                return commentsArray;
            }

            try {
                WebElement loadMore = driver.findElement(By.cssSelector(
                        "[class*='load-more'], button[class*='more-comments'], [id*='load-more']"));
                int maxClicks = 2;
                while (loadMore.isDisplayed() && maxClicks-- > 0) {
                    loadMore.click();
                    wait.until(ExpectedConditions.stalenessOf(loadMore));
                    loadMore = driver.findElement(By.cssSelector(
                            "[class*='load-more'], button[class*='more-comments'], [id*='load-more']"));
                }
            } catch (Exception e) {
                // No load more button
            }

            commentContainers = driver.findElements(By.cssSelector(
                    "div.item-comment, div.comment-item, div.review-comment, .item-review-comment"));
            System.out.println("Found " + commentContainers.size() + " comments for " + productUrl);

            for (WebElement c : commentContainers) {
                ObjectNode commentJson = mapper.createObjectNode();

                String commenter = "Unknown";
                try {
                    WebElement commenterElement = c.findElement(By.cssSelector(
                            ".box-cmt__box-info strong, .commenter-name, .user-name, [class*='comment-author'], .comment-author"));
                    commenter = commenterElement.getText().trim();
                } catch (NoSuchElementException e) {
                    try {
                        commenter = c.findElement(By.xpath(
                                ".//*[contains(@class, 'name') or contains(@class, 'author') or contains(@class, 'user')]")).getText().trim();
                    } catch (Exception ex) {
                        System.out.println("Commenter not found for a comment in " + productUrl);
                    }
                }
                commentJson.put("commenter", commenter.isEmpty() ? "Unknown" : commenter);

                String commentContent = "No content";
                try {
                    WebElement contentElement = c.findElement(By.cssSelector(
                            ".box-cmt__box-question, .comment-content > p, .review-text, [class*='comment-text']"));
                    commentContent = contentElement.getText().trim();
                } catch (Exception e) {
                    System.out.println("Comment content not found for a comment in " + productUrl);
                }
                commentJson.put("content", commentContent);

                ArrayNode repliesArray = mapper.createArrayNode();
                try {
                    List<WebElement> replies = c.findElements(By.cssSelector(
                            ".list-rep-comment .item-rep-comment, .reply-item, [class*='reply-comment']"));
                    for (WebElement reply : replies) {
                        ObjectNode replyJson = mapper.createObjectNode();
                        String replier = "Unknown";
                        try {
                            replier = reply.findElement(By.cssSelector(
                                    ".box-cmt__box-info strong, .replier-name, [class*='reply-author']")).getText().trim();
                        } catch (NoSuchElementException e) {
                            try {
                                replier = reply.findElement(By.xpath(
                                        ".//*[contains(@class, 'name') or contains(@class, 'author')]")).getText().trim();
                            } catch (Exception ex) {
                                // Fallback
                            }
                        }
                        String replyContent = "No reply content";
                        try {
                            replyContent = reply.findElement(By.cssSelector(
                                    ".box-cmt__box-question, .reply-content, [class*='reply-text'], .comment-content > p")).getText().trim();
                        } catch (Exception e) {
                            // Fallback
                        }
                        replyJson.put("replier", replier.isEmpty() ? "Unknown" : replier);
                        replyJson.put("reply_content", replyContent);
                        repliesArray.add(replyJson);
                    }
                } catch (Exception e) {
                    System.out.println("Error fetching replies for a comment in " + productUrl);
                }
                commentJson.set("replies", repliesArray);

                commentsArray.add(commentJson);
            }
        } catch (Exception e) {
            System.out.println("Error fetching comments for " + productUrl + ": " + e.getMessage());
        } finally {
            driver.quit();
        }
        return commentsArray;
    }
}
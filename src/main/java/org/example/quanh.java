package org.example;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class quanh {
    public static void main(String[] args) {
        // Set path to chromedriver.exe
        System.setProperty("webdriver.chrome.driver", "D:\\Downloads\\chromedriver-win64\\chromedriver.exe");

        // Enable ChromeDriver logging for debugging
        System.setProperty("webdriver.chrome.logfile", "D:\\chromedriver.log");
        System.setProperty("webdriver.chrome.verboseLogging", "true");

        // Initialize ChromeDriver
        ChromeOptions opts = new ChromeOptions();
         opts.addArguments("--headless"); // Uncomment for headless mode after testing
        opts.addArguments("--remote-debugging-port=9222"); // Avoid port conflicts
        opts.addArguments("user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36"); // Set user-agent
        WebDriver driver = new ChromeDriver(opts);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));

        try {
            // Open the laptop listing page
            driver.get("https://cellphones.com.vn/laptop.html");
            wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("div.product-info")));

            // Collect product URLs
            List<WebElement> itemContainers = driver.findElements(
                    By.cssSelector("div.product-info a[href]"));
            List<String> productUrls = new ArrayList<>();
            for (WebElement a : itemContainers) {
                String href = a.getAttribute("href");
                productUrls.add(href);
            }

            System.out.println("Total products found: " + productUrls.size());

            // Crawl each product URL
            for (String url : productUrls) {
                System.out.println("\n=== Crawling " + url + " ===");
                driver.get(url);

                // Crawl rating attributes
                List<WebElement> ratingAttrs = driver.findElements(
                        By.cssSelector(".comment-content"));
                System.out.println("  • Rating attributes:");
                for (WebElement ra : ratingAttrs) {
                    System.out.println("    - " + ra.getText().trim());
                }

                // Crawl comments
                List<WebElement> commentContainers = driver.findElements(
                        By.cssSelector(
                                ".item-review-comment.is-flex.is-justify-content-space-between.is-flex-direction-column"));
                System.out.println("  • Total comments: " + commentContainers.size());
                for (WebElement c : commentContainers) {
                    WebElement p = c.findElement(By.cssSelector(".comment-content > p"));
                    System.out.println("    • Comment: " + p.getText().trim());
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            driver.quit();
        }
    }
}
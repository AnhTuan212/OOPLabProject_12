module web.scrapping {
    requires org.json;
    requires java.net.http;
    requires com.fasterxml.jackson.databind; // Correct for jackson-databind
    requires com.fasterxml.jackson.core; // Transitive dependency for jackson-databind
    requires org.seleniumhq.selenium.chrome_driver;
    requires org.seleniumhq.selenium.support;
    requires org.jsoup;
    requires dev.failsafe.core;
    requires com.google.common;
}
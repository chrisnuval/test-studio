package com.recorder.driver;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class WebDriverFactory {
    private static WebDriver driver;

    public static WebDriver createDriver() {
        if (driver == null) {
            WebDriverManager.chromedriver().setup();
            ChromeOptions options = new ChromeOptions();
            
            options.addArguments("--disable-web-security");
            options.addArguments("--allow-running-insecure-content");
            String userDataDir = System.getProperty("java.io.tmpdir") + "/selenium-profile";
            options.addArguments("--user-data-dir=" + userDataDir);
            options.addArguments("--disable-gpu");
            options.addArguments("--no-sandbox");
            options.addArguments("--start-maximized"); // so the UI renders fully
            System.setProperty("webdriver.chrome.verboseLogging", "true");

            driver = new ChromeDriver(options);
        }
        return driver;
    }

    public static void quitDriver() {
        if (driver != null) {
            driver.quit();
            driver = null;
        }
    }
}

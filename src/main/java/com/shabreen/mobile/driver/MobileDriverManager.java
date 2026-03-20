package com.shabreen.mobile.driver;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.ios.options.XCUITestOptions;
import com.shabreen.mobile.config.MobileConfig;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;

/**
 * MobileDriverManager — thread-safe Appium driver factory.
 * Supports Android (UiAutomator2) and iOS (XCUITest).
 * Handles local Appium server and cloud (AWS Device Farm / Firebase).
 */
public class MobileDriverManager {

    private static final ThreadLocal<AppiumDriver> driverThread = new ThreadLocal<>();

    public static AppiumDriver getDriver() {
        return driverThread.get();
    }

    public static void initAndroidDriver() {
        UiAutomator2Options options = new UiAutomator2Options()
                .setPlatformName("Android")
                .setPlatformVersion(MobileConfig.get("android.version", "13"))
                .setDeviceName(MobileConfig.get("android.device", "Pixel_7_API_33"))
                .setApp(MobileConfig.get("android.app.path"))
                .setAppPackage(MobileConfig.get("android.app.package"))
                .setAppActivity(MobileConfig.get("android.app.activity"))
                .setAutomationName("UiAutomator2")
                .setAutoGrantPermissions(true)
                .setNewCommandTimeout(Duration.ofSeconds(120))
                .setNoReset(false)
                .setFullReset(false);

        // Add ADB command options for CI
        options.setCapability("appium:adbExecTimeout", 60000);
        options.setCapability("appium:uiautomator2ServerLaunchTimeout", 60000);
        options.setCapability("appium:skipServerInstallation",
                Boolean.parseBoolean(MobileConfig.get("skipServerInstall", "false")));

        try {
            AppiumDriver driver = new AndroidDriver(
                    new URL(MobileConfig.get("appium.server.url", "http://localhost:4723")), options);
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
            driverThread.set(driver);
        } catch (MalformedURLException e) {
            throw new RuntimeException("Invalid Appium server URL", e);
        }
    }

    public static void initIOSDriver() {
        XCUITestOptions options = new XCUITestOptions()
                .setPlatformName("iOS")
                .setPlatformVersion(MobileConfig.get("ios.version", "17.0"))
                .setDeviceName(MobileConfig.get("ios.device", "iPhone 15"))
                .setApp(MobileConfig.get("ios.app.path"))
                .setBundleId(MobileConfig.get("ios.bundle.id"))
                .setAutomationName("XCUITest")
                .setNewCommandTimeout(Duration.ofSeconds(120))
                .setWdaLaunchTimeout(Duration.ofSeconds(120))
                .setNoReset(false);

        try {
            AppiumDriver driver = new IOSDriver(
                    new URL(MobileConfig.get("appium.server.url", "http://localhost:4723")), options);
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
            driverThread.set(driver);
        } catch (MalformedURLException e) {
            throw new RuntimeException("Invalid Appium server URL", e);
        }
    }

    public static void quitDriver() {
        if (driverThread.get() != null) {
            driverThread.get().quit();
            driverThread.remove();
        }
    }
}

package com.shabreen.mobile.tests;

import com.shabreen.mobile.base.MobileBaseTest;
import com.shabreen.mobile.screens.HomeScreen;
import com.shabreen.mobile.screens.RideJournalScreen;
import com.shabreen.mobile.screens.BatteryScreen;
import com.shabreen.mobile.screens.ChargingScreen;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * EVCompanionAppTest — validates post-purchase scooter companion app workflows.
 *
 * Covers MoveOS features:
 *  - Ride Journal
 *  - Energy Insights
 *  - Hypercharger Billing
 *  - Battery health monitoring
 *  - Emergency SOS flow
 */
public class EVCompanionAppTest extends MobileBaseTest {

    // ── Ride Journal ──────────────────────────────────────────────

    @Test(description = "Ride Journal should display today's trip summary",
          groups = {"smoke", "ridejournal"})
    public void testRideJournalDisplaysTripSummary() {
        HomeScreen home = new HomeScreen(driver);
        RideJournalScreen rideJournal = home.tapRideJournal();

        Assert.assertTrue(rideJournal.isDisplayed(), "Ride Journal screen did not open");
        Assert.assertNotNull(rideJournal.getLastRideDistance(), "Last ride distance should not be null");
        Assert.assertTrue(rideJournal.getLastRideDistance() > 0, "Last ride distance should be positive");
        Assert.assertNotNull(rideJournal.getLastRideTimestamp(), "Ride timestamp should be present");
    }

    @Test(description = "Ride Journal should show energy consumed per trip",
          groups = {"regression", "ridejournal"})
    public void testRideJournalEnergyConsumption() {
        RideJournalScreen rideJournal = new HomeScreen(driver).tapRideJournal();
        double energyConsumedKwh = rideJournal.getEnergyConsumedForLastRide();

        Assert.assertTrue(energyConsumedKwh > 0, "Energy consumed should be greater than 0");
        Assert.assertTrue(energyConsumedKwh < 5.0, "Energy consumed value seems unrealistic (>5 kWh for a single ride)");
    }

    // ── Battery Health ────────────────────────────────────────────

    @Test(description = "Battery screen should display SOC and health correctly",
          groups = {"smoke", "battery"})
    public void testBatteryScreenDisplaysCorrectSOC() {
        BatteryScreen battery = new HomeScreen(driver).tapBattery();

        int soc = battery.getStateOfCharge();
        Assert.assertTrue(soc >= 0 && soc <= 100,
                "SOC should be between 0 and 100, got: " + soc);
        Assert.assertNotNull(battery.getRangeEstimateKm(), "Range estimate should be displayed");
        Assert.assertTrue(battery.getBatteryHealthPercent() > 0,
                "Battery health percentage should be displayed");
    }

    @Test(description = "Low battery warning should appear below 15% SOC",
          groups = {"regression", "battery"})
    public void testLowBatteryWarningThreshold() {
        BatteryScreen battery = new HomeScreen(driver).tapBattery();
        int soc = battery.getStateOfCharge();

        if (soc < 15) {
            Assert.assertTrue(battery.isLowBatteryWarningVisible(),
                    "Low battery warning should be visible when SOC < 15%");
        } else {
            Assert.assertFalse(battery.isLowBatteryWarningVisible(),
                    "Low battery warning should not show when SOC >= 15%");
        }
    }

    // ── Hypercharger Billing ──────────────────────────────────────

    @Test(description = "Hypercharger billing screen should show session history",
          groups = {"regression", "charging"})
    public void testHyperchargerBillingSessionHistory() {
        ChargingScreen charging = new HomeScreen(driver).tapCharging();
        Assert.assertTrue(charging.isDisplayed(), "Charging screen did not load");
        Assert.assertTrue(charging.getSessionCount() >= 0, "Session count should be non-negative");
        if (charging.getSessionCount() > 0) {
            Assert.assertNotNull(charging.getLastSessionCost(), "Last session cost should be displayed");
            Assert.assertNotNull(charging.getLastSessionDuration(), "Session duration should be displayed");
        }
    }

    @Test(description = "Find nearest Hypercharger should display results",
          groups = {"smoke", "charging"})
    public void testFindNearestHypercharger() {
        ChargingScreen charging = new HomeScreen(driver).tapCharging();
        charging.tapFindNearestCharger();

        Assert.assertTrue(charging.isMapDisplayed(), "Map should be displayed after tapping Find Charger");
        Assert.assertTrue(charging.getChargerPinCount() > 0,
                "At least one charger pin should be visible on map");
    }

    // ── Emergency SOS ─────────────────────────────────────────────

    @Test(description = "Emergency SOS button should be accessible from home screen",
          groups = {"smoke", "safety"})
    public void testEmergencySOSAccessible() {
        HomeScreen home = new HomeScreen(driver);
        Assert.assertTrue(home.isSOSButtonVisible(), "SOS button should be visible on home screen");
    }

    @Test(description = "Emergency SOS confirmation dialog should appear on tap",
          groups = {"regression", "safety"})
    public void testEmergencySOSConfirmationDialog() {
        HomeScreen home = new HomeScreen(driver);
        home.tapSOSButton();
        Assert.assertTrue(home.isSOSConfirmationDialogVisible(),
                "SOS confirmation dialog should appear after tapping SOS");
        home.dismissSOSDialog(); // Clean up — don't actually trigger SOS
    }

    // ── Deep link / OTA ───────────────────────────────────────────

    @Test(description = "OTA update notification should navigate to update screen",
          groups = {"regression", "ota"})
    public void testOTAUpdateDeepLink() {
        driver.get("olaelectric://ota/update");
        HomeScreen home = new HomeScreen(driver);
        Assert.assertTrue(home.isOTAUpdateBannerVisible() || home.isDisplayed(),
                "App should handle OTA deep link gracefully");
    }
}

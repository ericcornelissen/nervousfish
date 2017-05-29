package com.nervousfish.nervousfish.test;

import android.app.Activity;
import android.support.test.espresso.core.deps.guava.collect.Iterables;
import android.support.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import android.support.test.runner.lifecycle.Stage;
import android.test.ActivityInstrumentationTestCase2;

import com.nervousfish.nervousfish.activities.BluetoothConnectionActivity;
import com.nervousfish.nervousfish.activities.MainActivity;
import com.nervousfish.nervousfish.R;
import com.nervousfish.nervousfish.activities.NFCActivity;
import com.nervousfish.nervousfish.activities.QRActivity;
import com.nervousfish.nervousfish.service_locator.EntryActivity;

import cucumber.api.CucumberOptions;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;



@CucumberOptions(features = "features")
public class SelectPairingMethodSteps extends ActivityInstrumentationTestCase2<MainActivity> {
    private static final Activity[] activity = new Activity[1];

    public SelectPairingMethodSteps(EntryActivity activityClass) {
        super(MainActivity.class);
    }

    @Given("^I have MainActivity$")
    public void iHaveMainActivity() {
        assertNotNull(getActivity());
    }

    @When("^I press the pairing button$")
    public void iPressThePairingButton() {
        onView(withId(R.id.pairing_button)).perform(click());
    }

    @When("^I press the bluetooth button$")
    public void iPressTheBluetoothButton() {
        onView(withId(R.id.pairing_menu_bluetooth)).perform(click());
    }

    @When("^I press the qr button$")
    public void iPressTheQRButton() {
        onView(withId(R.id.pairing_menu_qr)).perform(click());
    }

    @When("^I press the nfc button$")
    public void iPressTheNFCButton() {
        onView(withId(R.id.pairing_menu_nfc)).perform(click());
    }

    @Then("^I go to the BluetoothConnectionActivity$")
    public void iGoToBluetoothConnection() {
        assertEquals(getCurrentActivity().getClass(), BluetoothConnectionActivity.class);
    }

    @Then("^I go to the QRActivity$")
    public void iGoToQR() {
        assertEquals(getCurrentActivity().getClass(), QRActivity.class);
    }

    @Then("^I go to the NFCActivity$")
    public void iGoToNFC() {
        assertEquals(getCurrentActivity().getClass(), NFCActivity.class);
    }

    private Activity getCurrentActivity() {
        getInstrumentation().waitForIdleSync();
        try {
            runTestOnUiThread(new GetCurrentActivityRunnable());
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        return activity[0];
    }

    private static class GetCurrentActivityRunnable implements Runnable {
        @Override
        public void run() {
            java.util.Collection<Activity> activities = ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED);
            activity[0] = Iterables.getOnlyElement(activities);
        }
    }

}
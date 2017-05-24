package com.nervousfish.nervousfish.test;

import android.app.Activity;
import android.content.Intent;
import android.support.test.espresso.core.deps.guava.collect.Iterables;
import android.support.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import android.support.test.runner.lifecycle.Stage;
import android.test.ActivityInstrumentationTestCase2;

import com.nervousfish.nervousfish.ConstantKeywords;
import com.nervousfish.nervousfish.R;
import com.nervousfish.nervousfish.activities.VisualVerificationActivity;
import com.nervousfish.nervousfish.service_locator.EntryActivity;

import cucumber.api.CucumberOptions;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertNotEquals;

@CucumberOptions(features = "features")
public class VisualVerificationSteps extends ActivityInstrumentationTestCase2<EntryActivity> {
    private static final Activity[] activity = new Activity[1];

    private int[] buttons = new int[] {
        R.id.visual_verification_button00,
        R.id.visual_verification_button01,
        R.id.visual_verification_button02,
        R.id.visual_verification_button10,
        R.id.visual_verification_button11,
        R.id.visual_verification_button12,
        R.id.visual_verification_button10,
        R.id.visual_verification_button21,
        R.id.visual_verification_button22,
        R.id.visual_verification_button30,
        R.id.visual_verification_button31,
        R.id.visual_verification_button32,
    };

    public VisualVerificationSteps() {
        super(EntryActivity.class);
    }


    @Given("^I am viewing the visual verification activity$")
    public void iHaveAVisualVerificationActivity() {
        Intent intent = new Intent(getActivity(), VisualVerificationActivity.class);
        intent.putExtra(ConstantKeywords.SERVICE_LOCATOR,
                getCurrentActivity().getIntent().getSerializableExtra(ConstantKeywords.SERVICE_LOCATOR));
        getActivity().startActivity(intent);
    }

    @When("^I press button (\\d+)$")
    public void iPressButton(final int button) {
        int btn = this.buttons[button];
        onView(withId(btn)).perform(click());
    }

    @Then("^I leave the visual verification activity$")
    public void iLeaveTheVisualVerificationActivity() {
        assertNotEquals(getCurrentActivity().getClass(), VisualVerificationActivity.class);
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

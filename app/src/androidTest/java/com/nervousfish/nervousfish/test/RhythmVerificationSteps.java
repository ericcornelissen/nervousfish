package com.nervousfish.nervousfish.test;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.support.test.espresso.intent.Checks;
import android.support.test.espresso.matcher.BoundedMatcher;
import android.support.test.rule.ActivityTestRule;
import android.view.View;
import android.widget.EditText;

import com.nervousfish.nervousfish.R;
import com.nervousfish.nervousfish.activities.RhythmVerificationActivity;
import com.nervousfish.nervousfish.activities.WaitActivity;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Rule;

import java.io.IOException;

import cucumber.api.CucumberOptions;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

@CucumberOptions(features = "features")
public class RhythmVerificationSteps {

    @Rule
    public ActivityTestRule<RhythmVerificationActivity> mActivityRule =
            new ActivityTestRule<>(RhythmVerificationActivity.class, true, false);

    @Given("^I am viewing the create rhythm activity$")
    public void iAmViewingTheCreateRhythmActivity() throws IOException {
        final Intent intent = new Intent();
        this.mActivityRule.launchActivity(intent);
    }

    @When("^I tap the start button$")
    public void iTapTheStartButton() {
        onView(withId(R.id.start_recording_button)).perform(click());
    }

    @When("^I press the stop button$")
    public void iPressTheStopButton() {
        onView(withId(R.id.stop_recording_button)).perform(click());
    }

    @When("^I press the done button$")
    public void iPressTheDoneButton() {
        onView(withId(R.id.done_tapping_button)).perform(click());
    }

    @When("^I tap the screen (.*?) times$")
    public void iTapTheScreenTimes(final int tapCount) {
        for (int i = 0; i < tapCount; i++) {
            onView(withId(R.id.tap_screen_rhythm_activity)).perform(click());
        }
    }

    @Then("^I get an error that I tapped not enough times$")
    public void iGetAnErrorThatITappedNotEnoughTimes() {
        onView(withId(R.id.error_frame)).check(matches(isDisplayed()));
        onView(withId(R.id.confirm_button)).perform(click());
    }

    @Then("^I go to WaitActivity$")
    public void iGoToWaitActivity() {
        intended(hasComponent(WaitActivity.class.getName()));
    }

}
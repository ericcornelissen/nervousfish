package com.nervousfish.nervousfish.test;

import android.content.Intent;
import android.support.test.rule.ActivityTestRule;

import com.nervousfish.nervousfish.R;
import com.nervousfish.nervousfish.activities.RhythmVerificationActivity;
import com.nervousfish.nervousfish.activities.SelectVerificationMethodActivity;
import com.nervousfish.nervousfish.activities.VisualVerificationActivity;

import org.junit.Rule;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

public class SelectVerificationMethodSteps {

    @Rule
    public ActivityTestRule<SelectVerificationMethodActivity> mActivityRule =
            new ActivityTestRule<>(SelectVerificationMethodActivity.class, true, false);

    @Given("^I am viewing the select verification method activity$")
    public void iAmViewingTheSelectVerificationMethodActivity() {
        final Intent intent = new Intent();
        this.mActivityRule.launchActivity(intent);
    }

    @When("I press the tab a rhythm button")
    public void iPressTheTabARhythmButton() {
        onView(withId(R.id.btn_select_rhythm_verification)).perform(click());
    }

    @When("I press the visual pattern button")
    public void iPressTheVisualPatternButton() {
        onView(withId(R.id.btn_select_visual_verification)).perform(click());
    }

    @Then("I should go to the rhythm activity to provide a pattern")
    public void iShouldGoToTheRhythmActivityToProvideAPattern() {
        intended(hasComponent(RhythmVerificationActivity.class.getName()));
    }

    @Then("I should go to the visual pattern activity to provide a pattern")
    public void iShouldGoToTheVisualPatternActivityToProvideAPattern() {
        intended(hasComponent(VisualVerificationActivity.class.getName()));
    }

}

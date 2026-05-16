package com.whitenoise.app;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.not;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class MainActivityTest {
    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Test
    public void activityLaunches_displaysTitle() {
        onView(withId(R.id.title)).check(matches(isDisplayed()));
        onView(withId(R.id.title)).check(matches(withText("White Noise")));
    }

    @Test
    public void playButton_initiallyEnabled() {
        onView(withId(R.id.playButton)).check(matches(isEnabled()));
    }

    @Test
    public void stopButton_initiallyDisabled() {
        onView(withId(R.id.stopButton)).check(matches(not(isEnabled())));
    }

    @Test
    public void clickPlay_enablesStopAndDisablesPlay() {
        onView(withId(R.id.playButton)).perform(click());
        onView(withId(R.id.stopButton)).check(matches(isEnabled()));
        onView(withId(R.id.playButton)).check(matches(not(isEnabled())));
    }

    @Test
    public void clickStop_afterPlay_reenablesPlay() {
        onView(withId(R.id.playButton)).perform(click());
        onView(withId(R.id.stopButton)).perform(click());
        onView(withId(R.id.playButton)).check(matches(isEnabled()));
        onView(withId(R.id.stopButton)).check(matches(not(isEnabled())));
    }

    @Test
    public void dateDisplay_isVisible() {
        onView(withId(R.id.dateDisplay)).check(matches(isDisplayed()));
    }

    @Test
    public void dayDisplay_isVisible() {
        onView(withId(R.id.dayDisplay)).check(matches(isDisplayed()));
    }

    @Test
    public void volumeSlider_isVisible() {
        onView(withId(R.id.volumeSlider)).check(matches(isDisplayed()));
    }

    @Test
    public void pitchSlider_isVisible() {
        onView(withId(R.id.pitchSlider)).check(matches(isDisplayed()));
    }

    @Test
    public void timerSlider_isVisible() {
        onView(withId(R.id.timerSlider)).check(matches(isDisplayed()));
    }

    @Test
    public void timerStatus_showsOffInitially() {
        onView(withId(R.id.timerStatus)).check(matches(withText("Timer: Off")));
    }
}

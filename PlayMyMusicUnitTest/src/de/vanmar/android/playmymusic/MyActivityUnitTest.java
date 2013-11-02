package de.vanmar.android.playmymusic;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.util.ActivityController;

@RunWith(RobolectricTestRunner.class)
public class MyActivityUnitTest {

	@Test
	public void shouldHaveCorrectAppName() throws Exception {
        ActivityController<MainActivity> activityController = Robolectric.buildActivity(MainActivity.class);
        final String appName = activityController.get().getResources().getString(
                R.string.appName);
		assertThat(appName, equalTo("Play my Music"));
	}
}

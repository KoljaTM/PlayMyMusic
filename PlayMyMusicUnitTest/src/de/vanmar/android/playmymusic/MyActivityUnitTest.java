package de.vanmar.android.playmymusic;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class MyActivityUnitTest {

	@Test
	public void shouldHaveCorrectAppName() throws Exception {
		final String appName = new MainActivity().getResources().getString(
				R.string.appName);
		assertThat(appName, equalTo("Play my Music"));
	}
}

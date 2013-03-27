package de.vanmar.android.playmymusic;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import de.vanmar.android.knitdroid.MainActivity;
import de.vanmar.android.knitdroid.R;

@RunWith(RobolectricTestRunner.class)
public class MyActivityUnitTest {

	@Test
	public void shouldHaveHappySmiles() throws Exception {
		final String hello = new MainActivity().getResources().getString(
				R.string.hello_world);
		assertThat(hello, equalTo("Hello World!"));
	}
}

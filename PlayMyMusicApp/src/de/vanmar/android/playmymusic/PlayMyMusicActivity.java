package de.vanmar.android.playmymusic;

import org.teleal.cling.model.meta.Device;

import android.support.v4.app.FragmentActivity;

import com.googlecode.androidannotations.annotations.EActivity;

import de.vanmar.android.playmymusic.fragment.RendererFragment.RendererFragmentListener;

@EActivity(resName = "playmymusicactivity")
public class PlayMyMusicActivity extends FragmentActivity implements
		RendererFragmentListener {

	private Device renderer;

	@Override
	public void onRendererSelected(final Device renderer) {
		this.renderer = renderer;
	}

	public Device getRenderer() {
		return renderer;
	}
}

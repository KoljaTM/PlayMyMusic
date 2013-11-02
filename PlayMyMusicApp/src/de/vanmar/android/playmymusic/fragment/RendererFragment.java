package de.vanmar.android.playmymusic.fragment;

import org.teleal.cling.android.AndroidUpnpService;
import org.teleal.cling.android.AndroidUpnpServiceImpl;
import org.teleal.cling.model.meta.Device;
import org.teleal.cling.model.meta.LocalDevice;
import org.teleal.cling.model.meta.RemoteDevice;
import org.teleal.cling.model.meta.Service;
import org.teleal.cling.model.types.ServiceType;
import org.teleal.cling.registry.DefaultRegistryListener;
import org.teleal.cling.registry.Registry;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.Bean;
import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.UiThread;
import com.googlecode.androidannotations.annotations.ViewById;

import de.vanmar.android.playmymusic.util.UiHelper;

@EFragment(resName = "rendererfragment")
public class RendererFragment extends Fragment {

	public interface RendererFragmentListener {
		void onRendererSelected(Device renderer);
	}

	private static final ServiceType SERVICE_TYPE_MEDIA_RENDERER = new ServiceType(
			"schemas-upnp-org", "AVTransport", 1);

	protected class BrowseRegistryListener extends DefaultRegistryListener {

		public void deviceAdded(final Device device) {
			RendererFragment.this.deviceAdded(device);
		}

		public void deviceRemoved(final Device device) {
			RendererFragment.this.deviceRemoved(device);
		}

		@Override
		public void localDeviceAdded(final Registry registry,
				final LocalDevice device) {
			deviceAdded(device);
		}

		@Override
		public void localDeviceRemoved(final Registry registry,
				final LocalDevice device) {
			deviceRemoved(device);
		}

		@Override
		public void remoteDeviceAdded(final Registry registry,
				final RemoteDevice device) {
			deviceAdded(device);
		}

		@Override
		public void remoteDeviceDiscoveryFailed(final Registry registry,
				final RemoteDevice device, final Exception ex) {
			uiHelper.displayError(ex);
			deviceRemoved(device);
		}
	}

	@ViewById(resName = "renderer")
	Spinner renderer;

	@Bean
	UiHelper uiHelper;

	private AndroidUpnpService upnpService;
	private final BrowseRegistryListener registryListener = new BrowseRegistryListener();

	private ArrayAdapter<Device> adapter;

	private final ServiceConnection serviceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(final ComponentName className,
				final IBinder service) {
			upnpService = (AndroidUpnpService) service;

			// Clear the list
			adapter.clear();

			// Get ready for future device advertisements
			upnpService.getRegistry().addListener(registryListener);

			// Now add all devices to the list we already know about
			for (final Device device : upnpService.getRegistry().getDevices()) {
				registryListener.deviceAdded(device);
			}

			// Search asynchronously for all devices, they will respond soon
			upnpService.getControlPoint().search();
		}

		@Override
		public void onServiceDisconnected(final ComponentName className) {
			upnpService = null;
		}
	};

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// This will start the UPnP service if it wasn't already started
		getActivity().bindService(
				new Intent(getActivity(), AndroidUpnpServiceImpl.class),
				serviceConnection, Context.BIND_AUTO_CREATE);
	}

	@AfterViews
	void afterViews() {
		adapter = new ArrayAdapter<Device>(getActivity(),
				android.R.layout.simple_list_item_1);
		renderer.setAdapter(adapter);
		renderer.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(final AdapterView<?> adapterView,
					final View view, final int position, final long id) {
				final Device device = adapter.getItem(position);
				((RendererFragmentListener) getActivity())
						.onRendererSelected(device);
			}

			@Override
			public void onNothingSelected(final AdapterView<?> adapterView) {
				System.out.println("unselected");
			}
		});
	}

	@Override
	public void onResume() {
		super.onResume();

	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@UiThread
	public void deviceAdded(final Device device) {
		if (!isMediaRenderer(device)) {
			return;
		}

		final int position = adapter.getPosition(device);
		if (position >= 0) {
			// Device already in the list, re-set new value at same
			// position
			adapter.remove(device);
			adapter.insert(device, position);
		} else {
			adapter.add(device);
		}
	}

	private boolean isMediaRenderer(final Device device) {
		for (final Service service : device.getServices()) {
			if (SERVICE_TYPE_MEDIA_RENDERER.equals(service.getServiceType())) {
				return true;
			}
		}
		return false;
	}

	@UiThread
	public void deviceRemoved(final Device device) {
		adapter.remove(device);
	}
}

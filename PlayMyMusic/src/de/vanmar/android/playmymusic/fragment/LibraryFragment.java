package de.vanmar.android.playmymusic.fragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.teleal.cling.android.AndroidUpnpService;
import org.teleal.cling.android.AndroidUpnpServiceImpl;
import org.teleal.cling.model.action.ActionInvocation;
import org.teleal.cling.model.message.UpnpResponse;
import org.teleal.cling.model.meta.Device;
import org.teleal.cling.model.meta.LocalDevice;
import org.teleal.cling.model.meta.RemoteDevice;
import org.teleal.cling.model.meta.Service;
import org.teleal.cling.model.types.ServiceType;
import org.teleal.cling.registry.DefaultRegistryListener;
import org.teleal.cling.registry.Registry;
import org.teleal.cling.support.contentdirectory.callback.Browse;
import org.teleal.cling.support.model.BrowseFlag;
import org.teleal.cling.support.model.DIDLContent;
import org.teleal.cling.support.model.container.Container;
import org.teleal.cling.support.model.item.Item;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.widget.ListView;

import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.Bean;
import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.UiThread;
import com.googlecode.androidannotations.annotations.ViewById;

import de.vanmar.android.playmymusic.beans.LibraryItem;
import de.vanmar.android.playmymusic.beans.LibraryItem.ItemType;
import de.vanmar.android.playmymusic.fragment.LibraryListAdapter.LibraryFragmentListener;
import de.vanmar.android.playmymusic.util.UiHelper;

@EFragment(resName = "libraryfragment")
public class LibraryFragment extends Fragment implements
		LibraryFragmentListener {

	private static final ServiceType SERVICE_TYPE_CONTENT_DIRECTORY = new ServiceType(
			"schemas-upnp-org", "ContentDirectory", 1);

	private Device currentContentDirectory = null;
	private final Stack<String> browseHierarchy = new Stack<String>();

	protected class BrowseRegistryListener extends DefaultRegistryListener {

		public void deviceAdded(final Device device) {
			LibraryFragment.this.deviceAdded(device);
		}

		public void deviceRemoved(final Device device) {
			LibraryFragment.this.deviceRemoved(device);
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

	@Bean
	UiHelper uiHelper;

	@ViewById(resName = "librarylist")
	ListView librarylist;

	private LibraryListAdapter adapter;
	private AndroidUpnpService upnpService;
	private final BrowseRegistryListener registryListener = new BrowseRegistryListener();

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
		adapter = new LibraryListAdapter(getActivity(), this);
		librarylist.setAdapter(adapter);
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
		if (!isMediaServer(device)) {
			return;
		}

		final LibraryItem item = new LibraryItem(ItemType.DEVICE);
		item.setDevice(device);
		final int position = adapter.getPosition(item);
		if (position >= 0) {
			// Device already in the list, re-set new value at same
			// position
			adapter.remove(item);
			adapter.insert(item, position);
		} else {
			adapter.add(item);
		}
	}

	private boolean isMediaServer(final Device device) {
		for (final Service service : device.getServices()) {
			if (SERVICE_TYPE_CONTENT_DIRECTORY.equals(service.getServiceType())) {
				return true;
			}
		}
		return false;
	}

	@UiThread
	public void deviceRemoved(final Device device) {
		final LibraryItem item = new LibraryItem(ItemType.DEVICE);
		item.setDevice(device);
		adapter.remove(item);
	}

	@Override
	public void onItemSelected(final LibraryItem item) {
		final String parent;
		if (item.getType() == ItemType.DEVICE) {
			currentContentDirectory = item.getDevice();
			browseHierarchy.clear();
			parent = "0";
		} else if (item.getType() == ItemType.CONTAINER) {
			parent = item.getContainer().getId();
		} else {
			uiHelper.displayError("Play not yet implemented");
			return;
		}

		browseHierarchy.push(parent);
		upnpService.getControlPoint().execute(
				new Browse(currentContentDirectory
						.findService(SERVICE_TYPE_CONTENT_DIRECTORY), parent,
						BrowseFlag.DIRECT_CHILDREN) {

					@Override
					public void failure(
							final ActionInvocation paramActionInvocation,
							final UpnpResponse upnpResponse,
							final String message) {
						uiHelper.displayError(message);
					}

					@Override
					public void updateStatus(final Status paramStatus) {
						// nothing to do
					}

					@Override
					public void received(
							final ActionInvocation actionInvocation,
							final DIDLContent content) {
						final List<LibraryItem> items = new ArrayList<LibraryItem>();
						for (final Container container : content
								.getContainers()) {
							final LibraryItem libraryItem = new LibraryItem(
									ItemType.CONTAINER);
							libraryItem.setContainer(container);
							items.add(libraryItem);
						}
						for (final Item item : content.getItems()) {
							final LibraryItem libraryItem = new LibraryItem(
									ItemType.ITEM);
							libraryItem.setItem(item);
							items.add(libraryItem);
						}
						displayLibraryItems(items);
					}

				});
	}

	@UiThread
	protected void displayLibraryItems(final List<LibraryItem> items) {
		adapter.clear();
		adapter.addAll(items);
	}
}

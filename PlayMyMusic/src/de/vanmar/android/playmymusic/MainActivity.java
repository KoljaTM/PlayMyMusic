package de.vanmar.android.playmymusic;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.teleal.cling.android.AndroidUpnpService;
import org.teleal.cling.android.AndroidUpnpServiceImpl;
import org.teleal.cling.model.meta.Device;
import org.teleal.cling.model.meta.LocalDevice;
import org.teleal.cling.model.meta.RemoteDevice;
import org.teleal.cling.model.meta.Service;
import org.teleal.cling.registry.DefaultRegistryListener;
import org.teleal.cling.registry.Registry;
import org.teleal.cling.transport.Router;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends ListActivity {

	protected class BrowseRegistryListener extends DefaultRegistryListener {

		public void deviceAdded(final Device device) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					final DeviceDisplay d = new DeviceDisplay(device);
					final int position = listAdapter.getPosition(d);
					if (position >= 0) {
						// Device already in the list, re-set new value at same
						// position
						listAdapter.remove(d);
						listAdapter.insert(d, position);
					} else {
						listAdapter.add(d);
					}
				}
			});
		}

		public void deviceRemoved(final Device device) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					listAdapter.remove(new DeviceDisplay(device));
				}
			});
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
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(
							MainActivity.this,
							"Discovery failed of '"
									+ device.getDisplayString()
									+ "': "
									+ (ex != null ? ex.toString()
											: "Couldn't retrieve device/service descriptors"),
							Toast.LENGTH_LONG).show();
				}
			});
			deviceRemoved(device);
		}

		/*
		 * End of optimization, you can remove the whole block if your Android
		 * handset is fast (>= 600 Mhz)
		 */

		/* Discovery performance optimization for very slow Android devices! */
		@Override
		public void remoteDeviceDiscoveryStarted(final Registry registry,
				final RemoteDevice device) {
			deviceAdded(device);
		}

		@Override
		public void remoteDeviceRemoved(final Registry registry,
				final RemoteDevice device) {
			deviceRemoved(device);
		}
	}

	protected class DeviceDisplay {

		Device device;

		public DeviceDisplay(final Device device) {
			this.device = device;
		}

		@Override
		public boolean equals(final Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || getClass() != o.getClass()) {
				return false;
			}
			final DeviceDisplay that = (DeviceDisplay) o;
			return device.equals(that.device);
		}

		public String getDetailsMessage() {
			final StringBuilder sb = new StringBuilder();
			if (getDevice().isFullyHydrated()) {
				sb.append(getDevice().getDisplayString());
				sb.append("\n\n");
				for (final Service service : getDevice().getServices()) {
					sb.append(service.getServiceType()).append("\n");
				}
			} else {
				sb.append(getString(R.string.deviceDetailsNotYetAvailable));
			}
			return sb.toString();
		}

		public Device getDevice() {
			return device;
		}

		@Override
		public int hashCode() {
			return device.hashCode();
		}

		@Override
		public String toString() {
			final String name = getDevice().getDetails() != null
					&& getDevice().getDetails().getFriendlyName() != null ? getDevice()
					.getDetails().getFriendlyName() : getDevice()
					.getDisplayString();
			// Display a little star while the device is being loaded (see
			// performance optimization earlier)
			return device.isFullyHydrated() ? name : name + " *";
		}
	}

	private ArrayAdapter<DeviceDisplay> listAdapter;

	private BrowseRegistryListener registryListener = new BrowseRegistryListener();

	private AndroidUpnpService upnpService;

	private ServiceConnection serviceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(final ComponentName className,
				final IBinder service) {
			upnpService = (AndroidUpnpService) service;

			// Clear the list
			listAdapter.clear();

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

		// Fix the logging integration between java.util.logging and Android
		// internal logging
		// org.seamless.util.logging.LoggingUtil
		// .resetRootHandler(new org.seamless.android.FixedAndroidLogHandler());
		// Now you can enable logging as needed for various categories of Cling:
		// Logger.getLogger("org.fourthline.cling").setLevel(Level.FINEST);

		listAdapter = new ArrayAdapter<DeviceDisplay>(this,
				android.R.layout.simple_list_item_1);
		setListAdapter(listAdapter);

		// This will start the UPnP service if it wasn't already started
		getApplicationContext().bindService(
				new Intent(this, AndroidUpnpServiceImpl.class),
				serviceConnection, Context.BIND_AUTO_CREATE);
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		menu.add(0, 0, 0, R.string.searchLAN).setIcon(
				android.R.drawable.ic_menu_search);
		menu.add(0, 1, 0, R.string.switchRouter).setIcon(
				android.R.drawable.ic_menu_revert);
		menu.add(0, 2, 0, R.string.toggleDebugLogging).setIcon(
				android.R.drawable.ic_menu_info_details);
		return true;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (upnpService != null) {
			upnpService.getRegistry().removeListener(registryListener);
		}
		// This will stop the UPnP service if nobody else is bound to it
		getApplicationContext().unbindService(serviceConnection);
	}

	@Override
	protected void onListItemClick(final ListView l, final View v,
			final int position, final long id) {
		final AlertDialog dialog = new AlertDialog.Builder(this).create();
		dialog.setTitle(R.string.deviceDetails);
		final DeviceDisplay deviceDisplay = (DeviceDisplay) l
				.getItemAtPosition(position);
		dialog.setMessage(deviceDisplay.getDetailsMessage());
		dialog.setButton(getString(R.string.OK),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(final DialogInterface dialog,
							final int which) {
					}
				});
		dialog.show();
		final TextView textView = (TextView) dialog
				.findViewById(android.R.id.message);
		textView.setTextSize(12);
		super.onListItemClick(l, v, position, id);
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
		case 0:
			if (upnpService == null) {
				break;
			}
			Toast.makeText(this, R.string.searchingLAN, Toast.LENGTH_SHORT)
					.show();
			upnpService.getRegistry().removeAllRemoteDevices();
			upnpService.getControlPoint().search();
			break;
		case 1:
			if (upnpService != null) {
				final Router router = upnpService.get().getRouter();
				// try {
				// if (router.isEnabled()) {
				// Toast.makeText(this, R.string.disablingRouter,
				// Toast.LENGTH_SHORT).show();
				// router.disable();
				// } else {
				// Toast.makeText(this, R.string.enablingRouter,
				// Toast.LENGTH_SHORT).show();
				// router.enable();
				// }
				// } catch (final RouterException ex) {
				// Toast.makeText(
				// this,
				// getText(R.string.errorSwitchingRouter)
				// + ex.toString(), Toast.LENGTH_LONG).show();
				// ex.printStackTrace(System.err);
				// }
			}
			break;
		case 2:
			final Logger logger = Logger.getLogger("org.fourthline.cling");
			if (logger.getLevel() != null
					&& !logger.getLevel().equals(Level.INFO)) {
				Toast.makeText(this, R.string.disablingDebugLogging,
						Toast.LENGTH_SHORT).show();
				logger.setLevel(Level.INFO);
			} else {
				Toast.makeText(this, R.string.enablingDebugLogging,
						Toast.LENGTH_SHORT).show();
				logger.setLevel(Level.FINEST);
			}
			break;
		}
		return false;
	}
}

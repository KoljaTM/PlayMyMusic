package de.vanmar.android.playmymusic.beans;

import org.teleal.cling.model.meta.Device;
import org.teleal.cling.support.model.container.Container;
import org.teleal.cling.support.model.item.Item;

public class LibraryItem {

	public enum ItemType {
		DEVICE {
			@Override
			protected String getTitle(final LibraryItem item) {
				return item.getDevice().getDetails().getFriendlyName();
			}
		},
		CONTAINER {
			@Override
			protected String getTitle(final LibraryItem item) {
				return item.getContainer().getTitle();
			}
		},
		ITEM {
			@Override
			protected String getTitle(final LibraryItem item) {
				return item.getItem().getTitle();
			}
		};
		protected abstract String getTitle(LibraryItem item);
	}

	private final ItemType type;
	private Device device;
	private Container container;
	private Item item;

	public LibraryItem(final ItemType type) {
		super();
		this.type = type;
	}

	public ItemType getType() {
		return type;
	}

	public String getTitle() {
		return type.getTitle(this);
	}

	public Device getDevice() {
		return device;
	}

	public void setDevice(final Device device) {
		this.device = device;
	}

	public Container getContainer() {
		return container;
	}

	public void setContainer(final Container container) {
		this.container = container;
	}

	public Item getItem() {
		return item;
	}

	public void setItem(final Item item) {
		this.item = item;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((container == null) ? 0 : container.hashCode());
		result = prime * result + ((device == null) ? 0 : device.hashCode());
		result = prime * result + ((item == null) ? 0 : item.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final LibraryItem other = (LibraryItem) obj;
		if (container == null) {
			if (other.container != null) {
				return false;
			}
		} else if (!container.equals(other.container)) {
			return false;
		}
		if (device == null) {
			if (other.device != null) {
				return false;
			}
		} else if (!device.equals(other.device)) {
			return false;
		}
		if (item == null) {
			if (other.item != null) {
				return false;
			}
		} else if (!item.equals(other.item)) {
			return false;
		}
		return true;
	}
}

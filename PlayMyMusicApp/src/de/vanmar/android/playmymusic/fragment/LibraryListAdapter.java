package de.vanmar.android.playmymusic.fragment;

import android.app.Activity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import de.vanmar.android.playmymusic.R;
import de.vanmar.android.playmymusic.beans.LibraryItem;

public class LibraryListAdapter extends ArrayAdapter<LibraryItem> {

	public interface LibraryAdapterListener {
		void onItemSelected(LibraryItem item);
	}

	private final LibraryAdapterListener handler;

	private static class ViewHolder {
		public TextView title;
	}

	protected LibraryListAdapter(final Activity context,
			final LibraryAdapterListener handler) {
		super(context, R.layout.libraryitem, R.id.title);
		this.handler = handler;
	}

	@Override
	public final View getView(final int position, final View convertView,
			final ViewGroup parent) {
		final View view = super.getView(position, convertView, parent);

		if (view.getTag() == null) {
			final ViewHolder viewHolder = new ViewHolder();
			viewHolder.title = (TextView) view.findViewById(R.id.title);
			view.setTag(viewHolder);
		}

		final ViewHolder holder = (ViewHolder) view.getTag();
		final LibraryItem item = getItem(position);
		holder.title.setText(item.getTitle());

		view.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(final View v) {
				handler.onItemSelected(item);
			}
		});

		return view;
	}
}

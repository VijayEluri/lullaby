package net.sileht.lullaby;

/* Copyright (c) 20010 ABAAKOUK Mehdi  <theli48@gmail.com>
 *
 * +------------------------------------------------------------------------+
 * | This program is free software; you can redistribute it and/or          |
 * | modify it under the terms of the GNU General Public License            |
 * | as published by the Free Software Foundation; either version 2         |
 * | of the License, or (at your option) any later version.                 |
 * |                                                                        |
 * | This program is distributed in the hope that it will be useful,        |
 * | but WITHOUT ANY WARRANTY; without even the implied warranty of         |
 * | MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the          |
 * | GNU General Public License for more details.                           |
 * |                                                                        |
 * | You should have received a copy of the GNU General Public License      |
 * | along with this program; if not, write to the Free Software            |
 * | Foundation, Inc., 59 Temple Place - Suite 330,                         |
 * | Boston, MA  02111-1307, USA.                                           |
 * +------------------------------------------------------------------------+
 */
import java.util.ArrayList;

import net.sileht.lullaby.R;
import net.sileht.lullaby.SongsActivity.ViewHolder;
import net.sileht.lullaby.objects.Playlist;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AlphabetIndexer;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SectionIndexer;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class PlaylistsActivity extends Activity {
	private MatrixCursor playlistsData;

	private SimpleCursorAdapter mAdapter;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.list_classic);
		ListView lv = (ListView) findViewById(R.id.list);
		ViewUtils e = new ViewUtils(this);
		lv.setOnItemClickListener(e);
		lv.setOnItemLongClickListener(e);
		lv.setOnCreateContextMenuListener(e);

		if (playlistsData == null) {
			// Tell them we're loading

			setProgressBarVisibility(true);

			playlistsData = new MatrixCursor(ViewUtils.mPlaylistsColumnName);
			startManagingCursor(playlistsData);

			AmpacheRequest request = new AmpacheRequest((Activity) this,
					new String[] { "playlists", "" }) {
				@SuppressWarnings("unchecked")
				@Override
				public void add_objects(ArrayList list) {
					for (Playlist playlist : (ArrayList<Playlist>) list) {
						playlistsData.newRow().add(playlist.id).add(playlist.name).add(playlist.tracks).add(playlist.owner);
						playlistsData.requery();
					}
				}
			};
			request.send();
		}
		mAdapter = new PlaylistsAdapter(this, playlistsData);
		lv.setAdapter(mAdapter);

	}
	static class PlaylistsAdapter extends SimpleCursorAdapter  implements SectionIndexer{

        private AlphabetIndexer mIndexer;
		private final StringBuilder mBuffer = new StringBuilder();
        
		public PlaylistsAdapter(Context context, Cursor cursor) {
			super(context, R.layout.track_list_item_mini, cursor,new String[] {}, new int[] {});

			Resources r = context.getResources();
			mIndexer = new AlphabetIndexer(cursor, cursor.getColumnIndex(ViewUtils.PLAYLIST_NAME), 
                    r.getString(R.string.fast_scroll_numeric_alphabet));
		}
		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			View v = super.newView(context, cursor, parent);
			ViewHolder vh = new ViewHolder();
			vh.line1 = (TextView) v.findViewById(R.id.line1);
			vh.line2 = (TextView) v.findViewById(R.id.line2);
			vh.play_indicator = (ImageView) v.findViewById(R.id.play_indicator);
			vh.icon = (ImageView) v.findViewById(R.id.icon);
			vh.duration = (TextView) v.findViewById(R.id.duration);
			v.setTag(vh);
			return v;
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {

			String name = cursor.getString(cursor
					.getColumnIndexOrThrow(ViewUtils.PLAYLIST_NAME));

			int numsongs = cursor.getInt(cursor
					.getColumnIndexOrThrow(ViewUtils.PLAYLIST_TRACKS));
			
			String owner = cursor.getString(cursor
					.getColumnIndexOrThrow(ViewUtils.PLAYLIST_OWNER));

			String displayname = name;
			boolean unknown = name == null;
			if (unknown) {
				displayname = "Unknown";
			}

			ViewHolder vh = (ViewHolder) view.getTag();
			vh.line1.setText(displayname);
			

			final StringBuilder builder = mBuffer;
			builder.delete(0, builder.length());

			if (numsongs == 1) {
				builder.append("1 song");
			} else {
				builder.append(numsongs + " songs");
			}

			vh.line2.setText(owner + " - " + builder.toString());
			
			vh.icon.setImageDrawable(null);
			vh.play_indicator.setImageDrawable(null);
			vh.duration.setVisibility(View.GONE);

		}
		@Override
        public Object[] getSections() {
            return mIndexer.getSections();
        }

		@Override
        public int getPositionForSection(int sectionIndex) {
            return mIndexer.getPositionForSection(sectionIndex);
        }

		@Override
        public int getSectionForPosition(int position) {
            return 0;
        }
		
	}
}

package com.ideabag.playtunes.adapter;

import com.ideabag.playtunes.R;
import com.ideabag.playtunes.PlaylistManager;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.ToggleButton;

public class SongsAllAdapter extends BaseAdapter {
	
	private Context mContext;
	Cursor cursor;
	private PlaylistManager PlaylistManager;
	
	View.OnClickListener songMenuClickListener;
	
    private static final String[] allSongsSelection = new String[] {
    	
    	MediaStore.Audio.Media._ID,
    	
    	MediaStore.Audio.Media.TITLE,
    	MediaStore.Audio.Media.ARTIST,
    	MediaStore.Audio.Media.ALBUM,
    	MediaStore.Audio.Media.TRACK,
    	MediaStore.Audio.Media.DATA,
    	MediaStore.Audio.Media.ALBUM_ID,
    	MediaStore.Audio.Media.ARTIST_ID
    	
    	
    };
    
    public Cursor getCursor() {
    	
    	return cursor;
    	
    }
    
	public SongsAllAdapter( Context context, View.OnClickListener menuClickListener ) {
		
		mContext = context;
		
		PlaylistManager = new PlaylistManager( mContext );
		
    	cursor = mContext.getContentResolver().query(
					MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
					allSongsSelection,
					null,
					null,
					MediaStore.Audio.Media.TITLE
    			);
    	
    	this.songMenuClickListener = menuClickListener;
		
	}
	
	@Override
	public int getCount() {
		
		return cursor.getCount();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override public View getView( int position, View convertView, ViewGroup parent ) {
		
		
		if ( null == convertView ) {
			
			LayoutInflater li = ( LayoutInflater ) mContext.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
			
			convertView = li.inflate( R.layout.list_item_song_no_album, null );
			
			convertView.findViewById( R.id.StarButton ).setOnClickListener( songMenuClickListener );
			convertView.findViewById( R.id.MenuButton ).setOnClickListener( songMenuClickListener );
			
		}
		
		cursor.moveToPosition( position );
		
		String songTitle = cursor.getString( cursor.getColumnIndexOrThrow( MediaStore.Audio.Media.TITLE ) );
		String songArtist = cursor.getString( cursor.getColumnIndexOrThrow( MediaStore.Audio.Media.ARTIST ) );
		String songAlbum = cursor.getString( cursor.getColumnIndexOrThrow( MediaStore.Audio.Media.ALBUM ) );
		String song_id = cursor.getString( cursor.getColumnIndexOrThrow( MediaStore.Audio.Media._ID ) );
		
		( ( TextView ) convertView.findViewById( R.id.SongTitle ) ).setText( songTitle );
		( ( TextView ) convertView.findViewById( R.id.SongArtist ) ).setText( songArtist );
		( ( TextView ) convertView.findViewById( R.id.SongAlbum ) ).setText( songAlbum );
		
		convertView.findViewById( R.id.StarButton ).setTag( R.id.tag_song_id, song_id );
		convertView.findViewById( R.id.MenuButton ).setTag( R.id.tag_song_id, song_id );
		
		ToggleButton starButton = ( ToggleButton ) convertView.findViewById( R.id.StarButton );
		
		starButton.setChecked( PlaylistManager.isStarred( song_id ) ); 
		
		
		return convertView;
		
	}
	


}

package com.ideabag.playtunes.fragment;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.ideabag.playtunes.R;
import com.ideabag.playtunes.activity.MainActivity;
import com.ideabag.playtunes.adapter.ArtistAllSongsAdapter;
import com.ideabag.playtunes.dialog.SongMenuDialogFragment;
import com.ideabag.playtunes.util.PlaylistBrowser;
import com.ideabag.playtunes.util.TrackerSingleton;

import android.app.Activity;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.ListView;
import android.widget.ToggleButton;

public class ArtistAllSongsFragment extends ListFragment implements PlaylistBrowser {
	
	public static final String TAG = "Artist All Songs";
	
	ArtistAllSongsAdapter adapter;
	
	private MainActivity mActivity;
	
	private String ARTIST_ID = "";
	
	@Override public void setMediaID( String media_id ) {
		
		ARTIST_ID = media_id;
		
	}
	
	@Override public String getMediaID() { return ARTIST_ID; }
	
	@Override public void onAttach( Activity activity ) {
			
		super.onAttach( activity );
		
		mActivity = ( MainActivity ) activity;
		
	}
    
	@Override public void onActivityCreated( Bundle savedInstanceState ) {
		super.onActivityCreated( savedInstanceState );
		
		//android.util.Log.i( "ARTIST_ID", ARTIST_ID );
    	adapter = new ArtistAllSongsAdapter( getActivity(), ARTIST_ID, songMenuClickListener );
		
		getView().setBackgroundColor( getResources().getColor( android.R.color.white ) );
		getListView().setDivider( getResources().getDrawable( R.drawable.list_divider ) );
		getListView().setDividerHeight( 1 );
		
    	setListAdapter( adapter );
		
    	getActivity().getContentResolver().registerContentObserver(
    			MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, true, mediaStoreChanged );
    	
	}
	
	@Override public void onResume() {
		super.onResume();
		
    	mActivity.setActionbarTitle( adapter.ARTIST_NAME );
    	mActivity.setActionbarSubtitle( getString( R.string.all_songs ) );
		
		Tracker t = TrackerSingleton.getDefaultTracker( mActivity );

	        // Set screen name.
	        // Where path is a String representing the screen name.
		t.setScreenName( TAG );
		//t.set( "_count", ""+adapter.getCount() );
		
	        // Send a screen view.
		t.send( new HitBuilders.AppViewBuilder().build() );
		
		t.send( new HitBuilders.EventBuilder()
    	.setCategory( "playlist" )
    	.setAction( "show" )
    	.setLabel( TAG )
    	.setValue( adapter.getCount() )
    	.build());
		
	}
		
	@Override public void onPause() {
		super.onPause();
		
		
		
	}
	
	@Override public void onDestroyView() {
	    super.onDestroyView();
	    
	    setListAdapter( null );
	    
	}
	
	@Override public void onDestroy() {
		super.onDestroy();
		
		getActivity().getContentResolver().unregisterContentObserver( mediaStoreChanged );
		
	}
	
	@Override public void onListItemClick( ListView l, View v, int position, long id ) {
		
		String playlistName = mActivity.getSupportActionBar().getTitle().toString();
		
		mActivity.mBoundService.setPlaylist( adapter.getCursor(), playlistName, ArtistAllSongsFragment.class, ARTIST_ID );
		
		mActivity.mBoundService.setPlaylistPosition( position - l.getHeaderViewsCount() );
		
		mActivity.mBoundService.play();
		
	}
	
	View.OnClickListener songMenuClickListener = new View.OnClickListener() {
		
		@Override public void onClick( View v ) {
			
			int viewID = v.getId();
			String songID = "" + v.getTag( R.id.tag_song_id );
			
			if ( viewID == R.id.StarButton ) {
				
				ToggleButton starButton = ( ToggleButton ) v;
				
				if ( starButton.isChecked() ) {
					
					mActivity.PlaylistManager.addFavorite( songID );
					//android.util.Log.i( "starred", songID );
					
				} else {
					
					mActivity.PlaylistManager.removeFavorite( songID );
					//android.util.Log.i( "unstarred", songID );
					
				}
				
			} else if ( viewID == R.id.MenuButton ) {
				
				FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
	        	
				SongMenuDialogFragment newFragment = new SongMenuDialogFragment();
				newFragment.setMediaID( songID );
	        	
	            newFragment.show( ft, "dialog" );
				
			}
			
			
			
		}
		
	};
	
	ContentObserver mediaStoreChanged = new ContentObserver(new Handler()) {

        @Override public void onChange( boolean selfChange ) {
            
            mActivity.runOnUiThread( new Runnable() {

				@Override public void run() {
					
					adapter.requery();
					adapter.notifyDataSetChanged();
				
				}
            	
            });
            
            super.onChange( selfChange );
            
        }

	};
	
	
}
package com.ideabag.playtunes.fragment;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.ideabag.playtunes.R;
import com.ideabag.playtunes.activity.MainActivity;
import com.ideabag.playtunes.adapter.GenresAllAdapter;
import com.ideabag.playtunes.database.MediaQuery;
import com.ideabag.playtunes.util.GAEvent.Playlist;
import com.ideabag.playtunes.util.IMusicBrowser;
import com.ideabag.playtunes.util.TrackerSingleton;
import com.ideabag.playtunes.util.GAEvent.Categories;

import android.app.Activity;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ListView;

public class GenresAllFragment extends SaveScrollListFragment implements IMusicBrowser {
	
	public static final String TAG = "All Genres Fragment";
	
	GenresAllAdapter adapter;
	
	private MainActivity mActivity;
	private Tracker mTracker;
	
	@Override public void onAttach( Activity activity ) {
			
		super.onAttach( activity );
		
		mActivity = ( MainActivity ) activity;
		mTracker = TrackerSingleton.getDefaultTracker( mActivity );
		mActivity.setActionbarTitle( getString( R.string.genres_plural) );
		
		mTracker.setScreenName( TAG );
		
	}
    
	@Override public void onActivityCreated( Bundle savedInstanceState ) {
		super.onActivityCreated( savedInstanceState );
		
		adapter = new GenresAllAdapter( getActivity(), new MediaQuery.OnQueryCompletedListener() {
			
			@Override public void onQueryCompleted( MediaQuery mQuery, Cursor mResult ) {
				
				mActivity.setActionbarSubtitle( adapter.getCount() + " " + ( adapter.getCount() == 1 ? getString( R.string.genre_singular ) : getString( R.string.genres_plural ) ) );
		    	
				restoreScrollPosition();
				
		    	mTracker.send( new HitBuilders.EventBuilder()
		    	.setCategory( Categories.PLAYLIST )
		    	.setAction( Playlist.ACTION_SHOWLIST )
		    	.setValue( mResult.getCount() )
		    	.build());
				
			}
			
		});
		
		getView().setBackgroundColor( getResources().getColor( android.R.color.white ) );
		getListView().setDivider( getResources().getDrawable( R.drawable.list_divider ) );
		getListView().setDividerHeight( 1 );
		getListView().setSelector( R.drawable.list_item_background );
		
		
    	setListAdapter( adapter );
		
		getActivity().getContentResolver().registerContentObserver(
				MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI, true, mediaStoreChanged );
    	
    	
	}
		
	@Override public void onResume() {
		super.onResume();
		
		mActivity.setActionbarTitle( getString( R.string.genres_plural) );
    	
	    // Send a screen view.
    	mTracker.send( new HitBuilders.AppViewBuilder().build() );
		
	}
		
	@Override public void onPause() {
		super.onPause();
		//mActivity.AdView.pause();
		
		
	}
	
	@Override public void onDestroyView() {
	    super.onDestroyView();
	    
	    //setListAdapter( null );
	    
	}
	
	@Override public void onDestroy() {
		super.onDestroy();
		
		getActivity().getContentResolver().unregisterContentObserver( mediaStoreChanged );
		
	}
	
	@Override public void onListItemClick( ListView l, View v, int position, long id ) {
		
		String genre_id = (String) v.getTag( R.id.tag_genre_id );
		
		GenresOneFragment genreFragment = new GenresOneFragment();
		
		genreFragment.setMediaID( genre_id );
		
		mActivity.transactFragment( genreFragment );
		
    	mTracker.send( new HitBuilders.EventBuilder()
    	.setCategory( Categories.PLAYLIST )
    	.setAction( Playlist.ACTION_CLICK )
    	.setValue( position )
    	.build());
		
	}

	// PlaylistBrowser interface methods
	
	@Override public void setMediaID(String media_id) { /* ... */ }

	@Override public String getMediaID() { return ""; }
	
	
	ContentObserver mediaStoreChanged = new ContentObserver(new Handler()) {

        @Override public void onChange( boolean selfChange ) {
            
            mActivity.runOnUiThread( new Runnable() {

				@Override public void run() {
					
					saveScrollPosition();
					adapter.requery();
				
				}
            	
            });
            
            super.onChange( selfChange );
            
        }

	};
	
}
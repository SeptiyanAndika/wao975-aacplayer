package com.webcraftbd.radio;

import java.net.URL;
import java.util.Collection;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

import net.roarsoftware.lastfm.*;

public class LastFMCover {
	
	public static String album;
	public static String artist;
	public static String track;
	
	public static String apiKey = "7fb78a81b20bee7cb6e8fad4cbcb3694";
	
	public static String getCoverUrlFromAlbum(String artist, String album) throws Exception {
		System.out.println("Getting getting image from Album: "+album+" - "+artist);
		Album albumObj = Album.getInfo(artist, album, apiKey);
		return albumObj.getImageURL(ImageSize.LARGE);
	}
	
	public static String getCoverUrlFromTrack(String artist, String track) throws Exception {
		
		int end = artist.toLowerCase().indexOf("feat");
        if(end>0)
        	artist = artist.substring(0, end).trim();
        
        end = track.toLowerCase().indexOf("feat");
        if(end>0)
        	track = track.substring(0, end).trim();
		
		System.out.println("Getting getting image from Track: "+track+" - "+artist);
		Track trackObj = Track.getInfo(artist, track, apiKey);
		album = trackObj.getAlbum();
		return trackObj.getImageURL(ImageSize.LARGE);
	}
	
	public static Bitmap getCoverImageFromAlbum(String artist, String album) {
		
		try {
			String imagepath = getCoverUrlFromAlbum(artist, album);
			System.out.println(imagepath);
			if(imagepath!=null) {
				URL url = new URL(imagepath);
				Bitmap bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
				return bmp;
			}			
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("No Cover Image Found");
		return null;
	}
	
	public static Bitmap getCoverImageFromTrack(String artist, String track) {
		
		try {
			String imagepath = getCoverUrlFromTrack(artist, track);
			System.out.println(imagepath);
			if(imagepath!=null) {
				URL url = new URL(imagepath);
				Bitmap bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
				return bmp;
			}			
		} catch (Exception e) {			
			e.printStackTrace();
		}
		System.out.println("No Cover Image Found");
		album = "";
		return null;		
	}
}

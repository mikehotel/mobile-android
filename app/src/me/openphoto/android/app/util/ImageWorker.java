
package me.openphoto.android.app.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import me.openphoto.android.app.BuildConfig;
import me.openphoto.android.app.ui.widget.ActionBar;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.BaseAdapter;
import android.widget.ImageView;

public class ImageWorker {

	private static final int HTTP_CACHE_SIZE = 10 * 1024 * 1024; // 10MB
    public static final String HTTP_CACHE_DIR = "http";
	public static final String TAG = "HSD";
    
    private HashMap<String, Drawable> imageCache;
    private static Drawable DEFAULT_ICON = null;
    private BaseAdapter adapt;
    private ActionBar mActionBar;
	private final DiskLruCache diskCache;

    static {
        // DEFAULT_ICON =
        // Resources.getSystem().getDrawable(R.drawable.newest_photo_noimage);
    }

    public ImageWorker(Context ctx, ActionBar actionBar)
    {
        imageCache = new HashMap<String, Drawable>();
        mActionBar = actionBar;
		final File cacheDir = DiskLruCache.getDiskCacheDir(ctx,
				HTTP_CACHE_DIR);
		diskCache =
				DiskLruCache.openCache(ctx, cacheDir, HTTP_CACHE_SIZE);
    }

    public Drawable loadImage(BaseAdapter adapt, ImageView view)
    {
        this.adapt = adapt;
        String url = (String) view.getTag();
        if (imageCache.containsKey(url))
        {
            return imageCache.get(url);
        }
        else {
            synchronized (this) {
                imageCache.put(url, DEFAULT_ICON);
            }
            new ImageTask().execute(url);
            return DEFAULT_ICON;
        }
    }

    private class ImageTask extends AsyncTask<String, Void, Drawable>
    {
        private String s_url;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (mActionBar != null)
                mActionBar.startLoading();
        }

        @Override
        protected Drawable doInBackground(String... params) {
            s_url = params[0];

			File cachedFile = getCachedFile(s_url);
			if (cachedFile != null)
			{
				return Drawable.createFromPath(cachedFile.getAbsolutePath());
			}

            InputStream istr;
            try {
                URL url = new URL(s_url);
                istr = url.openStream();
            } catch (MalformedURLException e) {
				Log.d(TAG, "Malformed: " + e.getMessage());
                throw new RuntimeException(e);
            } catch (IOException e)
            {
				Log.d(TAG, "I/O : " + e.getMessage());
                throw new RuntimeException(e);

            }
			Drawable result = Drawable.createFromStream(istr, "src");
			if (result instanceof BitmapDrawable)
			{
				diskCache.put(s_url, ((BitmapDrawable) result).getBitmap());
			}
			return result;
        }

        @Override
        protected void onPostExecute(Drawable result) {
            super.onPostExecute(result);
            if (mActionBar != null)
                mActionBar.stopLoading();

            synchronized (this) {
                imageCache.put(s_url, result);
            }
            adapt.notifyDataSetChanged();
        }

		protected File getCachedFile(String urlString)
		{
			final File cacheFile = new File(diskCache.createFilePath(urlString));

			if (diskCache.containsKey(urlString))
			{
				if (BuildConfig.DEBUG)
				{
					Log.d(TAG, "downloadBitmap - found in http cache - "
							+ urlString);
				}
				return cacheFile;
			}
			return null;
		}
    }
}

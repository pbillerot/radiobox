package fr.pbillerot.radiobox;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
        implements AdapterView.OnItemClickListener {
    private static final String TAG = "MainActivity";

    ListView mListView;
    MyListAdapter mAdapter;

    private View mViewCurrent;
    ArrayList<AudioItem> mAudioItems = new ArrayList<>();
    int mStreamId = -1;

    private MediaPlayer mPlayer;
    private String mPlayerUrlSong = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

    }

    @Override
    protected void onStart() {
        super.onStart();  // Always call the superclass method first
        // Activity being restarted from stopped state

        try {

            mListView = findViewById(R.id.list_view);

            mAdapter = new MyListAdapter(this, mAudioItems);
            mListView.setAdapter(mAdapter);
            mListView.setOnItemClickListener(this);

            new LoadXmlAsyncTask().execute("https://pbillerot.github.io/memodoc/audio.xml");

            Toast.makeText(getApplicationContext()
                    , "Radiobox Version "  + BuildConfig.VERSION_NAME
                    , Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
            e.printStackTrace();
            Toast.makeText(getApplicationContext()
                    , "Radiobox - Radio not found"
                    , Toast.LENGTH_LONG).show();

        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        AudioItem audioItem = (AudioItem) parent.getItemAtPosition(position);
        if ( mViewCurrent == view ) {
            // Visuel : on supprime la surbrillance de la view du media en cours
            // -> aucune ligne n'est sélectionnée
            mViewCurrent.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
            view.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
            // On arrête le media en cours
            stopPlaying();
            mStreamId = -1;
            mViewCurrent = null;
        } else {
            if ( mStreamId != -1 ) {
                // Visuel : on supprime la surbrillance de la view du media en cours
                mViewCurrent.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
                view.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
                // On arrête le media en cours
                stopPlaying();
            }
            // Visuel : on met en surbrillance la ligne de la view sélectionnée
            view.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary));
            mViewCurrent = view;
            // On démarre le media
            // à noter que l'indice des éléments dans le pool commence à 1 (position+1 de la view)
            startPlaying(audioItem.audio_url);
            mStreamId = position;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        // Démarrage de l'activité associé au menu
        if (id == R.id.action_settings) {
            Intent is = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(is);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop() {
        super.onStop();  // Always call the superclass method first
        // Arrêt du media en cours
        stopPlaying();
    }

    /**
     * Chargement en asynchrone du fichier XML des audios
     */
    private class LoadXmlAsyncTask extends AsyncTask<String, Void, ArrayList<AudioItem>> {

        /**
         * Récup du paramètre fourni
         * new LoadXmlAsyncTask().execute(mUrlXml);
         * @param arg_url
         * @return ArrayList<AudioItem>
         */
        protected ArrayList<AudioItem> doInBackground(String... arg_url) {
            // Some long-running task like downloading an image.
            // ... code shown above to send request and retrieve string builder
            ArrayList<AudioItem> audioItems = new ArrayList<AudioItem>();
            try {
                if (BuildConfig.DEBUG) Log.d(TAG, "LoadXmlAsyncTask.doInBackground " + arg_url[0]);
                InputStream stream;
                if ( arg_url[0].startsWith("http")) {
                    URL url = new URL(arg_url[0]);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                    conn.setReadTimeout(10000 /* milliseconds */);
                    conn.setConnectTimeout(15000 /* milliseconds */);
                    conn.setRequestMethod("GET");
                    conn.setDoInput(true);
                    conn.connect();
                    stream = conn.getInputStream();
                } else {
                    stream = new FileInputStream(new File(arg_url[0]));
                }

                XmlPullParserFactory xmlFactoryObject = XmlPullParserFactory.newInstance();
                XmlPullParser parser = xmlFactoryObject.newPullParser();

                parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                parser.setInput(stream, null);

                audioItems = AudioItem.parseXML(parser);

                stream.close();

            }
            catch (Exception e) {
                e.printStackTrace();
            }
            return audioItems;
        }

        // On récupère le résulat de doInBackground
        protected void onPostExecute(ArrayList<AudioItem> audioItems) {
            // This method is executed in the UIThread
            // with access to the result of the long running task

            // Rechargement de l'adapter
            mAdapter.clear();
            for (int i = 0; i < audioItems.size(); i++) {
                AudioItem audioItem = audioItems.get(i);
                mAdapter.add(audioItem);
            }
        }
    }

    private void startPlaying(String url) {
        if ( mPlayer != null && mPlayer.isPlaying() ) {
            stopPlaying();
        }
        if ( BuildConfig.DEBUG ) Log.d(TAG,"startPlaying() " + url);
        mPlayerUrlSong = url;

        mPlayer = new MediaPlayer();
        try {
            mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mPlayer.setDataSource(url);

            mPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    // ... react appropriately ...
                    // The MediaPlayer has moved to the Error state, must be reset!
                    Log.e(TAG, "MediaPlayer what:" + what + " extra: + extra");
                    return false;
                }
            });

            mPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                public void onPrepared(MediaPlayer mp) {
                    if (mp == mPlayer) {
                        mPlayer.start();
                    }
                }
            });
            mPlayer.prepareAsync();

        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            Log.e(TAG, e.getMessage() + " : " + e.getStackTrace().toString());
        } catch (IllegalStateException e) {
            e.printStackTrace();
            Log.e(TAG, e.getMessage() + " : " + e.getStackTrace().toString());
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, e.getMessage() + " : " + e.getStackTrace().toString());
        }

    }

    private void stopPlaying() {
        if ( BuildConfig.DEBUG ) Log.d(TAG,"stopPlaying()");
        if (mPlayer != null && mPlayer.isPlaying()) {
            mPlayer.stop();
            mPlayer.release();
            mPlayer = null;
            mPlayerUrlSong = "";
        }
    }


}

package com.mirwanda.nottiled;

import android.*;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.*;
import android.content.pm.*;
import android.database.Cursor;
import android.net.Uri;
import android.os.*;
import android.provider.DocumentsContract;
import android.provider.OpenableColumns;

import com.badlogic.gdx.backends.android.*;
//import com.google.android.gms.ads.*;//

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
//import javax.annotation.*;
//import org.solovyev.android.checkout.*;

//import com.mirwanda.nottiled.BuildConfig;//


public class MainActivity extends AndroidApplication implements Interface
{

	private static final int CREATE_REQUEST_CODE = 40;
	private static final int OPEN_REQUEST_CODE = 41;
	private static final int REGAIN_ACCESS_CODE = 44;
	private static final int SAVEAS_REQUEST_CODE = 43;
	private static final int BINARY_CREATE_CODE = 39;
	private static final int REQUEST_TREE_CODE = 45;
	Uri currentMAP = null;
	SharedPreferences myPrefs;
	SharedPreferences.Editor prefs;
	PackageInfo pInfo;
	String version;
	@Override
	public void speak(final String s)
	{
		
			
		//tts.speak(s, TextToSpeech.QUEUE_FLUSH, null);
		
		// TODO: Implement this method
	}

	@Override
	public AndroidAudio createAudio(Context context, AndroidApplicationConfiguration config){
	return new AsynchronousAndroidAudio( context, config );
	}

	@Override
	public void changelanguage(String lang)
	{

	}


	@Override
	public boolean ispro()
	{
		return proVersion;
	}

	@Override
	public String getVersione()
	{
		return version;
	}





	@Override
	public byte[] getData() {
		return SAFdata;
	}

	@Override
	public java.util.List<byte[]> getDatas() {
		return SAFdatas;
	}

	@Override
	public String getFilename() {
		return SAFfilename;
	}

	@Override
	public String getUri() {
		return SAFuri;
	}

	@Override
	public java.util.List<String> getFilenames() {
		return SAFfilenames;
	}

	@Override
	public String getStatus() {
		return SAFstatus;
	}

	@Override
	public String getOS() {
		return vers;
	}


	boolean proVersion = true;
	
	@Override
	public void showinterstitial(){
		runOnUiThread(() -> {
});
	}
	
	@Override
	public void showbanner(final boolean show)
	{
		runOnUiThread(new Runnable() {
				@Override
				public void run() {
					/**/
					if (proVersion) 
					{
						//adView.setVisibility(View.GONE);
						return;
					}
					if (show){
						//adView.setVisibility(View.VISIBLE);
					}else
					{
						//adView.setVisibility(View.GONE);
					}
					/**/
				}
			});
	}

	@Override
	public boolean buyadfree()
	{
		runOnUiThread(new Runnable() {
				@Override
				public void run() {
					//mCheckout.startPurchaseFlow(ProductTypes.IN_APP, AD_FREE, null, new PurchaseListener());
				}
			});
		return true;
	}
	
	
	
	
	
	
	
/*
    private class PurchaseListener extends EmptyRequestListener<Purchase> {
        @Override
        public void onSuccess(@Nonnull Purchase purchase) {
          	proVersion=true;
        }
    }

    private class InventoryCallback implements Inventory.Callback {
        @Override
        public void onLoaded(@Nonnull Inventory.Products products) {
            final Inventory.Product product = products.get(ProductTypes.IN_APP);
            if (!product.supported) {
                return;
            }
            if (product.isPurchased(AD_FREE)) {
				proVersion= true;
                return;
            }
        }
    }

 */

	final static int APP_STORAGE_ACCESS_REQUEST_CODE = 501; // Any value
	public String vers;
	public void requestAccess(){
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
			vers="android10+";
		}else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Build.VERSION.SDK_INT < Build.VERSION_CODES.R ) {
			vers="android9-";
			if (this.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
				this.requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1024);
			}
		}else{
			vers="android9-";
		}
	}

	public void runGDX(){
		String intend="";
		Intent intent = getIntent();
		if (intent.getData()!=null) intend=intent.getData().toString();

		AndroidApplicationConfiguration cfg = new AndroidApplicationConfiguration();
		initialize(new NotiledAndoridPro(intend,this), cfg);

	}

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        myPrefs = getSharedPreferences("NotTiled", 0);
		prefs = myPrefs.edit();

		try {
			currentMAP = Uri.parse(myPrefs.getString("url", ""));
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		requestAccess();
		runGDX();


		try {
			pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			version = pInfo.versionName;
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}
    }

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged( hasFocus );
	}

	@Override
	public void newFile()
	{
		SAFstatus="";
		SAFdata=null;
		Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);

		intent.addCategory(Intent.CATEGORY_OPENABLE);
		intent.setType("*/*");
		intent.putExtra(Intent.EXTRA_TITLE, "newfile.tmx");
		startActivityForResult(intent, CREATE_REQUEST_CODE);
	}

	@Override
	public void openFile()
	{
		SAFstatus="";
		SAFdata=null;

		Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		intent.addFlags(
				Intent.FLAG_GRANT_READ_URI_PERMISSION
						| Intent.FLAG_GRANT_WRITE_URI_PERMISSION
						| Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
						| Intent.FLAG_GRANT_PREFIX_URI_PERMISSION);
		intent.setType("*/*");
		startActivityForResult(intent, OPEN_REQUEST_CODE);
	}

	@Override
	public void selectFolder()
	{
		SAFstatus="";
		SAFdata=null;
		Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
		startActivityForResult(intent, REQUEST_TREE_CODE);
	}

	@Override
	public String getdatafromURI(String URI)
	{
		currentMAP = Uri.parse(URI);
		//
		prefs.putString("url", currentMAP.toString());
		prefs.commit();
		try {
			String s = readFileContent( currentMAP );
			return s;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void setOrientation(int ori) {
		switch (ori){
			case 0: //both
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
				break;
			case 1: //landscape
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
				break;
			case 2: //portrait
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
				break;
		}
	}

	@Override
	public void saveFile(String data)
	{
		if (currentMAP ==null) {
			SAFstatus="";
			SAFdata=null;
			tmpdata = data;

			Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
			intent.addCategory(Intent.CATEGORY_OPENABLE);
			intent.addFlags(
					Intent.FLAG_GRANT_READ_URI_PERMISSION
							| Intent.FLAG_GRANT_WRITE_URI_PERMISSION
							| Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
							| Intent.FLAG_GRANT_PREFIX_URI_PERMISSION);
			intent.setType("*/*");
			startActivityForResult(intent, REGAIN_ACCESS_CODE);

		}else{
			writeFileContent( currentMAP, data);
		}
	}

	String tmpdata;
	byte[] bytedata;
	@Override
	public void saveasFile(String data, String filenamesuggestion)
	{
		tmpdata = data;
		SAFdata = null;
		SAFstatus="";
		Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		intent.setType("*/*");
		intent.putExtra(Intent.EXTRA_TITLE, filenamesuggestion);
		startActivityForResult(intent, CREATE_REQUEST_CODE);
	}

	@Override
	public void saveasFile(byte[] data, String filenamesuggestion)
	{
		bytedata = data;
		SAFdata = null;
		Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		intent.setType("*/*");
		intent.putExtra(Intent.EXTRA_TITLE, filenamesuggestion);
		startActivityForResult(intent, BINARY_CREATE_CODE);
	}


	java.util.List<byte[]> SAFdatas = new ArrayList<byte[]>();
	java.util.List<String> SAFfilenames = new ArrayList<String>();

	byte[] SAFdata;
	String SAFfilename="";
	String SAFuri="";
	String SAFstatus="";

	@SuppressLint("WrongConstant")
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent resultData) {
		//mCheckout.onActivityResult(requestCode, resultCode, data);

		if (resultCode == RESULT_OK)
		{
			if (requestCode == CREATE_REQUEST_CODE)
			{
				if (resultData != null) {
					Uri uri = resultData.getData();
					writeFileContent( uri, tmpdata);
					SAFuri = uri.toString();
					SAFfilename = getFileName( uri );

					if (SAFfilename.endsWith( "tmx" )){
						currentMAP = resultData.getData();
						prefs.putString("url", currentMAP.toString());
						prefs.commit();

						final int takeFlags = resultData.getFlags()
								& (Intent.FLAG_GRANT_READ_URI_PERMISSION
								| Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
						getContentResolver().takePersistableUriPermission(uri, takeFlags);
						SAFstatus="ok";
					}

				}else{
					SAFstatus="cancel";
				}
			}
			if (requestCode == BINARY_CREATE_CODE)
			{
				if (resultData != null) {
					Uri uri = resultData.getData();
					writeByte( uri, bytedata);
				}
			}
			else if (requestCode == SAVEAS_REQUEST_CODE) {

				if (resultData != null) {
					currentMAP = resultData.getData();
					writeFileContent( currentMAP, "test");
				}
			}
			else if (requestCode == REQUEST_TREE_CODE) {

				if (resultData != null) {
					java.util.List<Uri> docs = readFiles( this,resultData );
					SAFdatas.clear(); SAFfilenames.clear();
					for (Uri u : docs){
						try {
							byte[] b = readBytes( u );
							String s = getFileName( u );
							SAFdatas.add( b );
							SAFfilenames.add( s );
						} catch (IOException e) {
							e.printStackTrace();
						}

					}
					SAFstatus = "OK";
				}
			}
			else if (requestCode == OPEN_REQUEST_CODE) {

				if (resultData != null) {

					try {
						Uri uri = resultData.getData();
						SAFdata = readBytes( uri );
						SAFfilename = getFileName( uri );
						SAFuri = uri.toString();

						if (SAFfilename.endsWith( "tmx" )){
							currentMAP = uri;
							prefs.putString("url", currentMAP.toString());
							prefs.commit();
							final int takeFlags = resultData.getFlags()
									& (Intent.FLAG_GRANT_READ_URI_PERMISSION
									| Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
							getContentResolver().takePersistableUriPermission(uri, takeFlags);

						}

						SAFstatus = "OK";
					} catch (IOException e) {
						SAFstatus = "error";
					}
				}
			} else if (requestCode == REGAIN_ACCESS_CODE) {

				if (resultData != null) {
					currentMAP = resultData.getData();
					SAFuri = currentMAP.toString();
					writeFileContent( currentMAP, tmpdata);
				}
			}else{
			}

		}
		super.onActivityResult(requestCode, resultCode, resultData);
	}

	private void writeFileContent(Uri uri, String data)
	{
		try{
			ParcelFileDescriptor pfd =
					this.getContentResolver().
							openFileDescriptor(uri, "rwt");

			FileOutputStream fileOutputStream =
					new FileOutputStream(
							pfd.getFileDescriptor());

			String textContent = data;

			fileOutputStream.write(textContent.getBytes());

			fileOutputStream.close();
			pfd.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void writeByte(Uri uri, byte[] data)
	{
		try{
			ParcelFileDescriptor pfd =
					this.getContentResolver().
							openFileDescriptor(uri, "rwt");

			FileOutputStream fileOutputStream =
					new FileOutputStream(
							pfd.getFileDescriptor());

			fileOutputStream.write( data );
			fileOutputStream.close();
			pfd.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}



	private byte[] readBytes(Uri uri) throws IOException {

		InputStream inputStream =
				getContentResolver().openInputStream(uri);


		ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
		int bufferSize = 1024;
		byte[] buffer = new byte[bufferSize];

		int len = 0;

		while ((len = inputStream.read(buffer)) != -1) {
			byteBuffer.write(buffer, 0, len);
		}
		inputStream.close();
		return byteBuffer.toByteArray();
	}


	private String readFileContent(Uri uri) throws IOException {

		InputStream inputStream =
				getContentResolver().openInputStream(uri);
		BufferedReader reader =
				new BufferedReader(new InputStreamReader(
						inputStream));
		StringBuilder stringBuilder = new StringBuilder();
		String currentline;
		while ((currentline = reader.readLine()) != null) {
			stringBuilder.append(currentline + "\n");
		}
		inputStream.close();
		return stringBuilder.toString();
	}

	public String getFileName(Uri uri) {
		String result = null;
		if (uri.getScheme().equals("content")) {
			Cursor cursor = getContentResolver().query(uri, null, null, null, null);
			try {
				if (cursor != null && cursor.moveToFirst()) {
					int dx = cursor.getColumnIndex( OpenableColumns.DISPLAY_NAME);
					if(dx!=-1) result = cursor.getString(dx);
				}
			} finally {
				cursor.close();
			}
		}
		if (result == null) {
			result = uri.getPath();
			int cut = result.lastIndexOf('/');
			if (cut != -1) {
				result = result.substring(cut + 1);
			}
		}
		return result;
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	private List<Uri> readFiles(Context context, Intent intent) {
		List<Uri> uriList = new ArrayList<>();

		// the uri returned by Intent.ACTION_OPEN_DOCUMENT_TREE
		Uri uriTree = intent.getData();
		// the uri from which we query the files
		Uri uriFolder = DocumentsContract.buildChildDocumentsUriUsingTree(uriTree, DocumentsContract.getTreeDocumentId(uriTree));

		Cursor cursor = null;
		try {
			// let's query the files
			cursor = context.getContentResolver().query(uriFolder,
					new String[]{DocumentsContract.Document.COLUMN_DOCUMENT_ID},
					null, null, null);

			if (cursor != null && cursor.moveToFirst()) {
				do {
					// build the uri for the file
					Uri uriFile = DocumentsContract.buildDocumentUriUsingTree(uriTree, cursor.getString(0));
					//add to the list
					uriList.add(uriFile);

				} while (cursor.moveToNext());
			}

		} catch (Exception e) {
			// TODO: handle error
		} finally {
			if (cursor!=null) cursor.close();
		}

		//return the list
		return uriList;
	}
}

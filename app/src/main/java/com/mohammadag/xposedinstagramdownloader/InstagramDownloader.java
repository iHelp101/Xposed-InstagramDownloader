package com.mohammadag.xposedinstagramdownloader;

import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.callStaticMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static de.robv.android.xposed.XposedHelpers.getStaticObjectField;

import java.io.File;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DownloadManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class InstagramDownloader implements IXposedHookLoadPackage, IXposedHookZygoteInit {

	private CharSequence[] mMenuOptions = null;
	private CharSequence[] mDirectShareMenuOptions = null;
	private Object mCurrentMediaOptionButton;
	private Object mCurrentDirectShareMediaOptionButton;
	private static final String mDownloadString = "Download";
	private static String mDownloadTranslated;

	private static Context mContext;
    private static XSharedPreferences mPreferences;

	private static Class<?> MediaType;
	private static Class<?> User;

	private static String FEED_CLASS_NAME = null;
	private static String MEDIA_CLASS_NAME = null;
	private static String MEDIA_TYPE_CLASS_NAME = null;
	private static String USER_CLASS_NAME = null;
	private static String MEDIA_OPTIONS_BUTTON_CLASS_NAME = null;
	private static String DS_MEDIA_OPTIONS_BUTTON_CLASS_NAME = null;
	private static String DS_PERM_MORE_OPTIONS_DIALOG_CLASS_NAME = null;
    private static String MEDIA_OPTIONS_BUTTON_HOOK = null;
    private static String MEDIA_OPTIONS_BUTTON_HOOK2 = null;
    private static String PERM__HOOK = null;
    private static String PERM__HOOK2 = null;
    private static String mMEDIA_HOOK = null;
    private static String VIDEOTYPE_HOOK = null;
    private static String mMEDIA_VIDEO_HOOK = null;
    private static String mMEDIA_PHOTO_HOOK = null;
    private static String USERNAME_HOOK = null;
    private static String FULLNAME__HOOK = null;



    private static void log(String log) {
		XposedBridge.log("InstagramDownloader: " + log);
	}

    @Override
    public void initZygote(IXposedHookZygoteInit.StartupParam startupParam) throws Throwable {
        mPreferences = new XSharedPreferences("com.mohammadag.xposedinstagramdownloader", "Hooks");
    }

	@Override
	public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {
		if (!lpparam.packageName.equals("com.instagram.android"))
			return;

        // Thank you to KeepChat For the Following Code Snippet
        // http://git.io/JJZPaw
        Object activityThread = callStaticMethod(findClass("android.app.ActivityThread", null), "currentActivityThread");
        final Context context = (Context) callMethod(activityThread, "getSystemContext");

        final int versionCheck = context.getPackageManager().getPackageInfo(lpparam.packageName, 0).versionCode;
        //End Snippet

        XposedBridge.log("Version Code: "+versionCheck);

        FEED_CLASS_NAME = mPreferences.getString("First", null);
        MEDIA_CLASS_NAME = mPreferences.getString("Second", null);
        MEDIA_TYPE_CLASS_NAME = mPreferences.getString("Third", null);
        USER_CLASS_NAME = mPreferences.getString("Fourth", null);
        MEDIA_OPTIONS_BUTTON_CLASS_NAME = mPreferences.getString("Fifth", null);
        DS_MEDIA_OPTIONS_BUTTON_CLASS_NAME = mPreferences.getString("Sixth", null);
        DS_PERM_MORE_OPTIONS_DIALOG_CLASS_NAME = mPreferences.getString("Seventh", null);
        MEDIA_OPTIONS_BUTTON_HOOK = mPreferences.getString("Eighth", null);
        MEDIA_OPTIONS_BUTTON_HOOK2 = mPreferences.getString("Ninth", null);
        PERM__HOOK = mPreferences.getString("Tenth", null);
        PERM__HOOK2 = mPreferences.getString("Eleventh", null);
        mMEDIA_HOOK = mPreferences.getString("Twelfth", null);
        VIDEOTYPE_HOOK = mPreferences.getString("Thirteenth", null);
        mMEDIA_VIDEO_HOOK = mPreferences.getString("Fourteenth", null);
        mMEDIA_PHOTO_HOOK = mPreferences.getString("Fifteenth", null);
        USERNAME_HOOK = mPreferences.getString("Sixteenth", null);
        FULLNAME__HOOK = mPreferences.getString("Seventeenth", null);

        mContext = context;

		/* Hi Facebook team! Obfuscating the package isn't enough */
		final Class<?> MediaOptionsButton = findClass(MEDIA_OPTIONS_BUTTON_CLASS_NAME, lpparam.classLoader);
		final Class<?> DirectSharePermalinkMoreOptionsDialog = findClass(DS_MEDIA_OPTIONS_BUTTON_CLASS_NAME,
				lpparam.classLoader);
		MediaType = findClass(MEDIA_TYPE_CLASS_NAME, lpparam.classLoader);
		User = findClass(USER_CLASS_NAME, lpparam.classLoader);

		XC_MethodHook injectDownloadIntoCharSequenceHook = new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				CharSequence[] result = (CharSequence[]) param.getResult();

				ArrayList<String> array = new ArrayList<String>();
				for (CharSequence sq : result)
					array.add(sq.toString());

				if (mContext == null) {
					try {
						Field f = XposedHelpers.findFirstFieldByExactType(param.thisObject.getClass(), Context.class);
						f.setAccessible(true);
						mContext = (Context) f.get(param.thisObject);
					} catch (Throwable t) {
						log("Unable to get Context, button not translated");
					}
				}

				if (mContext != null) {
					mDownloadTranslated = ResourceHelper.getString(mContext, R.string.the_not_so_big_but_big_button);
				}

				if (!array.contains(getDownloadString()))
					array.add(getDownloadString());
				CharSequence[] newResult = new CharSequence[array.size()];
				array.toArray(newResult);
				Field menuOptionsField;
				if (param.thisObject.getClass().getName().contains("directshare")) {
					menuOptionsField = XposedHelpers.findFirstFieldByExactType(param.thisObject.getClass(), CharSequence[].class);
				} else {
					menuOptionsField = XposedHelpers.findFirstFieldByExactType(MediaOptionsButton, CharSequence[].class);
				}
				menuOptionsField.set(param.thisObject, newResult);
				if (param.thisObject.getClass().getName().contains("directshare")) {
					mDirectShareMenuOptions  = (CharSequence[]) menuOptionsField.get(param.thisObject);
				} else {
					mMenuOptions = (CharSequence[]) menuOptionsField.get(param.thisObject);
				}
				param.setResult(newResult);
			}
		};

		findAndHookMethod(MediaOptionsButton, MEDIA_OPTIONS_BUTTON_HOOK, injectDownloadIntoCharSequenceHook);
        findAndHookMethod(MediaOptionsButton, MEDIA_OPTIONS_BUTTON_HOOK2, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                mCurrentMediaOptionButton = param.thisObject;
            }
        });


		findAndHookMethod(DirectSharePermalinkMoreOptionsDialog, PERM__HOOK, injectDownloadIntoCharSequenceHook);
		findAndHookMethod(DirectSharePermalinkMoreOptionsDialog, PERM__HOOK2, new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				mCurrentDirectShareMediaOptionButton = param.thisObject;
			}
		});

		Class<?> DirectShareMenuClickListener = findClass(DS_PERM_MORE_OPTIONS_DIALOG_CLASS_NAME, lpparam.classLoader);
		findAndHookMethod(DirectShareMenuClickListener, "onClick", DialogInterface.class, int.class, new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				CharSequence localCharSequence = mDirectShareMenuOptions[(Integer) param.args[1]];
				if (mContext == null)
					mContext = ((Dialog) param.args[0]).getContext();
				if (getDownloadString().equals(localCharSequence)) {
					Object mMedia = null;

					Field[] mCurrentMediaOptionButtonFields =
							mCurrentDirectShareMediaOptionButton.getClass().getDeclaredFields();
					for (Field iField : mCurrentMediaOptionButtonFields) {
						if (iField.getType().getName().equals(MEDIA_CLASS_NAME)) {
							iField.setAccessible(true);
							mMedia = iField.get(mCurrentDirectShareMediaOptionButton);
							break;
						}
					}

					if (mMedia == null) {
						Toast.makeText(mContext, ResourceHelper.getString(mContext, R.string.direct_share_download_failed),
								Toast.LENGTH_SHORT).show();
						log("Unable to determine media");
						return;
					}

					if (isPackageInstalled(mContext, "com.mohammadag.xposedinstagramdownloaderdonate")) {
						downloadMedia(mCurrentDirectShareMediaOptionButton, mMedia);
					} else {
						showRequiresDonatePackage(mContext);
					}
					param.setResult(null);
				}
			}
		});

		Class<?> MenuClickListener = findClass(FEED_CLASS_NAME, lpparam.classLoader);
		findAndHookMethod(MenuClickListener, "onClick", DialogInterface.class, int.class, new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				if (mContext == null) {
                    mContext = context;
                }
				CharSequence localCharSequence = mMenuOptions[(Integer) param.args[1]];
				if (mDownloadString.equals(localCharSequence)) {
					Object mMedia = null;

					try {
						mMedia = getObjectField(mCurrentMediaOptionButton, mMEDIA_HOOK);
					} catch (NoSuchFieldError e) {
						log("Failed to get media: " + e.getMessage());
						e.printStackTrace();
					}

					if (mMedia == null) {
						Toast.makeText(mContext, "Unable to determine media, download failed",
								Toast.LENGTH_SHORT).show();
						log("Unable to determine media");
						return;
					}

					try {
						downloadMedia(mCurrentMediaOptionButton, mMedia);
					} catch (Throwable t) {
						log("Unable to download media: " + t.getMessage());
						t.printStackTrace();
					}
					param.setResult(null);
				}
			}
		});
	}

	@SuppressLint("NewApi")
	private static void downloadMedia(Object sourceButton, Object mMedia) throws IllegalAccessException, IllegalArgumentException {
		Field contextField =
				XposedHelpers.findFirstFieldByExactType(sourceButton.getClass(), Context.class);
		if (mContext == null) {
			try {
				mContext = (Context) contextField.get(sourceButton);
			} catch (Exception e) {
				e.printStackTrace();
				log("Failed to get Context");
				return;
			}
		}

		log("Downloading media...");
		Object mMediaType = getFieldByType(mMedia, MediaType);
		if (mMediaType == null) {
			log("Failed to get MediaType");
			return;
		}

		Object videoType = getStaticObjectField(MediaType, VIDEOTYPE_HOOK);

        if (videoType == null) {
            log("Video Type not found!");
        }

		String linkToDownload;
		String filenameExtension;
		String descriptionType;
		int descriptionTypeId = R.string.photo;
		
//		String[] qualities = { "m", "l", "k", "o", "n" };
//		for (String field : qualities) {
//			XposedBridge.log("InstagramDownloader: " + field + ": " + (String) getObjectField(mMedia, field));
//		}

		if (mMediaType.equals(videoType)) {
            linkToDownload = (String) getObjectField(mMedia, mMEDIA_VIDEO_HOOK);
			filenameExtension = "mp4";
			descriptionType = "video";
			descriptionTypeId = R.string.video;
		} else {
			linkToDownload = (String) getObjectField(mMedia, mMEDIA_PHOTO_HOOK);
			filenameExtension = "jpg";
			descriptionType = "photo";
			descriptionTypeId = R.string.photo;
		}

		// Construct filename
		// username_imageId.jpg
		descriptionType = ResourceHelper.getString(mContext, descriptionTypeId);
		String toastMessage = ResourceHelper.getString(mContext, R.string.downloading, descriptionType);
		Toast.makeText(mContext, toastMessage, Toast.LENGTH_SHORT).show();

		Object mUser = getFieldByType(mMedia, User);
		String userName, userFullName;
		if (mUser == null) {
			log("Failed to get User from Media, using placeholders");
			userName = "username_placeholder";
			userFullName = "Unknown name";
		} else {
			userName = (String) getObjectField(mUser, USERNAME_HOOK);
			userFullName = (String) getObjectField(mUser, FULLNAME__HOOK);
		}

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH);
        String itemId = sdf.format(new Date());
		String fileName = userName + "_" + itemId + "." + filenameExtension;

		if (TextUtils.isEmpty(userFullName)) {
			userFullName = userName;
		}

		File directory =
				new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/Instagram");
		if (!directory.exists())
			directory.mkdirs();

		String notificationTitle = ResourceHelper.getString(mContext, R.string.username_thing, userFullName, descriptionType);
		String description = ResourceHelper.getString(mContext, R.string.instagram_item,
				descriptionType);

		DownloadManager.Request request = new DownloadManager.Request(Uri.parse(linkToDownload));
		request.setTitle(notificationTitle);
		request.setDescription(description);
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
			request.allowScanningByMediaScanner();
			request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
		}
		request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "Instagram/" + fileName);

		DownloadManager manager = (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);
		manager.enqueue(request);
	}

	public static final boolean isPackageInstalled(Context context, String packageName) {
		PackageManager pm = context.getPackageManager();
		try {
			pm.getPackageInfo(packageName, PackageManager.GET_META_DATA);
			return "com.android.vending".equals(pm.getInstallerPackageName(packageName));
		} catch (NameNotFoundException e) {
			return false;
		}
	}

	@SuppressLint("NewApi")
	private static final void showRequiresDonatePackage(final Context context) {
		String title = ResourceHelper.getString(context, R.string.requires_donation_package_title);
        Toast.makeText(mContext, title, Toast.LENGTH_SHORT).show();

        String url = null;

        try {
            context.getPackageManager().getPackageInfo("com.android.vending", 0);
            url = "market://details?id=com.mohammadag.xposedinstagramdownloaderdonate";
        } catch ( final Exception e ) {
            url = "https://play.google.com/store/apps/details?id=com.mohammadag.xposedinstagramdownloaderdonate";
        }

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

	private String getDownloadString() {
		if (mDownloadTranslated == null)
			return mDownloadString;

		return mDownloadTranslated;
	}

	private static Object getFieldByType(Object object, Class<?> type) {
		Field f = XposedHelpers.findFirstFieldByExactType(object.getClass(), type);
		try {
			return f.get(object);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			return null;
		}
	}
}

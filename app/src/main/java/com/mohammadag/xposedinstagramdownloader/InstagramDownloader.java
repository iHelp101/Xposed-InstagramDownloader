package com.mohammadag.xposedinstagramdownloader;

import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.callStaticMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static de.robv.android.xposed.XposedHelpers.getStaticObjectField;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AppOpsManager;
import android.app.Dialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;
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

    private static String SAVE = "Instagram";
	private static String FEED_CLASS_NAME = "Nope";
	private static String MEDIA_CLASS_NAME = "Nope";
	private static String MEDIA_TYPE_CLASS_NAME = "Nope";
	private static String USER_CLASS_NAME = "Nope";
	private static String MEDIA_OPTIONS_BUTTON_CLASS_NAME = "Nope";
	private static String DS_MEDIA_OPTIONS_BUTTON_CLASS_NAME = "Nope";
	private static String DS_PERM_MORE_OPTIONS_DIALOG_CLASS_NAME = "Nope";
    private static String MEDIA_OPTIONS_BUTTON_HOOK = "Nope";
    private static String MEDIA_OPTIONS_BUTTON_HOOK2 = "Nope";
    private static String PERM__HOOK = "Nope";
    private static String PERM__HOOK2 = "Nope";
    private static String mMEDIA_HOOK = "Nope";
    private static String VIDEOTYPE_HOOK = "Nope";
    private static String mMEDIA_VIDEO_HOOK = "Nope";
    private static String mMEDIA_PHOTO_HOOK = "Nope";
    private static String USERNAME_HOOK = "Nope";
    private static String FULLNAME__HOOK = "Nope";



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

        XposedBridge.log("Instagram Version Code: "+versionCheck);

        mPreferences.reload();

        if (mPreferences.getString("First", "Nope").equals("Nope")) {
            mPreferences = new XSharedPreferences("com.mohammadag.xposedinstagramdownloader", "Hooks");
        }

        SAVE = mPreferences.getString("Save", "Instagram");
        FEED_CLASS_NAME = mPreferences.getString("First", "Nope");
        MEDIA_CLASS_NAME = mPreferences.getString("Second", "Nope");
        MEDIA_TYPE_CLASS_NAME = mPreferences.getString("Third", "Nope");
        USER_CLASS_NAME = mPreferences.getString("Fourth", "Nope");
        MEDIA_OPTIONS_BUTTON_CLASS_NAME = mPreferences.getString("Fifth", "Nope");
        DS_MEDIA_OPTIONS_BUTTON_CLASS_NAME = mPreferences.getString("Sixth", "Nope");
        DS_PERM_MORE_OPTIONS_DIALOG_CLASS_NAME = mPreferences.getString("Seventh", "Nope");
        MEDIA_OPTIONS_BUTTON_HOOK = mPreferences.getString("Eighth", "Nope");
        MEDIA_OPTIONS_BUTTON_HOOK2 = mPreferences.getString("Ninth", "Nope");
        PERM__HOOK = mPreferences.getString("Tenth", "Nope");
        PERM__HOOK2 = mPreferences.getString("Eleventh", "Nope");
        mMEDIA_HOOK = mPreferences.getString("Twelfth", "Nope");
        VIDEOTYPE_HOOK = mPreferences.getString("Thirteenth", "Nope");
        mMEDIA_VIDEO_HOOK = mPreferences.getString("Fourteenth", "Nope");
        mMEDIA_PHOTO_HOOK = mPreferences.getString("Fifteenth", "Nope");
        USERNAME_HOOK = mPreferences.getString("Sixteenth", "Nope");
        FULLNAME__HOOK = mPreferences.getString("Seventeenth", "Nope");

        mContext = context;

        if (FEED_CLASS_NAME.equals("Nope")||MEDIA_CLASS_NAME.equals("Nope")||MEDIA_TYPE_CLASS_NAME.equals("Nope")||USER_CLASS_NAME.equals("Nope")||MEDIA_OPTIONS_BUTTON_CLASS_NAME.equals("Nope")||DS_MEDIA_OPTIONS_BUTTON_CLASS_NAME.equals("Nope")||DS_PERM_MORE_OPTIONS_DIALOG_CLASS_NAME.equals("Nope")||MEDIA_OPTIONS_BUTTON_HOOK.equals("Nope")||MEDIA_OPTIONS_BUTTON_HOOK2.equals("Nope")||PERM__HOOK.equals("Nope")||PERM__HOOK2.equals("Nope")||mMEDIA_HOOK.equals("Nope")||VIDEOTYPE_HOOK.equals("Nope")||mMEDIA_VIDEO_HOOK.equals("Nope")||mMEDIA_PHOTO_HOOK .equals("Nope")||USERNAME_HOOK.equals("Nope")||FULLNAME__HOOK.equals("Nope")) {
            XposedBridge.log("Please update hooks via the module.");
        } else {
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
                        mDirectShareMenuOptions = (CharSequence[]) menuOptionsField.get(param.thisObject);
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

        if (SAVE.equals("Instagram")) {
            File directory =
                    new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/Instagram");
            if (!directory.exists())
                directory.mkdirs();
        } else {
            File directory = new File(URI.create(SAVE).getPath());
            if (!directory.exists()) {
                directory.mkdirs();
            }
        }

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

        if (SAVE.equals("Instagram")) {
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "Instagram/" + fileName);
        } else {
            request.setDestinationUri(Uri.parse(SAVE + "/" +fileName));
        }

        DownloadManager manager = (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);
		manager.enqueue(request);
	}

	public static final boolean isPackageInstalled(Context context, String packageName) {
		try {
            context.getPackageManager().getApplicationInfo(packageName, 0);
			return true;
		} catch (NameNotFoundException e) {
			return false;
		}
	}

	@SuppressLint("NewApi")
	private static final void showRequiresDonatePackage(final Context context) {
		String title = ResourceHelper.getString(context, R.string.requires_donation_package_title);
        Toast.makeText(mContext, title, Toast.LENGTH_SHORT).show();

        String url;

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

    private static int[] appendInt(int[] cur, int val)
    {
        if (cur == null) {
            return new int[]
                    { val };
        }
        final int N = cur.length;
        for (int i = 0; i < N; i++) {
            if (cur[i] == val) {
                return cur;
            }
        }
        int[] ret = new int[N + 1];
        System.arraycopy(cur, 0, ret, 0, N);
        ret[N] = val;
        return ret;
    }
}


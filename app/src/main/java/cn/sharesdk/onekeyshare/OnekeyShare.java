package cn.sharesdk.onekeyshare;

import static com.mob.tools.utils.BitmapHelper.captureView;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.ShareSDK;
import com.mob.tools.utils.R;

public class OnekeyShare {
	private HashMap<String, Object> params;

	public OnekeyShare() {
		params = new HashMap<String, Object>();
		params.put("customers", new ArrayList<CustomerLogo>());
		params.put("hiddenPlatforms", new HashMap<String, String>());
	}

	public void setAddress(String address) {
		params.put("address", address);
	}

	public void setTitle(String title) {
		params.put("title", title);
	}

	public void setTitleUrl(String titleUrl) {
		params.put("titleUrl", titleUrl);
	}

	public void setText(String text) {
		params.put("text", text);
	}

	public String getText() {
		return params.containsKey("text") ? String.valueOf(params.get("text"))
				: null;
	}

	public void setImagePath(String imagePath) {
		if (!TextUtils.isEmpty(imagePath))
			params.put("imagePath", imagePath);
	}

	public void setImageUrl(String imageUrl) {
		if (!TextUtils.isEmpty(imageUrl))
			params.put("imageUrl", imageUrl);
	}

	public void setUrl(String url) {
		params.put("url", url);
	}

	public void setFilePath(String filePath) {
		params.put("filePath", filePath);
	}

	public void setComment(String comment) {
		params.put("comment", comment);
	}

	public void setSite(String site) {
		params.put("site", site);
	}

	public void setSiteUrl(String siteUrl) {
		params.put("siteUrl", siteUrl);
	}

	public void setVenueName(String venueName) {
		params.put("venueName", venueName);
	}

	public void setVenueDescription(String venueDescription) {
		params.put("venueDescription", venueDescription);
	}

	public void setLatitude(float latitude) {
		params.put("latitude", latitude);
	}

	public void setLongitude(float longitude) {
		params.put("longitude", longitude);
	}

	public void setSilent(boolean silent) {
		params.put("silent", silent);
	}

	public void setPlatform(String platform) {
		params.put("platform", platform);
	}

	public void setInstallUrl(String installurl) {
		params.put("installurl", installurl);
	}

	public void setExecuteUrl(String executeurl) {
		params.put("executeurl", executeurl);
	}

	public void setMusicUrl(String musicUrl) {
		params.put("musicUrl", musicUrl);
	}

	public void setCallback(PlatformActionListener callback) {
		params.put("callback", callback);
	}

	public PlatformActionListener getCallback() {
		return R.forceCast(params.get("callback"));
	}

	public void setShareContentCustomizeCallback(
			ShareContentCustomizeCallback callback) {
		params.put("customizeCallback", callback);
	}

	public ShareContentCustomizeCallback getShareContentCustomizeCallback() {
		return R.forceCast(params.get("customizeCallback"));
	}

	public void setCustomerLogo(Bitmap logo, String label, OnClickListener ocl) {
		CustomerLogo cl = new CustomerLogo();
		cl.logo = logo;
		cl.label = label;
		cl.listener = ocl;
		ArrayList<CustomerLogo> customers = R
				.forceCast(params.get("customers"));
		customers.add(cl);
	}

	public void disableSSOWhenAuthorize() {
		params.put("disableSSO", true);
	}

	public void setVideoUrl(String url) {
		params.put("url", url);
		params.put("shareType", Platform.SHARE_VIDEO);
	}

	@Deprecated
	public void setDialogMode() {
		params.put("dialogMode", true);
	}

	public void addHiddenPlatform(String platform) {
		HashMap<String, String> hiddenPlatforms = R.forceCast(params
				.get("hiddenPlatforms"));
		hiddenPlatforms.put(platform, platform);
	}

	public void setViewToShare(View viewToShare) {
		try {
			Bitmap bm = captureView(viewToShare, viewToShare.getWidth(),
					viewToShare.getHeight());
			params.put("viewToShare", bm);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	public void setImageArray(String[] imageArray) {
		params.put("imageArray", imageArray);
	}

	public void setShareToTencentWeiboWhenPerformingQQOrQZoneSharing() {
		params.put("isShareTencentWeibo", true);
	}

	public void setTheme(OnekeyShareTheme theme) {
		params.put("theme", theme.getValue());
	}

	@SuppressWarnings("unchecked")
	public void show(Context context) {
		HashMap<String, Object> shareParamsMap = new HashMap<String, Object>();
		shareParamsMap.putAll(params);

		ShareSDK.initSDK(context);

		ShareSDK.logDemoEvent(1, null);

		int iTheme = 0;
		try {
			iTheme = R.parseInt(String.valueOf(shareParamsMap.remove("theme")));
		} catch (Throwable t) {
		}
		OnekeyShareTheme theme = OnekeyShareTheme.fromValue(iTheme);
		OnekeyShareThemeImpl themeImpl = theme.getImpl();

		themeImpl.setShareParamsMap(shareParamsMap);
		themeImpl
				.setDialogMode(shareParamsMap.containsKey("dialogMode") ? ((Boolean) shareParamsMap
						.remove("dialogMode")) : false);
		themeImpl
				.setSilent(shareParamsMap.containsKey("silent") ? ((Boolean) shareParamsMap
						.remove("silent")) : false);
		themeImpl.setCustomerLogos((ArrayList<CustomerLogo>) shareParamsMap
				.remove("customers"));
		themeImpl.setHiddenPlatforms((HashMap<String, String>) shareParamsMap
				.remove("hiddenPlatforms"));
		themeImpl
				.setPlatformActionListener((PlatformActionListener) shareParamsMap
						.remove("callback"));
		themeImpl
				.setShareContentCustomizeCallback((ShareContentCustomizeCallback) shareParamsMap
						.remove("customizeCallback"));
		if (shareParamsMap.containsKey("disableSSO") ? ((Boolean) shareParamsMap
				.remove("disableSSO")) : false) {
			themeImpl.disableSSO();
		}

		themeImpl.show(context);
	}

}

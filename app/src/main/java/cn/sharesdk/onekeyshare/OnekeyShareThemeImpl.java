/*
 * 瀹樼綉鍦扮珯:http://www.mob.com
 * 鎶�鏈敮鎸丵Q: 4006852216
 * 瀹樻柟寰俊:ShareSDK   锛堝鏋滃彂甯冩柊鐗堟湰鐨勮瘽锛屾垜浠皢浼氱涓�鏃堕棿閫氳繃寰俊灏嗙増鏈洿鏂板唴瀹规帹閫佺粰鎮ㄣ�傚鏋滀娇鐢ㄨ繃绋嬩腑鏈変换浣曢棶棰橈紝涔熷彲浠ラ�氳繃寰俊涓庢垜浠彇寰楄仈绯伙紝鎴戜滑灏嗕細鍦�24灏忔椂鍐呯粰浜堝洖澶嶏級
 *
 * Copyright (c) 2013骞� mob.com. All rights reserved.
 */

package cn.sharesdk.onekeyshare;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.os.Handler.Callback;
import android.os.Message;
import android.text.TextUtils;
import android.widget.Toast;
import cn.sharesdk.framework.CustomPlatform;
import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.Platform.ShareParams;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.ShareSDK;

import com.mob.tools.utils.R;
import com.mob.tools.utils.UIHandler;

public abstract class OnekeyShareThemeImpl implements PlatformActionListener, Callback {
	protected boolean dialogMode;
	protected HashMap<String, Object> shareParamsMap;
	protected boolean silent;
	protected ArrayList<CustomerLogo> customerLogos;
	protected HashMap<String, String> hiddenPlatforms;
	protected PlatformActionListener callback;
	protected ShareContentCustomizeCallback customizeCallback;
	protected boolean disableSSO;
	protected Context context;

	public OnekeyShareThemeImpl() {
		callback = this;
	}

	public final void setDialogMode(boolean dialogMode) {
		this.dialogMode = dialogMode;
	}

	public final void setShareParamsMap(HashMap<String, Object> shareParamsMap) {
		this.shareParamsMap = shareParamsMap;
	}

	public final void setSilent(boolean silent) {
		this.silent = silent;
	}

	public final void setCustomerLogos(ArrayList<CustomerLogo> customerLogos) {
		this.customerLogos = customerLogos;
	}

	public final void setHiddenPlatforms(HashMap<String, String> hiddenPlatforms) {
		this.hiddenPlatforms = hiddenPlatforms;
	}

	public final void setPlatformActionListener(PlatformActionListener callback) {
		this.callback = callback == null ? this : callback;
	}

	public final void setShareContentCustomizeCallback(ShareContentCustomizeCallback customizeCallback) {
		this.customizeCallback = customizeCallback;
	}

	public final void disableSSO() {
		disableSSO = true;
	}

	public final void show(Context context) {
		this.context = context;

		if (shareParamsMap.containsKey("platform")) {
			String name = String.valueOf(shareParamsMap.get("platform"));
			Platform platform = ShareSDK.getPlatform(name);
			boolean isCustomPlatform = platform instanceof CustomPlatform;
			boolean isUseClientToShare = isUseClientToShare(platform);
			if (silent || isCustomPlatform || isUseClientToShare) {
				shareSilently(platform);
			} else {
				prepareForEditPage(platform);
			}
		} else {
			showPlatformPage(context);
		}
	}

	final boolean isUseClientToShare(Platform platform) {
		String name = platform.getName();
		if ("Wechat".equals(name) || "WechatMoments".equals(name)
				|| "WechatFavorite".equals(name) || "ShortMessage".equals(name)
				|| "Email".equals(name) || "GooglePlus".equals(name)
				|| "QQ".equals(name) || "Pinterest".equals(name)
				|| "Instagram".equals(name) || "Yixin".equals(name)
				|| "YixinMoments".equals(name) || "QZone".equals(name)
				|| "Mingdao".equals(name) || "Line".equals(name)
				|| "KakaoStory".equals(name) || "KakaoTalk".equals(name)
				|| "Bluetooth".equals(name) || "WhatsApp".equals(name)
				|| "BaiduTieba".equals(name) || "Laiwang".equals(name)
				|| "LaiwangMoments".equals(name) || "Alipay".equals(name)
				|| "FacebookMessenger".equals(name)
				) {
			return true;
		} else if ("Evernote".equals(name)) {
			if ("true".equals(platform.getDevinfo("ShareByAppClient"))) {
				return true;
			}
		} else if ("SinaWeibo".equals(name)) {
			if ("true".equals(platform.getDevinfo("ShareByAppClient"))) {

				Intent test = new Intent(Intent.ACTION_SEND);
				test.setPackage("com.sina.weibo");
				test.setType("image/*");
				ResolveInfo ri = platform.getContext().getPackageManager().resolveActivity(test, 0);
				if(ri == null) {
					test = new Intent(Intent.ACTION_SEND);
					test.setPackage("com.sina.weibog3");
					test.setType("image/*");
					ri = platform.getContext().getPackageManager().resolveActivity(test, 0);
				}
				return (ri != null);
			}
		}

		return false;
	}

	final void shareSilently(Platform platform) {
		if (formateShareData(platform)) {
			ShareParams sp = shareDataToShareParams(platform);
			if (sp != null) {
				toast("ssdk_oks_sharing");
				if (customizeCallback != null) {
					customizeCallback.onShare(platform, sp);
				}
				if (disableSSO) {
					platform.SSOSetting(disableSSO);
				}
				platform.setPlatformActionListener(callback);
				platform.share(sp);
			}
		}
	}

	private void prepareForEditPage(Platform platform) {
		if (formateShareData(platform)) {
			ShareParams sp = shareDataToShareParams(platform);
			if (sp != null) {
				ShareSDK.logDemoEvent(3, null);
				if (customizeCallback != null) {
					customizeCallback.onShare(platform, sp);
				}
				showEditPage(context, platform, sp);
			}
		}
	}

	final boolean formateShareData(Platform plat) {
		String name = plat.getName();

		boolean isAlipay = "Alipay".equals(name);
		if (isAlipay && !plat.isClientValid()) {
			toast("ssdk_alipay_client_inavailable");
			return false;
		}

		boolean isKakaoTalk = "KakaoTalk".equals(name);
		if (isKakaoTalk && !plat.isClientValid()) {
			toast("ssdk_kakaotalk_client_inavailable");
			return false;
		}

		boolean isKakaoStory = "KakaoStory".equals(name);
		if (isKakaoStory && !plat.isClientValid()) {
			toast("ssdk_kakaostory_client_inavailable");
			return false;
		}

		boolean isLine = "Line".equals(name);
		if (isLine && !plat.isClientValid()) {
			toast("ssdk_line_client_inavailable");
			return false;
		}

		boolean isWhatsApp = "WhatsApp".equals(name);
		if (isWhatsApp && !plat.isClientValid()) {
			toast("ssdk_whatsapp_client_inavailable");
			return false;
		}

		boolean isPinterest = "Pinterest".equals(name);
		if (isPinterest && !plat.isClientValid()) {
			toast("ssdk_pinterest_client_inavailable");
			return false;
		}

		if ("Instagram".equals(name) && !plat.isClientValid()) {
			toast("ssdk_instagram_client_inavailable");
			return false;
		}

		boolean isLaiwang = "Laiwang".equals(name);
		boolean isLaiwangMoments = "LaiwangMoments".equals(name);
		if(isLaiwang || isLaiwangMoments){
			if (!plat.isClientValid()) {
				toast("ssdk_laiwang_client_inavailable");
				return false;
			}
		}

		boolean isYixin = "YixinMoments".equals(name) || "Yixin".equals(name);
		if (isYixin && !plat.isClientValid()) {
			toast("ssdk_yixin_client_inavailable");
			return false;
		}

		boolean isWechat = "WechatFavorite".equals(name) || "Wechat".equals(name) || "WechatMoments".equals(name);
		if (isWechat && !plat.isClientValid()) {
			toast("ssdk_wechat_client_inavailable");
			return false;
		}

		if ("FacebookMessenger".equals(name) && !plat.isClientValid()) {
			toast("ssdk_facebookmessenger_client_inavailable");
			return false;
		}

		if (!shareParamsMap.containsKey("shareType")) {
			int shareType = Platform.SHARE_TEXT;
			String imagePath = String.valueOf(shareParamsMap.get("imagePath"));
			if (imagePath != null && (new File(imagePath)).exists()) {
				shareType = Platform.SHARE_IMAGE;
				if (imagePath.endsWith(".gif") && isWechat) {
					shareType = Platform.SHARE_EMOJI;
				} else if (shareParamsMap.containsKey("url") && !TextUtils.isEmpty(shareParamsMap.get("url").toString())) {
					shareType = Platform.SHARE_WEBPAGE;
					if (shareParamsMap.containsKey("musicUrl") && !TextUtils.isEmpty(shareParamsMap.get("musicUrl").toString()) && isWechat) {
						shareType = Platform.SHARE_MUSIC;
					}
				}
			} else {
				Bitmap viewToShare = R.forceCast(shareParamsMap.get("viewToShare"));
				if (viewToShare != null && !viewToShare.isRecycled()) {
					shareType = Platform.SHARE_IMAGE;
					if (shareParamsMap.containsKey("url") && !TextUtils.isEmpty(shareParamsMap.get("url").toString())) {
						shareType = Platform.SHARE_WEBPAGE;
						if (shareParamsMap.containsKey("musicUrl") && !TextUtils.isEmpty(shareParamsMap.get("musicUrl").toString()) && isWechat) {
							shareType = Platform.SHARE_MUSIC;
						}
					}
				} else {
					Object imageUrl = shareParamsMap.get("imageUrl");
					if (imageUrl != null && !TextUtils.isEmpty(String.valueOf(imageUrl))) {
						shareType = Platform.SHARE_IMAGE;
						if (String.valueOf(imageUrl).endsWith(".gif") && isWechat) {
							shareType = Platform.SHARE_EMOJI;
						} else if (shareParamsMap.containsKey("url") && !TextUtils.isEmpty(shareParamsMap.get("url").toString())) {
							shareType = Platform.SHARE_WEBPAGE;
							if (shareParamsMap.containsKey("musicUrl") && !TextUtils.isEmpty(shareParamsMap.get("musicUrl").toString()) && isWechat) {
								shareType = Platform.SHARE_MUSIC;
							}
						}
					}
				}
			}
			shareParamsMap.put("shareType", shareType);
		}

		return true;
	}

	final ShareParams shareDataToShareParams(Platform plat) {
		if (plat == null || shareParamsMap == null) {
			toast("ssdk_oks_share_failed");
			return null;
		}

		try {
			String imagePath = R.forceCast(shareParamsMap.get("imagePath"));
			Bitmap viewToShare = R.forceCast(shareParamsMap.get("viewToShare"));
			if (TextUtils.isEmpty(imagePath) && viewToShare != null && !viewToShare.isRecycled()) {
				String path = R.getCachePath(plat.getContext(), "screenshot");
				File ss = new File(path, String.valueOf(System.currentTimeMillis()) + ".jpg");
				FileOutputStream fos = new FileOutputStream(ss);
				viewToShare.compress(CompressFormat.JPEG, 100, fos);
				fos.flush();
				fos.close();
				shareParamsMap.put("imagePath", ss.getAbsolutePath());
			}
		} catch (Throwable t) {
			t.printStackTrace();
			toast("ssdk_oks_share_failed");
			return null;
		}

		return new ShareParams(shareParamsMap);
	}

	private void toast(final String resOrName) {
		UIHandler.sendEmptyMessage(0, new Callback() {
			public boolean handleMessage(Message msg) {
				int resId = R.getStringRes(context, resOrName);
				if (resId > 0) {
					Toast.makeText(context, resId, Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(context, resOrName, Toast.LENGTH_SHORT).show();
				}
				return false;
			}
		});
	}

	protected abstract void showPlatformPage(Context context);

	protected abstract void showEditPage(Context context, Platform platform, ShareParams sp);

	public final void onComplete(Platform platform, int action,
			HashMap<String, Object> res) {
		Message msg = new Message();
		msg.arg1 = 1;
		msg.arg2 = action;
		msg.obj = platform;
		UIHandler.sendMessage(msg, this);
	}

	public final void onError(Platform platform, int action, Throwable t) {
		t.printStackTrace();

		Message msg = new Message();
		msg.arg1 = 2;
		msg.arg2 = action;
		msg.obj = t;
		UIHandler.sendMessage(msg, this);

		ShareSDK.logDemoEvent(4, platform);
	}

	public final void onCancel(Platform platform, int action) {
		Message msg = new Message();
		msg.arg1 = 3;
		msg.arg2 = action;
		msg.obj = platform;
		UIHandler.sendMessage(msg, this);

		ShareSDK.logDemoEvent(5, platform);
	}

	public final boolean handleMessage(Message msg) {
		switch (msg.arg1) {
			case 1: {
				int resId = R.getStringRes(context, "ssdk_oks_share_completed");
				if (resId > 0) {
					toast(context.getString(resId));
				}
			} break;
			case 2: {
				String expName = msg.obj.getClass().getSimpleName();
				if ("WechatClientNotExistException".equals(expName)
						|| "WechatTimelineNotSupportedException".equals(expName)
						|| "WechatFavoriteNotSupportedException".equals(expName)) {
					toast("ssdk_wechat_client_inavailable");
				} else if ("GooglePlusClientNotExistException".equals(expName)) {
					toast("ssdk_google_plus_client_inavailable");
				} else if ("QQClientNotExistException".equals(expName)) {
					toast("ssdk_qq_client_inavailable");
				} else if ("YixinClientNotExistException".equals(expName)
						|| "YixinTimelineNotSupportedException".equals(expName)) {
					toast("ssdk_yixin_client_inavailable");
				} else if ("KakaoTalkClientNotExistException".equals(expName)) {
					toast("ssdk_kakaotalk_client_inavailable");
				} else if ("KakaoStoryClientNotExistException".equals(expName)) {
					toast("ssdk_kakaostory_client_inavailable");
				} else if("WhatsAppClientNotExistException".equals(expName)){
					toast("ssdk_whatsapp_client_inavailable");
				} else if("FacebookMessengerClientNotExistException".equals(expName)){
					toast("ssdk_facebookmessenger_client_inavailable");
				} else {
					toast("ssdk_oks_share_failed");
				}
			} break;
			case 3: {
				toast("ssdk_oks_share_canceled");
			} break;
		}
		return false;
	}

}

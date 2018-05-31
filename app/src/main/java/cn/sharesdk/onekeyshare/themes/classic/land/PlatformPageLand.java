
package cn.sharesdk.onekeyshare.themes.classic.land;

import java.util.ArrayList;

import cn.sharesdk.onekeyshare.OnekeyShareThemeImpl;
import cn.sharesdk.onekeyshare.themes.classic.PlatformPage;
import cn.sharesdk.onekeyshare.themes.classic.PlatformPageAdapter;

public class PlatformPageLand extends PlatformPage {

	public PlatformPageLand(OnekeyShareThemeImpl impl) {
		super(impl);
	}

	public void onCreate() {
		requestLandscapeOrientation();
		super.onCreate();
	}

	protected PlatformPageAdapter newAdapter(ArrayList<Object> cells) {
		return new PlatformPageAdapterLand(this, cells);
	}

}


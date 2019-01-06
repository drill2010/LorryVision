package name.marinchenko.lorryvision.util.net;

import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

/**
 * NetView contains data for viewing in the networks list.
 */

public class NetView {

    public static String BUNDLE_KEY_LIST_SSID = "bundle_key_list_ssid";
    public static String BUNDLE_KEY_LIST_TYPE = "bundle_key_list_type";
    public static String BUNDLE_KEY_LIST_WAS_DETACHED = "bundle_key_list_was_detached";
    public static String BUNDLE_KEY_LIST_HIGHLIGHTED = "bundle_key_list_highlighted";
    public static String BUNDLE_KEY_LIST_LEVEL = "bundle_key_list_level";

    private final String ssid;
    private final NetType type;
    private final boolean wasDetached;
    private final boolean highlighted;
    private final int level;


    public NetView(final String ssid,
                   final boolean type,
                   final boolean wasDetached,
                   final boolean highlighted,
                   final int level) {
        this.ssid = ssid;
        this.type = type ? NetType.lorryNetwork : NetType.wifiNetwork;
        this.wasDetached = wasDetached;
        this.highlighted = highlighted;
        this.level = level;
    }

    public String getSsid() { return this.ssid; }
    public NetType getType() { return this.type; }
    public boolean wasDetached() { return this.wasDetached; }
    public boolean getHighlighted() { return this.highlighted; }

    public int getSignalIcon() {
        if (this.level > -56) return 4;
        else if (this.level > -67) return 3;
        else if (this.level > -78) return 2;
        else if (this.level > -89) return 1;
        else return 0;
    }


    public static List<NetView> getNetViewList(final Bundle bundle) {
        final ArrayList<NetView> netViews = new ArrayList<>();

        final ArrayList<String> ssidList = bundle.getStringArrayList(BUNDLE_KEY_LIST_SSID);
        final boolean typeArray[] = bundle.getBooleanArray(BUNDLE_KEY_LIST_TYPE);
        final boolean wasDetachedArray[] = bundle.getBooleanArray(BUNDLE_KEY_LIST_WAS_DETACHED);
        final boolean highlightedArray[] = bundle.getBooleanArray(BUNDLE_KEY_LIST_HIGHLIGHTED);
        final int levelArray[] = bundle.getIntArray(BUNDLE_KEY_LIST_LEVEL);

        if (ssidList != null && typeArray != null && wasDetachedArray != null
                && highlightedArray != null && levelArray != null) {
            for (int i = 0; i < ssidList.size(); i++) {
                netViews.add(new NetView(
                        ssidList.get(i),
                        typeArray[i],
                        wasDetachedArray[i],
                        highlightedArray[i],
                        levelArray[i]
                ));
            }
        }

        return netViews;
    }

    public static Bundle getBundle(final List<Net> nets) {
        final Bundle bundle = new Bundle();
        bundle.putStringArrayList(BUNDLE_KEY_LIST_SSID, getSsidList(nets));
        bundle.putBooleanArray(BUNDLE_KEY_LIST_TYPE, getTypeArray(nets));
        bundle.putBooleanArray(BUNDLE_KEY_LIST_WAS_DETACHED, getWasDetachedArray(nets));
        bundle.putBooleanArray(BUNDLE_KEY_LIST_HIGHLIGHTED, getHighlightedArray(nets));
        bundle.putIntArray(BUNDLE_KEY_LIST_LEVEL, getLevelArray(nets));

        return bundle;
    }

    public static ArrayList<String> getSsidList(final List<Net> nets) {
        final ArrayList<String> ssidList = new ArrayList<>();
        for (Net net : nets) {
            ssidList.add(net.getSsid());
        }
        return ssidList;
    }

    public static boolean[] getTypeArray(final List<Net> nets) {
        final boolean typeArray[] = new boolean[nets.size()];
        for (int i = 0; i < typeArray.length; i++) {
            typeArray[i] = nets.get(i).getType() == NetType.lorryNetwork;
        }
        return typeArray;
    }

    public static boolean[] getWasDetachedArray(final List<Net> nets) {
        final boolean wasDetachedArray[] = new boolean[nets.size()];
        for (int i = 0; i < wasDetachedArray.length; i++) {
            wasDetachedArray[i] = nets.get(i).wasDetached();
        }
        return wasDetachedArray;
    }

    public static boolean[] getHighlightedArray(final List<Net> nets) {
        final boolean higlightedArray[] = new boolean[nets.size()];
        for (int i = 0; i < higlightedArray.length; i++) {
            higlightedArray[i] = nets.get(i).getHighlighted();
        }
        return higlightedArray;
    }

    public static int[] getLevelArray(final List<Net> nets) {
        final int levelArray[] = new int[nets.size()];
        for (int i = 0; i < levelArray.length; i++) {
            levelArray[i] = nets.get(i).getLevel();
        }
        return levelArray;
    }
}

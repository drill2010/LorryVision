package name.marinchenko.lorryvision.util.debug;

import java.util.ArrayList;

import name.marinchenko.lorryvision.util.net.Net;

/**
 * UI demonstration container.
 */

public class TestBase {

    /**
     * ListView items demonstration.
     * @return items
     */
    public static ArrayList<Net> getNetlistForListViewTest() {
        final ArrayList<Net> list = new ArrayList<>();

        final String strs[] = new String[]{
                "LorryVision network 1", "LorryVision network 2",
                "Usual network 1", "Usual network 2", "Usual network 3",
                "Usual network 4", "Usual network 5", "Scrolling demo network",
                "Network with too long name"};

        for (int i = 0; i < strs.length; i++) {
            list.add(new Net(
                    strs[i],
                    "",
                    "",
                    "",
                    Math.abs(i - 4)
            ));
        }

        return list;
    }
}

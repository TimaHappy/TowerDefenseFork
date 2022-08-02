package castle.components;

import arc.struct.StringMap;
import arc.util.Http;

public class CastleIcons {

    private static final StringMap icons = new StringMap();

    public static void load() {
        Http.get("https://raw.githubusercontent.com/Anuken/Mindustry/v136/core/assets/icons/icons.properties").submit(response -> {
            String[] lines = response.getResultAsString().split("\n");
            for (String line : lines) {
                String[] values = line.split("\\|")[0].split("=");

                String name = values[1];
                String icon = String.valueOf((char) Integer.parseInt(values[0]));

                icons.put(name, icon);
            }
        });
    }

    public static String get(String key) {
        return icons.get(key);
    }
}

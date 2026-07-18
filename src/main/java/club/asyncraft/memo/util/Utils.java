package club.asyncraft.memo.util;

import club.asyncraft.memo.Memo;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.ResourceBundle;

public class Utils {

    public static TextComponent getTextComponent(@NotNull String key) {
        return getTextComponent(key, Memo.instance.getConfig().getLocale());
    }

    public static String getTextComponentContent(@NotNull String key) {
        return getTextComponent(key).content();
    }

    public static TextComponent getTextComponent(@NotNull String key, Locale locale) {
        ResourceBundle bundle = ResourceBundle.getBundle("club.asyncraft.memo.Bundle", locale);
        return Component.text(bundle.getString(key));
    }

    public static String getTextComponentContent(@NotNull String key, Locale locale) {
        return getTextComponent(key, locale).content();
    }

}

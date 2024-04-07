package club.asyncraft.memo.util;

import club.asyncraft.memo.Memo;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.translation.GlobalTranslator;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public class Utils {

    public static TextComponent getTextComponent(@NotNull String key) {
        return getTextComponent(key, Memo.instance.getConfig().getLocale());
    }

    public static String getTextComponentContent(@NotNull String key) {
        return getTextComponent(key).content();
    }

    public static TextComponent getTextComponent(@NotNull String key, Locale locale) {
        return (TextComponent) GlobalTranslator.render(Component.translatable(key), locale);
    }

    public static String getTextComponentContent(@NotNull String key, Locale locale) {
        return getTextComponent(key, locale).content();
    }

}

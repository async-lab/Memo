package club.asyncraft.memo.util;

import club.asyncraft.memo.Memo;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.translation.GlobalTranslator;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public class Utils {

    public static TextComponent getTextComponent(@NotNull String key) {
        return (TextComponent) GlobalTranslator.render(Component.translatable(key), Memo.instance.getConfig().getLocale());
    }

    public static TextComponent getTextComponent(@NotNull String key, Locale locale) {
        return (TextComponent) GlobalTranslator.render(Component.translatable(key), locale);
    }

}

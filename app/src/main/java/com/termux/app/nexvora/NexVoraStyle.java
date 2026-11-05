package com.termux.app.nexvora;

import android.content.Context;
import android.graphics.Typeface;
import android.net.Uri;

import androidx.annotation.NonNull;

import com.termux.shared.termux.TermuxConstants;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Writes the standard Termux style files (~/.termux/colors.properties, ~/.termux/font.ttf) and the
 * login greeting ($PREFIX/etc/motd). TermuxActivity re-reads them on its next onStart() when the
 * dirty flag is set; the underlying Termux code that reads them is unchanged.
 */
public final class NexVoraStyle {

    private static volatile boolean sStyleDirty = false;

    private NexVoraStyle() {}

    public static void markStyleDirty() { sStyleDirty = true; }

    /** Returns true once after a style change; used by TermuxActivity.onStart(). */
    public static boolean consumeStyleDirty() {
        boolean d = sStyleDirty;
        sStyleDirty = false;
        return d;
    }

    // ---------------------------------------------------------------- color themes

    public static final class ColorTheme {
        public final String name;
        public final String bg, fg, cursor;
        public final String[] ansi; // 16 entries

        ColorTheme(String name, String bg, String fg, String cursor, String... ansi) {
            this.name = name; this.bg = bg; this.fg = fg; this.cursor = cursor; this.ansi = ansi;
        }

        String toProperties() {
            StringBuilder sb = new StringBuilder();
            sb.append("# Written by NexVora Terminal (").append(name).append(")\n");
            sb.append("background=").append(bg).append('\n');
            sb.append("foreground=").append(fg).append('\n');
            sb.append("cursor=").append(cursor).append('\n');
            for (int i = 0; i < ansi.length; i++) sb.append("color").append(i).append('=').append(ansi[i]).append('\n');
            return sb.toString();
        }
    }

    public static List<ColorTheme> themes() {
        List<ColorTheme> l = new ArrayList<>();
        l.add(new ColorTheme("NexVora Cyan (default)", "#050507", "#ECECF4", "#00E5FF",
            "#1A1A26", "#FF5C7A", "#3DDC97", "#FFD166", "#5B8CFF", "#8B5CF6", "#00E5FF", "#CFCFE0",
            "#3A3A52", "#FF8099", "#6CF0B5", "#FFE08A", "#84A9FF", "#B18CFF", "#66F0FF", "#FFFFFF"));
        l.add(new ColorTheme("NexVora Purple Night", "#07050F", "#E8E3FF", "#8B5CF6",
            "#1B1530", "#FF5C8A", "#4ADE9A", "#F7C948", "#6C8CFF", "#8B5CF6", "#22D3EE", "#D6D0F0",
            "#3B3358", "#FF85A8", "#7CF2B8", "#FFDB7A", "#93AAFF", "#B18CFF", "#67E8F9", "#FFFFFF"));
        l.add(new ColorTheme("Dracula", "#282a36", "#f8f8f2", "#f8f8f2",
            "#21222c", "#ff5555", "#50fa7b", "#f1fa8c", "#bd93f9", "#ff79c6", "#8be9fd", "#f8f8f2",
            "#6272a4", "#ff6e6e", "#69ff94", "#ffffa5", "#d6acff", "#ff92df", "#a4ffff", "#ffffff"));
        l.add(new ColorTheme("Nord", "#2e3440", "#d8dee9", "#d8dee9",
            "#3b4252", "#bf616a", "#a3be8c", "#ebcb8b", "#81a1c1", "#b48ead", "#88c0d0", "#e5e9f0",
            "#4c566a", "#bf616a", "#a3be8c", "#ebcb8b", "#81a1c1", "#b48ead", "#8fbcbb", "#eceff4"));
        l.add(new ColorTheme("Monokai", "#272822", "#f8f8f2", "#f8f8f0",
            "#272822", "#f92672", "#a6e22e", "#f4bf75", "#66d9ef", "#ae81ff", "#a1efe4", "#f8f8f2",
            "#75715e", "#f92672", "#a6e22e", "#f4bf75", "#66d9ef", "#ae81ff", "#a1efe4", "#f9f8f5"));
        l.add(new ColorTheme("Solarized Dark", "#002b36", "#839496", "#93a1a1",
            "#073642", "#dc322f", "#859900", "#b58900", "#268bd2", "#d33682", "#2aa198", "#eee8d5",
            "#002b36", "#cb4b16", "#586e75", "#657b83", "#839496", "#6c71c4", "#93a1a1", "#fdf6e3"));
        l.add(new ColorTheme("Classic Green", "#000000", "#33ff66", "#33ff66",
            "#000000", "#cd0000", "#00cd00", "#cdcd00", "#3465a4", "#cd00cd", "#00cdcd", "#e5e5e5",
            "#7f7f7f", "#ff0000", "#00ff00", "#ffff00", "#5c5cff", "#ff00ff", "#00ffff", "#ffffff"));
        return l;
    }

    private static void ensureDataHome() throws IOException {
        File dir = TermuxConstants.TERMUX_DATA_HOME_DIR;
        if (!dir.isDirectory() && !dir.mkdirs())
            throw new IOException("Cannot create " + dir.getAbsolutePath() + " (is the bootstrap installed?)");
    }

    private static void writeText(@NonNull File f, @NonNull String s) throws IOException {
        try (OutputStream out = new FileOutputStream(f)) {
            out.write(s.getBytes(StandardCharsets.UTF_8));
        }
    }

    public static void applyTheme(@NonNull ColorTheme t) throws IOException {
        ensureDataHome();
        writeText(TermuxConstants.TERMUX_COLOR_PROPERTIES_FILE, t.toProperties());
        markStyleDirty();
    }

    public static void resetTheme() {
        //noinspection ResultOfMethodCallIgnored
        TermuxConstants.TERMUX_COLOR_PROPERTIES_FILE.delete();
        markStyleDirty();
    }

    // ---------------------------------------------------------------- font

    /** Copies a user-picked .ttf into ~/.termux/font.ttf after checking Android can load it. */
    public static void applyFont(@NonNull Context context, @NonNull Uri uri) throws IOException {
        ensureDataHome();
        File tmp = new File(TermuxConstants.TERMUX_DATA_HOME_DIR, "font.ttf.nexvora-tmp");
        try (InputStream in = context.getContentResolver().openInputStream(uri);
             OutputStream out = new FileOutputStream(tmp)) {
            if (in == null) throw new IOException("Cannot open the selected file");
            byte[] buf = new byte[8192];
            int n;
            while ((n = in.read(buf)) > 0) out.write(buf, 0, n);
        }
        try {
            Typeface.createFromFile(tmp); // throws RuntimeException on invalid fonts
        } catch (RuntimeException e) {
            //noinspection ResultOfMethodCallIgnored
            tmp.delete();
            throw new IOException("That file is not a usable TTF/OTF font");
        }
        File dest = TermuxConstants.TERMUX_FONT_FILE;
        //noinspection ResultOfMethodCallIgnored
        dest.delete();
        if (!tmp.renameTo(dest)) throw new IOException("Cannot install font");
        markStyleDirty();
    }

    public static void resetFont() {
        //noinspection ResultOfMethodCallIgnored
        TermuxConstants.TERMUX_FONT_FILE.delete();
        markStyleDirty();
    }

    // ---------------------------------------------------------------- greeting (motd)

    public static File greetingFile() {
        return new File(TermuxConstants.TERMUX_ETC_PREFIX_DIR, "motd");
    }

    public static String readGreeting() {
        File f = greetingFile();
        if (!f.isFile()) return "";
        try (InputStream in = new java.io.FileInputStream(f)) {
            java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
            byte[] buf = new byte[4096];
            int n;
            while ((n = in.read(buf)) > 0) bos.write(buf, 0, n);
            return new String(bos.toByteArray(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            return "";
        }
    }

    /** Writes the text shown by the Termux {@code login} script at the start of each new session. */
    public static void writeGreeting(@NonNull String text) throws IOException {
        File dir = TermuxConstants.TERMUX_ETC_PREFIX_DIR;
        if (!dir.isDirectory())
            throw new IOException("Bootstrap not installed yet - open the terminal once first");
        writeText(greetingFile(), text.isEmpty() || text.endsWith("\n") ? text : text + "\n");
    }
}

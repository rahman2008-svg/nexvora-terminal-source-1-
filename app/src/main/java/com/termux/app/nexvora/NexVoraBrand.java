package com.termux.app.nexvora;

/**
 * NexVora Terminal brand and developer information. Single source for the About screen.
 *
 * NexVora Terminal is a fork of Termux (https://github.com/termux/termux-app), licensed under GPLv3.
 */
public final class NexVoraBrand {

    private NexVoraBrand() {}

    public static final String APP_NAME = "NexVora Terminal";
    public static final String BRAND = "NexVora";
    public static final String TAGLINE = "A real Linux terminal, built for developers.";

    public static final String DEVELOPER_NAME = "Prince AR Abdur Rahman";
    public static final String COMPANY = "NexVora Lab's Ofc";
    public static final String COPYRIGHT = "\u00A9 2026 NexVora Lab's Ofc. All Rights Reserved.";

    public static final String[] WHATSAPP_NUMBERS = {"01707424006", "01796951709"};
    /** wa.me needs an international number; the numbers above are assumed to be Bangladesh (+880) numbers. */
    public static final String[] WHATSAPP_INTERNATIONAL = {"8801707424006", "8801796951709"};
    public static final String FACEBOOK_URL = "https://www.facebook.com/share/1BNn32qoJo/";
    public static final String INSTAGRAM_URL = "https://www.instagram.com/ur___abdur____rahman__2008";
    public static final String EMAIL = "prince.ar.abdur.rahman2008@gmail.com";

    public static final String UPSTREAM_NAME = "Termux";
    public static final String UPSTREAM_URL = "https://github.com/termux/termux-app";
    public static final String LICENSE_NOTICE =
        "NexVora Terminal is based on Termux (\u00A9 Termux developers), which is licensed under the "
        + "GNU General Public License v3.0. The terminal emulator and view libraries are derived from "
        + "Android Terminal Emulator (Apache License 2.0). Original copyright and license notices are "
        + "preserved in the source tree (see LICENSE.md). NexVora Terminal modifications are distributed "
        + "under the same license terms.";

    public static final String DEFAULT_GREETING =
        "Welcome to NexVora Terminal\n"
        + "by Prince AR Abdur Rahman - NexVora Lab's Ofc\n";
}

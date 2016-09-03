package zhj.notetaking.utils;

import android.app.Activity;
import android.content.Context;

import zhj.notetaking.R;


public class ThemeUtils {

    public static void changeTheme(Activity activity, Theme theme){
        if (activity == null)
            return;
        int style = R.style.RedTheme;
        switch (theme){
            case DEFAULT:
                style=R.style.AppTheme;
                break;
            case BROWN:
                style = R.style.BrownTheme;
                break;
            case BLUE:
                style = R.style.BlueTheme;
                break;
            case BLUE_GREY:
                style = R.style.BlueGreyTheme;
                break;
            case YELLOW:
                style = R.style.YellowTheme;
                break;
            case DEEP_PURPLE:
                style = R.style.DeepPurpleTheme;
                break;
            case PINK:
                style = R.style.PinkTheme;
                break;
            case GREEN:
                style = R.style.GreenTheme;
                break;
            case PINKMIXYELLOW:
                style = R.style.PinkMixYellowTheme;
                break;
            case GREENMIXRED:
                style = R.style.GreenMixRed;
                break;
            case BROWNMIXGREY:
                style=R.style.BrownMixGrey;
                break;
            default:
                break;
        }
        activity.setTheme(style);
    }

    public static Theme getCurrentTheme(Context context){
        int value =  PrefUtils.getInt(context,"change_theme_key",0x00 );
        return ThemeUtils.Theme.mapValueToTheme(value);
    }

    public enum Theme{
        DEFAULT(0),
        PINKMIXYELLOW(1),
        GREENMIXRED(2),
        BROWNMIXGREY(3),
        RED(4),
        BROWN(5),
        BLUE(6),
        BLUE_GREY(7),
        YELLOW(8),
        DEEP_PURPLE(9),
        PINK(10),
        GREEN(11);

        private int mValue;

        Theme(int value){
            this.mValue = value;
        }

        public static Theme mapValueToTheme(final int value) {
            for (Theme theme : Theme.values()) {
                if (value == theme.getIntValue()) {
                    return theme;
                }
            }
            // If run here, return default
            return DEFAULT;
        }

        static Theme getDefault()
        {
            return DEFAULT;
        }
        public int getIntValue() {
            return mValue;
        }
    }
}

package ru.likeapp.doze;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import com.marianhello.logging.LoggerManager;

import org.slf4j.Logger;

/**
 * Created by Pavel on 17.02.2018.
 */
public abstract class BatteryOptimization {
    private final static Logger logger = LoggerManager.getLogger(BatteryOptimization.class);

    public static boolean ignore(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            logger.info("BatteryOptimization ignore: android version OK");
//            Intent intent = new Intent();
//            String packageName = context.getPackageName();
//            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
//            if (pm.isIgnoringBatteryOptimizations(packageName)) {
//                logger.info("BatteryOptimization ignore: setAction ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS");
//                intent.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
//                return true;
//            } else {
//                logger.info("BatteryOptimization ignore: setAction ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS");
//                intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
//                intent.setData(Uri.parse("package:" + packageName));
//            }
            String packageName = context.getPackageName();
            logger.info("BatteryOptimization ignore: getDozeState " + getDozeState(context));
            logger.info("BatteryOptimization ignore: getPowerSaveState " + getPowerSaveState(context));
            registerPowerSaveReceiver(context, new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    logger.info("BatteryOptimization ignore: onReceive ACTION_DEVICE_IDLE_MODE_CHANGED");
                    logger.info("BatteryOptimization ignore: getDozeState " + getDozeState(context));
                    logger.info("BatteryOptimization ignore: getPowerSaveState " + getPowerSaveState(context));
                }
            });
            Intent intent = prepareIntentForWhiteListingOfBatteryOptimization(context, packageName, true);
            if (intent != null) {
                context.startActivity(intent);
            }
        }
        return false;
    }

    public enum PowerSaveState {
        ON, OFF, ERROR_GETTING_STATE, IRRELEVANT_OLD_ANDROID_API
    }

    public enum WhiteListedInBatteryOptimizations {
        WHITE_LISTED, NOT_WHITE_LISTED, ERROR_GETTING_STATE, UNKNOWN_TOO_OLD_ANDROID_API_FOR_CHECKING, IRRELEVANT_OLD_ANDROID_API
    }

    public enum DozeState {
        NORMAL_INTERACTIVE, DOZE_TURNED_ON_IDLE, NORMAL_NON_INTERACTIVE, ERROR_GETTING_STATE, IRRELEVANT_OLD_ANDROID_API, UNKNOWN_TOO_OLD_ANDROID_API_FOR_CHECKING
    }

    public static DozeState getDozeState(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
            return DozeState.IRRELEVANT_OLD_ANDROID_API;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return DozeState.UNKNOWN_TOO_OLD_ANDROID_API_FOR_CHECKING;
        }
        final PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        if (pm == null)
            return DozeState.ERROR_GETTING_STATE;
        return pm.isDeviceIdleMode() ? DozeState.DOZE_TURNED_ON_IDLE : pm.isInteractive() ? DozeState.NORMAL_INTERACTIVE : DozeState.NORMAL_NON_INTERACTIVE;
    }

    public static PowerSaveState getPowerSaveState(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
            return PowerSaveState.IRRELEVANT_OLD_ANDROID_API;
        final PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        if (pm == null)
            return PowerSaveState.ERROR_GETTING_STATE;
        return pm.isPowerSaveMode() ? PowerSaveState.ON : PowerSaveState.OFF;
    }

    private static WhiteListedInBatteryOptimizations getIfAppIsWhiteListedFromBatteryOptimizations(Context context, String packageName) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
            return WhiteListedInBatteryOptimizations.IRRELEVANT_OLD_ANDROID_API;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            return WhiteListedInBatteryOptimizations.UNKNOWN_TOO_OLD_ANDROID_API_FOR_CHECKING;
        final PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        if (pm == null)
            return WhiteListedInBatteryOptimizations.ERROR_GETTING_STATE;
        return pm.isIgnoringBatteryOptimizations(packageName) ? WhiteListedInBatteryOptimizations.WHITE_LISTED : WhiteListedInBatteryOptimizations.NOT_WHITE_LISTED;
    }

    @TargetApi(Build.VERSION_CODES.M)
    public static Intent prepareIntentForWhiteListingOfBatteryOptimization(Context context, String packageName, boolean alsoWhenWhiteListed) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
            return null;
        final WhiteListedInBatteryOptimizations appIsWhiteListedFromPowerSave = getIfAppIsWhiteListedFromBatteryOptimizations(context, packageName);
        logger.info("BatteryOptimization ignore: getIfAppIsWhiteListedFromBatteryOptimizations " + appIsWhiteListedFromPowerSave);
        Intent intent = null;
        switch (appIsWhiteListedFromPowerSave) {
            case WHITE_LISTED:
                if (alsoWhenWhiteListed)
                    intent = new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                break;
            case NOT_WHITE_LISTED:
                intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).setData(Uri.parse("package:" + packageName));
                break;
            case ERROR_GETTING_STATE:
            case UNKNOWN_TOO_OLD_ANDROID_API_FOR_CHECKING:
            case IRRELEVANT_OLD_ANDROID_API:
            default:
                break;
        }
        return intent;
    }

    /**
     * registers a receiver to listen to power-save events. returns true if succeeded to register the broadcastReceiver.
     */
    @TargetApi(Build.VERSION_CODES.M)
    public static boolean registerPowerSaveReceiver(Context context, BroadcastReceiver receiver) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            return false;
        IntentFilter filter = new IntentFilter();
        filter.addAction(PowerManager.ACTION_DEVICE_IDLE_MODE_CHANGED);
        context.registerReceiver(receiver, filter);
        return true;
    }
}

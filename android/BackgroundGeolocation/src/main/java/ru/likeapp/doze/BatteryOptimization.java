package ru.likeapp.doze;

import android.content.Context;
import android.content.Intent;
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

    public static void ignore(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            logger.info("BatteryOptimization ignore: android version OK");
            try {
                String packageName = context.getPackageName();
                PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
                if (pm.isIgnoringBatteryOptimizations(packageName)) {
                    logger.info("BatteryOptimization ignore: isIgnoringBatteryOptimizations");
                } else {
                    logger.info("BatteryOptimization ignore: setAction ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS");
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                    intent.setData(Uri.parse("package:" + packageName));
                    context.startActivity(intent);
                }
            } catch (Exception e) {
                logger.error("BatteryOptimization ignore failed: {}", e.getMessage());
            }
        }
    }
}

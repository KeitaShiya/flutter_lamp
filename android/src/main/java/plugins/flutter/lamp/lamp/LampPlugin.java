package plugins.flutter.lamp.lamp;

import android.content.pm.PackageManager;

import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.PluginRegistry.Registrar;

import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import android.os.Build;
import android.os.Handler;

/**
 * LampPlugin
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class LampPlugin implements MethodCallHandler {

    @RequiresApi(api = Build.VERSION_CODES.M)
    private LampPlugin(Registrar registrar) {
        this._registrar = registrar;
        cameraManager = (CameraManager) _registrar.context().getSystemService(Context.CAMERA_SERVICE);
        cameraManager.registerTorchCallback(new CameraManager.TorchCallback() {
            @Override
            public void onTorchModeChanged(@NonNull String cameraId, boolean enabled) {
                super.onTorchModeChanged(cameraId, enabled);
                cameraID = cameraId;
            }
        }, new Handler());
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static void registerWith(Registrar registrar) {
        final MethodChannel channel = new MethodChannel(registrar.messenger(), "github.com/clovisnicolas/flutter_lamp");
        channel.setMethodCallHandler(new LampPlugin(registrar));
    }

    private Registrar _registrar;
    private CameraManager cameraManager;
    private String cameraID;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onMethodCall(MethodCall call, Result result) {
        try {
            switch(call.method){
                case "turnOn":
                    this.turn(true);
                    result.success(null);
                    break;
                case "turnOff":
                    this.turn(false);
                    result.success(null);
                    break;
                case "hasLamp":
                    result.success(this.hasLamp());
                    break;
                default:
                    result.notImplemented();
            }
        } catch (CameraAccessException e) {
            result.error(String.valueOf(e.getReason()), e.getLocalizedMessage(), e.getMessage());
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void turn(boolean on) throws CameraAccessException {
        if (!hasLamp()) {
            return;
        }
        if (cameraManager == null || cameraID == null)
            return ;
        cameraManager.setTorchMode(cameraID, on);
    }

    private boolean hasLamp() {
        return _registrar.context().getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
    }
}

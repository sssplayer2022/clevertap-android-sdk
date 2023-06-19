package com.clevertap.android.sdk;

import android.content.Context;
import com.clevertap.android.sdk.db.DBManager;
import com.clevertap.android.sdk.events.EventMediator;
import com.clevertap.android.sdk.events.EventQueueManager;
import com.clevertap.android.sdk.featureFlags.CTFeatureFlagsFactory;
import com.clevertap.android.sdk.inapp.InAppController;
import com.clevertap.android.sdk.login.LoginController;
import com.clevertap.android.sdk.network.NetworkManager;
import com.clevertap.android.sdk.pushnotification.PushProviders;
import com.clevertap.android.sdk.task.CTExecutorFactory;
import com.clevertap.android.sdk.task.MainLooperHandler;
import com.clevertap.android.sdk.task.Task;
import com.clevertap.android.sdk.validation.ValidationResultStack;
import com.clevertap.android.sdk.validation.Validator;
import com.clevertap.android.sdk.variables.CTVariables;
import com.clevertap.android.sdk.variables.Parser;
import com.clevertap.android.sdk.variables.VarCache;
import java.util.concurrent.Callable;

class CleverTapFactory {

    static CoreState getCoreState(Context context, CleverTapInstanceConfig cleverTapInstanceConfig,
            String cleverTapID) {
        CoreState coreState = new CoreState(context);

        CoreMetaData coreMetaData = new CoreMetaData();
        coreState.setCoreMetaData(coreMetaData);

        Validator validator = new Validator();

        ValidationResultStack validationResultStack = new ValidationResultStack();
        coreState.setValidationResultStack(validationResultStack);

        CTLockManager ctLockManager = new CTLockManager();
        coreState.setCTLockManager(ctLockManager);

        MainLooperHandler mainLooperHandler = new MainLooperHandler();
        coreState.setMainLooperHandler(mainLooperHandler);

        CleverTapInstanceConfig config = new CleverTapInstanceConfig(cleverTapInstanceConfig);
        coreState.setConfig(config);

        EventMediator eventMediator = new EventMediator(context, config, coreMetaData);
        coreState.setEventMediator(eventMediator);

        LocalDataStore localDataStore = new LocalDataStore(context, config);
        coreState.setLocalDataStore(localDataStore);

        DeviceInfo deviceInfo = new DeviceInfo(context, config, cleverTapID, coreMetaData);
        coreState.setDeviceInfo(deviceInfo);

        CTPreferenceCache.getInstance(context,config);

        BaseCallbackManager callbackManager = new CallbackManager(config, deviceInfo);
        coreState.setCallbackManager(callbackManager);

        SessionManager sessionManager = new SessionManager(config, coreMetaData, validator, localDataStore);
        coreState.setSessionManager(sessionManager);

        DBManager baseDatabaseManager = new DBManager(config, ctLockManager);
        coreState.setDatabaseManager(baseDatabaseManager);

        ControllerManager controllerManager = new ControllerManager(context, config,
                ctLockManager, callbackManager, deviceInfo, baseDatabaseManager);
        coreState.setControllerManager(controllerManager);

        //Get device id should be async to avoid strict mode policy.
        Task<Void> taskInitFCManager = CTExecutorFactory.executors(config).ioTask();
        taskInitFCManager.execute("initFCManager", new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                if (coreState.getDeviceInfo() != null && coreState.getDeviceInfo().getDeviceID() != null
                        && controllerManager.getInAppFCManager() == null) {
                    coreState.getConfig().getLogger()
                            .verbose(config.getAccountId() + ":async_deviceID",
                                    "Initializing InAppFC with device Id = " + coreState.getDeviceInfo().getDeviceID());
                    controllerManager
                            .setInAppFCManager(new InAppFCManager(context, config, coreState.getDeviceInfo().getDeviceID()));
                }
                return null;
            }
        });



        NetworkManager networkManager = new NetworkManager(context, config, deviceInfo, coreMetaData,
                validationResultStack, controllerManager, baseDatabaseManager,
                callbackManager, ctLockManager, validator, localDataStore);
        coreState.setNetworkManager(networkManager);

        EventQueueManager baseEventQueueManager = new EventQueueManager(baseDatabaseManager, context, config,
                eventMediator,
                sessionManager, callbackManager,
                mainLooperHandler, deviceInfo, validationResultStack,
                networkManager, coreMetaData, ctLockManager, localDataStore, controllerManager);
        coreState.setBaseEventQueueManager(baseEventQueueManager);

        AnalyticsManager analyticsManager = new AnalyticsManager(context, config, baseEventQueueManager, validator,
                validationResultStack, coreMetaData, localDataStore, deviceInfo,
                callbackManager, controllerManager, ctLockManager);
        coreState.setAnalyticsManager(analyticsManager);

        InAppController inAppController = new InAppController(context, config, mainLooperHandler,
                controllerManager, callbackManager, analyticsManager, coreMetaData, deviceInfo);
        coreState.setInAppController(inAppController);
        coreState.getControllerManager().setInAppController(inAppController);

        Task<Void> taskInitFeatureFlags = CTExecutorFactory.executors(config).ioTask();
        taskInitFeatureFlags.execute("initFeatureFlags", new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                initFeatureFlags(context, controllerManager, config, deviceInfo, callbackManager, analyticsManager);
                return null;
            }
        });



        LocationManager locationManager = new LocationManager(context, config, coreMetaData, baseEventQueueManager);
        coreState.setLocationManager(locationManager);

        PushProviders pushProviders = PushProviders
                .load(context, config, baseDatabaseManager, validationResultStack,
                        analyticsManager, controllerManager);
        coreState.setPushProviders(pushProviders);

        ActivityLifeCycleManager activityLifeCycleManager = new ActivityLifeCycleManager(context, config,
                analyticsManager, coreMetaData, sessionManager, pushProviders, callbackManager, inAppController,
                baseEventQueueManager);
        coreState.setActivityLifeCycleManager(activityLifeCycleManager);

        LoginController loginController = new LoginController(context, config, deviceInfo,
                validationResultStack, baseEventQueueManager, analyticsManager,
                coreMetaData, controllerManager, sessionManager,
                localDataStore, callbackManager, baseDatabaseManager, ctLockManager);
        coreState.setLoginController(loginController);

        VarCache varCache = new VarCache(config,context);
        coreState.setVarCache(varCache);

        CTVariables ctVariables = new CTVariables(varCache );
        coreState.setCTVariables(ctVariables);
        coreState.getControllerManager().setCtVariables(ctVariables);

        Parser parser = new Parser(ctVariables);
        coreState.setParser(parser);

        ctVariables.init();

        return coreState;
    }

    static void initFeatureFlags(Context context, ControllerManager controllerManager, CleverTapInstanceConfig config,
            DeviceInfo deviceInfo, BaseCallbackManager callbackManager, AnalyticsManager analyticsManager) {

        config.getLogger().verbose(config.getAccountId() + ":async_deviceID",
                "Initializing Feature Flags with device Id = " + deviceInfo.getDeviceID());
        if (config.isAnalyticsOnly()) {
            config.getLogger().debug(config.getAccountId(), "Feature Flag is not enabled for this instance");
        } else {
            controllerManager.setCTFeatureFlagsController(CTFeatureFlagsFactory.getInstance(context,
                    deviceInfo.getDeviceID(),
                    config, callbackManager, analyticsManager));
            config.getLogger().verbose(config.getAccountId() + ":async_deviceID", "Feature Flags initialized");
        }

    }
}
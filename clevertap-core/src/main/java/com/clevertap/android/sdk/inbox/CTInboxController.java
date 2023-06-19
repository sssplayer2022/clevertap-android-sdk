package com.clevertap.android.sdk.inbox;


import androidx.annotation.AnyThread;
import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;
import androidx.annotation.WorkerThread;
import com.clevertap.android.sdk.BaseCallbackManager;
import com.clevertap.android.sdk.CTLockManager;
import com.clevertap.android.sdk.CleverTapInstanceConfig;
import com.clevertap.android.sdk.Logger;
import com.clevertap.android.sdk.db.DBAdapter;
import com.clevertap.android.sdk.task.CTExecutorFactory;
import com.clevertap.android.sdk.task.Task;
import org.json.JSONArray;
import org.json.JSONException;
import java.util.ArrayList;
import java.util.concurrent.Callable;

/**
 * Controller class which handles Users and Messages for the Notification Inbox
 */
@RestrictTo(Scope.LIBRARY)
public class CTInboxController {

    private final DBAdapter dbAdapter;

    private ArrayList<CTMessageDAO> messages;

    private final Object messagesLock = new Object();

    private final String userId;

    private final boolean videoSupported;

    private final CTLockManager ctLockManager;

    private final BaseCallbackManager callbackManager;

    private final CleverTapInstanceConfig config;

    // always call async
    @WorkerThread
    public CTInboxController(CleverTapInstanceConfig config, String guid, DBAdapter adapter,
                             CTLockManager ctLockManager,
                             BaseCallbackManager callbackManager,
                             boolean videoSupported) {
        this.userId = guid;
        this.dbAdapter = adapter;
        this.messages = this.dbAdapter.getMessages(this.userId);
        this.videoSupported = videoSupported;
        this.ctLockManager = ctLockManager;
        this.callbackManager = callbackManager;
        this.config = config;
    }

    public int count() {
        return getMessages().size();
    }

    @AnyThread
    public void deleteInboxMessage(final CTInboxMessage message) {
        Task<Void> task = CTExecutorFactory.executors(config).postAsyncSafelyTask();
        task.execute("deleteInboxMessage", new Callable<Void>() {
            @Override
            public Void call() {
                synchronized (ctLockManager.getInboxControllerLock()) {
                    boolean update = _deleteMessageWithId(message.getMessageId());
                    if (update) {
                        callbackManager._notifyInboxMessagesDidUpdate();
                    }
                }
                return null;
            }
        });
    }

    @AnyThread
    public void deleteInboxMessagesForIDs(final ArrayList<String> messageIDs) {
        Task<Void> task = CTExecutorFactory.executors(config).postAsyncSafelyTask();
        task.execute("deleteInboxMessagesForIDs", new Callable<Void>() {
            @Override
            public Void call() {
                synchronized (ctLockManager.getInboxControllerLock()) {
                    boolean update = _deleteMessagesForIds(messageIDs);
                    if (update) {
                        callbackManager._notifyInboxMessagesDidUpdate();
                    }
                }
                return null;
            }
        });
    }

    @AnyThread
    public CTMessageDAO getMessageForId(String messageId) {
        return findMessageById(messageId);
    }

    @AnyThread
    public ArrayList<CTMessageDAO> getMessages() {
        synchronized (messagesLock) {
            trimMessages();
            return messages;
        }
    }

    @AnyThread
    public ArrayList<CTMessageDAO> getUnreadMessages() {
        ArrayList<CTMessageDAO> unread = new ArrayList<>();
        synchronized (messagesLock) {
            ArrayList<CTMessageDAO> messages = getMessages();
            for (CTMessageDAO message : messages) {
                if (message.isRead() == 0) {
                    unread.add(message);
                }
            }
        }
        return unread;
    }

    @AnyThread
    public void markReadInboxMessage(final CTInboxMessage message) {
        Task<Void> task = CTExecutorFactory.executors(config).postAsyncSafelyTask();
        task.execute("markReadInboxMessage", new Callable<Void>() {
            @Override
            public Void call() {
                synchronized (ctLockManager.getInboxControllerLock()) {
                    boolean read = _markReadForMessageWithId(message.getMessageId());
                    if (read) {
                        callbackManager._notifyInboxMessagesDidUpdate();
                    }
                }
                return null;
            }
        });
    }

    @AnyThread
    public void markReadInboxMessagesForIDs(final ArrayList<String> messageIDs) {
        Task<Void> task = CTExecutorFactory.executors(config).postAsyncSafelyTask();
        task.execute("markReadInboxMessagesForIDs", new Callable<Void>() {
            @Override
            public Void call() {
                synchronized (ctLockManager.getInboxControllerLock()) {
                    boolean read = _markReadForMessagesWithIds(messageIDs);
                    if (read) {
                        callbackManager._notifyInboxMessagesDidUpdate();
                    }
                }
                return null;
            }
        });
    }

    @AnyThread
    public int unreadCount() {
        return getUnreadMessages().size();
    }

    // always call async
    @WorkerThread
    public boolean updateMessages(final JSONArray inboxMessages) {
        Logger.v( "CTInboxController:updateMessages() called");

        boolean haveUpdates = false;
        ArrayList<CTMessageDAO> newMessages = new ArrayList<>();

        for (int i = 0; i < inboxMessages.length(); i++) {
            try {
                CTMessageDAO messageDAO = CTMessageDAO.initWithJSON(inboxMessages.getJSONObject(i), this.userId);

                if (messageDAO == null) {
                    continue;
                }

                if (!videoSupported && messageDAO.containsVideoOrAudio()) {
                    Logger.d(
                            "Dropping inbox message containing video/audio as app does not support video. For more information checkout CleverTap documentation.");
                    continue;
                }

                newMessages.add(messageDAO);

                Logger.v("Inbox Message for message id - " + messageDAO.getId() + " added");
            } catch (JSONException e) {
                Logger.d("Unable to update notification inbox messages - " + e.getLocalizedMessage());
            }
        }

        if (newMessages.size() > 0) {
            this.dbAdapter.upsertMessages(newMessages);
            haveUpdates = true;
            Logger.v("New Notification Inbox messages added");
            synchronized (messagesLock) {
                this.messages = this.dbAdapter.getMessages(this.userId);
                trimMessages();
            }
        }
        return haveUpdates;
    }

    @AnyThread
    boolean _deleteMessageWithId(final String messageId) {
        CTMessageDAO messageDAO = findMessageById(messageId);
        if (messageDAO == null) {
            return false;
        }
        synchronized (messagesLock) {
            this.messages.remove(messageDAO);
        }

        Task<Void> task = CTExecutorFactory.executors(config).postAsyncSafelyTask();
        task.execute("RunDeleteMessage", new Callable<Void>() {
            @Override
            @WorkerThread
            public Void call() {
                dbAdapter.deleteMessageForId(messageId, userId);
                return null;
            }
        });
        return true;
    }

    @AnyThread
    boolean _deleteMessagesForIds(final ArrayList<String> messageIDs) {
        ArrayList<CTMessageDAO> messageDAOList = new ArrayList<>();
        for (String messageID : messageIDs) {
            CTMessageDAO messageDAO = findMessageById(messageID);
            if (messageDAO == null) {
                continue;
            }
            messageDAOList.add(messageDAO);
        }
        if (messageDAOList.isEmpty())
            return false;

        synchronized (messagesLock) {
            this.messages.removeAll(messageDAOList);
        }

        Task<Void> task = CTExecutorFactory.executors(config).postAsyncSafelyTask();
        task.execute("RunDeleteMessagesForIDs", new Callable<Void>() {
            @Override
            @WorkerThread
            public Void call() {
                dbAdapter.deleteMessagesForIDs(messageIDs, userId);
                return null;
            }
        });
        return true;
    }

    @AnyThread
    boolean _markReadForMessageWithId(final String messageId) {
        CTMessageDAO messageDAO = findMessageById(messageId);
        if (messageDAO == null) {
            return false;
        }

        synchronized (messagesLock) {
            messageDAO.setRead(1);
        }
        Task<Void> task = CTExecutorFactory.executors(config).postAsyncSafelyTask();
        task.addOnSuccessListener(unused -> callbackManager._notifyInboxMessagesDidUpdate() );//  //OR callbackManager.getInboxListener().inboxMessagesDidUpdate();
        task.addOnFailureListener(e -> Logger.d("Failed to update message read state for id:"+messageId,e));

        task.execute("RunMarkMessageRead", new Callable<Void>() {
            @Override
            @WorkerThread
            public Void call() {
                dbAdapter.markReadMessageForId(messageId, userId);
                return null;
            }
        });
        return true;
    }

    @AnyThread
    boolean _markReadForMessagesWithIds(final ArrayList<String> messageIDs) {
        Boolean atleastOneMessageIsValid = false;
        for (String messageId : messageIDs) {
            CTMessageDAO messageDAO = findMessageById(messageId);
            if (messageDAO == null) {
                continue;
            } else {
                atleastOneMessageIsValid = true;
                synchronized (messagesLock) {
                    messageDAO.setRead(1);
                }
            }
        }
        if (!atleastOneMessageIsValid)
            return false;

        Task<Void> task = CTExecutorFactory.executors(config).postAsyncSafelyTask();
        task.addOnSuccessListener(unused -> callbackManager._notifyInboxMessagesDidUpdate());
        task.addOnFailureListener(e -> Logger.d("Failed to update message read state for ids:" + messageIDs, e));

        task.execute("RunMarkMessagesReadForIDs", new Callable<Void>() {
            @Override
            @WorkerThread
            public Void call() {
                dbAdapter.markReadMessagesForIds(messageIDs, userId);
                return null;
            }
        });
        return true;
    }

    @AnyThread
    private CTMessageDAO findMessageById(String id) {
        synchronized (messagesLock) {
            for (CTMessageDAO message : messages) {
                if (message.getId().equals(id)) {
                    return message;
                }
            }
        }
        Logger.v("Inbox Message for message id - " + id + " not found");
        return null;
    }

    @AnyThread
    private void trimMessages() {
        Logger.v( "CTInboxController:trimMessages() called");
        ArrayList<CTMessageDAO> toDelete = new ArrayList<>();
        synchronized (messagesLock) {
            for (CTMessageDAO message : this.messages) {
                if (!videoSupported && message.containsVideoOrAudio()) {
                    Logger.d(
                            "Removing inbox message containing video/audio as app does not support video. For more information checkout CleverTap documentation.");
                    toDelete.add(message);
                    continue;
                }
                long expires = message.getExpires();
                boolean expired = (expires > 0 && System.currentTimeMillis() / 1000 > expires);
                if (expired) {
                    Logger.v("Inbox Message: " + message.getId() + " is expired - removing");
                    toDelete.add(message);
                }
            }

            if (toDelete.size() <= 0) {
                return;
            }

            for (CTMessageDAO message : toDelete) {
                _deleteMessageWithId(message.getId());
            }
        }
    }
}

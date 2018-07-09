package org.briarproject.briar.android.journey;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.UiThread;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.ActionMenuView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.briarproject.bramble.api.FormatException;
import org.briarproject.bramble.api.contact.Contact;
import org.briarproject.bramble.api.contact.ContactId;
import org.briarproject.bramble.api.contact.ContactManager;
import org.briarproject.bramble.api.contact.event.ContactRemovedEvent;
import org.briarproject.bramble.api.crypto.CryptoExecutor;
import org.briarproject.bramble.api.db.DatabaseExecutor;
import org.briarproject.bramble.api.db.DbException;
import org.briarproject.bramble.api.db.NoSuchContactException;
import org.briarproject.bramble.api.event.Event;
import org.briarproject.bramble.api.event.EventBus;
import org.briarproject.bramble.api.event.EventListener;
import org.briarproject.bramble.api.identity.AuthorId;
import org.briarproject.bramble.api.nullsafety.MethodsNotNullByDefault;
import org.briarproject.bramble.api.nullsafety.ParametersNotNullByDefault;
import org.briarproject.bramble.api.plugin.ConnectionRegistry;
import org.briarproject.bramble.api.plugin.event.ContactConnectedEvent;
import org.briarproject.bramble.api.plugin.event.ContactDisconnectedEvent;
import org.briarproject.bramble.api.settings.Settings;
import org.briarproject.bramble.api.settings.SettingsManager;
import org.briarproject.bramble.api.sync.GroupId;
import org.briarproject.bramble.api.sync.Message;
import org.briarproject.bramble.api.sync.MessageId;
import org.briarproject.bramble.api.sync.event.MessagesAckedEvent;
import org.briarproject.bramble.api.sync.event.MessagesSentEvent;
import org.briarproject.bramble.util.StringUtils;
import org.briarproject.briar.R;
import org.briarproject.briar.android.activity.ActivityComponent;
import org.briarproject.briar.android.activity.BriarActivity;
import org.briarproject.briar.android.blog.BlogActivity;
import org.briarproject.briar.android.contact.ConversationActivity;

import org.briarproject.briar.android.forum.ForumActivity;
import org.briarproject.briar.android.introduction.IntroductionActivity;
import org.briarproject.briar.android.privategroup.conversation.GroupActivity;
import org.briarproject.briar.android.view.BriarRecyclerView;
import org.briarproject.briar.android.view.TextInputView;
import org.briarproject.briar.android.view.TextInputView.TextInputListener;
import org.briarproject.briar.api.android.AndroidNotificationManager;
import org.briarproject.briar.api.blog.BlogSharingManager;
import org.briarproject.briar.api.client.ProtocolStateException;
import org.briarproject.briar.api.client.SessionId;
import org.briarproject.briar.api.forum.ForumSharingManager;
import org.briarproject.briar.api.introduction.IntroductionManager;
import org.briarproject.briar.api.introduction.IntroductionMessage;
import org.briarproject.briar.api.introduction.IntroductionRequest;
import org.briarproject.briar.api.introduction.IntroductionResponse;
import org.briarproject.briar.api.introduction.event.IntroductionRequestReceivedEvent;
import org.briarproject.briar.api.introduction.event.IntroductionResponseReceivedEvent;
import org.briarproject.briar.api.messaging.MessagingManager;
import org.briarproject.briar.api.messaging.PrivateMessage;
import org.briarproject.briar.api.messaging.PrivateMessageFactory;
import org.briarproject.briar.api.messaging.PrivateMessageHeader;
import org.briarproject.briar.api.messaging.event.PrivateMessageReceivedEvent;
import org.briarproject.briar.api.privategroup.invitation.GroupInvitationManager;
import org.briarproject.briar.api.sharing.InvitationMessage;
import org.briarproject.briar.api.sharing.InvitationRequest;
import org.briarproject.briar.api.sharing.InvitationResponse;
import org.briarproject.briar.api.sharing.event.InvitationRequestReceivedEvent;
import org.briarproject.briar.api.sharing.event.InvitationResponseReceivedEvent;
import org.thoughtcrime.securesms.components.util.FutureTaskListener;
import org.thoughtcrime.securesms.components.util.ListenableFutureTask;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

import javax.annotation.Nullable;
import javax.inject.Inject;

import de.hdodenhof.circleimageview.CircleImageView;
import im.delight.android.identicons.IdenticonDrawable;
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt;
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt.PromptStateChangeListener;

import static android.support.v4.view.ViewCompat.setTransitionName;
import static android.support.v7.util.SortedList.INVALID_POSITION;
import static android.widget.Toast.LENGTH_SHORT;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.WARNING;
import static org.briarproject.briar.android.activity.RequestCodes.REQUEST_INTRODUCTION;
import static org.briarproject.briar.android.contact.ConversationActivity.CONTACT_ID;
import static org.briarproject.briar.android.settings.SettingsFragment.SETTINGS_NAMESPACE;
import static org.briarproject.briar.android.util.UiUtils.getAvatarTransitionName;
import static org.briarproject.briar.android.util.UiUtils.getBulbTransitionName;
import static org.briarproject.briar.api.messaging.MessagingConstants.MAX_PRIVATE_MESSAGE_BODY_LENGTH;
import static uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt.STATE_DISMISSED;
import static uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt.STATE_FINISHED;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.TimePicker;

import org.briarproject.bramble.api.FormatException;
import org.briarproject.bramble.api.db.DbException;
import org.briarproject.bramble.api.event.Event;
import org.briarproject.bramble.api.nullsafety.MethodsNotNullByDefault;
import org.briarproject.bramble.api.nullsafety.ParametersNotNullByDefault;
import org.briarproject.briar.R;
import org.briarproject.briar.android.activity.ActivityComponent;
import org.briarproject.briar.android.fragment.BaseEventFragment;
import org.briarproject.briar.api.android.AndroidNotificationManager;


import static java.util.logging.Level.WARNING;



import java.util.Calendar;
import java.util.concurrent.Executor;
import java.util.logging.Logger;

import javax.annotation.Nullable;
import javax.inject.Inject;

import static java.util.logging.Level.WARNING;

@MethodsNotNullByDefault
@ParametersNotNullByDefault
public class JourneyFragment extends BaseEventFragment implements OnClickListener {

    public final static String TAG = JourneyFragment.class.getName();
    private final static Logger LOG = Logger.getLogger(TAG);
    private static Calendar journeyToAdd;

    @Inject
    AndroidNotificationManager notificationManager;

    //@Inject
    //volatile ForumSharingManager forumSharingManager;

    public static JourneyFragment newInstance() {

        Bundle args = new Bundle();

        JourneyFragment fragment = new JourneyFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        Log.e("ughugh","ughugh");

        Intent i = new Intent(getActivity(),
                ConversationActivity.class);
//        ContactId contactId = item.getContact().getId();
        i.putExtra(CONTACT_ID, 1);
        i.putExtra("something", true);
        startActivity(i);

        getActivity().setTitle(R.string.journey_button);

        View contentView =
                inflater.inflate(R.layout.journey, container,
                        false);

        return contentView;

    }
/*
    private void loadGroupId(String body, long timestamp) {
            try {
                createMessage(body, timestamp);
            } catch (DbException e) {
                if (LOG.isLoggable(WARNING)) LOG.log(WARNING, e.toString(), e);
            }
    }

    private void createMessage(String body, long timestamp) {
        cryptoExecutor.execute(() -> {
            try {
              //  noinspection ConstantConditions init in loadGroupId()

                ContactId contactId = new ContactId(1);

                MessagingManager messagingManager;

                GroupId messagingGroupId =
                        messagingManager.getConversationId(contactId);

                storeMessage(privateMessageFactory.createPrivateMessage(
                        messagingGroupId, timestamp, body), body);
                throw new FormatException();
            } catch (FormatException e) {throw new RuntimeException(e);
            }
        });
    }*/
/*
    private void storeMessage(PrivateMessage m, String body) {
            try {
                long now = System.currentTimeMillis();
                messagingManager.addLocalMessage(m);
                long duration = System.currentTimeMillis() - now;
                if (LOG.isLoggable(INFO))
                    LOG.info("Storing message took " + duration + " ms");
                Message message = m.getMessage();
                PrivateMessageHeader h = new PrivateMessageHeader(
                        message.getId(), message.getGroupId(),
                        message.getTimestamp(), true, false, false, false);
                ConversationItem item = ConversationItem.from(h);
                item.setBody(body);
                bodyCache.put(message.getId(), body);
                addConversationItem(item);
            } catch (DbException e) {
                if (LOG.isLoggable(WARNING)) LOG.log(WARNING, e.toString(), e);
            }
    }*/

    @Override
    public String getUniqueTag() {
        return TAG;
    }

    @Override
    public void injectFragment(ActivityComponent component) {
        component.inject(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        notificationManager.clearAllForumPostNotifications();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.journey_actions, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_create_journey:
                //Intent intent =
                //        new Intent(getContext(), CreateForumActivity.class);
                //startActivity(intent);
                DialogFragment newFragment = new DatePickerFragment();
                newFragment.show(this.getFragmentManager(), "datePicker");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void eventOccurred(Event e) {
		/*if (e instanceof ContactRemovedEvent) {
			LOG.info("Contact removed, reloading available forums");
		} else if (e instanceof GroupAddedEvent) {
			GroupAddedEvent g = (GroupAddedEvent) e;
			if (g.getGroup().getClientId().equals(CLIENT_ID)) {
				LOG.info("Forum added, reloading forums");
			}
		} else if (e instanceof GroupRemovedEvent) {
			GroupRemovedEvent g = (GroupRemovedEvent) e;
			if (g.getGroup().getClientId().equals(CLIENT_ID)) {
				LOG.info("Forum removed, removing from list");
				removeForum(g.getGroup().getId());
			}
		} else if (e instanceof ForumPostReceivedEvent) {
			ForumPostReceivedEvent f = (ForumPostReceivedEvent) e;
			LOG.info("Forum post added, updating item");
			updateItem(f.getGroupId(), f.getHeader());
		} else if (e instanceof ForumInvitationRequestReceivedEvent) {
			LOG.info("Forum invitation received, reloading available forums");
		}*/
    }

    @Override
    public void onClick(View view) {
/*//		// snackbar click
//		Intent i = new Intent(getContext(), ForumInvitationActivity.class);
//		startActivity(i);*/
    }

    @Override
    public void onSaveInstanceState(Bundle saveInstanceState) {
        super.onSaveInstanceState(saveInstanceState);
        saveDates();
    }

    private void saveDates() {

    }

    public static class DatePickerFragment extends DialogFragment
                            implements DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            // Do something with the date chosen by the user
            journeyToAdd = Calendar.getInstance();
            journeyToAdd.set(year, month, day);
            DialogFragment newFragment = new TimePickerFragment();
            newFragment.show(this.getFragmentManager(), "timePicker");
        }
    }

    public static class TimePickerFragment extends DialogFragment
                            implements TimePickerDialog.OnTimeSetListener {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current time as the default values for the picker
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        // Create a new instance of TimePickerDialog and return it
        return new TimePickerDialog(getActivity(), this, hour, minute,
                DateFormat.is24HourFormat(getActivity()));
    }

    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        // Do something with the time chosen by the user
        journeyToAdd.set(Calendar.HOUR_OF_DAY,hourOfDay);
        journeyToAdd.set(Calendar.MINUTE,minute);
        JourneyTimer.addJourney(journeyToAdd,getActivity().getApplicationContext());
    }
}
}

package org.briarproject.briar.android.home;


import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import org.briarproject.bramble.api.event.Event;
import org.briarproject.bramble.api.nullsafety.MethodsNotNullByDefault;
import org.briarproject.bramble.api.nullsafety.ParametersNotNullByDefault;
import org.briarproject.briar.R;
import org.briarproject.briar.android.activity.ActivityComponent;
import org.briarproject.briar.android.contact.ConversationActivity;
import org.briarproject.briar.android.fragment.BaseEventFragment;
import org.briarproject.briar.android.journey.JourneyFragment;
import org.briarproject.briar.api.android.AndroidNotificationManager;

import java.util.Calendar;
import java.util.logging.Logger;

import javax.annotation.Nullable;
import javax.inject.Inject;

import static org.briarproject.briar.android.contact.ConversationActivity.CONTACT_ID;

@MethodsNotNullByDefault
@ParametersNotNullByDefault
public class HomeFragment extends BaseEventFragment implements OnClickListener {

    public final static String TAG = org.briarproject.briar.android.emergency.EmergencyFragment.class.getName();
    private final static Logger LOG = Logger.getLogger(TAG);

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    @Inject
    AndroidNotificationManager notificationManager;

    public static HomeFragment newInstance() {

        Bundle args = new Bundle();

        HomeFragment fragment = new HomeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    //@Inject
    //AndroidNotificationManager notificationManager;

    public HomeFragment() {
        //Required empty public constructor
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
    {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        getActivity().setTitle("Home");

        View contentView =
                inflater.inflate(R.layout.home, container,
                        false);

        return contentView;
    }

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
                DialogFragment newFragment = new JourneyFragment.DatePickerFragment();
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

    }

    @Override
    public void onSaveInstanceState(Bundle saveInstanceState) {
        super.onSaveInstanceState(saveInstanceState);
    }

    private void goToEmergency(){
        Intent i = new Intent(getActivity(),
                ConversationActivity.class);
//        ContactId contactId = item.getContact().getId();
        i.putExtra(CONTACT_ID, 1);
        i.putExtra("something", true);
        startActivity(i);
    }

}
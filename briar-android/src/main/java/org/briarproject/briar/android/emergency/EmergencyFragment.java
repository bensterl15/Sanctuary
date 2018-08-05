package org.briarproject.briar.android.emergency;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
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
import org.briarproject.briar.api.android.AndroidNotificationManager;

import java.util.Calendar;
import java.util.logging.Logger;

import javax.annotation.Nullable;
import javax.inject.Inject;

import static org.briarproject.briar.android.contact.ConversationActivity.CONTACT_ID;

@MethodsNotNullByDefault
@ParametersNotNullByDefault
public class EmergencyFragment extends BaseEventFragment implements OnClickListener{

    public final static String TAG = EmergencyFragment.class.getName();
    private final static Logger LOG = Logger.getLogger(TAG);

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;


    //For the timer:
    private CountDownTimer emergencyCountDown;
    private boolean timerIsRunning = false;
    private boolean bGoToEmergency = true;

    private static TextView countdownLabel;

    //@Inject
    //AndroidNotificationManager notificationManager;

    public EmergencyFragment(){
        //Required empty public constructor
    }

    public static EmergencyFragment newInstance() {

        Bundle args = new Bundle();

        EmergencyFragment fragment = new EmergencyFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
    {
        countdownLabel = (TextView) view.findViewById(R.id.countdown_label);
        final ImageView emergencyStopButton = (ImageView) getView().findViewById(R.id.emergency_stop_button);
        final ImageView countdownButton = (ImageView) getView().findViewById(R.id.countdown_emergency_button);
        final FrameLayout cancel_frame = (FrameLayout) getView().findViewById(R.id.cancel_frame);
        final Button emergency_stop_stop = (Button) getView().findViewById(R.id.emergency_stop_stop);
        final Button emergency_stop_cancel = (Button) getView().findViewById(R.id.emergency_stop_cancel);

        countdownButton.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent){
                countdownLabel.setVisibility(View.VISIBLE);
                countdownButton.setVisibility(View.INVISIBLE);
                emergencyStopButton.setVisibility(View.VISIBLE);
                if(!timerIsRunning) {
                    emergencyCountDown = new CountDownTimer(11000, 1000) {
                        @Override
                        public void onTick(long millisUntilFinished) {
                            timerIsRunning = true;
                            int currentTime = Integer.parseInt(
                                    countdownLabel.getText().toString());
                            currentTime--;
                            countdownLabel.setText("" + currentTime);
                        }

                        @Override
                        public void onFinish() {
                            timerIsRunning = false;
                            countdownLabel.setText("11");
                            countdownLabel.setVisibility(View.INVISIBLE);
                            countdownButton.setVisibility(View.VISIBLE);
                            emergencyStopButton.setVisibility(View.INVISIBLE);
                            cancel_frame.setVisibility(View.INVISIBLE);
                            if(bGoToEmergency == true) goToEmergency();
                            bGoToEmergency = true;
                        }
                    }.start();
                }else{
                    bGoToEmergency = false;
                    emergencyCountDown.cancel();
                    emergencyCountDown.onFinish();
                }
                return false;
            }
        });
        emergencyStopButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                cancel_frame.setVisibility(View.VISIBLE);
            }
        });
        emergency_stop_stop.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                //Copied and pasted from OnFinish method above^
                timerIsRunning = false;
                countdownLabel.setText("11");
                countdownLabel.setVisibility(View.INVISIBLE);
                countdownButton.setVisibility(View.VISIBLE);
                emergencyStopButton.setVisibility(View.INVISIBLE);
                cancel_frame.setVisibility(View.INVISIBLE);
                bGoToEmergency = false;
                emergencyCountDown.cancel();
                emergencyCountDown.onFinish();
            }
        });
        emergency_stop_cancel.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                cancel_frame.setVisibility(View.INVISIBLE);
            }
        });
        //getView().setOnTouchListener((()));
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.emergency, container,false);
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
        //notificationManager.clearAllForumPostNotifications();
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

    private void goToEmergency(){
        Intent i = new Intent(getActivity(),
                ConversationActivity.class);
//        ContactId contactId = item.getContact().getId();
        i.putExtra(CONTACT_ID, 1);
        i.putExtra("something", true);
        startActivity(i);
    }

}

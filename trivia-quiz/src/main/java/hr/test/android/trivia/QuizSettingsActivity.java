package hr.test.android.trivia;

import java.util.Calendar;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.text.format.Time;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

public class QuizSettingsActivity extends QuizActivity {
	SharedPreferences mGameSettings;
	static final int DATE_DIALOG_ID = 0;
	static final int PASSWORD_DIALOG_ID = 1;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings);
		// Retrieve the shared preferences
		mGameSettings = getSharedPreferences(GAME_PREFERENCES, Context.MODE_PRIVATE);
		// Initialize the nickname entry
		initNicknameEntry();
		// Initialize the email entry
		initEmailEntry();
		// Initialize the Password chooser
		initPasswordChooser();
		// Initialize the Date picker
		initDatePicker();
		// Initialize the spinner
		initGenderSpinner();
	}

	@Override
	protected void onDestroy() {
		Log.d(DEBUG_TAG, "SHARED PREFERENCES");
		Log.d(DEBUG_TAG, "Nickname is: " + mGameSettings.getString(GAME_PREFERENCES_NICKNAME, "Not set"));
		Log.d(DEBUG_TAG, "Email is: " + mGameSettings.getString(GAME_PREFERENCES_EMAIL, "Not set"));
		Log.d(DEBUG_TAG, "Gender (M=1, F=2, U=0) is: " + mGameSettings.getInt(GAME_PREFERENCES_GENDER, 0));
		Log.d(DEBUG_TAG, "Password is: " + mGameSettings.getString(GAME_PREFERENCES_PASSWORD, "Not set"));
		Log.d(DEBUG_TAG, "DOB is: " + DateFormat.format("MMMM dd, yyyy", mGameSettings.getLong(GAME_PREFERENCES_DOB, 0)));
		super.onDestroy();
	}

	/**
	 * Initialize the nickname entry
	 */
	private void initNicknameEntry() {
		// Save Nickname
		final EditText nicknameText = (EditText) findViewById(R.id.EditText_Nickname);
		if (mGameSettings.contains(GAME_PREFERENCES_NICKNAME)) {
			nicknameText.setText(mGameSettings.getString(GAME_PREFERENCES_NICKNAME, ""));
		}
		nicknameText.setOnKeyListener(new View.OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
					String strNickname = nicknameText.getText().toString();
					Editor editor = mGameSettings.edit();
					editor.putString(GAME_PREFERENCES_NICKNAME, strNickname);
					editor.commit();
					return true;
				}
				return false;
			}
		});
	}

	/**
	 * Initialize the email entry
	 */
	private void initEmailEntry() {
		// Save Email
		final EditText emailText = (EditText) findViewById(R.id.EditText_Email);
		if (mGameSettings.contains(GAME_PREFERENCES_EMAIL)) {
			emailText.setText(mGameSettings.getString(GAME_PREFERENCES_EMAIL, ""));
		}
		emailText.setOnKeyListener(new View.OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
					Editor editor = mGameSettings.edit();
					editor.putString(GAME_PREFERENCES_EMAIL, emailText.getText().toString());
					editor.commit();
					return true;
				}
				return false;
			}
		});
	}

	/**
	 * Initialize the Password chooser
	 */
	private void initPasswordChooser() {
		// Set password info
		TextView passwordInfo = (TextView) findViewById(R.id.TextView_Password_Info);
		if (mGameSettings.contains(GAME_PREFERENCES_PASSWORD)) {
			passwordInfo.setText(R.string.settings_pwd_set);
		} else {
			passwordInfo.setText(R.string.settings_pwd_not_set);
		}
		// Handle password setting dialog
		Button setPassword = (Button) findViewById(R.id.Button_Password);
		setPassword.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				showDialog(PASSWORD_DIALOG_ID);
			}
		});
	}

	/**
	 * Initialize the Date picker
	 */
	private void initDatePicker() {
		// Set password info
		TextView dobInfo = (TextView) findViewById(R.id.TextView_DOB_Info);
		if (mGameSettings.contains(GAME_PREFERENCES_DOB)) {
			dobInfo.setText(DateFormat.format("MMMM dd, yyyy", mGameSettings.getLong(GAME_PREFERENCES_DOB, 0)));
		} else {
			dobInfo.setText(R.string.settings_dob_not_set);
		}
		// Handle date picking dialog
		Button pickDate = (Button) findViewById(R.id.Button_DOB);
		pickDate.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				showDialog(DATE_DIALOG_ID);
			}
		});
	}

	/**
	 * Initialize the spinner
	 */
	private void initGenderSpinner() {
		// Populate Spinner control with genders
		final Spinner spinner = (Spinner) findViewById(R.id.Spinner_Gender);
		ArrayAdapter<?> adapter = ArrayAdapter.createFromResource(this, R.array.genders, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		if (mGameSettings.contains(GAME_PREFERENCES_GENDER)) {
			spinner.setSelection(mGameSettings.getInt(GAME_PREFERENCES_GENDER, 0));
		}
		// Handle spinner selections
		spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parent, View itemSelected, int selectedItemPosition, long selectedId) {
				Editor editor = mGameSettings.edit();
				editor.putInt(GAME_PREFERENCES_GENDER, selectedItemPosition);
				editor.commit();
			}

			public void onNothingSelected(AdapterView<?> parent) {
			}
		});
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DATE_DIALOG_ID:
			final TextView dob = (TextView) findViewById(R.id.TextView_DOB_Info);
			Calendar cal = Calendar.getInstance();
			// Today's date fields

			int iDay,
			iMonth,
			iYear;
			iDay = cal.get(Calendar.DAY_OF_MONTH);
			iMonth = cal.get(Calendar.MONTH);
			iYear = cal.get(Calendar.YEAR);

			DatePickerDialog dateDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
				public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

					Time dateOfBirth = new Time();
					dateOfBirth.set(dayOfMonth, monthOfYear, year);

					long dtDob = dateOfBirth.toMillis(true);
					dob.setText(DateFormat.format("MMMM dd, yyyy", dtDob));

					Editor editor = mGameSettings.edit();
					editor.putLong(GAME_PREFERENCES_DOB, dtDob);
					editor.commit();
				}
			}, iYear, iMonth, iDay);
			return dateDialog;
		case PASSWORD_DIALOG_ID:

			return null;
		}
		return null;
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		super.onPrepareDialog(id, dialog);
		switch (id) {
		case DATE_DIALOG_ID:
			// Handle any DatePickerDialog initialization here
			DatePickerDialog dateDialog = (DatePickerDialog) dialog;
			int iDay,
			iMonth,
			iYear;
			// Check for date of birth preference
			if (mGameSettings.contains(GAME_PREFERENCES_DOB)) {
				// Retrieve Birth date setting from preferences
				long msBirthDate = mGameSettings.getLong(GAME_PREFERENCES_DOB, 0);

				Time dateOfBirth = new Time();
				dateOfBirth.set(msBirthDate);

				iDay = dateOfBirth.monthDay;
				iMonth = dateOfBirth.month;
				iYear = dateOfBirth.year;
			} else {

				Calendar cal = Calendar.getInstance();
				// Today's date fields
				iDay = cal.get(Calendar.DAY_OF_MONTH);
				iMonth = cal.get(Calendar.MONTH);
				iYear = cal.get(Calendar.YEAR);
			}

			// Set the date in the DatePicker to the date of birth OR to the
			// current date
			dateDialog.updateDate(iYear, iMonth, iDay);
			return;
		case PASSWORD_DIALOG_ID:
			// Handle any Password Dialog initialization here
			// Since we don't want to show old password dialogs, just set new
			// ones, we need not do anything here
			// Because we are not "reusing" password dialogs once they have
			// finished, but removing them from
			// the Activity Dialog pool explicitly with removeDialog() and
			// recreating them as needed.
			return;
		}
	}
}

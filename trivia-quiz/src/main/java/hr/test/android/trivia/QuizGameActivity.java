package hr.test.android.trivia;

import java.io.IOException;
import java.net.URL;
import java.util.Hashtable;

import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher;

public class QuizGameActivity extends QuizActivity {
	/** Called when the activity is first created. */

	SharedPreferences mGameSettings;
	Hashtable<Integer, Question> mQuestions;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.game);

		// Handle yes button
		Button yesButton = (Button) findViewById(R.id.Button_Yes);
		yesButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				handleAnswerAndShowNextQuestion(true);
			}
		});

		// Handle no button
		Button noButton = (Button) findViewById(R.id.Button_No);
		noButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				handleAnswerAndShowNextQuestion(false);
			}
		});

		// Retrieve the shared preferences
		mGameSettings = getSharedPreferences(GAME_PREFERENCES, Context.MODE_PRIVATE);

		// Initialize question batch
		mQuestions = new Hashtable<Integer, Question>(QUESTION_BATCH_SIZE);

		// Load the questions
		int startingQuestionNumber = mGameSettings.getInt(GAME_PREFERENCES_CURRENT_QUESTION, 0);

		// If we're at the beginning of the quiz, initialize the Shared
		// preferences
		if (startingQuestionNumber == 0) {
			Editor editor = mGameSettings.edit();
			editor.putInt(GAME_PREFERENCES_CURRENT_QUESTION, 1);
			editor.commit();
			startingQuestionNumber = 1;
		}

		try {
			loadQuestionBatch(startingQuestionNumber);
		} catch (Exception e) {
			Log.e(DEBUG_TAG, "Loading initial question batch failed", e);
		}

		Animation in = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
		Animation out = AnimationUtils.loadAnimation(this, android.R.anim.fade_out);

		// Set up Text Switcher
		TextSwitcher questionTextSwitcher = (TextSwitcher) findViewById(R.id.TextSwitcher_QuestionText);
		questionTextSwitcher.setInAnimation(in);
		questionTextSwitcher.setOutAnimation(out);
		questionTextSwitcher.setFactory(new MyTextSwitcherFactory());

		// Set up Image Switcher
		ImageSwitcher questionImageSwitcher = (ImageSwitcher) findViewById(R.id.ImageSwitcher_QuestionImage);
		questionImageSwitcher.setInAnimation(in);
		questionImageSwitcher.setOutAnimation(out);
		questionImageSwitcher.setFactory(new MyImageSwitcherFactory());

		// If the question was loaded properly, display it
		if (mQuestions.containsKey(startingQuestionNumber) == true) {
			// Set the text of the textswitcher
			questionTextSwitcher.setCurrentText(getQuestionText(startingQuestionNumber));

			// Set the image of the imageswitcher
			Drawable image = getQuestionImageDrawable(startingQuestionNumber);
			questionImageSwitcher.setImageDrawable(image);
		} else {
			// Tell the user we don't have any new questions at this time
			handleNoQuestions();
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		getMenuInflater().inflate(R.menu.gameoptions, menu);
		menu.findItem(R.id.help_menu_item).setIntent(new Intent(this, QuizHelpActivity.class));
		menu.findItem(R.id.settings_menu_item).setIntent(new Intent(this, QuizSettingsActivity.class));
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		startActivity(item.getIntent());
		return true;
	}

	/**
	 * A switcher factory for use with the question image. Creates the next
	 * {@code ImageView} object to animate to
	 * 
	 */
	private class MyImageSwitcherFactory implements ViewSwitcher.ViewFactory {
		public View makeView() {
			ImageView imageView = new ImageView(QuizGameActivity.this);
			imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
			imageView.setLayoutParams(new ImageSwitcher.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
			return imageView;
		}
	}

	/**
	 * A switcher factory for use with the question text. Creates the next
	 * {@code TextView} object to animate to
	 * 
	 */
	private class MyTextSwitcherFactory implements ViewSwitcher.ViewFactory {
		public View makeView() {
			TextView textView = new TextView(QuizGameActivity.this);
			textView.setGravity(Gravity.CENTER);
			Resources res = getResources();
			float dimension = res.getDimension(R.dimen.game_question_size);
			int titleColor = res.getColor(R.color.title_color);
			int shadowColor = res.getColor(R.color.title_glow);
			textView.setTextSize(dimension);
			textView.setTextColor(titleColor);
			textView.setShadowLayer(10, 5, 5, shadowColor);
			return textView;
		}
	}

	/**
	 * 
	 * Helper method to record the answer the user gave and load up the next
	 * question.
	 * 
	 * @param bAnswer
	 *            The answer the user gave
	 */
	private void handleAnswerAndShowNextQuestion(boolean bAnswer) {
		int curScore = mGameSettings.getInt(GAME_PREFERENCES_SCORE, 0);
		int nextQuestionNumber = mGameSettings.getInt(GAME_PREFERENCES_CURRENT_QUESTION, 1) + 1;

		Editor editor = mGameSettings.edit();
		editor.putInt(GAME_PREFERENCES_CURRENT_QUESTION, nextQuestionNumber);

		// Log the number of "yes" answers only
		if (bAnswer == true) {
			editor.putInt(GAME_PREFERENCES_SCORE, curScore + 1);
		}
		editor.commit();

		if (mQuestions.containsKey(nextQuestionNumber) == false) {
			// Load next batch
			try {
				loadQuestionBatch(nextQuestionNumber);
			} catch (Exception e) {
				Log.e(DEBUG_TAG, "Loading updated question batch failed", e);
			}
		}

		if (mQuestions.containsKey(nextQuestionNumber) == true) {
			// Update question text
			TextSwitcher questionTextSwitcher = (TextSwitcher) findViewById(R.id.TextSwitcher_QuestionText);
			questionTextSwitcher.setText(getQuestionText(nextQuestionNumber));

			// Update question image
			ImageSwitcher questionImageSwitcher = (ImageSwitcher) findViewById(R.id.ImageSwitcher_QuestionImage);
			Drawable image = getQuestionImageDrawable(nextQuestionNumber);
			questionImageSwitcher.setImageDrawable(image);
		} else {
			handleNoQuestions();
		}

	}

	/**
	 * Helper method to configure the question screen when no questions were
	 * found. Could be called for a variety of error cases, including no new
	 * questions, IO failures, or parser failures.
	 */
	private void handleNoQuestions() {
		TextSwitcher questionTextSwitcher = (TextSwitcher) findViewById(R.id.TextSwitcher_QuestionText);
		questionTextSwitcher.setText(getResources().getText(R.string.no_questions));
		ImageSwitcher questionImageSwitcher = (ImageSwitcher) findViewById(R.id.ImageSwitcher_QuestionImage);
		questionImageSwitcher.setImageResource(R.drawable.noquestion);

		// Disable yes button
		Button yesButton = (Button) findViewById(R.id.Button_Yes);
		yesButton.setEnabled(false);

		// Disable no button
		Button noButton = (Button) findViewById(R.id.Button_No);
		noButton.setEnabled(false);
	}

	/**
	 * Returns a {@code String} representing the text for a particular question
	 * number
	 * 
	 * @param questionNumber
	 *            The question number to get the text for
	 * @return The text of the question, or null if {@code questionNumber} not
	 *         found
	 */
	private String getQuestionText(Integer questionNumber) {
		String text = null;
		Question curQuestion = (Question) mQuestions.get(questionNumber);
		if (curQuestion != null) {
			text = curQuestion.mText;
		}
		return text;
	}

	/**
	 * Returns a {@code String} representing the URL to an image for a
	 * particular question
	 * 
	 * @param questionNumber
	 *            The question to get the URL for
	 * @return A {@code String} for the URL or null if none found
	 */
	private String getQuestionImageUrl(Integer questionNumber) {
		String url = null;
		Question curQuestion = (Question) mQuestions.get(questionNumber);
		if (curQuestion != null) {
			url = curQuestion.mImageUrl;
		}
		return url;
	}

	/**
	 * Retrieves a {@code Drawable} object for a particular question
	 * 
	 * @param questionNumber
	 *            The question number to get the {@code Drawable} for
	 * @return A {@code Drawable} for the particular question, or a placeholder
	 *         image if the loading failed or the question doesn't exist
	 */
	private Drawable getQuestionImageDrawable(int questionNumber) {
		Drawable image;
		URL imageUrl;

		try {
			// Create a Drawable by decoding a stream from a remote URL
			imageUrl = new URL(getQuestionImageUrl(questionNumber));
			Bitmap bitmap = BitmapFactory.decodeStream(imageUrl.openStream());
			image = new BitmapDrawable(bitmap);
		} catch (Exception e) {
			Log.e(DEBUG_TAG, "Decoding Bitmap stream failed.");
			image = getResources().getDrawable(R.drawable.noquestion);
		}
		return image;
	}

	/**
	 * Loads the XML into the {@see mQuestions} class member variable
	 * 
	 * @param startQuestionNumber
	 *            TODO: currently unused
	 * @throws XmlPullParserException
	 *             Thrown if XML parsing errors
	 * @throws IOException
	 *             Thrown if errors loading XML
	 */
	private void loadQuestionBatch(int startQuestionNumber) throws XmlPullParserException, IOException {
		// Remove old batch
		mQuestions.clear();

		// TODO: Contact the server and retrieve a batch of question data,
		// beginning at startQuestionNumber
		XmlResourceParser questionBatch;

		// BEGIN MOCK QUESTIONS
		if (startQuestionNumber < 16) {
			questionBatch = getResources().getXml(R.xml.samplequestions);
		} else {
			questionBatch = getResources().getXml(R.xml.samplequestions2);
		}
		// END MOCK QUESTIONS

		// Parse the XML
		int eventType = -1;

		// Find Score records from XML
		while (eventType != XmlResourceParser.END_DOCUMENT) {
			if (eventType == XmlResourceParser.START_TAG) {

				// Get the name of the tag (eg questions or question)
				String strName = questionBatch.getName();

				if (strName.equals(XML_TAG_QUESTION)) {

					String questionNumber = questionBatch.getAttributeValue(null, XML_TAG_QUESTION_ATTRIBUTE_NUMBER);
					Integer questionNum = Integer.valueOf(questionNumber);
					String questionText = questionBatch.getAttributeValue(null, XML_TAG_QUESTION_ATTRIBUTE_TEXT);
					String questionImageUrl = questionBatch.getAttributeValue(null, XML_TAG_QUESTION_ATTRIBUTE_IMAGEURL);

					// Save data to our hashtable
					mQuestions.put(questionNum, new Question(questionNum, questionText, questionImageUrl));
				}
			}
			eventType = questionBatch.next();
		}
	}

	/**
	 * Object to manage the data for a single quiz question
	 * 
	 */
	private class Question {
		@SuppressWarnings("unused")
		int mNumber;
		String mText;
		String mImageUrl;

		/**
		 * 
		 * Constructs a new question object
		 * 
		 * @param questionNum
		 *            The number of this question
		 * @param questionText
		 *            The text for this question
		 * @param questionImageUrl
		 *            A valid image Url to display with this question
		 */
		public Question(int questionNum, String questionText, String questionImageUrl) {
			mNumber = questionNum;
			mText = questionText;
			mImageUrl = questionImageUrl;
		}
	}
}

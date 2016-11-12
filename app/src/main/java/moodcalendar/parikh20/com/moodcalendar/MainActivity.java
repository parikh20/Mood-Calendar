package moodcalendar.parikh20.com.moodcalendar;

import android.content.Intent;
import android.os.AsyncTask;
import android.speech.RecognizerIntent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.ibm.watson.developer_cloud.tone_analyzer.v3.ToneAnalyzer;
import com.ibm.watson.developer_cloud.tone_analyzer.v3.model.ToneAnalysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private FloatingActionButton mVoiceButton;
    private FloatingActionButton mNewEventButton;

    // Input speech
    private String mProcessedSpeech;
    private ToneAnalysis mTone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mVoiceButton = (FloatingActionButton) findViewById(R.id.fab_voice);
        mNewEventButton = (FloatingActionButton) findViewById(R.id.fab_new_event);

        //Setup voice listener
        mVoiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Record and process speech

                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, Locale.getDefault());
                try {
                    startActivityForResult(intent, 100);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                System.out.println("Fin.");
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Process the speech and store the text
        switch (requestCode) {
            case 100: {
                if (resultCode == RESULT_OK && data != null) {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent
                            .EXTRA_RESULTS);
                    mProcessedSpeech = result.get(0);
                    Toast.makeText(MainActivity.this, mProcessedSpeech,
                            Toast.LENGTH_SHORT).show();

                    // Process tone
                    new MainActivity.BackgroundThread().execute();
                }
                break;
            }
        }
    }

    // Async task to process tone
    public class BackgroundThread extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            ToneAnalyzer service = new ToneAnalyzer(ToneAnalyzer.VERSION_DATE_2016_05_19);
            service.setUsernameAndPassword("751f2adf-d4f5-46f4-864c-2dd3d9568576", "mx6fQ6Mlf0li");
            String text = mProcessedSpeech;

            ToneAnalysis tone = service.getTone(text, null).execute();
            mTone = tone;
            System.out.println(tone);

            // Find max scoring emotion and value
            double maxScore = 0;
            String emotionName = "";
            for (int i = 0; i < tone.getDocumentTone().getTones().get(0).getTones().size(); i++) {
                double toneScore = tone.getDocumentTone().getTones().get(0).getTones()
                        .get(i).getScore();
                String toneName = tone.getDocumentTone().getTones().get(0).getTones()
                        .get(i).getName();
                if ( toneScore > maxScore) {
                    maxScore = toneScore;
                    emotionName = toneName;
                }
            }

            System.out.println(emotionName + " " + maxScore);

            return null;
        }
    }
}









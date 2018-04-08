package com.fireb.store.firebasestorage;

import com.firebase.client.Firebase;
import com.firebase.client.Firebase.CompletionListener;
import com.firebase.client.FirebaseError;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.speech.v1.RecognitionAudio;
import com.google.cloud.speech.v1.RecognitionConfig;
import com.google.cloud.speech.v1.RecognitionConfig.AudioEncoding;
import com.google.cloud.speech.v1.RecognizeResponse;
import com.google.cloud.speech.v1.SpeechClient;
import com.google.cloud.speech.v1.SpeechRecognitionAlternative;
import com.google.cloud.speech.v1.SpeechRecognitionResult;
import com.google.cloud.speech.v1.SpeechSettings;
import com.google.cloud.storage.*;
import com.google.protobuf.ByteString;

import java.io.*;
import java.util.logging.*;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class App {

	/**
	 * SECURITY
	 * No unnecessary API keys are included
	 * API key is used only for the necessary applications and are unique to this product
	 * API key should be regenerated periodically every 3 months
	 * Current API key was created on 3rd April
	 * REGENERATION DATE 3rd July
	 */

	// The JSON credentials for our project in google cloud platform
	public static final String GOOGLE_APPLICATION_CREDENTIALS = "LetMeSpeak-32d7113ca387.json";

	public static void main(String[] args) {

		try {

			/**
			 * SECURITY
			 * API key is not embeded directly within the code.
			 * It is accessed only via the json file
			 */

			// Getting the json file of the google speech API in the file input stream
			FileInputStream credentialsStream = new FileInputStream("LetMeSpeak-32d7113ca387.json");

			// Creating google credentials using file input stream
			GoogleCredentials credentials = GoogleCredentials.fromStream(credentialsStream);

			// Adding the credentials for the credentials provided object
			FixedCredentialsProvider credentialsProvider = FixedCredentialsProvider.create(credentials);

			// Using the above information regarding the API, the speech settings in the API is built
			SpeechSettings speechSettings = SpeechSettings.newBuilder().setCredentialsProvider(credentialsProvider)
					.build();

			// Getting the path for the recording
			Path path = Paths.get("Recording.amr");

			// Getting the recording data into a byte array
			byte[] data = Files.readAllBytes(path);

			// Creating a speech client
			SpeechClient speechClient = SpeechClient.create(speechSettings);

			// Creating a byte string from the audio file
			ByteString audioBytes = ByteString.copyFrom(data);

			// Builds the sync recognize request
			RecognitionConfig config = RecognitionConfig.newBuilder().setEncoding(AudioEncoding.AMR)
					.setSampleRateHertz(8000).setLanguageCode("en-US").build();
			RecognitionAudio audio = RecognitionAudio.newBuilder().setContent(audioBytes).build();

			// To perform speech recognition on the audio file
			RecognizeResponse response = speechClient.recognize(config, audio);

			// To store a list of responses from the speech recognition
			List<SpeechRecognitionResult> results = response.getResultsList();

			// To store the value of all the text spoken in the recording
			String speechValue = "";

			// Creating a string builder for all the text spoken
			StringBuilder allSpeech = new StringBuilder("");

			for (SpeechRecognitionResult result : results) {

				// Using the first alternative result as it is tend's to be the most accurate
				SpeechRecognitionAlternative alt = result.getAlternatives(0);

				// Adding the transcript of the first alternative to the speechValue string
				speechValue += alt.getTranscript();
			}

			// Adding the speech said to the String Builder as it is mutable
			allSpeech.append(speechValue);

			/*
			 * To replace the full stops and the commas
			 * with an empty characters for the algorithm to work
			 */
			speechValue = speechValue.replaceAll("\\,", "");
			speechValue = speechValue.replaceAll("\\.", "");
			speechValue = speechValue.toLowerCase();

			// Printing the speech value to the console for logging and debugging purposes
			System.out.println(speechValue);

			/**
			 * SECURITY
			 * By using firebase the security rules are node based and controlled
			 * Firebase CLI will warn owners if any security rules are breached, thereby making it safe for this application
			 * Appropriate security permissions are enabled on Firebase and therefore cannot be changed by the user
			 */

			// Reference to firebase database
			Firebase ref = new Firebase("https://letmespeak-f0b81.firebaseio.com/");

			// The current date and time of the recording
			Date now = new Date();

			// The current user's ID for access to the database and storage
			String userId = "5nv0H64fLfeqD3i8evnxfdoOut63";

			// String formatters for the date to add to the database or storage
			SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd");
			SimpleDateFormat titleformat = new SimpleDateFormat("MMMM dd, yyyy");
			SimpleDateFormat filenameFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
			SimpleDateFormat timeformat1 = new SimpleDateFormat("HH:mm:ss");
			SimpleDateFormat timeformat2 = new SimpleDateFormat("HH-mm-ss");

			// Reference to the child of the main database for reports
			Firebase reportRef = ref.child("username/" + userId + "/reports");

			/*
			 * To get the pattern from the text and
			 * to find the number of matches for that pattern in the text
			 */
			int numStutters = SDGPAlgo.getPattern(speechValue);

			// To get the stutter percent of the recording
			// Printing the number of stutters to the console for logging and debugging purposes
			System.out.println(numStutters);
			double stuttersPercStart = numStutters / SDGPAlgo.totalNoOfWords;
			double stuttersPerc = stuttersPercStart * 100;

			// To round the value to two decimal places
			String stutterPercent = String.format("%.2f", stuttersPerc);

			// Setting the title of the current report in the database
			reportRef.child(dateformat.format(now)).child("title")
					.setValue(titleformat.format(now), new Firebase.CompletionListener() {

						@Override public void onComplete(FirebaseError arg0, Firebase arg1) {
							if (arg0 != null) {
								System.out.println("Data could not be saved. " + arg0.getMessage());
							} else {
								System.out.println("Data saved successfully.");
							}
						}
					});

			// Setting the number of stuttering for that recording in the report details in the database
			reportRef.child(dateformat.format(now)).child("details").child("stutters")
					.setValue(numStutters, new Firebase.CompletionListener() {

						@Override public void onComplete(FirebaseError arg0, Firebase arg1) {
							if (arg0 != null) {
								System.out.println("Data could not be saved. " + arg0.getMessage());
							} else {
								System.out.println("Data saved successfully.");
							}
						}
					});

			// Setting the stutter percent for that recording in the report details in the database
			reportRef.child(dateformat.format(now)).child("details").child("stutterPercent")
					.setValue(stutterPercent, new Firebase.CompletionListener() {

						@Override public void onComplete(FirebaseError arg0, Firebase arg1) {
							if (arg0 != null) {
								System.out.println("Data could not be saved. " + arg0.getMessage());
							} else {
								System.out.println("Data saved successfully.");
							}
						}
					});

			// Getting the path for the recording
			path = Paths.get("Recording.mp3");

			// Getting the recording data into a byte array
			data = Files.readAllBytes(path);

			// Creating storage reference for the recording, giving the credentials
			Storage storage = StorageOptions.newBuilder().setCredentials(
					ServiceAccountCredentials.fromStream(new FileInputStream("LetMeSpeak-32d7113ca387.json"))).build()
					.getService();

			// Creating a blob to contain the file for upload to storage
			BlobId blobId = BlobId
					.of("letmespeak-f0b81.appspot.com", userId + "/" + filenameFormat.format(now) + ".mp3");
			BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType("audio/mp3").build();
			Blob blob = storage.create(blobInfo, data);

			// Changing the download url to a usable one for the user in the application
			String downloadUrl = blob.getMediaLink();
			downloadUrl = downloadUrl.replace("www", "firebasestorage");
			downloadUrl = downloadUrl.replace("/download/storage/v1", "/v0");
			downloadUrl = downloadUrl.split("\\?")[0];
			downloadUrl += "?alt=media";

			// Reference to the child of the main database for recordings
			Firebase recordRef = ref.child("username/" + userId + "/recordings");

			// Setting the time for the recording in the database
			recordRef.child(dateformat.format(now)).child(timeformat2.format(now)).child("details")
					.setValue(timeformat1.format(now), new CompletionListener() {

						@Override public void onComplete(FirebaseError arg0, Firebase arg1) {
							if (arg0 != null) {
								System.out.println("Data could not be saved. " + arg0.getMessage());
							} else {
								System.out.println("Data saved successfully.");
							}
						}

					});

			// Setting the download URL for that recording in the database
			recordRef.child(dateformat.format(now)).child(timeformat2.format(now)).child("filename")
					.setValue(downloadUrl, new CompletionListener() {

						@Override public void onComplete(FirebaseError arg0, Firebase arg1) {
							if (arg0 != null) {
								System.out.println("Data could not be saved. " + arg0.getMessage());
							} else {
								System.out.println("Data saved successfully.");
							}
						}
					});

		} catch (IOException ex) {
			Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
		}

	}
}

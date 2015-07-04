package com.jdroid.sample.android.ui.drive;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;
import com.jdroid.android.fragment.AbstractFragment;
import com.jdroid.java.concurrent.ExecutorUtils;
import com.jdroid.java.utils.IdGenerator;
import com.jdroid.java.utils.LoggerUtils;
import com.jdroid.sample.android.R;

import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;


public class GoogleDriveFragment extends AbstractFragment implements GoogleApiClient.ConnectionCallbacks,
		GoogleApiClient.OnConnectionFailedListener {

	private final static Logger LOGGER = LoggerUtils.getLogger(GoogleDriveFragment.class);

	private static final int RESOLVE_CONNECTION_REQUEST_CODE = 1;

	private GoogleApiClient googleApiClient;

	final private ResultCallback<DriveApi.MetadataBufferResult> metadataCallback = new
			ResultCallback<DriveApi.MetadataBufferResult>() {
				@Override
				public void onResult(DriveApi.MetadataBufferResult result) {
					if (result.getStatus().isSuccess()) {
						LOGGER.info("Retrieved file count: " + result.getMetadataBuffer().getCount());
						Iterator<Metadata> iterator = result.getMetadataBuffer().iterator();
						while (iterator.hasNext()) {
							Metadata metadata = iterator.next();
							LOGGER.info("Title: " + metadata.getTitle());
							LOGGER.info("Drive id: " + metadata.getDriveId());
							Drive.DriveApi.fetchDriveId(googleApiClient, metadata.getDriveId().toString()).setResultCallback(idCallback);
						}
					} else {
						LOGGER.error(result.getStatus().getStatusMessage());
					}
				}
			};

	final private ResultCallback<DriveFolder.DriveFileResult> driveFileResultCallback = new ResultCallback<DriveFolder.DriveFileResult>() {


		@Override
		public void onResult(final DriveFolder.DriveFileResult driveFileResult) {
			ExecutorUtils.execute(new Runnable() {
				@Override
				public void run() {
					if (driveFileResult.getStatus().isSuccess()) {
						LOGGER.info("File created !!!!");
					} else {
						LOGGER.error(driveFileResult.getStatus().getStatusMessage());
					}
				}
			});
		}
	};

	final private ResultCallback<DriveApi.DriveIdResult> idCallback = new ResultCallback<DriveApi.DriveIdResult>() {
		@Override
		public void onResult(final DriveApi.DriveIdResult result) {

			ExecutorUtils.execute(new Runnable() {
				@Override
				public void run() {
					if (result.getStatus().isSuccess()) {
						String contents = null;
						DriveFile file = Drive.DriveApi.getFile(googleApiClient, result.getDriveId());
						DriveApi.DriveContentsResult driveContentsResult =
								file.open(googleApiClient, DriveFile.MODE_READ_ONLY, null).await();
						if (driveContentsResult.getStatus().isSuccess()) {
							DriveContents driveContents = driveContentsResult.getDriveContents();
							BufferedReader reader = new BufferedReader(
									new InputStreamReader(driveContents.getInputStream()));
							StringBuilder builder = new StringBuilder();
							String line;
							try {
								while ((line = reader.readLine()) != null) {
									builder.append(line);
								}
								contents = builder.toString();
								LOGGER.info(contents);
							} catch (IOException e) {
								LOGGER.error("IOException while reading from the stream", e);
							}

							driveContents.discard(googleApiClient);
						}
					} else {
						LOGGER.error(result.getStatus().getStatusMessage());
					}
				}
			});
		}
	};

	private final ResultCallback<DriveApi.DriveContentsResult> contentsCallback = new
			ResultCallback<DriveApi.DriveContentsResult>() {
				@Override
				public void onResult(DriveApi.DriveContentsResult result) {
					if (!result.getStatus().isSuccess()) {
						// Handle error
						return;
					}

					MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
							.setTitle("New file " + IdGenerator.getRandomIntId())
							.setMimeType("application/vnd.google-apps.spreadsheet").build();
					// Create a file in the root folder
					Drive.DriveApi.getRootFolder(googleApiClient)
							.createFile(googleApiClient, changeSet, result.getDriveContents())
							.setResultCallback(driveFileResultCallback);
				}
			};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		googleApiClient = new GoogleApiClient.Builder(getActivity())
				.addApi(Drive.API)
				.addScope(Drive.SCOPE_FILE)
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this)
				.build();
	}

	@Override
	public void onStart() {
		super.onStart();
		googleApiClient.connect();
	}

	/**
	 * @see android.support.v4.app.Fragment#onCreateView(LayoutInflater, ViewGroup,
	 * Bundle)
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.google_drive_fragment, container, false);
	}

	/**
	 * @see AbstractFragment#onViewCreated(View, Bundle)
	 */
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		
		findView(R.id.sendExampleEvent).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Drive.DriveApi.newDriveContents(googleApiClient)
						.setResultCallback(contentsCallback);
			}
		});
	}

	@Override
	public void onConnected(Bundle bundle) {

		Drive.DriveApi.requestSync(googleApiClient).setResultCallback(new ResultCallback<Status>() {
			@Override
			public void onResult(Status status) {
				LOGGER.info("requestSync status: " + status.getStatusMessage());
			}
		});

		Query query = new Query.Builder()
				.addFilter(Filters.eq(SearchableField.STARRED, false))
//				.addFilter(Filters.eq(SearchableField.TITLE, "Doc Sample"))
				.build();
		Drive.DriveApi.query(googleApiClient, query)
				.setResultCallback(metadataCallback);

	}

	@Override
	public void onConnectionSuspended(int i) {

	}

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		if (connectionResult.hasResolution()) {
			try {
				connectionResult.startResolutionForResult(getActivity(), RESOLVE_CONNECTION_REQUEST_CODE);
			} catch (IntentSender.SendIntentException e) {
				// TODO Unable to resolve, message user appropriately
			}
		} else {
			GooglePlayServicesUtil.getErrorDialog(connectionResult.getErrorCode(), getActivity(), 0).show();
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == RESOLVE_CONNECTION_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
			googleApiClient.connect();
		}

	}
}

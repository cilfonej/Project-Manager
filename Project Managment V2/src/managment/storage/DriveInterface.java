package managment.storage;
//
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.nio.channels.Channels;
//import java.nio.file.Files;
//import java.nio.file.Paths;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Collections;
//import java.util.List;
//
//import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
//import com.google.api.client.auth.oauth2.Credential;
//import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
//import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
//import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
//import com.google.api.client.http.FileContent;
//import com.google.api.client.http.GenericUrl;
//import com.google.api.client.http.HttpResponse;
//import com.google.api.client.http.HttpTransport;
//import com.google.api.client.http.javanet.NetHttpTransport;
//import com.google.api.client.json.JsonFactory;
//import com.google.api.client.json.jackson2.JacksonFactory;
//import com.google.api.client.util.store.FileDataStoreFactory;
//import com.google.api.services.drive.Drive;
//import com.google.api.services.drive.DriveScopes;
//import com.google.api.services.drive.model.File;
//import com.google.api.services.drive.model.ParentReference;
//import com.google.api.services.drive.model.Revision;
//
//import managment.conflict.panels.SyncResolutionInterface;
//
public class DriveInterface {
//	private static final FileDataStoreFactory DATA_STORE_FACTORY; static {
//		FileDataStoreFactory fileFactory = null;
//		
//		try { fileFactory = new FileDataStoreFactory(FileStorage.ROOT_DIR); }
//		catch(IOException e) { e.printStackTrace(); }
//		
//		DATA_STORE_FACTORY = fileFactory;
//	}
//	
//	private static final String CLIENT_ID = "119216131413-bmva2stv4fcqrlgjr4054h30l2epb6qk.apps.googleusercontent.com";
//	private static final String CLIENT_SECRET = "Wkz8rRi1WUZHI-bdaCDyk9D2";
//
//	private static final List<String> SCOPES = Arrays.asList(DriveScopes.DRIVE_APPDATA);
//	
//	private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
//	private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
//	
	private static DriveInterface instance;
	public  static DriveInterface getInstance() {
		if(instance == null) 
			instance = new DriveInterface();
		return instance;
	} 
//	
////	---
//	
//	private Credential credential;
//	private Drive drive;
//	
//	private File file;
//	private String revisionId;
//	
	private DriveInterface() {
//		try { 
//			loadCredentails();
//			drive = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential).build();
//			findFile();
////			casheLatest();
//		} catch (IOException e) { e.printStackTrace(); }
	}
//	
//	private void loadCredentails() throws IOException {
//		AuthorizationCodeFlow authorization = new GoogleAuthorizationCodeFlow.Builder(
//				HTTP_TRANSPORT, JSON_FACTORY, CLIENT_ID, CLIENT_SECRET, SCOPES)
//			.setDataStoreFactory(DATA_STORE_FACTORY)
//			.setAccessType("offline")
//		.build();
//		
//		credential = new AuthorizationCodeInstalledApp(authorization, new LocalServerReceiver()).authorize(CLIENT_ID);
//	}
//	
//	private void findFile() throws IOException {
//		List<File> list = drive.files().list()
//				.setQ("title='" + FileStorage.FILE_NAME + "'")
//				.setFields("items(id, title, fileSize, headRevisionId)")
//				.setSpaces("appDataFolder")
//			.execute().getItems();
//		
////		for(File file : drive.files().list().setSpaces("appDataFolder").execute().getItems())
////			System.out.println(file.getTitle() + ": " + file.getId() + " : " + file.getFileSize());
//		
//		if(!list.isEmpty()) {
//			file = list.get(0);
//			
//		} else {
//			file = new File();
//			file.setTitle(FileStorage.FILE_NAME);
//			file.setMimeType("application/octet-stream");
//			file.setParents(Collections.singletonList(new ParentReference().setId("appDataFolder")));
//			
//			file = drive.files().insert(file).setFields("id").execute();
//		}
//	}
//	
//	public void syncToLocal() {
//		try(FileOutputStream out = new FileOutputStream(FileStorage.SAVE_FILE)) { 
//			file = drive.files().get(file.getId()).execute();
//			revisionId = file.getHeadRevisionId();
//			
//			HttpResponse resp = drive.getRequestFactory().buildGetRequest(new GenericUrl(file.getDownloadUrl())).execute();
//			out.getChannel().transferFrom(Channels.newChannel(resp.getContent()), 0, Long.MAX_VALUE);
//		} catch (IOException e) { e.printStackTrace(); }
//	}
//	
//	public void syncToDrive() {
//		try { 
//			String head = drive.files().get(file.getId()).setFields("headRevisionId").execute().getHeadRevisionId();
//			if(!head.equals(revisionId)) if(!nonConcurentVersions()) { syncToLocal(); return; }
//			
//			FileContent mediaContent = new FileContent("application/octet-stream", FileStorage.SAVE_FILE);
//			file = drive.files().update(file.getId(), file, mediaContent).execute();
//			revisionId = file.getHeadRevisionId();
//		} catch (IOException e) { e.printStackTrace(); }
//		
//		System.out.println("Synced!");
//	}
//	
//	private boolean nonConcurentVersions() {
//		SyncResolutionInterface syncRes = new SyncResolutionInterface();
//		syncRes.setVisible(true);
//		
//		return syncRes.shouldOverwrite();
//	}
//	
//	public ArrayList<Revision> getRevisions() {
//		try {
//			return new ArrayList<>(drive.revisions()
//					.list(file.getId())
//					.setFields("items(id, modifiedDate, downloadUrl)")
//			.execute().getItems());
//		} catch(IOException e) { e.printStackTrace(); }
//		
//		return new ArrayList<>();
//	}
//	
//	public java.io.File downloadRevision(Revision revision) {
//		try { 
//			java.io.File outFile = java.io.File.createTempFile("revision_" + revision.getId(), ".prjs");
//			FileOutputStream out = new FileOutputStream(outFile);
//			
//			HttpResponse resp = drive.getRequestFactory()
//					.buildGetRequest(new GenericUrl(revision.getDownloadUrl())).execute();
//			out.getChannel().transferFrom(Channels.newChannel(resp.getContent()), 0, Long.MAX_VALUE);
//			out.close();
//			
//			return outFile;
//		} catch (IOException e) { e.printStackTrace(); return null; }
//	}
//	
//	public void clearDriveFiles() {
//		try {
//			List<File> allFiles = drive.files().list()
//					.setFields("items(id, title)")
//					.setSpaces("appDataFolder")
//				.execute().getItems();
//			
//			for(File file : allFiles) drive.files().delete(file.getId()).execute();
//		} catch (IOException e) { e.printStackTrace(); }
//	}
}

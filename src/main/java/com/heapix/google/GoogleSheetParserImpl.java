package com.heapix.google;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetRequest;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.google.api.services.sheets.v4.model.ValueRange;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

public class GoogleSheetParserImpl implements GoogleSheetParser {

    private static final ResourceBundle bundle = ResourceBundle.getBundle(GoogleSheetKey.NAME);

    private static final String APPLICATION_NAME = bundle.getString(GoogleSheetKey.APPLICATION_NAME);
    private static final String CREDENTIAL_FOLDER = bundle.getString(GoogleSheetKey.CREDENTIALS_FOLDER);
    private static final String CLIENT_SECRETE_DIR = bundle.getString(GoogleSheetKey.CLIENT_SECRET_DIR);

    private static final String SPREADSHEET_ID = bundle.getString(GoogleSheetKey.SPREADSHEET_ID);

    private static final String USER_ID = bundle.getString(GoogleSheetKey.USER_ID);
    private static final String ACCESS_TYPE = bundle.getString(GoogleSheetKey.ACCESS_TYPE);

    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS);

    @Override
    public void parseTable() {

        try {

            List<List<Object>> values = getSheetValue();
            if (values == null || values.isEmpty()) {
                System.out.println("No data found.");
            } else {
                for (List row : values) {
                    row.set(0, "1");
                }
            }

        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<List<Object>> getSheetValue() throws IOException, GeneralSecurityException {

        Sheets sheets = buildSheets();
        ValueRange response = sheets.spreadsheets().values().get(SPREADSHEET_ID, "A3:A13").execute();
        response.getValues().get(0).set(0, "1");
        ValueRange valueRange = new ValueRange().setRange("A3:A13").setValues(response.getValues()).setMajorDimension("ROWS");


//        sheets.spreadsheets().create(new Spreadsheet()).execute();
        sheets.spreadsheets().values().update(SPREADSHEET_ID, "A3:A13", valueRange).setValueInputOption("RAW").execute();
        return response.getValues();
    }

    private Sheets buildSheets() throws IOException, GeneralSecurityException {

        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        return new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    private Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {

        InputStream in = GoogleSheetParserImpl.class.getResourceAsStream(CLIENT_SECRETE_DIR);
            GoogleClientSecrets secrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
        GoogleAuthorizationCodeFlow flow = buildCodeFlow(HTTP_TRANSPORT, secrets);
        return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize(USER_ID);
    }

    private GoogleAuthorizationCodeFlow buildCodeFlow(final NetHttpTransport HTTP_TRANSPORT, final GoogleClientSecrets SECRETS) throws IOException {


        return new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY, SECRETS, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new File(CREDENTIAL_FOLDER)))
                .setAccessType(ACCESS_TYPE)
                .build();
    }

}

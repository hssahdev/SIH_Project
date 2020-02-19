package com.xander.android.reportanalyzer.ui.home;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.xander.android.reportanalyzer.PatientInfo;
import com.xander.android.reportanalyzer.R;
import com.xander.android.reportanalyzer.ReportActivity;
import com.xander.android.reportanalyzer.ScoreCard;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    String currentPhotoPath;
    Gson gson;
    List<PatientInfo> reportHistory;
    SharedPreferences preferences;
    static final int REQUEST_TAKE_PHOTO = 1;
    Uri photo;

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
//            startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);

//             Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(getContext(),
                        "com.xander.android.reportanalyzer.fileprovider",
                        photoFile);
                photo = photoURI;
                Log.v("URI", photoURI.toString());
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        preferences = getContext().getSharedPreferences("pref", Context.MODE_PRIVATE);
        gson = new Gson();
        reportHistory = getReportHistory();
        Log.v("Report",reportHistory.toString());

        root.findViewById(R.id.capture).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });

        root.findViewById(R.id.importButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), ReportActivity.class));
            }
        });

        root.findViewById(R.id.score).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), ScoreCard.class));
            }
        });

        root.findViewById(R.id.scans).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://192.168.186.211:8042/app/explorer.html")));
            }
        });

        return root;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == Activity.RESULT_OK) {

            FirebaseVisionImage image = null;
            try {
                image = FirebaseVisionImage.fromFilePath(getContext(),photo);
            } catch (IOException e) {
                e.printStackTrace();
            }

            FirebaseVisionTextRecognizer detector = FirebaseVision.getInstance()
                    .getOnDeviceTextRecognizer();

            detector.processImage(image).addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                @Override
                public void onSuccess(FirebaseVisionText firebaseVisionText) {

                    if(!firebaseVisionText.getText().isEmpty()){
                        float vals[] = parseImageData(firebaseVisionText);
//                        String str = "Glycosylated Haemoglobin: "+vals[0]+
//                                "\nEstimated Average Glucose: "+vals[1]+
//                                "\nBUN: "+vals[2]+
//                                "\nCreatinine: "+vals[3];
                        PatientInfo patient = new PatientInfo(vals);
                        reportHistory.add(patient);
                        updateReportHistory(reportHistory);
                        Toast.makeText(getContext(), "Data extracted", Toast.LENGTH_SHORT).show();
                    }else
                        Toast.makeText(getContext(), "No text found", Toast.LENGTH_SHORT).show();
//                    Log.v("OCR",firebaseVisionText.getText());
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getContext(), "Failed", Toast.LENGTH_SHORT).show();

                    Log.v("OCR","Failed"+e.toString());
                }
            });
        }
    }

    private List<PatientInfo> getReportHistory(){
        Type type = new TypeToken<List<PatientInfo>>() {}.getType();
        List<PatientInfo> patientInfos = gson.fromJson(preferences.getString("patient_history",""),type);
        if (patientInfos==null)
            return new ArrayList<PatientInfo>();
        return patientInfos;
    }

    private void updateReportHistory(List<PatientInfo> patientInfos){
        preferences.edit().putString("patient_history",gson.toJson(patientInfos)).apply();
    }

    private float[] parseImageData(FirebaseVisionText result) {
        List<FirebaseVisionText.TextBlock> blocks = result.getTextBlocks();
        float[] vals =new float[4];
        int k=3;
        for(int i=blocks.size()-1;i>=0;i--){
            String text = blocks.get(i).getText();
            try{
                if(k>=0){
                    vals[k] = Float.parseFloat(text);
                    k--;
                }
            }catch (Exception e){

            }
        }

        for(int i=0;i<4;i++)
            Log.v("VALS",vals[i]+"");

        return vals;
//        for (FirebaseVisionText.TextBlock block: result.getTextBlocks()) {
//            String blockText = block.getText();
//            Log.v("BlockText",blockText);
//            Float blockConfidence = block.getConfidence();
//            List<RecognizedLanguage> blockLanguages = block.getRecognizedLanguages();
//            Point[] blockCornerPoints = block.getCornerPoints();
////            Log.v("BlockCorner",blockCornerPoints[0].toString());
//
//            Rect blockFrame = block.getBoundingBox();
//            for (FirebaseVisionText.Line line: block.getLines()) {
//                String lineText = line.getText();
//                Float lineConfidence = line.getConfidence();
//                List<RecognizedLanguage> lineLanguages = line.getRecognizedLanguages();
//                Point[] lineCornerPoints = line.getCornerPoints();
//                Rect lineFrame = line.getBoundingBox();
//                for (FirebaseVisionText.Element element: line.getElements()) {
//                    String elementText = element.getText();
//                    Float elementConfidence = element.getConfidence();
//                    List<RecognizedLanguage> elementLanguages = element.getRecognizedLanguages();
//                    Point[] elementCornerPoints = element.getCornerPoints();
//                    Rect elementFrame = element.getBoundingBox();
//                }
//            }
//        }
    }


    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

}
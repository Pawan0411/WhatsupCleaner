package com.pawan.whatsAppCleaner.tabs;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.format.Formatter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.pawan.whatsAppCleaner.DataHolder;
import com.pawan.whatsAppCleaner.R;
import com.pawan.whatsAppCleaner.adapters.innerAdapeters.InnerDetailsAdapter;
import com.pawan.whatsAppCleaner.datas.FileDetails;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class FilesFragment extends Fragment implements InnerDetailsAdapter.OnCheckboxListener {

    private Button button, date, name, size;
    private TextView no_ads;
    private ImageView no_files;
    private InnerDetailsAdapter innerDetailsAdapter;
    private ArrayList<FileDetails> innerDataList = new ArrayList<>();
    private ArrayList<FileDetails> filesToDelete = new ArrayList<>();
    private ProgressDialog progressDialog;
    private AdView mAdView;
    private CheckBox selectall;
    private boolean flag_d = true, flag_n = true, flag_s = true;

    private FirebaseAnalytics mFirebaseAnalytics;

    public static FilesFragment newInstance(String category, String path) {
        FilesFragment filesFragment = new FilesFragment();
        Bundle bundle = new Bundle();
        bundle.putString("path", path);
        bundle.putString("category", category);
        filesFragment.setArguments(bundle);
        return filesFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView;
        String category = null;
        String path = null;

        if (getArguments() != null) {
            path = getArguments().getString("path");
            category = getArguments().getString("category");
        } else {
            Toast.makeText(getContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
            if (getActivity() != null)
                getActivity().finish();
        }

        if (category == null) {
            if (getActivity() != null)
                getActivity().finish();
            return null;
        }

        RecyclerView recyclerView;
        int IMAGES = 1;
        int VIDEOS = 2;
        int AUDIOS = 3;
        int FILE = 4;
        int VOICE = 6;
        switch (category) {
            default:
            case DataHolder.WALLPAPER:
            case DataHolder.IMAGE:
                rootView = inflater.inflate(R.layout.image_activity, container, false);
                recyclerView = rootView.findViewById(R.id.recycler_view);
                recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
                innerDetailsAdapter = new InnerDetailsAdapter(IMAGES, getActivity(), innerDataList, this);
                break;
            case DataHolder.GIF:
            case DataHolder.VIDEO:
                rootView = inflater.inflate(R.layout.image_activity, container, false);
                recyclerView = rootView.findViewById(R.id.recycler_view);
                recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
                innerDetailsAdapter = new InnerDetailsAdapter(VIDEOS, getActivity(), innerDataList, this);
                break;
            case DataHolder.VOICE:
                rootView = inflater.inflate(R.layout.doc_activity, container, false);
                recyclerView = rootView.findViewById(R.id.recycler_view);
                recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                innerDetailsAdapter = new InnerDetailsAdapter(VOICE, getActivity(), innerDataList, this);
                break;
            case DataHolder.AUDIO:
                rootView = inflater.inflate(R.layout.doc_activity, container, false);
                recyclerView = rootView.findViewById(R.id.recycler_view);
                recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                innerDetailsAdapter = new InnerDetailsAdapter(AUDIOS, getActivity(), innerDataList, this);
                break;
            case DataHolder.DOCUMENT:
                rootView = inflater.inflate(R.layout.doc_activity, container, false);
                recyclerView = rootView.findViewById(R.id.recycler_view);
                recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                innerDetailsAdapter = new InnerDetailsAdapter(FILE, getActivity(), innerDataList, this);
                break;
        }

        //Switch toggle = rootView.findViewById(R.id.switch1);
        no_ads = rootView.findViewById(R.id.ads_not_loaded);

        button = rootView.findViewById(R.id.delete);
        date = rootView.findViewById(R.id.date);
        name = rootView.findViewById(R.id.name);
        size = rootView.findViewById(R.id.size);
        no_files = rootView.findViewById(R.id.nofiles);
        mAdView = rootView.findViewById(R.id.adView);
        selectall = rootView.findViewById(R.id.selectall);

        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(innerDetailsAdapter);

        SharedPreferences sharedPreferences = Objects.requireNonNull(getContext()).getSharedPreferences("Network", 0);
        boolean status = sharedPreferences.getBoolean("Status", false);

        if (status) {
            no_ads.setVisibility(View.VISIBLE);
            no_ads.setText(R.string.no_ads);


            mAdView.loadAd(new AdRequest.Builder().addTestDevice("623B1B7759D51209294A77125459D9B7").build());

            mAdView.setAdListener(new AdListener() {
                @Override
                public void onAdClosed() {
                    if (!mAdView.isLoading()) {
                        mAdView.loadAd(new AdRequest.Builder().addTestDevice("623B1B7759D51209294A77125459D9B7").build());
                    }
                }

                @Override
                public void onAdLoaded() {
                    Log.e("Banner", "Loaded");
                    no_ads.setVisibility(View.INVISIBLE);
                }

                @Override
                public void onAdFailedToLoad(int i) {
                    Log.e("Bannercode", String.valueOf(i));
                }
            });
        } else {
            no_ads.setVisibility(View.VISIBLE);
            no_ads.setText(R.string.no_ads);
        }

        Log.e("TEST", "" + path);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getContext());

        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, String.valueOf(recyclerView));
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, String.valueOf(recyclerView));
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, String.valueOf(button));
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, String.valueOf(button));
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "name");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

        new FetchFiles(this, category, new FetchFiles.OnFilesFetched() {
            @Override
            public void onFilesFetched(List<FileDetails> fileDetails) {
                if (fileDetails != null && !fileDetails.isEmpty()) {
                    innerDataList.addAll(fileDetails);
                    innerDetailsAdapter.notifyDataSetChanged();
                    progressDialog.dismiss();
                    no_files.setVisibility(View.INVISIBLE);
                } else {
                    progressDialog.dismiss();
                    Log.e("Nofiles", "NO Files Found");
                    no_files.setVisibility(View.VISIBLE);
                    no_files.setImageResource(R.drawable.file);
                }
            }
        }).execute(path);

//        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                if (isChecked) {
//                    Collections.reverse(innerDataList);
//                    Log.e("State", "Toggled");
//                    innerDetailsAdapter.notifyDataSetChanged();
//                } else {
//                    Collections.reverse(innerDataList);
//                    Log.e("State", "Disabled");
//                    innerDetailsAdapter.notifyDataSetChanged();
//                }
//            }
//        });


        date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!flag_n || !flag_s) {
                    flag_n = true;
                    flag_s = true;
                    name.setTextColor(Color.parseColor("#FF161616"));
                    size.setTextColor(Color.parseColor("#FF161616"));
                }
                if (flag_d) {
                    Toast.makeText(getContext(), "sorted", Toast.LENGTH_SHORT).show();
                    flag_d = false;
                    date.setTextColor(Color.parseColor("#C103A9F4"));
                    Collections.sort(innerDataList, new Comparator<FileDetails>() {
                        @Override
                        public int compare(FileDetails o1, FileDetails o2) {
                            return -o1.getMod().compareTo(o2.getMod());
                        }
                    });
                    innerDetailsAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(getContext(), "Unsorted", Toast.LENGTH_SHORT).show();
                    flag_d = true;
                    date.setTextColor(Color.parseColor("#FF161616"));
                    Collections.sort(innerDataList, new Comparator<FileDetails>() {
                        @Override
                        public int compare(FileDetails o1, FileDetails o2) {
                            return o1.getMod().compareTo(o2.getMod());
                        }
                    });
                    Log.e("State", "Disabled");
                    innerDetailsAdapter.notifyDataSetChanged();
                }
            }
        });

        name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!flag_d || !flag_s) {
                    flag_d = true;
                    flag_s = true;
                    date.setTextColor(Color.parseColor("#FF161616"));
                    size.setTextColor(Color.parseColor("#FF161616"));
                }
                if (flag_n) {
                    Toast.makeText(getContext(), "sorted", Toast.LENGTH_SHORT).show();
                    flag_n = false;
                    name.setTextColor(Color.parseColor("#C103A9F4"));
                    Collections.sort(innerDataList, new Comparator<FileDetails>() {
                        @Override
                        public int compare(FileDetails o1, FileDetails o2) {
                            return o1.getName().compareTo(o2.getName());
                        }
                    });
                    Log.e("State", "Toggled");
                    innerDetailsAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(getContext(), "Unsorted", Toast.LENGTH_SHORT).show();
                    flag_n = true;
                    name.setTextColor(Color.parseColor("#FF161616"));
                    Collections.sort(innerDataList, new Comparator<FileDetails>() {
                        @Override
                        public int compare(FileDetails o1, FileDetails o2) {
                            return -o1.getName().compareTo(o2.getName());
                        }
                    });
                    Log.e("State", "Disabled");
                    innerDetailsAdapter.notifyDataSetChanged();
                }
            }
        });
        size.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!flag_d || !flag_n) {
                    flag_d = true;
                    flag_n = true;
                    date.setTextColor(Color.parseColor("#FF161616"));
                    name.setTextColor(Color.parseColor("#FF161616"));
                }
                if (flag_s) {
                    Toast.makeText(getContext(), "sorted", Toast.LENGTH_SHORT).show();
                    flag_s = false;
                    size.setTextColor(Color.parseColor("#C103A9F4"));
                    Collections.sort(innerDataList, new Comparator<FileDetails>() {
                        @Override
                        public int compare(FileDetails o1, FileDetails o2) {
                            return -o1.getS().compareTo(o2.getS());
                        }
                    });
                    Log.e("State", "Toggled");
                    innerDetailsAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(getContext(), "Unsorted", Toast.LENGTH_SHORT).show();
                    flag_s = true;
                    size.setTextColor(Color.parseColor("#FF161616"));
                    Collections.sort(innerDataList, new Comparator<FileDetails>() {
                        @Override
                        public int compare(FileDetails o1, FileDetails o2) {
                            return o1.getS().compareTo(o2.getS());
                        }
                    });
                    Log.e("State", "Disabled");
                    innerDetailsAdapter.notifyDataSetChanged();
                }
            }
        });

        selectall.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    for (int i = 0; i < innerDataList.size(); i++) {
                        innerDataList.get(i).setSelected(true);
                    }
                    innerDetailsAdapter.notifyDataSetChanged();
                } else {
                    for (int i = 0; i < innerDataList.size(); i++) {
                        innerDataList.get(i).setSelected(false);
                    }
                    innerDetailsAdapter.notifyDataSetChanged();
                }
            }
        });
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!filesToDelete.isEmpty() && filesToDelete != null) {
                    int success = -1;
                    ArrayList<FileDetails> deletedFiles = new ArrayList<>();

                    for (FileDetails details : filesToDelete) {
                        File file = new File(details.getPath());
                        if (file.exists()) {
                            if (file.delete()) {
                                deletedFiles.add(details);
                                if (success == 0) {
                                    return;
                                }
                                success = 1;
                            } else {
                                Log.e("TEST", "" + file.getName() + " delete failed");
                                success = 0;
                            }
                        } else {
                            Log.e("TEST", "" + file.getName() + " doesn't exists");
                            success = 0;
                        }
                    }

                    filesToDelete.clear();

                    for (FileDetails deletedFile : deletedFiles) {
                        innerDataList.remove(deletedFile);
                    }
                    innerDetailsAdapter.notifyDataSetChanged();
                    if (success == 0) {
                        Toast.makeText(getContext(), "Couldn't delete some files", Toast.LENGTH_SHORT).show();
                    } else if (success == 1) {
                        Toast.makeText(getContext(), "Deleted successfully", Toast.LENGTH_SHORT).show();
                    }
                    button.setText(R.string.delete_items_blank);
                    button.setTextColor(Color.parseColor("#A9A9A9"));
                }
            }
        });

        return rootView;
    }

    private static long getFileSize(File file) {
        if (file != null && file.isFile()) {
            return file.length();
        }
        return 0;
    }

    @Override
    public void oncheckboxlistener(View view, List<FileDetails> updatedFiles) {

        filesToDelete.clear();

        for (FileDetails details : updatedFiles) {
            if (details.isSelected()) {
                filesToDelete.add(details);
            }
        }

        if (filesToDelete.size() > 0) {

            long totalFileSize = 0;

            for (FileDetails details : filesToDelete) {
                File file = new File(details.getPath());
                totalFileSize += file.length();
            }

            String size = Formatter.formatShortFileSize(getActivity(), totalFileSize);
            button.setText("Delete Selected Items (" + size + ")");
            button.setTextColor(Color.parseColor("#C103A9F4"));
        } else {
            button.setText(R.string.delete_items_blank);
            button.setTextColor(Color.parseColor("#A9A9A9"));
        }
    }

    private static class FetchFiles extends AsyncTask<String, Void, Object> {

        private String category;
        private OnFilesFetched onFilesFetched;
        private WeakReference<FilesFragment> filesFragmentWeakReference;

        FetchFiles(FilesFragment filesFragment, String category, OnFilesFetched onFilesFetched) {
            filesFragmentWeakReference = new WeakReference<>(filesFragment);
            this.category = category;
            this.onFilesFetched = onFilesFetched;
        }

        @Override
        protected void onPreExecute() {
            // display a progress dialog for good user experiance
            filesFragmentWeakReference.get().progressDialog = new ProgressDialog(filesFragmentWeakReference.get().getContext());
            filesFragmentWeakReference.get().progressDialog.setMessage("Please Wait");
            filesFragmentWeakReference.get().progressDialog.setCancelable(false);
            if (!filesFragmentWeakReference.get().progressDialog.isShowing()) {
                filesFragmentWeakReference.get().progressDialog.show();
            }
        }

        @Override
        protected Object doInBackground(String... strings) {

            String path = strings[0];
            String extension;
            ArrayList<FileDetails> files = new ArrayList<>();

            if (path != null) {
                File directory = new File(path);
                File[] results = directory.listFiles();

                if (results != null) {
                    //Still verify if the file is an image in whatsapp preferred format(jpg)
                    for (final File file : results) {
                        switch (category) {
                            case DataHolder.IMAGE:
                                if (file.isFile()) {
                                    if (!file.getName().endsWith(".nomedia")) {
                                        FileDetails fileDetails = new FileDetails();
                                        fileDetails.setName(file.getName());
                                        fileDetails.setPath(file.getPath());
                                        fileDetails.setMod(file.lastModified());
                                        String mime = "*/*";
                                        File a = new File(file.getPath());
                                        Uri uri = FileProvider.getUriForFile(Objects.requireNonNull(filesFragmentWeakReference.get().getContext()),
                                                Objects.requireNonNull(filesFragmentWeakReference.get().getContext()).getApplicationContext().getPackageName() +
                                                        ".my.package.name.provider", a);
                                        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
                                        if (mimeTypeMap.hasExtension(
                                                MimeTypeMap.getFileExtensionFromUrl(uri.toString()))) {

                                            mime = Objects.requireNonNull(mimeTypeMap.getMimeTypeFromExtension(
                                                    MimeTypeMap.getFileExtensionFromUrl(uri.toString()))).split("/")[0];
                                        }

                                        fileDetails.setSize(Formatter.formatShortFileSize(filesFragmentWeakReference.get().
                                                        getContext(),
                                                getFileSize(file)));
                                        fileDetails.setS(getFileSize(file));
                                        files.add(fileDetails);
                                    }
                                }
                                break;
                            case DataHolder.DOCUMENT:
                                //Check if it is a file or a folder
                                if (file.isFile()) {
                                    if (!file.getName().endsWith(".nomedia")) {
                                        FileDetails fileDetails = new FileDetails();
                                        fileDetails.setName(file.getName());
                                        fileDetails.setPath(file.getPath());
                                        fileDetails.setImage(R.drawable.ic_doc);
                                        extension = FilenameUtils.getExtension((file.getPath()));
                                        fileDetails.setMod(file.lastModified());
                                        String mime = "*/*";
                                        File a = new File(file.getPath());
                                        Uri uri = FileProvider.getUriForFile(Objects.requireNonNull(filesFragmentWeakReference.get().getContext()),
                                                Objects.requireNonNull(filesFragmentWeakReference.get().getContext()).getApplicationContext().getPackageName() +
                                                        ".my.package.name.provider", a);
                                        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
                                        if (mimeTypeMap.hasExtension(
                                                MimeTypeMap.getFileExtensionFromUrl(uri.toString()))) {

                                            mime = Objects.requireNonNull(mimeTypeMap.getMimeTypeFromExtension(
                                                    MimeTypeMap.getFileExtensionFromUrl(uri.toString()))).split("/")[0];
                                        }
                                        switch (mime) {
                                            case "image":
                                                fileDetails.setColor(R.color.green);
                                                fileDetails.setExt(extension);
                                                break;
                                            case "video":
                                                fileDetails.setColor(R.color.blue);
                                                fileDetails.setExt(extension);
                                                break;
                                            case "text":
                                                fileDetails.setColor(R.color.orange);
                                                fileDetails.setExt(extension);
                                                break;
                                            case "application":
                                                fileDetails.setColor(R.color.red);
                                                fileDetails.setExt(extension);
                                                break;
                                            case "audio":
                                                fileDetails.setColor(R.color.purple);
                                                fileDetails.setExt(extension);
                                                break;
                                            default:
                                                fileDetails.setColor(R.color.gray);
                                                fileDetails.setImage(R.drawable.ic_unkown);
                                                break;
                                        }

                                        fileDetails.setSize(Formatter.formatShortFileSize(filesFragmentWeakReference.get().getContext(), getFileSize(file)));
                                        fileDetails.setS(getFileSize(file));
                                        files.add(fileDetails);
                                    }
                                }
                                break;
                            case DataHolder.VIDEO:
                                if (file.isFile()) {
                                    if (!file.getName().endsWith(".nomedia")) {
                                        FileDetails fileDetails = new FileDetails();
                                        fileDetails.setName(file.getName());
                                        fileDetails.setPath(file.getPath());
                                        fileDetails.setImage(R.drawable.video_play);
                                        fileDetails.setColor(R.color.transparent);
                                        fileDetails.setMod(file.lastModified());
                                        fileDetails.setSize(Formatter.formatShortFileSize(filesFragmentWeakReference.get()
                                                        .getContext(),
                                                getFileSize(file)));
                                        fileDetails.setS(getFileSize(file));
                                        files.add(fileDetails);
                                    }
                                }
                                break;
                            case DataHolder.AUDIO:
                                if (file.isFile()) {
                                    if (!file.getName().endsWith(".nomedia")) {
                                        FileDetails fileDetails = new FileDetails();
                                        fileDetails.setName(file.getName());
                                        fileDetails.setPath(file.getPath());
                                        fileDetails.setMod(file.lastModified());
                                        fileDetails.setImage(R.drawable.ic_audio);
                                        extension = FilenameUtils.getExtension((file.getPath()));
                                        String mime = "*/*";
                                        File a = new File(file.getPath());
                                        Uri uri = FileProvider.getUriForFile(Objects.requireNonNull(filesFragmentWeakReference.get().getContext()),
                                                Objects.requireNonNull(filesFragmentWeakReference.get().getContext()).getApplicationContext().getPackageName() +
                                                        ".my.package.name.provider", a);
                                        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
                                        if (mimeTypeMap.hasExtension(
                                                MimeTypeMap.getFileExtensionFromUrl(uri.toString()))) {

                                            mime = Objects.requireNonNull(mimeTypeMap.getMimeTypeFromExtension(
                                                    MimeTypeMap.getFileExtensionFromUrl(uri.toString()))).split("/")[0];
                                        }
                                        switch (mime) {
                                            case "image":
                                                fileDetails.setColor(R.color.green);
                                                fileDetails.setExt(extension);
                                                break;
                                            case "video":
                                                fileDetails.setColor(R.color.blue);
                                                fileDetails.setExt(extension);
                                                break;
                                            case "text":
                                                fileDetails.setColor(R.color.orange);
                                                fileDetails.setExt(extension);
                                                break;
                                            case "application":
                                                fileDetails.setColor(R.color.red);
                                                fileDetails.setExt(extension);
                                                break;
                                            case "audio":
                                                fileDetails.setColor(R.color.purple);
                                                fileDetails.setExt(extension);
                                                break;
                                            default:
                                                fileDetails.setColor(R.color.gray);
                                                fileDetails.setImage(R.drawable.ic_unkown);
                                                break;
                                        }
                                        fileDetails.setSize(Formatter.formatShortFileSize(filesFragmentWeakReference.get()
                                                        .getContext(),
                                                getFileSize(file)));
                                        fileDetails.setS(getFileSize(file));
                                        files.add(fileDetails);
                                    }
                                }
                                break;
                            case DataHolder.GIF:
                                if (file.isFile()) {
                                    if (!file.getName().endsWith(".nomedia")) {
                                        FileDetails fileDetails = new FileDetails();
                                        fileDetails.setName(file.getName());
                                        fileDetails.setPath(file.getPath());
                                        fileDetails.setMod(file.lastModified());
                                        fileDetails.setImage(R.drawable.video_play);
                                        fileDetails.setColor(R.color.transparent);
                                        fileDetails.setSize(Formatter.formatShortFileSize(filesFragmentWeakReference.get().getContext(),
                                                getFileSize(file)));
                                        fileDetails.setS(getFileSize(file));
                                        files.add(fileDetails);
                                    }
                                }
                                break;
                            case DataHolder.WALLPAPER:
                                if (file.isFile()) {
                                    if (!file.getName().endsWith(".nomedia")) {
                                        FileDetails fileDetails = new FileDetails();
                                        fileDetails.setName(file.getName());
                                        fileDetails.setPath(file.getPath());
                                        fileDetails.setMod(file.lastModified());
                                        fileDetails.setSize(Formatter.formatShortFileSize(filesFragmentWeakReference.get().getContext(),
                                                getFileSize(file)));
                                        fileDetails.setS(getFileSize(file));
                                        files.add(fileDetails);
                                    }
                                }
                                break;
                            case DataHolder.VOICE:
                                if (file.isDirectory()) {
                                    if (!file.getName().equals("Sent")) {
                                        File[] res = file.listFiles();
                                        for (File re : res) {
                                            if (!re.getName().endsWith(".nomedia")) {
                                                FileDetails fileDetails = new FileDetails();
                                                fileDetails.setName(re.getName());
                                                fileDetails.setPath(re.getPath());
                                                fileDetails.setMod(re.lastModified());
                                                fileDetails.setImage(R.drawable.voice);
                                                fileDetails.setColor(R.color.orange);
                                                extension = FilenameUtils.getExtension((re.getPath()));

                                                String mime = "*/*";
                                                File a = new File(re.getPath());
                                                Uri uri = FileProvider.getUriForFile(Objects.requireNonNull(filesFragmentWeakReference.get().getContext()),
                                                        Objects.requireNonNull(filesFragmentWeakReference.get().getContext()).getApplicationContext().getPackageName() +
                                                                ".my.package.name.provider", a);
                                                MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
                                                if (mimeTypeMap.hasExtension(
                                                        MimeTypeMap.getFileExtensionFromUrl(uri.toString()))) {

                                                    mime = Objects.requireNonNull(mimeTypeMap.getMimeTypeFromExtension(
                                                            MimeTypeMap.getFileExtensionFromUrl(uri.toString()))).split("/")[0];
                                                }
                                                switch (mime) {
                                                    case "image":
                                                        fileDetails.setColor(R.color.green);
                                                        fileDetails.setExt(extension);
                                                        break;
                                                    case "video":
                                                        fileDetails.setColor(R.color.blue);
                                                        fileDetails.setExt(extension);
                                                        break;
                                                    case "text":
                                                        fileDetails.setColor(R.color.orange);
                                                        fileDetails.setExt(extension);
                                                        break;
                                                    case "application":
                                                        fileDetails.setColor(R.color.red);
                                                        fileDetails.setExt(extension);
                                                        break;
                                                    case "audio":
                                                        fileDetails.setColor(R.color.purple);
                                                        fileDetails.setExt(extension);
                                                        break;
                                                    default:
                                                        fileDetails.setColor(R.color.gray);
                                                        fileDetails.setImage(R.drawable.ic_unkown);
                                                        break;
                                                }
                                                fileDetails.setSize(Formatter.formatShortFileSize(filesFragmentWeakReference.get().getContext(),
                                                        getFileSize(re)));
                                                fileDetails.setS(getFileSize(re));
                                                Log.e("size", String.valueOf(getFileSize(re)));
                                                files.add(fileDetails);
                                            }
                                        }
                                    }
                                }
                            case DataHolder.STATUS:
                                if (file.isFile()) {
                                    if (!file.getName().endsWith(".nomedia")) {
                                        FileDetails fileDetails = new FileDetails();
                                        fileDetails.setName(file.getName());
                                        fileDetails.setPath(file.getPath());
                                        fileDetails.setMod(file.lastModified());
                                        String mime = "*/*";
                                        File a = new File(file.getPath());
                                        Uri uri = FileProvider.getUriForFile(Objects.requireNonNull(filesFragmentWeakReference.get().getContext()),
                                                Objects.requireNonNull(filesFragmentWeakReference.get().getContext()).getApplicationContext().getPackageName() +
                                                        ".my.package.name.provider", a);
                                        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
                                        if (mimeTypeMap.hasExtension(
                                                MimeTypeMap.getFileExtensionFromUrl(uri.toString()))) {

                                            mime = Objects.requireNonNull(mimeTypeMap.getMimeTypeFromExtension(
                                                    MimeTypeMap.getFileExtensionFromUrl(uri.toString()))).split("/")[0];
                                        }

                                        fileDetails.setSize(Formatter.formatShortFileSize(filesFragmentWeakReference.get().
                                                        getContext(),
                                                getFileSize(file)));
                                        fileDetails.setS(getFileSize(file));
                                        files.add(fileDetails);
                                    }
                                }

                        }
                    }
                } else {
                    Log.e("Files", "No files found in " + directory.getName());
                }
            } else {
                Log.e("Files", "Path is empty");
            }
            //filesFragmentWeakReference.get().flag_d = false;
            return files;
        }


        @Override
        protected void onPostExecute(Object o) {
            List<FileDetails> files = (List<FileDetails>) o;
            if (onFilesFetched != null) {
                onFilesFetched.onFilesFetched(files);
            }
        }

        public interface OnFilesFetched {
            void onFilesFetched(List<FileDetails> fileDetails);
        }

    }


}
package com.ydkim2110.drinkshopadminapp.Utils;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;

/**
 * Created by Kim Yongdae on 2018-12-08
 * email : ydkim2110@gmail.com
 */
public class ProgressRequestBody extends RequestBody {

    private static final String TAG = "ProgressRequestBody";

    private File mFile;
    private static final int DEFAULT_BUFFER_SIZE = 4096;

    private UploadCallBack mUploadCallBack;

    public ProgressRequestBody(File file, UploadCallBack uploadCallBack) {
        mFile = file;
        mUploadCallBack = uploadCallBack;
    }

    @Override
    public long contentLength() throws IOException {
        Log.d(TAG, "contentLength: called");
        return mFile.length();
    }

    @Nullable
    @Override
    public MediaType contentType() {
        return MediaType.parse("image/*");
    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        Log.d(TAG, "writeTo: called");
        long fileLength = mFile.length();
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        FileInputStream in = new FileInputStream(mFile);
        long uploaded = 0;

        try {
            int read;
            Handler handler = new Handler(Looper.getMainLooper());
            while ((read = in.read(buffer)) != -1) {
                handler.post(new ProgressUpdater(uploaded, fileLength));
                uploaded += read;
                sink.write(buffer, 0, read);
            }
        } finally {
            in.close();
        }
    }

    private class ProgressUpdater implements Runnable {

        private static final String TAG = "ProgressUpdater";

        private long uploaded, fileLength;
        public ProgressUpdater(long uploaded, long fileLength) {
            this.uploaded = uploaded;
            this.fileLength = fileLength;
        }

        @Override
        public void run() {
            mUploadCallBack.onProgressUpdate((int)(100*uploaded/fileLength));
        }
    }
}

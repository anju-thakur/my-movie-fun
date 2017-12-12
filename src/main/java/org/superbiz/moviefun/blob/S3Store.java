package org.superbiz.moviefun.blob;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import org.apache.tika.Tika;
import org.apache.tika.io.IOUtils;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Optional;


public class S3Store implements BlobStore {

    private final AmazonS3Client s3Client;
    private final String s3BucketName;
    public S3Store(AmazonS3Client s3Client, String s3BucketName) {
        this.s3Client = s3Client;
        this.s3BucketName = s3BucketName;

        System.out.println(s3BucketName);
        System.out.println(s3Client);
    }

    @Override
    public void put (Blob objBlob) throws IOException{

        s3Client.putObject(s3BucketName,objBlob.getName(),objBlob.getInputStream(),new ObjectMetadata());

    }

    @Override
    public Optional<Blob> get(String name) throws IOException{

        if(!s3Client.doesObjectExist(s3BucketName,name))
        {
            return Optional.empty();
    }
        else {
            try(S3Object obj = s3Client.getObject(s3BucketName, name)) {
                InputStream s3Stream = obj.getObjectContent();
                byte[] bytes = IOUtils.toByteArray(s3Stream);
                String contentType = new Tika().detect(bytes);
                Blob img = new Blob(name, new ByteArrayInputStream(bytes), contentType);


                return Optional.of(img);
            }
        }
    }

    @Override
    public void deleteAll(){
        ObjectListing object_listing = s3Client.listObjects(s3BucketName);
        while (true) {
            for (Iterator<?> iterator =
                 object_listing.getObjectSummaries().iterator();
                 iterator.hasNext();) {
                S3ObjectSummary summary = (S3ObjectSummary)iterator.next();
                s3Client.deleteObject(s3BucketName, summary.getKey());
            }

            // more object_listing to retrieve?
            if (object_listing.isTruncated()) {
                object_listing = s3Client.listNextBatchOfObjects(object_listing);
            } else {
                break;
            }
        };
    }
}

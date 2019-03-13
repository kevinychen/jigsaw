package com.kyc.jigsaw.server;

import org.glassfish.jersey.media.multipart.MultiPartFeature;

import com.kyc.jigsaw.BulkPieceParser;
import com.kyc.jigsaw.PieceParser;
import com.kyc.jigsaw.PieceTester;
import com.kyc.jigsaw.classifier.Classifier;

import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.setup.Environment;

public class JigsawServer extends Application<Configuration> {

    public static void main(String[] args) throws Exception {
        new JigsawServer().run("server");
    }

    @Override
    public void run(Configuration configuration, Environment environment) throws Exception {
        Classifier classifier = Classifier.getDefault();
        PieceParser parser = new PieceParser(classifier);
        BulkPieceParser bulkParser = new BulkPieceParser(classifier);
        PieceTester tester = new PieceTester();

        environment.jersey().register(new MultiPartFeature());
        environment.jersey().register(new JigsawResource(parser, bulkParser, tester));
    }
}

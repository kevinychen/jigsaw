package com.kyc.jigsaw;

import org.glassfish.jersey.media.multipart.MultiPartFeature;

import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.setup.Environment;

public class JigsawServer extends Application<Configuration> {

    public static void main(String[] args) throws Exception {
        new JigsawServer().run("server");
    }

    @Override
    public void run(Configuration configuration, Environment environment) throws Exception {
        environment.jersey().register(new MultiPartFeature());
        environment.jersey().register(new JigsawResource());
    }
}

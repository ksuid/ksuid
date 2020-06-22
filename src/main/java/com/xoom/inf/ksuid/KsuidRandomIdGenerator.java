package com.xoom.inf.ksuid;

import com.xoom.inf.boot.util.RandomIdGenerator;

import java.security.SecureRandom;

/**
 * RandomIdGenerator that produces KSUID.
 * 
 * Note that caller needs to depend on xoom-boot-core maven module, if such (transitive) dependency does not exist already.
 */
public class KsuidRandomIdGenerator implements RandomIdGenerator {

    private static final KsuidRandomIdGenerator INSTANCE = new KsuidRandomIdGenerator(new KsuidGenerator(new SecureRandom()));

    private KsuidGenerator ksuidGenerator;

    public static String generateKsuid() {
        return INSTANCE.generate();
    }

    public KsuidRandomIdGenerator(KsuidGenerator ksuidGenerator) {
        this.ksuidGenerator = ksuidGenerator;
    }

    @Override
    public String generate() {
        return ksuidGenerator.newKsuid().asString();
    }

}
package com.xoom.inf.ksuid;

import org.junit.Before;
import org.junit.Test;

import java.security.SecureRandom;

import static com.xoom.inf.ksuid.KsuidRandomIdGenerator.generateKsuid;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

public class KsuidRandomIdGeneratorTest {

    private final KsuidGenerator ksuidGenerator = spy(new KsuidGenerator(new SecureRandom()));
    private final String exampleKsuid = ksuidGenerator.newKsuid().asString();

    @Before
    public void setUp() {
        reset(ksuidGenerator);
    }

    @Test
    public void generateKsuidProducesSameSizeId() {
        final String id = generateKsuid();
        assertThat(id).hasSameSizeAs(exampleKsuid);
    }

    @Test
    public void generateProducesSameId() {
        final Ksuid ksuid = ksuidGenerator.newKsuid();
        doReturn(ksuid).when(ksuidGenerator).newKsuid();

        final KsuidRandomIdGenerator randomIdGenerator = new KsuidRandomIdGenerator(ksuidGenerator);
        final String id = randomIdGenerator.generate();

        assertThat(id).isEqualTo(ksuid.asString());
        verify(ksuidGenerator, atLeastOnce()).newKsuid();
    }
}

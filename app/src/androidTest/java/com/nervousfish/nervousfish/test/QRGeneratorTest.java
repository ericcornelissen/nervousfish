package com.nervousfish.nervousfish.test;

import android.graphics.Bitmap;
import android.support.test.runner.AndroidJUnit4;

import com.nervousfish.nervousfish.data_objects.IKey;
import com.nervousfish.nervousfish.data_objects.KeyPair;
import com.nervousfish.nervousfish.modules.cryptography.IKeyGenerator;
import com.nervousfish.nervousfish.modules.qr.QRGenerator;
import com.nervousfish.nervousfish.service_locator.NervousFish;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

@RunWith(AndroidJUnit4.class)
public class QRGeneratorTest {

    private static final int QRCODE_IMAGE_HEIGHT = 400;
    private static final int QRCODE_IMAGE_WIDTH = 400;
    private IKey publicKey;
    private String publicKeyString;

    @Before
    public void setUp() throws Exception {
        final IKeyGenerator keyGenerator = NervousFish.getServiceLocator().getKeyGenerator();
        final KeyPair pair = keyGenerator.generateRSAKeyPair("test");
        this.publicKey = pair.getPublicKey();
        final String space = " ";
        this.publicKeyString = this.publicKey.getType() + space + this.publicKey.getName()
                + space + this.publicKey.getKey();
    }

    @Test
    public void testEncodeNotNull() throws Exception {
        final Bitmap qrCode = QRGenerator.encode(this.publicKeyString);
        assertNotNull(qrCode);
    }

    @Test
    public void testDeconstructAsExpected() throws Exception {
        IKey keyTest = QRGenerator.deconstructToKey(this.publicKeyString);
        assertEquals(this.publicKey, keyTest);
    }

    @Test
    public void testWidthIsAsExpected() throws Exception {
        final Bitmap QRcode = QRGenerator.encode(this.publicKeyString);
        assertEquals(QRCODE_IMAGE_WIDTH, QRcode.getWidth());
    }

    @Test
    public void testHeightIsAsExpected() throws Exception {
        final Bitmap QRcode = QRGenerator.encode(this.publicKeyString);
        assertEquals(QRCODE_IMAGE_HEIGHT, QRcode.getHeight());
    }

}
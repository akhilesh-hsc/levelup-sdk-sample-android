/*
 * Copyright (C) 2014 SCVNGR, Inc. d/b/a LevelUp
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.example.levelup.core.app.qr;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.SystemClock;
import android.support.annotation.Nullable;

import com.example.levelup.core.app.Constants;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.google.zxing.qrcode.encoder.Encoder;
import com.google.zxing.qrcode.encoder.QRCode;
import com.scvngr.levelup.core.ui.view.LevelUpQrCodeGenerator;
import com.scvngr.levelup.core.util.LogManager;

/**
 * Generates LevelUp QR codes using the included ZXing library.
 */
public final class ZXingCodeGenerator implements LevelUpQrCodeGenerator {

    @Override
    @Nullable
    public LevelUpQrCodeImage generateLevelUpQrCode(String qrCodeDataString) {
        LevelUpQrCodeImage result = null;

        try {
            result = getQrCodeBitmapOrThrow(qrCodeDataString);

            if (Constants.ASYNC_BACKGROUND_TASK_DELAY_ENABLED) {
                SystemClock.sleep(Constants.ASYNC_BACKGROUND_TASK_DELAY_MS);
            }
        } catch (WriterException e) {
            LogManager.e("Could not generate QR code", e);
        }

        return result;
    }

    /**
     * Generate a QR code from a given string (using the ZXing default encoding, ISO-8859-1).
     * 
     * @param qrCodeDataString String to encode.
     * @return an immutable bitmap of the QR code that was generated.
     * @throws WriterException if there was a problem generating the bitmap
     */
    private static LevelUpQrCodeImage getQrCodeBitmapOrThrow(final String qrCodeDataString)
            throws WriterException {
        final MultiFormatWriter writer = new MultiFormatWriter();

        /*
         * We end up encoding the QR twice, first encode the string and get the minimum size we can
         * store the result in. Once we have the proper size, we can then generate the minimal
         * bitmap that can be scaled on the code screen. This allows us to reduce the in-memory size
         * of the QR cache significantly.
         */
        final QRCode code = Encoder.encode(qrCodeDataString, ErrorCorrectionLevel.L);
        final BitMatrix result =
                writer.encode(qrCodeDataString, BarcodeFormat.QR_CODE,
                        code.getMatrix().getHeight(), code.getMatrix().getWidth(), null);
        final int width = result.getWidth();
        final int height = result.getHeight();
        final int[] pixels = new int[width * height];

        // All are 0, or black, by default.
        // The output bitmap is rotated 180° from the input.
        for (int y = 0; y < height; y++) {
            final int offset = y * width;
            for (int x = 0; x < width; x++) {
                if (result.get(x, y)) {
                    pixels[width * height - 1 - (offset + x)] = Color.BLACK;
                } else {
                    pixels[width * height - 1 - (offset + x)] = Color.WHITE;
                }
            }
        }

        // The return is [x,y].
        int[] topLeft = result.getTopLeftOnBit();

        /*
         * The target size should be a constant, but ZXing doesn't expose it anywhere, so it's
         * computed from the result. However, one can safely assume that targets in a given image
         * are all the same size and square.
         */
        int targetSize = 0;

        /*
         * The code margin should be the same on all sides, but just to be safe we use the computed
         * value when computing the target size.
         */
        int codeMargin = topLeft[0];
        int topMargin = topLeft[1];

        // Start at the top left "on" bit, then scan for the first "off" bit.
        for (int x = codeMargin; x < width; x++) {
            if (!result.get(x, topMargin)) {
                targetSize = x - codeMargin;
                break;
            }
        }

        /*
         * This returns an immutable bitmap, which is important for thread safety.
         */
        LevelUpQrCodeImage codeBitmap =
                new LevelUpQrCodeImage(Bitmap.createBitmap(pixels, width, height,
                        Bitmap.Config.RGB_565), targetSize, codeMargin);

        return codeBitmap;
    }
}

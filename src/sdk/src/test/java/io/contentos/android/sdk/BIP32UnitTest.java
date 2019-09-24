package io.contentos.android.sdk;

import org.junit.Test;
import static org.junit.Assert.*;
import io.contentos.android.sdk.crypto.Key;
import io.contentos.android.sdk.encoding.WIF;
import io.contentos.android.sdk.prototype.Type;

public class BIP32UnitTest {
    @Test
    public void bip32_isCorrect() {
        int count = vectors.length / 3;
        for (int i=0; i<count; i++) {
            int offset = i * 3;
            String mnemonic = vectors[offset];
            String expected_pubkey_wif = vectors[offset + 1];
            String expected_prikey_wif = vectors[offset + 2];

            Type.private_key_type privateKey = Key.generateFromMnemonic(mnemonic);
            Type.public_key_type publicKey = Key.publicKeyOf(privateKey);
            assertEquals(expected_prikey_wif, WIF.fromPrivateKey(privateKey));
            assertEquals(expected_pubkey_wif, WIF.fromPublicKey(publicKey));
        }
    }

    private static final String[] vectors = {
            "dance bleak matter hurt mule prefer casual mad actress tragic click badge involve suspect close wink fortune base genuine record burst order awkward west",
            "COS5Uk4F1bk91EKSN4NkS2fokDi1SQ8uin5bbwycSPHw4aCopLZNX",
            "4MernMvhJ4YJ3JeQ4rtmKBpnjwC8xG9mAim9TwMnofL9hBEPvc",

            "trial meadow film under spot flower color lift dance switch verify defy cram coil injury grit ancient allow promote catalog once response clean fold",
            "COS5A189FNRm8u1VqDEZZAFUwdEnNZqVfh4DK8DiTAKSpcy8SUNYF",
            "4FkUfdk3fM3fBsxSdfZ1WjbnhvnMoeJKHFK3EnCweDygsmmDrp",

            "flavor identify people cup squirrel sad ostrich common inch toast paddle purse bid ceiling stem clay three pudding arctic wing thrive approve memory lumber",
            "COS7SC6RkXvvcQ2KgJPigm4oZYEGW6qN6QpRpiHC3uyiwdirWHgY4",
            "4CH5U9Q4xsbnNC8sdTCv4pcu1r8fHGVWdNwN5H86WpgXfsw3cW",

            "scatter pole curve maximum harsh praise alien lamp cook target dust improve giggle buffalo disease motion what relief bulb pool kit mandate mixed damage",
            "COS6YSaPYX8krmC3FexdcgULNrKJQ69uttUz7SXBXSzBqf3HE6nSC",
            "4gsGAGL7A3wtys7kwbgnDjqEgTqPk3KcEJLAKhdHjVxgyp2eks",

            "around lecture mystery key retire audit combine episode guard bamboo solid mosquito combine mango clay foot prepare flip safe gas misery fever scissors industry",
            "COS7FmWmosEoEWosj9bi7E28fRoWTsz8v282HAMRG191QKtTp9mPq",
            "4YAgTcBY59Bq6eEpZyh3ZNnfWGaYUEdPtCSjkkmRiJyj7uNVzt",

            "syrup ice one bag obscure will syrup deliver payment ability grain half never often fan gun caught noodle urge begin bronze canal enact awake",
            "COS675qi9hUyvHSzPSPKjoZZa64dBPrzoxjiN3YEnMU3WUgrTYtJg",
            "3CR8ZM4BEPvtJdqLXwysL1ewPigbbQdo1d7ykMPAMFPmi7wn6X",

            "wool brain base sad sphere garbage end practice master tooth budget expect plate dust music fine crane bird exile leave isolate select walnut story",
            "COS6SNiqWthVdpRaYK5Exu7D4xy6GrYPD2bmKKWPoWd2GFdokefob",
            "3WQxqvU1xAhAHEAwzkZYLnL9xCjniy8JNxashJYfPkh7uzEaep",

            "butter ticket stadium sunny arrange emotion rib gadget slush fortune crawl spell win fix either blood script amazing west lazy present best agree stereo",
            "COS7ue9V6B27RGyGeoiNZBmWkXveqAeqnibUASNGmBG47oyBfamYr",
            "4Wk27auR5sGBiRh9F3a9D4HnympCpzJfjxtYC2dxiMs2PSxNQx",

            "lecture skin empower license winner sadness mass hockey sibling shop student joke calm picnic smooth simple humor doctor can road toilet prefer verify column",
            "COS74PkwHVxrDYLLwKGjKc7zSguFJVcp4G9o1QgY3rWL8CJ3R2tsb",
            "2z8tRpTc4F8o8DvKckhzJJAqsicoAvCLu3PB5XQg2izL8Tbn5g",

            "like pigeon deputy ceiling edit alcohol bench move welcome waste filter hood soft quit draw educate jewel arch scheme point always bachelor attend fever",
            "COS6GSePms9tpFEgcJCrR4uqKhXbuw7mioRvSKYHhtXWNy7CqXVKB",
            "36sE6fmfGooXqpNxQcNNyP5innMs1aZhJEPxmGrntJWtaNBaU1",
    };
}

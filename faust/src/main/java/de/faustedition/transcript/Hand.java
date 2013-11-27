package de.faustedition.transcript;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;

import java.util.List;
import java.util.Set;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class Hand {
    private static final Splitter DESCRIPTION_SPLITTER = Splitter.on("_").omitEmptyStrings();
    private static final Set<String> SCRIBES = ImmutableSet.of("ec", "gt", "gh", "jo", "ri", "sc");

    private final String scribalHand;
    private final String material;
    private final String type;

    public static Hand fromDescription(String desc) {
        final List<String> handComponents = DESCRIPTION_SPLITTER.splitToList(desc);
        final int handComponentNum = handComponents.size();
        return new Hand(
                handComponentNum > 0 ? handComponents.get(0) : "",
                handComponentNum > 1 ? handComponents.get(1) : "",
                handComponentNum > 2 ? handComponents.get(2) : ""
        );
    }

    private Hand(String scribalHand, String material, String type) {
        this.scribalHand = scribalHand;
        this.material = material;
        this.type = type;
    }

    public String getScribalHand() {
        return scribalHand;
    }

    public String getMaterial() {
        return material;
    }

    public String getType() {
        return type;
    }

    public boolean isScribe() {
        return SCRIBES.contains(scribalHand);
    }
}

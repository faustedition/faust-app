package de.faustedition.index;

import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.StopwordAnalyzerBase;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.IOException;
import java.io.Reader;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class CustomAnalyzer extends StopwordAnalyzerBase {

    public CustomAnalyzer() {
        super(Index.LUCENE_VERSION);
    }

    @Override
    protected TokenStreamComponents createComponents(String fieldName, Reader reader) {
        final StandardTokenizer src = new StandardTokenizer(Index.LUCENE_VERSION, reader);
        return new TokenStreamComponents(src, wrap(src));
    }

    public static TokenStream wrap(TokenStream ts) {
        ts = new CustomTokenFilter(ts);
        ts = new LowerCaseFilter(Index.LUCENE_VERSION, ts);
        return ts;
    }

    private static class CustomTokenFilter extends TokenFilter {

        private final CharTermAttribute charTermAttribute;

        private CustomTokenFilter(TokenStream input) {
            super(input);
            this.charTermAttribute = addAttribute(CharTermAttribute.class);
        }

        @Override
        public boolean incrementToken() throws IOException {
            if (!input.incrementToken()) {
                 return false;
            }

            final int cl = charTermAttribute.length();
            final StringBuilder buf = new StringBuilder(cl);

            for (int cc = 0; cc < cl; cc++) {
                final char currentChar = charTermAttribute.charAt(cc);
                switch (currentChar) {
                    case '\u017f':
                        // substitute "Langes s"
                        buf.append('s');
                        break;
                    case '\u0304':
                    case '\u0305':
                        // resolve "Geminationsstriche"
                        int next = cc + 1;
                        if (next < cl) {
                            buf.append(charTermAttribute.charAt(next));
                        }
                        break;
                    default:
                        switch (Character.getType(currentChar)) {
                            case Character.CONNECTOR_PUNCTUATION:
                            case Character.DASH_PUNCTUATION:
                            case Character.END_PUNCTUATION:
                            case Character.FINAL_QUOTE_PUNCTUATION:
                            case Character.INITIAL_QUOTE_PUNCTUATION:
                            case Character.OTHER_PUNCTUATION:
                            case Character.START_PUNCTUATION:
                                // filter punctuation
                                break;
                            default:
                                buf.append(currentChar);
                        }
                }
            }

            charTermAttribute.setEmpty().append(buf);

            return true;
        }
    }
}

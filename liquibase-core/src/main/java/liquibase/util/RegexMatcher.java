package liquibase.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Check that a text matches an array of regular expressions.<br/>
 *
 * @author lujop
 */
public class RegexMatcher {
    private String text;
    private Pattern []patterns;
    private boolean allMatched;
    /**
     * Constructs the matcher
     * @param text Text to search for mathces
     * @param regexToMatch Regex to match
     */
    public RegexMatcher(String text,String [] regexToMatch) {
        assert (text != null) && (regexToMatch != null);

        this.text=text;
        patterns=new Pattern[regexToMatch.length];
        for(int i=0;i<regexToMatch.length;i++) {
            patterns[i]=Pattern.compile(regexToMatch[i],Pattern.CASE_INSENSITIVE|Pattern.MULTILINE);
        }

        allMatched=checkMatchingInSequentialOrder();
    }

    private boolean checkMatchingInSequentialOrder() {
        int index=0;
        for(Pattern p:patterns) {
            Matcher m=p.matcher(text.substring(index));
            if(!m.find())
                return false;
            else
                index+=m.end();
        }
        return true;
    }

    public boolean allMatchedInSequentialOrder() {
        return allMatched;
    }


}

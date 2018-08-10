package com.alphawang.diff.format;

import com.alphawang.diff.DiffResult;
import com.alphawang.diff.Difference;
import com.google.common.base.Joiner;

import java.util.Map;

import static com.alphawang.diff.util.ReflectionUtils.isSimpleClass;

public class DiffResultFormatter {
    
    private static final String NEW_LINE = "\n";
    private static final String EMPTY = "";
    
    public static String format(DiffResult diffResult) {
        if (diffResult == null || !diffResult.hasDifference()) {
            return null;
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append(formatIdentities(diffResult));
        sb.append(NEW_LINE);
        sb.append(formatDifferences(diffResult));
        
        return sb.toString();
    }
    
    public static String formatIdentities(DiffResult diffResult) {
        Map identities = diffResult.getIdentities();
        if (identities == null || identities.size() <= 0) {
            return "no-identities";
        }
        return Joiner.on(", ").withKeyValueSeparator(" = ").join(identities);
    }
    
    public static String formatDifferences(DiffResult diffResult) {
        Map<String, Difference> differenceMap = diffResult.getDifferences();
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Difference> entry : differenceMap.entrySet()) {
            sb.append(formatDifference(entry.getValue())).append(NEW_LINE);
        }
        return sb.toString();
    }
    
    private static String formatDifference(Difference difference) {
        if (difference == null) {
            return null;
        }

        return getSummaryMsg(difference) + getDetailMsg(difference);
    } 
    
    private static String getSummaryMsg(Difference difference) {
        return new StringBuilder()
            .append("[").append(difference.getPath()).append("] ")
            .append(difference.getType())
            .append(" - ")
            .append(defaultString(difference.getSummary()))
            .toString();
    }
    
    private static String getDetailMsg(Difference difference) {
        if (isSimpleClass(difference.getLeftValue()) || isSimpleClass(difference.getRightValue())) {
            return new StringBuilder()
                .append(NEW_LINE)
                .append(" [L] ").append(difference.getLeftValue())
                .append(NEW_LINE)
                .append(" [R] ").append(difference.getRightValue())
                .toString();
        }
        
        return EMPTY;
    }
    
    private static String defaultString(String msg) {
        return msg == null ? EMPTY : msg;
    }
}

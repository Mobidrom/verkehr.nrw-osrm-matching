package nrw.mobidrom.verkehr.service.osrm.parser;

import org.json.JSONArray;
import org.json.JSONException;

public class ParsingHelper {

    public static int[] getIntArray(JSONArray jsonArray) throws JSONException {
        int[] res = new int[jsonArray.length()];
        for (int i = 0; i < jsonArray.length(); i++) {
            res[i] = jsonArray.getInt(i);
        }
        return res;
    }

    public static double[] getDoubleArray(JSONArray jsonArray) throws JSONException {
        double[] res = new double[jsonArray.length()];
        for (int i = 0; i < jsonArray.length(); i++) {
            res[i] = jsonArray.getDouble(i);
        }
        return res;
    }

    public static long[] getLongArray(JSONArray jsonArray) throws JSONException {
        long[] res = new long[jsonArray.length()];
        for (int i = 0; i < jsonArray.length(); i++) {
            res[i] = jsonArray.getLong(i);
        }
        return res;
    }
}

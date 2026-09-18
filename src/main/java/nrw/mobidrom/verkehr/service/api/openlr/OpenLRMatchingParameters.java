package nrw.mobidrom.verkehr.service.api.openlr;

import java.util.ArrayList;
import java.util.List;

public class OpenLRMatchingParameters {

    private static final List<OpenLRMatchingParameter> parameterList;

    static {
        parameterList = new ArrayList<OpenLRMatchingParameter>();
        parameterList.add(new OpenLRMatchingParameter(false, 0, 0, true));
        parameterList.add(new OpenLRMatchingParameter(false, 0, 0, false));
        parameterList.add(new OpenLRMatchingParameter(false, 2, 0, true));
        parameterList.add(new OpenLRMatchingParameter(false, 2, 0, false));
        parameterList.add(new OpenLRMatchingParameter(false, 2, 20, true));
        parameterList.add(new OpenLRMatchingParameter(false, 2, 20, false));
        parameterList.add(new OpenLRMatchingParameter(true, 0, 0, true));
        parameterList.add(new OpenLRMatchingParameter(true, 0, 0, false));
        parameterList.add(new OpenLRMatchingParameter(true, 20, 20, true));
        parameterList.add(new OpenLRMatchingParameter(true, 20, 20, false));
    }

    public static List<OpenLRMatchingParameter> getParameterlist() {
        return parameterList;
    }

    public static int size() {
        return parameterList == null ? -1 : parameterList.size();
    }

}
